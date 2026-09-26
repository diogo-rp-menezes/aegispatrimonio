# Use Case Specification — Aegis1 (Regenerado com AST Java)

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Product / Arquitetura
> **Base:** Glossário (Ubiquitous Language) + BRD v2.0 + NFR v2.0
> **Cobertura:** Frontend (Vue 3) + Backend (Java 21/Spring Boot 3.3) — domínio completo extraído via AST

---

## Sumário de Casos de Uso por Domínio

| Domínio | Casos de Uso | Atores Principais |
| :--- | :--- | :--- |
| **Cadastros Mestres** | UC-01 a UC-04 | Admin Cadastros, Gestor |
| **Ativos & Hardware** | UC-05 a UC-12 | Gestor Patrimônio, Técnico, Admin |
| **Manutenção (Ordens)** | UC-13 a UC-20 | Gestor, Técnico, Aprovador |
| **Manutenção Preventiva** | UC-21 a UC-24 | Gestor, Técnico |
| **Manutenção Preditiva** | UC-25 a UC-28 | Gestor, Sistema (automático) |
| **Busca Inteligente (Fuzzy)** | UC-29 a UC-31 | Todos |
| **Segurança & Aegis Shield** | UC-32 a UC-38 | Admin Global, Auditor, Usuário |
| **Relatórios & QR/PDF** | UC-39 a UC-42 | Gestor, Técnico, Admin |
| **Auditoria & Compliance** | UC-43 a UC-45 | Auditor, Compliance, Admin |

---

## Domínio: Cadastros Mestres

### UC-01 — Gerenciar Filiais (CRUD + Multi-tenancy Root)
* **Ator Primário:** Administrador Global / Admin Cadastros
* **Pré-condições:** Usuário autenticado com role ADMIN; backend disponível
* **Fluxo Principal:**
  1. Admin acessa "Cadastros → Filiais"
  2. Lista paginada com busca fuzzy (nome, CNPJ, código)
  3. **Criar:** Preenche razão social, CNPJ, código, endereço, telefone, email → `POST /api/v1/filiais` → 201
  4. **Editar:** Seleciona filial → `GET /api/v1/filiais/{id}` → altera → `PUT /api/v1/filiais/{id}` → 200
  5. **Excluir:** Confirma modal → `DELETE /api/v1/filiais/{id}` → valida BR-04 (sem ativos/ordens vinculados) → 204
* **Regras:** BR-04, BR-13 (multi-tenancy root), NFR-SEC04
* **API:** `FilialController` + `FilialService` + `FilialRepository`

### UC-02 — Gerenciar Departamentos (CRUD + Hierarquia)
* **Ator Primário:** Administrador de Cadastros (por Filial)
* **Pré-condições:** Filial selecionada no contexto (multi-tenancy); role GESTOR ou ADMIN na filial
* **Fluxo Principal:** Similar a UC-01, endpoint `/api/v1/filiais/{filialId}/departamentos`
* **Regras:** BR-04, BR-13 (isolamento por filial), NFR-SEC04
* **API:** `DepartamentoController` + `DepartamentoService`

### UC-03 — Gerenciar Fornecedores (CRUD + Contratos/SLA)
* **Ator Primário:** Administrador de Cadastros / Gestor
* **Campos Estendidos:** Razão social, CNPJ, contato, email, telefone, endereço, **categoria**, **SLA padrão (horas)**, **avaliação (1-5)**, **certificações**
* **Fluxo Principal:** Endpoint `/api/v1/fornecedores` (scoped por filial via multi-tenancy)
* **Regras:** BR-04, BR-13, NFR-SEC04
* **API:** `FornecedorController` + `FornecedorService`

### UC-04 — Gerenciar Funcionários (CRUD + Vinculação Usuário + Responsabilidades)
* **Ator Primário:** Administrador de Cadastros / RH
* **Campos Estendidos:** Nome, CPF, matrícula, cargo, email, telefone, **filial**, **departamento**, **função manutenção** (TECNICO, APROVADOR, SOLICITANTE), **usuário do sistema** (link 1:1 com `Usuario` para login)
* **Fluxo Principal:** Endpoint `/api/v1/funcionarios`; criação pode provisionar `Usuario` + credenciais iniciais
* **Regras:** BR-13, BR-14 (permissões por função), NFR-SEC04
* **API:** `FuncionarioController` + `FuncionarioService` + `UsuarioService` (provisionamento)

### UC-05 — Gerenciar Tipos de Ativo (Classificação + Depreciação)
* **Ator Primário:** Administrador de Cadastros / Gestor Patrimônio
* **Campos:** Nome, código, categoria (HARDWARE, SOFTWARE, MOBILIARIO, VEICULO, OUTRO), **vida útil padrão (anos)**, **valor residual %**, **requer detalhe hardware** (boolean)
* **Fluxo Principal:** `/api/v1/tipos-ativo`; usado na criação de ativos para depreciação automática
* **Regras:** BR-03 (depreciação linear), NFR-M01
* **API:** `TipoAtivoController` + `TipoAtivoService`

---

## Domínio: Ativos & Hardware

### UC-06 — Cadastrar Ativo Completo (com Detalhe Hardware + QR Code + Termo PDF)
* **Ator Primário:** Gestor Patrimônio / Admin Cadastros
* **Pré-condições:** TipoAtivo, Filial, Departamento, Localização, Fornecedor (opcional), Funcionário responsável pré-cadastrados
* **Fluxo Principal:**
  1. Acessa "Ativos → Novo Ativo"
  2. Seleciona TipoAtivo → se `requerDetalheHardware=true`, exibe seção hardware
  3. Preenche: tag (único), serial, modelo, fabricante, data aquisição, valor, filial, departamento, localização, responsável, fornecedor, nota fiscal
  4. **Se Hardware:** Preenche CPU (modelo, cores, frequência), Memória (total GB, tipo), Discos (array: tipo SSD/HDD, capacidade GB, serial, health SMART), Adaptadores de Rede (array: MAC, IP, velocidade, tipo)
  5. Submete → `POST /api/v1/ativos` → Backend: valida, calcula depreciação (BR-03), gera QR Code (tag + URL pública + hash), persiste `Ativo` + `AtivoDetalheHardware` + componentes
  6. Retorna 201 com ativo completo + URL do QR Code + URL do Termo PDF
  7. Frontend exibe sucesso; opção "Imprimir Termo" (PDF) e "Imprimir Etiqueta QR"
* **Regras:** BR-01, BR-02, BR-03, BR-17, BR-18, NFR-P04
* **API:** `AtivoController.create()` → `AtivoService.createWithHardware()` → `QRCodeGenerator` + `PdfGenerator`

### UC-07 — Consultar Ativo (Detalhe + Hardware + Histórico + Auditoria)
* **Ator Primário:** Técnico, Gestor, Auditor, Aprovador
* **Fluxo Principal:**
  1. Busca por tag/serial/modelo (busca fuzzy UC-29) ou lista paginada
  2. Clica no ativo → `GET /api/v1/ativos/{id}` → retorna ativo + detalhe hardware + componentes + depreciação atual + `custoTotalPorAtivo` + últimas ordens + health check atual
  3. Aba "Auditoria" → `GET /api/v1/ativos/{id}/auditoria` (Envers) → timeline imutável
  4. Aba "QR Code" → visualiza/imprime etiqueta
* **Regras:** BR-13 (multi-tenancy), BR-16 (auditoria), NFR-SEC04
* **API:** `AtivoController.findById()`, `AtivoController.auditoria()`

### UC-08 — Atualizar Ativo (Dados Cadastrais + Transferência + Reavaliação)
* **Ator Primário:** Gestor Patrimônio / Técnico (transferência)
* **Fluxo Principal:**
  1. Edita dados cadastrais → `PUT /api/v1/ativos/{id}` (campos permitidos: localização, responsável, departamento, status)
  2. **Transferência:** Mudança de filial/departamento/localização → gera novo Termo de Responsabilidade PDF (BR-17) → auditoria registra transferência
  3. **Reavaliação:** Atualização de valor/residual → recalcula depreciação futura
* **Regras:** BR-01 (multi-tenancy na transferência), BR-03, BR-17, BR-16
* **API:** `AtivoController.update()`, `AtivoService.transferir()`

### UC-09 — Baixar/Desativar Ativo (Fim de Vida)
* **Ator Primário:** Gestor Patrimônio
* **Fluxo Principal:**
  1. Confirma baixa → `PATCH /api/v1/ativos/{id}/baixar` → status = "BAIXADO", data baixa, motivo
  2. Bloqueia novas ordens; mantém histórico e auditoria
  3. `custoTotalPorAtivo` final consolidado para relatório de TCO
* **Regras:** BR-04 (ordens vinculadas impedem exclusão física), BR-16
* **API:** `AtivoController.baixar()`

### UC-10 — Gerenciar Detalhe de Hardware (CRUD Componentes)
* **Ator Primário:** Técnico (coleta em campo) / Gestor
* **Fluxo Principal:**
  1. Em detalhe do ativo, aba "Hardware"
  2. **Adicionar/Atualizar Disco:** `POST/PUT /api/v1/ativos/{id}/hardware/discos` → campos: tipo, capacidade, serial, health SMART (raw), temperatura, horas ligado
  3. **Adicionar/Atualizar Memória:** `POST/PUT /api/v1/ativos/{id}/hardware/memorias` → capacidade, tipo, frequência
  4. **Adicionar/Atualizar Adaptador Rede:** `POST/PUT /api/v1/ativos/{id}/hardware/adaptadores-rede` → MAC, IP, velocidade, tipo (WiFi/Ethernet)
  5. Coleta em campo: Técnico escaneia QR Code → app PWA → preenche métricas SMART → sincroniza
* **Regras:** BR-02, BR-10 (health check alimenta preditiva), NFR-P03
* **API:** `AtivoDetalheHardwareController` + sub-recursos

### UC-11 — Depreciação & TCO (Custo Total por Ativo)
* **Ator Primário:** Gestor Patrimônio / Financeiro
* **Fluxo Principal:**
  1. Dashboard "Análise de Custos" → `GET /api/v1/relatorios/custo-total-por-ativo` (paginado, filtros: filial, tipo, período)
  2. Retorna: ativo, tag, valor aquisição, valor residual, depreciação acumulada, valor contábil atual, `custoTotalPorAtivo` (soma ordens concluídas), **TCO = valor aquisição + custoTotalPorAtivo - valor residual**
  3. Drill-down: clica no ativo → detalhe de ordens concluídas com custos (mão de obra, material, terceiros)
  4. Exporta CSV/PDF
* **Regras:** BR-08, BR-13, NFR-P01
* **API:** `RelatorioController.custoTotalPorAtivo()`

### UC-12 — Buscar Ativos (Fuzzy + Filtros + Paginação)
* **Ver UC-29** — Busca Inteligente unificada

---

## Domínio: Manutenção (Ordens Corretivas)

### UC-13 — Criar Ordem de Manutenção Corretiva
* **Ator Primário:** Solicitante (Funcionário) / Gestor / Técnico
* **Pré-condições:** Ativo existe; técnico responsável cadastrado como funcionário com função TECNICO
* **Fluxo Principal:**
  1. "Ordens → Nova Ordem" → seleciona "Corretiva"
  2. Preenche: ativo (busca fuzzy), descrição problema, prioridade (BAIXA/MEDIA/ALTA/CRITICA), filial/departamento/localização (herdados do ativo), técnico responsável, tipo serviço, fornecedor (se terceirizado), custo estimado
  3. Submete → `POST /api/v1/ordens` → Backend valida BR-05 (estado "Aberta" + técnico alocado) → persiste `SolicitacaoManutencao` com estado "ABERTA"
  4. Retorna 201 com ordem + notificação (push/email) para técnico responsável
* **Regras:** BR-05, BR-13, BR-14 (técnico só vê suas ordens), NFR-P01
* **API:** `SolicitacaoManutencaoController.createCorretiva()`

### UC-14 — Iniciar Execução da Ordem
* **Ator Primário:** Técnico de Campo (mobile/PWA)
* **Pré-condições:** Ordem estado "ABERTA"; técnico = responsável ou GESTOR da filial
* **Fluxo Principal:**
  1. Técnico abre ordem (lista "Minhas Ordens" ou QR Code do ativo)
  2. Clica "Iniciar" → `PATCH /api/v1/ordens/{id}/iniciar` → Backend valida BR-05 → estado = "EM_ANDAMENTO", timestamp início, usuário executor
  3. Frontend: cronômetro inicia; técnico registra materiais/horas parciais
* **Regras:** BR-05, BR-13, BR-14, NFR-U02 (mobile touch)
* **API:** `SolicitacaoManutencaoController.iniciar()`

### UC-15 — Aprovar Ordem (Validação de Evidências)
* **Ator Primário:** Aprovador / Supervisor / Gestor da filial
* **Pré-condições:** Ordem estado "EM_ANDAMENTO" ou "AGUARDANDO_APROVACAO"; usuário com permissão `ORDEM_APROVAR` na filial
* **Fluxo Principal:**
  1. Aprovador abre ordem → revisa descrição, fotos, checklist, assinatura do técnico
  2. Se conforme: "Aprovar" → `PATCH /api/v1/ordens/{id}/aprovar` → Backend valida BR-06 (evidência obrigatória) → estado = "APROVADA", timestamp, aprovador
  3. Se não conforme: "Rejeitar" → motivo → estado = "EM_ANDAMENTO" (retorna para técnico)
  4. Notificação para técnico e solicitante
* **Regras:** BR-06, BR-13, BR-14, BR-16 (auditoria), NFR-C03 (NR-10/12 checklist)
* **API:** `SolicitacaoManutencaoController.aprovar()`, `SolicitacaoManutencaoController.rejeitar()`

### UC-16 — Concluir Ordem (Fechamento + Custos Finais)
* **Ator Primário:** Técnico (após aprovação) / Gestor
* **Pré-condições:** Ordem estado "APROVADA"
* **Fluxo Principal:**
  1. Técnico/Gestor abre ordem → "Concluir"
  2. Preenche custos finais: mão de obra (horas × rate), materiais (itens + qtd × valor), terceiros (fornecedor + valor), observações finais
  3. Submete → `PATCH /api/v1/ordens/{id}/concluir` → Backend valida BR-07 → estado = "CONCLUIDA", timestamp, custos finais persistidos
  4. **Trigger automático:** Atualiza `custoTotalPorAtivo` do ativo (BR-08); verifica se dispara manutenção preventiva baseada em uso/tempo
  5. Gera Termo de Responsabilidade final (PDF) se transferência de custódia
* **Regras:** BR-07, BR-08, BR-13, BR-16, NFR-P01
* **API:** `SolicitacaoManutencaoController.concluir()` → `AtivoService.atualizarCustoTotal()`

### UC-17 — Cancelar Ordem
* **Ator Primário:** Solicitante / Gestor / Técnico
* **Pré-condições:** Ordem estado ≠ "CONCLUIDA"
* **Fluxo Principal:**
  1. "Cancelar" → motivo obrigatório → `PATCH /api/v1/ordens/{id}/cancelar` → estado = "CANCELADA"
  2. Não afeta `custoTotalPorAtivo` (apenas ordens concluídas contam)
  3. Auditoria registra cancelamento com motivo
* **Regras:** BR-07, BR-16
* **API:** `SolicitacaoManutencaoController.cancelar()`

### UC-18 — Listar/Buscar Ordens (Filtros + Paginação + Multi-tenancy)
* **Ator Primário:** Todos (visibilidade por role/filial)
* **Fluxo Principal:**
  1. "Ordens" → lista paginada server-side
  2. Filtros: estado, prioridade, filial, departamento, técnico, ativo (busca fuzzy), período, tipo (corretiva/preventiva/preditiva)
  3. `GET /api/v1/ordens?page=0&size=20&estado=ABERTA&filialId=1&busca=notebok` → fuzzy no ativo
  4. Ordenação: prioridade (critica primeiro), data criação, SLA
* **Regras:** BR-11, BR-12, BR-13, BR-14, NFR-P01, NFR-S02
* **API:** `SolicitacaoManutencaoController.listar()`

### UC-19 — Dashboard de Ordens (KPIs + Alertas Tempo Real)
* **Ator Primário:** Gestor, Aprovador, Admin
* **Fluxo Principal:**
  1. Dashboard → `GET /api/v1/dashboard/ordens` → cards: abertas, em andamento, aguardando aprovação, vencendo SLA (24h), concluídas mês, custo total mês
  2. Gráficos: tendência 30 dias, por prioridade, por filial, por tipo ativo
  3. Alertas: ordens > 24h sem aprovação, ordens > SLA, health check crítico (preditiva)
  4. WebSocket / SSE para atualizações tempo real (ordem iniciada, aprovada, concluída)
* **Regras:** BR-13, BR-14, NFR-O03, NFR-O04
* **API:** `DashboardController.ordens()` + WebSocket endpoint

### UC-20 — SLA & Escalation (Automático)
* **Ator Primário:** Sistema (Scheduler) → Notifica Aprovador/Gestor
* **Fluxo Principal:**
  1. Job diário (Spring Scheduler) verifica ordens "ABERTA"/"EM_ANDAMENTO"/"AGUARDANDO_APROVACAO" > 24h
  2. Envia notificação (email/push) para aprovador + gestor da filial
  3. Após 48h: escala para ADMIN global
  4. Registra evento de auditoria (SLA breach)
* **Regras:** BR-06, NFR-O04
* **API:** `SlaSchedulerService` + `NotificationService`

---

## Domínio: Manutenção Preventiva

### UC-21 — Cadastrar Plano de Manutenção Preventiva
* **Ator Primário:** Gestor de Manutenção
* **Pré-condições:** Ativo cadastrado; técnico padrão opcional; checklist template (futuro)
* **Fluxo Principal:**
  1. "Preventiva → Novo Plano" → seleciona ativo(s) ou tipo de ativo + filial
  2. Define: frequência (CRON: diário, semanal, mensal, trimestral, anual), dia/hora preferencial, técnico padrão, descrição padrão, checklist (futuro)
  3. Submete → `POST /api/v1/preventivas` → persiste `ManutencaoPreventiva` com próximo agendamento calculado
* **Regras:** BR-09, BR-13, BR-14
* **API:** `ManutencaoPreventivaController.create()`

### UC-22 — Geração Automática de Ordens Preventivas (Scheduler)
* **Ator Primário:** Sistema (Spring Scheduler @Scheduled)
* **Fluxo Principal:**
  1. Job a cada 15 min verifica `ManutencaoPreventiva` com `proximaExecucao <= now`
  2. Para cada: cria `SolicitacaoManutencao` tipo "PREVENTIVA" com dados do plano, estado "ABERTA", técnico padrão
  3. Atualiza `proximaExecucao` (próximo CRON)
  4. Notifica técnico responsável
  5. Log de auditoria: geração automática
* **Regras:** BR-09, BR-16, NFR-A05
* **API:** `ManutencaoPreventivaSchedulerService`

### UC-23 — Gerenciar Planos Preventivos (Editar, Pausar, Excluir)
* **Ator Primário:** Gestor de Manutenção
* **Fluxo Principal:** CRUD padrão em `/api/v1/preventivas/{id}`; pausa = `ativo=false`; exclusão bloqueada se houver ordens geradas (BR-04 adaptado)

### UC-24 — Relatório de Aderência Preventiva
* **Ator Primário:** Gestor / Compliance
* **Fluxo Principal:**
  1. `GET /api/v1/relatorios/preventiva-aderencia` → % ordens preventivas concluídas no prazo / total geradas
  2. Filtros: filial, período, tipo ativo
  3. Detalha: plano, ordens geradas, concluídas no prazo, atrasadas, canceladas
  4. Exporta PDF/CSV para evidência NR-10/12

---

## Domínio: Manutenção Preditiva (Health Check + Regressão Linear)

### UC-25 — Health Check de Ativo (Coleta + Análise SMART)
* **Ator Primário:** Técnico (coleta) / Sistema (agendado) / `ManutencaoPreditivaService`
* **Fluxo Principal:**
  1. **Coleta manual:** Técnico escaneia QR Code → PWA → lê SMART (smartctl / WMI) → preenche: reallocated_sectors, seek_error_rate, spin_retry_count, temperature, power_on_hours → `POST /api/v1/ativos/{id}/health-check`
  2. **Coleta automática (agente):** Serviço em background no ativo (futuro) → envia métricas periódicas
  3. Backend: `HealthCheckService.analisar()` → calcula score 0-100 baseado em thresholds configuráveis por tipo de disco
  4. Persiste `HealthCheck` (histórico) + atualiza `AtivoDetalheHardware.discos[].healthScore`
  5. Se score < threshold (ex.: 60) → gera alerta + UC-27
* **Regras:** BR-10, NFR-P03, NFR-O04
* **API:** `HealthCheckController.coletar()`, `HealthCheckService.analisar()`

### UC-26 — Previsão de Falha de Disco (Regressão Linear - Mínimos Quadrados)
* **Ator Primário:** Sistema (`ManutencaoPreditivaService` - batch noturno)
* **Fluxo Principal:**
  1. Job noturno (02:00) → para cada ativo com ≥ 3 health checks históricos:
  2. `ManutencaoPreditivaService.preverFalhaDisco(ativoId)` → regressão linear simples (y = ax + b) sobre `reallocated_sectors` vs `power_on_hours` (ou temperatura vs tempo)
  3. Calcula: **dias até threshold crítico** (ex.: reallocated_sectors > 100) + **intervalo de confiança 95%**
  4. Persiste `PrevisaoFalha` (ativo, disco, dataPrevista, probabilidade, icInferior, icSuperior, modeloUsado)
  5. Se probabilidade > 80% E dataPrevista < 30 dias → UC-27
* **Regras:** BR-10, NFR-P03, NFR-S03
* **API:** `ManutencaoPreditivaService.preverFalhaDisco()`, `PrevisaoFalhaRepository`

### UC-27 — Geração Automática de Ordem Preditiva
* **Ator Primário:** Sistema (triggered by UC-26 ou UC-25)
* **Fluxo Principal:**
  1. Condição: previsão falha disco > 80% em < 30 dias OU health check score < 40 (crítico imediato)
  2. Cria `SolicitacaoManutencao` tipo "PREDITIVA" → descrição: "Falha de disco prevista para {data} (probabilidade {X}%). Substituir disco preventivamente."
  3. Prioridade = ALTA/CRITICA; técnico = responsável do ativo ou padrão da filial
  4. Alerta no dashboard (UC-19) + notificação push/email
  5. Auditoria: origem = "PREDITIVA_AUTO"
* **Regras:** BR-10, BR-16, NFR-O04
* **API:** `ManutencaoPreditivaService.gerarOrdemPreditiva()`

### UC-28 — Dashboard Preditivo & Gestão de Riscos
* **Ator Primário:** Gestor Patrimônio / Gestor Manutenção / Admin
* **Fluxo Principal:**
  1. "Preditiva → Dashboard" → `GET /api/v1/dashboard/preditiva`
  2. Cards: ativos monitorados, alertas ativos, ordens preditivas abertas, previsões < 30 dias
  3. Tabela: ativo, tag, disco, health score, probabilidade falha, data prevista, IC 95%, ação (ver ordem, agendar substituição)
  4. Filtros: filial, probabilidade, horizonte (7/30/90 dias)
  5. Ação: "Agendar Substituição" → cria ordem preventiva programada + marca previsão como "tratada"
* **Regras:** BR-13, BR-14, NFR-P01, NFR-O03
* **API:** `DashboardController.preditiva()`

---

## Domínio: Busca Inteligente (Fuzzy Search - Levenshtein)

### UC-29 — Busca Global Unificada (Fuzzy + Filtros)
* **Ator Primário:** Todos os usuários
* **Fluxo Principal:**
  1. Header: campo de busca global (placeholder: "Buscar ativo, ordem, funcionário, fornecedor...")
  2. Digita termo (ex.: "notebok", "dell 5520", "joao silva") → debounce 300ms
  3. `GET /api/v1/busca?q=notebok&types=ativo,ordem,funcionario,fornecedor&filialId=1&page=0&size=10`
  4. Backend: `FuzzySearchService.buscar()` → Levenshtein distance ≤ 2 (configurável) nos campos indexados
  5. Combina: score fuzzy + boost por tipo (ativo > ordem > funcionário > fornecedor) + filtros exatos
  6. Retorna resultados unificados paginados com highlight do termo
* **Regras:** BR-11, BR-12, BR-13, NFR-P02
* **API:** `BuscaController.global()`, `FuzzySearchService`, `LevenshteinDistance`

### UC-30 — Busca Fuzzy Específica por Entidade
* **Ator Primário:** Usuário em tela específica (ex.: seleção de ativo em ordem)
* **Fluxo Principal:** Modal de seleção → campo busca → `GET /api/v1/busca/ativos?q=...&filialId=...` → retorna apenas ativos com score fuzzy
* **Regras:** BR-11, BR-12, BR-13

### UC-31 — Configuração de Threshold Fuzzy (Admin)
* **Ator Primário:** Admin Global
* **Fluxo Principal:** Configuração → "Busca" → ajusta similaridade mínima (0.5–0.9, default 0.7), campos indexados por entidade, pesos de boost
* **Persiste:** `application.properties` / banco (tabela config) → reload runtime via `@RefreshScope`

---

## Domínio: Segurança & Aegis Shield (RBAC Granular + Multi-tenancy)

### UC-32 — Login & Autenticação (JWT + Refresh)
* **Ator Primário:** Qualquer usuário
* **Fluxo Principal:**
  1. Tela login → email + senha → `POST /api/v1/auth/login`
  2. Backend: `AuthenticationManager` → valida → `JwtTokenProvider.generateToken(usuario, roles, filialId)` → access token (15 min) + refresh token (7 dias, HttpOnly cookie)
  3. Frontend: `authInterceptor` armazena access token (memória) + refresh cookie; anexa `Authorization: Bearer <token>` em todas requisições
  4. Expiração access token → `authInterceptor` detecta 401 → `POST /api/v1/auth/refresh` (cookie) → novo access token → retry original request (1x)
  5. Falha refresh → limpa armazenamento → redirect login
* **Regras:** BR-15, NFR-SEC02, NFR-SEC03
* **API:** `AuthController.login()`, `AuthController.refresh()`, `JwtTokenProvider`, `SecurityConfig`

### UC-33 — Gerenciar Roles & Permissions (Matriz Aegis Shield)
* **Ator Primário:** Admin Global
* **Fluxo Principal:**
  1. "Admin → Roles & Permissions" → matriz visual: Roles (linhas) × Permissions (colunas) × Contexto (Filial/Global)
  2. **Roles padrão:** ADMIN (global), GESTOR (filial), TECNICO (próprio), USER (leitura), AUDITOR (leitura + auditoria)
  3. **Permissions granulares:** `ATIVO_CRIAR`, `ATIVO_LER`, `ATIVO_ATUALIZAR`, `ATIVO_EXCLUIR`, `ORDEM_CRIAR`, `ORDEM_INICIAR`, `ORDEM_APROVAR`, `ORDEM_CONCLUIR`, `ORDEM_CANCELAR`, `ORDEM_LER_TODAS`, `RELATORIO_GERAR`, `CONFIG_GERENCIAR`, `AUDITORIA_LER`
  4. **Contexto:** Global (ADMIN) ou Filial (GESTOR, TECNICO, USER, AUDITOR)
  5. Edita matriz → `PUT /api/v1/admin/roles/{role}/permissions` → valida consistência (ex.: TECNICO não pode ter `ORDEM_APROVAR`)
* **Regras:** BR-13, BR-14, NFR-SEC03, NFR-SEC04
* **API:** `RoleController`, `PermissionController`, `AegisShieldPermissionEvaluator`

### UC-34 — Gerenciar Usuários (Provisionamento + Roles + Filial)
* **Ator Primário:** Admin Global / Admin Cadastros (por filial)
* **Fluxo Principal:**
  1. "Admin → Usuários" → lista paginada (multi-tenancy: admin global vê todos; admin filial vê só sua filial)
  2. **Criar:** Email, nome, senha temporária, role(s), filial(es) → `POST /api/v1/usuarios` → provisiona `Usuario` + `Funcionario` (opcional) → envia email boas-vindas com link primeiro acesso
  3. **Editar:** Roles, filial, status (ativo/inativo), reset senha
  4. **Excluir/Inativar:** Soft delete (status INATIVO) — mantém auditoria
* **Regras:** BR-13, BR-14, BR-15, BR-16, NFR-SEC04
* **API:** `UsuarioController`, `UsuarioService`

### UC-35 — Multi-tenancy: Troca de Contexto de Filial (Admin Global)
* **Ator Primário:** Admin Global
* **Fluxo Principal:**
  1. Header: seletor de filial (apenas ADMIN global vê todas)
  2. Troca filial → `MultiTenancyFilter` define `tenantId` no Hibernate Filter → todas queries subsequentes filtradas
  3. UI reflete dados da filial selecionada
* **Regras:** BR-13, NFR-SEC04, NFR-S03
* **API:** `MultiTenancyFilter` + `TenantContextHolder`

### UC-36 — Auditoria de Acessos & Alterações (Envers)
* **Ator Primário:** Auditor / Compliance / Admin Global
* **Fluxo Principal:**
  1. "Auditoria" → `GET /api/v1/auditoria?entidade=Ativo&id=123&page=0&size=50`
  2. Retorna timeline: revisão, timestamp, usuário, ação (CREATE/UPDATE/DELETE), diff campo a campo (antes/depois), IP, user-agent
  3. Filtros: entidade, ID, usuário, período, ação
  4. Exporta PDF/CSV (assinado digitalmente para evidência legal)
* **Regras:** BR-16, NFR-C02, NFR-C04, NFR-C05
* **API:** `AuditoriaController`, `EnversRevisionRepository`

### UC-37 — LGPD: Direito ao Esquecimento / Portabilidade
* **Ator Primário:** Titular dos dados (Usuário) / DPO
* **Fluxo Principal:**
  1. Usuário logado → "Minha Conta → Privacidade" → "Solicitar Exclusão dos Meus Dados"
  2. `POST /api/v1/usuarios/me/solicitar-exclusao` → cria tarefa para DPO (workflow futuro) OU executa imediato se automatizado:
     - Anonimiza dados pessoais em `Usuario` (nome → "USUARIO_ANONIMIZADO_{hash}", email → hash@anonymized.local)
     - Mantém `Auditoria` (Envers) com hash do usuário original (rastreabilidade legal)
     - Revoga tokens, invalida sessões
  3. "Exportar Meus Dados" → `GET /api/v1/usuarios/me/exportar` → JSON com todos dados do titular (ordens, ativos responsáveis, health checks, auditoria)
* **Regras:** NFR-C01, NFR-C02, BR-16
* **API:** `UsuarioController.solicitarExclusao()`, `UsuarioController.exportar()`

### UC-38 — Teste de Penetração & Validação de Segurança (Contínuo)
* **Ator Primário:** Segurança da Informação / Pipeline CI
* **Fluxo Principal:**
  1. CI: OWASP Dependency Check + SpotBugs + Checkstyle + Semgrep
  2. Staging: DAST (OWASP ZAP) + SAST (SonarQube/CodeQL)
  3. Pré-prod: Pen test automatizado (auth bypass, IDOR, multi-tenancy leak, injection)
  4. Falha crítica/alta = block deploy
* **Regras:** NFR-SEC10, NFR-M03

---

## Domínio: Relatórios & QR Code / PDF

### UC-39 — Gerar Termo de Responsabilidade (PDF Assinado)
* **Ator Primário:** Gestor / Técnico (no ato de alocação/transferência)
* **Fluxo Principal:**
  1. Em ativo (UC-06, UC-08 transferência) → "Gerar Termo"
  2. `POST /api/v1/relatorios/termo-responsabilidade` → payload: ativoId, responsavelId, tipo (ALOCACAO/TRANSFERENCIA/BAIXA), observações
  3. Backend: `PdfGenerator.gerarTermo()` → template Thymeleaf/Flying Saucer → PDF com: dados ativo, responsável, filial, data, QR Code de verificação, hash integridade
  4. **Assinatura digital:** Integração ICP-Brasil (futuro) ou assinatura eletrônica avançada (gov.br / certificado A1) — placeholder v1: campo assinatura manual + testemunha
  5. Retorna PDF (base64 ou stream) → frontend abre em nova aba / download
  6. Auditoria: geração de termo registrada
* **Regras:** BR-17, BR-16, NFR-P04, NFR-C03
* **API:** `RelatorioController.gerarTermoResponsabilidade()`, `PdfGenerator`

### UC-40 — Gerar Etiquetas QR Code (Unitário + Lote)
* **Ator Primário:** Gestor / Técnico (inventário)
* **Fluxo Principal:**
  1. **Unitário:** Em ativo → "Imprimir Etiqueta" → `GET /api/v1/ativos/{id}/qr-code` → retorna SVG/PNG
  2. **Lote:** "Relatórios → Etiquetas em Lote" → seleciona filtro (filial, tipo, status) → `POST /api/v1/relatorios/etiquetas-lote` → `QRCodeGenerator.gerarLote()` → ZIP com SVGs ou PDF pronto para impressão (A4, 24 etiquetas/folha)
  3. QR Code contém: `https://aegis1.com/public/ativo/{tag}?h={hash}` → página pública read-only (dados básicos + status + termo)
* **Regras:** BR-18, NFR-P04
* **API:** `RelatorioController.gerarEtiquetasLote()`, `QRCodeGenerator`

### UC-41 — Dashboard Analytics (Drill-down + Alertas Tempo Real)
* **Ver UC-19** — já coberto

### UC-42 — Relatórios de Compliance (NR-10/12, LGPD, ISO 27001)
* **Ator Primário:** Compliance / Auditor / Gestor
* **Fluxo Principal:**
  1. "Relatórios → Compliance" → seleciona tipo: NR-10/12 (ordens com checklist), LGPD (solicitações exclusão/exportação), ISO 27001 (acessos, auditoria, incidentes)
  2. `GET /api/v1/relatorios/compliance?tipo=NR12&periodo=2025-01` → PDF estruturado com evidências
  3. Assinatura digital do responsável pelo relatório
* **Regras:** NFR-C03, NFR-C04, NFR-C05, BR-16
* **API:** `RelatorioController.compliance()`

---

## Domínio: Auditoria & Compliance

### UC-43 — Consultar Trilha de Auditoria Completa (Envers)
* **Ver UC-36** — já coberto

### UC-44 — Relatório de Acessos Negados / Tentativas de Vazamento (Multi-tenancy)
* **Ator Primário:** Auditor / Segurança / Admin Global
* **Fluxo Principal:**
  1. `GET /api/v1/auditoria/acessos-negados?periodo=24h` → logs de `AccessDeniedException` + tentativas cross-tenant (MultiTenancyFilter block)
  2. Alertas automáticos se > 10/min (NFR-O04)
  3. Integração SIEM (Syslog/Elastic) para correlação
* **Regras:** NFR-SEC04, NFR-O04, NFR-C04

### UC-45 — Validação de Integridade de Dados (Checksums + Auditoria)
* **Ator Primário:** Sistema (batch semanal) / Auditor
* **Fluxo Principal:**
  1. Job semanal calcula checksums (SHA-256) de dados críticos: ativos, ordens, usuários, auditoria
  2. Compara com baseline armazenado em tabela `integridade_checksum`
  3. Divergência → alerta crítico + incidente de segurança
  4. Relatório de integridade assinado digitalmente
* **Regras:** BR-16, NFR-C04, NFR-C05

---

## Rastreabilidade Use Cases ↔ Artefatos

| UC | BRD Regras | NFR | API Spec | State Machine | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| UC-01 a UC-05 | BR-04, BR-13, BR-14 | NFR-SEC04, NFR-S03 | `/api/v1/filiais`, `/departamentos`, `/fornecedores`, `/funcionarios`, `/tipos-ativo` | — | Integration: *ControllerIT |
| UC-06 a UC-12 | BR-01,02,03,08,17,18 | NFR-P01,03,04, NFR-SEC04 | `/api/v1/ativos`, `/hardware`, `/relatorios/custo-total` | `state-machines.md#ativo` | Integration: AtivoControllerIT, HardwareControllerIT |
| UC-13 a UC-20 | BR-05,06,07,08,13,14 | NFR-P01, NFR-S02, NFR-SEC04, NFR-O03,04 | `/api/v1/ordens`, `/dashboard/ordens` | `state-machines.md#solicitacaomanutencao` | Integration: OrdemControllerIT, SlaSchedulerIT |
| UC-21 a UC-24 | BR-09, BR-16 | NFR-A05, NFR-S02 | `/api/v1/preventivas`, `/relatorios/preventiva-aderencia` | `state-machines.md#manutencaopreventiva` | Integration: PreventivaSchedulerIT |
| UC-25 a UC-28 | BR-10, BR-16 | NFR-P03, NFR-O04 | `/api/v1/health-check`, `/dashboard/preditiva`, `/previsao-falha` | `state-machines.md#healthcheck` | Unit: ManutencaoPreditivaServiceTest, HealthCheckServiceTest |
| UC-29 a UC-31 | BR-11, BR-12, BR-13 | NFR-P02, NFR-S03 | `/api/v1/busca` | — | Unit: FuzzySearchServiceTest, LevenshteinDistanceTest; Integration: BuscaControllerIT |
| UC-32 a UC-38 | BR-13,14,15,16 | NFR-SEC01-10, NFR-C01-05 | `/api/v1/auth`, `/admin/roles`, `/admin/usuarios`, `/auditoria` | `state-machines.md#usuario` | Integration: SecurityConfigIT, AegisShieldTest, MultiTenancyIT |
| UC-39 a UC-42 | BR-17,18, BR-16 | NFR-P04, NFR-C03 | `/api/v1/relatorios/termo`, `/qr-code`, `/etiquetas-lote`, `/compliance` | — | Integration: RelatorioControllerIT, QRCodeGeneratorTest, PdfGeneratorTest |
| UC-43 a UC-45 | BR-16 | NFR-C02,04,05, NFR-O04 | `/api/v1/auditoria` | — | Integration: AuditoriaControllerIT, IntegridadeChecksumIT |

---

*Documento regenerado com base em análise AST completa do backend Java (domain, service, controller, security, predictive, search, audit, report) + frontend Vue. Substitui versão 1.0 que continha apenas visão frontend.*