# Database Performance, Recovery & Observability — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Dados · **Status:** Draft

## 1. Performance Targets
```yaml
performance:
  workload: "mixed"
  targets:
    reads: { p50_ms: "100", p95_ms: "500", p99_ms: "1000" }
    writes: { p95_ms: "200" }
  throughput:
    reads_per_second: "200"
    writes_per_second: "50"
  capacity:
    expected_rows: "ativo: 100k; manutencao: 1M; alerta: 500k; auditoria: 10M (ano 1)"
    growth_per_month: "ativo: +2k; manutencao: +15k; alerta: +10k; auditoria: +500k"
```

### Queries Críticas
| ID | Propósito | Latência Máx. | Índices Esperados |
| :--- | :--- | :--- | :--- |
| QUERY-001 | Busca ativo por tag_patrimonial (etiqueta física, importação) | ≤ 50ms p95 | INDEX-001 (btree unique tag_patrimonial) |
| QUERY-002 | Listagem ativos por filial + status (dashboard, relatórios) | ≤ 100ms p95 | INDEX-003 (btree filial_id, status) |
| QUERY-003 | Filtros dinâmicos ManutencaoSpecification.build (ativo, tipo, status, datas, fornecedor, técnico, faixa custo) | ≤ 50ms p95 | INDEX-010 (btree ativo_id, status, tipo, data_abertura DESC) |
| QUERY-004 | Agregação custoTotalPorAtivo offload read-replica | ≤ 2s p95 | INDEX-009 (btree ativo_id, data_conclusao DESC, custo_total) + vw_custo_manutencao_por_ativo (materialized) |
| QUERY-005 | listarAlertas/getRecentAlerts: alertas ativos por ativo ordenados severidade/data | ≤ 100ms p95 | INDEX-014 (btree ativo_id, status, severidade, data_criacao DESC) |
| QUERY-006 | Histórico auditoria por entidade (todas alterações de um ativo) | ≤ 200ms p95 | INDEX-031 (btree entidade, entidade_id, data_hora DESC) |
| QUERY-007 | Ações por usuário (compliance SOX/LGPD) | ≤ 200ms p95 | INDEX-032 (btree usuario_id, data_hora DESC) |
| QUERY-008 | checkResourceUsageAlerts job (12k ativos) | ≤ 30s total | INDEX-007 (btree tipo_ativo_id, status) + INDEX-014 + INDEX-015 |
| QUERY-009 | AtivoMapper.toDTO join ativo_detalhe_hardware (evita N+1) | ≤ 100ms p95 | INDEX-026 (btree unique ativo_id on ativo_detalhe_hardware) |
| QUERY-010 | Particionamento lógico/arquivamento auditoria por data_hora (mensal) | N/A (DDL) | INDEX-033 (btree data_hora ASC) |

<!-- source: db-schema-spec#4 (Indexes) -->
<!-- source: nfr#NFR-P01, NFR-P04, NFR-S01, NFR-S02 -->
<!-- source: db-schema-spec#5 (Views: vw_custo_manutencao_por_ativo materialized) -->
<!-- source: db-schema-spec#9 (PART-001, IDX-001) -->

## 2. Backup & Recovery
```yaml
recovery:
  backup:
    frequency: "diário (full) + WAL/log shipping contínuo se suportado pelo SGBD"
    retention: "7 anos (SOX) para auditoria; 5 anos para demais tabelas transacionais; 90 dias para logs operacionais"
    types: ["full", "incremental/WAL"]
  rpo_target: "24h (backup diário) — alvo NFR-A03; pode reduzir para < 1h com WAL shipping se SGBD suportar"
  rto_target: "4h (restauração completa banco + aplicação) — alvo NFR-A02"
  disaster_recovery:
    regions: "[PENDENTE: definir região primária e DR — depende de provedor cloud/on-prem]"
    failover_strategy: "read-replica promovida a primária + restore point-in-time a partir de backup + WAL; runbook documentado e testado trimestralmente"
  restore_test_frequency: "trimestral (simulação restore point-in-time + validação hash_encadeado auditoria)"
```

### Detalhamento por Tabela (Política de Retenção e Recuperabilidade)

| Tabela | Classificação | Retenção Mínima | Estratégia Backup | Criticidade Recuperação | Observações |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `auditoria` | audit | 7 anos (SOX) | Full diário + WAL contínuo + cópia assíncrona WORM (audit-worm-storage) | **Crítica** — imutável, hash_encadeado, append-only | Particionamento mensal (data_hora); tiering hot 30d SSD / cold 7a archive |
| `ativo` | core | 5 anos | Full diário + WAL | **Alta** — núcleo patrimonial | Particionamento lógico data_aquisicao; PK UUID |
| `manutencao` | transactional | 5 anos | Full diário + WAL | **Alta** — custos, SLA, compliance | Particionamento lógico data_abertura; MV vw_custo_manutencao_por_ativo refresh diário |
| `alerta` | transactional | 3 anos | Full diário + WAL | **Média** — operacional | Arquiva lidos > 90 dias (lifecycle); particionamento data_criacao |
| `usuario`, `funcionario`, `role`, `permissao`, `usuario_role`, `role_permissao` | core/reference | 5 anos (LGPD: anonimização sob demanda) | Full diário + WAL | **Alta** — RBAC, acesso | LGPD: anonymize_user function para direito ao esquecimento |
| `filial`, `departamento`, `localizacao`, `fornecedor`, `tipo_ativo` | reference | Indefinida (dados mestres) | Full diário + WAL | **Média** — catálogos | Baixa volatilidade |
| `ativo_detalhe_hardware`, `adaptador_rede`, `disco`, `memoria` | supporting | 5 anos (vinculado ao ativo pai) | Full diário + WAL | **Média** — inventário técnico | Cascade delete com ativo; coletado_em para versionamento |

<!-- source: db-schema-spec#1 (Tables classification) -->
<!-- source: db-schema-spec#9 (WORM-001, PART-001, SEC-001) -->
<!-- source: nfr#NFR-A02, NFR-A03, NFR-A04, NFR-C01, NFR-C02, NFR-CO03 -->
<!-- source: db-schema-spec#6 (Functions: anonymize_user, calcular_hash_encadeado_auditoria) -->
<!-- source: db-schema-spec#7 (Triggers: trg_auditoria_insert_hash_chain, trg_auditoria_prevent_update_delete) -->

## 3. Observability
```yaml
observability:
  metrics:
    - name: "db_connections_active"
      warning_threshold: "400 (80% pool 500)"
      critical_threshold: "475 (95% pool)"
    - name: "db_query_latency_p95_ms"
      warning_threshold: "400"
      critical_threshold: "800"
    - name: "db_query_error_rate"
      warning_threshold: "0.05%"
      critical_threshold: "0.1%"
    - name: "db_replication_lag_seconds"
      warning_threshold: "30"
      critical_threshold: "120"
    - name: "db_disk_usage_percent"
      warning_threshold: "70%"
      critical_threshold: "85%"
    - name: "auditoria_hash_chain_integrity"
      warning_threshold: "1 falha verificação"
      critical_threshold: "qualquer falha"
    - name: "checkResourceUsageAlerts_duration_seconds"
      warning_threshold: "20"
      critical_threshold: "30"
    - name: "materialized_view_refresh_duration_seconds"
      warning_threshold: "60"
      critical_threshold: "120"
    - name: "backup_duration_seconds"
      warning_threshold: "1800 (30min)"
      critical_threshold: "3600 (1h)"
    - name: "backup_size_bytes"
      warning_threshold: "crescimento > 20% mês a mês"
      critical_threshold: "espaço disco < 20% livre"
  alerts:
    - name: "AltaLatenciaLeitura"
      condition: "db_query_latency_p95_ms > 400 por 5min"
      severity: "high"
    - name: "AltaLatenciaEscrita"
      condition: "db_write_latency_p95_ms > 200 por 5min"
      severity: "high"
    - name: "TaxaErroElevada"
      condition: "db_query_error_rate > 0.1% por 2min"
      severity: "critical"
    - name: "PoolConexoesEsgotando"
      condition: "db_connections_active > 475 por 5min"
      severity: "critical"
    - name: "ReplicationLagAlto"
      condition: "db_replication_lag_seconds > 120 por 5min"
      severity: "high"
    - name: "DiscoQuaseCheio"
      condition: "db_disk_usage_percent > 85%"
      severity: "critical"
    - name: "FalhaIntegridadeAuditoria"
      condition: "auditoria_hash_chain_integrity falha verificação"
      severity: "critical"
    - name: "JobAlertasLento"
      condition: "checkResourceUsageAlerts_duration_seconds > 30"
      severity: "high"
    - name: "MVRefreshFalhou"
      condition: "materialized_view_refresh_duration_seconds > 120 OU erro"
      severity: "high"
    - name: "BackupLentoOuFalhou"
      condition: "backup_duration_seconds > 3600 OU erro"
      severity: "critical"
  migration_monitoring:
    lock_duration: true
    execution_duration: true
    affected_rows: true
    errors: true
```

### Instrumentação Necessária (Gaps a Implementar)

| Componente | Métrica/Log/Trace | Status Atual | Ação Requerida |
| :--- | :--- | :--- | :--- |
| **Spring Boot Actuator** | `/actuator/metrics/http.server.requests`, `/actuator/metrics/hikaricp.connections.*`, `/actuator/metrics/jvm.*` | Não confirmado no código | Adicionar `spring-boot-starter-actuator`, `micrometer-registry-prometheus`; expor `/actuator/prometheus` |
| **Health Checks** | `/actuator/health` (liveness/readiness) verificando DB, disco, scheduler health checks ativos | Não confirmado | Implementar `HealthIndicator` custom para conectividade DB, espaço disco, job `checkResourceUsageAlerts` |
| **Distributed Tracing** | OpenTelemetry Java agent propagando `traceparent` em 100% requisições HTTP + chamadas JDBC | Não configurado | Adicionar agent OTel; configurar exportador (OTLP/Jaeger/Zipkin); instrumentar JDBC via `otel.instrumentation.jdbc.enabled=true` |
| **Structured Logging** | JSON logs: `timestamp`, `level`, `traceId`, `spanId`, `service`, `message`, `context` | `console.error/debug` residuais em `api.js` (frontend) | Remover `console.*`; configurar Logback/Log4j2 com `LogstashEncoder`; correlacionar `traceId` via MDC |
| **Audit Integrity Verification** | Job periódico validando `hash_encadeado` monotônico por `correlation_id` (INDEX-030) | Função `calcular_hash_encadeado_auditoria` + trigger definidos no schema | Implementar job batch (Spring Batch/Quartz) rodando diário; alertar em falha |
| **Partition Maintenance** | Monitorar criação de partições mensais (auditoria), verificação de arquivamento alertas lidos > 90d | Particionamento lógico definido, não implementado | Implementar procedure/trigger para rotação partições; job de arquivamento alertas |
| **Materialized View Refresh** | `REFRESH MATERIALIZED VIEW CONCURRENTLY vw_custo_manutencao_por_ativo` diário | View definida, refresh não agendado | Agendar via `pg_cron` (PG) / `DBMS_SCHEDULER` (Oracle) / Agent Job (SQL Server); monitorar duração |
| **Backup Verification** | `pg_basebackup`/`RMAN`/`sqlcmd` + restore test automatizado trimestral | Runbook < 4h definido (NFR-A02), não automatizado | Automatizar restore em staging; validar contagem linhas + checksums + hash_encadeado auditoria |

<!-- source: nfr#NFR-O01, NFR-O02, NFR-O03, NFR-O04 -->
<!-- source: db-schema-spec#6 (Functions), #7 (Triggers), #4 (INDEX-030, INDEX-033) -->
<!-- source: db-schema-spec#5 (vw_custo_manutencao_por_ativo materialized) -->
<!-- source: db-schema-spec#9 (PART-001, WORM-001, IDX-001) -->
<!-- source: Diagnóstico#Chamadas console.* residuais (frontend\src\services\api.js:36,99) -->

---

## 4. Decisões Pendentes e Riscos (Bloqueadores para Implementação)

| ID | Decisão | Impacto em Performance/Recovery/Observability | Responsável | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| **MOTOR-001** | Definir SGBD (PostgreSQL, Oracle, SQL Server, MySQL) | **Crítico**: Tipos físicos (UUID, JSONB, ENUM, INET, MACADDR), particionamento nativo, WAL shipping, WORM storage, índices parciais, MV refresh concorrente, RLS, funções/trigger syntax | Arquiteto de Dados / Tech Lead | Imediato |
| **PART-001** | Particionamento nativo vs lógico (views+triggers) | **Alto**: Performance queries históricas, manutenção, backup granular, purge dados antigos | DBA / Arquiteto | Antes geração DDL |
| **WORM-001** | Storage WORM auditoria (S3 Object Lock, Azure Immutable Blob, tabela append-only cluster separado) | **Crítico**: Compliance SOX 7 anos, integridade hash_encadeado | SecOps / DBA | Antes produção |
| **ENUM-001** | Implementação ENUMs (tipo nativo, CHECK constraint, tabela referência) | **Médio**: Performance CHECK vs join, manutenção valores, portabilidade | Arquiteto de Dados | Antes geração DDL |
| **JSON-001** | Suporte JSONB/JSON no SGBD alvo | **Médio**: `tipo_ativo.campos_tecnicos_obrigatorios`, `alerta.metadados`, `auditoria.valores_*` | Arquiteto de Dados | Antes geração DDL |
| **SEC-001** | RLS / views segurança para segregação `filial_id` (multi-tenancy lógico) | **Alto**: Isolamento dados por unidade, performance queries com predicate pushdown | SecOps / Arquiteto | Antes produção |
| **IDX-001** | Validar 35 índices propostos com `EXPLAIN ANALYZE` carga realista (100k+ ativos, 1M+ manutenções, 500k+ alertas, 10M+ auditoria) | **Alto**: Evitar over-indexing, confirmar planos de execução queries críticas | DBA / Eng. Performance | Pós-carga teste |
| **OBS-001** | Stack observabilidade (Prometheus/Grafana, Datadog, New Relic, ELK, Loki, Tempo) | **Médio**: Implementação métricas, alertas, tracing, logs centralizados | DevOps / Platform | Sprint 0 |

<!-- source: db-schema-spec#9 (Pendências e Decisões Necessárias) -->
<!-- source: nfr#O que falta verificar (Gaps de Informação) -->

---

## 5. Checklist de Prontidão Operacional (Go-Live)

| Item | Critério | Evidência Esperada | Status |
| :--- | :--- | :--- | :--- |
| **PERF-01** | Queries críticas (QUERY-001 a QUERY-010) validadas com `EXPLAIN ANALYZE` em carga ≥ 50% produção | Relatórios de plano execução + latência p95/p99 | ⬜ Pendente |
| **PERF-02** | Job `checkResourceUsageAlerts` processa 12k ativos em ≤ 30s | Logs execução + métrica `checkResourceUsageAlerts_duration_seconds` | ⬜ Pendente |
| **PERF-03** | Read-replica suporta `custoTotalPorAtivo` ≤ 2s p95 via MV + INDEX-009 | Teste carga read-replica isolada | ⬜ Pendente |
| **RECV-01** | Restore point-in-time testado trimestralmente com sucesso (RTO ≤ 4h, RPO ≤ 24h) | Relatório teste restore + validação hash_encadeado auditoria | ⬜ Pendente |
| **RECV-02** | Backup full diário + WAL shipping (se suportado) completos sem erro ≥ 30 dias consecutivos | Logs backup + alertas `BackupLentoOuFalhou` zerados | ⬜ Pendente |
| **RECV-03** | WORM storage auditoria provisionado, replicado cross-region, imutabilidade verificada | Auditoria bucket/política + teste tentativa delete/overwrite falha | ⬜ Pendente |
| **OBS-01** | Métricas Prometheus expostas em `/actuator/prometheus` (JVM, HTTP, DB pool, cache, custom) | Scrape Prometheus bem-sucedido + dashboards Grafana operacionais | ⬜ Pendente |
| **OBS-02** | Health checks `/actuator/health` (liveness/readiness) verificam DB, disco, scheduler | Kubernetes/Orquestrador usa probes; falha induzida derruba pod | ⬜ Pendente |
| **OBS-03** | Tracing distribuído 100% requisições HTTP + JDBC com `traceparent` propagado | Amostra traces Jaeger/Tempo mostrando spans DB | ⬜ Pendente |
| **OBS-04** | Alertas críticos (TaxaErroElevada, PoolConexoesEsgotando, DiscoQuaseCheio, FalhaIntegridadeAuditoria, BackupLentoOuFalhou) disparando no Alertmanager/Grafana | Testes de injeção falha + recebimento notificação | ⬜ Pendente |
| **OBS-05** | Logs estruturados JSON com `traceId`/`spanId` correlacionados em toda stack (frontend → backend → DB) | Busca log por `traceId` retorna cadeia completa | ⬜ Pendente |
| **SEC-01** | RLS / views segurança `filial_id` implementadas e testadas para todos roles | Query plano mostra predicate `filial_id = current_setting('app.current_filial')::uuid` | ⬜ Pendente |
| **SEC-02** | Rotação segredos (JWT, DB, criptografia) a cada 90 dias via cofre (Vault/Secrets Manager) | Pipeline rotação automatizado + auditoria rotação | ⬜ Pendente |
| **COMP-01** | LGPD `anonymize_user` function testada e auditada (registro em `auditoria` com `acao='UPDATE'`) | Teste anonimização + verificação hash_encadeado | ⬜ Pendente |
| **COMP-02** | Matriz retenção dados aprovada por Compliance/Legal (7a SOX, LGPD por tipo dado) | Documento assinado + implementado em jobs purge/arquivamento | ⬜ Pendente |

<!-- source: nfr#NFR-P01, NFR-P04, NFR-A02, NFR-A03, NFR-A04, NFR-O01..O04, NFR-SEC01..SEC05, NFR-C01, NFR-C02, NFR-CO03 -->
<!-- source: db-schema-spec#6, #7, #9 -->