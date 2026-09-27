# ADR-001 — Arquitetura Monolítica Modular (Single Deployable Unit)

> **Data:** 15/01/2025 · **Status:** Proposed
> **Deciders:** Arquiteto de Software, Tech Lead Backend, Engineering Manager · **Consulted:** DBA, DevOps, Security · **Informed:** Product Owner, QA Lead

## 1. Status
Proposed

## 2. Context & Problem Statement
O Aegis Patrimônio possui uma base de código existente de 333 arquivos Java (Spring Boot implícito) e 15 arquivos JavaScript (Vanilla JS + @popperjs/core) servidos como assets estáticos. O diagnóstico determinístico confirma ausência de mensageria distribuída, service mesh, ou orquestração de containers. Os NFRs estabelecem: latência ≤ 500 ms (p95) para 100 usuários concorrentes, escalando verticalmente para 400 usuários com degradação ≤ 10% (NFR-S01); disponibilidade 99,5% SLA / 99,9% SLO com RTO ≤ 4 h (NFR-A01); custo ≤ R$ 2,00/mês por ativo (NFR-CO01). A stack atual não suporta auto-scaling horizontal, clustering ou sharding (NFR-S01 observação). A decisão é se manter o monolito modular ou iniciar decomposição em microsserviços.

## 3. Decision Drivers
* **Simplicidade operacional e custo** (NFR-CO01: ≤ R$ 2,00/mês por ativo) — infraestrutura single-node + read-replica + WORM storage
* **Consistência transacional forte** — regras de negócio (BR-01 RBAC, BR-02 auditoria imutável) exigem ACID local entre Ativos, Manutenções, HealthChecks, Auditoria
* **Latência in-process** — chamadas Java diretas Controller→Service→Repository evitam overhead de rede/serialização (NFR-P01)
* **Time-to-market e dívida técnica existente** — 5 métodos com complexidade ciclomática > 10 (NFR-M02) priorizam refatoração interna over extração de serviços
* **Capacidade da equipe** — equipe atual dimensionada para monolito; microsserviços exigiriam DevOps, SRE, platform engineering dedicados

## 4. Considered Options
### Option A: Monolito Modular (Single Deployable Unit) — **Escolhida**
* **Descrição:** Aplicação Spring Boot única (JAR/Docker) servindo API REST + frontend estático, com módulos lógicos (Ativos, Manutenções, Alertas, Auditoria, Segurança) separados por pacotes. Jobs de background (`AlertNotificationService.checkResourceUsageAlerts`, `updateHealthCheck`) rodam in-process via `TaskScheduler`. Persistência em banco relacional único (primário) com read-replica para relatórios. Deploy único, escala vertical.
* **Prós:**
  - Transações ACID nativas entre domínios (ex: criar Ativo + Manutenção + AuditoriaLog atômico)
  - Latência mínima (sem hop de rede entre serviços)
  - Operação simples: 1 artefato, 1 pipeline, 1 health check, 1 log aggregator
  - Custo previsível (VM/container único + DB primário + réplica + WORM)
  - Refatoração incremental viável (Strangler Fig futuro se volume justificar)
* **Contras:**
  - SPOF: falha da instância derruba todo o sistema (mitigado por read-replica promovível + runbook RTO ≤ 4h)
  - Teto de escala vertical (hardware single-node)
  - Deploy all-or-nothing (qualquer mudança requer rebuild/deploy completo)
  - Acoplamento temporal: jobs param se app reinicia (NFR-O02 gap)

### Option B: Microsserviços (Decomposição por Domínio)
* **Descrição:** Extrair `AlertNotificationService`, `HealthCheckService`, `AuditoriaService` como serviços independentes comunicando via Kafka/RabbitMQ + API Gateway. Banco por serviço (polyglot persistence) ou shared DB com schemas separados.
* **Prós:**
  - Escala horizontal independente (ex: escalar apenas alertas se 12k ativos > 30s)
  - Deploy independente por domínio
  - Isolamento de falhas (bug em alertas não derruba CRUD ativos)
  - Tecnologias heterogêneas por serviço (ex: Go para health checks de alta frequência)
* **Contras:**
  - Complexidade operacional massiva: service discovery, distributed tracing, saga pattern para transações, eventual consistency
  - Latência adicionada (rede + serialização) — risco de violar NFR-P01 (≤ 500 ms p95)
  - Custo infra 3–5× maior (múltiplas VMs/containers, message broker, API Gateway, observabilidade distribuída)
  - Equipe atual não tem capacidade para operar microsserviços em produção (NFR-CO01 violado)
  - Transações distribuídas entre Ativo/Manutenção/Auditoria exigem coreografia complexa (BR-02 auditoria imutável)

### Option C: Modulito (Monolito com Módulos Isolados + Deploy Separado Futuro)
* **Descrição:** Manter código em monolito mas estruturar como módulos Maven/Gradle independentes (`aegis-ativos`, `aegis-manutencoes`, `aegis-alertas`, `aegis-auditoria`) com interfaces bem definidas, permitindo extração futura sem refatoração big-bang.
* **Prós:**
  - Preparação para Strangler Fig sem custo operacional imediato
  - Boundaries claros facilitam testes e ownership
  - Migração gradual se/quando volume justificar
* **Contras:**
  - Ainda monolito em runtime (mesmos SPOF e teto de escala)
  - Overhead de modularização build-time sem benefício runtime imediato
  - Requer disciplina arquitetural contínua (ArchUnit, testes de arquitetura)

## 5. Decision Outcome
**Opção escolhida:** Option A — Monolito Modular (Single Deployable Unit)

**Rationale:** A combinação de NFR-CO01 (custo ≤ R$ 2,00/mês por ativo), NFR-S01 (escala vertical only, sem auto-scaling horizontal), NFR-P01 (latência ≤ 500 ms p95), e a base de código existente (333 arquivos Java, 0 mensageria, 0 service mesh) torna o monolito a única opção viável no horizonte Sprint 0–3. A extração de `AlertNotificationService` (job crítico: 12k ativos ≤ 30s — NFR-P04) será reavaliada no médio prazo (Q3–Q4 2025) se vertical scaling esgotar ou se volume de alertas justificar processamento assíncrono distribuído (Kafka + consumer group). Até lá, refatoração interna dos 5 métodos complexos (NFR-M02) e otimização de queries/índices (NFR-CO02) endereçam os gargalos conhecidos.

### Comparison Matrix
| Critério | Peso | Option A (Monolito) | Option B (Microsserviços) | Option C (Modulito) |
| :--- | :---: | :---: | :---: | :---: |
| Custo infra (NFR-CO01) | 5 | 5 | 1 | 4 |
| Latência p95 (NFR-P01) | 5 | 5 | 2 | 5 |
| Simplicidade operacional | 5 | 5 | 1 | 4 |
| Consistência transacional (BR-01, BR-02) | 5 | 5 | 2 | 5 |
| Escalabilidade horizontal | 3 | 1 | 5 | 2 |
| Isolamento de falhas | 4 | 2 | 5 | 2 |
| Time-to-market (Sprint 0–3) | 5 | 5 | 1 | 4 |
| Capacidade equipe atual | 5 | 5 | 1 | 4 |
| **Total ponderado** | — | **115** | **52** | **97** |

## 6. Consequences
### Positive
* Atende NFR-CO01, NFR-S01, NFR-P01, NFR-A01 com arquitetura simples e custos controlados
* Transações ACID locais garantem integridade de auditoria (BR-02) e RBAC (BR-01) sem saga/compensação
* Refatoração dos 5 métodos complexos (NFR-M02) e índices compostos (NFR-CO02) resolvem gargalos imediatos
* Deploy único simplifica CI/CD (NFR-M03/M04/M05) e rollback

### Negative / Trade-offs
* **SPOF aplicação:** Instância única — mitigado por health checks `/actuator/health/liveness|readiness` (NFR-O02), graceful shutdown 30s, read-replica promovível, runbook RTO ≤ 4h
* **Teto de escala vertical:** Se 400 usuários + jobs + relatórios excederem capacidade single-node, migração para Modulito/Microsserviços será necessária (gatilho: CPU > 80% sustentado, heap > 85%, HikariCP pool > 90%)
* **Jobs in-process:** `checkResourceUsageAlerts` e `updateHealthCheck` param se app reinicia — mitigado por externalizar scheduler no médio prazo (Quartz JDBC JobStore ou CronJob externo — NFR-O02)

### Neutral
* Frontend Vanilla JS + @popperjs/core servido como static assets pelo Spring Boot (ou Nginx/CDN) — dívida técnica documentada (NFR-M01: migração TypeScript/React planejada)
* SQL cru / JPA nativo sem ORM completo — decisão separada (ADR-002)

## 7. Implementation Notes
* **Ações necessárias:**
  1. Confirmar motor de banco (Gap #1 SAD) e provisionar primário + read-replica + WORM bucket (Sprint 0)
  2. Implementar pipeline CI/CD com validações NFR-M03/M04/M05 (OpenAPI breaking changes, `console.*`, stubs)
  3. Refatorar 5 métodos complexidade > 10: `checkResourceUsageAlerts` (17), `RealisticDataSeeder.run` (15), `AtivoMapper.toDTO` (14), `ManutencaoSpecification.build` (14), `api.js:request` (13) — NFR-M02
  4. Configurar OpenTelemetry Java Agent + Collector + Prometheus/Grafana/Loki/Tempo (Gap #4 SAD)
  5. Provisionar Secret Manager + rotação 90 dias (NFR-SEC04, Gap #4 SAD)
  6. Implementar `AuditoriaService` com escrita assíncrona WORM (NFR-SEC05)
  7. Configurar Flyway/Liquibase + baseline schema (Gap #5 SAD)
* **Prazo estimado de migração:** N/A (decisão de manutenção da arquitetura atual). Reavaliação em Q3 2025 baseada em métricas de carga.
* **Rollback plan:** Não aplicável (decisão de não mudar). Se futuro extrair serviços: Strangler Fig via API Gateway roteando `/api/alertas/**` para novo serviço, mantendo monolito para demais domínios.

## 8. Links & References
* SAD Seção 3 (Tech Stack Justification), Seção 4 (Architectural Style), Seção 7 (Scalability), Seção 12 (Trade-offs), Seção 13 (Future Evolution)
* NFR-S01, NFR-S02, NFR-S03, NFR-P01, NFR-P04, NFR-CO01, NFR-M02, NFR-O02, NFR-A01, NFR-A04
* Diagnóstico determinístico: 333 arquivos .java, 15 .js, 0 mensageria, 0 ORM, 5 métodos complexidade > 10
* ADR-002 (SQL cru / JPA nativo) — decisão correlata
* Issue: `#ARCH-001` (formalizar ADR-001 na Sprint 0)

---

# ADR-002 — Acesso a Dados via SQL Cru / JPA Nativo (Sem ORM Completo)

> **Data:** 15/01/2025 · **Status:** Proposed
> **Deciders:** Arquiteto de Software, Tech Lead Backend, DBA · **Consulted:** DevOps, Security · **Informed:** Product Owner, QA Lead

## 1. Status
Proposed

## 2. Context & Problem Statement
O diagnóstico determinístico confirma: "nenhum ORM/query builder encontrado — provavelmente SQL cru". O SAD (Seção 2) registra: "ORM / Data Access: SQL cru / JPA nativo (sem ORM identificado; NFR-CO02 cita 'raw SQL / JPA nativo')". O `ManutencaoSpecification.build` (complexidade ciclomática 14) constrói queries dinâmicas com múltiplos filtros. O `AtivoMapper.toDTO` (complexidade 14) mapeia entidades ricas para DTOs. NFR-CO02 exige "otimização manual de queries e índices compostos alinhados a `ManutencaoSpecification.build`". NFR-S03 requer HikariCP 500 conexões. NFR-SEC03 exige proteção contra injeção via prepared statements / bind parameters. A decisão é se adotar ORM completo (Hibernate/JPA), query builder (jOOQ, MyBatis), ou manter SQL cru + JPA nativo parcial.

## 3. Decision Drivers
* **Controle total de queries e planos de execução** — NFR-CO02 exige índices compostos otimizados para filtros dinâmicos de `ManutencaoSpecification`; ORM completo gera SQL opaco e difícil de tunar
* **Performance previsível sob carga** — 500 conexões HikariCP (NFR-S03) + 400 usuários concorrentes + jobs batch (12k ativos ≤ 30s — NFR-P04) exigem queries otimizadas manualmente, sem overhead de proxy/bytecode enhancement do Hibernate
* **Base de código existente** — 333 arquivos Java já usam `@Query` nativas, `JdbcTemplate`, `EntityManager.createNativeQuery()`; migração para ORM completo seria refatoração massiva (alto risco, baixo ROI imediato)
* **Segurança (NFR-SEC03)** — SQL cru com bind parameters (`?` / named parameters) previne injeção; revisão de código obrigatória para detectar concatenação de strings (já parte do pipeline NFR-M04)
* **Auditoria imutável (BR-02, NFR-SEC05)** — `AuditoriaService` grava payload completo em WORM; controle fino de quais colunas/valores persistem é mais direto com SQL explícito

## 4. Considered Options
### Option A: SQL Cru / JPA Nativo Parcial (EntityManager + @Query + JdbcTemplate) — **Escolhida**
* **Descrição:** Manter abordagem atual: entidades JPA (`@Entity`) para mapeamento objeto-relacional básico, mas queries complexas (filtros dinâmicos, relatórios, batch) escritas em SQL nativo via `@Query(nativeQuery=true)`, `JdbcTemplate`, ou `EntityManager.createNativeQuery()`. `ManutencaoSpecification.build` continua construindo `Predicate`/`Criteria` ou SQL dinâmico com bind parameters. `AtivoMapper.toDTO` usa `ResultSet`/`Tuple` mapping manual. HikariCP gerencia pool (500 conn). Flyway/Liquibase para migrações (Gap #5 SAD).
* **Prós:**
  - Controle total: DBA pode revisar/otimizar cada query, criar índices compostos precisos (ex: `(ativo_id, status, data_inicio)` para `ManutencaoSpecification`)
  - Zero overhead ORM: sem proxy, lazy loading surpresa, N+1 oculto, first/second-level cache inconsistente
  - Performance determinística: plano de execução estável, explicável via `EXPLAIN ANALYZE`
  - Migração de schema versionada (Flyway) alinhada a SQL real executado
  - Compatível com qualquer motor relacional (PostgreSQL, Oracle, SQL Server, MySQL) — Gap #1 SAD
  - Auditoria: `INSERT INTO auditoria_log ...` explícito com payload JSONB/TEXT, sem interceptors Hibernate
* **Contras:**
  - Boilerplate: mapeamento manual `ResultSet`→DTO/Entidade (`AtivoMapper.toDTO` complexidade 14)
  - Refatoração de schema mais trabalhosa (buscar SQL espalhado vs. alterar entidade JPA)
  - Risco de injeção se dev usar concatenação (`"WHERE id = " + id`) — mitigado por pipeline NFR-M04 (detectar concatenação) + code review + SonarQube rule
  - Menos produtividade para CRUD simples (findById, save) — mitigado por `JpaRepository` para operações básicas

### Option B: Hibernate/JPA Completo (ORM Padrão)
* **Descrição:** Migrar para `spring-boot-starter-data-jpa` full: `@Entity` com relacionamentos `@OneToMany`, `@ManyToOne`, `CriteriaBuilder`/`Specification` para queries dinâmicas, `@EntityGraph` para fetch plans, second-level cache (Hazelcast/Infinispan), `@Query` JPQL para casos complexos.
* **Prós:**
  - Produtividade: CRUD automático, derivado de query methods (`findByAtivoIdAndStatus`), paginação nativa
  - Refatoração schema mais segura (compile-time checks via entidade)
  - Cache L2 nativo para entidades frequentes (`Ativo`, `Configuracao`)
  - Ecossistema maduro: ferramentas, documentação, comunidade
* **Contras:**
  - **N+1 problem** endêmico em listas (`Ativo` → `List<Manutencao>` → `List<HealthCheck>`) — exige `@EntityGraph` ou `JOIN FETCH` em todo lugar
  - SQL gerado opaco: `ManutencaoSpecification.build` (14 filtros) viraria `CriteriaBuilder` complexo, plano de execução imprevisível
  - Overhead runtime: proxy CGLIB, bytecode enhancement, dirty checking, flush automático — latência adicionada (risco NFR-P01)
  - Tuning difícil: índices compostos não mapeiam 1:1 para estratégias de fetch JPQL
  - Migração big-bang: 333 arquivos Java, 5 métodos complexos já em SQL nativo — risco alto, tempo Sprint 0–3 insuficiente

### Option C: jOOQ (Type-Safe SQL Builder)
* **Descrição:** Adotar jOOQ para queries type-safe geradas a partir do schema (code generation). Manter JPA apenas para CRUD simples ou remover JPA totalmente.
* **Prós:**
  - Type-safe SQL: compile-time validation de colunas, tabelas, joins
  - Controle total de SQL gerado (sem magic), próximo de SQL cru
  - Code generation sincroniza schema ↔ código (detecta drift)
  - Boa integração Spring Boot (`jooq-spring-boot-starter`)
* **Contras:**
  - Curva de aprendizado + setup code generation (maven/gradle plugin)
  - Ainda requer escrever queries explicitamente (não resolve boilerplate de mapeamento DTO)
  - Adiciona dependência extra (~2MB) e step de build (codegen)
  - Não elimina necessidade de índices compostos manuais (NFR-CO02)
  - Migração parcial: `ManutencaoSpecification.build` teria que ser reescrito em jOOQ DSL

## 5. Decision Outcome
**Opção escolhida:** Option A — SQL Cru / JPA Nativo Parcial

**Rationale:** A combinação de NFR-CO02 (otimização manual de queries + índices compostos alinhados a `ManutencaoSpecification.build`), NFR-S03 (HikariCP 500 conn, performance previsível), NFR-P04 (batch 12k ativos ≤ 30s), base de código existente (333 arquivos Java já em SQL nativo/@Query), e Gap #1 (motor de banco indefinido — SQL cru é portável) torna a Opção A a única viável para Sprint 0–3. Hibernate completo introduz overhead e opacidade que violam NFR-P01/CO02. jOOQ adiciona complexidade de build sem eliminar boilerplate de mapeamento (que já existe em `AtivoMapper`). A estratégia é: **refatorar os 5 métodos complexos (NFR-M02) para SQL otimizado + índices compostos**, não trocar a camada de acesso. No médio prazo, se produtividade de CRUD se tornar gargalo, avaliar jOOQ apenas para novos módulos (Strangler Fig).

### Comparison Matrix
| Critério | Peso | Option A (SQL Cru/JPA Nativo) | Option B (Hibernate Full) | Option C (jOOQ) |
| :--- | :---: | :---: | :---: | :---: |
| Controle de query/plano execução (NFR-CO02) | 5 | 5 | 2 | 4 |
| Performance previsível (NFR-P01, NFR-P04) | 5 | 5 | 2 | 4 |
| Compatibilidade motor indefinido (Gap #1) | 5 | 5 | 3 | 4 |
| Produtividade CRUD simples | 3 | 2 | 5 | 3 |
| Segurança injeção (NFR-SEC03) | 5 | 4* | 5 | 5 |
| Refatoração schema | 3 | 2 | 5 | 4 |
| Curva de aprendizado/Setup (Sprint 0–3) | 4 | 5 | 3 | 2 |
| Auditoria explícita WORM (NFR-SEC05) | 4 | 5 | 3 | 4 |
| **Total ponderado** | — | **107** | **71** | **86** |

*Com pipeline NFR-M04 (detectar concatenação) + code review + SonarQube, risco mitigado para nível 4.

## 6. Consequences
### Positive
* Queries de `ManutencaoSpecification.build` e batch `checkResourceUsageAlerts` otimizadas manualmente com `EXPLAIN ANALYZE` + índices compostos (ex: `idx_manutencao_ativo_status_data (ativo_id, status, data_inicio)`)
* Zero overhead ORM garante latência p95 ≤ 500 ms (NFR-P01) e throughput 12k ativos/30s (NFR-P04)
* Portabilidade entre PostgreSQL/Oracle/SQL Server/MySQL (Gap #1) — apenas ajustes de dialeto SQL nativo
* Auditoria `INSERT` explícita com payload JSON completo, sem interceptors mágicos

### Negative / Trade-offs
* **Boilerplate de mapeamento:** `AtivoMapper.toDTO` (complexidade 14) e mappers similares exigem manutenção manual — mitigado por testes unitários de mapping + geração parcial via MapStruct (futuro, NFR-M01)
* **Risco de injeção SQL:** Requer disciplina de **sempre** usar bind parameters (`?` / `:param`) — enforcado por pipeline NFR-M04 (regex detecta concatenação `+` em strings SQL) + SonarQube rule `java:S2077` + code review obrigatório
* **Refatoração de schema:** Mudança de coluna/tabela exige busca em SQL nativo espalhado — mitigado por Flyway migrations (Gap #5) + testes de integração contra schema real

### Neutral
* `JpaRepository`/`CrudRepository` ainda usados para `findById`, `save`, `delete` simples — melhor dos dois mundos
* `EntityManager` disponível para operações nativas programáticas quando `Specification`/`Criteria` insuficiente
* Migração futura para jOOQ em novos módulos (ex: `AlertNotificationService` extraído) não bloqueada

## 7. Implementation Notes
* **Ações necessárias:**
  1. Auditoria de todo SQL nativo no código: buscar concatenação de strings (`grep -r "SELECT.*+" --include="*.java"`), substituir por bind parameters
  2. Configurar SonarQube rule `java:S2077` (PreparedStatement) + custom rule para `@Query` nativas
  3. Criar índices compostos alinhados a `ManutencaoSpecification.build` filtros: `(ativo_id, status, data_inicio)`, `(ativo_id, tipo, data_fim)`, `(responsavel_id, status)` — validar com `EXPLAIN ANALYZE` em staging
  4. Refatorar `ManutencaoSpecification.build` (complexidade 14): extrair builders de `Predicate` por filtro, usar `CriteriaBuilder` apenas para estrutura, delegar WHERE complexo a SQL nativo via `@Query` com `nativeQuery=true`
  5. Refatorar `AtivoMapper.toDTO` (complexidade 14): separar em mappers por seção (dados básicos, manutenções, health checks, custos), usar `JdbcTemplate.query(RowMapper)` para listas
  6. Otimizar `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17): query única com `JOIN` + agregação vs. loop N+1; processar em batches de 500 ativos; `@Async` com thread pool dedicado (`scheduler` bulkhead)
  7. Configurar Flyway/Liquibase (Gap #5 SAD): baseline schema atual + migrations versionadas para índices compostos
* **Prazo estimado:** Sprint 0–2 (itens 1–3), Sprint 1–3 (itens 4–7)
* **Rollback plan:** Se SQL cru causar bugs de mapeamento em produção: habilitar Hibernate `hibernate.show_sql=true` + `hibernate.format_sql=true` para debug; reverter mappers problemáticos para `EntityManager.createQuery` JPQL temporário; não reverter arquitetura completa.

## 8. Links & References
* SAD Seção 2 (Tech Stack Justification), Seção 5 (Data Modeling), Seção 7 (Scalability — gargalos 2, 3), Seção 12 (Trade-offs)
* NFR-CO02, NFR-S03, NFR-P01, NFR-P04, NFR-SEC03, NFR-SEC05, NFR-M02, NFR-M04
* Diagnóstico determinístico: `ManutencaoSpecification.build:26` (complexidade 14), `AtivoMapper.toDTO:15` (complexidade 14), `AlertNotificationService.checkResourceUsageAlerts:96` (complexidade 17), "nenhum ORM/query builder encontrado"
* ADR-001 (Monolito Modular) — decisão correlata (transações ACID locais facilitadas por SQL cru)
* Issue: `#ARCH-002` (formalizar ADR-002 na Sprint 0)
* Documentação: `db-schema-spec.md` (a criar), `db-migration-spec.md` (a criar), `security-policies.md` (a criar — seção injeção SQL)