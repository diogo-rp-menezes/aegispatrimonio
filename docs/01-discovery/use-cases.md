# Use Cases — Sistema de Gestão de Patrimônio (A4)

> Documento de especificação de casos de uso derivado do **Business Requirements Document (BRD)** (casos de uso UC-01..UC-04, requisitos RF-01..RF-29, regras de negócio RN-01..RN-08, critérios de aceitação CA-01..CA-10 e RNF-01..RNF-10) e da **varredura determinística do codebase (A5** — 350 arquivos, 27.537 LOC, 1268 funções, 344 classes**)**.

## Notas de Escopo e Verificação

**Rotas/IPC/Handlers:** A varredura de rotas/IPC/handlers **não identificou nenhuma rota explícita no código**. Por isso, os fluxos abaixo são mapeados aos **contratos comportamentais definidos no BRD** (nomes dos testes de integração, ex.: `criar_comAdmin_deveRetornarCreated`) e aos **códigos de regra de negócio (RN-XX)**, em vez de caminhos de endpoint. Caminhos concretos de API devem ser confirmados no `api-specification.md` quando este for gerado.

**Stack real (varredura de dependências):** as dependências reais capturadas limitam-se a `@popperjs/core` (produção); não foram encontrados motor de banco, ORM/query builder ou empacotamento Tauri nas dependências varridas. O codebase é uma aplicação backend única (335 arquivos Java em `src/`) com frontend JavaScript (15 arquivos em `frontend/`). Requisitos do BRD que pressupõem capacidades além disso — ex.: RNF-02 (bcrypt/argon2), RNF-03 (HTTPS/TLS), RNF-06 (horizontal scaling), RNF-09 (OpenAPI/Swagger) — são tratados neste documento como **metas do BRD pendentes de verificação no codebase**; nenhum mecanismo de infraestrutura não detectado é afirmado como existente nos fluxos.

**Convenção de rótulos:** trechos marcados com **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** são hipóteses plausíveis derivadas do contexto (BRD + varredura), não fatos verificados; as premissas adotadas estão declaradas junto a cada trecho.

**Códigos de regra:** os códigos **RN-XX** referenciados abaixo correspondem às Business Rules da seção 5 do BRD.

## Achados da varredura (A5) que afetam diretamente estes fluxos

- `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` — TODO de performance: busca de ativos carrega até 1000 candidatos (id+nome) e faz ranking (afeta UC-02, passo 2).
- `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` — método `toDTO` com complexidade ciclomática 14 (consulta de detalhes do ativo — RF-14; afeta UC-02).
- `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` — método `build` com complexidade 14 (filtros dinâmicos de manutenção; afeta UC-03, passo 1).
- `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96` — `checkResourceUsageAlerts` com complexidade 17 (único serviço de alerta detectado; ligação com a notificação de aprovação não estabelecida — afeta UC-03).
- `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86` — `setUsername` com corpo vazio/stub (afeta cadastro e vínculo de funcionário — RN-06; afeta UC-02).
- `frontend/src/services/api.js:44` e `:107` — chamadas residuais `console.error`/`console.debug` no cliente API usado por todos os fluxos (UC-01..UC-04).
- `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34` — seeder de dados realistas, método `run` com complexidade 15 (dados de teste dos fluxos; afeta UC-01).

## Matriz de Rastreabilidade (resumo)

| UC | Nome | Ator primário | RFs | RNs | CAs |
|----|------|---------------|-----|-----|-----|
| UC-01 | Administrador Cadastra Nova Filial | Admin | RF-06 | RN-01, RN-02, RN-03, RN-07 | CA-01, CA-02, CA-06, CA-08 |
| UC-02 | Funcionário Solicita Manutenção de Ativo | User (Funcionário) | RF-15, RF-18, RF-22 | RN-03, RN-04, RN-05, RN-06, RN-07 | CA-02, CA-03, CA-04, CA-06, CA-07, CA-08, CA-10 |
| UC-03 | Aprovador Autoriza Manutenção | Aprovador | RF-17, RF-19, RF-20 | RN-04, RN-05, RN-08 | CA-03, CA-04, CA-05, CA-09 |
| UC-04 | Técnico Conclui Ordem de Serviço | Equipe de Manutenção | RF-17, RF-21, RF-22 | RN-03, RN-04, RN-05, RN-07, RN-08 | CA-04, CA-05, CA-09 |

---

# Use Case Specification: UC-01 — Administrador Cadastra Nova Filial

## 1. Characterization
* **Primary Actor:** Administrador do Sistema (role **Admin**); pode ser o Gestor de Patrimônio (Product Owner) atuando com perfil Admin.
* **Secondary Actors:** Cliente API do frontend (`frontend/src/services/api.js`), responsável por injetar o token de autenticação nas requisições via interceptador de auth (RF-24, CA-07). Nenhum sistema externo é envolvido neste fluxo. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — o interceptador é atestado pelo BRD (RF-24); o papel do `api.js` como canal único das requisições foi inferido da estrutura do frontend (15 arquivos `.js`, com `services/api.js` centralizando a função `request`).
* **Stakeholders & Interests:**
  - **Gestor de Patrimônio (Product Owner):** precisa de filiais corretas para rastrear ativos por unidade organizacional.
  - **Administrador do Sistema:** mantém cadastros mestres consistentes (RN-01).
  - **TI/Infraestrutura:** operações de escrita auditadas (RNF-07).
* **Trigger:** Admin acessa "Cadastro de Filiais" e submete o formulário de criação.
* **Preconditions:**
  - Usuário autenticado com role Admin (RN-01), com sessão válida (autenticação JWT conforme RF-23/RNF-01 do BRD).
  - Não há dependência de outros cadastros para criar a filial, conforme fluxo do BRD.
* **Postconditions (Success Guarantee):** Filial persistida e disponível para associação a ativos e departamentos (RN-07 — ativos passam a poder referenciar esta filial).
* **Scope:** Módulo de Gestão Organizacional (entidades mestres) do Sistema de Gestão de Patrimônio.
* **Level:** User goal.

## 2. Main Scenario (Happy Path)
1. Admin acessa "Cadastro de Filiais" no frontend.
2. O frontend despacha a requisição de criação através do cliente API (`services/api.js`), que injeta automaticamente o token de autenticação (RF-24, CA-07).
3. O sistema valida os dados obrigatórios do formulário: nome, código, endereço e responsável (RN-03).
4. O sistema persiste a filial e retorna **201 Created** (RN-01; contrato `criar_comAdmin_deveRetornarCreated`; CA-01).
5. Caso de uso encerra com sucesso; a filial fica disponível para associação a ativos e departamentos.

## 3. Alternative Scenarios

### AS-1: Admin cancela o preenchimento do formulário **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** A partir do passo 1 (antes da submissão).
* **Passos:**
  1. Admin navega para fora da tela ou descarta o formulário.
  2. Nenhuma requisição de escrita é disparada; nenhum dado é persistido.
* **Retorno ao fluxo principal:** Não — encerra sem efeito no sistema. Premissa: o formulário possui descarte/cancelamento local, comportamento não especificado no BRD.

### AS-2: Admin cadastra departamento vinculado à filial recém-criada **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Após o passo 5.
* **Passos:**
  1. Admin acessa "Cadastro de Departamentos" (RF-01).
  2. Associa o departamento à filial criada (Filial/Departamento são unidades organizacionais hierárquicas, conforme linguagem ubíqua do BRD).
* **Retorno ao fluxo principal:** Não — encadeia para o fluxo de departamentos (fora do escopo deste UC).

## 4. Exception Scenarios

### EX-1: Falha de validação de dados (RN-03)
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Campos obrigatórios ausentes (nome, código, endereço, responsável) ou formatos inválidos no payload.
* **Tratamento:** Sistema rejeita com **400 Bad Request** e mensagem clara (contrato `criar_comDadosInvalidos_deveRetornarBadRequest`; CA-02). A escrita não é executada. A tentativa deve ser registrada em audit trail conforme RNF-07 **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: nenhuma implementação de audit trail foi detectada na varredura do codebase]**.

### EX-2: Usuário sem role Admin tenta criar filial (RN-02)
* **Ponto de extensão:** Passo 3 (antes da persistência).
* **Condição de erro:** Token válido, porém role = **User**.
* **Tratamento:** Sistema retorna **403 Forbidden** (contrato `criar_comUser_deveRetornarForbidden`; CA-01). Nenhum dado é persistido.

### EX-3: Sessão expirada ou token ausente
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Token expirado (expiração em 1h conforme RNF-01 do BRD) ou requisição sem token.
* **Tratamento:** Sistema retorna **401 Unauthorized** (CA-06). O frontend deve limpar a sessão (`clearSession`, RF-25) e o token invalidado server-side conforme CA-08. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o comportamento de redirecionamento para login no frontend não está especificado no BRD nem detectado na varredura.]**

### EX-4: Falha de conexão entre frontend e backend **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Indisponibilidade de rede/backend no momento do envio.
* **Tratamento:** O cliente `services/api.js` possui tratamento de erros na função `request` (complexidade ciclomática 13; a varredura registrou `console.error` em `api.js:44`). Presume-se exibição de mensagem de erro ao usuário e não-persistência. Premissa: não há retry automático configurado nas fontes disponíveis. Observação: a chamada residual `console.debug` (`api.js:107`) deve ser removida antes de produção.

## 5. Business Rules Envolvidas
* **RN-01:** Apenas role **Admin** pode criar entidades mestres — filial é entidade mestre; a criação exige Admin e retorna 201.
* **RN-02:** Role **User** recebe **403 Forbidden** em operações de escrita em entidades mestres.
* **RN-03:** Dados de entrada inválidos retornam **400 Bad Request**.
* **RN-07:** (efeito colateral) Cada ativo pertence a **uma** Filial — a filial criada passa a ser referenciável por ativos.

## 6. Non-Functional Requirements Relevantes
* **RNF-01 (Segurança):** token JWT com expiração de 1h — a sessão deve estar válida durante o fluxo (meta do BRD).
* **RNF-04 (Desempenho):** tempo de resposta da API < 200ms (p95) para a criação (meta do BRD; não verificável nas fontes disponíveis — a varredura não capturou instrumentação de métricas).
* **RNF-07 (Auditoria):** operação de escrita (criar) deve gerar registro em audit trail imutável (meta do BRD; implementação não detectada na varredura — ver Open Issues).
* **RNF-08 (Usabilidade):** interface responsiva em desktop/tablet para o formulário de cadastro.

## 7. Frequency of Use
**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Baixa frequência — cadastros mestres são criados em eventos de onboarding, reestruturação organizacional ou abertura de filiais; tipicamente poucas operações por mês por administrador. Premissa: volume de filiais estável após go-live; o risco R-02 do BRD (migração de dados legados) indica carga inicial concentrada, possivelmente via seeder (`RealisticDataSeeder`, detectado na varredura) em ambiente de teste.

## 8. Assumptions
* Os campos do formulário (nome, código, endereço, responsável) correspondem ao modelo de dados de Filial — extraídos do fluxo principal do BRD; o modelo completo não foi verificado na varredura.
* A unicidade de código/nome da filial **não** é definida como regra no BRD (RN-01..RN-08 não cobrem duplicidade) — presume-se que duplicatas sejam aceitas até que regra seja criada (ver Open Issues).
* O ambiente de desenvolvimento possui o seeder `RealisticDataSeeder` (varredura: `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java`) que popula cadastros para os testes de integração referenciados pelo BRD.

## 9. Open Issues
* Nenhuma rota/endpoint explícito foi detectado na varredura — o caminho concreto da API de criação de filiais precisa ser confirmado no `api-specification.md`.
* Restrição de unicidade para código/nome de filial não definida no BRD.
* Implementação do audit trail (RNF-07) não detectada na varredura do codebase.
* O método `run` do `RealisticDataSeeder` apresenta complexidade ciclomática alta (15) — impacto potencial na manutenção dos dados de teste.

## 10. Related Artifacts
* **User Stories:** Não definidas no BRD — requisitos cobertos por RF-06 (Criar Filial) e RF-01..RF-05 (contexto organizacional).
* **User Flow:** Diagrama ainda não gerado (`user-flows.md` pendente) — o fluxo está descrito na seção 2 deste documento.
* **API envolvida:** `api-specification.md` ainda não gerado; nenhuma rota detectada na varredura. Contratos comportamentais de referência no BRD: `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `criar_comDadosInvalidos_deveRetornarBadRequest`.

---

# Use Case Specification: UC-02 — Funcionário Solicita Manutenção de Ativo

## 1. Characterization
* **Primary Actor:** Funcionário/Colaborador (role **User**).
* **Secondary Actors:**
  - **Aprovador:** recebe a solicitação na fila de pendentes (pós-condição do BRD).
  - **Cliente API do frontend** (`services/api.js`) com interceptador de auth (RF-24, CA-07).
  - **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Serviço de armazenamento de anexos (fotos) — o BRD menciona "anexa fotos (opcional)" no fluxo, mas nenhum endpoint/serviço de upload foi detectado na varredura.
* **Stakeholders & Interests:**
  - **Funcionário:** registrar o problema do ativo alocado e acompanhar o reparo.
  - **Equipe de Manutenção:** receber demanda qualificada.
  - **Aprovação/Compliance:** visibilidade das demandas pendentes.
  - **Gestor de Patrimônio:** histórico de intervenções por ativo (RF-22).
* **Trigger:** Funcionário identifica um problema em ativo e acessa "Nova Solicitação".
* **Preconditions:**
  - Usuário autenticado, com token injetado automaticamente pelo interceptador (RF-24, CA-07).
  - Ativo existe e está ativo (RN-07 — ativo vinculado a filial/departamento/localização).
  - Funcionário pode estar vinculado a usuário do sistema (RN-06; RF-08: `createFuncionario` / `createFuncionarioAndUsuario`).
* **Postconditions (Success Guarantee):** Solicitação criada com status **Pendente** (RF-18), visível para aprovadores; ativo referenciado no histórico de manutenção (RF-22).
* **Scope:** Módulo de Gestão de Manutenção (criação de solicitação) + consulta/filtragem de ativos (RF-15).
* **Level:** User goal.

## 2. Main Scenario (Happy Path)
1. Funcionário acessa "Nova Solicitação" no frontend.
2. Funcionário busca/seleciona o ativo por filial, departamento ou localização (RF-15 — listagem/filtragem de ativos).
3. Funcionário descreve o problema, define a prioridade e anexa fotos (opcional).
4. Funcionário submete a solicitação; o cliente API injeta o token (RF-24) e o sistema valida os dados (RN-03).
5. Sistema cria a solicitação com status **Pendente** (RF-18; RN-05 — estado inicial do fluxo) e a torna visível para aprovadores.
6. Caso de uso encerra com sucesso.

## 3. Alternative Scenarios

### AS-1: Funcionário não encontra o ativo na busca
* **Ponto de extensão:** Passo 2.
* **Passos:**
  1. Os filtros (filial/departamento/localização) retornam lista vazia.
  2. Funcionário ajusta os filtros ou desiste da solicitação.
* **Retorno ao fluxo principal:** Sim, no passo 2 (após ajuste dos filtros) — ou encerra sem efeito. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o comportamento de "lista vazia" não está especificado no BRD; a busca com ranking de candidatos está implementada em `AtivoService`, conforme TODO de performance detectado na varredura (`AtivoService.java:119`).]**

### AS-2: Funcionário cancela antes de submeter **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Entre os passos 1 e 4.
* **Passos:**
  1. Funcionário descarta o formulário.
  2. Nenhuma requisição de escrita é disparada.
* **Retorno ao fluxo principal:** Não — encerra sem efeito.

## 4. Exception Scenarios

### EX-1: Ativo inexistente (RN-04)
* **Ponto de extensão:** Passo 2 ou 5.
* **Condição de erro:** ID do ativo inexistente ou ativo baixado.
* **Tratamento:** Sistema retorna **404 Not Found** com mensagem padronizada (contrato `buscarPorId_comIdInexistente_deveRetornarNotFound`; CA-03). A solicitação não é criada.

### EX-2: Falha de validação de dados (RN-03)
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** Descrição/prioridade ausentes ou inválidas; payload malformado.
* **Tratamento:** **400 Bad Request** com mensagem clara (CA-02). A solicitação não é criada.

### EX-3: Sessão expirada ou token ausente
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** Token expirado (1h — RNF-01 do BRD) ou ausente.
* **Tratamento:** **401 Unauthorized** (CA-06); frontend limpa sessão (`clearSession`, RF-25; CA-08) e redireciona para login **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: redirecionamento não especificado no BRD]**.

### EX-4: Falha no upload de fotos (anexo opcional) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Serviço de anexo indisponível ou arquivo rejeitado (tamanho/formato).
* **Tratamento:** Premissa adotada: como o anexo é opcional no fluxo do BRD, o sistema permite submeter a solicitação sem fotos e registra a falha do anexo. Nenhum endpoint de upload foi detectado na varredura — o mecanismo real precisa ser confirmado.

### EX-5: Falha de conexão frontend-backend **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** Rede/backend indisponível no envio.
* **Tratamento:** Tratamento de erro na função `request` do `services/api.js` (varredura: `console.error` em `api.js:44`); presume-se mensagem ao usuário e não-persistência; sem retry automático nas fontes disponíveis.

## 5. Business Rules Envolvidas
* **RN-03:** Dados de entrada inválidos retornam **400 Bad Request**.
* **RN-04:** Ativo inexistente retorna **404 Not Found**.
* **RN-05:** Solicitação inicia em **Pendente**; o fluxo subsequente (Pendente → Aprovada → Em Andamento → Concluída) é executado nos UC-03/UC-04.
* **RN-06:** Funcionário pode ser vinculado a usuário do sistema para autenticação.
* **RN-07:** Ativo pertence a **um** Tipo de Ativo, **uma** Filial, **um** Departamento e **uma** Localização — base para a busca/filtragem do passo 2.

## 6. Non-Functional Requirements Relevantes
* **RNF-08 (Usabilidade):** mobile-first para solicitações (meta do BRD; a adoção do módulo depende desta UX — risco R-04 do BRD).
* **RNF-04 (Desempenho):** resposta < 200ms (p95) na criação e na busca de ativos. **Atenção:** a varredura identificou TODO de performance em `AtivoService.java:119` ("carrega até 1000 candidatos (id+nome) e faz ranking") — caminho exatamente deste fluxo (passo 2); a complexidade de `AtivoMapper.toDTO` (14) também impacta a consulta de detalhes do ativo.
* **RNF-01 (Segurança):** token JWT válido; injeção automática via interceptador (RF-24/CA-07).
* **RNF-10 (Testabilidade):** cobertura > 80% incluindo cenários de permissão Admin vs User (CA-10).
* **RNF-06 (Escalabilidade):** meta do BRD de 10k+ ativos / 1k+ usuários simultâneos — tratada como meta pendente de verificação: a varredura não detectou mecanismos de scaling horizontal no codebase (aplicação backend única).

## 7. Frequency of Use
**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Média a alta — depende do volume de manutenções da organização; esperado múltiplas vezes por dia em operações com parque grande de ativos (o BRD mira 10k+ ativos — RNF-06). Premissa: adoção do módulo pelos funcionários conforme mitigação do risco R-04 do BRD (UX mobile-first, treinamento, notificações).

## 8. Assumptions
* O campo "prioridade" é parte do modelo de Solicitação (extraído do fluxo do BRD); os valores permitidos não estão definidos no BRD.
* Anexos de fotos são opcionais e não bloqueiam a criação da solicitação.
* O vínculo Funcionário↔Usuário (RN-06) é opcional no cadastro (RF-08: `createFuncionario` e `createFuncionarioAndUsuario`).
* O método `setUsername` de `Usuario` possui corpo vazio (stub) conforme varredura (`Usuario.java:86`) — presume-se que a atualização de username não tenha efeito até implementação (ver Open Issues).

## 9. Open Issues
* Nenhuma rota explícita detectada — endpoints de criação de solicitação e de busca de ativos a confirmar no `api-specification.md`.
* Mecanismo de upload de anexos (fotos) não detectado na varredura.
* TODO de performance em `AtivoService.java:119` (ranking de até 1000 candidatos) — otimização pendente que impacta o passo 2.
* `Usuario.setUsername` é stub vazio (`Usuario.java:86`) — impacto no cadastro/vínculo de funcionário (RN-06).
* Valores permitidos para "prioridade" não definidos no BRD.

## 10. Related Artifacts
* **User Stories:** Não definidas no BRD — requisitos cobertos por RF-15, RF-18 e RF-22.
* **User Flow:** Diagrama ainda não gerado (`user-flows.md` pendente) — fluxo descrito na seção 2.
* **API envolvida:** `api-specification.md` pendente; nenhuma rota detectada. Contratos comportamentais de referência no BRD: `criar_comDadosInvalidos_deveRetornarBadRequest`, `buscarPorId_comIdInexistente_deveRetornarNotFound`.

---

# Use Case Specification: UC-03 — Aprovador Autoriza Manutenção

## 1. Characterization
* **Primary Actor:** Aprovador (Admin ou role específica — o BRD prevê CRUD de roles/permissões granulares em RF-27, mas não fixa qual role aprova).
* **Secondary Actors:**
  - **Equipe de Manutenção:** notificada após a aprovação (pós-condição do BRD).
  - **Cliente API do frontend** (`services/api.js`).
  - **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Mecanismo de notificação — a varredura detectou `AlertNotificationService` (`src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java`, método `checkResourceUsageAlerts`), porém sua ligação com o fluxo de aprovação de manutenção não está estabelecida nas fontes; o canal de notificação da equipe de manutenção é uma lacuna a confirmar.
* **Stakeholders & Interests:**
  - **Aprovação/Compliance:** validar solicitações e garantir conformidade.
  - **Gestor de Patrimônio:** controle de custo — analisa `custoTotalPorAtivo` (RF-17/RN-08).
  - **Equipe de Manutenção:** receber ordens autorizadas.
  - **Funcionário:** acompanhar o progresso da solicitação.
* **Trigger:** Existência de solicitação em status **Pendente**; aprovador acessa a fila de pendentes.
* **Preconditions:**
  - Aprovador autenticado com permissão de aprovação (RBAC — RF-26).
  - Solicitação existe e está em status **Pendente** (RN-05).
* **Postconditions (Success Guarantee):** Status da solicitação atualizado para **Aprovada** (RN-05); equipe de manutenção notificada (conforme BRD — mecanismo a confirmar, ver Secondary Actors e Open Issues).
* **Scope:** Módulo de Gestão de Manutenção (transição de aprovação) + consulta de custo por ativo (RF-17).
* **Level:** User goal.

## 2. Main Scenario (Happy Path)
1. Aprovador visualiza a fila de solicitações pendentes.
2. Aprovador analisa os detalhes da solicitação e o histórico de custos do ativo (`custoTotalPorAtivo` — RF-17; RN-08 — somatório das ordens concluídas no período).
3. Aprovador clica "Aprovar".
4. Sistema valida a transição de estado (**Pendente** → **Aprovada**, RN-05) e persiste.
5. Sistema confirma a atualização; a equipe de manutenção é notificada (pós-condição do BRD — mecanismo de notificação não confirmado nas fontes, ver seção 1 e Open Issues).
6. Caso de uso encerra com sucesso.

## 3. Alternative Scenarios

### AS-1: Aprovador cancela a solicitação (RF-20)
* **Ponto de extensão:** Passo 3 (em vez de "Aprovar").
* **Passos:**
  1. Aprovador clica "Cancelar".
  2. Sistema atualiza o status para **Cancelada** (RN-05 — pode ser cancelada a qualquer momento; CA-05 — cancelamento libera recursos).
* **Retorno ao fluxo principal:** Não — encerra com a solicitação cancelada.

### AS-2: Aprovador adia a decisão **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 2.
* **Passos:**
  1. Aprovador sai da análise sem decidir; a solicitação permanece **Pendente**.
* **Retorno ao fluxo principal:** Sim — o caso de uso pode ser retomado a qualquer momento enquanto o status for Pendente. Premissa: não há SLA técnico de expiração de solicitação pendente nas fontes; o KPI do BRD ("tempo médio de aprovação < 4 horas úteis") é meta de gestão, não comportamento de sistema.

## 4. Exception Scenarios

### EX-1: Transição de estado inválida / condição de corrida (RN-05)
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** A solicitação não está mais **Pendente** (ex.: aprovada ou cancelada por outro aprovador entre a visualização e a confirmação).
* **Tratamento:** Sistema rejeita a transição e mantém o estado consistente com RN-05. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define o código HTTP para transição inválida; presume-se 400 Bad Request (RN-03) ou 409 Conflict — a confirmar no `api-specification.md`.]**

### EX-2: Solicitação inexistente (RN-04)
* **Ponto de extensão:** Passo 1 ou 4.
* **Condição de erro:** ID da solicitação inexistente.
* **Tratamento:** **404 Not Found** com mensagem padronizada (CA-03).

### EX-3: Aprovador sem permissão (RF-26/RBAC)
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Usuário autenticado sem role/permissão de aprovação.
* **Tratamento:** **403 Forbidden**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD define 403 para escrita em entidades mestres (RN-02); a aplicação de 403 à transição de aprovação é inferida do modelo RBAC (RF-26) e do critério de cobertura de permissões (CA-10).]**

### EX-4: Timeout na consulta de custo total por ativo
* **Ponto de extensão:** Passo 2.
* **Condição de erro:** Consulta pesada de `custoTotalPorAtivo` excede o tempo aceitável — risco R-03 do BRD ("Performance em relatórios de custo total — Timeout em consultas pesadas").
* **Tratamento:** Mitigações previstas no BRD (R-03): consultas otimizadas, paginação e cache. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: nenhuma dessas mitigações foi detectada como implementada na varredura; o mecanismo de persistência atual não é discoverable a partir das fontes disponíveis (a varredura de dependências não encontrou motor de banco nem ORM).]** Presume-se que a falha degrade a análise (passo 2) sem bloquear a decisão (passos 3–4).

## 5. Business Rules Envolvidas
* **RN-05:** Fluxo **Pendente → Aprovada → Em Andamento → Concluída**; cancelamento permitido a qualquer momento (AS-1).
* **RN-08:** Custo total de manutenção por ativo = soma das ordens concluídas no período — base da análise no passo 2.
* **RN-04:** Solicitação inexistente retorna **404 Not Found**.
* **RN-01/RN-02:** (contexto RBAC) permissões diferenciadas Admin/User; evolução para permissões granulares prevista em RF-27 (risco R-01 do BRD: começar com 2 roles e evoluir).

## 6. Non-Functional Requirements Relevantes
* **RNF-04 (Desempenho):** resposta < 200ms (p95), **excluindo operações de relatório pesado** — a consulta de `custoTotalPorAtivo` (passo 2) está explicitamente fora dessa garantia, conforme RNF-04 do BRD.
* **RNF-07 (Auditoria):** a transição de escrita (aprovar) deve gerar registro de auditoria imutável (meta do BRD; implementação não detectada na varredura).
* **RNF-01 (Segurança):** token JWT válido durante a análise (meta do BRD).
* KPI associado (BRD): tempo médio de aprovação < 4 horas úteis (meta semanal de gestão).

## 7. Frequency of Use
**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Média — proporcional ao volume de solicitações criadas (UC-02); esperado múltiplas vezes por dia em operações com parque grande de ativos. Premissa: a fila de pendentes é revisada continuamente pelos aprovadores (KPI do BRD: tempo médio de aprovação < 4 horas úteis).

## 8. Assumptions
* A role de aprovação é Admin ou role específica criável via RF-27 (`createRole`, `createPermission`) — o BRD não fixa qual role aprova.
* A notificação da equipe de manutenção (pós-condição do BRD) depende de mecanismo a confirmar (ver seção 1 — Secondary Actors).
* A consulta `custoTotalPorAtivo` (RF-17) retorna o somatório das ordens concluídas no período (RN-08) e está disponível ao aprovador no passo 2.
* O filtro dinâmico da fila de manutenção está implementado em `ManutencaoSpecification` (varredura: `ManutencaoSpecification.java:26`, método `build` com complexidade 14) — presume-se que suporte a filtragem de pendentes.

## 9. Open Issues
* Nenhuma rota explícita detectada — endpoints de listagem de pendentes e de aprovação a confirmar no `api-specification.md`.
* Role de aprovação não definida explicitamente no BRD (Admin vs role específica).
* Mecanismo de notificação da equipe de manutenção não confirmado; o `AlertNotificationService` detectado na varredura trata alertas de uso de recursos (`checkResourceUsageAlerts`), sem ligação estabelecida com a aprovação.
* Código HTTP para transição inválida (RN-05) não definido no BRD.
* Complexidade ciclomática alta em `ManutencaoSpecification.build` (14) — risco de manutenção nos filtros da fila.
* Implementação do audit trail (RNF-07) não detectada na varredura.

## 10. Related Artifacts
* **User Stories:** Não definidas no BRD — requisitos cobertos por RF-17, RF-19, RF-20 e RF-22.
* **User Flow:** Diagrama ainda não gerado (`user-flows.md` pendente) — fluxo descrito na seção 2.
* **API envolvida:** `api-specification.md` pendente; nenhuma rota detectada. Contratos comportamentais de referência no BRD: `aprovar`, `cancelar`, `custoTotalPorAtivo`.

---

# Use Case Specification: UC-04 — Técnico Conclui Ordem de Serviço

## 1. Characterization
* **Primary Actor:** Equipe de Manutenção (técnico designado).
* **Secondary Actors:**
  - **Cliente API do frontend** (`services/api.js`).
  - **Gestor de Patrimônio:** consumidor do histórico e do custo total atualizado (RF-22, RF-17).
  - **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Fluxo de designação de técnico — a pré-condição "técnico designado" do BRD não possui requisito funcional correspondente (RF-18..RF-22 não cobrem designação); presume-se designação manual/externa.
* **Stakeholders & Interests:**
  - **Equipe de Manutenção:** registrar a execução e encerrar a ordem.
  - **Gestor de Patrimônio:** custo total por ativo atualizado e confiável (RN-08; CA-09).
  - **Aprovação/Compliance:** rastreabilidade das intervenções (RNF-07).
  - **Funcionário:** ativo devolvido/disponível.
* **Trigger:** Execução do serviço pelo técnico; técnico registra os dados e clica "Concluir".
* **Preconditions:**
  - Solicitação em status **Aprovada** ou **Em Andamento** (RN-05).
  - Técnico designado (origem da designação não especificada — ver Secondary Actors).
  - Técnico autenticado com permissão de execução (RBAC — RF-26). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define a role do técnico; presume-se dentro do modelo Admin/User ou de role específica via RF-27.]**
* **Postconditions (Success Guarantee):** Status atualizado para **Concluída** (RN-05); histórico do ativo atualizado (RF-22); `custoTotalPorAtivo` reflete a nova ordem concluída (RN-08); ativo disponível.
* **Scope:** Módulo de Gestão de Manutenção (conclusão de ordem) + atualização de custos do ativo (RF-17).
* **Level:** User goal.

## 2. Main Scenario (Happy Path)
1. Técnico executa o serviço no ativo.
2. Técnico registra: data, descrição, peças utilizadas, custo e tempo.
3. Técnico clica "Concluir"; o cliente API injeta o token (RF-24) e o sistema valida os dados (RN-03).
4. Sistema valida a transição de estado (**Aprovada/Em Andamento** → **Concluída**, RN-05) e persiste a ordem.
5. Sistema atualiza o `custoTotalPorAtivo` do ativo (RF-17; RN-08 — somatório das ordens concluídas no período).
6. Caso de uso encerra com sucesso; histórico atualizado e ativo disponível.

## 3. Alternative Scenarios

### AS-1: Técnico registra execução parcial e retoma depois **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 2.
* **Passos:**
  1. Técnico salva dados parciais sem concluir; a ordem permanece **Aprovada/Em Andamento**.
  2. Técnico retoma o registro e conclui posteriormente.
* **Retorno ao fluxo principal:** Sim, no passo 2. Premissa: o estado "Em Andamento" do fluxo (RN-05) suporta execução parcial; o mecanismo de salvamento parcial não está especificado no BRD.

### AS-2: Ordem cancelada antes da conclusão
* **Ponto de extensão:** Passo 2 (antes de "Concluir").
* **Passos:**
  1. A solicitação é cancelada (RF-20) por aprovador ou solicitante.
  2. Status → **Cancelada** (RN-05; CA-05 — libera recursos); a ordem não é concluída e **não compõe** o custo total (RN-08 — apenas ordens concluídas somam).
* **Retorno ao fluxo principal:** Não — encerra.

## 4. Exception Scenarios

### EX-1: Transição de estado inválida (RN-05)
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** Solicitação não está em **Aprovada** nem **Em Andamento** (ex.: Pendente ou Cancelada).
* **Tratamento:** Sistema rejeita a transição e mantém a consistência com RN-05. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: código HTTP para transição inválida não definido no BRD; presume-se 400 Bad Request ou 409 Conflict.]**

### EX-2: Dados de execução inválidos (RN-03)
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Custo/tempo com formato inválido; campos obrigatórios ausentes (data, descrição, peças).
* **Tratamento:** **400 Bad Request** com mensagem clara (CA-02); a ordem não é concluída e o `custoTotalPorAtivo` não é alterado.

### EX-3: Ordem/solicitação inexistente (RN-04)
* **Ponto de extensão:** Passo 4.
* **Condição de erro:** ID inexistente.
* **Tratamento:** **404 Not Found** com mensagem padronizada (CA-03).

### EX-4: Divergência entre relatório de custo e ordens concluídas (CA-09)
* **Ponto de extensão:** Passo 5.
* **Condição de erro:** `custoTotalPorAtivo` não bate com a soma das ordens concluídas no período.
* **Tratamento:** O critério de aceitação CA-09 exige conciliação ("relatórios de custo total por ativo batem com soma das ordens concluídas"). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: o BRD não define mecanismo de reconciliação automática; presume-se verificação em testes de integração e correção manual.]**

### EX-5: Sessão expirada durante o registro em campo **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Ponto de extensão:** Passo 3.
* **Condição de erro:** Token expirado (1h — RNF-01 do BRD) após execução prolongada em campo.
* **Tratamento:** **401 Unauthorized** (CA-06); novo login e re-submissão dos dados. Premissa: não há draft automático nas fontes disponíveis — dados não submetidos podem ser perdidos.

## 5. Business Rules Envolvidas
* **RN-05:** Transição **Aprovada/Em Andamento → Concluída**; cancelamento permitido a qualquer momento (AS-2).
* **RN-08:** Custo total de manutenção por ativo = soma das ordens concluídas no período — a conclusão (passo 5) alimenta o indicador.
* **RN-03:** Dados de entrada inválidos retornam **400 Bad Request**.
* **RN-04:** Ordem inexistente retorna **404 Not Found**.
* **RN-07:** (contexto) o ativo permanece vinculado a **uma** Filial, **um** Departamento e **uma** Localização após a conclusão.

## 6. Non-Functional Requirements Relevantes
* **RNF-04 (Desempenho):** resposta < 200ms (p95) na conclusão (meta do BRD).
* **RNF-07 (Auditoria):** escrita (concluir) deve gerar registro de auditoria imutável (meta do BRD; implementação não detectada na varredura).
* **RNF-01 (Segurança):** token JWT válido; a expiração de 1h é relevante para execução em campo (EX-5).
* **RNF-10 (Testabilidade):** cobertura > 80% incluindo o cenário de conciliação de custos (CA-09).

## 7. Frequency of Use
**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Média a alta — cada solicitação aprovada (UC-03) gera uma conclusão; esperado múltiplas vezes por dia em operações com parque grande de ativos. Premissa: taxa de solicitações canceladas < 10% (KPI do BRD), logo a maioria das ordens aprovadas chega à conclusão.

## 8. Assumptions
* A designação do técnico é pré-condição sem requisito funcional associado no BRD — presume-se designação manual/externa (ver seção 1).
* Os campos de registro (data, descrição, peças, custo, tempo) correspondem ao modelo da ordem — extraídos do fluxo do BRD; o modelo completo não foi verificado na varredura.
* O estado "Em Andamento" (RN-05) é alcançável entre Aprovada e Concluída, embora nenhum requisito funcional do BRD descreva a transição que o dispara (lacuna — ver Open Issues).
* Apenas ordens **Concluídas** compõem o `custoTotalPorAtivo` (RN-08).

## 9. Open Issues
* Nenhuma rota explícita detectada — endpoint de conclusão de ordem a confirmar no `api-specification.md`.
* Transição para "Em Andamento" não coberta por RF-18..RF-22 (o fluxo RN-05 a prevê, mas não há requisito/UC que a dispare).
* Fluxo de designação de técnico não definido no BRD.
* Código HTTP para transição inválida (RN-05) não definido no BRD.
* Implementação do audit trail (RNF-07) não detectada na varredura.

## 10. Related Artifacts
* **User Stories:** Não definidas no BRD — requisitos cobertos por RF-17, RF-21 e RF-22.
* **User Flow:** Diagrama ainda não gerado (`user-flows.md` pendente) — fluxo descrito na seção 2.
* **API envolvida:** `api-specification.md` pendente; nenhuma rota detectada. Contratos comportamentais de referência no BRD: `concluir`, `custoTotalPorAtivo`.