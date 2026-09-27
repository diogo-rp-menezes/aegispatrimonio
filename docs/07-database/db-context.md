# Database Context — aegis_patrimonio

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Dados · **Status:** Draft

## 1. Purpose
Persistir e gerenciar o estado transacional do domínio de gestão de patrimônio (ativos, manutenções, alertas, usuários, auditoria) com garantias ACID, rastreabilidade total (LGPD/SOX) e suporte a consultas analíticas offloadadas para read-replica. O banco é a *source of truth* única do monolito Spring Boot `aegis-patrimonio` — não há event sourcing nem CQRS; toda mutação passa por transações relacionais.

## 2. System
Sistema monolítico **Aegis Patrimônio** (Spring Boot, pacote `br.com.aegispatrimonio`), referenciado em [[system-architecture]] §3, §5, §7, §9, §12, §13. Frontend consome via REST (JavaScript em `frontend/src/services/api.js`). Jobs de batch (seeder, alertas, relatórios) executam no mesmo processo/JVM.

## 3. Responsibilities
- Armazenar entidades de domínio: `Ativo`, `Manutencao`, `Alerta`, `Usuario`, `Auditoria` (tabelas correspondentes mapeadas via JPA/Hibernate — inferido da estrutura de pacotes `model`, `repository`, `mapper`).
- Garantir integridade referencial e constraints de negócio (chaves estrangeiras, checks, unique keys) — consistência forte (manifesto § `architecture.consistency: "strong"`).
- Suportar queries dinâmicas de manutenção (`ManutencaoSpecification.build`, complexidade ciclomática 14) via índices compostos otimizados.
- Fornecer read-replica dedicada para relatórios (`custoTotalPorAtivo`, health checks) sem impactar o primário.
- Manter logs de auditoria imutáveis (WORM) por 7 anos (SOX) em armazenamento separado do banco transacional.
- Servir como *source of truth* versionado via migrations (Flyway/Liquibase) em `database/migrations/`.

## 4. Non-Responsibilities
- **Cache de sessão/token** — responsabilidade de camada externa (Spring Security + JWT em memória/Redis, não no banco).
- **Armazenamento de blobs/arquivos grandes** — binários (fotos de ativos, manuais PDF) ficam em object storage (S3/MinIO); banco guarda apenas metadados + URI.
- **Event streaming / message broker** — não há tabelas de outbox ou CDC; integrações assíncronas (se houver) usam broker dedicado.
- **Dados de telemetria/métricas de infra** — Prometheus/Grafana/Loki fora do escopo deste banco.
- **Multi-tenancy / isolamento por cliente** — arquitetura single-tenant (manifesto § `architecture.tenancy: "single"`).

## 5. Domain
Domínio **Gestão de Patrimônio Corporativo** — ativos (hardware, software, móveis), ciclo de vida de manutenções (preventiva/corretiva), alertas de uso/recursos, controle de acesso (RBAC), trilha de auditoria. Termos canônicos em [[glossario]] (ex.: `Ativo`, `Manutencao`, `Alerta`, `Usuario`, `CustoTotalPorAtivo`, `WORM`).

## 6. Consumers
| Consumer | Tipo | Responsabilidade |
| :--- | :--- | :--- |
| `aegis-patrimonio` (Spring Boot) | API (REST) + Batch | CRUD transacional, jobs agendados (seeder, `AlertNotificationService.checkResourceUsageAlerts`), relatórios operacionais |
| `aegis-patrimonio-read-replica` | Analytics (read-only) | Offload de queries analíticas (`custoTotalPorAtivo`, dashboards, health checks) — <!-- source: db-manifest#L58 --> |
| `audit-worm-storage` | Write-once (append-only) | Recebe cópia imutável de `Auditoria` para retenção SOX 7 anos — <!-- source: db-manifest#L61 --> |
| CI/CD Pipeline | Validation | `schema_validation: true`, `drift_detection: true` no deploy — <!-- source: db-manifest#L48-L49 --> |

## 7. Data Ownership
| Conjunto de Dados | Owner (Time/Papel) | Observação |
| :--- | :--- | :--- |
| `Ativo`, `Manutencao`, `Alerta` | Engenharia de Dados / Product Owner Patrimônio | Domínio core — mudanças de schema via migration controlada |
| `Usuario`, `Role`, `Permissao` | Segurança / IAM | RBAC — alterações exigem aprovação Security + Platform |
| `Auditoria` (tabela transacional) | Engenharia de Dados | Retenção curta (janela quente); cópia WORM vai para storage separado |
| `Auditoria` (WORM) | Compliance / Legal | Imutável, 7 anos, fora do banco transacional — <!-- source: db-manifest#L61 --> |
| Migrations / Schema versioning | Engenharia de Dados / DBA | `database/migrations/` como source of truth — <!-- source: db-manifest#L30 --> |

## 8. Consistency Requirements
| Dado / Operação | Nível | Justificativa |
| :--- | :--- | :--- |
| Criação/atualização de `Ativo`, `Manutencao`, `Alerta` | **Forte (ACID)** | Transações financeiras/patrimoniais — manifesto § `architecture.consistency: "strong"` |
| Atribuição/remoção de `Role`/`Permissao` em `Usuario` | **Forte** | Segurança/RBAC — inconsistência = acesso indevido |
| Escrita em `Auditoria` (tabela transacional) | **Forte** | Trilha confiável; falha na auditoria = rollback da operação principal |
| Replicação para read-replica | **Eventual (segundos)** | Offload analítico — staleness aceitável para relatórios — <!-- source: db-manifest#L58 --> |
| Cópia para WORM storage | **Eventual (assíncrona, garantida)** | Imutabilidade SOX; falha de cópia alerta mas não bloqueia transação — <!-- source: db-manifest#L61 --> |

## 9. Availability Requirements
- **Primário (writer):** 99.9% mensal (≈ 43 min/mês downtime) — SLA alinhado a NFR-A03 no [[system-architecture]] §7.
- **Read-replica:** 99.95% — falha da réplica não derruba escrita; fallback para primário em queries analíticas (degradado).
- **Janela de manutenção:** Domingos 02:00–06:00 UTC (migrações, vacuum, backup lógico) — coordenado com Platform.
- **RPO/RTO:** RPO = 0 (replicação síncrona para standby se PostgreSQL/Oracle; caso MySQL/SQL Server, semi-síncrono com perda máxima de 1 transação). RTO < 15 min (failover automático + health check).

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** SLA numérico (99.9%) inferido de práticas comuns para sistemas patrimoniais críticos; não consta explicitamente no diagnóstico. Validar com Product/Platform.

## 10. Performance Requirements
| Métrica | Target | Contexto |
| :--- | :--- | :--- |
| Latência p95 `SELECT` simples (PK) | ≤ 5 ms | Pool HikariCP 500 conexões — <!-- source: db-manifest#L64 --> |
| Latência p95 query dinâmica `ManutencaoSpecification` | ≤ 50 ms | Índices compostos alinhados ao `build()` — <!-- source: db-manifest#L60 --> |
| Throughput escrituras (pico) | 200 TPS | 400 usuários concorrentes + jobs batch — <!-- source: db-manifest#L64 --> |
| `custoTotalPorAtivo` (agregação) | ≤ 2 s na read-replica | Offload do primário — <!-- source: db-manifest#L58 --> |
| Tempo de migração (baseline V1) | < 10 min | `database/migrations/V1__baseline.sql` — <!-- source: db-manifest#L73 --> |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Targets numéricos inferidos do HikariCP 500 conn + 400 usuários + complexidade de `ManutencaoSpecification` (14). Validar com DBA/Performance Eng.

Detalhamento completo em [[db-performance-recovery]].

## 11. Compliance Requirements
| Regulamento | Escopo no Banco | Controle |
| :--- | :--- | :--- |
| **LGPD (Lei 13.709/2018)** | Dados pessoais em `Usuario` (nome, email, login), logs de auditoria com `usuario_id` | Pseudonimização em relatórios; direito ao esquecimento via `anonymize_user()` procedure (a implementar); DPIA documentada |
| **SOX (Sarbanes-Oxley)** | `Auditoria` — trilha imutável de alterações patrimoniais/financeiras | Retenção 7 anos em WORM storage separado; hash encadeado (hash anterior + payload) — <!-- source: db-manifest#L61 --> |
| **ISO 27001 (A.12.4, A.8.2)** | Controle de acesso (RBAC), logs de acesso/admin, criptografia em repouso/transito | `pgcrypto`/`TDE` conforme engine; auditoria de `GRANT/REVOKE` |
| **BCB / CVM (se aplicável)** | Valores de ativos, depreciação, custos | Precisão decimal (`NUMERIC(18,4)`), rastreabilidade de fórmulas |

> **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** Aplicabilidade de BCB/CVM depende do setor do cliente (não identificado no diagnóstico). Validar com Compliance/Legal.

## 12. Lifecycle
| Fase | Ação | Responsável | Critério de Entrada/Saída |
| :--- | :--- | :--- | :--- |
| **Criação** | `V1__baseline.sql` via engenharia reversa (se BD existente) ou DDL inicial | Engenharia de Dados / DBA | Sprint 0 — <!-- source: db-manifest#L72 --> |
| **Evolução** | Migrations versionadas (Flyway/Liquibase) em `database/migrations/` | Engenharia (PR + CI) | `migration_policy: controlled`; destructive changes = approval required — <!-- source: db-manifest#L46-L47 --> |
| **Validação** | `schema_validation: true` + `drift_detection: true` no CI/CD | Platform / Engenharia | Sprint 1 — <!-- source: db-manifest#L74-L75 --> |
| **Operação** | Backup diário (lógico + físico), point-in-time recovery, vacuum/analyze automático | DBA / Platform | RPO=0, RTO<15min |
| **Arquivamento** | Particionamento por data (`Manutencao`, `Auditoria`) + move para storage frio após 2 anos | Engenharia de Dados | Políticas de retenção por tabela (a definir) |
| **Eliminação** | `DROP DATABASE` apenas com aprovação escrita de Legal + Compliance + Product | DBA / Legal | Fim de vida do produto ou migração para novo sistema |

---

## ⚠️ Gaps Críticos Pendentes (Bloqueiam Aprovação Final)

| Item | Status | Ação | Responsável | Referência |
| :--- | :--- | :--- | :--- | :--- |
| **Motor de banco (vendor/version/compatibility)** | **NÃO DEFINIDO** | Workshop DBA/Infra: PostgreSQL 16 vs Oracle 23c vs SQL Server 2022 vs MySQL 8.0 | DBA / Infra | Sprint 0 — <!-- source: db-manifest#L68-L71 --> |
| **Estrutura `database/migrations/` + Flyway/Liquibase** | **PENDENTE** | Criar baseline + configurar no Spring Boot | Engenharia | Sprint 0–1 — <!-- source: db-manifest#L72 --> |
| **Política `destructive_changes: approval-required` validada** | **RASCUNHO** | Revisar com Platform/Security; hook no CI/CD | Tech Lead / Platform | Sprint 1 — <!-- source: db-manifest#L73 --> |
| **Ferramenta de `drift_detection`** | **NÃO SELECIONADA** | Avaliar Atlas / schemadiff / pg_dump+diff | Engenharia | Sprint 1 — <!-- source: db-manifest#L75 --> |
| **Especificação de particionamento / tablespaces** | **NÃO DEFINIDO** | Depende do vendor escolhido (PG: declarative partitioning; Oracle: interval; SQL Server: partition scheme) | DBA / Engenharia | Pós-definição do engine |
| **Criptografia em repouso (TDE/pgcrypto) e gestão de chaves** | **NÃO DEFINIDO** | Alinhar com KMS da nuvem / HSM on-prem | Security / Infra | Pós-definição do engine |

> **Nota:** Este documento **não pressupõe** PostgreSQL, Oracle, SQL Server ou MySQL. Todos os requisitos de performance, disponibilidade, particionamento, criptografia e tooling de migração/drift dependem da decisão do motor (Gap Crítico #1). Assim que o vendor/version for definido, este contexto deve ser atualizado com especificidades (ex.: `pg_stat_statements`, `DBMS_STATS`, `Query Store`, `sys.dm_exec_query_stats`).