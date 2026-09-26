# Database Test & Data Quality Specification — Aegis1 Frontend

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active
> Complementa [[test-strategy]] com testes específicos de banco de dados.

---

## Contexto Arquitetural

Este repositório **não possui banco de dados próprio**. Conforme o *Database Schema Specification* ([[db-schema-spec]]) e o *Database Domain & Entity Model* ([[db-domain-model]]), o Aegis1 Frontend é uma SPA desacoplada servida via CDN; toda responsabilidade de persistência reside no backend externo (API REST/HTTPS), fora do escopo deste workspace. O array `entities` no modelo de domínio está vazio e nenhuma dependência de banco, ORM ou query builder foi encontrada no `package.json` (apenas `@popperjs/core`). <!-- source: docs/07-database/db-schema-spec#1 --> <!-- source: docs/07-database/db-domain-model.md#2 --> <!-- source: package.json -->

**Consequência direta:** Não existem tabelas, colunas, constraints, índices, views, functions, triggers ou relacionamentos declarados neste contrato. Qualquer necessidade de testes de schema, integridade, invariantes de negócio no banco, segurança em nível de banco ou regras de qualidade de dados deve ser atendida consultando a documentação e a suíte de testes do serviço de backend correspondente.

---

## 1. Schema Tests

| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| — | — | — | **Não aplicável** — este repositório não declara objetos de banco de dados. Verificar testes de schema no repositório do backend externo. |

---

## 2. Integrity Tests

| ID | Tipo | Alvo | Condição Esperada |
| :--- | :--- | :--- | :--- |
| — | — | — | **Não aplicável** — ausência de chaves estrangeiras, constraints ou relacionamentos no frontend. A integridade referencial é garantida pelo backend. |

---

## 3. Invariant Tests (regras de negócio no banco)

```yaml
invariant_tests: []
```

> Não há invariantes de negócio implementadas no banco de dados deste repositório — a camada de persistência é externa. Regras como BR-01 a BR-06 (transições de estado de ordens, validação de custos, permissões) são validadas no frontend via testes de integração (MSW) e contratos Pact, conforme [[test-strategy#2]].

---

## 4. Security Tests

| ID | Role | Operação | Esperado |
| :--- | :--- | :--- | :--- |
| — | — | — | **Não aplicável** — não há roles, grants, RLS ou políticas de segurança em nível de banco neste workspace. A segurança de dados (tokens, PII) é testada no frontend via SAST (Semgrep) e validação de que `authInterceptor` não vaza JWT em logs/erros, conforme [[test-strategy#5]]. |

---

## 5. Data Quality Rules

```yaml
data_quality: []

anomaly_detection:
  enabled: false

severity_thresholds:
  warning: "N/A"
  critical: "N/A"
```

> Não há colunas, tabelas ou pipelines de dados neste repositório para aplicar regras de qualidade (not_null, unique, custom thresholds). A qualidade dos dados consumidos da API é verificada indiretamente via:
> - **Contract Tests (Pact):** garantem que o shape e tipos dos payloads correspondem ao acordado com o backend ([[test-strategy#2]])
> - **Integration Tests (MSW):** validam tratamento de erros 409, 401, 404, 5xx, timeout e paginação/filtros ([[test-strategy#2]])
> - **E2E Tests:** confirmam consistência de `custoTotalPorAtivo` entre backend e frontend ([[test-strategy#1]])

---

## Rastreabilidade e Próximos Passos

| Item | Ação | Responsável | Fonte |
| :--- | :--- | :--- | :--- |
| Testes de schema/integridade/invariantes/segurança/dados no banco | Executar no repositório do **backend externo** (fora deste workspace) | Backend Team | [[db-schema-spec#Evidências e Rastreabilidade]] |
| Validação de contratos de dados (Pact) | Manter consumer tests no frontend; provider verification no backend | Dev + Backend Lead | [[test-strategy#2]] |
| Monitoramento de qualidade de dados em produção | Implementar no backend (ex: Great Expectations, dbt tests) | Backend Team | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Premissa: backend possui pipeline de dados observável |

---

> **Nota final:** Este documento existe para satisfazer o catálogo de artefatos do projeto, mas seu conteúdo é intencionalmente vazio no que tange a testes de banco de dados, refletindo a arquitetura real: **frontend-only, sem persistência local**. Qualquer especificação de testes de banco deve ser mantida no repositório do serviço de backend correspondente.