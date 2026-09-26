# User Stories Specification

> **Épico relacionado:** Aegis1 — Módulo de Manutenção · **Sprint:** 1 · **Status geral:** Backlog

---

## US-CAD-01 — Criar Departamento

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, crud]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** criar um novo departamento preenchendo código e nome
* **So that** o departamento fique disponível para associação em ordens de manutenção e relatórios

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Primeira operação do módulo de cadastros mestres (UC-01). Departamentos são pré-requisito para criar ordens de manutenção (UC-02 exige pelo menos um departamento cadastrado).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar departamento com dados válidos — caminho feliz
* **Given** o administrador está autenticado e acessa a tela "Novo Departamento"
* **When** preenche "Código" com "MEC" e "Nome" com "Mecânica" e clica em "Salvar"
* **Then** o frontend valida localmente, envia `POST /api/departamentos` via `api.js:request`, recebe `201 Created` com o departamento criado (incluindo ID), exibe toast "Departamento criado com sucesso" e atualiza a lista chamando `listar`

#### Scenario 2: Tentar criar departamento com código duplicado — erro de negócio
* **Given** já existe departamento com código "MEC"
* **When** o administrador tenta criar outro com o mesmo código
* **Then** o backend retorna `409 Conflict`, `api.js:handleApiError` captura, frontend exibe toast "Registro já existe" e mantém o formulário aberto para correção

#### Scenario 3: Submeter formulário com campo obrigatório vazio — validação frontend
* **Given** o administrador deixa "Código" vazio
* **When** clica em "Salvar"
* **Then** o frontend bloqueia a submissão, destaca o campo em vermelho e exibe mensagem inline "Campo obrigatório" — não chama a API

#### Scenario 4: Falha de rede ao criar departamento — erro de infraestrutura
* **Given** o backend está indisponível ou timeout (>30s)
* **When** o administrador submete o formulário válido
* **Then** `handleApiError` registra no console (dev), exibe toast "Erro de conexão — tente novamente" e mantém o estado anterior do formulário

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tela de cadastro de departamento]
* **Estados da interface:** loading (enviando), empty (lista vazia), error (toast), success (toast + lista atualizada)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js` (função `request`, `handleApiError`, `authInterceptor`), componente de formulário de departamento, componente de lista
* **Dependências técnicas:** Endpoint `POST /api/departamentos` (backend), JWT válido no `authInterceptor`
* **Considerações de performance/segurança:** Latência P95 < 800 ms (NFR), token JWT anexado automaticamente, zero vazamento de dados sensíveis no frontend

### 6. Out of Scope
* Validação de unicidade de código no frontend (apenas backend)
* Edição/exclusão de departamentos (stories separadas)

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Contrato de API `POST /api/departamentos` alinhado com Backend
- [ ] Design/UX aprovado
- [ ] Estimativa de esforço realizada

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (validação frontend, sucesso, 409, erro de rede)
- [ ] Testes de integração/E2E para fluxo completo
- [ ] Tipagem estrita
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados
* **Use Cases relacionados:** UC-01
* **Riscos relacionados:** Open Issue — Contrato OpenAPI formal não existe; console.error/log residuais em `api.js:26,49,52`

---

## US-CAD-02 — Listar Departamentos

* **Priority:** Must
* **Complexity:** 2
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, listagem]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** visualizar a lista paginada de departamentos com opção de buscar por código/nome
* **So that** eu possa localizar rapidamente um departamento para editar ou excluir

### 2. Business Context
Parte do UC-01 — listagem é necessária para todas as operações CRUD subsequentes.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregar lista inicial de departamentos — caminho feliz
* **Given** o administrador acessa a tela "Departamentos"
* **When** o componente monta
* **Then** o frontend chama `GET /api/departamentos?page=1&size=20` via `request`, recebe lista paginada, renderiza tabela com colunas Código, Nome, Ações (Editar/Excluir)

#### Scenario 2: Filtrar departamentos por termo de busca — caso de borda
* **Given** a lista está carregada
* **When** o administrador digita "MEC" no campo de busca e pressiona Enter
* **Then** o frontend adiciona `?search=MEC` à query e recarrega a lista filtrada

#### Scenario 3: Paginar lista de departamentos — caso de borda
* **Given** há mais de 20 departamentos
* **When** o administrador clica na página 2
* **Then** o frontend chama `GET /api/departamentos?page=2&size=20` e renderiza a segunda página

#### Scenario 4: Erro ao carregar lista — erro de infraestrutura
* **Given** falha de rede ou erro 5xx
* **When** o componente tenta carregar
* **Then** `handleApiError` exibe toast "Erro ao carregar departamentos — tente novamente", mostra botão "Tentar novamente" e estado vazio com skeleton

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — lista de departamentos]
* **Estados da interface:** loading (skeleton), empty (ilustração "Nenhum departamento cadastrado"), error (toast + botão retry), success (tabela populada)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, componente de lista com paginação e busca
* **Dependências técnicas:** Endpoint `GET /api/departamentos` com suporte a `page`, `size`, `search`
* **Considerações de performance/segurança:** Cache não aplicado (dados voláteis), authInterceptor em todas as chamadas

### 6. Out of Scope
* Ordenação por colunas (fora do MVP)
* Exportação CSV (futuro)

### 7. Definition of Ready
- [ ] Critérios de aceite validados
- [ ] Parâmetros de query da API definidos com Backend

### 8. Definition of Done
- [ ] Implementado e revisado
- [ ] Testes unitários (carregar, filtrar, paginar, erro)
- [ ] Testes E2E fluxo completo
- [ ] Demais critérios padrão

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API
* **Use Cases relacionados:** UC-01
* **Riscos relacionados:** —

---

## US-CAD-03 — Buscar Departamento por ID

* **Priority:** Must
* **Complexity:** 1
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, detalhe]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** visualizar os detalhes completos de um departamento ao clicar em "Editar"
* **So that** eu possa alterar seus dados com segurança

### 2. Business Context
Usado no fluxo de edição (AS-1 do UC-01) — pré-carrega formulário com dados atuais.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Buscar departamento existente — caminho feliz
* **Given** o administrador clica em "Editar" na linha do departamento "MEC"
* **When** o frontend chama `GET /api/departamentos/{id}` via `buscarPorId`
* **Then** recebe o departamento completo, preenche o formulário de edição e habilita botão "Salvar"

#### Scenario 2: Departamento não encontrado (404) — erro de negócio
* **Given** o ID não existe ou foi excluído por outro usuário
* **When** o frontend tenta buscar
* **Then** `handleApiError` processa 404, exibe toast "Departamento não encontrado" e redireciona para a lista

#### Scenario 3: Falha de rede ao buscar — erro de infraestrutura
* **Given** timeout ou erro de rede
* **When** a busca é executada
* **Then** toast "Erro de conexão — tente novamente", botão "Tentar novamente" no formulário

### 4. UI/UX Notes
* **Wireframe/Mockup:** Mesmo da edição (US-CAD-04)
* **Estados:** loading (spinner no formulário), error (toast + redirect), success (formulário preenchido)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:buscarPorId` (wrapper de `request`), formulário de edição
* **Dependências técnicas:** Endpoint `GET /api/departamentos/{id}`

### 6. Out of Scope
* Visualização read-only separada (apenas edição usa este fetch)

### 7. Definition of Ready
- [ ] Contrato de resposta `GET /api/departamentos/{id}` definido

### 8. Definition of Done
- [ ] Implementado, testado, revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95
* **Use Cases relacionados:** UC-01 (AS-1)
* **Riscos relacionados:** —

---

## US-CAD-04 — Atualizar Departamento

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, crud]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** alterar o nome de um departamento existente
* **So that** a informação permaneça correta para novas ordens e relatórios

### 2. Business Context
Fluxo AS-1 do UC-01 — edição após busca por ID.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Atualizar departamento com dados válidos — caminho feliz
* **Given** o formulário de edição está preenchido com dados atuais do departamento "MEC"
* **When** o administrador altera "Nome" para "Mecânica Industrial" e clica "Salvar"
* **Then** frontend valida localmente, envia `PUT /api/departamentos/{id}` via `request`, recebe `200 OK` com entidade atualizada, exibe toast "Departamento atualizado" e atualiza a lista

#### Scenario 2: Tentar atualizar para código duplicado — erro de negócio
* **Given** outro departamento já usa o código "ELE"
* **When** o administrador tenta mudar o código de "MEC" para "ELE"
* **Then** backend retorna `409 Conflict`, `handleApiError` exibe toast "Registro já existe", formulário permanece aberto

#### Scenario 3: Validação frontend impede submissão inválida — caso de borda
* **Given** o administrador limpa o campo "Nome"
* **When** tenta submeter
* **Then** frontend bloqueia, destaca campo, mensagem "Campo obrigatório" — sem chamada à API

#### Scenario 4: Falha de autenticação durante atualização — erro de auth (BR-06)
* **Given** token JWT expirou e refresh falhou
* **When** o administrador submete
* **Then** `authInterceptor` detecta 401, falha no refresh, redireciona para login limpo, preserva dados do formulário em `sessionStorage` para recuperação pós-login

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — edição de departamento]
* **Estados:** loading (botão desabilitado + spinner), error (toast), success (toast + lista atualizada)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, formulário de edição
* **Dependências técnicas:** Endpoint `PUT /api/departamentos/{id}`, refresh token endpoint
* **Considerações de performance/segurança:** Retry único automático após refresh (BR-06), latência P95 < 800 ms

### 6. Out of Scope
* Alteração de código de departamento se já houver ordens vinculadas (regra de backend)

### 7. Definition of Ready
- [ ] Contrato `PUT /api/departamentos/{id}` alinhado

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, 409, validação, auth), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados
* **Use Cases relacionados:** UC-01 (AS-1)
* **Riscos relacionados:** BR-06 (authInterceptor + refresh)

---

## US-CAD-05 — Excluir Departamento Sem Vinculações

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, crud]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** excluir um departamento que não possui ordens vinculadas
* **So that** a base de cadastros permaneça limpa e consistente

### 2. Business Context
Fluxo AS-2 do UC-01 — exclusão permitida apenas sem ordens vinculadas (BR-05).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Excluir departamento sem vínculos — caminho feliz
* **Given** o departamento "MEC" não possui ordens de manutenção associadas
* **When** o administrador clica "Excluir", confirma no modal
* **Then** frontend envia `DELETE /api/departamentos/{id}`, backend verifica ausência de ordens (BR-05), retorna `204 No Content`, frontend remove da lista local e exibe toast "Departamento excluído"

#### Scenario 2: Tentar excluir departamento com ordens vinculadas — erro de negócio (BR-05)
* **Given** o departamento "MEC" possui 3 ordens de manutenção vinculadas
* **When** o administrador tenta excluir
* **Then** backend retorna `409 Conflict` com mensagem "Entidade possui ordens vinculadas", `handleApiError` processa, frontend exibe modal informativo: "Não é possível excluir — existem ordens de manutenção associadas. Reatribua ou conclua as ordens antes."

#### Scenario 3: Cancelar exclusão no modal — caso de borda
* **Given** o modal de confirmação está aberto
* **When** o administrador clica "Cancelar" ou fecha o modal
* **Then** nenhuma chamada à API é feita, lista permanece inalterada

#### Scenario 4: Falha de rede na exclusão — erro de infraestrutura
* **Given** erro de rede ao enviar DELETE
* **When** a requisição falha
* **Then** `handleApiError` loga, toast "Erro ao excluir — tente novamente", item permanece na lista

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal de confirmação de exclusão]
* **Estados:** loading (modal com spinner), error (modal informativo BR-05 ou toast rede), success (item removido da lista + toast)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, modal de confirmação, lista de departamentos
* **Dependências técnicas:** Endpoint `DELETE /api/departamentos/{id}`, validação BR-05 no backend
* **Considerações de performance/segurança:** Confirmação em duas etapas (botão + modal), authInterceptor

### 6. Out of Scope
* Exclusão em lote
* Reatribuição de ordens (ação separada)

### 7. Definition of Ready
- [ ] Mensagem exata do erro 409 alinhada com Backend

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, 409 BR-05, cancelar, erro rede), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API
* **Use Cases relacionados:** UC-01 (AS-2)
* **Riscos relacionados:** BR-05 (exclusão bloqueada por ordens vinculadas)

---

## US-CAD-06 a US-CAD-20 — CRUD Filiais, Fornecedores, Funcionários

* **Priority:** Must
* **Complexity:** 3 cada (similar a US-CAD-01 a US-CAD-05)
* **Épico:** Cadastros Mestres
* **Labels:** [frontend, cadastros, crud]

### 1. Description (INVEST)
* **As a** Administrador de Cadastros
* **I want to** criar, listar, buscar, atualizar e excluir **Filiais**, **Fornecedores** e **Funcionários**
* **So that** esses cadastros mestres estejam disponíveis para o módulo de ordens de manutenção

### 2. Business Context
Mesmo padrão do UC-01 para 4 entidades: departamentos, filiais, fornecedores, funcionários. Total de 20 stories (4 entidades × 5 operações). Funcionários são críticos para UC-02 (técnico responsável obrigatório — BR-01).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: CRUD completo para cada entidade — caminho feliz
* **Given** administrador autenticado
* **When** executa criar/listar/buscar/atualizar/excluir em Filiais/Fornecedores/Funcionários
* **Then** mesmo comportamento de US-CAD-01 a US-CAD-05, adaptando campos obrigatórios por entidade:
  - **Filial:** código, nome, endereço
  - **Fornecedor:** CNPJ, nome fantasia, razão social, contato
  - **Funcionário:** matrícula, nome, cargo, departamento, filial, tipo (interno/terceiro)

#### Scenario 2: Validações específicas por entidade — caso de borda
* **Given** campos obrigatórios de cada entidade
* **When** submete com campo vazio
* **Then** validação frontend bloqueia com mensagem inline específica

#### Scenario 3: Exclusão bloqueada por integridade referencial — erro de negócio
* **Given** filial/fornecedor/funcionário vinculado a ordens
* **When** tenta excluir
* **Then** backend retorna `409 Conflict` (BR-05), frontend exibe modal informativo específico

#### Scenario 4: Funcionário — técnico responsável obrigatório para ordens (BR-01)
* **Given** lista de funcionários vazia
* **When** usuário tenta criar ordem (US-ORD-01)
* **Then** frontend exibe alerta "Cadastre técnicos antes de criar ordens" e desabilita "Nova Ordem" (EX-2 do UC-02)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — telas de cadastro por entidade]
* **Estados:** consistentes com US-CAD-01 a US-CAD-05

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js` (request, handleApiError, authInterceptor), componentes genéricos de CRUD reutilizáveis por entidade
* **Dependências técnicas:** Endpoints `POST/GET/PUT/DELETE /api/{filiais|fornecedores|funcionarios}` e `/{id}`
* **Considerações de performance/segurança:** Reutilização de componentes, validações específicas por entidade no frontend

### 6. Out of Scope
* Importação em lote (CSV/Excel)
* Hierarquia de filiais/departamentos

### 7. Definition of Ready
- [ ] Contratos de API por entidade alinhados com Backend
- [ ] Campos obrigatórios/opcionais definidos por entidade

### 8. Definition of Done
- [ ] 20 stories implementadas, testadas, revisadas
- [ ] Componentes genéricos de CRUD documentados

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados
* **Use Cases relacionados:** UC-01
* **Riscos relacionados:** Open Issue — Contrato OpenAPI formal não existe; console.error/log residuais

---

## US-ORD-01 — Criar Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, crud, business-critical]

### 1. Description (INVEST)
* **As a** Gestor de Manutenção ou Técnico de Campo
* **I want to** criar uma nova ordem de manutenção informando ativo, problema, prioridade, departamento, filial, técnico responsável e tipo
* **So that** a demanda seja registrada, rastreável e alocada para execução

### 2. Business Context
UC-02 — caso de uso de alta frequência (múltiplas vezes/dia). Pré-requisito: cadastros mestres populados (US-CAD-01 a US-CAD-20). BR-01 exige técnico responsável na criação.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar ordem com todos os campos válidos — caminho feliz
* **Given** o usuário autenticado acessa "Nova Ordem", existem departamentos, filiais, funcionários (técnicos) cadastrados
* **When** preenche: Ativo "Bomba-01", Descrição "Vazamento no selo mecânico", Prioridade "Alta", Departamento "MEC", Filial "SP-01", Técnico "João Silva", Tipo "Corretiva", Fornecedor (opcional) vazio, e clica "Criar"
* **Then** frontend valida localmente (campos obrigatórios + técnico alocado), envia `POST /api/ordens` via `request`, backend valida BR-01 (estado "Aberta" + técnico), persiste, retorna `201 Created` com ordem completa (ID, estado "Aberta", timestamps), frontend exibe toast "Ordem criada com sucesso" e redireciona para detalhe (`buscarPorId`)

#### Scenario 2: Tentar criar ordem sem técnico responsável — validação frontend (BR-01)
* **Given** formulário de nova ordem
* **When** o usuário deixa "Técnico responsável" vazio e tenta submeter
* **Then** frontend bloqueia, destaca campo, exibe mensagem "Técnico responsável é obrigatório para criar a ordem (BR-01)" — não chama API

#### Scenario 3: Cadastros mestres ausentes — caso de borda (EX-2 UC-02)
* **Given** listas de departamentos/filiais/funcionários retornam array vazio do backend
* **When** o usuário acessa "Nova Ordem"
* **Then** frontend exibe alerta "Cadastre departamentos, filiais e técnicos antes de criar ordens" e desabilita botão "Nova Ordem"

#### Scenario 4: Erro de validação backend (400/422) — erro de negócio
* **Given** payload com filial inválida (ID inexistente)
* **When** submete
* **Then** backend retorna `400/422`, `handleApiError` exibe toast com mensagem do backend, mantém formulário preenchido para correção

#### Scenario 5: Falha de autenticação/token expirado durante criação — erro de auth (BR-06)
* **Given** token expira no momento do POST
* **When** `authInterceptor` detecta 401, tenta refresh, falha
* **Then** redireciona para login limpo, preserva dados do formulário em `sessionStorage` para recuperação pós-login

#### Scenario 6: Falha de rede/timeout — erro de infraestrutura
* **Given** rede instável ou timeout >30s
* **When** requisição falha
* **Then** `handleApiError` loga, toast "Erro de conexão — tente novamente", mantém estado anterior

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — formulário de nova ordem]
* **Estados:** loading (submetendo), empty (listas mestras vazias → alerta + botão desabilitado), error (toast + formulário preservado), success (toast + redirect para detalhe)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, formulário de ordem (com selects dependentes de cadastros mestres), carregamento inicial de listas mestras (cache local após primeiro load < 1s)
* **Dependências técnicas:** Endpoint `POST /api/ordens`, endpoints de listagem de cadastros mestres (`GET /api/departamentos`, `/api/filiais`, `/api/funcionarios?tipo=tecnico`), `sessionStorage` para recuperação pós-login
* **Considerações de performance/segurança:** Latência P95 < 800 ms, listas mestras cacheadas localmente, JWT + refresh automático, validação BR-01 no frontend e backend

### 6. Out of Scope
* Criação a partir de template/checklist (AS-1 UC-02 — futuro, fora de escopo)
* Autocomplete de ativo (open issue — alinhar com Backend)

### 7. Definition of Ready
- [ ] Contrato `POST /api/ordens` definido (campos obrigatórios/opcionais, enums prioridade/tipo)
- [ ] Listas mestras disponíveis na API
- [ ] Design aprovado

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, validação BR-01, mestres ausentes, 400, auth, rede), revisado
- [ ] Testes E2E fluxo completo criar → detalhe

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados, NFR-Carregamento-Listas-Mestras (<1s)
* **Use Cases relacionados:** UC-02
* **Riscos relacionados:** BR-01, BR-06, Open Issues — campos obrigatórios vs opcionais, validação de ativo

---

## US-ORD-02 — Iniciar Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, workflow, business-critical]

### 1. Description (INVEST)
* **As a** Técnico de Campo
* **I want to** iniciar a execução de uma ordem aberta que me foi alocada
* **So that** o sistema registre o início real do trabalho e a ordem transicione para "Em Andamento"

### 2. Business Context
UC-03 — transição de estado "Aberta" → "Em Andamento". Muito alta frequência (cada ordem ativa passa uma vez). BR-01: só pode iniciar se estado "Aberta" e técnico responsável = usuário logado.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Iniciar ordem alocada ao técnico logado — caminho feliz
* **Given** o técnico logado acessa "Minhas Ordens" → filtro "Abertas", clica na ordem "ORD-123" (estado "Aberta", responsável = técnico logado)
* **When** o detalhe carrega (`GET /api/ordens/123` via `buscarPorId`), exibe botão "Iniciar" habilitado, técnico clica e confirma no modal
* **Then** frontend envia `PATCH /api/ordens/123/iniciar` (ou `PUT` com estado "EM_ANDAMENTO"), backend valida BR-01, transiciona, retorna `200 OK` com ordem atualizada (estado "Em Andamento", `iniciadoEm`, `iniciadoPor`), frontend atualiza UI: badge "Em Andamento", botão "Iniciar" oculto, timestamp visível

#### Scenario 2: Ordem não está "Aberta" — erro de negócio (BR-01)
* **Given** ordem já está "Em Andamento", "Aprovada", "Concluída" ou "Cancelada"
* **When** técnico tenta iniciar (ou backend valida)
* **Then** backend retorna `409 Conflict` "Transição inválida", `handleApiError` exibe toast, frontend desabilita botão "Iniciar" e recarrega estado via `buscarPorId`

#### Scenario 3: Técnico logado não é o responsável — caso de borda
* **Given** ordem "Aberta" mas `responsavelId` ≠ `usuarioLogado.id` e sem permissão de delegação
* **When** técnico acessa o detalhe
* **Then** frontend oculta/desabilita botão "Iniciar" com tooltip "Apenas o técnico responsável pode iniciar"

#### Scenario 4: Falha de rede/timeout — erro de infraestrutura
* **Given** erro de rede no PATCH de iniciar
* **When** requisição falha
* **Then** `handleApiError` loga, toast "Erro ao iniciar — tente novamente", botão permanece habilitado para retry

#### Scenario 5: Token expirado durante ação — erro de auth (BR-06)
* **Given** 401 no PATCH, refresh falha
* **When** `authInterceptor` processa
* **Then** redireciona login, preserva `ordemId` para retomar após autenticação

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — detalhe da ordem com botão "Iniciar"]
* **Estados:** loading (modal confirmando + spinner), error (toast), success (badge atualizado, botão oculto, timestamp)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, `buscarPorId`, componente de detalhe da ordem com lógica de permissão (role/ownership)
* **Dependências técnicas:** Endpoint de transição `PATCH /api/ordens/{id}/iniciar` (ou `PUT /api/ordens/{id}` com `{estado: "EM_ANDAMENTO"}`) — a confirmar com Backend
* **Considerações de performance/segurança:** Latência P95 < 800 ms, controle de UI baseado em ownership (responsável = usuário logado), apenas online (offline-first [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: PWA com sincronização posterior não especificado no BRD)

### 6. Out of Scope
* Delegação de início a outro técnico (AS-1 UC-03 — futuro, fora de escopo)

### 7. Definition of Ready
- [ ] Contrato exato do endpoint de transição definido com Backend
* [ ] Regra de permissão (apenas responsável vs delegação) decidida

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, 409 estado, permissão, rede, auth), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados
* **Use Cases relacionados:** UC-03
* **Riscos relacionados:** BR-01, BR-06, Open Issues — contrato endpoint, permissão delegação

---

## US-ORD-03 — Aprovar Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, workflow, business-critical, compliance]

### 1. Description (INVEST)
* **As a** Aprovador/Supervisor
* **I want to** revisar evidências de execução e aprovar uma ordem em andamento
* **So that** a ordem possa ser concluída pelo técnico e os custos sejam contabilizados no ativo (BR-04)

### 2. Business Context
UC-04 — transição "Em Andamento" → "Aprovada". Alta frequência. BR-02: exige evidência (checklist/foto) — backend valida `canApprove: true`. BR-03: concluir só após aprovar.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Aprovar ordem com evidências suficientes — caminho feliz
* **Given** o aprovador autenticado (role "aprovador") acessa lista "Para Aprovação" (filtro estado "Em Andamento" + `canApprove: true`), clica na ordem "ORD-123"
* **When** o detalhe carrega (`buscarPorId`), exibe evidências anexadas (checklist, fotos — retornadas pela API) e botão "Aprovar" habilitado, aprovador revisa, clica "Aprovar", confirma no modal (opcional: preenche "Observações da aprovação")
* **Then** frontend envia `PATCH /api/ordens/123/aprovar` com body opcional `{observacoes}`, backend valida BR-02 (evidência presente), transiciona, retorna `200 OK` com ordem atualizada (estado "Aprovada", `aprovadoEm`, `aprovadoPor`), frontend atualiza UI: badge "Aprovada", botão "Aprovar" oculto, "Concluir" visível para técnico

#### Scenario 2: Aprovar com observações — caso alternativo (AS-1 UC-04)
* **Given** mesmo cenário feliz
* **When** aprovador preenche "Observações da aprovação" no modal
* **Then** payload inclui `observacoesAprovacao`, backend persiste no histórico, mesmo fluxo de sucesso

#### Scenario 3: Evidências insuficientes — erro de negócio (BR-02)
* **Given** ordem "Em Andamento" mas `canApprove: false` (sem checklist/foto)
* **When** aprovador acessa detalhe
* **Then** frontend oculta/desabilita "Aprovar", exibe banner "Aguardando evidências do técnico (checklist/foto)"

#### Scenario 4: Tentar aprovar sem evidências via API — erro de negócio
* **Given** aprovador força chamada (ex: ferramenta externa) ou race condition
* **When** backend recebe PATCH sem evidências
* **Then** retorna `409 Conflict` "Evidências necessárias", `handleApiError` toast, recarrega estado

#### Scenario 5: Ordem não está "Em Andamento" — erro de negócio
* **Given** estado divergente (já "Aprovada" ou "Cancelada")
* **When** tentativa de aprovar
* **Then** `409 Conflict` "Transição inválida", toast, recarrega

#### Scenario 6: Aprovador sem permissão — caso de borda
* **Given** usuário logado não tem role "aprovador"
* **When** acessa detalhe de ordem "Em Andamento"
* **Then** frontend não exibe botão "Aprovar" (controle de UI baseado em role do token/JWT)

#### Scenario 7: Falha de rede/auth — erros de infraestrutura/auth
* **Given** mesmos padrões US-ORD-02 EX-3/EX-4
* **When** falha
* **Then** mesmos tratamentos (toast, retry, redirect login + preservar ordemId)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — detalhe da ordem com evidências e botão "Aprovar"]
* **Estados:** loading (carregando evidências), empty (sem evidências → banner), error (toast), success (badge + botão "Concluir" visível)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, `buscarPorId`, componente de detalhe com renderização condicional de evidências e botão "Aprovar" (baseado em `canApprove` e role)
* **Dependências técnicas:** Endpoint `PATCH /api/ordens/{id}/aprovar` com body opcional `{observacoes}`, API retorna `canApprove: boolean` no `GET /api/ordens/{id}` quando estado = "Em Andamento"
* **Considerações de performance/segurança:** Latência P95 < 800 ms, auditoria: log de aprovação imutável (backend), controle de acesso por role no token

### 6. Out of Scope
* Anexo de evidências pelo técnico (fora de escopo atual — BRD Future Considerations: "Checklists digitais e anexos fotográficos" como futuro). **Gap crítico** — alinhar com Backend se evidência é apenas campo texto "relato" no MVP.

### 7. Definition of Ready
- [ ] Contrato `PATCH /api/ordens/{id}/aprovar` definido
- [ ] Definição de `canApprove` e estrutura de evidências no `GET /api/ordens/{id}`
- [ ] Role "aprovador" no token/JWT confirmada

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, observações, evidências insuficientes UI/API, estado inválido, permissão, rede, auth), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados, NFR-Auditoria
* **Use Cases relacionados:** UC-04
* **Riscos relacionados:** BR-02, BR-03, BR-06, Open Issues — como técnico anexa evidências no MVP, role-based vs alocação

---

## US-ORD-04 — Concluir Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, workflow, business-critical, financeiro]

### 1. Description (INVEST)
* **As a** Técnico de Campo
* **I want to** concluir uma ordem aprovada informando custos reais (mão de obra, materiais, terceiros)
* **So that** a ordem seja finalizada, o `custoTotalPorAtivo` seja atualizado automaticamente (BR-04) e a base de custos fique consistente

### 2. Business Context
UC-05 — transição "Aprovada" → "Concluída". Alta frequência. BR-03: concluir só após aprovar. BR-04: `custoTotalPorAtivo` = soma custos ordens concluídas (cálculo no backend).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Concluir ordem com ajuste de custos — caminho feliz
* **Given** técnico logado (responsável) acessa "Minhas Ordens" → filtro "Aprovadas", clica na ordem "ORD-123" (estado "Aprovada")
* **When** detalhe carrega (`buscarPorId`), exibe resumo de custos (se pré-preenchidos) + botão "Concluir" habilitado, técnico clica, modal solicita confirmação e permite ajustar custos finais (horas reais, materiais, valor terceiros), técnico confirma
* **Then** frontend envia `PATCH /api/ordens/123/concluir` com `{custos: {maoDeObra, materiais, terceiros}}`, backend valida BR-03 (estado "Aprovada"), persiste custos, recalcula `custoTotalPorAtivo` do ativo atomicamente, transiciona para "Concluída", retorna `200 OK` com ordem finalizada + `custoTotalPorAtivo` atualizado, frontend atualiza UI: badge "Concluída", todos botões desabilitados, exibe custo total da ordem e do ativo

#### Scenario 2: Concluir sem ajuste (valores padrão) — caso alternativo (AS-1 UC-05)
* **Given** modal exibe valores pré-preenchidos
* **When** técnico apenas confirma
* **Then** mesmo endpoint, mesmo fluxo de sucesso

#### Scenario 3: Ordem não está "Aprovada" — erro de negócio (BR-03)
* **Given** ordem em estado "Em Andamento", "Cancelada", etc.
* **When** técnico tenta concluir (ou backend valida)
* **Then** backend `409 Conflict` "Concluir só permitido após aprovação", frontend toast, recarrega estado

#### Scenario 4: Custos obrigatórios não informados — erro de validação
* **Given** backend exige pelo menos um custo > 0
* **When** técnico submete com todos custos zerados
* **Then** backend `400 Bad Request`, `handleApiError` exibe erros de validação por campo no modal, técnico corrige e reenvia

#### Scenario 5: Falha no cálculo de custoTotalPorAtivo — erro de backend
* **Given** backend retorna `500` ou valor inconsistente
* **When** conclusão processada
* **Then** `handleApiError` loga, toast "Erro ao finalizar — contate suporte", ordem pode ficar inconsistente → requer intervenção manual

#### Scenario 6: Falha de rede/auth — erros de infraestrutura/auth
* **Given** mesmos padrões US-ORD-02
* **When** falha
* **Then** mesmos tratamentos

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal de conclusão com campos de custo]
* **Estados:** loading (modal + spinner), error (validação inline no modal ou toast), success (badge "Concluída", custos exibidos, botões desabilitados)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, `buscarPorId`, modal de conclusão com formulário de custos, exibição de `custoTotalPorAtivo` retornado
* **Dependências técnicas:** Endpoint `PATCH /api/ordens/{id}/concluir` com body de custos, backend recalcula `custoTotalPorAtivo` atomicamente
* **Considerações de performance/segurança:** Latência P95 < 800 ms, consistência: valor exibido deve bater com backend (teste de contrato Pact — BRD Risk), frontend não calcula — apenas exibe valor retornado

### 6. Out of Scope
* Cálculo de custos no frontend
* Permissão de concluir por técnico não responsável (open issue)

### 7. Definition of Ready
- [ ] Contrato `PATCH /api/ordens/{id}/concluir` definido (campos de custo, obrigatórios)
- [ ] Regras de validação de custo alinhadas com Backend/Financeiro

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, sem ajuste, 409 estado, 400 custos, 500 cálculo, rede, auth), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados, NFR-Consistência-Custos (Pact)
* **Use Cases relacionados:** UC-05
* **Riscos relacionados:** BR-03, BR-04, BR-06, Open Issues — campos de custo obrigatórios, permissão concluir

---

## US-ORD-05 — Cancelar Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, workflow]

### 1. Description (INVEST)
* **As a** Gestor de Manutenção ou Técnico de Campo
* **I want to** cancelar uma ordem desnecessária ou duplicada informando justificativa
* **So that** a ordem saia do fluxo ativo, fique auditável e não impacte `custoTotalPorAtivo` (BR-04)

### 2. Business Context
UC-06 — transição para "Cancelada" permitida em qualquer estado exceto "Concluída" (BR-03). Baixa/média frequência.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Cancelar ordem com justificativa — caminho feliz
* **Given** usuário (gestor ou técnico responsável) abre ordem em estado "Aberta", "Em Andamento" ou "Aprovada"
* **When** sistema exibe botão "Cancelar" habilitado (BR-03), usuário clica, modal exige justificativa obrigatória, usuário preenche "Duplicada — ordem ORD-124 já cobre este serviço" e confirma
* **Then** frontend envia `PATCH /api/ordens/{id}/cancelar` com `{justificativa}`, backend valida estado ≠ "Concluída", persiste, transiciona para "Cancelada", retorna `200 OK` com `canceladoEm`, `canceladoPor`, `justificativaCancelamento`, frontend atualiza UI: badge "Cancelada", botões desabilitados, justificativa visível no histórico

#### Scenario 2: Tentar cancelar ordem "Concluída" — erro de negócio (BR-03)
* **Given** ordem no estado "Concluída"
* **When** usuário tenta cancelar (ou força chamada)
* **Then** frontend não exibe botão "Cancelar" (UI); se forçado, backend `409 Conflict` "Ordem concluída não pode ser cancelada"

#### Scenario 3: Justificativa vazia — validação frontend
* **Given** modal de cancelamento aberto
* **When** usuário tenta confirmar sem preencher justificativa
* **Then** frontend valida localmente, bloqueia botão "Confirmar", mensagem "Justificativa é obrigatória"

#### Scenario 4: Usuário sem permissão — caso de borda
* **Given** usuário não é gestor nem técnico responsável
* **When** acessa detalhe da ordem
* **Then** botão "Cancelar" oculto/desabilitado (UI baseada em role/ownership)

#### Scenario 5: Falha de rede/auth — erros de infraestrutura/auth
* **Given** mesmos padrões
* **When** falha
* **Then** mesmos tratamentos

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal de cancelamento com campo justificativa]
* **Estados:** loading (modal + spinner), error (validação inline ou toast), success (badge + justificativa no histórico)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, modal de cancelamento, controle de permissão (role/ownership)
* **Dependências técnicas:** Endpoint `PATCH /api/ordens/{id}/cancelar` com `{justificativa}` (string obrigatória, máx 500 chars — a definir)
* **Considerações de performance/segurança:** Latência P95 < 800 ms, auditoria: justificativa imutável após confirmação

### 6. Out of Scope
* Regras granulares de quem cancela em cada estado (open issue — não definido no BRD)
* Estorno de custos parciais (BRD: apenas concluídas contam — assumir sem custos parciais persistidos antes de concluir)

### 7. Definition of Ready
- [ ] Regra de permissão por estado definida (técnico cancela "Aberta", gestor cancela "Aprovada"?)
- [ ] Tamanho máximo da justificativa definido

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, 409 concluída, validação justificativa, permissão, rede, auth), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados, NFR-Auditoria
* **Use Cases relacionados:** UC-06
* **Riscos relacionados:** BR-03, BR-04, BR-06, Open Issues — permissão por estado, estorno custos parciais

---

## US-ORD-06 — Listar Ordens de Manutenção com Filtros e Paginação

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, listagem, performance]

### 1. Description (INVEST)
* **As a** Gestor de Manutenção, Técnico de Campo ou Aprovador
* **I want to** visualizar a lista paginada de ordens com filtros por estado, prioridade, técnico, filial, período
* **So that** eu possa acompanhar o andamento, localizar ordens específicas e tomar decisões

### 2. Business Context
UC-07 — tela principal de todos os perfis. Muito alta frequência.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregar primeira página de ordens — caminho feliz
* **Given** usuário autenticado acessa "Ordens de Manutenção"
* **When** componente monta
* **Then** frontend chama `GET /api/ordens?page=1&size=20` via `request`, recebe lista paginada + metadados (total, page, size), renderiza tabela/cards com colunas: ID, Ativo, Estado, Prioridade, Técnico, Datas

#### Scenario 2: Aplicar múltiplos filtros — caso de borda
* **Given** lista carregada
* **When** usuário seleciona Estado "Aprovada", Prioridade "Alta", Técnico "João", Filial "SP-01", Período "01/01/2025 a 31/01/2025" e aplica
* **Then** frontend adiciona query params (`estado=APROVADA&prioridade=ALTA&tecnicoId=123&filialId=456&dataInicio=2025-01-01&dataFim=2025-01-31`) e recarrega lista filtrada

#### Scenario 3: Paginar e ordenar — caso de borda
* **Given** resultados filtrados > 20
* **When** usuário vai para página 2 ou clica no cabeçalho "Prioridade" para ordenar
* **Then** frontend chama `GET /api/ordens?page=2&size=20&sort=prioridade,desc` (exemplo) e renderiza

#### Scenario 4: Ordem não encontrada (acesso direto por ID) — erro de negócio (AS-1 UC-07)
* **Given** usuário cola URL `/ordens/999999` (ID inexistente ou sem permissão)
* **When** frontend chama `GET /api/ordens/999999` (`buscarPorId`)
* **Then** `handleApiError` processa 404, exibe página "Ordem não encontrada" com link "Voltar à lista"

#### Scenario 5: Falha no carregamento inicial — erro de infraestrutura
* **Given** rede, 5xx, timeout
* **When** tentativa de carregar
* **Then** `handleApiError` toast, botão "Tentar novamente", estado vazio com skeleton

#### Scenario 6: Filtros retornam resultado vazio — caso de borda
* **Given** filtros aplicados
* **When** backend retorna `data: []`, `total: 0`
* **Then** frontend exibe estado vazio ilustrado "Nenhuma ordem encontrada com estes filtros" + botão "Limpar filtros"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — lista de ordens com barra de filtros]
* **Estados:** loading (skeleton linhas), empty (ilustração + botão limpar filtros), error (toast + retry), success (tabela/cards)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, componente de lista com paginação, filtros, ordenação, `buscarPorId` para acesso direto
* **Dependências técnicas:** Endpoints `GET /api/ordens` (paginado, filtros via query) e `GET /api/ordens/{id}`, parâmetros: `page`, `size`, `estado`, `prioridade`, `tecnicoId`, `filialId`, `departamentoId`, `dataInicio`, `dataFim`, `sort`
* **Considerações de performance/segurança:** Latência P95 < 800 ms, paginação tamanho configurável (padrão 20, máx 100), cache não aplicado na lista (dados sensíveis/voláteis), detalhe pode cachear 30s [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA], authInterceptor em GETs para autorização

### 6. Out of Scope
* Exportar CSV (AS-2 UC-07 — futuro, Out-of-Scope BRD)
* Filtros salvos/preferências de usuário

### 7. Definition of Ready
- [ ] Parâmetros de query e estrutura de resposta da API definidos com Backend
- [ ] Permissão de visualização decidida (todas da filial do usuário? apenas próprias?)

### 8. Definition of Done
- [ ] Implementado, testado (carregar, filtrar, paginar, ordenar, 404 detalhe, erro carga, vazio), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados
* **Use Cases relacionados:** UC-07
* **Riscos relacionados:** BR-06, Open Issues — permissão visualização, campos listar vs detalhe

---

## US-ORD-07 — Visualizar Detalhe da Ordem de Manutenção

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Ordens de Manutenção
* **Labels:** [frontend, ordens, detalhe]

### 1. Description (INVEST)
* **As a** Gestor, Técnico ou Aprovador
* **I want to** visualizar o detalhe completo de uma ordem (dados, evidências, custos, histórico de transições)
* **So that** eu possa executar ações contextuais (iniciar, aprovar, concluir, cancelar) ou auditar

### 2. Business Context
UC-07 AS-1 — acesso direto por ID (URL, notificação). Usado por todas as stories de workflow (US-ORD-02 a US-ORD-05).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregar detalhe completo — caminho feliz
* **Given** usuário acessa `/ordens/123` (ordem existente, com permissão)
* **When** frontend chama `GET /api/ordens/123` (`buscarPorId`)
* **Then** recebe ordem completa com: dados cadastrais, evidências (se "Em Andamento"/"Aprovada"), custos (se "Concluída"), histórico de transições (iniciadoEm/por, aprovadoEm/por, concluídoEm/por, canceladoEm/por/justificativa), `custoTotalPorAtivo` do ativo (BR-04), renderiza tela de detalhe com ações conforme estado/permissão

#### Scenario 2: Ordem não encontrada/sem permissão — erro de negócio
* **Given** ID inexistente ou usuário sem permissão de visualização
* **When** busca
* **Then** `handleApiError` 404, página "Ordem não encontrada" + link "Voltar à lista"

#### Scenario 3: Falha de carga — erro de infraestrutura
* **Given** rede/timeout
* **When** busca
* **Then** toast, skeleton, botão "Recarregar"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — detalhe da ordem com abas: Dados, Evidências, Custos, Histórico]
* **Estados:** loading (skeleton cards), error (página 404 customizada), success (detalhe completo)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:buscarPorId`, `handleApiError`, `authInterceptor`, componente de detalhe com renderização condicional de seções/ações
* **Dependências técnicas:** Endpoint `GET /api/ordens/{id}` com resposta rica (evidências, custos, histórico, custoTotalPorAtivo)
*   **Considerações de performance/segurança:** Cache 30s no detalhe [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA], authInterceptor

### 6. Out of Scope
* Edição inline no detalhe (apenas ações de workflow)

### 7. Definition of Ready
- [ ] Estrutura completa da resposta `GET /api/ordens/{id}` definida

### 8. Definition of Done
- [ ] Implementado, testado (sucesso, 404, erro rede), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API
* **Use Cases relacionados:** UC-07 (AS-1), UC-03, UC-04, UC-05, UC-06
* **Riscos relacionados:** BR-04 (custoTotalPorAtivo no detalhe), BR-06

---

## US-KPI-01 — Visualizar Custo Total por Ativo

* **Priority:** Should
* **Complexity:** 5
* **Épico:** KPIs e Dashboards
* **Labels:** [frontend, dashboard, kpi, financeiro]

### 1. Description (INVEST)
* **As a** Gestor de Manutenção
* **I want to** visualizar o custo total acumulado por ativo (soma de ordens concluídas)
* **So that** eu possa basear decisões de substituição, plano preventivo e alocação orçamentária no TCO real

### 2. Business Context
UC-08 — BR-04: `custoTotalPorAtivo` = soma custos (mão de obra + material + terceiros) de ordens **concluídas** vinculadas ao ativo. Cálculo no backend, frontend apenas exibe. Média frequência (semanal/reuniões).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregar dashboard de custos por ativo — caminho feliz
* **Given** gestor acessa "Custos por Ativo" (menu lateral) ou dashboard com cards
* **When** frontend chama `GET /api/ativos/custos-totais` (ou `GET /api/ativos?include=custoTotal`)
* **Then** backend retorna array `[{ativoId, identificador, descricao, custoTotalPorAtivo, qtdOrdensConcluidas}]`, frontend renderiza tabela/grid ordenável por custo (decrescente padrão)

#### Scenario 2: Drill-down no ativo para breakdown — caso alternativo (AS-1 UC-08)
* **Given** tabela de custos carregada
* **When** gestor clica no ativo "Bomba-01"
* **Then** frontend chama `GET /api/ativos/{id}/ordens-concluidas`, recebe lista de ordens concluídas com custos individuais (mão de obra, material, terceiros), exibe breakdown no detalhe

#### Scenario 3: Ver custo do ativo no detalhe da ordem — contexto diferente (AS-1 UC-08)
* **Given** usuário visualiza detalhe de ordem "Concluída" (US-ORD-07)
* **When** tela renderiza
* **Then** exibe card "Custo Total do Ativo: R$ X.XXX,XX" vindo do campo `custoTotalPorAtivo` no response de `buscarPorId` da ordem

#### Scenario 4: Ativo sem ordens concluídas — caso de borda válido
* **Given** ativo nunca teve ordem concluída
* **When** carregado
* **Then** backend retorna `custoTotalPorAtivo: 0` ou `null`, frontend exibe "R$ 0,00" ou "Sem ordens concluídas" — não é erro

#### Scenario 5: Divergência frontend/backend — risco BRD
* **Given** valor exibido não confere com soma manual das ordens
* **When** detectado (teste de contrato Pact no CI)
* **Then** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: teste de contrato (Pact) no CI detecta. Frontend deve confiar no backend (BR-04: cálculo no backend). Se divergência, log + alerta para time.

#### Scenario 6: Falha de carga/timeout — erro de infraestrutura
* **Given** erro na chamada de agregado ou breakdown
* **When** falha
* **Then** `handleApiError` toast, skeleton loading, botão "Recarregar"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — dashboard custos por ativo + drill-down]
* **Estados:** loading (skeleton cards/tabela), empty (R$ 0,00 / "Sem ordens concluídas"), error (toast + retry), success (tabela ordenada + drill-down)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `api.js:request`, `handleApiError`, `authInterceptor`, componente de dashboard/tabela de ativos, componente de breakdown (modal ou página), componente de card de custo no detalhe da ordem
* **Dependências técnicas:** Endpoint agregado `GET /api/ativos/custos-totais` (ou `GET /api/ativos?include=custoTotal`), endpoint breakdown `GET /api/ativos/{id}/ordens-concluidas`, campo `custoTotalPorAtivo` no `GET /api/ordens/{id}`
* **Considerações de performance/segurança:** Latência P95 < 800 ms (dashboard carrega múltiplos ativos), consistência: valor idêntico em dashboard, tela de ativos e detalhe da ordem (single source of truth = backend)

### 6. Out of Scope
* Cadastro de ativos (CRUD) — BRD menciona apenas "departamentos, filiais, fornecedores, funcionários" como cadastros mestres. Ativos podem vir de outro sistema (CMMS/ERP) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* Fórmula exata de custo (horas × rateio + materiais + terceiros) — confirmar com Backend/Financeiro

### 7. Definition of Ready
- [ ] Endpoints de ativos/custos definidos com Backend
- [ ] Origem dos ativos (sistema próprio vs integração) decidida
- [ ] Fórmula de custo validada com Financeiro

### 8. Definition of Done
- [ ] Implementado, testado (dashboard, drill-down, card no detalhe, ativo sem ordens, divergência Pact, erro rede), revisado

### 9. Traceability
* **NFRs relacionadas:** NFR-Latência-P95, NFR-Taxa-Erro-API, NFR-Segurança-Dados, NFR-Consistência-Custos (Pact)
* **Use Cases relacionados:** UC-08
* **Riscos relacionados:** BR-04, BR-06, Open Issues — cadastro de ativos, fórmula de custo, origem dos ativos

---

## Notas Gerais de Rastreabilidade e Premissas

### Mapeamento Use Cases → User Stories
| Use Case | User Stories |
|----------|--------------|
| UC-01 | US-CAD-01 a US-CAD-20 (4 entidades × 5 ops) |
| UC-02 | US-ORD-01 |
| UC-03 | US-ORD-02 |
| UC-04 | US-ORD-03 |
| UC-05 | US-ORD-04 |
| UC-06 | US-ORD-05 |
| UC-07 | US-ORD-06, US-ORD-07 |
| UC-08 | US-KPI-01 |

### Business Rules Referenciadas
- **BR-01:** Ordem só inicia se "Aberta" + técnico responsável alocado (US-ORD-01, US-ORD-02)
- **BR-02:** Aprovar exige evidência (US-ORD-03)
- **BR-03:** Concluir só após aprovar; cancelar exceto "Concluída" (US-ORD-04, US-ORD-05)
- **BR-04:** `custoTotalPorAtivo` = soma custos ordens concluídas (backend) (US-ORD-04, US-KPI-01)
- **BR-05:** Exclusão mestres bloqueada se ordens vinculadas (US-CAD-05, US-CAD-06 a US-CAD-20)
- **BR-06:** `authInterceptor` JWT + refresh automático + retry único (todas as mutações e GETs)

### NFRs Referenciadas
- **NFR-Latência-P95:** < 800 ms em todas as chamadas `request`
- **NFR-Taxa-Erro-API:** < 1%
- **NFR-Segurança-Dados:** Zero vazamento no frontend (authInterceptor)
- **NFR-Carregamento-Listas-Mestras:** < 1s (cache local após primeiro load)
- **NFR-Auditoria:** Logs imutáveis de aprovação/cancelamento
- **NFR-Consistência-Custos:** Valor idêntico em todas as telas (teste Pact)

### Premissas Inferidas [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
1. **Offline-first/PWA:** Não implementado — escopo apenas online (UC-03, UC-04, UC-05, UC-06)
2. **Cache detalhe ordem:** 30s (UC-07)
3. **Evidências no MVP:** Gap crítico — BRD lista "checklist assinado ou foto" mas Future Considerations coloca "Checklists digitais e anexos fotográficos" como futuro. Alinhar se MVP usa apenas campo texto "relato" (UC-04, US-ORD-03)
4. **Permissão delegação início:** Assumir "apenas responsável" (UC-03)
5. **Permissão cancelar por estado:** Não definido — assumir gestor/técnico responsável conforme ownership (UC-06)
6. **Ativos:** Entidade distinta, pode vir de sistema externo (CMMS/ERP) — frontend apenas consome (UC-08)
7. **Role "aprovador" no token:** Assumir role-based (UC-04)
8. **Contratos de API:** Não documentados (OpenAPI) — alinhar com Backend antes de testes integrados (todas)

### Riscos Técnicos Identificados no Diagnóstico
- `api.js:request` — complexidade ciclomática alta (13) — considerar quebrar em funções menores
- `console.error/log` residuais em `api.js:26,49,52` — remover antes de produção
- Stack mínima: apenas `@popperjs/core` como dependência — sem framework UI explícito detectado (Vanilla JS ou framework não listado em package.json)