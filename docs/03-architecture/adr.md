# Architecture Decision Records (ADR) — Aegis1

> **Versão:** 2.0 · **Owner:** Arquitetura/Tech Lead · **Status:** Accepted (todas)
> **Base:** System Architecture v2.0 + BRD v2.0 + NFR v2.0 + Análise AST Java Completa
> **Formato:** MADR (Markdown ADR) — Context, Decision, Consequences, Compliance

---

## Índice de ADRs

| ID | Título | Status | Data | Impacto |
| :--- | :--- | :--- | :--- | :--- |
| **ADR-001** | Busca Fuzzy Nativa (Levenshtein Java) vs Elasticsearch/Algolia | Accepted | 2025-01-10 | Backend Search |
| **ADR-002** | Manutenção Preditiva: Regressão Linear Simples (OLS) vs ML Complexo | Accepted | 2025-01-10 | Backend Predictive |
| **ADR-003** | RBAC Granular "Aegis Shield" (Role×Permission×Contexto) vs Spring ACL/Casbin/Keycloak | Accepted | 2025-01-10 | Security |
| **ADR-004** | Multi-tenancy: Hibernate Filter (Query-level) vs Schema-per-tenant / Column-discriminator | Accepted | 2025-01-10 | Data Architecture |
| **ADR-005** | Auditoria: Hibernate Envers (Automático) vs Custom Triggers / Event Sourcing / CDC | Accepted | 2025-01-10 | Audit/Compliance |
| **ADR-006** | PDF/QR Code: FlyingSaucer (Thymeleaf) + ZXing vs iText / PDFBox / Puppeteer | Accepted | 2025-01-10 | Reports |
| **ADR-007** | Frontend Stack: Vue 3 + Vite + PWA vs React/Next.js / SvelteKit / Vanilla | Accepted | 2025-01-10 | Frontend |
| **ADR-008** | Testes Integração: TestContainers MySQL Real vs H2 / Mockito Only | Accepted | 2025-01-10 | Quality |
| **ADR-009** | PWA Offline-First: Service Worker + IndexedDB + Background Sync vs LocalStorage Only | Accepted | 2025-01-10 | Frontend/PWA |
| **ADR-010** | LGPD Anonimização: Hash Preservado em Envers vs Hard Delete / Pseudonimização Simples | Accepted | 2025-01-10 | Compliance |
| **ADR-011** | Arquitetura Backend: Modular Monolith (Spring Boot) vs Microserviços | Accepted | 2025-01-10 | Architecture |
| **ADR-012** | Banco de Dados: MySQL 8.0 vs PostgreSQL / SQL Server / Oracle | Accepted | 2025-01-10 | Data |
| **ADR-013** | Autenticação: JWT RS256 Stateless + Refresh Rotation vs Session/Cookie / OAuth2 Server | Accepted | 2025-01-10 | Auth |
| **ADR-014** | Observabilidade: Prometheus/Grafana/Tempo/Loki vs Datadog/New Relic/Elastic APM | Accepted | 2025-01-10 | Observability |
| **ADR-015** | CI/CD: GitHub Actions + K8s (Helm/Kustomize) vs GitLab CI / Jenkins / ArgoCD | Accepted | 2025-01-10 | DevOps |

---

## ADR-001 — Busca Fuzzy Nativa (Levenshtein Java) vs Elasticsearch/Algolia

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Backend Lead, Arquiteto | **Consulted:** DBA, Product, Segurança

### Contexto
Requisito BR-11/12: Busca global unificada tolerante a erros de digitação (ex.: "notebok" → "Notebook Dell Latitude 5520") combinada com filtros exatos (filial, status, tipo, data) e paginação server-side. Volume estimado: 100k–500k ativos + ordens + funcionários + fornecedores.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Levenshtein Java Nativo + Trigram Indexes MySQL + Redis Cache** (Escolhida) | Algoritmo Levenshtein otimizado (Wagner-Fischer + early exit) em `LevenshteinDistance.java`; Índices `pg_trgm` (via plugin MySQL) ou `FULLTEXT` ngram; Cache Redis TTL 5min para queries frequentes | Zero dependência externa; Controle total de ranking (boost por tipo, threshold configurável); Latência P95 < 300ms (testado 100k); Custo zero licença; Simplicidade operacional | Requer índices trigram/ngram no MySQL (plugin); Memória Redis para cache; Implementação custom ranking |
| **B: Elasticsearch / OpenSearch** | Cluster dedicado (3+ nodes); Analyzer ngram/edge_ngram; Fuzzy query nativo | Poderoso para full-text complexo; Escalável horizontalmente; Recursos avançados (highlight, suggest, synonyms) | Complexidade operacional alta (cluster, JVM, shards, replicas); Custo infra (3 nodes ≈ $300/mês); Latência rede adicional; Overkill para fuzzy simples + filtros exatos |
| **C: Algolia / Meilisearch / Typesense** | SaaS (Algolia) ou Self-hosted (Meili/Typesense); API REST; Ranking configurável | Setup rápido; UX excelente (instant search, typo tolerance); Dashboard analytics | Custo SaaS alto (Algolia > $1.5k/mês para 100k records); Vendor lock-in; Self-hosted ainda requer infra; Dados sensíveis saem do perímetro (LGPD) |
| **D: Apenas pg_trgm / FULLTEXT MySQL (sem Levenshtein ranking)** | `WHERE col % 'termo'` (similarity) ou `MATCH() AGAINST()` | Simples; Nativo MySQL 8.0 | Sem ranking fuzzy preciso (Levenshtein distance); Threshold fixo; Difícil combinar boost por tipo + filtros exatos + paginação performática |

### Decisão
**Opção A** — Implementar `FuzzySearchService` com `LevenshteinDistance` otimizado (early exit se distance > threshold), índices trigram/ngram no MySQL para candidate filtering, Redis cache para queries repetidas, threshold configurável (default 0.7 similarity), boost por tipo de entidade.

### Consequências
- **Positivas:** Latência controlada (< 300ms P95), custo zero, simplicidade, LGPD compliant (dados não saem), threshold/runtime configurável via `@RefreshScope`.
- **Negativas:** Manutenção de algoritmo custom; Necessidade de plugin trigram MySQL (ou migração para PostgreSQL onde é nativo); Cache invalidation strategy.
- **Riscos:** Performance em base > 500k (mitigação: partitionamento + read replicas + cache warming).
- **Compliance:** NFR-P02, NFR-S03, NFR-SEC04 (dados não vazam), NFR-M01 (ciclomática ≤ 10 no service).

---

## ADR-002 — Manutenção Preditiva: Regressão Linear Simples (OLS) vs ML Complexo

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Data Engineer, Backend Lead | **Consulted:** Product, Operações Manutenção

### Contexto
Requisito BR-10: Prever falha de disco baseada em métricas SMART históricas (reallocated_sectors, seek_error_rate, spin_retry_count, temperature, power_on_hours). Batch noturno para 10k ativos. Deve gerar ordem preditiva automática se probabilidade > 80% e horizonte < 30 dias.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Regressão Linear Simples (OLS - Mínimos Quadrados) em Java Puro** (Escolhida) | `ManutencaoPreditivaService.preverFalhaDisco()`: y = ax + b (reallocated_sectors vs power_on_hours); Cálculo analítico (Σx, Σy, Σxy, Σx²); IC 95% via t-Student; Sem dependências ML | Interpretável (coeficientes explicam relação); Leve (CPU/memória mínimos); Roda em batch noturno single-thread; Zero dependência ML runtime (Python, TensorFlow, ONNX); Fácil auditoria/teste; Deploy simples (JAR único) | Assume linearidade (pode subestimar falhas não-lineares); Sensível a outliers; Requer ≥ 3 pontos históricos; Precisão limitada vs ensemble/ML |
| **B: Random Forest / XGBoost / LightGBM (Java via SMILE/Tribuo ou Python Sidecar)** | Modelo ensemble não-linear; Feature engineering (rolling windows, lag features) | Captura não-linearidades; Maior precisão geral; Feature importance | Dependência ML runtime (Python sidecar = complexidade deploy, latência, serialização) OU lib Java limitada (SMILE/Tribuo menos maduras); Treino/retrain pipeline; Menos interpretável; Overhead CPU/memória |
| **C: LSTM / RNN (Time Series Forecasting)** | Rede neural sequencial; Input: janela temporal de métricas SMART | Modela dependências temporais complexas; State-of-art para séries temporais | Muito complexo para v1; Requer GPU/TPU para treino; Data hunger (milhares de séries longas); Black box; Deploy serving complexo (TF Serving/TorchServe) |
| **D: Prophet / ARIMA / Exponential Smoothing** | Modelos estatísticos de série temporal | Interpretáveis; Sazonalidade/feriados; Uncertainty intervals | Projetados para séries temporais regulares (diárias/semanais); SMART é esparso/irregular; Não captura bem degradação monotônica de disco |

### Decisão
**Opção A** — Regressão Linear Simples (OLS) implementada nativamente em `ManutencaoPreditivaService` usando `org.apache.commons.math3.stat.regression.SimpleRegression` ou implementação própria (Mínimos Quadrados). Threshold conservador (prob > 80%, horizonte < 30 dias). Fallback: health check baseado em regras (score < 40 → alerta imediato). Evolução para ML (v2.0) quando histórico ≥ 1 ano.

### Consequências
- **Positivas:** Simplicidade extrema (single JAR), interpretabilidade total (auditoria), performance batch 10k ativos < 5 min, zero dependências ML, compliance LGPD/ISO 27001 (modelo explicável).
- **Negativas:** Precisão limitada nos primeiros meses (poucos dados); Falsos positivos/negativos possíveis; Não captura padrões não-lineares (ex.: degradação acelerada).
- **Mitigação:** Coleta contínua melhora modelo; Threshold conservador; Health check regras como safety net; Métricas de precisão (real vs previsto) monitoradas no Grafana.
- **Compliance:** NFR-P03, NFR-C04 (modelo explicável), NFR-M01.

---

## ADR-003 — RBAC Granular "Aegis Shield" (Role×Permission×Contexto) vs Spring ACL / Casbin / Keycloak

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Security Lead, Arquiteto | **Consulted:** Backend, Frontend, Compliance, DPO

### Contexto
Requisitos BR-13, BR-14, NFR-SEC03, NFR-SEC04: Controle de acesso granular, hierárquico, contextual com multi-tenancy por Filial. Permissões por recurso/ação/contexto (ex.: GESTOR pode `ORDEM_APROVAR` na sua filial; TECNICO só `ORDEM_INICIAR`/`CONCLUIR` nas suas ordens). Admin Global vê tudo. Auditoria de alterações de permissão.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Aegis Shield Nativo (Spring Security + Custom PermissionEvaluator + Matriz Configurável)** (Escolhida) | `AegisShieldPermissionEvaluator` implementa `PermissionEvaluator`; Matriz `Role × Permission × Contexto (GLOBAL/FILIAL)` armazenada em BD (`role_permission_context`); Admin Global edita via UI; Avaliação SpEL + Custom Logic (hierarquia roles + ownership + contexto filial); Integração nativa `@PreAuthorize("hasPermission(#ordem, 'APROVAR')")` | Nativo Spring (performance, integração Method Security); Flexibilidade total (matriz dinâmica, sem redeploy); Contexto filial nativo (multi-tenancy); Auditoria Envers em `RolePermissionContext`; UI Admin para gestão; Zero dependência externa | Desenvolvimento custom (PermissionEvaluator, Matriz, UI); Testes abrangentes necessários (matriz 5 roles × 20 permissions × 2 contextos = 200 combinações); Manutenção de regras de consistência (ex.: TECNICO não pode APROVAR) |
| **B: Spring ACL (Access Control List)** | ACLs por objeto (ObjectIdentity + Sid + Permission); Tabelas `acl_object_identity`, `acl_entry`, `acl_class` | Granularidade por instância (objeto); Nativo Spring Security | Complexidade extrema (tabelas, cache, lookup); Performance ruim em escala (join ACLs); Não suporta nativamente "contexto filial" + hierarquia roles; Configuração programática trabalhosa; Overkill para RBAC por tipo/ação/contexto |
| **C: Casbin (Policy Engine Externo)** | Arquivo policy (CSV/DB) + Enforcer; Modelos: RBAC, ABAC, RESTful; SDKs multi-linguagem | Separação policy/code; Modelos formais (ABAC); Performance (cache); Multi-linguagem | Dependência externa (Go library + Java wrapper); Deploy sidecar ou library; Curva aprendizado modelo (REQUEST_DEFINITION, POLICY_DEFINITION); Menos integrado Spring Method Security; Auditoria policy changes custom |
| **D: Keycloak (IAM Externo) + Resource Permissions** | Realm + Clients + Roles + Resources + Policies + Permissions; UMA 2.0 | Enterprise-grade; SSO, Social, MFA, User Federation; Admin Console rico; Fine-grained permissions | Complexidade operacional alta (cluster Keycloak, DB, themes); Latência rede (token introspection / UMA); Overkill para RBAC interno; Multi-tenancy realms/groups complexo; Custo infra; Vendor lock-in |

### Decisão
**Opção A** — `AegisShieldPermissionEvaluator` custom + Matriz `RolePermissionContext` (entidade JPA) + UI Admin (Matriz Visual Role × Permission × Contexto) + Hierarquia Roles fixa (ADMIN > GESTOR > TECNICO > USER > AUDITOR) + Contexto Filial nativo via `MultiTenancyFilter`.

### Consequências
- **Positivas:** Performance nativa (sem rede), flexibilidade total, auditoria integrada (Envers), multi-tenancy nativo, UI self-service Admin, zero vendor lock-in.
- **Negativas:** Código custom para manter; Testes de integração cross-tenant obrigatórios CI; Regras de consistência matriz (validação no service).
- **Riscos:** Bugs de autorização (mitigação: matriz de testes 100% combinações + property-based testing + pen test).
- **Compliance:** NFR-SEC03, NFR-SEC04, NFR-C04, NFR-C05.

---

## ADR-004 — Multi-tenancy: Hibernate Filter (Query-level) vs Schema-per-tenant / Column-discriminator

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DBA, Backend Lead | **Consulted:** DevOps, Security, Compliance

### Contexto
Requisito BR-13: Isolamento de dados por Filial (tenant). Usuário da Filial A não vê dados da Filial B. Admin Global vê todas. 100 filiais estimadas. Performance crítica (queries com filtro automático).

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Hibernate Filter (Query-level) + `@FilterDef` + `MultiTenancyFilter`** (Escolhida) | `@FilterDef(name="filialFilter", parameters=@ParamDef(name="filialId", type="Long"))` em TODAS entidades; `@Filter(name="filialFilter", condition="filial_id = :filialId")`; `MultiTenancyFilter` (OncePerRequestFilter) extrai `filialId` do JWT → `TenantContextHolder.set(filialId)` → Hibernate Filter ativado automaticamente; Admin Global: `filialId = null` (filter desativado) | Isolamento REAL no SQL (WHERE filial_id = ? em TODAS queries); Performance nativa (índice em filial_id); Compatível Envers (auditoria mantém filial_id); Testável (TestContainers cross-tenant); Zero código boilerplate nos repositories; Admin Global vê tudo sem query especial | Requer disciplina: TODAS entidades devem ter `filial_id` + `@Filter`; Admin Global precisa bypass explícito; Não isola schema (mesmo banco); Migração schema-per-tenant futura complexa |
| **B: Schema-per-tenant (Database per Filial)** | Cada filial = schema/database separado; `AbstractRoutingDataSource` roteia por tenant | Isolamento físico total (LGPD/GDPR forte); Backup/restore por filial; Escalabilidade horizontal (sharding) | Complexidade operacional extrema (100+ schemas, migrações Flyway por schema, connection pooling por schema); Cross-tenant queries impossíveis (relatórios globais); Custo infra 100x; Overkill para 100 filiais |
| **C: Column-discriminator (Shared Schema, Shared Table) + WHERE manual** | Todas tabelas têm `filial_id`; Developers adicionam `AND filial_id = :filialId` em TODAS queries (QueryDSL/Specifications/Manual) | Simples conceitualmente; Um schema | **Erro humano garantido** (esquecer WHERE = vazamento); Não testável automaticamente; Boilerplate massivo; Envers não filtra automaticamente; Performance depende de índice + query plan |
| **D: Row-Level Security (RLS) PostgreSQL / MySQL 8.0 RLS** | Policy no banco: `CREATE POLICY filial_isolation ON ativo USING (filial_id = current_setting('app.filial_id')::bigint)` | Isolamento no banco (mesmo com SQL direto); Performance nativa; Admin bypass via `SET app.filial_id = 0` | MySQL 8.0 RLS limitado (views com security_invoker); PostgreSQL nativo; Requer `SET` por conexão (connection pool complexity); Menos portável; Debugging difícil |

### Decisão
**Opção A** — Hibernate Filter com `MultiTenancyFilter` injetando `filialId` no `TenantContextHolder` (ThreadLocal/InheritableThreadLocal). `@Filter` em **TODAS** entidades de domínio (validado por ArchUnit test em CI). Admin Global: `filialId = null` desativa filter.

### Consequências
- **Positivas:** Isolamento garantido no SQL (EXPLAIN mostra `WHERE filial_id = ?`); Performance (índice `filial_id` em todas tabelas); Envers compatível; Testes cross-tenant CI (TestContainers) = vazamento = build fail; Simplicidade repositórios (Spring Data JPA padrão).
- **Negativas:** Disciplina obrigatória (ArchUnit test); Admin Global bypass especial; Schema compartilhado (LGPD: anonimização por filial requer cuidado).
- **Riscos:** Esquecer `@Filter` em nova entidade (mitigação: ArchUnit test + Code Review checklist).
- **Compliance:** NFR-SEC04, NFR-S03, NFR-C01, NFR-C04.

---

## ADR-005 — Auditoria: Hibernate Envers (Automático) vs Custom Triggers / Event Sourcing / CDC

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DBA, Compliance, Backend Lead | **Consulted:** Auditoria, Legal, Segurança

### Contexto
Requisito BR-16, NFR-C02, NFR-C04, NFR-C05: Auditoria imutável de TODAS alterações em entidades de domínio (CREATE/UPDATE/DELETE) com diff campo-a-campo (antes/depois), usuário, timestamp, IP, user-agent. Retenção 7 anos. Imutável (não pode ser desativado/alterado). Export assinado para evidência legal.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Hibernate Envers (Automático @Audited)** (Escolhida) | `@Audited` em todas entidades domínio; Tabelas `*_AUD` + `REVINFO`; `RevisionListener` custom captura usuarioId, IP, UA; `AuditReader` query API (revisões, diff, data); Conditional auditing (`@Audited(withModifiedFlag=true)`); Export JSON/PDF assinado | Nativo JPA/Hibernate (zero boilerplate); Diff campo-a-campo automático; Query API rica (revisões por entidade, por data, por usuário); Imutável (append-only); Conditional auditing (performance); Integração Spring Data JPA; Retenção via partitionamento `REVINFO` | Tabelas `_AUD` crescem (partitioning necessário); Overhead escrita ~10% (aceitável); Não audita DDL/schema changes; Custom `RevisionListener` para contexto web (IP, UA, userId) |
| **B: Custom Triggers (PostgreSQL/MySQL) + Tabelas Auditoria Próprias** | `CREATE TRIGGER ... AFTER INSERT/UPDATE/DELETE` → INSERT em `auditoria_custom` | Controle total SQL; Performance previsível; Independente ORM | Manutenção massiva (trigger por tabela, por coluna); Diff manual (NEW vs OLD); Não portável; Dificulta migrações Flyway; Envers já faz isso nativamente |
| **C: Event Sourcing (Event Store / Axon / Custom)** | Estado = sequência de eventos; `EventStore` append-only; Projections para read models | Auditoria nativa (eventos SÃO a auditoria); Replay temporal; CQRS natural | Mudança paradigmática (Domain-Driven Design completo); Complexidade alta (event versioning, snapshots, projections); Overkill para CRUD + auditoria; Curva aprendizado time |
| **D: Change Data Capture (Debezium + Kafka + Elasticsearch/S3)** | Connector Debezium lê binlog MySQL → Kafka → Sink (Elasticsearch para busca, S3 para archive) | Tempo real; Não invasivo (lê binlog); Escalável; Múltiplos consumers | Complexidade infra (Kafka, Connect, Schema Registry); Latência (near real-time); Não substitui auditoria transacional (precisa correlacionar transação); Custo infra; Overkill para requisito |

### Decisão
**Opção A** — `@Audited` em 100% entidades de domínio (validado por ArchUnit test). `CustomRevisionListener` captura `usuarioId`, `ip`, `userAgent` via `RequestContextHolder`. `AuditoriaController` expõe `/api/v1/auditoria` com diff visual (antes/depois). Partitioning `REVINFO` por mês. Retenção 7 anos (política `DROP PARTITION` job anual).

### Consequências
- **Positivas:** Implementação rápida (anotações), diff automático, query API poderosa, imutável, padrão JPA, retenção gerenciável via partitioning.
- **Negativas:** Armazenamento cresce (partitioning + compressão); Overhead escrita ~10% (monitorar); Não audita operações bulk/nativas SQL (mitigação: evitar bulk ou auditar manualmente).
- **Riscos:** Performance auditoria consultas (mitigação: índices em `REVINFO.timestamp`, `entidade_id`, `usuario_id`; read replicas).
- **Compliance:** NFR-C02 (retenção 7 anos), NFR-C04 (ISO 27001 A.12.4), NFR-C05 (SOC 2), BR-16.

---

## ADR-006 — PDF/QR Code: FlyingSaucer (Thymeleaf) + ZXing vs iText / PDFBox / Puppeteer

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Backend Lead | **Consulted:** Frontend, Compliance (assinatura digital)

### Contexto
Requisitos BR-17, BR-18: Termo de Responsabilidade PDF (assinatura digital placeholder v1, ICP-Brasil/gov.br futuro) + Etiquetas QR Code (unitário + lote 100 etiquetas < 10s). Performance: PDF unitário < 2s, Lote < 10s.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: FlyingSaucer (iText 5 fork) + Thymeleaf Templates + ZXing (QR Code)** (Escolhida) | `PdfGenerator`: Thymeleaf processa template HTML/CSS → XHTML → FlyingSaucer (iText 5.5.x fork) → PDF; `QRCodeGenerator`: ZXing (Google) gera SVG/PNG QR Code; Templates versionados no classpath; Pure Java | Licença MIT/Apache 2.0 (iText 5 fork = FlyingSaucer); HTML/CSS templates (fácil design, designers podem editar); ZXing nativo Java (QR Code SVG/PNG); Performance adequada (< 2s unit, < 10s lote 100); Zero dependência Node/Headless Chrome | FlyingSaucer suporta CSS 2.1 limitado (sem Flexbox/Grid moderno); Templates HTML precisam ser "print-friendly"; Assinatura digital ICP-Brasil requer integração separada (placeholder v1) |
| **B: iText 7 / iText 8 (AGPL / Commercial)** | API programática iText 7/8; Layout engine avançado; Assinatura digital nativa (PAdES, CAdES) | Suporte completo PDF 2.0, PAdES-LTV, assinatura digital certificada ICP-Brasil; Layout moderno (Flexbox-like); Performance alta | **Licença AGPL (viral) ou Comercial ($$$)** — incompatível com MIT do projeto; Requer licença comercial para uso fechado |
| **C: Apache PDFBox (Apache 2.0)** | Low-level PDF manipulation; Renderização; Assinatura digital (básico) | Licença Apache 2.0; Pure Java; Assinatura digital possível | API low-level (muito código para layout); Templates não HTML/CSS; Performance menor para documentos complexos; Assinatura digital PAdES limitada |
| **D: Puppeteer / Playwright (Headless Chrome/Chromium) via Node Sidecar** | Frontend renderiza HTML/CSS moderno → Headless Chrome → PDF | Suporte CSS moderno completo (Flexbox, Grid, @page); Pixel-perfect; Assinatura digital via Web Crypto API | **Dependência Node.js + Chromium** (imagem Docker > 1GB); Latência cold start; Complexidade deploy (sidecar ou serviço separado); Recursos pesados (CPU/RAM); Não pure Java |

### Decisão
**Opção A** — `PdfGenerator` com Thymeleaf + FlyingSaucer (iText 5 fork MIT) + `QRCodeGenerator` com ZXing. Templates HTML/CSS em `src/main/resources/templates/pdf/`. Placeholder assinatura digital v1 (campo manual + testemunha); Integração ICP-Brasil/gov.br A1 planejada v1.1 (ADR futuro).

### Consequências
- **Positivas:** Pure Java, licença compatível (MIT/Apache), templates HTML/CSS editáveis, performance adequada, QR Code nativo.
- **Negativas:** CSS limitado (2.1); Assinatura digital requer trabalho extra v1.1.
- **Riscos:** Layout complexo pode quebrar no FlyingSaucer (mitigação: templates simples, tabelas, @page margins).
- **Compliance:** NFR-P04, BR-17, BR-18, NFR-C03 (NR-10/12 termo assinado).

---

## ADR-007 — Frontend Stack: Vue 3 + Vite + PWA vs React/Next.js / SvelteKit / Vanilla

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Frontend Lead, Arquiteto | **Consulted:** UX, Product, DevOps

### Contexto
Frontend moderno para SPA + PWA (offline-first, push, background sync, QR scanner). Requisitos: TypeScript strict, Composition API, State Management (Pinia), UI Library (Bootstrap 5 CSS-only), Code-splitting, Bundle ≤ 150 kB gzipped, PWA (Workbox), Testes (Vitest + Vue Test Utils), CI/CD.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Vue 3 + Vite + Pinia + Bootstrap 5 + Vite PWA Plugin (Workbox)** (Escolhida) | `<script setup>` + Composition API + TypeScript strict; Pinia (type-safe, modular); Bootstrap 5 (CSS-only, sem JS runtime, acessível); Vite (ESM nativo, HMR rápido, code-splitting automático, PWA plugin Workbox); Vue DevTools | Ecossistema maduro; Composition API + TS = type safety excelente; Pinia > Vuex (simples, type-safe); Bootstrap 5 = zero JS runtime, leve, acessível; Vite = build rápido, PWA plugin oficial; Bundle otimizado (tree-shaking, code-splitting); PWA: SW, Manifest, Push, Background Sync, Install Prompt | Bundle base Vue + Bootstrap ~40 kB gzipped (folga para 150 kB); Bootstrap classes verbosas (mitigação: Sass custom + purgecss) |
| **B: React 18 + Vite + Redux Toolkit / Zustand + Tailwind CSS** | Hooks + TS; Zustand (leve) ou RTK; Tailwind (utility-first, JIT) | Ecossistema enorme; Concurrent Features; Tailwind = design system via utils; RTK Query / TanStack Query para data fetching | Bundle React + RTK + Tailwind ~50-60 kB base; Tailwind JIT no build; Mais boilerplate (providers, hooks); Redux Toolkit verboso vs Pinia |
| **C: Next.js 14 (App Router) + React Server Components** | SSR/SSG/ISR nativo; Server Actions; Edge Runtime | SEO, First Paint, Streaming, Server Components, Edge Middleware | **Viola SAD:** "Static SPA + Backend externo", "Zero SSR", "Zero Edge Compute"; Lock-in Vercel/Node; Custo infra; Overkill para dashboard interno |
| **D: SvelteKit + Svelte 5 (Runes) + Tailwind** | Compilado (sem VDOM); Reactividade fine-grained; Kit full-stack | Bundle menor (sem VDOM); Sintaxe simples; Performance nativa; SvelteKit = full-stack | Ecossistema menor; Svelte 5 breaking changes; SvelteKit = full-stack (SSR) — viola SAD; Menos vagas/recursos mercado |
| **E: Vanilla TypeScript + Vite + Lit / Web Components** | Zero framework; Web Components nativos; Lit (leve wrapper) | Bundle mínimo (~10 kB); Interoperabilidade total; Standards-based | Produtividade baixa (boilerplate: routing, state, forms, validation); PWA manual; Menos ferramentas/testing; Curva aprendizado Web Components |

### Decisão
**Opção A** — Vue 3 + Vite + Pinia + Bootstrap 5 + Vite PWA Plugin (Workbox). TypeScript strict mode. Composition API + `<script setup>`. Bootstrap 5 via Sass (customização variáveis, purgecss). PWA: Service Worker (Workbox), Manifest, Push API, Background Sync, Install Prompt, QR Scanner (Barcode Detection API / fallback ZXing WASM).

### Consequências
- **Positivas:** Produtividade alta, type safety, bundle controlado, PWA nativo, ecossistema maduro, Bootstrap acessível, zero JS runtime UI.
- **Negativas:** Bootstrap classes verbosas (mitigação: componentes wrapper Vue + Sass mixins); Vue 3 migration guide se vindo de Vue 2 (projeto novo = não aplica).
- **Riscos:** Bundle size creep (mitigação: `vite-bundle-analyzer` CI gate ≤ 150 kB gzipped).
- **Compliance:** NFR-P06, P07, P08, NFR-U01, U02, U03, NFR-PO01, NFR-PO02.

---

## ADR-008 — Testes Integração: TestContainers MySQL Real vs H2 / Mockito Only

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, QA Lead, Backend Lead | **Consulted:** DevOps, DBA

### Contexto
Requisito NFR-M02: Coverage ≥ 80% gate CI. Testes de integração reais para: Flyway migrations, JPA queries nativas, Hibernate Filter (multi-tenancy), Envers auditoria, Stored Procedures, Índices Trigram, Transações, Locks.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: TestContainers MySQL 8.0 Real (Reuso Habilitado)** (Escolhida) | `@Testcontainers` + `MySQLContainer<>`; `.testcontainers.properties` com `testcontainers.reuse.enable=true`; Inicialização once per JVM (não por teste); Flyway migrate automático; Dados de teste via `@Sql` / TestDataBuilder | **MySQL real** = dialeto exato, queries nativas, índices trigram, partitioning, Envers, locks, transações; Confiança total pré-deploy; Reuso containers = velocidade (inicialização ~30s once vs 30s por teste); Paralelização CI (multiple containers) | Recursos CI (RAM/CPU) para containers; Flakiness raro (network, startup); Imagem MySQL 8.0 ~500MB pull |
| **B: H2 Database (Modo MySQL Compatibility)** | `spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL` | Rápido (in-memory); Zero container; Zero config CI | **Dialeto diferente** (funções, tipos, índices, trigram NÃO existem, partitioning NÃO suportado, Envers behavior diferente); Falso positivo/negativo em queries nativas; Não valida Flyway MySQL-specific |
| **C: Mockito Only (Unit Tests Only) + Contract Tests** | Mock `Repository` / `Service`; Testes unitários puros; Contract Tests (Pact) para API | Rápido; Isolado; Foco lógica negócio | **Não testa:** SQL real, JPA mapping, Hibernate Filter, Envers, Flyway, índices, performance, deadlocks, constraints; Cobertura falsa de integração |
| **D: TestContainers PostgreSQL (para validar portabilidade)** | Adicional ao MySQL; CI matrix: MySQL + PostgreSQL | Valida dialeto abstraction (Flyway, JPA); Futuro migração | Duplica tempo CI; PostgreSQL trigram nativo (pg_trgm) vs MySQL plugin; Adotar como "nice to have" v1.2 (ADR-012) |

### Decisão
**Opção A** — TestContainers MySQL 8.0 real com reuso habilitado (`.testcontainers.properties: testcontainers.reuse.enable=true`). Configuração CI: `services: mysql` (GitHub Actions) OU TestContainers auto-detect Docker. Testes: `*ControllerIT`, `*ServiceIT`, `*RepositoryIT`, `SecurityConfigIT`, `MultiTenancyIT`, `AegisShieldTest`, `IntegridadeChecksumIT`. Coverage gate 80% (JaCoCo + frontend Vitest coverage).

### Consequências
- **Positivas:** Confiança real (MySQL produção = MySQL teste); Valida Flyway, índices, Envers, Filter, Locks; Reuso = velocidade CI aceitável; Paralelizável.
- **Negativas:** CI mais lento/caro (mitigação: runners com 16GB RAM; cache Docker layers; paralelização matrix).
- **Riscos:** Flakiness container (mitigação: retry logic, health check container, timeout generoso).
- **Compliance:** NFR-M02, NFR-M04, NFR-A06, NFR-SEC04 (cross-tenant test).

---

## ADR-009 — PWA Offline-First: Service Worker + IndexedDB + Background Sync vs LocalStorage Only

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Frontend Lead, UX | **Consulted:** Product (Técnicos Campo), DevOps

### Contexto
Requisito UC-25/US-PRD-002: Técnicos em campo coletam Health Check (SMART) em locais sem conectividade (subsolo, datacenter, área rural). Dados devem sincronizar automaticamente ao reconectar. Push notifications para alertas preditivos/ordens. Install prompt para "app-like" experience.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Service Worker (Workbox via Vite PWA Plugin) + IndexedDB + Background Sync API + Push API** (Escolhida) | `vite-plugin-pwa` (Workbox) gera SW com: Precache (assets), Runtime Cache (API GET), Offline Fallback; IndexedDB (via `idb` lib ou nativo) armazena health checks offline; `navigator.serviceWorker.ready.sync.register('health-check-sync')` → Background Sync quando online; Push API (VAPID) para notificações servidor; Install Prompt (beforeinstallprompt) | Padrão web nativo (Progressive Enhancement); Workbox = best practices (cache strategies, expiration, broadcast update); IndexedDB = armazenamento estruturado, transacional, queryable; Background Sync = garantia entrega (retry exponencial nativo); Push = notificações mesmo app fechado; Install Prompt = "Add to Home Screen" nativo | Background Sync não suportado Safari (iOS) — fallback: sincronização ao abrir app (Service Worker `fetch` event + queue); Push iOS requer Web Push API (iOS 16.4+); Service Worker escopo = origin (subpath não suportado) |
| **B: LocalStorage Only + Manual Sync** | `localStorage.setItem('health_checks', JSON.stringify(queue))`; `window.addEventListener('online', sync)` | Simples; Funciona em todo browser (incluindo Safari antigo) | Limite 5MB (insuficiente para mídia/fotos); Sem transacionalidade; Sem retry exponencial nativo; Sem push; Sem install prompt; Race conditions (múltiplas abas) |
| **C: Dexie.js (IndexedDB Wrapper) + Custom SW + Push Library** | Dexie.js (reactive, schema, sync plugins); SW custom; `web-push` library | DX melhor (TypeScript, reactive, live queries); Sync plugin (CRDT/operational transform) | Dependência extra (Dexie ~30 kB); SW custom = reinventar Workbox; Push library = mais código; Overengineering para v1 |
| **D: Capacitor / Ionic (Native Bridge) — App Híbrido** | WebView + Plugins nativos (Storage, Background Task, Push, Camera, Barcode Scanner) | Acesso APIs nativas completas; Background Task real (iOS/Android); Push nativo (FCM/APNs); Camera/Scanner nativo | **Não PWA** — App híbrido (build iOS/Android); App Store/Play Store; Certificados; Code signing; Atualização via store (não instantânea); Overkill para v1 (roadmap v2.0) |

### Decisão
**Opção A** — Vite PWA Plugin (Workbox) + IndexedDB nativo (`idb` lib 8 kB) + Background Sync API + Push API (VAPID). Fallback iOS: sincronização ao `focus`/`visibilitychange` + `navigator.onLine` polling. QR Scanner: Barcode Detection API (Chrome/Edge) + fallback ZXing WASM (carregado dinamicamente).

### Consequências
- **Positivas:** Padrão web, offline-first real, push nativo, install prompt, bundle leve (Workbox ~15 kB + idb ~8 kB), background sync garantido (Chrome/Edge/Firefox).
- **Negativas:** iOS Safari limitações (Background Sync, Push < 16.4); SW escopo origin.
- **Mitigação iOS:** Detecta iOS → desabilita Background Sync → sincroniza ao abrir/focus; Push: solicita permissão se iOS 16.4+, senão polling + notificação local.
- **Compliance:** NFR-U02 (mobile), NFR-PO01, UC-25, US-PRD-002.

---

## ADR-010 — LGPD Anonimização: Hash Preservado em Envers vs Hard Delete / Pseudonimização Simples

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DPO, Legal, Compliance, Backend Lead | **Consulted:** Auditoria, Segurança

### Contexto
Requisito NFR-C01: Direito ao esquecimento (Art. 18 LGPD). Usuário solicita exclusão dos seus dados pessoais. Porém: Auditoria (Envers) deve ser mantida para conformidade legal (trabalhista, fiscal, segurança) — Art. 16 LGPD (exceção: cumprimento obrigação legal).

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Anonimização + Hash Preservado em Envers** (Escolhida) | `POST /usuarios/me/solicitar-exclusao` → `LgpdService.anonimizar(usuarioId)`: `Usuario.nome = "USUARIO_ANON_{SHA256(usuarioId+salt)[:8]}"`, `email = "{hash}@anonymized.local"`, `cpf = null`, `telefone = null`; `status = INATIVO`; Revoga tokens + invalida sessões; **Envers mantém revisões originais** (diff mostra valores reais antes) + **Nova revisão ANONIMIZACAO** com hash de correlação; `AuditoriaController` permite filtrar "anonimizados" | Cumpre LGPD (dados PII removidos do estado atual); Mantém rastreabilidade legal (Envers imutável com valores originais); Hash permite correlação anonimizado↔original se ordem judicial; Referências FK mantidas (usuario_id em ordens, auditoria) → integridade referencial | Hash em Envers = dado pseudo-pessoal (mitigação: salt secreto + hash truncado 8 chars); Complexidade service (transação única: anonimiza + revisão Envers); Export portabilidade deve incluir hash |
| **B: Hard Delete (DELETE FROM usuario + CASCADE)** | Remove usuário + cascata (ordens, ativos, auditoria?) | Simples; Remoção total | **Viola LGPD Art. 16** (obrigação legal retenção auditoria); Quebra integridade referencial (FK ordens, auditoria); Perde rastreabilidade; Não permite portabilidade posterior |
| **C: Pseudonimização Simples (Troca ID por UUID Aleatório)** | `usuario_id` → `uuid_random()` em todas tabelas; Mantém estrutura | Mantém referências; Remove PII direto | Reversível (mapeamento UUID↔original existe); Não remove PII dos campos (nome, email, CPF); Envers ainda tem valores originais; Complexidade migração IDs (FKs) |
| **D: Soft Delete Only (status = EXCLUIDO, mantém PII)** | Apenas marca `status = EXCLUIDO` | Simples; Mantém tudo | **Viola LGPD Art. 18** (dados PII permanecem acessíveis internamente); Não atende "direito ao esquecimento" |

### Decisão
**Opção A** — Anonimização irreversível dos campos PII (nome, email, CPF, telefone) no estado atual (`Usuario` table) + Preservação total no Envers (revisões históricas imutáveis) + Hash de correlação SHA-256(salt + usuarioId) truncado 8 chars armazenado em `usuario.anonimizacao_hash` + Nova revisão Envers tipo `ANONIMIZACAO` com hash. `LgpdController.exportar()` inclui hash para portabilidade.

### Consequências
- **Positivas:** LGPD compliant (Art. 18 + Art. 16); Auditoria imutável preservada; Integridade referencial mantida; Rastreabilidade judicial via hash; Portabilidade completa.
- **Negativas:** Hash em Envers = risco residual (mitigação: salt HSM, truncamento 8 chars, acesso restrito Auditor/Admin Global); Complexidade service (transação distribuída Usuario + Envers).
- **Riscos:** Vazamento hash + salt = reidentificação (mitigação: salt em Vault, rotação anual, acesso Auditor-only).
- **Compliance:** NFR-C01, NFR-C02, NFR-C04, NFR-C05, BR-16.

---

## ADR-011 — Arquitetura Backend: Modular Monolith (Spring Boot) vs Microserviços

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Arquiteto, DevOps | **Consulted:** Backend, Frontend, DBA, Security

### Contexto
Domínio rico (Ativos, Ordens, Preventiva, Preditiva, Busca, Cadastros, Segurança, Auditoria, Relatórios, LGPD). Time: 2 backend devs. Requisitos: Deploy simples, transações ACID cross-domain, consistência forte, baixa latência inter-serviço, observabilidade unificada.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Modular Monolith (Spring Boot Single Deployable)** (Escolhida) | Um JAR/WAR; Módulos por domínio (`domain/ativo`, `domain/ordem`, `domain/preditiva`, `domain/seguranca`, etc.); Shared kernel (Security, Auditoria, Multi-tenancy); Transações ACID nativas (@Transactional); Deploy único (K8s Deployment); Observabilidade unificada | Simplicidade operacional (1 deploy, 1 log, 1 trace); Transações ACID cross-domain (ex.: Ativo + Ordem + Auditoria na mesma transação); Latência zero inter-módulo (chamada direta); TestContainers único MySQL; Debugging simples; Refatoração segura (compile-time); Evolução para microserviços futura (extração módulo) | Acoplamento compile-time (mitigação: arquitetura modular, boundaries claros, ArchUnit tests); Escala vertical (todo sobe junto); Falha derruba tudo (mitigação: circuit breaker interno, health checks granulares); Time coupling (mitigação: módulos independentes, interfaces estáveis) |
| **B: Microserviços (10+ Services: Ativo, Ordem, Preventiva, Preditiva, Busca, Cadastro, Auth, Auditoria, Relatorio, LGPD)** | Deploy independente; Comunicação async (Kafka) + sync (REST/gRPC); Saga pattern transações distribuídas; Service Mesh (Istio/Linkerd) | Escala independente; Deploy independente; Isolamento falhas; Polyglot; Team autonomy | **Complexidade operacional extrema** (10+ deploys, 10+ DBs, Kafka, Service Mesh, Distributed Tracing, Saga, Eventual Consistency); Latência rede; Transações distribuídas complexas (Saga); Debugging distribuído; Custo infra 5-10x; Overkill para time 2 devs; Data consistency challenges |
| **C: Modulith (Spring Modulith) — Estrutura Modular + Verificação** | Spring Modulith (documentação, verificação, testes, observabilidade por módulo) + Monolith deploy | Estrutura formal módulos; Verificação ArchUnit automática; Documentação viva; Testes por módulo; Observabilidade por módulo; Preparação extração futura | Ainda monolith deploy (mesmos prós/contras A); Adiciona ferramenta (Spring Modulith) — adotar como prática dentro Opção A |

### Decisão
**Opção A** — Modular Monolith Spring Boot 3.3. Módulos por domínio (`domain/*`), Shared Kernel (`security`, `auditoria`, `multi-tenancy`, `config`). ArchUnit tests validam boundaries (nenhum `@Autowired` cross-domain exceto shared kernel). Transações `@Transactional` cross-domain quando necessário (ex.: Ativo + Ordem + Auditoria). Preparação para extração futura: interfaces `*Service` estáveis, eventos de domínio (`ApplicationEventPublisher`) para desacoplamento assíncrono futuro.

### Consequências
- **Positivas:** Velocidade desenvolvimento (time 2 devs), simplicidade deploy/ops, ACID nativo, latência zero, debugging fácil, custo infra 1/10 microserviços.
- **Negativas:** Acoplamento compile-time; Escala vertical; Single point of failure (mitigação: K8s HPA + PDB + Health Checks granulares + Circuit Breaker interno).
- **Evolução:** Módulo `preditiva` candidato a extração v2.0 (ML pipeline independente); `busca` candidato a extração (Elasticsearch cluster).
- **Compliance:** NFR-M01, NFR-M04, NFR-A05, NFR-S02.

---

## ADR-012 — Banco de Dados: MySQL 8.0 vs PostgreSQL / SQL Server / Oracle

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DBA, Backend Lead | **Consulted:** DevOps, Compliance, Cost

### Contexto
Persistência relacional transacional ACID. Requisitos: JSON support, CTEs, Window Functions, Trigram/Full-text Search (Busca Fuzzy), Partitioning (Auditoria), Read Replicas, Cloud Managed (RDS/Cloud SQL), Custo controlado.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: MySQL 8.0 (Cloud Managed: AWS RDS / GCP Cloud SQL / Azure Database)** (Escolhida) | MySQL 8.0: CTEs, Window Functions, JSON, GIS, Roles, Caching (Query Cache removido mas InnoDB buffer pool), `pg_trgm` via plugin (ou `FULLTEXT` ngram), Partitioning nativo, Read Replicas, Clone/Point-in-Time Recovery | Cloud managed maduro (RDS, Cloud SQL, Azure); Custo competitivo (db.r6g.xlarge ≈ $300/mês); Ecossistema vasto; Team knowledge; Flyway/TestContainers suporte nativo; Backup/restore automático; Performance Insights | Trigram requer plugin (não nativo como PostgreSQL `pg_trgm`); `FULLTEXT` ngram limitado; Partitioning menos flexível que PG; `CHECK` constraints só 8.0.16+ |
| **B: PostgreSQL 15+ (Cloud Managed: RDS/Cloud SQL/Cloud SQL for PG)** | PG 15: `pg_trgm` nativo, Partitioning declarativo, JSONB avançado, CTEs recursivas, Window Functions, Parallel Query, Logical Replication, Extensions (PostGIS, TimescaleDB) | `pg_trgm` nativo (busca fuzzy superior); Partitioning mais flexível; JSONB superior; Extensions ecosystem; Padrão cloud-native; Suporte nativo RLS (Row Level Security) | Custo ligeiramente maior (instâncias equivalentes); Team knowledge MySQL > PG; Migração Flyway dialeto (ADR-012 futuro: suporte PG v1.2) |
| **C: SQL Server 2022 (Azure SQL Managed Instance / RDS Custom)** | T-SQL, Columnstore, In-Memory OLTP, Graph, JSON, RLS, Ledger (tamper-evident) | Ledger nativo (auditoria tamper-proof); Columnstore analytics; Integração Azure/AD; Enterprise features | Custo alto (licenciamento + instância); Vendor lock-in Microsoft; Menos cloud-agnostic; Tooling Linux/container menos maduro |
| **D: Oracle 21c (Autonomous Database / RDS Custom)** | Autonomous (auto-tuning, auto-patching); Sharding; JSON; Blockchain Tables; RAC | Enterprise grade; Autonomous ops; Features avançadas | Custo altíssimo; Vendor lock-in extremo; Licenciamento complexo; Overkill |

### Decisão
**Opção A** — MySQL 8.0 Cloud Managed (AWS RDS Multi-AZ / GCP Cloud SQL HA / Azure Database for MySQL Flexible Server). Trigram via plugin `pg_trgm` (compilado) OU `FULLTEXT` ngram para busca fuzzy. Partitioning `REVINFO` (auditoria) por mês. Read Replicas para relatórios/dashboard. Backup automático + Point-in-Time Recovery. Flyway migrations testadas TestContainers MySQL.

### Consequências
- **Positivas:** Custo controlado, managed service maduro, team knowledge, Flyway/TestContainers native, backup/restore automático.
- **Negativas:** Trigram plugin (mitigação: Dockerfile custom MySQL com plugin pré-instalado OU usar `FULLTEXT` ngram + Levenshtein Java para ranking final); RLS nativo limitado (mitigação: Hibernate Filter ADR-004).
- **Futuro:** ADR-012 v1.2 — Suporte PostgreSQL 15+ via Flyway dialect abstraction (testado CI matrix MySQL + PG).
- **Compliance:** NFR-PO03, NFR-CO03, NFR-A03, NFR-A06.

---

## ADR-013 — Autenticação: JWT RS256 Stateless + Refresh Rotation vs Session/Cookie / OAuth2 Server

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, Security Lead, Backend Lead | **Consulted:** Frontend, DevOps, Compliance

### Contexto
Requisitos BR-15, NFR-SEC02, NFR-SEC03: Stateless, escalável, refresh token seguro, rotação de chaves, multi-tenancy aware. Frontend PWA (Service Worker) + Mobile futuro.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: JWT RS256 Stateless + Refresh Token Rotation (HttpOnly Cookie) + JwtTokenProvider Custom** (Escolhida) | Access Token: JWT RS256 (15 min), claims: `sub`, `roles`, `filialId`, `permissions` (resumido), `jti`; Refresh Token: Opaco (UUID), HttpOnly; Secure; SameSite=Strict; 7 dias; Armazenado em BD (`refresh_token` table: hash, usuarioId, filialId, expiração, revogado, userAgent, IP); Rotação: novo refresh token a cada uso (invalida anterior); `JwtTokenProvider` gera/valida; `JwtAuthenticationFilter` valida access token; `MultiTenancyFilter` extrai `filialId` do JWT | Stateless (escala horizontal); RS256 = chave pública para validação (gateway/microserviços futuros); Refresh rotation = segurança (token roubado = uso único); HttpOnly Cookie = XSS protection; `filialId` no JWT = multi-tenancy filter rápido (sem query BD); Rotação chaves (JWKS endpoint) planejada | Complexidade custom (JwtTokenProvider, RefreshTokenRepository, Rotation Logic); Chave privada RS256 proteção (HSM/Vault); Clock skew (mitigação: leeway 30s); Token size (claims resumidos) |
| **B: Session/Cookie Tradicional (Spring Session + Redis)** | `JSESSIONID` cookie; Session data em Redis; `Spring Session` | Simples; Revocação imediata (del Redis); Funciona sem JS | Stateful (sticky sessions OU Redis cluster); Não escala tão bem (session affinity); CSRF protection necessário; Não nativo para PWA/Service Worker (cookie same-site issues); Mobile nativo futuro complexo |
| **C: OAuth2 Authorization Server (Keycloak / Spring Authorization Server / Auth0)** | External IdP; OIDC; Tokens padrão; MFA, Social, User Federation | Enterprise features; Standards-based; Offload auth complexity | **Complexidade operacional** (Keycloak cluster, DB, themes); Latência rede (introspection/token endpoint); Vendor lock-in (Keycloak) ou custo (Auth0); Overkill para app interno único; Multi-tenancy realms complexo |
| **D: PASETO (Platform-Agnostic Security Tokens) vs JWT** | PASETO v4 (local/public); Sem algorithm confusion; Versionado | Segurança por design (sem alg confusion); Versionado; Libraries Java/JS | Ecossistema menor; Menos tooling; JWT RS256 + boas práticas (jti, exp, aud, iss, leeway) = seguro o suficiente; Migração futura se padrão emergir |

### Decisão
**Opção A** — `JwtTokenProvider` custom (RS256, chave privada PEM em Vault/SealedSecret, chave pública exposta em `/api/v1/.well-known/jwks.json` para validação gateway/microserviços futuros). Refresh Token: tabela `refresh_token` (hash SHA-256, usuarioId, filialId, expiração, revogado, userAgent, IP, criadoEm). Rotação: cada `/auth/refresh` invalida refresh token usado + emite novo par (access + refresh). Frontend: `authInterceptor` (Pinia) gerencia access token memória + retry 1x automático.

### Consequências
- **Positivas:** Stateless, escalável, seguro (RS256 + rotation + HttpOnly), multi-tenancy via claim `filialId`, JWKS ready para microserviços futuros, controle total.
- **Negativas:** Código custom para manter; Chave privada proteção crítica (Vault/HSM); Token size (claims resumidos — permissions expandidas via `AegisShieldPermissionEvaluator` consultando BD).
- **Riscos:** Clock skew (mitigação: leeway 30s + NTP sync); Refresh token replay (mitigação: rotation + hash armazenado + revogação imediata).
- **Compliance:** NFR-SEC02, NFR-SEC03, NFR-SEC04, BR-15, NFR-C01 (revogação tokens no esquecimento).

---

## ADR-014 — Observabilidade: Prometheus/Grafana/Tempo/Loki vs Datadog/New Relic/Elastic APM

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DevOps, SRE | **Consulted:** Backend, Frontend, Security, Cost

### Contexto
Requisitos NFR-O01 a O05: Métricas Golden Signals + Business KPIs, Alertas, Tracing distribuído, Logs estruturados JSON, Frontend Core Web Vitals. Custo controlado (NFR-CO). OpenTelemetry native.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: Stack Open Source (Prometheus + Grafana + Tempo + Loki + Alertmanager + Sentry Frontend)** (Escolhida) | Prometheus (metrics pull), Grafana (dashboards), Tempo (traces), Loki (logs), Alertmanager (alerts), Sentry (frontend errors + RUM); OpenTelemetry SDK (Java + JS); Self-hosted K8s (Operators: Prometheus Operator, Grafana Operator, Tempo Operator, Loki Stack) | **Custo zero licença** (infra only); Controle total dados (LGPD/GDPR); OpenTelemetry native (vendor-neutral); Integração nativa Spring Boot 3 (Micrometer + OTel); K8s Operators = GitOps; Custom dashboards/alerts ilimitados; Retenção configurável; Sem vendor lock-in | Operação self-hosted (mitigação: Operators + GitOps + Runbooks); Expertise SRE necessário; Escala (Tempo/Loki object storage S3/GCS); Sentry self-hosted ou SaaS (gratuito até 5k events/mês) |
| **B: Datadog / New Relic / Elastic APM (SaaS)** | Agent-based (DD/NR) ou Elastic Agent; Unified platform; ML-based alerts; Synthetics; RUM nativo | Setup rápido; ML/Anomaly detection; Synthetics; RUM avançado; Support enterprise; Menos ops | **Custo alto** (Datadog ~$31/host/mês + APM + Logs + RUM = $500-2000+/mês para 3 nodes); Vendor lock-in; Dados saem do perímetro (LGPD); Customização limitada; Fatura imprevisível (cardinality) |
| **C: Elastic Stack (ELK) Self-Hosted** | Elasticsearch + Logstash/Fluent Bit + Kibana + APM Server + Fleet | Unified (logs, metrics, APM, SIEM); Open Source (Basic license); Kibana dashboards; ML (platinum) | **Recursos pesados** (ES heap, storage, 3+ nodes); Complexidade operacional alta; Licença SSPL/Elastic v2 (restrições); APM sampling limitado basic; Upgrade para features enterprise |
| **D: Híbrido: Prometheus/Grafana (Metrics) + Loki (Logs) + Jaeger (Traces) + Sentry (Frontend)** | Best-of-breed open source; Jaeger (traces) mais maduro que Tempo | Jaeger > Tempo (ecossistema, sampling adaptativo); Loki leve (index-free); Prometheus/Grafana padrão | 3 sistemas traces/logs/metrics (vs 2: Tempo+Loki); Operação múltipla; Jaeger storage (Cassandra/ES) mais pesado |

### Decisão
**Opção A** — Stack OSS unificado (Prometheus + Grafana + Tempo + Loki + Alertmanager) via Kubernetes Operators (Prometheus Operator, Grafana Operator, Tempo Operator, Loki Operator). OpenTelemetry SDK Java (Spring Boot 3.3 `spring-boot-starter-otel`) + JS (`@opentelemetry/sdk-trace-web`, `@opentelemetry/exporter-collector`). Sentry SaaS (Free tier) para Frontend Errors + RUM (Core Web Vitals). Dashboards provisionados GitOps (ConfigMaps). Alertas: Golden Signals + Business KPIs + Security (cross-tenant leak, preditiva threshold, SLA breach).

### Consequências
- **Positivas:** Custo zero licença, controle total dados (LGPD), OpenTelemetry vendor-neutral, GitOps, dashboards/alertas ilimitados, integração nativa Spring Boot 3.
- **Negativas:** Operação self-hosted (mitigação: Operators + Runbooks + SRE on-call); Object storage para Tempo/Loki (S3/GCS); Expertise necessário.
- **Riscos:** Cardinality explosion (mitigação: label naming conventions, relabeling, drop high-cardinality labels).
- **Compliance:** NFR-O01 a O05, NFR-CO01, NFR-C01 (dados no perímetro), NFR-C04.

---

## ADR-015 — CI/CD: GitHub Actions + K8s (Helm/Kustomize) vs GitLab CI / Jenkins / ArgoCD

**Data:** 2025-01-10 | **Status:** Accepted | **Deciders:** Tech Lead, DevOps, Backend Lead | **Consulted:** Security, QA, Frontend

### Contexto
Pipeline: Build → Test (Unit + Integration TestContainers) → Security Scan (OWASP Dep Check, SpotBugs, Semgrep, CodeQL) → Contract Test (Pact/Spring Cloud Contract) → Build Images (Multi-stage Docker, Distroless, SBOM Syft, Sign Cosign) → Deploy Staging (K8s) → Smoke Tests → Deploy Prod (Blue/Green ou Canary) → Rollback Automático (Health Check Fail). GitOps para manifests.

### Opções Consideradas
| Opção | Descrição | Prós | Contras |
| :--- | :--- | :--- | :--- |
| **A: GitHub Actions + Kustomize/Helm + ArgoCD (GitOps) OU Flux** (Escolhida) | GH Actions: Matrix builds (Java 21, Node 20), TestContainers (Docker-in-Docker ou Host Docker), Security Scans (OWASP Dep Check Action, SpotBugs, Semgrep, CodeQL), Contract Tests, Docker Buildx (multi-arch), Syft SBOM, Cosign Sign, Trivy Scan; Deploy: `kubectl apply -k` (Kustomize) OU `helm upgrade`; GitOps: ArgoCD ou Flux (sync manifests from Git) | Nativo GitHub (repo já no GH); Actions marketplace rico; Matrix builds; TestContainers suporte (Docker socket ou `docker` container); Security scans integrados; Cosign/Syft/Trivy actions oficiais; Kustomize/Helm nativos kubectl; ArgoCD/Flux = GitOps declarativo; Custo zero (GH Actions minutes incluídos) | GH Actions minutes limit (mitigação: self-hosted runners para TestContainers pesados); ArgoCD/Flux = componente extra para operar |
| **B: GitLab CI + GitLab Registry + GitLab Kubernetes Agent** | `.gitlab-ci.yml`; Auto DevOps; Integrated Container Registry; K8s Agent (GitOps nativo) | Tudo integrado (CI, Registry, K8s, Security, Pages); Auto DevOps templates; GitOps nativo (Agent) | Requer GitLab (self-hosted ou SaaS); Migração repo; Runner config; Menos marketplace actions vs GH |
| **C: Jenkins (Self-Hosted) + Pipeline DSL + ArgoCD** | Jenkinsfile; Plugins ecosistema; Shared Libraries; Agents K8s (Jenkins Operator) | Flexibilidade total; Plugins vastos; Shared Libraries reutilizáveis; Self-hosted control | **Manutenção Jenkins** (upgrades, plugins, agents, security, groovy); Infra dedicada; Curva aprendizado; Não nativo GitHub |
| **D: CircleCI / Travis / Bitrise / Semaphore** | SaaS CI/CD | Setup rápido; Mac/iOS support (Bitrise/CircleCI) | Custo; Vendor lock-in; Menos controle; TestContainers Docker-in-Docker complexo em SaaS |

### Decisão
**Opção A** — GitHub Actions (workflows: `ci.yml`, `cd-staging.yml`, `cd-prod.yml`, `security.yml`, `dependency-update.yml`). TestContainers: self-hosted runners (AWS EC2/GCP VM) com Docker socket para performance. Images: Multi-stage Dockerfile (builder → runtime distroless `gcr.io/distroless/java21-debian12`), SBOM (Syft), Sign (Cosign keyless), Scan (Trivy). Deploy: Kustomize (overlays: staging, prod) + ArgoCD (GitOps) — `argocd app sync` via GH Action. Rollback: `argocd app rollback` ou `kubectl rollout undo`. Smoke Tests: `curl /actuator/health` + E2E crítico (Cypress headless).

### Consequências
- **Positivas:** Nativo GitHub, custo zero (minutes incluídos), marketplace rico, TestContainers em self-hosted runners, Cosign/Syft/Trivy oficiais, Kustomize/ArgoCD GitOps, rollback rápido.
- **Negativas:** Self-hosted runners manutenção (mitigação: ASG + user-data bootstrap); ArgoCD operação (mitigação: GitOps = manifests em Git, ArcoCD só sincroniza).
- **Riscos:** GH Actions minutes limit em builds pesados (mitigação: self-hosted runners dedicados para TestContainers).
- **Compliance:** NFR-M02, NFR-M03, NFR-M04, NFR-SEC10, NFR-A02, NFR-A06.

---

## Rastreabilidade ADR ↔ Requisitos ↔ Artefatos

| ADR | BRD Rules | NFR | System Arch | Use Cases | Code Location |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ADR-001 | BR-11, BR-12 | NFR-P02, S03, SEC04 | §9.3 | UC-29 a UC-31 | `FuzzySearchService`, `LevenshteinDistance`, `BuscaController` |
| ADR-002 | BR-10 | NFR-P03, C04 | §9.2 | UC-25 a UC-28 | `ManutencaoPreditivaService`, `HealthCheckService`, `PreditivaScheduler` |
| ADR-003 | BR-13, BR-14 | NFR-SEC03, SEC04, C04, C05 | §7.2 | UC-32 a UC-38 | `AegisShieldPermissionEvaluator`, `RolePermissionContext`, `AdminController` |
| ADR-004 | BR-13 | NFR-SEC04, S03, C01, C04 | §7.3 | UC-35, UC-36 | `MultiTenancyFilter`, `TenantContextHolder`, `@FilterDef` entidades |
| ADR-005 | BR-16 | NFR-C02, C04, C05 | §6, §7.3 | UC-36, UC-43 a UC-45 | `@Audited` entidades, `CustomRevisionListener`, `AuditoriaController` |
| ADR-006 | BR-17, BR-18 | NFR-P04, C03 | §9.1 | UC-39, UC-40 | `PdfGenerator`, `QRCodeGenerator`, `RelatorioController` |
| ADR-007 | — | NFR-P06, P07, P08, U01, U02, U03, PO01, PO02 | §4 (Frontend) | Todos UC Frontend | `frontend/` (Vue 3, Vite, Pinia, Bootstrap 5, PWA Plugin) |
| ADR-008 | — | NFR-M02, M04, A06, SEC04 | §5 (Quality) | Todos UC Backend | `*ControllerIT`, `*ServiceIT`, `*RepositoryIT`, `.testcontainers.properties` |
| ADR-009 | — | NFR-U02, PO01 | §4 (PWA) | UC-25, US-PRD-002 | `frontend/vite.config.ts` (PWA Plugin), `frontend/src/sw/`, `idb` lib |
| ADR-010 | — | NFR-C01, C02, C04, C05 | §7.3 (LGPD) | UC-37, UC-43 a UC-45 | `LgpdService`, `LgpdController`, `CustomRevisionListener` |
| ADR-011 | — | NFR-M01, M04, A05, S02 | §3, §5 | Todos UC Backend | `domain/*` modules, `shared-kernel/`, ArchUnit tests |
| ADR-012 | — | NFR-PO03, CO03, A03, A06 | §4 (Data Layer) | Todos UC Backend | `Flyway` migrations, `application.yml`, `Dockerfile` MySQL |
| ADR-013 | BR-15 | NFR-SEC02, SEC03, SEC04, C01 | §7.1 | UC-32 | `JwtTokenProvider`, `JwtAuthenticationFilter`, `RefreshTokenRepository` |
| ADR-014 | — | NFR-O01 a O05, CO01, C01, C04 | §11 | Transversal | `Micrometer`, `OpenTelemetry`, `Prometheus Operator`, `Grafana Operator`, `Sentry` |
| ADR-015 | — | NFR-M02, M03, M04, SEC10, A02, A06 | §10, §12 | Transversal | `.github/workflows/*.yml`, `k8s/`, `Dockerfile`, `pom.xml`, `frontend/vite.config.ts` |

---

*Documento regenerado completamente com 15 ADRs baseadas em análise AST completa do backend Java + frontend Vue/PWA + infra. Substitui versão 1.0 que continha apenas ADR-001 (frontend vanilla JS vs framework).*