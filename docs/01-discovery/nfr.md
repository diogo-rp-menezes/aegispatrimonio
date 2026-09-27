# Non-Functional Requirements (NFR) — Aegis Patrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Engenharia/Arquitetura

## 1. Performance
* **NFR-P01:** Tempo de resposta da API (p95) ≤ 500 ms para endpoints de leitura (listagem, busca por ID, health history, alertas) e ≤ 800 ms para endpoints de escrita (criação, atualização, health check, aprovação) — meta alinhada ao *guardrail* do BRD (O4) e à complexidade ciclomática atual de `request` (13) que impõe risco de degradação <!-- source: diagnóstico#L1 --> <!-- source: brd#L1 -->
* **NFR-P02:** Time to First Byte (TTFB) ≤ 150 ms em rede local (intranet) para o monolito Java servindo API REST e assets estáticos do frontend
* **NFR-P03:** Renderização inicial (LCP) < 3,0 s em conexão 4G / 3G médio (frontend vanilla JS + `@popperjs/core` apenas, sem framework reativo) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] premissa: bundle JS total < 200 KB gzip, sem code-splitting
* **NFR-P04:** Throughput mínimo de 100 req/s sustentado por instância (single VM/container) para carga mista 70% leitura / 30% escrita — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] premissa: VM com 2 vCPU / 4 GB RAM, pool de conexões HikariCP (default Spring Boot) = 10, banco com índices adequados em `filial_id`, `ativo_id`, `usuario_id`
* **NFR-P05:** Latência de `updateHealthCheck` (ingestão de métricas de hardware) ≤ 200 ms p95 — crítico para O4 (monitoramento preditivo ativo) <!-- source: brd#L1 -->
* **Método de medição:** k6 scripts em `tests/performance/` (a criar) executados em pipeline de CI/CD; APM via Micrometer + Prometheus + Grafana (endpoints `/actuator/prometheus`, `/actuator/health`) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] stack de observabilidade não existe no código atual

## 2. Escalabilidade
* **NFR-S01:** Escalabilidade **vertical only** (scale-up de vCPU/RAM da VM/container) — a topologia é monolito single-tenant on-premise/cloud privado sem Kubernetes, sem auto-scaling horizontal <!-- source: stack#L1 --> <!-- source: brd#L1 -->
* **NFR-S02:** Suportar até 500 usuários concorrentes (sessões JWT ativas) sem degradação > 15% na latência p95 — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] premissa: 10k ativos totais, 20% ativos com health check diário, pool de threads Tomcat = 200
* **NFR-S03:** Otimização de consultas com `findByFilialIdIn` (isolamento por filial) via índices compostos `(filial_id, ativo_id)` e paginação obrigatória (max page size = 100) para evitar full table scans — deriva de BR-07 e risco R3 de vazamento de dados <!-- source: brd#L1 -->
* **NFR-S04:** `RealisticDataSeeder` (complexidade 15) executado apenas em ambientes de teste/homologação; em produção, migrações de dados via scripts Flyway/Liquibase versionados — mitigação do risco R2 <!-- source: diagnóstico#L1 --> <!-- source: brd#L1 -->

## 3. Disponibilidade & Confiabilidade
* **NFR-A01:** Uptime mínimo **99,5% (SLA contratual)** / **99,9% (SLO interno)** — janela de manutenção planejada: 2h/semana (domingo 02:00–04:00 UTC-3) para deploy de releases e manutenção de banco
* **NFR-A02:** RTO (Recovery Time Objective) ≤ 4 h — restauração de VM + banco a partir de snapshot/backup diário (single instance, sem HA cluster) <!-- source: brd#L1 -->
* **NFR-A03:** RPO (Recovery Point Objective) ≤ 24 h — backup diário completo do banco (pg_dump / mysqldump / expdp conforme engine) + WAL/archivelog se suportado — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] engine de banco não definido no código
* **NFR-A04:** Zero *single points of failure* **dentro da aplicação** (health checks de liveness/readiness em `/actuator/health/liveness`, `/actuator/health/readiness`; graceful shutdown com `server.shutdown=graceful`); infraestrutura (VM, DB, DNS, TLS) fora do escopo do código — responsabilidade de Infra/Cloud

## 4. Segurança
* **NFR-SEC01:** Todos os dados em trânsito criptografados via **TLS 1.3** (certificado válido, HSTS, secure cookies) — terminação no reverse proxy (nginx/Traefik) ou load balancer cloud
* **NFR-SEC02:** Dados sensíveis em repouso criptografados (**AES-256** no volume do banco / TDE se suportado pelo engine) — LGPD exige proteção de dados de funcionários, ativos, localização <!-- source: brd#L1 -->
* **NFR-SEC03:** Autenticação **stateless JWT** (HS256 ou RS256) com expiração ≤ 15 min (access) + refresh token rotation ≤ 7 dias; revogação imediata via `logout`/`clearSession` idempotentes (BR-12) <!-- source: brd#L1 --> <!-- source: glossario#L1 -->
* **NFR-SEC04:** Autorização granular **baseada em papel (Admin/User)** aplicada em **camada de serviço** (não apenas em query) — mitigação do risco R3 (vazamento entre filiais) <!-- source: brd#L1 -->
* **NFR-SEC05:** Conformidade **OWASP Top 10** (2021): validação de entrada (BR-05), proteção contra IDOR via `buscarPorId_comIdInexistente_deveRetornarNotFound` (BR-06), headers de segurança (CSP, X-Frame-Options, Referrer-Policy), rate limiting em `/auth/*` (5 req/min/IP) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] rate limiting não implementado no código atual
* **NFR-SEC06:** Rotação de segredos (JWT secret, DB password, SMTP credentials) a cada **90 dias** via vault/secret manager externo — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] não há vault no código
* **NFR-SEC07:** Auditoria imutável: hooks `preUpdate`/`onUpdate` gravam `updated_at`, `updated_by` em tabela de log separada (append-only) — BR-10 <!-- source: brd#L1 --> <!-- source: glossario#L1 -->
* **NFR-SEC08:** Remoção de `console.error`/`console.debug` em produção (`frontend/src/services/api.js:44,107`) antes de release — risco R7 <!-- source: diagnóstico#L1 -->

## 5. Usabilidade & Acessibilidade
* **NFR-U01:** Conformidade **WCAG 2.1 AA** nos fluxos críticos (login, listagem de ativos, criação de manutenção, aprovação, health check) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] frontend vanilla JS + Popper.js exige testes manuais/automatizados (axe-core)
* **NFR-U02:** Suporte a leitores de tela (NVDA, JAWS, VoiceOver) — labels ARIA, foco visível, ordem de tabulação lógica
* **NFR-U03:** Interface **responsiva** de 320 px a 1920 px (breakpoints: 320, 768, 1024, 1440) — CSS Grid/Flexbox nativo, sem framework UI
* **NFR-U04:** Feedback visual de latência (skeleton loaders, spinners) para chamadas `request` > 300 ms — mitiga percepção de lentidão dada complexidade de `request` (13) <!-- source: diagnóstico#L1 -->

## 6. Observabilidade
* **NFR-O01:** Logs estruturados **JSON** (timestamp ISO-8601, level, traceId, spanId, userId, filialId, action, entity, entityId, durationMs) via Logback/Log4j2 + MDC — correlacionáveis no Loki/ELK
* **NFR-O02:** Métricas expostas em `/actuator/prometheus` (latência p50/p95/p99 por endpoint, taxa de erro 5xx, pool de conexões, JVM heap/GC, contadores de negócio: `ativos.cadastrados`, `manutencoes.aprovadas`, `alertas.disparados`)
* **NFR-O03:** **Distributed tracing** (OpenTelemetry Java agent) em 100% das requisições HTTP de entrada + chamadas JDBC — propagação de `traceparent` para frontend via header `trace-id` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] não instrumentado hoje
* **NFR-O04:** Alertas configurados (PrometheusRule / Grafana Alerting):
  - Latência p95 > 500 ms por 5 min (NFR-P01)
  - Taxa de erro 5xx > 1% por 2 min
  - Uso de heap > 85% por 10 min
  - Pool de conexões > 90% por 5 min
  - Falha de health check de liveness/readiness
* **NFR-O05:** Dashboard único "Aegis Overview" (Grafana) com: throughput, latência, erros, saturação (USE method), métricas de negócio (ativos ativos, health checks/dia, MTTR calculado)

## 7. Manutenibilidade & Qualidade de Código
* **NFR-M01:** Cobertura mínima de testes **80%** (linhas) — unitários (JUnit 5 + Mockito) para services/mappers/specifications; integração (Testcontainers) para repositories/controllers; contrato (Pact) para API — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] não há testes no codebase atual
* **NFR-M02:** Complexidade ciclomática **máxima 10** por método/função — refatorar itens atuais acima do limite:
  - `frontend/src/services/api.js:54` `request` (13) → extrair `buildHeaders`, `handleResponse`, `retryLogic`
  - `RealisticDataSeeder.run` (15) → dividir em seeders por entidade (`FilialSeeder`, `AtivoSeeder`, …)
  - `AtivoMapper.toDTO` (14) → separar DTOs *list* vs *detail*, evitar N+1 (risco R4) <!-- source: diagnóstico#L1 -->
  - `ManutencaoSpecification.build` (14) → *predicate builders* compostos
  - `AlertNotificationService.checkResourceUsageAlerts` (17) → *rules engine* simples (threshold por tipo de ativo) — risco R5 <!-- source: diagnóstico#L1 -->
* **NFR-M03:** **Zero stubs vazios** — corrigir `Usuario.setUsername` (linha 86) imediatamente — risco R6 <!-- source: diagnóstico#L1 -->
* **NFR-M04:** Documentação de API via **OpenAPI 3.1** (SpringDoc) gerada em build, publicada em `/swagger-ui.html` e validada em CI (breaking change detection)
* **NFR-M05:** *Static analysis* no pipeline: SpotBugs, PMD, Checkstyle (Google Java Style), ESLint (frontend) — quality gate: 0 critical/high, ≤ 5 medium

## 8. Compliance & Regulatório
* **NFR-C01:** **LGPD** — Direito ao esquecimento: endpoint `DELETE /api/usuarios/{id}/anonymize` (pseudonimização de dados pessoais em logs de auditoria, ativos, manutenções) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] não existe no código
* **NFR-C02:** Retenção de dados: logs de auditoria (`onUpdate`/`preUpdate`) **5 anos**; health checks **2 anos**; alertas lidos (`markAsRead`) **1 ano** — política configurável via tabela `retention_policy` — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **NFR-C03:** Isolamento estrito por filial (BR-07) validado por **testes de penetração** trimestrais (OWASP ZAP + cenários de *tenant crossing*) — risco R3 <!-- source: brd#L1 -->

## 9. Portabilidade & Compatibilidade
* **NFR-PO01:** Compatibilidade com **2 últimos major releases** de Chrome, Firefox, Edge, Safari (desktop) — frontend vanilla ES2020+, transpilado via esbuild/Babel para ES2017
* **NFR-PO02:** Deploy suportado em **qualquer VM/container Linux x86_64/ARM64** com JRE 21+ (Eclipse Temurin) + banco relacional (PostgreSQL 15+, MySQL 8.0+, Oracle 19c+) — engine a definir na implantação <!-- source: stack#L1 -->
* **NFR-PO03:** Build reproduzível: `./mvnw -DskipTests package` gera JAR fat/uber + `frontend/dist/` empacotado em `static/` — imagem Docker multi-stage (build + runtime distroless) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Dockerfile não existe no repo

## 10. Custo (Cost Efficiency)
* **NFR-CO01:** Custo de infraestrutura **≤ R$ 0,15 / ativo / mês** (meta do BRD — Guardrail Metric #3) — para 10k ativos = ≤ R$ 1.500/mês (compute + DB + backup + monitoramento) — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] premissa: VM t3.medium (2 vCPU, 4 GB) + RDS db.t3.medium em AWS SA-East-1 ≈ R$ 1.200/mês
* **NFR-CO02:** Otimização de queries (índices, paginação, DTOs enxutos) para minimizar I/O e CPU do banco — impacto direto no custo de instância RDS

## 11. Rastreabilidade
| ID | Requisito Relacionado (BRD / Business Rule) | Prioridade | Status de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01 | O4 (Guardrail: Latência P95 ≤ 500 ms), BR-08 | Must | Não validado |
| NFR-P02 | O2 (MTTR ≤ 4,8h — depende de API rápida) | Must | Não validado |
| NFR-P03 | O1 (Inventário 100% — usabilidade afeta adoção) | Should | Não validado |
| NFR-P04 | O3 (Adoção fluxo digital ≥ 90%) | Should | Não validado |
| NFR-P05 | O4 (Monitoramento preditivo ≥ 80%) | Must | Não validado |
| NFR-S01 | Topologia single-tenant on-premise (BRD §5) | Must | Não validado |
| NFR-S02 | O1, O3 (volume de usuários/ativos) | Should | Não validado |
| NFR-S03 | BR-07 (Isolamento por filial), Risco R3 | Must | Não validado |
| NFR-S04 | BR-14 (Seed realista), Risco R2 | Must | Não validado |
| NFR-A01 | SLA implícito para sistema de gestão patrimonial | Must | Não validado |
| NFR-A02 | RTO ≤ 4h (single instance restore) | Must | Não validado |
| NFR-A03 | RPO ≤ 24h (backup diário) | Must | Não validado |
| NFR-A04 | Health checks, graceful shutdown | Should | Não validado |
| NFR-SEC01 | LGPD (BRD §7), dados em trânsito | Must | Não validado |
| NFR-SEC02 | LGPD, dados em repouso | Must | Não validado |
| NFR-SEC03 | BR-12 (Sessão idempotente), JWT stateless | Must | Não validado |
| NFR-SEC04 | BR-01 a BR-04 (Permissões Admin/User), Risco R3 | Must | Não validado |
| NFR-SEC05 | BR-05, BR-06, OWASP Top 10 | Must | Não validado |
| NFR-SEC06 | Boas práticas de secret management | Should | Não validado |
| NFR-SEC07 | BR-10 (Auditoria automática) | Must | Não validado |
| NFR-SEC08 | Risco R7 (Console logs em prod) | Must | Não validado |
| NFR-U01 | Acessibilidade legal (LGPD/Decreto 10.098) | Should | Não validado |
| NFR-U02 | Inclusão digital | Should | Não validado |
| NFR-U03 | Uso em campo (notebooks, tablets) | Should | Não validado |
| NFR-U04 | Percepção de performance (complexidade request) | Should | Não validado |
| NFR-O01 | Observabilidade para MTTR (O2) | Must | Não validado |
| NFR-O02 | Métricas de negócio (North Star) | Must | Não validado |
| NFR-O03 | Tracing para debug distribuído | Should | Não validado |
| NFR-O04 | Alertas proativos (latência, erro, saturação) | Must | Não validado |
| NFR-O05 | Dashboard único para Operações | Should | Não validado |
| NFR-M01 | Qualidade, prevenção de regressão (Risco R1) | Must | Não validado |
| NFR-M02 | Complexidade ciclomática (Riscos R1, R4, R5) | Must | Não validado |
| NFR-M03 | Stub vazio (Risco R6) | Must | Não validado |
| NFR-M04 | Contrato de API estável | Should | Não validado |
| NFR-M05 | Quality gate automatizado | Should | Não validado |
| NFR-C01 | LGPD — direito ao esquecimento | Must | Não validado |
| NFR-C02 | Retenção legal de logs/health checks | Must | Não validado |
| NFR-C03 | Validação de isolamento (Risco R3) | Must | Não validado |
| NFR-PO01 | Suporte a dispositivos heterogêneos | Should | Não validado |
| NFR-PO02 | Portabilidade de deploy (on-prem/cloud) | Must | Não validado |
| NFR-PO03 | Build reproduzível, containerizável | Should | Não validado |
| NFR-CO01 | Guardrail BRD: ≤ R$ 0,15/ativo/mês | Must | Não validado |
| NFR-CO02 | Eficiência de queries → custo DB | Should | Não validado |