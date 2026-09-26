# Database Manifest — Aegis1 (Frontend Repository)

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active
> Ponto de entrada da automação Database as Code (DBaC). Este repositório **não contém banco de dados** — o manifesto documenta a ausência intencional e referencia o backend externo.

```yaml
database:
  id: "aegis1-frontend-no-database"
  name: "Aegis1 Frontend (sem banco de dados próprio)"
  version: "0.1.0"
  status: "active"

  engine:
    vendor: "none"
    version: "N/A"
    compatibility: "N/A — este repositório é uma SPA estática servida via CDN; a persistência reside no backend externo (fora do escopo deste codebase)"

  architecture:
    model: "none"
    tenancy: "none"
    consistency: "none"

  source_of_truth:
    type: "none"
    location: "N/A — não há schema, migrations, models ou seeds neste repositório"

  environments:
    - name: development
      purpose: "desenvolvimento local da SPA (mock API opcional)"
    - name: test
      purpose: "testes automatizados da SPA (msw/jest mocks)"
    - name: staging
      purpose: "pré-produção da SPA contra backend de staging"
    - name: production
      purpose: "produção da SPA contra backend de produção"

  ownership:
    team: "Frontend Team"
    service: "aegis1-frontend"

  governance:
    migration_policy: "none"
    destructive_changes: "forbidden"
    drift_detection: false
    schema_validation: false

  artifacts:
    root: "N/A"
    schema: "N/A"
    migrations: "N/A"
    specifications: "docs/07-database/ (apenas este manifesto e db-readme.md explicando a ausência)"
    tests: "N/A"
    documentation: "docs/07-database/db-readme.md"
```

## Notas de Governança

- **Este repositório não possui banco de dados, ORM, query builder, migrations, seeds ou modelos de dados persistentes.** O diagnóstico determinístico confirma: *"nenhum motor de banco conhecido encontrado nas dependências"*, *"nenhum ORM/query builder encontrado — provavelmente SQL cru"* (referindo-se ao backend externo, não a este codebase).
- **Toda persistência e regras de negócio de dados residem no backend externo** (API REST/HTTPS), fora do escopo deste repositório. O frontend apenas consome a API via `frontend/src/services/api.js`.
- **Mudanças de schema de banco de dados** são responsabilidade do time de backend e devem ser documentadas nos artefatos correspondentes daquele repositório (ex.: `db-schema-spec.md`, `db-migration-spec.md`, `db-domain-model.md`).
- **Detecção de drift** não se aplica a este repositório por não haver estado de banco declarado aqui.
- **Referência cruzada:** Para o modelo de dados real (entidades, relacionamentos, constraints, DDL), consultar a documentação do backend (fora deste workspace).