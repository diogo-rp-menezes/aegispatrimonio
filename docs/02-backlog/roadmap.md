# Product Roadmap & Sprint History — Aegis Patrimônio

> **Horizonte de planejamento:** Trimestral · **Última atualização:** 15/01/2025

<!-- source: story-map#L1-L10 -->

## 1. Strategic Themes (Now / Next / Later)
### Now (Sprint Atual / Q1 2025 — Preparação & MVP Foundation)
* **Fundação Técnica & MVP Core** — Entregar jornada ponta-a-ponta mínima (Onboarding → Ativo → Manutenção → Custo → Auditoria) para piloto em 1 filial (500 ativos, 20 usuários) no Q2/2025. Inclui refatoração obrigatória de código de alta complexidade, correção de stubs, configuração de pipeline CI/CD e definição de banco de dados/migrações. <!-- source: story-map#L55-L78 --><!-- source: story-map#L140-L155 -->

### Next (Q2 2025 — MVP Release & Piloto)
* **MVP Release 1 (Slice 1: Must Have)** — Lançar REQ-001 a REQ-008 em produção para piloto; validar fluxo reativo de manutenção + auditoria + TCO; coletar feedback para calibrar thresholds de health checks no Release 2. <!-- source: story-map#L55-L78 -->

### Next (Q3 2025 — Expansão & Monitoramento)
* **Release 2 (Slice 2: Should Have)** — Habilitar monitoramento preditivo de hardware (REQ-009, REQ-010), gestão de funcionários/portal do usuário final (REQ-011, REQ-012), migração de dados legados (REQ-013) e cobertura de testes ≥80% (REQ-014). Expandir para 5 filiais (3.000 ativos, 100 usuários). <!-- source: story-map#L80-L100 -->

### Later (Q4 2025+ — Backlog Estratégico)
* **Release 3 (Slice 3: Could Have)** — Dashboard executivo North Star (REQ-015), relatórios avançados TCO/depreciação (REQ-016), exportação WORM de auditoria (REQ-017), integração LDAP/AD (REQ-018), agendador nativo de health checks (REQ-019), pipeline CI/CD completo (REQ-020). Preparação para compliance SOX/LGPD. <!-- source: story-map#L102-L122 -->

---

## 2. Upcoming Sprints
### Sprint 0 — 20/01/2025 a 02/02/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Eliminar bloqueadores técnicos críticos antes do MVP: refatorar `AlertNotificationService` (complexidade 17) e `ManutencaoSpecification` (14), corrigir `setUsername` stub em `Usuario.java:86`, definir motor de banco (PostgreSQL) + Flyway, configurar Vite+Vitest+ESLint+CI para frontend, remover `console.*` residuais em `api.js`. <!-- source: story-map#L140-L155 --><!-- source: story-map#L157-L165 -->
* **Capacidade do time:** 21 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: time 3B/2F/1QA/1DevOps, 2 semanas, foco 100% em dívida técnica e infra.
* **Stories planejadas:**
  - [ ] **REQ-008 (parcial)** — Frontend: remover `console.error` (linha 36) e `console.debug` (linha 99) em `frontend/src/services/api.js` (3 pts)
  - [ ] **REQ-008 (parcial)** — Frontend: configurar Vite + Vitest + ESLint + GitHub Actions CI (5 pts)
  - [ ] **Refatoração técnica** — Quebrar `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17) em métodos menores + testes unitários (5 pts) <!-- source: story-map#L140-L145 -->
  - [ ] **Refatoração técnica** — Quebrar `ManutencaoSpecification.build` (complexidade 14) em métodos menores + testes unitários (3 pts) <!-- source: story-map#L140-L145 -->
  - [ ] **Correção crítica** — Implementar `Usuario.setUsername` (linha 86) + teste de regressão para fluxo `createFuncionarioAndUsuario` (2 pts) <!-- source: story-map#L157-L160 -->
  - [ ] **Infraestrutura** — Definir PostgreSQL + Flyway; criar migration inicial (schema filiais, departamentos, tipos, fornecedores, usuários, roles, permissions) (3 pts) <!-- source: story-map#L157-L165 -->
* **Riscos conhecidos:** Ausência de ORM pode exigir decisão rápida entre JPA/Hibernate vs jOOQ; single point of failure do monolito exige runbook de backup/restore testado antes do piloto. <!-- source: story-map#L165-L175 -->

### Sprint 1 — 03/02/2025 a 16/02/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Entregar Onboarding & Configuração Inicial (Etapa 1) + base de Auditoria: CRUD mestre (Filial, Departamento, TipoAtivo, Fornecedor) + RBAC completo (Role, Permission, Usuario, login JWT, regras admin/user) + `audit_log` append-only para operações de escrita. <!-- source: story-map#L55-L65 --><!-- source: story-map#L70-L72 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: mesma equipe, 2 semanas, velocidade estabilizada pós-Sprint 0.
* **Stories planejadas:**
  - [ ] **REQ-001** — Cadastrar Filial, Departamento, Tipo de Ativo, Fornecedor (CRUD mestre) (8 pts) <!-- source: story-map#L55-L58 -->
  - [ ] **REQ-002** — RBAC: criar Role, Permission, Usuario; login JWT; regras `*_comAdmin_deveRetornarCreated` / `*_comUser_deveRetornarForbidden` (13 pts) <!-- source: story-map#L55-L58 -->
  - [ ] **REQ-007** — Auditoria: tabela `audit_log` append-only populada em criar/atualizar/deletar/aprovar/concluir/cancelar (usuario_id, timestamp, entidade, entidade_id, acao, valores_anteriores, valores_novos) (8 pts) <!-- source: story-map#L70-L72 -->
  - [ ] **REQ-013 (parcial)** — Template CSV para migração de ativos, filiais, departamentos (5 pts) <!-- source: story-map#L85-L88 -->
* **Riscos conhecidos:** `setUsername` deve estar corrigido da Sprint 0; `audit_log` precisa cobrir 100% das operações de escrita do slice (validação via teste de integração). <!-- source: story-map#L157-L160 --><!-- source: story-map#L130-L135 -->

### Sprint 2 — 17/02/2025 a 02/03/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Entregar Cadastro & Gestão de Ativos (Etapa 2) + Execução de Manutenção core (Etapa 3): CRUD Ativo com associações + fluxo solicitação→aprovação→conclusão/cancelamento + especificação de busca por status/filial/depto/tipo/período/prioridade. <!-- source: story-map#L58-L62 --><!-- source: story-map#L62-L66 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] **REQ-003** — CRUD Ativo (create/listar/buscar/atualizar/deletar) com associações (filial, depto, tipo, fornecedor) (13 pts) <!-- source: story-map#L58-L60 -->
  - [ ] **REQ-004** — Solicitação de Manutenção: iniciar → aprovar → concluir/cancelar; especificação de busca (status, filial, depto, tipo, período, prioridade) (13 pts) <!-- source: story-map#L62-L65 -->
  - [ ] **REQ-007 (continuação)** — Garantir `audit_log` para operações de Ativo e Manutenção (criar/atualizar/deletar/aprovar/concluir/cancelar) (5 pts) <!-- source: story-map#L70-L72 -->
  - [ ] **REQ-013 (continuação)** — Script de importação CSV para ativos, filiais, departamentos (3 pts) <!-- source: story-map#L85-L88 -->
* **Riscos conhecidos:** Complexidade ciclomática alta em `AtivoMapper.toDTO` (14) — refatorar se impactar performance de listagem. <!-- source: story-map#L140-L145 -->

### Sprint 3 — 03/03/2025 a 16/03/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Completar MVP: custos de manutenção + TCO por ativo + frontend service `request` com authInterceptor/handleApiError/handleResponse + validação DoD completa (testes de contrato, zero console.*, performance P95 ≤500ms). <!-- source: story-map#L65-L68 --><!-- source: story-map#L72-L75 --><!-- source: story-map#L125-L135 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] **REQ-005** — Registro de custos de manutenção (peças, mão de obra, terceiros) vinculados ao ativo (8 pts) <!-- source: story-map#L65-L66 -->
  - [ ] **REQ-006** — `custoTotalPorAtivo` (soma de custos agrupada por ativo) via API e relatório (8 pts) <!-- source: story-map#L72-L74 -->
  - [ ] **REQ-008 (final)** — Frontend: serviço `request` com authInterceptor, handleApiError, handleResponse; validação zero `console.*` residual (10 pts) <!-- source: story-map#L75-L78 -->
  - [ ] **REQ-007 (validação)** — Teste de integração automatizado: `audit_log` registra 100% das operações de escrita do MVP (5 pts) <!-- source: story-map#L130-L135 -->
  - [ ] **DoD MVP** — Testes de contrato API (`request` + `authInterceptor`) passam; latência P95 ≤500ms; taxa erro 5xx <0,1%; documentação runbooks deploy/restore/health check, matriz RBAC, guia migração CSV (3 pts) <!-- source: story-map#L125-L135 -->
* **Riscos conhecidos:** Performance de `custoTotalPorAtivo` em agregação SQL cru; validar índices no banco. Aprovação PO + Security/Compliance Lead obrigatória antes do piloto. <!-- source: story-map#L130-L135 --><!-- source: story-map#L165-L170 -->

### Sprint 4 — 17/03/2025 a 30/03/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Piloto em produção (1 filial, 500 ativos, 20 usuários) — deploy, monitoramento, coleta de feedback, correções de bugs críticos, preparação para Release 2. <!-- source: story-map#L55-L58 --><!-- source: story-map#L135-L140 -->
* **Capacidade do time:** 21 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — Premissa: foco em estabilização, não novas features.
* **Stories planejadas:**
  - [ ] **Deploy Piloto** — Deploy em ambiente de homologação → produção; runbook testado; backup diário + restore validado (8 pts) <!-- source: story-map#L170-L175 -->
  - [ ] **Monitoramento Piloto** — Acompanhar latência P95, taxa erro, logs de auditoria, feedback usuários (Analistas de Manutenção, Admins) (5 pts)
  - [ ] **Bug Fixes Críticos** — Correções de bloqueadores encontrados no piloto (5 pts)
  - [ ] **Lições Aprendidas & Preparação Release 2** — Documentar calibração de thresholds para REQ-010; priorizar backlog Should (3 pts) <!-- source: story-map#L140-L145 -->
* **Riscos conhecidos:** RTO<4h / RPO<24h não testados em carga real; single point of failure pode virar incidente. <!-- source: story-map#L170-L175 -->

### Sprint 5 — 31/03/2025 a 13/04/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Iniciar Release 2 (Should): Health Checks de Hardware (REQ-009) + Alertas de Recursos (REQ-010) com thresholds configuráveis por TipoAtivo. <!-- source: story-map#L80-L90 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] **REQ-009** — Health Check de Hardware: `updateHealthCheck` (coleta adaptadores de rede, discos, memórias); `getHealthHistory`; limpeza em cascata (`deleteByAtivoDetalheHardwareId`) (13 pts) <!-- source: story-map#L80-L83 -->
  - [ ] **REQ-010** — Alertas de Recursos: `checkResourceUsageAlerts` (disco >85%, memória >90%, latência rede >100ms); `listarAlertas`, `getRecentAlerts`, `markAsRead`; thresholds configuráveis por tipo de ativo (13 pts) <!-- source: story-map#L83-L86 -->
  - [ ] **REQ-014 (parcial)** — Testes automatizados: cobertura ≥80% em `AlertNotificationService` (refatorado na Sprint 0) e `ManutencaoSpecification` (5 pts) <!-- source: story-map#L95-L98 -->
  - [ ] **Infra Release 2** — Agendador nativo `@Scheduled` para coleta periódica de health checks (idempotência) — antecipação parcial de REQ-019 (3 pts) <!-- source: story-map#L115-L118 -->
* **Riscos conhecidos:** Falsos positivos/negativos em hardware heterogêneo; thresholds iniciais baseados em palpites — calibrar com dados do piloto. <!-- source: story-map#L175-L180 -->

### Sprint 6 — 14/04/2025 a 27/04/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Gestão de Funcionários + Portal do Usuário Final (REQ-011, REQ-012) + Migração de dados legados completa (REQ-013). <!-- source: story-map#L86-L92 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] **REQ-011** — Gestão de Funcionários: `createFuncionario`, `createFuncionarioAndUsuario`; vincular ativos a responsável (8 pts) <!-- source: story-map#L86-L88 -->
  - [ ] **REQ-012** — Portal do Usuário Final: visualizar ativos sob responsabilidade; abrir solicitação de manutenção; acompanhar status; notificações (13 pts) <!-- source: story-map#L88-L91 -->
  - [ ] **REQ-013 (final)** — Migração de dados legados: execução do script de importação para 5 filiais (3.000 ativos) + validação (8 pts) <!-- source: story-map#L85-L88 -->
  - [ ] **REQ-014 (final)** — Testes automatizados: cobertura ≥80% em `AtivoMapper`, `api.js` (frontend) (5 pts) <!-- source: story-map#L95-L98 -->
* **Riscos conhecidos:** Portal do usuário final exige UX validada com usuários reais; migração de 3.000 ativos pode expor gaps no template CSV. <!-- source: story-map#L140-L145 -->

### Sprint 7 — 28/04/2025 a 11/05/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Release 2 pronto para produção — validação DoD completa, expansão para 5 filiais, documentação atualizada. <!-- source: story-map#L80-L100 --><!-- source: story-map#L125-L135 -->
* **Capacidade do time:** 34 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] **Validação Release 2** — Testes de contrato API; `audit_log` 100% operações escrita (incl. health checks/alertas); performance P95 ≤500ms; taxa erro <0,1% (8 pts) <!-- source: story-map#L125-L135 -->
  - [ ] **Deploy 5 Filiais** — Rollout gradual; runbook de scale (read-replica apenas para relatórios, avaliação) (5 pts) <!-- source: story-map#L170-L175 -->
  - [ ] **Documentação** — Runbooks atualizados, matriz permissões RBAC expandida, guia health checks/alertas, relatório piloto (5 pts)
  - [ ] **REQ-014 (validação final)** — Cobertura ≥80% confirmada em todos serviços refatorados (3 pts)
  - [ ] **Planejamento Release 3** — Refinamento REQ-015 a REQ-020 com PO + Security/Compliance (13 pts) <!-- source: story-map#L102-L122 -->
* **Riscos conhecidos:** Aprovação Security/Compliance Lead para expansão de auditoria/RBAC. <!-- source: story-map#L130-L135 -->

---

## 3. Closed Sprints
### Sprint -1 (Discovery & Setup) — Concluída em 17/01/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Velocity:** 0 pontos (fase de discovery, não contabilizada)
* **Entregas:** Levantamento de requisitos (BRD), Story Map v1.0, diagnóstico de codebase (complexidade ciclomática, stubs, console.*), definição de stack (Java monolito + JS frontend, sem ORM/banco definido).
* **Não concluído / Carry-over:** Definição de banco de dados, configuração CI/CD, refatoração de código complexo, correção de `setUsername` stub.
* **Retrospectiva:**
  * **O que funcionou bem:** Story Map alinhado com BRD; rastreabilidade REQ↔BR clara; identificação precoce de riscos técnicos (complexidade, stub, console, ausência ORM).
  * **O que pode melhorar:** Decisão de banco de dados adiada; pipeline CI/CD inexistente; estimativas de esforço não validadas com time.
  * **Ações para próxima sprint (Sprint 0):** Travar PostgreSQL + Flyway; configurar Vite/Vitest/ESLint/CI; refatorar `AlertNotificationService` e `ManutencaoSpecification`; corrigir `setUsername`.

---

## 4. Milestones & Releases
| Milestone | Data Alvo | Escopo | Status |
| :--- | :--- | :--- | :--- |
| **Sprint 0 Concluída** | 02/02/2025 | Bloqueadores técnicos resolvidos (refatoração, stub, CI/CD frontend, banco definido) | Planejado |
| **MVP Feature Complete** | 16/03/2025 | REQ-001 a REQ-008 implementados, testados, documentados | Planejado |
| **Piloto em Produção (1 filial)** | 30/03/2025 | Deploy validado, 500 ativos, 20 usuários, feedback coletado | Planejado |
| **Release 1 (MVP) GA** | 13/04/2025 | MVP estável, lições aprendidas documentadas, pronto para expansão | Planejado |
| **Release 2 Feature Complete** | 27/04/2025 | REQ-009 a REQ-014 implementados (Health Checks, Alertas, Funcionários, Portal, Migração, Testes ≥80%) | Planejado |
| **Release 2 GA (5 filiais)** | 11/05/2025 | Expansão validada, 3.000 ativos, 100 usuários, documentação completa | Planejado |
| **Release 3 Planning Ready** | 11/05/2025 | Backlog Could (REQ-015 a REQ-020) refinado, estimado, priorizado com PO/Security | Planejado |

---

## 5. Velocity Trend
| Sprint | Planejado | Entregue | Observação |
| :--- | :--- | :--- | :--- |
| Sprint -1 (Discovery) | 0 | 0 | Fase de discovery, não contabilizada |
| Sprint 0 (Tech Debt/Infra) | 21 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: foco 100% dívida técnica |
| Sprint 1 (Onboarding+RBAC+Auditoria) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: velocidade estabilizada pós-Sprint 0 |
| Sprint 2 (Ativos+Manutenção Core) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Sprint 3 (Custos+TCO+Frontend+DoD) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Sprint 4 (Piloto) | 21 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: foco estabilização |
| Sprint 5 (Health Checks+Alertas) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Sprint 6 (Funcionários+Portal+Migração) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| Sprint 7 (Release 2 GA+Planning R3) | 34 | — | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |

> **Nota:** Todas as velocidades planejadas são [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]. Premissas: time estável 3B/2F/1QA/1DevOps (7 pessoas), sprints de 2 semanas, 100% alocação, sem feriados/ausências significativas. Velocidade real será calibrada após Sprint 0 e Sprint 1.

---

## 6. Dependencies & Blockers Ativos
| Item | Bloqueado por | Dono | Previsão de resolução |
| :--- | :--- | :--- | :--- |
| **REQ-001 a REQ-007 (MVP Backend)** | Definição de banco de dados (PostgreSQL) + Flyway migrations + schema travado | Tech Lead / DBA | Sprint 0 (02/02/2025) |
| **REQ-002 (RBAC/JWT)** | Correção `Usuario.setUsername` stub (linha 86) + validação fluxo `createFuncionarioAndUsuario` | Backend Lead | Sprint 0 (02/02/2025) |
| **REQ-003 (CRUD Ativo)** | Refatoração `AtivoMapper.toDTO` (complexidade 14) se impactar performance | Backend Lead | Sprint 0 ou Sprint 2 |
| **REQ-004 (Manutenção)** | Refatoração `ManutencaoSpecification.build` (complexidade 14) | Backend Lead | Sprint 0 (02/02/2025) |
| **REQ-009/REQ-010 (Health Checks/Alertas)** | Refatoração `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17) + testes ≥80% | Backend Lead | Sprint 0 (02/02/2025) — **Bloqueador crítico para Release 2** |
| **REQ-008 (Frontend Service)** | Configuração Vite + Vitest + ESLint + CI/CD (GitHub Actions) | Frontend Lead | Sprint 0 (02/02/2025) |
| **REQ-012 (Portal Usuário Final)** | UX validada com usuários reais (Analistas de Manutenção) | PO / UX | Sprint 5-6 (Abr/2025) |
| **REQ-013 (Migração Legado)** | Template CSV aprovado + script testado com dados anonimizados de produção | Backend Lead / DBA | Sprint 2 (02/03/2025) |
| **Deploy Piloto / Release 2** | Runbook backup/restore testado; RTO<4h / RPO<24h validados; aprovação Security/Compliance | DevOps / Security Lead | Sprint 3-4 (Mar/2025) |
| **REQ-018 (LDAP/AD)** | Decisão arquitetural: substituir `mockLogin` por integração real | Tech Lead / Security | Release 3 (Q4/2025+) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| **REQ-020 (Pipeline CI/CD Completo)** | Build, test, SAST, deploy automatizados para Java + JS | DevOps | Sprint 0 (infra frontend) + Release 3 (pipeline completo) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |