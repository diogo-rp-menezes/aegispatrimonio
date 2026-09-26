# Migration Specification & Plan — MIG-20250115-001

> **Versão:** 1.0 · **Status:** Not Applicable
> Este documento registra a ausência de migrações de banco de dados no repositório Aegis1 Frontend, conforme arquitetura confirmada.

## 1. Intent

```yaml
migration:
  id: "MIG-20250115-001"
  title: "Nenhuma migração aplicável — repositório sem banco de dados próprio"
  source_state: { version: "N/A" }
  target_state: { version: "N/A" }

  changes: []

  compatibility:
    backward_compatible: true
    forward_compatible: true

  deployment_strategy: "none"

  rollback:
    possible: false
    strategy: "Não aplicável — sem objetos de banco para reverter"
```

> **Justificativa:** O artefato-fonte [[db-schema-spec]] (Database Schema Specification) declara explicitamente: "Este repositório **não possui banco de dados próprio**. Conforme o *Database Domain & Entity Model*, o Aegis1 Frontend é uma SPA desacoplada servida via CDN; toda responsabilidade de persistência reside no backend externo (API REST/HTTPS), fora do escopo deste workspace." O array `entities` no modelo de domínio está vazio e nenhuma dependência de banco, ORM ou query builder foi encontrada no `package.json` (apenas `@popperjs/core`). <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/07-database/db-domain-model.md#2 --> <!-- source: package.json -->

## 2. Plano Operacional (Expand → Migrate → Contract)

| Fase | Nome | Operações | Checks |
| :--- | :--- | :--- | :--- |
| 1 | Preflight | Verificar ausência de `database/` no workspace; confirmar `entities: []` no domain model | `ls database/ 2>/dev/null \| wc -l` retorna 0; `grep -c "entities: \[\]" docs/07-database/db-domain-model.md` retorna 1 |
| 2 | Schema Preparation (Expand) | Nenhuma — sem objetos para criar | — |
| 3 | Data Transformation | Nenhuma — sem dados persistidos localmente | — |
| 4 | Application Compatibility | Confirmar que `frontend/src/services/api.js` continua apontando para backend externo inalterado | Diff do arquivo vs. baseline conhecido |
| 5 | Validation | Confirmar que build/frontend continua funcional sem migração | `npm run build` (se script existir) ou verificação manual |
| 6 | Cleanup (Contract) | Nenhuma — sem artefatos obsoletos de banco | — |

## 3. Risk Assessment

```yaml
risk:
  overall_level: "LOW"
  dimensions:
    data_loss: { score: 0, evidence: ["Nenhum dado persistido neste repositório"] }
    downtime: { score: 0, evidence: ["Nenhuma operação de banco executada"] }
    locking: { score: 0, evidence: ["Sem conexão de banco local"] }
    compatibility: { score: 0, evidence: ["Contrato de API externo inalterado"] }
    reversibility: { score: 0, evidence: ["Nenhuma mudança para reverter"] }
  destructive_operations: []
  required_controls: []
```

## 4. Approval Gate

| Campo | Valor |
| :--- | :--- |
| Aprovação obrigatória? | Não |
| Motivo | Nenhuma operação de banco — migração não aplicável |
| Aprovado por | N/A |
| Status | Not Applicable |

## 5. Verification (pós-execução)

> **Resultado:** Nenhuma migração executada. Verificação confirmada:
> - [x] Workspace não contém diretório `database/` nem arquivos `.sql` de migração
> - [x] `db-domain-model.md` confirma `entities: []` e descrição "NÃO possui banco de dados próprio"
> - [x] `package.json` lista apenas `@popperjs/core` — zero dependências de banco/ORM
> - [x] Ponto único de integração de dados é `frontend/src/services/api.js` (HTTP para backend externo)
> - [x] Build e execução do frontend não dependem de schema local

> **Nota:** Qualquer necessidade de migração de schema físico deve ser atendida no repositório do **backend externo**, fora do escopo deste workspace. Este documento existe apenas para satisfazer o catálogo de artefatos do projeto e registrar a decisão arquitetural documentada em [[db-schema-spec]] e [[db-domain-model]].