# Story Map & Slicing Strategy — Aegis1

> Baseado na metodologia de *User Story Mapping* (Jeff Patton). O eixo horizontal representa a jornada do usuário; o eixo vertical representa a priorização por release/slice.

## 1. Backbone (Atividades do Usuário)
* Gerenciar Cadastros Mestres → Criar Ordem de Manutenção → Executar Ordem (Iniciar/Concluir) → Aprovar Ordem → Visualizar Custos por Ativo

## 2. Horizontal Axis (User Journey Steps)
| Etapa | Objetivo do Usuário | Tarefas Associadas |
| :--- | :--- | :--- |
| **Gerenciar Cadastros Mestres** | Manter dados de referência (departamentos, filiais, fornecedores, funcionários) consistentes para uso nas ordens | CRUD completo: criar, listar, buscarPorId, atualizar, deletar para cada entidade mestra |
| **Criar Ordem de Manutenção** | Registrar nova solicitação de manutenção com ativo, descrição, prioridade, técnico responsável | Preencher formulário, selecionar ativo/fornecedor/funcionário dos cadastros, submeter → estado "Aberta" |
| **Executar Ordem (Iniciar/Concluir)** | Técnico registra início da execução e posterior conclusão com materiais/horas gastas | Botão "Iniciar" (valida BR-01: estado Aberta + técnico alocado) → Botão "Concluir" (após aprovação, BR-03) |
| **Aprovar Ordem** | Supervisor valida execução com evidências e autoriza fechamento | Revisar checklist/fotos → Botão "Aprovar" (habilitado apenas quando API retorna `canApprove: true`, BR-02) |
| **Visualizar Custos por Ativo** | Gestor consulta custoTotalPorAtivo consolidado para decisões de substituição/preventiva | Dashboard/lista de ativos com custoTotalPorAtivo (soma de ordens concluídas, BR-04), filtros por período/filial |

```mermaid
graph LR
    A[Gerenciar Cadastros Mestres] --> B[Criar Ordem de Manutenção]
    B --> C[Executar Ordem<br/>Iniciar/Concluir]
    C --> D[Aprovar Ordem]
    D --> E[Visualizar Custos por Ativo]
    C -.->|Cancelar (qualquer estado<br/>exceto Concluída, BR-03)| B
```

## 3. Vertical Axis (MoSCoW Slicing)

### Slice 1: MVP (Must Have) — Release 1
* **Objetivo do slice:** Entregar jornada ponta-a-ponta funcional mínima: cadastros essenciais → criar ordem → iniciar → aprovar → concluir → visualizar custoTotalPorAtivo, com integração API funcional e sem console logs em produção.
* **Stories:**
  - **[REQ-CAD-001]** — CRUD Departamentos (criar, listar, buscarPorId, atualizar, deletar) — *Gerenciar Cadastros Mestres*
  - **[REQ-CAD-002]** — CRUD Filiais (criar, listar, buscarPorId, atualizar, deletar) — *Gerenciar Cadastros Mestres*
  - **[REQ-CAD-003]** — CRUD Fornecedores (criar, listar, buscarPorId, atualizar, deletar) — *Gerenciar Cadastros Mestres*
  - **[REQ-CAD-004]** — CRUD Funcionários (criar, listar, buscarPorId, atualizar, deletar) — *Gerenciar Cadastros Mestres*
  - **[REQ-ORD-001]** — Criar Ordem de Manutenção (formulário com validação, submissão → estado "Aberta") — *Criar Ordem de Manutenção*
  - **[REQ-ORD-002]** — Listar/Buscar Ordens (com filtros básicos: status, filial, ativo) — *Criar Ordem de Manutenção*
  - **[REQ-ORD-003]** — Iniciar Ordem (botão habilitado apenas se estado="Aberta" e técnico alocado, BR-01) — *Executar Ordem*
  - **[REQ-ORD-004]** — Aprovar Ordem (botão habilitado apenas quando `canApprove: true` da API, BR-02) — *Aprovar Ordem*
  - **[REQ-ORD-005]** — Concluir Ordem (apenas após aprovação, BR-03) — *Executar Ordem*
  - **[REQ-ORD-006]** — Cancelar Ordem (qualquer estado exceto "Concluída", BR-03) — *Executar Ordem*
  - **[REQ-CUST-001]** — Exibir custoTotalPorAtivo (lista de ativos com soma de custos de ordens concluídas, BR-04) — *Visualizar Custos por Ativo*
  - **[REQ-TEC-001]** — Refatorar `request` em `api.js` (complexidade ciclomática 13 → funções menores: retry, timeout, parsing) — *Transversal (qualidade)*
  - **[REQ-TEC-002]** — Remover 3 chamadas `console.*` residuais em `api.js` (linhas 26, 49, 52) — *Transversal (qualidade)*
  - **[REQ-TEC-003]** — Implementar `authInterceptor` com JWT + refresh automático + retry único (BR-06) — *Transversal (integração)*
  - **[REQ-TEC-004]** — Tratamento de erro `handleApiError` com mapeamento 409 (exclusão bloqueada, BR-05) e exibição amigável — *Transversal (integração)*

### Slice 2: Should Have — Release 2
* **Objetivo do slice:** Melhorar usabilidade, rastreabilidade e robustez operacional para adoção em larga escala.
* **Stories:**
  - **[REQ-CAD-005]** — Validação de duplicidade no frontend (nome/código único) antes de submeter CRUD mestres — *Gerenciar Cadastros Mestres*
  - **[REQ-ORD-007]** — Histórico de estados da ordem (timeline: Aberta → Iniciada → Aprovada → Concluída) — *Executar Ordem / Aprovar Ordem*
  - **[REQ-ORD-008]** — Atribuição/reatribuição de técnico responsável na ordem (modal de busca funcionários) — *Criar Ordem de Manutenção*
  - **[REQ-ORD-009]** — Campos de custo direto na conclusão: horas homem, materiais, terceiros (enviados à API para cálculo backend) — *Executar Ordem*
  - **[REQ-CUST-002]** — Filtros avançados no custoTotalPorAtivo: período, filial, departamento, fornecedor — *Visualizar Custos por Ativo*
  - **[REQ-CUST-003]** — Exportar custoTotalPorAtivo para CSV/Excel — *Visualizar Custos por Ativo*
  - **[REQ-TEC-005]** — Testes de contrato (Pact) para endpoints de ordens e custoTotalPorAtivo — *Transversal (qualidade)*
  - **[REQ-TEC-006]** — Pipeline de build com regra ESLint `no-console` (falha se `console.*` no bundle) — *Transversal (qualidade)*

### Slice 3: Could Have — Backlog
* **Objetivo do slice:** Funcionalidades de valor incremental para maturidade do produto, não bloqueantes para MVP.
* **Stories:**
  - **[REQ-ORD-010]** — Checklists digitais na ordem (itens configuráveis por tipo de manutenção) — *Executar Ordem*  [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: backend suportará estrutura de checklist; frontend apenas renderiza e coleta assinatura.
  - **[REQ-ORD-011]** — Anexos fotográficos na aprovação/conclusão (upload para backend, preview no frontend) — *Aprovar Ordem / Executar Ordem*  [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: backend expõe endpoint de upload; frontend usa input file + preview.
  - **[REQ-ORD-012]** — Agendamento de manutenção preventiva recorrente (calendário, geração automática de ordens) — *Criar Ordem de Manutenção*  [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: backend tem motor de agendamento; frontend apenas configura recorrência.
  - **[REQ-CUST-004]** — Gráfico de evolução de custo por ativo (série temporal) — *Visualizar Custos por Ativo*  [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: backend expõe endpoint de série temporal; frontend usa biblioteca de gráficos leve (ex: Chart.js via CDN).
  - **[REQ-TEC-007]** — PWA: manifest, service worker para offline-first (leitura de ordens/cache de cadastros) — *Transversal*  [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: stack atual (JS vanilla + Popper) permite SW sem build complexo.

### Won't Have (Neste Ciclo)
* **App mobile nativo** — Fora do escopo (BRD: PWA/responsivo apenas).
* **Módulo de compras/estoque de peças** — Fora do escopo (BRD: apenas fornecedores como cadastro).
* **Relatórios avançados/BI** — Fora do escopo (BRD: apenas indicadores citados).
* **Multi-idioma e white-label** — Future Considerations (BRD).
* **Integração com sensores IoT** — Future Considerations (BRD).

## 4. Mapa Visual (Matriz Completa)
| | Gerenciar Cadastros Mestres | Criar Ordem de Manutenção | Executar Ordem (Iniciar/Concluir) | Aprovar Ordem | Visualizar Custos por Ativo |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **MVP** | REQ-CAD-001 a REQ-CAD-004 | REQ-ORD-001, REQ-ORD-002 | REQ-ORD-003, REQ-ORD-005, REQ-ORD-006 | REQ-ORD-004 | REQ-CUST-001 |
| **MVP (Técnico)** | REQ-TEC-001 a REQ-TEC-004 | | | | |
| **Should** | REQ-CAD-005 | REQ-ORD-008 | REQ-ORD-007, REQ-ORD-009 | | REQ-CUST-002, REQ-CUST-003 |
| **Should (Técnico)** | | | | | REQ-TEC-005, REQ-TEC-006 |
| **Could** | | REQ-ORD-012 | REQ-ORD-010, REQ-ORD-011 | REQ-ORD-011 | REQ-CUST-004 |
| **Could (Técnico)** | | | | | REQ-TEC-007 |

## 5. Critérios de Corte entre Releases
* **MVP "Pronto para Lançar"**: Jornada ponta-a-ponta funcional (cadastro mínimo → criar → iniciar → aprovar → concluir → ver custo) **sem erros de integração** (handleApiError < 1%), **sem console logs** no bundle, **authInterceptor** com refresh testado, **request** refatorado (complexidade ≤ 5 por função).
* **Slice 2 "Pronto para Lançar"**: MVP + histórico de estados + atribuição de técnico + custos na conclusão + filtros/export de custo + testes de contrato no CI + build falha se `console.*`.
* **Nenhum slice avança** se a integração com backend (fora deste repositório) não estiver estável nos endpoints correspondentes — critério de dependência externa (BRD Seção 7).

## 6. Riscos de Slicing
* **Risco 1:** MVP pode gerar experiência degradada se backend não entregar `canApprove: true` corretamente (BR-02) — *Mitigação:* Mockar API no frontend para desenvolvimento paralelo; definir contrato OpenAPI antecipado (BRD Risco 1).
* **Risco 2:** `request` com complexidade 13 gera bugs de integração silenciosos (timeout, parsing, retry) — *Mitigação:* Refatorar **obrigatoriamente no MVP** (REQ-TEC-001) antes de testes integrados (BRD Risco 2).
* **Risco 3:** Console logs em produção expõem tokens/dados sensíveis — *Mitigação:* Regra ESLint `no-console` no pipeline de build (REQ-TEC-006 no Should, mas **validar no MVP** via revisão manual).
* **Risco 4:** Cálculo de `custoTotalPorAtivo` divergente entre frontend/backend — *Mitigação:* Contrato de API define fórmula; teste de contrato Pact no Should (REQ-TEC-005) (BRD Risco 5).
* **Risco 5:** Exclusão de entidades mestras (BR-05) retorna 409 mas frontend não trata — *Mitigação:* `handleApiError` mapeia 409 → mensagem "Não é possível excluir: existem ordens vinculadas" no MVP (REQ-TEC-004).