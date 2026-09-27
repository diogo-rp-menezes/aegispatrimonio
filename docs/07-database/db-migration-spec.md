# Migration Specification & Plan — MIG-20250115-001

> **Versão:** 1.0 · **Status:** Proposed
> Migração inicial de criação do schema completo do Aegis Patrimônio conforme [[db-schema-spec]] v1.0.
> **⚠️ PRÉ-REQUISITO CRÍTICO:** Decisão do SGBD alvo (MOTOR-001) e estratégia de ORM/migração (ORM-001) devem estar resolvidas antes da execução.

```yaml
migration:
  id: "MIG-20250115-001"
  title: "Initial schema creation for Aegis Patrimônio asset management system"
  source_state: { version: "0.0.0 (empty database)" }
  target_state: { version: "1.0.0 (full schema per db-schema-spec v1.0)" }

  changes:
    - type: "CREATE"
      object: "19 tables (core, reference, transactional, supporting, audit)"
      description: "Create all tables defined in db-schema-spec sections 1.1–1.19 with logical types; physical type mapping deferred to SGBD decision"
    - type: "CREATE"
      object: "35 foreign key relationships"
      description: "Add FK constraints per db-schema-spec section 2 (REL-001 to REL-036) with defined on_delete/on_update semantics"
    - type: "CREATE"
      object: "56 constraints (UNIQUE, CHECK)"
      description: "Add all constraints per db-schema-spec section 3 (CONSTRAINT-001 to CONSTRAINT-056)"
    - type: "CREATE"
      object: "35 indexes"
      description: "Create indexes per db-schema-spec section 4 (INDEX-001 to INDEX-035) with documented query-pattern evidence"
    - type: "CREATE"
      object: "4 views (1 materialized)"
      description: "Create views per db-schema-spec section 5: vw_ativo_completo, vw_manutencao_completa, vw_alerta_ativo, vw_custo_manutencao_por_ativo (materialized)"
    - type: "CREATE"
      object: "4 functions"
      description: "Create functions per db-schema-spec section 6: validar_cpf, validar_cnpj, calcular_hash_encadeado_auditoria, anonymize_user, atualizar_custo_total_manutencao"
    - type: "CREATE"
      object: "6 triggers"
      description: "Create triggers per db-schema-spec section 7: audit hash chain, audit append-only enforcement, manutencao custo_total sync, funcionario ativo sync, usuario_role audit, role_permissao audit"

  compatibility:
    backward_compatible: true
    forward_compatible: true

  deployment_strategy: "expand-contract"

  rollback:
    possible: true
    strategy: "Full schema drop in reverse dependency order (triggers → functions → views → indexes → constraints → FKs → tables). Requires zero active connections. Data loss: total (initial migration)."
```

## 2. Plano Operacional (Expand → Migrate → Contract)

| Fase | Nome | Operações | Checks |
| :--- | :--- | :--- | :--- |
| 1 | Preflight | Verify SGBD decision (MOTOR-001) documented; ORM/migration tool chosen (ORM-001); target database empty; backup strategy confirmed; maintenance window scheduled | `SELECT version();` confirms target SGBD; `pg_isready` / equivalent OK; no existing objects in target schema |
| 2 | Schema Preparation (Expand) | **2a** Create custom ENUM types (if native ENUM chosen per ENUM-001) or CHECK constraints for all logical enums<br>**2b** Create 19 tables with columns, PKs, logical types (physical mapping per SGBD)<br>**2c** Add 35 FK constraints (REL-001 to REL-036)<br>**2d** Add 56 constraints (CONSTRAINT-001 to CONSTRAINT-056)<br>**2e** Create 4 functions (validar_cpf, validar_cnpj, calcular_hash_encadeado_auditoria, anonymize_user, atualizar_custo_total_manutencao)<br>**2f** Create 6 triggers | Each step: `EXPLAIN`/`SHOW CREATE TABLE` verification; FK referential integrity test with sample inserts; constraint violation tests; function execution tests |
| 3 | Data Transformation | **3a** Seed reference data: tipo_ativo (6 categories), role (ADMIN, GESTOR_PATRIMONIO, TECNICO_MANUTENCAO, VISUALIZADOR), permissao (full RBAC matrix), filial (initial units)<br>**3b** Create initial admin usuario + funcionario link<br>**3c** Grant initial usuario_role + role_permissao<br>**3d** Refresh materialized view vw_custo_manutencao_por_ativo | Row counts match seed scripts; FK references valid; RBAC matrix complete; materialized view populates without error |
| 4 | Application Compatibility | **4a** Deploy application version compatible with schema v1.0.0 (JPA entities mapped to new tables per ASM-017)<br>**4b** Run integration test suite against migrated schema<br>**4c** Verify AtivoMapper.toDTO (ASM-020) works with vw_ativo_completo<br>**4d** Verify ManutencaoSpecification.build (ASM-019) uses INDEX-010<br>**4e** Verify AlertNotificationService.checkResourceUsageAlerts (ASM-005) writes to alerta table | All integration tests pass; p95 latency ≤ 50ms for ManutencaoSpecification queries; health check collection writes to ativo.ultimo_health_check; audit trail captures CREATE actions with hash_encadeado |
| 5 | Validation | **5a** Schema comparison: actual vs db-schema-spec (all 19 tables, 35 FKs, 56 constraints, 35 indexes, 4 views, 4 functions, 6 triggers)<br>**5b** Constraint validation: attempt invalid inserts for each CHECK/UNIQUE — all rejected<br>**5c** Index usage: `EXPLAIN ANALYZE` on representative queries (INDEX-001 to INDEX-035) — index scans confirmed<br>**5d** Audit immutability: attempt UPDATE/DELETE on auditoria — blocked by trigger<br>**5e** Hash chain integrity: verify SHA-256 chain monotonic per correlation_id<br>**5f** Materialized view refresh: `REFRESH MATERIALIZED VIEW CONCURRENTLY vw_custo_manutencao_por_ativo` completes < 2s (ASM-018) | Automated schema diff tool reports zero drift; all constraint tests pass; all query plans show index usage; audit trigger blocks writes; hash chain valid; MV refresh < 2s |
| 6 | Cleanup (Contract) | **6a** Drop any temporary staging tables used during seed<br>**6b** Revoke excessive privileges granted for migration (if any)<br>**6c** Document final schema version in schema_version table<br>**6d** Update [[db-readme]] with applied migration ID and timestamp | No orphan objects; least-privilege roles verified; schema_version record exists; documentation updated |

## 3. Risk Assessment

```yaml
risk:
  overall_level: "HIGH"
  dimensions:
    data_loss:
      score: 0
      evidence: ["Initial migration on empty database — no existing data at risk"]
    downtime:
      score: 3
      evidence: ["Requires maintenance window for schema creation + seed + app deploy; estimated 30-60 min for full pipeline on empty DB; zero-downtime not applicable for initial creation"]
    locking:
      score: 2
      evidence: ["CREATE TABLE/INDEX/CONSTRAINT on empty tables — no lock contention; FK creation may briefly lock parent tables during validation but tables are empty"]
    compatibility:
      score: 4
      evidence: ["CRITICAL: Physical type mappings (UUID, JSONB, ENUM, INET, MACADDR, TIMESTAMP WITH TIME ZONE) depend on unresolved MOTOR-001; JPA/Hibernate mapping (ASM-017) must align with chosen SGBD dialects; ENUM-001 decision affects all 15 logical enums; JSON-001 affects 4 columns across 3 tables"]
    reversibility:
      score: 2
      evidence: ["Full rollback via DROP SCHEMA CASCADE possible but destroys all seeded reference data; rollback tested in staging required"]
  destructive_operations: []
  required_controls:
    - "MOTOR-001 decision documented and approved before Phase 2 start"
    - "ENUM-001 implementation strategy (native type vs CHECK vs reference table) decided and tested"
    - "JSON-001 support confirmed in target SGBD (JSONB/JSON/TEXT+validation)"
    - "Partitioning strategy (PART-001) defined for ativo, manutencao, alerta, auditoria — may require DDL adjustments post-initial-creation"
    - "WORM storage (WORM-001) provisioned for auditoria table before production traffic"
    - "RLS/Security views (SEC-001) designed for filial_id segregation"
    - "Index validation (IDX-001) with realistic data volumes (100k+ ativos, 1M+ manutencoes) in staging"
    - "FK-001 review: all on_delete/on_update semantics validated against business rules"
    - "SEQ-001: Business ID generation (tag_patrimonial, numero_ordem, codigo) implemented via sequence/trigger/app"
```

## 4. Approval Gate

| Campo | Valor |
| :--- | :--- |
| Aprovação obrigatória? | Sim |
| Motivo | Migração inicial de alto risco: decisões de arquitetura pendentes (MOTOR-001, ENUM-001, JSON-001, PART-001, WORM-001, SEC-001) impactam fisicamente todo o DDL; rollback destrutivo; compatibilidade com JPA/Hibernate (ASM-017) não verificada |
| Aprovado por | [Arquiteto de Dados / Tech Lead / DBA / SecOps] |
| Status | Pending |

> **Checklist de pré-aprovação (todos obrigatórios):**
> - [ ] MOTOR-001: SGBD alvo definido (PostgreSQL / Oracle / SQL Server / MySQL)
> - [ ] ORM-001: Ferramenta de migração escolhida (Flyway / Liquibase / JPA DDL generation / SQL scripts)
> - [ ] ENUM-001: Estratégia de ENUM decidida e testada no SGBD alvo
> - [ ] JSON-001: Suporte a JSONB/JSON confirmado; mapeamento físico documentado
> - [ ] PART-001: Estratégia de particionamento definida para 4 tabelas grandes
> - [ ] WORM-001: Storage imutável provisionado para auditoria
> - [ ] SEC-001: Modelo de segregação por filial_id (RLS/views) aprovado
> - [ ] SEQ-001: Geração de IDs de negócio implementada
> - [ ] FK-001: Semânticas on_delete/on_update revisadas e aprovadas
> - [ ] Staging environment provisionado com SGBD alvo idêntico a produção

## 5. Verification (pós-execução)

> **Preencher somente após a migration ser aplicada — checks de schema, integridade, compatibilidade e performance**

| Check | Método | Critério de Sucesso | Resultado | Evidência |
| :--- | :--- | :--- | :--- | :--- |
| **Schema completeness** | Automated diff (schemadiff / pg_dump --schema-only vs spec) | Zero drift vs db-schema-spec v1.0 | [ ] Pass / [ ] Fail | [arquivo de diff / log] |
| **Constraint enforcement** | Test suite: 56 negative test cases (one per CONSTRAINT-XXX) | All invalid inserts rejected with correct error codes | [ ] Pass / [ ] Fail | [test report] |
| **FK referential integrity** | Insert valid/invalid FK references across all 35 relationships | Valid inserts succeed; invalid rejected | [ ] Pass / [ ] Fail | [test report] |
| **Index usage** | `EXPLAIN ANALYZE` on 15 representative queries (covering INDEX-001 to INDEX-035) | All queries use expected index (Index Scan / Bitmap Index Scan); no Seq Scan on large tables | [ ] Pass / [ ] Fail | [query plans] |
| **Audit immutability** | Attempt UPDATE/DELETE on auditoria table | Trigger `trg_auditoria_prevent_update_delete` blocks with clear error | [ ] Pass / [ ] Fail | [error message log] |
| **Hash chain integrity** | Verify `hash_encadeado` = SHA256(prev_hash \|\| current_payload) for all rows per correlation_id | 100% chains valid; monotonic ordering by data_hora | [ ] Pass / [ ] Fail | [verification script output] |
| **Materialized view refresh** | `REFRESH MATERIALIZED VIEW CONCURRENTLY vw_custo_manutencao_por_ativo` | Completes < 2s (ASM-018); data matches manual aggregation | [ ] Pass / [ ] Fail | [timing + data comparison] |
| **Application integration** | Full integration test suite (JPA entities, mappers, specifications, services) | All tests pass; p95 latency targets met (ManutencaoSpecification ≤ 50ms, custoTotalPorAtivo ≤ 2s) | [ ] Pass / [ ] Fail | [CI/CD pipeline results] |
| **RBAC functionality** | Test role/permission resolution for all 4 default roles | Correct permission sets resolved; usuario_role/role_permissao triggers audit correctly | [ ] Pass / [ ] Fail | [test report] |
| **Seed data validity** | Count rows in all reference tables; verify FK links | Expected row counts; no orphan references | [ ] Pass / [ ] Fail | [row counts + FK check queries] |

---

## Apêndice: Mapeamento de Tipos Lógicos → Físicos (Pendente MOTOR-001)

> **Fonte:** [[db-schema-spec]] seções 1.1–1.19 — todos os tipos abaixo são **LÓGICOS**. A conversão física **deve** ser definida na decisão MOTOR-001 antes da Fase 2.

| Tipo Lógico | PostgreSQL | Oracle | SQL Server | MySQL 8.0+ | Observação |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TechnicalID` (UUID) | `uuid` (gen_random_uuid) | `RAW(16)` / `SYS_GUID()` | `UNIQUEIDENTIFIER` (NEWID/NEWSEQUENTIALID) | `CHAR(36)` / `BINARY(16)` | Requer extensão `uuid-ossp` ou `pgcrypto` no PG |
| `BusinessID` / `BusinessCode` | `VARCHAR(n)` | `VARCHAR2(n)` | `NVARCHAR(n)` | `VARCHAR(n)` | — |
| `SerialNumber` | `VARCHAR(n)` | `VARCHAR2(n)` | `NVARCHAR(n)` | `VARCHAR(n)` | — |
| `Name` / `Title` / `Description` / `String` / `Address` / `City` / `StateCode` / `ZipCode` / `Phone` / `UserAgent` | `VARCHAR(n)` / `TEXT` | `VARCHAR2(n)` / `CLOB` | `NVARCHAR(n)` / `NVARCHAR(MAX)` | `VARCHAR(n)` / `TEXT` | TEXT/CLOB para > 4000 chars |
| `ForeignKey` | `uuid` | `RAW(16)` | `UNIQUEIDENTIFIER` | `CHAR(36)` | Mesmo tipo do PK referenciado |
| `LocalDate` | `DATE` | `DATE` | `DATE` | `DATE` | — |
| `Instant` (TIMESTAMP WITH TIME ZONE) | `TIMESTAMPTZ` | `TIMESTAMP WITH TIME ZONE` | `DATETIMEOFFSET` | `TIMESTAMP(6)` | MySQL não tem TZ nativo — armazenar UTC + coluna tz separada ou usar `DATETIME(6)` UTC |
| `Money` / `DecimalRate` / `Percentage` | `NUMERIC(p,s)` | `NUMBER(p,s)` | `DECIMAL(p,s)` | `DECIMAL(p,s)` | Precisão fixa obrigatória para financeiro |
| `PositiveInteger` / `Counter` | `INTEGER` + `CHECK (>0)` | `NUMBER(10)` + `CHECK` | `INT` + `CHECK` | `INT` + `CHECK` | — |
| `BooleanFlag` | `BOOLEAN` | `NUMBER(1)` (0/1) | `BIT` | `BOOLEAN` / `TINYINT(1)` | Oracle/MySQL não têm BOOLEAN nativo verdadeiro |
| `StatusAtivo` / `CondicaoAtivo` / `TipoManutencao` / `StatusManutencao` / `Prioridade` / `TipoAlerta` / `Severidade` / `StatusAlerta` / `CategoriaAtivo` / `TipoDisco` / `TipoAdaptadorRede` / `AuditAction` | **Ver ENUM-001** | **Ver ENUM-001** | **Ver ENUM-001** | **Ver ENUM-001** | Decisão única para todos os 15 enums lógicos |
| `JSONMetadata` / `JSONSnapshot` | `JSONB` | `JSON` (21c+) / `BLOB` + `IS JSON` | `NVARCHAR(MAX)` + `ISJSON` | `JSON` | PG: JSONB (binário, indexável); outros: validar suporte |
| `MACAddress` | `MACADDR` / `MACADDR8` | `VARCHAR(17)` + `CHECK REGEXP` | `VARCHAR(17)` + `CHECK` | `VARCHAR(17)` + `CHECK` | PG nativo valida formato; outros via CHECK |
| `InetAddress` (IPv4/IPv6) | `INET` / `CIDR` | `VARCHAR(45)` + `CHECK` | `VARCHAR(45)` + `CHECK` | `VARCHAR(45)` + `CHECK` | PG nativo suporta operações de rede |
| `CPF` / `CNPJ` | `VARCHAR(14/18)` + `CHECK` + function | `VARCHAR2` + `CHECK` + function | `NVARCHAR` + `CHECK` + function | `VARCHAR` + `CHECK` + function | Validação algorítmica via function (validar_cpf/validar_cnpj) |
| `Email` | `VARCHAR(255)` + `CHECK` regex | `VARCHAR2(255)` + `CHECK` | `NVARCHAR(255)` + `CHECK` | `VARCHAR(255)` + `CHECK` | Regex básica; validação completa na app |
| `PasswordHash` | `VARCHAR(255)` | `VARCHAR2(255)` | `NVARCHAR(255)` | `VARCHAR(255)` | Armazenar apenas hash (bcrypt/argon2) |
| `CorrelationID` / `EntityID` | `uuid` | `RAW(16)` | `UNIQUEIDENTIFIER` | `CHAR(36)` | Mesmo que TechnicalID |
| `HashChain` | `CHAR(64)` | `CHAR(64)` | `CHAR(64)` | `CHAR(64)` | SHA-256 hex (64 chars) |
| `ResourceName` / `ActionName` | `VARCHAR(50/100)` | `VARCHAR2` | `NVARCHAR` | `VARCHAR` | — |

---

## Apêndice: Ordem de Criação (Respeitando Dependências FK)

```text
-- Nível 0: Sem dependências (tabelas de referência puras)
1. filial
2. tipo_ativo
3. role
4. permissao
5. fornecedor

-- Nível 1: Dependem apenas do Nível 0
6. departamento      (FK → filial)
7. localizacao       (FK → filial)
8. usuario           (FK → usuario [created_by/updated_by] — auto-referência; FK → funcionario [opcional])
9. funcionario       (FK → filial, departamento, usuario [created_by/updated_by])

-- Nível 2: Dependem do Nível 1
10. ativo            (FK → tipo_ativo, filial, departamento, localizacao, fornecedor, funcionario, usuario [created_by/updated_by])
11. usuario_role     (FK → usuario, role, usuario [concedido_por])
12. role_permissao   (FK → role, permissao, usuario [concedido_por])

-- Nível 3: Dependem do Nível 2
13. manutencao       (FK → ativo, fornecedor, funcionario, usuario [aprovador, created_by, updated_by])
14. alerta           (FK → ativo, usuario [leitura, resolucao])
15. ativo_detalhe_hardware (FK → ativo [UNIQUE 1:1])

-- Nível 4: Dependem do Nível 3
16. adaptador_rede   (FK → ativo_detalhe_hardware [ON DELETE CASCADE])
17. disco            (FK → ativo_detalhe_hardware [ON DELETE CASCADE])
18. memoria          (FK → ativo_detalhe_hardware [ON DELETE CASCADE])

-- Nível 5: Auditoria (depende de usuario, mas pode ser criada por último)
19. auditoria        (FK → usuario)

-- Objetos dependentes (criar após tabelas)
20. Functions (5)
21. Triggers (6)
22. Indexes (35) — podem ser criados após tabelas + dados de seed para estatísticas
23. Views (4)
24. Materialized View (1) — refrescar após seed de manutencao
```

---

## Apêndice: Scripts de Validação Pós-Migração (Exemplos)

```sql
-- 1. Verificar contagem de objetos criados
SELECT 
  (SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE') AS tables,
  (SELECT count(*) FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY' AND table_schema = 'public') AS fks,
  (SELECT count(*) FROM information_schema.table_constraints WHERE constraint_type IN ('UNIQUE','CHECK') AND table_schema = 'public') AS constraints,
  (SELECT count(*) FROM pg_indexes WHERE schemaname = 'public') AS indexes,
  (SELECT count(*) FROM information_schema.views WHERE table_schema = 'public') AS views,
  (SELECT count(*) FROM information_schema.routines WHERE routine_schema = 'public') AS functions;

-- 2. Verificar integridade da hash chain (amostragem)
WITH chain AS (
  SELECT 
    correlation_id,
    data_hora,
    hash_encadeado,
    LAG(hash_encadeado) OVER (PARTITION BY correlation_id ORDER BY data_hora) AS prev_hash,
    valores_anteriores,
    valores_novos,
    metadados
  FROM auditoria
  WHERE correlation_id IS NOT NULL
)
SELECT correlation_id, count(*) AS breaks
FROM chain
WHERE prev_hash IS NOT NULL
  AND hash_encadeado <> calcular_hash_encadeado_auditoria(prev_hash, 
       jsonb_build_object('old', valores_anteriores, 'new', valores_novos, 'meta', metadados))
GROUP BY correlation_id
HAVING count(*) > 0;

-- 3. Verificar uso de índices nas queries críticas (exemplo INDEX-010)
EXPLAIN ANALYZE
SELECT * FROM manutencao 
WHERE ativo_id = 'uuid-exemplo' 
  AND status = 'EM_ANDAMENTO' 
  AND tipo = 'CORRETIVA' 
  AND data_abertura >= '2025-01-01'
ORDER BY data_abertura DESC
LIMIT 20;

-- 4. Verificar materialized view
SELECT count(*) FROM vw_custo_manutencao_por_ativo;
REFRESH MATERIALIZED VIEW CONCURRENTLY vw_custo_manutencao_por_ativo;
```

---

## Apêndice: Correção M-12 — Índice `idx_alertas_ativo_id` (2026-09-27)

**Problema:** a V15 criava `idx_alertas_ativo_id` sem guarda idempotente. Em produção legada
(onde a tabela `alertas` foi criada pelo Hibernate `ddl-auto=update`), o índice já existe e a
migration falhava por "duplicate index" — bloqueador de deploy.

**Decisão técnica:** nenhuma guarda SQL única é portável entre H2 2.3 (SeedDataIT/dev,
MODE=MySQL) e MySQL 8 (prod) — validado empiricamente:

- H2 2.3 não suporta `information_schema.statistics` nem `PREPARE stmt FROM @var`;
  `DATABASE()` retorna nome lowercase que não casa com `table_schema = 'PUBLIC'`.
- MySQL 8 não suporta `CREATE INDEX IF NOT EXISTS`, `EXECUTE IMMEDIATE` nem
  `information_schema.indexes`.

Solução adotada: **migration Java** `V17__create_alertas_index`
(`src/main/java/db/migration/V17__create_alertas_index.java`), que verifica a existência do
índice via JDBC `DatabaseMetaData.getIndexInfo()` (portável para qualquer SGBD) e cria o índice
apenas se ausente. A V15 foi ajustada para não criar o índice (apenas as tabelas, com
`IF NOT EXISTS`).

**Pontos de atenção:**

1. **Checksum da V15 mudou** — em bancos locais que já aplicaram a V15 antiga, executar
   `flyway repair` antes de `flyway migrate`.
2. **Validação em MySQL real fica como passo de deploy** — a migration foi validada em H2
   (SeedDataIT, BUILD SUCCESS, V17 aplicada). Antes do deploy em produção, validar em MySQL 8:
   - Banco novo: `flyway migrate` cria o índice normalmente.
   - Banco legado com índice pré-existente: a guarda via `DatabaseMetaData` detecta e ignora
     (sem erro de índice duplicado).
3. **Pacote obrigatório:** a classe Java deve ficar em `db.migration` (não em
   `br.com.aegispatrimonio.*`) porque o Flyway mapeia a location `classpath:db/migration`
   para esse pacote ao escanear migrations Java.
4. **Não fechar a `Connection` do `Context`** — ela pertence ao Flyway; fechá-la quebra o
   commit da migration ("Connection is closed").

---

> **Nota:** A execução real (SQL, rollback automático, checagem contra banco efêmero) depende de um motor determinístico de execução/validação — este documento é o contrato e o registro, não o executor. Ver limitações em [[db-readme]].