# Security Re-Audit — Verificação das correções C1–C4, H1–H5

**Data:** 2026-09-26 · **Modo:** Security Reviewer (verificação independente) · **Escopo:** correções do `PRODUCTION_AUDIT_aegispatrimonio_2026-09-26.md`
**Método:** auditoria estática read-only (leitura de código + grep). Nenhum fix aplicado (Segregação de Responsabilidades).

---

## 1. Tabela de veredictos

| Achado original | Veredicto | Evidência principal |
|---|---|---|
| **C1** Segredos hardcoded | **FECHADO** (com residuais LOW) | [`application.properties:43`](src/main/resources/application.properties:43) `jwt.secret=${JWT_SECRET}` sem default; [`application-prod.properties:2-4`](src/main/resources/application-prod.properties:2) sem credenciais; [`docker-compose.yml:14-16`](docker-compose.yml:14) `${SPRING_DATASOURCE_*}`/`${JWT_SECRET}` sem fallback; [`k8s/aegis-app.yaml:34-37`](k8s/aegis-app.yaml:34) placeholders; [`JwtSecretValidator.java:22-23`](src/main/java/br/com/aegispatrimonio/config/JwtSecretValidator.java:22) fail-fast; [`JwtService.java:24`](src/main/java/br/com/aegispatrimonio/security/JwtService.java:24) `@Value("${jwt.secret}")` sem default |
| **C2** Filter chain permissive | **FECHADO** (com ressalvas — ver N1, N2) | [`SecurityConfig.java:91`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:91) `anyRequest().authenticated()`; `/api/**` authenticated (L73); actuator: health público (L76), resto `hasRole("ADMIN")` (L77); [`application.properties:34`](src/main/resources/application.properties:34) `show-details=when_authorized` |
| **C3** ddl-auto em prod | **FECHADO** | [`application-prod.properties:8`](src/main/resources/application-prod.properties:8) `ddl-auto=none`; [`k8s/aegis-app.yaml:16`](k8s/aegis-app.yaml:16) `SPRING_JPA_HIBERNATE_DDL_AUTO: "none"`; [`V15__create_alertas_table.sql:7`](src/main/resources/db/migration/V15__create_alertas_table.sql:7) `CREATE TABLE IF NOT EXISTS alertas` cobre a tabela que o Hibernate criava |
| **C4** Logout sem revogação | **PARCIAL** (ver N3) | [`TokenDenylistService.java:31-38`](src/main/java/br/com/aegispatrimonio/security/TokenDenylistService.java:31) revoke com eviction; [`JwtService.java:48-51`](src/main/java/br/com/aegispatrimonio/security/JwtService.java:48) rejeita revogados; [`AuthController.java:116-135`](src/main/java/br/com/aegispatrimonio/controller/AuthController.java:116) logout 204 idempotente, valida o token (parse) antes de revogar — tokens forjados não entram na denylist (sem DoS de memória) |
| **H1** RBAC granular | **PARCIAL** (ver N4, N5) | 9 controllers migrados a `@permissionService` (ex.: [`ManutencaoController.java:38`](src/main/java/br/com/aegispatrimonio/controller/ManutencaoController.java:38)); [`V16__seed_granular_permissions.sql:19-128`](src/main/resources/db/migration/V16__seed_granular_permissions.sql:19) seeda os pares com `context_key` coerente (só DEPARTAMENTO:CREATE e LOCALIZACAO:CREATE recebem `filialId`, idêntico ao que os controllers passam); Role/Permission/Group/HealthCheck/AuditController./logs mantêm `hasRole('ADMIN')` ([`RoleController.java:21`](src/main/java/br/com/aegispatrimonio/controller/RoleController.java:21), [`AuditController.java:31`](src/main/java/br/com/aegispatrimonio/controller/AuditController.java:31)) |
| **H2** Tenant filter Manutenção/Movimentação | **PARCIAL** (ver N6) | Listagens e operações por ID filtram por filial: [`ManutencaoService.java:99-106`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:99), [`ManutencaoService.java:207-215`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:207), [`MovimentacaoService.java:48-124`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:48), [`MovimentacaoService.java:193-201`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:193) |
| **H3** TenantContext null no Dashboard | **FECHADO** | [`DashboardService.java:38-41`](src/main/java/br/com/aegispatrimonio/service/DashboardService.java:38) lança `IllegalArgumentException` (→400) quando `TenantContext.getFilialId() == null`; frontend sempre envia `X-Filial-ID` após login ([`api.js:70-72`](frontend/src/services/api.js:70), [`LoginView.vue:98`](frontend/src/views/LoginView.vue:98) seta `currentFilial`) |
| **H4** Token em logs (frontend) | **FECHADO** | Grep por `console.*token` em `frontend/src`: apenas `localStorage.setItem/getItem` operacionais; nenhum log de valor de token |
| **H5** 401 global + logout server-side | **FECHADO** | [`api.js:13-18`](frontend/src/services/api.js:13) 401 → `clearSession()` + redirect `/login` (com guarda anti-loop); [`api.js:103-112`](frontend/src/services/api.js:103) `logout()` chama `POST /auth/logout` e limpa sessão mesmo em falha |

---

## 2. Findings novos / residuais

### N1 — Swagger/OpenAPI público em produção — **MEDIUM**
- **Evidência:** [`SecurityConfig.java:79-80`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:79) — `/swagger-ui/**` e `/v3/api-docs/**` com `permitAll()`, sem gate de profile.
- **Impacto:** disclosure completo da superfície de API (endpoints, DTOs, enums) para atacante não autenticado em prod. Não é RCE, mas facilita recon.
- **Fix area:** Backend Specialist — restringir swagger/api-docs a `hasRole('ADMIN')` ou desabilitar via `springdoc.api-docs.enabled=false` no profile prod.

### N2 — `/error/**` público — **LOW**
- **Evidência:** [`SecurityConfig.java:81`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:81).
- **Impacto:** mínimo (Spring Boot sanitiza mensagens de erro por padrão); manter apenas se o SPA precisar. Avaliar remoção.

### N3 — Denylist in-memory não compartilhada entre réplicas — **HIGH (residual de C4)**
- **Evidência:** [`TokenDenylistService.java:17-19`](src/main/java/br/com/aegispatrimonio/security/TokenDenylistService.java:17) documenta a limitação; [`k8s/aegis-app.yaml:47`](k8s/aegis-app.yaml:47) `replicas: 2`.
- **Impacto:** logout revoga o token apenas na réplica que atendeu o request; o mesmo token continua válido na outra réplica (e após restart da réplica, a denylist inteira se perde). O achado C4 fica **parcialmente fechado** em deploy k8s.
- **Pontos positivos verificados:** `ConcurrentHashMap` + eviction lazy/periódico (sem race relevante); [`AuthController.java:125`](src/main/java/br/com/aegispatrimonio/controller/AuthController.java:125) faz parse/validação do token antes de `revoke()` — tokens forjados/inválidos não poluem a denylist (DoS de memória mitigado); revogar qualquer token válido apresentado é aceitável (o portador só derruba a própria sessão).
- **Fix area:** Backend Specialist — migrar denylist para Redis/DB (ou sticky sessions), ou reduzir `jwt.expiration`.

### N4 — V16 concede a ROLE_USER pares de alta sensibilidade (FILIAL:CREATE/UPDATE/DELETE, DEPARTAMENTO:DELETE, LOCALIZACAO:DELETE, TIPO_ATIVO:DELETE) — **HIGH (widening de H1)**
- **Evidência:** [`V16__seed_granular_permissions.sql:153-165`](src/main/resources/db/migration/V16__seed_granular_permissions.sql:153) vincula **todos** os pares granulares a `ROLE_USER`, incluindo `FILIAL:DELETE`; controllers aceitam: [`FilialController.java:115`](src/main/java/br/com/aegispatrimonio/controller/FilialController.java:115) `FILIAL DELETE` sem contexto de filial.
- **Impacto:** um USER comum pode criar/alterar/**excluir filiais inteiras** e deletar departamentos/localizações/tipos de ativo — escalation claro em relação ao modelo anterior (essas operações eram `hasRole('ADMIN')` ou cobertas apenas por `ATIVO:*`). O comentário da migration ("preservando comportamento atual") não se sustenta para FILIAL/TIPO_ATIVO:DELETE.
- **Fix area:** Database Specialist — nova migration removendo da `ROLE_USER` os pares de administração estrutural (no mínimo `FILIAL:CREATE/UPDATE/DELETE`, avaliar `DEPARTAMENTO:DELETE`, `LOCALIZACAO:DELETE`, `TIPO_ATIVO:DELETE`); ou Backend Specialist se a intenção for restringir no `PermissionServiceImpl`.

### N5 — Permissões `FORNECEDOR:*` usadas pelos controllers mas não seedadas em nenhuma migration — **MEDIUM**
- **Evidência:** [`FornecedorController.java:39-93`](src/main/java/br/com/aegispatrimonio/controller/FornecedorController.java:39) usa `FORNECEDOR READ/CREATE/UPDATE/DELETE`; grep por `'FORNECEDOR'` em `src/main/resources/db/migration/*.sql` retorna **0 resultados** (V6 seeda apenas ATIVO e FUNCIONARIO; V16 não inclui FORNECEDOR).
- **Impacto:** fail-closed para não-ADMIN (negado), ADMIN passa via bypass — funcionalmente equivalente ao estado anterior, mas os `@PreAuthorize` de FORNECEDOR nunca podem ser satisfeitos por permissão real; inconsistência do modelo RBAC e armadilha para futuras concessões.
- **Fix area:** Database Specialist — seedar `FORNECEDOR:*` (decidindo o vínculo de role) em migration, ou Backend Specialist alinhar o controller.

### N6 — Bypasses residuais do tenant guard (H2) — **MEDIUM**
- **Evidência:**
  - [`ManutencaoService.java:41-50`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:41) `criar()` não verifica se o **ativo** pertence às filiais do usuário logado — valida apenas consistência solicitante↔ativo ([`ManutencaoService.java:253-259`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:253)); um USER com `MANUTENCAO:CREATE` pode abrir manutenção sobre ativo de filial alheia.
  - [`MovimentacaoService.java:35-45`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:35) `criar()` idem — sem checagem `userFiliais.contains(ativo.filial)`.
  - [`ManutencaoService.java:188-196`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:188) `custoTotalPorAtivo()` sem guard de filial — USER de outra filial lê o custo total de manutenções de qualquer ativo (IDOR de leitura).
- **Fix area:** Backend Specialist — adicionar `requireAcessoFilial`-equivalente nos dois `criar()` e no `custoTotalPorAtivo()`.

### N7 — `.env` e `application-e2e.properties` versionados no git — **LOW**
- **Evidência:** `git ls-files` confirma `.env`, `frontend/.env` e `src/main/resources/application-e2e.properties` trackeados; [`application-e2e.properties:15`](src/main/resources/application-e2e.properties:15) contém JWT secret dummy (`c2VjdXJl...`); `.env` local contém `Admin123` apenas em comentário (linha 6).
- **Impacto:** baixo (dummy/comentário), mas o padrão de versionar `.env` convida a commitar valores reais no futuro.
- **Fix area:** DevOps — remover `.env` do tracking (`git rm --cached`), manter só `.env.example`; renomear o secret do e2e para placeholder via env var.

### N8 — Segredo JWT de teste idêntico em 3 arquivos — **INFO**
- **Evidência:** `c2VjdXJlLXNlY3JldC1leGFtcGxlLXNlZWt0ZXN0LXNlY3JldA==` em [`BaseIT.java:30`](src/test/java/br/com/aegispatrimonio/BaseIT.java:30), [`application-test.properties:19`](src/test/resources/application-test.properties:19), [`application-e2e.properties:15`](src/main/resources/application-e2e.properties:15).
- **Impacto:** nenhum (valor óbvio de teste, nunca usar em prod). Aceitável.

---

## 3. Recomendação de handoff

| Finding | Severidade | Handoff |
|---|---|---|
| N3 denylist multi-réplica | HIGH | Backend Specialist (Redis/DB-backed denylist) |
| N4 USER com FILIAL:DELETE etc. | HIGH | Database Specialist (migration corretiva de role_permission) |
| N5 FORNECEDOR:* não seedado | MEDIUM | Database Specialist |
| N6 bypasses tenant em `criar()`/`custoTotalPorAtivo` | MEDIUM | Backend Specialist |
| N1 swagger público | MEDIUM | Backend Specialist |
| N2 `/error/**` público | LOW | Backend Specialist |
| N7 `.env` versionado | LOW | DevOps |

**Conclusão:** C1, C2, C3, H3, H4, H5 **FECHADOS**; C4 e H1 **PARCIAIS** (N3, N4); H2 **PARCIAL** (N6). Nenhuma regressão crítica introduzida; os dois HIGH (N3, N4) são residuais/decisões de seed, não quebras das correções existentes.
