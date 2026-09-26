# Product Roadmap & Sprint History — Aegis1 (Regenerado com AST Java)

> **Versão:** 2.0 · **Horizonte de planejamento:** Trimestral · **Última atualização:** 2025-01-15
> **Base:** Story Map v2.0 + Use Cases v2.0 + BRD v2.0 + NFR v2.0
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/PWA) + Infra (K8s/Docker)

---

## 1. Strategic Themes (Now / Next / Later)

### Now (Q1 2025 — **Release 1.0 MVP** — *Core Patrimônio + Manutenção Corretiva + Auth + Busca + QR/PDF*)
* **Jornada ponta-a-ponta funcional completa:** Cadastros mestres (multi-tenancy) → Ativos com hardware + depreciação + QR Code + Termo PDF → Ordens Corretivas (criar→iniciar→aprovar→concluir/cancelar) → CustoTotalPorAtivo/TCO → Auth JWT + Refresh + RBAC Básico + Auditoria Envers → Busca Fuzzy Global → QR Code Lote
* **Qualidade de engenharia:** `request` refatorado (ciclomática ≤ 10), zero `console.*`, TestContainers MySQL real, Coverage ≥ 80%, OpenAPI 3.1 + Contract Tests, CI/CD completo
* **Infra pronta para produção:** K8s manifests, Docker Compose, Flyway migrations, Observabilidade (Prometheus/Grafana/Alertmanager), Security headers, Rate limiting

### Next (Q2 2025 — **Release 1.1 Should Have** — *Preventiva + Preditiva + RBAC Granular + LGPD + Compliance*)
* **Manutenção Preventiva Completa:** Planos CRON, geração automática ordens, relatório aderência (evidência NR-10/12)
* **Manutenção Preditiva Completa:** Health Check PWA mobile (offline-first + QR scan), Regressão Linear (Mínimos Quadrados) batch noturno, Ordem Preditiva Automática (prob > 80% < 30d), Dashboard Riscos com IC 95%
* **Segurança Aegis Shield Granular:** Matriz Role × Permission × Contexto (Global/Filial), Auditoria 100% entidades + export assinado, Cross-tenant leak detection + SIEM, Troca contexto filial (Admin Global)
* **LGPD & Compliance:** Anonimização (esquecimento) + auditoria preservada, Portabilidade (export JSON), Relatórios Compliance (NR-10/12, LGPD, ISO 27001), Termo Responsabilidade assinatura digital (placeholder ICP-Brasil)
* **Observabilidade Avançada:** Golden Signals, Business KPIs, Tracing distribuído (trace-id), Alertas preditivos/SLA/segurança

### Later (Q3-Q4 2025 — **Release 1.2 Could Have** — *Maturidade Operacional + PWA Completo + Multi-cloud*)
* **Maturidade Ordens:** Checklists digitais configuráveis, Anexos fotográficos, Reavaliação ativos + depreciação recalculada, Importação lote ativos (CSV/Excel)
* **Busca Avançada:** Configuração threshold fuzzy runtime (Admin), Pesos boost por entidade
* **Segurança Hardening:** Pen test automatizado CI (ZAP/CodeQL) + block deploy, Rate limiting API Gateway por endpoint
* **Compliance Avançado:** Validação integridade checksums semanal (SHA-256), DPIA automatizado versionado
* **PWA Completo:** Manifest, Service Worker, Push Notifications, Background Sync, Install prompt
* **Multi-cloud/DB:** PostgreSQL 15+ suportado via Flyway dialect abstraction (testado CI)

---

## 2. Release Timeline & Milestones

| Release | Versão | Data Alvo | Foco Principal | Status |
| :--- | :--- | :--- | :--- | :--- |
| **MVP** | 1.0.0 | **31/03/2025** | Core Patrimônio + Corretiva + Auth + Busca + QR/PDF + Qualidade/Infra | 🟡 Planejado |
| **Should** | 1.1.0 | **30/06/2025** | Preventiva + Preditiva + RBAC Granular + LGPD + Compliance + Observabilidade | 🟡 Planejado |
| **Could** | 1.2.0 | **30/09/2025** | Maturidade + PWA Completo + Hardening + Multi-cloud | 🟢 Backlog |
| **Mobile** | 2.0.0 | **Q1 2026** | App Mobile Nativo (iOS/Android) — Offline-first, QR Scanner nativo, Push nativo | 🔵 Futuro |

---

## 3. Sprint Plan (MVP — Release 1.0)

> **Premissas de Capacidade** [REQUER VALIDAÇÃO HUMANA]: Time full-stack 4 devs (2 backend, 2 frontend), velocidade histórica 32 pts/sprint (2 semanas), sprints iniciando às segundas. Pontuação Fibonacci (1,2,3,5,8,13). Início Sprint 1: 20/01/2025. **Validar com PO e Tech Leads antes de comprometer.**

### Sprint 1 — 20/01 a 31/01/2025
**Sprint Goal:** Fundação Backend (Domain, Repository, Config) + Cadastros Mestres API + Multi-tenancy + Frontend Cadastros UI + Qualidade Base

| Story | Descrição | Pts | Owner | Dependências |
| :--- | :--- | :--- | :--- | :--- |
| **MVP-CAD-001** | CRUD Filiais (Admin Global) — Domain, Repository, Service, Controller, Flyway seed | 8 | Backend | — |
| **MVP-CAD-002** | CRUD Departamentos (scoped filial) — MultiTenancyFilter + Hibernate Filter | 8 | Backend | MVP-CAD-001 |
| **MVP-CAD-003** | CRUD Fornecedores (categoria, SLA, avaliação) | 5 | Backend | MVP-CAD-001 |
| **MVP-CAD-004** | CRUD Funcionários + Provisionamento Usuario | 8 | Backend | MVP-CAD-001, MVP-CAD-002 |
| **MVP-CAD-005** | CRUD Tipos de Ativo (vida útil, valor residual, requerHardware) | 5 | Backend | — |
| **MVP-FE-CAD-001** | Frontend: UI Cadastros (Tabela paginada, Modal CRUD, Busca Fuzzy, Seletor Filial) | 13 | Frontend | MVP-CAD-001 a 005 (API mock MSW se atraso) |
| **MVP-TEC-002** | Remover 3 `console.*` residuais (`api.js:26,49,52`) + ESLint no-console error | 2 | Frontend | — |
| **MVP-TEC-004** | `handleApiError` padronizado (401→refresh, 409→regra, 5xx→amigável) + Sentry | 5 | Frontend | — |
| **MVP-INFRA-001** | Spring Boot 3.3 + Java 21 + MySQL 8 + Flyway + Lombok + Maven Wrapper | 3 | Backend | — |
| **MVP-INFRA-002** | TestContainers MySQL + Config CI (GitHub Actions) + Coverage Gate 80% | 5 | Backend | MVP-INFRA-001 |
| **MVP-INFRA-003** | Docker Compose (MySQL, Redis) + K8s Manifests Base (Deployment, Service, Ingress, HPA) | 5 | DevOps | MVP-INFRA-001 |

**Riscos:** Contrato OpenAPI não definido → mockar com MSW no frontend; Flyway baseline em produção.

---

### Sprint 2 — 03/02 a 14/02/2025
**Sprint Goal:** Ativos + Hardware + QR Code + Termo PDF + Depreciação + Auth JWT + Refresh + Frontend Ativos

| Story | Descrição | Pts | Owner | Dependências |
| :--- | :--- | :--- | :--- | :--- |
| **MVP-ASV-001** | Ativo Domain/Service/Controller — CRUD + Depreciação Linear (BR-03) + Validação BR-01, BR-02 | 13 | Backend | MVP-CAD-001 a 005 |
| **MVP-ASV-002** | AtivoDetalheHardware + Componentes (Disco, Memória, AdaptadorRede) — CRUD Sub-recursos | 8 | Backend | MVP-ASV-001 |
| **MVP-ASV-003** | QRCodeGenerator (ZXing) + PdfGenerator (FlyingSaucer/Thymeleaf) — Unitário + Termo PDF | 8 | Backend | MVP-ASV-001 |
| **MVP-ASV-004** | Transferência Ativo + Novo Termo + Auditoria | 5 | Backend | MVP-ASV-001, MVP-ASV-003 |
| **MVP-ASV-005** | Baixa Ativo + TCO Final Consolidado | 3 | Backend | MVP-ASV-001 |
| **MVP-SRC-001** | Auth: JwtTokenProvider (RS256), SecurityConfig, AuthController (/login, /refresh), Refresh Cookie HttpOnly | 13 | Backend | MVP-INFRA-001 |
| **MVP-SRC-002** | Multi-tenancy: MultiTenancyFilter + TenantContextHolder + Hibernate Filter (filial_id) | 8 | Backend | MVP-SRC-001 |
| **MVP-SRC-003** | RBAC Básico: Roles (ADMIN, GESTOR, TECNICO, USER), AegisShieldPermissionEvaluator | 8 | Backend | MVP-SRC-001 |
| **MVP-SRC-004** | Auditoria Envers: @Audited em Ativo, Ordem, Usuario; AuditoriaController (consulta + diff) | 8 | Backend | MVP-SRC-001 |
| **MVP-FE-ASV-001** | Frontend: Ativos UI (Form Hardware dinâmico, QR Display, PDF Viewer, Transferência, Baixa) | 13 | Frontend | MVP-ASV-001 a 005 (API) |
| **MVP-FE-AUTH-001** | Frontend: Login Form + authInterceptor (Pinia) + Refresh Automático 1x + Redirect Login | 8 | Frontend | MVP-SRC-001 |
| **MVP-TEC-001** | Refatorar `request` (ciclomática 13 → retry, timeout, parsing, auth separados; ≤10 cada) | 8 | Frontend | — |

**Riscos:** Geração PDF/QR performance; Assinatura digital placeholder; Sincronização refresh token concorrente.

---

### Sprint 3 — 17/02 a 28/02/2025
**Sprint Goal:** Ordens Corretivas Completas + Busca Fuzzy Global + CustoTotalPorAtivo/TCO + Frontend Ordens + Dashboard

| Story | Descrição | Pts | Owner | Dependências |
| :--- | :--- | :--- | :--- | :--- |
| **MVP-ORD-001** | Ordem Corretiva: Domain, Service, Controller — Criar (busca fuzzy ativo), BR-05, Estado ABERTA | 13 | Backend | MVP-ASV-001, MVP-CAD-004 |
| **MVP-ORD-002** | Ordem: Listar/Buscar (Filtros + Paginação + Fuzzy Ativo) | 8 | Backend | MVP-ORD-001 |
| **MVP-ORD-003** | Ordem: Iniciar (PATCH /iniciar) — Valida BR-05 → EM_ANDAMENTO | 5 | Backend | MVP-ORD-001 |
| **MVP-ORD-004** | Ordem: Aprovar (Evidência Obrigatória BR-06) → APROVADA | 5 | Backend | MVP-ORD-003 |
| **MVP-ORD-005** | Ordem: Concluir (Custos Finais) → CONCLUIDA + Atualiza custoTotalPorAtivo (BR-08) | 8 | Backend | MVP-ORD-004 |
| **MVP-ORD-006** | Ordem: Cancelar (Motivo Obrigatório) → CANCELADA | 3 | Backend | MVP-ORD-001 |
| **MVP-CUST-001** | Relatório CustoTotalPorAtivo/TCO: Controller + Service (Drill-down ordens) | 8 | Backend | MVP-ORD-005, MVP-ASV-001 |
| **MVP-BUS-001** | Busca Global Fuzzy: FuzzySearchService + LevenshteinDistance + BuscaController (tipos, boost, highlight) | 13 | Backend | MVP-ASV-001, MVP-ORD-001, MVP-CAD-003, MVP-CAD-004 |
| **MVP-FE-ORD-001** | Frontend: Ordens UI (Criar com busca fuzzy, Lista filtros, Detalhe, Ações Iniciar/Aprovar/Concluir/Cancelar) | 13 | Frontend | MVP-ORD-001 a 006, MVP-BUS-001 |
| **MVP-FE-CUST-001** | Frontend: Dashboard Custos (Tabela TCO, Drill-down, Export CSV) | 5 | Frontend | MVP-CUST-001 |
| **MVP-FE-BUS-001** | Frontend: Busca Global Header (Debounce, Dropdown agrupado, Highlight, Navegação) | 5 | Frontend | MVP-BUS-001 |
| **MVP-REL-001** | Etiquetas QR Code Lote: RelatorioController + QRCodeGenerator Lote (PDF A4 24/folha) | 5 | Backend | MVP-ASV-003 |

**Riscos:** Busca fuzzy performance (índices trigram); CustoTotalPorAtivo divergência frontend/backend (contrato OpenAPI); Evidência aprovação validação.

---

### Sprint 4 — 03/03 a 14/03/2025
**Sprint Goal:** Dashboard Unificado + Alertas Tempo Real + WebSocket + Observabilidade + Segurança Hardening + Testes Integrados

| Story | Descrição | Pts | Owner | Dependências |
| :--- | :--- | :--- | :--- | :--- |
| **MVP-FE-DASH-001** | Frontend: Dashboard Principal (KPIs Cards, Gráficos Chart.js, Alertas Feed, WebSocket/SSE) | 13 | Frontend | MVP-ORD-002, MVP-CUST-001 |
| **MVP-BACK-DASH-001** | Backend: DashboardController (Ordens KPIs, Tendência, Por Prioridade/Filial/Tipo) + WebSocket Broker | 8 | Backend | MVP-ORD-002, MVP-CUST-001 |
| **MVP-OBS-001** | Observabilidade: Micrometer + Prometheus + Grafana Dashboards (Golden Signals, Business KPIs) | 8 | Backend/DevOps | MVP-INFRA-001 |
| **MVP-OBS-002** | Alertas: Alertmanager Rules (Erro>1%, Latência>500ms, Refresh>5%, SLA Breach) | 5 | DevOps | MVP-OBS-001 |
| **MVP-OBS-003** | Tracing: trace-id Header Propagado (Frontend→Backend) + Logs JSON Correlation ID | 5 | Backend/Frontend | MVP-OBS-001 |
| **MVP-SEC-001** | Security Hardening: CSP, CORS, Rate Limiting (Spring Cloud Gateway), OWASP Dep Check CI | 8 | Backend/DevOps | MVP-SRC-001 |
| **MVP-TEC-003** | OpenAPI 3.1 Spec + Contract Tests (Spring Cloud Contract / Pact) Frontend↔Backend | 8 | Backend/Frontend | MVP-ORD-001, MVP-ASV-001 |
| **MVP-TEC-005** | Testes Integração E2E (TestContainers): Cadastros, Ativos, Ordens, Auth, Busca, Auditoria | 13 | Backend | Todas APIs MVP |
| **MVP-TEC-006** | Testes E2E Frontend (Cypress): Jornada Completa Cadastro→Ativo→Ordem→Custo→Busca | 8 | Frontend | MVP-FE-* |

**Riscos:** WebSocket escalabilidade; Contract tests manutenção; Performance busca fuzzy em base grande.

---

### Sprint 5 — 17/03 a 28/03/2025 (Buffer / Hardening MVP)
**Sprint Goal:** Bug Bash + Correção Regressões + Validação Critérios Lançamento + Documentação + Deploy Produção

| Story | Descrição | Pts | Owner | Dependências |
| :--- | :--- | :--- | :--- | :--- |
| **MVP-HARD-001** | Bug Bash + Correção Regressões (Prioridade: Integração, Segurança, Performance) | 13 | All | Sprint 1-4 |
| **MVP-HARD-002** | Validação Critérios Lançamento: Coverage ≥80%, Zero Console.*, Zero Vuln Crítica/Alta, P95 Latência, Multi-tenancy Test Pass | 8 | QA/All | Sprint 1-4 |
| **MVP-HARD-003** | Documentação: OpenAPI Final, ADRs Decisões, Runbooks Deploy/Rollback, Changelog | 5 | Tech Lead | Sprint 1-4 |
| **MVP-HARD-004** | Deploy Produção: K8s Prod, DNS, TLS, Secrets, Smoke Tests, Monitoramento Ativo | 8 | DevOps | MVP-HARD-002 |
| **MVP-HARD-005** | Go-Live Support: Hypercare 2 semanas, Runbook Incidentes, Escalação | 5 | DevOps/Support | MVP-HARD-004 |

**Critérios de Lançamento MVP (Definition of Done):**
- [ ] Coverage ≥ 80% (Backend + Frontend)
- [ ] Zero `console.*` no bundle produção (ESLint error)
- [ ] Zero vulnerabilidades crítica/alta (OWASP Dep Check)
- [ ] Latência P95 API < 500ms (Backend), < 800ms (Frontend→Backend)
- [ ] Testes Cross-tenant Multi-tenancy: 0 vazamentos
- [ ] Contrato OpenAPI 3.1 validado + Contract Tests Pass
- [ ] Auditoria Envers: 100% entidades domínio (CREATE/UPDATE/DELETE + Diff)
- [ ] Busca Fuzzy: P95 < 300ms (100k ativos, índices trigram)
- [ ] QR Code/PDF: Geração unitária < 2s, Lote 100 < 10s
- [ ] Auth: Refresh token 1x retry funciona; Falha → logout limpo
- [ ] Runbooks: Deploy, Rollback, Incident Response, Backup/Restore testados

---

## 4. Sprint Plan (Should — Release 1.1)

> **Início Estimado:** 07/04/2025 | **Fim Estimado:** 30/06/2025 (6 Sprints)

| Sprint | Sprint Goal | Stories Principais |
| :--- | :--- | :--- |
| **Sprint 6** | Preventiva Core: Planos CRON + Scheduler Geração Auto + Relatório Aderência | SH-PRE-001 a 004 |
| **Sprint 7** | Preditiva Core: Health Check PWA (QR Scan + SMART) + Offline-First (IndexedDB/Sync) | SH-PRD-001, SH-PRD-002 |
| **Sprint 8** | Preditiva ML: Regressão Linear Batch Noturno + Ordem Preditiva Auto + Dashboard Riscos | SH-PRD-003, SH-PRD-004, SH-PRD-005 |
| **Sprint 9** | Aegis Shield Granular: Matriz RBAC (Role×Perm×Contexto) + Auditoria 100% + Cross-tenant Leak Detection | SH-SRC-001 a 004 |
| **Sprint 10** | LGPD + Compliance: Anonimização + Portabilidade + Relatórios NR-10/12/LGPD/ISO + Termo Assinatura Digital | SH-LGD-001, SH-LGD-002, SH-REL-001, SH-REL-002 |
| **Sprint 11** | Observabilidade Avançada + Tracing + Hardening + Buffer | SH-TEC-001, SH-TEC-002 + Bug Bash + Validação Release 1.1 |

---

## 5. Sprint Plan (Could — Release 1.2)

> **Início Estimado:** 07/07/2025 | **Fim Estimado:** 30/09/2025 (6 Sprints)

| Sprint | Sprint Goal | Stories Principais |
| :--- | :--- | :--- |
| **Sprint 12** | Maturidade Ordens: Checklists + Anexos + Reavaliação Ativos | CO-ORD-001, CO-ORD-002, CO-ASV-001 |
| **Sprint 13** | Importação Lote Ativos + Config Fuzzy Runtime + Rate Limiting | CO-ASV-002, CO-BUS-001, CO-SEC-002 |
| **Sprint 14** | Pen Test CI (ZAP/CodeQL) + Block Deploy + Security Hardening | CO-SEC-001 |
| **Sprint 15** | Compliance Avançado: Integridade Checksums + DPIA Automatizado | CO-CMP-001, CO-CMP-002 |
| **Sprint 16** | PWA Completo: Manifest, SW, Push, Background Sync, Install Prompt | CO-PWA-001 |
| **Sprint 17** | PostgreSQL Support + Multi-cloud Deploy Test + Buffer + Release 1.2 | CO-TEC-001 + Bug Bash + Validação |

---

## 6. Closed Sprints

> Nenhuma sprint concluída registrada neste repositório (projeto em fase inicial). Histórico será populado a partir da Sprint 1 (20/01/2025).

---

## 7. Capacity Planning & Resource Allocation

| Role | Alocação MVP | Alocação Should | Alocação Could |
| :--- | :--- | :--- | :--- |
| Backend Dev (Java/Spring) | 2.0 FTE | 2.0 FTE | 1.5 FTE |
| Frontend Dev (Vue/PWA) | 2.0 FTE | 1.5 FTE | 1.5 FTE |
| DevOps / Platform | 0.5 FTE | 0.5 FTE | 0.5 FTE |
| QA / Test Engineer | 0.5 FTE | 1.0 FTE | 1.0 FTE |
| Tech Lead / Arquiteto | 0.5 FTE | 0.5 FTE | 0.5 FTE |
| Product Owner | 0.5 FTE | 0.5 FTE | 0.5 FTE |
| Security / Compliance | 0.25 FTE | 0.5 FTE | 0.5 FTE |

---

## 8. Risk Register (Top 10 — MVP)

| ID | Risco | Prob. | Impacto | Mitigação | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| R-01 | Backend endpoints não entregues a tempo para frontend | Média | Alto | Mock MSW + Contract First (OpenAPI); Desenvolvimento paralelo | Tech Leads |
| R-02 | Busca Fuzzy performance (Levenshtein em 100k+ registros) | Média | Médio | Índices Trigram/pg_trgm MySQL + Redis Cache + Paginação Obrigatória | Backend Lead |
| R-03 | Regressão Linear preditiva com poucos dados históricos (início) | Alta | Médio | Threshold conservador (80%); Fallback health check regras; Coleta contínua melhora modelo | Data/Backend |
| R-04 | Multi-tenancy vazamento dados entre filiais | Baixa | Crítico | Testes Cross-tenant obrigatórios CI (TestContainers); Hibernate Filter em TODAS queries; Auditoria acesso negado | Backend Lead |
| R-05 | Refresh Token concorrência / Race Condition | Média | Alto | Token Bucket Client-side; Mutex no interceptor; Testes carga concorrente | Frontend Lead |
| R-06 | Geração PDF/QR Code lote timeout/memória | Baixa | Médio | Streaming PDF; Processamento assíncrono (CompletableFuture); Limite lote configurável | Backend Lead |
| R-07 | Contrato OpenAPI breaking changes sem versionamento | Média | Alto | Versionamento obrigatório (v1→v2); Contract Tests CI; Deprecation Policy 3 sprints | Tech Leads |
| R-08 | PWA Offline-First conflitos sincronização health checks | Média | Médio | Versionamento otimista (timestamp); Resolução último-ganha + log auditoria; UI indica pendentes | Frontend Lead |
| R-09 | LGPD Anonimização quebra auditoria/referências | Baixa | Alto | Hash preservado em Envers; Soft Delete (status INATIVO); Validação referencial antes anonimizar | Backend Lead |
| R-10 | Dependência Externa: Infra K8s / TLS / Secrets não pronta | Média | Alto | Ambiente Staging espelha Prod; IaC (Terraform/Helm) versionado; Dry-run semanal | DevOps |

---

## 9. Budget Estimate (High-Level — USD/Mês)

| Componente | MVP (3 meses) | Should (3 meses) | Could (3 meses) |
| :--- | :--- | :--- | :--- |
| **Pessoal (Time 5.75 FTE médio)** | $180,000 | $195,000 | $180,000 |
| **Infra K8s (3 nodes r6g.xlarge + MySQL RDS + Redis + CDN)** | $22,500 | $22,500 | $22,500 |
| **Observabilidade (Grafana Cloud / Datadog / Sentry)** | $3,600 | $4,500 | $4,500 |
| **Segurança (Pen Test, Certificados, SIEM)** | $5,000 | $8,000 | $10,000 |
| **Licenças/Tools (GitHub Advanced, SonarCloud, etc.)** | $2,700 | $2,700 | $2,700 |
| **Contingência (15%)** | $32,070 | $34,905 | $32,955 |
| **Total Estimado** | **$245,870** | **$267,605** | **$252,655** |

---

*Documento regenerado com base em Story Map v2.0 (domínio completo AST Java) + BRD v2.0 + NFR v2.0. Substitui versão 1.0 que continha apenas visão frontend e escopo reduzido (5 sprints vs 17 sprints planejados).*