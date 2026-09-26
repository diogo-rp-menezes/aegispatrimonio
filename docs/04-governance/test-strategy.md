# Test Strategy — Aegis1

> **Versão:** 2.0 · **Owner:** QA Lead / Tech Leads · **Status:** Draft
> **Base:** System Architecture v2.0 + NFR v2.0 + Use Cases v2.0 + Análise AST Java Completa
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/Vite/PWA) + Infra (Docker/K8s/TestContainers)

---

## 1. Objectives & Quality Goals

* **Zero regressões críticas (P0/P1)** em produção nos fluxos core: Ativos + Hardware + QR/PDF, Ordens (Corretiva/Preventiva/Preditiva), Cadastros Mestres, Auth/RBAC/Multi-tenancy, Busca Fuzzy, Preditiva, Auditoria, LGPD
* **MTTD < 2 horas** via execução contínua CI + observabilidade (Prometheus/Grafana/Sentry) + alertas automatizados
* **100% regras de negócio (BR-01 a BR-18)** validadas via testes BDD (Gherkin) + Contract Tests
* **Latência P95 < 500ms (Backend) / < 800ms (Frontend→Backend)** sob carga (k6 + k8s staging)
* **Zero vazamento dados sensíveis** (tokens, PII, cross-tenant) via SAST/DAST/Secrets Scan + Testes cross-tenant
* **Consistência `custoTotalPorAtivo` / TCO** entre backend/frontend via Contract Tests (Pact/Spring Cloud Contract)
* **Cobertura ≥ 80%** (instruções/linhas) — Gate CI obrigatório (JaCoCo + Vitest V8)
* **Acessibilidade WCAG 2.1 AA** — Zero violações críticas/severas (axe-core E2E)

---

## 2. Test Pyramid (Full Stack)

| Camada | Cobertura Mínima | Ferramentas | Frequência | Responsável | Escopo |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | **≥ 85% linhas / ≥ 80% branches** (Crítico: ≥ 95% em domain services, security, predictive, fuzzy) | **Backend:** JUnit 5 + Mockito + AssertJ<br>**Frontend:** Vitest + Vue Test Utils + Happy DOM | A cada commit (pre-push hook + CI) | Dev | Domain logic, Services, Utils, Stores, Composables, Validators, Levenshtein, OLS Regression |
| **Integration Tests** | **100% Controllers + Services + Repositories + Security + Schedulers** | **Backend:** TestContainers (MySQL 8.0 real, Redis) + Spring Boot Test + RestAssured<br>**Frontend:** Vitest + MSW (Mock Service Worker) para API mocks | A cada PR (pipeline obrigatório) | Dev | Controllers (CRUD + Transições), Services (Business Rules), Repositories (Queries, Filters), Security (Auth, RBAC, Multi-tenancy), Schedulers (Preventiva, Preditiva, SLA), Auditoria (Envers), LGPD |
| **Contract Tests** | **100% endpoints públicos** (Consumer-driven) | **Backend:** Spring Cloud Contract (Producer)<br>**Frontend:** Pact (Consumer) + Pact Broker | A cada PR (Consumer) + Daily (Provider verification) | Dev + Backend | Todos endpoints `/api/v1/**` consumidos pelo frontend: Auth, Ativos, Ordens, Preventiva, Preditiva, Busca, Cadastros, Relatórios, Admin, Auditoria, LGPD |
| **E2E Tests** | **100% jornadas críticas** (Happy Path + 1 erro por story) | **Frontend:** Cypress (Chromium, Firefox, WebKit) + axe-core<br>**Backend:** TestContainers + RestAssured (fluxos cross-service) | A cada deploy staging + Nightly | QA | Login, Criar Ativo+Hardware+QR+PDF, Ordem Completa (Criar→Iniciar→Aprovar→Concluir), Preventiva (Plano→Geração Auto), Preditiva (Health Check→Previsão→Ordem Auto), Busca Fuzzy, Admin (RBAC Matrix, Troca Filial), Auditoria, LGPD, Relatórios QR/PDF |
| **Performance** | **P95 < 500ms (Backend) / < 800ms (Frontend)** | **k6** (scripts `tests/performance/`) + Grafana k6 Cloud | Release Candidate + Weekly Staging | QA/DevOps | Carga basal (50 VUs), Pico (200 VUs), Soak (1h), Spike (500 VUs 30s) — Endpoints: Auth, Ativos, Ordens, Busca, Dashboard, Preditiva |
| **Security** | **Zero Critical/High** | **SAST:** SpotBugs + Semgrep + CodeQL<br>**DAST:** OWASP ZAP (Active Scan)<br>**SCA:** OWASP Dep Check + Trivy<br>**Secrets:** TruffleHog/GitLeaks<br>**Container:** Trivy | SAST/SCA: Every PR<br>DAST: Staging Deploy + Weekly<br>Container: Every Build | Security/DevOps | OWASP Top 10, LGPD, Multi-tenancy leak, RBAC bypass, Token replay, XSS/CSRF, Rate Limit, CSP |
| **Accessibility** | **WCAG 2.1 AA — Zero Critical/Serious** | **axe-core** (Cypress + @axe-core/playwright) | Every PR (E2E) + Nightly | QA | Todas páginas user-facing (Dashboard, Assets, Orders, Preventiva, Preditiva, Admin, Relatórios, LGPD) |
| **Chaos/Resilience** | **MTTR < 15min** (RTO) | **Toxiproxy** (Staging) + **LitmusChaos** (K8s Staging) | Sprint + Ad-hoc incidentes | DevOps/SRE | Latência injetada, Falha 5xx, DB/Redis down, Pod kill, Network partition, Token expiry + refresh fail |

---

## 3. Test Environments

| Ambiente | Propósito | Dados | Acesso | Provisionamento |
| :--- | :--- | :--- | :--- | :--- |
| **Local (Dev)** | Desenvolvimento, debug unit/integration | TestContainers (MySQL/Redis efêmeros) + MSW fixtures JSON | Dev (IDE) | `./mvnw test` / `pnpm test` / `docker compose --profile dev up` |
| **CI (GitHub Actions)** | Validação automatizada PRs | TestContainers (MySQL 8.0 real, Redis) + MSW | Pipeline (obrigatório merge) | `act` local / GitHub Actions runners (self-hosted para TestContainers) |
| **Staging** | Homologação pré-prod com dados realistas anonimizados | Dump anonimizado produção (script `scripts/anonymize-dump.sh` + `scripts/seed-staging.sh`) | QA, PO, Eng Lead (VPN/SSO) | ArgoCD sync `staging` overlay + `scripts/seed-staging.sh` pós-deploy |
| **Production** | Smoke tests pós-deploy + monitoramento sintético | Dados reais (somente leitura sintéticos) | Automação (Playwright agendado 15min) + Alertas | ArgoCD sync `prod` overlay + Canary/Blue-Green |

---

## 4. Coverage Targets (Quality Gates)

| Métrica | Backend (JaCoCo) | Frontend (Vitest V8) | Gate CI |
| :--- | :--- | :--- | :--- |
| **Instructions / Lines** | ≥ 80% | ≥ 80% | **Fail se < 80%** |
| **Branches** | ≥ 80% | ≥ 80% | **Fail se < 80%** |
| **Methods / Functions** | ≥ 80% | ≥ 80% | **Fail se < 80%** |
| **Crítico (Domain Services, Security, Predictive, Fuzzy, Auth)** | ≥ 95% | ≥ 95% | **Fail se < 95%** |
| **Exclusões Justificadas** | `*Configuration*`, `*Application*`, generated code, `main` classes | `main.ts`, `vite.config.ts`, `*.config.*`, generated | Documentado em `jacoco-excludes.xml` / `vitest.config.ts` |

---

## 5. Non-Functional Testing Details

### 5.1 Performance (k6)

```javascript
// tests/performance/load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const options = {
  stages: [
    { duration: '2m', target: 50 },   // Ramp up
    { duration: '10m', target: 50 },  // Steady state
    { duration: '2m', target: 200 },  // Spike
    { duration: '5m', target: 200 },  // Peak
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    'http_req_duration{type:api}': ['p(95)<500'],      // Backend P95 < 500ms
    'http_req_duration{type:frontend}': ['p(95)<800'], // Frontend→Backend P95 < 800ms
    'http_req_failed': ['rate<0.01'],                  // Error rate < 1%
    'checks': ['rate>0.99'],                           // Checks pass > 99%
  },
};

const authToken = `__ENV.ACCESS_TOKEN__`;
const headers = { Authorization: `Bearer ${authToken}`, 'X-Trace-Id': `k6-${__ITER}` };

export default function() {
  const base = 'https://staging-api.aegis1.empresa.com/api/v1';
  
  // Auth (setup)
  if (__ITER === 0) {
    const login = http.post(`${base}/auth/login`, JSON.stringify({email: 'test@aegis1.com', senha: 'Test123!'}), { headers: { 'Content-Type': 'application/json' } });
    check(login, { 'login 200': (r) => r.status === 200 });
  }

  // Cenários representativos
  const endpoints = [
    { name: 'Listar Ativos', url: `${base}/ativos?page=0&size=20`, type: 'api' },
    { name: 'Buscar Ativo', url: `${base}/ativos/1`, type: 'api' },
    { name: 'Criar Ordem', url: `${base}/ordens`, method: 'POST', body: JSON.stringify({ativoId: 1, tipo: 'CORRETIVA', descricao: 'Teste k6', prioridade: 'MEDIA', tecnicoResponsavelId: 5}), type: 'api' },
    { name: 'Iniciar Ordem', url: `${base}/ordens/1/iniciar`, method: 'PATCH', type: 'api' },
    { name: 'Busca Fuzzy', url: `${base}/busca?q=notebok&types=ativo,ordem`, type: 'api' },
    { name: 'Dashboard Ordens', url: `${base}/dashboard/ordens`, type: 'api' },
    { name: 'Dashboard Preditiva', url: `${base}/dashboard/preditiva`, type: 'api' },
    { name: 'Health Check', url: `${base}/actuator/health`, type: 'api' },
  ];

  endpoints.forEach(ep => {
    const params = { headers, tags: { type: ep.type || 'api', name: ep.name } };
    let res;
    if (ep.method === 'POST') res = http.post(ep.url, ep.body, params);
    else if (ep.method === 'PATCH') res = http.patch(ep.url, null, params);
    else res = http.get(ep.url, params);
    
    check(res, {
      [`${ep.name} status 2xx`]: (r) => r.status >= 200 && r.status < 300,
      [`${ep.name} p95 < threshold`]: (r) => r.timings.duration < (ep.type === 'frontend' ? 800 : 500),
    });
  });
  
  sleep(1);
}
```

**Critérios de Aceite:**
- P95 Latência: Backend < 500ms, Frontend→Backend < 800ms
- Taxa Erro: < 1% (janela 5 min)
- Throughput: > 100 req/s (lista paginada), > 50 req/s (mutations)
- Disponibilidade: 99.9% durante teste

### 5.2 Segurança (Continuous)

| Tipo | Ferramenta | Frequência | Gate |
| :--- | :--- | :--- | :--- |
| **SAST** | SpotBugs + Semgrep (OWASP Top 10 + Custom: `localStorage.setItem('token')`, `console.*` sensível, hardcoded secrets) | Every PR | Fail se HIGH/CRITICAL |
| **DAST** | OWASP ZAP Active Scan (staging) | Staging Deploy + Weekly | Fail se HIGH/CRITICAL |
| **SCA** | OWASP Dependency Check (CVSS ≥ 7) + Trivy (container) | Every PR + Daily | Fail se CVSS ≥ 7 |
| **Secrets Scan** | TruffleHog / GitLeaks | Pre-commit + CI | Fail se qualquer segredo |
| **Container Scan** | Trivy (CRITICAL/HIGH) | Every Image Build | Fail se CRITICAL/HIGH |
| **Pen Test** | Manual (anual) + ZAP Automated | Anual + Pré-prod | Relatório + Remediação 30d |

### 5.3 Acessibilidade (WCAG 2.1 AA)

```typescript
// cypress/support/e2e.ts
import 'cypress-axe';

Cypress.Commands.add('checkA11y', (context, options) => {
  cy.injectAxe();
  cy.checkA11y(context, options, (violations) => {
    const critical = violations.filter(v => v.impact === 'critical' || v.impact === 'serious');
    if (critical.length > 0) {
      throw new Error(`A11y violations: ${JSON.stringify(critical, null, 2)}`);
    }
  });
});

// Uso nos testes E2E
it('Dashboard Ordens - Acessibilidade', () => {
  cy.login('gestor@aegis1.com');
  cy.visit('/dashboard');
  cy.checkA11y(); // Fail se violações critical/serious
});
```

**Critérios:** Zero violações `critical`/`serious` em páginas de alta frequência; `moderate`/`minor` documentados com ticket de correção.

### 5.4 Chaos Engineering & Resilience (Staging)

| Experimento | Ferramenta | Cenário | Validação |
| :--- | :--- | :--- | :--- |
| **Latência Injetada** | Toxiproxy | +2s, +5s, +30s latency em `/api/v1/**` | Toast "Erro de conexão" + Retry automático (1x) + Circuit Breaker abre |
| **Falha Intermitente 5xx** | Toxiproxy | 10%, 50% falhas aleatórias | `handleApiError` preserva estado + Toast amigável + Retry manual |
| **DB Down** | LitmusChaos | `PodFailure` MySQL Primary | Read Replica assume (se configurado) / Graceful degradation (cache Redis) |
| **Redis Down** | LitmusChaos | `PodFailure` Redis | Cache miss → Fallback DB (performance degradada) / Rate limit local |
| **Pod Kill** | LitmusChaos | `PodDelete` Backend (1/3 replicas) | HPA escala + Rolling update sem downtime (PDB minAvailable=2) |
| **Network Partition** | LitmusChaos | `NetworkPartition` Backend ↔ DB | Circuit Breaker + Fallback cache / Error amigável |
| **Token Expiry + Refresh Fail** | Cypress + MSW | Access token expirado + Refresh 401 | Logout limpo → Redirect `/login` + Estado limpo |

---

## 6. Test Data Management

### 6.1 Estratégia de Geração

| Fonte | Descrição | Localização |
| :--- | :--- | :--- |
| **Fixtures JSON** | Cenários BDD versionados por entidade (válidos, duplicados, vazios, com vínculos, sem vínculos) | `tests/fixtures/` (backend) / `frontend/tests/fixtures/` |
| **Factories** | Geração dinâmica tipada para testes integração/E2E (`createAtivo({tipo: 'HARDWARE'})`, `createOrdem({estado: 'ABERTA'})`) | `tests/factories/` (backend) / `frontend/tests/factories/` |
| **Builders** | Test Data Builders (Builder Pattern) para objetos complexos (Ativo com Hardware, Ordem com Evidências) | `tests/builders/` |
| **Seed Scripts** | Staging: `scripts/seed-staging.sh` consome fixtures + randomização controlada (seed fixo por execução) | `scripts/` |

### 6.2 Dados Sensíveis (LGPD Compliance)

- **Obrigatório:** Anonimização/mascaramento fora de produção (CPF/CNPJ, matrículas, nomes reais, emails, telefones)
- **Implementação:** `scripts/anonymize-dump.sh` (mysqldump → `pt-online-schema-change` anonimização → import staging)
- **Tokens/JWT:** Nunca commitados; Staging usa issuer de teste com chaves rotacionadas; Local usa MSW com tokens JWT mockados (HS256 conhecida)
- **PII em Logs:** Mascaramento automático via Logback (`%replace(%msg){'\\d{11}', '***'}`) + MDC `usuarioId` hash

---

## 7. Regression Strategy

| Nível | Escopo | Frequência | Tempo Alvo | Ambiente |
| :--- | :--- | :--- | :--- | :--- |
| **Nível 1 (CI - Obrigatório)** | Unit + Integration + Contract + SAST/SCA | Every PR | < 15 min | CI (GitHub Actions) |
| **Nível 2 (Staging Deploy - Obrigatório)** | E2E Crítico (12 jornadas: 5 Ordens + 4 Cadastros + Listagem + Dashboard + Login/Auth + Busca) | Every Staging Deploy | < 25 min | Staging |
| **Nível 3 (Nightly - Completo)** | E2E Full (todas 20+ stories BDD + bordas + A11y + Performance Smoke + Security Scan) | Nightly (02:00) | < 60 min | Staging |
| **Nível 4 (Release Candidate)** | Full Suite + Performance (k6) + Security (DAST) + Chaos (Toxiproxy) + Accessibility Full | Release Candidate | < 120 min | Staging |
| **Nível 5 (Produção - Pós-Deploy)** | Smoke Tests (Health, Auth, 3 jornadas críticas) + Synthetic Monitoring | Every Prod Deploy + 15min | < 5 min | Production |

---

## 8. Defect Management & Metrics

### 8.1 Classificação Severidade

| Severidade | Definição | SLA Correção | Exemplo |
| :--- | :--- | :--- | :--- |
| **P0 - Critical** | Sistema indisponível, perda dados, vazamento segurança, compliance violation | **< 2 horas** (hotfix) | Cross-tenant leak, Token replay, DB corruption, LGPD breach |
| **P1 - High** | Funcionalidade core quebrada, performance degradada > 50%, regra negócio violada | **< 24 horas** | Ordem não conclui, Busca fuzzy não retorna, RBAC bypass, Refresh token fail |
| **P2 - Medium** | Funcionalidade não-core quebrada, UI/UX issue, performance < 50% | **< 5 dias úteis** | Export PDF falha, Filtro busca não funciona, Toast não aparece |
| **P3 - Low** | Cosmético, documentação, melhoria, tech debt | **Próxima sprint** | Typos, cores inconsistentes, logs verbosos |

### 8.2 Métricas de Qualidade (Dashboard Grafana)

| Métrica | Target | Fonte |
| :--- | :--- | :--- |
| **Defect Escape Rate** | < 5% (bugs encontrados em prod vs total) | Jira/GitHub Issues |
| **MTTD (Mean Time to Detect)** | < 2 horas | Prometheus Alerts + Sentry |
| **MTTR (Mean Time to Resolve)** | P0: < 2h, P1: < 24h, P2: < 5d | Jira |
| **Change Failure Rate** | < 10% (deployments causing incidents) | ArgoCD + Incident Tracker |
| **Deployment Frequency** | ≥ 1/dia (staging), ≥ 1/semana (prod) | ArgoCD |
| **Test Coverage Trend** | ↗ (não decrescer) | JaCoCo + Vitest History |
| **Flaky Test Rate** | < 1% (testes instáveis) | CI History |
| **Performance Regression** | P95 latency não aumentar > 10% | k6 History |

---

## 8.3 Test Reporting & Artifacts

| Artefato | Ferramenta | Localização | Retenção |
| :--- | :--- | :--- | :--- |
| **Unit/Integration Test Report** | Surefire/Failsafe + JUnit XML | `target/surefire-reports/`, `target/failsafe-reports/` | 30 dias (CI artifacts) |
| **Coverage Report** | JaCoCo HTML / Vitest V8 HTML | `target/site/jacoco/`, `frontend/coverage/` | 30 dias |
| **Contract Test Results** | Spring Cloud Contract / Pact | `target/contracts/`, `frontend/pact/` | 30 dias |
| **E2E Test Report** | Cypress Mochawesome / Playwright HTML | `frontend/cypress/reports/`, `frontend/playwright-report/` | 30 dias |
| **Performance Report** | k6 HTML / Grafana Dashboard | `tests/performance/report.html`, Grafana | 90 dias |
| **Security Scan Reports** | SpotBugs XML, Semgrep SARIF, Trivy JSON, ZAP HTML | CI Artifacts / GitHub Security Tab | 90 dias |
| **Accessibility Report** | axe-core JSON / Cypress Screenshots | `frontend/cypress/a11y-report.json` | 30 dias |
| **Chaos Experiment Report** | LitmusChaos / Toxiproxy Logs | `chaos/reports/` | 90 dias |

---

## 9. Rastreabilidade Testes ↔ Artefatos

| Test Type | Use Cases | User Stories | API Spec | State Machine | Component | CI Job |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Unit (Backend)** | UC-01 a UC-45 | US-CAD-*, US-ASV-*, US-ORD-*, US-PRE-*, US-PRD-*, US-BUS-*, US-SEC-*, US-REL-*, US-LGD-* | — | Ordem FSM, Ativo FSM, Preventiva FSM, Preditiva FSM, Usuario FSM | Services, Utils, Levenshtein, OLS | `backend-unit-tests` |
| **Integration (Backend)** | UC-01 a UC-45 | US-CAD-*, US-ASV-*, US-ORD-*, US-PRE-*, US-PRD-*, US-BUS-*, US-SEC-*, US-REL-*, US-LGD-* | `/api/v1/**` | Todos FSMs | Controllers, Repositories, Security, Schedulers, Envers | `backend-integration-tests` |
| **Contract** | UC-01 a UC-45 | US-CAD-*, US-ASV-*, US-ORD-*, US-PRE-*, US-PRD-*, US-BUS-*, US-SEC-*, US-REL-*, US-LGD-* | `/api/v1/**` (OpenAPI) | — | Controllers (Producer), Stores/API (Consumer) | `contract-tests` |
| **Unit (Frontend)** | UC-13 a UC-20, UC-25 a UC-31, UC-32 a UC-38 | US-ORD-*, US-PRD-*, US-BUS-*, US-SEC-* | — | Ordem FSM, Preditiva FSM, Usuario FSM | Stores, Composables, Utils, Validators | `frontend-unit-tests` |
| **E2E (Frontend)** | UC-06 a UC-12, UC-13 a UC-20, UC-21 a UC-28, UC-29 a UC-31, UC-32 a UC-38, UC-39 a UC-42 | US-ASV-*, US-ORD-*, US-PRE-*, US-PRD-*, US-BUS-*, US-SEC-*, US-REL-*, US-LGD-* | `/api/v1/**` (via MSW/Backend) | Todos FSMs (UI State Mapping) | Pages, Components, Router Guards, Pinia Stores | `frontend-e2e-tests` |
| **Performance** | UC-13 a UC-20, UC-25 a UC-28, UC-29 a UC-31 | US-ORD-*, US-PRD-*, US-BUS-* | `/api/v1/ativos`, `/ordens`, `/busca`, `/dashboard/*` | — | — | `performance-tests` |
| **Security** | UC-32 a UC-38, UC-43 a UC-45 | US-SEC-*, US-LGD-* | `/auth/*`, `/admin/*`, `/auditoria/*` | Usuario FSM | SecurityConfig, AegisShield, MultiTenancyFilter | `security-tests` |
| **Accessibility** | Todos UC Frontend | Todos US Frontend | — | UI State Mapping | Todos Components/Pages | `a11y-tests` |
| **Chaos/Resilience** | UC-13 a UC-20, UC-25 a UC-28 | US-ORD-*, US-PRD-* | `/api/v1/**` | Ordem FSM, Preditiva FSM | Circuit Breaker, Retry, Fallback | `chaos-tests` |

---

*Documento regenerado completamente com base em análise AST completa do backend Java (domain, service, controller, security, predictive, search, audit, scheduler) + frontend Vue/PWA + infra K8s/Docker/TestContainers. Substitui versão 1.0 que continha apenas visão frontend vanilla JS + MSW mocks.*