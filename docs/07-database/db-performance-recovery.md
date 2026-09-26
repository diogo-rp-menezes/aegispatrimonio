# Database Performance, Recovery & Observability — Aegis1 Frontend

> **Versão:** 1.0 · **Owner:** Frontend Team · **Status:** Active

> **Nota importante:** Este repositório **não possui banco de dados próprio**. Conforme a *Database Schema Specification* ([[db-schema-spec]]), o Aegis1 Frontend é uma SPA desacoplada servida via CDN; toda responsabilidade de persistência, performance de queries, backup, recovery e observabilidade de banco de dados reside no **backend externo** (API REST/HTTPS), fora do escopo deste workspace. O array `entities` no modelo de domínio está vazio e nenhuma dependência de banco, ORM ou query builder foi encontrada no `package.json` (apenas `@popperjs/core`). <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/07-database/db-domain-model.md#2 --> <!-- source: package.json -->

---

## 1. Performance Targets

```yaml
performance:
  workload: "N/A — sem banco de dados neste repositório"
  targets:
    reads: { p50_ms: "N/A", p95_ms: "N/A", p99_ms: "N/A" }
    writes: { p95_ms: "N/A" }
  throughput:
    reads_per_second: "N/A"
    writes_per_second: "N/A"
  capacity:
    expected_rows: "N/A"
    growth_per_month: "N/A"
```

### Queries Críticas

| ID | Propósito | Latência Máx. | Índices Esperados |
| :--- | :--- | :--- | :--- |
| — | Não aplicável — não há queries de banco neste repositório | — | — |

> **Contexto:** A única camada de acesso a dados no frontend é o módulo `frontend/src/services/api.js`, que encapsula chamadas HTTP para o backend externo. Os requisitos de latência para essa integração estão definidos no **NFR** (NFR-P01: latência P95 da chamada `request` ≤ 800 ms). <!-- source: docs/07-database/db-domain-model.md#3 (ASM-001) --> <!-- source: docs/03-architecture/nfr.md#1 (NFR-P01) -->

---

## 2. Backup & Recovery

```yaml
recovery:
  backup:
    frequency: "N/A — sem banco de dados neste repositório"
    retention: "N/A"
    types: []
  rpo_target: "N/A — dados de negócio são responsabilidade do backend externo"
  rto_target: "N/A — para o frontend, RTO de deploy ≤ 15 min (rollback via CDN) conforme NFR-A02"
  disaster_recovery:
    regions: ["N/A — assets estáticos servidos via CDN multi-região (configuração de infra, fora do repo)"]
    failover_strategy: "N/A — responsabilidade da camada de infra/CDN"
  restore_test_frequency: "N/A"
```

> **Evidência:** O *Database Schema Specification* afirma explicitamente: "RPO (Recovery Point Objective) = 0 para dados de frontend (assets imutáveis versionados); dados de negócio são responsabilidade do backend." <!-- source: docs/07-database/db-schema-spec.md#Evidências e Rastreabilidade --> <!-- source: docs/03-architecture/nfr.md#3 (NFR-A03) -->

---

## 3. Observability

```yaml
observability:
  metrics:
    - name: "api_request_latency_p95_ms"
      warning_threshold: "600"
      critical_threshold: "800"
      source: "NFR-P01 / NFR-O03"
    - name: "api_error_rate_5min_pct"
      warning_threshold: "0.5"
      critical_threshold: "1.0"
      source: "NFR-P04 / NFR-O04"
    - name: "token_refresh_failure_rate_15min_pct"
      warning_threshold: "2"
      critical_threshold: "5"
      source: "NFR-O04"
    - name: "core_web_vitals_lcp_p75_ms"
      warning_threshold: "2000"
      critical_threshold: "2500"
      source: "NFR-O02"
  alerts:
    - name: "API Latency P95 > 800ms (5min)"
      condition: "api_request_latency_p95_ms > 800 for 5m"
      severity: "critical"
    - name: "API Error Rate > 1% (5min)"
      condition: "api_error_rate_5min_pct > 1.0 for 5m"
      severity: "critical"
    - name: "Token Refresh Failure Rate > 5% (15min)"
      condition: "token_refresh_failure_rate_15min_pct > 5 for 15m"
      severity: "high"
    - name: "LCP P75 > 2.5s"
      condition: "core_web_vitals_lcp_p75_ms > 2500"
      severity: "medium"
  migration_monitoring:
    lock_duration: false
    execution_duration: false
    affected_rows: false
    errors: false
```

> **Observação:** A seção `migration_monitoring` é mantida como `false` porque **não há migrações de banco de dados neste repositório** (ausência de schema, ORM, ferramentas de migração). Toda observabilidade relevante ao frontend está centrada na **integração HTTP com o backend** (latência, erros, refresh de token) e em **Core Web Vitals** da aplicação servida via CDN. <!-- source: docs/07-database/db-schema-spec.md#1 --> <!-- source: docs/03-architecture/nfr.md#6 (NFR-O01 a NFR-O04) -->

---

## 4. Rastreabilidade e Próximos Passos

| Item | Status | Ação Requerida |
| :--- | :--- | :--- |
| Performance targets de banco (reads/writes) | **Não aplicável** | Consultar documentação do backend externo para SLOs de banco |
| Backup / RPO / RTO de banco | **Não aplicável** | Confirmar com time de backend/infra a estratégia de disaster recovery do banco de produção |
| Observabilidade de queries / locks / migrações | **Não aplicável** | Instrumentar APM no backend (ex.: Datadog, New Relic) para visibilidade de banco |
| Alertas de banco (conexões, disco, replicação) | **Não aplicável** | Definir e configurar no plano de observabilidade do backend |

> **Conclusão:** Este documento existe para satisfazer o catálogo de artefatos do projeto, mas seu conteúdo reflete a arquitetura real: **frontend-only, sem persistência local**. Qualquer necessidade de performance, backup, recovery ou observabilidade de **banco de dados** deve ser atendida consultando a documentação e os runbooks do **serviço de backend correspondente**.