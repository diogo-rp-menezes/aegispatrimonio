# Database Performance, Recovery & Observability — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** backend-team · **Status:** Draft

## 1. Performance Targets
```yaml
performance:
  workload: "OLTP"
  targets:
    reads: { p50_ms: "200", p95_ms: "2000", p99_ms: "5000" }
    writes: { p95_ms: "500" }
  throughput:
    reads_per_second: "100"
    writes_per_second: "50"
  capacity:
    expected_rows: "ativos: 5000, manutencoes: 50000/ano, health_check_disco: 1M+/ano, auditoria: 500k+/ano"
    growth_per_month: "ativos: +100, health_check_disco: +80k, auditoria: +40k"
```

### Queries Críticas
| ID | Propósito | Latência Máx. | Índices Esperados |
| :--- | :--- | :--- | :--- |
| QUERY-001 | Listagem paginada de ativos por filial/status (dashboard) | 2000 ms (p95) | INDEX-002 (filial_id, status) |
| QUERY-002 | Busca exata de ativo por tag_patrimonio | 50 ms (p95) | INDEX-001 (tag_patrimonio UNIQUE) |
| QUERY-003 | Histórico de manutenções de um ativo (mais recente primeiro) | 500 ms (p95) | INDEX-009 (ativo_id, data_solicitacao DESC) |
| QUERY-004 | Fila de manutenções por status ordenadas por antiguidade | 500 ms (p95) | INDEX-010 (status, data_solicitacao) |
| QUERY-005 | Alertas não lidos de um ativo (painel de saúde) | 200 ms (p95) | INDEX-013 (ativo_id, lido, created_at DESC) |
| QUERY-006 | Caixa de entrada de alertas do usuário (não lidos primeiro) | 200 ms (p95) | INDEX-014 (usuario_id, lido, created_at DESC) |
| QUERY-007 | Alertas críticos/altos recentes (monitoramento operacional) | 500 ms (p95) | INDEX-015 (severidade, created_at DESC) |
| QUERY-008 | Último health check de cada disco (vw_disco_saude_atual) | 1000 ms (p95) | INDEX-029 (disco_id, coletado_em DESC) + INDEX-026 (saude_smart, usado_gb DESC) |
| QUERY-009 | Histórico de auditoria de uma entidade específica | 1000 ms (p95) | INDEX-031 (entidade, entidade_id, created_at DESC) |
| QUERY-010 | Ações realizadas por um usuário (investigação, LGPD) | 1000 ms (p95) | INDEX-032 (usuario_id, created_at DESC) |
| QUERY-011 | Depreciação acumulada e valor residual por ativo (vw_depreciacao_ativo) | 2000 ms (p95) | INDEX-006 (tipo_ativo_id) + INDEX-008 (data_aquisicao DESC) |
| QUERY-012 | Busca de ativo por MAC address (inventário de rede) | 100 ms (p95) | INDEX-028 (mac_address UNIQUE) |

<!-- source: db-schema-spec#4-indexes -->
<!-- source: nfr#NFR-P01 -->
<!-- source: nfr#NFR-P05 -->

## 2. Backup & Recovery
```yaml
recovery:
  backup:
    frequency: "full: daily 02:00 UTC; incremental: every 15 minutes (WAL archiving)"
    retention: "full: 30 days; incremental/WAL: 7 days; archived: 5 years (compliance LGPD/SOX)"
    types: ["full", "incremental", "wal_archiving"]
  rpo_target: "15 minutes"
  rto_target: "1 hour"
  disaster_recovery:
    regions: ["primary: us-east-1 (or on-prem DC), standby: us-west-2 (or secondary DC)"]
    failover_strategy: "synchronous streaming replication with automatic failover (Patroni/repmgr) or managed service HA (RDS Multi-AZ, Cloud SQL HA)"
  restore_test_frequency: "monthly (automated restore to staging + checksum validation)"
```

### Políticas de Retenção por Tabela (Particionamento)
| Tabela | Estratégia | Intervalo | Retenção | Ação de Purge |
| :--- | :--- | :--- | :--- | :--- |
| `health_check_disco` | RANGE (coletado_em) | 1 month | 13 months | `DROP PARTITION` automático via pg_partman/job |
| `auditoria` | RANGE (created_at) | 1 year | 5 years | `DROP PARTITION` automático + arquivamento WORM para storage frio |

<!-- source: db-schema-spec#1.15-health_check_disco-partitioning -->
<!-- source: db-schema-spec#1.16-auditoria-partitioning -->
<!-- source: nfr#NFR-A02 -->
<!-- source: nfr#NFR-A03 -->
<!-- source: nfr#NFR-C02 -->

## 3. Observability
```yaml
observability:
  metrics:
    - name: "db_connections_active"
      warning_threshold: "70% of max_connections"
      critical_threshold: "90% of max_connections"
    - name: "db_connections_idle_in_transaction"
      warning_threshold: "> 10 for > 5 min"
      critical_threshold: "> 20 for > 2 min"
    - name: "db_replication_lag_bytes"
      warning_threshold: "> 100 MB"
      critical_threshold: "> 1 GB"
    - name: "db_table_bloat_ratio"
      warning_threshold: "> 20%"
      critical_threshold: "> 50%"
    - name: "db_index_usage_ratio"
      warning_threshold: "< 80%"
      critical_threshold: "< 50%"
    - name: "db_sequential_scan_ratio"
      warning_threshold: "> 30% on tables > 10k rows"
      critical_threshold: "> 50% on tables > 10k rows"
    - name: "db_checkpoint_write_latency_ms"
      warning_threshold: "> 50 ms"
      critical_threshold: "> 200 ms"
    - name: "db_wal_growth_rate_mb_min"
      warning_threshold: "> 500 MB/min sustained"
      critical_threshold: "> 1 GB/min sustained"
    - name: "health_check_disco_partition_count"
      warning_threshold: "> 14 partitions (retention breach)"
      critical_threshold: "> 15 partitions"
    - name: "auditoria_partition_count"
      warning_threshold: "> 6 partitions (retention breach)"
      critical_threshold: "> 7 partitions"
    - name: "query_latency_p95_ms_by_endpoint"
      warning_threshold: "> 1500 ms (listagem ativos), > 400 ms (CRUD unitário)"
      critical_threshold: "> 3000 ms (listagem), > 1000 ms (CRUD)"
    - name: "deadlock_count_per_minute"
      warning_threshold: "> 0"
      critical_threshold: "> 5"
  alerts:
    - name: "DB_High_Connection_Usage"
      condition: "db_connections_active > 90% for 2 min"
      severity: "critical"
    - name: "DB_Replication_Lag_High"
      condition: "db_replication_lag_bytes > 1 GB for 5 min"
      severity: "critical"
    - name: "DB_Table_Bloat_Critical"
      condition: "db_table_bloat_ratio > 50% on any core table (ativo, manutencao, auditoria)"
      severity: "high"
    - name: "DB_Sequential_Scan_High"
      condition: "db_sequential_scan_ratio > 50% on tables > 10k rows for 10 min"
      severity: "high"
    - name: "DB_Checkpoint_Latency_High"
      condition: "db_checkpoint_write_latency_ms > 200 ms for 5 min"
      severity: "high"
    - name: "DB_WAL_Growth_Abnormal"
      condition: "db_wal_growth_rate_mb_min > 1 GB/min for 10 min"
      severity: "high"
    - name: "Partition_Retention_Breach_HealthCheck"
      condition: "health_check_disco_partition_count > 14"
      severity: "high"
    - name: "Partition_Retention_Breach_Auditoria"
      condition: "auditoria_partition_count > 6"
      severity: "critical"
    - name: "Query_Latency_SLA_Breach"
      condition: "query_latency_p95_ms_by_endpoint exceeds thresholds for 5 min"
      severity: "high"
    - name: "Deadlock_Storm"
      condition: "deadlock_count_per_minute > 5 for 2 min"
      severity: "critical"
    - name: "Disk_Space_Critical"
      condition: "data directory > 80% full"
      severity: "critical"
    - name: "Backup_Failure"
      condition: "pg_basebackup or WAL archiving fails"
      severity: "critical"
  migration_monitoring:
    lock_duration: true
    execution_duration: true
    affected_rows: true
    errors: true
```

### Dashboards Obrigatórios (Grafana)
| Dashboard | Painéis Principais | Fonte de Dados |
| :--- | :--- | :--- |
| **DB Overview** | Conexões ativas/ociosas, TPS, latência p50/p95/p99, cache hit ratio, replication lag | Prometheus (postgres_exporter) |
| **Table & Index Health** | Bloat ratio por tabela, index usage %, sequential scan %, dead tuples, autovacuum activity | Prometheus (postgres_exporter) |
| **Partition Management** | Contagem de partições por tabela, tamanho por partição, próxima partição a expirar, status do pg_partman | Prometheus (custom queries) |
| **Query Performance** | Latência por query tag (endpoint), top 10 queries por tempo total, top 10 por chamadas, planos de execução recentes | Prometheus (pg_stat_statements) + pgBadger |
| **Backup & Recovery** | Status do último backup (sucesso/falha), tamanho, duração, WAL arquivado, RPO atual, último restore test | Prometheus (backup_exporter) + logs |
| **Audit & Compliance** | Volume de auditoria por dia/entidade/usuário, latência de insert na tabela auditoria, contagem de partições | Prometheus (custom queries) |

### Logs Estruturados (PostgreSQL)
```yaml
log_config:
  log_destination: "stderr,csvlog"
  logging_collector: "on"
  log_directory: "pg_log"
  log_filename: "postgresql-%Y-%m-%d_%H%M%S.log"
  log_rotation_age: "1d"
  log_rotation_size: "100MB"
  log_min_duration_statement: "1000"  # log queries > 1s
  log_checkpoints: "on"
  log_connections: "on"
  log_disconnections: "on"
  log_lock_waits: "on"
  log_temp_files: "10MB"
  log_autovacuum_min_duration: "5000"
  log_statement: "ddl,mod"
  log_line_prefix: "%m [%p] %q%u@%d %r %a "
  log_timezone: "UTC"
```

### Tracing Distribuído (OpenTelemetry)
- **Instrumentação:** `oteljavaagent` com auto-instrumentação JDBC, Spring MVC
- **Propagação:** `traceparent` / `b3` headers
- **Amostragem:** 100% para transações de escrita (INSERT/UPDATE/DELETE em tabelas core), 10% para reads
- **Atributos DB:** `db.system=postgresql`, `db.operation`, `db.sql.table`, `db.statement` (sanitizado), `db.rows_affected`

<!-- source: nfr#NFR-O01 -->
<!-- source: nfr#NFR-O02 -->
<!-- source: nfr#NFR-O03 -->
<!-- source: nfr#NFR-O04 -->
<!-- source: nfr#NFR-O05 -->
<!-- source: db-schema-spec#1.15-health_check_disco -->
<!-- source: db-schema-spec#1.16-auditoria -->

---

## 4. Índices de Performance Críticos (Validação Contínua)
> Estes índices devem ser monitorados para uso (`pg_stat_user_indexes.idx_scan = 0` por > 7 dias = candidato a remoção) e bloat (`pgstattuple`).

| Índice | Tabela | Colunas | Tipo | Justificativa de Performance |
| :--- | :--- | :--- | :--- | :--- |
| INDEX-001 | ativo | tag_patrimonio | btree UNIQUE | PK de negócio, busca exata O(log n) |
| INDEX-002 | ativo | filial_id, status | btree | Dashboard principal: listagem por filial + filtro status |
| INDEX-003 | ativo | departamento_id, status | btree | Gestão departamental |
| INDEX-009 | manutencao | ativo_id, data_solicitacao DESC | btree | Histórico por ativo (mais recente primeiro) |
| INDEX-010 | manutencao | status, data_solicitacao | btree | Fila de trabalho por status |
| INDEX-013 | alerta | ativo_id, lido, created_at DESC | btree | Painel saúde do ativo |
| INDEX-014 | alerta | usuario_id, lido, created_at DESC | btree | Caixa de entrada usuário |
| INDEX-015 | alerta | severidade, created_at DESC | btree | Monitoramento operacional (CRITICA/ALTA) |
| INDEX-026 | disco | saude_smart, usado_gb DESC | btree | Job de alerta DISCO_CRITICO |
| INDEX-029 | health_check_disco | disco_id, coletado_em DESC | btree | Histórico saúde por disco (LATERAL join em view) |
| INDEX-030 | health_check_disco | coletado_em | BRIN | Purge particionado + scans temporais em tabela grande |
| INDEX-031 | auditoria | entidade, entidade_id, created_at DESC | btree | Auditoria por entidade (mais recente) |
| INDEX-032 | auditoria | usuario_id, created_at DESC | btree | Investigação LGPD por usuário |
| INDEX-033 | auditoria | created_at | BRIN | Purge particionado + scans temporais append-only |

<!-- source: db-schema-spec#4-indexes -->

## 5. Configurações de Performance (PostgreSQL 15+)
```yaml
postgresql_conf_tuning:
  # Memória
  shared_buffers: "25% RAM (ex: 4GB em 16GB)"
  effective_cache_size: "75% RAM (ex: 12GB em 16GB)"
  work_mem: "64MB (ajustar p/ sorts/hashes complexos)"
  maintenance_work_mem: "1GB"
  max_parallel_workers_per_gather: "4"
  max_parallel_workers: "8"
  max_parallel_maintenance_workers: "4"

  # WAL / Checkpoint
  wal_level: "replica"
  max_wal_size: "4GB"
  min_wal_size: "1GB"
  checkpoint_completion_target: "0.9"
  wal_buffers: "64MB"
  wal_writer_delay: "200ms"
  synchronous_commit: "on"  # "remote_apply" se synchronous_standby_names configurado

  # Planner
  random_page_cost: "1.1"  # SSD/NVMe
  effective_io_concurrency: "200"  # NVMe
  default_statistics_target: "500"
  constraint_exclusion: "partition"
  enable_partitionwise_join: "on"
  enable_partitionwise_aggregate: "on"
  jit: "on"
  jit_above_cost: "100000"

  # Autovacuum (crítico para tabelas de alta rotatividade)
  autovacuum: "on"
  autovacuum_max_workers: "4"
  autovacuum_naptime: "30s"
  autovacuum_vacuum_threshold: "50"
  autovacuum_vacuum_scale_factor: "0.05"
  autovacuum_analyze_threshold: "50"
  autovacuum_analyze_scale_factor: "0.02"
  autovacuum_vacuum_cost_limit: "2000"
  autovacuum_vacuum_cost_delay: "2ms"
  # Overrides por tabela (via ALTER TABLE ... SET (autovacuum_vacuum_scale_factor = 0.01)):
  # health_check_disco, auditoria, alerta, manutencao

  # Conexões
  max_connections: "200"  # via PgBouncer pool_mode=transaction para 200 usuários concorrentes
  superuser_reserved_connections: "3"

  # Logging (ver seção Observability)
  # ...
```

### PgBouncer (Connection Pooling)
```yaml
pgbouncer:
  pool_mode: "transaction"
  max_client_conn: "1000"
  default_pool_size: "50"
  min_pool_size: "10"
  reserve_pool_size: "10"
  reserve_pool_timeout: "5s"
  max_db_connections: "150"
  max_user_connections: "150"
  server_reset_query: "DISCARD ALL"
  server_check_query: "SELECT 1"
  server_check_delay: "30s"
  query_timeout: "30s"
  query_wait_timeout: "10s"
```

<!-- source: nfr#NFR-S01 -->
<!-- source: nfr#NFR-S04 -->

## 6. Capacidade de Crescimento & Planejamento
| Métrica | Atual (Estimado) | 12 Meses | 36 Meses | Ação de Escala |
| :--- | :--- | :--- | :--- | :--- |
| `ativo` rows | 5.000 | 6.200 | 8.600 | Vertical (RAM/CPU) suficiente |
| `manutencao` rows/ano | 50.000 | 60.000 | 80.000 | Índices compostos mantêm performance |
| `health_check_disco` rows/ano | 1.000.000 | 1.200.000 | 1.500.000 | Particionamento mensal + BRIN + purge 13m |
| `auditoria` rows/ano | 500.000 | 600.000 | 800.000 | Particionamento anual + BRIN + purge 5a |
| `disco` rows | 15.000 | 18.000 | 25.000 | Índice saude_smart + usado_gb |
| Tamanho DB (dados + índices) | ~15 GB | ~25 GB | ~50 GB | Storage auto-expand (managed) ou +disk |
| WAL/dia | ~5 GB | ~8 GB | ~15 GB | WAL archiving para S3/GCS |

<!-- source: nfr#NFR-S03 -->
<!-- source: db-schema-spec#1.15 -->
<!-- source: db-schema-spec#1.16 -->

## 7. Checklist de Validação Operacional
| Item | Frequência | Responsável | Ferramenta/Query |
| :--- | :--- | :--- | :--- |
| Verificar `idx_scan = 0` em índices > 7 dias | Semanal | DBA | `pg_stat_user_indexes` |
| Verificar bloat > 20% em tabelas core | Semanal | DBA | `pgstattuple` / `pg_freespacemap` |
| Confirmar autovacuum rodando nas tabelas particionadas | Diário | DBA | `pg_stat_progress_vacuum` + logs |
| Validar contagem de partições vs retenção | Diário | Automação | Query custom + alerta Prometheus |
| Testar restore de backup (staging) | Mensal | DevOps | `pg_restore` + checksum |
| Verificar replication lag < 100 MB | Contínuo | Monitoramento | `pg_stat_replication` |
| Analisar top 10 queries por `total_exec_time` | Semanal | DBA | `pg_stat_statements` |
| Revisar `work_mem` / `maintenance_work_mem` p/ queries complexas | Quinzenal | DBA | `EXPLAIN (ANALYZE, BUFFERS)` |
| Validar RPO/RTO em drill de disaster recovery | Trimestral | DevOps + DBA | Runbook documentado |

---

## 8. Pendências & Riscos Conhecidos
| ID | Descrição | Impacto | Mitigação | Status |
| :--- | :--- | :--- | :--- | :--- |
| DB-001 | Motor de banco não confirmado no diagnóstico (package.json só mostra frontend). Schema spec assume PostgreSQL 15+ (UUID, JSONB, TIMESTAMPTZ, BRIN, particionamento nativo). | Alto — DDL incompatível se for MySQL/SQL Server | Confirmar com Tech Lead/DBA: stack backend Java/Spring Boot usa PostgreSQL? | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-002 | Ferramenta de migração não confirmada (Flyway vs Liquibase). DDL gerado deve ser versionado. | Médio | Definir ferramenta e criar baseline migration V1__init.sql | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-003 | Sequence `seq_numero_os` referenciada em `fn_gerar_numero_os` não declarada no schema. | Médio | Criar sequence global ou por ano com reset anual via job | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-004 | Validação completa CPF/CNPJ (dígitos verificadores) apenas via regex no CHECK constraint. | Baixo | Implementar function PL/pgSQL `fn_validar_cpf/cnpj` + trigger ou mover para camada app | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-005 | Middleware para popular `app.current_user_id` e `app.client_ip` (auditoria) não implementado. | Alto — Auditoria incompleta | Implementar filter/interceptor Spring Boot | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-006 | Criptografia em repouso para colunas `sensitivity: restricted/confidential` não implementada. | Alto — LGPD | JPA AttributeConverter + AES-256-GCM ou TDE (volume criptografado) | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-007 | Índices BRIN em `health_check_disco.coletado_em` e `auditoria.created_at` assumem inserts ordenados. Backfill/out-of-order inserts degradam BRIN. | Médio | Monitorar `pg_stat_user_indexes` + `pg_brin_summarize`; fallback para B-tree se necessário | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| DB-008 | TODO em `AtivoService.java:119`: carrega até 1000 candidatos (id+nome) e faz ranking em memória. | Alto — Performance listagem | Implementar query otimizada com window function ou buscar paginado no DB | `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |

<!-- source: diagnostico#TODO-perf-AtivoService -->
<!-- source: db-schema-spec#9.7 -->
<!-- source: db-schema-spec#9.12 -->
<!-- source: nfr#NFR-SEC02 -->
<!-- source: nfr#NFR-C01 -->