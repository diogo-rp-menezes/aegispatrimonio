# Application Security Policies & Boundary Enforcement — Aegis1

> **Versão:** 2.0 · **Owner:** Security/Eng Lead · **Classificação:** Interno/Confidencial
> **Base:** System Architecture v2.0 + NFR v2.0 + ADRs (Aegis Shield, Multi-tenancy, Auditoria) + Análise AST Java Completa
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/Vite/PWA) + Infra (K8s/Docker)

---

## 1. Threat Model Overview (STRIDE + MITRE ATT&CK)

### 1.1 Ativos a Proteger (Crown Jewels)

| Ativo | Classificação | Localização | Impacto Comprometimento |
| :--- | :--- | :--- | :--- |
| **JWT Access/Refresh Tokens** | Restrito (Credencial) | Frontend (memória Pinia + HttpOnly Cookie) / Backend (Redis blacklist) | Acesso total à conta, elevação de privilégio |
| **Chaves Privadas JWT (RS256)** | Crítico (Chave Mestra) | Vault / K8s SealedSecrets / cert-manager | Emissão de tokens forjados, bypass total auth |
| **Dados de Auditoria (Envers)** | Confidencial (Evidência Legal) | MySQL (tabelas `*_AUD` + `REVINFO`) | Perda rastreabilidade legal, não conformidade LGPD/ISO 27001 |
| **PII Usuários/Funcionários** | Restrito (PII/LGPD) | MySQL (`usuario`, `funcionario`) | Violação LGPD Art. 18, multas, reputação |
| **Dados de Ordens/Custos** | Confidencial (Negócio) | MySQL (`solicitacao_manutencao`, `ativo`) | Vazamento estratégico, fraude, manipulação custos |
| **Health Checks SMART / Previsões** | Confidencial (Operacional) | MySQL (`health_check`, `previsao_falha`) | Manipulação preditiva, falsos alertas, downtime |
| **Configurações Segurança (RBAC, Multi-tenancy)** | Restrito (Config) | MySQL (`role`, `permission`, `role_permission_context`) | Elevação privilégio, vazamento cross-tenant |

### 1.2 Atores de Ameaça (STRIDE)

| Ameaça | Vetor | Mitigação Principal |
| :--- | :--- | :--- |
| **Spoofing** | Token JWT roubado (XSS, MITM, phishing) | RS256 + HttpOnly Cookie + Refresh Rotation + Short TTL (15min) |
| **Tampering** | Modificação payload API (custos, evidências) | Assinatura JWT (RS256), HTTPS/TLS 1.3, Validação backend, Auditoria Envers |
| **Repudiation** | Usuário nega ação (aprovação, exclusão) | Auditoria Envers imutável (quem, quando, o quê, IP, UA) + Assinatura digital termo |
| **Information Disclosure** | Vazamento cross-tenant (Filial A vê dados Filial B) | Hibernate Filter (Multi-tenancy query-level) + Testes cross-tenant CI |
| **Denial of Service** | Flood login/refresh, queries pesadas (fuzzy search) | Rate Limiting (Gateway), Circuit Breaker, Cache Redis, Índices Trigram |
| **Elevation of Privilege** | Bypass RBAC (TECNICO → APROVAR), manipulação `filialId` | Aegis Shield (PermissionEvaluator + Contexto Filial) + MultiTenancyFilter |

### 1.3 Superfícies de Ataque Principais

| Superfície | Componentes | Controles |
| :--- | :--- | :--- |
| **API Gateway / Backend** | `/api/v1/**` (REST), `/auth/*`, `/ws/*` (WebSocket) | Rate Limit, JWT Validation, Circuit Breaker, CORS, CSP Headers |
| **Autenticação** | `/auth/login`, `/auth/refresh`, `/auth/me` | RS256, Refresh Rotation, HttpOnly Cookie, Brute Force Protection |
| **Autorização (Aegis Shield)** | `AegisShieldPermissionEvaluator`, `MultiTenancyFilter` | RBAC Granular (Role×Permission×Contexto), Hibernate Filter |
| **Auditoria** | `AuditoriaController`, Envers `RevisionListener` | Imutável, Diff campo-a-campo, Export assinado, Retenção 7 anos |
| **Busca Fuzzy** | `FuzzySearchService`, `LevenshteinDistance`, Redis Cache | Input validation, Rate limit, Cache TTL, Índices Trigram |
| **Preditiva** | `ManutencaoPreditivaService`, `HealthCheckService` | Input sanitization (SMART), Model versioning, Audit trail |
| **Frontend (SPA/PWA)** | Vue 3 + Axios + Pinia + SW | CSP, XSS Protection (DOMPurify), HttpOnly Cookie, SW Integrity |
| **Infra (K8s/Cloud)** | Ingress, Secrets, NetworkPolicy, RBAC K8s | NetworkPolicy, PodSecurityStandards, SealedSecrets, cert-manager |

---

## 2. Context Isolation Boundaries

### 2.1 Multi-tenancy (Filial) — Isolamento Query-Level

**Princípio:** Dados de uma Filial **nunca** visíveis a outra Filial (exceto Admin Global).

**Mecanismo:** `MultiTenancyFilter` (OncePerRequestFilter) → `TenantContextHolder.set(filialId)` → Hibernate Filter `@Filter(name="filialFilter", condition="filial_id = :filialId")` em **TODAS** entidades `@Audited`.

**Validação Obrigatória (CI):**
- ArchUnit test: `@Filter` presente em todas entidades domínio
- TestContainers cross-tenant: 100+ cenários (Filial A cria ativo → Filial B não vê → 404/403)
- Pen test: Tentativa `X-Tenant-Id` header manipulation → bloqueado por `MultiTenancyFilter`

### 2.2 Sessão & Token Isolation

| Camada | Isolamento |
| :--- | :--- |
| **Frontend (SPA)** | Pinia store por aba (memória isolada); `trace-id` UUID por request; Service Worker scope = origin |
| **Backend (Stateless)** | JWT RS256 (claims: `sub`, `roles`, `filialId`, `permissions`, `jti`); Sem estado servidor |
| **Refresh Token** | HttpOnly; Secure; SameSite=Strict Cookie (não acessível JS); Rotação a cada uso; Blacklist Redis |
| **Network** | NetworkPolicy K8s: Frontend ↔ Backend apenas; Backend ↔ DB/Redis apenas; Ingress TLS 1.3 only |

### 2.3 Traceability & Non-Repudiation

- **Trace-ID:** UUID v4 gerado frontend → Header `X-Trace-Id` propagado em TODAS chamadas (REST + WS) → MDC logging backend → Correlation em logs, métricas, traces
- **Auditoria Envers:** `@Audited` em 100% entidades domínio → `CustomRevisionListener` captura `usuarioId`, `ip`, `userAgent`, `traceId`, `tipoRevisao` → Tabelas `*_AUD` + `REVINFO` append-only (trigger bloqueia DELETE/UPDATE)
- **Assinatura Digital:** Termo Responsabilidade PDF + Hash SHA-256 + Timestamp Authority (futuro ICP-Brasil)

---

## 3. Data Protection Rules

### 3.1 Classificação & Controles

| Dado | Classificação | Em Repouso | Em Trânsito | Acesso | Retenção |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **JWT Private Key** | Crítico | Vault/HSM (FIPS 140-2) | TLS 1.3 (mTLS interno) | Apenas Auth Service | Rotação 90 dias |
| **Refresh Tokens** | Restrito | Redis (hash SHA-256 + salt) | HTTPS/TLS 1.3 | Auth Service + Gateway | 7 dias (rotação a cada uso) |
| **PII (nome, email, CPF, telefone)** | Restrito (LGPD) | MySQL (criptografia coluna opcional) | TLS 1.3 | Aegis Shield (contexto filial) | 7 anos (auditoria) / Anonimização LGPD |
| **Auditoria Envers** | Confidencial (Legal) | MySQL (partitioned, compressão) | TLS 1.3 (replication) | Auditor/Compliance/Admin Global | 7 anos (particionamento mensal) |
| **Health Checks SMART** | Confidencial | MySQL | TLS 1.3 | Técnico/Gestor/Preditiva (filial) | 2 anos (rolling) |
| **Previsões Falha** | Confidencial | MySQL | TLS 1.3 | Gestor/Preditiva (filial) | 1 ano (rolling) |
| **Config RBAC/Multi-tenancy** | Restrito | MySQL | TLS 1.3 | Admin Global | Permanente (auditado) |

### 3.2 Criptografia

- **Em Trânsito:** TLS 1.3 obrigatório (TLS 1.2+ mínimo); HSTS (1 ano, includeSubDomains, preload); Certificate Transparency monitoring; cert-manager auto-rotation (Let's Encrypt / Vault PKI)
- **Em Repouso:** MySQL TDE (Transparent Data Encryption) / Cloud Provider Encryption (AWS RDS / GCP Cloud SQL); Redis TLS; S3 SSE-S3 / SSE-KMS
- **Chaves:** RSA 2048+ (JWT RS256); AES-256-GCM (dados sensíveis coluna); KMS/Vault para key management

### 3.3 LGPD Compliance (Art. 18 — Direito ao Esquecimento)

**Implementação:** `LgpdService.anonimizar(usuarioId)`
1. Gera `hash = SHA256(salt + usuarioId)[:8]` (salt no Vault)
2. `UPDATE usuario SET nome='USUARIO_ANON_{hash}', email='{hash}@anonymized.local', cpf=NULL, telefone=NULL, status='INATIVO'`
3. Revoga todos `refresh_token` + invalida sessões
4. **Envers preserva revisões originais** (valores reais antes) + Nova revisão `TIPO=ANONIMIZACAO` com `hashCorrelacao`
5. `AuditoriaController` permite filtrar "anonimizados" (acesso restrito Auditor/Admin Global)

**Portabilidade (Art. 18 §2º):** `GET /api/v1/usuarios/me/exportar` → JSON completo (perfil, ordens, ativos, health checks, auditoria, consentimentos, `hashCorrelacao`)

---

## 4. Authentication & Authorization (Aegis Shield)

### 4.1 Autenticação (JWT RS256 + Refresh Rotation)

```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend (Pinia)
    participant G as Gateway
    participant A as Auth Service
    participant B as Backend Services
    
    U->>F: Email + Senha
    F->>G: POST /api/v1/auth/login
    G->>A: Forward
    A->>A: AuthenticationManager.authenticate()
    A->>A: JwtTokenProvider.generateToken(usuario, roles, filialId)
    A->>F: 200 {accessToken (15min), refreshToken (HttpOnly Cookie 7d, SameSite=Strict)}
    F->>F: Pinia: accessToken (memória); Cookie: refreshToken
    Note over F: authInterceptor anexa Authorization: Bearer <accessToken> em TODAS requests
    
    F->>G: GET /api/v1/ativos (Bearer token)
    G->>B: Forward + Valida JWT (JwtAuthenticationFilter)
    B->>F: 200 dados
    
    Note over F,B: AccessToken expira (401)
    F->>G: POST /api/v1/auth/refresh (Cookie automático)
    G->>A: Forward
    A->>A: Valida refreshToken (assinatura + expiração + não revogado)
    A->>F: 200 {novo accessToken (15min), novo refreshToken (rotação)}
    F->>F: Atualiza Pinia + Cookie
    F->>G: Retry request original (1x only)
    G->>B: Forward
    B->>F: 200 dados
    
    Note over F: Falha refresh → Limpa Pinia + Cookie → Redirect /login
```

### 4.2 Autorização (Aegis Shield — RBAC Granular + Contexto)

**Modelo:** `Permission(recurso, acao, contexto)` onde:
- **Recurso:** ATIVO, ORDEM, PREVENTIVA, PREDITIVA, RELATORIO, CONFIG, AUDITORIA, LGPD, HEALTH_CHECK, USUARIO, ROLE
- **Acao:** CRIAR, LER, ATUALIZAR, EXCLUIR, APROVAR, INICIAR, CONCLUIR, CANCELAR, GERENCIAR, COLETAR, EXPORTAR
- **Contexto:** GLOBAL (Admin Global) | FILIAL (demais roles)

**Matriz Padrão (Configurável via Admin UI):**

| Role | Contexto | Permissions (Resumo) |
| :--- | :--- | :--- |
| **ADMIN** | GLOBAL | `*_*` (Todas recursos, todas ações, contexto global) |
| **GESTOR** | FILIAL | `ATIVO_*`, `ORDEM_*` (inclui APROVAR), `PREVENTIVA_GERENCIAR`, `PREDITIVA_LER`, `RELATORIO_GERAR`, `HEALTH_CHECK_COLETAR`, `AUDITORIA_LER` (scoped filial) |
| **TECNICO** | FILIAL (próprio) | `ORDEM_INICIAR`, `ORDEM_CONCLUIR`, `ORDEM_LER_PROPRIAS`, `HEALTH_CHECK_COLETAR`, `QR_CODE_ESCANEAR`, `ATIVO_LER` (próprios) |
| **USER** | FILIAL | `ATIVO_LER`, `ORDEM_LER_PROPRIAS`, `RELATORIO_GERAR` (básico) |
| **AUDITOR** | FILIAL/GLOBAL | `AUDITORIA_LER`, `ACESSOS_NEGADOS_LER`, `INTEGRIDADE_VALIDAR`, `RELATORIO_GERAR` (compliance) |

**Avaliação:** `AegisShieldPermissionEvaluator.hasPermission(auth, resource, action, contextFilial)` → SpEL + Custom Logic (hierarquia roles + ownership + contexto filial).

**Validação:** `@PreAuthorize("hasPermission(#ordem, 'APROVAR')")` em Controllers; Testes unitários 100% matriz (5 roles × 20 permissions × 2 contextos = 200 combinações).

---

## 5. Application Security Controls

### 5.1 Input Validation & Sanitization

| Camada | Controle |
| :--- | :--- |
| **Frontend** | Zod/Yup schemas em formulários; DOMPurify para HTML user-generated (evidências, observações); TypeScript strict types |
| **Backend (Controller)** | `@Valid` + Bean Validation (`@NotNull`, `@Size`, `@Pattern`, `@Email`, `@PastOrPresent`, custom `@ValidCNPJ`, `@ValidTagUnico`) |
| **Backend (Service)** | Regras de negócio (BR-01 a BR-18) + Sanitização `StringEscapeUtils` para logs; Prepared Statements (JPA/Hibernate) — **Zero SQL Injection** |
| **Busca Fuzzy** | Input length ≤ 100 chars; Allowlist caracteres; Rate limit 30 req/min/user |
| **Upload Evidências** | MIME allowlist (`image/jpeg`, `image/png`, `application/pdf`); Max 5MB; Sanitização filename; Virus scan (ClamAV) staging/prod |
| **LGPD Export** | JSON schema validation; Rate limit 1 req/hora/user |

### 5.2 Output Encoding & CSP

**Frontend CSP (via Nginx Ingress / Meta Tag):**
```
Content-Security-Policy: 
  default-src 'self'; 
  script-src 'self' 'wasm-unsafe-eval'; 
  style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; 
  font-src 'self' https://fonts.gstatic.com; 
  img-src 'self' data: blob:; 
  connect-src 'self' wss: https:; 
  frame-ancestors 'none'; 
  base-uri 'self'; 
  form-action 'self';
```

**Headers Adicionais (Nginx Ingress / Spring Security):**
```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(self), microphone=(), geolocation=()
Cross-Origin-Opener-Policy: same-origin
Cross-Origin-Resource-Policy: same-origin
```

### 5.3 Rate Limiting & DoS Protection

| Camada | Configuração |
| :--- | :--- |
| **API Gateway (Spring Cloud Gateway)** | `RateLimiter` Redis: 100 req/min `/auth/*`, 500 req/min `/api/v1/**`, 50 req/min `/busca`, 10 req/min `/relatorios/*` |
| **Auth Endpoints** | Bucket4j: 5 tentativas login/15min por IP+email; Lockout 15min após 5 falhas; Captcha (hCaptcha) após 3 falhas |
| **WebSocket** | 50 conexões simultâneas/user; Heartbeat 30s; Idle timeout 5min |
| **K8s NetworkPolicy** | Ingress apenas de Frontend namespace; Egress apenas DB/Redis/Storage/SMTP |

### 5.4 Secrets & Key Management

| Segredo | Armazenamento | Rotação | Acesso |
| :--- | :--- | :--- | :--- |
| **JWT Private Key (RS256)** | Vault (PKI) / cert-manager (K8s) | 90 dias (auto) | Auth Service (runtime), Gateway (validação) |
| **JWT Public Key (JWKS)** | `/api/v1/.well-known/jwks.json` (public) | Auto com rotação privada | Gateway, Microserviços futuros |
| **DB Password** | Vault / AWS Secrets Manager / SealedSecrets | 30 dias (auto) | Backend (runtime via External Secrets) |
| **S3 Credentials** | Vault / AWS Secrets Manager | 90 dias | Backend (Storage Service) |
| **SMTP Password** | Vault | 90 dias | Backend (Notification Service) |
| **Sentry DSN** | GitHub Actions Secrets / Vault | Conforme rotação Sentry | Frontend (build time), Backend (runtime) |

---

## 6. Infrastructure Security (K8s / Cloud)

### 6.1 Kubernetes Hardening

```yaml
# PodSecurityStandards: restricted
apiVersion: v1
kind: Pod
metadata:
  annotations:
    seccomp.security.alpha.kubernetes.io/pod: runtime/default
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    runAsGroup: 1000
    fsGroup: 1000
    seccompProfile:
      type: RuntimeDefault
  containers:
  - name: backend
    securityContext:
      allowPrivilegeEscalation: false
      capabilities:
        drop: ["ALL"]
      readOnlyRootFilesystem: true
      runAsNonRoot: true
      runAsUser: 1000
```

### 6.2 Network Policies

```yaml
# Backend só fala com DB, Redis, Storage, SMTP
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-egress
  namespace: aegis1
spec:
  podSelector:
    matchLabels:
      app: aegis1-backend
  policyTypes: [Egress]
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: mysql
    ports:
    - protocol: TCP
      port: 3306
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
  - to:
    - namespaceSelector:
        matchLabels:
          name: external-smtp
    ports:
    - protocol: TCP
      port: 587
```

### 6.3 Supply Chain Security

- **SBOM:** Syft (GitHub Actions) → `spdx.json` anexado ao release
- **Image Signing:** Cosign (keyless) → `cosign sign --yes ghcr.io/owner/aegis1-backend:sha`
- **Image Scan:** Trivy (CI) — fail se CRITICAL/HIGH; `trivy image --severity HIGH,CRITICAL --exit-code 1`
- **Base Images:** Distroless (`gcr.io/distroless/java21-debian12`) / Alpine (frontend nginx)
- **Dependencies:** Renovate/Dependabot (auto-PR semanal); `mvn versions:display-plugin-updates` mensal

---

## 7. Incident Response & Monitoring

### 7.1 Alertas de Segurança (Alertmanager)

| Alerta | Condição | Severidade | Rota | Runbook |
| :--- | :--- | :--- | :--- | :--- |
| **Cross-Tenant Leak** | `acessos_negados_total > 10/min` | **Critical** | PagerDuty + Slack #security | `runbooks/cross-tenant-leak.md` |
| **Brute Force Login** | `login_failed_total{ip} > 5/15min` | **Warning** | Slack #security | `runbooks/brute-force.md` |
| **Refresh Token Replay** | `refresh_token_reused_total > 0` | **Critical** | PagerDuty | `runbooks/token-replay.md` |
| **Preditiva Threshold** | `previsao_falha_probabilidade > 0.9` | **Warning** | Slack #ops | `runbooks/predictive-alert.md` |
| **SLA Breach** | `ordem_sla_breach_total > 0` | **Warning** | Slack #ops | `runbooks/sla-breach.md` |
| **Audit Log Tamper** | `envers_revision_deleted_total > 0` | **Critical** | PagerDuty + Email DPO | `runbooks/audit-tamper.md` |
| **Container Vuln** | `trivy_critical_vulnerabilities > 0` | **Critical** | Slack #security | `runbooks/container-vuln.md` |

### 7.2 Runbooks (Resumo)

| Runbook | Ação Imediata | Investigação | Recuperação |
| :--- | :--- | :--- | :--- |
| **Cross-Tenant Leak** | Bloquear IP/usuário; Revogar tokens; Isolar namespace | Logs `MultiTenancyFilter` + `AegisShield` + Auditoria Envers | Corrigir `@Filter` faltante; Deploy hotfix; Testes cross-tenant CI |
| **Token Replay** | Revogar todos refresh tokens do usuário; Forçar re-login | `refresh_token` table audit; `JwtTokenProvider` logs | Rotacionar chaves JWT (se comprometida); Deploy nova chave |
| **Brute Force** | Bloquear IP (WAF/Gateway); Alertar usuário afetado | `login_failed` logs; GeoIP; User-Agent | Desbloquear após 1h; Revisar rate limits |
| **Audit Tamper** | Isolar DB (read-only); Alertar DPO/Legal | `REVINFO` integrity check; Trigger logs; Forense | Restore backup PITR; Rebuild audit tables; Notificar autoridades se LGPD |

---

## 8. Compliance & Auditoria

### 8.1 LGPD (Lei 13.709/2018)

| Artigo | Requisito | Implementação |
| :--- | :--- | :--- |
| **Art. 7º** | Base legal | Consentimento (opt-in) + Legítimo interesse (auditoria, segurança) |
| **Art. 18** | Direitos do titular | Esquecimento (anonimização + hash Envers), Portabilidade (export JSON), Retificação, Acesso |
| **Art. 16** | Exceções | Auditoria Envers preservada (obrigação legal trabalhista/fiscal) |
| **Art. 46-49** | Segurança | Criptografia, Pseudonimização, Minimização, Retenção 7 anos (auditoria) |
| **Art. 52-54** | DPO / Relatório | `LGPD_GERENCIAR` role; Relatórios automáticos; Notificação incidente 48h |

### 8.2 ISO 27001 / SOC 2 (Controles Mapeados)

| Controle ISO 27001 | Implementação Aegis1 |
| :--- | :--- |
| **A.5.15** Controle de acesso | Aegis Shield (RBAC granular + multi-tenancy) |
| **A.8.2** Classificação informação | Tabela classificação (Crítico/Restrito/Confidencial/Interno) |
| **A.8.3** Manipulação mídia | Upload evidências (sanitização, virus scan, criptografia S3) |
| **A.12.4** Log de eventos | Logs JSON estruturados (correlation ID) + Loki 30d + Envers 7 anos |
| **A.14.2** Segurança desenvolvimento | SAST (SpotBugs/Semgrep/CodeQL), DAST (ZAP), Dependency Check, Container Scan |
| **A.18.1** Conformidade legal | LGPD compliance, LGPD_GERENCIAR role, DPIA automatizado |

### 8.3 Auditoria Contínua

- **Envers:** 100% entidades domínio auditadas (CREATE/UPDATE/DELETE + Diff campo-a-campo)
- **Integridade:** Job semanal SHA-256 tabelas críticas vs baseline (`integridade_checksum`)
- **Acessos Negados:** Log + Alerta > 10/min (cross-tenant leak detection)
- **Export Legal:** PDF/CSV assinado digitalmente (Timestamp Authority) para evidência judicial

---

## 9. Security Testing (Continuous)

| Tipo | Ferramenta | Frequência | Gate |
| :--- | :--- | :--- | :--- |
| **SAST** | SpotBugs + Semgrep + CodeQL | Every PR + Daily | Fail se HIGH/CRITICAL |
| **DAST** | OWASP ZAP (Active Scan) | Staging deploy + Weekly | Fail se HIGH/CRITICAL |
| **SCA** | OWASP Dependency Check + Trivy | Every PR + Daily | Fail se CVSS ≥ 7 |
| **Container Scan** | Trivy | Every Image Build | Fail se CRITICAL/HIGH |
| **Secrets Scan** | TruffleHog / GitLeaks | Pre-commit + CI | Fail se qualquer segredo |
| **Pen Test** | Manual (anual) + Automatizado (ZAP) | Anual + Pré-prod | Relatório + Remediação 30d |
| **ArchUnit** | ArchUnit Tests | Every PR | Fail se boundary violation |

---

## 10. Rastreabilidade Políticas ↔ Artefatos

| Política | ADR | System Arch | NFR | Código | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Multi-tenancy Isolamento** | ADR-004 | §7.3 | NFR-SEC04, S03 | `MultiTenancyFilter`, `@Filter` entidades | MultiTenancyIT, ArchUnitTest |
| **Aegis Shield RBAC** | ADR-003 | §7.2 | NFR-SEC03, SEC04 | `AegisShieldPermissionEvaluator`, `RolePermissionContext` | AegisShieldTest, SecurityConfigIT |
| **JWT RS256 + Refresh Rotation** | ADR-013 | §7.1 | NFR-SEC02, SEC03 | `JwtTokenProvider`, `RefreshTokenRepository` | AuthControllerIT, SecurityConfigIT |
| **Auditoria Envers** | ADR-005 | §6, §7.3 | NFR-C02, C04, C05 | `@Audited`, `CustomRevisionListener` | AuditoriaControllerIT, EnversTest |
| **LGPD Anonimização** | ADR-010 | §7.3 (LGPD) | NFR-C01, C02 | `LgpdService`, `CustomRevisionListener` | UsuarioControllerIT, LgpdServiceTest |
| **Busca Fuzzy Segura** | ADR-001 | §9.3 | NFR-P02, SEC04 | `FuzzySearchService`, RateLimit | FuzzySearchServiceTest, BuscaControllerIT |
| **Preditiva Segura** | ADR-002 | §9.2 | NFR-P03, C04 | `ManutencaoPreditivaService`, Input Validation | ManutencaoPreditivaServiceTest |
| **Container/Supply Chain** | ADR-015 | §10, §12 | NFR-SEC10, M03 | `Dockerfile`, `.github/workflows/`, `cosign`, `trivy` | Trivy Scan CI, SBOM Verification |
| **K8s Hardening** | ADR-015 | §10 | NFR-SEC01, SEC04 | `k8s/` (PodSecurity, NetworkPolicy, RBAC) | K8s Policy Tests (Kyverno/OPA) |

---

*Documento regenerado completamente com base em análise AST completa do backend Java (security, audit, lgpd, predictive, search, multi-tenancy) + frontend Vue/PWA + infra K8s/Docker. Substitui versão 1.0 que continha apenas visão frontend vanilla JS + API externa.*