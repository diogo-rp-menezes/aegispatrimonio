# Operations Runbook & System Maintenance Guide — Aegis Patrimônio

> **Owner:** [SRE/DevOps/Eng On-call] · **Última revisão:** [DD/MM/AAAA]
> **Sistema de alerta:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: PagerDuty/Opsgenie] · **Canal de incidentes:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: #aegis-alerts no Slack/Teams]

## 1. System Overview
* **Componentes críticos:**
  - **Backend API:** Spring Boot (Java 17+) — 335 arquivos em `src/main/java/br/com/aegispatrimonio/` — JAR executável `aegis-patrimonio-{version}.jar`
  - **Frontend:** JavaScript vanilla/ESM — 15 arquivos em `frontend/src/` — assets estáticos servidos pelo backend ou CDN
  - **Banco de dados:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: PostgreSQL/MySQL — não detectado no diagnóstico; Deployment Plan assume Flyway/Liquibase para migrações]
  - **Message broker / Job scheduler:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Spring @Scheduled para health checks de ativos — ver `AlertNotificationService.checkResourceUsageAlerts`]
  - **Autenticação/Autorização:** JWT RS256 (chaves RSA rotacionadas a cada 90 dias) + BCrypt para senhas
  - **Feature Flags:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Togglz — config via `application.yml` / Consul]
* **Diagrama de arquitetura:** Ver `system-architecture.md` (não fornecido no diagnóstico) — referência ao Deployment Plan §4 (Environments) e §2 (Pipeline Stages)
* **Dependências externas críticas:**
  - **SMTP:** Envio de notificações de alerta (credenciais rotacionadas a cada 90 dias — Deployment Plan §8)
  - **Provedor de nuvem/Kubernetes:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: EKS/GKE/AKS — Deployment Plan assume K8s para deploy]
  - **Container Registry:** GHCR (`ghcr.io/{org}/aegis-patrimonio:*`) — Deployment Plan §3
  - **Secret Manager:** HashiCorp Vault (produção) + GitHub Actions Secrets (CI) + K8s Secrets (runtime) — Deployment Plan §8
  - **Observabilidade:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Prometheus + Grafana + Loki + Tempo (stack Grafana) ou Datadog — Deployment Plan §7, §8]

## 2. Monitoring & System Health Check
| Métrica | Ferramenta | Threshold Saudável | Threshold de Alerta |
| :--- | :--- | :--- | :--- |
| Latência P95 (endpoints críticos) | [INFERIDO: Prometheus/Grafana ou Datadog] | < 500 ms (conforme NFR/Deployment Plan §7) | > 1000 ms sustentado por 5 min (Deployment Plan §5) |
| Taxa de erro HTTP 5xx | [INFERIDO: Prometheus/Alertmanager] | < 0.1% | > 1% em 5 min (Deployment Plan §5 — gatilho de rollback) |
| Saturação CPU (container) | [INFERIDO: cAdvisor/Prometheus] | < 70% | > 85% sustentado |
| Saturação Memória (JVM Heap) | [INFERIDO: Micrometer/JVM Exporter] | < 70% Heap usado | > 85% Heap usado / GC pause > 500 ms |
| Latência P99 `/actuator/health` | [INFERIDO: Prometheus] | < 100 ms | > 500 ms |
| Job Health Check (`lastRun`) | [INFERIDO: Micrometer custom metric / Actuator] | < 15 min atrás | > 30 min sem execução (Deployment Plan §7) |
| Assets processados no health check | [INFERIDO: Micrometer custom metric] | > 0 por execução | = 0 por 2 execuções consecutivas |
| Alertas gerados (últimas 24h) | [INFERIDO: Micrometer custom metric] | ≥ 0 (esperado) | Pico anômalo > 3x baseline |
| Fila de migrações Flyway/Liquibase pendentes | [INFERIDO: Flyway API / Actuator] | 0 | > 0 em produção |

* **Dashboards principais:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Grafana "Aegis Patrimônio - Overview", "Aegis - JVM", "Aegis - Business Metrics", "Aegis - Deployments"]
* **Health check endpoint:** `GET /actuator/health` (retorna `UP`/`DOWN`/`DEGRADED` + componentes: `db`, `diskSpace`, `ping`, `customHealthCheck` para jobs de ativos) — Deployment Plan §4, §7
* **Métricas específicas de banco:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Ver `db-performance-recovery.md` se existir — não duplicar aqui. Métricas mínimas: conexões ativas/pool, latência query P95, locks, replication lag se réplica]

## 3. On-Call & Escalation
| Nível | Papel | Tempo de resposta esperado | Critério de acionamento |
| :--- | :--- | :--- | :--- |
| L1 | On-call Engineer (rota semanal) | 15 min | Alerta PagerDuty/Opsgenie disparado (thresholds §2) |
| L2 | Tech Lead / Senior Backend | 30 min | L1 não resolve em 30 min OU incidente P0/P1 (múltiplos serviços impactados, perda de dados, segurança) |
| L3 | Engineering Manager / Incident Commander | 1 h | Incidente se estende > 1 h OU requer decisão de rollback de banco/dados OU comunicação externa (clientes, compliance) |

> **Rotação on-call:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Semanal, handoff segunda-feira 10:00 BRT, documentado no Confluence "Aegis On-call Rotation"]
> **Escalação automática:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: PagerDuty escalation policy: L1 → L2 após 15 min sem ack → L3 após 30 min sem resolução]

## 4. Failure Recovery Procedures

### Incident Type 1: API retornando 5xx em massa / Health check `DOWN`
* **Symptom:** 
  - Alerta Prometheus: `http_server_requests_seconds_count{status=~"5.."}` > 1% em 5 min
  - `/actuator/health` retorna `DOWN` ou `DEGRADED`
  - Usuários relatam "Erro interno do servidor" em operações CRUD (ativos, manutenções, relatórios)
* **Possíveis Causas:**
  1. Exceção não tratada em `AtivoService`, `ManutencaoService` ou `AlertNotificationService` (complexidade ciclomática alta detectada — diagnóstico AST)
  2. Pool de conexões DB esgotado (HikariCP) — query lenta/travada em `ManutencaoSpecification.build()` (CC 14)
  3. Falha na validação JWT (chave RSA expirada/rotacionada sem redeploy) — Deployment Plan §8
  4. `OutOfMemoryError` / `StackOverflowError` (recursão em mappers: `AtivoMapper.toDTO` CC 14)
  5. Migração Flyway/Liquibase travada/bloqueando startup
* **Diagnóstico:**
  1. Verificar dashboard "Aegis - JVM": heap, GC, threads, classes carregadas
  2. `kubectl logs -l app=aegis-patrimonio --tail=200 -f` (procurar `ERROR`, `Exception`, `Caused by`)
  3. `kubectl exec -it <pod> -- jcmd <pid> VM.flags` + `jcmd <pid> GC.heap_info`
  4. Checar `/actuator/health` componentes individuais: `db` (conectividade), `diskSpace`, `customHealthCheck`
  5. Verificar Alertmanager: alertas ativos de `JVMHeapHigh`, `DBConnectionPoolExhausted`, `FlywayMigrationFailed`
  6. Se `db` DOWN: `kubectl exec -it <pg-pod> -- pg_isready` + `SELECT count(*) FROM pg_stat_activity;`
* **Remediation:**
  1. **Se OOM/Heap:** `kubectl scale deployment aegis-patrimonio --replicas=0 && kubectl scale deployment aegis-patrimonio --replicas=2` (reinício forçado) — **temporário**; analisar heap dump depois
  2. **Se pool DB esgotado:** Aumentar `spring.datasource.hikari.maximum-pool-size` no ConfigMap + rollout restart; investigar query lenta em `ManutencaoSpecification` (CC 14) e `AtivoService` (TODO perf: carrega 1000 candidatos)
  3. **Se JWT key mismatch:** Verificar Vault: chave `rsa-private-key` vs `rsa-public-key` em uso; se rotacionadas, fazer rollout restart para recarregar (Spring carrega na inicialização)
  4. **Se migração travada:** `kubectl exec -it <pod> -- flyway info` / `liquibase status`; se lock: `DELETE FROM flyway_schema_history WHERE installed_rank = (SELECT max(installed_rank) FROM flyway_schema_history WHERE success = false);` — **cuidado: apenas se migração falhou, não se está rodando**
  5. **Rollback imediato (se causa não identificada em 10 min):** Blue-Green switch para versão anterior (Deployment Plan §5, mecanismo 1) — alvo < 5 min
* **Verificação pós-remediação:**
  - `/actuator/health` → `UP` (todos componentes)
  - Smoke tests: `POST /api/auth/login`, `GET /api/ativos?page=0&size=10`, `GET /api/manutencoes?status=SOLICITADA` — todos 200 < 500 ms
  - Taxa 5xx < 0.1% por 10 min consecutivos
  - Job health check: `lastRun` < 5 min, `assetsProcessed` > 0
* **Severidade típica:** P0 (indisponibilidade total) ou P1 (degradação severa)

### Incident Type 2: Job de Health Check de Ativos não roda / Alertas não gerados
* **Symptom:**
  - Métrica custom `aegis.healthcheck.lastRun` > 30 min (Deployment Plan §7)
  - Métrica `aegis.healthcheck.assetsProcessed` = 0 por 2 execuções
  - Alerta `HealthCheckJobStuck` disparado no Alertmanager
  - Usuários não recebem notificações de degradação de ativos
* **Possíveis Causas:**
  1. `AlertNotificationService.checkResourceUsageAlerts` (CC 17) travado em loop ou exceção silenciosa
  2. `@Scheduled` não executando (thread pool `taskScheduler` esgotado — Spring Boot default: 1 thread)
  3. Query de busca de ativos para verificação muito lenta (timeout DB)
  4. Falha no envio SMTP (credenciais expiradas, servidor indisponível) — Deployment Plan §8
  5. Feature flag `health-check.enabled` OFF acidentalmente (Togglz) — Deployment Plan §3
* **Diagnóstico:**
  1. `kubectl logs -l app=aegis-patrimonio --since=1h | grep -i "healthcheck\|AlertNotificationService\|checkResourceUsageAlerts"`
  2. Verificar `/actuator/scheduledtasks` — próxima execução, última execução, duração
  3. Verificar `/actuator/metrics/aegis.healthcheck.lastRun` e `.assetsProcessed`
  4. Checar Togglz console / `/actuator/features` — flag `health-check.enabled` = ON?
  5. Testar SMTP: `kubectl exec -it <pod> -- nc -zv smtp.example.com 587` + credenciais no Vault
  6. `EXPLAIN ANALYZE` na query de busca de ativos para health check (ver `AtivoRepository` / `AtivoSpecification`)
* **Remediation:**
  1. **Se thread pool esgotado:** Aumentar `spring.task.scheduling.pool.size` (ex: 5) no ConfigMap + rollout restart
  2. **Se exceção em `checkResourceUsageAlerts`:** Corrigir código (quebrar método CC 17) + deploy hotfix (Deployment Plan §3 — cadência patch contínua)
  3. **Se SMTP falha:** Rotacionar credenciais no Vault (Deployment Plan §8 — procedimento manual documentado) + reiniciar pods para recarregar
  4. **Se flag OFF:** Ativar via Togglz console (imediato, sem deploy) — Deployment Plan §3 kill switch
  5. **Workaround temporário:** Trigger manual via `POST /api/admin/health-check/trigger` (se endpoint existir) ou `kubectl exec -it <pod> -- curl -X POST localhost:8080/actuator/healthcheck/trigger`
* **Verificação pós-remediação:**
  - `aegis.healthcheck.lastRun` < 5 min
  - `aegis.healthcheck.assetsProcessed` > 0 (condizente com total de ativos)
  - `aegis.healthcheck.alertsGenerated` ≥ 0 (sem erro de envio)
  - Próxima execução agendada visível em `/actuator/scheduledtasks`
* **Severidade típica:** P1 (falha silenciosa de monitoramento de negócio — risco de ativos degradados não detectados)

### Incident Type 3: Latência P95 > 1000 ms sustentada / Timeout em endpoints de listagem
* **Symptom:**
  - Alerta `HighLatencyP95` (Prometheus: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{uri=~"/api/ativos|/api/manutencoes|/api/relatorios/ativos"}[5m])) > 1`)
  - Usuários relatam lentidão ao carregar listas paginadas, relatórios
  - `/actuator/health` ainda `UP` mas `customHealthCheck` pode mostrar `DEGRADED`
* **Possíveis Causas:**
  1. `AtivoService` TODO perf: carrega 1000 candidatos (id+nome) e faz ranking em memória (linha 119) — diagnóstico AST
  2. `ManutencaoSpecification.build()` (CC 14) gerando SQL ineficiente com muitos predicados
  3. Índices faltando em colunas de filtro frequente: `ativo.status`, `manutencao.status`, `manutencao.data_solicitacao`, `filial_id`
  4. Pool de conexões DB saturado (queries lentas seguram conexões)
  5. GC pressure / JVM heap alto (ver Incident Type 1)
  6. Frontend: `api.js request()` (CC 13) fazendo requisições sequenciais desnecessárias
* **Diagnóstico:**
  1. Dashboard "Aegis - Business Metrics": latência por endpoint (`/api/ativos`, `/api/manutencoes`, `/api/relatorios/ativos`)
  2. `/actuator/metrics/http.server.requests` com tag `uri` — identificar endpoints lentos
  3. `EXPLAIN ANALYZE` nas queries geradas por `ManutencaoSpecification` e `AtivoService` ranking
  4. Verificar índices: `\d+ ativo`, `\d+ manutencao` (PostgreSQL) / `SHOW INDEX FROM ativo` (MySQL)
  5. `kubectl top pods -l app=aegis-patrimonio` — CPU/Memória
  6. Frontend: Network tab — verificar se `api.js` faz N+1 requests em listagens
* **Remediation:**
  1. **Curto prazo (hotfix):** Aumentar `spring.datasource.hikari.maximum-pool-size` + `spring.task.execution.pool.max-size` se processamento assíncrono
  2. **Curto prazo:** Ativar cache em listagens frequentes (`@Cacheable` em `AtivoService.findAll`, `ManutencaoService.findByStatus`) — feature flag `cache.enabled`
  3. **Médio prazo:** Refatorar `AtivoService` ranking (TODO linha 119) — mover para query SQL com window function ou buscar apenas IDs e hidratar em batch
  4. **Médio prazo:** Otimizar `ManutencaoSpecification` — quebrar método CC 14, evitar `OR` excessivo, usar índices compostos
  5. **Médio prazo:** Adicionar índices: `CREATE INDEX idx_manutencao_status_data ON manutencao(status, data_solicitacao); CREATE INDEX idx_ativo_filial_status ON ativo(filial_id, status);`
  6. **Frontend:** Refatorar `api.js request()` (CC 13) — quebrar em funções menores, usar `Promise.all` para requests paralelos
  7. **Se crítico e causa não resolvida em 15 min:** Blue-Green rollback (Deployment Plan §5)
* **Verificação pós-remediação:**
  - P95 < 500 ms por 15 min consecutivos (Deployment Plan §7)
  - Throughput requests/s dentro de baseline ±10%
  - Pool DB: conexões ativas < 70% do max
* **Severidade típica:** P1 (degradação severa de performance) ou P2 (lentidão perceptível mas funcional)

### Incident Type 4: Falha de autenticação / JWT inválido em massa
* **Symptom:**
  - Pico de `401 Unauthorized` em `/api/**`
  - `/actuator/health` `UP` mas usuários não conseguem logar (`POST /api/auth/login` falha)
  - Logs: `JWT signature validation failed`, `Expired JWT`, `Invalid key`
* **Possíveis Causas:**
  1. Chaves RSA rotacionadas no Vault mas pods não reiniciados (Spring carrega chaves no startup) — Deployment Plan §8
  2. Relógio do servidor dessincronizado (NTP) — `exp`/`iat` inválidos
  3. Chave pública/privada mismatch (rotação parcial)
  4. `BCrypt` falha (cost factor alterado, hash corrompido)
* **Diagnóstico:**
  1. `kubectl logs -l app=aegis-patrimonio | grep -i "jwt\|jws\|signature\|expired"`
  2. Verificar Vault: `vault kv read secret/aegis/rsa-keys` — `private-key`, `public-key`, `rotation-date`
  3. `kubectl exec -it <pod> -- date` + `timedatectl` — sincronismo NTP
  4. `/actuator/env` — verificar `security.jwt.public-key` e `private-key` carregados (mascarados)
  5. Testar login manual: `curl -X POST /api/auth/login -d '{"username":"test","password":"test"}'`
* **Remediation:**
  1. **Se chaves rotacionadas sem restart:** `kubectl rollout restart deployment aegis-patrimonio` (rolling update, ~2-3 min) — Deployment Plan §5 mecanismo 3
  2. **Se NTP dessincronizado:** Corrigir no node/cluster (DaemonSet `ntp-sync` ou cloud provider) + restart pods
  3. **Se mismatch:** Verificar Vault — ambas chaves devem ser do mesmo par; se não, restaurar par anterior + restart
  4. **Emergência:** Desativar validação JWT temporariamente (feature flag `auth.jwt.validation.enabled`=OFF) — **apenas último recurso, reativar em < 30 min**
* **Verificação pós-remediação:**
  - `POST /api/auth/login` → 200 + JWT válido (decodificar em jwt.io)
  - Tokens existentes ainda válidos (se rotação planejada) ou novo login funciona
  - Nenhum 401 espúrio por 10 min
* **Severidade típica:** P0 (negado de acesso total) ou P1 (parcial — alguns usuários/tokens)

### Incident Type 5: Deploy falha / Smoke tests falham em Staging/Produção
* **Symptom:**
  - Pipeline GitHub Actions falha no Stage 4 (Staging) ou Stage 5 (Produção)
  - Smoke tests pós-deploy falham (Deployment Plan §4, §7)
  - Health check `/actuator/health` não sobe para `UP` em < 5 min
* **Possíveis Causas:**
  1. Migração Flyway/Liquibase falhou (schema incompatível, lock, checksum mismatch)
  2. Configuração de perfil errada (`spring.profiles.active` não bate com ambiente)
  3. Secrets não injetados (Vault → K8s Secrets → env vars) — Deployment Plan §8
  4. Imagem Docker corrompida / tag errada
  5. Frontend build falhou (assets estáticos ausentes) — `frontend/src/services/api.js` console.error/debug residuais
  6. Dependência de inicialização não disponível (DB, Vault, SMTP)
* **Diagnóstico:**
  1. Logs do pipeline GitHub Actions (Stage 3 Build, Stage 4 Deploy)
  2. `kubectl describe pod -l app=aegis-patrimonio` — Events, InitContainers, Readiness/Liveness probes
  3. `kubectl logs <pod> -c init-migration` (se sidecar de migração) ou logs do container principal
  4. Verificar `kubectl get secrets aegis-secrets -o yaml` — chaves presentes?
  5. `curl -v https://staging.aegispatrimonio.example.com/actuator/health` (ou prod)
* **Remediation:**
  1. **Se migração falhou:** Rollback DB (Flyway `undo` / Liquibase `rollback` — Deployment Plan §5 mecanismo 4) + rollback deploy (Blue-Green switch)
  2. **Se secrets faltando:** Verificar Vault → External Secrets Operator / CSI driver sync + `kubectl rollout restart`
  3. **Se perfil errado:** Corrigir `spring.profiles.active` no Deployment/ConfigMap + rollout restart
  4. **Se imagem errada:** `kubectl set image deployment/aegis-patrimonio aegis-patrimonio=ghcr.io/{org}/aegis-patrimonio:v{previous-tag}` (Deployment Plan §5 mecanismo 3)
  5. **Se frontend build falhou:** Verificar `frontend/src/services/api.js` — remover `console.error` (linha 44) e `console.debug` (linha 107) antes de build produção
* **Verificação pós-remediação:**
  - Pipeline verde (Stages 1-4 para staging, 1-5 para prod)
  - Smoke tests todos verdes (Deployment Plan §7 checklist)
  - Métricas de negócio dentro do esperado (§7)
* **Severidade típica:** P0 (deploy prod falhou — release bloqueado) ou P1 (staging falhou — bloqueia próximo release)

## 5. Backup & Rollback Protocol
> **Nota:** Estratégia de backup/RPO/RTO do banco de dados vive em `db-performance-recovery.md` (se existir) — não duplicar aqui. Esta seção cobre rollback de aplicação/deploy.

* **Rollback de deploy (Aplicação):**
  1. **Mecanismo preferido (Blue-Green — < 30s):** `kubectl patch ingress aegis-patrimonio -p '{"spec":{"rules":[{"host":"aegispatrimonio.example.com","http":{"paths":[{"path":"/","pathType":"Prefix","backend":{"service":{"name":"aegis-patrimonio-blue","port":{"number":8080}}}}]}}]}}'` (trocar `blue` ↔ `green`) — Deployment Plan §5 mecanismo 1
  2. **Feature Flag kill switch (imediato, sem redeploy):** Desativar flag da feature problemática via Togglz console ou `PATCH /actuator/features/{featureName} {"enabled":false}` — Deployment Plan §5 mecanismo 2
  3. **Reverter tag Docker (Rolling update — ~2-3 min):** `kubectl set image deployment/aegis-patrimonio aegis-patrimonio=ghcr.io/{org}/aegis-patrimonio:v{previous-tag}` — Deployment Plan §5 mecanismo 3
  4. **Database migration rollback:** Flyway `undo` ou Liquibase `rollback <tag>` — **apenas se migração foi aplicada no deploy e é reversível** — Deployment Plan §5 mecanismo 4
* **Tempo alvo de rollback (RTO):** **< 5 minutos** (alinhado ao NFR de disponibilidade e Deployment Plan §5, Test Strategy §7)
* **Backup de configuração/infraestrutura (fora do banco):**
  - **Kubernetes manifests:** Versionados no repo (GitOps) — `k8s/` ou `helm/` (não detectado no diagnóstico, inferido)
  - **ConfigMaps/Secrets (não-secrets):** `application-{profile}.yml` versionado no repo + ConfigMap K8s para overrides — Deployment Plan §8
  - **Secrets:** Gerenciados no Vault (backup automático Vault Raft/snapshot) — Deployment Plan §8
  - **Certificados TLS:** cert-manager + Let's Encrypt (renovação automática) — Deployment Plan §8
  - **Feature Flags state:** Togglz armazena em banco (tabela `togglz_feature`) — backup junto com DB

## 6. Maintenance Windows
* **Janela padrão:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Domingos 02:00–04:00 BRT (UTC-3) — alinhado a Deployment Plan §3 janela deploy prod terça-quinta 10:00-16:00 BRT; manutenção de infra/banco fora do horário comercial]
* **Comunicação prévia necessária:** 
  - **Manutenção programada (infra, DB, K8s upgrade):** Aviso 48h antecipado no canal `#aegis-alerts` + e-mail stakeholders + calendário compartilhado
  - **Deploy produção (padrão):** Não requer aviso prévio além do processo de aprovação (Deployment Plan §6 — Eng Lead + QA Lead, bake time 2h staging)
  - **Hotfix/emergência:** Comunicação imediata no `#aegis-alerts` + issue no tracker com tag `hotfix` — Deployment Plan §6

## 7. Runbook de Rotina (Checklists Operacionais)
- [ ] **Diário (início do dia útil):** Verificar dashboards "Aegis - Overview", "Aegis - Business Metrics" — confirmar health checks verdes, latência P95 < 500 ms, taxa erro < 0.1%, job health check `lastRun` < 15 min
- [ ] **Diário (fim do dia):** Revisar alertas disparados nas últimas 24h no Alertmanager/PagerDuty — confirmar resolvidos ou com owner atribuído
- [ ] **Semanal:** Revisão de alertas silenciados / inibidos — remover silenciamentos expirados, validar se regras ainda fazem sentido
- [ ] **Semanal:** Verificar rotação de chaves RSA (JWT) — Deployment Plan §8: a cada 90 dias; confirmar última rotação no Vault
- [ ] **Semanal:** Verificar expiração certificados TLS — `kubectl get certificates -A` (cert-manager) — Deployment Plan §8
- [ ] **Mensal:** Revisão de custos de infraestrutura (K8s nodes, DB, Vault, observabilidade) — relatório para Eng Manager
- [ ] **Mensal:** Testar procedimento de rollback Blue-Green em staging (simular switch) — validar < 5 min
- [ ] **Mensal:** Validar backup/restore Vault (snapshot) e DB (conforme `db-performance-recovery.md`)
- [ ] **Trimestral:** Rotação de credenciais DB, SMTP, API keys (procedimento manual documentado no Confluence) — Deployment Plan §8
- [ ] **Por release (conforme Deployment Plan §7 Post-Deploy Verification):**
  - [ ] Health checks verdes: `/actuator/health`, `/actuator/info`, `/actuator/metrics`
  - [ ] Métricas de negócio dentro do esperado (throughput, erro, P95, job health check)
  - [ ] Nenhum erro crítico em 30 min (Logs, APM, Alertmanager silencioso)
  - [ ] Smoke tests E2E críticos verdes (Playwright < 5 min)
  - [ ] Comunicação de release enviada (changelog + Slack `#aegis-releases` + tag Git `v{X.Y.Z}`)
  - [ ] Rollback plan confirmado (versão anterior marcada `rollback-candidate`, flags novas OFF)

## 8. Post-Incident Process
* **Postmortem obrigatório para:** Severidade **P0** (indisponibilidade total, perda de dados, segurança) e **P1** (degradação severa, funcionalidade crítica impactada)
* **Template de postmortem:** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Confluence "Aegis Postmortem Template" — seções: Resumo Executivo, Linha do Tempo (com timestamps UTC), Impacto (usuários, receita, SLA), Causa Raiz (5 Whys), Ações Corretivas Imediatas, Ações Preventivas (com owner e prazo), Lições Aprendidas, Métricas de Detecção/Resolução (MTTD, MTTR)]
* **Prazo para publicar postmortem:** **5 dias úteis** após resolução do incidente (alinhado a práticas SRE padrão)
* **Revisão de postmortems:** Mensal na reunião de "Incident Review" — acompanhar ações preventivas abertas, fechar as concluídas, escalar as atrasadas

## 9. Contacts & Escalation Paths
| Sistema/Serviço | Responsável | Contato de Emergência |
| :--- | :--- | :--- |
| **Aplicação Aegis Patrimônio (Backend/Frontend)** | Tech Lead Backend / Tech Lead Frontend | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack DM @tech-lead-backend, @tech-lead-frontend / PagerDuty schedule "Aegis-App"] |
| **Kubernetes Cluster / Infraestrutura** | Platform Team / SRE | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack #platform-oncall / PagerDuty schedule "Platform-Infra"] |
| **Banco de Dados** | DBA / Platform Team | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @dba-oncall / PagerDuty schedule "Database"] |
| **HashiCorp Vault / Secrets** | Security / Platform Team | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @security-oncall / PagerDuty schedule "Security-Secrets"] |
| **Observabilidade (Prometheus, Grafana, Loki, Alertmanager)** | SRE / Observability Team | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack #observability-oncall / PagerDuty schedule "Observability"] |
| **CI/CD (GitHub Actions, GHCR)** | DevOps / Platform Team | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @devops-oncall / PagerDuty schedule "CI-CD"] |
| **DNS / TLS / Certificados** | Platform Team / NetOps | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @netops-oncall / PagerDuty schedule "Network"] |
| **SMTP / Notificações** | Platform Team / Infra | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @infra-oncall / PagerDuty schedule "Infra-Email"] |
| **Stakeholders / Product Owner** | Product Owner / Eng Manager | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @po-aegis, @eng-manager / E-mail: po@empresa.com, eng-manager@empresa.com] |
| **Compliance / LGPD / Auditoria** | Security / Legal | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: Slack @compliance / E-mail: compliance@empresa.com] |

---

## Apêndice: Referências Cruzadas & Validações Pendentes

| Item | Origem | Status | Ação Necessária |
| :--- | :--- | :--- | :--- |
| Ferramenta de CI/CD (GitHub Actions) | Deployment Plan Header | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Confirmar com time DevOps |
| Build Tool (Maven/Gradle) | Deployment Plan §3 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Verificar `pom.xml` ou `build.gradle` no repo |
| Orquestração (Kubernetes) | Deployment Plan §4, §5 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Confirmar cluster (EKS/GKE/AKS) e namespace |
| Banco de Dados (Engine, Flyway/Liquibase) | Deployment Plan §5, §8 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Diagnosticar dependências reais (não detectadas no package.json) |
| Observabilidade Stack (Prometheus/Grafana/Loki/Tempo vs Datadog) | Deployment Plan §7, §8 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Definir stack padrão da empresa |
| URLs de Ambiente (staging/prod) | Deployment Plan §4 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Registrar DNS, certificados, Ingress |
| Feature Flags (Togglz) | Deployment Plan §3 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Confirmar biblioteca e configuração |
| Secret Manager (Vault) | Deployment Plan §8 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Confirmar instância Vault, políticas, audit log |
| Rotação de Chaves RSA (90 dias) | Deployment Plan §8 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Implementar script cron + Vault API |
| Métricas Custom (health check job) | Deployment Plan §7 | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Implementar Micrometer metrics em `AlertNotificationService` |
| Endpoint Trigger Manual Health Check | Incident Type 2 Remediation | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Criar endpoint `POST /api/admin/health-check/trigger` ou actuator custom |
| Cache em Listagens (Feature Flag) | Incident Type 3 Remediation | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` | Implementar `@Cacheable` + flag `cache.enabled` |
| Refatoração `AtivoService` TODO (perf) | Diagnóstico AST + Incident Type 3 | **Real — código existente** | Priorizar: quebrar ranking em query SQL / batch |
| Refatoração `ManutencaoSpecification` (CC 14) | Diagnóstico AST + Incident Type 3 | **Real — código existente** | Quebrar método, otimizar predicados |
| Refatoração `AlertNotificationService` (CC 17) | Diagnóstico AST + Incident Type 2 | **Real — código existente** | Quebrar `checkResourceUsageAlerts` em métodos menores |
| Refatoração `AtivoMapper.toDTO` (CC 14) | Diagnóstico AST | **Real — código existente** | Simplificar mapeamento, evitar recursão |
| Refatoração `RealisticDataSeeder.run` (CC 15) | Diagnóstico AST | **Real — código existente** | Quebrar seeder em métodos por entidade |
| Refatoração `api.js request()` (CC 13) | Diagnóstico AST | **Real — código existente** | Quebrar em funções menores, remover console.* |
| Remover `console.error/debug` em `api.js` | Diagnóstico AST | **Real — código existente** | Remover antes de build produção |
| Corrigir `Usuario.setUsername` stub vazio | Diagnóstico AST | **Real — código existente** | Implementar setter ou remover se desnecessário |

> **Todos os itens marcados com `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` devem ser revisados e confirmados pelo time de Engenharia/DevOps/SRE antes da implementação deste runbook em produção.**