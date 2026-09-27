# Application Security Policies & Boundary Enforcement — Aegis Patrimônio

> **Owner:** Security/Eng Lead · **Classificação do documento:** Confidencial  
> **Versão:** 1.0 · **Status:** Draft  
> **Referência arquitetural:** `system-architecture.md` (SAD v1.0)  
> **Rastreabilidade:** Cada seção abaixo mapeia diretamente para requisitos do SAD (NFR-SEC01 a NFR-SEC05, NFR-C01 a NFR-C03, BR-01, BR-02) e gaps identificados no diagnóstico.

---

## 1. Threat Model Overview

* **Ativos a proteger:**
  - Dados patrimoniais sensíveis: cadastro de ativos (tag `LGPD_SENSITIVE` — NFR-C03), histórico de manutenções, health checks, custos agregados (`custoTotalPorAtivo`).
  - Credenciais e tokens: JWT access/refresh tokens, segredos de assinatura (HS256/RS256), chaves de criptografia AES-256 (NFR-SEC01, NFR-SEC04).
  - Trilha de auditoria imutável (WORM) — operações de escrita BR-02 (`criar`, `atualizar`, `deletar`, `aprovar`, `cancelar`, `concluir`, `iniciar`) com payload completo (NFR-SEC05, NFR-C02).
  - Dados de usuários e RBAC: `Usuario`, `Funcionario`, roles (`ADMIN`, `AUDITOR`, `GESTOR`, `OPERADOR`), permissões mapeadas (BR-01).
  - Propriedade intelectual: código-fonte Java (333 arquivos), frontend Vanilla JS (15 arquivos), queries SQL cru/nativas otimizadas.

* **Atores de ameaça considerados:**
  - **Externo malicioso:** atacante de rede explorando API REST pública (única interface de usuário — SAD §6), tentativas de bypass de autenticação/autorização, injeção SQL via parâmetros de query (stack usa SQL cru — SAD §2, §5), força bruta em endpoints de login.
  - **Insider (funcionário/operador):** escalação de privilégios via RBAC quebrado (NFR-SEC03 exige testes de acesso quebrado), acesso não autorizado a dados `LGPD_SENSITIVE`, manipulação de logs de auditoria.
  - **Agente automatizado/AI:** bots de credential stuffing, scraping de endpoints não autenticados, DoS via `checkResourceUsageAlerts` (job que processa 12k ativos em ≤30s — NFR-P04) ou `ManutencaoSpecification.build` (filtros dinâmicos complexos — complexidade 14).

* **Superfícies de ataque principais:**
  1. **API REST pública** (Frontend ↔ Backend — SAD §6): todos endpoints CRUD ativos, manutenções, health checks, alertas, autenticação, auditoria.
  2. **Autenticação/Autorização:** endpoint `/login` (emissão JWT), refresh token, validação de `jti` para revogação (blocklist).
  3. **Camada de dados:** SQL cru/JPA nativo sem ORM completo — risco de concatenação de strings em queries dinâmicas (`ManutencaoSpecification.build`).
  4. **Integrações outbound:** Identity Provider (OIDC/SAML/LDAP — Gap #3), Email/Notification Service, WORM Audit Storage (S3 API), Observability Stack (OTLP).
  5. **Frontend servido estaticamente:** XSS via inputs não sanitizados, `console.error/debug` residuais em `api.js:36,99` (diagnóstico), CSP ausente.
  6. **Scheduler in-process:** jobs `checkResourceUsageAlerts` e `updateHealthCheck` executam no mesmo processo — falha de isolamento pode afetar API.

---

## 2. Context Isolation Boundary

* **Princípio:** Cada requisição HTTP (sessão de usuário autenticado) e cada execução de job agendado deve operar em contexto isolado — sem vazamento de dados entre tenants/usuários, sem compartilhamento de `EntityManager`/`JdbcTemplate` stateful entre threads, e com `traceId`/`spanId` propagados para correlação de auditoria e tracing (SAD §10).

* **Mecanismo de enforcement:**
  - **Thread-local / MDC (Mapped Diagnostic Context):** `traceId` (W3C `traceparent`), `userId`, `roles[]`, `tenantId` (se multi-tenant futuro) injetados via `SecurityFilterChain` Spring Security e propagados para logs JSON (SAD §10) e `AuditoriaService`.
  - **Transações demarcadas por request:** `@Transactional` em services de escrita; read-replica usada apenas para queries analíticas (`@Transactional(readOnly = true)` + `RoutingDataSource`).
  - **Jobs agendados:** executam em `TaskScheduler` pool dedicado (`scheduler` thread pool — SAD §8 bulkhead), sem acesso a `SecurityContext` de usuários web; identidade de sistema (`SYSTEM_SCHEDULER`) para auditoria.
  - **Cache L1 (Caffeine — inferido):** chaves escopadas por `userId`/`role` para lookups RBAC; invalidação por `@CacheEvict` em services de escrita.

* **Validação:**
  - Testes de integração: cenários multi-usuário concorrente validando isolamento de `SecurityContext` e `EntityManager`.
  - Testes de carga (k6/Gatling — SAD §13 médio prazo): verificar ausência de vazamento de `traceId`/`userId` entre requests.
  - Auditoria de logs: amostragem automatizada verificando que cada entrada de log JSON contém `traceId`, `userId`, `service=aegis-patrimonio` (SAD §10).

---

## 3. Workspace / Filesystem Boundary Validation

* **Regra:** A aplicação **não realiza operações de filesystem arbitrárias** (upload de arquivos, leitura/escrita de arquivos locais, execução de comandos shell) no estado atual. O único acesso a disco é:
  - Logs stdout/stderr (container/VM) → coletor (Fluent Bit/Vector/Promtail) → Loki/Elasticsearch (SAD §10).
  - Escrita assíncrona em WORM Object Storage via S3 API (NFR-SEC05) — **não** filesystem local.
  - Assets estáticos frontend servidos via `src/main/resources/static` (Spring Boot) ou CDN (NFR-PO03) — somente leitura.

* **Validação de path traversal (defesa em profundidade):** Caso futuras features exijam upload/import (ex.: CSV de ativos), aplicar regra algorítmica:

```java
// Regra de referência para validação de caminho (Java NIO)
public boolean isPathAllowed(Path basePath, Path targetPath) {
    try {
        Path resolvedBase = basePath.toRealPath();
        Path resolvedTarget = targetPath.toRealPath();
        return resolvedTarget.startsWith(resolvedBase);
    } catch (IOException e) {
        return false; // falha segura
    }
}
```

* **Casos de teste obrigatórios (quando aplicável):**
  - Path traversal: `../../etc/passwd`, `..\..\windows\system32`
  - Symlinks maliciosos apontando fora do diretório base
  - Caminhos absolutos injetados via parâmetro (`/var/lib/app/uploads/../../../etc/shadow`)
  - Null bytes (`%00`) em nomes de arquivo
  - Nomes de arquivo Unicode normalizados (NFC/NFD) para bypass de validação

---

## 4. Command Execution Boundary

* **Aplicabilidade:** **Não aplicável no estado atual.** A stack não executa comandos de SO, scripts shell, nem processos filhos (diagnóstico: 333 arquivos `.java`, 15 `.js`; sem dependências de `ProcessBuilder`, `Runtime.exec`, `child_process`, Tauri/Rust). Jobs rodam in-process via Spring `TaskScheduler`.

* **Se futuramente houver execução de comandos (ex.: geração de relatórios PDF via binário externo):**
  - **Allowlist explícita:** apenas binários assinados e versionados no container (ex.: `wkhtmltopdf`, `pandoc`).
  - **Timeout máximo:** 30s por execução (alinhado ao hard limit do job `checkResourceUsageAlerts` — NFR-P04).
  - **Isolamento de rede:** container sem egresso externo; apenas loopback para app.
  - **Auditoria:** todo comando logado com `command`, `args` (sanitizados), `exitCode`, `durationMs`, `actor` (`SYSTEM_SCHEDULER` ou `userId`), `traceId`.

---

## 5. Data Protection Rules

| Classificação | Exemplos no Aegis Patrimônio | Criptografia em Repouso | Criptografia em Trânsito | Mascaramento/Anonimização (Non-Prod) | Retenção & Descarte |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Restrito (PII/Sensível)** | `Funcionario` (CPF, dados bancários, saúde), `Usuario` (credenciais, MFA secrets), `AuditoriaLog` (payload completo de operações BR-02), ativos com tag `LGPD_SENSITIVE` (NFR-C03) | **AES-256** — volume do DB criptografado (managed disk encryption / TDE — NFR-SEC01). Chaves gerenciadas no Secret Manager (Vault/AWS Secrets Manager — NFR-SEC04). | **TLS 1.2+ (preferencial 1.3)** em todas conexões: app↔DB, app↔WORM, app↔IdP, app↔Observability, LB↔Client (NFR-SEC01). | **Obrigatório** em dev/staging/homolog: anonimização irreversível (hash + salt) de CPF, email, nomes; substituição de valores monetários por ruído estatístico. Dados de auditoria **não** anonimizados (SOX 7 anos — NFR-C02). | **SOX:** 7 anos imutáveis em WORM (NFR-C02). **LGPD:** direito ao esquecimento — exclusão lógica + anonimização em `Funcionario`, `Usuario`, `AuditoriaLog` via endpoint administrativo (NFR-C01). Descarte seguro: cripto-shredding (destruição de chaves) + verificação de ausência em backups. |
| **Confidencial** | `Ativo` (metadados, localização, valor), `Manutencao` (histórico, custos), `HealthCheck` (métricas de recursos), `Configuracao` (parâmetros de alerta, thresholds) | AES-256 (mesmo volume do DB) | TLS 1.2+ | Pseudonimização: IDs substituídos por UUIDs determinísticos; valores monetários arredondados. | 7 anos (alinhado SOX) ou até solicitação LGPD. Backup diário (RPO ≤ 24h — NFR-A03) com mesma criptografia. |
| **Interno** | Logs de aplicação (exceto auditoria), métricas Prometheus, traces OpenTelemetry, cache Caffeine/Redis (se provisionado) | Criptografia de disco do host/container (padrão cloud provider) | TLS 1.2+ para exportação (OTLP, Prometheus scrape) | Não requerido (já agregados/sem PII direta). | Retenção observabilidade: 30 dias quentes (Loki/Tempo), 1 ano frio (S3/Glacier). Métricas: 13 meses (Prometheus/Thanos). |
| **Público** | Assets frontend (JS, CSS, imagens), OpenAPI spec, health check público (`/actuator/health/liveness`) | Não aplicável (arquivos estáticos versionados) | TLS 1.2+ (HSTS, certificate pinning — médio prazo) | Não aplicável. | Imutáveis por versionamento (cache `max-age=31536000, immutable` — NFR-PO03). |

* **Notas críticas:**
  - **Chaves de criptografia/JWT:** rotação a cada 90 dias via Secret Manager (NFR-SEC04). `jti` em JWT para revogação (blocklist em cache/Redis).
  - **Prepared statements obrigatórios:** stack usa SQL cru — **revisão de código obrigatória** para proibir concatenação de strings em queries (NFR-SEC03). Usar `JdbcTemplate` com `?` bind parameters ou JPA Criteria/`@Query` com parâmetros nomeados.
  - **WORM Audit Storage:** bucket com **Object Lock** (Compliance Mode) + Versioning habilitado. Policy `Deny DeleteObject` sem `LegalHold` bypass. Replicação cross-region (SAD §9).

---

## 6. Authentication & Authorization

* **Mecanismo de auth:** **JWT Stateless** (HS256 ou RS256 — definir na Sprint 0 com Secret Manager).
  - Access token: expiração **≤ 1 hora** (NFR-SEC02).
  - Refresh token: rotação a cada uso (refresh token rotation), armazenado em HttpOnly Secure SameSite=Strict cookie **ou** no frontend com armazenamento seguro (IndexedDB + Crypto API) — decisão pendente com Frontend Lead.
  - `jti` (JWT ID) único por token para revogação via blocklist (cache L1 Caffeine TTL = expiração do access token; L2 Redis se provisionado).
  - Claims obrigatórios: `sub` (userId), `roles[]` (array de strings: `ADMIN`, `AUDITOR`, `GESTOR`, `OPERADOR`), `traceId` (correlação), `exp`, `iat`, `iss` (`aegis-patrimonio`), `aud` (`aegis-patrimonio-api`).

* **Modelo de autorização:** **RBAC estrito** (BR-01) — matriz de permissões por role:

| Recurso / Ação | ADMIN | AUDITOR | GESTOR | OPERADOR |
| :--- | :---: | :---: | :---: | :---: |
| **Ativos** (CRUD) | ✅ | 👁️ (read) | ✅ (criar, ler, atualizar) | 👁️ (ler) |
| **Manutenções** (CRUD + aprovar/cancelar/concluir/iniciar) | ✅ | 👁️ | ✅ | ✅ (criar solicitação, ler próprias) |
| **Health Checks** (ler, disparar) | ✅ | 👁️ | ✅ | ✅ (ler próprios ativos) |
| **Alertas** (ler, reconhecer, configurar thresholds) | ✅ | 👁️ | ✅ | 👁️ (ler alertas próprios ativos) |
| **Usuários/Roles** (CRUD, atribuir roles) | ✅ | ❌ | ❌ | ❌ |
| **Auditoria** (ler logs, exportar) | ✅ | ✅ | ❌ | ❌ |
| **Configurações** (ler, alterar) | ✅ | ❌ | 👁️ (ler) | ❌ |
| **LGPD: Direito ao esquecimento** (endpoint admin) | ✅ | ❌ | ❌ | ❌ |

* **Enforcement técnico:**
  - `@PreAuthorize("hasRole('ADMIN')")` / `@PreAuthorize("hasAnyRole('GESTOR','OPERADOR')")` em **Controllers e Services** (defesa em profundidade).
  - `SecurityFilterChain` valida JWT assinatura, expiração, `jti` não revogado, `iss`/`aud` corretos.
  - Testes de acesso quebrado (NFR-SEC03): suite automatizada (Spring Security Test + MockMvc) cobrindo todas combinações role×endpoint×método HTTP.

* **Política de senha/MFA:**
  - **Senha:** mínimo 12 chars, 1 maiúscula, 1 minúscula, 1 número, 1 especial; bloqueio após 5 tentativas falhas (15 min); histórico últimas 12 senhas; rotação não forçada (NIST 800-63B) — exceto se vazamento detectado.
  - **MFA obrigatório para ADMIN** (NFR-SEC02, BRD#7): TOTP (RFC 6238) via app autenticador (Google Authenticator, Authy, Microsoft Authenticator) **ou** WebAuthn/FIDO2 (chave de segurança hardware). Integração com IdP corporativo (Keycloak/Azure AD/Okta — Gap #3) provê MFA nativo; app valida claim `amr` (Authentication Methods References) contendo `mfa`.

* **Gestão de sessão:**
  - Stateless: sem sessão server-side. Revogação via blocklist `jti` (cache) + rotação de chave de assinatura (kid rotation).
  - Logout: invalida access token (adiciona `jti` à blocklist) + revoga refresh token (deleta do store).
  - Concorrência: limite configurável de sessões ativas por usuário (padrão: 5) — refresh tokens armazenados com `userId` + `deviceId` hash.

---

## 7. Secrets Management

* **Armazenamento:** **HashiCorp Vault** ou **AWS Secrets Manager** (NFR-SEC04, Gap #4) — **nunca** em código-fonte, `application.yml`, variáveis de ambiente em plaintext, Dockerfile, ou CI/CD logs.
  - Segredos gerenciados: JWT signing key (rotacionada), DB password, WORM storage credentials (access/secret key), IdP client secret, TLS certificate/key, chaves de criptografia AES-256 (DEK/KEK), email/SMTP credentials.

* **Rotação:** **Cadência de 90 dias** (NFR-SEC04) — automatizada via Vault rotation policies / AWS Secrets Manager rotation Lambda.
  - JWT signing key: rotação com **kid (Key ID)** no header JWS; overlap de 24h aceitando ambos `kid` antigo e novo para evitar invalidação abrupta de tokens válidos.
  - DB password: rotação com grace period (HikariCP reconecta com nova credencial via `DataSource` refresh — Spring Cloud Config / Vault database secrets engine).
  - Certificados TLS: renovação automática (Let's Encrypt / ACME ou CA corporativa) 30 dias antes da expiração; alerta se < 30 dias (NFR-O04).

* **Acesso:** **Princípio do menor privilégio**.
  - App runtime: role/identity com permissão `read` apenas nos secrets específicos (path-based policy no Vault: `secret/data/aegis/patrimonio/prod/*`).
  - CI/CD pipeline: token de curta duração (TTL 1h) com permissão `read` apenas para secrets de build (ex.: Docker registry credentials, SonarQube token) — **não** secrets de runtime/produção.
  - Desenvolvedores: acesso **zero** a secrets de produção. Secrets de dev/staging em namespaces separados (`secret/data/aegis/patrimonio/dev/*`).

* **Injeção na aplicação:** Spring Cloud Vault / AWS Secrets Manager integration — secrets injetados como properties no bootstrap (`bootstrap.yml`) ou via `EnvironmentPostProcessor`. **Nenhum segredo em imagem Docker** (multi-stage build: secrets apenas em runtime via volume/CSI driver ou env vars injetadas pelo orchestrator).

---

## 8. Input Validation & Output Encoding

* **Validação de entrada (allowlist, não denylist):**
  - **Camada Controller:** Bean Validation (`@Valid` + constraints: `@NotNull`, `@Size`, `@Pattern`, `@Email`, `@PastOrPresent`, `@FutureOrPresent`, constraints custom `@ValidCpf`, `@ValidTagPatrimonio`).
  - **Camada Service:** Re-validação de regras de negócio (ex.: `ativoId` existe, usuário tem permissão no ativo, `dataInicio <= dataFim`).
  - **Camada Repository/Data:** **Prepared statements obrigatórios** — zero concatenação de strings em SQL. `ManutencaoSpecification.build` (complexidade 14) deve usar `CriteriaBuilder` / `Predicate` com parâmetros bind, **nunca** interpolação de valores no SQL string.
  - **Frontend (Vanilla JS):** Validação client-side espelhando regras server-side (UX) — **nunca** substitui validação server-side. Sanitização de inputs antes de renderizar no DOM (DOMPurify ou `textContent` em vez de `innerHTML`).

* **Prevenção de injeção:**
  - **SQL Injection:** Prepared statements / JPA Criteria / `@Query` com parâmetros bind (NFR-SEC03). Code review checklist item: "Nenhuma query construída via concatenação/string interpolation".
  - **Command Injection:** N/A (sem execução de comandos — §4).
  - **Prompt Injection:** N/A (sem integração LLM no estado atual).
  - **LDAP Injection:** Se/quando integrar LDAP direto (Gap #3), usar `LdapTemplate` / `DirContext` com parâmetros bind, não concatenação de filter string.
  - **Header Injection:** Validação/strip de `\r\n` em headers HTTP de entrada (ex.: `X-Forwarded-For`, `User-Agent` usados em logs).

* **Sanitização de output / XSS Prevention:**
  - **API REST:** `Content-Type: application/json; charset=utf-8` em todas respostas. JSON serialization via Jackson (default Spring) — escapa Unicode/HTML chars automaticamente. **Nenhum endpoint retorna `text/html`** exceto health check público e possivelmente página de erro customizada.
  - **Frontend servido estaticamente:** **CSP estrito** (Content-Security-Policy) — médio prazo (SAD §13). Política inicial (report-only):
    ```
    Content-Security-Policy-Report-Only: default-src 'self'; script-src 'self' 'wasm-unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self' https://observability.example.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; report-uri /csp-report
    ```
    - `'wasm-unsafe-eval'` apenas se futuro uso de WebAssembly (ex.: PDF generation client-side).
    - `'unsafe-inline'` para styles temporário — migrar para nonces/hashes.
  - **Headers de segurança (obrigatórios em todas respostas HTTP):**
    - `X-Frame-Options: DENY`
    - `X-Content-Type-Options: nosniff`
    - `Referrer-Policy: strict-origin-when-cross-origin`
    - `Permissions-Policy: accelerometer=(), camera=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), payment=(), usb=()`
    - `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload` (HSTS — após validação TLS 1.3)
    - `Cross-Origin-Opener-Policy: same-origin`
    - `Cross-Origin-Resource-Policy: same-origin`

---

## 9. Dependency & Supply Chain Security

* **Scan de vulnerabilidades:**
  - **Backend (Java/Maven/Gradle):** **OWASP Dependency-Check** ou **Snyk** integrado no pipeline CI/CD (NFR-M03/M04/M05). Frequência: **a cada build** (PR + merge to main) + **scan semanal agendado** (base de vulnerabilidades atualizada).
  - **Frontend (npm):** `npm audit` (nível `high`+`critical` bloqueia pipeline) + **Snyk** / **Dependabot alerts** no GitHub/GitLab. Frequência: a cada `package-lock.json` change + semanal.
  - **Container (Docker):** **Trivy** / **Grype** scan da imagem final (`aegis-patrimonio:{{version}}`) no pipeline — bloquear se `CRITICAL` ou `HIGH` com fix disponível.

* **Política de atualização (SLA para patches):**
  | Severidade (CVSS v3.1) | SLA de Atualização | Ação se Fix Indisponível |
  | :--- | :--- | :--- |
  | **Critical (≥ 9.0)** | **24 horas** | Mitigação compensatória (WAF rule, network policy, feature flag disable) + escalation para Security Lead |
  | **High (7.0–8.9)** | **72 horas** | Plano de mitigação documentado + prazo máximo 7 dias |
  | **Medium (4.0–6.9)** | **30 dias** | Agendar na próxima sprint |
  | **Low (0.1–3.9)** | **90 dias** | Agendar na janela de manutenção trimestral |

* **SBOM (Software Bill of Materials):**
  - **Gerado a cada release** (tag Git `vX.Y.Z` + pipeline) via **Syft** (formato SPDX JSON + CycloneDX XML).
  - **Armazenado em:** Artifact registry (ex.: GitHub Packages, Nexus, S3 bucket versionado) com retenção **7 anos** (alinhado SOX).
  - **Publicado para:** Consumidores downstream (se houver) via `/.well-known/sbom.json` no registry ou endpoint `/actuator/sbom` (Spring Boot 3.2+).

* **Dependências atuais (diagnóstico + SAD):**
  - **Produção:** `@popperjs/core@^2.11.8` (frontend) — monitorar CVEs.
  - **Backend:** Spring Boot 3.x (BOM gerencia versões transitivas), HikariCP, Jackson, Logback, Micrometer, OpenTelemetry Java Agent — **verificar `pom.xml`/`build.gradle` ausentes no diagnóstico (Gap #7)** para lista completa.

---

## 10. Logging & Auditability

* **O que DEVE ser logado (JSON estruturado — SAD §10):**
  - **Autenticação:** Login sucesso/falha (IP, user-agent, `userId` se conhecido, motivo falha: `INVALID_CREDENTIALS`, `ACCOUNT_LOCKED`, `MFA_REQUIRED`, `TOKEN_EXPIRED`, `TOKEN_REVOKED`).
  - **Autorização:** Acesso negado (403) — `userId`, `roles[]`, endpoint, método HTTP, `requiredRole`.
  - **Operações de escrita (BR-02):** `criar`, `atualizar`, `deletar`, `aprovar`, `cancelar`, `concluir`, `iniciar` — **payload completo** (request body + path params + query params), `userId`, `roles[]`, `traceId`, `timestamp` ISO-8601 UTC, `outcome` (`SUCCESS`/`FAILURE`), `errorCode` se falha.
  - **Mudanças de permissão/RBAC:** Atribuição/remoção de roles, criação/deleção de usuários — `actorUserId`, `targetUserId`, `changedRoles[]`.
  - **Configurações sensíveis:** Alteração de thresholds de alerta, parâmetros de criptografia, configurações de integração (IdP, Email, WORM).
  - **Jobs agendados:** Início/fim de `checkResourceUsageAlerts` e `updateHealthCheck` — `jobName`, `durationMs`, `processedCount`, `errorCount`, `traceId` (gerado no início do job).
  - **Segredos/Chaves:** Rotação de chaves (JWT, DB, TLS, AES) — `keyId`, `operation` (`ROTATE`, `REVOKE`, `CREATE`), `actor` (`SYSTEM_VAULT` ou `adminUserId`).
  - **Erros de segurança:** Falhas de validação de assinatura JWT, `jti` revogado detectado, tentativas de path traversal (se upload futuro), CSP violation reports.

* **O que NUNCA deve ser logado (em nenhum nível — nem DEBUG):**
  - Senhas (plaintext ou hash), `password`, `currentPassword`, `newPassword`, `confirmPassword`.
  - Tokens JWT completos (access/refresh) — log apenas `jti` (primeiros 8 chars) + `sub` + `exp`.
  - Secrets: DB password, AWS/Vault tokens, chaves de criptografia (DEK/KEK), client secrets IdP, SMTP passwords.
  - PII sensível em texto claro: CPF, dados bancários, saúde — **mascarar** nos logs de aplicação (ex.: `cpf: "***.***.***-**"`, `email: "u***@d***.com"`). **Exceção:** `AuditoriaLog` em WORM armazena payload completo (requisito legal SOX/LGPD) — acesso restrito a `ADMIN`/`AUDITOR`.
  - Números de cartão, tokens de pagamento (não aplicável no escopo atual).

* **Retenção de logs de auditoria:**
  - **WORM Audit Storage (SOX/LGPD):** **7 anos imutáveis** (NFR-C02) — Object Lock Compliance Mode, sem possibilidade de deleção antes do retention period.
  - **Logs de aplicação (Loki/Elasticsearch):** 30 dias quentes (SSD), 1 ano frio (S3/Glacier Instant Retrieval).
  - **Logs de segurança isolados (SIEM):** Exportar eventos de auth/autorização/operações BR-02 para SIEM dedicado (Splunk/Elastic Security/Chronicle) com retenção **2 anos**.

* **Correlação & Integridade:**
  - `traceId`/`spanId` (W3C TraceContext) em **100%** das entradas de log (MDC + propagação HTTP/JDBC/HTTP outbound — SAD §10).
  - Hash encadeado (SHA-256) nos logs de auditoria WORM: cada entrada inclui `prevEntryHash` para detectar deleção/inserção/intercalação (verificação de integridade periódica).

---

## 11. Incident Response (Segurança)

* **Processo de disclosure de vulnerabilidade:**
  - **Canal responsável:** `security@aegispatrimonio.com.br` (caixa de entrada monitorada 24/7 por Security On-Call).
  - **PGP Key:** Publicada em `https://aegispatrimonio.com.br/.well-known/security.txt` (RFC 9116).
  - **Scope:** Vulnerabilidades na aplicação, infraestrutura, dependências, configurações cloud.
  - **Safe Harbor:** Compromisso de não ação legal contra pesquisadores que sigam coordinated disclosure (90 dias para fix antes de publicação).

* **SLA de resposta por severidade (alinhado NFR-O04 + indústria):**

| Severidade | Critérios | SLA Resposta Inicial | SLA Mitigação/Contenção | SLA Resolução (Fix em Produção) |
| :--- | :--- | :--- | :--- | :--- |
| **Crítico (P0)** | Exploração ativa, vazamento de dados PII/sensíveis, RCE, auth bypass, DoS confirmado | **15 min** (ack) / **1 h** (análise inicial) | **4 horas** (workaround: WAF rule, feature flag, network isolate) | **24 horas** (hotfix + deploy emergencial) |
| **Alto (P1)** | Vulnerabilidade explorável com acesso autenticado, escalação de privilégio, SQLi confirmado | **1 hora** | **8 horas** | **72 horas** |
| **Médio (P2)** | Information disclosure não-PII, XSS stored/reflected, CSRF, config insegura | **4 horas** | **24 horas** | **7 dias** |
| **Baixo (P3)** | Information disclosure menor, headers de segurança ausentes, dependência vulnerável sem exploit conhecido | **1 dia útil** | **5 dias úteis** | **30 dias** |

* **Playbooks documentados (runbooks):**
  1. **Comprometimento de credenciais/JWT key:** Revogação imediata (`kid` rotation + blocklist flush), invalidação de todas sessões, rotação de segredos no Vault, notificação usuários afetados (LGPD Art. 48).
  2. **Vazamento de dados PII/LGPD_SENSITIVE:** Contenção (isolamento DB), avaliação de escopo (query logs, auditoria WORM), notificação ANPD (72h), comunicação titulares, relatório de causa raiz.
  3. **Ransomware/Encryptor no DB/Storage:** Failover para read-replica (promoção), restore do backup diário (RPO ≤ 24h — NFR-A03) + WAL/log shipping, validação de integridade (hashes), investigação de vetor inicial.
  4. **Ataque de negação de serviço (DoS/DDoS):** Rate limiting no LB/WAF, geo-blocking temporário, scale-up vertical (VM/container resize), acionamento provedor cloud (Shield/Advanced).

* **Pós-incidente:** Blameless post-mortem em **5 dias úteis** — timeline, root cause (5 Whys), action items com owners/prazos, atualização de runbooks e testes de regressão de segurança.

---

## 12. Compliance Mapping

| Requisito Regulatório / Contratual | Como é Atendido no Aegis Patrimônio | Evidência / Artefato |
| :--- | :--- | :--- |
| **LGPD (Lei 13.709/2018) — Art. 7º, 11, 12, 18** | Base legal legítima (contrato/legítimo interesse) para tratamento; minimização (apenas dados necessários para gestão patrimonial); direitos do titular (endpoint admin "direito ao esquecimento" — NFR-C01); anonimização irreversível em non-prod; DPIA para dados `LGPD_SENSITIVE` (NFR-C03). | `LGPD_DPIA_Ativos_Sensiveis.pdf`, `Endpoint_Esquecimento_Spec.md`, `Data_Map_Ativos.xlsx`, `NonProd_Anonymization_Script.sql` |
| **LGPD — Art. 48 (Notificação de Incidente)** | Plano de resposta a incidentes (§11) com notificação ANPD em ≤ 72h; logs de auditoria WORM para investigação de escopo. | `Incident_Response_Playbook.md`, `Security_Team_OnCall_Rotation.xlsx` |
| **SOX (Sarbanes-Oxley) — Seção 404 / 802** | Trilha de auditoria imutável (WORM) para **todas** operações de escrita financeira/patrimonial (BR-02); retenção 7 anos (NFR-C02); integridade via hash encadeado; acesso restrito (ADMIN/AUDITOR); segregação de duties (RBAC BR-01). | `WORM_Bucket_Policy.json`, `Audit_Log_Schema.sql`, `RBAC_Matrix.csv`, `SOX_Control_Matrix.xlsx` |
| **ISO 27001:2022 — A.5, A.8, A.9, A.12, A.16** | Políticas de segurança documentadas (este doc); gestão de ativos (inventário CMDB via `Ativo`); controle de acesso (RBAC + MFA ADMIN + JWT stateless); logging/auditoria (WORM + SIEM); gestão de incidentes (§11). | `ISO27001_SoA.xlsx`, `Risk_Treatment_Plan.md`, `Access_Review_Quarterly_Procedure.md` |
| **PCI DSS v4.0 (se aplicável a dados de pagamento)** | **Fora de escopo atual** — aplicação não processa, armazena ou transmite dados de cartão (CHD/SAD). Se futuro módulo de pagamento: segmentação de rede, tokenização, SAQ D. | `PCI_DSS_Scope_Assessment.pdf` (atestado fora de escopo) |
| **Marco Civil da Internet (Lei 12.965/2014) — Art. 10, 15** | Guarda de registros de acesso a aplicação (logs de auditoria WORM + logs de aplicação) por 6 meses mínimo; proteção de dados pessoais (LGPD sobrepõe). | `Access_Log_Retention_Policy.md`, `WORM_Storage_Config.yaml` |
| **Contrato Cliente / SLA (NFR-A01/A02/A03/A04)** | Disponibilidade 99.5% (SLA) / 99.9% (SLO); RTO ≤ 4h, RPO ≤ 24h; backup diário testado trimestralmente; read-replica para HA parcial. | `SLA_Document.pdf`, `DR_Runbook.md`, `Backup_Restore_Test_Report_YYYYQQ.pdf` |

---

## 13. Gaps Conhecidos & Plano de Ação (Rastreabilidade SAD §13)

| Gap | Risco de Segurança | Ação | Prazo | Owner |
| :--- | :--- | :--- | :--- | :--- |
| **Motor de banco não definido** (Gap #1 SAD) | Impossível validar TDE, prepared statements support, replicação WAL, roles/grants nativos. | Confirmar com DBA/Infra (PostgreSQL/Oracle/SQL Server/MySQL) e documentar hardening específico. | Sprint 0 | DBA / Infra Lead |
| **IdP / MFA não provisionado** (Gap #3 SAD) | MFA ADMIN não implementado (NFR-SEC02); auth depende de `Usuario` table local (risco: hash fraco, sem lockout centralizado). | Selecionar IdP (Keycloak/Azure AD/Okta), implementar OIDC/SAML, migrar usuários, habilitar MFA obrigatório ADMIN. | Sprint 1–2 | Security / IAM Team |
| **Secret Manager não provisionado** (Gap #4 SAD) | Segredos em `application.yml` ou env vars plaintext (risco: vazamento em logs, CI/CD, imagens). | Provisionar Vault/AWS Secrets Manager, migrar todos segredos, configurar rotação 90 dias. | Sprint 0 | DevOps / Security |
| **Observability Stack não definida** (Gap #4 SAD) | Sem logs centralizados, métricas, tracing — impossível detectar anomalias de segurança em tempo real. | Definir stack (Prometheus/Grafana/Loki/Tempo ou Datadog/New Relic), instrumentar OpenTelemetry Java Agent, configurar alertas NFR-O04. | Sprint 0–1 | Platform / SRE |
| **CSP / Security Headers ausentes no frontend** | XSS, clickjacking, MIME sniffing, referrer leakage. | Implementar `SecurityFilterChain` headers + CSP report-only → enforce; remover `console.*` (diagnóstico: `api.js:36,99`). | Sprint 1 | Frontend Lead / Security |
| **SQL Injection risk (SQL cru)** | `ManutencaoSpecification.build` (complexidade 14) e queries nativas podem usar concatenação. | Code review obrigatório em todas queries; regra SonarQube/Checkstyle proibindo concatenação; testes de fuzzing SQL (sqlmap) em staging. | Contínuo (Sprint 0+) | Tech Lead / Security |
| **Scheduler in-process (SPOF jobs)** | Falha app para jobs de alerta/health check; sem auditoria de execução isolada. | Externalizar scheduler (Quartz JDBC JobStore ou CronJob K8s/systemd) — NFR-O02. | Q3 2025 | Backend Lead |
| **Matriz de retenção por entidade não aprovada** (Gap #5 SAD) | Retenção SOX 7 anos vs LGPD direito ao esquecimento — conflito não resolvido. | Workshop Compliance/Legal → matriz aprovada → implementar no `AuditoriaService` e job de purge/anonimização. | Sprint 1 | Compliance / Legal / Tech Lead |

---

## 14. Referências Normativas Internas

| Documento | Versão | Localização | Status |
| :--- | :--- | :--- | :--- |
| `system-architecture.md` (SAD) | 1.0 | `docs/architecture/` | Draft |
| `non-functional-requirements.md` (NFR) | 1.0 | `docs/requirements/` | Draft |
| `business-requirements.md` (BRD) | 1.0 | `docs/requirements/` | Aprovado |
| `db-schema-spec.md` | — | `docs/database/` | **Pendente** (Gap #5) |
| `db-migration-spec.md` (Flyway/Liquibase) | — | `docs/database/` | **Pendente** |
| `api-openapi-spec.yaml` | — | `docs/api/` | **Pendente** (SpringDoc não configurado) |
| `incident-response-playbook.md` | 1.0 | `docs/security/` | **A criar** (baseado no §11) |
| `secure-coding-guidelines-java.md` | 1.0 | `docs/security/` | **A criar** (Prepared statements, validation, logging) |
| `secure-coding-guidelines-js.md` | 1.0 | `docs/security/` | **A criar** (XSS, CSP, dependency mgmt) |

---

> **Fim do documento.**  
> **Próximos passos:** Revisão por Security Lead + Compliance + DBA + Infra na Sprint 0. Aprovação formal antes de implementação de controles técnicos.