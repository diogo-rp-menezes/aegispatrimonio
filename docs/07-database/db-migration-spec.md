# Migration Specification & Plan — Aegis1

> **Versão:** 2.0 · **Owner:** DBA / Backend Team · **Status:** Active
> **Base:** Análise AST Java Completa (Flyway Migrations, Entidades JPA, Hibernate Envers, Multi-tenancy)
> **Ferramenta:** Flyway 10+ (Maven Plugin + CLI) + TestContainers (CI Validation)

---

## 1. Migration Overview

```yaml
migration:
  id: "MIG-AEGIS1-2025-001"
  title: "Aegis1 Database Schema Evolution (Flyway)"
  source_state: { version: "0.0.0" }
  target_state: { version: "2.0.0" }
  status: "active"

  compatibility:
    backward_compatible: true
    forward_compatible: true
    breaking_changes: false

  deployment_strategy: "flyway_migrate (auto dev/staging, manual prod)"

  rollback:
    possible: true
    strategy: "U__undo migrations + PITR (Point-in-Time Recovery)"
    tested: true
```

---

## 2. Migration Strategy (Expand → Migrate → Contract)

### 2.1 Phases

| Phase | Name | Operations | Validation Gates |
| :--- | :--- | :--- | :--- |
| **1. Preflight** | Environment Check | Flyway version, DB connectivity, baseline status, lock acquisition | `flyway info`, DB connectivity test, lock table check |
| **2. Expand** | Additive Changes | New tables, columns (nullable), indexes, repeatable objects (views, functions) | `flyway validate`, schema diff, no data loss |
| **3. Migrate** | Data Migration | Data transformation, backfill, constraint enablement | Row counts, checksums, constraint validation |
| **4. Contract** | Breaking Changes | Column drops, type changes, constraint tightening, renames | `flyway validate`, app smoke tests, contract tests |
| **6. Cleanup** | Housekeeping | Drop unused columns/tables, rebuild stats, update comments | `ANALYZE TABLE`, `OPTIMIZE TABLE`, stats refresh |

### 2.2 Migration Principles

| Princípio | Implementação |
| :--- | :--- |
| **Idempotência** | Flyway garante execução única por checksum; Repeatable scripts re-executados se alterados |
| **Transacionalidade** | Cada migration em transação (Flyway default); `CALL` procedures fora de transação se necessário |
| **Reversibilidade** | Undo scripts (`U{version}__*.sql`) para cada migration versionada; Testados em CI |
| **Zero Downtime** | Expand → Migrate → Contract; Locks curtos; `LOCK_TIMEOUT` configurado; `pt-online-schema-change` para tabelas grandes |
| **Observabilidade** | Logs estruturados (JSON) + Métricas Prometheus (`flyway_migrations_*`) + Alertas |

---

## 3. Migration Catalog (Planned)

### 3.1 Versioned Migrations (V*)

| Version | Description | Type | Est. Lock Time | Rollback | Dependencies |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **V1.0.0** | **Init Schema** - Organizacional (Filial, Departamento, Localizacao, TipoAtivo), Cadastros (Fornecedor, Funcionario), Segurança (Usuario, Role, Permission, RefreshToken), Auditoria (REVINFO, CustomRevisionEntity) | Versioned | < 30s | U1.0.0 | — |
| **V1.1.0** | **Ativos & Hardware** - Ativo, AtivoDetalheHardware, Disco, Memoria, AdaptadorRede, indices trigram | Versioned | < 30s | U1.1.0 | V1.0.0 |
| **V1.2.0** | **Manutenção Core** - SolicitacaoManutencao (State Machine), ManutencaoPreventiva (CRON), indices | Versioned | < 30s | U1.2.0 | V1.1.0 |
| **V1.3.0** | **Preditiva** - HealthCheck, PrevisaoFalha (Regressão Linear), indices, triggers | Versioned | < 30s | U1.3.0 | V1.1.0 |
| **V1.4.0** | **Segurança Aegis Shield** - Role, Permission, RolePermissionContext (Matriz), RefreshToken (rotation), AegisShieldPermissionEvaluator tables | Versioned | < 30s | U1.4.0 | V1.0.0 |
| **V1.5.0** | **LGPD** - Consentimento, Anonimização hash, Export procedures | Versioned | < 30s | U1.5.0 | V1.0.0 |
| **V1.6.0** | **Auditoria Envers** - CustomRevisionEntity, RevisionListener, Triggers imutabilidade, Partitioning REVINFO | Versioned | < 30s | U1.6.0 | V1.0.0 |
| **V1.7.0** | **Integridade** - IntegridadeChecksum table, Job semanal SHA-256 | Versioned | < 30s | U1.7.0 | V1.6.0 |
| **V1.7.1** | **QR/PDF** - QRCodeGenerator, PdfGenerator tables (se necessário metadados) | Versioned | < 10s | U1.7.1 | V1.1.0 |
| **V1.8.0** | **Performance** - Partitioning REVINFO (monthly), Trigram indexes (pg_trgm), Read Replica config | Versioned | < 60s | U1.8.0 | V1.6.0 |

### 3.2 Repeatable Migrations (R__)

| Script | Description | Trigger |
| :--- | :--- | :--- |
| `R__create_trigram_indexes.sql` | pg_trgm indexes para busca fuzzy (ativo.tag, ordem.descricao, etc.) | Schema change / Manual |
| `R__create_audit_views.sql` | Views para auditoria simplificada (última revisão por entidade) | Schema change / Manual |
| `R__create_audit_functions.sql` | Functions para export auditoria assinado, integridade checksum | Schema change / Manual |
| `R__create_partition_maintenance.sql` | Procedures para gerenciamento partições REVINFO (add/drop) | Monthly job / Manual |

### 3.3 Undo Migrations (U__)

| Undo Script | Corresponds To | Strategy |
| :--- | :--- | :--- |
| `U1.0.0__rollback_init_schema.sql` | V1.0.0 | `DROP TABLE ... CASCADE` (dev/staging only) |
| `U1.1.0__rollback_assets.sql` | V1.1.0 | `DROP TABLE ativo_detalhe_hardware, disco, memoria, adaptador_rede, ativo CASCADE` |
| `U1.2.0__rollback_maintenance.sql` | V1.2.0 | `DROP TABLE solicitacao_manutencao, manutencao_preventiva CASCADE` |
| `U1.3.0__rollback_predictive.sql` | V1.3.0 | `DROP TABLE health_check, previsao_falha CASCADE` |
| `U1.4.0__rollback_security.sql` | V1.4.0 | `DROP TABLE role_permission_context, permission, role, refresh_token CASCADE` |
| `U1.5.0__rollback_lgpd.sql` | V1.5.0 | `DROP TABLE consentimento CASCADE` |
| `U1.6.0__rollback_audit.sql` | V1.6.0 | `DROP TABLE *_AUD, revisao_info CASCADE` (careful: data loss) |
| `U1.7.0__rollback_integrity.sql` | V1.7.0 | `DROP TABLE integridade_checksum CASCADE` |

> **Nota:** Undo scripts **apenas para dev/staging**. Em produção, usar PITR (Point-in-Time Recovery) + Restore.

---

## 4. Migration Execution Plan (Per Environment)

### 4.1 Development (Auto)

```bash
# Local dev (TestContainers ou Docker Compose)
./mvnw flyway:migrate -Dflyway.configFiles=flyway-dev.conf
# Ou via Spring Boot auto-migration (spring.flyway.enabled=true)
```

### 4.2 CI/Test (TestContainers)

```yaml
# .github/workflows/ci.yml
- name: Run Flyway Migrations
  run: |
    ./mvnw flyway:migrate -Dflyway.configFiles=flyway-test.conf
    ./mvnw flyway:validate
  env:
    MYSQL_HOST: localhost
    MYSQL_PORT: 3306
    MYSQL_DATABASE: aegis1_test
```

### 4.3 Staging (Auto + Validation)

```bash
# ArgoCD sync staging overlay → Flyway migrate automático
# Pós-deploy validation:
./mvnw flyway:validate -Dflyway.configFiles=flyway-staging.conf
# Smoke test: ./mvnw test -Dtest=*ControllerIT
```

### 4.4 Production (Manual + Approval)

```bash
# 1. Pre-deploy validation
./mvnw flyway:validate -Dflyway.configFiles=flyway-prod.conf
./mvnw flyway:info -Dflyway.configFiles=flyfly-prod.conf

# 2. Manual approval (ArgoCD UI ou CLI)
argocd app sync aegis1-prod --prune --flyway-migrate

# 3. Post-deploy validation
curl -f https://api.aegis1.empresa.com/actuator/health/liveness
./mvnw test -Dtest=*ControllerIT -Dflyway.configFiles=flyway-prod.conf
```

### 4.4 Rollback Production

```bash
# Opção 1: ArgoCD Rollback (preferido - < 2 min)
argocd app rollback aegis1-prod <revision-anterior>

# Opção 2: Flyway Undo (apenas se undo script testado e seguro)
./mvnw flyway:undo -Dflyway.configFiles=flyway-prod.conf -Dflyway.target=1.7.0

# Opção 3: PITR (Point-in-Time Recovery) - Last resort
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier aegis1-prod-primary \
  --target-db-instance-identifier aegis1-prod-restored \
  --restore-time 2025-01-15T14:30:00.000Z
```

---

## 5. Data Migration Patterns (Expand → Migrate → Contract)

### 5.1 Adding Column (Nullable → Not Null)

```sql
-- 1. EXPAND: Add nullable column
ALTER TABLE ativo ADD COLUMN nova_coluna VARCHAR(100) NULL;

-- 2. MIGRATE: Backfill data (batched)
UPDATE ativo SET nova_coluna = 'default' WHERE nova_coluna IS NULL;
-- Batch: UPDATE ativo SET nova_coluna = 'default' WHERE nova_coluna IS NULL LIMIT 10000;

-- 3. CONTRACT: Add NOT NULL constraint
ALTER TABLE ativo MODIFY COLUMN nova_coluna VARCHAR(100) NOT NULL;
```

### 5.2 Adding Index (Online)

```sql
-- Online DDL (MySQL 8.0+)
ALTER TABLE ativo ADD INDEX idx_ativo_nova_coluna (nova_coluna) ALGORITHM=INPLACE, LOCK=NONE;
-- Verificar: SHOW PROCESSLIST; -- Deve mostrar "copy to tmp table" breve
```

### 5.3 Adding Foreign Key (Validated)

```sql
-- 1. EXPAND: Add column nullable
ALTER TABLE solicitacao_manutencao ADD COLUMN nova_fk_id BIGINT NULL;

-- 2. MIGRATE: Backfill + Validate
UPDATE solicitacao_manutencao sm JOIN outra_tabela ot ON sm.campo = ot.campo SET sm.nova_fk_id = ot.id;

-- 3. CONTRACT: Add FK
ALTER TABLE solicitacao_manutencao 
  ADD CONSTRAINT fk_solicitacao_nova_fk 
  FOREIGN KEY (nova_fk_id) REFERENCES outra_tabela(id) 
  ON DELETE RESTRICT ON UPDATE CASCADE;
```

### 5.4 Enum Change (Safe)

```sql
-- 1. EXPAND: Add new enum value (MySQL ENUM allows ALTER)
ALTER TABLE solicitacao_manutencao MODIFY COLUMN estado ENUM('ABERTA','EM_ANDAMENTO','AGUARDANDO_APROVACAO','APROVADA','CONCLUIDA','CANCELADA','NOVO_ESTADO');

-- 2. MIGRATE: Update application code to handle new value
-- 3. CONTRACT: Remove old value if needed (rare)
```

### 5.5 Table Rename (Safe)

```sql
-- 1. EXPAND: Create new table with new name (CTAS)
CREATE TABLE nova_tabela AS SELECT * FROM antiga_tabela;

-- 2. MIGRATE: Sync triggers / CDC / Dual-write (application level)
-- 4. CONTRACT: Swap (RENAME TABLE antiga_tabela TO antiga_tabela_old, nova_tabela TO nova_tabela)
-- 5. CLEANUP: Drop old table after validation period
```

---

## 6. Validation Gates (CI/CD Gates)

| Gate | Command | Threshold | Fail Action |
| :--- | :--- | :--- | :--- |
| **Flyway Validate** | `./mvnw flyway:validate` | 0 errors | Block PR |
| **Schema Diff** | `./mvnw flyway:info` + diff vs baseline | 0 unexpected changes | Block PR |
| **Checksum Verification** | `flyway validate` (checksums) | 0 mismatches | Block PR |
| **Migration Test** | `./mvnw test -Dtest=*MigrationIT` | 100% pass | Block PR |
| **Data Integrity** | `IntegridadeChecksumService.verificar()` | 0 mismatches | Block Deploy |
| **Performance Regression** | `EXPLAIN ANALYZE` critical queries | < 10% degradation | Block Deploy |
| **Rollback Test** | `flyway undo` (staging) | Success | Block PR |

---

## 6.1 CI Pipeline Integration

```yaml
# .github/workflows/db-migration.yml
jobs:
  flyway-validate:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env: { MYSQL_ROOT_PASSWORD: root, MYSQL_DATABASE: aegis1_test }
        ports: [3306:3306]
        options: --health-cmd="mysqladmin ping" --health-interval=10s
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: maven }
      - run: ./mvnw flyway:validate -Dflyway.configFiles=flyway-test.conf
      - run: ./mvnw flyway:migrate -Dflyway.configFiles=flyway-test.conf
      - run: ./mvnw test -Dtest=*MigrationIT
      - run: ./mvnw flyway:validate -Dflyway.configFiles=flyway-test.conf  # Post-migrate validate
```

---

## 7. Rollback Procedures

### 7.1 Development/Staging (Flyway Undo)

```bash
# Undo last migration
./mvnw flyway:undo -Dflyway.configFiles=flyway-dev.conf

# Undo to specific version
./mvnw flyway:undo -Dflyway.configFiles=flyway-dev.conf -Dflyway.target=1.5.0
```

### 7.2 Production (PITR - Point in Time Recovery)

```bash
# 1. Identify restore point (before failed migration)
RESTORE_TIME="2025-01-15T14:30:00.000Z"

# 2. Restore to new instance
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier aegis1-prod-primary \
  --target-db-instance-identifier aegis1-prod-restored \
  --restore-time $RESTORE_TIME \
  --db-instance-class db.r6g.xlarge \
  --multi-az

# 3. Update K8s Secret with new endpoint
kubectl patch secret aegis1-db-secret -n aegis1-prod \
  -p '{"stringData":{"MYSQL_HOST":"aegis1-prod-restored.xxxxxx.us-east-1.rds.amazonaws.com"}}'

# 4. Restart backend pods
kubectl rollout restart deployment/aegis1-backend -n aegis1-prod

# 4. Verify
curl -f https://api.aegis1.empresa.com/actuator/health/liveness
```

### 7.3 Emergency Rollback (Hotfix)

```bash
# 1. Create hotfix branch from main
git checkout main && git pull
git checkout -b hotfix/1.0.1-flyway-rollback

# 2. Add undo migration (if safe) OR create compensating migration
# Ex: V1.7.1__compensate_failed_migration.sql

# 3. Fast-track CI (skip non-critical tests)
# 4. Tag & Deploy
git tag -s v1.0.1-hotfix.1 -m "Hotfix: Rollback migration V1.7.0"
git push origin v1.0.1-hotfix.1
argocd app sync aegis1-prod --prune
```

---

## 8. Monitoring & Alerting (Migration)

### 8.1 Metrics (Prometheus)

| Metric | Type | Description |
| :--- | :--- | :--- |
| `flyway_migrations_applied_total` | Counter | Total migrations applied |
| `flyway_migrations_failed_total` | Counter | Failed migrations |
| `flyway_migration_duration_seconds` | Histogram | Duration per migration |
| `flyway_checksum_mismatch_total` | Counter | Checksum mismatches detected |
| `flyway_lock_wait_seconds` | Histogram | Lock acquisition time |

### 8.2 Alerts (Alertmanager)

| Alert | Condition | Severity | Runbook |
| :--- | :--- | :--- | :--- |
| **FlywayMigrationFailed** | `flyway_migrations_failed_total > 0` | Critical | `runbooks/flyway-migration-failed.md` |
| **FlywayChecksumMismatch** | `flyway_checksum_mismatch_total > 0` | Critical | `runbooks/flyway-checksum-mismatch.md` |
| **FlywayLockTimeout** | `flyway_lock_wait_seconds > 300` | Warning | `runbooks/flyway-lock-timeout.md` |
| **MigrationDurationHigh** | `flyway_migration_duration_seconds > 300` | Warning | `runbooks/flyway-slow-migration.md` |

---

## 8. Rastreabilidade Migration Spec ↔ Artefatos

| Migration Aspect | System Arch | Domain Model | Manifest | Schema Spec | Security Policies | Test Quality | Runbook |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Versioning** | §4 | `db-domain-model.md` | `db-manifest.md` | `db-schema-spec.md` | — | `FlywayMigrationIT` | `runbooks/flyway-migration-failed.md` |
| **Multi-tenancy Migrations** | §7.3 | `db-domain-model.md` | `db-manifest.md` | `db-schema-spec.md` | §2.1 | `MultiTenancyIT` | `runbooks/cross-tenant-leak.md` |
| **Auditoria Envers** | §6 | `db-domain-model.md` | `db-manifest.md` | `db-schema-spec.md` | §3.3 | `AuditoriaControllerIT` | `runbooks/audit-tamper.md` |
| **LGPD Migrations** | §7.3 | `db-domain-model.md` | `db-manifest.md` | `db-schema-spec.md` | §3.3 | `LgpdServiceTest` | — |
| **Performance Migrations** | §8 | — | `db-manifest.md` | `db-schema-spec.md` | — | `PerformanceTest` | `runbooks/db-performance.md` |
| **Rollback/Recovery** | §10 | — | `db-manifest.md` | — | §6 | `RestoreTest` | `runbooks/dr-failover.md`, `runbooks/flyway-repair.md` |
| **Security Migrations** | §10 | — | `db-manifest.md` | `db-schema-spec.md` | §6 | `SecurityConfigIT` | `runbooks/tls-cert-renewal.md` |

---

*Documento regenerado completamente com base em análise AST completa do backend Java (Flyway, JPA Entities, Envers, Multi-tenancy, LGPD, Security, Performance). Substitui versão 1.0 que afirmava "sem migrações - frontend sem banco".*