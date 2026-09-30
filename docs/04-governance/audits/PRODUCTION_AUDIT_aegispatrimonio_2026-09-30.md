# PRODUCTION_AUDIT — Aegis Patrimônio — 2026-09-30

Auditoria full-spectrum (arquitetura, segurança, performance, qualidade, testes, prontidão para produção) do estado atual do código. Este relatório **apenas reporta e prioriza** — nenhum fix foi aplicado. Cada achado deve ser delegado via `new_task` ao modo especialista correspondente, com re-verificação posterior (Segregação de Responsabilidades).

## Stack e arquitetura (contexto)

- **Backend**: Java 21, Spring Boot 3.4.2 ([`pom.xml`](pom.xml:19)), Spring Security + JWT (jjwt), OAuth2 client (Google/GitHub), JPA/Hibernate + Envers (auditoria), Flyway (16 migrações), MySQL 8, Actuator + Micrometer/Prometheus.
- **Frontend**: Vue 3 + Vite ([`frontend/src`](frontend/src)), servido embutido no JAR ([`Dockerfile`](Dockerfile:26)) ou via container nginx separado.
- **Infra**: Docker multi-stage com usuário non-root ([`Dockerfile`](Dockerfile:46)); k8s com probes, PDB e NetworkPolicy ([`k8s/aegis-app.yaml`](k8s/aegis-app.yaml)); CI GitHub Actions com gate JaCoCo ≥80% ([`.github/workflows/ci.yml`](.github/workflows/ci.yml:29)).
- **Padrão**: Controllers finos com `@PreAuthorize` granular por filial (RBAC), services transacionais, mappers dedicados, multi-tenant via header `X-Filial-ID` + ThreadLocal ([`TenantFilter.java`](src/main/java/br/com/aegispatrimonio/security/TenantFilter.java:18)).

---

## Achados por severidade

### CRITICAL

**C1 — Denylist de logout in-memory quebra com 2+ réplicas (k8s)**
- Local: [`TokenDenylistService.java`](src/main/java/br/com/aegispatrimonio/security/TokenDenylistService.java:17) (limitação documentada no próprio Javadoc) vs [`k8s/aegis-app.yaml`](k8s/aegis-app.yaml:47) (`replicas: 2`).
- Impacto: logout server-side NÃO revoga o token nas outras réplicas — token "revogado" continua válido até 8h ([`application.properties`](src/main/resources/application.properties:44)). Usuário comprometido não pode ser deslogado de fato.
- Handoff: Backend Specialist (migrar para Redis/DB) → Security Reviewer re-verifica.

**C2 — JWT sem claims de identidade/tenant e sem `jti`; revogação por token-string**
- Local: [`JwtService.generateToken()`](src/main/java/br/com/aegispatrimonio/security/JwtService.java:39) — token contém apenas `sub`/`iat`/`exp`; [`TokenDenylistService.revoke()`](src/main/java/br/com/aegispatrimonio/security/TokenDenylistService.java:31) indexa pelo token completo.
- Impacto: impossível revogar "todos os tokens de um usuário" (ex.: desativação de conta, troca de senha); mudança de permissões RBAC só efetiva após re-login; denylist cresce com strings completas de token.
- Handoff: Backend Specialist + Security Reviewer.

### HIGH

**H1 — `JwtAuthFilter` engole qualquer exceção e loga em ERROR sem stack**
- Local: [`JwtAuthFilter.doFilterInternal()`](src/main/java/br/com/aegispatrimonio/security/JwtAuthFilter.java:65) — `catch (Exception e)` com `log.error("... {}", e.getMessage())`.
- Impacto: token expirado/inválido (evento comum) gera ERROR sem stacktrace — ruído em alertas de monitoramento; erros reais de infra ficam indistinguíveis.
- Handoff: Observability Specialist (nível WARN + causa) / Backend Specialist.

**H2 — `TenantFilter` aceita qualquer `X-Filial-ID` sem validar permissão no ponto de entrada**
- Local: [`TenantFilter.java`](src/main/java/br/com/aegispatrimonio/security/TenantFilter.java:27) roda **antes** do `JwtAuthFilter` ([`SecurityConfig.java`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:98)) e faz `Long.parseLong` sem bound; a validação de acesso fica no `TenantAccessFilter` posterior — mas o contexto ThreadLocal já está setado com valor não autenticado.
- Impacto: header malicioso/inválido influencia contexto de tenant antes da autenticação; depende de cada service revalidar (risco de bypass em endpoints que confiam só no `TenantContext`).
- Handoff: Security Reviewer para confirmar superfície de bypass → Backend Specialist.

**H3 — `@Scheduled` de depreciação mensal sem lock distribuído**
- Local: [`DepreciacaoService.calcularDepreciacaoMensalAgendada()`](src/main/java/br/com/aegispatrimonio/service/DepreciacaoService.java:42) — `@Scheduled(cron)` + `@Transactional` varrendo todos os ativos.
- Impacto: com 2 réplicas (k8s), o job roda **duas vezes** no mesmo dia — depreciação duplicada = corrupção de dado financeiro. Também carrega todos os ativos ATIVOS em memória (`collect(Collectors.toList())`) sem paginação, ao contrário de [`recalcularDepreciacaoTodosAtivos()`](src/main/java/br/com/aegispatrimonio/service/DepreciacaoService.java:61) que faz batching.
- Handoff: Backend Specialist (ShedLock/lock DB + batching) → Database Specialist revisa.

**H4 — Frontend sem testes e sem gate de lint no CI**
- Local: job `frontend` em [`.github/workflows/ci.yml`](.github/workflows/ci.yml:55) só roda `npm ci` + `npm run build`; não há suíte de testes em [`frontend/src`](frontend/src).
- Impacto: regressões de UI/auth-flow (ex.: [`api.js handleResponse()`](frontend/src/services/api.js:12)) chegam direto em produção.
- Handoff: Vitest Test Engineer.

### MEDIUM

**M1 — `System.out.println` no seeder dev**
- Local: [`RealisticDataSeeder.java`](src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:41) e linha 144.
- Impacto: contorna o padrão de logging (SLF4J); perfil `dev` apenas, mas é o tipo de código que vaza para prod se o `@Profile` for removido.
- Handoff: Software Engineer (trocar por logger).

**M2 — `DevConfig` imprime token JWT de admin no log**
- Local: [`DevConfig.printDevToken()`](src/main/java/br/com/aegispatrimonio/config/DevConfig.java:37).
- Impacto: token de admin em texto plano em logs — se o perfil `dev` for ativado por engano em um ambiente compartilhado, é credencial vazada. Mitigado por `@Profile("dev")`, mas o fail-fast deveria ser mais rígido.
- Handoff: Security Reviewer avalia; Software Engineer ajusta.

**M3 — Versões dessincronizadas de jjwt**
- Local: [`pom.xml`](pom.xml:83) — `jjwt-api`/`jjwt-impl` 0.13.0, `jjwt-jackson` **0.12.5**.
- Impacto: mistura de minor versions da mesma lib pode causar incompatibilidade de runtime em parsing/serialização.
- Handoff: Software Engineer (alinhar via BOM).

**M4 — Fuzzy search carrega até 1000 candidatos e faz Levenshtein em memória**
- Local: [`AtivoService.listarTodos()`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119) (TODO documentado).
- Impacto: degrada com crescimento do parque; aceitável para MVP, precisa de índice fulltext/FTS antes de escalar.
- Handoff: Database Specialist (índice FULLTEXT MySQL) + Backend Specialist.

**M5 — `AuthController.buildResponse()` engole exceção silenciosamente**
- Local: [`AuthController.buildResponse()`](src/main/java/br/com/aegispatrimonio/controller/AuthController.java:150) — `catch (Exception e)` com fallback para lista vazia, sem log.
- Impacto: falha ao carregar filiais do usuário vira "usuário sem filiais" — diagnóstico difícil em produção.
- Handoff: Observability Specialist (log WARN com contexto).

**M6 — `application.properties` default de datasource com `allowPublicKeyRetrieval=true` e `useSSL=false`**
- Local: [`application.properties`](src/main/resources/application.properties:4).
- Impacto: default inseguro se variável de ambiente não for definida em ambientes não-prod; prod usa env vars, mas o default é perigoso.
- Handoff: DevOps/Software Engineer.

**M7 — k8s: MySQL como Deployment single-replica sem PVC declarado no manifest lido (verificar) e sem egress policy**
- Local: [`k8s/aegis-app.yaml`](k8s/aegis-app.yaml:154) (mysql Deployment), NetworkPolicy sem egress ([`k8s/aegis-app.yaml`](k8s/aegis-app.yaml:117) — pendência documentada).
- Impacto: MySQL single point of failure; egress livre contradiz postura default-deny.
- Handoff: DevOps.

### LOW

**L1 — `pom.xml` com `<name>aegispatispatrimonio</name>` (typo)**
- Local: [`pom.xml`](pom.xml:13). Handoff: Software Engineer.

**L2 — `JwtSecretValidator` valida apenas não-vazio, não a entropia/tamanho**
- Local: [`JwtSecretValidator.validate()`](src/main/java/br/com/aegispatrimonio/config/JwtSecretValidator.java:20) — aceita "abc" como secret.
- Handoff: Security Reviewer define mínimo (ex.: ≥256 bits base64) → Software Engineer implementa.

**L3 — `authInterceptor` legado morto no frontend**
- Local: [`api.js`](frontend/src/services/api.js:115) — marcado "mantido para compatibilidade" sem evidência de uso.
- Handoff: Software Engineer (remover após grep de uso).

**L4 — Arquivos de output de build commitados na raiz**
- Local: `mvn_output*.txt`, `mvn_test_output.txt`, `test-results/` (ver listagem da raiz).
- Impacto: ruído no repo; risco de vazar dados de ambiente no output.
- Handoff: Software Engineer (remover + `.gitignore`).

**L5 — `docker-compose.yml` usa a mesma senha para root MySQL e app user**
- Local: [`docker-compose.yml`](docker-compose.yml:64) — `MYSQL_ROOT_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}`.
- Impacto: app roda como root do banco (viola least privilege); dev-only, mas o padrão tende a ser copiado.
- Handoff: DevOps.

---

## Pontos positivos (evidência)

- Segurança base sólida: fail-fast de JWT secret ([`JwtSecretValidator.java`](src/main/java/br/com/aegispatrimonio/config/JwtSecretValidator.java:19)), default-deny em [`SecurityConfig.java`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:91), actuator restrito ([`SecurityConfig.java`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:76)), Swagger desligado em prod ([`application-prod.properties`](src/main/resources/application-prod.properties:21)).
- Infra madura: non-root + healthcheck no Docker ([`Dockerfile`](Dockerfile:56)), probes/PDB/NetworkPolicy no k8s, secrets out-of-band documentados ([`k8s/aegis-app.yaml`](k8s/aegis-app.yaml:26)).
- CI com gate de cobertura 80% e actions pinadas por SHA ([`.github/workflows/ci.yml`](.github/workflows/ci.yml:16)).
- Suite de testes ampla: ~70 arquivos de teste incluindo ITs de segurança, RBAC, tenant e performance (ver [`src/test`](src/test/java/br/com/aegispatrimonio)).

## Ordem de correção sugerida

1. **C1** (denylist multi-réplica) — dado financeiro/segurança, quebra o contrato de logout em prod.
2. **H3** (job duplicado) — risco de corrupção de dado financeiro com 2 réplicas.
3. **C2** (claims JWT/revogação por usuário) — habilita resposta a incidentes.
4. **H2** (validação de tenant no ponto de entrada) — confirmar superfície de bypass com Security Reviewer.
5. **H1 + M5** (higiene de logging) — Observability Specialist.
6. **H4** (testes frontend) — Vitest Test Engineer.
7. M1–M7 e L1–L5 em sequência.

Nenhum item está marcado como "concluído" — cada um só pode ser fechado pelo especialista que aplicar e verificar o fix.
