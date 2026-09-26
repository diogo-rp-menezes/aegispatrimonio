# Production Audit — Aegis Patrimônio (2026-09-26)

**Modo:** 🏭 Production Code Audit (piloto automático) · **Stack:** Spring Boot 3.4.2 / Java 21 / JPA+Envers / Flyway / MySQL · Vue 3 + Vite · Docker Compose + k8s
**Escopo:** varredura full-spectrum (arquitetura, segurança, performance, qualidade, testes, infra). Nenhum fix foi aplicado — todos os achados são handoff.

---

## 🔴 CRITICAL

### C1. Segredos hardcoded em múltiplas camadas (OWASP A02)
- JWT secret default commitado em [`application.properties:44`](src/main/resources/application.properties:44) (`jwt.secret=${JWT_SECRET:C4f9lnnEUDwCsFdRouLLP5naPLxrie8FbJ1JTh/BDKE=}`) — qualquer deployment sem env var usa uma chave pública no repositório.
- Credenciais de banco hardcoded em [`application-prod.properties:2-4`](src/main/resources/application-prod.properties:2) (`root`/`Admin123`) e default em [`application.properties:6`](src/main/resources/application.properties:6).
- Fallback inseguro no compose: [`docker-compose.yml:18`](docker-compose.yml:18) (`JWT_SECRET:-achgjghjgjh...`) e [`docker-compose.yml:17`](docker-compose.yml:17) (senha `Admin123`).
- k8s: [`k8s/aegis-app.yaml:29-30`](k8s/aegis-app.yaml:29) — Secret com `Admin123` e `JWT_SECRET: "change-me-in-prod"` em texto plano no manifesto (e MySQL root password em [`k8s/aegis-app.yaml:127`](k8s/aegis-app.yaml:127)).
- **Handoff:** Backend Specialist (externalizar sem fallback; falhar startup sem `JWT_SECRET`) + re-verificação por Security Reviewer.

### C2. `anyRequest().permitAll()` na filter chain (OWASP A01)
- [`SecurityConfig.java:78`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:78) — todo endpoint fora de `/api/**` é público. Hoje os controllers estão sob `/api`, mas qualquer endpoint novo fora do prefixo (ex.: actuator já é `permitAll` em [`SecurityConfig.java:75`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:75)) fica aberto por default. Default deve ser `denyAll`/`authenticated`.
- **Agravante:** `/actuator/**` totalmente público ([`SecurityConfig.java:75`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:75)) expõe `prometheus`, `metrics` e `health` com `show-details=when_authorized` — mas sem autenticação na chain, `when_authorized` nunca dispara para anônimos; exposição de métricas e detalhes de infra sem credencial.
- **Handoff:** Backend Specialist + Security Reviewer.

### C3. `ddl-auto=update` em produção
- [`application-prod.properties:8`](src/main/resources/application-prod.properties:8) — Hibernate gerando schema em prod, conflitando com Flyway (V1–V14) e podendo causar drift/data loss. O k8s ConfigMap sobrescreve para `none` ([`k8s/aegis-app.yaml:16`](k8s/aegis-app.yaml:16)), mas o profile `prod` via docker-compose fica com `update`.
- **Handoff:** Database Specialist.

### C4. Sem revogação/refresh de token JWT
- [`JwtService.java:33-45`](src/main/java/br/com/aegispatrimonio/security/JwtService.java:33) — token de 8h (28800000ms) sem refresh, sem denylist, sem logout server-side; token roubado é válido até expirar. `isTokenValid` não valida assinatura separadamente do parse (ok via jjwt), mas não há `jti`/revogação.
- **Handoff:** Backend Specialist + Security Reviewer.

---

## 🟠 HIGH

### H1. Autorização inconsistente entre controllers (RBAC granular vs role fixa)
- Dois modelos coexistem: `@permissionService.hasPermission(...)` (ex.: [`FornecedorController.java:39`](src/main/java/br/com/aegispatrimonio/controller/FornecedorController.java:39), [`AtivoController.java:57`](src/main/java/br/com/aegispatrimonio/controller/AtivoController.java:57)) e `hasRole('ADMIN')` fixo (ex.: [`ManutencaoController.java:38`](src/main/java/br/com/aegispatrimonio/controller/ManutencaoController.java:38), [`MovimentacaoController.java:44`](src/main/java/br/com/aegispatrimonio/controller/MovimentacaoController.java:44), [`DepreciacaoController.java:40`](src/main/java/br/com/aegispatrimonio/controller/DepreciacaoController.java:40)). Usuário com permissão granular `MANUTENCAO:CREATE` no banco não consegue criar manutenção; a tabela de permissões não é fonte única de verdade.
- **Handoff:** Backend Specialist (unificar em `permissionService`).

### H2. Isolamento de tenant ausente em Manutenção/Movimentação
- [`ManutencaoService.java:89-98`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:89) e [`MovimentacaoService.java:45-86`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:45) — listagens sem filtro por filiais do usuário (diferente de [`AtivoService.listarTodos()`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:71), que filtra). Usuário USER de uma filial vê manutenções/movimentações de todas as filiais. `TenantAccessFilter` ([`TenantAccessFilter.java:34`](src/main/java/br/com/aegispatrimonio/security/TenantAccessFilter.java:34)) só valida quando o header `X-Filial-ID` é enviado — omitir o header bypassa o controle.
- **Handoff:** Backend Specialist + Security Reviewer.

### H3. `TenantContext` estático em queries SpEL — risco de NPE/cross-tenant
- [`AtivoRepository.java:172-198`](src/main/java/br/com/aegispatrimonio/repository/AtivoRepository.java:172) — queries com `:#{T(TenantContext).getFilialId()}`; se o header não for enviado, `getFilialId()` retorna null e a query filtra por `filial.id = null` (retorna 0 rows ou comportamento imprevisível), sem erro claro. Dashboard ([`DashboardController.java:20`](src/main/java/br/com/aegispatrimonio/controller/DashboardController.java:20)) depende disso.
- **Handoff:** Backend Specialist + Database Specialist.

### H4. Frontend: token e roles em `localStorage` + guard baseado em dado client-side
- [`api.js:37`](frontend/src/services/api.js:37) e [`router/index.js:74-96`](frontend/src/router/index.js:74) — token JWT em localStorage (XSS → roubo de sessão); guard de rota lê `userRoles` do localStorage (editável pelo usuário; é só UX, a API protege — mas documentar). Sem tratamento de 401 global ([`api.js:5-9`](frontend/src/services/api.js:5) tem o redirect comentado).
- **Handoff:** Frontend Specialist + Security Reviewer.

### H5. `console.log` do token JWT no cliente
- [`api.js:49`](frontend/src/services/api.js:49) — `console.log('Adding Authorization header with token:', token)` vaza o token completo para o console em produção.
- **Handoff:** Frontend Specialist.

### H6. CI sem gates de qualidade
- [`ci.yml:24-25`](.github/workflows/ci.yml:24) roda `mvnw verify`, mas: sem checagem do `jacoco.minimum.coverage=0.80` ([`pom.xml:27`](pom.xml:27)) como gate explícito, sem lint/SCA (Dependabot/CodeQL ausentes), sem job de frontend (build/test/e2e do Vue não rodam no CI), sem build/push de imagem, sem pin de actions por SHA. [`automerge-development.yml`](.github/workflows/automerge-development.yml) auto-merge sem revisão humana visível.
- **Handoff:** DevOps + Release Audit Specialist.

---

## 🟡 MEDIUM

### M1. Performance — fuzzy search carrega até 1000 candidatos e ranqueia em memória
- [`AtivoService.java:112-152`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:112) — Levenshtein sobre 1000 nomes por request, sem cache; escala mal com crescimento do parque de ativos. Aceitável para MVP; monitorar.
- **Handoff:** Backend Specialist.

### M2. `findAllWithDetails()` sem paginação
- [`AtivoRepository.java:24-29`](src/main/java/br/com/aegispatrimonio/repository/AtivoRepository.java:24) chamado no caminho admin unpaged sem filtros ([`AtivoService.java:159-162`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:159)) — carrega todos os ativos com 4 JOINs FETCH em memória. Com dezenas de milhares de ativos, OOM/latência.
- **Handoff:** Backend Specialist + Database Specialist.

### M3. Logging de bind SQL em TRACE no default
- [`application.properties:29`](src/main/resources/application.properties:29) — `BasicBinder=TRACE` loga todos os valores de parâmetros SQL (inclui dados pessoais de funcionários) no profile default, que é o fallback de prod via compose. `show-sql=true` ([`application.properties:9`](src/main/resources/application.properties:9)) também.
- **Handoff:** Observability Specialist.

### M4. `log.error` com stacktrace em falha de login
- [`AuthController.java:71-73`](src/main/java/br/com/aegispatrimonio/controller/AuthController.java:71) — credenciais inválidas geram ERROR com stacktrace (ruído de alerta; deveria ser WARN/DEBUG). Sem rate limiting/brute-force protection no `/login` (OWASP A07).
- **Handoff:** Backend Specialist + Observability Specialist.

### M5. Arquitetura — god service e acoplamento
- [`AtivoService.java:31-67`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:31) com 13 dependências; [`PermissionServiceImpl.java:50-52`](src/main/java/br/com/aegispatrimonio/service/impl/PermissionServiceImpl.java:50) usa self-injection `@Lazy` (code smell). Controllers com regras de negócio duplicadas entre `dto/` e `dto/request`+`dto/response` (dois conjuntos paralelos de DTOs para as mesmas entidades).
- **Handoff:** Software Engineer (refactor incremental, com aprovação).

### M6. k8s sem recursos de produção
- [`k8s/aegis-app.yaml:51`](k8s/aegis-app.yaml:51) — `image: aegispatrimonio:latest` + `IfNotPresent` (imutabilidade ausente); sem HPA, sem PDB, sem NetworkPolicy, sem Ingress/TLS, MySQL single-replica sem PVC gerenciado visível ([`k8s/aegis-app.yaml:149-150`](k8s/aegis-app.yaml:149)).
- **Handoff:** DevOps.

### M7. Testes — cobertura desigual
- ITs existem para Ativo/Auth/Dashboard/Audit/Alerta/Departamento ([`src/test/java/.../controller/`](src/test/java/br/com/aegispatrimonio/controller/AtivoControllerIT.java)), mas **não há IT** para Manutencao, Movimentacao, Fornecedor, Filial, Funcionario, Localizacao, Relatorio, Depreciacao, Group/Role/Permission controllers — justamente os endpoints com autorização inconsistente (H1/H2). E2e Playwright existe ([`frontend/e2e/`](frontend/e2e/login.spec.js)) mas não roda no CI (H6). `jacoco.minimum.coverage` declarado mas sem verificação de gate visível no pipeline.
- **Handoff:** Vitest Test Engineer (frontend) + Backend/SWE (ITs Java).

### M8. `spring.jpa.show-sql=true` em test properties e `application-test.properties-bkp` versionado
- [`src/test/resources/application-test.properties:9`](src/test/resources/application-test.properties:9) e arquivo de backup commitado ([`application-test.properties-bkp`](src/test/resources/application-test.properties-bkp)) — dead code/config no VCS.
- **Handoff:** Software Engineer.

---

## 🟢 LOW

- **L1.** `console.log` de debug espalhado no frontend ([`api.js:49-52`](frontend/src/services/api.js:49)); sem logger centralizado. → Frontend Specialist.
- **L2.** `version: '3.8'` obsoleta no compose ([`docker-compose.yml:1`](docker-compose.yml:1)); MySQL exposto em `3306:3306` no host ([`docker-compose.yml:70-71`](docker-compose-compose.yml:70)) sem necessidade aparente. → DevOps.
- **L3.** `QRCodeService` engole exceção genérica em `RuntimeException` ([`QRCodeService.java:20-22`](src/main/java/br/com/aegispatrimonio/service/QRCodeService.java:20)) — perde tipo de erro. → Software Engineer.
- **L4.** `handleResponse` do frontend lança `Error` com corpo bruto do backend ([`api.js:12-13`](frontend/src/services/api.js:12)) — mensagens de erro não tratadas na UI. → Frontend Specialist.
- **L5.** Duplicação de rotas OAuth (Google aparece 2x) em [`LoginView.vue:39-56`](frontend/src/views/LoginView.vue:39). → Frontend Specialist.
- **L6.** `AntPathRequestMatcher` deprecated no Spring Security 6.3+ ([`SecurityConfig.java:68-76`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:68)). → Software Engineer.

---

## ✅ Pontos positivos (evidência)
- RBAC granular com cache + métricas Micrometer + audit log de autorização ([`PermissionServiceImpl.java:72-150`](src/main/java/br/com/aegispatrimonio/service/impl/PermissionServiceImpl.java:72)).
- Correlation ID em erros via ProblemDetail ([`ApplicationControllerAdvice.java:103-106`](src/main/java/br/com/aegispatrimonio/exception/ApplicationControllerAdvice.java:103)).
- Dockerfile multi-stage, non-root user, healthchecks actuator ([`Dockerfile:46-60`](Dockerfile:46)).
- Probes liveness/readiness no k8s ([`k8s/aegis-app.yaml:70-85`](k8s/aegis-app.yaml:70)).
- Migrations Flyway versionadas V1–V14; Envers para auditoria de entidades.
- Testcontainers + JaCoCo configurados; e2e Playwright existente.

---

## Ordem sugerida de execução (risco combinado)
1. **C1 + C2** — segredos e filter chain (Security Reviewer re-verifica após Backend Specialist).
2. **C3** — `ddl-auto=update` em prod (Database Specialist).
3. **H1 + H2 + H3** — unificar autorização e fechar bypass de tenant (Backend + Security Reviewer).
4. **C4 + M4** — token lifecycle e rate limiting no login.
5. **H5 + H4** — remover log de token; plano para 401 global no frontend.
6. **H6** — CI gates (coverage, SCA, frontend build, pin de actions) antes de qualquer deploy.
7. **M7** — ITs para os controllers sem cobertura (Manutenção/Movimentação/RBAC).
8. **M1–M8, L1–L6** — em ordem de impacto conforme crescimento do uso.

**Nenhum item está "resolvido"** — cada achado requer execução pelo especialista correspondente e re-verificação antes do fechamento.
