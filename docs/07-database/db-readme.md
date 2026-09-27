# Aegis Patrimônio — Database README

> **Versão:** 1.0 · **Owner:** Aegis Patrimônio — Engenharia de Dados · **Status:** Draft

## 1. Purpose

Este documento é o ponto de entrada operacional para o banco de dados do sistema **Aegis Patrimônio** — plataforma de gestão de ativos patrimoniais (hardware, software, móveis, equipamentos, veículos) com rastreamento de ciclo de vida, depreciação, manutenções (preventiva, corretiva, preditiva, melhoria), alertas de saúde/recursos, auditoria imutável (SOX/LGPD) e controle de acesso RBAC.

O banco suporta:
- **Gestão patrimonial completa**: cadastro, localização, responsável, depreciação, condição, health checks de disco/memória/CPU
- **Ordens de manutenção** com máquina de estados (SOLICITADA → APROVADA → EM_ANDAMENTO → CONCLUIDA/CANCELADA/AGUARDANDO_PECAS), custos, fornecedores, técnicos, aprovadores
- **Alertas automáticos** (uso de disco/memória crítico, manutenção vencida, saúde degradada, garantia expirando, depreciação acelerada) gerados por job agendado `AlertNotificationService.checkResourceUsageAlerts`
- **Auditoria WORM** (append-only, hash encadeado SHA-256, retenção 7 anos SOX Seção 802) para todas as operações relevantes
- **RBAC** com 4 roles padrão (ADMIN, AUDITOR, GESTOR, OPERADOR) + permissões atômicas por recurso/ação
- **Multi-filial** com segregação por Row-Level Security (RLS) no nível de `filial_id`

> **Fonte:** [[db-manifest]] §database.description, [[db-schema-spec]] §1 (tabelas), [[db-security-lifecycle]] §1 (roles), §4 (data lifecycle)

---

## 2. Architecture

| Atributo | Valor |
| :--- | :--- |
| **Modelo** | Relacional (strong consistency, ACID) |
| **Tenancy** | Single-tenant (monolito único, segregação lógica por `filial_id` via RLS) |
| **Engine** | **NÃO DEFINIDO — Gap crítico #1** (ver [[db-manifest]] Gaps Críticos) |
| **Versão do Engine** | **NÃO DEFINIDO — Gap crítico #1** |
| **Compatibilidade** | **NÃO DEFINIDO — Gap crítico #1** |
| **ORM / Query Builder** | Nenhum identificado — provavelmente SQL cru + migrações Flyway/Liquibase (ver [[db-manifest]] §governance.migration_policy) |
| **Pool de conexões** | HikariCP 500 conexões (configurado para 400 usuários concorrentes + jobs) — [[db-manifest]] Decisões Arquiteturais |
| **Read-replica** | Prevista para offload de `custoTotalPorAtivo` e health checks do primário — [[db-manifest]] Decisões Arquiteturais |
| **WORM Storage** | Separado para auditoria (Object Lock Compliance Mode, 7 anos) — [[db-manifest]] Decisões Arquiteturais, [[db-security-lifecycle]] §4 (Auditoria) |

> **Fonte:** [[db-manifest]] §database.architecture, §database.engine, Decisões Arquiteturais; [[db-schema-spec]] §8 (ER Diagram); [[db-security-lifecycle]] §4

---

## 3. Domains & Core Entities

O domínio é organizado em **5 áreas funcionais** com 19 tabelas físicas:

| Área | Tabelas (Entidades) | Classificação |
| :--- | :--- | :--- |
| **Patrimônio (Core)** | `ativo` (core), `tipo_ativo` (reference), `ativo_detalhe_hardware` (supporting), `adaptador_rede` (supporting), `disco` (supporting), `memoria` (supporting) | Core / Reference / Supporting |
| **Manutenção (Transactional)** | `manutencao` (transactional) | Transactional |
| **Alertas (Transactional)** | `alerta` (transactional) | Transactional |
| **Organização & Pessoas (Core/Reference)** | `filial` (reference), `departamento` (reference), `localizacao` (reference), `fornecedor` (reference), `funcionario` (core) | Reference / Core |
| **Segurança & Auditoria (Core/Audit)** | `usuario` (core), `role` (reference), `permissao` (reference), `usuario_role` (supporting), `role_permissao` (supporting), `auditoria` (audit) | Core / Reference / Supporting / Audit |

**Entidades centrais (aggregates):**
- **Ativo** — raiz do agregado patrimonial; possui detalhes de hardware (1:1), manutenções (1:N), alertas (1:N), auditoria (1:N)
- **Manutencao** — agregado transacional com máquina de estados, custos derivados (`custo_total = custo_pecas + custo_mao_obra`), aprovação
- **Usuario** — identidade de acesso com vínculo opcional 1:1 a `funcionario`; roles via `usuario_role` (N:N)
- **Auditoria** — append-only imutável, hash chain por `correlation_id`, WORM storage

> **Fonte:** [[db-schema-spec]] §1 (19 tabelas), §2 (36 relacionamentos), §8 (ER Diagram); [[db-domain-model]] (referenciado no spec)

---

## 4. Source of Truth

| Aspecto | Definição |
| :--- | :--- |
| **Tipo** | Migration-based (Flyway/Liquibase recomendados no SAD §5) |
| **Localização** | `database/migrations/` (pendente criação — Gap crítico #1) |
| **Contrato físico** | `docs/07-database/db-schema-spec.md` — **nunca edite o SQL gerado diretamente**; edite este contrato e regenere |
| **Manifesto** | `docs/07-database/db-manifest.md` (este arquivo referencia `db-readme.md` como documentação) |
| **Versionamento** | Obrigatório — cada mudança de schema = migration versionada |
| **Política de migração** | `controlled` — mudanças destrutivas exigem aprovação humana explícita |
| **Detecção de drift** | `true` — requer motor determinístico próprio (introspecção + checksum) — **não implementado** |

> **Fonte:** [[db-manifest]] §database.source_of_truth, §governance, §artifacts; [[db-migration-spec]] §1 (changes), §2 (plano operacional)

---

## 5. Migration Strategy

**Estratégia:** Expand → Migrate → Contract (zero-downtime onde aplicável; migração inicial em banco vazio requer janela de manutenção).

**Políticas-chave (do Manifesto):**
- `migration_policy: "controlled"` — mudanças planejadas, versionadas, revisadas
- `destructive_changes: "approval-required"` — `DROP TABLE`, `DROP COLUMN`, truncamento exigem aprovação humana; **nunca automático**
- `drift_detection: true` — comparar manifesto/schema declarado vs. estado real (motor não implementado)
- `schema_validation: true` — validação automática no CI/CD (NFR-M03)

**Migração inicial (MIG-20250115-001):**
- Cria 19 tabelas, 36 FKs, 56 constraints, 35 índices, 4 views (1 materializada), 4 functions, 6 triggers
- Seed de dados de referência (tipos de ativo, roles, permissões, filiais, RBAC baseline)
- **Pré-requisitos bloqueantes:** MOTOR-001 (SGBD), ORM-001 (ferramenta), ENUM-001, JSON-001, PART-001, WORM-001, SEC-001, SEQ-001, FK-001 — todos documentados em [[db-schema-spec]] §9 e [[db-migration-spec]] §3–4

**Rollback:** Possível (DROP SCHEMA CASCADE em ordem reversa de dependência) mas destrutivo — testado em staging obrigatório.

> **Fonte:** [[db-manifest]] §governance; [[db-migration-spec]] §1 (migration), §2 (plano operacional), §3 (risk), §4 (approval gate)

---

## 6. Environments

| Environment | Purpose | Configuração Específica |
| :--- | :--- | :--- |
| **Development** | Desenvolvimento local / feature branches | Seed determinístico `core_reference_data` + `rbac_baseline` + `patrimonio_sample` (100 ativos) + `manutencao_sample` (50 ordens) + `alerta_sample` (30) + `auditoria_sample` (200) + `lgpd_edge_cases` — [[db-security-lifecycle]] §5 |
| **Test** | Validação automatizada (CI/CD) | Mesmo seed de development; banco efêmero por pipeline; testes de integração cobrem ManutencaoSpecification.build (p95 ≤ 50ms), AtivoMapper.toDTO, custoTotalPorAtivo (≤ 2s read-replica), audit hash chain |
| **Staging** | Pré-produção / validação de migração | Carga realista (100k+ ativos, 1M+ manutenções, 500k+ alertas, 10M+ auditoria) para validação de índices (IDX-001), particionamento (PART-001), RLS (SEC-001), WORM (WORM-001) |
| **Production** | Produção | **Sem seeds**; migrações aplicadas via pipeline controlado; read-replica ativa; WORM Object Lock Compliance Mode provisionado; backup PITR + cópia assíncrona auditoria para storage imutável |

> **Fonte:** [[db-manifest]] §database.environments; [[db-security-lifecycle]] §5 (seed); [[db-migration-spec]] §2 fase 5 (validation), §3 (risk)

---

## 7. Security

### 7.1 Roles & Permissions (RBAC)

| Role | Propósito | Tabelas com Acesso (Resumo) |
| :--- | :--- | :--- |
| **ADMIN** | Acesso total: usuários, roles, permissões, configurações, auditoria, operações LGPD | Todas as 19 tabelas: SELECT/INSERT/UPDATE/DELETE |
| **AUDITOR** | Leitura apenas para compliance (SOX, LGPD) | Todas as 19 tabelas: **apenas SELECT** |
| **GESTOR** | Gestão patrimonial: CRUD ativos, manutenções (incl. aprovar), health checks, alertas | `ativo`, `manutencao`, `alerta`, `funcionario`, `filial`, `departamento`, `localizacao`, `fornecedor`, `tipo_ativo`, `ativo_detalhe_hardware`, `adaptador_rede`, `disco`, `memoria`, `auditoria` — SELECT/INSERT/UPDATE (sem DELETE) |
| **OPERADOR** | Operação diária: leitura ativos próprios, criar solicitações manutenção, ler alertas próprios, health checks | `ativo`, `manutencao`, `alerta`, `funcionario`, `filial`, `departamento`, `localizacao`, `tipo_ativo`, `ativo_detalhe_hardware`, `adaptador_rede`, `disco`, `memoria` — SELECT/INSERT (manutencao/alerta) |
| **SYSTEM_SCHEDULER** | Identidade técnica para jobs (checkResourceUsageAlerts, updateHealthCheck) | `alerta` (INSERT/UPDATE), `ativo` (UPDATE health check), `auditoria` (INSERT), `manutencao` (SELECT) |

> **Fonte:** [[db-security-lifecycle]] §1 (roles)

### 7.2 Row-Level Security (RLS)

RLS habilitado em 7 tabelas com políticas baseadas em `current_setting('app.current_user_funcionario_id')` e `current_setting('app.current_user_role')`:

| Tabela | Políticas (Resumo) |
| :--- | :--- |
| `ativo` | `rls_ativo_filial`: vê apenas ativos da sua filial; `rls_ativo_departamento`: vê apenas ativos do seu departamento |
| `manutencao` | `rls_manutencao_filial`: vê manutenções de ativos da sua filial; `rls_manutencao_tecnico`: técnico vê apenas suas atribuições |
| `alerta` | `rls_alerta_filial`: vê alertas globais (ativo_id NULL) ou de ativos da sua filial |
| `funcionario` | `rls_funcionario_filial`: vê apenas funcionários da sua filial |
| `departamento` | `rls_departamento_filial`: vê apenas departamentos da sua filial |
| `localizacao` | `rls_localizacao_filial`: vê apenas localizações da sua filial |
| `auditoria` | `rls_auditoria_admin_auditor`: apenas ADMIN e AUDITOR podem ler (SOX/LGPD) |

> **Fonte:** [[db-security-lifecycle]] §2 (row_level_security)

### 7.3 Sensitive Data Classification

| Campo | Classificação | Proteção |
| :--- | :--- | :--- |
| `usuario.password_hash` | **restricted** | Hash bcrypt/Argon2 — nunca logado, nunca exposto em API |
| `usuario.email`, `username`, `tentativas_login_falhas`, `bloqueado_ate` | **confidential** | Restricted-access; username imutável (stub `setUsername` vazio) |
| `funcionario.cpf` | **restricted** | Criptografia em repouso (AES-256 TDE) + masking em logs (`***.****.***-**`); validação algorítmica via `validar_cpf()` |
| `funcionario.email_corporativo`, `telefone`, `data_admissao`, `data_desligamento` | **confidential** | Restricted-access + masking (email/telefone) |
| `ativo.valor_aquisicao` | **confidential** | Restricted-access; agregado em `vw_custo_manutencao_por_ativo` (read-replica) |
| `manutencao.custo_pecas`, `custo_mao_obra`, `custo_total` | **confidential** | Restricted-access |
| `fornecedor.cnpj` | **internal** | Restricted-access + masking (`**.***.***/****-**`); validação via `validar_cnpj()` |
| `auditoria.valores_anteriores`, `valores_novos`, `metadados`, `ip_origem`, `user_agent`, `hash_encadeado` | **restricted** | Criptografia + restricted-access + WORM (Object Lock Compliance Mode); acesso apenas ADMIN/AUDITOR |
| `tipo_ativo.campos_tecnicos_obrigatorios`, `alerta.metadados` | **internal** | Restricted-access |

**Segredos:** Proibidos no schema — gerenciados exclusivamente via HashiCorp Vault / AWS Secrets Manager (NFR-SEC04).

> **Fonte:** [[db-security-lifecycle]] §3 (sensitive_data, secrets)

### 7.4 Data Lifecycle Highlights

| Entidade | Retenção | Deleção | Base Legal |
| :--- | :--- | :--- | :--- |
| `Ativo` | 7 anos após descarte/venda | **Nunca** (hard delete proibido) — anonimização campos sensíveis | SOX Seção 404/802, LGPD Art. 16 |
| `Manutencao` | 7 anos após conclusão/cancelamento | **Nunca** — anonimização custos/observações | SOX, LGPD Art. 16 |
| `Alerta` | 2 anos após resolução/ignorado | **Hard delete permitido** após 2 anos (job de purga) | Requisito operacional + LGPD Art. 16 (logs segurança) |
| `Usuario` | 7 anos após desativação | Apenas via endpoint admin "direito ao esquecimento" (LGPD Art. 18) — anonimiza PII, mantém FK | SOX, LGPD Art. 16/18 |
| `Funcionario` | 7 anos após desligamento | **Nunca** — anonimização LGPD (CPF, email, telefone, nome) após 7 anos | SOX, LGPD obrigação legal trabalhista |
| `Auditoria` | 7 anos imutáveis | **Nunca** — crypto-shredding (destruição chaves) após 7 anos | SOX Seção 802 (crime federal alterar), LGPD Art. 16 |
| `TipoAtivo`, `Filial`, `Departamento`, `Localizacao`, `Role`, `Permissao` | Permanente | Soft delete apenas (`ativo=false`) | Referência histórica para integridade referencial |
| `Fornecedor` | 7 anos após inativação sem referências | Anonimização CNPJ/razão_social/contato | SOX (contratos/custos) |
| `UsuarioRole`, `RolePermissao` | 7 anos após revogação/expiração | **Nunca** — trigger de auditoria registra REVOKE | SOX 404 (segregação de duties) |

> **Fonte:** [[db-security-lifecycle]] §4 (data_lifecycle)

---

## 8. Backup, Recovery & Observability

| Capacidade | Detalhes |
| :--- | :--- |
| **Backup Strategy** | PITR (Point-in-Time Recovery) nativo do SGBD + snapshots diários; cópia assíncrona da tabela `auditoria` para WORM Object Storage (S3 Object Lock Compliance Mode / Azure Immutable Blob) particionada mensalmente (INDEX-033) |
| **RPO / RTO** | Não definidos explicitamente — dependem da decisão MOTOR-001 e provisionamento de infra (ver [[db-manifest]] Gaps Críticos) |
| **Observabilidade** | Índices instrumentados para query patterns críticos (INDEX-001 a INDEX-035 com `evidence` e `query_pattern` documentados); `EXPLAIN ANALYZE` obrigatório em validação pós-migração; materialized view `vw_custo_manutencao_por_ativo` com refresh < 2s (ASM-018) |
| **Audit Trail** | Tabela `auditoria` append-only com hash chain SHA-256 monotônico por `correlation_id`; triggers `trg_auditoria_insert_hash_chain` (BEFORE INSERT) e `trg_auditoria_prevent_update_delete` (BEFORE UPDATE/DELETE) garantem imutabilidade |
| **Drift Detection** | `drift_detection: true` no manifesto — **motor não implementado**; requer ferramenta determinística (Atlas, schemadiff, pg_dump + diff) para automação |
| **Health Checks** | Job `updateHealthCheck` coleta telemetria (uso_disco_percentual, memoria_total_gb, processador, SO) em `ativo` e `ativo_detalhe_hardware`; job `checkResourceUsageAlerts` gera alertas `USO_DISCO_CRITICO`, `USO_MEMORIA_CRITICO`, `SAUDE_DEGRADADA` |

> **Fonte:** [[db-manifest]] Decisões Arquiteturais (HikariCP, read-replica, WORM); [[db-schema-spec]] §4 (indexes com evidence), §5 (views), §7 (triggers); [[db-security-lifecycle]] §4 (Auditoria archival); [[db-migration-spec]] §5 (verification)

---

## 9. Change Management

**Processo de mudança de schema (Database as Code):**

1. **Proposta** — Engenheiro edita `db-schema-spec.md` (contrato físico) com a alteração desejada (nova coluna, índice, constraint, view, function, trigger)
2. **Validação Local** — Gera DDL para SGBD alvo (após MOTOR-001); roda testes de constraint/índice em banco efêmero
3. **Pull Request** — Inclui: diff do `db-schema-spec.md`, migration script gerado (`database/migrations/V{next}__description.sql`), plano de rollback, evidência de `EXPLAIN ANALYZE` para índices novos/modificados
4. **Revisão Obrigatória** — Tech Lead + DBA + SecOps (se envolver RLS, dados sensíveis, auditoria, WORM)
5. **Aprovação** — Gate obrigatório para mudanças destrutivas (`destructive_changes: approval-required`); CI/CD valida `schema_validation: true` e `drift_detection: true` (quando motor existir)
6. **Aplicação** — Deploy via pipeline controlado (Flyway/Liquibase); fase Expand → Migrate → Contract; verificação pós-deploy (schema diff, constraint tests, index usage, audit immutability, hash chain, MV refresh, integration tests)
7. **Documentação** — Atualiza `db-readme.md` (este arquivo) com migration ID aplicada, timestamp, e quaisquer decisões de runtime

**Ferramentas previstas:** Flyway ou Liquibase (ORM-001 pendente); migrações versionadas em `database/migrations/` (Gap crítico #1 — estrutura não criada).

> **Fonte:** [[db-manifest]] §governance, §artifacts, Próximos Passos; [[db-migration-spec]] §2 (plano operacional), §4 (approval gate), §5 (verification)

---

## 10. Directory Structure

```text
database/
├── dbac.yaml                    # Database as Code config (referencia db-manifest.md)
├── model/                       # Modelo canônico (derivado de db-domain-model.md)
│   └── (arquivos de modelo lógico — não versionados diretamente no repo)
├── schema/                      # SQL GERADO a partir de db-schema-spec.md — NÃO EDITAR MANUALMENTE
│   ├── 00_enums.sql             # Tipos ENUM nativos (se ENUM-001 = native type)
│   ├── 01_tables.sql            # 19 tabelas com PKs, colunas, tipos lógicos mapeados
│   ├── 02_fks.sql               # 36 foreign keys (REL-001 a REL-036)
│   ├── 03_constraints.sql       # 56 constraints (CONSTRAINT-001 a CONSTRAINT-056)
│   ├── 04_indexes.sql           # 35 índices (INDEX-001 a INDEX-035)
│   ├── 05_functions.sql         # 4 functions (validar_cpf, validar_cnpj, calcular_hash_encadeado_auditoria, anonymize_user, atualizar_custo_total_manutencao)
│   ├── 06_triggers.sql          # 6 triggers (auditoria hash chain, append-only, manutencao custo_total, funcionario ativo sync, usuario_role audit, role_permissao audit)
│   ├── 07_views.sql             # 3 views + 1 materialized view
│   └── 08_grants_rls.sql        # Grants por role + políticas RLS (7 tabelas)
├── migrations/                  # Uma pasta por migration, referenciando db-migration-*.md
│   ├── V1__baseline/            # Migração inicial MIG-20250115-001 (schema + seed)
│   │   ├── up.sql
│   │   ├── down.sql
│   │   ├── seed_core_reference_data.sql
│   │   ├── seed_rbac_baseline.sql
│   │   ├── seed_patrimonio_sample.sql
│   │   ├── seed_manutencao_sample.sql
│   │   ├── seed_alerta_sample.sql
│   │   ├── seed_auditoria_sample.sql
│   │   └── seed_lgpd_edge_cases.sql
│   └── V{next}__.../            # Migrações subsequentes
├── seeds/                       # Scripts de seed reutilizáveis (dev/test)
│   ├── core_reference_data.sql
│   ├── rbac_baseline.sql
│   ├── patrimonio_sample.sql
│   ├── manutencao_sample.sql
│   ├── alerta_sample.sql
│   ├── auditoria_sample.sql
│   └── lgpd_edge_cases.sql
└── tests/                       # Testes de validação de schema (executados no CI/CD)
    ├── constraint_tests.sql     # 56 negative test cases (um por CONSTRAINT-XXX)
    ├── fk_integrity_tests.sql   # 36 FK referential integrity tests
    ├── index_usage_tests.sql    # EXPLAIN ANALYZE para 15 queries representativas
    ├── audit_immutability_tests.sql
    ├── hash_chain_integrity_tests.sql
    ├── mv_refresh_performance_tests.sql
    └── rbac_functionality_tests.sql
```

> **Fonte:** [[db-manifest]] §artifacts; [[db-schema-spec]] §1–7 (estrutura de objetos); [[db-migration-spec]] §2 (fases 2–3), §5 (verification); [[db-security-lifecycle]] §5 (seed datasets)

---

## 11. Limitações Atuais do Sistema (importante)

Este conjunto de artefatos cobre a camada de **contrato e documentação versionada** (Database as Code). Os itens abaixo, sugeridos na análise original, dependem de um **motor determinístico** — código de introspecção, parsing SQL, execução em banco efêmero e comparação de estado — que **não existe ainda** neste projeto e não é gerado por templates:

| Capacidade | Status | Observação |
| :--- | :--- | :--- |
| Introspecção de banco existente (brownfield) | **Não implementado** | Necessário para engenharia reversa se houver banco legado; hoje `database/migrations/` não existe |
| Schema Diff / Drift Report contra banco vivo | **Não implementado** | `drift_detection: true` no manifesto requer ferramenta própria (Atlas, schemadiff, pg_dump + diff) |
| Execução de migration em banco efêmero + verificação automática | **Não implementado** | Pipeline de validação (fase 5 do plano operacional) roda manualmente hoje |
| Compatibility Matrix automática app↔schema | **Não implementado** | JPA/Hibernate entities (ASM-017) não são verificados contra `db-schema-spec` automaticamente |
| Changelog e Validation Report gerados automaticamente | **Não implementado** | Documentação de migração aplicada é manual (atualização `db-readme.md` passo 6d) |

**Gaps Críticos Bloqueantes (devem ser resolvidos antes de qualquer execução):**

| Gap | Descrição | Impacto | Responsável | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| **MOTOR-001** | SGBD alvo não definido (PostgreSQL, Oracle, SQL Server, MySQL) | Afeta **todos** os mapeamentos de tipo físico (UUID, JSONB, ENUM, INET, MACADDR, TIMESTAMP WITH TIME ZONE), particionamento, funções, triggers | Arquiteto de Dados / Tech Lead | Imediato (Sprint 0) |
| **ORM-001** | Ferramenta de migração não escolhida (Flyway / Liquibase / JPA DDL / SQL scripts) | Define formato de migrations, versionamento, rollback, seed | Tech Lead | Imediato (Sprint 0) |
| **ENUM-001** | Implementação de 15 ENUMs lógicos: nativo vs CHECK vs tabela referência | Afeta DDL de todas as tabelas com enums (status, condição, tipo, categoria, severidade, prioridade, ação, disco_tipo, adaptador_tipo) | Arquiteto de Dados | Antes da geração DDL |
| **JSON-001** | Suporte a JSONB/JSON no SGBD alvo para 4 colunas | `tipo_ativo.campos_tecnicos_obrigatorios`, `alerta.metadados`, `auditoria.valores_anteriores/novos/metadados` | Arquiteto de Dados | Antes da geração DDL |
| **PART-001** | Estratégia de particionamento para 4 tabelas grandes | `ativo` (data_aquisicao), `manutencao` (data_abertura), `alerta` (data_criacao), `auditoria` (data_hora mensal) | DBA / Arquiteto | Antes da geração DDL |
| **WORM-001** | Storage imutável para auditoria (Object Lock Compliance Mode) | Requisito SOX Seção 802; deve estar provisionado antes de tráfego de produção | SecOps / DBA | Antes de produção |
| **SEC-001** | Modelo de segregação por `filial_id` (RLS/views) aprovado | 7 tabelas com RLS definidas; depende de `app.current_user_funcionario_id` setado pela app | SecOps / Arquiteto | Antes de produção |
| **SEQ-001** | Geração de IDs de negócio (`tag_patrimonial`, `numero_ordem`, `codigo`) | Sequence nativa, UUID+trigger, ou aplicação | Tech Lead | Antes da geração DDL |
| **FK-001** | Revisão de `on_delete`/`on_update` de 36 FKs | `restrict` vs `cascade` vs `set_null` alinhado com regras de negócio | Arquiteto de Dados | Antes da geração DDL |

> **Fonte:** [[db-manifest]] Gaps Críticos, Próximos Passos; [[db-schema-spec]] §9 (Pendências); [[db-migration-spec]] §3 (Risk Assessment), §4 (Approval Gate checklist)

---

> **Nota:** Este README reflete o estado **Draft** dos artefatos de banco (v1.0). A execução real (DDL, migrações, seeds, validações) depende da resolução dos gaps acima e da implementação do motor determinístico de Database as Code. Qualquer alteração no contrato físico deve ser feita em `db-schema-spec.md` e propagada via processo de Change Management (Seção 9).