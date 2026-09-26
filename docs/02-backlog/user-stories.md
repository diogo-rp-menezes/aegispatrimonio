# User Stories Specification — Aegis1 (Regenerado com AST Java)

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Product / Tech Leads
> **Base:** Story Map v2.0 + Use Cases v2.0 + User Flows v2.0 + BRD v2.0 + NFR v2.0
> **Formato:** INVEST + BDD/Gherkin + Definition of Ready/Done + Rastreabilidade
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/PWA) + Infra
> **Organização:** Por Épico (Domínio) → Slice (MVP/Should/Could) → Story ID

---

## Sumário de Épicos e Stories por Slice

| Épico | MVP (Must) | Should | Could | Total |
| :--- | :--- | :--- | :--- | :--- |
| **Cadastros Mestres** | 25 | — | — | 25 |
| **Ativos & Hardware** | 30 | — | 8 | 38 |
| **Ordens Corretivas** | 28 | — | 6 | 34 |
| **Manutenção Preventiva** | — | 16 | — | 16 |
| **Manutenção Preditiva** | — | 22 | — | 22 |
| **Busca Inteligente (Fuzzy)** | 8 | — | 4 | 12 |
| **Segurança Aegis Shield** | 20 | 16 | 4 | 40 |
| **Relatórios & QR/PDF** | 6 | 8 | — | 14 |
| **LGPD & Compliance** | — | 6 | 4 | 10 |
| **Observabilidade & Qualidade** | 12 | 6 | 2 | 20 |
| **Infra & DevOps** | 8 | — | 2 | 10 |
| **TOTAL** | **137** | **74** | **30** | **241** |

---

## Épico 1: Cadastros Mestres (MVP — 25 Stories)

### US-CAD-001 — Criar Filial (Admin Global)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:**
- **Given** Admin Global autenticado acessa "Novo Filial"
- **When** Preenche razão social, CNPJ (válido), código, endereço, telefone, email → Salvar
- **Then** POST /api/v1/filiais → 201 + filial criada; Toast sucesso; Lista atualizada
- **And** Validação: CNPJ único (409 se duplicado); Campos obrigatórios (frontend)
- **And** Auditoria: CREATE registrado (Envers)

### US-CAD-002 — Listar/Buscar Filiais (Admin Global)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** Paginação server-side; Busca fuzzy (Levenshtein) por razão social/CNPJ/código; Filtros ativos/inativos

### US-CAD-003 — Editar Filial (Admin Global)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** GET /filiais/{id} → Modal pré-preenchido → PUT → 200; Não permite alterar CNPJ; Auditoria UPDATE

### US-CAD-004 — Excluir Filial (Admin Global) — Bloqueio se Vínculos
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** DELETE /filiais/{id} → 204 se sem vínculos; 409 "Possui departamentos/ativos/ordens vinculados" se houver; Auditoria DELETE

### US-CAD-005 — Trocar Contexto Filial (Admin Global Header)
**Priority:** Must | **Complexity:** 2 | **Slice:** MVP
**BDD:** Seletor filial no header (só Admin Global); Troca → MultiTenancyFilter define tenantId; UI reflete dados da filial; Auditoria troca contexto

### US-CAD-006 — Criar Departamento (Scoped Filial)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** POST /api/v1/filiais/{filialId}/departamentos; Código único por filial; Scoped multi-tenancy

### US-CAD-007 — Listar/Buscar Departamentos (Scoped Filial)
**Priority:** Must | **Complexity:** 2 | **Slice:** MVP
**BDD:** Filtros: ativo/inativo; Busca fuzzy nome/código; Paginação

### US-CAD-008 — Editar Departamento
**Priority:** Must | **Complexity:** 2 | **Slice:** MVP

### US-CAD-009 — Excluir Departamento (Bloqueio se Vínculos)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** 409 se houver ativos/ordens vinculados

### US-CAD-010 — Criar Fornecedor (Categoria, SLA, Avaliação)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Campos: razão social, CNPJ, contato, email, telefone, endereço, **categoria**, **SLA padrão (horas)**, **avaliação 1-5**, **certificações**; Scoped filial

### US-CAD-011 — Listar/Buscar/Editar/Excluir Fornecedor
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP (4 stories: listar, buscar, editar, excluir)

### US-CAD-015 — Criar Funcionário + Provisionar Usuario
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** Nome, CPF, matrícula, cargo, email, telefone, filial, departamento, **função manutenção** (TECNICO/APROVADOR/SOLICITANTE); Cria `Usuario` vinculado + credenciais iniciais; Email boas-vindas

### US-CAD-016 — Listar/Buscar/Editar/Excluir Funcionário
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP (4 stories)

### US-CAD-020 — Criar Tipo de Ativo (Vida Útil, Valor Residual, Requer Hardware)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** Nome, código, categoria (HARDWARE/SOFTWARE/MOBILIARIO/VEICULO/OUTRO), **vida útil padrão (anos)**, **valor residual %**, **requerDetalheHardware** (boolean)

### US-CAD-021 — Listar/Buscar/Editar/Excluir Tipo de Ativo
**Priority:** Must | **Complexity:** 4 | **Slice:** MVP (4 stories)

---

## Épico 2: Ativos & Hardware (MVP — 30 Stories + Could — 8)

### US-ASV-001 — Cadastrar Ativo Básico (Tag, Serial, Modelo, Fabricante, Aquisição, Valor, Filial, Depto, Local, Responsável, Fornecedor, NF)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:**
- **Given** Gestor Patrimônio autenticado (permissão ATIVO_CRIAR na filial) acessa "Novo Ativo"
- **When** Seleciona TipoAtivo → Preenche campos obrigatórios → Salvar
- **Then** POST /api/v1/ativos → 201; Backend calcula depreciação linear (BR-03: data aquisição + vida útil TipoAtivo + valor residual); Retorna ativo com depreciação atual
- **And** Validação: Tag único global; Filial/Depto/Local consistentes (mesma filial); Responsável pertence à filial

### US-ASV-002 — Cadastrar Ativo com Detalhe Hardware (CPU, Memória, Discos, Rede)
**Priority:** Must | **Complexity:** 13 | **Slice:** MVP
**BDD:**
- **Given** TipoAtivo.requerDetalheHardware = true
- **When** Preenche seção hardware: CPU (modelo, cores, frequência), Memória (total GB, tipo, frequência), **Discos** (array: +Adicionar → tipo SSD/HDD, capacidade GB, serial, SMART raw, temperatura, horas ligado), **Adaptadores Rede** (array: +Adicionar → MAC, IP, velocidade, tipo WiFi/Ethernet)
- **Then** Persiste `AtivoDetalheHardware` + componentes vinculados; Validação BR-02 (hardware obrigatório)

### US-ASV-003 — Gerar QR Code Unitário + Termo Responsabilidade PDF
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:**
- **Given** Ativo criado (US-ASV-001/002)
- **When** Clica "Gerar QR Code" / "Gerar Termo"
- **Then** QR Code: SVG/PNG com `https://aegis1.com/public/ativo/{tag}?h={hash}` (hash SHA-256 tag+timestamp); Termo PDF: template Thymeleaf/FlyingSaucer com dados ativo + responsável + filial + data + QR Code + hash integridade; Download/Impressão

### US-ASV-004 — Visualizar/Imprimir QR Code + Termo PDF
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-ASV-005 — Transferir Ativo (Filial/Depto/Local/Responsável) + Novo Termo
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** PUT /ativos/{id}/transferir → Valida multi-tenancy (filial destino); Gera novo Termo PDF (BR-17); Auditoria: transferência registrada (filialOrigem, filialDestino, responsavelAnterior, responsavelNovo)

### US-ASV-006 — Baixar/Desativar Ativo (Fim de Vida) + TCO Final
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** PATCH /ativos/{id}/baixar → Status BAIXADO + data + motivo; Bloqueia novas ordens; `custoTotalPorAtivo` final consolidado para relatório TCO

### US-ASV-007 — Consultar Ativo (Detalhe + Hardware + Histórico Ordens + Auditoria + Health Check)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** GET /ativos/{id} → Retorna ativo + detalhe hardware + componentes + depreciação atual + custoTotalPorAtivo + últimas 10 ordens + health check atual; Aba Auditoria → GET /ativos/{id}/auditoria (Envers timeline + diff)

### US-ASV-008 — Editar Ativo (Dados Cadastrais Permitidos)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** Campos editáveis: localização, responsável, departamento, status; Não edita: tag, serial, data aquisição, valor original

### US-ASV-009 — Listar/Buscar Ativos (Filtros + Paginação + Fuzzy)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Filtros: filial, tipo, status, localização, responsável; Busca fuzzy tag/serial/modelo/fabricante; Paginação server-side; Ordenação: tag, data aquisição, valor

### US-ASV-010 — Gerenciar Discos do Ativo (CRUD + SMART)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** POST/PUT/DELETE /ativos/{id}/hardware/discos; Campos: tipo, capacidade, serial, smartRaw, temperatura, horasLigado; Alimenta Health Check (UC-25)

### US-ASV-011 — Gerenciar Memórias do Ativo (CRUD)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-ASV-012 — Gerenciar Adaptadores de Rede do Ativo (CRUD)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-ASV-013 — Dashboard CustoTotalPorAtivo / TCO (Drill-down + Export)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** GET /relatorios/custo-total-por-ativo?filial=&tipo=&periodo= → Lista: ativo, tag, valorAquisicao, valorResidual, depreciacaoAcumulada, valorContabil, custoTotalPorAtivo, **TCO = valorAquisicao + custoTotalPorAtivo - valorResidual**; Drill-down: clica ativo → ordens concluídas com custos (mão de obra, material, terceiros); Export CSV/PDF

### US-ASV-014 — Depreciação Automática (Batch Mensal + Recalculo Reavaliação)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Job mensal recalcula depreciação acumulada; US-ASV-015 (Reavaliação) dispara recálculo futuro

### US-ASV-015 — Reavaliar Ativo (Valor/Residual) + Depreciação Recalculada
**Priority:** Could | **Complexity:** 5 | **Slice:** Could
**BDD:** PUT /ativos/{id}/reavaliar → Novo valor/residual → Recalcula depreciação futura; Auditoria diff valor

### US-ASV-016 — Importar Ativos em Lote (CSV/Excel + Validação Linha a Linha + Relatório Erros)
**Priority:** Could | **Complexity:** 13 | **Slice:** Could
**BDD:** Template download; Upload → Validação transacional (tudo ou nada); Relatório erros por linha; Auditoria importação

### US-ASV-017 — Etiquetas QR Code em Lote (Filtros + PDF A4 24/folha)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** POST /relatorios/etiquetas-lote {filtros} → PDF pronto impressão; QR Code URL pública read-only

### US-ASV-018 — Página Pública Read-Only Ativo (QR Code Scan)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** GET /public/ativo/{tag}?h={hash} → Valida hash → Exibe: tag, serial, modelo, fabricante, filial, localização, status, último health check, termo responsabilidade link

---

## Épico 3: Ordens Corretivas (MVP — 28 Stories + Could — 6)

### US-ORD-001 — Criar Ordem Corretiva (Busca Fuzzy Ativo + Validação BR-05)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:**
- **Given** Gestor/Técnico autenticado (ORDEM_CRIAR na filial) acessa "Nova Ordem" → Seleciona "Corretiva"
- **When** Preenche: Ativo (busca fuzzy Levenshtein), Descrição problema, Prioridade (BAIXA/MEDIA/ALTA/CRITICA), Técnico responsável (funcionário com função TECNICO na filial), Fornecedor opcional, Custo estimado → Salvar
- **Then** POST /api/v1/ordens → Backend valida BR-05 (técnico alocado + estado ABERTA) → 201 com ordem completa (ID, estado ABERTA, timestamps); Notificação push/email para técnico

### US-ORD-002 — Listar/Buscar Ordens (Filtros Completos + Paginação + Fuzzy Ativo)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** Filtros: Estado (ABERTA/EM_ANDAMENTO/APROVADA/CONCLUIDA/CANCELADA), Prioridade, Filial, Depto, Técnico, Ativo (fuzzy), Período, Tipo (CORRETIVA/PREVENTIVA/PREDITIVA); Ordenação: Prioridade (CRITICA primeiro), Data criação, SLA; Paginação server-side

### US-ORD-003 — Detalhe Ordem (Timeline Estados + Evidências + Custos + Auditoria)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** GET /ordens/{id} → Ordem + timeline (Aberta→Iniciada→Aprovada→Concluída) + Evidências (fotos, checklist, assinatura) + Custos (estimado vs realizado) + Auditoria Envers

### US-ORD-004 — Iniciar Ordem (Técnico) — Valida BR-05
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** PATCH /ordens/{id}/iniciar → Só se ABERTA + técnico alocado (ou GESTOR filial) → EM_ANDAMENTO + timestamp início + usuário executor; Cronômetro UI inicia

### US-ORD-005 — Aprovar Ordem (Evidência Obrigatória BR-06)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** PATCH /ordens/{id}/aprovar → Valida `canApprove: true` (evidência: foto/checklist/assinatura digital) → APROVADA + timestamp + aprovador; Notifica técnico + solicitante

### US-ORD-006 — Rejeitar Ordem (Volta para EM_ANDAMENTO)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** PATCH /ordens/{id}/rejeitar {motivo} → EM_ANDAMENTO; Notifica técnico

### US-ORD-007 — Concluir Ordem (Custos Finais + custoTotalPorAtivo Atualizado)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** PATCH /ordens/{id}/concluir {maoDeObraHoras, maoDeObraValor, materiais[], terceiros[]} → Só se APROVADA → CONCLUIDA + timestamp + custos finais; Trigger: Atualiza `custoTotalPorAtivo` do ativo (BR-08); Verifica preventiva baseada em uso/tempo

### US-ORD-008 — Cancelar Ordem (Qualquer Estado Exceto CONCLUIDA)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** PATCH /ordens/{id}/cancelar {motivo} → CANCELADA; Não afeta custoTotalPorAtivo; Auditoria motivo

### US-ORD-009 — Reatribuir Técnico Responsável (Modal Busca Funcionários)
**Priority:** Should | **Complexity:** 3 | **Slice:** Should (era Could no story-map v1, movido para Should)
**BDD:** PUT /ordens/{id}/reatribuir {tecnicoId} → Valida função TECNICO na filial; Notifica novo + antigo técnico

### US-ORD-010 — Histórico de Estados da Ordem (Timeline Visual)
**Priority:** Should | **Complexity:** 3 | **Slice:** Should
**BDD:** Componente timeline visual no detalhe; Estados com ícones, timestamps, usuários, duração entre estados

### US-ORD-011 — Campos Custo Direto na Conclusão (Horas, Materiais, Terceiros)
**Priority:** Should | **Complexity:** 5 | **Slice:** Should
**BDD:** Formulário estruturado na conclusão: Mão de obra (horas × rate automático por cargo), Materiais (item + qtd + valor unitário), Terceiros (fornecedor + valor + NF)

### US-ORD-012 — Checklists Digitais Configuráveis por Tipo Manutenção
**Priority:** Could | **Complexity:** 8 | **Slice:** Could
**BDD:** Backend: Template checklist por TipoManutencao; Frontend: Renderiza dinamicamente + Coleta assinatura digital por item

### US-ORD-013 — Anexos Fotográficos (Upload + Preview + Vinculação Ordem)
**Priority:** Could | **Complexity:** 5 | **Slice:** Could
**BDD:** POST /ordens/{id}/anexos (multipart); Preview frontend; Tipos: ANTES, DURANTE, DEPOIS, EVIDENCIA_APROVACAO

### US-ORD-014 — SLA & Escalation Automático (Job Diário + Notificação)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Job diário verifica ordens ABERTA/EM_ANDAMENTO/AGUARDANDO_APROVACAO > 24h → Notifica aprovador + gestor filial; > 48h → escala ADMIN global; Auditoria SLA breach

### US-ORD-015 — Dashboard Ordens (KPIs + Gráficos + Alertas Tempo Real WebSocket)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** GET /dashboard/ordens → Cards: Abertas, Em Andamento, Aguardando Aprovação, Vencendo SLA 24h, Concluídas Mês, Custo Total Mês; Gráficos: Tendência 30d, Por Prioridade, Por Filial, Por Tipo Ativo; WebSocket: ordem.criada, iniciada, aprovada, concluida, cancelada, sla.breach

---

## Épico 4: Manutenção Preventiva (Should — 16 Stories)

### US-PRE-001 — Cadastrar Plano Preventivo (CRON + Técnico Padrão + Ativos/TipoAtivo)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** Frequência CRON (quartz); Dia/hora preferencial; Técnico padrão; Descrição padrão; Seleciona: Ativos específicos OU TipoAtivo + Filial (gera para todos ativos do tipo na filial)

### US-PRE-002 — Scheduler Geração Automática Ordens Preventivas (Job 15min)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** @Scheduled 15min; Verifica `proximaExecucao <= now`; Cria ordem PREVENTIVA ABERTA com dados do plano; Atualiza `proximaExecucao`; Notifica técnico; Auditoria geração auto

### US-PRE-003 — Gerenciar Planos (Editar, Pausar/Reativar, Excluir Bloqueada)
**Priority:** Should | **Complexity:** 5 | **Slice:** Should (3 stories)

### US-PRE-004 — Relatório Aderência Preventiva (% Prazo + Evidência NR-10/12)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** GET /relatorios/preventiva-aderencia?filial=&periodo=&tipoAtivo= → % concluídas no prazo / total geradas; Detalhe: plano, ordens geradas, concluídas no prazo, atrasadas, canceladas; Export PDF/CSV assinado

### US-PRE-005 — Calendário Visual Preventivas (Próximas Execuções)
**Priority:** Should | **Complexity:** 5 | **Slice:** Should
**BDD:** View mensal/semanal; Próximas execuções coloridas por prioridade; Clique → detalhe plano

---

## Épico 5: Manutenção Preditiva (Should — 22 Stories)

### US-PRD-001 — Health Check Coleta PWA Mobile (QR Scan + SMART Input)
**Priority:** Should | **Complexity:** 13 | **Slice:** Should
**BDD:**
- **Given** Técnico escaneia QR Code ativo (PWA câmera) → Abre `/public/ativo/{tag}?h={hash}`
- **When** Login rápido (biometria/pin) → Tela Health Check: Discos listados + campos SMART (reallocated_sectors, seek_error_rate, spin_retry_count, temperature, power_on_hours) → Salvar
- **Then** POST /ativos/{id}/health-check → HealthCheckService.analisar() → Score 0-100 (thresholds por tipo disco); Persiste HealthCheck histórico + atualiza Disco.healthScore; Se score < threshold → Alerta crítico + Ordem Preditiva Auto (US-PRD-005)

### US-PRD-002 — Health Check Offline-First (IndexedDB + Service Worker + Background Sync)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** Service Worker cache assets; IndexedDB armazena health checks offline; Sync automático ao reconectar; UI: badge "Pendente sincronização {n}" → Toast "Sincronizado" ao concluir

### US-PRD-003 — Regressão Linear Falha Disco (Batch Noturno 02:00 + Mínimos Quadrados)
**Priority:** Should | **Complexity:** 13 | **Slice:** Should
**BDD:** Job 02:00; Para cada ativo com ≥ 3 health checks: `ManutencaoPreditivaService.preverFalhaDisco(ativoId)` → Regressão linear simples (y = ax + b) sobre `reallocated_sectors` vs `power_on_hours` (ou temperatura vs tempo); Calcula: **dias até threshold crítico** (ex.: reallocated > 100) + **IC 95%**; Persiste `PrevisaoFalha` (ativo, disco, dataPrevista, probabilidade, icInferior, icSuperior, modeloUsado, rQuadrado)

### US-PRD-004 — Previsão Falha: Threshold Probabilidade + Horizonte Configuráveis
**Priority:** Should | **Complexity:** 3 | **Slice:** Should
**BDD:** Configuração: Probabilidade mínima (default 80%), Horizonte máximo (default 30 dias), Threshold crítico por tipo disco

### US-PRD-005 — Ordem Preditiva Automática (Prob > 80% < 30 dias → Ordem PREDITIVA ALTA/CRITICA)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** Trigger: US-PRD-003 resultado OU US-PRD-001 score crítico (< 40); Cria ordem tipo PREDITIVA: Descrição "Falha de disco prevista para {data} (probabilidade {X}%). Substituir preventivamente."; Prioridade ALTA/CRITICA; Técnico = responsável ativo ou padrão filial; Alerta dashboard + notificação; Auditoria origem=PREDITIVA_AUTO

### US-PRD-006 — Dashboard Preditivo (Riscos + Drill-down + Ação "Agendar Substituição")
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** GET /dashboard/preditiva → Cards: Monitorados, Alertas Ativos, Ordens Preditivas Abertas, Previsões <30d; Tabela: Ativo, Tag, Disco, Health Score, Probabilidade, Data Prevista, IC 95%, Ação (Ver Ordem / Agendar Substituição); Filtros: Filial, Probabilidade (slider), Horizonte (7/30/90 dias); WebSocket: preditiva.alerta

### US-PRD-007 — Agendar Substituição (Cria Preventiva Programada + Marca Previsão TRATADA)
**Priority:** Should | **Complexity:** 5 | **Slice:** Should
**BDD:** Botão "Agendar Substituição" na tabela → Cria plano preventivo one-shot (data = dataPrevista - 2 dias) + Marca `PrevisaoFalha.tratada = true`; Auditoria

### US-PRD-008 — Health Check Agente Automático (Futuro — Background Service no Ativo)
**Priority:** Could | **Complexity:** 13 | **Slice:** Could (fora do escopo v1.1 — placeholder)

---

## Épico 6: Busca Inteligente Fuzzy (MVP — 8 + Could — 4)

### US-BUS-001 — Busca Global Unificada (Header + Debounce + Dropdown Agrupado)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** Header campo busca; Debounce 300ms; GET /busca?q=...&types=ativo,ordem,funcionario,fornecedor&filialId=&page=0&size=10 → Dropdown resultados agrupados por tipo (ícones, highlight termo, ações rápidas: "Ver detalhe", "Criar ordem para este ativo")

### US-BUS-002 — Busca Fuzzy Backend (Levenshtein Distance ≤ 2 + Boost por Tipo + Filtros Exatos)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** FuzzySearchService.buscar(); Campos indexados: Ativo(tag,serial,modelo,fabricante), Ordem(numero,descricao), Funcionario(nome,matricula), Fornecedor(razaoSocial,cnpj); Score = fuzzyScore × boostTipo (ativo=1.0, ordem=0.9, funcionario=0.8, fornecedor=0.7) + filtros exatos (filial, status, tipo, data)

### US-BUS-003 — Índices Trigram MySQL + Cache Redis para Performance
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** CREATE INDEX idx_ativo_trigram ON ativo USING GIN (tag gin_trgm_ops, serial gin_trgm_ops, ...); Redis cache queries frequentes (TTL 5min); Invalidação em escrita

### US-BUS-004 — Busca Fuzzy Específica por Entidade (Modais Seleção)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** Modal seleção ativo em ordem → GET /busca/ativos?q=...&filialId=... → Retorna apenas ativos com score fuzzy

### US-BUS-005 — Configuração Threshold Fuzzy (Admin) + @RefreshScope
**Priority:** Could | **Complexity:** 3 | **Slice:** Could
**BDD:** UI Admin: Similaridade mínima (0.5-0.9, default 0.7), Campos indexados por entidade (checkbox), Pesos boost por tipo (number input); Reload runtime sem restart

### US-BUS-006 — Métricas Busca (Taxa Sucesso, Zero Results, Tempo Resposta)
**Priority:** Could | **Complexity:** 3 | **Slice:** Could
**BDD:** Eventos: busca_realizada, resultado_clicado, zero_results; Dashboard: Taxa clique/resultado, P95 latência, % queries com typo

---

## Épico 7: Segurança Aegis Shield (MVP — 20 + Should — 16 + Could — 4)

### US-SEC-001 — Login JWT + Refresh Token (HttpOnly Cookie + RS256)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** POST /auth/login {email, senha} → 200 {accessToken (15min, RS256), refreshToken (HttpOnly; Secure; SameSite=Strict; 7d)}; Frontend: authInterceptor armazena accessToken memória (Pinia); Anexa Authorization: Bearer em TODAS requests

### US-SEC-002 — Refresh Token Automático (1x Retry) + Logout Limpo
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** 401 response → authInterceptor detecta → POST /auth/refresh (cookie automático) → Novo accessToken → Retry request original (1x only); Falha refresh → Limpa Pinia + cookie → Redirect /login

### US-SEC-003 — Multi-tenancy: Filtro Filial no Nível Query (Hibernate Filter)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** MultiTenancyFilter extrai filialId do JWT/Contexto → Hibernate Filter `filialFilter` ativo em TODAS entidades @FilterDef; Admin Global: tenantId = null (vê todas); Testes cross-tenant CI: 0 vazamentos

### US-SEC-004 — RBAC Básico: Roles (ADMIN, GESTOR, TECNICO, USER, AUDITOR) + Permissions Fixas
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** Roles hierárquicas: ADMIN > GESTOR > TECNICO > USER > AUDITOR; Permissions por role fixas no MVP; AegisShieldPermissionEvaluator avalia `hasPermission(usuario, recurso, acao, contextoFilial)`

### US-SEC-005 — Auditoria Envers: 100% Entidades Domínio (CREATE/UPDATE/DELETE + Diff + IP + UA)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** @Audited em Ativo, AtivoDetalheHardware, Disco, Memoria, AdaptadorRede, Filial, Departamento, Localizacao, Fornecedor, Funcionario, TipoAtivo, SolicitacaoManutencao, ManutencaoPreventiva, Usuario, Role, Permission; AuditoriaController: GET /auditoria?entidade=&id=&usuario=&periodo=&acao= → Timeline + Diff visual (antes/depois)

### US-SEC-006 — Matriz RBAC Granular (Admin Global: Role × Permission × Contexto Global/Filial)
**Priority:** Should | **Complexity:** 13 | **Slice:** Should
**BDD:** UI Matriz visual: Linhas=Roles, Colunas=Permissions (ATIVO_CRIAR, ATIVO_LER, ATIVO_ATUALIZAR, ATIVO_EXCLUIR, ORDEM_CRIAR, ORDEM_INICIAR, ORDEM_APROVAR, ORDEM_CONCLUIR, ORDEM_CANCELAR, ORDEM_LER_TODAS, RELATORIO_GERAR, CONFIG_GERENCIAR, AUDITORIA_LER, LGPD_GERENCIAR, HEALTH_CHECK_COLETAR, PREDITIVA_LER, PREVENTIVA_GERENCIAR), Contexto=Global/Filial; Edita célula → PUT /admin/roles/{role}/permissions; Validação consistência (ex.: TECNICO não pode ORDEM_APROVAR)

### US-SEC-007 — Gerenciar Usuários (Provisionamento + Roles + Filial + Status + Reset Senha)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** CRUD /admin/usuarios; Admin Global vê todos; Admin Filial vê só sua filial; Provisiona Funcionario opcional; Reset senha → email link temporário; Soft delete (status INATIVO)

### US-SEC-008 — Acessos Negados / Cross-tenant Leak Detection + SIEM
**Priority:** Should | **Complexity:** 5 | **Slice:** Should
**BDD:** Log AccessDeniedException + tentativas cross-tenant (MultiTenancyFilter block); Alerta >10/min; Export Syslog/Elastic para SIEM

### US-SEC-009 — Rate Limiting API Gateway (IP/Usuário + Config por Endpoint)
**Priority:** Could | **Complexity:** 3 | **Slice:** Could
**BDD:** Spring Cloud Gateway: 100 req/min /auth, 500 req/min /api/v1/**, config por endpoint via YAML

### US-SEC-010 — Pen Test Automatizado CI (OWASP ZAP DAST + CodeQL SAST) + Block Deploy
**Priority:** Could | **Complexity:** 8 | **Slice:** Could
**BDD:** Staging: ZAP active scan + CodeQL; Falha crítica/alta = block deploy; Relatório artefato CI

### US-SEC-011 — CSP + CORS + Security Headers + OWASP Top 10 Frontend
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** CSP: script-src 'self', style-src 'self' 'unsafe-inline', img-src 'self' data:; DOMPurify sanitização inputs; SameSite=Strict cookies; X-Frame-Options DENY

### US-SEC-012 — LGPD: Anonimização (Esquecimento) + Auditoria Preservada (Hash)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** POST /usuarios/me/solicitar-exclusao → Anonimiza Usuario (nome→USUARIO_ANON_{hash}, email→hash@anonymized.local); Mantém Envers com hash usuário original; Revoga tokens + invalida sessões + status INATIVO

### US-SEC-013 — LGPD: Portabilidade (Export JSON Completo)
**Priority:** Should | **Complexity:** 3 | **Slice:** Should
**BDD:** GET /usuarios/me/exportar → JSON: perfil, ordens, ativos responsáveis, health checks, auditoria, consentimentos

### US-SEC-014 — Validação Integridade Checksums (Batch Semanal SHA-256)
**Priority:** Could | **Complexity:** 5 | **Slice:** Could
**BDD:** Job domingo 03:00; SHA-256 tabelas críticas (ativo, ordem, usuario, auditoria); Compara baseline tabela `integridade_checksum`; Divergência → Alerta CRÍTICO + Incidente Segurança

---

## Épico 8: Relatórios & QR/PDF (MVP — 6 + Should — 8)

### US-REL-001 — Termo Responsabilidade PDF (Alocação/Transferência/Baixa + Assinatura Placeholder)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** POST /relatorios/termo-responsabilidade {ativoId, responsavelId, tipo: ALOCACAO|TRANSFERENCIA|BAIXA, observacoes} → PdfGenerator (Thymeleaf/FlyingSaucer) → PDF com dados completos + QR Code verificação + Hash SHA-256; Assinatura: Placeholder v1 (manual + testemunha) | Futuro: ICP-Brasil/gov.br A1

### US-REL-002 — Etiquetas QR Code Lote (Filtros + PDF A4 24/folha + ZIP SVG)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-REL-003 — Dashboard Unificado (Ordens + Preditiva + Custos + Alertas Tempo Real)
**Priority:** Must | **Complexity:** 13 | **Slice:** MVP (Frontend + Backend WebSocket)

### US-REL-004 — Relatórios Compliance (NR-10/12, LGPD, ISO 27001) — PDF Estruturado + Assinatura
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** GET /relatorios/compliance?tipo=NR12&periodo=2025-01 → PDF com evidências: ordens com checklist, anexos, assinaturas, auditoria, acessos

### US-REL-005 — Termo Responsabilidade Assinatura Digital (Integração ICP-Brasil/gov.br Placeholder)
**Priority:** Should | **Complexity:** 8 | **Slice:** Should
**BDD:** Campo assinatura digital no PDF; Integração futura: ICP-Brasil (certificado A1/A3) ou gov.br (assinatura eletrônica avançada)

### US-REL-006 — Exportação Dados (CSV/PDF) para Todos Relatórios
**Priority:** Should | **Complexity:** 3 | **Slice:** Should

---

## Épico 9: LGPD & Compliance (Should — 6 + Could — 4)

### US-LGD-001 — Consentimento Granular por Finalidade (Cadastro + Config)
**Priority:** Should | **Complexity:** 5 | **Slice:** Should
**BDD:** Tabela `consentimento` (usuarioId, finalidade, concedidoEm, revogadoEm); UI: Painel privacidade com toggles por finalidade

### US-LGD-002 — DPIA Automatizado + Versionado (Gera Relatório Baseado em Entidades + Fluxos)
**Priority:** Could | **Complexity:** 8 | **Slice:** Could
**BDD:** Job/Script analisa entidades com dados pessoais + fluxos de processamento → Gera DPIA Markdown/PDF versionado no repo

### US-LGD-003 — Retenção Logs Erro/Telemetria (30 dias) + Anonimização IDs (7 dias)
**Priority:** Should | **Complexity:** 3 | **Slice:** Should
**BDD:** Logback/Sentry: retenção 30 dias; Pipeline anonimização: ordemId/usuarioId → hash após 7 dias

---

## Épico 10: Observabilidade & Qualidade (MVP — 12 + Should — 6 + Could — 2)

### US-OBS-001 — Métricas Prometheus (Golden Signals + Business KPIs + JVM + DB Pool + Cache)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** Micrometer + Prometheus; Latência (histogram), Throughput (counter), Erros (counter), JVM (memory, GC, threads), HikariCP (active/idle), Redis (hit/miss), Health Check Preditiva (score, threshold)

### US-OBS-002 — Dashboards Grafana (Golden Signals por Endpoint + Business KPIs)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Dashboards: API Latency/Traffic/Errors/Saturation por endpoint; Ordens/dia, CustoTotalPorAtivo, Alertas Preditivos Ativos, Aderência Preventiva, Login Success Rate

### US-OBS-003 — Alertas Alertmanager (Erro>1%, Latência>500ms, Refresh>5%, Preditiva>Threshold, SLA Breach, Cross-tenant>10/min)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-OBS-004 — Tracing Distribuído (trace-id Header Propagado Frontend→Backend + 100% Mutações)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP
**BDD:** Frontend gera UUID → Header `X-Trace-Id` → Backend propaga em logs (MDC) + chamadas internas; 100% mutações (POST/PUT/PATCH/DELETE) cobertas

### US-OBS-005 — Logs Estruturados JSON (SLF4J + Logback + Logstash Encoder + Correlation ID)
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP
**BDD:** Níveis: ERROR/WARN prod, DEBUG staging; Correlation ID (trace-id) em todos logs; Campos: timestamp, level, logger, message, traceId, usuarioId, filialId, entidade, acao

### US-OBS-006 — Frontend Core Web Vitals (LCP, FID, CLS) via RUM (Sentry/Datadog) + Zero Console.*
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-OBS-007 — TestContainers MySQL Real + CI Coverage Gate 80% + Contract Tests
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP
**BDD:** `.testcontainers.properties` reuso habilitado; Testes integração: ControllerIT (Cadastros, Ativos, Ordens, Auth, Busca, Auditoria, Relatórios); Contract Tests (Spring Cloud Contract / Pact) frontend↔backend

### US-OBS-008 — Qualidade Estática: Checkstyle (Google Java Style) + SpotBugs + PMD + ESLint (Airbnb) + Prettier + TypeScript Strict
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-OBS-009 — OpenAPI 3.1 Spec Versionado + Validação CI + Breaking Change = Version Bump
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-OBS-010 — ADRs para Decisões Arquiteturais + Diagramas C4 (Structurizr/PlantUML) Versionados
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-OBS-011 — Pen Test CI (ZAP/CodeQL) + Block Deploy Crítico/Alto
**Priority:** Could | **Complexity:** 8 | **Slice:** Could

### US-OBS-012 — PostgreSQL 15+ Support (Flyway Dialect Abstraction + CI Test)
**Priority:** Could | **Complexity:** 5 | **Slice:** Could

---

## Épico 11: Infra & DevOps (MVP — 8 + Could — 2)

### US-INFRA-001 — Spring Boot 3.3 + Java 21 + MySQL 8 + Flyway + Lombok + Maven Wrapper
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-INFRA-002 — Docker Compose (MySQL, Redis, Backend, Frontend) + K8s Manifests (Deployment, Service, Ingress, HPA, ConfigMap, Secret)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP

### US-INFRA-003 — CI/CD Pipeline (GitHub Actions): Build → Test (Coverage) → Security Scan → Contract Test → Deploy Staging → Smoke → Deploy Prod (Blue/Green)
**Priority:** Must | **Complexity:** 8 | **Slice:** MVP

### US-INFRA-004 — Secrets Management (K8s Secrets / Vault / GitHub Secrets) + Rotação Certificados (cert-manager)
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-INFRA-005 — Backup/Restore MySQL (Binlog + Point-in-Time Recovery) + Testes Restore Mensais
**Priority:** Must | **Complexity:** 5 | **Slice:** MVP

### US-INFRA-006 — Runbooks: Deploy, Rollback, Incident Response, Scale, Backup/Restore, Security Incident
**Priority:** Must | **Complexity:** 3 | **Slice:** MVP

### US-INFRA-007 — Multi-cloud Deploy Test (AWS EKS + Azure AKS + GCP GKE) — Could
**Priority:** Could | **Complexity:** 8 | **Slice:** Could

### US-INFRA-008 — FinOps Dashboard (Custos por Serviço/Ambiente/Time) — Could
**Priority:** Could | **Complexity:** 3 | **Slice:** Could

---

## Definition of Ready (Global — Aplicável a Todas Stories)

- [ ] Critérios de aceite BDD definidos e validados com PO
- [ ] Contrato OpenAPI alinhado (Backend) / Mock MSW pronto (Frontend)
- [ ] Design/UX aprovado (Figma/Storybook)
- [ ] Estimativa de esforço realizada (Planning Poker)
- [ ] Dependências técnicas identificadas e resolvidas ou mitigação planejada
- [ ] Riscos conhecidos documentados com mitigação
- [ ] Segurança/Privacidade revisada (DPIA se dados pessoais)
- [ ] Performance/Escalabilidade considerada (NFRs referenciados)

---

## Definition of Done (Global — Aplicável a Todas Stories)

- [ ] Código implementado seguindo padrões do projeto (Clean Architecture, DDD tático)
- [ ] Code Review aprovado (mínimo 1 reviewer sênior)
- [ ] Testes unitários cobrindo cenários BDD (cobertura ≥ 80% nas funções alteradas)
- [ ] Testes de integração (TestContainers MySQL real) para endpoints alterados
- [ ] Testes de contrato (Pact/Spring Cloud Contract) se API alterada
- [ ] Testes E2E (Cypress) para fluxos críticos user-facing
- [ ] Tipagem estrita (TypeScript strict / Java — sem `any` / raw types)
- [ ] Lint/Format ok (Checkstyle/SpotBugs/PMD backend; ESLint/Prettier frontend)
- [ ] Sem regressões de acessibilidade (axe-core CI)
- [ ] Documentação atualizada: OpenAPI, ADR se decisão arquitetural, README módulo
- [ ] Feature testada em Staging (dados realistas, multi-tenancy, roles)
- [ ] Métricas/Observabilidade instrumentadas (Prometheus metrics, logs JSON, tracing)
- [ ] Security Scan pass (OWASP Dep Check, SpotBugs, Semgrep)
- [ ] Changelog atualizado (Conventional Commits)

---

## Rastreabilidade Consolidada (Matriz Story ↔ Artefatos)

| Story ID | Use Case | User Flow | API Spec | State Machine | Component | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| US-CAD-* | UC-01 a UC-05 | Flow 2 | `/filiais`, `/departamentos`, `/fornecedores`, `/funcionarios`, `/tipos-ativo` | — | `EntityTable`, `EntityForm`, `EntityModal`, `TenantSelector` | ControllerIT, Cypress CRUD |
| US-ASV-* | UC-06 a UC-12 | Flow 3, 4 | `/ativos`, `/hardware`, `/relatorios/custo-total`, `/public/ativo` | `state-machines.md#ativo` | `AtivoForm`, `HardwareSection`, `QRCodeDisplay`, `PdfViewer`, `AtivoDetail` | AtivoControllerIT, HardwareControllerIT, RelatorioControllerIT |
| US-ORD-* | UC-13 a UC-20 | Flow 1, 11 | `/ordens`, `/dashboard/ordens` | `state-machines.md#solicitacaomanutencao` | `OrderCard`, `OrderDetail`, `OrderActions`, `OrderTimeline`, `CostForm`, `KPICard`, `ChartWidget`, `AlertFeed` | OrdemControllerIT, SlaSchedulerIT, DashboardControllerIT, Cypress E2E Ordem |
| US-PRE-* | UC-21 a UC-24 | Flow 5 | `/preventivas`, `/relatorios/preventiva-aderencia` | `state-machines.md#manutencaopreventiva` | `PreventivaPlanForm`, `PreventivaCalendar`, `AderenciaReport` | PreventivaSchedulerIT, PreventivaControllerIT |
| US-PRD-* | UC-25 a UC-28 | Flow 6, 11 | `/health-check`, `/dashboard/preditiva`, `/previsao-falha` | `state-machines.md#healthcheck` | `HealthCheckForm`, `QRScanner`, `PreditivaDashboard`, `PrevisaoTable`, `AlertCard` | ManutencaoPreditivaServiceTest, HealthCheckServiceTest, PrevisaoFalhaControllerIT |
| US-BUS-* | UC-29 a UC-31 | Flow 7 | `/busca` | — | `GlobalSearch`, `SearchResults`, `SearchConfig` | FuzzySearchServiceTest, LevenshteinDistanceTest, BuscaControllerIT |
| US-SEC-* | UC-32 a UC-38 | Flow 8 | `/auth`, `/admin/roles`, `/admin/usuarios`, `/auditoria` | `state-machines.md#usuario` | `LoginForm`, `RoleMatrix`, `TenantSelector`, `AuditTimeline`, `PrivacyPanel` | SecurityConfigIT, AegisShieldTest, MultiTenancyIT, AuthControllerIT |
| US-REL-* | UC-39 a UC-42 | Flow 9, 11 | `/relatorios/termo`, `/qr-code`, `/etiquetas-lote`, `/compliance`, `/dashboard/*` | — | `PdfViewer`, `QRCodeGenerator`, `BatchExport`, `KPICard`, `ChartWidget` | RelatorioControllerIT, QRCodeGeneratorTest, PdfGeneratorTest |
| US-LGD-* | UC-37, UC-43 a UC-45 | Flow 10, 12 | `/usuarios/me/exclusao`, `/exportar`, `/auditoria/integridade` | — | `PrivacyPanel`, `DataExport`, `IntegrityBadge` | UsuarioControllerIT, AuditoriaControllerIT, IntegridadeChecksumIT |
| US-OBS-* | Transversal | Transversal | `/actuator/*`, `/metrics` | — | `MetricsInterceptor`, `TracingFilter` | ObservabilityIT, ContractTests |
| US-INFRA-* | Transversal | Transversal | — | — | `Dockerfile`, `docker-compose.yml`, `k8s/*.yaml`, `.github/workflows/*.yml` | InfraIT, DeploySmokeTest |

---

*Documento regenerado com base em análise AST completa do backend Java (domain, service, controller, security, predictive, search, audit, report, scheduler, config) + frontend Vue/PWA. Substitui versão 1.0 que continha apenas 20 stories frontend de cadastros/ordens básicas. Total: 241 stories organizadas em 11 épicos × 3 slices (MVP/Should/Could).*