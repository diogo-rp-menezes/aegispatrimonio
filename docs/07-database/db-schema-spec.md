# Database Schema Specification — Aegis1 Frontend

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active
> Contrato físico do banco. Cada objeto aqui declarado é a fonte da verdade para gerar `database/schema/` — **nunca edite o SQL gerado diretamente**; edite este contrato e regenere.

## 1. Tables

> **Nota:** Este repositório **não possui banco de dados próprio**. Conforme o *Database Domain & Entity Model* ([[db-domain-model]]), o Aegis1 Frontend é uma SPA desacoplada servida via CDN; toda responsabilidade de persistência reside no backend externo (API REST/HTTPS), fora do escopo deste workspace. O array `entities` no modelo de domínio está vazio e nenhuma dependência de banco, ORM ou query builder foi encontrada no `package.json` (apenas `@popperjs/core`). <!-- source: docs/07-database/db-domain-model.md#2 --> <!-- source: docs/07-database/db-manifest.md#1 --> <!-- source: package.json -->

Nenhuma tabela é declarada neste contrato porque **não há schema de banco neste repositório**. Para o modelo físico real (tabelas, colunas, constraints, DDL, migrações), consulte a documentação do backend externo.

## 2. Relationships

```yaml
relationships: []
```

> Não há relacionamentos a declarar — ausência de entidades persistidas no frontend. <!-- source: docs/07-database/db-domain-model.md#2 -->

## 3. Constraints

```yaml
constraints: []
```

> Não há constraints de banco — o repositório não define modelo persistente. <!-- source: docs/07-database/db-domain-model.md#2 -->

## 4. Indexes

```yaml
indexes: []
```

> Não há índices — não existe banco de dados neste workspace. <!-- source: docs/07-database/db-domain-model.md#2 -->

## 5. Views

```yaml
views: []
```

> Não há views — persistência é responsabilidade do backend externo. <!-- source: docs/07-database/db-manifest.md#1 -->

## 6. Functions

```yaml
functions: []
```

> Não há funções de banco — o frontend apenas consome a API via `frontend/src/services/api.js`. <!-- source: docs/07-database/db-domain-model.md#3 (ASM-001) -->

## 7. Triggers

```yaml
triggers: []
```

> Não há triggers — ausência de camada de persistência local. <!-- source: docs/07-database/db-domain-model.md#3 (ASM-001) -->

## 8. Diagrama Entidade-Relacionamento (Físico)

```mermaid
erDiagram
    %% Este repositório não possui entidades persistidas.
    %% O diagrama ER físico pertence ao backend externo (fora deste workspace).
```

---

## Evidências e Rastreabilidade

| Item | Fonte | Observação |
|------|-------|------------|
| Ausência de banco no repositório | `docs/07-database/db-domain-model.md#1` | Domínio `aegis1-frontend` declara `entities: []` e descrição explícita de "NÃO possui banco de dados próprio" |
| Ponto único de integração de dados | `docs/07-database/db-domain-model.md#3 (ASM-001)` | `frontend/src/services/api.js` encapsula chamadas HTTP para backend externo |
| Nenhuma dependência de banco/ORM | `docs/07-database/db-domain-model.md#3 (ASM-003)` | `package.json` + varredura determinística: apenas `@popperjs/core` |
| Contexto de banco confirmado | `docs/07-database/db-manifest.md#1` | "Este repositório não possui banco de dados nem modelo de domínio persistente" |

> **Conclusão:** Este documento existe para satisfazer o catálogo de artefatos do projeto, mas seu conteúdo é intencionalmente vazio no que tange a objetos de banco, refletindo a arquitetura real: **frontend-only, sem persistência local**. Qualquer necessidade de schema físico deve ser atendida consultando a documentação do serviço de backend correspondente.