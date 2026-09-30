# User Stories (BDD) — Sistema de Gestão de Patrimônio (A3)

> **Épico relacionado:** Gestão de Patrimônio — Gestão Organizacional (entidades mestres), Gestão de Manutenção (ciclo de solicitações) e Compliance/Auditoria · **Sprint:** não definida nas fontes — a definir em planning **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** · **Status geral:** Backlog

## Notas de Derivação e Verificação

- **Origem:** as stories abaixo foram derivadas dos casos de uso **UC-01..UC-04** (artefato *Use Cases*/A4, baseado no BRD — RF-01..RF-29, RN-01..RN-08, CA-01..CA-10, RNF-01..RNF-10) e da varredura determinística do codebase (**A5** — 350 arquivos, 27.537 LOC, 1268 funções, 344 classes). O BRD **não define User Stories** — os requisitos estão cobertos pelos RFs; portanto, todo o backlog abaixo é derivação rastreável, não escopo inventado.
- **Rotas/tabelas:** a varredura **não identificou nenhuma rota/endpoint explícito nem tabelas de banco de dados** no código. Por isso, os critérios de aceite são ancorados aos **contratos comportamentais definidos no BRD** (nomes dos testes de integração, ex.: `criar_comAdmin_deveRetornarCreated`) e aos **códigos de regra de negócio (RN-XX)**, em vez de caminhos de endpoint. Caminhos concretos de API devem ser confirmados no `api-specification.md` quando este for gerado.
- **Stack real (varredura de dependências):** as dependências reais capturadas limitam-se a `@popperjs/core` (produção); não foram encontrados motor de banco, ORM/query builder ou empacotamento Tauri. O codebase é composto por 335 arquivos `.java` em `src/` e 15 arquivos `.js` em `frontend/`. Requisitos do BRD que pressupõem capacidades além disso — ex.: RNF-01 (token JWT), RNF-06 (scaling horizontal) — são tratados como **metas do BRD pendentes de verificação no codebase**; nenhum mecanismo de infraestrutura não detectado é afirmado como existente.
- **Rótulos:** trechos marcados com **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** são hipóteses plausíveis derivadas do contexto (BRD + varredura), não fatos verificados; as premissas adotadas estão declaradas junto a cada trecho.
- **Códigos de regra:** os códigos **RN-XX** correspondem às Business Rules da seção 5 do BRD.

### Achados da varredura (A5) que afetam diretamente estas stories

- `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` — TODO de performance: busca carrega até 1000 candidatos (id+nome) e faz ranking → **US-002** (passo 2), **US-005**.
- `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` — método `toDTO` com complexidade ciclomática 14 (detalhes do ativo — RF-14) → **US-002**, **US-005**.
- `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` — método `build` com complexidade 14 (filtros dinâmicos da fila de manutenção) → **US-003**.
- `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96` — `checkResourceUsageAlerts` com complexidade 17 (único serviço de alerta detectado; ligação com a aprovação não estabelecida) → **US-003**.
- `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86` — `setUsername` com corpo vazio (stub) → **US-002** (RN-06), **US-007**.
- `frontend/src/services/api.js:44` e `:107` — chamadas residuais `console.error`/`console.debug` no cliente API usado por todos os fluxos → **US-001..US-004**, **US-007**.
- `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34` — seeder de dados realistas, método `run` com complexidade 15 → dados de teste dos fluxos (**US-001**, DoD).

## Visão Geral do Backlog

| ID | Story | UC | Prioridade (MoSCoW) | Pontos | Labels |
|----|-------|----|---------------------|--------|--------|
| US-001 | Administrador cadastra nova filial | UC-01 | Must | 3 | backend, frontend, ux |
| US-002 | Funcionário solicita manutenção de ativo | UC-02 | Must | 5 | backend, frontend, ux |
| US-003 | Aprovador autoriza manutenção | UC-03 | Must | 5 | backend, frontend, ux |
| US-004 | Técnico conclui ordem de serviço | UC-04 | Must | 5 | backend, frontend, ux |
| US-005 | Otimização da busca e filtragem de ativos | UC-02 | Should | 3 | backend, tech-debt, performance |
| US-006 | Audit trail de operações de escrita (RNF-07) | UC-01..UC-04 | Should | 5 | backend, infra, compliance, tech-debt |
| US-007 | Higiene de código: stub `setUsername` e logs residuais | UC-02 | Could | 1 | backend, frontend, tech-debt |

> Pontos de história em sequência Fibonacci, estimados por IA — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; premissas adotadas declaradas na seção de complexidade de cada story.

---

## US-001 — Administrador cadastra nova filial

* **Priority:** Must — filial é entidade mestre (RN-01) e pré-requisito para o rastreamento de ativos (RN-07) e para a busca por filial da UC-02 (RF-15).
* **Complexity:** 3 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: CRUD de entidade mestre com validação de campos obrigatórios (RN-03) e RBAC (RN-01/RN-02), sem dependência de outros cadastros (precondição do UC-01); fluxo de 5 passos.
* **Épico:** Gestão Organizacional (entidades mestres)
* **Labels:** backend, frontend, ux

### 1. Description (INVEST)
* **As a** Administrador do Sistema (role **Admin**)
* **I want to** cadastrar novas filiais informando nome, código, endereço e responsável
* **So that** ativos e departamentos possam ser vinculados e rastreados por unidade organizacional (RN-07)

*Checklist INVEST:* **Independent** — não depende de outros cadastros (precondição do UC-01); **Negotiable** — campos do formulário negociáveis com PO; **Valuable** — habilita o rastreamento por filial; **Estimable** — fluxo simples e contratos de teste definidos; **Small** — um formulário, uma escrita; **Testable** — contratos `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `criar_comDadosInvalidos_deveRetornarBadRequest`.

### 2. Business Context
Filial é a unidade organizacional básica do sistema: cada ativo pertence a **uma** filial (RN-07) e, sem filiais cadastradas, a busca de ativos por filial da UC-02 (RF-15) não tem base operacional. A story atende diretamente RF-06 (Criar Filial) e os critérios CA-01/CA-02 do BRD. É também pré-requisito da carga inicial de dados (risco R-02 do BRD — migração de legados), hoje exercitada em ambiente de teste pelo seeder `RealisticDataSeeder` (detectado na varredura A5).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Criação bem-sucedida de filial por Admin (caminho feliz)
* **Given** um usuário autenticado com role **Admin** e sessão válida (RN-01)
* **When** ele submete o formulário de criação com nome, código, endereço e responsável preenchidos (RN-03)
* **Then** o sistema persiste a filial e retorna **201 Created** (contrato `criar_comAdmin_deveRetornarCreated`; CA-01)
* **And** a filial fica disponível para associação a ativos e departamentos (RN-07)

#### Scenario 2: Cadastro de filial com código duplicado (caso de borda)
* **Given** uma filial com código "FIL-001" já persistida
* **When** o Admin submete uma nova filial com o mesmo código
* **Then** o sistema aceita a criação e retorna **201 Created**
* **And** nenhuma mensagem de duplicidade é exibida — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** premissa: RN-01..RN-08 não definem regra de unicidade para código/nome de filial (ver Open Issues do UC-01); caso o PO defina unicidade, este cenário deve ser revisado.

#### Scenario 3: Usuário sem role Admin tenta criar filial (erro)
* **Given** um usuário autenticado com role **User** e token válido
* **When** ele tenta submeter a criação de uma filial
* **Then** o sistema retorna **403 Forbidden** (contrato `criar_comUser_deveRetornarForbidden`; RN-02; CA-01)
* **And** nenhum dado é persistido

#### Scenario 4: Dados obrigatórios ausentes (erro)
* **Given** um usuário autenticado com role **Admin**
* **When** ele submete o formulário sem um ou mais campos obrigatórios (nome, código, endereço, responsável)
* **Then** o sistema retorna **400 Bad Request** com mensagem clara (contrato `criar_comDadosInvalidos_deveRetornarBadRequest`; RN-03; CA-02)
* **And** a escrita não é executada

#### Scenario 5: Sessão expirada durante o cadastro (erro)
* **Given** um usuário autenticado cujo token expirou (expiração de 1h — RNF-01 do BRD, meta pendente de verificação no codebase)
* **When** ele submete o formulário de criação
* **Then** o sistema retorna **401 Unauthorized** (CA-06)
* **And** o frontend limpa a sessão (`clearSession` — RF-25; CA-08) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: redirecionamento para login não especificado no BRD nem detectado na varredura]**

### 4. UI/UX Notes
* **Wireframe/Mockup:** não disponível nas fontes — design a criar/validar com PO. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** loading (envio via `services/api.js`), success (confirmação de criação), error (mensagens de 400/401/403), empty (formulário iniciado com campos vazios — estado inicial de preenchimento).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** cliente API do frontend `frontend/src/services/api.js` (função `request`, complexidade ciclomática 13 — responsável por injetar o token de autenticação, RF-24/CA-07); persistência de filiais no backend (nenhuma classe específica de filial foi destacada na varredura A5 — confirmar no codebase).
* **Dependências técnicas:** nenhuma rota/endpoint explícito detectado na varredura — caminho concreto da API a confirmar no `api-specification.md` (pendente); nenhum motor de banco ou ORM encontrado nas dependências varridas (apenas `@popperjs/core` em produção) — o mecanismo de persistência real não é discoverable a partir das fontes; dados de teste via `RealisticDataSeeder` (`src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java` — método `run` com complexidade 15).
* **Considerações de performance/segurança:** RNF-04 — resposta < 200ms (p95) na criação (meta do BRD; nenhuma instrumentação de métricas detectada na varredura); RNF-01 — token JWT com expiração de 1h (meta do BRD); RNF-07 — escrita deve gerar registro em audit trail imutável (meta do BRD; implementação não detectada — ver US-006); chamadas residuais `console.error` (`api.js:44`) e `console.debug` (`api.js:107`) devem ser removidas antes de produção (ver US-007).

### 6. Out of Scope
* Cadastro/edição de departamentos (encadeia para fluxo próprio — AS-2 do UC-01; RF-01).
* Edição e exclusão de filial (RF-06 cobre apenas criação).
* Regra de unicidade de código/nome de filial (não definida no BRD — ver Open Issues).
* Carga massiva de filiais legadas (risco R-02 do BRD — tratada à parte).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — rota/endpoint a confirmar no `api-specification.md` (pendente)
- [ ] Design/UX aprovado (se aplicável) — wireframe inexistente nas fontes
- [ ] Estimativa de esforço realizada — 3 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD (incl. permissão Admin vs User — CA-10)
- [ ] Testes de integração cobrindo os contratos `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `criar_comDadosInvalidos_deveRetornarBadRequest`
- [ ] Tipagem estrita (backend Java; frontend JavaScript — não há TypeScript nas dependências detectadas; validar contratos de payload)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas (`api-specification.md`)
- [ ] Feature testada em staging (com dados do `RealisticDataSeeder`)
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação detectada na varredura

### 9. Traceability
* **Use Cases relacionados:** UC-01
* **Requisitos funcionais:** RF-06 (Criar Filial); RF-24 (interceptador de auth); RF-25 (`clearSession`)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-01, RN-02, RN-03, RN-07
* **Critérios de aceitação (BRD):** CA-01, CA-02, CA-06, CA-08
* **NFRs relacionadas:** RNF-01, RNF-04, RNF-07, RNF-08
* **Riscos relacionados:** R-02 (migração de dados legados)

---

## US-002 — Funcionário solicita manutenção de ativo

* **Priority:** Must — núcleo do módulo de manutenção (RF-18); sem ela, UC-03 e UC-04 não existem (fila de pendentes vazia).
* **Complexity:** 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: envolve busca/filtragem de ativos (RF-15) + criação de solicitação + anexos opcionais; TODO de performance conhecido em `AtivoService.java:119` e complexidade de `AtivoMapper.toDTO` (14) no caminho exato deste fluxo.
* **Épico:** Gestão de Manutenção (ciclo Pendente → Aprovada → Em Andamento → Concluída — RN-05)
* **Labels:** backend, frontend, ux

### 1. Description (INVEST)
* **As a** Funcionário/Colaborador (role **User**)
* **I want to** buscar um ativo por filial, departamento ou localização e registrar uma solicitação de manutenção com descrição, prioridade e fotos opcionais
* **So that** a equipe de manutenção e os aprovadores possam qualificar e autorizar o reparo, e o histórico do ativo seja registrado (RF-22)

*Checklist INVEST:* **Independent** — executável mesmo sem filiais cadastradas (filtros retornam vazio), porém a filtragem por filial (RN-07) beneficia-se de US-001; **Negotiable** — valores permitidos para "prioridade" a negociar com PO (não definidos no BRD); **Valuable** — inicia o fluxo de manutenção; **Estimable** — fluxo de 6 passos com contratos definidos; **Small** — uma busca + uma escrita; **Testable** — contratos `criar_comDadosInvalidos_deveRetornarBadRequest`, `buscarPorId_comIdInexistente_deveRetornarNotFound`.

### 2. Business Context
A story materializa o risco R-04 do BRD: a adoção do módulo de manutenção depende de UX mobile-first (RNF-08). O status **Pendente** (RF-18; RN-05) alimenta diretamente a fila de aprovação (UC-03). O passo de busca é exatamente o caminho apontado pelo TODO de performance detectado na varredura (`AtivoService.java:119` — "carrega até 1000 candidatos (id+nome) e faz ranking"), o que ameaça a meta RNF-04 com o crescimento do parque (RNF-06: 10k+ ativos).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Criação de solicitação com ativo selecionado (caminho feliz)
* **Given** um funcionário autenticado (token injetado automaticamente pelo interceptador — RF-24/CA-07) e um ativo existente e ativo, vinculado a filial/departamento/localização (RN-07)
* **When** ele busca e seleciona o ativo (RF-15), descreve o problema, define a prioridade e submete a solicitação
* **Then** o sistema valida os dados (RN-03) e cria a solicitação com status **Pendente** (RF-18; RN-05)
* **And** a solicitação fica visível na fila de pendentes dos aprovadores (pós-condição do UC-02)

#### Scenario 2: Busca sem resultados e ajuste de filtros (caso de borda)
* **Given** um funcionário autenticado na tela "Nova Solicitação"
* **When** ele aplica filtros (filial/departamento/localização) que não retornam nenhum ativo
* **Then** o sistema exibe lista vazia e o funcionário ajusta os filtros
* **And** ao ajustar os filtros para critérios com resultados, o fluxo retoma na seleção do ativo — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: comportamento de "lista vazia" não especificado no BRD; a busca com ranking de até 1000 candidatos está implementada em `AtivoService` — TODO de performance em `AtivoService.java:119`]**

#### Scenario 3: Falha no upload de fotos — anexo opcional (caso de borda) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Given** um funcionário autenticado com um ativo selecionado e fotos anexadas ao formulário
* **When** o serviço de anexo falha (indisponibilidade ou arquivo rejeitado por tamanho/formato) e ele submete a solicitação
* **Then** o sistema permite a submissão sem as fotos e cria a solicitação com status **Pendente**
* **And** a falha do anexo é registrada/exibida — premissa: o anexo é opcional no fluxo do BRD e não bloqueia a criação; nenhum endpoint de upload foi detectado na varredura — mecanismo real a confirmar.

#### Scenario 4: Ativo inexistente (erro)
* **Given** um funcionário autenticado
* **When** ele submete uma solicitação referenciando um ID de ativo inexistente ou ativo baixado
* **Then** o sistema retorna **404 Not Found** com mensagem padronizada (contrato `buscarPorId_comIdInexistente_deveRetornarNotFound`; RN-04; CA-03)
* **And** a solicitação não é criada

#### Scenario 5: Descrição/prioridade ausentes (erro)
* **Given** um funcionário autenticado com um ativo selecionado
* **When** ele submete a solicitação sem descrição ou prioridade (ou com payload malformado)
* **Then** o sistema retorna **400 Bad Request** com mensagem clara (RN-03; CA-02)
* **And** a solicitação não é criada

#### Scenario 6: Sessão expirada na submissão (erro)
* **Given** um funcionário autenticado cujo token expirou (1h — RNF-01 do BRD)
* **When** ele submete a solicitação
* **Then** o sistema retorna **401 Unauthorized** (CA-06)
* **And** o frontend limpa a sessão (`clearSession` — RF-25; CA-08) e redireciona para login **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: redirecionamento não especificado no BRD]**

### 4. UI/UX Notes
* **Wireframe/Mockup:** não disponível nas fontes — design mobile-first a criar/validar com PO (RNF-08; risco R-04). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** loading (busca de ativos e envio), empty (busca sem resultados), error (400/401/404 e falha de conexão), success (solicitação criada com status Pendente).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js` (função `request`, complexidade 13; `console.error` residual em `api.js:44`); `src/main/java/br/com/aegispatrimonio/service/AtivoService.java` (busca com ranking — TODO de performance na linha 119); `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java` (método `toDTO`, complexidade 14 — detalhes do ativo, RF-14); serviço de solicitações de manutenção (classe específica não destacada na varredura A5 — confirmar no codebase).
* **Dependências técnicas:** nenhuma rota detectada — endpoints de busca de ativos e de criação de solicitação a confirmar no `api-specification.md`; sem motor de banco/ORM nas dependências (persistência não discoverable); mecanismo de upload de anexos não detectado na varredura; vínculo Funcionário↔Usuário (RN-06; RF-08: `createFuncionario` / `createFuncionarioAndUsuario`).
* **Considerações de performance/segurança:** RNF-04 — < 200ms (p95) na criação e na busca; **atenção** ao TODO de `AtivoService.java:119` e à complexidade de `AtivoMapper.toDTO` (14) — otimização tratada em US-005; RNF-01 — token JWT (meta do BRD); RNF-10 — cobertura > 80% incluindo permissões Admin vs User (CA-10); RNF-06 — meta de 10k+ ativos / 1k+ usuários simultâneos, pendente de verificação (aplicação backend única, sem mecanismos de scaling detectados).

### 6. Out of Scope
* Aprovação da solicitação (US-03) e conclusão da ordem (US-04).
* Transição para "Em Andamento" (não coberta por RF-18..RF-22 — ver US-04/Open Issues do UC-04).
* Implementação do mecanismo de upload de anexos (não detectado — escopo a definir com PO).
* Gestão do cadastro de funcionários (RF-08).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — endpoints a confirmar no `api-specification.md`; valores de "prioridade" a definir com PO
- [ ] Design/UX aprovado (se aplicável) — wireframe mobile-first inexistente nas fontes
- [ ] Estimativa de esforço realizada — 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD (incl. permissões — CA-10)
- [ ] Testes de integração cobrindo os contratos `criar_comDadosInvalidos_deveRetornarBadRequest` e `buscarPorId_comIdInexistente_deveRetornarNotFound`
- [ ] Tipagem estrita (backend Java; frontend JavaScript — não há TypeScript nas dependências detectadas; validar contratos de payload)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas (`api-specification.md`)
- [ ] Feature testada em staging (com dados do `RealisticDataSeeder`)
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação detectada na varredura

### 9. Traceability
* **Use Cases relacionados:** UC-02
* **Requisitos funcionais:** RF-15, RF-18, RF-22; RF-24, RF-25; RF-08 (contexto RN-06)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-03, RN-04, RN-05, RN-06, RN-07
* **Critérios de aceitação (BRD):** CA-02, CA-03, CA-04, CA-06, CA-07, CA-08, CA-10
* **NFRs relacionadas:** RNF-01, RNF-04, RNF-06, RNF-08, RNF-10
* **Riscos relacionados:** R-04 (adoção do módulo — UX mobile-first)

---

## US-003 — Aprovador autoriza manutenção

* **Priority:** Must — a transição **Pendente → Aprovada** (RN-05) é obrigatória no fluxo; sem ela, nenhuma ordem chega à conclusão (UC-04) e o controle de custo (RN-08) não opera.
* **Complexity:** 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: fila com filtros dinâmicos (`ManutencaoSpecification.build`, complexidade 14), consulta de custo por ativo (RF-17/RN-08) com risco de timeout (R-03 do BRD) e transição de estado sujeita a condição de corrida.
* **Épico:** Gestão de Manutenção
* **Labels:** backend, frontend, ux

### 1. Description (INVEST)
* **As a** Aprovador
* **I want to** visualizar a fila de solicitações pendentes, analisar o histórico de custos do ativo e autorizar (ou cancelar) cada solicitação
* **So that** apenas demandas conformes sigam para a equipe de manutenção e o custo total por ativo permaneça sob controle (RN-08)

*Checklist INVEST:* **Independent** — depende da existência de solicitações Pendentes (US-002), dependência de fluxo e não de implementação; **Negotiable** — role de aprovação não fixada no BRD (RF-26/RF-27); **Valuable** — gate de conformidade e custo; **Estimable** — contratos `aprovar`, `cancelar`, `custoTotalPorAtivo` definidos; **Small** — uma fila + uma transição; **Testable** — cenários de permissão e transição cobertos por CA-03/CA-04/CA-05/CA-09/CA-10.

### 2. Business Context
O KPI do BRD ("tempo médio de aprovação < 4 horas úteis") depende diretamente desta story. O risco R-03 do BRD ("Performance em relatórios de custo total — Timeout em consultas pesadas") afeta o passo de análise (`custoTotalPorAtivo`), e o risco R-01 (começar com 2 roles e evoluir para permissões granulares — RF-26/RF-27) define o modelo de autorização. A notificação da equipe de manutenção (pós-condição do BRD) é lacuna a confirmar: o único serviço de alerta detectado na varredura (`AlertNotificationService.checkResourceUsageAlerts`) trata alertas de uso de recursos, sem ligação estabelecida com a aprovação.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Aprovação de solicitação pendente (caminho feliz)
* **Given** um aprovador autenticado com permissão de aprovação (RBAC — RF-26) e uma solicitação em status **Pendente** (RN-05)
* **When** ele analisa os detalhes da solicitação e o `custoTotalPorAtivo` (RF-17; RN-08 — somatório das ordens concluídas no período) e clica "Aprovar"
* **Then** o sistema valida a transição **Pendente → Aprovada** (RN-05) e persiste
* **And** a equipe de manutenção é notificada (pós-condição do BRD — mecanismo de notificação não confirmado nas fontes **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; o `AlertNotificationService` detectado trata alertas de uso de recursos, sem ligação estabelecida com a aprovação)

#### Scenario 2: Cancelamento de solicitação (caso de borda — RF-20)
* **Given** um aprovador autenticado com permissão de aprovação e uma solicitação em status **Pendente**
* **When** ele clica "Cancelar"
* **Then** o sistema atualiza o status para **Cancelada** (RN-05 — cancelamento permitido a qualquer momento; CA-05)
* **And** os recursos alocados são liberados (CA-05) e a solicitação não compõe o custo total (RN-08 — apenas ordens concluídas somam)

#### Scenario 3: Aprovador adia a decisão (caso de borda) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Given** um aprovador autenticado analisando uma solicitação **Pendente**
* **When** ele sai da análise sem decidir
* **Then** a solicitação permanece em status **Pendente** e pode ser retomada a qualquer momento
* **And** nenhum SLA técnico de expiração é aplicado — premissa: o KPI "tempo médio de aprovação < 4 horas úteis" é meta de gestão, não comportamento de sistema.

#### Scenario 4: Transição de estado inválida / condição de corrida (erro)
* **Given** dois aprovadores autenticados visualizando a mesma solicitação **Pendente**
* **When** o primeiro aprova e o segundo tenta aprovar (ou cancelar) em seguida
* **Then** o sistema rejeita a segunda transição e mantém o estado consistente com RN-05
* **And** retorna **400 Bad Request** (RN-03) ou **409 Conflict** — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define o código HTTP para transição inválida; a confirmar no `api-specification.md`]**

#### Scenario 5: Aprovador sem permissão (erro)
* **Given** um usuário autenticado sem role/permissão de aprovação
* **When** ele tenta aprovar ou cancelar uma solicitação
* **Then** o sistema retorna **403 Forbidden** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: inferido do modelo RBAC (RF-26) e do critério de cobertura de permissões (CA-10); o BRD define 403 explicitamente apenas para escrita em entidades mestres (RN-02)]**
* **And** o status da solicitação permanece inalterado

#### Scenario 6: Solicitação inexistente (erro)
* **Given** um aprovador autenticado
* **When** ele tenta aprovar uma solicitação com ID inexistente
* **Then** o sistema retorna **404 Not Found** com mensagem padronizada (RN-04; CA-03)
* **And** nenhuma transição é executada

### 4. UI/UX Notes
* **Wireframe/Mockup:** não disponível nas fontes — design a criar/validar com PO. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** loading (fila e consulta de custo), empty (fila sem pendentes), error (403/404, transição inválida e timeout na consulta de custo — risco R-03), success (status atualizado para Aprovada/Cancelada).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js`; `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java` (método `build`, complexidade 14 — filtros dinâmicos da fila de manutenção; presume-se suporte a filtragem de pendentes); `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java` (método `checkResourceUsageAlerts`, complexidade 17 — único serviço de alerta detectado; ligação com a aprovação não estabelecida); consulta `custoTotalPorAtivo` (RF-17).
* **Dependências técnicas:** nenhuma rota detectada — endpoints de listagem de pendentes e de aprovação a confirmar no `api-specification.md`; role de aprovação não fixada no BRD (RF-26/RF-27 — `createRole`, `createPermission`); sem motor de banco/ORM detectado nas dependências — o mecanismo de persistência e de consulta de custo não é discoverable a partir das fontes.
* **Considerações de performance/segurança:** RNF-04 — < 200ms (p95), **excluindo operações de relatório pesado** — `custoTotalPorAtivo` está explicitamente fora dessa garantia (risco R-03; mitigações previstas no BRD — consultas otimizadas, paginação, cache — nenhuma detectada como implementada); RNF-07 — a transição de escrita (aprovar) deve gerar registro de auditoria imutável (meta do BRD; ver US-006); RNF-01 — token JWT válido durante a análise (meta do BRD).

### 6. Out of Scope
* Conclusão da ordem de serviço (US-04).
* Transição para "Em Andamento" (sem requisito/UC que a dispare — ver US-04).
* CRUD de roles/permissões (RF-26/RF-27 — story própria).
* Implementação do mecanismo de notificação da equipe de manutenção (lacuna a confirmar nas fontes).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — endpoints a confirmar no `api-specification.md`; role de aprovação a definir com PO
- [ ] Design/UX aprovado (se aplicável) — wireframe inexistente nas fontes
- [ ] Estimativa de esforço realizada — 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD (incl. condição de corrida e permissões — CA-10)
- [ ] Testes de integração cobrindo os contratos `aprovar`, `cancelar` e `custoTotalPorAtivo`
- [ ] Tipagem estrita (backend Java; frontend JavaScript — não há TypeScript nas dependências detectadas; validar contratos de payload)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas (`api-specification.md`)
- [ ] Feature testada em staging (com dados do `RealisticDataSeeder`)
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação detectada na varredura

### 9. Traceability
* **Use Cases relacionados:** UC-03
* **Requisitos funcionais:** RF-17, RF-19, RF-20; RF-26/RF-27 (contexto RBAC)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-04, RN-05, RN-08; RN-01/RN-02 (contexto RBAC)
* **Critérios de aceitação (BRD):** CA-03, CA-04, CA-05, CA-09
* **NFRs relacionadas:** RNF-01, RNF-04, RNF-07
* **Riscos relacionados:** R-01 (evolução de roles), R-03 (performance em relatórios de custo)

---

## US-004 — Técnico conclui ordem de serviço

* **Priority:** Must — fecha o ciclo de manutenção (RN-05) e alimenta o `custoTotalPorAtivo` (RN-08; CA-09), indicador central de controle de custo do BRD.
* **Complexity:** 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: registro de execução com múltiplos campos (data, descrição, peças, custo, tempo), transição de estado, atualização de custo com conciliação (CA-09); designação de técnico sem requisito funcional correspondente (RF-18..RF-22 não cobrem designação).
* **Épico:** Gestão de Manutenção
* **Labels:** backend, frontend, ux

### 1. Description (INVEST)
* **As a** técnico da Equipe de Manutenção
* **I want to** registrar data, descrição, peças utilizadas, custo e tempo da execução e concluir a ordem de serviço
* **So that** o histórico do ativo e o custo total por ativo fiquem atualizados e confiáveis (RF-22; RF-17; RN-08) e o ativo volte a ficar disponível

*Checklist INVEST:* **Independent** — depende de ordem Aprovada (US-003), dependência de fluxo e não de implementação; **Negotiable** — campos de registro extraídos do fluxo do BRD, modelo completo não verificado; **Valuable** — fecha o ciclo e alimenta o KPI de custo; **Estimable** — contrato `concluir` definido; **Small** — um registro + uma transição; **Testable** — cenários de transição, validação e conciliação cobertos por CA-02/CA-03/CA-04/CA-05/CA-09.

### 2. Business Context
A conclusão é o ponto em que o custo total por ativo (RN-08) é alimentado — o critério CA-09 do BRD exige que os relatórios de custo batam com a soma das ordens concluídas. O KPI do BRD "taxa de solicitações canceladas < 10%" indica que a maioria das ordens aprovadas chega à conclusão. Lacunas do BRD afetam esta story: a transição para "Em Andamento" é prevista no fluxo (RN-05) mas não há requisito/UC que a dispare, e a designação de técnico não possui requisito funcional.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Conclusão de ordem aprovada/em andamento (caminho feliz)
* **Given** um técnico autenticado com permissão de execução (RBAC — RF-26) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define a role do técnico; presume-se dentro do modelo Admin/User ou de role específica via RF-27]** designado para a ordem, e uma solicitação em status **Aprovada** ou **Em Andamento** (RN-05)
* **When** ele registra data, descrição, peças utilizadas, custo e tempo e clica "Concluir"
* **Then** o sistema valida os dados (RN-03) e a transição (**Aprovada/Em Andamento → Concluída**, RN-05) e persiste a ordem
* **And** o `custoTotalPorAtivo` passa a refletir a nova ordem concluída (RF-17; RN-08) e o histórico do ativo é atualizado (RF-22)

#### Scenario 2: Registro parcial e retomada posterior (caso de borda) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Given** um técnico autenticado em campo com uma ordem em **Aprovada/Em Andamento**
* **When** ele salva dados parciais da execução sem concluir
* **Then** a ordem permanece em **Aprovada/Em Andamento** e o `custoTotalPorAtivo` não é alterado (RN-08 — apenas ordens concluídas somam)
* **And** o técnico retoma o registro e conclui posteriormente — premissa: o estado "Em Andamento" (RN-05) suporta execução parcial; o mecanismo de salvamento parcial não está especificado no BRD.

#### Scenario 3: Ordem cancelada antes da conclusão (caso de borda)
* **Given** uma ordem em **Aprovada/Em Andamento** com registro parcial do técnico
* **When** a solicitação é cancelada (RF-20) por aprovador ou solicitante
* **Then** o status passa a **Cancelada** (RN-05; CA-05 — libera recursos) e a ordem não é concluída
* **And** a ordem cancelada **não compõe** o `custoTotalPorAtivo` (RN-08 — apenas ordens concluídas somam)

#### Scenario 4: Transição de estado inválida (erro)
* **Given** um técnico autenticado
* **When** ele tenta concluir uma ordem que não está em **Aprovada** nem **Em Andamento** (ex.: **Pendente** ou **Cancelada**)
* **Then** o sistema rejeita a transição e mantém a consistência com RN-05
* **And** retorna **400 Bad Request** (RN-03) ou **409 Conflict** — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: código HTTP para transição inválida não definido no BRD; a confirmar no `api-specification.md`]**
* **And** o `custoTotalPorAtivo` não é alterado

#### Scenario 5: Dados de execução inválidos (erro)
* **Given** um técnico autenticado com uma ordem em **Aprovada/Em Andamento**
* **When** ele submete custo/tempo com formato inválido ou sem campos obrigatórios (data, descrição, peças)
* **Then** o sistema retorna **400 Bad Request** com mensagem clara (RN-03; CA-02)
* **And** a ordem não é concluída e o `custoTotalPorAtivo` não é alterado

#### Scenario 6: Ordem/solicitação inexistente (erro)
* **Given** um técnico autenticado
* **When** ele tenta concluir uma ordem com ID inexistente
* **Then** o sistema retorna **404 Not Found** com mensagem padronizada (RN-04; CA-03)
* **And** nenhuma transição é executada

#### Scenario 7: Token expirado durante registro em campo (erro) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Given** um técnico autenticado cujo token expirou (1h — RNF-01 do BRD) após execução prolongada em campo
* **When** ele clica "Concluir"
* **Then** o sistema retorna **401 Unauthorized** (CA-06)
* **And** o técnico refaz o login e re-submete os dados — premissa: não há draft automático nas fontes disponíveis; dados não submetidos podem ser perdidos.

### 4. UI/UX Notes
* **Wireframe/Mockup:** não disponível nas fontes — design para uso em campo a criar/validar com PO. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** loading (envio da conclusão), empty (n/a — o fluxo parte de uma ordem existente), error (400/401/404 e transição inválida), success (ordem concluída com custo total atualizado).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js` (função `request` — injeção de token, RF-24); serviço de ordens de manutenção (classe específica não destacada na varredura A5 — confirmar no codebase); consulta `custoTotalPorAtivo` (RF-17).
* **Dependências técnicas:** nenhuma rota detectada — endpoint de conclusão a confirmar no `api-specification.md`; transição para "Em Andamento" não coberta por RF-18..RF-22 (lacuna do BRD); designação de técnico sem requisito funcional — presume-se designação manual/externa; sem motor de banco/ORM detectado nas dependências (persistência não discoverable).
* **Considerações de performance/segurança:** RNF-04 — < 200ms (p95) na conclusão (meta do BRD); RNF-07 — escrita (concluir) deve gerar registro de auditoria imutável (meta do BRD; ver US-006); RNF-01 — token JWT com expiração de 1h, relevante para execução em campo (Scenario 7); RNF-10 — cobertura > 80% incluindo o cenário de conciliação de custos (CA-09).

### 6. Out of Scope
* Aprovação/cancelamento da solicitação (US-03).
* Transição que dispara o estado "Em Andamento" (lacuna do BRD — story própria a definir).
* Fluxo de designação de técnico (não definido no BRD).
* Reconciliação automática de custos (CA-09 — presume-se verificação em testes de integração e correção manual; mecanismo a definir).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — endpoint a confirmar no `api-specification.md`; lacunas (Em Andamento, designação de técnico) a decidir com PO
- [ ] Design/UX aprovado (se aplicável) — wireframe inexistente nas fontes
- [ ] Estimativa de esforço realizada — 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD (incl. transições inválidas e conciliação — CA-09)
- [ ] Testes de integração cobrindo o contrato `concluir` e a conciliação do `custoTotalPorAtivo` (CA-09)
- [ ] Tipagem estrita (backend Java; frontend JavaScript — não há TypeScript nas dependências detectadas; validar contratos de payload)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas (`api-specification.md`)
- [ ] Feature testada em staging (com dados do `RealisticDataSeeder`)
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação detectada na varredura

### 9. Traceability
* **Use Cases relacionados:** UC-04
* **Requisitos funcionais:** RF-17, RF-21, RF-22; RF-24
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-03, RN-04, RN-05, RN-07, RN-08
* **Critérios de aceitação (BRD):** CA-02, CA-03, CA-04, CA-05, CA-09
* **NFRs relacionadas:** RNF-01, RNF-04, RNF-07, RNF-08, RNF-10
* **Riscos relacionados:** R-03 (conciliação/performance de custos — indireto)

---

## US-005 — Otimização da busca e filtragem de ativos

* **Priority:** Should — a busca funciona hoje, mas o TODO de performance (`AtivoService.java:119`) e a complexidade do `AtivoMapper.toDTO` (14) ameaçam a meta RNF-04 (< 200ms p95) com o crescimento do parque (RNF-06: 10k+ ativos).
* **Complexity:** 3 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: refatoração do caminho de busca (reduzir candidatos carregados/ranking) e quebra do método `toDTO` em métodos menores, sem mudança do contrato comportamental (RF-15).
* **Épico:** Gestão de Manutenção / Tech-Debt (performance)
* **Labels:** backend, tech-debt, performance

### 1. Description (INVEST)
* **As a** desenvolvedor do Sistema de Gestão de Patrimônio
* **I want to** otimizar o caminho de busca/listagem de ativos — hoje carrega até 1000 candidatos (id+nome) e faz ranking — e reduzir a complexidade do mapeamento para DTO
* **So that** a listagem/filtragem de ativos (RF-15) atenda à meta de resposta < 200ms (p95) mesmo com o crescimento do parque de ativos

*Checklist INVEST:* **Independent** — não altera contratos de outros fluxos; **Negotiable** — estratégia de otimização a negociar em design técnico; **Valuable** — protege a meta RNF-04 e a UX da UC-02 (risco R-04); **Estimable** — escopo delimitado aos arquivos apontados pela varredura; **Small** — dois pontos de refatoração; **Testable** — regressão dos contratos existentes + medição de tempo de resposta.

### 2. Business Context
O TODO de performance em `AtivoService.java:119` está exatamente no passo 2 da UC-02 (busca de ativos por filial/departamento/localização — RF-15), fluxo de maior frequência do sistema. Com a meta RNF-06 do BRD (10k+ ativos / 1k+ usuários simultâneos), o carregamento e ranking de até 1000 candidatos por busca tende a degradar a resposta e a adoção do módulo (risco R-04). A varredura também registrou `AtivoMapper.toDTO` com complexidade ciclomática 14, impactando a consulta de detalhes do ativo (RF-14).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Busca otimizada dentro da meta de performance (caminho feliz)
* **Given** um parque de ativos com volume compatível com a meta do BRD (RNF-06 — 10k+ ativos) e um funcionário autenticado
* **When** ele aplica filtros de filial, departamento ou localização (RF-15)
* **Then** a resposta é retornada em menos de 200ms (p95) (RNF-04)
* **And** o resultado contém os ativos compatíveis com os filtros, sem alteração do contrato comportamental

#### Scenario 2: Busca com grande volume de candidatos (caso de borda)
* **Given** critérios de busca que, no caminho atual, carregariam até 1000 candidatos (id+nome) para ranking (TODO em `AtivoService.java:119`)
* **When** a busca é executada no caminho otimizado
* **Then** o resultado não depende do carregamento/ranking completo dos candidatos
* **And** a resposta permanece dentro da meta RNF-04 — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: a estratégia de otimização (ex.: delegar filtragem à consulta, paginação) não está definida nas fontes; sem motor de banco/ORM detectado nas dependências, a estratégia não pode presumir recursos de consulta específicos — a confirmar em design técnico]**

#### Scenario 3: Falha na busca após refatoração (erro)
* **Given** a refatoração aplicada ao caminho de busca e ao mapper
* **When** a busca falha (erro interno ou critérios malformados)
* **Then** o sistema retorna erro com mensagem clara, sem expor detalhes internos
* **And** os testes de regressão dos contratos existentes (ex.: `buscarPorId_comIdInexistente_deveRetornarNotFound`) continuam passando

### 4. UI/UX Notes
* **Wireframe/Mockup:** não aplicável — refatoração sem mudança de interface. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** inalterados em relação à UC-02 (loading na busca, empty sem resultados, error em falhas, success na listagem).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `src/main/java/br/com/aegispatrimonio/service/AtivoService.java` (linha 119 — TODO de performance); `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java` (método `toDTO`, complexidade ciclomática 14); `frontend/src/services/api.js` (consumidor do caminho de busca).
* **Dependências técnicas:** sem motor de banco/ORM detectado nas dependências varridas (apenas `@popperjs/core` em produção) — a estratégia de otimização não pode presumir recursos de consulta específicos; contrato RF-15 inalterado; nenhuma rota detectada na varredura.
* **Considerações de performance/segurança:** RNF-04 (< 200ms p95) é o critério central; RNF-06 (10k+ ativos / 1k+ usuários) é meta pendente de verificação — aplicação backend única, sem mecanismos de scaling horizontal detectados no codebase, logo a otimização deve ser feita dentro do caminho de aplicação existente.

### 6. Out of Scope
* Mudança no contrato comportamental da busca (RF-15).
* Infraestrutura de scaling horizontal (não detectada no codebase — fora do escopo até que exista).
* Implementação de cache/paginação como produto (as mitigações do risco R-03 referem-se a relatórios de custo — UC-03).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — estratégia de otimização a definir em design técnico
- [ ] Design/UX aprovado (se aplicável) — não aplicável (sem mudança de interface)
- [ ] Estimativa de esforço realizada — 3 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD
- [ ] Testes de regressão dos contratos existentes da busca passando
- [ ] Tipagem estrita (backend Java)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação atualizada (notas de design técnico da otimização)
- [ ] Feature testada em staging (medição de tempo de resposta com dados do `RealisticDataSeeder`)
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação de métricas detectada na varredura; medição do p95 requer instrumentação nova

### 9. Traceability
* **Use Cases relacionados:** UC-02 (passo 2); UC-03 (indireto — padrão de filtros em `ManutencaoSpecification`)
* **Requisitos funcionais:** RF-15 (listagem/filtragem de ativos); RF-14 (detalhes do ativo via `toDTO`)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-07 (base de filtragem)
* **NFRs relacionadas:** RNF-04, RNF-06
* **Riscos relacionados:** R-04 (adoção do módulo — UX/performance)

---

## US-006 — Audit trail de operações de escrita (RNF-07)

* **Priority:** Should — meta do BRD (RNF-07) referenciada por todos os casos de uso de escrita (UC-01..UC-04); sem ela, a rastreabilidade de compliance exigida pelo BRD não é atendida. Derivada dos Open Issues dos UCs ("implementação do audit trail não detectada na varredura").
* **Complexity:** 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: mecanismo transversal de registro imutável para todas as escritas (criar filial, criar solicitação, aprovar/cancelar, concluir ordem); nenhum mecanismo de persistência detectado nas dependências — o design depende de decisão técnica a confirmar.
* **Épico:** Compliance/Auditoria (transversal)
* **Labels:** backend, infra, compliance, tech-debt

### 1. Description (INVEST)
* **As a** gestor de compliance/auditoria
* **I want to** que toda operação de escrita (criar filial, criar solicitação, aprovar/cancelar, concluir ordem) gere um registro imutável em audit trail, incluindo tentativas rejeitadas
* **So that** as operações sejam rastreáveis e auditáveis conforme RNF-07 e os critérios de compliance do BRD

*Checklist INVEST:* **Independent** — mecanismo transversal, não acoplado a um fluxo específico; **Negotiable** — escopo do registro (payload, IP) a negociar; **Valuable** — atende RNF-07 e stakeholders de compliance; **Estimable** — escopo delimitado às escritas dos UC-01..UC-04; **Small** — um mecanismo transversal; **Testable** — cenários de registro em sucesso, rejeição e falha.

### 2. Business Context
O RNF-07 do BRD exige que operações de escrita gerem registro em audit trail imutável — requisito citado nos UC-01 (criar filial), UC-02 (criar solicitação), UC-03 (aprovar) e UC-04 (concluir). A varredura A5 **não detectou nenhuma implementação de audit trail** no codebase, e o UC-01 (EX-1) afirma que tentativas rejeitadas também devem ser registradas. Esta story fecha essa lacuna de forma transversal, evitando implementação ad hoc em cada fluxo.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Escrita bem-sucedida gera registro de auditoria (caminho feliz)
* **Given** um usuário autenticado com permissão para uma operação de escrita (ex.: Admin criando filial — RN-01)
* **When** a operação é concluída com sucesso (ex.: **201 Created**)
* **Then** um registro imutável de auditoria é gerado com usuário, operação, data/hora e resultado (RNF-07)
* **And** o registro não pode ser alterado por operações subsequentes

#### Scenario 2: Tentativa rejeitada também é registrada (caso de borda)
* **Given** um usuário autenticado que submete uma operação de escrita com dados inválidos (RN-03) ou sem permissão (RN-02)
* **When** o sistema rejeita a operação (**400 Bad Request** / **403 Forbidden**)
* **Then** a tentativa é registrada no audit trail conforme RNF-07 — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o UC-01 (EX-1) afirma que tentativas devem ser registradas; o escopo exato do registro (payload, IP) não está definido no BRD]**

#### Scenario 3: Falha na gravação do audit trail (erro)
* **Given** uma operação de escrita válida e autorizada
* **When** a gravação do registro de auditoria falha
* **Then** o comportamento esperado (bloquear a escrita ou registrar a falha e sinalizar) deve ser definido — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define o comportamento de falha do audit trail; premissa adotada: a falha de auditoria não deve corromper a operação de negócio, mas deve ser sinalizada]**

### 4. UI/UX Notes
* **Wireframe/Mockup:** não aplicável — mecanismo de backend, sem interface (a interface de consulta do audit trail não é especificada no BRD). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** inalterados nos fluxos existentes (o registro é responsabilidade do backend e transparente ao usuário).

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** mecanismo transversal a todos os fluxos de escrita (UC-01..UC-04); o cliente `frontend/src/services/api.js` não é afetado (registro é responsabilidade do backend).
* **Dependências técnicas:** nenhum mecanismo de persistência detectado nas dependências varridas (apenas `@popperjs/core` em produção; sem motor de banco/ORM) — o design do audit trail depende de decisão técnica a confirmar; nenhuma rota detectada na varredura; `api-specification.md` pendente.
* **Considerações de performance/segurança:** RNF-04 — o registro não deve comprometer a meta de < 200ms (p95) (premissa: gravação de baixo custo ou assíncrona — a confirmar); RNF-01 — registro associado ao usuário autenticado; imutabilidade garantida (RNF-07).

### 6. Out of Scope
* Interface de consulta/visualização do audit trail (não especificada no BRD).
* Política de retenção/backup dos registros (não especificada no BRD).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — mecanismo de persistência e comportamento de falha a definir em design técnico
- [ ] Design/UX aprovado (se aplicável) — não aplicável (sem interface)
- [ ] Estimativa de esforço realizada — 5 pontos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD (sucesso, rejeição, falha)
- [ ] Testes de integração verificando o registro nas escritas dos UC-01..UC-04
- [ ] Tipagem estrita (backend Java)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação atualizada
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — nenhuma instrumentação detectada na varredura

### 9. Traceability
* **Use Cases relacionados:** UC-01, UC-02, UC-03, UC-04 (todas as operações de escrita)
* **Requisitos funcionais:** RF-06 (criar filial), RF-18 (criar solicitação), RF-19/RF-20 (aprovar/cancelar), RF-21 (concluir)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-01, RN-02, RN-03 (tentativas rejeitadas)
* **NFRs relacionadas:** RNF-07 (primária), RNF-01, RNF-04
* **Riscos relacionados:** nenhum risco específico mapeado nas fontes para auditoria

---

## US-007 — Higiene de código: stub `setUsername` e logs residuais no cliente API

* **Priority:** Could — impacto funcional limitado (RN-06 — vínculo Funcionário↔Usuário) e higiene de produção; não bloqueia os fluxos principais. Derivada dos achados da varredura A5 (`Usuario.java:86`, `api.js:44`, `api.js:107`).
* **Complexity:** 1 ponto **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: implementação do corpo vazio de `setUsername` e remoção de duas chamadas de log no cliente API.
* **Épico:** Tech-Debt (qualidade de código)
* **Labels:** backend, frontend, tech-debt

### 1. Description (INVEST)
* **As a** desenvolvedor do Sistema de Gestão de Patrimônio
* **I want to** implementar o corpo vazio do método `setUsername` de `Usuario` e remover as chamadas residuais `console.error`/`console.debug` do cliente API
* **So that** a atualização de username tenha efeito (RN-06) e o cliente API não produza logs residuais em produção

*Checklist INVEST:* **Independent** — não depende de outros itens do backlog; **Negotiable** — comportamento esperado do `setUsername` a confirmar com o modelo de dados; **Valuable** — corrige stub funcional e higiene de produção; **Estimable** — escopo de dois pontos de código apontados pela varredura; **Small** — alterações mínimas; **Testable** — regressão dos fluxos existentes.

### 2. Business Context
A varredura A5 registrou `Usuario.setUsername` com corpo vazio (stub) em `Usuario.java:86` — o UC-02 presume que a atualização de username não tem efeito até implementação, impactando o cadastro/vínculo de funcionário (RN-06; RF-08: `createFuncionario` / `createFuncionarioAndUsuario`). No frontend, o cliente API usado por **todos** os fluxos (UC-01..UC-04) contém chamadas residuais `console.error` (`api.js:44`) e `console.debug` (`api.js:107`), que devem ser removidas antes de produção.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Atualização de username com efeito (caminho feliz)
* **Given** um usuário do sistema com vínculo a funcionário (RN-06; RF-08)
* **When** o método `setUsername` é invocado com um novo valor e a entidade é persistida
* **Then** o username é atualizado com efeito (não mais um stub vazio — varredura: `Usuario.java:86`)

#### Scenario 2: Cliente API sem logs residuais (caso de borda)
* **Given** o cliente API `frontend/src/services/api.js` em execução
* **When** uma requisição falha ou é depurada
* **Then** nenhuma chamada `console.error` (`api.js:44`) ou `console.debug` (`api.js:107`) é emitida em produção
* **And** o tratamento de erros da função `request` permanece inalterado (complexidade ciclomática 13)

#### Scenario 3: Regressão nos fluxos existentes (erro)
* **Given** as alterações de higiene aplicadas
* **When** os fluxos UC-01..UC-04 são executados (incl. cadastro/vínculo de funcionário — RN-06)
* **Then** nenhum comportamento dos contratos existentes é alterado
* **And** os testes de integração referenciados pelo BRD continuam passando

### 4. UI/UX Notes
* **Wireframe/Mockup:** não aplicável — alterações de código sem mudança de interface. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Estados da interface:** inalterados em relação aos fluxos UC-01..UC-04.

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `src/main/java/br/com/aegispatrimonio/model/Usuario.java` (método `setUsername`, linha 86 — corpo vazio); `frontend/src/services/api.js` (linhas 44 e 107 — `console.error`/`console.debug`).
* **Dependências técnicas:** nenhuma rota detectada na varredura; comportamento esperado do `setUsername` (persistência do username) a confirmar com o modelo de dados — o modelo completo não foi verificado na varredura.
* **Considerações de performance/segurança:** remoção de logs reduz exposição potencial de dados em console (boa prática de produção); sem impacto de performance esperado.

### 6. Out of Scope
* Cadastro/edição de funcionários (RF-08 — story própria).
* Refatoração da função `request` do `api.js` (complexidade ciclomática 13 — apenas as chamadas de log residuais estão em escopo).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos (neste documento) e validados com PO — **pendente de validação**
- [ ] Dependências identificadas e desbloqueadas — comportamento esperado do `setUsername` a confirmar
- [ ] Design/UX aprovado (se aplicável) — não aplicável (sem mudança de interface)
- [ ] Estimativa de esforço realizada — 1 ponto **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar em planning

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo os cenários BDD
- [ ] Testes de regressão dos fluxos UC-01..UC-04 passando
- [ ] Tipagem estrita (backend Java; frontend JavaScript — não há TypeScript nas dependências detectadas)
- [ ] Lint/format ok (nenhuma configuração de linter detectada nas dev-dependencies — configurar/validar se aplicável)
- [ ] Sem regressões de acessibilidade
- [ ] Documentação atualizada
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável) — não aplicável

### 9. Traceability
* **Use Cases relacionados:** UC-02 (vínculo Funcionário↔Usuário — RN-06); UC-01, UC-03, UC-04 (cliente API comum a todos os fluxos)
* **Requisitos funcionais:** RF-08 (`createFuncionario` / `createFuncionarioAndUsuario`)
* **Regras de negócio (RN-XX = Business Rules do BRD):** RN-06
* **NFRs relacionadas:** RNF-10 (testabilidade — regressão)
* **Riscos relacionados:** nenhum risco específico mapeado nas fontes