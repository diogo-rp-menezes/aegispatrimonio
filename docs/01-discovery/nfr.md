# Non-Functional Requirements (NFR) — Aegis1

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Arquitetura / Engenharia Backend / Engenharia Frontend / Segurança

---

## 1. Performance

### Backend (Java/Spring Boot)
* **NFR-P01:** Latência P95 de endpoints REST `/api/v1/**` ≤ 500 ms em condições normais (excluindo operações de I/O pesado como geração de PDF/QR Code em lote).
* **NFR-P02:** Busca Fuzzy (Levenshtein) — tempo de resposta P95 ≤ 300 ms para base de 100k ativos (com índices trigram/pg_trgm no MySQL + cache Redis para queries frequentes).
* **NFR-P03:** Manutenção Preditiva (Regressão Linear / Mínimos Quadrados) — cálculo de health check por ativo ≤ 50 ms; batch noturno para 10k ativos ≤ 5 min.
* **NFR-P04:** Geração de PDF (Termo de Responsabilidade) ≤ 2 s por documento; lote de 100 etiquetas QR Code ≤ 10 s.
* **NFR-P05:** Auditoria (Hibernate Envers) — overhead de escrita ≤ 10% em operações CRUD; consultas de histórico com paginação ≤ 500 ms.

### Frontend (Vue 3)
* **NFR-P06:** Latência P95 da chamada `request` (frontend → backend) ≤ 800 ms em condições normais de rede.
* **NFR-P07:** Time to Interactive (TTI) ≤ 3,5 s em 4G (mobile) e ≤ 2 s em broadband desktop.
* **NFR-P08:** Bundle JS (gzipped) ≤ 150 kB inicial (code-splitting por rota obrigatório).
* **NFR-P09:** Taxa de erro de integração API (`handleApiError`) < 1% das requisições totais (janela 5 min).

### Métricas & Ferramentas
* **Medição Backend:** Spring Boot Actuator + Micrometer + Prometheus/Grafana; k6 para load testing.
* **Medição Frontend:** Lighthouse CI no pipeline (TTI/bundle), APM (Sentry/Datadog RUM) em produção.
* **Tracing:** Header `trace-id` propagado frontend→backend; 100% mutações cobertas.

---

## 2. Escalabilidade

* **NFR-S01:** Frontend estático via CDN ≥ 500 usuários concorrentes ativos sem degradação > 10%.
* **NFR-S02:** Backend escala horizontalmente (K8s HPA) para picos de 200 req/s em mutações de ordens (iniciar, aprovar, concluir, cancelar, busca fuzzy).
* **NFR-S03:** Multi-tenancy por Filial — isolamento no nível de query (Hibernate Filter / `MultiTenancyFilter`) sem degradação linear com nº de filiais (testado até 100 filiais).
* **NFR-S04:** Sessão stateless (JWT); `authInterceptor` refresh automático único (max 1 retry) sem gargalo de refresh simultâneo (token bucket client-side).
* **NFR-S05:** Banco de dados MySQL 8.0 — connection pool (HikariCP) dimensionado para 2x CPU cores; read replicas para consultas de relatório/dashboard.

---

## 3. Disponibilidade & Confiabilidade

* **NFR-A01:** Disponibilidade frontend (CDN+DNS) ≥ 99,9% mensal (SLO); SLA provedor ≥ 99,5%.
* **NFR-A02:** RTO deploy frontend ≤ 15 min (rollback CDN); RTO backend ≤ 30 min (K8s rollback + Flyway down migration testada).
* **NFR-A03:** RPO = 0 para assets frontend (imutáveis versionados); RPO backend ≤ 1 min (MySQL binlog + backup contínuo).
* **NFR-A04:** Falha na API backend não trava UI; `handleApiError` exibe mensagem amigável + retry manual; circuit breaker (Resilience4j) no backend para dependências externas.
* **NFR-A05:** Health checks: `/actuator/health` (liveness/readiness) com checks de DB, Redis, disco; K8s probes configurados.
* **NFR-A06:** TestContainers (MySQL real) em CI garante compatibilidade de migrações Flyway antes de deploy.

---

## 4. Segurança

### Autenticação & Autorização (Aegis Shield)
* **NFR-SEC01:** Comunicação exclusivamente HTTPS (TLS 1.3); HSTS no CDN; certificado válido + rotação automática (cert-manager).
* **NFR-SEC02:** JWT stateless (RS256, chave rotacionada); `authInterceptor` frontend + `JwtTokenProvider` backend; refresh token HttpOnly cookie (SameSite=Strict) — migração de localStorage antes do go-live.
* **NFR-SEC03:** **RBAC Granular (Aegis Shield)**: Roles hierárquicas (ADMIN > GESTOR > TECNICO > USER > AUDITOR); Permissions contextuais por recurso/ação/filial; avaliação em tempo real (SpEL / custom `PermissionEvaluator`).
* **NFR-SEC04:** **Multi-tenancy por Filial**: Isolamento obrigatório no nível de query (Hibernate Filter + `MultiTenancyFilter`); testes cross-tenant em CI (TestContainers) — vazamento = falha de build.
* **NFR-SEC05:** Auditoria imutável (Hibernate Envers) em 100% entidades de domínio: captura quem, quando, o quê (diff campo a campo), IP, user-agent; logs de auditoria imutáveis (append-only, retenção 7 anos).

### Frontend & OWASP
* **NFR-SEC06:** Zero `console.*` em build produção; pipeline falha se `console.*` no bundle (eslint/no-console error).
* **NFR-SEC07:** CSP restritivo (`script-src 'self'`, `style-src 'self' 'unsafe-inline'`, `img-src 'self' data:`); sanitização de entradas (DOMPurify) em formulários de cadastro.
* **NFR-SEC08:** Proteção CSRF via SameSite=Strict + header `X-CSRF-Token` para mutações; CORS restrito a domínios permitidos.
* **NFR-SEC09:** Rate limiting por IP/usuário no API Gateway / Spring Cloud Gateway (ex.: 100 req/min para auth, 500 req/min para API).
* **NFR-SEC10:** Dependências: OWASP Dependency Check no CI; zero vulnerabilidades críticas/altas; renovação automática (Dependabot).

---

## 5. Usabilidade & Acessibilidade

* **NFR-U01:** WCAG 2.1 AA nos fluxos críticos (criar ordem, iniciar, aprovar, concluir, cancelar, listar, custoTotalPorAtivo, dashboard preditivo).
* **NFR-U02:** Responsivo 320–1920 px; touch targets ≥ 48×48 px para técnico em campo (mobile/PWA).
* **NFR-U03:** Leitores de tela: ARIA labels, roles, live regions em badges de status, botões condicionais, alertas de health check.
* **NFR-U04:** Internacionalização (i18n) preparada — v1 apenas pt-BR; chaves externas em JSON.

---

## 6. Observabilidade

* **NFR-O01:** Logs estruturados (JSON) backend: SLF4J + Logback + Logstash encoder; correlation ID (`trace-id`) propagado; níveis: ERROR/WARN em produção, DEBUG em staging.
* **NFR-O02:** Métricas Prometheus: latência (histogram), throughput (counter), erro (counter), JVM (memory, GC, threads), pool DB, cache hit/miss, health check preditivo (score, threshold).
* **NFR-O03:** Dashboards Grafana: Golden Signals (latency, traffic, errors, saturation) por endpoint; Business KPIs (ordens/dia, custoTotalPorAtivo, alertas preditivos ativos).
* **NFR-O04:** Alertas: erro API > 1% (5 min), latência P95 > 500 ms (5 min), falha refresh > 5% (15 min), disco preditivo > threshold (imediato), auditoria acesso negado > 10/min (possível ataque).
* **NFR-O05:** Frontend: Core Web Vitals (LCP, FID, CLS) via RUM; erros não tratados → Sentry; zero `console.*` em produção.

---

## 7. Manutenibilidade & Qualidade de Código

* **NFR-M01:** Complexidade ciclomática ≤ 10 por função (backend: Checkstyle/SpotBugs; frontend: ESLint complexity). Refatorar `request` (complexidade 13) em: retry, timeout, parsing, auth.
* **NFR-M02:** Cobertura mínima testes ≥ 80% (gate CI): Unit (JUnit 5 + Mockito), Integration (TestContainers MySQL), Contract (Spring Cloud Contract / Pact), Frontend (Vitest + Vue Test Utils).
* **NFR-M03:** Qualidade estática: Checkstyle (Google Java Style), SpotBugs, PMD no backend; ESLint (Airbnb/Standard) + Prettier + TypeScript strict no frontend; `no-console` error em prod.
* **NFR-M04:** Contrato OpenAPI 3.1 versionado (`/api/v1/openapi.yaml`); validação no CI (contract tests frontend↔backend); breaking changes = version bump (v2).
* **NFR-M05:** Documentação viva: ADRs para decisões arquiteturais; diagramas C4 (Structurizr/PlantUML) versionados; README por módulo.

---

## 8. Compliance & Regulatório

* **NFR-C01:** **LGPD** — Direito ao esquecimento: endpoint `DELETE /api/v1/usuarios/me` anonimiza dados pessoais (mantém auditoria Envers com hash); consentimento granular por finalidade.
* **NFR-C02:** **Retenção**: Logs erro/telemetria ≤ 30 dias; anonimização IDs ordem/usuário após 7 dias; Auditoria Envers retenção 7 anos (requisito fiscal/trabalhista).
* **NFR-C03:** **NR-10/NR-12** — Rastreabilidade documental: Termo de Responsabilidade PDF assinado digitalmente (ICP-Brasil ou assinatura eletrônica avançada); checklist de segurança obrigatório no fluxo **aprovar**.
* **NFR-C04:** **ISO 27001** — Controles: A.5.15 (acesso), A.8.2 (classificação informação), A.8.3 (manipulação mídia), A.12.4 (log), A.14.2 (segurança desenvolvimento).
* **NFR-C05:** **SOC 2 Type II** — Princípios: Segurança, Disponibilidade, Confidencialidade; evidências via auditoria Envers + logs estruturados + testes de penetração anuais.

---

## 9. Portabilidade & Compatibilidade

* **NFR-PO01:** Backend: Java 21 LTS, Spring Boot 3.3, MySQL 8.0 — compatível com qualquer K8s 1.28+ / Docker 24+.
* **NFR-PO02:** Frontend: Build independente de cloud; assets estáticos deployáveis em S3+CloudFront, Azure Static Web Apps, Netlify, Vercel, Nginx.
* **NFR-PO03:** Banco: MySQL 8.0 (primário); migração para PostgreSQL 15+ suportada via Flyway (dialect abstraction) — testado em CI.

---

## 10. Custo (Cost Efficiency)

* **NFR-CO01:** Hospedagem estática frontend ≤ USD 30/mês (500 users, 10k pageviews/dia).
* **NFR-CO02:** Backend K8s: 3 nodes (4 vCPU, 16 GB) ≈ USD 400/mês (spot/preemptible para batch preditivo noturno).
* **NFR-CO03:** MySQL managed (Cloud SQL / RDS) db.r6g.xlarge ≈ USD 300/mês; Redis cache ≈ USD 50/mês.
* **NFR-CO04:** Zero custo licença runtime/framework (stack MIT/Apache 2.0); ferramentas CI/CD open source (GitHub Actions/GitLab CI).

---

## 11. Rastreabilidade

| ID | Requisito Relacionado (BRD) | Prioridade | Artefato de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01–P05 | BRD#4, BRD#6 BR-10, BR-11 | Must | k6 load test, Actuator metrics |
| NFR-S01–S05 | BRD#3, BRD#5, BRD#6 BR-13 | Must | K8s HPA config, TestContainers cross-tenant |
| NFR-A01–A06 | BRD#7, BRD#8 | Must | K8s probes, Actuator health, CI TestContainers |
| NFR-SEC01–SEC10 | BRD#6 BR-13–16, BRD#8 | Must | Pen test, OWASP Dep Check, CI cross-tenant test |
| NFR-U01–U04 | BRD#3 | Should | Lighthouse CI, axe-core, manual QA |
| NFR-O01–O05 | BRD#4, BRD#8 | Must | Grafana dashboards, Alertmanager rules |
| NFR-M01–M05 | BRD#8, BRD#9 | Must | CI gates (coverage, quality, contract) |
| NFR-C01–C05 | BRD#7, BRD#8 | Must | DPIA, Audit logs, Pen test report |
| NFR-PO01–PO03 | BRD#7 | Should | Multi-cloud deploy test |
| NFR-CO01–CO04 | BRD#9 | Could | FinOps dashboard |

---

*Documento regenerado com base em análise AST completa do backend Java (performance, security, observability, compliance) + frontend Vue. Substitui versão 1.0 que continha apenas visão frontend.*