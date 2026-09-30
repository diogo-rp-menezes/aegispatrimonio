# Catálogo de Casos de Uso (Use Case Specifications) — a6 · AegisPatrimônio

> **Versão:** 1.0 · **Status:** Draft · **Artefato-fonte:** Business Requirements Document (BRD) v1.0 · **Base de código:** diagnóstico determinístico do workspace (varredura + AST — 353 arquivos, ~27.912 LOC, 1.287 funções, 347 classes)

**Escopo e método:** este catálogo deriva do BRD v1.0 (artefato-fonte prioritário) e do diagnóstico determinístico do codebase. Cada caso de uso mapeia os fluxos dos cenários para os **métodos de serviço e componentes verificados no código**; as regras de negócio referenciadas (BR-01 a BR-10) são exatamente as definidas na seção 6 do BRD. O fluxo de manutenção foi decomposto em três casos de uso (UC-05 a UC-07) para refletir os atores e transições distintos do ciclo de aprovação.

**Nota de rastreabilidade (rotas/IPC):** a varredura do workspace **não detectou rotas/IPC/handlers explícitos** nos fontes analisados. Por isso, os fluxos são mapeados diretamente para os métodos de serviço e componentes verificados — ex.: `createAtivo`, `iniciar`, `aprovar`, `concluir`, `cancelar`, `updateHealthCheck`, `updateScalars`, `getHealthHistory`, `checkResourceUsageAlerts`, `listarAlertas`, `getRecentAlerts`, `markAsRead`, `custoTotalPorAtivo`, `hasPermission`, `doFilterInternal`, `ManutencaoSpecification` — e para a **camada de serviços do frontend** (`request`/`handleResponse`/`handleApiError` em `frontend/src/services/api.js`, com `authInterceptor` anexando credenciais a cada chamada). Quando uma especificação de API for produzida, cada passo destes cenários deve ser vinculado à rota correspondente.

**Premissas gerais (visíveis em todo o catálogo):**
* O mecanismo real de persistência **não está declarado** nas dependências (nenhum motor de banco ou ORM/query builder encontrado — Constraint C-01 do BRD); os fluxos assumem que existe persistência, a ser confirmada pela engenharia.
* Não há telemetria instrumentada no repositório — frequências de uso, baselines e metas de latência são estimativas qualitativas **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* Os atores humanos derivam das personas do BRD (seção 3), que já estão marcadas como inferência no próprio BRD.
* Sem integrações com sistemas de terceiros — a única dependência de produção declarada é `@popperjs/core ^2.11.8` (Out-of-Scope do BRD).

### Índice de Casos de Uso

| UC | Nome | Ator primário | Nível |
| :--- | :--- | :--- | :--- |
| UC-01 | Autenticar e Gerir Sessão | Usuário do sistema (ADMIN/USER) | User goal |
| UC-02 | Gerir Cadastros Base do Patrimônio | Administrador de Patrimônio (ADMIN) | User goal |
| UC-03 | Gerir Usuários, Papéis e Permissões | Administrador (ADMIN) | User goal |
| UC-04 | Consultar Listagens e Registros com Filtros | Usuário autenticado (ADMIN/USER) | User goal |
| UC-05 | Iniciar Solicitação de Manutenção | Gestor de Filial (USER) | User goal |
| UC-06 | Aprovar Manutenção | Administrador (ADMIN) *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]* | User goal |
| UC-07 | Concluir ou Cancelar Manutenção | Gestor de Filial (USER) / Administrador | User goal |
| UC-08 | Registrar Health Check e Consultar Histórico de Saúde | Técnico de TI / Monitoramento | User goal |
| UC-09 | Gerir Alertas de Uso de Recursos | Técnico de TI / Monitoramento | User goal |
| UC-10 | Consultar Custo Total por Ativo | Gestor de Filial / Administrador | User goal |
| UC-11 | Atualizar Inventário de Hardware do Ativo | Técnico de TI / Monitoramento | User goal |
| UC-12 | Executar Carga de Dados Realista para Homologação | Engenharia/TI (operação técnica) | Subfunction |
| UC-13 | Consultar Eventos de Auditoria e Histórico de Modificações | Auditor / Compliance | User goal |

---

## UC-01 — Autenticar e Gerir Sessão

### 1. Characterization
* **Primary Actor:** Usuário do sistema (perfil ADMIN ou USER) — funcionário com credenciais emitidas.
* **Secondary Actors:** Filtro de controle de acesso no backend (`doFilterInternal`), que valida a credencial a cada requisição; camada de serviços do frontend (`authInterceptor`), que anexa as credenciais automaticamente a cada chamada. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** serviço de emissão de token (`createUserAndToken`) como componente de backend — o mecanismo exato de emissão/validação do token não está detalhado na varredura.
* **Stakeholders & Interests:** TI/Segurança (cada acesso indevido é incidente — meta do BRD: 0 incidentes em produção); Auditoria/Compliance (rastreabilidade de sessão); Gestores de filial (dependem de acesso para operar o patrimônio da unidade).
* **Trigger:** Usuário acessa o sistema sem sessão válida (login) ou decide encerrar a sessão (logout).
* **Preconditions:** Funcionário com credenciais emitidas e vínculo funcionário↔usuário estabelecido (`createFuncionarioAndUsuario` — UC-03); aplicação acessível pela rede interna.
* **Postconditions (Success Guarantee):** Sessão válida estabelecida, com credenciais anexadas automaticamente às chamadas subsequentes; ou, no logout, **token invalidado imediatamente** (BR-07) e sessão encerrada (`clearSession`), exigindo nova autenticação.
* **Scope:** Sistema AegisPatrimônio — autenticação e sessão (backend + frontend).
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Usuário informa as credenciais na tela de login.
2. Frontend envia a solicitação pela camada de serviços (`request` em `frontend/src/services/api.js`).
3. Sistema valida as credenciais e emite o token de acesso (`createUserAndToken`).
4. Sistema estabelece a sessão; a partir daí `authInterceptor` anexa automaticamente as credenciais a cada chamada e `doFilterInternal` valida a credencial a cada requisição.
5. Caso de uso encerra com sucesso — usuário autenticado, com perfil (ADMIN/USER) e contexto (filial/departamento) disponíveis para autorização (BR-04).

### 3. Alternative Scenarios
#### AS-1: Login de validação (`mockLogin`)
* **Ponto de extensão:** Passo 2 (envio da solicitação de autenticação).
* **Passos:**
  1. Em ambiente de validação/homologação, o fluxo de login é executado de forma simulada (`mockLogin`), sem credenciais reais.
  2. Sessão de validação estabelecida para exercitar os demais casos de uso.
* **Retorno ao fluxo principal:** Não — encerra como variante de validação. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** premissa: `mockLogin` é usado apenas para validação, nunca em produção (BRD, seção 5 — In-Scope: "incl. `mockLogin` para validação").

#### AS-2: Logout com invalidação imediata
* **Ponto de extensão:** Qualquer momento após o passo 5 (usuário já autenticado).
* **Passos:**
  1. Usuário autenticado aciona o logout.
  2. Sistema invalida **imediatamente** o token de acesso (BR-07).
  3. Sistema encerra a sessão (`clearSession`).
* **Retorno ao fluxo principal:** Não — encerra; nova autenticação é exigida (UC-01 reinicia).

### 4. Exception Scenarios
#### EX-1: Credenciais inválidas
* **Ponto de extensão:** Passo 3 (validação das credenciais).
* **Condição de erro:** Credenciais informadas não correspondem a um usuário válido.
* **Tratamento:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** recusa do login com mensagem de erro apresentada ao usuário pela camada `handleApiError`; nenhuma sessão é estabelecida. O texto exato da mensagem não está evidenciado na varredura.

#### EX-2: Falha de conexão com o backend
* **Ponto de extensão:** Passo 2 (chamada `request`).
* **Condição de erro:** Backend indisponível ou falha de rede durante a chamada.
* **Tratamento:** `handleApiError` captura a falha e apresenta o erro ao usuário (guardrail do BRD: "taxa de erros de API tratados ao usuário"); o `console.error` residual em `frontend/src/services/api.js:44` registra o erro no console — residual a remover antes de produção (risco registrado no BRD). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** nenhuma política de retry automático está evidenciada no código.

### 5. Business Rules Envolvidas
* **BR-07:** O logout invalida imediatamente o token de acesso; sessão encerrada (`clearSession`) exige nova autenticação — regra central deste caso de uso.
* **BR-04:** A partir da sessão estabelecida, toda autorização (`hasPermission`) considera sempre o perfil de acesso e o contexto (filial/departamento).
* **BR-02:** Usuários autenticados podem consultar listagens completas — habilita os demais casos de uso de consulta.

### 6. Non-Functional Requirements Relevantes
* **Performance:** latência do login compatível com uso interativo; sem telemetria no repositório, a meta numérica deve ser definida pela engenharia. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Segurança:** invalidação imediata de token no logout (BR-07) é requisito de segurança obrigatório; credenciais anexadas automaticamente via `authInterceptor` em todas as chamadas.
* **Manutenibilidade:** `request` em `frontend/src/services/api.js` com complexidade ciclomática 13 (achado AST) — refatorar em funções menores antes de evoluir a camada de serviços (mitigação registrada no BRD).
* **Disponibilidade:** sem SLO evidenciado no código; aplicação com backend e frontend próprios, sem evidência de clustering ou redundância — requisitos de disponibilidade a definir. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 7. Frequency of Use
Alta — todo usuário executa login a cada sessão de trabalho (múltiplas vezes por dia por usuário ativo). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** estimativa qualitativa; não há telemetria no repositório.

### 8. Assumptions
* Credenciais emitidas via `createUserAndToken` e vínculo funcionário↔usuário via `createFuncionarioAndUsuario` (verificados no BRD, seção 5).
* `mockLogin` destina-se apenas a validação (BRD).
* O mecanismo de emissão/validação/invalidação de token existe, mas sua implementação não está detalhada na varredura (nenhuma rota detectada).

### 9. Open Issues
* Mecanismo exato de emissão, validação e invalidação de token não documentado — levantar com a engenharia.
* Política de expiração de token e de retry em falha de rede não evidenciada no código.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Sessão").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `createUserAndToken`, `authInterceptor`, `doFilterInternal`, `clearSession`; vincular a `api-specification.md` quando produzida.

---

## UC-02 — Gerir Cadastros Base do Patrimônio

### 1. Characterization
* **Primary Actor:** Administrador de Patrimônio (perfil ADMIN).
* **Secondary Actors:** Mecanismo de auditoria automática (`onUpdate`/`preUpdate`), que registra data/hora de modificação; camada de conversão DTO↔entidade (`AtivoMapper` — verificado no código; `toDTO` com complexidade ciclomática 14, risco de manutenção registrado no BRD).
* **Stakeholders & Interests:** Administrador de Patrimônio (cadastro íntegro e centralizado); Gestores de filial (dados corretos para consulta e para o fluxo de manutenção); Auditoria/Compliance (trilha de quem alterou o quê e quando); Diretoria (fonte única de verdade do patrimônio — visão do BRD).
* **Trigger:** Necessidade de criar, atualizar ou excluir um cadastro: ativo patrimonial, filial, departamento, localização física (prédio/andar/sala), tipo de ativo, fornecedor ou funcionário.
* **Preconditions:** Usuário autenticado com perfil ADMIN (BR-01); cadastros de apoio necessários já existentes (ex.: filial, departamento, localização e tipo de ativo para criar um ativo). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a dependência entre cadastros não está explícita na varredura; é coerente com o modelo de dados do BRD.
* **Postconditions (Success Guarantee):** Registro criado (retorno **Created** — BR-06), atualizado ou excluído (retorno **NoContent** — BR-06); data/hora da última modificação registrada automaticamente (BR-08); dados inválidos nunca persistidos (BR-05, BR-09).
* **Scope:** Cadastros base do AegisPatrimônio (ativos e cadastros de apoio).
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Administrador acessa o formulário de cadastro de ativo e informa os dados (filial, departamento, localização, tipo de ativo).
2. Sistema valida os dados — entradas inválidas ou incompletas são recusadas (BR-05).
3. Sistema converte DTO→entidade apenas com dados válidos (BR-09) e persiste o registro (`createAtivo`).
4. Sistema registra automaticamente a data/hora de criação/modificação (`onUpdate`/`preUpdate` — BR-08).
5. Sistema retorna confirmação de criação (**Created** — BR-06); caso de uso encerra com sucesso.

### 3. Alternative Scenarios
#### AS-1: Atualização de cadastro
* **Ponto de extensão:** Passo 1 (a partir do acesso ao cadastro existente).
* **Passos:**
  1. Administrador localiza o registro (consulta — UC-04) e altera os dados.
  2. Sistema valida (BR-05), converte (BR-09) e persiste a atualização.
  3. Sistema mantém a data/hora da última modificação (BR-08).
* **Retorno ao fluxo principal:** Não — encerra com sucesso (atualização concluída).

#### AS-2: Exclusão de cadastro
* **Ponto de extensão:** Passo 1.
* **Passos:**
  1. Administrador solicita a exclusão do registro.
  2. Sistema valida permissão (BR-01/BR-04) e remove o registro (`deletar`).
  3. Sistema retorna **NoContent** (BR-06).
* **Retorno ao fluxo principal:** Não — encerra.

#### AS-3: Cadastros de apoio (mesmo padrão)
* **Ponto de extensão:** Passo 1 (o mesmo fluxo se aplica a cada tipo de cadastro).
* **Passos:**
  1. O mesmo padrão criar/validar/persistir/auditar aplica-se a filiais (`createFilial`), departamentos (`createDepartamento`), localizações físicas — prédio, andar, sala (`createLocalizacao`), tipos de ativo (`createTipoAtivo`), fornecedores (`createFornecedor`) e funcionários (`createFuncionario`).
  2. Para o funcionário, o cadastro base criado aqui é pré-requisito da emissão de credenciais (UC-03).
* **Retorno ao fluxo principal:** Sim — o fluxo é idêntico para cada tipo de cadastro.

### 4. Exception Scenarios
#### EX-1: Dados inválidos ou incompletos
* **Ponto de extensão:** Passo 2 (validação).
* **Condição de erro:** Validação recusa a entrada (dados inválidos ou incompletos).
* **Tratamento:** Retorno **BadRequest** (evidência de teste: `criar_comDadosInvalidos_deveRetornarBadRequest`); erro tratado ao usuário pela camada `handleApiError`; **nenhum registro é criado** (BR-05; DTO nulo não gera registro — BR-09).

#### EX-2: Tentativa de escrita por usuário comum (USER)
* **Ponto de extensão:** Passo 2/3 (validação de permissão).
* **Condição de erro:** Perfil sem permissão de administração tenta criar, atualizar ou excluir.
* **Tratamento:** Acesso negado — **Forbidden** (evidências: `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`); nenhuma alteração ocorre (BR-01).

#### EX-3: Identificador inexistente
* **Ponto de extensão:** Passo 1/2 (localização do registro para atualizar/excluir).
* **Condição de erro:** ID informado não existe.
* **Tratamento:** Retorno "não encontrado" (**NotFound** — evidência: `buscarPorId_comIdInexistente_deveRetornarNotFound`), nunca dados incorretos (BR-05).

### 5. Business Rules Envolvidas
* **BR-01:** Criação, alteração e exclusão de cadastros (ativos, filiais, departamentos, fornecedores, funcionários, tipos de ativo) são restritas a administradores — USER recebe acesso negado.
* **BR-04:** A autorização (`hasPermission`) considera sempre o perfil de acesso e o contexto (filial/departamento).
* **BR-05:** Cadastros com dados inválidos ou incompletos são recusados; identificador inexistente retorna "não encontrado".
* **BR-06:** Criação bem-sucedida retorna **Created**; exclusão bem-sucedida retorna **NoContent**.
* **BR-08:** Todo registro alterado mantém data/hora da última modificação registrada automaticamente (`onUpdate`/`preUpdate`).
* **BR-09:** A conversão DTO→entidade só ocorre com dados válidos; DTO nulo não gera registro.

### 6. Non-Functional Requirements Relevantes
* **Performance:** validação síncrona antes da persistência; latência de criação/atualização compatível com uso interativo — meta numérica a definir (sem telemetria). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Segurança:** 100% das operações de escrita cobertas por `hasPermission` (KPI do BRD: 0 incidentes de acesso indevido em produção).
* **Manutenibilidade:** `AtivoMapper.toDTO` com complexidade ciclomática 14 (achado AST em `mapper/AtivoMapper.java`) — refatorar antes de evoluir o mapeamento (mitigação registrada no BRD).

### 7. Frequency of Use
Média — picos na implantação e na entrada de novos bens; operações pontuais na operação contínua. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* A criação de ativo pressupõe a existência prévia de filial, departamento, localização e tipo de ativo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* O mecanismo de persistência existe, mas não está declarado nas dependências (C-01 do BRD) — confirmar antes de decisões de infraestrutura.

### 9. Open Issues
* Comportamento na exclusão de cadastros com vínculos (ex.: excluir filial com ativos vinculados) não evidenciado na varredura — verificar integridade referencial real.
* Regras de unicidade (ex.: código patrimonial duplicado) não evidenciadas — confirmar com a engenharia.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Cadastros base").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `createAtivo`, `createFilial`, `createDepartamento`, `createLocalizacao`, `createTipoAtivo`, `createFornecedor`, `createFuncionario`, `AtivoMapper`; vincular a `api-specification.md` quando produzida.

---

## UC-03 — Gerir Usuários, Papéis e Permissões

### 1. Characterization
* **Primary Actor:** Administrador (perfil ADMIN).
* **Secondary Actors:** Funcionário (recebe as credenciais); filtro de controle de acesso (`doFilterInternal`), que aplica as permissões a cada requisição a partir da emissão.
* **Stakeholders & Interests:** TI/Segurança (o modelo RBAC contextual é a regra central de proteção — BR-04); Auditoria (rastreabilidade das credenciais emitidas); Gestores de filial (a matriz de permissões por filial determina o que podem consultar).
* **Trigger:** Necessidade de emitir credenciais a funcionário, criar usuário/papel/permissão ou ajustar o acesso contextual por filial/departamento.
* **Preconditions:** Usuário autenticado com perfil ADMIN (BR-01); funcionário existente para o vínculo com credenciais (criado em UC-02). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Postconditions (Success Guarantee):** Usuário e vínculo funcionário↔credenciais criados (`createFuncionarioAndUsuario`); token emitido (`createUserAndToken`); papéis (`createRole`) e permissões (`createPermission`) disponíveis para o modelo RBAC contextual; data/hora registrada (BR-08).
* **Scope:** Controle de acesso do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Administrador acessa a gestão de usuários e seleciona o funcionário que receberá credenciais.
2. Sistema valida os dados e o perfil de acesso (BR-05).
3. Sistema cria o usuário e o vínculo funcionário↔credenciais (`createFuncionarioAndUsuario`) e emite o token (`createUserAndToken`).
4. Sistema registra a data/hora da modificação (BR-08).
5. Caso de uso encerra com sucesso — funcionário apto a autenticar (UC-01) e a operar conforme o contexto concedido (BR-04).

### 3. Alternative Scenarios
#### AS-1: Criar papéis e permissões
* **Ponto de extensão:** Passo 1 (a partir da gestão de acesso).
* **Passos:**
  1. Administrador define papéis (`createRole`) e permissões (`createPermission`).
  2. Sistema registra os dados, que passam a alimentar a verificação contextual `hasPermission` (BR-04).
* **Retorno ao fluxo principal:** Não — encerra como variante de configuração de acesso.

#### AS-2: Ajuste de acesso contextual por filial/departamento
* **Ponto de extensão:** Passo 1.
* **Passos:**
  1. Administrador atualiza as permissões considerando o contexto (filial/departamento) do usuário.
  2. Sistema aplica o novo contexto nas requisições seguintes via `doFilterInternal`/`hasPermission` (BR-04); as consultas por filial passam a respeitar as filiais autorizadas (restrição `findByFilialIdIn` citada no BRD).
* **Retorno ao fluxo principal:** Não — encerra.

### 4. Exception Scenarios
#### EX-1: Dados inválidos
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Dados inválidos ou incompletos na criação de usuário/papel/permissão.
* **Tratamento:** Retorno **BadRequest** (BR-05); erro tratado via `handleApiError`; nenhum registro criado.

#### EX-2: Escrita por usuário comum (USER)
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Perfil sem permissão de administração.
* **Tratamento:** **Forbidden** (BR-01); nenhuma alteração.

#### EX-3: Atualização de nome de usuário com stub
* **Ponto de extensão:** Passo 3 (quando o fluxo envolve atualização de usuário existente).
* **Condição de erro:** `Usuario.setUsername` está com **corpo vazio (stub)** (Constraint C-04 do BRD; achado AST em `src\main\java\br\com\aegispatrimonio\model\Usuario.java:86`).
* **Tratamento:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento incompleto — o nome de usuário pode não ser efetivamente atualizado; mitigação registrada no BRD: implementar o comportamento ou remover o campo do fluxo de atualização, com teste de atualização de usuário cobrindo o caso.

### 5. Business Rules Envolvidas
* **BR-01:** Criação/alteração de usuários, papéis e permissões é restrita a administradores.
* **BR-04:** A autorização considera sempre o perfil de acesso e o contexto (filial/departamento) — RBAC com contexto; é a regra central que este caso de uso configura.
* **BR-05:** Dados inválidos ou incompletos são recusados.
* **BR-06:** Criação bem-sucedida retorna **Created**.
* **BR-08:** Data/hora da última modificação registrada automaticamente.

### 6. Non-Functional Requirements Relevantes
* **Segurança:** o modelo RBAC contextual (`hasPermission` + `doFilterInternal`) deve cobrir 100% das operações de escrita (KPI do BRD: 0 incidentes de acesso indevido); matriz de permissões por filial deve ser mantida (mitigação de risco do BRD).
* **Performance:** a verificação de permissão é executada a cada requisição (via `doFilterInternal`) — sua latência impacta todos os casos de uso e deve ser compatível com uso interativo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 7. Frequency of Use
Baixa a média — emissão de credenciais em admissões de funcionários; ajustes de permissão pontuais. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* Todo usuário do sistema possui funcionário vinculado (o cadastro de funcionário precede a emissão de credenciais). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* Papéis e permissões criados neste caso de uso alimentam diretamente a verificação `hasPermission` (BR-04).

### 9. Open Issues
* Matriz de permissões por filial não documentada na varredura — criar e manter (mitigação de risco registrada no BRD).
* Processo de revogação de credenciais (ex.: desligamento de funcionário) não evidenciado no código — levantar.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Usuários e controle de acesso").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `createUsuario`, `createFuncionarioAndUsuario`, `createUserAndToken`, `createRole`, `createPermission`, `hasPermission`, `doFilterInternal`; vincular a `api-specification.md` quando produzida.

---

## UC-04 — Consultar Listagens e Registros com Filtros

### 1. Characterization
* **Primary Actor:** Usuário autenticado (ADMIN ou USER).
* **Secondary Actors:** Filtro de controle de acesso (`doFilterInternal`); especificação de consulta combinada (`ManutencaoSpecification` — verificado no código; `build` com complexidade ciclomática 14, risco de manutenção registrado no BRD).
* **Stakeholders & Interests:** Gestores de filial (visibilidade do patrimônio da unidade — BR-02); Auditoria (consultas rastreáveis); Técnico de TI (consulta de histórico de saúde — BR-03).
* **Trigger:** Necessidade de consultar uma listagem de registros ou um registro específico.
* **Preconditions:** Usuário autenticado (BR-02); para histórico de saúde, permissão de leitura na filial do ativo (BR-03).
* **Postconditions (Success Guarantee):** Listagem/registro retornado conforme as permissões do usuário; nenhum dado incorreto exposto (BR-05).
* **Scope:** Consultas do AegisPatrimônio (ativos, manutenções, alertas, histórico de saúde).
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Usuário acessa uma listagem (ex.: ativos, manutenções, alertas).
2. Sistema valida a autenticação e aplica o filtro de controle de acesso (`doFilterInternal` — BR-04).
3. Sistema executa a consulta — para manutenções, com filtros combinados montados por `ManutencaoSpecification`; para histórico de saúde, restrito às filiais autorizadas (BR-03).
4. Sistema retorna a listagem/registro (**Ok** — evidência de teste: `listarTodos_comUser_deveRetornarOk`).
5. Caso de uso encerra com sucesso.

### 3. Alternative Scenarios
#### AS-1: Consulta por identificador
* **Ponto de extensão:** Passo 1.
* **Passos:**
  1. Usuário consulta um registro específico por ID (`buscarPorId`).
  2. Sistema valida permissão e retorna o registro; ID inexistente → **NotFound** (BR-05).
* **Retorno ao fluxo principal:** Não — encerra com o resultado da consulta.

#### AS-2: Consulta de manutenções com filtros combinados
* **Ponto de extensão:** Passo 3.
* **Passos:**
  1. Usuário combina filtros de consulta de manutenções.
  2. `ManutencaoSpecification.build` monta a consulta combinada (complexidade ciclomática 14 — risco de manutenção registrado no BRD).
* **Retorno ao fluxo principal:** Sim — retorna ao passo 4 com o resultado filtrado. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** os filtros exatos suportados não estão detalhados na varredura.

#### AS-3: Consulta de histórico de saúde por filial
* **Ponto de extensão:** Passo 3.
* **Passos:**
  1. Usuário solicita o histórico de saúde de um ativo (`getHealthHistory`).
  2. Sistema verifica a permissão de leitura **na filial à qual o ativo pertence** (BR-03) e retorna o histórico.
* **Retorno ao fluxo principal:** Não — encerra.

### 4. Exception Scenarios
#### EX-1: Identificador inexistente
* **Ponto de extensão:** Passo 3/4.
* **Condição de erro:** ID consultado não existe.
* **Tratamento:** **NotFound** — nunca dados incorretos (BR-05); erro tratado via `handleApiError`.

#### EX-2: Acesso a histórico de saúde sem permissão na filial
* **Ponto de extensão:** Passo 3 (variante AS-3).
* **Condição de erro:** Usuário sem permissão de leitura na filial à qual o ativo pertence.
* **Tratamento:** Acesso negado (BR-03); nenhum dado de saúde é retornado.

#### EX-3: Falha de conexão / erro de API
* **Ponto de extensão:** Passo 2/3.
* **Condição de erro:** Backend indisponível ou erro durante a consulta.
* **Tratamento:** `handleApiError` apresenta o erro ao usuário (guardrail do BRD); `console.debug` residual em `frontend/src/services/api.js:107` registra no console — residual a remover antes de produção (risco registrado no BRD).

### 5. Business Rules Envolvidas
* **BR-02:** Usuários autenticados podem consultar listagens completas de registros.
* **BR-03:** O acesso ao histórico de saúde de um ativo exige permissão de leitura na filial à qual o ativo pertence.
* **BR-04:** A autorização considera sempre o perfil de acesso e o contexto (filial/departamento).
* **BR-05:** Consultas por identificador inexistente retornam "não encontrado", nunca dados incorretos.

### 6. Non-Functional Requirements Relevantes
* **Performance:** a latência das consultas de listagem é **guardrail do BRD** (área sensível) — o TODO de performance em `AtivoService` (linha 119) indica que o caminho carrega até 1000 candidatos (id+nome) e faz ranking em memória; monitorar latência e, no futuro, empurrar ranking/filtragem para a camada de consulta com paginação (Future Considerations do BRD). Meta numérica a definir (sem telemetria). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Segurança:** restrição de consultas por filiais autorizadas (`findByFilialIdIn`) deve ser coberta por testes dedicados de acesso por filial (mitigação de risco do BRD).

### 7. Frequency of Use
Alta — consultas múltiplas vezes por dia por usuário ativo (esperadamente o caso de uso mais frequente do sistema). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* As listagens respeitam o escopo de filiais autorizadas do usuário (modelo de permissão contextual — BR-04).
* Os filtros combinados de manutenção são montados por `ManutencaoSpecification` (verificado no código).

### 9. Open Issues
* TODO de performance em `AtivoService` (ranking em memória de até 1000 candidatos) — otimização adiada (Future Considerations do BRD); monitorar latência das listagens como guardrail.
* Paginação das listagens não evidenciada na varredura — confirmar comportamento com grandes volumes.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — consultas e "Fluxo de manutenção").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `listarTodos`, `buscarPorId`, `ManutencaoSpecification`, `getHealthHistory`, `doFilterInternal`; vincular a `api-specification.md` quando produzida.

---

## UC-05 — Iniciar Solicitação de Manutenção

### 1. Characterization
* **Primary Actor:** Gestor de Filial (perfil USER) — ou Administrador.
* **Secondary Actors:** Administrador (recebe a solicitação para aprovação — UC-06); mecanismo de auditoria (`onUpdate`/`preUpdate`).
* **Stakeholders & Interests:** Gestor de filial (fluxo de manutenção formal e rastreável); Administrador (fila de aprovação); Diretoria Administrativa/Financeira (custo de manutenção visível por ativo).
* **Trigger:** Equipamento precisa de manutenção (corretiva ou preventiva). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** o gatilho de negócio não está explícito no código; inferido do domínio patrimonial.
* **Preconditions:** Usuário autenticado; ativo cadastrado (UC-02); usuário com permissão no contexto do ativo (BR-04).
* **Postconditions (Success Guarantee):** Manutenção registrada no estado inicial do fluxo (`iniciar`), rastreável até a aprovação (UC-06); data/hora registrada (BR-08).
* **Scope:** Fluxo de manutenção do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Gestor de filial acessa o ativo e solicita a manutenção.
2. Sistema valida autenticação e permissão no contexto (BR-01/BR-04) e os dados da solicitação (BR-05).
3. Sistema registra a manutenção no estado inicial do fluxo (`iniciar`).
4. Sistema registra a data/hora da modificação (BR-08).
5. Caso de uso encerra com sucesso — manutenção aguardando aprovação (UC-06).

### 3. Alternative Scenarios
#### AS-1: Acompanhamento da solicitação
* **Ponto de extensão:** Passo 5 (após o registro).
* **Passos:**
  1. Gestor acompanha o status da manutenção via consultas com filtros (`ManutencaoSpecification` — UC-04).
* **Retorno ao fluxo principal:** Não — encerra como consulta de acompanhamento.

### 4. Exception Scenarios
#### EX-1: Dados inválidos na solicitação
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Dados inválidos ou incompletos.
* **Tratamento:** **BadRequest** (BR-05); erro tratado via `handleApiError`; manutenção não registrada.

#### EX-2: Ativo inexistente
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Ativo informado não existe.
* **Tratamento:** **NotFound** (BR-05); manutenção não registrada.

#### EX-3: Usuário sem permissão no contexto
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Usuário sem permissão sobre o ativo (filial/departamento fora do contexto autorizado).
* **Tratamento:** **Forbidden** (BR-01/BR-04); manutenção não registrada.

### 5. Business Rules Envolvidas
* **BR-01:** Escrita em cadastros é restrita a administradores; para manutenções, as personas do BRD atribuem ao Gestor de Filial (USER) a condução de solicitações. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** confirmar se `iniciar` exige ADMIN ou aceita USER.
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento).
* **BR-05:** Dados inválidos são recusados; ativo inexistente retorna "não encontrado".
* **BR-08:** Data/hora da modificação registrada automaticamente.

### 6. Non-Functional Requirements Relevantes
* **Performance:** registro da manutenção compatível com uso interativo; sem telemetria, meta a definir. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Auditoria/Segurança:** o registro deve ser rastreável no fluxo de aprovação (objetivo do BRD: fluxo de aprovação auditável).

### 7. Frequency of Use
Média — conforme a demanda de manutenção das filiais; esperadamente várias vezes por semana por unidade. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* O ciclo de manutenção possui os estados inferidos dos métodos verificados no BRD: `iniciar` → `aprovar` → `concluir`/`cancelar`. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a máquina de estados exata e as transições permitidas não estão detalhadas na varredura.
* O Gestor de Filial (USER) pode iniciar solicitações (personas do BRD — já marcadas como inferência no BRD).

### 9. Open Issues
* Transições de estado permitidas e executor de cada transição não detalhados — verificar a implementação real de `iniciar`.
* Campos obrigatórios da solicitação de manutenção (ex.: descrição do problema, prioridade) não evidenciados na varredura.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Fluxo de manutenção").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `iniciar` (serviço de manutenção) e `ManutencaoSpecification`; vincular a `api-specification.md` quando produzida.

---

## UC-06 — Aprovar Manutenção

### 1. Characterization
* **Primary Actor:** Administrador (perfil ADMIN) — aprovador. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** o executor da aprovação não está explícito no BRD/código; inferido do objetivo "restringir escrita a administradores" e de a persona Gestor de Filial apenas "acompanhar aprovação".
* **Secondary Actors:** Gestor de filial (solicitante — acompanha o resultado via consulta); mecanismo de auditoria (`onUpdate`/`preUpdate`).
* **Stakeholders & Interests:** Diretoria Administrativa/Financeira (a aprovação formaliza o gasto de manutenção); Gestor de filial (solicitação atendida); Auditoria/Compliance (rastreabilidade da aprovação — quem aprovou e quando).
* **Trigger:** Manutenção registrada no estado inicial (`iniciar`) aguardando aprovação.
* **Preconditions:** Usuário autenticado com perfil ADMIN; manutenção existente no estado inicial. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** estado prévio exigido inferido da sequência `iniciar` → `aprovar`.
* **Postconditions (Success Guarantee):** Manutenção aprovada e rastreável no fluxo; data/hora da aprovação registrada (BR-08).
* **Scope:** Fluxo de manutenção do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Administrador acessa a fila de manutenções aguardando aprovação (consulta com filtros via `ManutencaoSpecification` — UC-04).
2. Sistema valida autenticação e permissão (BR-01/BR-04).
3. Sistema registra a aprovação (`aprovar`).
4. Sistema registra a data/hora da modificação (BR-08).
5. Caso de uso encerra com sucesso — manutenção apta a ser executada e concluída (UC-07).

### 3. Alternative Scenarios
#### AS-1: Encaminhamento para cancelamento em vez de aprovação
* **Ponto de extensão:** Passo 3 (decisão do aprovador).
* **Passos:**
  1. Administrador avalia a solicitação e decide não aprová-la.
  2. Fluxo segue para o cancelamento da manutenção (`cancelar` — UC-07, AS-1). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** não há método de "rejeição" distinto na varredura; o cancelamento é o único encaminhamento alternativo evidenciado.
* **Retorno ao fluxo principal:** Não — encerra via UC-07.

### 4. Exception Scenarios
#### EX-1: Manutenção inexistente
* **Ponto de extensão:** Passo 2/3.
* **Condição de erro:** Identificador da manutenção não existe.
* **Tratamento:** **NotFound** (BR-05); aprovação não registrada.

#### EX-2: Manutenção em estado não aprovável
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Manutenção não está no estado que permite aprovação (ex.: já aprovada ou cancelada).
* **Tratamento:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** recusa da transição com erro tratado ao usuário; o comportamento exato para transição inválida não está evidenciado na varredura — verificar implementação de `aprovar`.

#### EX-3: Escrita por usuário comum (USER)
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Perfil sem permissão de administração tenta aprovar.
* **Tratamento:** **Forbidden** (BR-01); aprovação não registrada. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** depende da confirmação de quem pode aprovar.

### 5. Business Rules Envolvidas
* **BR-01:** Operações de escrita restritas a administradores — a aprovação é ato de decisão sobre registro de manutenção. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento).
* **BR-05:** Manutenção inexistente retorna "não encontrado".
* **BR-08:** Data/hora da aprovação registrada automaticamente — rastreabilidade obrigatória do fluxo de aprovação.

### 6. Non-Functional Requirements Relevantes
* **Auditoria:** a aprovação deve ser rastreável (objetivo do BRD: "fluxo de manutenção com aprovação auditável"); data/hora garantida por `onUpdate`/`preUpdate` (BR-08).
* **Performance:** o tempo médio do ciclo `iniciar` → `concluir` é KPI do BRD — a aprovação não deve ser gargalo do ciclo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 7. Frequency of Use
Média — proporcional ao volume de solicitações de manutenção (UC-05). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* A aprovação é executada por perfil ADMIN (inferido do objetivo de restringir escrita a administradores). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* A sequência `iniciar` → `aprovar` → `concluir` representa o ciclo feliz do fluxo (métodos verificados no BRD).

### 9. Open Issues
* Definir formalmente quem aprova (perfil e contexto) e se existe fluxo de rejeição com justificativa — não evidenciado na varredura.
* Comportamento para transição inválida (aprovar manutenção já aprovada/cancelada) não evidenciado.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Fluxo de manutenção").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `aprovar` (serviço de manutenção) e `ManutencaoSpecification`; vincular a `api-specification.md` quando produzida.

---

## UC-07 — Concluir ou Cancelar Manutenção

### 1. Characterization
* **Primary Actor:** Gestor de Filial (perfil USER) para conclusão (personas do BRD); cancelamento executado pelo responsável do fluxo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** executor do cancelamento não explícito.
* **Secondary Actors:** Relatório de custo por ativo (`custoTotalPorAtivo`) — a conclusão alimenta o custo acumulado do ativo; mecanismo de auditoria (`onUpdate`/`preUpdate`).
* **Stakeholders & Interests:** Diretoria (custo acumulado por ativo atualizado — base para decisão de substituição/reparo/desativação); Gestor de filial (fluxo encerrado formalmente); Auditoria (rastreabilidade do encerramento).
* **Trigger:** Serviço de manutenção executado (concluir) ou solicitação descartada (cancelar). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** gatilho de negócio inferido do domínio.
* **Preconditions:** Usuário autenticado; manutenção existente em estado compatível — aprovada para conclusão; em aberto para cancelamento. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Postconditions (Success Guarantee):** Manutenção concluída ou cancelada; data/hora registrada (BR-08); custo acumulado do ativo refletido no relatório `custoTotalPorAtivo` (UC-10). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a relação entre conclusão e custo não está explícita na varredura; inferida da finalidade declarada do indicador no BRD.
* **Scope:** Fluxo de manutenção do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path — conclusão)
1. Responsável acessa a manutenção aprovada.
2. Sistema valida autenticação e permissão (BR-01/BR-04) e o estado da manutenção.
3. Sistema registra a conclusão (`concluir`).
4. Sistema registra a data/hora da modificação (BR-08).
5. Caso de uso encerra com sucesso — custo acumulado do ativo atualizado em `custoTotalPorAtivo` (UC-10). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 3. Alternative Scenarios
#### AS-1: Cancelamento da manutenção
* **Ponto de extensão:** Passo 2 (a partir da manutenção em aberto).
* **Passos:**
  1. Responsável solicita o cancelamento da manutenção.
  2. Sistema valida permissão e estado, e registra o cancelamento (`cancelar`).
  3. Sistema registra a data/hora (BR-08).
* **Retorno ao fluxo principal:** Não — encerra o fluxo daquela manutenção sem execução.

### 4. Exception Scenarios
#### EX-1: Estado incompatível com a transição
* **Ponto de extensão:** Passo 2/3.
* **Condição de erro:** Tentativa de concluir manutenção não aprovada, ou cancelar manutenção já concluída.
* **Tratamento:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** recusa da transição com erro tratado ao usuário; comportamento exato não evidenciado na varredura — verificar implementação de `concluir`/`cancelar`.

#### EX-2: Manutenção inexistente
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Identificador não existe.
* **Tratamento:** **NotFound** (BR-05).

### 5. Business Rules Envolvidas
* **BR-01:** Operações de escrita controladas por perfil. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** confirmar executor de cada transição.
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento).
* **BR-05:** Manutenção inexistente retorna "não encontrado".
* **BR-08:** Data/hora do encerramento registrada automaticamente.

### 6. Non-Functional Requirements Relevantes
* **Performance:** o tempo médio do ciclo `iniciar` → `concluir` é KPI do BRD (meta: 100% dos ativos com custo acumulado visível até o 2º trimestre pós-go-live).
* **Auditoria:** encerramento rastreável com data/hora garantida (BR-08).

### 7. Frequency of Use
Média — proporcional ao volume de manutenções aprovadas (UC-06) e solicitações descartadas. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* A conclusão de manutenções alimenta o custo acumulado por ativo (`custoTotalPorAtivo`). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* O Gestor de Filial (USER) pode concluir manutenções (personas do BRD — já marcadas como inferência no BRD).

### 9. Open Issues
* Campos registrados na conclusão (ex.: custo real, observações, peças substituídas) não evidenciados na varredura — levantar com a engenharia.
* Comportamento para transição inválida não evidenciado.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Fluxo de manutenção").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `concluir`, `cancelar` (serviço de manutenção) e `custoTotalPorAtivo`; vincular a `api-specification.md` quando produzida.

---

## UC-08 — Registrar Health Check e Consultar Histórico de Saúde

### 1. Characterization
* **Primary Actor:** Técnico de TI / Monitoramento.
* **Secondary Actors:** Gestor de filial (consulta o histórico — BR-03); sistema de alertas de uso de recursos (UC-09). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a relação entre health check e alertas não está explícita na varredura; inferida da finalidade comum de monitoramento.
* **Stakeholders & Interests:** Técnico de TI (antecipação de falhas); Gestor de filial (condição operacional dos equipamentos da unidade); Diretoria (priorização de manutenção baseada em evidência — objetivo do BRD).
* **Trigger:** Ciclo de monitoramento do equipamento — coleta de dados de hardware e disco. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** periodicidade não definida no código.
* **Preconditions:** Usuário autenticado; ativo cadastrado (UC-02); para consulta do histórico, permissão de leitura na filial do ativo (BR-03).
* **Postconditions (Success Guarantee):** Health check registrado com dados de hardware e disco (`updateHealthCheck`); métricas escalares atualizadas (`updateScalars`); histórico consultável via `getHealthHistory` com permissão por filial (BR-03); dados registrados "para viabilizar análise preditiva de falhas" (finalidade declarada no BRD).
* **Scope:** Monitoramento de saúde dos ativos do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path — registro de health check)
1. Técnico acessa o ativo e registra o health check com dados de hardware e disco (`updateHealthCheck`).
2. Sistema valida autenticação e permissão no contexto (BR-04) e os dados coletados (BR-05).
3. Sistema atualiza as métricas escalares do ativo (`updateScalars`).
4. Sistema registra a data/hora da modificação (BR-08).
5. Caso de uso encerra com sucesso — histórico de saúde atualizado e consultável (`getHealthHistory`).

### 3. Alternative Scenarios
#### AS-1: Consulta do histórico de saúde
* **Ponto de extensão:** Passo 5 (ou diretamente, sem novo registro).
* **Passos:**
  1. Usuário solicita o histórico de saúde do ativo (`getHealthHistory`).
  2. Sistema verifica a permissão de leitura **na filial à qual o ativo pertence** (BR-03) e retorna o histórico.
* **Retorno ao fluxo principal:** Não — encerra como consulta.

#### AS-2: Atualização do inventário de hardware
* **Ponto de extensão:** Passo 1 (quando há substituição de componentes).
* **Passos:**
  1. Fluxo segue para a atualização do inventário de hardware (UC-11) — limpeza dos componentes antigos antes do registro dos novos (BR-10).
* **Retorno ao fluxo principal:** Não — encerra via UC-11.

### 4. Exception Scenarios
#### EX-1: Dados de health check inválidos
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Dados de hardware/disco inválidos ou incompletos.
* **Tratamento:** **BadRequest** (BR-05); erro tratado via `handleApiError`; health check não registrado.

#### EX-2: Acesso ao histórico sem permissão na filial
* **Ponto de extensão:** AS-1, passo 2.
* **Condição de erro:** Usuário sem permissão de leitura na filial do ativo.
* **Tratamento:** Acesso negado (BR-03); nenhum dado de saúde retornado.

#### EX-3: Ativo inexistente
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Ativo informado não existe.
* **Tratamento:** **NotFound** (BR-05).

### 5. Business Rules Envolvidas
* **BR-03:** O acesso ao histórico de saúde exige permissão de leitura na filial à qual o ativo pertence.
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento).
* **BR-05:** Dados inválidos são recusados; ativo inexistente retorna "não encontrado".
* **BR-08:** Data/hora da modificação registrada automaticamente — base temporal do histórico de saúde.
* **BR-10:** (indireta, via UC-11) a atualização do inventário de hardware limpa os componentes antigos antes de registrar os novos.

### 6. Non-Functional Requirements Relevantes
* **Performance:** KPI do BRD — ≥ 90% dos ativos monitorados (com `updateHealthCheck` atualizado) até o 2º trimestre pós-go-live; a coleta deve ser compatível com o ciclo de monitoramento definido. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Segurança:** leitura do histórico restrita por filial (BR-03) — cobrir com testes dedicados de acesso por filial (mitigação de risco do BRD).
* **Evolutividade:** os dados de hardware e disco são registrados para viabilizar a análise preditiva de falhas (Future Considerations do BRD) — o formato de registro deve preservar essa finalidade.

### 7. Frequency of Use
Alta para ativos monitorados — conforme o ciclo de coleta de métricas. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** periodicidade não definida no código.

### 8. Assumptions
* O health check compreende dados de hardware e disco, e as métricas escalares são atualizadas em conjunto (`updateHealthCheck` + `updateScalars` — verificados no BRD).
* A consulta do histórico é o mecanismo de acesso aos dados de saúde (a análise preditiva avançada é adiada — BRD).

### 9. Open Issues
* Periodicidade e formato exato do health check não evidenciados na varredura — levantar com a engenharia.
* Critérios que conectam o histórico de saúde aos alertas de uso de recursos (UC-09) não evidenciados.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Monitoramento de saúde dos ativos").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `updateHealthCheck`, `updateScalars`, `getHealthHistory`; vincular a `api-specification.md` quando produzida.

---

## UC-09 — Gerir Alertas de Uso de Recursos

### 1. Characterization
* **Primary Actor:** Técnico de TI / Monitoramento (verificação e acompanhamento) e Gestor de Filial (consulta e baixa). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** distribuição de responsabilidades entre perfis inferida das personas do BRD.
* **Secondary Actors:** Mecanismo de auditoria (`onUpdate`/`preUpdate`); ativos monitorados (fonte das métricas de uso de recursos — UC-08). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Stakeholders & Interests:** Técnico de TI (priorização de manutenção baseada em evidência); Gestor de filial (visibilidade dos alertas da unidade); Diretoria (redução do custo reativo de manutenção — custo de inação do BRD).
* **Trigger:** Verificação de uso de recursos dos ativos (`checkResourceUsageAlerts`). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** não há agendador evidenciado no código; o disparo (manual, agendado ou por evento) precisa ser confirmado.
* **Preconditions:** Usuário autenticado; ativos com métricas de uso de recursos disponíveis (UC-08). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Postconditions (Success Guarantee):** Alertas gerados para os casos que excedem os critérios; alertas consultáveis (`listarAlertas`, `getRecentAlerts`); baixa formal registrada (`markAsRead`); data/hora registrada (BR-08).
* **Scope:** Alertas de uso de recursos do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. A verificação de uso de recursos é executada (`checkResourceUsageAlerts` — método com a maior complexidade ciclomática do codebase: 17; risco de manutenção registrado no BRD).
2. Sistema gera alertas para os casos que excedem os critérios de uso de recursos.
3. Usuário autenticado consulta os alertas gerais e recentes (`listarAlertas`, `getRecentAlerts` — BR-02).
4. Responsável dá baixa formal no alerta (`markAsRead`).
5. Caso de uso encerra com sucesso — alerta reconhecido (KPI do BRD: % de alertas reconhecidos dentro do SLA).

### 3. Alternative Scenarios
#### AS-1: Verificação sem alertas a gerar
* **Ponto de extensão:** Passo 2.
* **Passos:**
  1. Verificação avalia os ativos e nenhum excede os critérios.
  2. Nenhum alerta é gerado; o fluxo encerra sem registro.
* **Retorno ao fluxo principal:** Não — encerra. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** critérios de geração de alerta não evidenciados na varredura.

#### AS-2: Consulta apenas de alertas recentes
* **Ponto de extensão:** Passo 3.
* **Passos:**
  1. Usuário consulta apenas os alertas recentes (`getRecentAlerts`).
* **Retorno ao fluxo principal:** Não — encerra como consulta.

### 4. Exception Scenarios
#### EX-1: Falha na verificação de uso de recursos
* **Ponto de extensão:** Passo 1.
* **Condição de erro:** Erro durante a verificação (`checkResourceUsageAlerts`).
* **Tratamento:** Erro tratado ao usuário; mitigação registrada no BRD: refatorar o método (complexidade 17) em funções menores antes de evoluir a área, mantendo testes de regressão.

#### EX-2: Baixa de alerta sem permissão
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** Perfil sem permissão para a baixa formal.
* **Tratamento:** **Forbidden** (BR-01/BR-04). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a varredura não explicita se `markAsRead` exige ADMIN; inferido do padrão de restrição de escrita a administradores (BR-01).

### 5. Business Rules Envolvidas
* **BR-01:** A baixa formal (`markAsRead`) é operação de escrita — restrita a administradores. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **BR-02:** Usuários autenticados podem consultar as listagens de alertas (`listarAlertas`, `getRecentAlerts`).
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento).
* **BR-05:** Alerta inexistente na baixa retorna "não encontrado".
* **BR-08:** Data/hora da baixa registrada automaticamente.

### 6. Non-Functional Requirements Relevantes
* **Performance:** KPI do BRD — % de alertas de uso de recursos reconhecidos (`markAsRead`) dentro do SLA até o 2º trimestre pós-go-live; SLA numérico a definir. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Manutenibilidade:** `checkResourceUsageAlerts` tem a maior complexidade ciclomática do codebase (17 — achado AST em `service/AlertNotificationService.java`) — refatorar antes de evoluir (mitigação do BRD).

### 7. Frequency of Use
Alta para a verificação (ciclo de monitoramento) e média para consulta/baixa. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* A verificação de uso de recursos consome as métricas registradas pelo monitoramento de saúde (UC-08). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* A baixa formal (`markAsRead`) é o mecanismo de reconhecimento do alerta (verificado no BRD).

### 9. Open Issues
* Critérios de geração de alerta (limiares de uso de recursos) não evidenciados na varredura — levantar com a engenharia.
* Disparo da verificação (manual, agendado ou por evento) não evidenciado — confirmar.
* SLA de reconhecimento de alertas a definir (KPI do BRD sem valor numérico).

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Alertas").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `checkResourceUsageAlerts`, `listarAlertas`, `getRecentAlerts`, `markAsRead`; vincular a `api-specification.md` quando produzida.

---

## UC-10 — Consultar Custo Total por Ativo

### 1. Characterization
* **Primary Actor:** Gestor de Filial / Administrador — e, indiretamente, a Diretoria Administrativa/Financeira (consumidora da informação para decisão).
* **Secondary Actors:** Fluxo de manutenção (fonte dos custos acumulados — UC-05/06/07). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a origem dos custos não está explícita na varredura; inferida da finalidade declarada do indicador.
* **Stakeholders & Interests:** Diretoria (decisões de reparo, substituição e desativação apoiadas em dados — visão do BRD); Gestores de filial (custo dos bens da unidade); Auditoria (conferência dos custos).
* **Trigger:** Necessidade de decisão sobre substituição, reparo ou desativação de um ativo — o indicador existe "para apoiar decisões sobre substituição, reparo ou desativação" (finalidade declarada no BRD).
* **Preconditions:** Usuário autenticado (BR-02); ativo cadastrado; histórico de manutenção do ativo disponível para custo acumulado. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Postconditions (Success Guarantee):** Custo total de manutenção acumulado por ativo apresentado ao usuário.
* **Scope:** Relatórios gerenciais do AegisPatrimônio.
* **Level:** User goal.

### 2. Main Scenario (Happy Path)
1. Usuário acessa o relatório de custo total por ativo.
2. Sistema valida autenticação (BR-02) e permissão contextual (BR-04).
3. Sistema calcula/apresenta o custo total de manutenção acumulado do ativo (`custoTotalPorAtivo`).
4. Sistema retorna o resultado.
5. Caso de uso encerra com sucesso.

### 3. Alternative Scenarios
#### AS-1: Consulta comparativa para decisão de desativação
* **Ponto de extensão:** Passo 1.
* **Passos:**
  1. Gestor consulta o custo acumulado de vários ativos para comparar candidatos a desativação/substituição.
  2. Sistema retorna os custos por ativo para a comparação.
* **Retorno ao fluxo principal:** Não — encerra como consulta comparativa. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** cenário de uso inferido da finalidade do indicador.

### 4. Exception Scenarios
#### EX-1: Ativo sem histórico de manutenção
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Ativo sem manutenções registradas (custo acumulado inexistente ou zero).
* **Tratamento:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** apresentação de custo zero ou indicação "sem dados"; comportamento exato não evidenciado na varredura.

#### EX-2: Ativo inexistente
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Ativo informado não existe.
* **Tratamento:** **NotFound** (BR-05).

### 5. Business Rules Envolvidas
* **BR-02:** Usuários autenticados podem consultar — o relatório é consulta.
* **BR-04:** A autorização considera sempre o perfil e o contexto (filial/departamento) — o custo é visível conforme o contexto autorizado.
* **BR-05:** Ativo inexistente retorna "não encontrado", nunca dados incorretos.

### 6. Non-Functional Requirements Relevantes
* **Performance:** KPI do BRD — 100% dos ativos com custo acumulado visível até o 2º trimestre pós-go-live; latência compatível com uso interativo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Integridade:** o custo apresentado deve refletir o histórico real de manutenções (fonte única de verdade — visão do BRD).

### 7. Frequency of Use
Baixa a média — ciclos de decisão gerencial (substituição, reparo, desativação). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 8. Assumptions
* O custo acumulado origina-se do histórico de manutenções do ativo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* O indicador `custoTotalPorAtivo` é o mecanismo de consulta do custo (verificado no BRD).

### 9. Open Issues
* Composição do custo (ex.: mão de obra, peças, custos indiretos) não evidenciada na varredura — levantar com a engenharia.
* Se o relatório suporta consolidação por filial/departamento não evidenciado.

### 10. Related Artifacts
* **User Stories:** a definir — artefato ainda não gerado; escopo de origem no BRD (seção 5, In-Scope — "Relatórios gerenciais").
* **User Flow:** a definir — artefato ainda não gerado.
* **API envolvida:** nenhuma rota detectada na varredura — rastreabilidade via `custoTotalPorAtivo`; vincular a `api-specification.md` quando produzida.

---

## UC-11 — Atualizar Inventário de Hardware do Ativo

### 1. Characterization
* **Primary Actor:** Técnico de TI / Monitoramento.
* **Secondary Actors:** Monitoramento de saúde (UC-08) — o inventário compõe os dados de hardware do ativo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Stakeholders & Interests:** Técnico de TI (inventário fiel do equipamento); Gestor de filial (condição operacional); Diretoria (base para a futura análise preditiva de falhas — Future Considerations do BRD).
* **Trigger:** Substituição ou alteração de componentes do equipamento — adaptadores de rede, discos e memórias. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** gatilho inferido da natureza dos componentes.
* **Preconditions:** Usuário autenticado; ativo com detalhe de hardware