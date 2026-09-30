# User Stories Specification (BDD) — a6 · AegisPatrimônio

> **Épico relacionado:** Múltiplos épicos — ver Mapa de Épicos abaixo · **Sprint:** A definir (backlog inicial — nenhum planejamento de sprint detectado no workspace) · **Status geral:** Backlog

**Fontes e método:** este documento deriva do **Catálogo de Casos de Uso (Use Cases) v1.0** (artefato-fonte prioritário, que por sua vez deriva do BRD v1.0) e do **diagnóstico determinístico do codebase** (353 arquivos, ~27.912 LOC, 1.287 funções, 347 classes). Cada story cobre os fluxos de caminho feliz, borda e erro do caso de uso correspondente, em formato BDD/Gherkin.

**Nota de rastreabilidade (rotas/tabelas):** a varredura do workspace **não detectou rotas/IPC/handlers explícitos e nenhum schema de banco de dados**. Por isso, a ancoragem técnica das stories é feita nos **métodos de serviço e componentes verificados no código** (ex.: `createAtivo`, `iniciar`, `aprovar`, `concluir`, `cancelar`, `updateHealthCheck`, `updateScalars`, `getHealthHistory`, `checkResourceUsageAlerts`, `listarAlertas`, `getRecentAlerts`, `markAsRead`, `custoTotalPorAtivo`, `hasPermission`, `doFilterInternal`, `ManutencaoSpecification`, `AtivoMapper`, `RealisticDataSeeder`) e na **camada de serviços do frontend** (`request`/`handleResponse`/`handleApiError` em `frontend/src/services/api.js`, com `authInterceptor` anexando credenciais a cada chamada). Quando a especificação de API for produzida, cada cenário deve ser vinculado à rota correspondente.

**Nota sobre UC-11, UC-12 e UC-13:** os três casos de uso constam do índice do catálogo-fonte; o conteúdo detalhado de UC-11 está **truncado** no material disponível e as seções completas de UC-12/UC-13 **não foram incluídas**. As stories correspondentes (US-011 a US-013) foram derivadas do índice, dos achados AST e das regras de negócio citadas, com todas as inferências explicitamente marcadas.

**Legenda de NFRs:** os artefatos-fonte não trazem um registro de NFRs com IDs explícitos; as categorias abaixo são as efetivamente usadas nas seções 6 dos casos de uso. Os IDs foram atribuídos apenas para viabilizar a rastreabilidade. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

| ID | Categoria | Escopo (conforme os casos de uso) |
| :--- | :--- | :--- |
| NFR-01 | Performance | Latência de consultas/listagens (guardrail do BRD) e de operações interativas; TODO de performance em `AtivoService` (ranking de até 1000 candidatos em memória) |
| NFR-02 | Segurança | RBAC contextual (`hasPermission` + `doFilterInternal`) cobrindo 100% das escritas; invalidação imediata de token no logout (BR-07); KPI do BRD: 0 incidentes de acesso indevido em produção |
| NFR-03 | Manutenibilidade | Refatorar pontos de alta complexidade ciclomática (achados AST): `checkResourceUsageAlerts` (17), `RealisticDataSeeder.run` (15), `AtivoMapper.toDTO` (14), `ManutencaoSpecification.build` (14), `request` (13) |
| NFR-04 | Disponibilidade | Sem SLO, clustering ou redundância evidenciados no código — requisitos a definir pela engenharia |
| NFR-05 | Auditoria e Integridade | Data/hora de modificação registrada automaticamente (`onUpdate`/`preUpdate` — BR-08); custo reflete o histórico real; fluxo de aprovação auditável |
| NFR-06 | Evolutividade | Dados de hardware/disco preservados para análise preditiva de falhas; paginação/filtragem na camada de consulta (Future Considerations do BRD) |

**Mapa de Épicos:** o agrupamento deriva dos itens In-Scope do BRD citados no Catálogo de Casos de Uso. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (agrupamento organizacional; os itens In-Scope citados são reais)

| Épico | Nome (item In-Scope do BRD) | Stories |
| :--- | :--- | :--- |
| EP-01 | Sessão | US-001 |
| EP-02 | Cadastros base | US-002 |
| EP-03 | Usuários e controle de acesso | US-003 |
| EP-04 | Consultas e listagens | US-004 |
| EP-05 | Fluxo de manutenção | US-005, US-006, US-007 |
| EP-06 | Monitoramento de saúde dos ativos | US-008, US-011 |
| EP-07 | Alertas | US-009 |
| EP-08 | Relatórios gerenciais | US-010 |
| EP-09 | Operação técnica de homologação (carga de dados) | US-012 |
| EP-10 | Auditoria e compliance | US-013 |

**Premissas globais de estimativa:** todas as prioridades MoSCoW e story points deste documento são **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. Premissas adotadas: (a) prioridade derivada do objetivo/KPI do BRD citado no UC correspondente e da dependência entre stories; (b) complexidade derivada do número de fluxos do UC (feliz/alternativos/exceções), da quantidade de componentes tocados e dos achados AST de complexidade ciclomática; (c) não há telemetria no repositório, portanto não existem dados históricos de esforço.

### Visão Geral das Stories

| ID | Título | Épico | Priority | Complexity | UC de origem |
| :--- | :--- | :--- | :--- | :--- | :--- |
| US-001 | Autenticar e Gerir Sessão (Login e Logout) | EP-01 | Must | 5 | UC-01 |
| US-002 | Gerir Cadastros Base do Patrimônio | EP-02 | Must | 8 | UC-02 |
| US-003 | Gerir Usuários, Papéis e Permissões | EP-03 | Must | 8 | UC-03 |
| US-004 | Consultar Listagens e Registros com Filtros | EP-04 | Must | 5 | UC-04 |
| US-005 | Iniciar Solicitação de Manutenção | EP-05 | Must | 3 | UC-05 |
| US-006 | Aprovar Manutenção | EP-05 | Must | 3 | UC-06 |
| US-007 | Concluir ou Cancelar Manutenção | EP-05 | Must | 5 | UC-07 |
| US-008 | Registrar Health Check e Consultar Histórico de Saúde | EP-06 | Should | 5 | UC-08 |
| US-009 | Gerir Alertas de Uso de Recursos | EP-07 | Should | 8 | UC-09 |
| US-010 | Consultar Custo Total por Ativo | EP-08 | Should | 3 | UC-10 |
| US-011 | Atualizar Inventário de Hardware do Ativo | EP-06 | Should | 5 | UC-11 (parcial) |
| US-012 | Executar Carga de Dados Realista para Homologação | EP-09 | Could | 3 | UC-12 (índice) |
| US-013 | Consultar Eventos de Auditoria e Histórico de Modificações | EP-10 | Should | 5 | UC-13 (índice) |

---

## US-001 — Autenticar e Gerir Sessão (Login e Logout)

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — gateway de todos os demais casos de uso; meta do BRD: 0 incidentes em produção
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: 2 fluxos principais (login/logout) + 2 exceções; componentes de backend e frontend
* **Épico:** EP-01 — Acesso e Sessão
* **Labels:** backend, frontend, segurança

### 1. Description (INVEST)
* **As a** usuário do sistema (perfil ADMIN ou USER) com credenciais emitidas
* **I want to** autenticar-me com minhas credenciais e encerrar a sessão quando desejar
* **So that** eu acesse o AegisPatrimônio com as credenciais anexadas automaticamente a cada chamada e o token seja invalidado imediatamente ao encerrar a sessão

*Checklist INVEST:* Independent (não depende de outras stories para existir; apenas do vínculo funcionário↔usuário de US-003 para credenciais reais); Negotiable (mecanismo de token negociável); Valuable (habilita 100% dos demais casos de uso); Estimable (fluxos claros no UC-01); Small (escopo restrito a sessão); Testable (cenários BDD abaixo).

### 2. Business Context
Todo acesso ao sistema passa por esta story — sem sessão válida, nenhum outro caso de uso opera. A meta do BRD de **0 incidentes de acesso indevido em produção** começa aqui: a credencial é validada a cada requisição (`doFilterInternal`) e o logout invalida o token imediatamente (BR-07). A auditoria/compliance depende da rastreabilidade de sessão; os gestores de filial dependem do acesso para operar o patrimônio da unidade.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Login com credenciais válidas — caminho feliz
* **Given** um funcionário com credenciais emitidas e vínculo funcionário↔usuário estabelecido (US-003 / `createFuncionarioAndUsuario`)
* **When** o usuário informa as credenciais na tela de login e o frontend envia a solicitação pela camada de serviços (`request` em `frontend/src/services/api.js`)
* **Then** o sistema valida as credenciais, emite o token de acesso (`createUserAndToken`) e estabelece a sessão; a partir de então `authInterceptor` anexa automaticamente as credenciais a cada chamada e `doFilterInternal` valida a credencial a cada requisição (BR-04)

#### Scenario 2: Logout com invalidação imediata — caso de borda
* **Given** um usuário autenticado com sessão válida e chamadas em curso
* **When** o usuário aciona o logout
* **Then** o sistema invalida **imediatamente** o token de acesso (BR-07) e encerra a sessão (`clearSession`); qualquer chamada subsequente com o token antigo é recusada e nova autenticação é exigida

#### Scenario 3: Credenciais inválidas — caminho de erro
* **Given** um usuário na tela de login sem sessão válida
* **When** o usuário informa credenciais que não correspondem a um usuário válido
* **Then** o sistema recusa o login com mensagem de erro tratada pela camada `handleApiError` e nenhuma sessão é estabelecida **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** texto exato da mensagem não evidenciado na varredura

#### Scenario 4: Falha de conexão com o backend — caminho de erro
* **Given** um usuário na tela de login
* **When** o backend está indisponível ou ocorre falha de rede durante a chamada `request`
* **Then** `handleApiError` captura a falha e apresenta o erro ao usuário (guardrail do BRD: taxa de erros de API tratados ao usuário); o `console.error` residual em `frontend/src/services/api.js:44` deve ser removido antes de produção **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** nenhuma política de retry automático evidenciada no código

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace (varredura determinística) — a criar; telas de login e indicador de sessão.
* **Estados da interface:** loading (durante `request` de login), success (sessão estabelecida), error (credenciais inválidas / falha de conexão via `handleApiError`), empty (não aplicável). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* A variante de validação `mockLogin` (AS-1 do UC-01) deve estar claramente sinalizada como ambiente de homologação — nunca disponível em produção.

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `createUserAndToken`, `clearSession`, `authInterceptor`, `doFilterInternal`, `request`/`handleResponse`/`handleApiError` (`frontend/src/services/api.js`).
* **Dependências técnicas:** nenhuma rota/handler detectado na varredura — vincular às rotas quando `api-specification.md` for produzida; mecanismo de persistência de sessão não declarado nas dependências (Constraint C-01 do BRD).
* **Considerações de performance/segurança:** invalidação imediata de token (BR-07) é requisito de segurança obrigatório (NFR-02); `request` tem complexidade ciclomática 13 (achado AST em `frontend/src/services/api.js:54`) — refatorar em funções menores antes de evoluir a camada de serviços (NFR-03); latência do login compatível com uso interativo (NFR-01, meta numérica a definir).

### 6. Out of Scope
* Emissão de credenciais e gestão de papéis/permissões (US-003).
* Recuperação de senha / self-service de credenciais — não evidenciado no código.
* Integrações com sistemas de terceiros (Out-of-Scope do BRD — única dependência de produção declarada: `@popperjs/core ^2.11.8`).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (vínculo funcionário↔usuário — US-003)
- [ ] Design/UX aprovado (se aplicável — tela de login)
- [ ] Estimativa de esforço realizada
- [ ] Mecanismo de emissão/validação/invalidação de token e política de expiração levantados com a engenharia (open issues do UC-01)

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (login válido, logout, credenciais inválidas, falha de conexão)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (código Java compilado sem erros; frontend em JavaScript — não há TypeScript no workspace)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — não há telemetria no repositório hoje)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-03, NFR-04
* **Use Cases relacionados:** UC-01 (origem), UC-03 (emissão de credenciais)
* **Regras de Negócio:** BR-02, BR-04, BR-07
* **Riscos relacionados:** complexidade ciclomática 13 em `request` (achado AST — mitigação registrada no BRD); `console.error` residual em `api.js:44` (remover antes de produção); mecanismo de token não documentado (open issue do UC-01)

---

## US-002 — Gerir Cadastros Base do Patrimônio

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — fonte única de verdade do patrimônio (visão do BRD); pré-requisito de US-005, US-008 e US-010
* **Complexity:** 8 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: 7 tipos de cadastro (ativo, filial, departamento, localização física, tipo de ativo, fornecedor, funcionário) × criar/atualizar/excluir; `AtivoMapper.toDTO` com complexidade 14
* **Épico:** EP-02 — Cadastros Base do Patrimônio
* **Labels:** backend, frontend, dados

### 1. Description (INVEST)
* **As a** administrador de patrimônio (perfil ADMIN)
* **I want to** criar, atualizar e excluir ativos patrimoniais e cadastros de apoio (filiais, departamentos, localizações físicas — prédio/andar/sala, tipos de ativo, fornecedores, funcionários)
* **So that** o patrimônio tenha uma fonte única de verdade íntegra e centralizada, com data/hora de modificação registrada automaticamente

*Checklist INVEST:* Independent (o fluxo é idêntico por tipo de cadastro; ordem de implantação depende dos cadastros de apoio); Negotiable; Valuable (base de todos os fluxos de consulta, manutenção e monitoramento); Estimable (evidências de teste já existentes no código); Small (um padrão CRUD repetido); Testable (cenários BDD abaixo).

### 2. Business Context
O cadastro íntegro é a condição para que gestores de filial consultem dados corretos e para que o fluxo de manutenção e o monitoramento de saúde operem sobre ativos reais. A diretoria depende da fonte única de verdade (visão do BRD); a auditoria depende da trilha de quem alterou o quê e quando (BR-08). O KPI do BRD de 0 incidentes de acesso indevido exige que 100% das escritas sejam cobertas por `hasPermission` (BR-01).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Criação de ativo com dados válidos — caminho feliz
* **Given** um administrador autenticado (BR-01) e os cadastros de apoio existentes (filial, departamento, localização, tipo de ativo)
* **When** o administrador submete o formulário de cadastro de ativo com dados válidos
* **Then** o sistema valida os dados (BR-05), converte DTO→entidade apenas com dados válidos (BR-09), persiste o registro (`createAtivo`), registra data/hora automaticamente (`onUpdate`/`preUpdate` — BR-08) e retorna confirmação de criação (Created — BR-06)

#### Scenario 2: Exclusão de cadastro — caso de borda
* **Given** um administrador autenticado e um registro existente localizado via consulta (US-004)
* **When** o administrador solicita a exclusão do registro
* **Then** o sistema valida permissão (BR-01/BR-04), remove o registro (`deletar`) e retorna NoContent (BR-06)
* Nota: comportamento para exclusão de cadastros com vínculos (ex.: filial com ativos vinculados) não evidenciado na varredura — verificar integridade referencial real (open issue do UC-02).

#### Scenario 3: Dados inválidos ou incompletos — caminho de erro
* **Given** um administrador autenticado no formulário de cadastro
* **When** o administrador submete dados inválidos ou incompletos
* **Then** o sistema retorna BadRequest (evidência de teste: `criar_comDadosInvalidos_deveRetornarBadRequest`), o erro é tratado ao usuário via `handleApiError` e nenhum registro é criado (BR-05; DTO nulo não gera registro — BR-09)

#### Scenario 4: Escrita por usuário comum (USER) — caminho de erro
* **Given** um usuário autenticado com perfil USER
* **When** o usuário tenta criar, atualizar ou excluir um cadastro
* **Then** o sistema nega o acesso com Forbidden (evidências: `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`) e nenhuma alteração ocorre (BR-01)

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; formulários por tipo de cadastro com campos de contexto (filial/departamento/localização).
* **Estados da interface:** loading (durante persistência), success (Created/NoContent), error (BadRequest/Forbidden via `handleApiError`), empty (listagem sem registros — ver US-004). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `createAtivo`, `createFilial`, `createDepartamento`, `createLocalizacao`, `createTipoAtivo`, `createFornecedor`, `createFuncionario`, `deletar`, `AtivoMapper` (conversão DTO↔entidade), `onUpdate`/`preUpdate` (auditoria automática).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; mecanismo de persistência não declarado nas dependências (C-01 do BRD); regras de unicidade (ex.: código patrimonial duplicado) não evidenciadas — confirmar com a engenharia.
* **Considerações de performance/segurança:** validação síncrona antes da persistência (NFR-01); 100% das operações de escrita cobertas por `hasPermission` (NFR-02); `AtivoMapper.toDTO` com complexidade ciclomática 14 (achado AST em `mapper/AtivoMapper.java`) — refatorar antes de evoluir o mapeamento (NFR-03).

### 6. Out of Scope
* Emissão de credenciais ao funcionário (US-003 — o cadastro de funcionário criado aqui é pré-requisito).
* Importação/exportação em massa de cadastros — não evidenciada no código.
* Carga de dados realista para homologação (US-012).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (cadastros de apoio precedem o ativo)
- [ ] Design/UX aprovado (se aplicável — formulários)
- [ ] Estimativa de esforço realizada
- [ ] Comportamento de exclusão com vínculos e regras de unicidade confirmados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (criação válida, exclusão, dados inválidos, escrita por USER)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-03, NFR-05
* **Use Cases relacionados:** UC-02 (origem), UC-03 (funcionário → credenciais), UC-04 (consulta), UC-12 (carga de homologação)
* **Regras de Negócio:** BR-01, BR-04, BR-05, BR-06, BR-08, BR-09
* **Riscos relacionados:** `AtivoMapper.toDTO` complexidade 14 (achado AST — mitigação registrada no BRD); exclusão de cadastros com vínculos (open issue); regras de unicidade não evidenciadas (open issue)

---

## US-003 — Gerir Usuários, Papéis e Permissões

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — o modelo RBAC contextual é a regra central de proteção (BR-04); KPI do BRD: 0 incidentes de acesso indevido
* **Complexity:** 8 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: emissão de credenciais + papéis + permissões + ajuste contextual por filial; stub `Usuario.setUsername` a resolver
* **Épico:** EP-03 — Usuários e Controle de Acesso
* **Labels:** backend, segurança, tech-debt

### 1. Description (INVEST)
* **As a** administrador (perfil ADMIN)
* **I want to** emitir credenciais a funcionários, criar usuários, papéis e permissões e ajustar o acesso contextual por filial/departamento
* **So that** cada usuário opere apenas conforme o contexto concedido, protegendo o patrimônio contra acesso indevido

*Checklist INVEST:* Independent (depende apenas do funcionário cadastrado em US-002); Negotiable (matriz de permissões negociável); Valuable (proteção central do sistema); Estimable (fluxos claros no UC-03); Small (escopo restrito a acesso); Testable (cenários BDD abaixo).

### 2. Business Context
O modelo RBAC contextual (`hasPermission` + `doFilterInternal`) é a regra que protege todas as operações de escrita e delimita as consultas por filial. Sem ele, os KPIs de segurança do BRD (0 incidentes) e a restrição de consultas às filiais autorizadas (`findByFilialIdIn`) não se sustentam. A emissão de credenciais em admissões de funcionários é o gatilho operacional mais comum.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Emissão de credenciais a funcionário — caminho feliz
* **Given** um administrador autenticado (BR-01) e um funcionário cadastrado (US-002)
* **When** o administrador seleciona o funcionário e confirma a emissão de credenciais
* **Then** o sistema cria o usuário e o vínculo funcionário↔credenciais (`createFuncionarioAndUsuario`), emite o token (`createUserAndToken`), registra data/hora (BR-08) e o funcionário fica apto a autenticar (US-001) e a operar conforme o contexto concedido (BR-04)

#### Scenario 2: Ajuste de acesso contextual por filial/departamento — caso de borda
* **Given** um administrador autenticado e um usuário com contexto (filial/departamento) definido
* **When** o administrador atualiza as permissões contextuais do usuário
* **Then** o sistema aplica o novo contexto nas requisições seguintes via `doFilterInternal`/`hasPermission` (BR-04) e as consultas por filial passam a respeitar as filiais autorizadas (restrição `findByFilialIdIn` citada no BRD)

#### Scenario 3: Dados inválidos na criação — caminho de erro
* **Given** um administrador autenticado na gestão de usuários/papéis/permissões
* **When** o administrador submete dados inválidos ou incompletos
* **Then** o sistema retorna BadRequest (BR-05), o erro é tratado via `handleApiError` e nenhum registro é criado

#### Scenario 4: Escrita por usuário comum (USER) — caminho de erro
* **Given** um usuário autenticado com perfil USER
* **When** o usuário tenta criar usuário, papel ou permissão
* **Then** o sistema nega com Forbidden (BR-01) e nenhuma alteração ocorre

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; telas de gestão de usuários, papéis, permissões e matriz de contexto por filial.
* **Estados da interface:** loading, success (credenciais emitidas / permissões atualizadas), error (BadRequest/Forbidden via `handleApiError`), empty (nenhum usuário/papel cadastrado). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `createUsuario`, `createFuncionarioAndUsuario`, `createUserAndToken`, `createRole`, `createPermission`, `hasPermission`, `doFilterInternal`.
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; matriz de permissões por filial não documentada — criar e manter (mitigação de risco registrada no BRD).
* **Considerações de performance/segurança:** a verificação de permissão é executada a cada requisição (via `doFilterInternal`) — sua latência impacta todos os casos de uso (NFR-01/NFR-02); **Constraint C-04 do BRD:** `Usuario.setUsername` está com **corpo vazio (stub)** (achado AST em `src\main\java\br\com\aegispatrimonio\model\Usuario.java:86`) — implementar o comportamento ou remover o campo do fluxo de atualização, com teste de atualização de usuário cobrindo o caso (NFR-03).

### 6. Out of Scope
* Login/logout e gestão de sessão (US-001).
* Revogação de credenciais por desligamento de funcionário — processo não evidenciado no código (open issue do UC-03).
* Integração com provedores de identidade externos (Out-of-Scope do BRD).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (funcionário cadastrado — US-002)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Matriz de permissões por filial documentada e destino do stub `setUsername` decidido com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (emissão de credenciais, ajuste contextual, dados inválidos, escrita por USER)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-03, NFR-05
* **Use Cases relacionados:** UC-03 (origem), UC-01 (consumo da credencial), UC-02 (cadastro de funcionário), UC-04 (restrição de consultas por filial)
* **Regras de Negócio:** BR-01, BR-04, BR-05, BR-06, BR-08
* **Riscos relacionados:** `Usuario.setUsername` com corpo vazio/stub (Constraint C-04, achado AST); matriz de permissões por filial não mantida (mitigação registrada no BRD); processo de revogação não evidenciado (open issue)

---

## US-004 — Consultar Listagens e Registros com Filtros

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — esperadamente o caso de uso mais frequente do sistema; latência de listagens é guardrail do BRD
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: listagens + consulta por ID + filtros combinados (`ManutencaoSpecification`)
* **Épico:** EP-04 — Consultas e Listagens
* **Labels:** backend, frontend, performance

### 1. Description (INVEST)
* **As a** usuário autenticado (ADMIN ou USER)
* **I want to** consultar listagens e registros específicos com filtros combinados
* **So that** eu visualize o patrimônio da minha unidade e localize registros rapidamente, sem expor dados fora do meu contexto

*Checklist INVEST:* Independent; Negotiable (filtros suportados negociáveis); Valuable (caso de uso mais frequente — BR-02); Estimable; Small; Testable (evidência de teste `listarTodos_comUser_deveRetornarOk` já existente).

### 2. Business Context
Consultas são a operação diária de gestores de filial (visibilidade do patrimônio da unidade — BR-02), de técnicos de TI (histórico de saúde — BR-03) e da auditoria (consultas rastreáveis). A latência das listagens é **guardrail do BRD** (área sensível): o TODO de performance em `AtivoService` (linha 119) indica carregamento de até 1000 candidatos (id+nome) com ranking em memória — monitorar é obrigatório.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Consulta de listagem com filtros combinados — caminho feliz
* **Given** um usuário autenticado (BR-02)
* **When** o usuário acessa uma listagem (ex.: manutenções) e combina filtros de consulta
* **Then** o sistema aplica o filtro de controle de acesso (`doFilterInternal` — BR-04), monta a consulta combinada (`ManutencaoSpecification.build`) e retorna a listagem (Ok — evidência de teste: `listarTodos_comUser_deveRetornarOk`)

#### Scenario 2: Consulta de histórico de saúde por filial — caso de borda
* **Given** um usuário autenticado e um ativo pertencente a uma filial
* **When** o usuário solicita o histórico de saúde do ativo (`getHealthHistory`)
* **Then** o sistema verifica a permissão de leitura **na filial à qual o ativo pertence** (BR-03) e retorna o histórico somente se autorizado

#### Scenario 3: Identificador inexistente — caminho de erro
* **Given** um usuário autenticado
* **When** o usuário consulta um registro por ID inexistente (`buscarPorId`)
* **Then** o sistema retorna NotFound (BR-05), nunca dados incorretos, e o erro é tratado via `handleApiError`

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; telas de listagem com barra de filtros combinados.
* **Estados da interface:** loading (durante consulta), success (listagem/registro retornado), error (falha de API via `handleApiError`), empty (listagem sem resultados para os filtros). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `listarTodos`, `buscarPorId`, `ManutencaoSpecification` (montagem de filtros combinados), `getHealthHistory`, `doFilterInternal`.
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; os filtros exatos suportados por `ManutencaoSpecification` não estão detalhados na varredura; paginação das listagens não evidenciada — confirmar comportamento com grandes volumes.
* **Considerações de performance/segurança:** TODO de performance em `AtivoService` (linha 119): o caminho carrega até 1000 candidatos (id+nome) e faz ranking em memória — monitorar latência (NFR-01) e, no futuro, empurrar ranking/filtragem para a camada de consulta com paginação (NFR-06); restrição de consultas por filiais autorizadas (`findByFilialIdIn`) deve ser coberta por testes dedicados de acesso por filial (NFR-02); `ManutencaoSpecification.build` com complexidade ciclomática 14 (achado AST) — refatorar antes de evoluir (NFR-03); `console.debug` residual em `frontend/src/services/api.js:107` — remover antes de produção.

### 6. Out of Scope
* Criação/alteração/exclusão de registros (US-002).
* Exportação de relatórios — não evidenciada no código.
* Análise preditiva de falhas (Future Considerations do BRD).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (cadastros e registros consultáveis — US-002)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Filtros suportados por `ManutencaoSpecification` e comportamento de paginação confirmados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (listagem com filtros, histórico por filial, ID inexistente)
- [ ] Testes de integração/E2E quando aplicável (incl. testes dedicados de acesso por filial)
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — latência das listagens é guardrail do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-03, NFR-06
* **Use Cases relacionados:** UC-04 (origem), UC-05/UC-06 (acompanhamento de manutenções), UC-08 (histórico de saúde)
* **Regras de Negócio:** BR-02, BR-03, BR-04, BR-05
* **Riscos relacionados:** TODO de performance em `AtivoService` — ranking em memória de até 1000 candidatos (mitigação/Future Considerations do BRD); `ManutencaoSpecification.build` complexidade 14 (achado AST); `console.debug` residual em `api.js:107`

---

## US-005 — Iniciar Solicitação de Manutenção

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — fluxo de manutenção formal e rastreável é objetivo central do BRD
* **Complexity:** 3 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: fluxo único de registro no estado inicial do ciclo
* **Épico:** EP-05 — Fluxo de Manutenção
* **Labels:** backend, frontend

### 1. Description (INVEST)
* **As a** gestor de filial (perfil USER)
* **I want to** registrar uma solicitação de manutenção para um ativo da minha unidade
* **So that** o equipamento entre no fluxo formal de manutenção, rastreável até a aprovação

*Checklist INVEST:* Independent (depende do ativo cadastrado — US-002); Negotiable (campos da solicitação negociáveis); Valuable (início do ciclo que formaliza o gasto de manutenção); Estimable; Small; Testable (cenários BDD abaixo).

### 2. Business Context
O fluxo de manutenção com aprovação auditável é objetivo declarado do BRD: a solicitação registrada no estado inicial (`iniciar`) é o ponto de partida do ciclo `iniciar` → `aprovar` → `concluir`/`cancelar` e alimenta o custo acumulado por ativo (KPI da diretoria). Sem o registro formal, o custo de manutenção permanece invisível e a manutenção reativa prevalece (custo de inação do BRD).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Registro de solicitação de manutenção — caminho feliz
* **Given** um usuário autenticado com permissão no contexto do ativo (BR-04) e o ativo cadastrado (US-002)
* **When** o gestor de filial acessa o ativo e solicita a manutenção
* **Then** o sistema valida autenticação, permissão (BR-01/BR-04) e dados da solicitação (BR-05), registra a manutenção no estado inicial do fluxo (`iniciar`), registra data/hora (BR-08) e a manutenção fica aguardando aprovação (US-006)

#### Scenario 2: Acompanhamento da solicitação — caso de borda
* **Given** uma manutenção registrada no estado inicial
* **When** o gestor acompanha o status via consultas com filtros (`ManutencaoSpecification` — US-004)
* **Then** o sistema retorna o status atual da manutenção sem alterar seu estado

#### Scenario 3: Usuário sem permissão no contexto do ativo — caminho de erro
* **Given** um usuário autenticado sem permissão sobre o ativo (filial/departamento fora do contexto autorizado)
* **When** o usuário tenta registrar a manutenção
* **Then** o sistema nega com Forbidden (BR-01/BR-04) e nenhuma manutenção é registrada
* Erros adicionais cobertos pelos mesmos pontos de extensão: BadRequest para dados inválidos (BR-05) e NotFound para ativo inexistente (BR-05).

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; formulário de solicitação de manutenção a partir do ativo.
* **Estados da interface:** loading, success (manutenção registrada no estado inicial), error (BadRequest/Forbidden/NotFound via `handleApiError`), empty (ativo sem manutenções). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `iniciar` (serviço de manutenção), `ManutencaoSpecification` (acompanhamento), `onUpdate`/`preUpdate` (auditoria).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; campos obrigatórios da solicitação (ex.: descrição do problema, prioridade) não evidenciados na varredura; transições de estado permitidas não detalhadas — verificar a implementação real de `iniciar`.
* **Considerações de performance/segurança:** registro compatível com uso interativo (NFR-01); o registro deve ser rastreável no fluxo de aprovação (NFR-05); **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** confirmar se `iniciar` exige ADMIN ou aceita USER — as personas do BRD atribuem ao Gestor de Filial (USER) a condução de solicitações, mas BR-01 restringe escrita a administradores.

### 6. Out of Scope
* Aprovação da manutenção (US-006).
* Conclusão/cancelamento (US-007).
* Gatilhos automáticos de manutenção (ex.: a partir de alertas) — não evidenciados no código.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (ativo cadastrado — US-002)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Executor de `iniciar` (ADMIN vs USER) e campos obrigatórios da solicitação confirmados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (registro válido, acompanhamento, sem permissão, dados inválidos, ativo inexistente)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-05
* **Use Cases relacionados:** UC-05 (origem), UC-02 (ativo), UC-04 (acompanhamento), UC-06 (aprovação)
* **Regras de Negócio:** BR-01, BR-04, BR-05, BR-08
* **Riscos relacionados:** transições de estado e executor de `iniciar` não detalhados (open issue do UC-05); ambiguidade BR-01 vs personas para `iniciar` [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

---

## US-006 — Aprovar Manutenção

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — a aprovação formaliza o gasto e o fluxo de aprovação auditável é objetivo do BRD
* **Complexity:** 3 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: fluxo único de aprovação com validação de estado
* **Épico:** EP-05 — Fluxo de Manutenção
* **Labels:** backend, frontend, auditoria

### 1. Description (INVEST)
* **As a** administrador (perfil ADMIN) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** executor da aprovação não explícito no BRD/código; inferido do objetivo de restringir escrita a administradores e de a persona Gestor de Filial apenas acompanhar a aprovação
* **I want to** aprovar as manutenções aguardando decisão
* **So that** o gasto de manutenção seja formalizado e o fluxo permaneça auditável (quem aprovou e quando)

*Checklist INVEST:* Independent (depende da solicitação registrada — US-005); Negotiable (fluxo de rejeição negociável); Valuable (formaliza o gasto — diretoria Administrativa/Financeira); Estimable; Small; Testable.

### 2. Business Context
A aprovação é o ato que formaliza o gasto de manutenção perante a diretoria e habilita a execução (US-007). O objetivo do BRD de "fluxo de manutenção com aprovação auditável" exige rastreabilidade da decisão (data/hora garantida por `onUpdate`/`preUpdate` — BR-08). O tempo médio do ciclo `iniciar` → `concluir` é KPI do BRD — a aprovação não deve ser gargalo.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Aprovação de manutenção no estado inicial — caminho feliz
* **Given** um administrador autenticado (BR-01) e uma manutenção registrada no estado inicial (`iniciar` — US-005)
* **When** o administrador acessa a fila de manutenções aguardando aprovação (consulta com filtros via `ManutencaoSpecification` — US-004) e registra a aprovação
* **Then** o sistema valida permissão (BR-01/BR-04), registra a aprovação (`aprovar`), registra data/hora (BR-08) e a manutenção fica apta a ser concluída (US-007)

#### Scenario 2: Encaminhamento para cancelamento em vez de aprovação — caso de borda
* **Given** um administrador autenticado e uma manutenção aguardando aprovação
* **When** o administrador avalia a solicitação e decide não aprová-la
* **Then** o fluxo segue para o cancelamento da manutenção (`cancelar` — US-007) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** não há método de "rejeição" distinto na varredura; o cancelamento é o único encaminhamento alternativo evidenciado

#### Scenario 3: Manutenção em estado não aprovável — caminho de erro
* **Given** um administrador autenticado e uma manutenção já aprovada ou cancelada
* **When** o administrador tenta aprovar a manutenção
* **Then** o sistema recusa a transição com erro tratado ao usuário e o estado permanece inalterado **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento exato para transição inválida não evidenciado — verificar implementação de `aprovar`
* Erros adicionais: NotFound para manutenção inexistente (BR-05); Forbidden para tentativa de aprovação por USER (BR-01) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** depende da confirmação de quem pode aprovar.

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; fila de aprovações com filtros por estado.
* **Estados da interface:** loading, success (aprovação registrada), error (transição inválida / NotFound / Forbidden via `handleApiError`), empty (fila sem manutenções aguardando aprovação). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `aprovar` (serviço de manutenção), `ManutencaoSpecification` (fila com filtros), `onUpdate`/`preUpdate` (auditoria).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; estado prévio exigido (`iniciar` → `aprovar`) inferido da sequência de métodos verificados **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Considerações de performance/segurança:** rastreabilidade da aprovação (quem aprovou e quando) é requisito de auditoria (NFR-05); a aprovação não deve ser gargalo do ciclo `iniciar` → `concluir` (KPI do BRD — NFR-01).

### 6. Out of Scope
* Registro da solicitação (US-005).
* Execução/conclusão da manutenção (US-007).
* Fluxo de rejeição com justificativa — não evidenciado na varredura (open issue do UC-06).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (solicitações registradas — US-005)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Perfil aprovador formalmente definido e comportamento para transição inválida verificado com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (aprovação válida, encaminhamento para cancelamento, transição inválida, USER sem permissão)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — tempo do ciclo é KPI do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-05
* **Use Cases relacionados:** UC-06 (origem), UC-05 (solicitação), UC-07 (cancelamento alternativo), UC-04 (fila com filtros)
* **Regras de Negócio:** BR-01, BR-04, BR-05, BR-08
* **Riscos relacionados:** executor da aprovação não definido [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; comportamento para transição inválida não evidenciado (open issue)

---

## US-007 — Concluir ou Cancelar Manutenção

* **Priority:** Must (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — alimenta o custo acumulado por ativo (KPI do BRD: 100% dos ativos com custo visível até o 2º trimestre pós-go-live)
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: duas transições (concluir/cancelar) + validação de estado
* **Épico:** EP-05 — Fluxo de Manutenção
* **Labels:** backend, frontend, dados

### 1. Description (INVEST)
* **As a** gestor de filial (perfil USER) para conclusão / responsável do fluxo para cancelamento **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** executor do cancelamento não explícito
* **I want to** concluir manutenções aprovadas ou cancelar solicitações em aberto
* **So that** o fluxo de manutenção seja encerrado formalmente e o custo acumulado do ativo reflita a realidade para decisões de substituição, reparo ou desativação

*Checklist INVEST:* Independent (depende da manutenção aprovada/em aberto — US-006); Negotiable (campos da conclusão negociáveis); Valuable (base da decisão de substituição/reparo/desativação — diretoria); Estimable; Small; Testable.

### 2. Business Context
O encerramento formal do fluxo é o que torna o custo acumulado por ativo confiável (`custoTotalPorAtivo` — UC-10), habilitando o KPI do BRD de 100% dos ativos com custo visível. Sem a conclusão registrada, a diretoria decide sem evidência e o tempo médio do ciclo `iniciar` → `concluir` (KPI do BRD) não pode ser medido.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Conclusão de manutenção aprovada — caminho feliz
* **Given** um usuário autenticado e uma manutenção aprovada (US-006)
* **When** o responsável acessa a manutenção e registra a conclusão
* **Then** o sistema valida autenticação, permissão (BR-01/BR-04) e estado da manutenção, registra a conclusão (`concluir`), registra data/hora (BR-08) e o custo acumulado do ativo passa a refletir a conclusão no relatório `custoTotalPorAtivo` (US-010) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** relação entre conclusão e custo inferida da finalidade declarada do indicador no BRD

#### Scenario 2: Cancelamento de manutenção em aberto — caso de borda
* **Given** um usuário autenticado e uma manutenção em aberto (aguardando aprovação ou aprovada não executada)
* **When** o responsável solicita o cancelamento
* **Then** o sistema valida permissão e estado, registra o cancelamento (`cancelar`) e a data/hora (BR-08), encerrando o fluxo daquela manutenção sem execução

#### Scenario 3: Estado incompatível com a transição — caminho de erro
* **Given** um usuário autenticado e uma manutenção em estado incompatível (ex.: não aprovada para conclusão, ou já concluída para cancelamento)
* **When** o responsável tenta executar a transição
* **Then** o sistema recusa a transição com erro tratado ao usuário e o estado permanece inalterado **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento exato não evidenciado — verificar implementação de `concluir`/`cancelar`
* Erro adicional: NotFound para manutenção inexistente (BR-05).

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; tela da manutenção com ações de concluir/cancelar conforme o estado.
* **Estados da interface:** loading, success (conclusão/cancelamento registrados), error (transição inválida / NotFound via `handleApiError`), empty (não aplicável). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `concluir`, `cancelar` (serviço de manutenção), `custoTotalPorAtivo` (relatório de custo), `onUpdate`/`preUpdate` (auditoria).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; campos registrados na conclusão (ex.: custo real, observações, peças substituídas) não evidenciados na varredura — levantar com a engenharia.
* **Considerações de performance/segurança:** o tempo médio do ciclo `iniciar` → `concluir` é KPI do BRD (NFR-01); encerramento rastreável com data/hora garantida (NFR-05); executor de cada transição a confirmar (BR-01) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.

### 6. Out of Scope
* Aprovação (US-006).
* Composição detalhada do custo (mão de obra, peças, indiretos) — open issue do UC-10, a levantar.
* Gatilhos automáticos de conclusão — não evidenciados no código.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (manutenções aprovadas — US-006)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Campos da conclusão e comportamento para transição inválida confirmados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (conclusão válida, cancelamento, estado incompatível, manutenção inexistente)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — tempo do ciclo é KPI do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-05
* **Use Cases relacionados:** UC-07 (origem), UC-05 (solicitação), UC-06 (aprovação), UC-10 (custo acumulado)
* **Regras de Negócio:** BR-01, BR-04, BR-05, BR-08
* **Riscos relacionados:** relação conclusão→custo não explícita na varredura [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; campos da conclusão não evidenciados (open issue)

---

## US-008 — Registrar Health Check e Consultar Histórico de Saúde

* **Priority:** Should (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — KPI do BRD: ≥ 90% dos ativos monitorados (com `updateHealthCheck` atualizado) até o 2º trimestre pós-go-live
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: registro (`updateHealthCheck` + `updateScalars`) + consulta (`getHealthHistory`) com permissão por filial
* **Épico:** EP-06 — Monitoramento de Saúde dos Ativos
* **Labels:** backend, frontend, monitoramento

### 1. Description (INVEST)
* **As a** técnico de TI / monitoramento
* **I want to** registrar health checks com dados de hardware e disco e consultar o histórico de saúde dos ativos
* **So that** a manutenção seja priorizada com base em evidência e a análise preditiva de falhas seja viabilizada no futuro

*Checklist INVEST:* Independent (depende do ativo cadastrado — US-002); Negotiable (formato/periodicidade do health check negociáveis); Valuable (antecipação de falhas — objetivo do BRD); Estimable; Small; Testable.

### 2. Business Context
Os dados de hardware e disco são registrados "para viabilizar análise preditiva de falhas" (finalidade declarada no BRD) e sustentam a priorização de manutenção baseada em evidência. O KPI do BRD de ≥ 90% dos ativos monitorados até o 2º trimestre pós-go-live depende deste fluxo; a leitura do histórico é restrita por filial (BR-03), protegendo dados sensíveis por unidade.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Registro de health check com dados de hardware e disco — caminho feliz
* **Given** um usuário autenticado com permissão no contexto do ativo (BR-04) e o ativo cadastrado (US-002)
* **When** o técnico acessa o ativo e registra o health check com dados de hardware e disco
* **Then** o sistema valida os dados coletados (BR-05), registra o health check (`updateHealthCheck`), atualiza as métricas escalares do ativo (`updateScalars`), registra data/hora (BR-08) e o histórico de saúde fica atualizado e consultável (`getHealthHistory`)

#### Scenario 2: Consulta do histórico de saúde restrita por filial — caso de borda
* **Given** um usuário autenticado e um ativo com histórico de saúde registrado
* **When** o usuário solicita o histórico de saúde do ativo (`getHealthHistory`)
* **Then** o sistema verifica a permissão de leitura **na filial à qual o ativo pertence** (BR-03) e retorna o histórico somente se autorizado

#### Scenario 3: Dados de health check inválidos — caminho de erro
* **Given** um técnico autenticado no registro de health check
* **When** o técnico submete dados de hardware/disco inválidos ou incompletos
* **Then** o sistema retorna BadRequest (BR-05), o erro é tratado via `handleApiError` e nenhum health check é registrado
* Erro adicional: NotFound para ativo inexistente (BR-05).

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; formulário de health check e visão de histórico de saúde do ativo.
* **Estados da interface:** loading, success (health check registrado / histórico retornado), error (BadRequest/NotFound via `handleApiError`), empty (ativo ainda sem histórico de saúde). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `updateHealthCheck`, `updateScalars`, `getHealthHistory`, `doFilterInternal`.
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; periodicidade e formato exato do health check não evidenciados na varredura; critérios que conectam o histórico aos alertas de uso de recursos (US-009) não evidenciados.
* **Considerações de performance/segurança:** KPI do BRD — ≥ 90% dos ativos monitorados até o 2º trimestre pós-go-live; a coleta deve ser compatível com o ciclo de monitoramento definido (NFR-01); leitura do histórico restrita por filial (BR-03) — cobrir com testes dedicados de acesso por filial (NFR-02); o formato de registro deve preservar a finalidade de análise preditiva (NFR-06).

### 6. Out of Scope
* Análise preditiva de falhas (Future Considerations do BRD — adiada).
* Geração e gestão de alertas de uso de recursos (US-009).
* Atualização do inventário de hardware (US-011 — fluxo distinto, regido por BR-10).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (ativo cadastrado — US-002)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Periodicidade e formato exato do health check levantados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (registro válido, consulta por filial, dados inválidos, ativo inexistente)
- [ ] Testes de integração/E2E quando aplicável (incl. testes dedicados de acesso por filial)
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — % de ativos monitorados é KPI do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-06
* **Use Cases relacionados:** UC-08 (origem), UC-02 (ativo), UC-09 (alertas), UC-11 (inventário de hardware)
* **Regras de Negócio:** BR-03, BR-04, BR-05, BR-08, BR-10 (indireta, via US-011)
* **Riscos relacionados:** periodicidade não definida no código [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; conexão histórico↔alertas não evidenciada (open issue)

---

## US-009 — Gerir Alertas de Uso de Recursos

* **Priority:** Should (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — KPI do BRD: % de alertas reconhecidos dentro do SLA (valor numérico a definir)
* **Complexity:** 8 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: `checkResourceUsageAlerts` tem a maior complexidade ciclomática do codebase (17 — achado AST); fluxos de verificação, consulta e baixa
* **Épico:** EP-07 — Alertas de Uso de Recursos
* **Labels:** backend, frontend, monitoramento, tech-debt

### 1. Description (INVEST)
* **As a** técnico de TI / monitoramento (e gestor de filial para consulta e baixa) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** distribuição de responsabilidades entre perfis inferida das personas do BRD
* **I want** que os alertas de uso de recursos sejam gerados, consultáveis e formalmente reconhecidos (baixa via `markAsRead`)
* **So that** a manutenção reativa seja reduzida (custo de inação do BRD) e nenhum alerta fique sem tratamento

*Checklist INVEST:* Independent (consome métricas de US-008); Negotiable (limiares e SLA negociáveis); Valuable (redução do custo reativo — diretoria); Estimable; Small; Testable.

### 2. Business Context
Os alertas de uso de recursos materializam a priorização de manutenção baseada em evidência: a verificação (`checkResourceUsageAlerts`) avalia os ativos monitorados e gera alertas para os casos que excedem os critérios. O KPI do BRD de % de alertas reconhecidos dentro do SLA depende da baixa formal (`markAsRead`); sem reconhecimento, o custo reativo de manutenção prevalece (custo de inação do BRD).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Verificação gera alertas e responsável dá baixa — caminho feliz
* **Given** ativos com métricas de uso de recursos disponíveis (US-008) e um usuário autenticado
* **When** a verificação de uso de recursos é executada (`checkResourceUsageAlerts`) e o responsável consulta os alertas (`listarAlertas`, `getRecentAlerts`) e dá baixa formal (`markAsRead`)
* **Then** o sistema gera alertas para os casos que excedem os critérios, registra a baixa com data/hora (BR-08) e o alerta fica reconhecido (KPI do BRD: % de alertas reconhecidos dentro do SLA)

#### Scenario 2: Verificação sem alertas a gerar — caso de borda
* **Given** ativos com métricas de uso de recursos disponíveis
* **When** a verificação avalia os ativos e nenhum excede os critérios
* **Then** nenhum alerta é gerado e o fluxo encerra sem registro **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** critérios de geração de alerta (limiares de uso de recursos) não evidenciados na varredura

#### Scenario 3: Baixa de alerta sem permissão — caminho de erro
* **Given** um usuário autenticado sem permissão para a baixa formal
* **When** o usuário tenta dar baixa no alerta (`markAsRead`)
* **Then** o sistema nega com Forbidden (BR-01/BR-04) e o alerta permanece sem baixa **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** a varredura não explicita se `markAsRead` exige ADMIN; inferido do padrão de restrição de escrita a administradores (BR-01)
* Erro adicional: falha na verificação de uso de recursos — erro tratado ao usuário; refatorar `checkResourceUsageAlerts` (complexidade 17) antes de evoluir a área, mantendo testes de regressão.

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; listagens de alertas gerais e recentes com ação de baixa.
* **Estados da interface:** loading, success (alertas listados / baixa registrada), error (falha na verificação / Forbidden via `handleApiError`), empty (nenhum alerta gerado). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `checkResourceUsageAlerts` (`service/AlertNotificationService.java` — maior complexidade ciclomática do codebase: 17), `listarAlertas`, `getRecentAlerts`, `markAsRead`, `onUpdate`/`preUpdate` (auditoria).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; disparo da verificação (manual, agendado ou por evento) não evidenciado no código — confirmar; SLA numérico de reconhecimento a definir (KPI do BRD sem valor).
* **Considerações de performance/segurança:** KPI do BRD — % de alertas reconhecidos dentro do SLA até o 2º trimestre pós-go-live (NFR-01); baixa formal é operação de escrita coberta por `hasPermission` (NFR-02); refatorar `checkResourceUsageAlerts` em funções menores antes de evoluir, com testes de regressão (NFR-03).

### 6. Out of Scope
* Registro de health checks e métricas (US-008).
* Notificações externas (e-mail/push) — não evidenciadas no código; única dependência de produção declarada é `@popperjs/core ^2.11.8`.
* Definição automática de manutenção a partir de alertas — não evidenciada.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (métricas de uso de recursos — US-008)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Limiares de alerta, disparo da verificação e SLA de reconhecimento definidos com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (verificação com alertas, verificação sem alertas, baixa sem permissão)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — % de alertas reconhecidos é KPI do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-03
* **Use Cases relacionados:** UC-09 (origem), UC-08 (métricas de uso), UC-04 (consulta de listagens)
* **Regras de Negócio:** BR-01, BR-02, BR-04, BR-05, BR-08
* **Riscos relacionados:** `checkResourceUsageAlerts` complexidade 17 — maior do codebase (achado AST — mitigação registrada no BRD); disparo da verificação não evidenciado (open issue); SLA de reconhecimento sem valor numérico (open issue)

---

## US-010 — Consultar Custo Total por Ativo

* **Priority:** Should (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — KPI do BRD: 100% dos ativos com custo acumulado visível até o 2º trimestre pós-go-live
* **Complexity:** 3 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: consulta única via `custoTotalPorAtivo`
* **Épico:** EP-08 — Relatórios Gerenciais
* **Labels:** backend, frontend

### 1. Description (INVEST)
* **As a** gestor de filial / administrador (e, indiretamente, a Diretoria Administrativa/Financeira)
* **I want to** consultar o custo total de manutenção acumulado por ativo
* **So that** decisões de substituição, reparo ou desativação sejam apoiadas em dados (finalidade declarada do indicador no BRD)

*Checklist INVEST:* Independent (consome o histórico de manutenções — US-005/006/007); Negotiable; Valuable (decisão gerencial apoiada em dados — visão do BRD); Estimable; Small; Testable.

### 2. Business Context
O indicador `custoTotalPorAtivo` existe "para apoiar decisões sobre substituição, reparo ou desativação" (finalidade declarada no BRD). O KPI de 100% dos ativos com custo acumulado visível até o 2º trimestre pós-go-live depende deste fluxo e da integridade do histórico de manutenções (fonte única de verdade — visão do BRD).

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Consulta do custo total por ativo — caminho feliz
* **Given** um usuário autenticado (BR-02) e um ativo com histórico de manutenção (US-005/006/007)
* **When** o usuário acessa o relatório de custo total por ativo
* **Then** o sistema valida permissão contextual (BR-04), calcula/apresenta o custo total de manutenção acumulado do ativo (`custoTotalPorAtivo`) e retorna o resultado

#### Scenario 2: Ativo sem histórico de manutenção — caso de borda
* **Given** um usuário autenticado e um ativo sem manutenções registradas
* **When** o usuário consulta o custo total do ativo
* **Then** o sistema apresenta custo zero ou indicação "sem dados", nunca dados incorretos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento exato não evidenciado na varredura

#### Scenario 3: Ativo inexistente — caminho de erro
* **Given** um usuário autenticado
* **When** o usuário consulta o custo de um ativo inexistente
* **Then** o sistema retorna NotFound (BR-05) e o erro é tratado via `handleApiError`

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; visão de custo acumulado por ativo (e comparação entre ativos candidatos a desativação — cenário AS-1 do UC-10).
* **Estados da interface:** loading, success (custo apresentado), error (NotFound via `handleApiError`), empty (ativo sem histórico de manutenção — custo zero/"sem dados"). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `custoTotalPorAtivo` (relatório de custo por ativo).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; composição do custo (mão de obra, peças, custos indiretos) não evidenciada na varredura — levantar; consolidação por filial/departamento não evidenciada.
* **Considerações de performance/segurança:** KPI do BRD — 100% dos ativos com custo acumulado visível até o 2º trimestre pós-go-live; latência compatível com uso interativo (NFR-01); o custo apresentado deve refletir o histórico real de manutenções (NFR-05).

### 6. Out of Scope
* Registro de custos na conclusão de manutenções (US-007).
* Consolidação gerencial por filial/departamento — não evidenciada (open issue do UC-10).
* Exportação de relatórios — não evidenciada no código.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (histórico de manutenções — US-005/006/007)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Composição do custo e comportamento para ativo sem manutenções confirmados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (consulta válida, ativo sem manutenções, ativo inexistente)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável — % de ativos com custo visível é KPI do BRD)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-05
* **Use Cases relacionados:** UC-10 (origem), UC-05/UC-06/UC-07 (fonte dos custos), UC-02 (ativo)
* **Regras de Negócio:** BR-02, BR-04, BR-05
* **Riscos relacionados:** origem dos custos não explícita na varredura [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; composição do custo não evidenciada (open issue)

---

## US-011 — Atualizar Inventário de Hardware do Ativo

* **Priority:** Should (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — inventário fiel compõe os dados de hardware que sustentam o monitoramento (US-008) e a futura análise preditiva
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: limpeza dos componentes antigos + registro dos novos (BR-10)
* **Épico:** EP-06 — Monitoramento de Saúde dos Ativos
* **Labels:** backend, frontend, monitoramento

> **Atenção:** o conteúdo do UC-11 no artefato-fonte está **truncado** (interrompido nas precondições). Os cenários abaixo derivam do trecho disponível, da regra BR-10 e das regras gerais BR-05; pontos não cobertos estão marcados como verificação pendente.

### 1. Description (INVEST)
* **As a** técnico de TI / monitoramento
* **I want to** atualizar o inventário de hardware do ativo (adaptadores de rede, discos, memórias) quando há substituição de componentes
* **So that** o inventário permaneça fiel ao equipamento e os dados de saúde preservem a finalidade de análise preditiva de falhas

*Checklist INVEST:* Independent (depende do ativo com detalhe de hardware — US-002/US-008); Negotiable; Valuable (base da futura análise preditiva — Future Considerations do BRD); Estimable (parcialmente — contrato truncado no artefato-fonte); Small; Testable.

### 2. Business Context
O inventário de hardware compõe os dados de hardware do ativo registrados pelo monitoramento de saúde (US-008). Sem atualização fiel após substituições, o histórico de saúde e a futura análise preditiva de falhas (Future Considerations do BRD) perdem a base de evidência. A regra BR-10 garante que componentes antigos sejam limpos antes do registro dos novos, evitando inventário duplicado/inconsistente.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Atualização completa do inventário de hardware — caminho feliz
* **Given** um usuário autenticado e um ativo com detalhe de hardware (precondição do UC-11)
* **When** o técnico registra o novo inventário de hardware (adaptadores de rede, discos, memórias)
* **Then** o sistema limpa os componentes antigos antes de registrar os novos (BR-10) e o inventário do ativo reflete a configuração atual

#### Scenario 2: Substituição parcial de componentes — caso de borda
* **Given** um usuário autenticado e um ativo com inventário de hardware existente
* **When** o técnico substitui apenas um componente (ex.: disco)
* **Then** o sistema remove apenas o componente substituído e mantém os demais, preservando a integridade do inventário **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** cenário inferido da regra BR-10 e da natureza dos componentes; o trecho do UC-11 está truncado no artefato-fonte

#### Scenario 3: Dados inválidos ou ativo sem detalhe de hardware — caminho de erro
* **Given** um técnico autenticado e um ativo sem detalhe de hardware ou com dados de inventário inválidos
* **When** o técnico tenta registrar o inventário
* **Then** o sistema recusa a entrada (BR-05) com erro tratado ao usuário e o inventário permanece inalterado **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento exato não evidenciado (trecho do UC-11 truncado no artefato-fonte)

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; tela de inventário de hardware do ativo com lista de componentes (adaptadores de rede, discos, memórias).
* **Estados da interface:** loading, success (inventário atualizado), error (dados inválidos via `handleApiError`), empty (ativo sem inventário de hardware registrado). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** métodos de atualização do inventário de hardware do ativo — **nomes exatos não evidenciados no trecho disponível do UC-11** — levantar com a engenharia; relação com `updateHealthCheck` (US-008), cujos dados de hardware compõem o inventário.
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; contrato de limpeza/registro de componentes (BR-10) a confirmar na implementação real.
* **Considerações de performance/segurança:** escrita coberta por `hasPermission` (NFR-02); o formato do inventário deve preservar a finalidade de análise preditiva (NFR-06).

### 6. Out of Scope
* Registro do health check em si (US-008).
* Análise preditiva de falhas (Future Considerations do BRD — adiada).
* Gestão de componentes como cadastro independente — não evidenciada no código.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (ativo com detalhe de hardware — US-002/US-008)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Métodos/contrato de atualização do inventário levantados com a engenharia (trecho do UC-11 truncado no artefato-fonte)

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (atualização completa, substituição parcial, dados inválidos)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-02, NFR-06
* **Use Cases relacionados:** UC-11 (origem — parcial/truncado), UC-08 (monitoramento de saúde)
* **Regras de Negócio:** BR-05, BR-10
* **Riscos relacionados:** artefato-fonte truncado — contrato real de atualização do inventário pendente de levantamento; inventário duplicado/inconsistente se BR-10 não for respeitado

---

## US-012 — Executar Carga de Dados Realista para Homologação

* **Priority:** Could (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — operação técnica de suporte à homologação (nível Subfunction no catálogo de UCs)
* **Complexity:** 3 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: execução do seeder; `RealisticDataSeeder.run` com complexidade ciclomática 15 (achado AST)
* **Épico:** EP-09 — Operação Técnica de Homologação (Carga de Dados)
* **Labels:** backend, dados, tech-debt

> **Atenção:** a seção completa do UC-12 **não foi incluída** no material disponível (apenas o índice do catálogo). Os cenários abaixo derivam do índice, do achado AST (`config/seeder/RealisticDataSeeder.java`) e do contexto de validação citado no catálogo; todos os pontos não cobertos estão marcados como verificação pendente.

### 1. Description (INVEST)
* **As a** engenheiro/TI (operação técnica)
* **I want to** executar uma carga de dados realista para homologação
* **So that** os demais casos de uso possam ser validados com dados próximos da realidade operacional

*Checklist INVEST:* Independent (não depende de outras stories — é suporte à validação); Negotiable (volume/composição dos dados negociáveis); Valuable (habilita validação realista dos fluxos); Estimable (parcialmente — seção do UC-12 ausente); Small; Testable.

### 2. Business Context
A validação realista dos fluxos (login, cadastros, manutenção, monitoramento) depende de dados próximos da produção. O catálogo de UCs cita o `mockLogin` para validação (UC-01, AS-1) e a carga de dados realista como operação técnica de homologação (UC-12) — juntas, compõem o kit de validação do ambiente.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Execução da carga de dados realista — caminho feliz
* **Given** um ambiente de homologação com o mecanismo de persistência disponível
* **When** a carga de dados realista é executada (`RealisticDataSeeder.run`)
* **Then** o ambiente é populado com dados realistas (ativos, cadastros de apoio e vínculos) aptos a exercitar os demais casos de uso **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** cenário derivado do índice do catálogo e do achado AST; composição exata dos dados não evidenciada

#### Scenario 2: Execução em ambiente já populado — caso de borda
* **Given** um ambiente de homologação já populado por execução anterior
* **When** a carga é executada novamente
* **Then** o comportamento (idempotência, duplicação ou limpeza prévia) deve ser o definido e verificado, sem corromper o ambiente **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento não evidenciado — levantar com a engenharia

#### Scenario 3: Falha durante a carga — caminho de erro
* **Given** um ambiente de homologação
* **When** ocorre erro durante a execução da carga (`RealisticDataSeeder.run`)
* **Then** o erro é tratado e o ambiente não fica em estado inconsistente; refatorar `run` (complexidade ciclomática 15 — achado AST em `config/seeder/RealisticDataSeeder.java:34`) em métodos menores antes de evoluir a carga (NFR-03) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 4. UI/UX Notes
* **Wireframe/Mockup:** não aplicável — operação técnica (sem interface de usuário evidenciada; execução por engenharia/TI).
* **Estados da interface:** não aplicável; estados da operação: execução em curso, sucesso (ambiente populado), erro (falha tratada). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** `RealisticDataSeeder.run` (`src\main\java\br\com\aegispatrimonio\config\seeder\RealisticDataSeeder.java:34`, complexidade ciclomática 15 — achado AST).
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; volume/composição dos dados, política de idempotência e ambiente de execução não evidenciados.
* **Considerações de performance/segurança:** a carga destina-se exclusivamente à homologação — nunca a produção; refatorar `run` (complexidade 15) antes de evoluir (NFR-03).

### 6. Out of Scope
* Uso da carga em produção.
* Cadastros manuais via interface (US-002).
* Geração de dados sintéticos parametrizáveis — não evidenciada no código.

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (mecanismo de persistência disponível em homologação)
- [ ] Design/UX aprovado (não aplicável — operação técnica)
- [ ] Estimativa de esforço realizada
- [ ] Política de idempotência e composição dos dados definidas com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (execução válida, reexecução, falha)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade (não aplicável — operação técnica)
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-03
* **Use Cases relacionados:** UC-12 (origem — índice), UC-01 (mockLogin — validação), UC-02 (cadastros populados)
* **Regras de Negócio:** nenhuma regra BR-XX diretamente vinculada no material disponível — a carga é operação técnica de suporte
* **Riscos relacionados:** `RealisticDataSeeder.run` complexidade 15 (achado AST — refatorar antes de evoluir); ambiente inconsistente em falha sem política de idempotência definida

---

## US-013 — Consultar Eventos de Auditoria e Histórico de Modificações

* **Priority:** Should (MoSCoW) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — rastreabilidade para auditoria/compliance; a auditoria automática (BR-08) é a base já existente
* **Complexity:** 5 story points [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — premissa: consulta de eventos/histórico por registro sobre o mecanismo `onUpdate`/`preUpdate`
* **Épico:** EP-10 — Auditoria e Compliance
* **Labels:** backend, frontend, auditoria

> **Atenção:** a seção completa do UC-13 **não foi incluída** no material disponível (apenas o índice do catálogo). Os cenários abaixo derivam do índice, do mecanismo de auditoria automática verificado (`onUpdate`/`preUpdate` — BR-08) e das regras gerais; todos os pontos não cobertos estão marcados como verificação pendente.

### 1. Description (INVEST)
* **As a** auditor / compliance
* **I want to** consultar os eventos de auditoria e o histórico de modificações dos registros
* **So that** eu verifique quem alterou o quê e quando, com trilha completa e rastreável

*Checklist INVEST:* Independent (consome a trilha gerada automaticamente pelas demais stories); Negotiable (escopo do que é auditado negociável); Valuable (rastreabilidade exigida por auditoria/compliance); Estimable (parcialmente — seção do UC-13 ausente); Small; Testable.

### 2. Business Context
A auditoria/compliance depende da trilha de quem alterou o quê e quando — trilha que já é gerada automaticamente por `onUpdate`/`preUpdate` (BR-08) em todas as modificações (cadastros, manutenções, health checks, alertas). A consulta estruturada desses eventos é o que transforma a trilha em evidência utilizável para auditoria.

### 3. Acceptance Criteria (BDD/Gherkin)

#### Scenario 1: Consulta do histórico de modificações de um registro — caminho feliz
* **Given** um auditor autenticado (BR-02) e um registro com modificações registradas (BR-08 — `onUpdate`/`preUpdate`)
* **When** o auditor consulta os eventos de auditoria/histórico de modificações do registro
* **Then** o sistema retorna a trilha de modificações com data/hora de cada alteração

#### Scenario 2: Registro sem modificações registradas — caso de borda
* **Given** um auditor autenticado e um registro recém-criado sem modificações posteriores
* **When** o auditor consulta o histórico do registro
* **Then** o sistema retorna lista vazia ou apenas o evento de criação, nunca dados incorretos **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento não evidenciado (seção do UC-13 não incluída no material disponível)

#### Scenario 3: Registro inexistente — caminho de erro
* **Given** um auditor autenticado
* **When** o auditor consulta o histórico de um registro inexistente
* **Then** o sistema retorna NotFound (BR-05) e o erro é tratado via `handleApiError` **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** comportamento inferido da regra geral BR-05

### 4. UI/UX Notes
* **Wireframe/Mockup:** nenhum artefato de design encontrado no workspace — a criar; visão de histórico de modificações por registro (timeline de eventos).
* **Estados da interface:** loading, success (trilha retornada), error (NotFound via `handleApiError`), empty (registro sem modificações registradas). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### 5. Technical Implementation Notes
* **Componentes/serviços afetados:** mecanismo de auditoria automática (`onUpdate`/`preUpdate` — BR-08); métodos de consulta de eventos de auditoria — **nomes exatos não evidenciados no material disponível** — levantar com a engenharia.
* **Dependências técnicas:** nenhuma rota/tabela detectada na varredura — vincular quando `api-specification.md` for produzida; escopo do que é auditado (usuário responsável, campos alterados) não evidenciado na varredura.
* **Considerações de performance/segurança:** consulta restrita a perfis autorizados (BR-04 — NFR-02); a trilha deve permanecer completa e imutável (NFR-05); latência compatível com uso interativo (NFR-01).

### 6. Out of Scope
* Alteração de registros (US-002) e transições do fluxo de manutenção (US-005/006/007) — a auditoria apenas consulta a trilha gerada.
* Exportação de relatórios de auditoria — não evidenciada no código.
* Retenção/arquivamento de eventos — política não evidenciada (open issue).

### 7. Definition of Ready
- [ ] Critérios de aceite definidos e validados com PO
- [ ] Dependências identificadas e desbloqueadas (trilha gerada por `onUpdate`/`preUpdate` — BR-08)
- [ ] Design/UX aprovado (se aplicável)
- [ ] Estimativa de esforço realizada
- [ ] Escopo do registro de auditoria (usuário/campos) e métodos de consulta levantados com a engenharia

### 8. Definition of Done
- [ ] Código implementado e revisado (PR aprovado)
- [ ] Testes unitários cobrindo cenários BDD (consulta válida, registro sem modificações, registro inexistente)
- [ ] Testes de integração/E2E quando aplicável
- [ ] Tipagem estrita aplicável ao stack (Java compilado sem erros; frontend em JavaScript)
- [ ] Lint/format ok
- [ ] Sem regressões de acessibilidade
- [ ] Documentação/API docs atualizadas
- [ ] Feature testada em staging
- [ ] Métricas/observabilidade instrumentadas (se aplicável)

### 9. Traceability
* **NFRs relacionadas:** NFR-01, NFR-02, NFR-05
* **Use Cases relacionados:** UC-13 (origem — índice), UC-02 (cadastros), UC-05/UC-06/UC-07 (trilhas do fluxo de manutenção), UC-08 (health checks)
* **Regras de Negócio:** BR-02, BR-04, BR-05, BR-08
* **Riscos relacionados:** mecanismo de consulta de eventos de auditoria não evidenciado no material disponível; escopo do que é auditado (usuário/campos) não documentado — levantar