# Product Roadmap & Sprint History — Aegis1

> **Horizonte de planejamento:** Trimestral · **Última atualização:** 15/01/2025

## 1. Strategic Themes (Now / Next / Later)
### Now (Trimestre Atual — MVP / Release 1)
* **Jornada ponta-a-ponta funcional** — Entregar cadastros mestres → criar ordem → iniciar → aprovar → concluir → visualizar custoTotalPorAtivo com integração API estável, sem console logs em produção e com `request` refatorado (complexidade ≤ 5) <!-- source: story-map#L52-L54 -->
* **Qualidade de integração crítica** — Implementar `authInterceptor` (JWT + refresh automático + retry único, BR-06) e `handleApiError` com mapeamento 409 (BR-05) antes de testes integrados <!-- source: story-map#L64-L65 -->

### Next (Próximo Trimestre — Release 2 / Should Have)
* **Usabilidade e rastreabilidade operacional** — Histórico de estados da ordem, atribuição/reatribuição de técnico, custos diretos na conclusão, filtros avançados e export CSV/Excel do custoTotalPorAtivo <!-- source: story-map#L70-L78 -->
* **Robustez de entrega contínua** — Testes de contrato (Pact) para endpoints de ordens e custoTotalPorAtivo + pipeline de build com regra ESLint `no-console` (falha se `console.*` no bundle) <!-- source: story-map#L79-L80 -->

### Later (Explorando / Backlog Estratégico — Could Have)
* **Maturidade de produto** — Checklists digitais configuráveis, anexos fotográficos, agendamento de preventiva recorrente, gráfico de evolução de custo por ativo (série temporal) <!-- source: story-map#L84-L88 -->
* **Experiência offline-first** — PWA com manifest + service worker para leitura de ordens/cache de cadastros <!-- source: story-map#L89 -->

---

## 2. Upcoming Sprints
> **Premissas de capacidade** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]: Time de 3 devs frontend, velocidade histórica estimada em 21 pts/sprint (2 semanas), sprints iniciando às segundas. Pontuação baseada em Fibonacci (1,2,3,5,8,13). Datas alvo assumem início em 20/01/2025. **Validar com Product Owner e Tech Lead antes de comprometer.**

### Sprint 1 — 20/01 a 31/01
* **Meta Central (Sprint Goal):** Fundação de cadastros mestres (CRUD completo para Departamentos, Filiais, Fornecedores, Funcionários) + remoção de console logs + início da refatoração do `request` em `api.js` <!-- source: story-map#L55-L58 --><!-- source: story-map#L62-L63 -->
* **Capacidade do time:** 21 pts
* **Stories planejadas:**
  - [ ] REQ-CAD-001 — CRUD Departamentos (8 pts)
  - [ ] REQ-CAD-002 — CRUD Filiais (8 pts)
  - [ ] REQ-TEC-002 — Remover 3 chamadas `console.*` residuais em `api.js` (2 pts) <!-- source: story-map#L63 -->
  - [ ] REQ-TEC-001 (Parte 1) — Refatorar `request`: extrair `retryLogic` e `timeoutHandler` (5 pts) <!-- source: story-map#L62 -->
* **Riscos conhecidos:** Dependência de contrato OpenAPI do backend para tipagem das entidades mestres; se backend não entregar endpoints a tempo, mockar com MSW para não bloquear frontend.

### Sprint 2 — 03/02 a 14/02
* **Meta Central (Sprint Goal):** Concluir refatoração do `request` (parsing, error handling) + implementar `authInterceptor` (JWT + refresh + retry único, BR-06) + `handleApiError` com mapeamento 409 (BR-05) <!-- source: story-map#L64-L65 -->
* **Capacidade do time:** 21 pts
* **Stories planejadas:**
  - [ ] REQ-TEC-001 (Parte 2) — Refatorar `request`: extrair `responseParser` e `errorMapper`; garantir complexidade ≤ 5 por função (8 pts) <!-- source: story-map#L62 -->
  - [ ] REQ-TEC-003 — Implementar `authInterceptor` com JWT + refresh automático + retry único (BR-06) (8 pts) <!-- source: story-map#L64 -->
  - [ ] REQ-TEC-004 — Tratamento de erro `handleApiError` com mapeamento 409 (exclusão bloqueada, BR-05) e exibição amigável (5 pts) <!-- source: story-map#L65 -->
* **Riscos conhecidos:** Complexidade de sincronização do refresh token em requisições concorrentes; validar com backend contrato de `/auth/refresh`.

### Sprint 3 — 17/02 a 28/02
* **Meta Central (Sprint Goal):** Criar Ordem de Manutenção (formulário, validação, submissão → estado "Aberta") + Listar/Buscar Ordens com filtros básicos (status, filial, ativo) <!-- source: story-map#L59-L60 -->
* **Capacidade do time:** 21 pts
* **Stories planejadas:**
  - [ ] REQ-ORD-001 — Criar Ordem de Manutenção (formulário com validação, submissão → estado "Aberta") (8 pts) <!-- source: story-map#L59 -->
  - [ ] REQ-ORD-002 — Listar/Buscar Ordens (com filtros básicos: status, filial, ativo) (5 pts) <!-- source: story-map#L60 -->
  - [ ] REQ-CAD-003 — CRUD Fornecedores (5 pts) <!-- source: story-map#L56 -->
  - [ ] REQ-CAD-004 — CRUD Funcionários (3 pts) <!-- source: story-map#L57 -->
* **Riscos conhecidos:** Validação cruzada frontend/backend para campos obrigatórios; dependência de lista de ativos (não está no escopo de cadastros mestres do MVP — confirmar se ativos vêm de outro módulo ou são livres).

### Sprint 4 — 03/03 a 14/03
* **Meta Central (Sprint Goal):** Execução da ordem — Iniciar (BR-01), Aprovar (BR-02), Concluir (BR-03), Cancelar (BR-03) + Exibir custoTotalPorAtivo (BR-04) <!-- source: story-map#L61-L66 -->
* **Capacidade do time:** 21 pts
* **Stories planejadas:**
  - [ ] REQ-ORD-003 — Iniciar Ordem (botão habilitado apenas se estado="Aberta" e técnico alocado, BR-01) (5 pts) <!-- source: story-map#L61 -->
  - [ ] REQ-ORD-004 — Aprovar Ordem (botão habilitado apenas quando `canApprove: true` da API, BR-02) (5 pts) <!-- source: story-map#L61 -->
  - [ ] REQ-ORD-005 — Concluir Ordem (apenas após aprovação, BR-03) (3 pts) <!-- source: story-map#L61 -->
  - [ ] REQ-ORD-006 — Cancelar Ordem (qualquer estado exceto "Concluída", BR-03) (3 pts) <!-- source: story-map#L61 -->
  - [ ] REQ-CUST-001 — Exibir custoTotalPorAtivo (lista de ativos com soma de custos de ordens concluídas, BR-04) (5 pts) <!-- source: story-map#L66 -->
* **Riscos conhecidos:** 
  - **Risco 1 (Story Map):** Backend pode não entregar `canApprove: true` corretamente — mitigação: mockar API no frontend para desenvolvimento paralelo <!-- source: story-map#L94-L95 -->
  - **Risco 4 (Story Map):** Cálculo de `custoTotalPorAtivo` divergente entre frontend/backend — mitigação: contrato de API define fórmula; validar com backend antes da sprint <!-- source: story-map#L98-L99 -->

### Sprint 5 — 17/03 a 28/03 (Buffer / Hardening MVP)
* **Meta Central (Sprint Goal):** Testes integrados ponta-a-ponta, correção de bugs de integração, validação de critérios de "Pronto para Lançar" do MVP (sem erros de integração < 1%, sem console logs no bundle, authInterceptor testado, request refatorado) <!-- source: story-map#L90-L92 -->
* **Capacidade do time:** 21 pts
* **Stories planejadas:**
  - [ ] Testes integrados E2E (Cypress/Playwright) — jornada completa (8 pts)
  - [ ] Bug bash + correção de regressões (8 pts)
  - [ ] Validação manual de critérios de lançamento MVP (5 pts) <!-- source: story-map#L90-L92 -->
* **Riscos conhecidos:** Instabilidade de endpoints do backend (fora deste repositório) — critério de dependência externa (BRD Seção 7) <!-- source: story-map#L93 -->

---

## 3. Closed Sprints
> Nenhuma sprint concluída registrada neste repositório (projeto em fase inicial). Histórico será populado a partir da Sprint 1.

---

## 4. Milestones & Releases
| Milestone | Data Alvo | Escopo | Status |
| :--- | :--- | :--- | :--- |
| **MVP Release 1 (Launch Ready)** | 28/03/2025 | Jornada ponta-a-ponta: cadastros mestres → criar ordem → iniciar → aprovar → concluir → custoTotalPorAtivo; `request` refatorado (complexidade ≤ 5); `authInterceptor` + `handleApiError` funcionando; zero `console.*` no bundle | Planejado |
| **Release 2 (Should Have)** | 27/06/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Histórico de estados, atribuição de técnico, custos diretos na conclusão, filtros/export custoTotalPorAtivo, testes de contrato Pact, pipeline ESLint `no-console` | Planejado |
| **PWA / Offline-First (Could Have)** | 26/09/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Manifest, service worker para leitura offline de ordens/cache de cadastros | Explorando |

---

## 5. Velocity Trend
| Sprint | Planejado | Entregue | Observação |
| :--- | :--- | :--- | :--- |
| Sprint 1 | 21 pts | — | Início em 20/01/2025 |
| Sprint 2 | 21 pts | — |  |
| Sprint 3 | 21 pts | — |  |
| Sprint 4 | 21 pts | — |  |
| Sprint 5 | 21 pts | — | Buffer/hardening MVP |

---

## 6. Dependencies & Blockers Ativos
| Item | Bloqueado por | Dono | Previsão de resolução |
| :--- | :--- | :--- | :--- |
| Contrato OpenAPI dos endpoints de cadastros mestres (/departments, /branches, /suppliers, /employees) | Backend (fora do repo) | Tech Lead Backend | 17/01/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Contrato de `/auth/refresh` e formato do JWT | Backend (fora do repo) | Tech Lead Backend | 24/01/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Endpoint `canApprove: true` para ordens (BR-02) | Backend (fora do repo) | Tech Lead Backend | 10/02/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Fórmula oficial de `custoTotalPorAtivo` (BR-04) | Backend (fora do repo) | Product Owner + Backend | 17/02/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Estrutura de ativos (lista/seleção no formulário de ordem) | Backend / outro módulo | Product Owner | 20/01/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |