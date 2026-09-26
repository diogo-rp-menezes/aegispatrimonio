# Operations Runbook & System Maintenance Guide — Aegis1 Módulo de Manutenção

> **Owner:** Eng Lead / DevOps · **Última revisão:** 15/01/2025
> **Sistema de alerta:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: PagerDuty/Opsgenie integrado ao Datadog/New Relic/Sentry · **Canal de incidentes:** #incidents (Slack)

## 1. System Overview
* **Componentes críticos:**
  * **Frontend Estático (Aegis1):** Aplicação vanilla JS (ES modules) servida via CDN — bundle em `frontend/dist/` gerado por Vite (assumido por convenção). Entry point: `frontend/src/main.js`.
  * **Camada de API (frontend/src/services/api.js):** Cliente HTTP único para comunicação com backend externo (não gerenciado neste repositório). Função `request` (complexidade ciclomática 13) centraliza chamadas, autenticação (Bearer token em `localStorage`), tratamento de erros e refresh de token.
  * **Dependência UI:** `@popperjs/core` (tooltips/popovers) — única dependência de produção.
* **Diagrama de arquitetura:** Ver `system-architecture.md` (se existir) — arquitetura frontend-only + CDN + backend externo via REST.
* **Dependências externas críticas:**
  * **Backend API (fora do escopo deste repo):** Endpoints consumidos — `/auth/login`, `/auth/refresh`, `/ordens`, `/ativos`, `/custos`, `/dashboard`. Contratos validados via Pact (consumer-side). Base URL injetada via `VITE_API_BASE_URL`.
  * **CDN/Hosting Estático:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: GitHub Pages / Netlify / Vercel / S3+CloudFront — swap Blue-Green via alteração de origin/path.
  * **Identity Provider (SSO/VPN):** Acesso a Staging protegido por VPN/SSO corporativo.
  * **Monitoramento/APM:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: Datadog/New Relic/Sentry configurado para RUM (Real User Monitoring) + error tracking frontend.

## 2. Monitoring & System Health Check
| Métrica | Ferramenta | Threshold Saudável | Threshold de Alerta |
| :--- | :--- | :--- | :--- |
| Latência P95 (carregamento página / TTI) | Datadog/New Relic RUM | < 800 ms | > 1.5 s |
| Taxa de erro JS (exceções não tratadas) | Sentry / Datadog RUM | < 0.5% sessões | > 2% sessões |
| Taxa de erro HTTP 5xx (chamadas `api.js`) | Datadog/New Relic (frontend → backend) | < 0.5% | > 1% |
| Disponibilidade CDN (health check `index.html`) | Uptime monitor (Pingdom/Datadog Synthetic) | 100% | < 99.9% (5 min) |
| Tempo de build CI (GitHub Actions) | GitHub Actions Insights | < 10 min (Stages 1-3) | > 15 min |
| Cobertura de testes (CI) | Vitest + Coverage | ≥ 85% statements / 80% branches | < mínimos definidos no Deployment Plan |

* **Dashboards principais:**
  * **Frontend Health (RUM):** Latência, erros JS, Core Web Vitals, sessões ativas — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Link: `https://datadoghq.com/dashboard/aegis1-frontend`
  * **CI/CD Pipeline:** Status builds, tempo por stage, taxa de sucesso — `https://github.com/<org>/<repo>/actions`
  * **Backend API (consumido):** Latência, erro rate, throughput — responsabilidade do Backend Lead; frontend alerta apenas em erro 5xx/401 inesperado.
* **Health check endpoint:** `GET https://aegis1.exemplo.com/` (retorna `index.html` 200 OK) — validado em Stage 4 (Staging) e Stage 5 (Production) do Deployment Plan.
* **Métricas específicas de banco:** Não aplicável — este repositório não possui banco de dados. Ver `db-performance-recovery` (se existir no projeto backend).

## 3. On-Call & Escalation
| Nível | Papel | Tempo de resposta esperado | Condições de ativação |
| :--- | :--- | :--- | :--- |
| L1 | On-call Engineer (rotação semanal) | 15 min (horário comercial) / 30 min (fora horário) | Alerta P1: CDN down, erro JS > 2%, build CI falhando em `main`, staging deploy falhou |
| L2 | Tech Lead / Eng Lead | 30 min | L1 não resolve em 30 min OU incidente P0: vazamento de token (`localStorage`), `custoTotalPorAtivo` divergente, rollback necessário |
| L3 | Engineering Manager / Incident Commander | 1 h | Incidente se estende > 1 h, impacto multi-time, decisão de rollback de produção, comunicação externa necessária |

* **Rotação on-call:** Definida no calendário compartilhado (Google Calendar/Outlook) — handoff às segundas 10h BRT.
* **Runbook de handoff:** Checklist em `docs/oncall-handoff.md` (a criar) — inclui dashboards, contatos backend, chaves de deploy staging/prod.

## 4. Failure Recovery Procedures

### Incident Type 1: CDN / Hosting Estático Indisponível (P0)
* **Symptom:** Health check `GET /` falha (timeout ou 5xx) → alerta sintético Datadog/Pingdom dispara; usuários veem página em branco ou erro 502/503.
* **Possíveis Causas:**
  1. Provedor CDN com outage (Netlify/Vercel/GitHub Pages/S3+CloudFront)
  2. Deploy ruim propagado (arquivos corrompidos, `index.html` ausente)
  3. Certificado TLS expirado / configuração DNS incorreta
  4. Limite de banda/quotas excedido (plano gratuito)
* **Diagnóstico:**
  1. Verificar status page do provedor CDN (ex: `www.netlifystatus.com`, `www.githubstatus.com`).
  2. Acessar URL de staging (`https://staging-aegis1.exemplo.com`) — se staging sobe, problema é produção/CDN.
  3. Verificar último deploy em GitHub Actions → aba "Deployments" → confirmar artifact `frontend/dist/` gerado corretamente.
  4. `curl -I https://aegis1.exemplo.com/` → checar headers `x-nf-request-id`, `cf-ray`, `x-vercel-id` para identificar edge.
* **Remediation:**
  1. **Rollback imediato (alvo < 2 min):** No painel do provedor CDN, reverter para versão anterior (`sha-<previous>` ou tag `vX.Y.Z-1`). Ver Deployment Plan §5 — Blue-Green swap via origin/path.
  2. Se rollback CDN falhar: Disparar workflow `cd-production.yml` com `workflow_dispatch` + `ref: vX.Y.Z-1` (tag anterior) → rebuild + redeploy.
  3. Se provedor CDN down: Atualizar DNS (CNAME) para provedor alternativo (ex: Netlify → Vercel) — requer configuração prévia de multi-provider [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
  4. Comunicar no `#incidents`: "Rollback iniciado, ETA 2 min".
* **Verificação pós-remediação:** Health check `GET /` retorna 200; smoke test Playwright (login → lista ordens → detalhe → logout) passa em produção; RUM mostra 0% erro JS.
* **Severidade típica:** P0

### Incident Type 2: Erro JavaScript em Massa / Regressão de Build (P1)
* **Symptom:** Sentry/Datadog RUM alerta: taxa de erro JS > 2% nas últimas 5 min; stack trace aponta para `api.js:request` ou chunk Vite específico; usuários reportam "tela branca" ou "botões não funcionam".
* **Possíveis Causas:**
  1. Novo deploy (`main` merge) introduziu bug em `api.js` (ex: tratamento de 401/refresh token quebrado, `console.error` residual expõe stack).
  2. Chunk Vite com hash alterado não carregado (cache CDN stale + `index.html` novo).
  3. Backend API alterou contrato (breaking change) — Pact consumer tests não pegaram (provider não verificou).
  4. Variável de ambiente `VITE_API_BASE_URL` incorreta no build de produção.
* **Diagnóstico:**
  1. Abrir Sentry → filtrar por `release: sha-<current>` → ver top errors + affected users.
  2. Verificar `Network` tab em sessão afetada: requests para `VITE_API_BASE_URL` retornando 401/404/500? CORS error?
  3. Comparar `index.html` atual vs anterior: hashes dos chunks JS/CSS batem?
  4. Rodar `npm run build` local + `npm run preview` → reproduzir?
* **Remediation:**
  1. **Rollback de deploy (alvo < 5 min):** Mesmo procedimento do Incident Type 1 — reverter CDN para `sha-<previous>`.
  2. Se bug é em `api.js:request` (complexidade 13): Hotfix em branch `hotfix/<desc>`, PR com review acelerado, CI verde → `workflow_dispatch` production com tag `vX.Y.Z+1` (patch).
  3. Se contrato backend quebrado: Coordenar com Backend Lead → rollback backend OU deploy frontend com fallback graceful (ex: mostrar toast "serviço indisponível" em vez de crash).
  4. Limpar cache CDN (purge) se `index.html` novo aponta para chunks inexistentes — token `CDN_PURGE_TOKEN` em GitHub Secrets.
* **Verificação pós-remediação:** Taxa erro JS < 0.5% por 15 min consecutivos; jornada crítica (login → ordem → custo) funcional em staging e prod.
* **Severidade típica:** P1 (P0 se vazamento de token em console/error tracking)

### Incident Type 3: Falha de Autenticação / Token Expirado em Loop (P1)
* **Symptom:** Usuários logados redirecionados para login repetidamente; `api.js:request` loga `console.error` (linha 26) com "401 Unauthorized" em loop; `localStorage.token` presente mas inválido.
* **Possíveis Causas:**
  1. Backend rotacionou signing key JWT sem aviso → tokens existentes invalidados.
  2. Lógica de refresh token em `api.js:request` (linhas 36-80) falha: request de refresh retorna 401 → loop infinito.
  3. Relógio do cliente dessincronizado (raro) → token considerado expirado prematuramente.
* **Diagnóstico:**
  1. Verificar `api.js:request` fluxo: `try { response } catch (401) → refreshToken() → retry original`. Checar se `refreshToken()` limpa `localStorage` em falha.
  2. Inspect `localStorage` no DevTools: `token`, `refreshToken` presentes? Expiração (`exp` claim) decodificada (jwt.io) condiz?
  3. Verificar logs backend (responsabilidade Backend Lead) — rota `/auth/refresh` retornando 200 ou 401?
* **Remediation:**
  1. **Kill switch imediato:** Se feature flag implementada (ver Deployment Plan §3), desabilitar `autoRefreshToken` via `localStorage.setItem('feature:autoRefresh', 'false')` — força re-login limpo.
  2. Deploy hotfix corrigindo loop em `api.js:request` (adicionar `maxRetries`, backoff, clear storage em falha definitiva).
  3. Comunicar usuários: "Sessão expirada, por favor faça login novamente" — banner via `sessionStorage` flag.
* **Verificação pós-remediação:** Fluxo login → acesso ordens → refresh automático (após 14 min) funciona sem loop; 0 erros 401 em 30 min.
* **Severidade típica:** P1

### Incident Type 4: Pipeline CI/CD Bloqueado (P2)
* **Symptom:** GitHub Actions `ci.yml` falha em `main` → merge bloqueado; stages: Static Analysis (ESLint/Semgrep), Testes (Vitest/Pact), Build.
* **Possíveis Causas:**
  1. `console.error`/`console.log` residual em `api.js` (linhas 26, 49, 52) → Semgrep finding crítico.
  2. Cobertura de testes abaixo do threshold (85% statements / 80% branches).
  3. Contrato Pact não gerado / quebrado (mudança em `api.js` sem atualizar consumer test).
  4. `npm audit` vulnerabilidade `high`/`critical` em `@popperjs/core` ou dependência transitiva.
* **Diagnóstico:**
  1. Abrir run falho → logs por stage.
  2. Stage 1: `npm run lint` output → corrigir ESLint/Semgrep.
  3. Stage 2: `npm run test:coverage` → ver `coverage/lcov-report/index.html` (artifact CI).
  4. Stage 3: `npm run build` → erro Vite (ex: `import.meta.env.VITE_API_BASE_URL` undefined).
* **Remediation:**
  1. Fix no código → push para `main` (ou reverter commit problemático `git revert <sha>`).
  2. Se `npm audit`: `npm audit fix` ou atualizar `@popperjs/core` para versão patched.
  3. Se Pact: Atualizar `pacts/*.json` rodando `npm run test:contract` local → commit contratos.
* **Verificação pós-remediação:** CI verde em `main`; artifact `frontend/dist/` gerado; contratos Pact em `pacts/`.
* **Severidade típica:** P2 (bloqueia deploy, mas produção roda versão anterior)

## 5. Backup & Rollback Protocol
> Este projeto **não possui banco de dados** (ver Stack real: "nenhum motor de banco conhecido"). Estratégia de backup/RPO/RTO de banco vive no repositório backend (se houver artefato `db-performance-recovery` lá). Esta seção cobre **apenas rollback da aplicação frontend/deploy**.

* **Rollback de deploy (Produção):**
  * **Mecanismo:** Blue-Green via CDN — alterar origin/path para versão anterior (`sha-<previous>` ou tag `vX.Y.Z-1`).
  * **Comando/Procedimento:**
    1. GitHub Actions → workflow `cd-production.yml` → `Run workflow` → `ref: vX.Y.Z-1` (tag anterior) OU `sha: <short-sha-anterior>`.
    2. Alternativa (provedor CDN): Painel Netlify/Vercel/CloudFront → "Rollback to previous deploy" / "Promote previous deployment".
  * **Tempo alvo:** < 2 min (execução) + < 3 min (verificação health check + smoke) = **< 5 min total** (conforme Deployment Plan §5).
* **Rollback de deploy (Staging):** Automático — novo merge em `main` sobrescreve; para reverter, `git revert <sha>` + push `main` ou re-run workflow `cd-staging.yml` com commit anterior.
* **Backup de configuração/infraestrutura (fora do banco):**
  * **GitHub Actions Secrets:** `STAGING_DEPLOY_KEY`, `PROD_DEPLOY_KEY`, `CDN_PURGE_TOKEN`, `VITE_API_BASE_URL` (por env), `VITE_SENTRY_DSN` — backup via export manual trimestral (responsável: Eng Lead) → armazenado em 1Password/cofre da equipe.
  * **Workflows CI/CD:** Versionados no repo (`.github/workflows/*.yml`) — backup = clone do repo.
  * **Configuração CDN/DNS:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: Terraform/CloudFormation ou configuração manual documentada em `infra/` (fora deste repo) — backup via state file versionado.

## 6. Maintenance Windows
* **Janela padrão:** Terça a quinta, 10h–16h BRT (conforme Deployment Plan §5 — janela de deploy produção). **Não há janela de manutenção noturna/semanal** — aplicação estática sem downtime para deploy (Blue-Green).
* **Comunicação prévia necessária:**
  * **Deploy produção (features visíveis):** Aviso no `#releases` (Slack) + changelog atualizado **antes** do `workflow_dispatch` — mínimo 30 min (bake time staging).
  * **Hotfix P0 (bypass staging):** Comunicação **pós-deploy** imediata no `#incidents` + `#releases` — template: "Hotfix vX.Y.Z+1 deployado: [resumo]. Rollback: tag vX.Y.Z."
  * **Manutenção infraestrutura CDN/DNS:** 48h antecedência via e-mail + Slack `#infra` — coordenar com DevOps.

## 7. Runbook de Rotina (Checklists Operacionais)
- [ ] **Diário (início do dia útil):** Verificar dashboard "Frontend Health" — latência P95 < 800 ms, erro JS < 0.5%, sessões ativas dentro do esperado; checar CI `main` último run verde.
- [ ] **Diário (fim do dia):** Confirmar nenhum alerta P0/P1 aberto no PagerDuty/Opsgenie; revisar Sentry "New Issues" nas últimas 24h.
- [ ] **Semanal (segunda 10h BRT — handoff on-call):** Rodar checklist `docs/oncall-handoff.md` — dashboards, segredos, contatos backend, último deploy prod (tag/sha), PRs abertos críticos.
- [ ] **Semanal:** Revisar alertas silenciados no Datadog/Sentry — remover silenciamentos > 7 dias sem resolução.
- [ ] **Mensal (primeira terça):** Revisão de custos CDN/hosting + GitHub Actions minutes — comparar com baseline; rotacionar `STAGING_DEPLOY_KEY` / `PROD_DEPLOY_KEY` se > 90 dias (conforme Deployment Plan §8).
- [ ] **Por release (features visíveis):** Pós-deploy verification checklist (Deployment Plan §7) — health checks, métricas negócio, error rate < 0.5% 60 min, P95 < 800 ms 60 min, smoke tests 30 min, comunicação `#releases` + changelog.

## 8. Post-Incident Process
* **Postmortem obrigatório para:** Severidade **P0 e P1** (conforme Deployment Plan §7 + §8 — P0: erro > 1%, vazamento token, divergência custo; P1: regressão JS, auth loop).
* **Template de postmortem:** `docs/postmortem-template.md` (a criar) — seções obrigatórias:
  1. **Resumo executivo** (impacto, duração, usuários afetados)
  2. **Linha do tempo** (UTC + BRT) — detecção, diagnóstico, remediação, verificação
  3. **Causa raiz** (5 Whys) — ex: "console.error residual não removido → Semgrep bloqueou CI → hotfix feito às pressas sem teste de contrato → breaking change backend não detectado"
  4. **Ações de follow-up** (Jira/GitHub Issues) — owner, prazo, prioridade
     * Ex: "Remover console.* residuals em api.js" (Dev, 0.5h, P1)
     * Ex: "Refatorar api.js:request complexidade 13 → funções menores" (Dev, 4h, P2)
     * Ex: "Implementar feature flag para kill switch auth" (Dev, 8h, P2)
  5. **Lições aprendidas / Melhorias de processo**
* **Prazo para publicar postmortem:** **5 dias úteis** após resolução do incidente — publicado em Confluence/Notion/GitHub Wiki + link no `#incidents` e `#releases`.
* **Revisão de postmortems:** Mensal na retro de engenharia — métricas: MTTR, recorrência, % ações concluídas no prazo.

## 9. Contacts & Escalation Paths
| Sistema/Serviço | Responsável | Contato de Emergência |
| :--- | :--- | :--- |
| **Frontend Aegis1 (este repo)** | Eng Lead / On-call Engineer | Slack DM @oncall + PagerDuty rotation "aegis1-frontend" |
| **Backend API (consumido)** | Backend Lead | Slack #backend-oncall + PagerDuty "aegis1-backend" |
| **CDN / Hosting (Produção)** | DevOps / Cloud Engineer | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: PagerDuty "cdn-provider" + e-mail suporte enterprise |
| **CDN / Hosting (Staging)** | DevOps | Slack #infra-staging |
| **Identity Provider (SSO/VPN)** | SecOps / IT | Slack #it-support + telefone plantão |
| **Monitoramento (Datadog/New Relic/Sentry)** | Eng Lead | Slack @eng-lead + PagerDuty "observability" |
| **GitHub Actions / Secrets** | Eng Lead / DevOps | Slack #devops + GitHub org admin |
| **Pact Broker (se usado)** | Backend Lead | Slack #backend-contracts |

---

**Rastreabilidade com Deployment Plan (CI/CD & Release Plan):**
* Seção 2 (Monitoring) ↔ Deployment Plan §7 (Post-Deploy Verification: error rate, P95, health checks)
* Seção 4.1 (CDN Down) ↔ Deployment Plan §5 (Rollback Strategy: Blue-Green swap < 2 min)
* Seção 4.2 (JS Error/Regressão) ↔ Deployment Plan §2 Stage 2 (Coverage thresholds) + §3 (Build & Tagging) + §5 (Rollback)
* Seção 4.3 (Auth Loop) ↔ Deployment Plan §3 (Feature Flags — kill switch) + §8 (Secrets: VITE_API_BASE_URL)
* Seção 4.4 (CI Bloqueado) ↔ Deployment Plan §2 Stages 1-3 (Blocking criteria) + §9 Checklist (Remove console.*, Refactor api.js)
* Seção 5 (Rollback Protocol) ↔ Deployment Plan §5 (Rollback Strategy) + §8 (Secrets rotation)
* Seção 6 (Maintenance Windows) ↔ Deployment Plan §5 (Deploy window: Ter-Qui 10-16h BRT)
* Seção 7 (Rotina) ↔ Deployment Plan §7 (Post-Deploy Verification) + §8 (Secrets rotation 90 dias)
* Seção 8 (Post-Incident) ↔ Deployment Plan §7 (Critérios de saída) + §8 (Bug Severity P0/P1)