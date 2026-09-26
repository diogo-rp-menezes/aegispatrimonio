# Database Domain & Entity Model — Aegis1 Frontend

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active
> Modelo conceitual — a IA pode inferir este modelo a partir do domínio, mas ele nunca deve ser convertido diretamente em SQL sem passar pelo contrato físico em [[db-schema-spec]].

## 1. Domain
```yaml
domain:
  id: "aegis1-frontend"
  name: "Aegis1 Frontend (SPA)"
  description: "Camada de apresentação desacoplada (Single Page Application) servida via CDN. Este repositório NÃO possui banco de dados próprio, não define modelo de domínio persistente e não executa operações de persistência. Toda responsabilidade de dados reside no backend externo (API REST/HTTPS), fora do escopo deste workspace."
```

## 2. Entities
```yaml
entities: []
```

> **Nota de rastreabilidade:** Conforme `Database Context` (db-context), este repositório não possui banco de dados nem modelo de domínio persistente. O frontend apenas consome a API externa via `frontend/src/services/api.js`. Para o modelo de dados real (entidades, relacionamentos, constraints, DDL, migrações), consultar a documentação do backend (fora deste workspace). <!-- source: docs/07-database/db-manifest.md -->

## 3. Evidence & Assumptions
```yaml
assumptions:
  - id: "ASM-001"
    claim:
      object: "frontend/src/services/api.js"
      interpretation: "Este arquivo é o único ponto de integração com dados; encapsula chamadas HTTP para o backend externo. Não há camada de persistência local (IndexedDB, localStorage para dados de negócio, etc.) identificada no codebase."
    confidence: 0.99
    evidence:
      - source: "frontend/src/services/api.js:1-60"
      - source: "docs/07-database/db-manifest.md#1"
    status: "validated"
  - id: "ASM-002"
    claim:
      object: "Glossário (Ubiquitous Language) — termos de domínio"
      interpretation: "Os termos extraídos (aprovar, atualizar, buscarPorId, cancelar, concluir, criar, custoTotalPorAtivo, deletar, iniciar, listar) refletem operações de UI/orquestração de chamadas à API, não entidades persistidas no frontend. São verbos de casos de uso disparados pela interface."
    confidence: 0.95
    evidence:
      - source: "docs/03-domain/glossario.md#1"
    status: "validated"
  - id: "ASM-003"
    claim:
      object: "package.json + varredura de dependências"
      interpretation: "Nenhuma dependência de banco de dados, ORM, query builder, ODM ou driver de banco foi encontrada (apenas @popperjs/core). Confirma ausência intencional de persistência neste repositório."
    confidence: 1.0
    evidence:
      - source: "package.json"
      - source: "diagnóstico determinístico — Stack real e verificada"
    status: "validated"
```

**Regra de threshold:**
| Confiança | Ação |
| :--- | :--- |
| ≥ 0.95 | Pode ser incorporado automaticamente |
| 0.80 – 0.94 | Pode ser proposto, requer revisão leve |
| 0.60 – 0.79 | Requer revisão humana explícita |
| < 0.60 | Não deve virar decisão de schema |