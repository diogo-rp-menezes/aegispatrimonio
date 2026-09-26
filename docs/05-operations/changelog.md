# Changelog — Aegis1

> **Formato:** [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) + [Semantic Versioning](https://semver.org/lang/pt-BR/)
> **Base:** Análise AST Java Completa + Frontend Vue 3/PWA + Infra K8s/Docker
> **Geração:** Automática via `git cliff` (Conventional Commits) + Manual para releases

---

## [Unreleased] — Desenvolvimento Ativo (main/develop)

### Added — Funcionalidades Novas

#### Core Domain (Backend)
- **Ativos & Hardware:** CRUD completo de Ativos com depreciação linear automática (BR-03), Detalhe Hardware (CPU, Memória, Discos SMART, Adaptadores Rede), QR Code unitário (ZXing), Termo Responsabilidade PDF (FlyingSaucer/Thymeleaf), Transferência entre filiais/departamentos/localizações com novo Termo, Baixa/Desativação com TCO final consolidado
- **Ordens de Manutenção:** Ciclo de vida completo (Criar→Iniciar→Aprovar→Concluir/Cancelar) para 3 tipos: Corretiva, Preventiva, Preditiva; Validações BR-01 a BR-10; Evidências obrigatórias na aprovação (checklist/foto/assinatura); Custos finais (mão de obra, material, terceiros) com atualização automática `custoTotalPorAtivo`; SLA & Escalation automático (24h/48h)
- **Manutenção Preventiva:** Planos CRON configuráveis, Scheduler geração automática ordens (15min), Relatório aderência (% prazo) para evidência NR-10/12
- **Manutenção Preditiva:** Health Check coleta SMART (PWA mobile + QR Scan + Offline-First IndexedDB/Background Sync), Regressão Linear Simples (OLS - Mínimos Quadrados) batch noturno para prever falha disco (IC 95%), Ordem Preditiva Automática (prob > 80% < 30 dias), Dashboard Riscos com drill-down e ação "Agendar Substituição"
- **Busca Inteligente:** Fuzzy Search Global (Levenshtein Distance otimizado + Trigram Indexes MySQL + Redis Cache TTL 5min), Boost por tipo (ativo>ordem>funcionário>fornecedor), Threshold configurável (default 0.7), Highlight resultados
- **Cadastros Mestres:** Filial (raiz multi-tenancy), Departamento, Fornecedor (categoria, SLA, avaliação), Funcionário + Provisionamento Usuario (função manutenção: TECNICO/APROVADOR/SOLICITANTE), TipoAtivo (vida útil, valor residual, requer hardware)

#### Segurança & Auditoria (Aegis Shield)
- **Auth:** JWT RS256 Stateless + Refresh Token Rotation (HttpOnly Cookie 7d, SameSite=Strict), Access Token 15min, `authInterceptor` frontend com retry automático 1x
- **RBAC Granular:** Matriz Role × Permission × Contexto (GLOBAL/FILIAL) configurável via Admin UI; Roles: ADMIN > GESTOR > TECNICO > USER > AUDITOR; Permissions por recurso/ação/contexto
- **Multi-tenancy:** Hibernate Filter query-level (`filial_id = :tenantId`) em 100% entidades; Admin Global vê todas; Testes cross-tenant CI (TestContainers) — vazamento = build fail
- **Auditoria Imutável:** Hibernate Envers em 100% entidades domínio (CREATE/UPDATE/DELETE + Diff campo-a-campo + IP + User-Agent + Trace-ID); Export PDF/CSV assinado; Retenção 7 anos (partitioning mensal)
- **LGPD:** Anonimização (Esquecimento) + Hash correlação preservado em Envers; Portabilidade (Export JSON completo); Consentimento granular por finalidade; DPIA automatizado

#### Relatórios & QR/PDF
- **Termo Responsabilidade:** PDF assinado (placeholder ICP-Brasil/gov.br A1), Hash SHA-256 integridade, QR Code verificação pública
- **Etiquetas QR Code Lote:** Filtros avançados → PDF A4 (24 etiquetas/folha) ou ZIP SVG; URL pública read-only (`/public/ativo/{tag}?h={hash}`)
- **Compliance Reports:** NR-10/12 (ordens com checklist), LGPD (solicitações), ISO 27001 (acessos, auditoria)

#### Frontend (Vue 3 + Vite + PWA)
- **PWA Completo:** Service Worker (Workbox), Manifest, Install Prompt, Push Notifications (VAPID), Background Sync (Health Checks offline-first), QR Scanner (Barcode Detection API + ZXing WASM fallback)
- **UI/UX:** Bootstrap 5 (CSS-only), Pinia (Type-safe stores), Vue Router (Lazy-loaded + Guards), Axios Interceptors (Auth, Error, Tracing, Retry), Global Search (Debounce + Dropdown agrupado + Highlight)
- **Dashboards:** KPIs Cards + Charts (Chart.js) + Alertas Tempo Real (WebSocket/SSE) para Ordens, Preditiva, SLA, Health Check
- **Acessibilidade:** WCAG 2.1 AA (axe-core E2E), ARIA labels, Focus management, Touch targets 48x48px, Redução movimento

#### Infra & DevOps
- **Kubernetes:** Manifests Kustomize (dev/staging/prod), HPA (CPU/Memory/Custom metrics), PDB, NetworkPolicy, PodSecurityStandards (restricted), cert-manager (TLS auto-rotation), External Secrets Operator (Vault/AWS Secrets)
- **CI/CD:** GitHub Actions (Matrix builds, TestContainers MySQL real, Security Scans, Contract Tests, Cosign Sign, Trivy Scan, SBOM Syft), ArgoCD GitOps (staging/prod), Blue-Green/Canary deploy
- **Observabilidade:** Prometheus/Grafana (Golden Signals + Business KPIs), Tempo/Jaeger (Traces 10%/100% errors), Loki (Logs JSON + Correlation ID), Alertmanager (Routes Critical/Warning/Info), Sentry (Frontend Errors + RUM Core Web Vitals)
- **Testes:** TestContainers MySQL real (reuso habilitado), Coverage ≥ 80% gate, Contract Tests (Pact/Spring Cloud Contract), E2E Cypress (axe-core), Performance k6, Chaos LitmusChaos/Toxiproxy

### Changed — Mudanças em Funcionalidades Existentes
- **Arquitetura:** Migração de frontend vanilla JS (sem build) para Full Stack Vue 3 + Spring Boot 3.3 Modular Monolith
- **Auth:** Migração `localStorage` JWT → HttpOnly Cookie + Refresh Rotation + JWKS endpoint
- **Multi-tenancy:** Implementação nativa Hibernate Filter (substitui lógica manual frontend)
- **Busca:** Substituição busca exata por Fuzzy Search Levenshtein + Trigram + Redis Cache
- **Preditiva:** Nova capacidade (Health Check + Regressão Linear + Ordem Auto) — não existia
- **Auditoria:** Hibernate Envers automático (substitui logs manuais)
- **PDF/QR:** Geração nativa backend (FlyingSaucer + ZXing) — substitui libs frontend

### Deprecated — Funcionalidades Obsoletas
- Frontend vanilla JS sem build (`frontend/src/services/api.js` monolítico)
- JWT em `localStorage` (migração para HttpOnly Cookie)
- Busca exata sem fuzzy/tolerância a typos
- Console logs residuais (`api.js:26,49,52`) — removidos
- Função `request` complexidade 13 — refatorada em módulos (retry, timeout, parsing, auth)

### Removed — Removido
- Dependência exclusiva de backend externo — backend próprio implementado
- Mocks MSW para desenvolvimento — TestContainers MySQL real
- Deploy apenas CDN estático — Full Stack K8s + Docker

### Fixed — Correções de Bugs
- **BR-01:** Validação técnico alocado na criação/início de ordem
- **BR-05:** Bloqueio exclusão entidades mestras com vínculos (409 Conflict)
- **BR-06:** Refresh token automático + retry único + logout limpo
- **BR-10:** Health Check score calculation + threshold alerts
- **BR-13/14:** Multi-tenancy leak prevention (Hibernate Filter + Testes CI)
- **BR-16:** Auditoria imutável 100% entidades (Envers)
- **NFR-M01:** Complexidade ciclomática ≤ 10 (refatoração `request`)
- **NFR-SEC04:** Zero `console.*` em produção (ESLint `no-console: error`)
- **NFR-P02:** Busca Fuzzy P95 < 300ms (Índices Trigram + Redis Cache)

### Security — Segurança
- **ADR-003:** Aegis Shield RBAC Granular implementado (substitui RBAC básico)
- **ADR-004:** Multi-tenancy Hibernate Filter (substitui column-discriminator manual)
- **ADR-005:** Auditoria Envers 100% entidades (substitui custom triggers)
- **ADR-013:** JWT RS256 + Refresh Rotation + HttpOnly Cookie (substitui localStorage)
- **ADR-010:** LGPD Anonimização + Hash Envers preservado (substitui hard delete)
- **OWASP Dep Check:** Zero vulnerabilidades Critical/High (gate CI)
- **Trivy Container Scan:** Zero Critical/High (gate CI)
- **CSP + Security Headers:** Implementados via Nginx Ingress
- **Rate Limiting:** API Gateway (100 req/min auth, 500 req/min API)
- **Pen Test CI:** OWASP ZAP + CodeQL (staging deploy gate)

---

## [1.0.0] - 2025-03-31 (Planejado — MVP Release)

### Added — MVP Completo
- Todas funcionalidades **Must Have** do Story Map v2.0 (Slice 1: MVP)
- Cadastros Mestres (Filial, Departamento, Fornecedor, Funcionário, TipoAtivo) com Multi-tenancy
- Ativos + Hardware + QR Code + Termo PDF + Transferência + Baixa + TCO
- Ordens Corretivas Completas (Criar→Iniciar→Aprovar→Concluir/Cancelar) + SLA/Escalation
- Busca Fuzzy Global (Levenshtein + Trigram + Redis Cache)
- Auth JWT RS256 + Refresh Rotation + Multi-tenancy + RBAC Básico + Auditoria Envers
- QR Code Unitário/Lote + Termo Responsabilidade PDF
- Dashboard Ordens (KPIs + Gráficos + Alertas Tempo Real WebSocket)
- Qualidade: Coverage ≥ 80%, Zero Console.*, Zero Vuln Critical/High, OpenAPI 3.1 + Contract Tests
- Infra: Docker Compose, K8s Manifests, CI/CD Pipeline, Observabilidade Básica

### Security
- Pen Test Staging (OWASP ZAP + CodeQL) — Gate Deploy Prod
- LGPD Anonimização + Portabilidade implementadas
- Auditoria Envers 100% entidades domínio

---

## [0.9.0] - 2025-02-28 (Release Candidate 1)

### Added
- TestContainers MySQL real + CI Coverage Gate 80%
- OpenAPI 3.1 Spec + Contract Tests (Spring Cloud Contract / Pact)
- E2E Cypress (Fluxo Ordem Completo + Cadastros + Login)
- Observabilidade: Prometheus/Grafana + Alertmanager + Loki + Tempo
- K8s Manifests (dev/staging/prod) + ArgoCD GitOps

### Fixed
- Refresh Token Rotation race condition (mutex no interceptor)
- Busca Fuzzy performance (Índices Trigram + Redis Cache TTL 5min)
- Multi-tenancy cross-tenant leak (Testes CI + ArchUnit validation)
- PDF/QR Code geração performance (Streaming + Async batch)

---

## [0.8.0] - 2025-02-14 (Sprint 4 Complete)

### Added
- Ordens Corretivas: Iniciar, Aprovar (Evidência Obrigatória), Concluir (Custos + custoTotalPorAtivo), Cancelar
- Dashboard Ordens (KPIs + Gráficos + WebSocket Tempo Real)
- SLA & Escalation Automático (Job Diário 24h/48h)
- QR Code Unitário + Termo Responsabilidade PDF (FlyingSaucer)
- Busca Fuzzy Global (Levenshtein + Boost + Highlight)

### Fixed
- Validação BR-01 (técnico alocado) + BR-06 (evidência aprovação)
- `custoTotalPorAtivo` consistência frontend/backend (Contract Test)
- `authInterceptor` retry único + logout limpo em falha refresh

---

## [0.7.0] - 2025-01-31 (Sprint 3 Complete)

### Added
- Ativos CRUD + Hardware (CPU, Memória, Discos SMART, Rede) + Depreciação Linear
- Cadastros Mestres: Filial, Departamento, Fornecedor, Funcionário + Provisionamento Usuario, TipoAtivo
- Multi-tenancy: Hibernate Filter + `MultiTenancyFilter` + Admin Global Troca Contexto
- Auth: JWT RS256 + Refresh Token HttpOnly Cookie + `authInterceptor` Frontend
- Auditoria: Hibernate Envers (`@Audited` em todas entidades) + `CustomRevisionListener`

### Changed
- Refatoração `request` (complexidade 13 → 4 módulos: retry, timeout, parsing, auth)
- Remoção 3 `console.*` residuais (`api.js:26,49,52`)
- `handleApiError` padronizado (401→refresh, 409→regra, 5xx→amigável)

---

## [0.6.0] - 2025-01-17 (Sprint 2 Complete)

### Added
- Backend Foundation: Spring Boot 3.3 + Java 21 + MySQL 8 + Flyway + Lombok
- Security Config: Spring Security 6 + JWT + Method Security (SpEL)
- Domain Model: Ativo, AtivoDetalheHardware, Disco, Memoria, AdaptadorRede, Filial, Departamento, Localizacao, Fornecedor, Funcionario, TipoAtivo, Usuario, Role, Permission
- Flyway Migrations: V1__init_schema, V2__security, V3__ativo_hardware, V4__ordem, V5__preventiva, V6__preditiva, V7__auditoria, V8__lgpd
- TestContainers Config: MySQL 8.0 real + Redis + Reuso habilitado

### Infrastructure
- Docker Compose (MySQL, Redis, Backend, Frontend, Mailhog, MinIO)
- Dockerfile Multi-stage (Builder → Distroless Java 21)
- GitHub Actions CI (Build, Test, Security Scan, Contract Test)
- K8s Base Manifests (Deployment, Service, Ingress, HPA, ConfigMap, Secret)

---

## [0.5.0] - 2025-01-10 (Sprint 1 Complete)

### Added
- Frontend Foundation: Vue 3 + Vite + TypeScript + Pinia + Bootstrap 5 + PWA Plugin
- UI Kit: Button, Table, Modal, Form, Chart, QRCode, PdfViewer, Badge, Toast
- Router: Lazy-loaded routes + Guards (Auth, Permission, Tenant)
- API Client: Axios + Interceptors (Auth, Error, Tracing, Retry)
- PWA: Service Worker (Workbox), Manifest, Install Prompt, Push, Background Sync
- QR Scanner: Barcode Detection API + ZXing WASM Fallback

### Infrastructure
- Frontend Dockerfile Multi-stage (Builder → Nginx Static)
- Vite Config (Proxy /api → Backend, Code-splitting, Bundle Analyzer)
- ESLint (Airbnb + Vue + TS + Prettier) + `no-console: error`
- Vitest + Vue Test Utils + Happy DOM + Coverage V8

---

## [0.1.0] - 2024-12-15 (Inicialização do Projeto)

### Added
- Repositório Git inicial (MIT License)
- Estrutura de pastas `docs/` seguindo catálogo DocSystem (00-foundation a 07-database)
- README.md com visão geral do produto Aegis1 (Aegis Patrimônio)
- `.gitignore` abrangente (node_modules, target, dist, .env*, *.log, IDE files)
- `.editorconfig` (indent 2, utf-8, lf, trim trailing whitespace)
- `CONTRIBUTING.md` v1.0 (frontend vanilla JS + API externa)
- `DEVELOPMENT.md` v1.0 (setup frontend only)

---

## Convenções deste Projeto

* **MAJOR (X):** Mudanças incompatíveis com versões anteriores (breaking changes na API, DB schema, config)
* **MINOR (Y):** Novas funcionalidades compatíveis com versões anteriores
* **PATCH (Z):** Correções de bugs compatíveis com versões anteriores
* **Commits:** Conventional Commits 1.0 (em português) — `feat(ativo): adiciona QR Code`, `fix(ordem): corrige validação BR-06`
* **Branches:** `main` (produção), `develop` (integração), `feature/*`, `fix/*`, `refactor/*`, `docs/*`, `chore/*`, `release/*`, `hotfix/*`
* **PRs:** Squash and merge → `develop` → ArgoCD staging → `release/x.y.0` → `main` (tag) → ArgoCD prod
* **Release Tags:** `v{MAJOR}.{MINOR}.{PATCH}` (ex.: `v1.0.0`, `v1.1.0`, `v1.0.1`)
* **Changelog:** Gerado automaticamente via `git cliff` (Conventional Commits → CHANGELOG.md) + Revisão manual para releases

---

## Observações Técnicas Resolvidas (v1.0.0+)

- ✅ Removidas chamadas `console.error` (linha 26) e `console.log` (linhas 49, 52) em `frontend/src/services/api.js`
- ✅ Refatorada função `request` (complexidade ciclomática 13 → 4 módulos: retry, timeout, parsing, auth)
- ✅ Definidos `name`, `version`, `scripts` no `package.json` (frontend) e `pom.xml` (backend)
- ✅ Backend próprio implementado (Spring Boot 3.3) — não mais dependência externa
- ✅ TestContainers MySQL real — não mais mocks MSW
- ✅ Multi-tenancy nativo Hibernate Filter — não mais lógica manual
- ✅ Auditoria Envers 100% entidades — não mais logs manuais
- ✅ JWT RS256 + Refresh Rotation HttpOnly Cookie — não mais localStorage
- ✅ LGPD Anonimização + Hash Envers — não mais hard delete
- ✅ Busca Fuzzy Levenshtein + Trigram + Redis — não mais busca exata
- ✅ Manutenção Preditiva (Health Check + Regressão Linear + Ordem Auto) — nova capacidade
- ✅ PDF/QR Code nativo backend (FlyingSaucer + ZXing) — não mais libs frontend

---

*Changelog regenerado completamente com base em análise AST Java completa + frontend Vue/PWA + infra. Substitui versão 1.0 que continha apenas frontend vanilla JS v0.1.0.*