# CI/CD & Release Plan — Aegis1

> **Versão:** 2.0 · **Owner:** DevOps / Tech Leads · **Status:** Draft
> **Base:** System Architecture v2.0 + Dev Environment v2.0 + NFR v2.0 + Análise AST Java Completa
> **Ferramenta CI/CD:** GitHub Actions + ArgoCD (GitOps) + Kustomize
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/Vite/PWA) + Infra (Docker/K8s)

---

## 1. Pipeline Overview

```mermaid
graph LR
    A[Push/PR] --> B[Static Analysis & Security]
    B --> C[Unit Tests]
    C --> D[Integration Tests (TestContainers)]
    D --> E[Contract Tests]
    E --> F[Build Docker Images]
    F --> G[Security Scan Images]
    G --> H[Push Registry + Sign]
    H --> I[Deploy Staging (ArgoCD)]
    I --> J[E2E + Smoke + Performance]
    J --> K{Aprovação Manual}
    K -- Sim --> L[Deploy Prod (ArgoCD)]
    K -- Auto (Hotfix) --> L
    L --> M[Post-Deploy Verification]
    M --> N[Monitoring + Alerting]
```

---

## 2. Pipeline Stages (GitHub Actions)

### Stage 1: Static Analysis & Security (< 5 min)

| Job | Ferramentas | Alvo | Gate |
| :--- | :--- | :--- | :--- |
| **Backend Lint** | Checkstyle (Google Java Style), SpotBugs, PMD | `src/main/java/**/*.java` | Fail se errors |
| **Frontend Lint** | ESLint (Airbnb + Vue + TS + Prettier), `no-console: error` | `frontend/src/**/*.ts`, `frontend/src/**/*.vue` | Fail se errors |
| **Security SAST** | Semgrep (OWASP Top 10 + Custom: hardcoded secrets, `localStorage.setItem('token')`, crypto weak) | Backend + Frontend | Fail se HIGH/CRITICAL |
| **Secrets Scan** | TruffleHog / GitLeaks | Todo repo (histórico incluído) | Fail se qualquer segredo |
| **Dependency Check** | OWASP Dependency Check (CVSS ≥ 7) | `pom.xml`, `frontend/package.json` | Fail se CRITICAL/HIGH |

### Stage 2: Unit Tests (< 5 min)

| Job | Ferramentas | Cobertura Mínima | Gate |
| :--- | :--- | :--- | :--- |
| **Backend Unit** | JUnit 5 + Mockito + AssertJ | ≥ 80% (Crítico: ≥ 95% domain services, security, predictive, fuzzy) | Fail se < threshold |
| **Frontend Unit** | Vitest + Vue Test Utils + Happy DOM | ≥ 80% (Crítico: ≥ 95% stores, composables, utils, validators) | Fail se < threshold |

### Stage 3: Integration Tests (TestContainers) (< 15 min)

| Job | Ferramentas | Escopo | Gate |
| :--- | :--- | :--- | :--- |
| **Backend IT** | TestContainers (MySQL 8.0 real, Redis) + Spring Boot Test + RestAssured | 100% Controllers + Services + Repositories + Security + Schedulers + Envers + LGPD | Fail se qualquer teste falha |
| **Frontend Integration** | Vitest + MSW (Mock Service Worker) | API mocks para fluxos críticos | Fail se qualquer teste falha |

> **TestContainers Config:** `.testcontainers.properties` → `testcontainers.reuse.enable=true`, `testcontainers.ryuk.disabled=true` (velocidade 10x)

### Stage 4: Contract Tests (< 5 min)

| Job | Ferramentas | Escopo | Gate |
| :--- | :--- | :--- | :--- |
| **Provider (Backend)** | Spring Cloud Contract (Producer) | Gera contratos `.json` em `target/contracts/` | Fail se contrato quebrado |
| **Consumer (Frontend)** | Pact (Consumer) + Pact Broker | Valida contratos contra provider | Fail se contrato quebrado |

### Stage 5: Build Docker Images (< 10 min)

| Imagem | Dockerfile | Base | Otimizações |
| :--- | :--- | :--- | :--- |
| **Backend** | `Dockerfile` (multi-stage) | Builder: `maven:3.9-eclipse-temurin-21` → Runtime: `gcr.io/distroless/java21-debian12` | Layer caching, `.dockerignore`, non-root user 1000, read-only rootfs |
| **Frontend** | `frontend/Dockerfile` (multi-stage) | Builder: `node:20-alpine` (pnpm) → Runtime: `nginx:alpine` (static) | Vite build, Brotli/Gzip, CSP headers, PWA assets |

**Tags:** `ghcr.io/owner/aegis1-backend:{sha}`, `ghcr.io/owner/aegis1-frontend:{sha}`, `latest` (main), `v{MAJOR}.{MINOR}.{PATCH}` (tags)

### Stage 6: Security Scan Images (< 5 min)

| Job | Ferramenta | Gate |
| :--- | :--- | :--- |
| **Trivy Scan** | `trivy image --severity HIGH,CRITICAL --exit-code 1` | Fail se CRITICAL/HIGH |
| **SBOM Generate** | Syft (`syft packages ghcr.io/owner/aegis1-backend:{sha} -o spdx-json`) | Artefato anexado ao release |
| **Cosign Sign** | `cosign sign --yes ghcr.io/owner/aegis1-backend:{sha}` (keyless) | Verificação `cosign verify` |

### Stage 7: Deploy Staging (ArgoCD GitOps) (< 5 min)

```yaml
# ArgoCD Application (staging)
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: aegis1-staging
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/diogo-rp-menezes/aegis1.git
    targetRevision: develop
    path: k8s/overlays/staging
  destination:
    server: https://kubernetes.default.svc
    namespace: aegis1-staging
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
    - CreateNamespace=true
    - PrunePropagationPolicy=foreground
    - PruneLast=true
  ignoreDifferences:
  - group: apps
    kind: Deployment
    jsonPointers:
    - /spec/replicas  # HPA gerencia replicas
```

**Validação Pós-Deploy Automática (< 5 min):**
- Health Checks: `GET /actuator/health/liveness` + `readiness` → 200
- Smoke Tests: Cypress (Login → Lista Ordens → Detalhe → Logout) + Playwright (API Health)
- Métricas: Prometheus scrape OK, Grafana dashboards carregando

### Stage 8: E2E + Smoke + Performance Staging (< 25 min)

| Job | Ferramenta | Escopo | Gate |
| :--- | :--- | :--- | :--- |
| **E2E Critical** | Cypress (Chromium/Firefox) | 12 jornadas: Login, 5 Ordens, 4 Cadastros, Listagem, Dashboard, Busca | Fail se qualquer falha |
| **Accessibility** | Cypress + axe-core | WCAG 2.1 AA (zero critical/serious) | Fail se violações |
| **Performance** | k6 (Grafana k6 Cloud) | Carga basal (50 VUs 10min) + Pico (200 VUs 5min) | P95 < 500ms (Backend), < 800ms (Frontend), Error < 1% |
| **Security DAST** | OWASP ZAP Active Scan | Staging URL | Fail se HIGH/CRITICAL |

### Stage 9: Deploy Production (ArgoCD GitOps + Manual Approval)

```yaml
# ArgoCD Application (prod)
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: aegis1-prod
  namespace: argocd
spec:
  project: production
  source:
    repoURL: https://github.com/diogo-rp-menezes/aegis1.git
    targetRevision: main
    path: k8s/overlays/prod
  destination:
    server: https://kubernetes.default.svc
    namespace: aegis1-prod
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
    - PrunePropagationPolicy=foreground
  # Manual sync only (requires ArgoCD UI/API approval)
```

**Gatilho:** Manual (`workflow_dispatch`) após:
- [ ] CI verde (Stages 1-6) na tag de release
- [ ] Staging deploy sucedido + E2E Critical + Performance + Security verdes
- [ ] 0 bugs P0/P1 abertos vinculados à release
- [ ] Pact contracts verificados contra provider (backend)
- [ ] Janela de deploy: Terça a quinta, 10h–16h BRT

**Estratégia Rollout:** Blue-Green via ArgoCD (Canary opcional 10% → 50% → 100%)
- `argocd app set aegis1-prod --parameter replicaCount=3` (HPA gerencia)
- Rollback: `argocd app rollback aegis1-prod <revision>` (< 2 min)

### Stage 10: Post-Deploy Verification (< 10 min)

- [ ] Health Checks verdes (CDN + Backend + DB + Redis)
- [ ] Métricas negócio: Ordens criadas/min, Taxa conclusão, Dashboard custos
- [ ] Zero erros críticos 30 min (Sentry + Grafana Alerts)
- [ ] Smoke Tests sintéticos (Playwright agendado 15min) passam 30 min consecutivos
- [ ] Error Rate < 0.5% primeiros 60 min
- [ ] P95 Latência < 500ms (Backend) / < 800ms (Frontend) primeiros 60 min
- [ ] Comunicação release: Slack #releases + Changelog atualizado + GitHub Release Notes

---

## 3. Release Strategy

### 3.1 Versionamento: SemVer (Semantic Versioning)

| Tipo | Exemplo | Quando |
| :--- | :--- | :--- |
| **Major** | `1.0.0` → `2.0.0` | Breaking changes (API, DB schema, config, auth) |
| **Minor** | `1.0.0` → `1.1.0` | Novas features backward-compatible |
| **Patch** | `1.0.0` → `1.0.1` | Bug fixes backward-compatible |

### 3.2 Release Flow

```mermaid
gitGraph
    commit id: "v1.0.0"
    branch develop
    checkout develop
    commit id: "feat: ativo qr pdf"
    commit id: "fix: ordem aprovacao"
    commit id: "refactor: api request"
    tag: "v1.1.0-rc.1"
    checkout main
    merge develop tag: "v1.1.0"
    branch hotfix/1.1.1
    checkout hotfix/1.1.1
    commit id: "fix: jwt refresh race"
    tag: "v1.1.1"
    checkout main
    merge hotfix/1.1.1
```

1. **Feature Development:** `feature/*` branches off `develop` → PR → CI → Merge `develop`
2. **Feature Freeze:** PR `develop` → `release/x.y.0` (apenas fixes, sem features)
3. **Stabilization:** Testes extensivos staging (load, chaos, security, UAT)
4. **Release Candidate:** Tag `v1.0.0-rc.1` → Deploy staging → Validação UAT (PO + QA)
5. **Release:** Tag `v1.0.0` → `main` → ArgoCD sync prod → Smoke tests
6. **Post-Release:** Hotfix branch se necessário (`hotfix/1.0.1` → `main` + `develop`)

### 3.3 Cadência & Feature Flags

- **Deploy Contínuo:** `develop` → Staging automático a cada merge
- **Produção:** Semanal (Terça/Quinta) ou sob demanda (Hotfix)
- **Feature Flags:** Não implementado v1.0 (roadmap v1.1: LaunchDarkly/Unleash ou flags baseadas em config DB)

---

## 4. Environments

| Ambiente | Propósito | URL | Deploy | Dados | Infra |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Local** | Desenvolvimento | `http://localhost:5173` (Frontend), `http://localhost:8080` (Backend) | Manual (`docker compose --profile dev up`) | TestContainers / Fixtures JSON | Docker Compose (MySQL, Redis, Backend, Frontend, Mailhog, MinIO) |
| **CI** | Validação PR | Ephemeral | GitHub Actions (Self-hosted runners para TestContainers) | TestContainers MySQL/Redis efêmeros | GitHub Actions Runners (16GB RAM) |
| **Staging** | Homologação | `https://staging-aegis1.empresa.com` (VPN/SSO) | ArgoCD (auto `develop`) | Dump anonimizado prod + Seed controlado | K8s Namespace `aegis1-staging` (HPA min=2, max=10) |
| **Production** | Usuários reais | `https://aegis1.empresa.com` | ArgoCD (manual `main`) | Reais (Read-only sintéticos) | K8s Namespace `aegis1-prod` (HPA min=3, max=20, PDB, NetPol) |

---

## 5. Rollback Strategy

| Gatilho | Mecanismo | Tempo Alvo |
| :--- | :--- | :--- |
| **Error Rate > 1%** (5xx + JS errors) primeiros 10 min | `argocd app rollback aegis1-prod <revision-anterior>` | < 2 min |
| **Alerta Crítico Negócio** (`custoTotalPorAtivo` divergente, vazamento token, cross-tenant leak) | Rollback imediato + Incident Response | < 5 min |
| **Smoke Tests Falham** (Playwright 15min consecutivos) | Rollback automático via ArgoCD webhook | < 2 min |
| **Performance Degradation** (P95 > 2x baseline 30 min) | Rollback + Scale up HPA | < 5 min |

**Kill Switch Complementar:** Feature Flag (config DB) para desabilitar funcionalidade problemática sem redeploy (roadmap v1.1)

---

## 6. Approval Gates

| Gate | Ambiente | Aprovador | SLA | Condições |
| :--- | :--- | :--- | :--- | :--- |
| **Deploy Produção** | Production | Eng Lead + PO (conjunto) | ≤ 1h úteis | CI verde, Staging verde, 0 P0/P1, Performance OK, Pact OK, Security OK |
| **Hotfix P0** | Production | Eng Lead (pode aprovar sozinho) | Imediato | Bypass staging se necessário (com Pact verification local + CI verde) |
| **Release Candidate** | Staging | QA Lead + PO | ≤ 4h | E2E Full + Performance + Security + Chaos verdes |

---

## 6. Secrets & Configuration Management

| Segredo | Armazenamento | Rotação | Injeção |
| :--- | :--- | :--- | :--- |
| **DB Password** | Vault / AWS Secrets Manager | 30 dias (auto) | External Secrets Operator → K8s Secret |
| **JWT Private/Public Key** | Vault PKI / cert-manager | 90 dias (auto) | Vault Agent Injector / cert-manager → K8s Secret |
| **S3 Credentials** | Vault / AWS Secrets Manager | 90 dias | External Secrets Operator |
| **SMTP Password** | Vault | 90 dias | External Secrets Operator |
| **Sentry DSN** | GitHub Actions Secrets / Vault | Conforme Sentry | Build arg (frontend) / Env var (backend) |
| **GitHub Actions Secrets** | GitHub Settings | 90 dias / saída membro | `secrets.*` nos workflows |

**Config Não-Secreta:** `ConfigMap` K8s (application.yml, nginx.conf) + `application-{dev,staging,prod}.yml` (Spring Profiles)

---

## 7. Disaster Recovery & Backup

| Componente | RTO | RPO | Estratégia |
| :--- | :--- | :--- | :--- |
| **Frontend (CDN)** | ≤ 15 min | 0 | Assets imutáveis versionados; Rollback CDN (versão anterior); DNS TTL 60s |
| **Backend (K8s)** | ≤ 30 min | ≤ 1 min | K8s Rollback (Deployment revision); MySQL PITR (binlog + backup snapshot diário); Testes restore mensais |
| **MySQL Primary** | ≤ 60 min | ≤ 1 min | Automated Backup (RDS/Cloud SQL) + Binlog Replication; Cross-region Read Replica; Restore testado mensalmente |
| **Redis** | ≤ 15 min | ≤ 5 min | AOF + RDB Snapshots; Replica Multi-AZ; Cache warming script pós-restore |
| **Object Storage** | ≤ 30 min | 0 | Versioning habilitado; Cross-region Replication (CRR); Lifecycle policies |
| **Secrets/Vault** | ≤ 15 min | 0 | Vault/SealedSecrets; Backup encrypted; Rotation automática (cert-manager TLS, JWT keys) |

---

## 8. Implementation Checklist (Definition of Done para Pipeline)

### CI/CD Pipeline
- [ ] GitHub Actions Workflows: `ci.yml`, `cd-staging.yml`, `cd-prod.yml`, `security.yml`, `dependency-update.yml`
- [ ] Self-hosted Runners para TestContainers (16GB RAM, Docker socket)
- [ ] TestContainers Reuso habilitado (`.testcontainers.properties`)
- [ ] Coverage Gate 80% (JaCoCo + Vitest) — Fail se abaixo
- [ ] Contract Tests (Spring Cloud Contract + Pact) — Bidirecional
- [ ] Docker Multi-stage Builds (Distroless + Nginx) + SBOM (Syft) + Sign (Cosign) + Scan (Trivy)
- [ ] ArgoCD Applications (staging + prod) + Kustomize Overlays
- [ ] Blue-Green/Canary Deploy via ArgoCD + Rollback < 2 min
- [ ] Post-Deploy Verification Automática (Health, Smoke, Metrics, Alerts)

### Observabilidade Pipeline
- [ ] Prometheus Operator + ServiceMonitors (Backend, Frontend, K8s, Node)
- [ ] Grafana Operator + Dashboards Provisionados (Golden Signals + Business KPIs)
- [ ] Tempo/Jaeger Operator + OpenTelemetry SDK (Java + JS) + Sampling 10%/100% errors
- [ ] Loki Stack + Promtail + Logback Logstash Encoder (Logs JSON + Correlation ID)
- [ ] Alertmanager + Routes (Critical→PagerDuty, Warning→Slack, Info→Log)
- [ ] Sentry (Frontend Errors + RUM Core Web Vitals) + Source Maps Upload CI

### Segurança Pipeline
- [ ] OWASP Dependency Check (CVSS ≥ 7 = Fail)
- [ ] SpotBugs + Semgrep + CodeQL (SAST) — Every PR
- [ ] Trivy Container Scan (CRITICAL/HIGH = Fail) — Every Build
- [ ] TruffleHog/GitLeaks (Secrets) — Pre-commit + CI
- [ ] OWASP ZAP Active Scan (DAST) — Staging Deploy + Weekly
- [ ] Pen Test Manual Anual + Relatório + Remediação 30d

---

## 9. Rastreabilidade Deploy ↔ Artefatos

| Stage | System Arch | Dev Env | Test Strategy | Security Policies | Código |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Static Analysis** | §5 (Quality) | §5 (Lint) | §6 (Static Analysis) | §5 (SAST) | `checkstyle.xml`, `spotbugs.xml`, `eslint.config.js`, `semgrep.yml` |
| **Unit Tests** | §5 (Quality) | §5 (Test Local) | §6 (Unit) | — | `*Test.java`, `*.test.ts` |
| **Integration Tests** | §5 (Quality) | §5 (TestContainers) | §6 (Integration) | §7 (Cross-tenant) | `*ControllerIT.java`, `*ServiceIT.java` |
| **Contract Tests** | §4 (API Spec) | — | §6 (Contract) | — | `*ContractTest.java`, `*.pact.test.ts` |
| **Build Images** | §10 (Deployment) | §2 (Docker) | — | §6 (Supply Chain) | `Dockerfile*`, `frontend/Dockerfile*`, `.dockerignore` |
| **Security Scan** | §11 (Observability) | — | §6 (Security) | §5 (SAST/DAST/SCA) | `trivy`, `cosign`, `syft`, `dependency-check` |
| **Deploy Staging** | §10 (Deployment) | §3 (Docker Compose) | §6 (E2E/Perf) | §7 (Incident Response) | `k8s/overlays/staging/`, `argocd-app-staging.yaml` |
| **E2E/Perf/Security** | §11 (Observability) | — | §6 (E2E/Perf/Security) | §7 (Alertas) | `cypress/`, `k6/`, `zap/`, `litmus/` |
| **Deploy Prod** | §10 (Deployment) | — | §6 (Smoke) | §7 (Rollback) | `k8s/overlays/prod/`, `argocd-app-prod.yaml` |
| **Post-Deploy** | §11 (Observability) | — | §6 (Monitoring) | §7 (Runbooks) | `runbooks/`, `grafana/dashboards/`, `alertmanager/rules/` |

---

*Documento regenerado completamente com base em System Architecture v2.0 + Dev Environment v2.0 + Test Strategy v2.0 + Security Policies v2.0. Substitui versão 1.0 que continha apenas pipeline frontend vanilla JS + CDN estático.*