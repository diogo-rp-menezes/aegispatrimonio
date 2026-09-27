# User Stories Specification

> **Épico relacionado:** Gestão de Ativos · **Sprint:** 1 · **Status geral:** Backlog

## US-ATIVO-001 — Cadastrar novo ativo patrimonial

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Ativos
* **Labels:** [backend, frontend, rbac, auditoria]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio (role ADMIN)
* **I want to** cadastrar um novo ativo informando código patrimonial, descrição, tipo, localização, departamento, filial, fornecedor, data de aquisição, valor, vida útil e status
* **So that** o ativo seja centralizado no sistema com trilha de auditoria imutável e fique disponível para gestão, manutenção e relatórios

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Cadastro de ativos é a operação fundacional do módulo de Gestão de Patrimônio. O BRD (Seção 4) estabelece como North Star Metric a cobertura de health check em ativos cadastrados; sem cadastro completo e auditável, a métrica não é confiável. A regra BR-01 restringe criação a ADMIN; BR-02 exige log WORM; BR-03 valida entrada; BR-07 garante mapper null-safe.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Cadastro bem-sucedido por administrador
* **Given** o usuário está autenticado com role `ADMIN` e token JWT válido
* **And** tipos de ativo, departamentos, filiais e fornecedores já existem no sistema
* **When** o usuário preenche o formulário "Novo Ativo" com todos os campos obrigatórios válidos e submete
* **Then** o sistema persiste o ativo com ID gerado
* **And** registra log de auditoria com `acao=criar`, `usuario_id`, `timestamp`, `valores_novos` (BR-02)
* **And** retorna `201 Created` com DTO do ativo criado
* **And** o frontend exibe toast de sucesso e redireciona para a lista de ativos

#### Scenario 2: Usuário sem permissão (role USER) tenta cadastrar
* **Given** o usuário está autenticado com role `USER` e token JWT válido
* **When** o usuário tenta submeter o formulário "Novo Ativo"
* **Then** o backend retorna `403 Forbidden` (BR-01: `criar_comUser_deveRetornarForbidden`)
* **And** o frontend exibe mensagem "Acesso negado: apenas administradores podem criar ativos"
* **And** o ativo não é persistido

#### Scenario 3: Código patrimonial duplicado
* **Given** o usuário ADMIN submete formulário com código patrimonial já existente
* **When** o backend valida unicidade no banco
* **Then** retorna `409 Conflict` com mensagem "Código patrimonial já cadastrado"
* **And** o frontend destaca o campo em vermelho com a mensagem de erro
* **And** o usuário pode corrigir e reenviar

#### Scenario 4: Dados inválidos no payload
* **Given** o usuário ADMIN submete formulário com campo obrigatório ausente, data futura ou referência inexistente
* **When** o backend valida via Bean Validation (`@Valid`)
* **Then** retorna `400 Bad Request` com detalhes por campo (BR-03: `criar_comDadosInvalidos_deveRetornarBadRequest`)
* **And** o frontend exibe erros inline por campo
* **And** log de erro estruturado é gravado no backend

#### Scenario 5: Falha de conexão com banco de dados
* **Given** o backend recebe requisição válida mas o pool de conexões está esgotado ou timeout ocorre
* **When** `AtivoRepository.save` falha
* **Then** retorna `500 Internal Server Error`
* **And** o frontend exibe "Erro interno, tente novamente"
* **And** alerta é enviado para equipe de infraestrutura via monitoramento
* **And** transação é rolada automaticamente

#### Scenario 6: Falha ao registrar log de auditoria (storage WORM indisponível)
* **Given** o ativo foi persistido com sucesso mas o storage WORM falha ao gravar log
* **When** o interceptor de auditoria tenta registrar
* **Then** a transação principal **não** é rolada (auditoria é best-effort)
* **And** erro crítico é logado em sistema de observabilidade
* **And** alerta `CRITICAL` é disparado para equipe de segurança/compliance
* **And** job de reconciliação posterior preenche o gap

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tela "Novo Ativo"]
* **Estados da interface:** loading (submit), empty (formulário limpo), error (validação inline + toast), success (toast + redirect)
* **Validação client-side:** máscara para código patrimonial, datepicker para data aquisição (não permite futura), selects dependentes (filial → departamento)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoService.criar`, `AtivoMapper.toEntity`/`toDTO` (complexidade ciclomática 14 — refatorar), `AtivoRepository.save`, `AuditInterceptor`, `authInterceptor` (frontend/src/services/api.js)
* **Dependências técnicas:** `POST /api/ativos` (api-specification.md), tabelas `ativo`, `tipo_ativo`, `localizacao`, `departamento`, `filial`, `fornecedor`, `audit_log` (a implementar)
* **Considerações de performance/segurança:** Latência P95 ≤ 500ms (NFR); token JWT injetado por `authInterceptor`; validação `isAdmin()` no service; `@Transactional` no service method

### 6. Out of Scope
* Geração automática de código patrimonial (decisão pendente — Open Issue UC-01)
* Aprovação em dois níveis para ativos > R$ 50.000 (Open Issue UC-01)
* Migração de ativos legados de planilhas (carga inicial vs. manual)

### 7. Definition of Ready
- [ ] Critérios de aceite validados com PO
- [ ] Dependências: tipos, departamentos, filiais, fornecedores cadastrados (UC-06 a UC-12)
- [ ] Design da tela aprovado
- [ ] Estimativa 8 SP confirmada pelo time
- [ ] `AtivoMapper` refatorado (complexidade 14 → < 10) — tarefa técnica prévia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (service + mapper + controller)
- [ ] Testes de integração: `POST /api/ativos` com ADMIN, USER, duplicado, inválido
- [ ] Tipagem estrita (Java 17 + DTOs)
- [ ] Lint/format ok (Checkstyle/Spotless)
- [ ] Sem regressões de acessibilidade (WCAG 2.1 AA no formulário)
- [ ] Documentação OpenAPI atualizada
- [ ] Feature testada em staging com dados realistas
- [ ] Métricas de latência P95 instrumentadas (Micrometer + Prometheus)

### 9. Traceability
* **NFRs relacionadas:** NFR-01 (Latência P95 ≤ 500ms), NFR-02 (Disponibilidade 99.9%), NFR-03 (Auditoria WORM < 100ms)
* **Use Cases relacionados:** UC-01
* **Riscos relacionados:** RISK-01 (Mapper complexidade 14), RISK-02 (AuditLog não implementado), RISK-03 (setUsername stub)

---

## US-ATIVO-002 — Listar ativos com paginação, ordenação e filtros

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Gestão de Ativos
* **Labels:** [backend, frontend, performance, ux]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio, Analista de Manutenção, Usuário Final ou Auditor
* **I want to** visualizar a lista paginada de ativos com filtros por termo, status, filial, departamento e tipo
* **So that** eu possa localizar rapidamente ativos para consulta, edição, manutenção ou auditoria

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Tela principal do sistema (alta frequência — UC-02). BR-01 permite leitura para ADMIN e USER. BR-07 garante DTOs padronizados. NFR exige P95 ≤ 300ms para primeira página (cache quente) e suporte a 10.000+ ativos com paginação server-side.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregamento inicial da lista (primeira página)
* **Given** o usuário autenticado (qualquer role) acessa a tela "Ativos"
* **When** o frontend carrega `GET /api/ativos?page=0&size=20&sort=codigoPatrimonial,asc`
* **Then** o backend retorna `200 OK` com `{ content: [...], totalElements, totalPages, number, size }`
* **And** a tabela renderiza com paginação, ordenação por código patrimonial ascendente e ações por linha (ver, editar, baixar, health check)
* **And** a latência P95 ≤ 300ms

#### Scenario 2: Filtro por termo de busca (debounce 300ms)
* **Given** o usuário digita "notebook" no campo de busca
* **When** o frontend aguarda 300ms (debounce) e envia `GET /api/ativos?search=notebook&page=0&size=20`
* **Then** o backend delega para `AtivoSpecification` (a criar, padrão `ManutencaoSpecification`) e retorna apenas ativos com correspondência em código, descrição ou número de série
* **And** a tabela atualiza com resultados filtrados

#### Scenario 3: Filtros compostos (status, filial, departamento, tipo)
* **Given** o usuário seleciona `status=ATIVO`, `filialId=3`, `tipoId=2`
* **When** o frontend envia `GET /api/ativos?status=ATIVO&filialId=3&tipoId=2&page=0&size=20`
* **Then** o backend aplica todos os filtros via `AtivoSpecification` e retorna resultado paginado
* **And** os filtros ativos são exibidos como chips removíveis no topo da tabela

#### Scenario 4: Exportação para CSV
* **Given** o usuário clica no botão "Exportar" com filtros ativos
* **When** o frontend envia `GET /api/ativos/export?format=csv&status=ATIVO&filialId=3`
* **Then** o backend stream resposta `Content-Type: text/csv` sem paginação (todos os registros filtrados)
* **And** o frontend dispara download do arquivo `ativos-export-YYYYMMDD.csv`
* **And** exportação de até 50.000 registros completa em < 10s (streaming)

#### Scenario 5: Parâmetros de paginação inválidos
* **Given** o frontend envia `page=-1` ou `size=200` ou `sort=campoInexistente`
* **When** o backend valida parâmetros
* **Then** retorna `400 Bad Request` com mensagem descritiva
* **And** o frontend corrige para defaults (page=0, size=20) e reenvia

#### Scenario 6: Timeout em query complexa
* **Given** o usuário aplica muitos filtros simultâneos em base grande
* **When** a query excede `statement_timeout` do banco (ex: 30s)
* **Then** retorna `504 Gateway Timeout`
* **And** o frontend sugere reduzir escopo de filtros
* **And** índice de banco é revisado pela equipe de DBA

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tela "Lista de Ativos" com sidebar de filtros]
* **Estados da interface:** loading (skeleton table), empty (ilustração + "Nenhum ativo encontrado"), error (toast + retry), success (tabela + paginação)
* **Acessibilidade:** tabela com `scope="col"`, ordenação via teclado, anúncio de resultados por screen reader

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoService.listarTodos`, `AtivoRepository.findAll(Pageable)`, `AtivoSpecification` (a criar), `AtivoMapper.toDTO` (complexidade 14), frontend: `AtivosTable`, `FiltrosSidebar`, `useAtivos` hook
* **Dependências técnicas:** `GET /api/ativos`, `GET /api/ativos/export`, índices em `codigo_patrimonial`, `status`, `filial_id`, `tipo_id`, `departamento_id`
* **Considerações de performance/segurança:** Cache Redis para listas frequentes (Open Issue UC-02); filtro implícito por filial/departamento do usuário (Open Issue UC-02 — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]); debounce 300ms no frontend

### 6. Out of Scope
* Filtro implícito por escopo organizacional (filial/departamento do usuário logado) — decisão pendente
* Cache Redis — avaliação futura

### 7. Definition of Ready
- [ ] Critérios validados com PO
- [ ] `AtivoSpecification` criada seguindo padrão `ManutencaoSpecification`
- [ ] Índices de banco confirmados pelo DBA
- [ ] Design da tabela e filtros aprovado

### 8. Definition of Done
- [ ] Código implementado e revisado
- [ ] Testes unitários: service + specification + mapper
- [ ] Testes de integração: paginação, ordenação, cada filtro isolado e combinado, exportação CSV
- [ ] Testes de performance: 10.000 ativos, P95 ≤ 300ms (primeira página), export 50k < 10s
- [ ] Tipagem estrita, lint ok
- [ ] Acessibilidade validada
- [ ] OpenAPI atualizada
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-04 (Latência P95 ≤ 300ms), NFR-05 (Export 50k < 10s)
* **Use Cases relacionados:** UC-02
* **Riscos relacionados:** RISK-01 (Mapper complexidade), RISK-04 (Specification a criar)

---

## US-ATIVO-003 — Visualizar detalhe completo de um ativo

* **Priority:** Must
* **Complexity:** 3
* **Épico:** Gestão de Ativos
* **Labels:** [backend, frontend, ux]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio, Analista de Manutenção ou Auditor
* **I want to** acessar o detalhe completo de um ativo clicando na linha da tabela ou via URL direta
* **So that** eu possa ver todas as informações, health checks recentes, manutenções, auditoria e depreciação para tomada de decisão

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Operação de alta frequência (cada edição, manutenção ou auditoria dispara busca por ID — UC-03). BR-01 permite leitura ADMIN/USER. BR-04 exige 404 para ID inexistente. BR-07 mapper null-safe. NFR: P95 ≤ 200ms (PK lookup), consistência forte read-after-write.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Detalhe de ativo existente com health check recente
* **Given** o usuário autenticado clica na linha de um ativo na lista
* **When** o frontend envia `GET /api/ativos/{id}`
* **Then** o backend retorna `200 OK` com DTO detalhado incluindo relacionamentos (tipo, localização, departamento, filial, fornecedor, health checks recentes)
* **And** o frontend renderiza modal/aba com seções: Geral, Health Checks (badges 🟢/🟡/🔴 baseados em thresholds BR-05), Manutenções, Auditoria, Depreciação
* **And** latência P95 ≤ 200ms

#### Scenario 2: Ativo inexistente ou ID inválido
* **Given** o usuário acessa URL direta `/ativos/999999` ou UUID malformado
* **When** o backend executa `AtivoRepository.findById`
* **Then** retorna `404 Not Found` padronizado (BR-04: `buscarPorId_comIdInexistente_deveRetornarNotFound`)
* **And** o frontend exibe "Ativo não encontrado" com botão "Voltar à lista"

#### Scenario 3: Erro de lazy loading (proxy não inicializado)
* **Given** o mapper acessa relacionamento LAZY fora de transação
* **When** ocorre `LazyInitializationException`
* **Then** o service method tem `@Transactional(readOnly=true)` e fetch plan explícito via `EntityGraph` ou DTO projection
* **And** log de erro é gravado para correção de mapeamento
* **And** o usuário vê mensagem amigável "Erro ao carregar detalhes, tente novamente"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal/aba "Detalhe do Ativo" com tabs]
* **Estados da interface:** loading (spinner por seção), empty (sem health checks), error (inline por seção), success (dados completos)
* **Campos calculados exibidos:** `idadeAtivo`, `valorResidual`, `proximaManutencao` (assumptions UC-03)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoService.buscarPorId`, `AtivoRepository.findById`, `AtivoMapper.toDTO` com fetch plan, frontend: `AtivoDetalheModal`, `HealthCheckBadges`, `ManutencaoTimeline`
* **Dependências técnicas:** `GET /api/ativos/{id}`, `AtivoDetalheHardware` para health checks, `Manutencao` para histórico
* **Considerações de performance/segurança:** Projeção SQL nativa (`new DTO(...)`) vs entity+mapper (Open Issue UC-03); `@Transactional(readOnly=true)` obrigatório

### 6. Out of Scope
* Histórico completo de health checks no detalhe (apenas último + indicadores — performance)
* Edição inline no detalhe (usa US-ATIVO-004)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] DTO de resposta definido com todos os campos calculados
- [ ] Fetch plan / EntityGraph desenhado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso, 404, lazy loading error handling
- [ ] Performance: P95 ≤ 200ms validado em staging
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-06 (Latência P95 ≤ 200ms), NFR-07 (Consistência forte read-after-write)
* **Use Cases relacionados:** UC-03
* **Riscos relacionados:** RISK-01 (Mapper), RISK-05 (Lazy loading)

---

## US-ATIVO-004 — Atualizar dados cadastrais de ativo

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Ativos
* **Labels:** [backend, frontend, rbac, auditoria, concorrencia]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio (role ADMIN)
* **I want to** editar os dados de um ativo existente (descrição, localização, departamento, filial, fornecedor, valor, vida útil, status)
* **So that** o cadastro reflita a realidade física e contábil, com trilha de auditoria completa (diff anterior/novo) e controle de concorrência otimista

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Alterações cadastrais menos frequentes que consultas (UC-04). BR-01: apenas ADMIN. BR-02: log com diff completo. BR-03: validação. BR-07: mapper. NFR: P95 ≤ 400ms, concorrência otimista obrigatória (`@Version`). Campos imutáveis: `codigoPatrimonial`, `dataAquisicao` (Open Issue UC-04).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Atualização bem-sucedida por administrador
* **Given** o usuário ADMIN abre o detalhe de um ativo, clica "Editar", altera localização e valor, e salva
* **When** o frontend envia `PUT /api/ativos/{id}` com payload parcial
* **Then** o backend valida permissão ADMIN, carrega entidade atual (para diff), valida campos, faz merge seletivo, persiste
* **And** registra log de auditoria com `acao=atualizar`, `valores_anteriores` (snapshot antes), `valores_novos` (depois) (BR-02)
* **And** retorna `200 OK` com DTO atualizado
* **And** o frontend atualiza cache local e exibe toast de sucesso

#### Scenario 2: Usuário USER tenta atualizar
* **Given** usuário com role `USER` tenta editar ativo
* **When** envia `PUT /api/ativos/{id}`
* **Then** retorna `403 Forbidden` (BR-01: `atualizar_comUser_deveRetornarForbidden`)
* **And** frontend exibe "Acesso negado"

#### Scenario 3: Conflito de concorrência otimista
* **Given** dois usuários ADMIN abrem o mesmo ativo para edição simultaneamente
* **When** o primeiro salva e o segundo tenta salvar com versão desatualizada
* **Then** retorna `409 Conflict` com mensagem "Ativo foi alterado por outro usuário. Recarregue e tente novamente."
* **And** o frontend recarrega `buscarPorId` e reapresenta formulário com diff visual

#### Scenario 4: Dados inválidos (filial inexistente, valor negativo)
* **Given** usuário ADMIN submete formulário com filialId inexistente ou valor negativo
* **When** validação Bean Validation falha
* **Then** retorna `400 Bad Request` com `FieldError[]` (BR-03)
* **And** frontend destaca campos inválidos

#### Scenario 5: Falha de auditoria (storage WORM indisponível)
* **Given** ativo atualizado com sucesso mas storage WORM falha no log
* **When** interceptor de auditoria tenta gravar
* **Then** transação principal commitada, auditoria best-effort com alerta `CRITICAL` (mesma estratégia UC-01 EX-3)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — formulário de edição pré-preenchido]
* **Estados da interface:** loading (carregando dados atuais), dirty (campos alterados), saving, error (inline + toast), success (toast + view mode)
* **Confirmação:** modal "Tem certeza?" ao sair com alterações não salvas
* **Campos imutáveis:** `codigoPatrimonial` e `dataAquisicao` desabilitados (readonly) com tooltip explicativo

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoService.atualizar`, `AtivoMapper.toEntity` (merge seletivo — complexidade 14), `AtivoRepository.save`, `AuditInterceptor`, `@Version` na entidade, frontend: `AtivoForm` (react-hook-form + Zod)
* **Dependências técnicas:** `PUT /api/ativos/{id}`, entidade `Ativo` com `@Version`, `audit_log`
* **Considerações de performance/segurança:** Merge seletivo preserva campos não enviados (PATCH semântico via PUT); auditoria captura apenas campos de negócio (não `createdAt`, `version`); refatorar `AtivoMapper` em métodos por seção (identificação, localização, financeiro, técnico)

### 6. Out of Scope
* Definição final de campos imutáveis vs mutáveis (Open Issue UC-04)
* Notificação para gestores afetados por mudança de filial/departamento (Open Issue UC-04)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Lista de campos imutáveis definida
- [ ] `AtivoMapper` refatorado (tarefa técnica prévia)
* **Complexity:** 8 (inclui refatoração do mapper)

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso, 403, 409 concorrência, 400 validação, auditoria diff
- [ ] Teste de concorrência: 2 requests simultâneos → 1 success, 1 409
- [ ] Performance P95 ≤ 400ms
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-08 (Latência P95 ≤ 400ms), NFR-09 (Concorrência otimista)
* **Use Cases relacionados:** UC-04
* **Riscos relacionados:** RISK-01 (Mapper complexidade 14), RISK-02 (AuditLog)

---

## US-ATIVO-005 — Baixar ativo (baixa patrimonial formal)

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Ativos
* **Labels:** [backend, frontend, rbac, auditoria, compliance, lgpd]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio (role ADMIN)
* **I want to** dar baixa formal em um ativo (fim de vida útil, perda, roubo, doação) informando motivo, data, valor residual e documento comprovante
* **So that** o ativo seja removido da gestão ativa com trilha de auditoria imutável, conformidade LGPD/SOX e atualização de relatórios de custo total

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Operação irreversível, baixa frequência (5-20/mês — UC-05). BR-01: apenas ADMIN. BR-02: log com motivo e valor residual. BR-10: cascata em health checks se hard delete. Decisão crítica: soft delete (status `BAIXADO`) vs hard delete (LGPD) — Open Issue UC-05. Confirmação em duas etapas no frontend.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Baixa patrimonial padrão (soft delete)
* **Given** o usuário ADMIN abre detalhe de ativo `ATIVO`, clica "Baixar Ativo", preenche motivo "Fim de vida útil", data de hoje, valor residual calculado, e confirma digitando "CONFIRMO"
* **When** o frontend envia `POST /api/ativos/{id}/baixa` com payload
* **Then** o backend valida permissão, status passível de baixa, sem manutenções em andamento
* **And** atualiza status para `BAIXADO`, preenche `dataBaixa`, `motivoBaixa`, `valorResidual`
* **And** registra auditoria `acao=baixar` com `valores_anteriores` (status anterior) e `valores_novos` (dados de baixa) (BR-02)
* **And** retorna `200 OK` com DTO do ativo baixado
* **And** o frontend remove da lista ativa (filtro padrão `status != BAIXADO`) e exibe toast

#### Scenario 2: Usuário USER tenta baixar
* **Given** usuário `USER` tenta acessar ação de baixa
* **When** envia requisição
* **Then** retorna `403 Forbidden` (BR-01: `deletar_comUser_deveRetornarForbidden`)

#### Scenario 3: Ativo com manutenção em andamento
* **Given** ativo tem `Manutencao` com `status=EM_ANDAMENTO`
* **When** usuário tenta baixar
* **Then** retorna `409 Conflict` — "Não é possível baixar ativo com manutenção em andamento. Conclua ou cancele a manutenção primeiro."

#### Scenario 4: Ativo já baixado
* **Given** ativo com `status=BAIXADO`
* **When** usuário tenta baixar novamente
* **Then** retorna `400 Bad Request` — "Ativo já possui baixa registrada em {dataBaixa}"

#### Scenario 5: Hard delete (LGPD) — fluxo alternativo
* **Given** solicitação de Compliance para remoção irreversível de ativo com dados sensíveis
* **When** admin executa `DELETE /api/ativos/{id}` (hard delete)
* **Then** cascata remove health checks via `deleteByAtivoDetalheHardwareId` (BR-10), manutenções (restringir se houver histórico?)
* **And** auditoria `acao=deletar` com valores anteriores
* **And** operação irreversível — confirmação em duas etapas + digitação "CONFIRMO HARD DELETE"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal "Baixar Ativo" com dois passos: dados + confirmação]
* **Estados da interface:** loading, validation error, confirmation step (digitar "CONFIRMO"), success (toast + lista atualizada)
* **Acessibilidade:** foco no campo motivo, anúncio de ação irreversível

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoService.baixar` / `deletar`, `AtivoRepository`, `AuditInterceptor`, `ManutencaoRepository` (validação), `AtivoDetalheHardwareRepository` (cascata BR-10), frontend: `BaixaAtivoModal` (2 steps)
* **Dependências técnicas:** `POST /api/ativos/{id}/baixa` ou `DELETE /api/ativos/{id}`, `audit_log`, `manutencao`, `ativo_detalhe_hardware`
* **Considerações de performance/segurança:** Auditoria imutável obrigatória (WORM); transação única para baixa + auditoria; valor residual calculado automaticamente se não informado (depreciação linear — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA])

### 6. Out of Scope
* Reativação de ativo baixado (Open Issue UC-05)
* Integração contábil automática (Open Issue UC-05)

### 7. Definition of Ready
- [ ] Decisão soft vs hard delete aprovada (arquitetura + compliance)
- [ ] Critérios validados
- [ ] Design do modal 2-step aprovado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso soft delete, 403, 409 manutenção ativa, 400 já baixado, hard delete cascata
- [ ] Auditoria verificada no `audit_log`
- [ ] Performance: baixa frequência, sem NFR específico além de transação ACID
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging com cenário LGPD

### 9. Traceability
* **NFRs relacionadas:** NFR-10 (Operação irreversível — confirmação 2 etapas), NFR-11 (Auditoria WORM)
* **Use Cases relacionados:** UC-05
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-06 (Soft vs Hard delete), RISK-07 (BR-10 cascata)

---

## US-ATIVO-006 — Gerenciar tipos de ativo (catálogo mestre)

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Configurações Mestras
* **Labels:** [backend, frontend, admin, cache]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** criar, editar e excluir tipos de ativo (nome, descrição, categoria HARDWARE/MOBILIARIO/EQUIPAMENTO, vida útil padrão, exige health check)
* **So that** o catálogo padronizado permita classificação consistente no cadastro de ativos e filtros confiáveis

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Entidade mestra, baixa frequência (setup + ocasionais — UC-06). BR-01: apenas ADMIN. BR-02: auditoria. BR-03: unicidade nome. BR-07: mapper. NFR: cacheável no frontend (atualizar a cada 1h ou on-demand). `exigeHealthCheck=true` apenas para categoria HARDWARE.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar tipo de ativo
* **Given** admin autenticado acessa "Configurações > Tipos de Ativo" e clica "Novo"
* **When** preenche nome "Notebook", categoria "HARDWARE", vida útil 48 meses, exige health check = true, e salva
* **Then** backend valida ADMIN, unicidade nome, persiste, audita `acao=criar`, retorna `201 Created`
* **And** frontend atualiza select de tipos no cadastro de ativo

#### Scenario 2: Editar tipo de ativo
* **Given** admin edita tipo existente, altera vida útil para 60 meses
* **When** envia `PUT /api/tipos-ativo/{id}`
* **Then** valida ADMIN, persiste, audita `acao=atualizar`, retorna `200 OK`
* **And** frontend invalida cache e atualiza select

#### Scenario 3: Excluir tipo sem ativos associados
* **Given** admin exclui tipo "Mesa Antiga" que não tem ativos vinculados
* **When** envia `DELETE /api/tipos-ativo/{id}`
* **Then** valida ADMIN, remove, audita `acao=deletar`, retorna `204 No Content`

#### Scenario 4: Excluir tipo com ativos associados
* **Given** admin tenta excluir "Notebook" que tem 150 ativos cadastrados
* **When** envia `DELETE`
* **Then** retorna `409 Conflict` — "Não é possível excluir tipo com ativos cadastrados. Reclassifique os ativos primeiro."
* **And** sugere soft delete: `ativo=false` como alternativa

#### Scenario 5: Nome duplicado
* **Given** admin tenta criar "Notebook" já existente
* **When** valida unicidade
* **Then** retorna `400 Bad Request` (BR-03)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — grid de tipos com ações inline]
* **Estados:** loading, empty (ilustração + "Nenhum tipo cadastrado"), error, success
* **Cache frontend:** invalidar on mutation, TTL 1h

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `TipoAtivoService`, `TipoAtivoRepository`, `TipoAtivoMapper`, frontend: `TipoAtivoGrid`, `TipoAtivoForm`
* **Dependências técnicas:** `POST/PUT/DELETE /api/tipos-ativo`, tabela `tipo_ativo`, `audit_log`
* **Considerações de performance/segurança:** Baixa frequência — cache frontend agressivo; vida útil padrão usada para sugestão de depreciação no cadastro de ativo

### 6. Out of Scope
* Tipos personalizados por filial (Open Issue UC-06 — hoje global)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Design aprovado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD + 409 com ativos + 400 duplicado
- [ ] Cache frontend invalidado corretamente
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-12 (Cache frontend TTL 1h)
* **Use Cases relacionados:** UC-06
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-ATIVO-007 — Gerenciar localizações (hierarquia física)

* **Priority:** Should
* **Complexity:** 5
* **Épico:** Configurações Mestras
* **Labels:** [backend, frontend, admin, tree, performance]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** gerenciar a hierarquia física de localizações (Prédio > Andar > Sala > Posição) com move de subárvore
* **So that** a alocação de ativos seja precisa e a auditoria tenha rastreabilidade física completa

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Entidade mestra, baixa frequência (UC-07). BR-01, BR-02, BR-03, BR-07. NFR: tree renderização < 200ms para 500+ nós (lazy loading). Path materializado (`/predio-a/andar-2/sala-201/posicao-a1`) para queries rápidas de "ativos na sala 201 e filhas".

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar hierarquia de localizações
* **Given** admin em "Configurações > Localizações" clica "Novo Prédio"
* **When** preenche nome "Prédio A", tipo `PREDIO`, sem parent, e salva
* **Then** persiste com `path=/predio-a`, audita, retorna `201`
* **And** repete para Andar 2 (parent=Prédio A), Sala 201 (parent=Andar 2), Posição A1 (parent=Sala 201)
* **And** `path` materializado gerado automaticamente: `/predio-a/andar-2/sala-201/posicao-a1`

#### Scenario 2: Mover subárvore (alterar parentId)
* **Given** admin arrasta "Sala 201" para "Andar 3" no tree view
* **When** frontend envia `PUT /api/localizacoes/{id}` com novo `parentId`
* **Then** backend valida ausência de ciclos, atualiza `path` da sala e de todas as filhas em cascata
* **And** audita `acao=atualizar` com diff de path
* **And** retorna `200 OK`, frontend atualiza tree

#### Scenario 3: Excluir localização com ativos alocados
* **Given** admin tenta excluir "Sala 201" que tem 10 ativos
* **When** envia `DELETE`
* **Then** retorna `409 Conflict` — "Mova os ativos primeiro"
* **And** sugere soft delete `ativo=false`

#### Scenario 4: Lazy loading da tree (performance)
* **Given** hierarquia com 500+ nós
* **When** frontend carrega tree view
* **Then** carrega apenas raízes (prédios); filhos carregados sob demanda ao expandir
* **And** renderização completa < 200ms

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tree view com drag-and-drop, lazy load]
* **Estados:** loading por nó, empty, error, success
* **Integração futura:** planta baixa SVG/imagem (Open Issue UC-07)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `LocalizacaoService`, `LocalizacaoRepository`, `LocalizacaoMapper`, frontend: `LocalizacaoTree` (react-arborist ou similar), `LocalizacaoForm`
* **Dependências técnicas:** `POST/PUT/DELETE /api/localizacoes`, tabela `localizacao` com `parent_id`, `path` (materialized path), `audit_log`
* **Considerações de performance/segurança:** Índice em `path` para queries `LIKE '/predio-a/andar-2/%'`; validação de ciclos no service; `@Transactional` no move

### 6. Out of Scope
* Planta baixa visual (futuro)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Modelo de dados (materialized path) aprovado
- [ ] Componente tree selecionado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD, move subárvore (validação ciclos + path cascata), 409 com ativos, lazy load performance
- [ ] Tree render < 200ms com 500 nós em staging
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-13 (Tree render < 200ms)
* **Use Cases relacionados:** UC-07
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-ATIVO-008 — Gerenciar departamentos

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Configurações Mestras
* **Labels:** [backend, frontend, admin, cache]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** cadastrar departamentos (nome, código, descrição, filial pai, gestor responsável)
* **So that** a estrutura organizacional suporte filtros de ativos, relatórios por departamento e sincronização futura com RH/AD

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Entidade mestra, baixa frequência (UC-10). BR-01, BR-02, BR-03 (unicidade código por filial), BR-07. NFR: cache frontend (select options) — invalidar on mutation. Hierarquia simples: filial > departamento (sem sub-departamentos por enquanto). Sync AD/LDAP futuro — mapear `codigo` para `ou` do AD (Open Issue UC-10).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar departamento
* **Given** admin em "Configurações > Departamentos" clica "Novo"
* **When** preenche nome "TI", código "TI", filial "Matriz", gestor "João Silva", salva
* **Then** valida ADMIN, unicidade código por filial, persiste, audita, retorna `201`
* **And** frontend atualiza select de departamentos

#### Scenario 2: Excluir departamento com ativos/funcionários
* **Given** admin tenta excluir "TI" que tem ativos alocados
* **When** envia `DELETE`
* **Then** retorna `409 Conflict` — "Reatribua os ativos/funcionários primeiro"
* **And** sugere soft delete `ativo=false`

#### Scenario 3: Código duplicado na mesma filial
* **Given** admin tenta criar "TI" código "TI" na "Matriz" onde já existe
* **When** valida
* **Then** retorna `400 Bad Request`

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — grid simples com filial como agrupamento]
* **Estados:** loading, empty, error, success
* **Cache:** select options TTL 1h, invalidar on mutation

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `DepartamentoService`, `DepartamentoRepository`, `DepartamentoMapper`, frontend: `DepartamentoGrid`, `DepartamentoForm`
* **Dependências técnicas:** `POST/PUT/DELETE /api/departamentos`, tabela `departamento` (FK `filial_id`, `gestor_id`), `audit_log`
*   **Considerações de performance/segurança:** Cache frontend simples; unicidade composta `(filial_id, codigo)`

### 6. Out of Scope
* Sub-departamentos (futuro)
* Sync AD/LDAP (futuro)

### 7. Definition of Ready
- [ ] Critérios validados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD, 409, 400 duplicado, cache invalidation
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-12 (Cache frontend)
* **Use Cases relacionados:** UC-10
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-ATIVO-009 — Gerenciar filiais

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Configurações Mestras
* **Labels:** [backend, frontend, admin, timezone]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** cadastrar filiais (nome, código, endereço, CNPJ, telefone, email, responsável, timezone, flag matriz)
* **So that** a topologia corporativa suporte escopo de ativos, agendamentos de health check por timezone e relatórios por jurisdição

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Raiz da hierarquia organizacional (UC-11). Muito baixa frequência. BR-01, BR-02, BR-03 (unicidade código/CNPJ), BR-07. Regra: apenas uma matriz (`count(filial where matriz=true) <= 1`). Timezone usado em agendamentos e relatórios. Exclusão com ativos/departamentos/usuários → `409` (soft delete preferido).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar filial matriz
* **Given** admin cadastra primeira filial com `matriz=true`
* **When** valida
* **Then** persiste, audita, retorna `201`
* **And** frontend atualiza seletor global no header

#### Scenario 2: Tentar criar segunda matriz
* **Given** já existe filial matriz
* **When** admin tenta criar outra com `matriz=true`
* **Then** retorna `400 Bad Request` — "Já existe uma filial matriz cadastrada"

#### Scenario 3: Excluir filial com dependências
* **Given** filial tem departamentos, ativos, usuários
* **When** admin tenta excluir
* **Then** retorna `409 Conflict` — "Desative a filial (soft delete) em vez de excluir"

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — card de filial com badge "Matriz"]
*   **Estados:** loading, empty, error, success
*   **Seletor global:** header dropdown com filiais, persiste preferência no localStorage

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `FilialService`, `FilialRepository`, `FilialMapper`, frontend: `FilialGrid`, `FilialForm`, `FilialSelector` (header)
* **Dependências técnicas:** `POST/PUT/DELETE /api/filiais`, tabela `filial` (unique `codigo`, `cnpj`, `matriz` boolean), `audit_log`
* **Considerações de performance/segurança:** Timezone armazenado como IANA (ex: `America/Sao_Paulo`); usado em `@Scheduled` de health checks e geração de relatórios

### 6. Out of Scope
* Multi-moeda por filial (BRD não menciona)

### 7. Definition of Ready
- [ ] Critérios validados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD, validação matriz única, 409 dependências, timezone IANA
- [ ] Seletor global funcional no header
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** Nenhuma específica além de BRs
* **Use Cases relacionados:** UC-11
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-ATIVO-010 — Gerenciar fornecedores

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Configurações Mestras
* **Labels:** [backend, frontend, admin, cnpj]

### 1. Description (INVEST)
* **As a** Administrador de Sistema ou Gestor de Patrimônio
* **I want to** cadastrar fornecedores (nome, CNPJ, contato, email, telefone, endereço, site, observações, categorias)
* **So that** ativos e manutenções possam ser vinculados a fornecedores para histórico de compras, custos e SLA futuro

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Entidade mestra, baixa frequência (UC-12). BR-01 (ADMIN), BR-02, BR-03 (unicidade CNPJ), BR-07. NFR: busca por CNPJ indexada (consulta frequente em relatórios). Fornecedor de manutenção vinculado em `Manutencao.fornecedorId` para `custoTotalPorAtivo` (BR-06). Integração ReceitaWS para validação CNPJ — Open Issue UC-12.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar fornecedor
* **Given** admin em "Configurações > Fornecedores" clica "Novo"
* **When** preenche "Dell Brasil", CNPJ "00.000.000/0001-00", categorias [HARDWARE, MANUTENCAO], salva
* **Then** valida ADMIN, unicidade CNPJ, persiste, audita, retorna `201`
* **And** disponível em select no cadastro de ativo e manutenção

#### Scenario 2: CNPJ duplicado
* **Given** admin tenta cadastrar CNPJ já existente
* **When** valida
* **Then** retorna `400 Bad Request` — "CNPJ já cadastrado"

#### Scenario 3: Editar fornecedor
* **Given** admin altera telefone do fornecedor
* **When** envia `PUT`
* **Then** valida, persiste, audita, retorna `200`, frontend atualiza selects

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — grid com máscara CNPJ, chips de categoria]
* **Estados:** loading, empty, error, success
*   **Validação CNPJ:** máscara + dígito verificador client-side; server-side valida formato

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `FornecedorService`, `FornecedorRepository`, `FornecedorMapper`, frontend: `FornecedorGrid`, `FornecedorForm`
* **Dependências técnicas:** `POST/PUT/DELETE /api/fornecedores`, tabela `fornecedor` (unique `cnpj`), `audit_log`
* **Considerações de performance/segurança:** Índice em `cnpj`; categorias como array/ENUM ou tabela separada `fornecedor_categoria`

### 6. Out of Scope
* Contratos/SLA com fornecedores (BRD Out-of-Scope)
* Integração ReceitaWS (futuro)

### 7. Definition of Ready
- [ ] Critérios validados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD, 400 CNPJ duplicado, selects atualizados
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-14 (Busca CNPJ indexada)
* **Use Cases relacionados:** UC-12
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-ATIVO-011 — Gerenciar funcionários e provisionar acesso ao sistema

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Pessoas & Acesso
* **Labels:** [backend, frontend, admin, rh, security, bug-blocker]

### 1. Description (INVEST)
* **As a** Administrador de Sistema ou RH
* **I want to** cadastrar funcionários (nome, CPF, matrícula, email, cargo, filial, departamento, gestor, data admissão) e opcionalmente criar usuário de sistema com token JWT em operação atômica
* **So that** o onboarding seja único, o funcionário tenha custódia de ativos e acesso imediato ao sistema conforme seu papel

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Onboarding/offboarding (UC-13, UC-28). BR-01 (ADMIN), BR-02 (auditoria dupla), BR-03 (unicidade CPF/matrícula/email/username), BR-07, BR-08 (`createUserAndToken`). **BUG BLOQUEANTE:** `Usuario.setUsername` em `Usuario.java:86` tem corpo vazio (stub) — impede criação de usuário. Correção obrigatória antes de qualquer release. Roles padrão: `USER` + role por cargo (ex: `ANALISTA_MANUTENCAO`). `mockLogin` apenas testes.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Onboarding completo (funcionário + usuário + token)
* **Given** RH/Admin preenche formulário unificado com dados do funcionário + credenciais (username, senha temporária, roles) e marca "Criar acesso ao sistema"
* **When** envia `POST /api/funcionarios/com-usuario`
* **Then** backend valida ADMIN + unicidades, em transação única:
  - Salva `Funcionario`
  - Chama `UsuarioService.createUserAndToken` — **corrigir `setUsername` stub antes**
  - Auditoria `acao=criar` para Funcionário + `acao=criar` para Usuario
* **And** retorna `201` com `{ funcionario, usuario, accessToken, refreshToken }`
* **And** frontend exibe credenciais para entrega segura (ou envia email com link primeiro acesso)

#### Scenario 2: Funcionário já existe, apenas criar usuário
* **Given** funcionário cadastrado sem usuário
* **When** admin clica "Criar acesso" em `POST /api/funcionarios/{id}/usuario`
* **Then** valida, cria usuário vinculado, retorna token, audita

#### Scenario 3: CPF/matrícula/email duplicado
* **Given** dados duplicados
* **When** valida
* **Then** `400 Bad Request` com campo específico

#### Scenario 4: `setUsername` stub falha silenciosamente
* **Given** bug não corrigido
* **When** cria usuário
* **Then** `username=null` → login falha, token inválido
* **And** **BLOQUEANTE** — deve ser corrigido antes de testes de integração

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — formulário unificado com aba "Dados Pessoais" e "Acesso ao Sistema"]
* **Estados:** loading, validation error (inline por campo), success (modal com credenciais copiáveis + botão "Enviar por email")
* **Entrega de credenciais:** fluxo a definir (email vs impressão vs gestor) — Open Issue UC-28

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `FuncionarioService.createFuncionarioAndUsuario`, `UsuarioService.createUserAndToken`, `FuncionarioMapper`, `UsuarioMapper`, `Usuario.java:86` (CORRIGIR STUB), `AuditInterceptor`, frontend: `FuncionarioForm` (abas), `CredenciaisModal`
* **Dependências técnicas:** `POST /api/funcionarios/com-usuario`, `POST /api/funcionarios/{id}/usuario`, tabelas `funcionario`, `usuario`, `audit_log`, JWT provider
* **Considerações de performance/segurança:** Atomicidade transacional obrigatória; senha temporária expiração 24h + força troca no primeiro login; hash BCrypt; token JWT RS256

### 6. Out of Scope
* Integração AD/LDAP (futuro — substituirá cadastro manual)
* Fluxo de offboarding (desativação em cascata — Open Issue UC-13)

### 7. Definition of Ready
- [ ] **BUG BLOQUEANTE CORRIGIDO:** `Usuario.setUsername` implementado corretamente
- [ ] Critérios validados
- [ ] Design do formulário unificado aprovado
- [ ] Matriz de roles por cargo definida

### 8. Definition of Done
- [ ] Código + revisão
- [ ] **Teste de regressão obrigatório:** `setUsername` funcional
- [ ] Testes: onboarding atômico, 400 duplicados, criação usuário separado, auditoria dupla
- [ ] Transação: rollback se usuário falhar após funcionário salvo
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging com cenário completo

### 9. Traceability
* **NFRs relacionadas:** NFR-15 (Atomicidade transacional), NFR-16 (Senha temporária 24h + força troca)
* **Use Cases relacionados:** UC-13, UC-28
* **Riscos relacionados:** RISK-03 (setUsername stub — BLOQUEANTE), RISK-02 (AuditLog)

---

## US-ATIVO-012 — Gerenciar papéis (Roles) e permissões

* **Priority:** Should
* **Complexity:** 5
* **Épico:** Segurança & RBAC
* **Labels:** [backend, frontend, admin, rbac, security]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** definir roles (`ADMIN`, `GESTOR_PATRIMONIO`, `ANALISTA_MANUTENCAO`, `USUARIO_FINAL`, `AUDITOR`) com permissões granulares (`ativo:create`, `ativo:read`, `manutencao:aprovar`, `healthcheck:write`, `relatorio:custo-total`, etc.)
* **So that** o controle de acesso siga menor privilégio e a matriz de permissões seja auditável

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Setup inicial, muito baixa frequência (UC-14, UC-15). BR-01: RBAC estrito — `isAdmin()` verifica role `ADMIN`; `hasPermission` verifica permissões da role. BR-02, BR-03, BR-07. NFR: cache de permissões por usuário O(1) — `ConcurrentHashMap` recarregado on change. Roles de sistema (`ADMIN`, `USER`) não editáveis/excluíveis (`system=true`).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar role com permissões
* **Given** admin em "Configurações > Papéis" clica "Novo"
* **When** preenche nome "GESTOR_PATRIMONIO", descrição, seleciona permissões `[ativo:create, ativo:read, ativo:update, manutencao:aprovar, relatorio:custo-total]`, salva
* **Then** valida ADMIN, unicidade nome, persiste role + associações role-permissão, audita, retorna `201`
* **And** disponível na matriz de permissões (UI)

#### Scenario 2: Editar permissões de role
* **Given** admin edita "ANALISTA_MANUTENCAO", adiciona `healthcheck:write`
* **When** envia `PUT`
* **Then** valida, atualiza associações, audita, retorna `200`
* **And** cache de permissões invalidado para usuários com essa role

#### Scenario 3: Tentar excluir role de sistema
* **Given** admin tenta `DELETE /api/roles/ADMIN`
* **When** valida `system=true`
* **Then** retorna `403 Forbidden` — "Role de sistema não pode ser excluída"

#### Scenario 4: Excluir role atribuída a usuários
* **Given** role "AUDITOR" atribuída a 3 usuários
* **When** admin tenta excluir
* **Then** retorna `409 Conflict` — "Remova a role dos usuários primeiro"

#### Scenario 5: Gerenciar permissões (recurso:ação)
* **Given** admin em "Configurações > Permissões" cria `relatorio:exportar`
* **When** valida unicidade `recurso:acao`
* **Then** persiste, audita, disponível para associação em roles

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — matriz roles x permissões com checkboxes, badge "Sistema" nas roles fixas]
* **Estados:** loading, empty, error, success
* **Cache:** invalidação automática on role/permission change

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `RoleService`, `PermissionService`, `RoleRepository`, `PermissionRepository`, mappers, frontend: `RoleMatrix`, `RoleForm`, `PermissionGrid`
* **Dependências técnicas:** `POST/PUT/DELETE /api/roles`, `POST/PUT/DELETE /api/permissoes`, tabelas `role`, `permissao`, `role_permissao`, `usuario_role`, `audit_log`
* **Considerações de performance/segurança:** `hasPermission` lookup O(1) — cache em memória recarregado via event listener on change; permissões estáticas (código) — não dinâmicas por instância

### 6. Out of Scope
* Matriz completa de permissões por role — workshop com stakeholders (Open Issue UC-14)
* Permissões dinâmicas por instância (ex: ativo específico)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Lista canônica de permissões definida (workshop)

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: CRUD roles/permissões, 403 role sistema, 409 role em uso, cache invalidation
- [ ] `hasPermission` O(1) validado com 1000 usuários
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-17 (Cache permissões O(1))
* **Use Cases relacionados:** UC-14, UC-15
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-08 (Matriz permissões indefinida)

---

## US-ATIVO-013 — Gerenciar usuários (provisionamento, reset senha, ativação)

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Segurança & RBAC
* **Labels:** [backend, frontend, admin, security, bug-blocker]

### 1. Description (INVEST)
* **As a** Administrador de Sistema
* **I want to** criar, editar, resetar senha, ativar/desativar usuários e atribuir roles
* **So that** o provisionamento de acesso seja controlado, auditável e seguro com invalidação de tokens

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Provisionamento (UC-16). BR-01 (ADMIN), BR-02 (auditoria roles anterior/novo), BR-03, BR-07, BR-08 (sessão & token). **BUG BLOQUEANTE:** `Usuario.setUsername` stub (linha 86) — mesmo impacto US-ATIVO-011. Estratégia de invalidação de token: blacklist Redis vs token versioning no usuário (Open Issue UC-16). Refresh token rotation implementado.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Criar usuário com roles
* **Given** admin em "Configurações > Usuários" clica "Novo", preenche username, email, senha temporária, roles, funcionário vinculado (opcional), status ATIVO
* **When** envia `POST /api/usuarios`
* **Then** valida ADMIN, unicidade username/email, hash BCrypt, persiste, audita (inclui roles), retorna `201` com DTO (sem senha)

#### Scenario 2: Reset de senha por admin
* **Given** admin clica "Resetar senha" em usuário
* **When** envia `POST /api/usuarios/{id}/reset-senha`
* **Then** gera senha temporária, envia email, força troca no primeiro login, audita `acao=atualizar` com `campo=senha`, retorna `200`

#### Scenario 3: Desativar usuário (soft delete)
* **Given** admin desativa usuário (`PATCH /api/usuarios/{id}` com `{ ativo: false }`)
* **When** valida
* **Then** invalida tokens ativos (BR-08: `clearSession`/`logout` propaga via blacklist ou versioning), audita, retorna `200`
* **And** usuário não consegue mais logar; tokens existentes rejeitados

#### Scenario 4: `setUsername` stub
* **Given** bug não corrigido
* **When** admin atualiza username
* **Then** falha silenciosamente — **BLOQUEANTE**

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — grid usuários com badge status, ações: editar, reset senha, ativar/desativar, roles]
* **Estados:** loading, empty, error, success
* **Reset senha:** modal confirmação → toast "Senha temporária enviada por email"

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `UsuarioService`, `UsuarioRepository`, `UsuarioMapper`, `AuthService` (invalidação tokens), frontend: `UsuarioGrid`, `UsuarioForm`, `RolesSelector`
* **Dependências técnicas:** `POST/PUT/PATCH/DELETE /api/usuarios`, `POST /api/usuarios/{id}/reset-senha`, tabelas `usuario`, `usuario_role`, `refresh_token`, `audit_log`
* **Considerações de performance/segurança:** Invalidação tokens no logout/desativação — definir estratégia (Open Issue UC-16); MFA TOTP roadmap futuro; SSO/SAML/OIDC fase 2

### 6. Out of Scope
* MFA, SSO (futuro)
* `mockLogin` (apenas testes — BR-08)

### 7. Definition of Ready
- [ ] **BUG BLOQUEANTE CORRIGIDO:** `Usuario.setUsername`
- [ ] Critérios validados
- [ ] Estratégia invalidação tokens definida

### 8. Definition of Done
- [ ] Código + revisão
- [ ] **Teste regressão:** `setUsername` funcional
- [ ] Testes: CRUD, reset senha (email mock), desativação invalida tokens, auditoria roles
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-18 (Invalidação tokens), NFR-19 (Refresh rotation)
* **Use Cases relacionados:** UC-16
* **Riscos relacionados:** RISK-03 (setUsername stub), RISK-02 (AuditLog), RISK-09 (Estratégia tokens)

---

## US-ATIVO-014 — Autenticação: Login, Refresh Token, Logout

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Segurança & RBAC
* **Labels:** [backend, frontend, security, jwt, ux]

### 1. Description (INVEST)
* **As a** Qualquer usuário (Admin, Gestor, Analista, Funcionário, Auditor)
* **I want to** fazer login com email/senha, ter refresh token automático silencioso e logout seguro com invalidação de tokens
* **So that** eu acesse o sistema de forma segura, sem interrupções por expiração de access token, e com garantia de revogação no logout

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Altíssima frequência (login 1x/dia, refresh a cada 8h — UC-17). BR-08: `authInterceptor` injeta token; `clearSession`/`logout` invalidam; `mockLogin` só testes. NFR: login P95 ≤ 800ms (BCrypt + JWT); refresh rotation sem race condition (fila no interceptor); JWT RS256, chaves rotacionadas 90 dias. **Diagnóstico:** `console.error` (api.js:36) e `console.debug` (api.js:99) — remover em produção. `request` function complexidade 13 — quebrar.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Login bem-sucedido
* **Given** usuário informa email/username + senha correta em `/login`
* **When** frontend envia `POST /api/auth/login` (sem interceptor — público)
* **Then** backend valida credenciais, usuário ativo, gera `accessToken` (JWT 8h) + `refreshToken` (opaco 30d, hash no banco)
* **And** audita `acao=login` com `usuario_id`, `timestamp`, `ip`, `userAgent`
* **And** retorna `200 OK` com `{ accessToken, refreshToken, usuario: { id, nome, roles, permissoes } }`
* **And** frontend armazena tokens (memory + httpOnly cookie para refresh), `authInterceptor` injeta `Authorization: Bearer <accessToken>`
* **And** redireciona para dashboard

#### Scenario 2: Credenciais inválidas
* **Given** usuário informa senha errada
* **When** envia login
* **Then** retorna `401 Unauthorized` — `handleApiError` exibe "Credenciais inválidas"

#### Scenario 3: Conta inativa
* **Given** usuário existe mas `ativo=false`
* **When** tenta login
* **Then** retorna `403 Forbidden` — "Conta desativada. Contate o administrador."

#### Scenario 4: Refresh token automático (silencioso)
* **Given** `authInterceptor` recebe `401` com código `TOKEN_EXPIRED` em requisição autenticada
* **When** interceptor chama `POST /api/auth/refresh` com refresh token (fila requisições pendentes)
* **Then** backend valida refresh token (não revogado, não expirado, hash confere), gera novo access + novo refresh (rotation)
* **And** atualiza storage, repete requisição original com novo token
* **And** sucesso transparente para usuário (sem piscar tela)

#### Scenario 5: Refresh token revogado/expirado/reutilizado
* **Given** refresh token inválido (rotação detectada)
* **When** interceptor tenta refresh
* **Then** retorna `401` com `code: REFRESH_TOKEN_INVALID`
* **And** `authInterceptor` limpa sessão e redireciona para login forçado

#### Scenario 6: Logout
* **Given** usuário clica "Sair" ou fecha aba (`beforeunload`)
* **When** frontend chama `POST /api/auth/logout` com refresh token
* **Then** backend invalida refresh token (marca revogado / incrementa `tokenVersion`), audita `acao=logout`
* **And** `clearSession` limpa storage local
* **And** retorna `204`, frontend redireciona para `/login`

#### Scenario 7: `mockLogin` (apenas testes)
* **Given** testes automatizados/Storybook
* **When** `POST /api/auth/mock-login` com `{ role: "ADMIN" }`
* **Then** retorna token fake com claims da role
* **And** desabilitado em produção (profile `test`)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tela login com "Lembrar-me", loading state, erro inline]
* **Estados:** loading (submit), error (credenciais/conta inativa), success (redirect)
* **Refresh silencioso:** sem indicador visual; apenas se falhar → redirect login
* **Logout:** confirmação opcional; `beforeunload` envia logout via `navigator.sendBeacon`

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AuthService`, `JwtProvider`, `RefreshTokenRepository`, `authInterceptor` (frontend/src/services/api.js:46 — complexidade 13, quebrar), `handleApiError`, `handleResponse`, `clearSession`, `logout`, `mockLogin`
* **Dependências técnicas:** `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`, `POST /api/auth/mock-login`, tabelas `usuario`, `refresh_token`, chaves RSA (keystore/JWKS)
* **Considerações de performance/segurança:** Stateless JWT access token; stateful refresh token revogável; fila de requisições no interceptor durante refresh (evita race condition); remover `console.error` (linha 36) e `console.debug` (linha 99) antes de produção

### 6. Out of Scope
* MFA TOTP (roadmap)
* SSO/SAML/OIDC (fase 2)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] `request` function refatorada (complexidade 13 → < 10)
- [ ] Chaves RSA configuradas em staging/prod
- [ ] Estratégia invalidação access token no logout definida

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: login sucesso/erro, refresh automático (simular expiração), logout, mockLogin (test profile)
- [ ] Teste de carga: 100 logins simultâneos P95 ≤ 800ms
- [ ] Race condition refresh: 10 requests paralelos com token expirado → 1 refresh, 9 reutilizam
- [ ] `console.*` removidos em build de produção
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-20 (Login P95 ≤ 800ms), NFR-21 (Refresh rotation sem race), NFR-22 (JWT RS256, rotação chaves 90d)
* **Use Cases relacionados:** UC-17
* **Riscos relacionados:** RISK-10 (request complexidade 13), RISK-11 (console.* produção), RISK-09 (Estratégia tokens)

---

## US-MAN-001 — Solicitar manutenção corretiva ou preventiva

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Manutenção
* **Labels:** [backend, frontend, workflow, notifications, rbac]

### 1. Description (INVEST)
* **As a** Usuário Final (Funcionário) ou Analista de Manutenção
* **I want to** abrir chamado de manutenção informando ativo, tipo (PREVENTIVA/CORRETIVA), prioridade, descrição, anexos, custo estimado e fornecedor preferencial
* **So that** o problema seja registrado, roteado para aprovação conforme alçada, e a equipe de manutenção possa atuar com rastreabilidade completa

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Alta frequência (dezenas/dia/filial — UC-18). BR-02 (auditoria `iniciar`), BR-03 (validação), BR-09 (`ManutencaoSpecification` complexidade 14 — refatorar). BR-01: assumir USER pode `iniciar` (BRD não especifica). Workflow: `SOLICITADA` → `PENDENTE_APROVACAO` → `APROVADA` → `EM_ANDAMENTO` → `CONCLUIDA`/`CANCELADA`. Alçadas por valor não definidas — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] (ex: > R$ 5.000 → Gerente Regional; > R$ 20.000 → Diretor). Notificação assíncrona para aprovadores.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Solicitação corretiva por usuário final
* **Given** usuário autenticado (role `USER`) acessa ativo > "Solicitar Manutenção", preenche tipo CORRETIVA, prioridade ALTA, descrição "Tela trincada", anexa foto, custo estimado R$ 800, salva
* **When** frontend envia `POST /api/manutencoes`
* **Then** backend valida: ativo existe, status `ATIVO`/`EM_MANUTENCAO`, usuário tem acesso ao ativo (filial/departamento? — Open Issue UC-18), dados válidos
* **And** cria `Manutencao` com `status=SOLICITADA`, `solicitanteId=usuarioLogado`, `dataSolicitacao=now`
* **And** audita `acao=iniciar` (BR-02)
* **And** dispara notificação assíncrona para aprovadores (gestores da filial do ativo)
* **And** retorna `201 Created` com DTO
* **And** frontend exibe "Solicitação enviada para aprovação" + número do chamado

#### Scenario 2: Preventiva agendada por analista (fluxo paralelo)
* **Given** analista em "Planejamento > Nova Preventiva" preenche tipo PREVENTIVA, `dataAgendada`, status `PLANEJADA`, pode pular aprovação se dentro de plano aprovado
* **When** envia `POST /api/manutencoes`
* **Then** cria com `status=PLANEJADA`, auditoria, notificação para equipe

#### Scenario 3: Roteamento por alçada de valor
* **Given** solicitação com custo estimado R$ 25.000
* **When** notificação assíncrona é disparada
* **Then** roteia para Diretor (alçada > R$ 20.000) — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

#### Scenario 4: Ativo em manutenção ativa
* **Given** ativo já tem `Manutencao` com `status=EM_ANDAMENTO`
* **When** usuário tenta nova solicitação
* **Then** retorna `409 Conflict` — "Ativo já possui manutenção em andamento. Aguarde conclusão ou cancele a anterior."

#### Scenario 5: Ativo baixado
* **Given** ativo `status=BAIXADO`
* **When** tenta solicitar
* **Then** retorna `400 Bad Request` — "Não é possível solicitar manutenção para ativo baixado."

#### Scenario 6: Falha ao notificar aprovadores
* **Given** manutenção criada mas email/SMTP falha
* **When** notificação assíncrona falha
* **Then** log erro; manutenção criada normalmente; job de retry; não bloquear criação

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — formulário "Nova Manutenção" com stepper: Ativo → Detalhes → Anexos → Confirmar]
* **Estados:** loading, validation error (inline), submitting, success (toast + número chamado), empty (sem ativos disponíveis)
* **Anexos:** upload para object storage (S3/MinIO) — referência salva no banco; preview de imagens

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoService.iniciar`, `ManutencaoRepository`, `ManutencaoSpecification` (refatorar complexidade 14), `AuditInterceptor`, `NotificationService` (assíncrono), frontend: `ManutencaoForm` (react-hook-form + Zod), `AnexoUpload`
* **Dependências técnicas:** `POST /api/manutencoes`, tabelas `manutencao`, `anexo`, `audit_log`, `usuario` (aprovadores por filial)
* **Considerações de performance/segurança:** Latência P95 ≤ 600ms (inclui notificação assíncrona); disponibilidade 99.9%; validação de acesso ao ativo por filial/departamento (Open Issue UC-18)

### 6. Out of Scope
* Matriz formal de alçadas (workshop Gestão/Financeiro — Open Issue UC-18)
* Solicitação anônima via QR code (futuro)
* Integração WhatsApp/Teams (futuro)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Matriz de alçadas aprovada (ou valores default documentados)
- [ ] `ManutencaoSpecification` refatorada (tarefa técnica prévia)
- [ ] Design do formulário stepper aprovado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso USER/ADMIN, 409 ativo em manutenção, 400 ativo baixado, validação campos, notificação assíncrona (mock)
- [ ] Auditoria `iniciar` verificada
- [ ] Performance P95 ≤ 600ms
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-23 (Latência P95 ≤ 600ms), NFR-24 (Disponibilidade 99.9%)
* **Use Cases relacionados:** UC-18
* **Riscos relacionados:** RISK-04 (ManutencaoSpecification complexidade 14), RISK-02 (AuditLog), RISK-12 (Alçadas indefinidas)

---

## US-MAN-002 — Aprovar ou rejeitar manutenção

* **Priority:** Must
* **Complexity:** 5
*   **Épico:** Gestão de Manutenção
* **Labels:** [backend, frontend, workflow, rbac, notifications]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio / Aprovador designado (por alçada)
* **I want to** revisar solicitações pendentes, aprovar com observação opcional ou rejeitar com motivo obrigatório
* **So that** o controle orçamentário seja exercido, a trilha de aprovação fique auditada e a equipe de execução seja notificada

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Volume proporcional a solicitações (UC-19). BR-02 (auditoria `aprovar`/`cancelar`), BR-09 (Specification para listar pendentes por alçada), BR-01 (perm `manutencao:aprovar`). Alçadas configuráveis por filial/valor/tipo (tabela `AlcadaAprovacao` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]). NFR: P95 ≤ 400ms, auditoria imutável.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Aprovar manutenção
* **Given** aprovador com permissão `manutencao:aprovar` e alçada compatível acessa "Minhas Aprovações" (filtro `status=PENDENTE_APROVACAO` + alçada)
* **When** clica "Aprovar", preenche observação opcional, confirma
* **Then** frontend envia `PATCH /api/manutencoes/{id}/aprovar` com `{ observacao }`
* **And** backend valida permissão + alçada, transição `SOLICITADA`/`PENDENTE_APROVACAO` → `APROVADA`
* **And** atualiza `aprovadorId`, `dataAprovacao`, `observacaoAprovacao`
* **And** audita `acao=aprovar` (BR-02)
* **And** notifica solicitante + analistas da filial
* **And** retorna `200 OK`, frontend remove da lista de pendências

#### Scenario 2: Rejeitar manutenção
* **Given** aprovador clica "Rejeitar"
* **When** modal exige motivo obrigatório (select + texto: "Orçamento não aprovado", "Duplicata", "Outro")
* **Then** status → `REJEITADA`, `rejeitadoPor`, `dataRejeicao`, `motivoRejeicao`
* **And** audita `acao=cancelar` (ou `rejeitar`)
* **And** notifica solicitante

#### Scenario 3: Aprovação em lote
* **Given** aprovador seleciona múltiplas manutenções na lista
* **When** clica "Aprovar selecionadas", preenche observação comum, confirma
* **Then** `POST /api/manutencoes/aprovar-lote` com `{ ids: [], observacao }`
* **And** transação única, auditoria individual por ID
* **And** retorna `200` com resumo `{ aprovadas: 5, falhas: 0 }`

#### Scenario 4: Manutenção já não está pendente
* **Given** aprovador tenta aprovar manutenção que outro aprovador já aprovou
* **When** envia requisição
* **Then** retorna `409 Conflict` — "Manutenção não está mais pendente. Status atual: {status}"

#### Scenario 5: Aprovador sem alçada
* **Given** aprovador tenta aprovar valor acima de sua alçada
* **When** backend valida alçada
* **Then** retorna `403 Forbidden` — "Valor excede sua alçada. Encaminhe para {proximoNivel}."

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — lista "Minhas Aprovações" com cards expansíveis, ações inline, badge de alçada]
* **Estados:** loading, empty ("Nenhuma aprovação pendente"), error, success
* **Acessibilidade:** navegação por teclado entre cards, anúncio de status

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoService.aprovar`/`rejeitar`, `ManutencaoRepository`, `AuditInterceptor`, `NotificationService`, `AlcadaAprovacaoService` (a criar), frontend: `AprovacoesList`, `AprovarModal`, `RejeitarModal`
* **Dependências técnicas:** `PATCH /api/manutencoes/{id}/aprovar`, `PATCH /api/manutencoes/{id}/rejeitar`, `POST /api/manutencoes/aprovar-lote`, tabela `alcada_aprovacao` (filial, valor_min, valor_max, role_aprovador)
* **Considerações de performance/segurança:** Auditoria imutável — não permitir alteração de `aprovadorId`/`dataAprovacao` após setado; notificação assíncrona não bloqueia resposta

### 6. Out of Scope
* Aprovação delegada (férias/ausência) — Open Issue UC-19

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Tabela `alcada_aprovacao` modelada e aprovada
- [ ] Workshop alçadas realizado

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: aprovar, rejeitar, lote, 409 estado, 403 alçada, concorrência (version)
- [ ] Auditoria verificada
- [ ] Performance P95 ≤ 400ms
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-25 (Latência P95 ≤ 400ms), NFR-26 (Auditoria imutável aprovação)
* **Use Cases relacionados:** UC-19
* **Riscos relacionados:** RISK-04 (Specification), RISK-02 (AuditLog), RISK-12 (Alçadas)

---

## US-MAN-003 — Cancelar manutenção

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Gestão de Manutenção
* **Labels:** [backend, frontend, workflow, audit]

### 1. Description (INVEST)
* **As a** Solicitante, Aprovador, Analista de Manutenção ou Admin
* **I want to** cancelar uma manutenção informando motivo obrigatório
* **So that** o cancelamento seja rastreado, custos incorridos sejam registrados (se em andamento) e partes interessadas notificadas

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Baixa a média frequência (UC-20). BR-02 (auditoria `cancelar`), BR-03 (motivo obrigatório), BR-09. Status canceláveis: `SOLICITADA`, `PENDENTE_APROVACAO`, `APROVADA`, `EM_ANDAMENTO` (com regras). Confirmação em duas etapas no frontend.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Cancelar manutenção pendente
* **Given** solicitante abre manutenção `SOLICITADA`, clica "Cancelar", seleciona motivo "Problema resolvido espontaneamente", confirma
* **When** envia `PATCH /api/manutencoes/{id}/cancelar` com `{ motivo, observacao }`
* **Then** valida permissão + transição válida, status `CANCELADA`, preenche `canceladoPor`, `dataCancelamento`, `motivoCancelamento`
* **And** audita `acao=cancelar` (BR-02)
* **And** notifica solicitante, aprovador, analista, fornecedor (se vinculado)
* **And** retorna `200 OK`

#### Scenario 2: Cancelar com custos incorridos (EM_ANDAMENTO)
* **Given** analista cancela manutenção `EM_ANDAMENTO` com peças/horas já lançadas
* **When** modal exibe resumo de custos incorridos, usuário confirma ciência
* **Then** status `CANCELADA_COM_CUSTO` (sub-status) ou `CANCELADA` com `custoIncurred > 0`
* **And** auditoria registra custos

#### Scenario 3: Tentar cancelar manutenção concluída
* **Given** manutenção `CONCLUIDA`
* **When** usuário tenta cancelar
* **Then** retorna `409 Conflict` — "Manutenção já concluída. Não é possível cancelar."

#### Scenario 4: Motivo não informado
* **Given** usuário submete cancelamento sem motivo
* **When** valida
* **Then** retorna `400 Bad Request` — "Motivo do cancelamento é obrigatório."

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — modal cancelar com select motivo + textarea observação, resumo de custos se houver]
* **Estados:** loading, validation error, confirmation (2 etapas), success
* **Irreversível:** aviso visual "Esta ação não pode ser desfeita"

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoService.cancelar`, `ManutencaoRepository`, `AuditInterceptor`, `NotificationService`, frontend: `CancelarManutencaoModal`
* **Dependências técnicas:** `PATCH /api/manutencoes/{id}/cancelar`, tabela `manutencao` (campos cancelamento), `audit_log`
* **Considerações de performance/segurança:** Confirmação 2 etapas no frontend; notificação fornecedor via email template se `fornecedorId` preenchido e status `APROVADA`/`EM_ANDAMENTO`

### 6. Out of Scope
* Reabertura de manutenção cancelada (Open Issue UC-20 — criar nova referenciando a cancelada)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Lista de motivos de cancelamento aprovada

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: cancelar pendente, cancelar com custos, 409 concluída, 400 sem motivo
- [ ] Auditoria verificada
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-27 (Cancelamento irreversível — 2 etapas)
* **Use Cases relacionados:** UC-20
* **Riscos relacionados:** RISK-02 (AuditLog)

---

## US-MAN-004 — Concluir manutenção com custos e relatório técnico

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Manutenção
* **Labels:** [backend, frontend, workflow, finance, audit, outbox]

### 1. Description (INVEST)
* **As a** Analista de Manutenção
* **I want to** registrar a conclusão da manutenção com relatório técnico, peças utilizadas, horas trabalhadas, custos (peças, mão de obra, terceiros) e anexos
* **So that** o custo total do ativo seja atualizado (`custoTotalPorAtivo` — BR-06), a próxima preventiva seja agendada automaticamente e a trilha de auditoria fique completa

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Volume = manutenções aprovadas (UC-21). BR-02 (auditoria `concluir` + `atualizar` ativo), BR-06 (`custoTotalPorAtivo` = soma custos manutenção por ativo), BR-03, BR-09. NFR: transação distribuída (manutenção + ativo + custo agregado) — outbox pattern para eventual consistency do custo total. P95 ≤ 800ms. `proximaManutencao` baseada em `TipoAtivo.intervaloManutencaoPreventivaDias` (a confirmar). Portal do Fornecedor — futuro.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Conclusão padrão com custos
* **Given** analista abre manutenção `APROVADA`/`EM_ANDAMENTO`, clica "Concluir", preenche data conclusão, relatório técnico obrigatório, grid de peças (item, qtd, valor unitário, fornecedor), horas trabalhadas (interno/terceiro), custo mão de obra, custo terceiros, anexa nota fiscal
* **When** envia `PATCH /api/manutencoes/{id}/concluir`
* **Then** backend valida permissão `manutencao:concluir` + estado, calcula `custoTotal = sum(pecas) + maoDeObra + terceiros`
* **And** atualiza manutenção: `status=CONCLUIDA`, todos os campos
* **And** atualiza ativo: `ultimaManutencao=dataConclusao`, `proximaManutencao` recalculada (se preventiva: + intervalo padrão do tipo)
* **And** mesma transação: `ManutencaoRepository.save` + `AtivoRepository.save`
* **And** auditoria: `acao=concluir` na manutenção + `acao=atualizar` no ativo (BR-02)
* **And** evento `CustoTotalPorAtivoService.recalcular(ativoId)` enfileirado (outbox pattern) — atualiza view materializada
* **And** notificações assíncronas
* **And** retorna `200 OK` com DTO, frontend exibe "Manutenção concluída. Custo total: R$ X.XXX"

#### Scenario 2: Conclusão custo zero (garantia/interno sem rateio)
* **Given** analista conclui com `custoTotal = 0`
* **When** relatório explica "Cobertura de garantia" ou "Mão de obra interna sem rateio"
* **Then** aceito, `custoTotal = 0` persistido, `custoTotalPorAtivo` não alterado

#### Scenario 3: Preventiva gera próxima preventiva automática
* **Given** manutenção `PREVENTIVA` concluída
* **When** job/trigger pós-conclusão
* **Then** cria nova `Manutencao` `status=PLANEJADA`, `tipo=PREVENTIVA`, `dataAgendada=proximaManutencao`, `ativoId` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

#### Scenario 4: Campos obrigatórios ausentes
* **Given** relatório técnico vazio ou peças com custo mas sem itens
* **When** valida
* **Then** `400 Bad Request` com validação por campo

#### Scenario 5: Falha ao atualizar `custoTotalPorAtivo` (view materializada)
* **Given** evento de recálculo falha
* **When** outbox pattern
* **Then** transação principal (manutenção + ativo) commitada; evento enfileirado; job de reconciliação diária processa

#### Scenario 6: Ativo baixado durante execução (race condition)
* **Given** ativo foi baixado enquanto manutenção em andamento
* **When** analista tenta concluir
* **Then** `409 Conflict` — "Ativo foi baixado durante a execução. Contate o gestor."

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — formulário conclusão com seções: Relatório, Peças (grid editável), Mão de Obra, Anexos, Resumo de Custos]
* **Estados:** loading, validation error (inline por seção), saving, success (toast com custo total)
* **Peças/serviços:** catálogo selecionável + cadastro on-the-fly

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoService.concluir`, `AtivoService.atualizarProximaManutencao`, `CustoTotalPorAtivoService` (outbox), `ManutencaoRepository`, `AtivoRepository`, `AuditInterceptor`, frontend: `ConcluirManutencaoForm` (wizard steps), `PecasGrid`, `AnexoUpload`
* **Dependências técnicas:** `PATCH /api/manutencoes/{id}/concluir`, tabelas `manutencao`, `manutencao_peca`, `ativo`, `mv_custo_total_ativo` (view materializada), `outbox_event`, `audit_log`
* **Considerações de performance/segurança:** Outbox pattern para eventual consistency do custo total; `@Transactional` no service principal; depreciação linear padrão para `proximaManutencao` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

### 6. Out of Scope
* Catálogo de peças/serviços estruturado (livre por enquanto)
* Integração Financeiro para rateio mão de obra interna (futuro)
* Portal Fornecedor (futuro)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Outbox pattern modelado e aprovado
- [ ] Intervalo preventiva por tipo de ativo definido

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: conclusão padrão, custo zero, 400 validação, 409 ativo baixado, outbox event gerado, reconciliação job
- [ ] Auditoria dupla verificada (manutenção + ativo)
- [ ] Performance P95 ≤ 800ms
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-28 (Transação distribuída outbox), NFR-29 (P95 ≤ 800ms)
* **Use Cases relacionados:** UC-21
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-13 (Outbox pattern), RISK-14 (Depreciação linear)

---

## US-MAN-005 — Filtrar e listar manutenções com especificação avançada

* **Priority:** Must
* **Complexity:** 8
* **Épico:** Gestão de Manutenção
* **Labels:** [backend, frontend, performance, specification, tech-debt]

### 1. Description (INVEST)
* **As a** Analista de Manutenção, Gestor de Patrimônio ou Auditor
* **I want to** filtrar manutenções por múltiplos critérios compostos (status, filial, tipo, prioridade, período, solicitante, aprovador) com paginação e ordenação
* **So that** eu possa gerenciar minha fila de trabalho, acompanhar KPIs e realizar auditorias amostrais com performance adequada

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Tela principal do Analista/Gestor (alta frequência — UC-22). BR-01 (leitura ADMIN/USER com escopo?), BR-09 (`ManutencaoSpecification.build` complexidade 14 — **refatoração obrigatória**), BR-07. NFR: P95 ≤ 500ms para filtros complexos. Specification testada com 10+ filtros. Filtro implícito por filial/departamento do usuário (se não ADMIN) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]. Exportação filtrada CSV/Excel. Filtros favoritos por usuário.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Filtros compostos com paginação
* **Given** analista em "Manutenções" aplica filtros: `status=APROVADA`, `filialId=3`, `tipo=PREVENTIVA`, `prioridade=ALTA`, `dataInicio=2025-01-01`, `dataFim=2025-01-31`
* **When** frontend envia `GET /api/manutencoes?status=APROVADA&filialId=3&tipo=PREVENTIVA&prioridade=ALTA&dataInicio=2025-01-01&dataFim=2025-01-31&page=0&size=20`
* **Then** `ManutencaoSpecification.build` monta `Specification` dinâmica, executa query otimizada
* **And** retorna paginado `200 OK` com DTOs leves (id, ativo, tipo, status, prioridade, datas, solicitante, aprovador, custoEstimado)
* **And** frontend renderiza tabela com ações contextuais (aprovar, iniciar, concluir, cancelar)
* **And** latência P95 ≤ 500ms

#### Scenario 2: Salvar filtro favorito
* **Given** analista configura filtros frequentes
* **When** clica "Salvar filtro", nomeia "Minhas preventivas da filial 3"
* **Then** `POST /api/manutencoes/filtros/salvos` persiste preferência
* **And** carrega via `GET /api/manutencoes/filtros/salvos` no próximo acesso

#### Scenario 3: Exportação filtrada
* **Given** gestor aplica filtros e clica "Exportar Excel"
* **When** `GET /api/manutencoes/export?filtros...&format=xlsx`
* **Then** streaming XLSX com todos os registros filtrados (sem paginação)

#### Scenario 4: Combinação inválida de filtros
* **Given** `dataFim < dataInicio`
* **When** valida no service antes de montar specification
* **Then** `400 Bad Request` — "Data fim deve ser posterior à data início"

#### Scenario 5: Query ineficiente (full table scan)
* **Given** combinação de filtros sem índices adequados
* **When** `EXPLAIN ANALYZE` em CI detecta scan
* **Then** pipeline falha; índices compostos `(filial_id, status, tipo, data_solicitacao)` criados

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — barra de filtros colapsável, chips ativos, tabela com ações por linha, paginação, export]
* **Estados:** loading (skeleton), empty, error, success
* **Filtros favoritos:** dropdown no header da barra de filtros

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoService.listar`, `ManutencaoSpecification.build` (refatorar: extrair `PredicateBuilder` por filtro — complexidade 14), `ManutencaoRepository`, `ManutencaoMapper`, frontend: `ManutencaoFilters`, `ManutencaoTable`, `FiltrosSalvos`
* **Dependências técnicas:** `GET /api/manutencoes`, `GET /api/manutencoes/export`, `GET/POST /api/manutencoes/filtros/salvos`, índices compostos, `audit_log`
* **Considerações de performance/segurança:** Specification usa JPA Criteria API ou Querydsl (diagnóstico: SQL cru assumido); filtro implícito por escopo organizacional (Open Issue UC-22); monitoramento slow queries em CI

### 6. Out of Scope
* Filtro implícito por escopo — decisão pendente
* Specification refatoração — tarefa técnica prévia

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] **Refatoração `ManutencaoSpecification` concluída** (complexidade 14 → < 10)
- [ ] Índices compostos criados e validados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: cada filtro isolado, combinações (10+), paginação, ordenação, export, favoritos
- [ ] Performance: 10k manutenções, filtros complexos P95 ≤ 500ms
- [ ] CI: `EXPLAIN ANALYZE` passa
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-30 (P95 ≤ 500ms filtros complexos)
* **Use Cases relacionados:** UC-22
* **Riscos relacionados:** RISK-04 (Specification complexidade 14 — CRÍTICO), RISK-02 (AuditLog), RISK-15 (Escopo visibilidade)

---

## US-MON-001 — Registrar health check de hardware (sistema/agendador)

* **Priority:** Must
* **Complexity:** 13
* **Épico:** Monitoramento de Hardware
* **Labels:** [backend, system, idempotency, cascade, alerts, tech-debt]

### 1. Description (INVEST)
* **As a** Agendador Externo (cron, Spring @Scheduled, Airflow) ou Agente de Coleta no ativo
* **I want to** enviar health check completo (adaptadores de rede, discos, memórias) de forma idempotente a cada 15 min
* **So that** o sistema mantenha dados frescos de saúde do hardware, limpe dados anteriores em cascata, e avalie thresholds para geração automática de alertas

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Muito alta frequência, contínua 24/7 (UC-08). BR-05 (thresholds: disco 85%, memória 90%, rede 100ms), BR-10 (cascata `deleteByAtivoDetalheHardwareId` antes de novo registro), BR-02 (auditoria opcional — volume alto). NFR: Idempotência (mesmo payload → mesmo resultado via delete+insert); Throughput 10.000 ativos × 4 checks/hora = ~11 req/s; P95 ≤ 1s (inclui alertas); Disponibilidade 99.95%. **CRÍTICO:** `AlertNotificationService.checkResourceUsageAlerts` complexidade 17 — refatorar antes de Q2.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Health check completo bem-sucedido
* **Given** agendador envia `POST /api/ativos/{id}/health-check` com payload completo (timestamp, adaptadoresRede[], discos[], memorias[])
* **When** backend valida: ativo existe, categoria HARDWARE, exigeHealthCheck=true, token de serviço válido (role `HEALTH_COLLECTOR` — a definir)
* **Then** `deleteByAtivoDetalheHardwareId(ativoId)` remove health check anterior (adaptadores, discos, memórias em cascata) (BR-10)
* **And** `HealthCheckMapper.toEntity` cria novo `AtivoDetalheHardware` com collections
* **And** persiste, retorna `200 OK` com `{ alertasGerados: 2, healthCheckId: 123 }`
* **And** assíncrono: `AlertNotificationService.checkResourceUsageAlerts(ativoId)` avalia thresholds e gera alertas se necessário

#### Scenario 2: Payload incompleto (apenas discos)
* **Given** agente envia apenas `discos` no payload
* **When** backend valida
* **Then** retorna `400 Bad Request` — "Payload deve conter adaptadoresRede, discos e memorias completos" (BR-10 exige limpeza total + re-registro completo)

#### Scenario 3: Ativo não é hardware ou não exige health check
* **Given** ativo tipo MOBILIARIO ou `exigeHealthCheck=false`
* **When** recebe health check
* **Then** retorna `400 Bad Request` — "Health check não aplicável para este tipo de ativo"

#### Scenario 4: Payload inválido / schema mismatch
* **Given** campos obrigatórios ausentes, tipos errados, arrays vazios
* **When** validação
* **Then** `400 Bad Request` com detalhes (BR-03); agente registra falha local e reenvia no próximo ciclo

#### Scenario 5: Falha no AlertNotificationService (complexidade 17)
* **Given** health check persistido mas `checkResourceUsageAlerts` excede tempo ou lança exceção
* **When** erro capturado
* **Then** **não** rola transação do health check (dado bruto salvo)
* **And** erro logado, alerta `ALERTA_PROCESSAMENTO_FALHA` gerado para equipe
* **And** job de reprocessamento posterior reavalia health checks sem alerta

#### Scenario 6: Ausência de health check (staleness)
* **Given** ativo hardware sem health check há > 2h (threshold configurável)
* **When** job `HealthCheckStalenessChecker` roda (fora do fluxo principal)
* **Then** gera alerta `HEALTH_CHECK_AUSENTE` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 4. UI/UX Notes
* **Wireframe/Mockup:** N/A (endpoint de sistema)
* **Observabilidade:** Logs estruturados (JSON), métricas: `health_checks_received`, `health_checks_persisted`, `alerts_generated`, `processing_duration_ms`

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `HealthCheckService`, `AtivoDetalheHardwareRepository`, `HealthCheckMapper`, `AlertNotificationService` (refatorar: `DiskAlertEvaluator`, `MemoryAlertEvaluator`, `NetworkAlertEvaluator`, `AlertDeduplicator`, `AlertPersister`, `NotificationDispatcher`), `AuthInterceptor` (valida token serviço), frontend: N/A
* **Dependências técnicas:** `POST /api/ativos/{id}/health-check`, tabelas `ativo_detalhe_hardware`, `adaptador_rede`, `disco`, `memoria`, `alerta`, `alerta_threshold` (por `tipoAtivoId`), `audit_log` (sampling)
* **Considerações de performance/segurança:** Particionamento `ativo_detalhe_hardware` por data? por ativo? (Open Issue UC-08); Token serviço com escopo `health:write` (RBAC granular); Thresholds configuráveis em tabela

### 6. Out of Scope
* Auditoria de health checks (volume alto — sampling se necessário)
* Definição de role `HEALTH_COLLECTOR` (a definir)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] **Refatoração `AlertNotificationService` concluída** (complexidade 17 → < 10 por classe)
- [ ] Tabela `alerta_threshold` modelada
- [ ] Role `HEALTH_COLLECTOR` definida
- [ ] Estratégia particionamento decidida

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso, 400 incompleto, 400 não hardware, 400 inválido, falha alertas (não rola health check), idempotência (reenvio mesmo payload)
- [ ] Carga: 11 req/s sustentado, P95 ≤ 1s em staging
- [ ] Tipagem, lint, OpenAPI
- [ ] Testado em staging com agente simulado

### 9. Traceability
* **NFRs relacionadas:** NFR-31 (Idempotência), NFR-32 (Throughput 11 req/s), NFR-33 (P95 ≤ 1s), NFR-34 (Disponibilidade 99.95%)
* **Use Cases relacionados:** UC-08
* **Riscos relacionados:** RISK-16 (AlertNotificationService complexidade 17 — CRÍTICO), RISK-17 (Particionamento), RISK-18 (Role HEALTH_COLLECTOR)

---

## US-MON-002 — Consultar histórico de health check de um ativo

* **Priority:** Should
* **Complexity:** 3
* **Épico:** Monitoramento de Hardware
* **Labels:** [backend, frontend, charts, ux]

### 1. Description (INVEST)
* **As a** Analista de Manutenção ou Gestor de Patrimônio
* **I want to** visualizar o histórico temporal de health checks de um ativo (gráficos de disco, memória, latência + tabela de alertas)
* **So that** eu possa identificar tendências, diagnosticar problemas recorrentes e embasar decisões de manutenção preventiva

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Média frequência (consultas sob demanda + relatórios semanais — UC-09). BR-01, BR-04, BR-07. NFR: Query otimizada com índice `(ativo_id, timestamp DESC)`. Retenção 13 meses (configurável) — job de purge mensal. Dados brutos não retornados — apenas métricas agregadas + alertas. Frontend usa Chart.js + Popper.js (dependência confirmada) para gráficos.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Histórico com gráficos
* **Given** usuário abre aba "Health Check" no detalhe do ativo
* **When** frontend envia `GET /api/ativos/{id}/health-history?dataInicio=2025-01-01&dataFim=2025-01-15&page=0&size=50`
* **Then** backend retorna lista paginada ordenada `timestamp DESC` com DTOs leves (timestamp, disco%, memoria%, rede ms, alertas gerados)
* **And** frontend renderiza gráficos de linha (Chart.js) para disco, memória, latência + tabela de alertas
* **And** tooltips com Popper.js mostram valores exatos ao hover

#### Scenario 2: Exportação CSV do histórico
* **Given** usuário clica "Exportar CSV" no histórico
* **When** `GET .../export?format=csv&dataInicio=...`
* **Then** streaming CSV com todos os registros do período

#### Scenario 3: Ativo sem health checks
* **Given** ativo nunca teve health check registrado
* **When** consulta histórico
* **Then** retorna `200 OK` com lista vazia + metadados
* **And** frontend exibe "Nenhum health check registrado" + ilustração

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — aba Health Check com 3 gráficos sincronizados (zoom/pan linked) + tabela alertas]
* **Estados:** loading (skeleton charts), empty, error, success
* **Acessibilidade:** gráficos com descrição textual alternativa, tabela acessível

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `HealthCheckService.getHealthHistory`, `AtivoDetalheHardwareRepository.findHistoryByAtivoId`, `HealthCheckHistoryMapper`, frontend: `HealthHistoryCharts` (Chart.js), `HealthHistoryTable`
* **Dependências técnicas:** `GET /api/ativos/{id}/health-history`, `GET /api/ativos/{id}/health-history/export`, índice `(ativo_id, timestamp DESC)`, retenção 13 meses
* **Considerações de performance/segurança:** View materializada para dashboards agregados (ex: % ativos saudáveis por filial) — Open Issue UC-09; Política de retenção/arquivamento cold storage — Open Issue UC-09

### 6. Out of Scope
* Dados brutos (adaptadores, discos, memórias) no histórico
* Retenção/arquivamento (futuro)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] DTO de histórico definido (métricas agregadas + alertas)
- [ ] Chart.js + Popper.js configurados no frontend

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: sucesso, vazio, export CSV, paginação
- [ ] Gráficos renderizam corretamente com dados reais
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-35 (Query otimizada índice), NFR-36 (Retenção 13 meses)
* **Use Cases relacionados:** UC-09
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-19 (Retenção/arquivamento)

---

## US-MON-003 — Visualizar e gerenciar alertas de recursos

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Monitoramento de Hardware
* **Labels:** [backend, frontend, realtime, notifications, ux]

### 1. Description (INVEST)
* **As a** Analista de Manutenção ou Gestor de Patrimônio
* **I want to** ver alertas não lidos (badge no header + painel lateral), filtrar por severidade/tipo, marcar como lido (individual/lote) e criar manutenção corretiva a partir do alerta
* **So that** eu possa agir rapidamente sobre riscos de hardware e manter a fila de alertas limpa

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Alta frequência (polling 30s + consultas — UC-23, UC-24). BR-01 (leitura com escopo?), BR-05 (alertas gerados por `checkResourceUsageAlerts`), BR-07. NFR: Count não lidos ≤ 100ms (cache Redis ou query otimizada). Polling 30s — avaliar WebSocket futuro. Deduplicação de alertas (mesmo ativo+tipo, janela 1h) — implementar no `AlertNotificationService` (Open Issue UC-23). Filtro implícito por escopo organizacional — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Badge de alertas não lidos (polling 30s)
* **Given** usuário logado, frontend inicia polling `GET /api/alertas/nao-lidos/count` a cada 30s
* **When** backend retorna `{ count: 7 }`
* **Then** badge no header atualiza para "7" com cor baseada na severidade mais alta (vermelho=CRITICA, laranja=ALTA, amarelo=MEDIA)

#### Scenario 2: Abrir painel de alertas
* **Given** usuário clica badge
* **When** frontend envia `GET /api/alertas?status=NAO_LIDO&severidade=CRITICA&page=0&size=20`
* **Then** backend aplica filtro implícito por escopo (filial/departamento do usuário?), retorna paginado
* **And** frontend agrupa por severidade/ativo, exibe ações: "Ver ativo", "Marcar como lido", "Criar manutenção"

#### Scenario 3: Criar manutenção a partir de alerta
* **Given** alerta `DISCO_CRITICO` no ativo "Notebook-001"
* **When** usuário clica "Criar Manutenção"
* **Then** abre formulário US-MAN-001 pré-preenchido: ativo=Notebook-001, tipo=CORRETIVA, prioridade=CRITICA, descrição="Alerta: Disco /dev/sda1 com 92% de uso"

#### Scenario 4: Marcar alerta como lido (individual)
* **Given** usuário clica "Marcar como lido" em alerta
* **When** `PATCH /api/alertas/{id}/ler`
* **Then** `UPDATE alerta SET lido=true, data_leitura=now, lido_por=? WHERE id=?`
* **And** badge count decrementa (idempotente — se já lido, `200 OK` sem erro)

#### Scenario 5: Marcar todos como lidos (lote)
* **Given** usuário clica "Marcar todos como lidos" com filtros atuais
* **When** `POST /api/alertas/ler-todos` com filtros
* **Then** atualização em massa, badge zera, retorna `200`

#### Scenario 6: Volume alto de alertas (storm)
* **Given** 1000+ alertas não lidos
* **When** usuário abre painel
* **Then** paginação obrigatória; agregação por ativo no backend para "resumo de alertas por ativo"; índice `(status, severidade, timestamp DESC)`

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — badge header com contador, painel lateral slide-over com abas: Não lidos / Todos, cards por ativo]
* **Estados:** loading (skeleton cards), empty ("Nenhum alerta"), error, success
* **Ações em lote:** checkbox por card + barra de ações flutuante

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AlertaService.listarAlertas`, `getRecentAlerts`, `marcarComoLido`, `AlertaRepository`, `AlertaMapper`, frontend: `AlertasBadge` (polling), `AlertasPanel`, `AlertaCard`, `CriarManutencaoDeAlerta`
* **Dependências técnicas:** `GET /api/alertas`, `GET /api/alertas/nao-lidos/count`, `PATCH /api/alertas/{id}/ler`, `POST /api/alertas/ler-lote`, `POST /api/alertas/ler-todos`, tabela `alerta` (índices), `audit_log` (sampling)
* **Considerações de performance/segurança:** Cache Redis para count não lidos; WebSocket futuro para tempo real; Deduplicação no `AlertNotificationService` (Open Issue UC-23)

### 6. Out of Scope
* WebSocket tempo real (futuro)
* Deduplicação (implementar no service produtor)
* Auditoria de leitura (volume vs rastreabilidade — Open Issue UC-24)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Filtro implícito por escopo definido
- [ ] Índices de alerta criados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: badge polling, listagem, filtros, criar manutenção a partir de alerta, marcar lido individual/lote/todos, idempotência
- [ ] Performance: count ≤ 100ms, listagem P95 ≤ 500ms
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-37 (Count não lidos ≤ 100ms), NFR-38 (Idempotência marcar lido)
* **Use Cases relacionados:** UC-23, UC-24
* **Riscos relacionados:** RISK-16 (AlertNotificationService), RISK-02 (AuditLog), RISK-15 (Escopo visibilidade), RISK-20 (Deduplicação)

---

## US-MON-004 — Job agendado de verificação de alertas de recursos

* **Priority:** Must
* **Complexity:** 13
* **Épico:** Monitoramento de Hardware
* **Labels:** [backend, system, scheduler, alerts, tech-debt, critical]

### 1. Description (INVEST)
* **As a** Agendador (Spring @Scheduled, cron, Airflow)
* **I want to** executar a verificação periódica de thresholds de recursos (disco, memória, rede) a cada 15 min, gerar/resolver alertas automaticamente e enviar notificações
* **So that** a equipe de manutenção receba alertas oportunos sem intervenção manual e o sistema mantenha visibilidade contínua de risco

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Contínua, a cada 15 min (UC-25). BR-05 (thresholds), BR-02 (auditoria opcional), BR-10 (health checks limpos garantem dados atuais). **CRÍTICO:** `AlertNotificationService.checkResourceUsageAlerts` complexidade 17 — **refatoração obrigatória antes de Q2**. NFR: Taxa erro 5xx < 0.1% (guardrail BRD); Execução completa < 2 min para 10.000 ativos; Idempotência (reexecutável sem duplicar). Refatoração em: `ResourceThresholdEvaluator` (interface), `DiskEvaluator`, `MemoryEvaluator`, `NetworkEvaluator`, `AlertDeduplicator`, `AlertPersister`, `NotificationDispatcher`. Thresholds em tabela `alerta_threshold` (`tipo_ativo_id`, `metrica`, `valor_critico`, `valor_alerta`, `histerese`).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Execução padrão do job
* **Given** scheduler dispara `checkResourceUsageAlerts()` a cada 15 min
* **When** service busca health checks recentes (últimos 30 min) via `AtivoDetalheHardwareRepository.findRecent(30min)`
* **Then** para cada ativo hardware com health check:
  - Avalia discos: `percentualUso > thresholdDisco` (padrão 85%, configurável por tipo)
  - Avalia memórias: `percentualUso > thresholdMemoria` (padrão 90%)
  - Avalia rede: `latenciaMs > thresholdRede` (padrão 100ms)
* **And** para cada violação: verifica deduplicação (alerta `ABERTO` mesmo ativo+tipo na janela); se não existe, cria `Alerta` `status=ABERTO`, `severidade=CRITICA|ALTA`, `tipo=DISCO_CRITICO|MEMORIA_CRITICA|REDE_LENTA`
* **And** persiste alerta, dispara notificação assíncrona (email, push, webhook) para responsáveis (analistas da filial + gestor)
* **And** para alertas `ABERTOS` anteriores: verifica normalização (uso < threshold - histerese 5% por 2 ciclos) → `status=RESOLVIDO`, `dataResolucao=now`, `resolvidoAutomaticamente=true`
* **And** log de execução: `quantidadeVerificados`, `alertasGerados`, `alertasResolvidos`, `duracaoMs`

#### Scenario 2: Thresholds por tipo de ativo
* **Given** tipo "Servidor" tem threshold disco 80%, tipo "Notebook" 85%
* **When** job avalia
* **Then** busca `AlertaThreshold` por `tipoAtivoId`; fallback para defaults globais

#### Scenario 3: Erro ao processar um ativo (health check corrompido)
* **Given** health check com dados inválidos
* **When** try-catch por ativo
* **Then** log erro; continua processando demais; métrica `alertasErros` incrementada

#### Scenario 4: Falha no envio de notificação (SMTP down)
* **Given** alerta persistido mas email falha
* **When** notificação
* **Then** alerta persistido normalmente; notificação enfileirada em `NotificationOutbox`; worker separado processa com retry exponencial

#### Scenario 5: Job excede 5 min (timeout scheduler)
* **Given** 10.000 ativos para processar
* **When** processamento em batches (100 ativos/batch), `@Async` com `CompletableFuture`
* **Then** métrica de duração; job completa < 2 min

#### Scenario 6: Trigger manual (Admin)
* **Given** admin clica "Verificar alertas agora" no painel
* **When** `POST /api/alertas/verificar`
* **Then** executa mesma lógica do job, retorna resumo sincronizado

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — painel admin "Verificação de Alertas" com botão trigger manual, log de últimas execuções, métricas]
* **Estados:** N/A (job), admin panel: loading (execução), success (resumo), error (detalhes)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AlertNotificationService.checkResourceUsageAlerts` (refatorado), `AtivoDetalheHardwareRepository.findRecent`, `AlertaRepository`, `AlertaThresholdRepository`, `NotificationOutbox`, `NotificationWorker`, `SchedulerConfig` (cron 15 min), frontend: `AlertasVerificationPanel` (admin)
* **Dependências técnicas:** Tabelas `alerta`, `alerta_threshold`, `notification_outbox`, `ativo_detalhe_hardware`, `audit_log` (sampling)
* **Considerações de performance/segurança:** Histerese 5% + 2 ciclos para evitar flapping; Particionamento `alerta` por mês; Idempotência via deduplicação; Taxa erro 5xx < 0.1% exige testes de caos + refatoração completa

### 6. Out of Scope
* Canais de notificação por severidade/role (email CRITICA, push ALTA, log BAIXA — Open Issue UC-25)
* Integração ITSM (Jira, ServiceNow) — futuro

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] **Refatoração `AlertNotificationService` CONCLUÍDA** (complexidade 17 → classes < 10)
- [ ] Tabela `alerta_threshold` criada
- [ ] `NotificationOutbox` + worker implementados
- [ ] Histerese e deduplicação definidas

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes unitários: cada evaluator isolado, deduplicação, histerese, resolução automática
- [ ] Testes de integração: job completo com 100 ativos mock, trigger manual
- [ ] Teste de carga: 10.000 ativos < 2 min, taxa erro < 0.1%
- [ ] Tipagem, lint, OpenAPI
- [ ] Testado em staging com agendador real

### 9. Traceability
* **NFRs relacionadas:** NFR-39 (Taxa erro 5xx < 0.1%), NFR-40 (Execução < 2 min 10k ativos), NFR-41 (Idempotência job)
* **Use Cases relacionados:** UC-25
* **Riscos relacionados:** RISK-16 (AlertNotificationService complexidade 17 — CRÍTICO), RISK-21 (Notificação falha), RISK-22 (Timeout job)

---

## US-REL-001 — Consultar custo total por ativo (TCO)

* **Priority:** Should
* **Complexity:** 5
* **Épico:** Relatórios & BI
* **Labels:** [backend, frontend, reports, finance, materialized-view]

### 1. Description (INVEST)
* **As a** Gestor de Patrimônio, Financeiro ou Auditor
* **I want to** visualizar relatório de custo total por ativo (valor aquisição + custos manutenção - valor residual) com filtros por filial, período e ordenação por maior custo
* **So that** eu possa identificar ativos com TCO elevado, planejar substituições e auditar custos de manutenção

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Média frequência (relatórios semanais/mensais — UC-26). BR-06 (`custoTotalPorAtivo` = soma custos manutenção por ativo). BR-01 (perm `relatorio:custo-total` — roles: GESTOR_PATRIMONIO, ADMIN, FINANCEIRO, AUDITOR). BR-04, BR-07. NFR: Query agregada ≤ 2s para 12.000 ativos (view materializada refrescada 30 min ou on-demand). Export 12k linhas CSV < 5s (streaming). Depreciação linear padrão: `valorResidualAtual = valorAquisicao * (1 - mesesDecorridos / vidaUtilMeses)` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]. `TCO = valorAquisicao + custoTotalManutencao - valorResidualAtual`.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Relatório padrão com filtros
* **Given** gestor acessa "Relatórios > Custo Total por Ativo", seleciona filial "Matriz", período 2024, ordenação `custoTotalManutencao DESC`
* **When** frontend envia `GET /api/relatorios/custo-total-por-ativo?filialId=1&dataInicio=2024-01-01&dataFim=2024-12-31&page=0&size=50`
* **Then** backend valida permissão, executa query agregada (view materializada `mv_custo_total_ativo` ou `SUM(manutencao.custo_total) GROUP BY ativo_id`)
* **And** join com `Ativo` para metadados, calcula `valorResidualAtual` (linear), `TCO`
* **And** retorna paginado `200 OK` com DTOs
* **And** frontend renderiza tabela com ordenação, filtros, export CSV/Excel
* **And** gráfico: Top 10 ativos por TCO / por custo manutenção

#### Scenario 2: Drill-down por ativo
* **Given** gestor clica na linha do ativo "Servidor-01"
* **When** `GET /api/relatorios/custo-total-por-ativo/{ativoId}/detalhamento`
* **Then** retorna lista de manutenções do ativo com custo, data, tipo, fornecedor

#### Scenario 3: Agregação por filial/departamento/tipo
* **Given** gestor quer visão consolidada
* **When** `GET /api/relatorios/custo-total-agregado?groupBy=filial&dataInicio=2024-01-01`
* **Then** retorna totais por grupo (filial, departamento, tipo)

#### Scenario 4: View materializada desatualizada
* **Given** última refresh > 1h atrás
* **When** consulta relatório
* **Then** header `X-Data-Atualizacao` no response; frontend avisa "Dados atualizados há 45 min"; botão "Atualizar agora" dispara refresh assíncrono

#### Scenario 5: Ativo sem manutenções
* **Given** ativo nunca teve manutenção
* **When** incluído no relatório
* **Then** `custoTotalManutencao = 0`; `TCO = valorAquisicao - valorResidualAtual`

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tabela relatório com colunas: Código, Descrição, Filial, Valor Aquisição, Custo Manutenção, Valor Residual, TCO, Última Manutenção; gráficos Top 10]
* **Estados:** loading (skeleton), empty, error, success, stale data warning
*   **Export:** CSV/Excel streaming com progress bar

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `RelatorioService.custoTotalPorAtivo`, `CustoTotalPorAtivoService` (outbox event listener), `RelatorioMapper`, frontend: `CustoTotalReport`, `CustoTotalCharts` (Chart.js + Popper.js)
* **Dependências técnicas:** `GET /api/relatorios/custo-total-por-ativo`, `GET /api/relatorios/custo-total-por-ativo/{id}/detalhamento`, `GET /api/relatorios/custo-total-agregado`, view materializada `mv_custo_total_ativo` (refresh trigger em `Manutencao` insert/update status=CONCLUIDA ou job 30 min), `audit_log`
* **Considerações de performance/segurança:** View materializada essencial para performance; Permissão `relatorio:custo-total` — definir roles exatas (Open Issue UC-26); Método depreciação oficial (linear vs fiscal) — Open Issue UC-26

### 6. Out of Scope
* Outros métodos de depreciação (saldo decrescente — futuro)
* Custo de peças em estoque alocadas (hoje apenas manutenções concluídas)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] View materializada modelada e aprovada
- [ ] Método depreciação definido
- [ ] Permissão `relatorio:custo-total` mapeada para roles

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: relatório padrão, drill-down, agregação, export, stale data warning, ativo sem manutenções
- [ ] Performance: query ≤ 2s, export 12k < 5s
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-42 (Query agregada ≤ 2s), NFR-43 (Export 12k < 5s)
* **Use Cases relacionados:** UC-26
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-23 (Depreciação linear), RISK-24 (Permissão relatório)

---

## US-REL-002 — Dashboard executivo (North Star Metric & Guardrails)

* **Priority:** Should
* **Complexity:** 8
* **Épico:** Relatórios & BI
* **Labels:** [backend, frontend, dashboard, kpi, popperjs]

### 1. Description (INVEST)
* **As a** Sponsor (Diretoria) ou Gestor de Patrimônio
* **I want to** visualizar o dashboard executivo com North Star Metric, Guardrails e KPIs do BRD na tela inicial
* **So that** a liderança tenha visão estratégica em tempo real (dados refrescados a cada 15 min) para tomada de decisão

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Alta frequência (tela inicial — UC-29). BR-01 (restrito ADMIN, GESTOR_PATRIMONIO). KPIs BRD Seção 4. NFR: P95 ≤ 1s (agregações pré-calculadas), Disponibilidade 99.95%. Popper.js para tooltips nos gráficos (dependência confirmada). Views materializadas: `mv_north_star`, `mv_guardrails`, `mv_kpis_brd`.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Carregamento do dashboard executivo
* **Given** usuário com role `ADMIN` ou `GESTOR_PATRIMONIO` acessa tela inicial
* **When** frontend carrega `GET /api/dashboard/executivo`
* **Then** backend agrega views materializadas (refrescadas 15 min) e retorna:
  - **North Star:** `ativosComHealthCheckRecente30d / totalAtivosCadastrados * 100` (gauge)
  - **Guardrails:** Latência P95 API (24h), Taxa erro 5xx `checkResourceUsageAlerts`, Custo infra/ativo, Churn usuários
  - **KPIs BRD:** % ativos cadastrados, Downtime não planejado (h/mês), % operações auditadas, % alertas convertidos em preventiva
* **And** frontend renderiza cards, gauges, sparklines (Chart.js + Popper.js tooltips)
* **And** latência P95 ≤ 1s

#### Scenario 2: Drill-down em métrica
* **Given** usuário clica no card "North Star: 78%"
* **When** navega para relatório detalhado
* **Then** abre US-REL-001 ou relatório específico de cobertura de health check

#### Scenario 3: Views materializadas não refrescadas
* **Given** última refresh > 30 min
* **When** dashboard carrega
* **Then** badge "Dados desatualizados" + botão "Atualizar agora" (dispara job assíncrono de refresh)

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — dashboard executivo com layout de cards responsivo, gauges animados, sparklines]
* **Estados:** loading (skeleton cards), stale data (warning banner), error, success
* **Acessibilidade:** valores numéricos em texto alternativo, navegação por teclado

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `DashboardService`, `DashboardMapper`, views materializadas, frontend: `ExecutiveDashboard`, `KpiCard`, `GaugeChart`, `SparklineChart` (Chart.js + Popper.js)
* **Dependências técnicas:** `GET /api/dashboard/executivo`, views `mv_north_star`, `mv_guardrails`, `mv_kpis_brd`, `@popperjs/core` para tooltips
* **Considerações de performance/segurança:** Views refrescadas por job agendado (pg_cron ou Spring @Scheduled); Permissão por filial (gestor vê apenas sua filial?) — Open Issue UC-29

### 6. Out of Scope
* Permissão por filial no dashboard (Open Issue UC-29)
* Queries exatas das views materializadas (a definir)

### 7. Definition of Ready
- [ ] Critérios validados
- [ ] Views materializadas modeladas e aprovadas
- [ ] Queries das views definidas

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: carregamento, drill-down, stale data warning, refresh manual
- [ ] Performance P95 ≤ 1s
- [ ] Popper.js tooltips funcionando
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging

### 9. Traceability
* **NFRs relacionadas:** NFR-44 (P95 ≤ 1s), NFR-45 (Disponibilidade 99.95%)
* **Use Cases relacionados:** UC-29
* **Riscos relacionados:** RISK-02 (AuditLog), RISK-25 (Views materializadas), RISK-26 (Permissão por filial)

---

## US-AUD-001 — Auditoria e trilha de custódia imutável

* **Priority:** Must
* **Complexity:** 13
* **Épico:** Auditoria & Compliance
* **Labels:** [backend, frontend, audit, security, compliance, blocked]

### 1. Description (INVEST)
* **As a** Auditor / Compliance
* **I want to** consultar a trilha de auditoria completa e imutável (logs de criar/atualizar/deletar/aprovar/cancelar/concluir/iniciar/baixar/logar/deslogar) com filtros por período, usuário, entidade, ação, filial
* **So that** eu possa evidenciar conformidade LGPD/SOX, investigar incidentes e comprovar custódia de ativos

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
**GAP CRÍTICO / BLOQUEANTE:** Não existe entidade `AuditLog` no código (UC-30). BR-02 exige log imutável com todos os campos obrigatórios. BR-01: acesso restrito AUDITOR/ADMIN. BR-07: mapper. NFR: Imutabilidade (tabela sem UPDATE/DELETE permits, grants restritos); Integridade: hash encadeado (hash do log anterior incluído no atual) para detectar adulteração — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; Disponibilidade 99.99%; Latência P95 ≤ 2s (particionamento + índices). Retenção: 7 anos (SOX) / 5 anos (LGPD); Tiering para cold storage (S3 Glacier). Volume: health checks — sampling? (Open Issue UC-30).

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Consulta de logs com filtros compostos
* **Given** auditor autenticado (role `AUDITOR`) acessa "Auditoria > Trilha de Custódia"
* **When** define filtros: período, usuário, entidade `Ativo`, ação `atualizar`, filial, envia `GET /api/auditoria/logs?entidade=Ativo&acao=atualizar&dataInicio=2025-01-01&usuarioId=123&page=0&size=50`
* **Then** backend valida permissão `auditoria:ler`, executa query em `audit_log` (particionada por mês, indexada por `entidade, entidade_id, timestamp`)
* **And** retorna `200 OK` paginado com DTOs: id, timestamp, usuario (nome, email), entidade, entidade_id, acao, valores_anteriores (JSON), valores_novos (JSON), ip, userAgent
* **And** frontend renderiza tabela expansível (clica linha → mostra diff JSON formatado)
* **And** latência P95 ≤ 2s

#### Scenario 2: Trilha de custódia de ativo específico
* **Given** auditor quer timeline completa de um ativo
* **When** `GET /api/auditoria/custodia/ativo/{ativoId}`
* **Then** retorna timeline ordenada por timestamp: criação, alocações (mudança filial/departamento/localização), manutenções, health checks, baixa — tudo em uma visão unificada

#### Scenario 3: Exportação WORM-compliant
* **Given** auditor clica "Exportar CSV" com filtros
* **When** `GET /api/auditoria/logs/export?filtros...&format=csv`
* **Then** streaming CSV com todos os registros (sem paginação), headers incluem hash encadeado para verificação de integridade

#### Scenario 4: Tabela audit_log não existe (estado atual)
* **Given** código atual não tem entidade AuditLog
* **When** auditor tenta acessar
* **Then** **IMPEDIMENTO** — retorna 500 ou erro de schema
* **And** **AÇÃO REQUERIDA:** Implementar `AuditLog` entity + interceptores JPA (`@PrePersist`, `@PreUpdate`, `@PreRemove`) + aspect para ações de service (aprovar, cancelar, concluir) **antes de qualquer release de produção** (Sprint 0)

#### Scenario 5: Tentativa de adulteração (teste de segurança)
* **Given** atacante com acesso direto ao banco tenta `UPDATE audit_log SET valores_novos='...' WHERE id=1`
* **When** executa
* **Then** banco rejeita (grants apenas INSERT/SELECT para role da aplicação)
* **And** hash encadeado detecta inconsistência em job de verificação de integridade

### 4. UI/UX Notes
* **Wireframe/Mockup:** [Link para Figma — tela auditoria com filtros avançados, tabela expansível com diff JSON syntax-highlighted, export]
* **Estados:** loading, empty, error, success
* **Diff visual:** biblioteca `diff2html` ou similar para comparação lado a lado

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AuditLogService`, `AuditLogRepository`, `AuditLogMapper`, `AuditInterceptor` (JPA `@PrePersist`/`@PreUpdate`/`@PreRemove` em entidades auditadas), `AuditAspect` (para ações de service: aprovar, cancelar, concluir, iniciar, baixar, login, logout), frontend: `AuditoriaFilters`, `AuditoriaTable`, `AuditDiffModal`
* **Dependências técnicas:** Tabela `audit_log` (particionada por mês: `audit_log_2025_01`), colunas: `id` (UUID), `timestamp`, `usuario_id`, `usuario_nome`, `usuario_email`, `entidade`, `entidade_id`, `acao`, `valores_anteriores` (JSONB), `valores_novos` (JSONB), `ip`, `user_agent`, `hash_encadeado` (SHA-256 do log anterior + atual), `audit_log` grants: `INSERT, SELECT` apenas
* **Considerações de performance/segurança:** Particionamento mensal + índices compostos; Tiering: logs > 1 ano movidos para tabela cold storage (mesma estrutura, storage barato); Hash encadeado vs assinatura digital — decisão arquitetura (Open Issue UC-30); Sampling para health checks (volume alto)

### 6. Out of Scope
* Assinatura digital (avaliar vs hash encadeado)
* Sampling health checks (definir política)

### 7. Definition of Ready
- [ ] **IMPEDIMENTO RESOLVIDO:** `AuditLog` entity + interceptores + aspect implementados (Sprint 0)
- [ ] Critérios validados
- [ ] Modelo de dados (particionamento, hash encadeado) aprovado por arquitetura/segurança
- [ ] Retenção por tipo de entidade definida

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes: consulta filtros, custódia ativo, export, imutabilidade (tentativa UPDATE falha), hash encadeado válido
- [ ] Performance: P95 ≤ 2s com 1M logs particionados
- [ ] Segurança: grants restritos, hash verification job
- [ ] Tipagem, lint, a11y, OpenAPI
- [ ] Testado em staging com carga de logs realista

### 9. Traceability
* **NFRs relacionadas:** NFR-46 (Imutabilidade tabela), NFR-47 (Integridade hash encadeado), NFR-48 (Disponibilidade 99.99%), NFR-49 (Latência P95 ≤ 2s)
* **Use Cases relacionados:** UC-30
* **Riscos relacionados:** RISK-02 (AuditLog — BLOQUEANTE), RISK-27 (Hash encadeado vs assinatura), RISK-28 (Retenção/volume), RISK-29 (Sampling health checks)

---

## US-TECH-001 — Refatorar AtivoMapper (complexidade ciclomática 14)

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [backend, refactoring, tech-debt, mapper]

### 1. Description (INVEST)
* **As a** Desenvolvedor Backend
* **I want to** quebrar o `AtivoMapper.toDTO` e `toEntity` em métodos menores por seção (identificação, localização, financeiro, técnico)
* **So that** a complexidade ciclomática caia de 14 para < 10, facilitando manutenção, testes e reduzindo risco de bugs em mapeamento

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Diagnóstico determinístico: `AtivoMapper.java:15` — complexidade 14. Afeta UC-01, UC-02, UC-03, UC-04, UC-05. Refatoração pré-requisito para US-ATIVO-001, US-ATIVO-004.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Mapper refatorado passa no quality gate
* **Given** `AtivoMapper` refatorado com métodos privados: `mapIdentificacao`, `mapLocalizacao`, `mapFinanceiro`, `mapTecnico`, `mapRelacionamentos`
* **When** executa `mvn verify` (Checkstyle/Spotless + SonarQube)
* **Then** complexidade ciclomática de `toDTO` e `toEntity` ≤ 10
* **And** todos os testes unitários existentes passam
* **And** cobertura de testes ≥ 80%

#### Scenario 2: Comportamento idêntico (regressão)
* **Given** suíte de testes de integração dos endpoints de ativo
* **When** executa com mapper refatorado
* **Then** todos os testes passam (mesmo JSON de entrada/saída)

### 4. UI/UX Notes
* N/A (refatoração interna)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AtivoMapper.java`, testes unitários `AtivoMapperTest`
* **Dependências técnicas:** Nenhuma nova
* **Considerações de performance/segurança:** Manter null-safety (BR-07); evitar N+1 em relacionamentos (fetch plan no service)

### 6. Out of Scope
* Mudança de assinatura pública do mapper

### 7. Definition of Ready
- [ ] Testes de caracterização existentes (snapshot dos DTOs atuais)

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Complexidade ≤ 10 verificada no SonarQube
- [ ] Testes unitários + integração passam
- [ ] Cobertura ≥ 80%
- [ ] Tipagem, lint
- [ ] Merge para main

### 9. Traceability
* **NFRs relacionadas:** NFR-50 (Qualidade código — complexidade < 10)
* **Use Cases relacionados:** UC-01, UC-02, UC-03, UC-04, UC-05
* **Riscos relacionados:** RISK-01 (Mapper complexidade 14)

---

## US-TECH-002 — Refatorar ManutencaoSpecification (complexidade ciclomática 14)

* **Priority:** Must
* **Complexity:** 5
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [backend, refactoring, tech-debt, specification, jpa]

### 1. Description (INVEST)
* **As a** Desenvolvedor Backend
* **I want to** extrair `PredicateBuilder` por filtro no `ManutencaoSpecification.build` para reduzir complexidade de 14 para < 10
* **So that** a especificação seja extensível sem quebrar clientes, testável isoladamente e performática

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Diagnóstico: `ManutencaoSpecification.java:26` — complexidade 14. Afeta UC-18, UC-19, UC-20, UC-21, UC-22. Pré-requisito para US-MAN-005.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Specification refatorada com builders por filtro
* **Given** `ManutencaoSpecification` refatorada com `StatusPredicateBuilder`, `FilialPredicateBuilder`, `TipoPredicateBuilder`, `PrioridadePredicateBuilder`, `DataRangePredicateBuilder`, `SolicitantePredicateBuilder`, `AprovadorPredicateBuilder`
* **When** `build(filtros)` compõe predicates dinamicamente
* **Then** complexidade ciclomática ≤ 10
* **And** cada builder testável isoladamente
* **And** novos filtros adicionados sem modificar `build` principal (Open/Closed Principle)

#### Scenario 2: Queries geradas são eficientes
* **Given** combinações de 10+ filtros
* **When** `EXPLAIN ANALYZE` em CI
* **Then** usa índices compostos `(filial_id, status, tipo, data_solicitacao)` — sem full table scan

### 4. UI/UX Notes
* N/A

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `ManutencaoSpecification.java`, novos `*PredicateBuilder` classes, testes unitários
* **Dependências técnicas:** JPA Criteria API ou Querydsl (diagnóstico: SQL cru assumido)
* **Considerações de performance/segurança:** Índices compostos obrigatórios; monitoramento slow queries em CI

### 6. Out of Scope
* Migração para Querydsl (se não usado hoje)

### 7. Definition of Ready
- [ ] Testes de caracterização das queries atuais

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Complexidade ≤ 10
- [ ] Testes unitários por builder + integração combinações
- [ ] CI: `EXPLAIN ANALYZE` passa
- [ ] Tipagem, lint
- [ ] Merge

### 9. Traceability
* **NFRs relacionadas:** NFR-50 (Qualidade código), NFR-30 (P95 ≤ 500ms filtros)
* **Use Cases relacionados:** UC-18, UC-19, UC-20, UC-21, UC-22
* **Riscos relacionados:** RISK-04 (Specification complexidade 14 — CRÍTICO)

---

## US-TECH-003 — Refatorar AlertNotificationService (complexidade ciclomática 17)

* **Priority:** Must
* **Complexity:** 13
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [backend, refactoring, tech-debt, alerts, critical]

### 1. Description (INVEST)
* **As a** Desenvolvedor Backend
* **I want to** separar `AlertNotificationService.checkResourceUsageAlerts` em: `ResourceThresholdEvaluator` (interface), `DiskEvaluator`, `MemoryEvaluator`, `NetworkEvaluator`, `AlertDeduplicator`, `AlertPersister`, `NotificationDispatcher`
* **So that** a complexidade caia de 17 para < 10 por classe, a taxa de erro 5xx < 0.1% seja atingível, e o job de verificação de alertas seja confiável em produção

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
**CRÍTICO:** Diagnóstico `AlertNotificationService.java:96` — complexidade 17. Afeta UC-08 (health check dispara alertas), UC-25 (job agendado). Guardrail BRD: taxa erro 5xx < 0.1%. Refatoração obrigatória antes de Q2. NFR: Execução job < 2 min para 10k ativos; Idempotência; Histerese 5% + 2 ciclos.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Serviço refatorado em classes coesas
* **Given** `AlertNotificationService` delega para:
  - `DiskEvaluator.evaluate(healthCheck)` → `List<AlertCandidate>`
  - `MemoryEvaluator.evaluate(healthCheck)` → `List<AlertCandidate>`
  - `NetworkEvaluator.evaluate(healthCheck)` → `List<AlertCandidate>`
  - `AlertDeduplicator.deduplicate(candidates, existingAlerts)` → `List<AlertToCreate>`
  - `AlertPersister.persist(alerts)` → `List<Alerta>`
  - `NotificationDispatcher.dispatch(alerts)` → assíncrono via `NotificationOutbox`
* **When** executa `checkResourceUsageAlerts(ativoId)` ou job completo
* **Then** cada classe tem complexidade ≤ 10
* **And** testes unitários cobrem cada evaluator isoladamente (thresholds, histerese, deduplicação)
* **And** taxa erro 5xx < 0.1% em teste de carga (10k ativos)

#### Scenario 2: Histerese e deduplicação funcionam
* **Given** ativo com disco 86% (threshold 85%) por 3 ciclos
* **When** job roda
* **Then** Ciclo 1: cria alerta `DISCO_CRITICO` ABERTO
* **And** Ciclo 2: deduplicação mantém alerta aberto (mesmo ativo+tipo)
* **And** Ciclo 3: disco cai para 82% (< 85% - 5% histerese) → alerta RESOLVIDO automaticamente

#### Scenario 3: Falha em evaluator não derruba job
* **Given** `DiskEvaluator` lança exceção para um ativo
* **When** job processa batch
* **Then** erro logado, métrica `evaluator_errors` incrementada, job continua para demais ativos/evaluators

### 4. UI/UX Notes
* N/A (serviço interno)

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `AlertNotificationService` (refatorado), novas classes evaluator/dispatcher, `AlertaThresholdRepository`, `NotificationOutbox`, `NotificationWorker`, testes
* **Dependências técnicas:** Tabelas `alerta`, `alerta_threshold`, `notification_outbox`, `ativo_detalhe_hardware`
* **Considerações de performance/segurança:** Processamento em batches (100 ativos) + `@Async`/`CompletableFuture`; Métricas Prometheus por evaluator; Circuit breaker no `NotificationDispatcher`

### 6. Out of Scope
* Canais de notificação por severidade/role (Open Issue UC-25)
* Integração ITSM (futuro)

### 7. Definition of Ready
- [ ] Arquitetura de refatoração aprovada (interfaces + classes)
- [ ] Tabela `alerta_threshold` modelada
- [ ] `NotificationOutbox` + worker prontos

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Testes unitários: cada evaluator (thresholds, histerese), deduplicador, persister, dispatcher (mock)
- [ ] Testes de integração: job completo 100 ativos mock, trigger manual
- [ ] Teste de carga: 10k ativos < 2 min, taxa erro < 0.1%
- [ ] Tipagem, lint, OpenAPI
- [ ] Testado em staging com agendador real

### 9. Traceability
* **NFRs relacionadas:** NFR-39 (Taxa erro 5xx < 0.1%), NFR-40 (Execução < 2 min), NFR-41 (Idempotência), NFR-50 (Qualidade código)
* **Use Cases relacionados:** UC-08, UC-25
* **Riscos relacionados:** RISK-16 (AlertNotificationService complexidade 17 — CRÍTICO), RISK-21 (Notificação falha), RISK-22 (Timeout job)

---

## US-TECH-004 — Corrigir stub Usuario.setUsername (linha 86)

* **Priority:** Must
* **Complexity:** 1
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [backend, bug, blocker, security]

### 1. Description (INVEST)
* **As a** Desenvolvedor Backend
* **I want to** implementar corretamente o método `setUsername` na entidade `Usuario`
* **So that** a criação/atualização de usuários (incluindo onboarding funcionário+usuário) funcione e não gere tokens inválidos

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
**BUG BLOQUEANTE** identificado no diagnóstico: `Usuario.java:86` — método `setUsername` tem corpo vazio (stub). Afeta US-ATIVO-011 (onboarding), US-ATIVO-013 (gerenciar usuários), UC-13, UC-16, UC-28. Impede login de usuários criados via API.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Set username persiste corretamente
* **Given** instância de `Usuario` nova ou existente
* **When** chama `usuario.setUsername("joao.silva")`
* **Then** campo `username` é atualizado na entidade
* **And** ao salvar (`UsuarioRepository.save`), banco persiste o valor
* **And** login com esse username funciona

#### Scenario 2: Validação de unicidade funciona
* **Given** dois usuários com mesmo username
* **When** tenta salvar o segundo
* **Then** constraint unique viola → exception tratada → `400 Bad Request` "Username já cadastrado"

### 4. UI/UX Notes
* N/A

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `Usuario.java:86` (linha exata do diagnóstico)
* **Dependências técnicas:** Tabela `usuario` (coluna `username` unique not null)
* **Considerações de performance/segurança:** Validação de formato (alphanumérico, 3-50 chars) no setter ou no service; `@Column(unique=true, nullable=false)`

### 6. Out of Scope
* Nenhum

### 7. Definition of Ready
- [ ] Imediato — bug bloqueante

### 8. Definition of Done
- [ ] Código: `setUsername(String username) { this.username = username; }` + validação básica
- [ ] Teste unitário: setter + persistência + unicidade
- [ ] Teste de integração: US-ATIVO-011 (onboarding) passa
- [ ] Tipagem, lint
- [ ] Merge urgente para main

### 9. Traceability
* **NFRs relacionadas:** NFR-51 (Correção bug bloqueante)
* **Use Cases relacionados:** UC-13, UC-16, UC-28
* **Riscos relacionados:** RISK-03 (setUsername stub — BLOQUEANTE)

---

## US-TECH-005 — Remover console.* residuais do frontend (api.js)

* **Priority:** Must
* **Complexity:** 1
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [frontend, cleanup, production-readiness]

### 1. Description (INVEST)
* **As a** Desenvolvedor Frontend
* **I want to** remover `console.error` (linha 36) e `console.debug` (linha 99) de `frontend/src/services/api.js`
* **So that** o build de produção não vaze logs sensíveis e o console do navegador fique limpo

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Diagnóstico determinístico: 2 chamadas `console.*` residuais. `console.error` em tratamento de erro, `console.debug` em log de requisição. Devem ser removidos ou substituídos por logger estruturado (ex: `pino` ou `winston` no backend, `loglevel` no frontend) antes de produção.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Build de produção sem console.*
* **Given** `npm run build` (ou equivalente) para produção
* **When** busca por `console\.` no bundle
* **Then** zero ocorrências de `console.error`, `console.debug`, `console.log` em `api.js`
* **And** tratamento de erros usa `handleApiError` (já existente) sem log direto no console

#### Scenario 2: Desenvolvimento ainda tem logs úteis
* **Given** `npm run dev`
* **When** faz requisições
* **Then** logs estruturados via logger configurado (nível debug em dev, warn em prod)

### 4. UI/UX Notes
* N/A

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js` (linhas 36, 99)
* **Dependências técnicas:** Logger frontend (ex: `loglevel` ou `pino-pretty` para dev)
* **Considerações de performance/segurança:** Não vazar tokens, PII ou stack traces no console de produção

### 6. Out of Scope
* Implementar logger estruturado completo (pode ser story separada)

### 7. Definition of Ready
- [ ] Imediato — limpeza simples

### 8. Definition of Done
- [ ] Código: linhas 36 e 99 removidas/substituídas
- [ ] Build produção verificado sem `console.*`
- [ ] Dev mode ainda loga via logger
- [ ] Lint passa
- [ ] Merge

### 9. Traceability
* **NFRs relacionadas:** NFR-52 (Produção sem console logs)
* **Use Cases relacionados:** UC-17 (authInterceptor usa api.js), todos os US que usam `request`
* **Riscos relacionados:** RISK-11 (console.* produção)

---

## US-TECH-006 — Refatorar função request (api.js) — complexidade 13

* **Priority:** Should
* **Complexity:** 5
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [frontend, refactoring, tech-debt, api-client]

### 1. Description (INVEST)
* **As a** Desenvolvedor Frontend
* **I want to** quebrar a função `request` em `frontend/src/services/api.js:46` (complexidade 13) em funções menores: `buildRequest`, `handleResponse`, `handleError`, `refreshTokenFlow`
* **So that** o cliente HTTP seja testável, manutenível e o interceptor de auth fique isolado

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Diagnóstico: `api.js:46` — função `request` complexidade 13. Usada por todos os US de frontend. Afeta US-ATIVO-014 (auth) principalmente.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Request refatorado em módulos coesos
* **Given** `api.js` refatorado com:
  - `buildRequest(config)` — monta headers, injeta token via `authInterceptor`
  - `executeRequest(requestConfig)` — fetch/axios call
  - `handleResponse(response)` — parsing, validação status
  - `handleError(error)` — `handleApiError` existente
  - `refreshTokenFlow(originalRequest)` — fila + refresh + retry
* **When** qualquer US faz chamada via `api.request(...)`
* **Then** comportamento idêntico (mesmos headers, mesma lógica de refresh, mesmo tratamento de erro)
* **And** complexidade ciclomática de cada função ≤ 5
* **And** testes unitários cobrem cada função isoladamente

### 4. UI/UX Notes
* N/A

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `frontend/src/services/api.js`, testes `api.test.js`
* **Dependências técnicas:** `authInterceptor`, `handleApiError`, `handleResponse`, `clearSession`, `logout`, `mockLogin`
* **Considerações de performance/segurança:** Fila de requisições durante refresh (evita race condition); Token em memory + httpOnly cookie

### 6. Out of Scope
* Migração para TanStack Query / React Query (futuro)

### 7. Definition of Ready
- [ ] Testes de caracterização do comportamento atual

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Complexidade ≤ 5 por função
- [ ] Testes unitários: buildRequest, handleResponse, handleError, refreshTokenFlow
- [ ] Testes de integração: todos os US de frontend passam
- [ ] Lint, tipagem
- [ ] Merge

### 9. Traceability
* **NFRs relacionadas:** NFR-50 (Qualidade código), NFR-21 (Refresh rotation sem race)
* **Use Cases relacionados:** UC-17, todos US frontend
* **Riscos relacionados:** RISK-10 (request complexidade 13)

---

## US-TECH-007 — Refatorar RealisticDataSeeder (complexidade 15)

* **Priority:** Could
* **Complexity:** 5
* **Épico:** Qualidade de Código & Tech Debt
* **Labels:** [backend, refactoring, test-data, seeder]

### 1. Description (INVEST)
* **As a** Desenvolvedor Backend
* **I want to** refatorar `RealisticDataSeeder.run` (complexidade 15) usando builders/factories para dados de teste realistas
* **So that** o seeder seja mantenível, extensível para novos cenários de teste e não viole quality gates

*Checklist INVEST — a story deve ser: Independent, Negotiable, Valuable, Estimable, Small, Testable.*

### 2. Business Context
Diagnóstico: `RealisticDataSeeder.java:34` — complexidade 15. Usado para testes/carga. Não afeta produção diretamente, mas quality gate falha.

### 3. Acceptance Criteria (BDD/Gherkin)
#### Scenario 1: Seeder refatorado com builders
* **Given** `RealisticDataSeeder` usa `AtivoBuilder`, `ManutencaoBuilder`, `UsuarioBuilder`, `FilialBuilder` etc.
* **When** `run()` executa
* **Then** complexidade ≤ 10
* **And** builders permitem variações (ex: `AtivoBuilder.comHealthCheck().comManutencaoPendente().build()`)
* **And** dados gerados são consistentes (FKs válidas, enums corretos)

### 4. UI/UX Notes
* N/A

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `RealisticDataSeeder.java`, novos `*Builder` classes (pattern Builder)
* **Dependências técnicas:** Nenhuma nova
* **Considerações de performance/segurança:** Seeders não rodam em produção (profile `test`/`dev`)

### 6. Out of Scope
* Dados de produção (apenas teste)

### 7. Definition of Ready
- [ ] Critérios validados

### 8. Definition of Done
- [ ] Código + revisão
- [ ] Complexidade ≤ 10
- [ ] Testes: seeder roda sem erro, dados consistentes
- [ ] Lint, tipagem
- [ ] Merge

### 9. Traceability
* **NFRs relacionadas:** NFR-50 (Qualidade código)
* **Use Cases relacionados:** Nenhum direto (testes)
* **Riscos relacionados:** RISK-30 (Seeder quality gate)

---

## Rastreabilidade Consolidada de Riscos Técnicos

| ID | Risco | Descrição | Impacto | Mitigação | Stories Afetadas |
|----|-------|-----------|---------|-----------|------------------|
| RISK-01 | Mapper complexidade | `AtivoMapper` complexidade 14 | Manutenibilidade, bugs mapeamento | US-TECH-001 (refatorar) | US-ATIVO-001, 002, 003, 004, 005 |
| RISK-02 | AuditLog não implementado | Tabela `audit_log` inexistente | **Não conformidade BR-02, LGPD, SOX** | US-AUD-001 (Sprint 0) | **TODAS** (auditoria transversal) |
| RISK-03 | setUsername stub | `Usuario.java:86` corpo vazio | **Bloqueia criação usuários, onboarding** | US-TECH-004 (correção urgente) | US-ATIVO-011, 013, US-MAN-001 (solicitante) |
| RISK-04 | Specification complexidade | `ManutencaoSpecification` complexidade 14 | Performance, extensibilidade filtros | US-TECH-002 (refatorar) | US-MAN-001, 002, 003, 004, 005 |
| RISK-05 | Lazy loading | `LazyInitializationException` em detalhe ativo | Erro 500 em US-ATIVO-003 | `@Transactional(readOnly=true)` + EntityGraph | US-ATIVO-003 |
| RISK-06 | Soft vs Hard delete | Decisão arquitetural pendente | LGPD, auditoria, relatórios | Definir com Compliance/Arquitetura | US-ATIVO-005 |
| RISK-07 | BR-10 cascata | `deleteByAtivoDetalheHardwareId` transacional | Integridade health checks | Testes de cascata + transação | US-ATIVO-005, US-MON-001 |
| RISK-08 | Matriz permissões | Roles/permissões não definidas | RBAC incompleto | Workshop stakeholders | US-ATIVO-012 |
| RISK-09 | Estratégia tokens | Invalidação access token no logout | Segurança, sessões órfãs | Definir: blacklist Redis vs versioning | US-ATIVO-013, 014 |
| RISK-10 | request complexidade | `api.js:46` complexidade 13 | Manutenibilidade frontend | US-TECH-006 | Todos US frontend |
| RISK-11 | console.* produção | Logs residuais em api.js | Vazamento info, console poluído | US-TECH-005 | UC-17, todos US frontend |
| RISK-12 | Alçadas indefinidas | Aprovação manutenção por valor | Fluxo de aprovação quebrado | Workshop Gestão/Financeiro | US-MAN-001, 002 |
| RISK-13 | Outbox pattern | Eventual consistency custo total | `custoTotalPorAtivo` desatualizado | Implementar outbox + reconciliação | US-MAN-004 |
| RISK-14 | Depreciação linear | Método não confirmado | Cálculo TCO incorreto | Validar com Financeiro | US-REL-001 |
| RISK-15 | Escopo visibilidade | USER vê apenas sua filial? | Vazamento dados, UX | Definir com PO | US-ATIVO-002, US-MAN-005, US-MON-003 |
| RISK-16 | AlertNotificationService | Complexidade 17 — **CRÍTICO** | Taxa erro 5xx, confiabilidade alertas | US-TECH-003 (refatorar Q2) | US-MON-001, 003, 004 |
| RISK-17 | Particionamento health check | `ativo_detalhe_hardware` crescimento | Performance consultas | Decidir: por data? por ativo? | US-MON-001, 002 |
| RISK-18 | Role HEALTH_COLLECTOR | RBAC granular para health check | Segurança endpoint sistema | Definir role + permissões | US-MON-001 |
| RISK-19 | Retenção health checks | 13 meses — policy não definida | Compliance, storage | Definir política + job purge | US-MON-002 |
| RISK-20 | Deduplicação alertas | Mesmo ativo+tipo janela 1h | Storm de alertas | Implementar no service refatorado | US-MON-003, 004 |
| RISK-21 | Notificação falha | SMTP down | Alertas não chegam | NotificationOutbox + retry exponencial | US-MON-001, 003, 004 |
| RISK-22 | Timeout job | > 5 min scheduler | Job incompleto | Batches 100 + @Async | US-MON-004 |
| RISK-23 | Depreciação linear | Assunção não validada | TCO incorreto | Validar com Financeiro | US-REL-001 |
| RISK-24 | Permissão relatório | Roles exatas não definidas | Acesso indevido/negado | Definir `relatorio:custo-total` | US-REL-001 |
| RISK-25 | Views materializadas | Queries não definidas | Dashboard não carrega | Modelar views + refresh job | US-REL-002 |
| RISK-26 | Permissão dashboard | Por filial? | Vazamento dados gestão | Definir com PO | US-REL-002 |
| RISK-27 | Hash encadeado vs assinatura | Integridade audit_log | Decisão arquitetura segurança | Avaliar com Segurança | US-AUD-001 |
| RISK-28 | Retenção/volume audit_log | 7 anos SOX, 5 LGPD, TB de logs | Storage, performance | Particionamento + tiering S3 Glacier | US-AUD-001 |
| RISK-29 | Sampling health checks | Volume alto auditoria | Perda rastreabilidade | Definir política sampling | US-AUD-001 |
| RISK-30 | Seeder quality gate | Complexidade 15 | CI falha | US-TECH-007 | Nenhuma (apenas testes) |

---

## Legenda de Marcadores Utilizados
* **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**: Hipótese baseada no contexto do BRD/código, não explicitada nas fontes
* **GAP CRÍTICO / BLOQUEANTE / BUG BLOQUEANTE**: Item que impede funcionamento correto ou conformidade
* **BR-XX**: Referência direta às Business Rules do BRD (Seção 6)
* **Complexidade ciclomática**: Conforme diagnóstico determinístico (AST)
* **NFR-XX**: Non-Functional Requirements derivados dos guardrails do BRD e requisitos dos Use Cases
* **RISK-XX**: Riscos técnicos identificados no diagnóstico e nos Use Cases