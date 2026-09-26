# Use Case Specification: UC-01 — Gerenciar Cadastros Mestres

## 1. Characterization
* **Primary Actor:** Administrador de Cadastros
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Administrador de Cadastros — mantém dados mestres consistentes para relatórios e alocação
  - Gestor de Manutenção — depende de departamentos, filiais, fornecedores e funcionários corretos para ordens
* **Trigger:** Usuário acessa módulo de cadastros e seleciona criar, listar, buscar, atualizar ou excluir entidade
* **Preconditions:** 
  - Usuário autenticado com perfil de administrador
  - Backend disponível e respondendo
* **Postconditions (Success Guarantee):** Entidade criada/atualizada/excluída com sucesso; lista atualizada refletindo alteração; validações de integridade referencial respeitadas
* **Scope:** Frontend Aegis1 — módulo de cadastros (departamentos, filiais, fornecedores, funcionários)
* **Level:** User goal

## 2. Main Scenario (Happy Path) — Criar Entidade
1. Administrador acessa tela de cadastro da entidade desejada (departamento/filial/fornecedor/funcionário)
2. Sistema exibe formulário vazio com campos obrigatórios marcados
3. Administrador preenche dados e submete
4. Frontend valida campos obrigatórios localmente
5. Frontend envia `POST /api/{entidade}` via `api.js:request` com payload JSON
6. Backend valida regras de negócio e persiste
7. Backend retorna `201 Created` com entidade criada (incluindo ID)
8. Frontend exibe toast de sucesso e atualiza lista (chama `listar`)
9. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Editar Entidade Existente
* **Ponto de extensão:** Passo 1 — administrador clica em "Editar" na linha da lista
* **Passos:**
  1. Sistema busca entidade via `GET /api/{entidade}/{id}` (`buscarPorId`)
  2. Preenche formulário com dados atuais
  3. Administrador altera campos e submete
  4. Frontend envia `PUT /api/{entidade}/{id}`
  5. Backend retorna `200 OK` com entidade atualizada
  6. Frontend atualiza lista e exibe sucesso
* **Retorno ao fluxo principal:** Não, encerra como fluxo independente

### AS-2: Excluir Entidade Sem Vinculações
* **Ponto de extensão:** Passo 1 — administrador clica em "Excluir" e confirma no modal
* **Passos:**
  1. Frontend envia `DELETE /api/{entidade}/{id}`
  2. Backend verifica ausência de ordens vinculadas (BR-05)
  3. Backend remove e retorna `204 No Content`
  4. Frontend remove da lista local e exibe sucesso
* **Retorno ao fluxo principal:** Não, encerra

## 4. Exception Scenarios
### EX-1: Validação de Campos Obrigatórios Falha (Frontend)
* **Ponto de extensão:** Passo 4 do cenário principal
* **Condição de erro:** Campo obrigatório vazio ou formato inválido
* **Tratamento:** Frontend bloqueia submissão, destaca campo em vermelho, exibe mensagem inline "Campo obrigatório" — não chama API

### EX-2: Entidade Duplicada (Backend 409)
* **Ponto de extensão:** Passo 6 do cenário principal
* **Condição de erro:** Backend retorna `409 Conflict` (ex: código de departamento já existe)
* **Tratamento:** `api.js:handleApiError` captura, frontend exibe toast "Registro já existe" e mantém formulário aberto para correção

### EX-3: Exclusão Bloqueada por Ordens Vinculadas (BR-05)
* **Ponto de extensão:** Passo 2 do AS-2
* **Condição de erro:** Backend retorna `409 Conflict` com mensagem "Entidade possui ordens vinculadas"
* **Tratamento:** `handleApiError` processa, frontend exibe modal informativo: "Não é possível excluir — existem ordens de manutenção associadas. Reatribua ou conclua as ordens antes."

### EX-4: Falha de Rede / Timeout
* **Ponto de extensão:** Qualquer chamada `request`
* **Condição de erro:** `fetch` rejeita ou timeout (sem resposta em 30s)
* **Tratamento:** `handleApiError` registra no console (dev) / Sentry (prod), exibe toast "Erro de conexão — tente novamente", mantém estado anterior

## 5. Business Rules Envolvidas
* **BR-05:** Exclusão de entidades mestras bloqueada se houver ordens vinculadas — backend retorna 409, frontend trata via `handleApiError`
* **BR-06:** Todas as mutações passam por `authInterceptor` para anexar token JWT; expiração dispara refresh automático antes de retry único

## 6. Non-Functional Requirements Relevantes
* Latência P95 da chamada `request` < 800 ms (BRD Guardrail)
* Taxa de erro de integração API < 1% (BRD Guardrail)
* Zero vazamento de dados sensíveis no frontend (authInterceptor) — BRD Guardrail

## 7. Frequency of Use
Baixa a média — usado na configuração inicial e manutenção esporádica de cadastros

## 8. Assumptions
* Backend expõe endpoints REST compatíveis: `POST/GET/PUT/DELETE /api/{departamentos|filiais|fornecedores|funcionarios}` e `/api/{entidade}/{id}`
* Payloads seguem contratos implícitos no `api.js` (não documentados no codebase atual)
* `authInterceptor` em `api.js` já implementa refresh token automático

## 9. Open Issues
* Contrato OpenAPI formal não existe — alinhar com time de Backend antes de testes integrados
* Tratamento de `console.error/log` residuais em `api.js:26,49,52` deve ser removido antes de produção (diagnóstico)

## 10. Related Artifacts
* **User Stories:** US-CAD-01 a US-CAD-20 (CRUD 4 entidades × 5 operações)
* **User Flow:** `user-flows.md#cadastros-mestres`
* **API envolvida:** `api-specification.md#cadastros-mestres`


# Use Case Specification: UC-02 — Criar Ordem de Manutenção

## 1. Characterization
* **Primary Actor:** Gestor de Manutenção ou Técnico de Campo (quem abre a solicitação)
* **Secondary Actors:** Sistema Backend (API REST), Administrador de Cadastros (dados mestres pré-existentes)
* **Stakeholders & Interests:** 
  - Gestor — rastreabilidade desde a origem
  - Técnico — clareza do que executar
  - Aprovador — base para decisão posterior
* **Trigger:** Usuário clica "Nova Ordem" no dashboard ou lista de ordens
* **Preconditions:** 
  - Usuário autenticado
  - Pelo menos um departamento, filial, fornecedor (se externo) e funcionário (técnico responsável) cadastrados
  - Ativo/equipamento identificado (informado no formulário)
* **Postconditions (Success Guarantee):** Ordem criada no estado "Aberta" com ID único, técnico responsável alocado, visível na lista para início
* **Scope:** Frontend Aegis1 — módulo de ordens de manutenção
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Usuário acessa "Nova Ordem de Manutenção"
2. Sistema exibe formulário com campos: ativo, descrição do problema, prioridade, departamento, filial, técnico responsável, tipo (preventiva/corretiva), fornecedor (opcional)
3. Usuário preenche campos obrigatórios e submete
4. Frontend valida localmente (campos obrigatórios, técnico alocado)
5. Frontend envia `POST /api/ordens` via `request` com payload
6. Backend valida BR-01 (estado "Aberta" + técnico alocado) e persiste
7. Backend retorna `201 Created` com ordem completa (ID, estado "Aberta", timestamps)
8. Frontend exibe toast "Ordem criada com sucesso", redireciona para detalhe da ordem (`buscarPorId`)
9. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Criar a Partir de Template/Checklist (Futuro)
* **Ponto de extensão:** Passo 2 — usuário seleciona "Usar template"
* **Passos:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: futuro módulo de checklists digitais (BRD Future Considerations). Não implementado no escopo atual.
* **Retorno ao fluxo principal:** Não aplicável (fora de escopo)

## 4. Exception Scenarios
### EX-1: Técnico Responsável Não Informado (BR-01)
* **Ponto de extensão:** Passo 4
* **Condição de erro:** Campo "técnico responsável" vazio
* **Tratamento:** Frontend bloqueia submissão, destaca campo, mensagem "Técnico responsável é obrigatório para criar a ordem (BR-01)"

### EX-2: Dados Mestres Ausentes
* **Ponto de extensão:** Passo 2
* **Condição de erro:** Listas de departamentos/filiais/funcionários vazias (GET retorna array vazio)
* **Tratamento:** Frontend exibe alerta "Cadastre departamentos, filiais e técnicos antes de criar ordens" e desabilita botão "Nova Ordem"

### EX-3: Erro de Validação Backend (400/422)
* **Ponto de extensão:** Passo 6
* **Condição de erro:** Backend rejeita payload (ex: ativo inexistente, filial inválida)
* **Tratamento:** `handleApiError` exibe toast com mensagem do backend, mantém formulário preenchido para correção

### EX-4: Falha de Autenticação / Token Expirado (BR-06)
* **Ponto de extensão:** Passo 5
* **Condição de erro:** `authInterceptor` detecta 401, tenta refresh, falha
* **Tratamento:** Redireciona para tela de login limpa, preserva dados do formulário em `sessionStorage` para recuperação pós-login

## 5. Business Rules Envolvidas
* **BR-01:** Uma ordem só pode ser iniciada se estiver no estado "Aberta" e tiver técnico responsável alocado — criação já exige técnico
* **BR-06:** Mutações passam por `authInterceptor` com JWT + refresh automático

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms (criação)
* Taxa de erro API < 1%
* Formulário deve carregar listas mestras em < 1s (cache local após primeiro load)

## 7. Frequency of Use
Alta — múltiplas vezes por dia por gestores e técnicos

## 8. Assumptions
* Backend endpoint `POST /api/ordens` existe e aceita contrato implícito
* Estados de ordem: "Aberta" → "Em Andamento" → "Aprovada" → "Concluída" / "Cancelada"
* Prioridade: enum (Baixa, Média, Alta, Crítica)

## 9. Open Issues
* Definir campos obrigatórios vs opcionais no contrato de API
* Validação de ativo: backend valida existência? Frontend precisa de autocomplete?

## 10. Related Artifacts
* **User Stories:** US-ORD-01 (Criar ordem)
* **User Flow:** `user-flows.md#criar-ordem`
* **API envolvida:** `api-specification.md#ordens-post`


# Use Case Specification: UC-03 — Iniciar Ordem de Manutenção

## 1. Characterization
* **Primary Actor:** Técnico de Campo
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Técnico — registra início real do trabalho
  - Gestor — visibilidade de ordens em andamento
  - Aprovador — sabe que execução começou
* **Trigger:** Técnico abre ordem no estado "Aberta" e clica "Iniciar"
* **Preconditions:** 
  - Ordem existe no estado "Aberta"
  - Técnico logado é o responsável alocado na ordem (ou tem permissão de delegação)
  - Backend disponível
* **Postconditions (Success Guarantee):** Ordem transiciona para "Em Andamento", timestamp de início registrado, botão "Iniciar" desabilitado, "Aprovar" ainda não habilitado
* **Scope:** Frontend Aegis1 — detalhe da ordem / ação "Iniciar"
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Técnico acessa lista de ordens, filtra "Minhas Ordens" / "Abertas"
2. Clica na ordem desejada → abre detalhe (`GET /api/ordens/{id}` → `buscarPorId`)
3. Sistema exibe detalhes + botão "Iniciar" habilitado (estado "Aberta" + técnico responsável = usuário logado)
4. Técnico clica "Iniciar", confirma no modal
5. Frontend envia `PATCH /api/ordens/{id}/iniciar` (ou `PUT /api/ordens/{id}` com estado "Em Andamento")
6. Backend valida BR-01 (estado "Aberta" + técnico alocado) e transiciona
7. Backend retorna `200 OK` com ordem atualizada (estado "Em Andamento", `iniciadoEm`, `iniciadoPor`)
8. Frontend atualiza UI: badge "Em Andamento", botão "Iniciar" oculto, timestamp visível
9. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Delegar Início a Outro Técnico (Futuro)
* **Ponto de extensão:** Passo 3 — técnico não é o responsável mas tem permissão
* **Passos:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: regra de delegação não definida no BRD. Fora de escopo atual.

## 4. Exception Scenarios
### EX-1: Ordem Não Está "Aberta" (BR-01)
* **Ponto de extensão:** Passo 3 ou 6
* **Condição de erro:** Ordem já "Em Andamento", "Aprovada", "Concluída" ou "Cancelada"
* **Tratamento:** Backend retorna `409 Conflict` "Transição inválida", `handleApiError` exibe toast, frontend desabilita botão "Iniciar" e recarrega estado via `buscarPorId`

### EX-2: Técnico Logado Não É o Responsável
* **Ponto de extensão:** Passo 3
* **Condição de erro:** `responsavelId` ≠ `usuarioLogado.id` e sem permissão de delegação
* **Tratamento:** Frontend oculta botão "Iniciar" (ou exibe desabilitado com tooltip "Apenas o técnico responsável pode iniciar")

### EX-3: Falha de Rede / Timeout
* **Ponto de extensão:** Passo 5
* **Condição de erro:** `request` falha (rede, timeout, 5xx)
* **Tratamento:** `handleApiError` loga, toast "Erro ao iniciar — tente novamente", botão permanece habilitado para retry

### EX-4: Token Expirado Durante Ação (BR-06)
* **Ponto de extensão:** Passo 5
* **Condição de erro:** `authInterceptor` recebe 401, refresh falha
* **Tratamento:** Redireciona login, preserva `ordemId` para retomar após autenticação

## 5. Business Rules Envolvidas
* **BR-01:** Ordem só pode ser iniciada se estado "Aberta" e técnico responsável alocado
* **BR-06:** `authInterceptor` anexa JWT, refresh automático + retry único

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms (ação de transição de estado)
* Disponibilidade: ação deve funcionar offline-first? [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: PWA com sincronização posterior não especificado no BRD. Escopo atual: apenas online.

## 7. Frequency of Use
Muito alta — cada ordem ativa passa por este passo uma vez

## 8. Assumptions
* Endpoint de transição: `PATCH /api/ordens/{id}/iniciar` ou `PUT /api/ordens/{id}` com `{ estado: "EM_ANDAMENTO" }` — a confirmar com Backend
* Backend registra `iniciadoEm` (timestamp server) e `iniciadoPor` (userId do token)

## 9. Open Issues
* Contrato exato do endpoint de transição de estado não documentado
* Permissão de delegação não definida — assumir "apenas responsável" por enquanto

## 10. Related Artifacts
* **User Stories:** US-ORD-02 (Iniciar ordem)
* **User Flow:** `user-flows.md#iniciar-ordem`
* **API envolvida:** `api-specification.md#ordens-patch-iniciar`


# Use Case Specification: UC-04 — Aprovar Ordem de Manutenção

## 1. Characterization
* **Primary Actor:** Aprovador/Supervisor
* **Secondary Actors:** Sistema Backend (API REST), Técnico de Campo (evidências)
* **Stakeholders & Interests:** 
  - Aprovador — valida execução com evidências
  - Gestor — conformidade auditável
  - Compliance — rastreabilidade para NR-10/NR-12
* **Trigger:** Aprovador abre ordem no estado "Em Andamento" e clica "Aprovar"
* **Preconditions:** 
  - Ordem no estado "Em Andamento"
  - Backend retorna `canApprove: true` na busca da ordem (evidências presentes)
  - Aprovador autenticado com perfil de aprovação
* **Postconditions (Success Guarantee):** Ordem transiciona para "Aprovada", timestamp e autor registrados, botão "Concluir" habilitado para técnico, "Cancelar" ainda disponível
* **Scope:** Frontend Aegis1 — detalhe da ordem / ação "Aprovar"
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Aprovador acessa lista de ordens "Para Aprovação" (filtro estado "Em Andamento" + `canApprove: true`)
2. Clica na ordem → abre detalhe (`buscarPorId`)
3. Sistema exibe evidências anexadas (checklist, fotos — retornadas pela API) + botão "Aprovar" habilitado
4. Aprovador revisa evidências, clica "Aprovar", confirma no modal
5. Frontend envia `PATCH /api/ordens/{id}/aprovar` (ou `PUT` com estado "Aprovada")
6. Backend valida BR-02 (evidência presente) e transiciona
7. Backend retorna `200 OK` com ordem atualizada (estado "Aprovada", `aprovadoEm`, `aprovadoPor`)
8. Frontend atualiza UI: badge "Aprovada", botão "Aprovar" oculto, "Concluir" visível para técnico
9. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Aprovar com Observações
* **Ponto de extensão:** Passo 4 — modal inclui campo "Observações da aprovação" (opcional)
* **Passos:**
  1. Aprovador preenche observações
  2. Payload inclui `observacoesAprovacao`
  3. Backend persiste no histórico da ordem
* **Retorno ao fluxo principal:** Sim, mesmo endpoint, dado adicional

## 4. Exception Scenarios
### EX-1: Evidências Insuficientes (BR-02)
* **Ponto de extensão:** Passo 3 ou 6
* **Condição de erro:** Backend retorna `canApprove: false` ou `409 Conflict` "Evidências necessárias"
* **Tratamento:** Frontend oculta/desabilita "Aprovar", exibe banner "Aguardando evidências do técnico (checklist/foto)". Técnico deve anexar antes.

### EX-2: Ordem Não Está "Em Andamento"
* **Ponto de extensão:** Passo 6
* **Condição de erro:** Estado divergente (ex: já "Aprovada" ou "Cancelada")
* **Tratamento:** `409 Conflict` "Transição inválida", `handleApiError` toast, recarrega estado

### EX-3: Aprovador Sem Permissão
* **Ponto de extensão:** Passo 3
* **Condição de erro:** Usuário logado não tem role "aprovador"
* **Tratamento:** Frontend não exibe botão "Aprovar" (controle de UI baseado em role do token/JWT)

### EX-4: Falha de Rede / Timeout / Auth (BR-06)
* **Ponto de extensão:** Passo 5
* **Condição de erro:** Mesmos padrões de UC-03
* **Tratamento:** Idem UC-03 EX-3/EX-4

## 5. Business Rules Envolvidas
* **BR-02:** Aprovar exige evidência de execução (checklist assinado ou foto) — backend valida, frontend habilita botão apenas quando `canApprove: true`
* **BR-03:** Concluir só permitido após aprovar; cancelar permitido em qualquer estado exceto "Concluída"
* **BR-06:** `authInterceptor` com JWT + refresh

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms
* Auditoria: log de aprovação imutável (backend) — frontend apenas exibe

## 7. Frequency of Use
Alta — cada ordem concluída passa por aprovação

## 8. Assumptions
* Endpoint: `PATCH /api/ordens/{id}/aprovar` com body opcional `{ observacoes }`
* API retorna `canApprove: boolean` no `GET /api/ordens/{id}` quando estado = "Em Andamento"
* Evidências (fotos, checklists) são anexadas pelo técnico em fluxo separado (fora de escopo atual — BRD Future Considerations)

## 9. Open Issues
* Como técnico anexa evidências no escopo atual? BRD menciona "checklist assinado ou foto" mas Future Considerations lista "Checklists digitais e anexos fotográficos" como futuro. **Gap crítico** — alinhar com Backend se evidência é apenas campo texto "relato" no MVP.
* Permissão de aprovação: role-based ou por alocação? Assumir role "aprovador" no token.

## 10. Related Artifacts
* **User Stories:** US-ORD-03 (Aprovar ordem)
* **User Flow:** `user-flows.md#aprovar-ordem`
* **API envolvida:** `api-specification.md#ordens-patch-aprovar`


# Use Case Specification: UC-05 — Concluir Ordem de Manutenção

## 1. Characterization
* **Primary Actor:** Técnico de Campo
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Técnico — fecha ordem após execução aprovada
  - Gestor — custoTotalPorAtivo atualizado automaticamente
  - Financeiro — base para rateio de custos
* **Trigger:** Técnico abre ordem no estado "Aprovada" e clica "Concluir"
* **Preconditions:** 
  - Ordem no estado "Aprovada" (BR-03)
  - Técnico logado é o responsável (ou tem permissão)
  - Custos (mão de obra, material, terceiros) informados ou calculados
* **Postconditions (Success Guarantee):** Ordem transiciona para "Concluída", timestamp registrado, custos consolidados, `custoTotalPorAtivo` do ativo atualizado no backend, botões de ação todos desabilitados
* **Scope:** Frontend Aegis1 — detalhe da ordem / ação "Concluir"
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Técnico acessa "Minhas Ordens" → filtro "Aprovadas"
2. Clica na ordem → detalhe (`buscarPorId`)
3. Sistema exibe resumo de custos (se já informados) + botão "Concluir" habilitado (estado "Aprovada")
4. Técnico clica "Concluir", modal solicita confirmação e permite ajustar custos finais (horas reais, materiais usados, valor terceiros)
5. Técnico confirma
6. Frontend envia `PATCH /api/ordens/{id}/concluir` com `{ custos: { maoDeObra, materiais, terceiros } }`
7. Backend valida BR-03 (estado "Aprovada"), persiste custos, recalcula `custoTotalPorAtivo` do ativo, transiciona para "Concluída"
8. Backend retorna `200 OK` com ordem finalizada + `custoTotalPorAtivo` atualizado do ativo
9. Frontend atualiza UI: badge "Concluída", todos botões de ação desabilitados, exibe custo total da ordem e do ativo
10. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Concluir Sem Ajuste de Custos (Valores Padrão)
* **Ponto de extensão:** Passo 4 — técnico aceita custos sugeridos/preenchidos anteriormente
* **Passos:** Modal exibe valores pré-preenchidos, técnico apenas confirma
* **Retorno ao fluxo principal:** Sim, mesmo endpoint

## 4. Exception Scenarios
### EX-1: Ordem Não Está "Aprovada" (BR-03)
* **Ponto de extensão:** Passo 3 ou 7
* **Condição de erro:** Estado ≠ "Aprovada" (ex: "Em Andamento", "Cancelada")
* **Tratamento:** Backend `409 Conflict` "Concluir só permitido após aprovação", frontend toast, recarrega estado

### EX-2: Custos Obrigatórios Não Informados
* **Ponto de extensão:** Passo 6
* **Condição de erro:** Backend exige pelo menos um custo > 0, retorna `400 Bad Request`
* **Tratamento:** `handleApiError` exibe erros de validação por campo no modal, técnico corrige e reenvia

### EX-3: Falha no Cálculo de custoTotalPorAtivo (Backend)
* **Ponto de extensão:** Passo 7
* **Condição de erro:** Backend retorna `500` ou inconsistência no valor retornado
* **Tratamento:** `handleApiError` loga, toast "Erro ao finalizar — contate suporte", ordem pode ficar em estado inconsistente → requer intervenção manual

### EX-4: Rede / Auth (BR-06)
* **Ponto de extensão:** Passo 6
* **Tratamento:** Idem UC-03 EX-3/EX-4

## 5. Business Rules Envolvidas
* **BR-03:** Concluir só permitido após aprovar; cancelar permitido em qualquer estado exceto "Concluída"
* **BR-04:** `custoTotalPorAtivo` = soma de custos (mão de obra + material + terceiros) de ordens **concluídas** vinculadas ao ativo — cálculo no backend, frontend apenas exibe
* **BR-06:** `authInterceptor` JWT + refresh

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms
* Consistência: `custoTotalPorAtivo` exibido no frontend deve bater com backend (teste de contrato Pact — BRD Risk)

## 7. Frequency of Use
Alta — cada ordem aprovada é concluída

## 8. Assumptions
* Endpoint: `PATCH /api/ordens/{id}/concluir` com body de custos
* Backend recalcula `custoTotalPorAtivo` atomicamente na transação de conclusão
* Frontend não calcula — apenas exibe valor retornado

## 9. Open Issues
* Campos de custo no modal: quais obrigatórios? Validação frontend vs backend?
* Permissão de concluir: apenas técnico responsável ou qualquer técnico da equipe?

## 10. Related Artifacts
* **User Stories:** US-ORD-04 (Concluir ordem)
* **User Flow:** `user-flows.md#concluir-ordem`
* **API envolvida:** `api-specification.md#ordens-patch-concluir`


# Use Case Specification: UC-06 — Cancelar Ordem de Manutenção

## 1. Characterization
* **Primary Actor:** Gestor de Manutenção ou Técnico de Campo (conforme permissão)
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Gestor — cancela ordens desnecessárias/duplicadas
  - Técnico — cancela se não for mais executar
  - Auditoria — rastro de cancelamento com justificativa
* **Trigger:** Usuário abre ordem (estado ≠ "Concluída") e clica "Cancelar"
* **Preconditions:** 
  - Ordem em qualquer estado exceto "Concluída" (BR-03)
  - Usuário com permissão de cancelamento (gestor ou técnico responsável)
* **Postconditions (Success Guarantee):** Ordem transiciona para "Cancelada", timestamp e autor registrados, justificativa salva, ordem não entra em `custoTotalPorAtivo` (apenas concluídas contam — BR-04)
* **Scope:** Frontend Aegis1 — detalhe da ordem / ação "Cancelar"
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Usuário abre ordem (estado "Aberta", "Em Andamento" ou "Aprovada")
2. Sistema exibe botão "Cancelar" habilitado (BR-03: permitido exceto "Concluída")
3. Usuário clica "Cancelar", modal exige justificativa obrigatória (texto livre)
4. Usuário preenche justificativa e confirma
5. Frontend envia `PATCH /api/ordens/{id}/cancelar` com `{ justificativa }`
6. Backend valida estado ≠ "Concluída", persiste, transiciona para "Cancelada"
7. Backend retorna `200 OK` com ordem cancelada (`canceladoEm`, `canceladoPor`, `justificativaCancelamento`)
8. Frontend atualiza UI: badge "Cancelada", botões de ação desabilitados, justificativa visível no histórico
9. Caso de uso encerra com sucesso

## 3. Alternative Scenarios
### AS-1: Cancelar Durante Criação (Antes de Iniciar)
* **Ponto de extensão:** Ordem no estado "Aberta" — mesmo fluxo, justificativa "Duplicada / Não procedente"
* **Retorno ao fluxo principal:** Mesmo cenário principal

## 4. Exception Scenarios
### EX-1: Ordem Já "Concluída" (BR-03)
* **Ponto de extensão:** Passo 2 ou 6
* **Condição de erro:** Estado = "Concluída"
* **Tratamento:** Frontend não exibe botão "Cancelar" (UI); se chamada forçada, backend `409 Conflict` "Ordem concluída não pode ser cancelada"

### EX-2: Justificativa Vazia
* **Ponto de extensão:** Passo 3
* **Condição de erro:** Usuário tenta confirmar sem preencher
* **Tratamento:** Frontend valida localmente, bloqueia botão "Confirmar", mensagem "Justificativa é obrigatória"

### EX-3: Sem Permissão
* **Ponto de extensão:** Passo 2
* **Condição de erro:** Usuário não é gestor nem técnico responsável
* **Tratamento:** Botão "Cancelar" oculto/desabilitado (UI baseada em role/ownership)

### EX-4: Rede / Auth
* **Ponto de extensão:** Passo 5
* **Tratamento:** Idem UC-03

## 5. Business Rules Envolvidas
* **BR-03:** Cancelar permitido em qualquer estado exceto "Concluída"
* **BR-04:** `custoTotalPorAtivo` soma apenas ordens **concluídas** — canceladas não entram
* **BR-06:** `authInterceptor` JWT + refresh

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms
* Auditoria: justificativa imutável após confirmação

## 7. Frequency of Use
Baixa a média — exceção, não regra

## 8. Assumptions
* Endpoint: `PATCH /api/ordens/{id}/cancelar` com `{ justificativa }`
* Justificativa: string obrigatória, máx 500 chars (a definir)

## 9. Open Issues
* Quem pode cancelar em cada estado? (ex: técnico cancela "Aberta", gestor cancela "Aprovada") — não definido no BRD
* Cancelamento de ordem "Em Andamento" deve estornar custos parciais? BRD diz apenas concluídas contam — assumir que não há custos parciais persistidos antes de concluir.

## 10. Related Artifacts
* **User Stories:** US-ORD-05 (Cancelar ordem)
* **User Flow:** `user-flows.md#cancelar-ordem`
* **API envolvida:** `api-specification.md#ordens-patch-cancelar`


# Use Case Specification: UC-07 — Consultar Ordens de Manutenção (Listar e Buscar por ID)

## 1. Characterization
* **Primary Actor:** Gestor de Manutenção, Técnico de Campo, Aprovador/Supervisor
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Todos — visibilidade de status, filtros, busca rápida
  - Gestor — relatórios e KPIs
* **Trigger:** Usuário acessa módulo de ordens (lista) ou clica em link/notificação de ordem específica
* **Preconditions:** 
  - Usuário autenticado
  - Backend disponível
* **Postconditions (Success Guarantee):** Lista paginada/filtrada exibida; ou detalhe completo da ordem carregado
* **Scope:** Frontend Aegis1 — listagem e detalhe de ordens
* **Level:** User goal

## 2. Main Scenario (Happy Path) — Listar com Filtros
1. Usuário acessa "Ordens de Manutenção"
2. Frontend carrega primeira página: `GET /api/ordens?page=1&size=20` via `request`
3. Backend retorna lista paginada + metadados (total, page, size)
4. Frontend renderiza tabela/cards com colunas: ID, Ativo, Estado, Prioridade, Técnico, Datas
5. Usuário aplica filtros (estado, prioridade, técnico, filial, período) → frontend adiciona query params e recarrega
6. Usuário pagina / ordena → novas requisições
7. Caso de uso encerra (usuário sai da tela)

## 3. Alternative Scenarios
### AS-1: Buscar Por ID (Acesso Direto)
* **Ponto de extensão:** Usuário cola URL `/ordens/123` ou clica notificação
* **Passos:**
  1. Frontend chama `GET /api/ordens/123` (`buscarPorId`)
  2. Backend retorna ordem completa com evidências, custos, histórico de transições
  3. Frontend exibe tela de detalhe com ações conforme estado/permissão
* **Retorno ao fluxo principal:** Não, fluxo independente

### AS-2: Exportar Lista (Futuro)
* **Ponto de extensão:** Passo 5 — botão "Exportar CSV"
* **Passos:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: relatórios avançados são Out-of-Scope (BRD). Não implementar no MVP.

## 4. Exception Scenarios
### EX-1: Ordem Não Encontrada (404)
* **Ponto de extensão:** AS-1 Passo 2
* **Condição de erro:** ID inexistente ou sem permissão de visualização
* **Tratamento:** `handleApiError` processa 404, frontend exibe página "Ordem não encontrada" com link "Voltar à lista"

### EX-2: Falha de Carregamento Inicial
* **Ponto de extensão:** Passo 2 (listar)
* **Condição de erro:** Rede, 5xx, timeout
* **Tratamento:** `handleApiError` toast, botão "Tentar novamente", estado vazio com skeleton

### EX-3: Filtros Retornam Resultado Vazio
* **Ponto de extensão:** Passo 5
* **Condição de erro:** Backend retorna `data: []`, `total: 0`
* **Tratamento:** Frontend exibe estado vazio ilustrado "Nenhuma ordem encontrada com estes filtros" + botão "Limpar filtros"

## 5. Business Rules Envolvidas
* **BR-06:** `authInterceptor` em todas as chamadas (GET incluídas para autorização)
* **BR-04:** `custoTotalPorAtivo` exibido no detalhe — apenas ordens concluídas somam

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms (listagem e detalhe)
* Paginação: tamanho de página configurável (padrão 20, máx 100)
* Cache: lista não cacheada (dados sensíveis/voláteis); detalhe pode cachear 30s [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 7. Frequency of Use
Muito alta — tela principal de todos os perfis

## 8. Assumptions
* Endpoints: `GET /api/ordens` (paginado, filtros via query) e `GET /api/ordens/{id}`
* Filtros suportados: `estado`, `prioridade`, `tecnicoId`, `filialId`, `departamentoId`, `dataInicio`, `dataFim`
* Ordenação: `sort=campo,direcao`

## 9. Open Issues
* Permissão de visualização: usuário vê apenas suas ordens ou todas da filial? BRD não define — assumir "todas da filial do usuário" por enquanto
* Campos retornados no `listar` vs `buscarPorId` — alinhar com Backend para evitar over-fetching

## 10. Related Artifacts
* **User Stories:** US-ORD-06 (Listar), US-ORD-07 (Buscar por ID)
* **User Flow:** `user-flows.md#listar-ordens`, `user-flows.md#detalhe-ordem`
* **API envolvida:** `api-specification.md#ordens-get`, `api-specification.md#ordens-get-id`


# Use Case Specification: UC-08 — Visualizar Custo Total por Ativo

## 1. Characterization
* **Primary Actor:** Gestor de Manutenção
* **Secondary Actors:** Sistema Backend (API REST)
* **Stakeholders & Interests:** 
  - Gestor — decisão de substituição / plano preventivo baseada em TCO
  - Diretoria/Financeiro — visibilidade de custos por equipamento
* **Trigger:** Gestor acessa dashboard ou tela "Custos por Ativo" ou clica em ativo na ordem
* **Preconditions:** 
  - Pelo menos uma ordem **concluída** vinculada ao ativo (BR-04)
  - Backend calcula e expõe `custoTotalPorAtivo`
* **Postconditions (Success Guarantee):** Valor exibido reflete soma de custos de ordens concluídas do ativo, atualizado em tempo real após cada conclusão
* **Scope:** Frontend Aegis1 — dashboard / tela de ativos / detalhe da ordem
* **Level:** User goal

## 2. Main Scenario (Happy Path)
1. Gestor acessa "Custos por Ativo" (menu lateral) ou dashboard com cards de ativos
2. Frontend carrega lista de ativos com custo: `GET /api/ativos/custos-totais` (ou `GET /api/ativos?include=custoTotal`)
3. Backend retorna array: `[{ ativoId, identificador, descricao, custoTotalPorAtivo, qtdOrdensConcluidas }]`
4. Frontend renderiza tabela/grid ordenável por custo (decrescente padrão)
5. Gestor clica em ativo → abre detalhe com breakdown: lista de ordens concluídas + custos individuais (mão de obra, material, terceiros)
6. Frontend chama `GET /api/ativos/{id}/ordens-concluidas` para breakdown
7. Caso de uso encerra

## 3. Alternative Scenarios
### AS-1: Ver Custo no Detalhe da Ordem
* **Ponto de extensão:** UC-07 detalhe da ordem (estado "Concluída")
* **Passos:**
  1. Tela de detalhe exibe card "Custo Total do Ativo: R$ X.XXX,XX"
  2. Valor vem do campo `custoTotalPorAtivo` no response de `buscarPorId` da ordem
* **Retorno ao fluxo principal:** Sim, mesmo dado, contexto diferente

## 4. Exception Scenarios
### EX-1: Ativo Sem Ordens Concluídas
* **Ponto de extensão:** Passo 3
* **Condição de erro:** Backend retorna `custoTotalPorAtivo: 0` ou `null`
* **Tratamento:** Frontend exibe "R$ 0,00" ou "Sem ordens concluídas" — não é erro, estado válido

### EX-2: Divergência Frontend/Backend (BRD Risk)
* **Ponto de extensão:** Passo 3 ou 6
* **Condição de erro:** Valor exibido não confere com soma manual das ordens
* **Tratamento:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: teste de contrato (Pact) no CI detecta. Frontend deve confiar no backend (BR-04: cálculo no backend). Se divergência, log + alerta para time.

### EX-3: Falha de Carga / Timeout
* **Ponto de extensão:** Passo 2 ou 6
* **Tratamento:** `handleApiError` toast, skeleton loading, botão "Recarregar"

## 5. Business Rules Envolvidas
* **BR-04:** `custoTotalPorAtivo` = soma de custos (mão de obra + material + terceiros) de ordens **concluídas** vinculadas ao ativo — cálculo no backend, frontend apenas exibe
* **BR-06:** `authInterceptor` em chamadas

## 6. Non-Functional Requirements Relevantes
* Latência P95 < 800 ms (dashboard carrega múltiplos ativos)
* Consistência: valor deve ser idêntico em dashboard, tela de ativos e detalhe da ordem (single source of truth = backend)

## 7. Frequency of Use
Média — gestor consulta semanalmente / em reuniões de planejamento

## 8. Assumptions
* Endpoint agregado: `GET /api/ativos/custos-totais` ou `GET /api/ativos` com `include=custoTotal`
* Endpoint breakdown: `GET /api/ativos/{id}/ordens-concluidas`
* Ativo é entidade distinta de ordem — cadastro de ativos não está no glossário do BRD mas é implícito. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: ativos existem no backend; frontend apenas consome.

## 9. Open Issues
* Cadastro de ativos: quem cria? Existe CRUD de ativos no frontend? BRD menciona apenas "departamentos, filiais, fornecedores, funcionários" como cadastros mestres. Ativos podem vir de outro sistema (CMMS/ERP).
* Fórmula exata de custo: horas × rateio + materiais + terceiros — confirmar com Backend/Financeiro

## 10. Related Artifacts
* **User Stories:** US-KPI-01 (Visualizar custo por ativo)
* **User Flow:** `user-flows.md#custo-total-por-ativo`
* **API envolvida:** `api-specification.md#ativos-custos`