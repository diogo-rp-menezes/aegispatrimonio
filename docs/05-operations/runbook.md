# Operations Runbook & System Maintenance Guide — Aegis1

> **Versão:** 2.0 · **Owner:** DevOps / SRE / Tech Leads · **Última revisão:** 2025-01-15
> **Sistema de Alerta:** Prometheus Alertmanager → PagerDuty (Critical) / Slack #alerts (Warning) / Email (Info)
> **Canal de Incidentes:** #incidents (Slack) + PagerDuty Escalation Policy
> **Base:** System Architecture v2.0 + Deployment Plan v2.0 + Security Policies v2.0 + NFR v2.0

---

## 1. System Overview

### 1.1 Componentes Críticos

| Componente | Tecnologia | Health Check | Criticidade |
| :--- | :--- | :--- | :--- |
| **Frontend (SPA/PWA)** | Vue 3 + Vite + Nginx (Static) | `GET /` → 200 OK + `index.html` | **P0** |
| **Backend API** | Spring Boot 3.3 (Java 21) | `GET /actuator/health/liveness` + `readiness` | **P0** |
| **Auth Service** | Spring Security + JWT RS256 | `GET /actuator/health` + `/auth/me` | **P0** |
| **MySQL 8.0 Primary** | RDS/Cloud SQL (Multi-AZ) | `SELECT 1` + Replication Lag < 1s | **P0** |
| **MySQL Read Replicas** | RDS/Cloud SQL | `SELECT 1` + Lag < 1s | **P1** |
| **Redis Cluster** | ElastiCache / Memorystore | `PING` + Memory < 80% | **P1** |
| **Object Storage (S3/MinIO)** | S3 / GCS / MinIO | `HEAD /bucket/health` | **P1** |
| **K8s Cluster** | EKS/GKE/AKS 1.28+ | `kubectl get nodes` + ComponentStatus | **P0** |
| **Ingress Controller** | NGINX Ingress | `GET /healthz` | **P0** |
| **Observabilidade** | Prometheus/Grafana/Tempo/Loki/Sentry | `GET /-/healthy` | **P1** |

### 1.2 Dependências Externas Críticas

| Dependência | SLA | Contato | Runbook Específico |
| :--- | :--- | :--- | :--- |
| **Cloud Provider (AWS/GCP/Azure)** | 99.99% | Enterprise Support | `runbooks/cloud-provider-outage.md` |
| **DNS (Route53/Cloud DNS)** | 100% | DNS Team | `runbooks/dns-failover.md` |
| **TLS Certificates (Let's Encrypt/Vault)** | Auto-renewal | cert-manager | `runbooks/tls-cert-renewal.md` |
| **Vault / Secrets Manager** | 99.9% | Security Team | `runbooks/vault-outage.md` |
| **SMTP / Push Gateway (FCM/APNs)** | 99.9% | Notification Team | `runbooks/notification-outage.md` |

---

## 2. Monitoring & System Health Check

### 2.1 Golden Signals (Prometheus + Grafana)

| Métrica | Target Saudável | Alerta Warning | Alerta Critical | Dashboard |
| :--- | :--- | :--- | :--- | :--- |
| **Latência P95 API** | < 500ms | > 800ms (5min) | > 2s (5min) | Golden Signals / API Latency |
| **Throughput (RPS)** | > 100 | < 50 (5min) | < 10 (5min) | Golden Signals / Traffic |
| **Error Rate (5xx)** | < 0.1% | > 1% (5min) | > 5% (5min) | Golden Signals / Errors |
| **Saturation (CPU/Mem)** | < 70% | > 80% (5min) | > 90% (5min) | Golden Signals / Saturation |
| **JVM Heap Usage** | < 70% | > 85% (5min) | > 95% (5min) | JVM Metrics |
| **DB Connections (HikariCP)** | < 70% pool | > 85% (5min) | > 95% (5min) | DB Pool |
| **Redis Memory** | < 70% | > 85% (5min) | > 95% (5min) | Redis Metrics |
| **K8s Pod Restarts** | 0/hr | > 5/hr | > 20/hr | K8s Overview |

### 2.2 Business KPIs (Grafana)

| Métrica | Target | Alerta | Dashboard |
| :--- | :--- | :--- | :--- |
| **Ordens Criadas/hora** | > 10 | < 1/hr (30min) | Business KPIs |
| **Taxa Conclusão Ordens** | > 90% | < 70% (1h) | Business KPIs |
| **custoTotalPorAtivo Atualizado** | 100% ordens concluídas | Atraso > 15min | Business KPIs |
| **Aderência Preventiva** | > 80% | < 60% (diário) | Preventiva |
| **Alertas Preditivos Ativos** | < 50 | > 100 | Preditiva |
| **Login Success Rate** | > 99% | < 95% (15min) | Auth |
| **Refresh Token Fail Rate** | < 0.1% | > 1% (15min) | Auth |

### 2.3 Security Alerts (Alertmanager)

| Alerta | Condição | Severidade | Runbook |
| :--- | :--- | :--- | :--- |
| **Cross-Tenant Leak** | `acessos_negados_total > 10/min` | **Critical** | `runbooks/cross-tenant-leak.md` |
| **Brute Force Login** | `login_failed_total{ip} > 5/15min` | **Warning** | `runbooks/brute-force.md` |
| **Refresh Token Replay** | `refresh_token_reused_total > 0` | **Critical** | `runbooks/token-replay.md` |
| **Preditiva Threshold** | `previsao_falha_probabilidade > 0.9` | **Warning** | `runbooks/predictive-alert.md` |
| **SLA Breach** | `ordem_sla_breach_total > 0` | **Warning** | `runbooks/sla-breach.md` |
| **Audit Tamper** | `envers_revision_deleted_total > 0` | **Critical** | `runbooks/audit-tamper.md` |
| **Container Vuln** | `trivy_critical_vulnerabilities > 0` | **Critical** | `runbooks/container-vuln.md` |

---

## 3. On-Call & Escalation

### 3.1 Rotação & Responsabilidades

| Nível | Papel | Tempo Resposta | Condições Ativação | Contato |
| :--- | :--- | :--- | :--- | :--- |
| **L1** | On-Call Engineer (Rotação Semanal) | 15 min (comercial) / 30 min (fora) | Alertas Warning/Critical (PagerDuty); CDN down; Error rate > 1%; Build CI falhando `main`; Staging deploy falhou | Slack #oncall + PagerDuty |
| **L2** | Tech Lead / Eng Lead (Backend/Frontend/Infra) | 30 min | L1 não resolve em 30 min; Incidente P0 (Cross-tenant leak, Token replay, DB down, Rollback necessário); Security Incident | Slack #oncall-leads + Phone |
| **L3** | Engineering Manager / Incident Commander | 1 hora | Incidente > 1h; Impacto multi-time; Decisão rollback produção; Comunicação externa (clientes, imprensa, reguladores) | Phone + Email Executivo |

### 3.2 Rotação On-Call

- **Semanal:** Segunda 10h BRT → Próxima Segunda 10h BRT
- **Handoff:** Checklist `docs/oncall-handoff.md` (Dashboards, Contatos Backend, Chaves Deploy Staging/Prod, Incidentes Abertos, Mudanças Recentes)
- **Escalação Automática:** PagerDuty Escalation Policy (L1 → L2 em 30min → L3 em 1h)

---

## 4. Failure Recovery Procedures

### 4.1 Incident Type 1: Frontend/CDN Indisponível (P0)

**Sintoma:** Health check `GET /` falha (timeout/5xx) → Alerta sintético; Usuários veem página em branco/502/503.

**Possíveis Causas:**
1. Provedor CDN outage (CloudFront, Cloudflare, Netlify, Vercel)
2. Deploy ruim (arquivos corrompidos, `index.html` ausente)
3. Certificado TLS expirado / DNS incorreto
4. Limite banda/quotas excedido

**Diagnóstico:**
1. Status page provedor CDN (`cloudfront-status.aws.amazon.com`, `cloudflarestatus.com`)
2. Staging acessível? (`https://staging-aegis1.empresa.com`) → Isola problema prod
3. Último deploy GitHub Actions → Artifact `frontend/dist/` gerado corretamente?
4. `curl -I https://aegis1.empresa.com/` → Headers `x-amz-cf-id`, `cf-ray`, `x-vercel-id`

**Remediação:**
1. **Rollback Imediato (< 2 min):** Painel CDN → Reverter versão anterior (`sha-<previous>` ou tag `vX.Y.Z-1`) — Blue-Green swap origin/path
2. **Rollback Deploy:** `workflow_dispatch` `cd-production.yml` com `ref: vX.Y.Z-1` → Rebuild + Redeploy
3. **Failover CDN:** Atualizar DNS CNAME para provedor alternativo (configuração prévia multi-provider)
4. **Comunicação:** `#incidents`: "Rollback iniciado, ETA 2 min"

**Verificação Pós-Remediação:** Health check 200; Smoke test Playwright (Login → Lista Ordens → Detalhe → Logout) passa; RUM 0% erro JS.

---

### 4.2 Incident Type 2: Backend API Indisponível / Degradado (P0)

**Sintoma:** `GET /actuator/health/liveness` falha; Latência P95 > 2s; Error Rate 5xx > 5%; Alertas Golden Signals Critical.

**Possíveis Causas:**
1. Pod(s) Backend CrashLoopBackOff / OOMKilled / Crash
2. MySQL Primary Down / Replication Lag Alto / Connection Pool Exhausted
3. Redis Down / Memory Pressure / Connection Limit
4. K8s Node Pressure (Disk/CPU/Memory) / Node NotReady
5. Deploy Ruim (Imagem quebrada, ConfigMap/Secret errado, Migration Flyway Falhou)

**Diagnóstico:**
1. `kubectl get pods -n aegis1-prod -o wide` → Status, Restarts, Node, Events
2. `kubectl logs -n aegis1-prod -l app=aegis1-backend --tail=100` → Stack traces, OOM, Flyway errors
3. `kubectl get events -n aegis1-prod --sort-by=.metadata.creationTimestamp` → Recent events
4. `kubectl top pods -n aegis1-prod` → CPU/Memory usage
5. MySQL: `SHOW PROCESSLIST;` + `SHOW ENGINE INNODB STATUS;` + Replication Lag (`Seconds_Behind_Master`)
6. Redis: `INFO memory` + `CLIENT LIST` + `SLOWLOG GET 10`

**Remediação:**
1. **Pod Crash/Restart:** `kubectl delete pod <pod-name> -n aegis1-prod` (HPA recria) — Se OOM: Increase memory limit + JVM `-XX:MaxRAMPercentage=75`
2. **MySQL Primary Down:** Failover automático (RDS/Cloud SQL) → Verificar Read Replica promovida; `kubectl rollout restart deployment/aegis1-backend -n aegis1-prod` (reconecta)
3. **Redis Down:** Failover automático (ElastiCache/Memorystore) → `kubectl rollout restart deployment/aegis1-backend` (reconecta)
4. **Deploy Ruim:** `argocd app rollback aegis1-prod <revision-anterior>` (< 2 min) — Verifica `argocd app history aegis1-prod`
7. **Node Pressure:** `kubectl cordon <node>` + `kubectl drain <node> --ignore-daemonsets --delete-emptydir-data` → Pods evicted → HPA escala em outros nodes

**Verificação:** Health checks 200; Latência P95 < 500ms; Error Rate < 0.1%; Business KPIs normais.

---

### 4.3 Incident Type 3: Cross-Tenant Data Leak (P0 - Security Critical)

**Sintoma:** Alerta `acessos_negados_total > 10/min` + Logs `MultiTenancyFilter` bloqueio cross-tenant + Auditoria Envers mostra acesso indevido.

**Diagnóstico:**
1. `kubectl logs -n aegis1-prod -l app=aegis1-backend | grep "CROSS_TENANT_ATTEMPT"` → IP, User, Filial Origem, Filial Alvo, Timestamp
2. `kubectl exec -n aegis1-prod deploy/aegis1-backend -- curl -s localhost:8080/actuator/auditevents | jq '.events[] | select(.principal=="<user>")'`
3. Verificar se `@Filter` faltando em nova entidade (ArchUnit test falhou?)

**Remediação:**
1. **Bloqueio Imediato:** Revogar tokens usuário (`DELETE FROM refresh_token WHERE usuario_id = ?`); Bloquear IP no WAF/Gateway
2. **Isolamento:** `kubectl label namespace aegis1-prod quarantine=true` (NetworkPolicy deny all exceto monitoring)
3. **Investigação:** Identificar entidade sem `@Filter` → Hotfix `git commit --amend` + `argocd app sync aegis1-prod` (hotfix branch)
4. **Notificação:** DPO + Legal + Affected Filials (se dados expostos) — LGPD 48h

**Verificação:** Testes Cross-tenant CI passam; Auditoria Envers mostra apenas acessos autorizados; Alerta cessa.

---

### 4.4 Incident Type 4: Refresh Token Replay / Token Compromise (P0 - Security Critical)

**Sintoma:** Alerta `refresh_token_reused_total > 0` + Múltiplos 401 em sequência + Usuários reportam logout inesperado.

**Diagnóstico:**
1. `SELECT * FROM refresh_token WHERE used_at IS NOT NULL AND revoked = false ORDER BY used_at DESC LIMIT 20;` → Tokens reutilizados
2. `kubectl logs -n aegis1-prod -l app=aegis1-backend | grep "REFRESH_TOKEN_REPLAY"` → User, IP, User-Agent, Timestamp
3. Verificar `authInterceptor` frontend: Mutex funcionando? `Promise` queue?

**Remediação:**
1. **Revogação Massiva:** `UPDATE refresh_token SET revoked = true, revoked_at = NOW(), revoked_reason = 'SECURITY_INCIDENT_TOKEN_REPLAY' WHERE usuario_id IN (SELECT DISTINCT usuario_id FROM refresh_token WHERE used_at IS NOT NULL AND revoked = false);`
2. **Forçar Re-login:** Frontend detecta 401 refresh → Limpa Pinia + Cookie → Redirect `/login` com mensagem "Sessão expirada por segurança, faça login novamente"
3. **Rotação Chaves JWT (se chave comprometida):** `kubectl create secret generic jwt-keys --from-file=private.pem=new-private.pem --from-file=public.pem=new-public.pem -n aegis1-prod --dry-run=client -o yaml | kubectl apply -f -` → `argocd app sync aegis1-prod` (restart pods)
4. **Comunicação:** `#incidents` + Email usuários afetados (se dados sensíveis acessados)

**Verificação:** Alerta cessa; Novos logins geram tokens com nova chave; Auditoria Envers registra revogação.

---

### 4.5 Incident Type 5: MySQL Primary Down / Replication Lag Crítico (P0)

**Sintoma:** `actuator/health` DOWN (db); Latência queries > 5s; Replication Lag > 60s; Alertas DB Critical.

**Diagnóstico:**
1. Cloud Provider Console (RDS/Cloud SQL) → Status Primary, Failover Status, CPU/Storage/IOPS
2. `kubectl exec -n aegis1-prod deploy/aegis1-backend -- mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW SLAVE STATUS\G"` (se replica)
3. `SHOW PROCESSLIST;` → Long running queries, Locks, Deadlocks
4. `SHOW ENGINE INNODB STATUS;` → Deadlocks recentes, Buffer Pool, Log Flush

**Remediação:**
1. **Failover Automático (RDS/Cloud SQL):** Verificar se failover ocorreu → Novo Primary promovido → `kubectl rollout restart deployment/aegis1-backend -n aegis1-prod` (reconecta novo endpoint)
2. **Failover Manual (se auto falhou):** `aws rds failover-db-instance --db-instance-identifier aegis1-prod-primary` / `gcloud sql instances failover aegis1-prod-primary`
3. **Lag Alto (> 60s):** `STOP SLAVE;` → `START SLAVE;` (se replicação parou); Verificar `Seconds_Behind_Master`; Se persistir: `SET GLOBAL innodb_flush_log_at_trx_commit = 2;` (temporário, performance) → Investigar queries lentas (`pt-query-digest` / `sys.schema_table_statistics`)
4. **Connection Pool Exhausted:** `kubectl scale deployment aegis1-backend -n aegis1-prod --replicas=10` (temporário) → Aumentar `spring.datasource.hikari.maximum-pool-size` (ConfigMap) → `argocd app sync`

**Verificação:** Health check UP; Lag < 1s; Latência P95 < 500ms; Pool connections < 70%.

---

### 4.6 Incident Type 6: Flyway Migration Failure / Schema Corruption (P0)

**Sintoma:** Backend falha startup (`FlywayException: Migration failed`); `actuator/health` DOWN; Logs mostram `SQL State: 42000` / `Checksum mismatch`.

**Diagnóstico:**
1. `kubectl logs -n aegis1-prod deploy/aegis1-backend | grep -A 20 "Flyway"` → Migration falha, SQL, Checksum
2. `flyway info` (via `kubectl exec` ou local apontando prod DB) → Status migrations
3. Verificar `flyway_schema_history` → `checksum`, `success`, `installed_rank`

**Remediação:**
1. **Checksum Mismatch (Dev/Staging Only):** `./mvnw flyway:repair` (APENAS DEV/STAGING — NUNCA PROD) → Recalcula checksums
2. **Migration Failed (Prod):** 
   - Se `success=false` e `undo` disponível: `flyway undo` (requer migration `U{versao}__rollback.sql`)
   - Se `success=false` e sem undo: **Hotfix SQL** → Criar `V{next}__fix_{issue}.sql` corrigindo problema → `argocd app sync` (Flyway aplica automaticamente)
   - Se schema corrompido irrecuperável: **Restore PITR** (Point-in-Time Recovery) → `aws rds restore-db-instance-to-point-in-time` / `gcloud sql backups restore` → Novo endpoint → Update K8s Secret → `argocd app sync`
3. **Baseline Perdido:** `flyway baseline` (apenas se DB vazio ou primeira vez)

**Verificação:** `flyway info` → Todas migrations `success=true`; Backend startup OK; `actuator/health` UP.

---

### 4.7 Incident Type 7: LGPD Data Breach / Anonimização Falha (P0 - Compliance Critical)

**Sintoma:** Alerta DPO/Legal; Usuário reporta dados PII expostos; Auditoria Envers mostra PII não anonimizada após solicitação exclusão.

**Diagnóstico:**
1. `SELECT * FROM usuario WHERE status = 'INATIVO' AND (nome NOT LIKE 'USUARIO_ANON_%' OR email NOT LIKE '%@anonymized.local');` → Usuários não anonimizados
2. `SELECT * FROM revisao_info WHERE tipo_revisao = 'ANONIMIZACAO' ORDER BY timestamp DESC LIMIT 10;` → Últimas anonimizações
3. Verificar `LgpdService.anonimizar()` logs → Erro transação, hash geração, Envers revision

**Remediação:**
1. **Anonimização Forçada (Manual):** `UPDATE usuario SET nome = CONCAT('USUARIO_ANON_', SUBSTRING(SHA2(CONCAT('salt', id), 256), 1, 8)), email = CONCAT(SUBSTRING(SHA2(CONCAT('salt', id), 256), 1, 8), '@anonymized.local'), cpf = NULL, telefone = NULL, status = 'INATIVO' WHERE id = <usuario_id>;`
2. **Envers Revision:** Inserir manual em `revisao_info` + `usuario_AUD` com `revtype = 2` (MOD) + `tipo_revisao = 'ANONIMIZACAO_FORCADA'` + `hash_correlacao`
3. **Revogação Tokens:** `DELETE FROM refresh_token WHERE usuario_id = <usuario_id>;`
4. **Notificação DPO/Legal:** Relatório incidente + Ações corretivas + Prazo 48h (LGPD Art. 48)

**Verificação:** Usuário anonimizado no estado atual; Envers preserva histórico original + hash correlação; Tokens revogados; DPO confirma conformidade.

---

### 4.8 Incident Type 8: Preditiva False Positive Storm / Model Drift (P1)

**Sintoma:** Dezenas de alertas `preditiva.alerta` em minutos; Ordens preditivas auto-criadas em massa; Dashboard Preditiva saturado.

**Diagnóstico:**
1. `SELECT COUNT(*) FROM previsao_falha WHERE status = 'ATIVA' AND probabilidade > 0.8 AND data_prevista < DATE_ADD(NOW(), INTERVAL 30 DAY);` → Count alto
2. Verificar `ManutencaoPreditivaService` logs → `rQuadrado` baixo (< 0.5), outliers SMART, threshold muito sensível
3. Verificar `HealthCheckService` thresholds (`thresholdCritico`, `thresholdAtencao`) por tipo disco

**Remediação:**
1. **Threshold Conservador Imediato:** `UPDATE configuracao SET valor = '0.9' WHERE chave = 'preditiva.probabilidade.minima';` + `UPDATE configuracao SET valor = '60' WHERE chave = 'preditiva.horizonte.maximo.dias';` (via ConfigMap/DB)
2. **Desabilitar Ordem Auto Temporária:** `UPDATE configuracao SET valor = 'false' WHERE chave = 'preditiva.ordem.auto.habilitada';` (Feature Flag)
3. **Recalibração Modelo:** Job manual `ManutencaoPreditivaService.recalibrarModelos()` (remove outliers, re-treina OLS)
4. **Limpeza Alertas Falsos:** `UPDATE previsao_falha SET status = 'EXPIRADA' WHERE status = 'ATIVA' AND probabilidade < 0.9;`

**Verificação:** Alertas param; Novas previsões com threshold conservador; Modelo recalibrado (R² > 0.7).

---

## 5. Routine Maintenance Procedures

### 5.1 Daily (Automatizado via Cron/K8s CronJob)

| Task | Comando/Script | Verificação |
| :--- | :--- | :--- |
| **Backup MySQL** | Automated (RDS/Cloud SQL) | Verificar `aws rds describe-db-snapshots` / `gcloud sql backups list` |
| **Flyway Migrations Check** | `./mvnw flyway:info` (CI) | Nenhuma migration pendente em prod |
| **Integridade Checksums** | `IntegridadeChecksumService.verificar()` (Job 03:00) | Alerta se divergência |
| **Preditiva Batch** | `ManutencaoPreditivaScheduler` (02:00) | Previsões geradas, alertas criados |
| **Preventiva Scheduler** | `PreventivaScheduler` (15min) | Ordens geradas, notificações enviadas |
| **SLA Escalation** | `SlaSchedulerService` (Diário 06:00) | Notificações enviadas, auditoria registrada |
| **Limpeza Refresh Tokens Expirados** | `DELETE FROM refresh_token WHERE expiracao < NOW();` | Linhas afetadas > 0 |
| **Log Rotation / Retention** | Logback/Loki retention policies | Loki retention 30d, Envers 7 anos |

### 5.2 Weekly

| Task | Responsável | Verificação |
| :--- | :--- | :--- |
| **Test Restore MySQL** | DBA/DevOps | Restore PITR point-in-time → Validação schema + dados amostrais |
| **Integridade Checksums Report** | DevOps | Relatório `integridade_checksum` comparado baseline |
| **Certificate Expiry Check** | DevOps/Security | `cert-manager` certificates > 30 dias expiração |
| **Dependency Updates Review** | Tech Leads | Dependabot/Renovate PRs revisados + merged |
| **Capacity Planning** | DevOps | K8s HPA metrics, DB CPU/Storage, Redis Memory → Scaling decisions |
| **Security Scan Review** | Security | Trivy/DepCheck/ZAP reports → Remediação Critical/High |

### 5.3 Monthly

| Task | Responsável | Verificação |
| :--- | :--- | :--- |
| **Disaster Recovery Drill** | DevOps/SRE | Failover MySQL + Restore PITR + K8s Cluster Recreate (Staging) → RTO/RPO validados |
| **Pen Test / Security Review** | Security | ZAP/CodeQL reports + Manual review → Remediação 30d |
| **Capacity Planning Review** | DevOps/Tech Leads | Trends 90d (CPU, Mem, DB, Storage, Network) → Forecast 90d |
| **Runbook Review & Update** | SRE/Tech Leads | Todos runbooks testados + atualizados + versionados |
| **Compliance Audit** | DPO/Compliance | LGPD (solicitações atendidas), NR-10/12 (termos assinados), ISO 27001 (controles) |
| **Cost Optimization (FinOps)** | DevOps/Finance | Custos por serviço/ambiente/time → Rightsizing, Reserved Instances, Savings Plans |

---

## 6. Deployment & Rollback Quick Reference

### 6.1 Deploy Staging (Automático `develop`)
```bash
# Via ArgoCD (auto-sync develop branch)
argocd app sync aegis1-staging --prune
# Ou GitHub Actions: workflow_dispatch cd-staging.yml
```

### 6.2 Deploy Production (Manual `main` tag)
```bash
# 1. Tag release
git tag -s v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# 2. ArgoCD Sync (manual approval)
argocd app sync aegis1-prod --prune

# 3. Verificar
argocd app wait aegis1-prod --health --timeout 300
kubectl get pods -n aegis1-prod -l app=aegis1-backend
curl -f https://aegis1.empresa.com/actuator/health/liveness
```

### 6.3 Rollback Production (< 2 min)
```bash
# Via ArgoCD (revision anterior)
argocd app rollback aegis1-prod <revision-anterior>

# Ou kubectl (se ArgoCD indisponível)
kubectl rollout undo deployment/aegis1-backend -n aegis1-prod
kubectl rollout undo deployment/aegis1-frontend -n aegis1-prod

# Verificar
kubectl rollout status deployment/aegis1-backend -n aegis1-prod --timeout=120s
curl -f https://aegis1.empresa.com/actuator/health/liveness
```

### 6.4 Hotfix Production (P0 Bypass Staging)
```bash
# 1. Branch hotfix de main
git checkout main && git pull
git checkout -b hotfix/1.0.1-jwt-refresh-race

# 2. Fix + Testes locais
./mvnw clean verify -DskipITs=false
cd frontend && pnpm test:coverage && pnpm build

# 3. PR main → CI verde → Merge → Tag
git tag -s v1.0.1 -m "Hotfix: JWT refresh race condition"
git push origin v1.0.1

# 4. Deploy Prod (manual approval)
argocd app sync aegis1-prod --prune
```

---

## 7. Communication Templates

### 7.1 Incident Declaration (Slack #incidents)
```
🚨 INCIDENT DECLARED: [P0/P1] - [Título Curto]
**Severity:** P0/P1
**Started:** <timestamp> BRT
**Impact:** <Descrição impacto usuários/negócio>
**Status:** Investigating
**Commander:** @<oncall-engineer>
**Channel:** #incidents (thread)
**Runbook:** docs/05-operations/runbooks/<runbook-id>.md
```

### 7.2 Status Update (A cada 15-30 min)
```
🔄 UPDATE: [Incident ID] - [Status]
**Progress:** <O que foi feito>
**Next Steps:** <Próximas ações>
**ETA Resolution:** <Estimativa>
**Impact Update:** <Mudança no impacto>
```

### 7.3 Resolution (Final)
```
✅ RESOLVED: [Incident ID] - [Título]
**Root Cause:** <Causa raiz (5 Whys)>
**Resolution:** <Ações corretivas>
**Prevention:** <Ações preventivas (runbook update, code fix, process change)>
**Timeline:** Detectado <T1> → Declarado <T2> → Mitigado <T3> → Resolvido <T4>
**Post-Mortem:** Agendado para <data> (link Confluence/Notion)
```

### 7.4 Post-Mortem Template (Confluence/Notion)
```markdown
# Post-Mortem: [Incident ID] - [Título]

## Resumo Executivo
- **Incidente:** [ID] - [Título]
- **Severidade:** P0/P1
- **Duração:** <Xh Ym> (Detecção → Resolução)
- **Impacto:** <Usuários afetados, receita, SLA, compliance>

## Linha do Tempo
| Timestamp (BRT) | Evento | Ação | Responsável |
| :--- | :--- | :--- | :--- |

## Análise Causa Raiz (5 Whys)
1. **Por que?** → Resposta
2. **Por que?** → Resposta
3. **Por que?** → Resposta
4. **Por que?** → Resposta
5. **Por que?** → Causa Raiz

## Ações Corretivas (Imediatas)
- [ ] Ação 1 (Owner, Prazo)
- [ ] Ação 2 (Owner, Prazo)

## Ações Preventivas (Long-term)
- [ ] Ação 1 (Owner, Prazo, Ticket Jira)
- [ ] Ação 2 (Owner, Prazo, Ticket Jira)

## Lições Aprendidas
- O que funcionou bem
- O que pode melhorar
- Gaps em runbooks/monitoramento/processos

## Métricas de Impacto
- Usuários afetados: X
- Receita impactada: R$ Y
- SLA violado: Z%
- Dados expostos: Sim/Não (LGPD)

## Anexos
- Logs relevantes
- Dashboards screenshots
- Comunicações Slack/Email
```

---

## 8. Quick Reference Cards

### 8.1 Comandos Essenciais K8s
```bash
# Status geral
kubectl get all -n aegis1-prod
kubectl get pods -n aegis1-prod -o wide

# Logs
kubectl logs -n aegis1-prod -l app=aegis1-backend --tail=100 -f
kubectl logs -n aegis1-prod -l app=aegis1-frontend --tail=50

# Restart / Rollout
kubectl rollout restart deployment/aegis1-backend -n aegis1-prod
kubectl rollout status deployment/aegis1-backend -n aegis1-prod --timeout=120s
kubectl rollout undo deployment/aegis1-backend -n aegis1-prod

# Scale
kubectl scale deployment aegis1-backend -n aegis1-prod --replicas=10

# Debug
kubectl exec -n aegis1-prod deploy/aegis1-backend -- jcmd <pid> GC.heap_dump /tmp/heap.hprof
kubectl exec -n aegis1-prod deploy/aegis1-backend -- mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW PROCESSLIST;"

# ArgoCD
argocd app get aegis1-prod
argocd app sync aegis1-prod --prune
argocd app rollback aegis1-prod <revision>
argocd app history aegis1-prod
```

### 8.2 MySQL Emergency Commands
```bash
# Conectar (via kubectl exec no pod backend ou bastion)
mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASSWORD aegis1

# Processos / Locks / Deadlocks
SHOW PROCESSLIST;
SHOW ENGINE INNODB STATUS\G
SELECT * FROM performance_schema.data_locks\G
SELECT * FROM performance_schema.data_lock_waits\G

# Replication
SHOW SLAVE STATUS\G
STOP SLAVE; START SLAVE;

# Flyway
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;

# Auditoria Envers
SELECT * FROM revisao_info ORDER BY timestamp DESC LIMIT 20;
SELECT * FROM usuario_AUD WHERE rev = <revision_id>;

# Integridade
SELECT * FROM integridade_checksum ORDER BY verificado_em DESC LIMIT 5;
```

---

## 9. Rastreabilidade Runbooks ↔ Artefatos

| Runbook | System Arch | Deployment Plan | Security Policies | Risk Register | Código/Config |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `cross-tenant-leak.md` | §7.3 | §5 (Rollback) | §2.1, §5.2 | RISK-003 | `MultiTenancyFilter`, `@Filter`, `AegisShield` |
| `token-replay.md` | §7.1 | §5 (Rollback) | §4.1 | RISK-004 | `JwtTokenProvider`, `RefreshTokenRepository`, `authInterceptor` |
| `brute-force.md` | §7.1 | — | §5.1 | — | `RateLimitFilter`, `SecurityConfig` |
| `predictive-alert.md` | §9.2 | — | §5.2 | RISK-002 | `ManutencaoPreditivaService`, `HealthCheckService` |
| `sla-breach.md` | §6 (Ordens) | — | — | — | `SlaSchedulerService`, `NotificationService` |
| `audit-tamper.md` | §6 (Audit) | — | §3.3 | RISK-010 | `Envers`, `CustomRevisionListener`, `IntegridadeChecksumService` |
| `container-vuln.md` | §12 (Supply Chain) | §6 (Security Scan) | §6 | RISK-007, 013 | `Dockerfile*`, `trivy`, `cosign`, `syft` |
| `cross-tenant-leak.md` | §7.3 | §5 (Rollback) | §2.1, §5.2 | RISK-003 | `MultiTenancyFilter`, `@Filter`, `AegisShield` |
| `dr-failover.md` | §10 (Deployment) | §7 (DR) | §6 (Infra) | RISK-009 | `k8s/`, `terraform/`, `RDS/Cloud SQL` |
| `tls-cert-renewal.md` | §10 (Infra) | — | §6 | — | `cert-manager`, `ClusterIssuer`, `Certificate` |
| `vault-outage.md` | §10 (Secrets) | — | §6 | — | `External Secrets Operator`, `Vault Agent` |
| `flyway-repair.md` | §6 (Data) | — | — | RISK-012 | `Flyway`, `V*__*.sql`, `U*__*.sql` |

---

*Documento regenerado completamente com base em System Architecture v2.0 + Deployment Plan v2.0 + Security Policies v2.0 + Risk Register v2.0 + NFR v2.0. Substitui versão 1.0 que continha apenas runbooks frontend vanilla JS + CDN estático.*