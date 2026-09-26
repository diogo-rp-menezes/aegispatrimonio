# Database Manifest — Aegis1

> **Versão:** 2.0 · **Owner:** DBA / Backend Team · **Status:** Active
> **Base:** Análise AST Java Completa (Entidades JPA, Flyway Migrations, Hibernate Envers, Multi-tenancy)
> **Ponto de entrada da automação Database as Code (DBaC)**

```yaml
database:
  id: "aegis1-backend"
  name: "Aegis1 Backend Database"
  version: "2.0.0"
  status: "active"

  engine:
    vendor: "MySQL"
    version: "8.0.36+"
    compatibility: "MySQL 8.0+ (InnoDB, utf8mb4, utf8mb4_unicode_ci)"
    edition: "Community / Enterprise (RDS/Cloud SQL/Azure Database)"

  architecture:
    model: "relational"
    tenancy: "multi-tenant (Filial-level via Hibernate Filter)"
    consistency: "strong (ACID) + eventual (cache Redis)"
    partitioning: "horizontal (REVINFO monthly) + vertical (audit tables)"

  source_of_truth:
    type: "flyway_migrations"
    location: "src/main/resources/db/migration/"
    baseline: "V1__init_schema.sql"
    versioning: "semantic (V{major}.{minor}.{patch}__{description}.sql)"

  environments:
    - name: development
      purpose: "desenvolvimento local (TestContainers MySQL real)"
      url: "jdbc:mysql://localhost:3306/aegis1_dev"
      migrations: "auto (Flyway baselineOnMigrate=true)"
      data: "seeds determinísticos (scripts/seed-dev.sql)"
    - name: test
      purpose: "CI/CD pipeline (TestContainers efêmero)"
      url: "jdbc:tc:mysql:8.0:///aegis1_test"
      migrations: "auto (Flyway clean + migrate)"
      data: "fixtures JSON versionados (tests/fixtures/)"
    - name: staging
      purpose: "homologação pré-produção"
      url: "jdbc:mysql://staging-db.aegis1.internal:3306/aegis1_staging"
      migrations: "auto (Flyway baselineOnMigrate=true)"
      data: "dump anonimizado produção + seed controlado"
    - name: production
      purpose: "produção (Multi-AZ, Read Replicas)"
      url: "jdbc:mysql://prod-db.aegis1.internal:3306/aegis1_prod"
      migrations: "manual (Flyway migrate via ArgoCD hook)"
      data: "dados reais (somente leitura para sintéticos)"

  ownership:
    team: "Backend Team / DBA"
    service: "aegis1-backend"
    dba: "dba@aegis1.empresa.com"
    backup_owner: "DevOps Team"

  governance:
    migration_policy: "versioned_flyway (V{version}__{description}.sql)"
    destructive_changes: "forbidden (require U__undo migration + DBA approval)"
    drift_detection: true
    schema_validation: "flyway validate (CI gate)"
    baseline_policy: "baseline_on_migrate=true (dev/staging), manual (prod)"
    naming_convention: "V{major}.{minor}.{patch}__{description}.sql | R__{description}.sql | U{version}__{description}.sql"
    review_required: "2 reviewers (1 DBA + 1 Backend Lead) for schema changes"

  artifacts:
    root: "src/main/resources/db/migration/"
    schema: "docs/07-database/db-schema-spec.md"
    migrations: "src/main/resources/db/migration/"
    specifications: "docs/07-database/ (db-schema-spec.md, db-migration-spec.md, db-domain-model.md, db-performance-recovery.md, db-security-lifecycle.md, db-test-quality.md)"
    tests: "src/test/java/.../IT (TestContainers), scripts/integrity-check.sh"
    documentation: "docs/07-database/ (db-readme.md, db-context.md, db-domain-model.md, db-performance-recovery.md, db-security-lifecycle.md, db-test-quality.md)"

  compliance:
    lgpd: true
    nr10_nr12: true
    iso27001: true
    soc2: true
    retention_years: 7
    encryption_at_rest: "TDE (AES-256) + Column-level AES-256-GCM (PII)"
    encryption_in_transit: "TLS 1.3 (mTLS internal)"
    audit_retention_years: 7
    backup_retention_days: 30
    pitr_enabled: true
```

---

## 1. Schema Overview (Tabelas Principais)

| Categoria | Tabelas | Contagem Estimada | Particionamento |
| :--- | :--- | :---: | :--- |
| **Organizacional** | `filial`, `departamento`, `localizacao`, `tipo_ativo` | 4 | Não |
| **Patrimônio** | `ativo`, `ativo_detalhe_hardware`, `disco`, `memoria`, `adaptador_rede` | 5 | Não |
| **Cadastros** | `fornecedor`, `funcionario`, `tipo_ativo` | 3 | Não |
| **Manutenção** | `solicitacao_manutencao`, `manutencao_preventiva` | 2 | Não |
| **Preditiva** | `health_check`, `previsao_falha` | 2 | Não |
| **Segurança** | `usuario`, `role`, `permission`, `role_permission_context`, `refresh_token`, `consentimento` | 6 | Não |
| **Auditoria (Envers)** | `*_AUD` (25+ tabelas), `revisao_info` | 26+ | **REVINFO: Monthly (7 anos)** |
| **LGPD** | `consentimento` | 1 | Não |
| **Integridade** | `integridade_checksum` | 1 | Não |
| **Total Estimado** | **~55 tabelas** | | |

---

## 2. Multi-Tenancy Implementation

| Aspecto | Implementação |
| :--- | :--- |
| **Strategy** | Hibernate Filter (`@FilterDef` + `@Filter`) em TODAS entidades |
| **Filter Name** | `filialFilter` (parameter: `filialId` Long) |
| **Injection** | `MultiTenancyFilter` (OncePerRequestFilter) → `TenantContextHolder.set(filialId)` |
| **Admin Global** | `filialId = null` → Filter desativado (vê todas filiais) |
| **Validation** | ArchUnit test (CI) + TestContainers cross-tenant tests (build fail se vazamento) |
| **Performance** | Índice `filial_id` em TODAS tabelas; Query plan mostra `WHERE filial_id = ?` |

---

## 3. Auditoria (Hibernate Envers)

| Aspecto | Detalhes |
| :--- | :--- |
| **Coverage** | 100% entidades domínio (`@Audited` em todas) |
| **Tables** | `*_AUD` (mirror + `rev`, `revtype`) + `revisao_info` (custom) |
| **Custom Revision Entity** | `CustomRevisionEntity` (usuarioId, IP, UA, traceId, tipoRevisao, hashCorrelacao) |
| **Revision Types** | CREATE (0), UPDATE (1), DELETE (2), ANONIMIZACAO, TRANSFERENCIA_FILIAL, BAIXA_ATIVO, ORDEM_TRANSICAO |
| **Immutability** | Trigger `BEFORE UPDATE/DELETE ON *_AUD` → `SIGNAL SQLSTATE '45000'` |
| **Retention** | 7 anos (Partitioning `REVINFO` por mês; Job anual `DROP PARTITION`) |
| **Query API** | `AuditReader` (Envers) + `AuditoriaController` (REST + Export PDF/CSV assinado) |
| **LGPD Integration** | Anonimização preserva Envers + Hash correlação SHA-256(salt+id)[:8] |

---

## 4. Flyway Migration Strategy

### 4.1 Versioning Convention

| Tipo | Padrão | Exemplo |
| :--- | :--- | :--- |
| **Versioned** | `V{major}.{minor}.{patch}__{description}.sql` | `V1.0.0__init_schema.sql`, `V1.1.0__add_health_check.sql` |
| **Repeatable** | `R__{description}.sql` | `R__create_trigram_indexes.sql`, `R__create_views.sql` |
| **Undo** | `U{version}__{description}.sql` | `U1.1.0__rollback_health_check.sql` |

### 4.2 Migration Baseline

| Ambiente | Baseline |
| :--- | :--- |
| **Development** | `flyway baseline` automático (dev) |
| **Staging** | `flyway baseline` automático (staging) |
| **Production** | `flyway baseline` manual (DBA) - versão inicial `V1.0.0` |

### 4.3 Migration History (Planejado)

| Versão | Descrição | Arquivo |
| :--- | :--- | :--- |
| `V1.0.0` | Schema inicial (Organizacional, Ativos, Cadastros, Segurança, Auditoria) | `V1.0.0__init_schema.sql` |
| `V1.1.0` | Hardware (Disco, Memoria, AdaptadorRede) + HealthCheck | `V1.1.0__add_hardware_healthcheck.sql` |
| `V1.2.0` | Manutenção (Ordens, Preventiva) | `V1.2.0__add_maintenance.sql` |
| `V1.3.0` | Preditiva (HealthCheck, PrevisaoFalha, Regressão Linear) | `V1.3.0__add_predictive.sql` |
| `V1.4.0` | Segurança (Roles, Permissions, RefreshToken, Aegis Shield) | `V1.4.0__add_security_aegis_shield.sql` |
| `V1.5.0` | LGPD (Consentimento, Anonimização Hash) | `V1.5.0__add_lgpd.sql` |
| `V1.6.0` | Auditoria Envers (CustomRevisionEntity, Triggers imutabilidade) | `V1.6.0__add_auditoria_envers.sql` |
| `V1.7.0` | Integridade (Checksums, Job semanal) | `V1.7.0__add_integridade.sql` |
| `R__` | Trigram indexes, Views, Functions | `R__create_trigram_indexes.sql`, `R__create_audit_views.sql` |

---

## 5. Performance & Scalability Specs

### 5.1 Connection Pool (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### 5.2 Read Replicas (Reporting/Analytics)

```yaml
spring:
  datasource:
    read-replica:
      url: jdbc:mysql://replica-db.aegis1.internal:3306/aegis1
      username: ${MYSQL_READ_USER}
      password: ${MYSQL_READ_PASSWORD}
```

### 5.3 Partitioning (Auditoria)

```sql
-- REVINFO partitioned by month (7 years retention)
ALTER TABLE revisao_info PARTITION BY RANGE (YEAR(timestamp) * 100 + MONTH(timestamp)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    -- ... até p203112
    PARTITION pMax VALUES LESS THAN MAXVALUE
);
-- Job anual: DROP PARTITION p202501; ALTER TABLE ... ADD PARTITION p203201...
```

### 5.4 Trigram Indexes (Busca Fuzzy)

```sql
-- MySQL 8.0: pg_trgm via plugin ou FULLTEXT ngram
CREATE INDEX idx_ativo_trigram_tag ON ativo USING GIN (tag gin_trgm_ops);
CREATE INDEX idx_ordem_trigram_desc ON solicitacao_manutencao USING GIN (descricao gin_trgm_ops);
-- Ou ngram FULLTEXT nativo MySQL 8.0
ALTER TABLE ativo ADD FULLTEXT INDEX ft_tag_serial (tag, serial) WITH PARSER ngram;
```

---

## 6. Security & Compliance

### 6.1 Encryption

| Camada | Algoritmo | Gestão Chaves |
| :--- | :--- | :--- |
| **Em Trânsito** | TLS 1.3 (mTLS interno opcional) | cert-manager (Let's Encrypt / Vault PKI) |
| **Em Repouso (MySQL)** | TDE (AES-256) / Cloud Provider Encryption | Cloud KMS / Vault |
| **Colunas PII** | AES-256-GCM (column-level) | Vault Transit Engine / AWS KMS |
| **S3/MinIO** | SSE-S3 / SSE-KMS | AWS KMS / Vault |
| **JWT** | RS256 (2048-bit) | Vault PKI / cert-manager (rotação 90 dias) |

### 6.2 Compliance Mapping

| Regulamento | Controles Implementados |
| :--- | :--- |
| **LGPD** | Art. 7 (Base legal), Art. 18 (Esquecimento/Portabilidade), Art. 16 (Exceção auditoria), Art. 46-49 (Segurança) |
| **NR-10/12** | Termo Responsabilidade PDF assinado (placeholder ICP-Brasil), Checklist obrigatório fluxo Aprovar, Evidências armazenadas |
| **ISO 27001** | A.5.15 (Acesso), A.8.2 (Classificação), A.8.3 (Mídia), A.12.4 (Logs), A.14.2 (Segurança desenvolvimento) |
| **SOC 2 Type II** | Segurança, Disponibilidade, Confidencialidade (Evidências: Auditoria Envers + Logs + Pen Test) |

---

## 7. Backup & Disaster Recovery

| Componente | RTO | RPO | Estratégia |
| :--- | :--- | :--- | :--- |
| **MySQL Primary** | ≤ 60 min | ≤ 1 min | Automated Backup (RDS/Cloud SQL) + Binlog Replication + Cross-region Read Replica; Restore testado mensalmente |
| **Read Replicas** | ≤ 15 min | ≤ 1 min | Promoção automática (RDS/Cloud SQL) ou manual (`promote read replica`) |
| **Redis** | ≤ 15 min | ≤ 5 min | AOF + RDB Snapshots; Replica Multi-AZ; Cache warming script pós-restore |
| **S3/MinIO** | ≤ 30 min | 0 | Versioning habilitado; Cross-region Replication (CRR); Lifecycle policies |
| **K8s/Config** | ≤ 15 min | 0 | GitOps (ArgoCD) + Git repo (source of truth); `argocd app sync` rollback |
| **Secrets/Vault** | ≤ 15 min | 0 | Vault/SealedSecrets; Backup encrypted; Rotation automática (cert-manager TLS, JWT keys) |

---

## 8. Monitoring & Observability

### 8.1 Key Metrics (Prometheus + Grafana)

| Métrica | Target | Alerta |
| :--- | :--- | :--- |
| **DB Connections Usage** | < 70% pool | > 85% (5min) |
| **Query Latency P95** | < 100ms (OLTP) / < 500ms (Analytics) | > 500ms / > 2s |
| **Slow Query Count** | 0/min | > 5/min |
| **Replication Lag** | < 1s | > 10s (5min) |
| **Deadlocks/min** | 0 | > 1/min |
| **Buffer Pool Hit Ratio** | > 99% | < 95% |
| **Disk Usage** | < 70% | > 85% |
| **Backup Success** | 100% | Any failure |

### 8.3 Slow Query Detection

```yaml
# MySQL slow query log → Loki → Grafana Alert
# Threshold: 1s
# Alert: > 5 slow queries/5min → Investigação EXPLAIN ANALYZE
```

---

## 9. Rastreabilidade Manifest ↔ Artefatos

| Manifest Section | System Arch | Domain Model | Migration Spec | Schema Spec | Security Policies | Test Quality | Runbook |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Multi-tenancy** | §7.3 | `db-domain-model.md` | `V1__init_schema.sql` | `schema-spec.md` (filters) | §2.1, §5.2 | `MultiTenancyIT` | `runbooks/cross-tenant-leak.md` |
| **Auditoria Envers** | §6 | `db-domain-model.md` | `V1__init_auditoria.sql` | `schema-spec.md` (AUD tables) | §3.3 | `AuditoriaControllerIT` | `runbooks/audit-tamper.md` |
| **LGPD** | §7.3 | `db-domain-model.md` | `V8__lgpd.sql` | `schema-spec.md` | §3.3 | `LgpdServiceTest` | — |
| **Performance** | §8 | — | — | `schema-spec.md` (indexes) | — | `PerformanceTest` | `runbooks/db-performance.md` |
| **Backup/DR** | §10 | — | — | — | §6 (Infra) | `RestoreTest` | `runbooks/dr-failover.md` |
| **Security/Encryption** | §10 | — | — | `schema-spec.md` (encrypted cols) | §6 | `SecurityConfigIT` | `runbooks/tls-cert-renewal.md` |
| **Flyway Migrations** | §4 | `db-domain-model.md` | `db-migration-spec.md` | `schema-spec.md` | — | `FlywayMigrationIT` | `runbooks/flyway-repair.md` |

---

*Documento regenerado completamente com base em análise AST completa do backend Java (JPA Entities, Flyway, Hibernate Envers, Multi-tenancy, LGPD, Security, Performance). Substitui versão 1.0 que afirmava "frontend sem banco de dados".*