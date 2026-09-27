# Use Case Specification: UC-01 — Cadastrar Ativo

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio
* **Secondary Actors:** Sistema de Auditoria (log imutável), Banco de Dados
* **Stakeholders & Interests:** Gestor de Patrimônio (cadastro centralizado), Auditor (trilha de custódia), Admin (controle de permissões)
* **Trigger:** Gestor de Patrimônio acessa tela "Novo Ativo" e submete formulário com dados do ativo
* **Preconditions:** 
  - Usuário autenticado com papel `ADMIN` (BR-01)
  - Tipos de ativo, departamentos, filiais e fornecedores já cadastrados
  - Sessão válida com token JWT (BR-08)
* **Postconditions (Success Guarantee):** 
  - Ativo persistido com ID gerado
  - Log de auditoria registrado com `acao=criar`, `usuario_id`, `timestamp`, `valores_novos` (BR-02)
  - Resposta `201 Created` com DTO do ativo criado
* **Scope:** Módulo Gestão de Ativos — Backend Java (`AtivoService`, `AtivoMapper`, `AtivoRepository`)
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Gestor de Patrimônio preenche formulário: código patrimonial, descrição, tipo, localização, departamento, filial, fornecedor, data aquisição, valor, vida útil, status
2. Frontend envia `POST /api/ativos` com payload JSON via serviço `request` (frontend/src/services/api.js)
3. `authInterceptor` injeta token JWT no header Authorization (BR-08)
4. Backend valida permissão: `isAdmin()` retorna `true` para usuário ADMIN (BR-01)
5. Backend valida dados de entrada: campos obrigatórios, formatos, referências existentes (BR-03)
6. `AtivoMapper.toEntity(dto)` converte DTO para entidade (BR-07: `toEntity_deveMapearDTOparaEntidade`)
7. `AtivoRepository.save(entity)` persiste no banco
8. Interceptor de auditoria registra log: `usuario_id`, `timestamp`, `entidade=Ativo`, `entidade_id`, `acao=criar`, `valores_anteriores=null`, `valores_novos=entity` (BR-02)
9. `AtivoMapper.toDTO(entity)` converte entidade para DTO de resposta (BR-07: `toDTO_deveTransformarEntidadeEmDTO`)
10. Backend retorna `201 Created` com DTO do ativo
11. Frontend exibe toast de sucesso e redireciona para lista de ativos

## 3. Alternative Scenarios
### AS-1: Usuário USER tenta cadastrar ativo
* **Ponto de extensão:** Passo 4 (validação de permissão)
* **Passos:**
  1. `isAdmin()` retorna `false`
  2. Backend retorna `403 Forbidden` (BR-01: `criar_comUser_deveRetornarForbidden`)
  3. Frontend `handleApiError` exibe mensagem "Acesso negado: apenas administradores podem criar ativos"
* **Retorno ao fluxo principal:** Não, encerra

### AS-2: Código patrimonial duplicado
* **Ponto de extensão:** Passo 5 (validação de dados)
* **Passos:**
  1. Validação de unicidade falha (constraint unique no banco)
  2. Backend retorna `409 Conflict` com mensagem "Código patrimonial já cadastrado"
  3. Frontend destaca campo em vermelho com mensagem de erro
* **Retorno ao fluxo principal:** Sim, no passo 1 (usuário corrige e reenvia)

## 4. Exception Scenarios
### EX-1: Dados inválidos no payload
* **Ponto de extensão:** Passo 5
* **Condição de erro:** Campo obrigatório ausente, formato inválido (ex: data futura), referência inexistente (tipo, filial, etc.)
* **Tratamento:** Backend retorna `400 Bad Request` com detalhes de validação (BR-03: `criar_comDadosInvalidos_deveRetornarBadRequest`); `handleApiError` no frontend exibe erros por campo; log de erro estruturado no backend

### EX-2: Falha de conexão com banco de dados
* **Ponto de extensão:** Passo 7
* **Condição de erro:** Timeout, connection pool esgotado, constraint violation não tratada
* **Tratamento:** Backend retorna `500 Internal Server Error`; `handleApiError` exibe "Erro interno, tente novamente"; alerta enviado para equipe de infraestrutura via monitoramento; transação rolada automaticamente

### EX-3: Falha ao registrar log de auditoria
* **Ponto de extensão:** Passo 8
* **Condição de erro:** Storage WORM indisponível, disco cheio
* **Tratamento:** Transação principal (cadastro do ativo) **não** é rolada (auditoria é best-effort); erro crítico logado em sistema de observabilidade; alerta `CRITICAL` disparado para equipe de segurança/compliance; job de reconciliação posterior preenche gap

## 5. Business Rules Envolvidas
* **BR-01 (RBAC Estrito):** Apenas ADMIN pode criar ativos; USER recebe 403
* **BR-02 (Trilha de Auditoria Obrigatória):** Log imutável com todos os campos obrigatórios
* **BR-03 (Validação de Entrada):** 400 para dados inválidos
* **BR-07 (Mapper Null-Safe):** `toEntity` retorna null para DTO nulo; `toDTO` padroniza resposta

## 6. Non-Functional Requirements Relevantes
* Latência P95 do endpoint `POST /api/ativos` ≤ 500ms (alinhado com guardrail do BRD)
* Disponibilidade 99.9% para operações de escrita em horário comercial
* Log de auditoria gravado em storage WORM com latência < 100ms

## 7. Frequency of Use
Média — estimada em 50-200 criações/dia em operação plena (12.000+ ativos corporativos)

## 8. Assumptions
* Banco de dados relacional suporta transações ACID e constraints de unicidade
* Token JWT válido e não expirado (refresh tratado pelo `authInterceptor`)
* Agendador de health checks não interfere na criação de ativos

## 9. Open Issues
* Definir se código patrimonial é gerado automaticamente ou informado pelo usuário
* Validar necessidade de aprovação em dois níveis para ativos acima de valor threshold (ex: > R$ 50.000)
* Confirmar estratégia de migração de ativos legados (planilhas) — carga inicial vs. cadastro manual

## 10. Related Artifacts
* **User Stories:** US-ATIVO-001, US-ATIVO-002
* **User Flow:** user-flows.md#fluxo-cadastro-ativo
* **API envolvida:** api-specification.md#post-apiativos
* **Entidades:** Ativo, TipoAtivo, Localizacao, Departamento, Filial, Fornecedor
* **Mappers:** AtivoMapper (complexidade ciclomática 14 — refatorar antes de Q2)

---

# Use Case Specification: UC-02 — Listar Ativos

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio, Analista de Manutenção, Usuário Final, Auditor
* **Secondary Actors:** Banco de Dados, Cache (se implementado)
* **Stakeholders & Interests:** Todos os perfis precisam visualizar ativos com filtros; Auditor precisa de lista completa para conferência
* **Trigger:** Usuário acessa tela "Ativos" ou dashboard com lista paginada
* **Preconditions:** 
  - Usuário autenticado (qualquer papel — BR-01: `listarTodos_comUser_deveRetornarOk`)
  - Sessão válida (BR-08)
* **Postconditions (Success Guarantee):** 
  - Lista paginada de ativos retornada (DTOs padronizados via `AtivoMapper.toDTO`)
  - Filtros aplicados corretamente
  - Resposta `200 OK` com metadados de paginação
* **Scope:** Módulo Gestão de Ativos — `AtivoService.listarTodos`, `AtivoRepository.findAll`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Usuário acessa tela de ativos; frontend carrega primeira página (page=0, size=20)
2. Frontend envia `GET /api/ativos?page=0&size=20&sort=codigoPatrimonial,asc` via `request`
3. `authInterceptor` injeta token
4. Backend valida permissão: `listarTodos` permitido para ADMIN e USER (BR-01)
5. `AtivoRepository.findAll(Pageable)` executa query com paginação e ordenação
6. `AtivoMapper.toDTO` converte cada entidade para DTO (BR-07)
7. Backend retorna `200 OK` com `{ content: [...], totalElements, totalPages, number, size }`
8. Frontend renderiza tabela com paginação, ordenação e ações por linha

## 3. Alternative Scenarios
### AS-1: Filtros aplicados (busca por termo, status, filial, departamento, tipo)
* **Ponto de extensão:** Passo 2
* **Passos:**
  1. Frontend adiciona query params: `?search=notebook&status=ATIVO&filialId=3&tipoId=2`
  2. Backend delega para `AtivoSpecification` (similar a `ManutencaoSpecification`) para construir query dinâmica
  3. Resultado filtrado retornado paginado
* **Retorno ao fluxo principal:** Sim, fluxo principal continua no passo 5

### AS-2: Exportação para CSV/Excel
* **Ponto de extensão:** Passo 1 (botão "Exportar")
* **Passos:**
  1. Frontend envia `GET /api/ativos/export?format=csv&filtros...`
  2. Backend stream resposta com `Content-Type: text/csv` sem paginação (todos os registros filtrados)
  3. Frontend dispara download do arquivo
* **Retorno ao fluxo principal:** Não, fluxo paralelo

## 4. Exception Scenarios
### EX-1: Parâmetros de paginação inválidos
* **Ponto de extensão:** Passo 4
* **Condição de erro:** `page < 0`, `size > 100` (limite máximo), `sort` em campo inexistente
* **Tratamento:** `400 Bad Request` com mensagem descritiva; frontend corrige para defaults (page=0, size=20)

### EX-2: Timeout em query complexa com muitos filtros
* **Ponto de extensão:** Passo 5
* **Condição de erro:** Query excede `statement_timeout` do banco (ex: 30s)
* **Tratamento:** `504 Gateway Timeout`; sugestão ao usuário para reduzir escopo de filtros; índice de banco revisado pela equipe de DBA

## 5. Business Rules Envolvidas
* **BR-01 (RBAC Estrito):** Leitura permitida para ADMIN e USER
* **BR-07 (Mapper Null-Safe):** `toDTO` padroniza resposta; lista vazia retorna array vazio (não null)

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 300ms para primeira página (dados quentes em cache)
* Suportar 10.000+ ativos com paginação server-side
* Exportação de até 50.000 registros em < 10s (streaming)

## 7. Frequency of Use
Alta — usada múltiplas vezes por dia por todos os perfis; tela principal do sistema

## 8. Assumptions
* Índices adequados em `codigo_patrimonial`, `status`, `filial_id`, `tipo_id`, `departamento_id`
* Frontend implementa debounce em busca textual (300ms)

## 9. Open Issues
* Definir se USER vê apenas ativos de sua filial/departamento (filtro implícito por escopo) ou todos
* Avaliar cache Redis para listas frequentes (ex: dashboard)

## 10. Related Artifacts
* **User Stories:** US-ATIVO-003, US-ATIVO-004
* **User Flow:** user-flows.md#fluxo-listagem-ativos
* **API envolvida:** api-specification.md#get-apiativos
* **Specifications:** AtivoSpecification (a criar, seguindo padrão ManutencaoSpecification)

---

# Use Case Specification: UC-03 — Buscar Ativo por ID

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio, Analista de Manutenção, Auditor
* **Secondary Actors:** Banco de Dados
* **Stakeholders & Interests:** Detalhamento completo do ativo para edição, manutenção, auditoria
* **Trigger:** Usuário clica em linha da tabela ou acessa URL direta `/ativos/{id}`
* **Preconditions:** 
  - Usuário autenticado (qualquer papel — leitura permitida BR-01)
  - ID do ativo conhecido (UUID ou Long)
* **Postconditions (Success Guarantee):** 
  - DTO completo do ativo retornado com relacionamentos (tipo, localização, departamento, filial, fornecedor, health checks recentes)
  - Resposta `200 OK`
* **Scope:** Módulo Gestão de Ativos — `AtivoService.buscarPorId`, `AtivoRepository.findById`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend envia `GET /api/ativos/{id}` via `request`
2. `authInterceptor` injeta token
3. Backend valida permissão: leitura permitida para ADMIN e USER (BR-01)
4. `AtivoRepository.findById(id)` busca entidade
5. Se não encontrado: lança exceção `EntityNotFoundException` → `404 Not Found` (BR-04: `buscarPorId_comIdInexistente_deveRetornarNotFound`)
6. `AtivoMapper.toDTO(entity)` converte com relacionamentos carregados (LAZY/EAGER conforme fetch plan)
7. Backend retorna `200 OK` com DTO detalhado
8. Frontend renderiza modal/detalhe com abas: Geral, Health Checks, Manutenções, Auditoria, Depreciação

## 3. Alternative Scenarios
### AS-1: Ativo com health checks recentes — exibir indicadores visuais
* **Ponto de extensão:** Passo 6
* **Passos:**
  1. Mapper inclui `ultimoHealthCheck` (via subquery ou fetch join)
  2. Frontend exibe badges: 🟢 Saudável, 🟡 Atenção, 🔴 Crítico baseados em thresholds (BR-05)
* **Retorno ao fluxo principal:** Sim, passo 7

## 4. Exception Scenarios
### EX-1: ID inexistente ou inválido (formato)
* **Ponto de extensão:** Passo 4-5
* **Condição de erro:** UUID malformado, ID numérico não encontrado
* **Tratamento:** `404 Not Found` padronizado (BR-04); frontend exibe "Ativo não encontrado" com botão "Voltar à lista"

### EX-2: Erro de lazy loading (proxy não inicializado fora de transação)
* **Ponto de extensão:** Passo 6
* **Condição de erro:** `LazyInitializationException` ao acessar relacionamento não fetchado
* **Tratamento:** `@Transactional(readOnly=true)` no service method; fetch plan explícito via `EntityGraph` ou DTO projection; log de erro para correção de mapeamento

## 5. Business Rules Envolvidas
* **BR-01 (RBAC Estrito):** Leitura permitida para ADMIN e USER
* **BR-04 (Busca Segura):** 404 para ID inexistente
* **BR-07 (Mapper Null-Safe):** `toDTO` transforma entidade em DTO padronizado

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 200ms (single entity lookup by PK)
* Consistência forte (read-after-write) para edição subsequente

## 7. Frequency of Use
Alta — cada edição, manutenção ou auditoria dispara busca por ID

## 8. Assumptions
* ID é chave primária indexada (PK)
* DTO inclui campos calculados: `idadeAtivo`, `valorResidual`, `proximaManutencao`

## 9. Open Issues
* Definir se DTO inclui histórico completo de health checks ou apenas último (performance)
* Avaliar projeção SQL nativa (JPQL `new DTO(...)`) vs. entity + mapper para performance

## 10. Related Artifacts
* **User Stories:** US-ATIVO-005
* **User Flow:** user-flows.md#fluxo-detalhe-ativo
* **API envolvida:** api-specification.md#get-apiativos-id

---

# Use Case Specification: UC-04 — Atualizar Ativo

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio
* **Secondary Actors:** Sistema de Auditoria, Banco de Dados
* **Stakeholders & Interests:** Gestor (correção de dados), Auditor (trilha de alterações), Admin (controle de permissão)
* **Trigger:** Usuário edita ativo na tela de detalhe e clica "Salvar"
* **Preconditions:** 
  - Usuário autenticado com papel `ADMIN` (BR-01: `atualizar_comUser_deveRetornarForbidden`)
  - Ativo existe (validado via `buscarPorId`)
  - Sessão válida (BR-08)
* **Postconditions (Success Guarantee):** 
  - Ativo atualizado no banco
  - Log de auditoria com `acao=atualizar`, `valores_anteriores`, `valores_novos` (BR-02)
  - Resposta `200 OK` com DTO atualizado
* **Scope:** Módulo Gestão de Ativos — `AtivoService.atualizar`, `AtivoMapper`, `AtivoRepository`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend envia `PUT /api/ativos/{id}` com payload parcial ou completo via `request`
2. `authInterceptor` injeta token
3. Backend valida permissão: `isAdmin()` → `true` (BR-01)
5. `AtivoRepository.findById(id)` carrega entidade atual (para auditoria e merge)
6. Validação de entrada: campos modificados válidos, referências existentes (BR-03)
7. `AtivoMapper.toEntity(dto)` para merge (BR-07) — **atenção**: mapper tem complexidade 14, refatorar
8. `AtivoRepository.save(entity)` persiste alterações
9. Interceptor de auditoria registra log com diff: `valores_anteriores` (snapshot antes), `valores_novos` (depois) (BR-02)
10. `AtivoMapper.toDTO(entity)` para resposta
11. Backend retorna `200 OK` com DTO atualizado
12. Frontend atualiza cache local e exibe toast de sucesso

## 3. Alternative Scenarios
### AS-1: Usuário USER tenta atualizar
* **Ponto de extensão:** Passo 3
* **Passos:** `403 Forbidden` (BR-01: `atualizar_comUser_deveRetornarForbidden`)
* **Retorno ao fluxo principal:** Não

### AS-2: Atualização parcial (PATCH semântico via PUT)
* **Ponto de extensão:** Passo 1
* **Passos:** Frontend envia apenas campos alterados; backend faz merge seletivo preservando campos não enviados
* **Retorno ao fluxo principal:** Sim, fluxo principal

## 4. Exception Scenarios
### EX-1: Conflito de concorrência otimista (versão desatualizada)
* **Ponto de extensão:** Passo 8
* **Condição de erro:** `@Version` entity mismatch — outro usuário alterou simultaneamente
* **Tratamento:** `409 Conflict` com mensagem "Ativo foi alterado por outro usuário. Recarregue e tente novamente."; frontend recarrega `buscarPorId` e reapresenta formulário com diff

### EX-2: Dados inválidos (ex: filial inexistente, valor negativo)
* **Ponto de extensão:** Passo 6
* **Condição de erro:** Validação Bean Validation (`@Valid`) falha
* **Tratamento:** `400 Bad Request` com `FieldError[]` (BR-03); frontend destaca campos

### EX-3: Falha de auditoria (storage WORM indisponível)
* **Ponto de extensão:** Passo 9
* **Condição de erro:** Igual UC-01 EX-3
* **Tratamento:** Mesma estratégia — transação principal commitada, auditoria best-effort com alerta crítico

## 5. Business Rules Envolvidas
* **BR-01 (RBAC Estrito):** Apenas ADMIN pode atualizar
* **BR-02 (Trilha de Auditoria):** Log com diff completo
* **BR-03 (Validação de Entrada):** 400 para inválidos
* **BR-07 (Mapper Null-Safe):** Conversão bidirecional

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 400ms (inclui busca + merge + save + audit)
* Concorrência otimista obrigatória (`@Version`)

## 7. Frequency of Use
Média — alterações cadastrais menos frequentes que consultas

## 8. Assumptions
* Campos imutáveis após criação: `codigoPatrimonial`, `dataAquisicao` (validar no service)
* Auditoria captura apenas campos de negócio (não campos técnicos: `createdAt`, `version`)

## 9. Open Issues
* Definir lista de campos imutáveis vs. mutáveis
* Avaliar se mudança de filial/departamento dispara notificação para gestores afetados

## 10. Related Artifacts
* **User Stories:** US-ATIVO-006
* **User Flow:** user-flows.md#fluxo-edicao-ativo
* **API envolvida:** api-specification.md#put-apiativos-id
* **Refatoração:** AtivoMapper (complexidade 14) — quebrar em métodos menores por seção (identificação, localização, financeiro, técnico)

---

# Use Case Specification: UC-05 — Excluir Ativo (Baixa Patrimonial)

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio
* **Secondary Actors:** Sistema de Auditoria, Banco de Dados
* **Stakeholders & Interests:** Gestor (baixa formal), Auditor (trilha de baixa), Compliance (LGPD/SOX)
* **Trigger:** Gestor inicia processo de baixa do ativo (fim de vida útil, perda, roubo, doação)
* **Preconditions:** 
  - Usuário autenticado com papel `ADMIN` (BR-01: `deletar_comUser_deveRetornarForbidden`)
  - Ativo existe e está em status passível de baixa (ex: `ATIVO`, `EM_MANUTENCAO` — não `BAIXADO`)
  - Nenhuma manutenção em andamento (`status != EM_ANDAMENTO`) — regra de negócio implícita
  - Sessão válida (BR-08)
* **Postconditions (Success Guarantee):** 
  - Ativo marcado como `BAIXADO` (soft delete) ou removido (hard delete — definir)
  - Log de auditoria com `acao=deletar`/`acao=baixar`, motivo, `valores_anteriores` (BR-02)
  - Resposta `200 OK` ou `204 No Content`
* **Scope:** Módulo Gestão de Ativos — `AtivoService.deletar` / `baixar`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Gestor clica "Baixar Ativo" na tela de detalhe; modal solicita: motivo (obrigatório), data baixa, valor residual, documento comprovante (upload opcional)
2. Frontend envia `DELETE /api/ativos/{id}` ou `POST /api/ativos/{id}/baixa` com payload `{ motivo, dataBaixa, valorResidual, documentoId }`
3. `authInterceptor` injeta token
4. Backend valida permissão: `isAdmin()` → `true` (BR-01)
5. `AtivoRepository.findById(id)` carrega entidade
6. Validações de negócio: status permite baixa, sem manutenções abertas
7. Atualiza status para `BAIXADO`, preenche `dataBaixa`, `motivoBaixa`, `valorResidual`
8. `AtivoRepository.save(entity)`
9. Auditoria: log com `acao=baixar`, `valores_anteriores` (status anterior), `valores_novos` (dados de baixa) (BR-02)
10. Backend retorna `200 OK` com DTO do ativo baixado
11. Frontend remove da lista ativa (filtro padrão `status != BAIXADO`) e exibe toast

## 3. Alternative Scenarios
### AS-1: Soft delete vs. Hard delete
* **Ponto de extensão:** Passo 7
* **Passos:** Se hard delete (LGPD — ativo com dados sensíveis irrecuperáveis): `repository.delete(entity)`; cascata remove health checks (BR-10: `deleteByAtivoDetalheHardwareId`), manutenções (restringir se houver histórico?)
* **Retorno ao fluxo principal:** Decisão arquitetural — documentar no BRD

### AS-2: Usuário USER tenta excluir
* **Ponto de extensão:** Passo 4
* **Passos:** `403 Forbidden` (BR-01: `deletar_comUser_deveRetornarForbidden`)
* **Retorno ao fluxo principal:** Não

## 4. Exception Scenarios
### EX-1: Ativo com manutenção em andamento
* **Ponto de extensão:** Passo 6
* **Condição de erro:** Existe `Manutencao` com `status=EM_ANDAMENTO` para este ativo
* **Tratamento:** `409 Conflict` — "Não é possível baixar ativo com manutenção em andamento. Conclua ou cancele a manutenção primeiro."

### EX-2: Ativo já baixado
* **Ponto de extensão:** Passo 6
* **Condição de erro:** `status == BAIXADO`
* **Tratamento:** `400 Bad Request` — "Ativo já possui baixa registrada em {dataBaixa}"

### EX-3: Falha ao remover health checks em cascata (hard delete)
* **Ponto de extensão:** Passo 7 (AS-1 hard delete)
* **Condição de erro:** Constraint violation, lock timeout
* **Tratamento:** `500 Internal Server Error`; transação rolada; alerta para DBA; BR-10 (`deleteByAtivoDetalheHardwareId`) deve ser transacional

## 5. Business Rules Envolvidas
* **BR-01 (RBAC Estrito):** Apenas ADMIN
* **BR-02 (Trilha de Auditoria):** Log de baixa com motivo e valor residual
* **BR-10 (Limpeza de Hardware):** Cascata em health checks se hard delete

## 6. Non-Functional Requirements Relevantes
* Operação irreversível — confirmação em duas etapas no frontend (modal + digitar "CONFIRMO")
* Auditoria imutável obrigatória (WORM)

## 7. Frequency of Use
Baixa — estimada em 5-20 baixas/mês

## 8. Assumptions
* Soft delete é padrão (status `BAIXADO`); hard delete apenas sob solicitação de Compliance/LGPD
* Valor residual calculado automaticamente se não informado (depreciação linear até data baixa)

## 9. Open Issues
* **CRÍTICO:** Definir soft vs. hard delete — impacto em LGPD, auditoria, relatórios históricos
* Regra de negócio: permitir reativação de ativo baixado? (fluxo de "reverter baixa")
* Integração com contabilidade: baixa contábil automática via `custoTotalPorAtivo` final

## 10. Related Artifacts
* **User Stories:** US-ATIVO-007, US-ATIVO-008
* **User Flow:** user-flows.md#fluxo-baixa-ativo
* **API envolvida:** api-specification.md#delete-apiativos-id ou #post-apiativos-id-baixa
* **BR-10:** deleteByAtivoDetalheHardwareId (cascata health checks)

---

# Use Case Specification: UC-06 — Gerenciar Tipos de Ativo

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Banco de Dados, Auditoria
* **Stakeholders & Interests:** Admin (catálogo padronizado), Gestor (classificação consistente), Analista (filtros por tipo)
* **Trigger:** Admin acessa "Configurações > Tipos de Ativo" e cria/edita/exclui tipos
* **Preconditions:** Usuário ADMIN autenticado (BR-01: entidades mestres só ADMIN)
* **Postconditions (Success Guarantee):** Tipo persistido/atualizado/removido; log de auditoria (BR-02)
* **Scope:** Módulo Configurações — `TipoAtivoService`, `TipoAtivoRepository`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin preenche: nome (ex: "Notebook", "Impressora", "Mesa"), descrição, categoria (HARDWARE, MOBILIARIO, EQUIPAMENTO), vida útil padrão (meses), exige health check (boolean)
2. Frontend `POST /api/tipos-ativo` (criar) / `PUT /api/tipos-ativo/{id}` (editar) / `DELETE /api/tipos-ativo/{id}` (excluir)
3. Validação permissão ADMIN (BR-01)
4. Validação unicidade nome (BR-03)
5. Persistência + auditoria (BR-02)
6. Retorno `201`/`200`/`204` com DTO

## 3. Alternative Scenarios
### AS-1: Exclusão de tipo com ativos associados
* **Ponto de extensão:** DELETE
* **Condição:** Existem ativos com `tipoAtivoId = id`
* **Tratamento:** `409 Conflict` — "Não é possível excluir tipo com ativos cadastrados. Reclassifique os ativos primeiro." (soft delete do tipo: `ativo=false` como alternativa)

## 4. Exception Scenarios
### EX-1: Nome duplicado
* **Tratamento:** `400 Bad Request` (BR-03)

## 5. Business Rules Envolvidas
* **BR-01:** Apenas ADMIN (entidade mestra)
* **BR-02:** Auditoria em criar/atualizar/deletar
* **BR-03:** Validação entrada
* **BR-07:** Mapper null-safe

## 6. Non-Functional Requirements Relevantes
* Baixa frequência — cacheável no frontend (atualizar a cada 1h ou on-demand)

## 7. Frequency of Use
Baixa — configuração inicial + ocasionais

## 8. Assumptions
* Vida útil padrão usada para sugestão de depreciação no cadastro de ativo
* `exigeHealthCheck=true` apenas para categoria HARDWARE

## 9. Open Issues
* Permitir tipos personalizados por filial? (hoje global)

## 10. Related Artifacts
* **API:** api-specification.md#tipos-ativo
* **Entidade:** TipoAtivo

---

# Use Case Specification: UC-07 — Gerenciar Localizações

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Banco de Dados, Auditoria
* **Stakeholders & Interests:** Admin (hierarquia física), Gestor (alocação precisa), Auditor (rastreabilidade física)
* **Trigger:** Admin gerencia prédios, andares, salas, posições
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Localização persistida; auditoria
* **Scope:** Módulo Configurações — `LocalizacaoService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin cria hierarquia: Prédio A > Andar 2 > Sala 201 > Posição A1
2. `POST /api/localizacoes` com `{ nome, tipo: PREDIO|ANDAR|SALA|POSICAO, parentId }`
3. Validação ADMIN + unicidade por nível + parentId existe
4. Persistência + auditoria
5. Frontend atualiza tree view

## 3. Alternative Scenarios
### AS-1: Mover subárvore (alterar parentId)
* **Tratamento:** Validação de ciclos; atualização em cascata de `path` materializado

## 4. Exception Scenarios
### EX-1: Excluir localização com ativos alocados
* **Tratamento:** `409 Conflict` — mover ativos primeiro

## 5. Business Rules Envolvidas
* **BR-01, BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Tree renderização < 200ms para 500+ nós (lazy loading)

## 7. Frequency of Use
Baixa

## 8. Assumptions
* Path materializado (`/predio-a/andar-2/sala-201/posicao-a1`) para queries rápidas de "ativos na sala 201 e filhas"

## 9. Open Issues
* Integração com planta baixa (SVG/imagem) — futuro

## 10. Related Artifacts
* **API:** api-specification.md#localizacoes

---

# Use Case Specification: UC-08 — Registrar Health Check de Hardware

## 1. Characterization
* **Primary Actor:** Agendador Externo (cron, Spring @Scheduled, Airflow) — **sistema, não humano**
* **Secondary Actors:** Agente de Coleta (script no ativo), Banco de Dados, AlertNotificationService
* **Stakeholders & Interests:** Analista de Manutenção (dados frescos), Gestor (visibilidade saúde), Alertas (disparo automático)
* **Trigger:** Agendador invoca `POST /api/ativos/{id}/health-check` periodicamente (ex: a cada 15 min) OU agente envia dados via endpoint idempotente
* **Preconditions:** 
  - Ativo existe e é do tipo HARDWARE (`tipoAtivo.categoria = HARDWARE` e `tipoAtivo.exigeHealthCheck = true`)
  - Endpoint idempotente (mesmo payload reenviado não duplica — BR-10)
  - Token de serviço (service account) com permissão de escrita (role `HEALTH_COLLECTOR` — a definir)
* **Postconditions (Success Guarantee):** 
  - Health check persistido com timestamp, adaptadores de rede, discos, memórias
  - Dados anteriores do mesmo ativo removidos em cascata (BR-10: `deleteByAtivoDetalheHardwareId`)
  - `AlertNotificationService.checkResourceUsageAlerts` avalia thresholds (BR-05) e gera alertas se necessário
  - Resposta `200 OK` ou `201 Created`
* **Scope:** Módulo Monitoramento — `HealthCheckService`, `AtivoDetalheHardwareRepository`, `AlertNotificationService` (complexidade 17 — refatorar)
* **Level:** Subfunction (system-triggered)

## 2. Main Scenario (Happy Path)
1. Agendador/agente envia `POST /api/ativos/{id}/health-check` com payload:
   ```json
   {
     "timestamp": "2025-01-15T10:30:00Z",
     "adaptadoresRede": [{ "nome": "eth0", "ip": "192.168.1.50", "mac": "00:11:22:33:44:55", "latenciaMs": 12, "status": "UP" }],
     "discos": [{ "device": "/dev/sda1", "mountPoint": "/", "totalGb": 500, "usadoGb": 380, "percentualUso": 76 }],
     "memorias": [{ "totalGb": 16, "usadoGb": 12, "percentualUso": 75 }]
   }
   ```
2. `authInterceptor` valida token de serviço
3. Backend valida: ativo existe, é HARDWARE, exige health check
4. `deleteByAtivoDetalheHardwareId(ativoId)` remove health check anterior (BR-10) — adaptadores, discos, memórias em cascata
5. `HealthCheckMapper.toEntity(dto)` cria novo `AtivoDetalheHardware` com collections
6. `AtivoDetalheHardwareRepository.save(entity)` persiste
7. **Assíncrono (evento):** `AlertNotificationService.checkResourceUsageAlerts(ativoId)` avalia:
   - Disco > 85% → alerta `DISCO_CRITICO`
   - Memória > 90% → alerta `MEMORIA_CRITICA`
   - Latência rede > 100ms → alerta `REDE_LENTA`
   - Thresholds configuráveis por tipo de ativo (BR-05)
8. Alertas persistidos + notificações (email, push, webhook) — fora de escopo detalhado aqui
9. Backend retorna `200 OK` com resumo: `{ alertasGerados: 2, healthCheckId: 123 }`

## 3. Alternative Scenarios
### AS-1: Health check parcial (apenas discos)
* **Ponto de extensão:** Passo 1
* **Passos:** Payload contém apenas `discos`; backend faz merge seletivo? **Não** — BR-10 exige limpeza total e re-registro completo para evitar dados órfãos. Payload deve ser completo.
* **Retorno ao fluxo principal:** Validação rejeita payload incompleto (`400 Bad Request`)

### AS-2: Ativo não é hardware ou não exige health check
* **Ponto de extensão:** Passo 3
* **Tratamento:** `400 Bad Request` — "Health check não aplicável para este tipo de ativo"

## 4. Exception Scenarios
### EX-1: Payload inválido / schema mismatch
* **Ponto de extensão:** Passo 3
* **Condição:** Campos obrigatórios ausentes, tipos errados, arrays vazios
* **Tratamento:** `400 Bad Request` com detalhes (BR-03); agente registra falha local e reenvia no próximo ciclo

### EX-2: Timeout / falha no AlertNotificationService (complexidade 17)
* **Ponto de extensão:** Passo 7
* **Condição:** `checkResourceUsageAlerts` excede tempo, lança exceção
* **Tratamento:** **Não** rolar transação do health check (dado bruto salvo). Erro capturado, logado, alerta `ALERTA_PROCESSAMENTO_FALHA` gerado para equipe. Job de reprocessamento posterior reavalia health checks sem alerta.

### EX-3: Agendador falha / ativo sem health check há > 2h
* **Ponto de extensão:** Fora do fluxo (monitoramento de ausência)
*   **Tratamento:** Job separado `HealthCheckStalenessChecker` consulta `getHealthHistory` (último registro) e gera alerta `HEALTH_CHECK_AUSENTE` se > threshold configurável (ex: 2h). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 5. Business Rules Envolvidas
* **BR-05 (Health Check de Hardware):** Thresholds disco 85%, memória 90%, rede 100ms; coleta periódica
* **BR-10 (Limpeza de Hardware):** `deleteByAtivoDetalheHardwareId` cascata antes de novo registro
* **BR-02 (Auditoria):** Log de `criar` health check (opcional — volume alto; avaliar sampling)

## 6. Non-Functional Requirements Relevantes
* **Idempotência:** Mesmo payload reenviado → mesmo resultado (upsert semântico via delete+insert)
* **Throughput:** Suportar 10.000 ativos × 4 checks/hora = ~11 req/s sustentado
* **Latência P95:** ≤ 1s (inclui avaliação de alertas)
* **Disponibilidade:** 99.95% — endpoint crítico para monitoramento

## 7. Frequency of Use
Muito alta — contínua, 24/7, por agendador externo

## 8. Assumptions
* Agente de coleta roda no próprio ativo (Windows/Linux) ou via SNMP/SSH remoto
* Token de serviço com escopo `health:write` (RBAC granular a implementar)
* Thresholds configuráveis em tabela `AlertaThreshold` por `tipoAtivoId`

## 9. Open Issues
* Definir role/permission específica para health check (`HEALTH_COLLECTOR`) vs. ADMIN
* Estratégia de particionamento da tabela `ativo_detalhe_hardware` (por data? por ativo?)
* `AlertNotificationService` complexidade 17 — **refatoração obrigatória antes de Q2** (extrair `DiskAlertEvaluator`, `MemoryAlertEvaluator`, `NetworkAlertEvaluator`, `AlertDispatcher`)

## 10. Related Artifacts
* **User Stories:** US-MON-001, US-MON-002
* **API:** api-specification.md#post-apiativos-id-health-check
* **Serviço:** AlertNotificationService.java:96 (refatorar)
* **BR-05, BR-10**

---

# Use Case Specification: UC-09 — Consultar Histórico de Health Check

## 1. Characterization
* **Primary Actor:** Analista de Manutenção, Gestor de Patrimônio
* **Secondary Actors:** Banco de Dados
* **Stakeholders & Interests:** Analista (tendências, diagnóstico), Gestor (relatórios de saúde), Auditor (evidência de monitoramento)
* **Trigger:** Usuário acessa aba "Health Check" no detalhe do ativo ou relatório "Saúde dos Ativos"
* **Preconditions:** Usuário autenticado (leitura permitida BR-01), ativo existe
* **Postconditions:** Série temporal de health checks retornada (paginada/filtrada por período)
* **Scope:** Módulo Monitoramento — `HealthCheckService.getHealthHistory`, `AtivoDetalheHardwareRepository.findHistoryByAtivoId`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend `GET /api/ativos/{id}/health-history?dataInicio=2025-01-01&dataFim=2025-01-15&page=0&size=50`
2. Validação permissão leitura (ADMIN/USER)
3. Repository busca histórico ordenado por `timestamp DESC`
4. Mapper converte para DTOs leves (timestamp, disco%, memoria%, rede ms, alertas gerados)
5. Retorno `200 OK` com lista paginada
6. Frontend renderiza gráficos de linha (disco, memória, latência) + tabela de alertas

## 3. Alternative Scenarios
### AS-1: Exportação CSV do histórico
* **Tratamento:** `GET .../export?format=csv` — streaming de todos os registros do período

## 4. Exception Scenarios
### EX-1: Ativo sem health checks
* **Tratamento:** `200 OK` com lista vazia + metadados; frontend exibe "Nenhum health check registrado"

## 5. Business Rules Envolvidas
* **BR-01:** Leitura ADMIN/USER
* **BR-04:** 404 se ativo inexistente
* **BR-07:** Mapper null-safe

## 6. Non-Functional Requirements Relevantes
* Query otimizada com índice `(ativo_id, timestamp DESC)`
* Retenção de histórico: 13 meses (configurável) — job de purge mensal

## 7. Frequency of Use
Média — consultas sob demanda + relatórios semanais

## 8. Assumptions
* Dados brutos (adaptadores, discos, memórias) não retornados no histórico — apenas métricas agregadas + alertas
* Frontend usa Chart.js ou similar para gráficos

## 9. Open Issues
* Definir política de retenção e arquivamento (cold storage)
* Avaliar materialized view para dashboards agregados (ex: % ativos saudáveis por filial)

## 10. Related Artifacts
* **API:** api-specification.md#get-apiativos-id-health-history
* **BR-05:** thresholds usados para colorir gráficos

---

# Use Case Specification: UC-10 — Gerenciar Departamentos

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Auditoria, Banco de Dados
* **Stakeholders & Interests:** Admin (estrutura organizacional), Gestor (filtro por departamento), RH (sincronização futura)
* **Trigger:** Admin em "Configurações > Departamentos"
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Departamento CRUD + auditoria
* **Scope:** Módulo Configurações — `DepartamentoService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin cria/edita/exclui: nome, código, descrição, filial pai (opcional), gestor responsável (funcionário)
2. `POST/PUT/DELETE /api/departamentos`
3. Validação ADMIN (BR-01), unicidade código por filial (BR-03)
4. Persistência + auditoria (BR-02)
5. Frontend atualiza select/tree de departamentos

## 3. Alternative Scenarios
### AS-1: Excluir departamento com ativos/funcionários
* **Tratamento:** `409 Conflict` — reatribuir primeiro; ou soft delete `ativo=false`

## 4. Exception Scenarios
### EX-1: Código duplicado na mesma filial
* **Tratamento:** `400 Bad Request`

## 5. Business Rules Envolvidas
* **BR-01, BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Cache no frontend (select options) — invalidar on mutation

## 7. Frequency of Use
Baixa

## 8. Assumptions
* Hierarquia simples (filial > departamento) — sem sub-departamentos por enquanto

## 9. Open Issues
* Sincronização com AD/LDAP futuro — mapear `codigo` para `ou` do AD

## 10. Related Artifacts
* **API:** api-specification.md#departamentos

---

# Use Case Specification: UC-11 — Gerenciar Filiais

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Auditoria, Banco de Dados
* **Stakeholders & Interests:** Admin (topologia corporativa), Gestor (escopo de ativos), Auditoria (jurisdição)
* **Trigger:** Admin em "Configurações > Filiais"
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Filial CRUD + auditoria
* **Scope:** Módulo Configurações — `FilialService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin cadastra: nome, código, endereço, CNPJ, telefone, email, responsável, timezone
2. `POST/PUT/DELETE /api/filiais`
3. Validação ADMIN, unicidade código/CNPJ
4. Persistência + auditoria
5. Frontend atualiza seletor de filial global (header)

## 3. Alternative Scenarios
### AS-1: Filial matriz vs. filiais — apenas uma matriz
* **Validação:** Regra de negócio: `count(filial where matriz=true) <= 1`

## 4. Exception Scenarios
### EX-1: Excluir filial com ativos/departamentos/usuários
* **Tratamento:** `409 Conflict` — desativar (soft delete) em vez de excluir

## 5. Business Rules Envolvidas
* **BR-01, BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Timezone da filial usado em agendamentos de health check e relatórios

## 7. Frequency of Use
Muito baixa — setup inicial + raras alterações

## 8. Assumptions
* Filial é raiz da hierarquia organizacional (departamentos, usuários, ativos pertencem a filial)

## 9. Open Issues
* Multi-moeda por filial? (hoje não — BRD não menciona)

## 10. Related Artifacts
* **API:** api-specification.md#filiais

---

# Use Case Specification: UC-12 — Gerenciar Fornecedores

## 1. Characterization
* **Primary Actor:** Administrador de Sistema, Gestor de Patrimônio
* **Secondary Actors:** Auditoria, Banco de Dados
* **Stakeholders & Interests:** Gestor (vincular ativos a fornecedor), Compras (histórico), Financeiro (custoTotalPorAtivo)
* **Trigger:** Cadastro/edição de fornecedor em "Configurações > Fornecedores"
* **Preconditions:** Usuário ADMIN (BR-01: entidade mestra)
* **Postconditions:** Fornecedor persistido + auditoria
* **Scope:** Módulo Configurações — `FornecedorService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Dados: nome, CNPJ, contato, email, telefone, endereço, site, observações, categorias (HARDWARE, MANUTENCAO, MOBILIARIO)
2. `POST/PUT/DELETE /api/fornecedores`
3. Validação ADMIN, unicidade CNPJ
4. Persistência + auditoria
5. Disponível em select no cadastro de ativo e manutenção

## 3. Alternative Scenarios
### AS-1: Fornecedor com contratos/SLA (futuro)
* **Nota:** Fora de escopo atual (BRD Out-of-Scope)

## 4. Exception Scenarios
### EX-1: CNPJ duplicado
* **Tratamento:** `400 Bad Request`

## 5. Business Rules Envolvidas
* **BR-01, BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Busca por CNPJ indexada (consulta frequente em relatórios)

## 7. Frequency of Use
Baixa

## 8. Assumptions
* Fornecedor de manutenção vinculado em `Manutencao.fornecedorId` para custoTotalPorAtivo

## 9. Open Issues
* Integração com ReceitaWS para validação CNPJ automática?

## 10. Related Artifacts
* **API:** api-specification.md#fornecedores

---

# Use Case Specification: UC-13 — Gerenciar Funcionários

## 1. Characterization
* **Primary Actor:** Administrador de Sistema, RH
* **Secondary Actors:** Auditoria, Banco de Dados, Sistema de Autenticação (criação de usuário vinculado)
* **Stakeholders & Interests:** RH (cadastro mestre), Admin (vincular usuário), Gestor (responsável por ativos/departamentos), Auditoria (custódia)
* **Trigger:** RH/Admin cadastra funcionário, opcionalmente criando usuário de sistema (`createFuncionarioAndUsuario`)
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Funcionário persistido; se `createFuncionarioAndUsuario`, usuário + token gerados; auditoria
* **Scope:** Módulo Configurações/RH — `FuncionarioService`, `UsuarioService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Dados: nome, CPF, matrícula, email corporativo, cargo, filial, departamento, gestor (outro funcionário), data admissão
2. `POST /api/funcionarios` ou `POST /api/funcionarios/com-usuario` (createFuncionarioAndUsuario)
3. Validação ADMIN, unicidade CPF/matrícula/email
4. Se com usuário: `UsuarioService.createUserAndToken(funcionario)` — gera usuário com role `USER`, token JWT retornado
5. **ATENÇÃO:** `Usuario.setUsername` em `Usuario.java:86` tem corpo vazio (stub) — **corrigir antes de produção**
6. Persistência + auditoria (BR-02)
6. Retorno `201` com DTO funcionário (+ token se criado usuário)

## 3. Alternative Scenarios
### AS-1: Atualizar filial/departamento do funcionário
* **Impacto:** Recalcular escopo de ativos visíveis (se filtro implícito por filial/departamento)

## 4. Exception Scenarios
### EX-1: CPF/matrícula/email duplicado
* **Tratamento:** `400 Bad Request` com campo específico

### EX-2: `setUsername` stub falha silenciosamente
* **Condição:** `createFuncionarioAndUsuario` cria usuário mas `username` fica null
*   **Tratamento:** **BUG CRÍTICO** — corrigir `Usuario.setUsername` antes de qualquer release; teste de regressão obrigatório

## 5. Business Rules Envolvidas
* **BR-01:** Apenas ADMIN
* **BR-02:** Auditoria
* **BR-03:** Validação
* **BR-07:** Mapper
* **BR-08:** `createUserAndToken` gera token; `authInterceptor` valida

## 6. Non-Functional Requirements Relevantes
* Token JWT expiração configurável (ex: 8h access, 30d refresh)

## 7. Frequency of Use
Baixa — onboarding/offboarding

## 8. Assumptions
* `mockLogin` apenas para testes automatizados (BR-08)
* Integração AD/LDAP futura substituirá cadastro manual

## 9. Open Issues
* **CRÍTICO:** Corrigir `Usuario.setUsername` stub (linha 86)
* Definir se funcionário inativo (demitido) mantém usuário ativo ou inativa em cascata

## 10. Related Artifacts
* **API:** api-specification.md#funcionarios
* **Código:** Usuario.java:86 (stub)

---

# Use Case Specification: UC-14 — Gerenciar Papéis (Roles)

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Auditoria, Banco de Dados
* **Stakeholders & Interests:** Admin (definir perfis), Segurança (menor privilégio)
* **Trigger:** Admin em "Configurações > Papéis"
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Role CRUD + auditoria
* **Scope:** Módulo Segurança — `RoleService`, `PermissionService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin cria roles: `ADMIN`, `GESTOR_PATRIMONIO`, `ANALISTA_MANUTENCAO`, `USUARIO_FINAL`, `AUDITOR`
2. Cada role: nome, descrição, conjunto de permissões (via `createPermission`)
3. `POST/PUT/DELETE /api/roles`
4. Validação ADMIN, unicidade nome
5. Persistência + auditoria
6. Disponível em matriz de permissões (UI)

## 3. Alternative Scenarios
### AS-1: Role padrão do sistema (não editável/excluível)
* **Tratamento:** `ADMIN` e `USER` marcados como `system=true`; `PUT/DELETE` retorna `403 Forbidden` para roles de sistema

## 4. Exception Scenarios
### EX-1: Excluir role atribuída a usuários
* **Tratamento:** `409 Conflict` — remover role dos usuários primeiro

## 5. Business Rules Envolvidas
* **BR-01:** RBAC estrito — roles definem permissões; `isAdmin()` verifica role `ADMIN`
* **BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Cache de permissões por usuário (invalidar on role change)

## 7. Frequency of Use
Muito baixa — setup inicial

## 8. Assumptions
* Permissões granulares: `ativo:create`, `ativo:read`, `ativo:update`, `ativo:delete`, `manutencao:aprovar`, `relatorio:exportar`, etc.

## 9. Open Issues
* Definir matriz completa de permissões por role (workshop com stakeholders)

## 10. Related Artifacts
* **API:** api-specification.md#roles
* **BR-01:** regras `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`

---

# Use Case Specification: UC-15 — Gerenciar Permissões

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Auditoria
* **Stakeholders & Interests:** Admin (granularidade), Segurança (menor privilégio)
* **Trigger:** Admin em "Configurações > Permissões"
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Permissão CRUD + auditoria
* **Scope:** Módulo Segurança — `PermissionService`
* **Level:** Subfunction (geralmente via UI de Roles)

## 2. Main Scenario (Happy Path)
1. Permissões são recursos + ações: `ativo:create`, `ativo:read`, `manutencao:aprovar`, `healthcheck:write`, `relatorio:custo-total`
2. `POST/PUT/DELETE /api/permissoes`
3. Validação ADMIN, unicidade `recurso:acao`
4. Persistência + auditoria
5. Associadas a roles na tela de Roles

## 3. Alternative Scenarios
* Nenhuma relevante

## 4. Exception Scenarios
### EX-1: Permissão referenciada por role
* **Tratamento:** `409 Conflict` ao deletar

## 5. Business Rules Envolvidas
* **BR-01, BR-02, BR-03, BR-07**

## 6. Non-Functional Requirements Relevantes
* Lookup de permissão em `hasPermission` deve ser O(1) — cache em memória (ConcurrentHashMap) recarregado on change

## 7. Frequency of Use
Muito baixa

## 8. Assumptions
* Permissões são estáticas (código) — não dinâmicas por instância

## 9. Open Issues
* Lista canônica de permissões a ser definida

## 10. Related Artifacts
* **API:** api-specification.md#permissoes
* **Código:** `hasPermission` (frontend/backend)

---

# Use Case Specification: UC-16 — Gerenciar Usuários

## 1. Characterization
* **Primary Actor:** Administrador de Sistema
* **Secondary Actors:** Auditoria, Funcionário (vínculo), Sistema de Autenticação
* **Stakeholders & Interests:** Admin (provisionamento), Segurança (controle de acesso), Auditoria (trilha)
* **Trigger:** Admin em "Configurações > Usuários" — criar, editar, resetar senha, ativar/desativar, atribuir roles
* **Preconditions:** Usuário ADMIN
* **Postconditions:** Usuário persistido + auditoria; token gerado se `createUserAndToken`
* **Scope:** Módulo Segurança — `UsuarioService`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Admin cria usuário: username, email, senha temporária, roles, funcionário vinculado (opcional), status ATIVO/INATIVO
2. `POST /api/usuarios` ou `POST /api/usuarios/com-token`
3. Validação ADMIN, unicidade username/email
4. Hash de senha (BCrypt), geração token se solicitado
5. Persistência + auditoria
6. Retorno `201` com DTO (sem senha) + token se aplicável

## 3. Alternative Scenarios
### AS-1: Reset de senha por admin
* **Fluxo:** `POST /api/usuarios/{id}/reset-senha` — gera senha temporária, envia email, força troca no primeiro login

### AS-2: Desativar usuário (soft delete)
* **Fluxo:** `PATCH /api/usuarios/{id}` com `{ ativo: false }` — invalida tokens ativos (BR-08: `clearSession`/`logout`)

## 4. Exception Scenarios
### EX-1: Username/email duplicado
* **Tratamento:** `400 Bad Request`

### EX-2: `setUsername` stub (Usuario.java:86)
* **Impacto:** Criação/atualização de username falha silenciosamente
*   **Tratamento:** **Corrigir antes de produção**

## 5. Business Rules Envolvidas
* **BR-01:** Apenas ADMIN
* **BR-02:** Auditoria (incluir roles anteriores/novas)
* **BR-03:** Validação
* **BR-07:** Mapper
* **BR-08:** Sessão & Token — `createUserAndToken`, `authInterceptor`, `clearSession`, `logout`

## 6. Non-Functional Requirements Relevantes
* Invalidação de tokens em `clearSession`/`logout` / desativação — propagação via Redis/blacklist ou JWT stateless com versionamento

## 7. Frequency of Use
Baixa — provisionamento

## 8. Assumptions
* `mockLogin` apenas testes (BR-08)
* Refresh token rotation implementado

## 9. Open Issues
* **CRÍTICO:** Corrigir `Usuario.setUsername` stub
* Estratégia de invalidação de token (stateless vs. stateful)

## 10. Related Artifacts
* **API:** api-specification.md#usuarios
* **Frontend:** authInterceptor, clearSession, logout (frontend/src/services/api.js)

---

# Use Case Specification: UC-17 — Autenticação e Sessão

## 1. Characterization
* **Primary Actor:** Qualquer usuário (Admin, Gestor, Analista, Funcionário, Auditor)
* **Secondary Actors:** Banco de Dados (validação credenciais), JWT Provider
* **Stakeholders & Interests:** Todos (acesso), Segurança (controle), Auditoria (login/logout)
* **Trigger:** Usuário acessa tela de login ou token expira (refresh)
* **Preconditions:** Usuário cadastrado e ativo (`Usuario.ativo = true`)
* **Postconditions:** 
  - Login: Access token + Refresh token retornados; `authInterceptor` passa a injetar access token
  - Logout: Tokens invalidados (blacklist ou versionamento); `clearSession` limpa storage local
* **Scope:** Módulo Segurança — `AuthService`, `authInterceptor` (frontend), `mockLogin` (testes)
* **Level:** User goal

## 2. Main Scenario (Happy Path) — Login
1. Usuário informa email/username + senha em `/login`
2. Frontend `POST /api/auth/login` via `request` (sem interceptor — público)
3. Backend valida credenciais: `UsuarioRepository.findByEmail`, `passwordEncoder.matches`
4. Se inválido: `401 Unauthorized` — `handleApiError` exibe "Credenciais inválidas"
5. Se válido e ativo: Gera `accessToken` (JWT, 8h) + `refreshToken` (opaco, 30d, armazenado hash no banco)
6. Auditoria: log `acao=login`, `usuario_id`, `timestamp`, `ip`, `userAgent`
7. Retorno `200 OK` com `{ accessToken, refreshToken, usuario: { id, nome, roles, permissoes } }`
8. Frontend armazena tokens (memory + httpOnly cookie para refresh), `authInterceptor` passa a injetar `Authorization: Bearer <accessToken>`
9. Redireciona para dashboard

## 3. Main Scenario (Happy Path) — Logout
1. Usuário clica "Sair" ou fecha aba (evento `beforeunload`)
2. Frontend chama `POST /api/auth/logout` com refresh token (body ou cookie)
3. Backend invalida refresh token (marca revogado / incrementa `tokenVersion` do usuário)
4. `clearSession` limpa storage local
5. Auditoria: log `acao=logout`
6. Retorno `204 No Content`
7. Frontend redireciona para `/login`

## 4. Alternative Scenarios
### AS-1: Refresh token automático (silencioso)
* **Trigger:** `authInterceptor` recebe `401` com código `TOKEN_EXPIRED` em requisição autenticada
* **Passos:**
  1. Interceptor chama `POST /api/auth/refresh` com refresh token (uma única vez, fila requisições pendentes)
  2. Backend valida refresh token (não revogado, não expirado, hash confere)
  3. Gera novo access token + novo refresh token (rotation)
  4. Atualiza storage, repete requisição original com novo token
  5. Sucesso transparente para usuário

### AS-2: `mockLogin` (apenas testes)
* **Trigger:** Testes automatizados / Storybook
*   **Passos:** `POST /api/auth/mock-login` com `{ role: "ADMIN" }` — retorna token fake com claims da role
*   **Nota:** BR-08 — apenas para testes; desabilitado em produção (profile `test`)

## 5. Exception Scenarios
### EX-1: Conta inativa / bloqueada
* **Tratamento:** `403 Forbidden` — "Conta desativada. Contate o administrador."

### EX-2: Refresh token revogado / expirado / reutilizado (rotação detectada)
* **Tratamento:** `401 Unauthorized` com `code: REFRESH_TOKEN_INVALID` — `authInterceptor` limpa sessão e redireciona para login forçado

### EX-3: Falha no `authInterceptor` (console.error residual em api.js:36)
* **Condição:** Erro de rede, parsing de resposta
*   **Tratamento:** `handleApiError` exibe toast genérico; `console.error` **remover antes de produção** (diagnóstico)

## 5. Business Rules Envolvidas
* **BR-08 (Sessão & Token):** `authInterceptor` injeta token; `clearSession`/`logout` invalidam; `mockLogin` só testes
* **BR-01:** Roles no token determinam permissões (`isAdmin`, `hasPermission`)
* **BR-02:** Auditoria login/logout

## 6. Non-Functional Requirements Relevantes
* Latência login P95 ≤ 800ms (BCrypt + JWT generation)
* Refresh token rotation sem race condition (fila no interceptor)
* Tokens JWT assinados RS256 (chave assimétrica) — chaves rotacionadas a cada 90 dias

## 7. Frequency of Use
Muito alta — login 1x/dia por usuário ativo; refresh a cada 8h

## 8. Assumptions
* Stateless JWT para access token; stateful (banco) para refresh token revogável
* `console.debug` em api.js:99 — remover em produção

## 9. Open Issues
* Definir estratégia de invalidação de access token no logout (blacklist Redis vs. token versioning no usuário)
* MFA (TOTP) — roadmap futuro
* Integração SSO/SAML/OIDC com AD — fase 2

## 10. Related Artifacts
* **API:** api-specification.md#auth
* **Frontend:** api.js (request, authInterceptor, handleApiError, handleResponse, clearSession, logout)
* **Diagnóstico:** console.error (linha 36), console.debug (linha 99) — remover

---

# Use Case Specification: UC-18 — Solicitar Manutenção

## 1. Characterization
* **Primary Actor:** Usuário Final (Funcionário), Analista de Manutenção
* **Secondary Actors:** Gestor de Patrimônio (aprovador), Fornecedor (se externo), Auditoria
* **Stakeholders & Interests:** Solicitante (resolução), Aprovador (controle de custo/necessidade), Analista (execução), Auditor (trilha)
* **Trigger:** Usuário identifica problema no ativo e abre chamado "Nova Manutenção"
* **Preconditions:** 
  - Usuário autenticado (qualquer papel — `iniciar` permitido para USER? BRD não especifica; assumir USER pode iniciar)
  - Ativo existe, status `ATIVO` ou `EM_MANUTENCAO`
  - Sessão válida
* **Postconditions (Success Guarantee):** 
  - Manutenção criada com status `SOLICITADA` / `PENDENTE_APROVACAO`
  - Log auditoria `acao=iniciar` (BR-02)
  - Notificação para aprovadores (gestor da filial/departamento)
  - Resposta `201 Created` com DTO da manutenção
* **Scope:** Módulo Manutenção — `ManutencaoService.iniciar`, `ManutencaoRepository`, `ManutencaoSpecification`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Usuário acessa ativo > "Solicitar Manutenção" ou dashboard > "Nova Manutenção"
2. Formulário: ativo (obrigatório), tipo (PREVENTIVA/CORRETIVA), prioridade (BAIXA/MEDIA/ALTA/CRITICA), descrição problema, anexos (fotos), custo estimado, fornecedor preferencial (opcional)
3. Frontend `POST /api/manutencoes` via `request`
4. `authInterceptor` injeta token
5. Backend valida: ativo existe, usuário tem acesso ao ativo (filial/departamento?), dados válidos (BR-03)
6. Cria `Manutencao` com `status=SOLICITADA`, `solicitanteId=usuarioLogado`, `dataSolicitacao=now`
7. `ManutencaoRepository.save`
8. Auditoria: `acao=iniciar` (BR-02)
9. **Assíncrono:** Dispara notificação (email/push) para aprovadores (gestores da filial do ativo)
10. Retorno `201` com DTO
11. Frontend exibe "Solicitação enviada para aprovação" + número do chamado

## 3. Alternative Scenarios
### AS-1: Manutenção preventiva agendada (criada por Analista, não por solicitação)
* **Ponto de extensão:** Passo 1 — Analista acessa "Planejamento > Nova Preventiva"
* **Diferença:** `tipo=PREVENTIVA`, `status=PLANEJADA`, `dataAgendada` preenchida, pode pular aprovação se dentro de plano aprovado
* **Retorno ao fluxo principal:** Fluxo paralelo

### AS-2: Solicitação com custo estimado > alçada do aprovador
* **Ponto de extensão:** Passo 9 (notificação)
* **Passos:** Regra de roteamento: se custo > R$ 5.000 → aprovação Gerente Regional; > R$ 20.000 → Diretor
*   **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — alçadas não definidas no BRD

## 4. Exception Scenarios
### EX-1: Ativo em manutenção ativa (status `EM_ANDAMENTO`)
* **Tratamento:** `409 Conflict` — "Ativo já possui manutenção em andamento. Aguarde conclusão ou cancele a anterior."

### EX-2: Ativo baixado (`status=BAIXADO`)
* **Tratamento:** `400 Bad Request` — "Não é possível solicitar manutenção para ativo baixado."

### EX-3: Falha ao notificar aprovadores
* **Tratamento:** Log erro; manutenção criada normalmente; job de retry de notificação; não bloquear criação

## 5. Business Rules Envolvidas
* **BR-02:** Auditoria `iniciar`
* **BR-03:** Validação entrada
* **BR-09:** `ManutencaoSpecification.build` para filtros futuros (complexidade 14 — refatorar)
* **BR-01:** Verificar se USER pode `iniciar` (BRD não lista regra explícita para manutenção; assumir permitido)

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 600ms (inclui notificação assíncrona)
* Disponibilidade 99.9% — canal de solicitação sempre aberto

## 7. Frequency of Use
Alta — dezenas por dia por filial

## 8. Assumptions
* Anexos armazenados em object storage (S3/MinIO) — referência salva no banco
* Workflow de aprovação: `SOLICITADA` → `APROVADA` → `EM_ANDAMENTO` → `CONCLUIDA` / `CANCELADA`

## 9. Open Issues
* Definir matriz de alçada de aprovação por valor/tipo/filial
* Permitir solicitação anônima (QR code no ativo)? — futuro
* Integração com WhatsApp/Teams para notificação

## 10. Related Artifacts
* **User Stories:** US-MAN-001, US-MAN-002
* **API:** api-specification.md#post-apimanutencoes
* **Specification:** ManutencaoSpecification (complexidade 14)
* **Estados:** SOLICITADA, PENDENTE_APROVACAO, APROVADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA

---

# Use Case Specification: UC-19 — Aprovar Manutenção

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio / Aprovador designado (por alçada)
* **Secondary Actors:** Solicitante (notificação), Analista de Manutenção (execução), Auditoria
* **Stakeholders & Interests:** Aprovador (controle orçamentário), Solicitante (agilidade), Auditoria (trilha de aprovação)
* **Trigger:** Aprovador recebe notificação, acessa "Minhas Aprovações" e clica "Aprovar" ou "Rejeitar"
* **Preconditions:** 
  - Usuário autenticado com permissão `manutencao:aprovar` (role `GESTOR_PATRIMONIO` ou `ADMIN`)
  - Manutenção existe com status `SOLICITADA` ou `PENDENTE_APROVACAO`
  - Aprovador tem alçada para o valor/tipo/filial
* **Postconditions (Success Guarantee):** 
  - Status alterado para `APROVADA` (ou `REJEITADA`)
  - `aprovadorId`, `dataAprovacao`, `observacaoAprovacao` preenchidos
  - Auditoria `acao=aprovar` (BR-02)
  - Notificação para solicitante e analistas
  - Resposta `200 OK`
* **Scope:** Módulo Manutenção — `ManutencaoService.aprovar`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Aprovador abre lista "Minhas Aprovações" (filtro: `status=PENDENTE_APROVACAO` + alçada)
2. Clica em manutenção > revisa detalhes, custo, histórico do ativo
3. Clica "Aprovar" — modal pede observação (opcional)
4. Frontend `PATCH /api/manutencoes/{id}/aprovar` com `{ observacao }`
5. `authInterceptor` injeta token
6. Backend valida permissão `manutencao:aprovar` + alçada (regra de negócio)
7. `ManutencaoRepository.findById` carrega entidade
8. Valida transição de estado: `SOLICITADA`/`PENDENTE_APROVACAO` → `APROVADA`
9. Atualiza campos: `status=APROVADA`, `aprovadorId`, `dataAprovacao=now`, `observacaoAprovacao`
10. `save` + auditoria `acao=aprovar` (BR-02)
11. Notificação assíncrona: solicitante + analistas da filial
12. Retorno `200 OK` com DTO atualizado
13. Frontend remove da lista de pendências, exibe toast

## 3. Alternative Scenarios
### AS-1: Rejeitar manutenção
* **Ponto de extensão:** Passo 3 — botão "Rejeitar"
* **Passos:** Modal exige observação obrigatória (motivo); status → `REJEITADA`; `rejeitadoPor`, `dataRejeicao`, `motivoRejeicao`; auditoria `acao=cancelar` (ou `rejeitar`); notificação solicitante
* **Retorno ao fluxo principal:** Fluxo paralelo

### AS-2: Aprovação em lote (múltiplas manutenções)
* **Ponto de extensão:** Lista com checkboxes
* **Passos:** `POST /api/manutencoes/aprovar-lote` com `{ ids: [], observacao }`; transação única; auditoria individual por ID

## 4. Exception Scenarios
### EX-1: Manutenção já aprovada/cancelada/concluída
* **Tratamento:** `409 Conflict` — "Manutenção não está mais pendente de aprovação. Status atual: {status}"

### EX-2: Aprovador sem alçada para o valor
* **Tratamento:** `403 Forbidden` — "Valor excede sua alçada de aprovação. Encaminhe para {proximoNivel}."

### EX-3: Erro de concorrência (outro aprovador agiu simultaneamente)
* **Tratamento:** `409 Conflict` com versão — recarregar lista

## 5. Business Rules Envolvidas
* **BR-02:** Auditoria `aprovar` (ou `cancelar` se rejeitada)
* **BR-09:** `ManutencaoSpecification` para listar pendentes por alçada
* **BR-01:** Permissão `manutencao:aprovar` (role baseada)

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 400ms
* Auditoria imutável — não permitir alteração de `aprovadorId`/`dataAprovacao` após setado

## 7. Frequency of Use
Média — volume proporcional a solicitações

## 8. Assumptions
* Alçada configurável por filial/valor/tipo (tabela `AlcadaAprovacao`)
* Notificação assíncrona não bloqueia resposta

## 9. Open Issues
* Definir alçadas formais (workshop com Gestão/Financeiro)
* Permitir aprovação delegada (férias/ausência)?

## 10. Related Artifacts
* **API:** api-specification.md#patch-apimanutencoes-id-aprovar
* **Estados:** PENDENTE_APROVACAO → APROVADA / REJEITADA

---

# Use Case Specification: UC-20 — Cancelar Manutenção

## 1. Characterization
* **Primary Actor:** Solicitante, Aprovador, Analista de Manutenção, Admin
* **Secondary Actors:** Auditoria, Fornecedor (se já acionado)
* **Stakeholders & Interests:** Todos (controle de cancelamento), Financeiro (custos incorridos), Auditoria (justificativa)
* **Trigger:** Usuário com permissão acessa manutenção e clica "Cancelar"
* **Preconditions:** 
  - Usuário autenticado com permissão `manutencao:cancelar` (roles: solicitante, aprovador, analista, admin)
  - Manutenção em status cancelável: `SOLICITADA`, `PENDENTE_APROVACAO`, `APROVADA`, `EM_ANDAMENTO` (com regras)
* **Postconditions:** 
  - Status `CANCELADA`
  - `canceladoPor`, `dataCancelamento`, `motivoCancelamento` (obrigatório)
  - Auditoria `acao=cancelar` (BR-02)
  - Se `EM_ANDAMENTO`: registrar custos incorridos até cancelamento
* **Scope:** Módulo Manutenção — `ManutencaoService.cancelar`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Usuário abre manutenção > "Cancelar"
2. Modal: motivo (obrigatório, select + texto livre: "Problema resolvido espontaneamente", "Duplicata", "Orçamento não aprovado", "Fornecedor indisponível", "Outro")
3. `PATCH /api/manutencoes/{id}/cancelar` com `{ motivo, observacao }`
4. Validação permissão + transição de estado válida
5. Atualiza status `CANCELADA`, campos de cancelamento
6. `save` + auditoria `acao=cancelar`
7. Notificação: solicitante, aprovador, analista, fornecedor (se vinculado)
8. Retorno `200 OK`

## 3. Alternative Scenarios
### AS-1: Cancelamento com custos incorridos (status `EM_ANDAMENTO`)
* **Passos adicionais:** Modal exibe resumo de custos já lançados (peças, mão de obra); usuário confirma ciência; manutenção vai para `CANCELADA_COM_CUSTO` (sub-status) ou `CANCELADA` com `custoIncurred > 0`

## 4. Exception Scenarios
### EX-1: Manutenção já concluída (`CONCLUIDA`)
* **Tratamento:** `409 Conflict` — "Manutenção já concluída. Não é possível cancelar."

### EX-2: Motivo não informado
* **Tratamento:** `400 Bad Request` — "Motivo do cancelamento é obrigatório."

## 5. Business Rules Envolvidas
* **BR-02:** Auditoria `cancelar`
* **BR-03:** Validação (motivo obrigatório)
* **BR-09:** Specification para listar canceláveis

## 6. Non-Functional Requirements Relevantes
* Cancelamento irreversível — confirmação em duas etapas no frontend

## 7. Frequency of Use
Baixa a média

## 8. Assumptions
* Fornecedor notificado via email/template se `fornecedorId` preenchido e status `APROVADA` ou `EM_ANDAMENTO`

## 9. Open Issues
* Definir se `CANCELADA` permite reabertura (criar nova referenciando a cancelada)

## 10. Related Artifacts
* **API:** api-specification.md#patch-apimanutencoes-id-cancelar

---

# Use Case Specification: UC-21 — Concluir Manutenção

## 1. Characterization
* **Primary Actor:** Analista de Manutenção, Fornecedor (portal futuro)
* **Secondary Actors:** Solicitante, Aprovador, Financeiro (custoTotalPorAtivo), Auditoria
* **Stakeholders & Interests:** Analista (fechar chamado), Financeiro (custo real), Gestor (histórico do ativo), Auditor (evidência)
* **Trigger:** Analista finaliza execução e registra conclusão
* **Preconditions:** 
  - Usuário autenticado com permissão `manutencao:concluir` (role `ANALISTA_MANUTENCAO`, `ADMIN`)
  - Manutenção em status `APROVADA` ou `EM_ANDAMENTO`
  - Se `EM_ANDAMENTO`: dados de execução já registrados (peças, horas, fornecedor)
* **Postconditions (Success Guarantee):** 
  - Status `CONCLUIDA`
  - `dataConclusao`, `tecnicoResponsavel`, `relatorioExecucao`, `custoTotal` (peças + mão de obra + terceiros), `pecasUtilizadas[]`, `horasTrabalhadas`
  - Ativo: `ultimaManutencao=dataConclusao`, `proximaManutencao` recalculada (se preventiva)
  - Auditoria `acao=concluir` (BR-02)
  - `custoTotalPorAtivo` atualizado (BR-06)
  - Notificação solicitante + aprovador
* **Scope:** Módulo Manutenção — `ManutencaoService.concluir`, `AtivoService.atualizarProximaManutencao`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Analista abre manutenção `APROVADA`/`EM_ANDAMENTO` > "Concluir"
2. Formulário: data conclusão (default now), relatório técnico (obrigatório), peças utilizadas (grid: item, qtd, valor unitário, fornecedor), horas trabalhadas (interno/terceiro), custo mão de obra, custo terceiros, anexos (laudos, notas fiscais)
3. Frontend `PATCH /api/manutencoes/{id}/concluir` com payload completo
4. Validação permissão + estado
5. Cálculo `custoTotal = sum(pecas) + maoDeObra + terceiros`
6. Atualiza manutenção: status `CONCLUIDA`, todos os campos acima
7. Atualiza ativo: `ultimaManutencao`, `proximaManutencao` (se preventiva: + intervalo padrão do tipo)
8. `ManutencaoRepository.save` + `AtivoRepository.save` (mesma transação)
9. Auditoria: `acao=concluir` na manutenção + `acao=atualizar` no ativo (BR-02)
10. **Evento:** `CustoTotalPorAtivoService.recalcular(ativoId)` — atualiza visão materializada / tabela agregada (BR-06)
11. Notificações assíncronas
12. Retorno `200 OK` com DTO concluído
13. Frontend exibe "Manutenção concluída com sucesso. Custo total: R$ X.XXX"

## 3. Alternative Scenarios
### AS-1: Conclusão com custo zero (garantia / interno sem custo)
* **Passos:** Permitir `custoTotal = 0`; relatório explica "Cobertura de garantia" / "Mão de obra interna sem rateio"

### AS-2: Conclusão de preventiva gera próxima preventiva automática
* **Passos:** Job/trigger cria nova `Manutencao` com `status=PLANEJADA`, `tipo=PREVENTIVA`, `dataAgendada=proximaManutencao`, `ativoId` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 4. Exception Scenarios
### EX-1: Campos obrigatórios ausentes (relatório, peças se houver custo)
* **Tratamento:** `400 Bad Request` com validação por campo

### EX-2: Falha ao atualizar `custoTotalPorAtivo` (view materializada)
* **Tratamento:** Transação principal (manutenção + ativo) commitada; evento de recálculo enfileirado (outbox pattern); job de reconciliação diária

### EX-3: Ativo não encontrado (race condition — baixado durante execução)
* **Tratamento:** `409 Conflict` — "Ativo foi baixado durante a execução. Contate o gestor."

## 5. Business Rules Envolvidas
* **BR-02:** Auditoria `concluir` + `atualizar` (ativo)
* **BR-06:** `custoTotalPorAtivo` = soma custos manutenção por ativo
* **BR-03:** Validação entrada
* **BR-09:** Specification para listar concluídas

## 6. Non-Functional Requirements Relevantes
* Transação distribuída (manutenção + ativo + custo agregado) — usar outbox pattern para eventual consistency do custo total
* Latência P95 ≤ 800ms (cálculos + 2 saves + auditoria)

## 7. Frequency of Use
Média — volume = manutenções aprovadas

## 8. Assumptions
* Peças/serviços podem ser cadastrados on-the-fly ou selecionados de catálogo
* `proximaManutencao` baseada em `TipoAtivo.intervaloManutencaoPreventivaDias` (campo a confirmar)

## 9. Open Issues
* Definir catálogo de peças/serviços vs. livre
* Integração com Financeiro para rateio de mão de obra interna
* Portal do Fornecedor para conclusão (futuro)

## 10. Related Artifacts
* **API:** api-specification.md#patch-apimanutencoes-id-concluir
* **BR-06:** custoTotalPorAtivo
* **Relatório:** custoTotalPorAtivo (frontend)

---

# Use Case Specification: UC-22 — Filtrar Manutenções (Especificação Avançada)

## 1. Characterization
* **Primary Actor:** Analista de Manutenção, Gestor de Patrimônio, Auditor
* **Secondary Actors:** Banco de Dados
* **Stakeholders & Interests:** Analista (fila de trabalho), Gestor (KPIs), Auditor (amostragem)
* **Trigger:** Usuário acessa "Manutenções" com filtros compostos
* **Preconditions:** Usuário autenticado (leitura permitida BR-01)
* **Postconditions:** Lista paginada filtrada retornada
* **Scope:** Módulo Manutenção — `ManutencaoService.listar`, `ManutencaoSpecification.build` (complexidade 14)
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend `GET /api/manutencoes?status=APROVADA&filialId=3&tipo=PREVENTIVA&prioridade=ALTA&dataInicio=2025-01-01&dataFim=2025-01-31&page=0&size=20`
2. `ManutencaoSpecification.build(filtros)` monta `Specification<Manutencao>` dinâmica (BR-09)
3. `ManutencaoRepository.findAll(spec, pageable)` executa
4. Mapper para DTOs leves (id, ativo, tipo, status, prioridade, datas, solicitante, aprovador, custoEstimado)
5. Retorno paginado `200 OK`
6. Frontend renderiza tabela com ações contextuais (aprovar, iniciar, concluir, cancelar)

## 3. Alternative Scenarios
### AS-1: Salvamento de filtros favoritos (por usuário)
* **Fluxo:** `POST /api/manutencoes/filtros/salvos` — persiste preferência; carrega via `GET /api/manutencoes/filtros/salvos`

### AS-2: Exportação filtrada
* **Fluxo:** `GET /api/manutencoes/export?filtros...&format=xlsx`

## 4. Exception Scenarios
### EX-1: Filtro com combinação inválida (ex: dataFim < dataInicio)
* **Tratamento:** `400 Bad Request` — validação no service antes de montar specification

### EX-2: Query gerada pela Specification ineficiente (full table scan)
* **Tratamento:** Monitoramento de slow queries; índices compostos `(filial_id, status, tipo, data_solicitacao)`; `EXPLAIN ANALYZE` em CI

## 5. Business Rules Envolvidas
* **BR-01:** Leitura ADMIN/USER (com filtro implícito por escopo? — open issue)
* **BR-09:** `ManutencaoSpecification.build` extensível sem quebrar clientes
* **BR-07:** Mapper null-safe

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 500ms para filtros complexos
* Specification testada com combinações de 10+ filtros

## 7. Frequency of Use
Alta — tela principal do Analista/Gestor

## 8. Assumptions
* Specification usa JPA Criteria API ou Querydsl (não identificado no diagnóstico — SQL cru assumido)
* Filtro implícito por filial/departamento do usuário logado (se não ADMIN) — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 9. Open Issues
* **CRÍTICO:** Refatorar `ManutencaoSpecification.build` (complexidade 14) — extrair `PredicateBuilder` por filtro
* Definir escopo de visibilidade: USER vê apenas sua filial/departamento ou todos?

## 10. Related Artifacts
* **Código:** ManutencaoSpecification.java:26 (complexidade 14)
* **API:** api-specification.md#get-apimanutencoes

---

# Use Case Specification: UC-23 — Listar e Consultar Alertas

## 1. Characterization
* **Primary Actor:** Analista de Manutenção, Gestor de Patrimônio, Admin
* **Secondary Actors:** Banco de Dados, AlertNotificationService (produtor)
* **Stakeholders & Interests:** Analista (ação sobre alertas), Gestor (visão de risco), Auditor (evidência de tratamento)
* **Trigger:** Usuário acessa "Alertas" no menu ou badge de notificação no header
* **Preconditions:** Usuário autenticado (leitura permitida)
* **Postconditions:** Lista de alertas (não lidos + histórico) retornada paginada
* **Scope:** Módulo Alertas — `AlertaService.listarAlertas`, `getRecentAlerts`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend carrega badge: `GET /api/alertas/nao-lidos/count` (polling 30s ou WebSocket futuro)
2. Usuário clica badge > abre painel lateral ou tela "Alertas"
3. `GET /api/alertas?status=NAO_LIDO&severidade=CRITICA&page=0&size=20`
4. Backend valida permissão, aplica filtro implícito por escopo (filial/departamento do usuário? — open issue)
5. `AlertaRepository.findAll(spec, pageable)`
6. Mapper para DTO: id, tipo (DISCO_CRITICO, MEMORIA_CRITICA, REDE_LENTA, HEALTH_CHECK_AUSENTE), severidade, ativo (resumo), mensagem, timestamp, lido
7. Retorno `200 OK`
8. Frontend agrupa por severidade/ativo, exibe ações: "Ver ativo", "Marcar como lido", "Criar manutenção"

## 3. Alternative Scenarios
### AS-1: Criar manutenção a partir de alerta
* **Fluxo:** Botão "Criar Manutenção" pré-preenche: ativo, tipo=CORRETIVA, prioridade=ALTA/CRITICA, descrição="Alerta: {mensagemAlerta}"
* **UC relacionado:** UC-18

## 4. Exception Scenarios
### EX-1: Volume alto de alertas (storm) — performance
* **Tratamento:** Paginação obrigatória; índice `(status, severidade, timestamp DESC)`; agregação por ativo no backend para "resumo de alertas por ativo"

## 5. Business Rules Envolvidas
* **BR-01:** Leitura permitida (com escopo?)
* **BR-05:** Alertas gerados por `checkResourceUsageAlerts` baseados em thresholds
* **BR-07:** Mapper

## 6. Non-Functional Requirements Relevantes
* Count de não lidos ≤ 100ms (cache Redis ou query otimizada)
* Polling 30s no frontend — avaliar WebSocket para tempo real

## 7. Frequency of Use
Alta — polling contínuo + consultas sob demanda

## 8. Assumptions
* Alertas não expiram automaticamente — apenas marcados como lidos ou resolvidos via manutenção
* `getRecentAlerts` retorna últimos 50 para dashboard

## 9. Open Issues
* Filtro implícito por escopo organizacional (filial/departamento do usuário)
* Deduplicação de alertas (mesmo ativo, mesmo tipo, janela de 1h) — implementar no `AlertNotificationService`

## 10. Related Artifacts
* **API:** api-specification.md#alertas
* **Serviço:** AlertNotificationService.checkResourceUsageAlerts (complexidade 17)
* **BR-05:** Thresholds de geração

---

# Use Case Specification: UC-24 — Marcar Alerta como Lido

## 1. Characterization
* **Primary Actor:** Analista de Manutenção, Gestor
* **Secondary Actors:** Banco de Dados
* **Stakeholders & Interests:** Analista (limpar fila), Gestor (métrica de tratamento)
* **Trigger:** Usuário clica "Marcar como lido" no alerta (individual ou em lote)
* **Preconditions:** Usuário autenticado, alerta existe, pertence ao escopo do usuário
* **Postconditions:** `Alerta.lido = true`, `dataLeitura = now`, `lidoPor = usuarioId`; auditoria opcional
* **Scope:** Módulo Alertas — `AlertaService.marcarComoLido`
* **Level:** Subfunction

## 2. Main Scenario (Happy Path)
1. Frontend `PATCH /api/alertas/{id}/ler` ou `POST /api/alertas/ler-lote` com `{ ids: [] }`
2. Validação permissão + existência
3. Atualização em massa: `UPDATE alerta SET lido=true, data_leitura=now, lido_por=? WHERE id IN (?)`
4. Auditoria (opcional — volume alto; sampling)
5. Retorno `200 OK` / `204 No Content`
6. Frontend atualiza badge count (decrementa)

## 3. Alternative Scenarios
### AS-1: Marcar todos como lidos
* **Fluxo:** `POST /api/alertas/ler-todos` com filtros atuais — atualização em massa

## 4. Exception Scenarios
### EX-1: Alerta já lido
* **Tratamento:** Idempotente — `200 OK` sem erro

## 5. Business Rules Envolvidas
* **BR-01:** Permissão de leitura/escrita no escopo
* **BR-07:** Mapper

## 6. Non-Functional Requirements Relevantes
* Latência ≤ 100ms
* Idempotência garantida

## 7. Frequency of Use
Muito alta — ação principal no painel de alertas

## 8. Assumptions
* Alerta lido não desaparece — fica em histórico com badge "Lido"

## 9. Open Issues
* Auditoria de leitura: gravar ou não? (volume vs. rastreabilidade)

## 10. Related Artifacts
* **API:** api-specification.md#patch-apialertas-id-ler

---

# Use Case Specification: UC-25 — Verificar Alertas de Recursos (Job Agendado)

## 1. Characterization
* **Primary Actor:** Agendador (Spring @Scheduled, cron, Airflow) — **sistema**
* **Secondary Actors:** AlertNotificationService, Banco de Dados, Sistema de Notificação (email/push/webhook)
* **Stakeholders & Interests:** Analista (alertas oportunos), Gestor (visibilidade de risco), Infra (saúde da frota)
* **Trigger:** Job roda a cada N minutos (configurável, ex: 15 min) e invoca `AlertNotificationService.checkResourceUsageAlerts()`
* **Preconditions:** 
  - Health checks recentes existem (últimos 30 min)
  - Thresholds configurados por tipo de ativo
* **Postconditions:** 
  - Novos alertas gerados para ativos fora de threshold
  - Alertas existentes resolvidos (volta ao normal) marcados como `RESOLVIDO` automaticamente
  - Notificações enviadas para canais configurados
* **Scope:** Módulo Monitoramento — `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17 — **refatorar**)
* **Level:** Subfunction (system-triggered)

## 2. Main Scenario (Happy Path)
1. Scheduler dispara `checkResourceUsageAlerts()`
2. Service busca health checks recentes: `AtivoDetalheHardwareRepository.findRecent(30min)`
3. Para cada ativo hardware com health check:
   - Avalia discos: `percentualUso > thresholdDisco` (padrão 85%, configurável por tipo)
   - Avalia memórias: `percentualUso > thresholdMemoria` (padrão 90%)
   - Avalia rede: `latenciaMs > thresholdRede` (padrão 100ms)
4. Para cada violação:
   - Verifica se já existe alerta `ABERTO` para mesmo ativo+tipo na janela (deduplicação)
   - Se não existe: cria `Alerta` com `status=ABERTO`, `severidade=CRITICA|ALTA`, `tipo=DISCO_CRITICO|MEMORIA_CRITICA|REDE_LENTA`
   - Persiste alerta
   - Dispara notificação assíncrona (email, push, webhook) para responsáveis (analistas da filial + gestor)
5. Para alertas `ABERTOS` anteriores: verifica se condição normalizou (uso < threshold - histerese 5%)
   - Se normalizou: `status=RESOLVIDO`, `dataResolucao=now`, `resolvidoAutomaticamente=true`
6. Log de execução: `quantidadeVerificados`, `alertasGerados`, `alertasResolvidos`, `duracaoMs`
7. Job encerra

## 3. Alternative Scenarios
### AS-1: Thresholds por tipo de ativo
* **Passos:** Service busca `AlertaThreshold` por `tipoAtivoId`; fallback para defaults globais

### AS-2: Histerese para evitar flapping
* **Passos:** Resolução só ocorre se métrica < (threshold - 5%) por 2 ciclos consecutivos

## 4. Exception Scenarios
### EX-1: Erro ao processar um ativo (ex: health check corrompido)
* **Tratamento:** Try-catch por ativo; log erro; continua processando demais; métrica `alertasErros` incrementada

### EX-2: Falha no envio de notificação (email/SMTP down)
* **Tratamento:** Alerta persistido normalmente; notificação enfileirada em `NotificationOutbox`; worker separado processa com retry exponencial

### EX-3: Job roda por mais de 5 min (timeout do scheduler)
* **Tratamento:** Processamento em batches (100 ativos/batch); `@Async` com `CompletableFuture`; métrica de duração

## 5. Business Rules Envolvidas
* **BR-05 (Health Check):** Thresholds disco 85%, memória 90%, rede 100ms; avaliação periódica
* **BR-02:** Auditoria de criação/resolução de alerta (opcional — volume)
* **BR-10:** Health checks limpos antes de novo registro (garante dados atuais)

## 6. Non-Functional Requirements Relevantes
* **Taxa de erro 5xx < 0.1%** (guardrail BRD) — complexidade 17 exige refatoração + testes
* Execução completa < 2 min para 10.000 ativos
* Idempotência: job reexecutável sem duplicar alertas

## 7. Frequency of Use
Contínua — a cada 15 min (configurável)

## 8. Assumptions
* `AlertNotificationService` refatorado em: `ResourceThresholdEvaluator` (interface), `DiskEvaluator`, `MemoryEvaluator`, `NetworkEvaluator`, `AlertDeduplicator`, `AlertPersister`, `NotificationDispatcher`
* Thresholds armazenados em tabela `alerta_threshold` com `tipo_ativo_id`, `metrica`, `valor_critico`, `valor_alerta`, `histerese`

## 9. Open Issues
* **CRÍTICO:** Refatorar `AlertNotificationService` (complexidade 17) antes de Q2
* Definir canais de notificação por severidade/role (email para CRITICA, push para ALTA, log para BAIXA)
* Integração com ITSM (Jira, ServiceNow) para criação automática de incidente

## 10. Related Artifacts
* **Código:** AlertNotificationService.java:96 (complexidade 17)
* **BR-05, BR-10**
* **API:** Nenhuma direta (job interno) — expor `POST /api/alertas/verificar` para trigger manual (Admin)

---

# Use Case Specification: UC-26 — Consultar Custo Total por Ativo

## 1. Characterization
* **Primary Actor:** Gestor de Patrimônio, Financeiro, Auditor
* **Secondary Actors:** Banco de Dados (view materializada / agregação)
* **Stakeholders & Interests:** Gestor (TCO por ativo), Financeiro (CAPEX/OPEX), Auditor (comprovação de custos)
* **Trigger:** Usuário acessa "Relatórios > Custo Total por Ativo" ou dashboard executivo
* **Preconditions:** Usuário autenticado com permissão `relatorio:custo-total` (roles: GESTOR_PATRIMONIO, ADMIN, FINANCEIRO, AUDITOR)
* **Postconditions:** Relatório/JSON com `ativoId`, `codigoPatrimonial`, `descricao`, `custoTotalManutencao`, `quantidadeManutencoes`, `ultimaManutencao`, `valorAquisicao`, `valorResidualAtual`, `TCO = valorAquisicao + custoTotalManutencao - valorResidualAtual`
* **Scope:** Módulo Relatórios — `RelatorioService.custoTotalPorAtivo` (frontend: `custoTotalPorAtivo` em glossário)
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Frontend `GET /api/relatorios/custo-total-por-ativo?filialId=3&dataInicio=2024-01-01&dataFim=2024-12-31&page=0&size=50`
2. Validação permissão `relatorio:custo-total`
3. Service executa query agregada (view materializada `mv_custo_total_ativo` ou query com `SUM(manutencao.custo_total) GROUP BY ativo_id`)
4. Join com `Ativo` para metadados (código, descrição, valorAquisicao, dataAquisicao, vidaUtil)
5. Cálculo `valorResidualAtual` = `valorAquisicao * (1 - mesesDecorridos / vidaUtilMeses)` (linear) — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
6. Cálculo `TCO = valorAquisicao + custoTotalManutencao - valorResidualAtual`
7. Ordenação: `custoTotalManutencao DESC` (padrão) ou `TCO DESC`
8. Retorno paginado `200 OK` com DTOs
9. Frontend renderiza tabela com ordenação, filtros, exportação CSV/Excel
10. Gráfico: Top 10 ativos por TCO / por custo manutenção

## 3. Alternative Scenarios
### AS-1: Detalhamento por ativo (drill-down)
* **Fluxo:** Clica linha > `GET /api/relatorios/custo-total-por-ativo/{ativoId}/detalhamento` — lista manutenções com custo, data, tipo, fornecedor

### AS-2: Agregação por filial/departamento/tipo
* **Fluxo:** `GET /api/relatorios/custo-total-agregado?groupBy=filial&dataInicio=...` — retorna totais por grupo

## 4. Exception Scenarios
### EX-1: View materializada desatualizada (lag > 1h)
* **Tratamento:** Header `X-Data-Atualizacao` no response; frontend avisa "Dados atualizados há 45 min"; botão "Atualizar agora" dispara refresh assíncrono

### EX-2: Ativo sem manutenções
* **Tratamento:** `custoTotalManutencao = 0`; incluído no relatório (TCO = valorAquisicao - valorResidual)

## 5. Business Rules Envolvidas
* **BR-06 (Custo Total por Ativo):** Soma de todos os custos de manutenção agrupados por `ativo_id`
* **BR-01:** Permissão `relatorio:custo-total` (roles autorizadas)
* **BR-04:** 404 se ativo inexistente (no drill-down)
* **BR-07:** Mapper para DTO de relatório

## 6. Non-Functional Requirements Relevantes
* Query agregada ≤ 2s para 12.000 ativos (view materializada refrescada a cada 30 min ou on-demand)
* Exportação 12.000 linhas CSV < 5s (streaming)

## 7. Frequency of Use
Média — relatórios semanais/mensais + consultas pontuais

## 8. Assumptions
* Custo de manutenção = peças + mão de obra + terceiros (campos em `Manutencao`)
* Depreciação linear padrão; outros métodos (ex: saldo decrescente) — futuro
* View materializada refrescada por trigger em `Manutencao` insert/update (status=CONCLUIDA) ou job agendado

## 9. Open Issues
* Definir método de depreciação oficial (linear vs. fiscal)
* Incluir custo de aquisição de peças em estoque alocadas? (hoje apenas manutenções concluídas)
* Permissão `relatorio:custo-total` — definir roles exatas

## 10. Related Artifacts
* **API:** api-specification.md#get-apirelatorios-custo-total-por-ativo
* **Frontend:** glossário `custoTotalPorAtivo`
* **BR-06**

---

# Use Case Specification: UC-27 — Atualizar Escalares de Hardware (updateScalars)

## 1. Characterization
* **Primary Actor:** Agendador Externo / Agente de Coleta (sistema)
* **Secondary Actors:** Banco de Dados
* **Stakeholders & Interests:** Analista (métricas atuais), Alertas (thresholds)
* **Trigger:** Coleta leve de métricas escalares (CPU%, RAM%, Disco%) — mais frequente que health check completo
* **Preconditions:** Ativo hardware existe, exige health check
* **Postconditions:** Tabela `ativo_detalhe_hardware` atualizada com `cpu_percent`, `memoria_percent`, `disco_percent`, `timestamp`; alertas reavaliados
* **Scope:** Módulo Monitoramento — `HealthCheckService.updateScalars`
* **Level:** Subfunction

## 2. Main Scenario (Happy Path)
1. Agente envia `PATCH /api/ativos/{id}/health-check/scalars` com `{ cpuPercent: 45, memoriaPercent: 68, discoPercent: 72, timestamp }`
2. Validação token serviço + ativo hardware
3. Upsert em `AtivoDetalheHardware` (apenas campos escalares + timestamp)
4. `AlertNotificationService.checkResourceUsageAlerts(ativoId)` avalia thresholds
5. Retorno `200 OK`

## 3. Alternative Scenarios
* Nenhuma relevante

## 4. Exception Scenarios
### EX-1: Métricas fora de range (ex: cpuPercent > 100)
* **Tratamento:** `400 Bad Request` — sanitizar no agente

## 5. Business Rules Envolvidas
* **BR-05:** Thresholds aplicados
* **BR-10:** Não remove componentes (adaptadores, discos, memórias) — apenas atualiza escalares

## 6. Non-Functional Requirements Relevantes
* Latência ≤ 200ms (leve)
* Frequência: a cada 1-5 min (vs. health check completo 15-60 min)

## 7. Frequency of Use
Muito alta — contínua

## 8. Assumptions
* Endpoint separado do health check completo para permitir frequência maior com payload menor

## 9. Open Issues
* Definir se `updateScalars` dispara alertas ou apenas health check completo dispara

## 10. Related Artifacts
* **Glossário:** `updateScalars`
* **BR-05, BR-10**

---

# Use Case Specification: UC-28 — Gerenciar Usuário-Funcionário (createFuncionarioAndUsuario)

## 1. Characterization
* **Primary Actor:** Administrador de Sistema, RH
* **Secondary Actors:** Auditoria, Sistema de Autenticação
* **Stakeholders & Interests:** RH (onboarding único), Admin (provisionamento atômico), Funcionário (acesso imediato)
* **Trigger:** RH cadastra novo funcionário e marca "Criar acesso ao sistema"
* **Preconditions:** Usuário ADMIN; dados do funcionário válidos
* **Postconditions:** Funcionário + Usuário criados em transação única; token JWT retornado; auditoria dupla
* **Scope:** Módulo RH/Segurança — `FuncionarioService.createFuncionarioAndUsuario`, `UsuarioService.createUserAndToken`
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Formulário unificado: dados funcionário (nome, CPF, matrícula, email, cargo, filial, depto, gestor) + credenciais (username, senha temporária, roles)
2. `POST /api/funcionarios/com-usuario`
3. Validação ADMIN + unicidades (CPF, matrícula, email, username)
4. Transação:
   - `FuncionarioRepository.save(funcionario)`
   - `UsuarioService.createUserAndToken(funcionario, roles, senhaTemporaria)` — **ATENÇÃO: `Usuario.setUsername` stub vazio (linha 86)**
   - Auditoria: `acao=criar` para Funcionário + `acao=criar` para Usuario
5. Retorno `201` com `{ funcionario, usuario, accessToken, refreshToken }`
6. Frontend exibe credenciais para entrega segura (ou envia email com link de primeiro acesso)

## 3. Alternative Scenarios
### AS-1: Funcionário já existe, apenas criar usuário
* **Fluxo:** `POST /api/funcionarios/{id}/usuario` — vincula usuário a funcionário existente

## 4. Exception Scenarios
### EX-1: `setUsername` stub falha
* **Impacto:** Usuário criado com `username=null` → login falha, token inválido
*   **Tratamento:** **BUG BLOQUEANTE** — corrigir antes de qualquer teste de integração

### EX-2: Falha na criação do usuário (após funcionário salvo)
* **Tratamento:** Transação rolada (ambos); `400/500` com mensagem clara

## 5. Business Rules Envolvidas
* **BR-01:** Apenas ADMIN
* **BR-02:** Auditoria dupla
* **BR-03:** Validação
* **BR-07:** Mappers
* **BR-08:** `createUserAndToken` gera token; `authInterceptor` valida

## 6. Non-Functional Requirements Relevantes
* Atomicidade transacional obrigatória
* Senha temporária: expiração 24h, força troca no primeiro login

## 7. Frequency of Use
Baixa — onboarding

## 8. Assumptions
* `mockLogin` não usado aqui (apenas testes)
* Roles padrão para novo funcionário: `USER` + role por cargo (ex: `ANALISTA_MANUTENCAO` se cargo = Analista)

## 9. Open Issues
* **CRÍTICO:** Corrigir `Usuario.setUsername` (Usuario.java:86)
* Definir fluxo de entrega de credenciais (email vs. impressão vs. gestor)

## 10. Related Artifacts
* **Código:** Usuario.java:86 (stub)
* **API:** api-specification.md#post-apifuncionarios-com-usuario
* **BR-08**

---

# Use Case Specification: UC-29 — Dashboard Executivo (North Star Metric)

## 1. Characterization
* **Primary Actor:** Sponsor (Diretoria), Gestor de Patrimônio
* **Secondary Actors:** Banco de Dados (views materializadas)
* **Stakeholders & Interests:** Liderança (visão estratégica), PMO (acompanhamento de metas)
* **Trigger:** Acesso à tela inicial / dashboard executivo
* **Preconditions:** Usuário autenticado com role `ADMIN` ou `GESTOR_PATRIMONIO`
* **Postconditions:** Métricas-chave carregadas: North Star, Guardrails, KPIs do BRD
* **Scope:** Módulo Dashboard — `DashboardService`, views materializadas
* **Level:** Summary

## 2. Main Scenario (Happy Path)
1. Frontend carrega dashboard: `GET /api/dashboard/executivo`
2. Backend agrega (views materializadas refrescadas a cada 15 min):
   - **North Star:** `ativosComHealthCheckRecente30d / totalAtivosCadastrados * 100`
   - **Guardrails:** Latência P95 API (últimas 24h), Taxa erro 5xx `checkResourceUsageAlerts`, Custo infra/ativo, Churn usuários
   - **KPIs BRD:** % ativos cadastrados, Downtime não planejado (horas/mês), % operações auditadas, % alertas convertidos em preventiva
3. Retorno `200 OK` com JSON estruturado
4. Frontend renderiza cards, gauges, sparklines (Chart.js + Popper.js para tooltips)

## 3. Alternative Scenarios
### AS-1: Drill-down em métrica
* **Fluxo:** Clica card > abre relatório detalhado (ex: UC-26 para custo total)

## 4. Exception Scenarios
### EX-1: Views materializadas não refrescadas
* **Tratamento:** Badge "Dados desatualizados" + botão refresh manual (dispara job assíncrono)

## 5. Business Rules Envolvidas
* **BR-01:** Permissão restrita (ADMIN, GESTOR_PATRIMONIO)
* **BR-04:** 404 se recurso não encontrado (não aplicável aqui)
* **KPIs do BRD Seção 4**

## 6. Non-Functional Requirements Relevantes
* Latência P95 ≤ 1s (agregações pré-calculadas)
* Disponibilidade 99.95%

## 7. Frequency of Use
Alta — tela inicial de gestores

## 8. Assumptions
* Views materializadas: `mv_north_star`, `mv_guardrails`, `mv_kpis_brd`
* Popper.js usado para tooltips nos gráficos (dependência confirmada)

## 9. Open Issues
* Definir queries exatas das views materializadas
* Permissão de acesso ao dashboard por filial (gestor vê apenas sua filial?)

## 10. Related Artifacts
* **BRD Seção 4:** North Star Metric, Guardrail Metrics, KPIs
* **Frontend:** @popperjs/core para tooltips/dropdowns

---

# Use Case Specification: UC-30 — Auditoria e Trilha de Custódia

## 1. Characterization
* **Primary Actor:** Auditor / Compliance
* **Secondary Actors:** Banco de Dados (tabela `audit_log` — **gap a implementar**)
* **Stakeholders & Interests:** Auditor (evidência), Compliance (LGPD/SOX), Segurança (investigação)
* **Trigger:** Auditor acessa "Auditoria > Trilha de Custódia" ou "Auditoria > Logs de Acesso"
* **Preconditions:** Usuário autenticado com role `AUDITOR` ou `ADMIN`
* **Postconditions:** Lista de logs de auditoria filtrada, imutável, exportável
* **Scope:** Módulo Auditoria — `AuditLogService`, `AuditLogRepository` (**a criar**)
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Auditor define filtros: período, usuário, entidade (Ativo, Manutencao, Usuario, etc.), ação (criar, atualizar, deletar, aprovar, cancelar, concluir, iniciar, baixar, logar, deslogar), filial/departamento
2. Frontend `GET /api/auditoria/logs?entidade=Ativo&acao=atualizar&dataInicio=2025-01-01&usuarioId=123&page=0&size=50`
3. Validação permissão `auditoria:ler` (role AUDITOR/ADMIN)
4. `AuditLogRepository.findAll(spec, pageable)` — tabela append-only, indexada por `(entidade, entidade_id, timestamp)`
5. Mapper para DTO: id, timestamp, usuario (nome, email), entidade, entidade_id, acao, valores_anteriores (JSON), valores_novos (JSON), ip, userAgent
6. Retorno `200 OK` paginado
7. Frontend renderiza tabela expansível (clica linha > mostra diff JSON formatado)
8. Exportação: `GET /api/auditoria/logs/export?filtros...&format=csv` — streaming WORM-compliant

## 3. Alternative Scenarios
### AS-1: Trilha de custódia de um ativo específico
* **Fluxo:** `GET /api/auditoria/custodia/ativo/{ativoId}` — retorna timeline completa: criação, alocações, manutenções, health checks, baixa — ordenado por timestamp

## 4. Exception Scenarios
### EX-1: Tabela `audit_log` não existe (gap atual)
* **Tratamento:** **IMPEDIMENTO** — implementar entidade `AuditLog` + interceptores JPA (`@PrePersist`, `@PreUpdate`, `@PreRemove`) + aspect para ações de service (aprovar, cancelar, concluir) antes de qualquer release de produção

### EX-2: Volume massivo (TB de logs) — performance
* **Tratamento:** Particionamento por mês (`audit_log_2025_01`); retenção 7 anos (SOX) / 5 anos (LGPD); tiering para cold storage (S3 Glacier)

## 5. Business Rules Envolvidas
* **BR-02 (Trilha de Auditoria Obrigatória):** **GAP CRÍTICO** — não há entidade de log no glossário; deve ser implementada
* **BR-01:** Acesso restrito a AUDITOR/ADMIN
* **BR-07:** Mapper para DTO de log

## 6. Non-Functional Requirements Relevantes
* **Imutabilidade:** Tabela `audit_log` sem `UPDATE`/`DELETE` permits (apenas `INSERT`); grants de banco restringidos
* **Integridade:** Hash encadeado (hash do log anterior incluído no atual) para detectar adulteração — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Disponibilidade:** 99.99% para leitura de auditoria
* **Latência:** P95 ≤ 2s para consultas com filtros complexos (particionamento + índices)

## 7. Frequency of Use
Baixa a média — auditorias periódicas + investigações pontuais

## 8. Assumptions
* Logs de autenticação (login/logout) também em `audit_log` (ação `login`, `logout`)
* `valores_anteriores`/`valores_novos` armazenados como JSONB (PostgreSQL) ou CLOB (Oracle)

## 9. Open Issues
* **BLOQUEANTE:** Implementar `AuditLog` entity + interceptores + aspect — **Sprint 0**
* Definir retenção por tipo de entidade/ação (ex: health checks — volume alto; sampling?)
* Hash encadeado vs. assinatura digital — decisão de arquitetura de segurança

## 10. Related Artifacts
* **BRD Seção 6:** BR-02 (gap identificado)
* **Entidade a criar:** AuditLog
* **API:** api-specification.md#auditoria (a definir)

---

## Resumo de Cobertura dos Use Cases

| UC | Nome | Ator Primário | BRs Principais | Status Código |
|----|------|---------------|----------------|---------------|
| UC-01 | Cadastrar Ativo | Gestor Patrimônio | BR-01, BR-02, BR-03, BR-07 | Mapper complexidade 14 |
| UC-02 | Listar Ativos | Todos (leitura) | BR-01, BR-07 | Specification a criar |
| UC-03 | Buscar Ativo por ID | Todos (leitura) | BR-01, BR-04, BR-07 | OK |
| UC-04 | Atualizar Ativo | Gestor (ADMIN) | BR-01, BR-02, BR-03, BR-07 | Mapper complexidade 14 |
| UC-05 | Excluir/Baixar Ativo | Gestor (ADMIN) | BR-01, BR-02, BR-10 | Soft vs Hard delete aberto |
| UC-06 | Gerenciar Tipos Ativo | Admin | BR-01, BR-02, BR-03, BR-07 | Simples |
| UC-07 | Gerenciar Localizações | Admin | BR-01, BR-02, BR-03, BR-07 | Hierarquia |
| UC-08 | Registrar Health Check | Sistema (Agendador) | BR-05, BR-10 | Idempotente, cascata |
| UC-09 | Histórico Health Check | Analista, Gestor | BR-01, BR-04, BR-07 | Retenção 13m |
| UC-10 | Gerenciar Departamentos | Admin | BR-01, BR-02, BR-03, BR-07 | Sync AD futuro |
| UC-11 | Gerenciar Filiais | Admin | BR-01, BR-02, BR-03, BR-07 | Timezone |
| UC-12 | Gerenciar Fornecedores | Admin, Gestor | BR-01, BR-02, BR-03, BR-07 | CNPJ único |
| UC-13 | Gerenciar Funcionários | Admin, RH | BR-01, BR-02, BR-03, BR-07, BR-08 | **BUG: setUsername stub** |
| UC-14 | Gerenciar Roles | Admin | BR-01, BR-02, BR-03, BR-07 | Matriz a definir |
| UC-15 | Gerenciar Permissões | Admin | BR-01, BR-02, BR-03, BR-07 | Cache O(1) |
| UC-16 | Gerenciar Usuários | Admin | BR-01, BR-02, BR-03, BR-07, BR-08 | **BUG: setUsername stub** |
| UC-17 | Autenticação/Sessão | Todos | BR-01, BR-02, BR-08 | Refresh rotation, console.* remover |
| UC-18 | Solicitar Manutenção | Usuario Final, Analista | BR-02, BR-03, BR-09 | Alçada a definir |
| UC-19 | Aprovar Manutenção | Aprovador | BR-02, BR-09 | Alçada a definir |
| UC-20 | Cancelar Manutenção | Solicitante, Aprovador, Analista | BR-02, BR-03, BR-09 | Custos incorridos |
| UC-21 | Concluir Manutenção | Analista | BR-02, BR-06, BR-03, BR-09 | Outbox para custo total |
| UC-22 | Filtrar Manutenções | Analista, Gestor, Auditor | BR-01, BR-09, BR-07 | **Refatorar Spec complexidade 14** |
| UC-23 | Listar Alertas | Analista, Gestor | BR-01, BR-05, BR-07 | Polling 30s |
| UC-24 | Marcar Alerta Lido | Analista | BR-01, BR-07 | Idempotente, lote |
| UC-25 | Verificar Alertas (Job) | Sistema | BR-05, BR-10 | **Refatorar Service complexidade 17** |
| UC-26 | Custo Total por Ativo | Gestor, Financeiro, Auditor | BR-01, BR-06, BR-04, BR-07 | View materializada |
| UC-27 | Atualizar Escalares | Sistema | BR-05, BR-10 | Alta frequência |
| UC-28 | Funcionário+Usuário | Admin, RH | BR-01, BR-02, BR-03, BR-07, BR-08 | **BUG: setUsername stub** |
| UC-29 | Dashboard Executivo | Sponsor, Gestor | BR-01, KPIs Seção 4 | Views materializadas |
| UC-30 | Auditoria/Trilha | Auditor | **BR-02 (GAP)**, BR-01, BR-07 | **Implementar AuditLog** |

---

## Rastreabilidade para Refatorações Críticas (Diagnóstico)

| Arquivo | Linha | Complexidade | Use Cases Afetados | Ação Requerida |
|---------|-------|--------------|-------------------|----------------|
| `AtivoMapper.java` | 15 | 14 | UC-01, UC-02, UC-03, UC-04, UC-05 | Quebrar em mappers por seção |
| `ManutencaoSpecification.java` | 26 | 14 | UC-18, UC-19, UC-20, UC-21, UC-22 | Extrair `PredicateBuilder` por filtro |
| `AlertNotificationService.java` | 96 | 17 | UC-08, UC-25 | **Prioridade máxima** — separar evaluators + dispatcher |
| `RealisticDataSeeder.java` | 34 | 15 | Testes/Carga | Refatorar para builders/factories |
| `Usuario.java` | 86 | — | UC-13, UC-16, UC-28 | **Corrigir stub `setUsername`** |
| `api.js` | 36, 99 | 13 (request) | UC-17, todos frontend | Remover `console.*`; quebrar `request` |

---

## Legenda de Marcadores
* **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**: Hipótese baseada no contexto do BRD/código, não explicitada nas fontes
* **GAP CRÍTICO / BLOQUEANTE / BUG BLOQUEANTE**: Item que impede funcionamento correto ou conformidade
* **BR-XX**: Referência direta às Business Rules do BRD (Seção 6)
* **Complexidade ciclomática**: Conforme diagnóstico determinístico (AST)