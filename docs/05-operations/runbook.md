# Operations Runbook & System Maintenance Guide — Aegis Patrimônio

> **Owner:** SRE/DevOps/Eng On-call · **Última revisão:** 15/01/2025
> **Sistema de alerta:** PagerDuty/Opsgenie · **Canal de incidentes:** #aegis-alerts no Slack/Teams

## 1. System Overview
* **Componentes críticos:**
  - **Backend API:** Spring Boot (Java 21 LTS) — 335 arquivos em `src/main/java/br/com/aegispatrimonio/` — JAR executável `aegis-patrimonio-{version}.jar`
  - **Frontend:** Vue.js 3 + Bootstrap 5 + Pinia + Vite — assets estáticos servidos pelo backend (`src/main/resources/static`)
  - **Banco de dados:** MySQL 8.0 (produção) — Flyway para migrações
  - **Job scheduler:** Spring `@Scheduled` para health checks de ativos — ver `AlertNotificationService.checkResourceUsageAlerts`
  - **Autenticação/Autorização:** JWT HS256/RS256 (expiração 1h, refresh 7 dias) + BCrypt para senhas
  - **Feature Flags:** Togglz — config via `application.yml`
* **Diagrama de arquitetura:** Ver `system-architecture.md` — referência ao Deployment Plan §4 (Environments) e §2 (Pipeline Stages)
* **Dependências externas críticas:**
  - **SMTP:** Envio de notificações de alerta (credenciais rotacionadas a cada 90 dias — Deployment Plan §8)
  - **Container Registry:** GHCR (`ghcr.io/{org}/aegis-patrimonio:*`) — Deployment Plan §3
  - **Secret Manager:** HashiCorp Vault (produção) + GitHub Actions Secrets (CI) + K8s Secrets (runtime) — Deployment Plan §8
  - **Observabilidade:** Prometheus + Grafana + Loki + OpenTelemetry Java Agent — Deployment Plan §7, §8

## 2. Monitoring & System Health Check
| Métrica | Ferramenta | Threshold Saudável | Threshold de Alerta |
| :--- | :--- | :--- | :--- |
| Latência P95 (endpoints críticos) | Prometheus/Grafana | < 200 ms (conforme NFR-P04) | > 500 ms sustentado por 5 min |
| Taxa de erro HTTP 5xx | Prometheus/Alertmanager | < 0.1% | > 1% em 5 min (gatilho de rollback) |
| Saturação CPU (container) | cAdvisor/Prometheus | < 70% | > 85% sustentado |
| Saturação Memória (JVM Heap) | Micrometer/JVM Exporter | < 70% Heap usado | > 85% Heap usado / GC pause > 500 ms |
| Latência P99 `/actuator/health` | Prometheus | < 100 ms | > 500 ms |
| Job Health Check (`lastRun`) | Micrometer custom metric / Actuator | < 15 min atrás | > 30 min sem execução |
| Assets processados no health check | Micrometer custom metric | > 0 por execução | = 0 por 2 execuções consecutivas |
| Alertas gerados (últimas 24h) | Micrometer custom metric | ≥ 0 (esperado) | Pico anômalo > 3x baseline |
| Fila de migrações Flyway pendentes | Flyway API / Actuator | 0 | > 0 em produção |

* **Dashboards principais:** Grafana "Aegis Patrimônio - Overview", "Aegis - JVM", "Aegis - Business Metrics", "Aegis - Deployments"
* **Health check endpoint:** `GET /actuator/health` (retorna `UP`/`DOWN`/`DEGRADED` + componentes: `db`, `diskSpace`, `ping`, `customHealthCheck` para jobs de ativos)
* **Métricas específicas de banco:** Ver `db-performance-recovery.md`. Métricas mínimas: conexões ativas/pool, latência query P95, locks, replication lag se réplica

## 3. On-Call & Escalation
| Nível | Papel | Tempo de resposta esperado | Critério de acionamento |
| :--- | :--- | :--- | :--- |
| L1 | On-call Engineer (rota semanal) | 15 min | Alerta PagerDuty/Opsgenie disparado (thresholds §2) |
| L2 | Tech Lead / Senior Backend | 30 min | L1 não resolve em 30 min OU incidente P0/P1 (múltiplos serviços impactados, perda de dados, segurança) |
| L3 | Engineering Manager / Incident Commander | 1 h | Incidente se estende > 1 h OU requer decisão de rollback de banco/dados OU comunicação externa (clientes, compliance) |

> **Rotação on-call:** Semanal, handoff segunda-feira 10:00 BRT, documentado no Confluence "Aegis On-call Rotation"
> **Escalação automática:** PagerDuty escalation policy: L1 → L2 após 15 min sem ack → L3 após 30 min sem resolução

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
  5. Migração Flyway travada/bloqueando startup
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
  4. **Se migração travada:** `kubectl exec -it <pod> -- flyway info`; se lock: `DELETE FROM flyway_schema_history WHERE installed_rank = (SELECT max(installed_rank) FROM flyway_schema_history WHERE success = false);` — **cuidado: apenas se migração falhou, não se está rodando**
  5. **Rollback imediato (se causa não identificada em 10 min):** Blue-Green switch para versão anterior (Deployment Plan §5, mecanismo 1) — alvo < 5 min
* **Verificação pós-remediação:**
  - `/actuator/health` → `UP` (todos componentes)
  - Smoke tests: `POST /api/auth/login`, `GET /api/ativos?page=0&size=10`, `GET /api/manutencoes?status=SOLICITADA` — todos 200 < 500 ms
  - Taxa 5xx < 0.1% por 10 min consecutivos
  - Job health check: `lastRun` < 5 min, `assetsProcessed` > 0
* **Severidade típica:** P0 (indisponibilidade total) ou P1 (degradação severa)

### Incident Type 2: Job de Health Check de Ativos falha / não executa
* **Symptom:**
  - Métrica `aegis.asset.healthcheck.lastRun` > 30 min
  - Métrica `aegis.asset.healthcheck.assetsProcessed` = 0 por 2 execuções consecutivas
  - Alerta: `AegisAssetHealthCheckStale` / `AegisAssetHealthCheckZeroProcessed`
* **Possíveis Causas:**
  1. `@Scheduled` não disparou (thread pool executor saturado / JVM paused)
  2. `AlertNotificationService.checkResourceUsageAlerts` lançou exceção não tratada (CC 17 — blast radius total)
  3. Query de busca de ativos para health check travou (lock DB / plano de execução ruim)
  4. SMTP falhou ao enviar notificação — exception propagou e matou o job
* **Diagnóstico:**
  1. Logs da aplicação: `grep "checkResourceUsageAlerts" /var/log/aegis/*.log`
  2. Verificar `ThreadPoolTaskScheduler` pool size vs active threads: `/actuator/metrics/executor.active` / `executor.queued`
  3. DB: `SHOW PROCESSLIST` / `pg_stat_activity` — buscar queries de `ativo` com `state = 'active'` há > 30s
  4. Verificar conectividade SMTP: `telnet smtp.corp 587` / logs de `JavaMailSender`
* **Remediation:**
  1. **Se thread pool saturado:** Aumentar `spring.task.scheduling.pool.size` (default 1) para ≥ 4; rollout restart
  2. **Se exceção no serviço:** Hotfix isolado em `AlertNotificationService` — try/catch por ativo individual + log estruturado + métrica de erro; deploy canary
  3. **Se query travada:** `EXPLAIN ANALYZE` da query de health check; adicionar índice composto `(filial_id, status, updated_at)`; kill query longa (`pg_terminate_backend` / `KILL QUERY`)
  4. **Se SMTP falhou:** Desacoplar envio de e-mail do job principal — usar `@Async` + fila resiliente (Redis/RabbitMQ) ou pelo menos try/catch isolado
* **Verificação pós-remediação:**
  - Próxima execução agendada: `lastRun` < 5 min, `assetsProcessed` > 0
  - Métrica `aegis.asset.healthcheck.errors` = 0 por 3 execuções
  - Alertas de disco/CPU/memória gerados corretamente para ativos conhecidos
* **Severidade típica:** P2 (degradação de funcionalidade preditiva, não bloqueia CRUD)

### Incident Type 3: Lentidão severa em endpoints de listagem/filtro de Ativos
* **Symptom:**
  - P95 `GET /api/ativos` > 1000 ms
  - Alerta: `AegisApiLatencyHigh` (Prometheus rule)
  - Usuários relatam timeout na UI (frontend `api.js:request` timeout default)
* **Possíveis Causas:**
  1. `ManutencaoSpecification.build()` (CC 14) gerando query com muitos JOINs/ORs — plano de execução ruim
  2. Falta de paginação obrigatória — `findAll` carregando 10k+ registros (RISK-010)
  3. N+1 em `AtivoMapper.toDTO` (CC 14) — lazy loading de relacionamentos (filial, departamento, localização, tipo, fornecedor)
  4. Índices ausentes em colunas de filtro frequentes (`filial_id`, `status`, `tipo_ativo_id`, `localizacao_id`)
  5. Pool HikariCP saturado (máx 50) — queries lentas segurando conexões
* **Diagnóstico:**
  1. `/actuator/metrics/hikaricp.connections.active` / `hikaricp.connections.idle` / `hikaricp.connections.pending`
  2. `EXPLAIN ANALYZE` da query gerada pelo Specification (ativar `spring.jpa.show-sql=true` temporário)
  3. Verificar `pg_stat_statements` / `sys.schema_table_statistics` para top queries por tempo total
  4. Heap dump se suspeita de OOM durante mapeamento
* **Remediation:**
  1. **Imediato:** Impor `Pageable` em todos `findAll` (default size=50, max=200) — patch via `@Query` nativo com `LIMIT` se necessário
  2. **Curto prazo:** Adicionar índices compostos: `(filial_id, status)`, `(filial_id, tipo_ativo_id)`, `(localizacao_id, status)`
  3. **Curto prazo:** `AtivoMapper` — usar `EntityGraph` / `JOIN FETCH` para evitar N+1; ou migrar para MapStruct com fetch strategy
  4. **Médio prazo:** Refatorar `ManutencaoSpecification` — extrair builders por domínio (RISK-002)
  5. **Cache:** Adicionar `@Cacheable` em `AtivoService.listarTodos` / `buscarPorId` (Caffeine/Redis) — invalidação em `criar`/`atualizar`/`deletar`
* **Verificação pós-remediação:**
  - P95 `GET /api/ativos` < 200 ms por 30 min
  - Conexões ativas HikariCP < 30 em carga normal
  - Zero N+1 detectado em logs (ativar `hibernate.generate_statistics=true` temporário)
* **Severidade típica:** P1 (impacta todos usuários, mas sistema funcional)

### Incident Type 4: Falha de autenticação / autorização em massa
* **Symptom:**
  - Pico de 401/403 em `/api/**`
  - Usuários não conseguem logar ou recebem "Acesso negado" em operações permitidas
  - Alerta: `AegisAuthFailureRateHigh`
* **Possíveis Causas:**
  1. Chave JWT (HS256/RS256) rotacionada sem redeploy — assinatura inválida
  2. Relógio do servidor dessincronizado (NTP) — token expirado/iat no futuro
  3. `Usuario.setUsername` stub vazio (linha 86) — atualização de credencial corrompe estado
  4. Role/Permission cache desatualizado (se houver cache de autorização)
  5. Banco indisponível — `UserDetailsService.loadUserByUsername` falha
* **Diagnóstico:**
  1. Verificar `/actuator/health` componente `db`
  2. Logs: `JwtAuthenticationFilter` / `SecurityConfig` — buscar `io.jsonwebtoken.ExpiredJwtException`, `SignatureException`, `MalformedJwtException`
  3. `date` / `timedatectl` no pod — offset > 1s vs NTP
  4. Vault: versão da chave `jwt-signing-key` vs `jwt-verification-key` em uso
* **Remediation:**
  1. **Se clock skew:** `chronyc makestep` / reiniciar `systemd-timesyncd`; rollout restart pods
  2. **Se chave rotacionada:** Rollout restart para recarregar chaves; ou configurar `@Scheduled` reload de chaves públicas (JWKS endpoint)
  3. **Se `setUsername` stub:** Hotfix — implementar validação + auditoria; deploy emergencial
  4. **Se DB down:** Verificar incidente Type 1
* **Verificação pós-remediação:**
  - Login bem-sucedido: `POST /api/auth/login` → 200 + JWT válido
  - Operação Admin: `POST /api/departamentos` (com token Admin) → 201
  - Operação User: `GET /api/ativos` (com token User) → 200
  - Taxa 401/403 < 0.5% por 10 min
* **Severidade típica:** P0 (ninguém acessa o sistema) ou P1 (parcial)

### Incident Type 5: Deploy falhou / Rollback necessário
* **Symptom:**
  - Pipeline CI/CD falhou em stage `deploy` ou `smoke-test`
  - Nova versão não passa health check `/actuator/health` em 5 min
  - Métricas de erro disparam após deploy
* **Remediation (Blue-Green):**
  1. **Não** fazer `kubectl rollout undo` — usa Blue-Green (Deployment Plan §5)
  2. Verificar se Green (nova versão) está em namespace `aegis-green` / service `aegis-patrimonio-green`
  3. Switch traffic: `kubectl patch service aegis-patrimonio -p '{"spec":{"selector":{"version":"blue"}}}'` (ou via Ingress/Argo Rollouts)
  4. Verificar Blue (versão anterior): `/actuator/health` → `UP`, smoke tests OK
  5. Marcar Green como `failed` no Argo CD / GitHub Actions; investigar causa raiz offline
* **Tempo alvo:** < 5 min para switch completo
* **Severidade típica:** P0 se deploy em produção, P1 se em staging

## 5. Routine Maintenance Procedures

### Daily (Automatizado via CronJob / GitHub Actions)
- [ ] Verificar execução do job de health check de ativos (`lastRun` < 24h, `assetsProcessed` > 0)
- [ ] Verificar migrações Flyway pendentes (`flyway info` — 0 pendentes em prod)
- [ ] Verificar backup incremental (últimas 24h) — bucket S3 WORM
- [ ] Verificar alertas ativos no Alertmanager — nenhum P0/P1 aberto > 1h
- [ ] Verificar uso de disco nos PVCs (PostgreSQL, logs) — < 80%

### Weekly
- [ ] Revisar dashboards: tendência de latência P95, taxa de erro, saturação recursos
- [ ] Revisar logs de auditoria (Hibernate Envers) — amostragem 10% operações de escrita
- [ ] Testar restore de backup (staging) — validar integridade + tempo de restore < RTO (1h)
- [ ] Rotação on-call handoff (segunda 10:00 BRT) — atualizar PagerDuty schedule
- [ ] Verificar expiração de certificados TLS (Let's Encrypt / corporativo) — > 30 dias

### Monthly
- [ ] Revisão de capacity planning: CPU/RAM/Disco vs crescimento de ativos (meta 10k+)
- [ ] Revisão de índices DB: `pg_stat_user_indexes` — remover não usados, adicionar faltantes
- [ ] Rotação de segredos: JWT keys (90 dias), SMTP credentials (90 dias), DB passwords (90 dias) — Deployment Plan §8
- [ ] Atualização de dependências: `dependabot` PRs — merge após CI verde + testes integração
- [ ] Revisão de Risk Register — atualizar status, fechar mitigados, adicionar novos
- [ ] Simulação de incidente (Game Day) — exercitar runbook Type 1 + Type 5

### Quarterly
- [ ] Revisão de arquitetura: ADRs pendentes, decisões técnicas, dívida técnica (SonarCloud)
- [ ] Teste de carga (k6) — validar NFR-P04 (< 200ms P95), NFR-S06 (1k+ usuários simultâneos)
- [ ] Penetration test / OWASP ZAP scan completo — corrigir findings High/Critical
- [ ] Revisão de compliance LGPD/SOX: auditoria imutável, direito ao esquecimento, retenção 5 anos
- [ ] Atualização de runbook: incorporar lições aprendidas de incidentes reais

## 6. Useful Commands Reference

### Kubernetes / Deploy
```bash
# Status deploy
kubectl get pods -n aegis -l app=aegis-patrimonio
kubectl rollout status deployment/aegis-patrimonio -n aegis

# Blue-Green switch
kubectl patch service aegis-patrimonio -n aegis -p '{"spec":{"selector":{"version":"blue"}}}'

# Logs
kubectl logs -n aegis -l app=aegis-patrimonio --tail=200 -f

# Exec no pod
kubectl exec -n aegis -it <pod> -- bash

# Scale (emergência)
kubectl scale deployment aegis-patrimonio -n aegis --replicas=0
kubectl scale deployment aegis-patrimonio -n aegis --replicas=2
```

### Banco de Dados (MySQL)
```bash
# Conectar
kubectl exec -n aegis -it <mysql-pod> -- mysql -u root -p

# Verificar conexões
SHOW PROCESSLIST;
SELECT * FROM performance_schema.threads WHERE TYPE='FOREGROUND';

# Flyway
kubectl exec -n aegis -it <app-pod> -- flyway info
kubectl exec -n aegis -it <app-pod> -- flyway repair

# Backup manual
kubectl exec -n aegis -it <mysql-pod> -- mysqldump -u root -p aegis_patrimonio > backup_$(date +%F).sql
```

### JVM / Debug
```bash
# Heap dump
kubectl exec -n aegis -it <pod> -- jcmd <pid> GC.heap_dump /tmp/heap.hprof
kubectl cp aegis/<pod>:/tmp/heap.hprof ./heap.hprof

# Thread dump
kubectl exec -n aegis -it <pod> -- jcmd <pid> Thread.print > threaddump.txt

# Flags JVM
kubectl exec -n aegis -it <pod> -- jcmd <pid> VM.flags
```

### Health Checks
```bash
# Health endpoint
curl -s http://localhost:8080/actuator/health | jq .

# Métricas Prometheus
curl -s http://localhost:8080/actuator/prometheus | grep -E 'jvm_|hikaricp_|http_server_|aegis_'

# Smoke tests
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"..."}'
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/ativos?page=0&size=10
```

## 7. Contacts & References

| Contato | Papel | Canal |
| :--- | :--- | :--- |
| **Diogo Menezes** | Tech Lead / Arquiteto | Slack @diogo / diogorpm@gmail.com |
| **DBA Team** | Banco de Dados | #aegis-dba / pagerduty-dba |
| **Infra/Cloud** | Kubernetes, Rede, DNS | #aegis-infra / pagerduty-infra |
| **Security** | Vault, Certificados, Compliance | #aegis-sec / pagerduty-sec |
| **Product Owner** | Decisões de negócio, priorização | Slack @po / email |

**Documentos de referência:**
- `system-architecture.md` — Arquitetura detalhada
- `deployment-plan.md` — Pipeline CI/CD, ambientes, blue-green
- `risk-register.md` — Riscos conhecidos e mitigações
- `db-performance-recovery.md` — Performance, backup, recovery do banco
- `security-policies.md` — Políticas de segurança, rotação de segredos
- `ADR-001` — Decisão monolito único

---

**Fim do Runbook** — Próxima revisão programada: 29/01/2025 (quinzenal)