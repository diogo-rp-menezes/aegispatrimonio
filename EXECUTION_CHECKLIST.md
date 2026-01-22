# Checklist de Execução — Aegis Patrimônio

**Propósito:** Este documento é o pilar operacional do projeto. Ele detalha as tarefas de cada Fase/Sprint e serve como o guia diário para a equipe de desenvolvimento, garantindo que os critérios de aceite e as métricas de sucesso sejam cumpridos.

---

## 📚 Documentos de Referência

Este checklist é guiado pelos seguintes documentos de arquitetura e planejamento. Consulte-os para obter o contexto completo.

- **Planejamento Mestre:**
  - [Plano de Ação Completo - Aegis Patrimônio.md](Plano%20de%20Ação%20Completo%20-%20Aegis%20Patrimônio.md)

- **Arquitetura e Design:**
  - [ARQUITETURA_PLAN.md](ARQUITETURA_PLAN.md) - Visão geral da arquitetura.
  - [DEVELOPMENT_MODELS.md](DEVELOPMENT_MODELS.md) - Padrões de código e implementação.
  - [TECH_DESIGN_RBAC.md](TECH_DESIGN_RBAC.md) - Design técnico para a Fase 1.
  - [TECH_DESIGN_HealthCheck.md](TECH_DESIGN_HealthCheck.md) - Design técnico para a Fase 2.

- **Qualidade e Testes:**
  - [TEST_PLAN.md](TEST_PLAN.md) - Catálogo de casos de teste.
  - [TEST_CHECK_STRATEGY.md](TEST_CHECK_STRATEGY.md) - Metodologia de execução de testes.

- **Governança e Colaboração:**
  - [rules.md](rules.md) - Regras de governança do projeto.
  - [agents.md](agents.md) - Definição de papéis e responsabilidades.

---

## Ações de Suporte à Qualidade (Fase 1)
- [x] CRÍTICO: Resolver configuração Testcontainers
  - [x] Criar arquivo .testcontainers.properties com reuse=true (raiz do projeto)
  - [ ] Validar em ambiente local do desenvolvedor (mvn clean verify) — medir tempo antes/depois
  - [x] Atualizar documentação de setup (README.md — seção TestContainers)
  - [ ] Configurar no CI/CD pipeline (avaliar reuso conforme runner persistente/efêmero)
  - [ ] Monitorar performance pós-correção (coletar tempos de build/teste por 5 execuções)

## FASE 1 — RBAC Avançado (Sprints 1–3)
Objetivo
- Implementar controle de acesso granular com contexto (filial, domínio do recurso, ação) e auditoria de acessos.

Métricas de sucesso
- 95% dos endpoints críticos protegidos por permissões granulares (não apenas roles). 
- Cobertura ≥ 80% na camada de serviço (PermissionService, evaluators, policies). 
- 0 vulnerabilidades críticas/altas (SAST/Dependency scan). 

Critérios de Aceite (DoR/DoD)
- DoR: Modelagem de domínio revisada (Architect + Security), escopo de recursos priorizados (Ativos, Funcionários), estratégia de migração definida (Flyway). 
- DoD: 
  - Testes unitários e integração verdes (mvn clean verify). 
  - Documentação OpenAPI atualizada nos endpoints alterados. 
  - Logs de auditoria sem dados sensíveis; erros tratados no ApplicationControllerAdvice. 
  - Métricas Micrometer expostas para contagem de negações/autorizações. 

Backlog por Sprint

Sprint 1 (Semanas 1–2) — Fundações de RBAC
- [*] Modelagem de domínio RBAC
  - [x] Entidades: Permission, Role, Group, UserGroup; tabelas de associação (RolePermission, GroupPermission, UserRole opcional). 
  - [*] Estratégia de contexto: campos para escopo (ex.: resource, action, tenant/filialId opcional). 
- [*] Migrações Flyway
  - [x] Scripts de criação de tabelas/índices. 
  - [x] Seeds iniciais de permissões base (READ/CREATE/UPDATE/DELETE para domínios prioritários). 
- [*] Infra de Autorização
  - [x] Interface IPermissionService + PermissionServiceImpl com verificações contextuais. 
  - [x] CustomPermissionEvaluator (Spring Security) + método hasPermission(resource, action, context?). 
  - [*] Security Expression para uso em @PreAuthorize("hasPermission(#id, 'ATIVO', 'READ')"). 
  - [x] AtivoController: GET /api/v1/ativos protegido com @PreAuthorize("hasPermission('ATIVO','READ')"). 
- [*] Testes e Observabilidade (S1)
  - [*] Testes unitários (PermissionService, PermissionEvaluator). 
  - [x] Métricas básicas: contadores de autorização/negação (aegis_authz_total) e timer (aegis_authz_eval_timer). 
  - [*] Logs estruturados (SLF4J) para decisões de autorização (nível DEBUG/INFO, sem dados sensíveis). 
  - [x] Teste de integração MockMvc: 403 em GET /api/v1/ativos quando usuário sem permissão (roles={GUEST}). 
  - [x] Testes de integração MockMvc (escrita):
    - POST /api/v1/ativos — USER → 403; ADMIN → autorização OK (4xx != 403 esperado por dados) 
    - PUT /api/v1/ativos/{id} — USER → 403; ADMIN → autorização OK (4xx != 403 esperado por id inexistente)
    - DELETE /api/v1/ativos/{id} — USER → 403; ADMIN → autorização OK (4xx != 403 esperado por id inexistente)

Sprint 2 (Semanas 3–4) — Integração nos Endpoints + Admin RBAC
- [ ] Integração com Controllers prioritários
  - [ ] AtivosController: trocar role-check por permission-check granular em 2–3 endpoints críticos. 
  - [ ] FuncionáriosController: idem para 2 endpoints críticos. 
  - [ ] Tratamento de exceções de autorização no ApplicationControllerAdvice. 
- [ ] Administração de RBAC (backend)
  - [ ] Endpoints CRUD: Roles, Permissions, Groups e atribuições (idempotentes). 
  - [ ] Validações: Bean Validation + regras de negócio no serviço. 
  - [ ] Seeds/DataLoader para perfis iniciais (ADMIN, USER). 
- [ ] Testes e Documentação (S2)
  - [ ] Testes de integração (MockMvc) cobrindo permissão concedida/negada. 
  - [ ] Documentação OpenAPI com requisitos de segurança atualizados. 

Sprint 3 (Semanas 5–6) — Auditoria + Hardening
- [ ] Auditoria de acesso
  - [ ] Eventos de auditoria (autorizado/negado) com usuário, recurso, ação e contexto (sem dados sensíveis). 
  - [ ] Endpoint/consulta para Dashboard de auditoria (mínimo viável). 
- [ ] Hardening e Qualidade
  - [ ] Revisão de cobertura ≥ 80% em serviços + policies. 
  - [ ] Revisão de segurança (SECURITY_ENGINEER): headers, rate limiting, dependências. 
  - [ ] Ajustes finais e documentação de guia de permissões. 

Riscos e Mitigações
- Complexidade de granularidade → Implementação incremental por domínio (Ativos, depois Funcionários). 
- Regressões em endpoints → Testes de integração por fluxo + gates de qualidade no CI. 
- Desempenho em verificações → Caching leve de permissões por usuário (invalidar em alterações). 

Dependências/Pré-requisitos
- Spring Security já configurado (JWT). 
- Flyway ativo. 
- Conjunto mínimo de domínios priorizados: Ativos, Funcionários. 

Responsáveis (placeholders)
- ARCHITECT: revisão de modelagem e decisões de segurança. 
- SECURITY_ENGINEER: PermissionEvaluator, policies e revisão final. 
- BACKEND_ENGINEER: serviços, controllers, migrações. 
- TEST_ENGINEER: suíte de testes e cobertura. 

---

## FASE 2 — HealthCheck Automatizado (Sprints 4–5)
Objetivo
- Implementar coleta automática de métricas de hardware (CPU, memória, disco, rede) com persistência histórica e alertas básicos.

Métricas de sucesso
- Coleta a cada 12 horas com sucesso > 99%.
- Cobertura ≥ 80% na camada de serviço (HealthCheckService, Collector, AlertService).
- Latência dos endpoints de leitura < 150 ms p95.
- 0 vulnerabilidades críticas/altas.

Critérios de Aceite (DoR/DoD)
- DoR:
  - [ ] Biblioteca OSHI validada e aprovada (licença e compatibilidade).
  - [ ] Estratégia de retenção de dados definida (30–90 dias) e índices planejados.
  - [ ] Propriedades de configuração definidas (cron, thresholds, retenção).
- DoD:
  - [ ] Testes unitários e integração verdes (mvn clean verify).
  - [ ] Métricas Micrometer publicadas (counters/gauges/timer).
  - [ ] Documentação OpenAPI dos endpoints criados.
  - [ ] Logs estruturados sem dados sensíveis e erros tratados no ApplicationControllerAdvice.

Backlog por Sprint

Sprint 4 (Semana 1–2) — Coleta + Persistência
- [ ] Integração biblioteca OSHI.
- [ ] Componente OSHIHealthCheckCollector com mapeamento das métricas alvo.
- [ ] Interface IHealthCheckService + HealthCheckServiceImpl (orquestração e validações).
- [ ] Entidade HealthCheckHistory + Repository + índices (desenhados, migration planejada).
- [ ] Scheduler @Scheduled (12 horas, externalizado em properties) + toggle enable/disable.
- [ ] Métricas Micrometer: counters (success/fail), timer e gauges (último snapshot).
- [ ] Testes unitários: Collector e Service (mocks de OSHI e repository).

Sprint 5 (Semana 3–4) — Alertas + Endpoints + Observabilidade
- [ ] HealthCheckAlertService (limiares: CPU > 90%, Memória < 10%, Disco < 10%).
- [ ] Endpoints GET: /healthcheck/last, /healthcheck/history?from&to&page, /healthcheck/alerts/recent.
- [ ] Documentação OpenAPI; segurança @PreAuthorize("hasAnyRole('ADMIN','USER')").
- [ ] Testes de integração (MockMvc) para cenários OK e limites.
- [ ] Dashboard (mínimo viável) — agregação simples (pode ser apenas endpoint + instrução de uso em Grafana/Prometheus).

Riscos e Mitigações
- Crescimento de tabela: aplicar retenção e índices, possível particionamento futuro.
- Overhead de coleta: manter frequência configurável, coletar apenas métricas relevantes.
- Compatibilidade OSHI: teste em ambiente alvo e fallback em caso de métrica indisponível.

Dependências/Pré-requisitos
- Spring Actuator e Micrometer configurados.
- Flyway ativo para migrações.
- Propriedades de configuração definidas em application.yml.

Responsáveis (placeholders)
- ARCHITECT: design e revisão final.
- BACKEND_ENGINEER: implementação de Collector/Service/Scheduler.
- TEST_ENGINEER: suíte de testes e cobertura.
- DEVOPS_ENGINEER: métricas/monitoramento (Prometheus/Grafana). 

## FASE 3 — OAuth2 + SSO (Sprints 6–7)
- [ ] Configuração Spring Security OAuth2. 
- [ ] Provedores: Google, Microsoft. 
- [ ] CustomOAuth2UserService (mapeamento de roles/permissions). 
- [ ] Refresh Token. 
- [ ] SSO entre módulos. 
- [ ] Telas de login (frontend). 

## FASE 4 — Workflows Avançados (Sprints 8–9)
- [ ] Entidades: Workflow, WorkflowStep, WorkflowInstance. 
- [ ] WorkflowEngine (máquina de estados). 
- [ ] Integração com ManutencaoService e MovimentacaoService. 
- [ ] Notificações (WebSocket/Email). 
- [ ] Componente visual + timeline. 

## FASE 5 — QR Code + Mobile (Sprints 10–12)
- [ ] QRCodeService (geração/validação). 
- [ ] Endpoints: GET /ativos/{id}/qrcode, POST /qrcode/validate. 
- [ ] App mobile (React Native) com scanner. 
- [ ] Offline-first e sincronização background. 
- [ ] Push notifications. 
- [ ] Assinatura digital/validação. 

## FASE 6 — IA Preditiva (Sprints 13–15)
- [ ] Coleta histórica (HealthCheck). 
- [ ] Treinamento de modelo inicial (scikit-learn). 
- [ ] PredictiveMaintenanceService. 
- [ ] Endpoint: GET /ativos/{id}/risk-assessment. 
- [ ] Alertas proativos + dashboard. 

## FASE 7 — Polimento e Otimização (Sprints 16–18)
- [ ] Performance tuning/otimização. 
- [ ] Security hardening final. 
- [ ] Testes de carga e stress. 
- [ ] Documentação técnica completa. 
- [ ] UAT e deploy produção. 

---

Governança e Qualidade
- Gates por sprint: 
  - Seguir rigorosamente os padrões do [DEVELOPMENT_MODELS.md](DEVELOPMENT_MODELS.md).
  - Adotar as diretrizes de governança do [rules.md](rules.md).
  - Coordenar as atividades conforme definido no [agents.md](agents.md).
  - Validar a cobertura de testes conforme o [TEST_PLAN.md](TEST_PLAN.md).
  - Build e testes (`mvn clean verify`) verdes. 
  - Cobertura de código ≥ 80% nos serviços. 
  - Sonar: 0 vulnerabilidades críticas/altas. 
  - Revisão do `SECURITY_ENGINEER` quando a segurança for impactada. 
- Comunicação: 
  - Standups diários (15m) e review na sexta (quinzenal). 
  - Atualização deste arquivo a cada mudança relevante. 

Changelog
- 2025-10-24: Criação do checklist inicial com detalhamento da Fase 1 e visão macro das Fases 2–7.
