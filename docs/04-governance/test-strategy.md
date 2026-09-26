# Test Strategy — Aegis1 Módulo de Manutenção

> **Versão:** 1.0 · **Owner:** QA Lead / Eng Lead · **Status:** Draft

## 1. Objectives & Quality Goals
* Garantir zero regressões críticas (P0) em produção nos fluxos de ordens de manutenção (US-ORD-01 a US-ORD-05) e cadastros mestres (US-CAD-01 a US-CAD-20) <!-- source: user-stories#US-ORD-01 -->
* Reduzir tempo médio de detecção de bugs (MTTD) para < 2 horas via execução contínua em CI e monitoramento de erro em staging <!-- source: user-stories#NFR-Taxa-Erro-API -->
* Validar conformidade com regras de negócio BR-01 a BR-06 em 100% dos cenários BDD documentados nas user stories <!-- source: user-stories#Business Rules Referenciadas -->
* Assegurar latência P95 < 800 ms em todas as chamadas `api.js:request` sob carga simulada <!-- source: user-stories#NFR-Latência-P95 -->
* Detectar vazamento de dados sensíveis no frontend (tokens, PII) via testes de segurança automatizados <!-- source: user-stories#NFR-Segurança-Dados -->
* Garantir consistência de custos (`custoTotalPorAtivo`) entre backend e frontend via testes de contrato Pact <!-- source: user-stories#NFR-Consistência-Custos -->

## 2. Test Pyramid
| Camada | Cobertura Mínima | Ferramenta | Frequência de Execução | Responsável |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | 85% linhas / 80% branches (crítico: 95% em `api.js:request`, `handleApiError`, `authInterceptor`, validações frontend) | Vitest + @testing-library/dom (JS vanilla) | A cada commit (pre-push hook + CI) | Dev |
| **Integration Tests** | 100% dos fluxos de API mockados (MSW) cobrindo: authInterceptor + refresh + retry único (BR-06), handleApiError (409, 401, 404, 5xx, timeout), paginação/filtros ordens, cache listas mestras (<1s) | Vitest + MSW (Mock Service Worker) | A cada PR (pipeline obrigatório) | Dev |
| **E2E Tests** | 100% das jornadas críticas (Happy Path + 1 erro por story): criar ordem (BR-01), iniciar (BR-01), aprovar (BR-02), concluir (BR-03/BR-04), cancelar (BR-03), listar/filtrar/paginar, dashboard custos, CRUD 4 entidades mestres | Playwright (Chromium, Firefox, WebKit) | A cada deploy para staging + nightly | QA |
| **Contract Tests** | Todos os endpoints públicos consumidos pelo frontend: `POST/GET/PUT/DELETE /api/{departamentos,filiais,fornecedores,funcionarios,ordens,ativos}` + endpoints de transição (`/iniciar`, `/aprovar`, `/concluir`, `/cancelar`) | Pact (consumer-driven) | A cada release candidate + CI do provider (backend) | Dev + Backend |

## 3. Test Environments
| Ambiente | Propósito | Dados | Acesso |
| :--- | :--- | :--- | :--- |
| **Local** | Desenvolvimento e debug de testes unitários/integração | MSW handlers + fixtures JSON (departamentos, filiais, fornecedores, funcionários, ordens, ativos) versionados em `tests/fixtures/` | Dev (npm run test:unit, npm run test:integration) |
| **CI (GitHub Actions/GitLab CI)** | Validação automatizada de PRs | Banco efêmero SQLite em memória (se backend roda no CI) ou MSW puro para frontend isolado; seeds determinísticos | Pipeline (obrigatório para merge) |
| **Staging** | Homologação pré-produção com dados realistas anonimizados | Dump anonimizado de produção (scripts `scripts/anonymize-dump.sql` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: existe pipeline de anonimização) | QA, PO, Eng Lead (URL protegida por VPN/SSO) |
| **Produção** | Smoke tests pós-deploy + monitoramento sintético | Dados reais (somente leitura para sintéticos) | Automação (Playwright agendado a cada 15 min) + Alertas Datadog/New Relic |

## 4. Coverage Targets
* **Mínimo global:** 85% statements / 80% branches / 80% functions / 85% lines (enforced no CI via `vitest --coverage`)
* **Crítico (regras de negócio BR-01 a BR-06 + `api.js`):** 95% statements / 90% branches — arquivos: `frontend/src/services/api.js`, componentes de formulário de ordem, validações de permissão (ownership/role) <!-- source: frontend/src/services/api.js#L36 -->
* **Exclusões justificadas:** 
  * `frontend/src/main.js` (bootstrap, sem lógica testável)
  * Arquivos de configuração (vite.config.js, .eslintrc)
  * Código gerado (se houver) — não detectado no diagnóstico
  * `console.error/log` residuais em `api.js:26,49,52` — serão removidos antes de produção <!-- source: frontend/src/services/api.js#L26 --><!-- source: frontend/src/services/api.js#L49 --><!-- source: frontend/src/services/api.js#L52 -->

## 5. Non-Functional Testing
* **Performance:** 
  * Ferramenta: **k6** (scripts em `tests/performance/`)
  * Cenários: 
    * Carga basal: 50 VUs, ramp 2 min, duração 10 min — endpoints: `GET /api/ordens` (lista), `POST /api/ordens`, `PATCH /api/ordens/{id}/iniciar|aprovar|concluir|cancelar`, `GET /api/ativos/custos-totais`
    * Pico: 200 VUs por 5 min (simula horário de abertura de plantão)
  * Critérios de aceite: P95 < 800 ms (NFR-Latência-P95), taxa de erro < 1% (NFR-Taxa-Erro-API), throughput > 100 req/s em lista paginada
  * Frequência: A cada release candidate + weekly em staging
* **Segurança:**
  * **SAST:** Semgrep (regras OWASP Top 10 + custom: detecção de `localStorage.setItem('token')`, `console.log` com dados sensíveis) — a cada commit (CI)
  * **DAST:** OWASP ZAP Baseline Scan contra staging — nightly
  * **Dependências:** `npm audit` + Snyk/Dependabot — daily
  * **Validação específica:** Teste automatizado de que `authInterceptor` não vaza token em logs/erros (mocka erro 500 e verifica ausência de Authorization no console) <!-- source: user-stories#BR-06 -->
* **Acessibilidade:**
  * Ferramenta: **axe-core** integrado nos testes E2E (Playwright + @axe-core/playwright)
  * Critérios: WCAG 2.1 AA — zero violações críticas/severas em páginas de alta frequência (lista ordens, detalhe, formulários CRUD, dashboard)
  * Frequência: A cada PR (E2E roda axe nas páginas visitadas)
* **Resiliência/Chaos:**
  * Testes de falha induzida via MSW (integração) e Toxiproxy (staging):
    * Latência injetada (2s, 5s, 30s timeout) — valida toast "Erro de conexão" e retry
    * Falha intermitente 5xx (10%, 50%) — valida `handleApiError` e preservação de estado
    * Expiração de token + refresh falho — valida redirect login + `sessionStorage` recovery (BR-06) <!-- source: user-stories#BR-06 -->
  * Frequência: A cada sprint (staging) + ad-hoc em incidentes

## 6. Test Data Management
* **Estratégia de geração:**
  * **Fixtures JSON versionados** em `tests/fixtures/` para cada entidade: `departamentos.json`, `filiais.json`, `fornecedores.json`, `funcionarios.json`, `ordens.json`, `ativos.json` — cobrem cenários BDD (válidos, duplicados, vazios, com vínculos, sem vínculos)
  * **Factories TypeScript/JS** (`tests/factories/`) para geração dinâmica em testes de integração/E2E (ex: `createOrdem({ estado: 'ABERTA', responsavelId: currentUser })`)
  * **Seed scripts** para staging: `scripts/seed-staging.js` consome fixtures + randomização controlada (seed fixo por execução)
* **Dados sensíveis:**
  * **Obrigatório:** Anonimização/mascaramento fora de produção — CPF/CNPJ, matrículas, nomes reais, emails
  * **Implementação:** Script `scripts/anonymize-dump.sql` (PostgreSQL/MySQL — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: backend usa SQL relacional) roda no pipeline de staging deploy
  * **Tokens/JWT:** Nunca commitados; staging usa issuer de teste com chaves rotacionadas; local usa MSW com tokens JWT mockados (assinatura HS256 conhecida)

## 7. Regression Strategy
* **Suite de regressão:** 
  * **Nível 1 (CI - obrigatório):** Unit + Integration + Contract (tempo alvo < 10 min)
  * **Nível 2 (Staging deploy - obrigatório):** E2E crítico (12 jornadas: 5 workflow ordens + 4 CRUD mestres + listagem ordens + dashboard custos + login/auth) — tempo alvo < 25 min
  * **Nível 3 (Nightly - completo):** E2E full (todas as 20+ stories BDD + cenários de borda + acessibilidade + performance smoke) — tempo alvo < 60 min
* **Critérios de entrada de release (Definition of Ready para deploy em produção):**
  * [ ] CI verde (Nível 1) na branch de release
  * [ ] Staging deploy sucedido + Nível 2 verde
  * [ ] 0 bugs P0/P1 abertos no Jira/GitHub Issues vinculados à release
  * [ ] Performance k6 dentro dos SLAs na staging
  * [ ] Pact contracts verificados contra provider (backend) — `pact-verifier` passa
  * [ ] Security scan (Semgrep + ZAP) sem findings críticos/altos não mitigados
* **Critérios de saída (pós-deploy produção):**
  * [ ] Smoke tests sintéticos (Playwright agendado) passam por 30 min consecutivos
  * [ ] Error rate (Datadog/New Relic) < 0.5% nos primeiros 60 min
  * [ ] P95 latência < 800 ms nos primeiros 60 min

## 8. Bug Triage & Severity
| Severidade | Definição | SLA de correção | Exemplo no Aegis1 |
| :--- | :--- | :--- | :--- |
| **Crítica (P0)** | Sistema fora do ar / perda de dados / vazamento de token / quebra de BR-01/BR-03/BR-04 (custos inconsistentes) | Imediato (hotfix em < 4h) | `authInterceptor` vaza JWT no console; `custoTotalPorAtivo` diverge do backend; ordem "Concluída" pode ser cancelada |
| **Alta (P1)** | Funcionalidade principal quebrada para ≥ 1 perfil (gestor/técnico/aprovador) sem workaround | 24h (próximo deploy) | Botão "Iniciar" não aparece para técnico responsável; filtro de ordens não aplica `tecnicoId`; dashboard custos retorna 500 |
| **Média (P2)** | Funcionalidade secundária afetada / erro de UX não bloqueante / validação frontend faltando | Próxima sprint (máx 2 semanas) | Toast de sucesso não some automaticamente; paginação reseta ao filtrar; modal de cancelamento não valida max 500 chars |
| **Baixa (P3)** | Cosmético / melhoria / documentação / console.log residual | Backlog (triagem mensal) | `console.log` em `api.js:49,52`; tooltip truncado em card de ativo; ordenação por coluna não implementada (fora do MVP) |

**Processo de triagem:** Daily bug scrub (15 min) com QA Lead + Eng Lead + PO. Bugs P0/P1 entram no sprint atual (interrupção permitida). P2/P3 vão para backlog priorizado.

## 9. Reporting & Metrics
* **Dashboards (Grafana/Datadog):**
  * **Cobertura:** % statements/branches/functions/lines por camada (unit, integration, e2e) — trend semanal
  * **Taxa de falha de build:** % builds falhando por causa de testes (meta < 5%)
  * **Flakiness:** % testes E2E que falham intermitentemente (meta < 2%) — quarentena automática após 3 falhas em 10 runs
  * **Tempo de execução:** Unit < 3 min, Integration < 7 min, E2E crítico < 25 min, E2E full < 60 min
* **Métricas de qualidade acompanhadas:**
  * **Escape rate:** Bugs P0/P1 encontrados em produção / total P0/P1 (meta < 5%)
  * **MTTR (Mean Time To Recovery):** Tempo médio do deploy com bug P0 até hotfix em produção (meta < 4h)
  * **Defect density:** Bugs válidos por 1k LOC (frontend: 627 LOC — meta < 2 bugs/kLOC/sprint) <!-- source: Diagnóstico determinístico do codebase -->
  * **Test effectiveness:** % bugs encontrados em cada camada (meta: Unit 60%, Integration 25%, E2E 15%, Produção < 5%)
  * **Pact verification status:** % contratos verificados com sucesso (meta 100%)

## 10. Roles & Responsibilities
| Papel | Responsabilidade |
| :--- | :--- |
| **Dev** | Escrever e manter testes unitários (Vitest) e de integração (MSW) para código próprio; corrigir flakiness em testes próprios; garantir cobertura crítica ≥ 95% em `api.js` e validações de BR; executar suite Nível 1 localmente antes de push; participar de Pact consumer tests |
| **QA** | Desenhar, implementar e manter suíte E2E (Playwright) baseada nos cenários BDD das user stories; gerenciar fixtures/factories e dados de staging; executar triagem diária de bugs; rodar performance (k6) e segurança (ZAP) em staging; validar acessibilidade (axe) nos E2E; reportar métricas semanais |
| **Eng Lead** | Gate de qualidade em releases (aprova/blocka deploy baseado nos critérios de entrada); priorizar correção de P0/P1; revisar arquitetura de testes (ex: decisão de quebrar `api.js:request` complexidade 13 em funções menores para testabilidade) <!-- source: frontend/src/services/api.js#L36 -->; alinhar contratos Pact com Backend Lead; garantir remoção de `console.error/log` residuais antes de produção <!-- source: frontend/src/services/api.js#L26 --><!-- source: frontend/src/services/api.js#L49 --><!-- source: frontend/src/services/api.js#L52 --> |
| **Backend Lead (parceiro)** | Fornecer contratos OpenAPI/Pact provider verificados; manter ambiente de staging com dados anonimizados; implementar endpoints de transição (`/iniciar`, `/aprovar`, `/concluir`, `/cancelar`) conforme contratos acordados; corrigir divergências de `custoTotalPorAtivo` detectadas por Pact |
| **PO** | Validar critérios de aceite BDD em staging; priorizar bugs P2/P3 no backlog; aprovar exceções de cobertura (ex: funcionalidade deprecated) |