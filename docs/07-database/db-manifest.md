# Database Manifest — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** Time Aegis Patrimônio · **Status:** Draft
> Ponto de entrada da automação Database as Code (DBaC). Todo o restante dos artefatos de banco (`docs/07-database/*`) referencia este manifesto.

```yaml
database:
  id: "aegis-patrimonio-primary"
  name: "aegis_patrimonio"
  version: "0.1.0"
  status: "draft"

  engine:
    vendor: "<NÃO DEFINIDO — Gap crítico #1>"
    version: "<NÃO DEFINIDO — Gap crítico #1>"
    compatibility: "<NÃO DEFINIDO — Gap crítico #1>"

  architecture:
    model: "relational"
    tenancy: "single"
    consistency: "strong"

  source_of_truth:
    type: "migration"
    location: "database/migrations/"

  environments:
    - name: development
      purpose: development
    - name: test
      purpose: automated-validation
    - name: staging
      purpose: pre-production
    - name: production
      purpose: production

  ownership:
    team: "Aegis Patrimônio — Engenharia de Dados"
    service: "aegis-patrimonio"

  governance:
    migration_policy: "controlled"
    destructive_changes: "approval-required"
    drift_detection: true
    schema_validation: true

  artifacts:
    root: "database/"
    schema: "database/schema/"
    migrations: "database/migrations/"
    specifications: "docs/07-database/"
    tests: "database/tests/"
    documentation: "docs/07-database/db-readme.md"
```

## Notas de Governança
- **Mudanças destrutivas** (`DROP TABLE`, `DROP COLUMN`, truncamento de dados) exigem aprovação humana explícita — nunca aplicadas automaticamente, independentemente da confiança da IA na proposta.
- **Detecção de drift** entre este manifesto/schema declarado e o estado real do banco requer um motor determinístico próprio (introspecção + comparação de checksum) — não é gerado por este template; ver seção de limitações em [`db-readme.md`](./db-readme.md).

---

## ⚠️ Gaps Críticos — Ação Requerida Antes de Aprovação

| Campo | Status | Ação Necessária | Responsável | Prazo |
|-------|--------|-----------------|-------------|-------|
| `engine.vendor` | **NÃO DEFINIDO** | Confirmar motor relacional: PostgreSQL, Oracle, SQL Server ou MySQL | DBA / Infra | Sprint 0 |
| `engine.version` | **NÃO DEFINIDO** | Definir versão suportada (ex.: PostgreSQL 16, Oracle 23c, SQL Server 2022, MySQL 8.0) | DBA / Infra | Sprint 0 |
| `engine.compatibility` | **NÃO DEFINIDO** | Estabelecer faixa de versões compatíveis para upgrades sem downtime | DBA / Infra | Sprint 0 |
| `source_of_truth.location` | **PENDENTE** | Criar estrutura `database/migrations/` e configurar Flyway/Liquibase | Engenharia | Sprint 0–1 |
| `governance.migration_policy` | **RASCUNHO** | Validar política "controlled" com time de plataforma | Tech Lead / Platform | Sprint 0 |

> **Origem dos gaps:** Conforme documentado no [System Architecture Document](./system-architecture.md) — Seção "O que falta verificar (Gaps de Informação)", Item #1: *"Motor de banco de dados: Não identificado no package.json nem em arquivos de configuração... NFRs de escalabilidade (NFR-S03), disponibilidade (NFR-A03), portabilidade (NFR-PO02) e custo (NFR-CO01) dependem desta definição."*

---

## Decisões Arquiteturais Já Consolidadas (Não Dependem do Motor)

| Decisão | Justificativa | Referência |
|---------|---------------|------------|
| **Modelo relacional** | Requisitos de auditoria (LGPD/SOX), transações ACID, RBAC, integridade referencial | SAD §5, NFR-C01/C02 |
| **Single-tenancy** | Monolito único, sem isolamento de dados por cliente | SAD §3, §12 |
| **Consistência forte** | Operações financeiras/patrimoniais exigem ACID; sem event sourcing | SAD §5, NFR-S03 |
| **Source of truth = migrations** | Flyway/Liquibase recomendados no SAD §5; versionamento obrigatório | SAD §5, §13 |
| **Read-replica para relatórios** | Offload de `custoTotalPorAtivo` e health checks do primário | SAD §3, §7, NFR-A04 |
| **WORM storage separado para auditoria** | Logs imutáveis (SOX 7 anos) fora do banco transacional | SAD §3, §9, NFR-SEC05 |
| **Índices compostos alinhados a `ManutencaoSpecification.build`** | Otimização de queries dinâmicas (complexidade 14) | SAD §5, §7, NFR-CO02 |
| **HikariCP 500 conexões** | Pool configurado para 400 usuários concorrentes + jobs | SAD §7, NFR-S03 |

---

## Próximos Passos Imediatos

1. **Sprint 0 — Definição do motor:** Workshop com DBA/Infra para escolher vendor/version/compatibility. Critérios: suporte a read-replica nativa, WAL/log shipping, particionamento nativo, custos de licença/cloud, expertise do time.
2. **Sprint 0 — Baseline de migração:** Criar `database/migrations/V1__baseline.sql` com schema atual (engenharia reversa se necessário) + configurar Flyway/Liquibase no Spring Boot.
3. **Sprint 1 — Validação de governança:** Revisar `destructive_changes: approval-required` com Platform/Security; implementar hook de validação no CI/CD (NFR-M03).
4. **Sprint 1 — Drift detection:** Avaliar ferramentas (Atlas, schemadiff, pg_dump + diff) para automação de `drift_detection: true`.