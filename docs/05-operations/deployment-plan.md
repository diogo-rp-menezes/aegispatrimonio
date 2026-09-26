# CI/CD & Release Plan — Aegis1 Módulo de Manutenção

> **Owner:** Eng Lead / DevOps · **Ferramenta de CI/CD:** GitHub Actions

## 1. Pipeline Overview
```mermaid
graph LR
    A[Push/PR] --> B[Static Analysis]
    B --> C[Unit + Integration Tests]
    C --> D[Build Frontend]
    D --> E[Contract Tests (Pact)]
    E --> F[Deploy Staging]
    F --> G[E2E Critical + Smoke]
    G --> H{Aprovação Manual}
    H -- Sim --> I[Deploy Produção]
    H -- Não/Automático --> I
    I --> J[Post-Deploy Verification]
```

## 2. Pipeline Stages

### Stage 1: Static Analysis
* **Ferramentas:** ESLint (regras recomendadas + plugin security), Semgrep (OWASP Top 10 + custom: detecção de `localStorage.setItem('token')`, `console.*` com dados sensíveis), `npm audit` (auditoria de dependências)
* **Arquivos alvo:** `frontend/src/**/*.js`, `frontend/*.js`
* **Critério de bloqueio:** Qualquer erro ESLint, finding crítico/alto Semgrep, vulnerabilidade `high`/`critical` no `npm audit` = pipeline vermelho
* **Tempo alvo:** < 3 min

### Stage 2: Automated Testing (Nível 1 — Obrigatório para Merge)
* **Escopo:** Unit Tests (Vitest) + Integration Tests (Vitest + MSW) + Contract Tests Consumer (Pact)
* **Cobertura mínima exigida:** 
  * Global: 85% statements / 80% branches / 80% functions / 85% lines
  * Crítico (`frontend/src/services/api.js`, validações de BR-01 a BR-06): 95% statements / 90% branches
* **Ferramentas:** Vitest + @testing-library/dom + MSW + @pact-foundation/pact
* **Artefatos gerados:** Relatórios de cobertura (HTML + LCOV), contratos Pact (`.json`) em `pacts/`
* **Critério de bloqueio:** Falha em qualquer teste, cobertura abaixo do mínimo, contrato Pact não gerado = pipeline vermelho
* **Tempo alvo:** < 10 min

### Stage 3: Build & Packaging
* **Comando:** `npm run build` (a ser definido — Vite assumido por convenção de projeto vanilla JS moderno)
* **Artefato gerado:** Bundle estático otimizado em `frontend/dist/` (HTML, JS minificado, CSS, assets)
* **Registry/Storage:** GitHub Actions Artifacts (retidos por 30 dias) + GitHub Pages / Netlify / Vercel / S3+CloudFront (conforme target de hospedagem — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: hospedagem estática em CDN)
* **Tagging:** 
  * `sha-<short-sha>` para cada run de CI
  * `latest` para `main` branch
  * `v<MAJOR>.<MINOR>.<PATCH>` para tags Git SemVer

### Stage 4: Deploy to Staging
* **Gatilho:** Automático em merge para `main` (após Stage 1-3 verdes)
* **Estratégia:** Deploy do bundle `frontend/dist/` para ambiente de staging (URL protegida por VPN/SSO)
* **Validação pós-deploy (automática, < 5 min):** 
  * Health check: `GET /health` ou carregamento da `index.html` retorna 200
  * Smoke test Playwright: login → lista ordens → abre detalhe → logout (1 jornada crítica)
* **Dados:** Dump anonimizado de produção (script `scripts/anonymize-dump.sql` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: backend usa SQL relacional e pipeline de anonimização existe)

### Stage 5: Deploy to Production
* **Gatilho:** Manual (workflow_dispatch) após aprovação em Staging + janela de bake time ≥ 30 min
* **Estratégia de rollout:** Blue-Green via CDN (swap de origin/path) — rollback instantâneo revertendo DNS/origin
* **Janela de deploy:** Terça a quinta, 10h–16h BRT (evitar segundas, sextas, feriados, fora de horário comercial)
* **Pré-requisitos (Definition of Ready):**
  * [ ] CI verde (Nível 1) na branch/tag de release
  * [ ] Staging deploy sucedido + E2E crítico (Nível 2) verde
  * [ ] 0 bugs P0/P1 abertos vinculados à release
  * [ ] Performance k6 dentro dos SLAs na staging (P95 < 800 ms, erro < 1%)
  * [ ] Pact contracts verificados contra provider (backend) — `pact-verifier` passa
  * [ ] Security scan (Semgrep + ZAP Baseline) sem findings críticos/altos não mitigados

## 3. Release Strategy
* **Versionamento:** SemVer (MAJOR.MINOR.PATCH) — tags Git `vX.Y.Z`
* **Cadência de release:** Contínua (deploy em produção a cada merge em `main` que passe gates) — batch opcional por sprint se negócio exigir
* **Feature Flags:** Não implementado no código atual — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: se necessário, usar LaunchDarkly/Unleash ou flags baseadas em `localStorage`/`sessionStorage` para rollout gradual de features frontend-only
* **Branching model:** Trunk-based development — `main` sempre deployável; feature branches de vida curta (< 2 dias); PRs obrigatórios com review + CI verde

## 4. Environments
| Ambiente | Propósito | URL | Deploy automático? | Dados |
| :--- | :--- | :--- | :--- | :--- |
| **Local** | Desenvolvimento | `http://localhost:5173` (Vite dev server) | N/A (dev) | MSW handlers + fixtures JSON versionados em `tests/fixtures/` |
| **CI (GitHub Actions)** | Validação de PR | N/A (ephemeral) | Sim (a cada push/PR) | MSW puro (frontend isolado) + seeds determinísticos |
| **Staging** | Homologação pré-produção | `https://staging-aegis1.exemplo.com` (VPN/SSO) | Sim (merge `main`) | Dump anonimizado de produção |
| **Production** | Usuários reais | `https://aegis1.exemplo.com` | Manual (workflow_dispatch) | Reais (somente leitura para sintéticos) |

## 5. Rollback Strategy
* **Gatilho de rollback:** 
  * Taxa de erro (5xx + JS errors) > 1% nos primeiros 10 min pós-deploy
  * Alerta crítico de negócio (ex: `custoTotalPorAtivo` divergente, vazamento de token)
  * Falha em smoke tests sintéticos agendados (Playwright a cada 15 min)
* **Mecanismo:** Reverter CDN/origin para versão anterior (`sha-<previous>` ou tag `vX.Y.Z-1`) — tempo alvo < 2 min
* **Kill switch complementar:** Feature flag (se implementada) para desabilitar funcionalidade problemática sem redeploy
* **Tempo alvo de rollback:** < 5 minutos (detecção + execução + verificação)

## 6. Approval Gates
| Gate | Ambiente | Aprovador | SLA |
| :--- | :--- | :--- | :--- |
| **Deploy Produção** | Production | Eng Lead + PO (conjunto) | ≤ 1h úteis após solicitação |
| **Hotfix P0** | Production | Eng Lead (pode aprovar sozinho) | Imediato (bypass staging se necessário, com Pact verification local) |

## 7. Post-Deploy Verification
- [ ] Health checks verdes (CDN + backend API reachable)
- [ ] Métricas de negócio dentro do esperado (ordens criadas/min, taxa de conclusão, dashboard custos carregando)
- [ ] Nenhum novo erro crítico em 30 min (Datadog/New Relic / Sentry — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: APM/error tracking configurado)
- [ ] Smoke tests sintéticos (Playwright agendado) passam por 30 min consecutivos
- [ ] Error rate < 0.5% nos primeiros 60 min
- [ ] P95 latência < 800 ms nos primeiros 60 min
- [ ] Comunicação de release enviada (Slack #releases + changelog atualizado) — se release com features visíveis

## 8. Secrets & Configuration Management
* **Ferramenta:** GitHub Actions Secrets (repository/organization) + `.env` files locais (não commitados)
* **Segredos por ambiente:**
  * **CI:** `NPM_TOKEN` (se publicar pacotes), `PACT_BROKER_TOKEN` (se usar Pact Broker), `SONAR_TOKEN` (se usar SonarCloud)
  * **Staging:** `STAGING_DEPLOY_KEY` (SSH/RSync/CLI do provedor), `STAGING_API_BASE_URL`
  * **Production:** `PROD_DEPLOY_KEY`, `PROD_API_BASE_URL`, `CDN_PURGE_TOKEN` (se invalidar cache)
* **Política de rotação:** 
  * Chaves de deploy: rotação a cada 90 dias ou após saída de membro da equipe
  * Tokens de API/serviços: conforme política do provedor (geralmente 90–365 dias)
  * JWT signing keys (backend): responsabilidade do Backend Lead — frontend apenas consome
* **Configuração não-secreta:** `vite.config.js` / `environment.js` com variáveis injetadas no build via `import.meta.env.VITE_*` (ex: `VITE_API_BASE_URL`, `VITE_SENTRY_DSN`)

---

## 9. Implementation Checklist (Próximos Passos)
*[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Itens abaixo não existem no codebase atual e precisam ser implementados para habilitar este pipeline.*

| Item | Descrição | Responsável | Estimativa |
| :--- | :--- | :--- | :--- |
| **package.json scripts** | Adicionar `test:unit`, `test:integration`, `test:e2e`, `test:contract`, `build`, `lint`, `audit` | Dev | 2h |
| **Vite config** | Configurar build otimizado (minify, hash assets, code splitting) + env vars | Dev | 2h |
| **Vitest + MSW setup** | Configurar testes unitários/integração com coverage thresholds | Dev | 4h |
| **Playwright setup** | Configurar E2E com fixtures, page objects, axe-core | QA | 8h |
| **Pact consumer tests** | Implementar testes de contrato para endpoints consumidos | Dev | 8h |
| **GitHub Actions workflows** | Criar `.github/workflows/ci.yml`, `cd-staging.yml`, `cd-production.yml` | Eng Lead | 8h |
| **Staging infra** | Provisionar ambiente staging (CDN + backend staging + DB anonimizado) | DevOps/Backend | 16h |
| **Production infra** | Configurar CDN production + Blue-Green swap mechanism | DevOps | 8h |
| **Monitoring/Alerting** | Configurar Datadog/New Relic/Sentry + dashboards + alertas P0 | Eng Lead | 8h |
| **Remove console.* residuals** | Limpar `console.error` (linha 26) e `console.log` (linhas 49, 52) em `api.js` | Dev | 0.5h |
| **Refactor api.js:request** | Quebrar função complexidade 13 em funções menores para testabilidade | Dev | 4h |

---

## 10. Rastreabilidade com Test Strategy
| Seção deste Plano | Origem no Test Strategy |
| :--- | :--- |
| Stage 2 (Unit/Integration/Contract) | Seção 2 (Test Pyramid) + Seção 4 (Coverage Targets) |
| Stage 4 (Staging Deploy + Smoke) | Seção 3 (Test Environments → Staging) + Seção 7 (Regression Strategy → Nível 2) |
| Stage 5 (Production Gates) | Seção 7 (Critérios de entrada de release) |
| Rollback Triggers | Seção 7 (Critérios de saída pós-deploy) + Seção 8 (Bug Severity P0) |
| Post-Deploy Verification | Seção 7 (Critérios de saída) + Seção 9 (Metrics: error rate, P95) |
| Secrets/Config | Seção 6 (Test Data Management → Tokens/JWT) + Seção 3 (Environments) |