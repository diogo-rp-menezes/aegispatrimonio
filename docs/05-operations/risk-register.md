# Risk Register — Aegis1

> **Versão:** 2.0 · **Owner:** Product Lead / Engineering Lead / Security Lead · **Última revisão:** 2025-01-15
> **Cadência de revisão:** Semanal (Sprint Review) + Mensal (Risk Committee)
> **Base:** BRD v2.0 + NFR v2.0 + System Architecture v2.0 + Security Policies v2.0 + Análise AST Java Completa
> **Metodologia:** ISO 31000 + NIST RMF + STRIDE Threat Modeling

---

## 1. Matriz de Riscos Consolidada

| ID | Categoria | Risco | Prob. | Impacto | Score | Mitigação Principal | Contingência | Dono | Status |
| :--- | :--- | :--- | :--- | :--- | :---: | :--- | :--- | :--- | :--- |
| **RISK-001** | Técnico/Arquitetura | **Busca Fuzzy Performance** — Levenshtein em 100k+ registros pode exceder P95 300ms | Média | Médio | 4 | Índices Trigram/pg_trgm MySQL + Redis Cache TTL 5min + Paginação obrigatória + Query optimization | Fallback busca exata (LIKE) se fuzzy > 500ms; Scale read replicas | Backend Lead | Aberto |
| **RISK-002** | Técnico/ML | **Preditiva Precisão Inicial** — Regressão Linear (OLS) com poucos dados históricos (< 3 health checks/ativo) gera falsos positivos/negativos | Alta | Médio | 6 | Threshold conservador (prob > 80%, horizonte < 30d); Fallback health check regras (score < 40); Coleta contínua melhora modelo; Métricas precisão no Grafana | Desabilitar ordem preditiva auto se precisão < 60% (feature flag); Alertas apenas | Data/Backend Lead | Aberto |
| **RISK-003** | Segurança/Cross-Tenant | **Vazamento Multi-tenancy** — Dados Filial A acessíveis por usuário Filial B (Hibernate Filter bypass, query nativa sem filtro) | Baixa | Crítico | 3 | **Testes Cross-tenant Obrigatórios CI** (TestContainers) — vazamento = build fail; ArchUnit test valida `@Filter` em TODAS entidades; `MultiTenancyFilter` + `TenantContextHolder` thread-safe | Isolamento imediato namespace K8s; Auditoria Envers rastreia acesso; Incident Response Runbook | Security Lead | Aberto |
| **RISK-004** | Segurança/Auth | **Refresh Token Race Condition** — Requisições concorrentes 401 → múltiplas tentativas refresh → token replay ou invalidação prematura | Média | Alto | 6 | Mutex no `authInterceptor` (Promise queue); Rotação obrigatória (invalida anterior + emite novo); `refresh_token` table com `used_at` timestamp; Blacklist Redis | Revogação massiva tokens (UPDATE `refresh_token` SET `revoked=true`); Forçar re-login global | Security Lead | Aberto |
| **RISK-005** | Negócio/Compliance | **LGPD Anonimização Quebra Auditoria** — Hard delete ou pseudonimização simples remove rastreabilidade legal (Art. 16 LGPD) | Baixa | Alto | 3 | **Anonimização + Hash Preservado em Envers** (ADR-010): PII removida estado atual, Envers imutável com valores originais + hash correlação SHA-256(salt+id)[:8] | Restore backup PITR se anonimização incorreta; DPO validação prévia | DPO/Legal | Aberto |
| **RISK-006** | Técnico/Performance | **PDF/QR Code Lote Timeout/Memória** — Geração 100+ etiquetas ou PDFs complexos excede memory/timeout | Baixa | Médio | 2 | Streaming PDF (FlyingSaucer), Processamento assíncrono (`CompletableFuture`), Limite lote configurável (default 100), Queue (Redis/RabbitMQ) v1.1 | Fallback síncrono para lotes < 20; Alertas memory/CPU | Backend Lead | Aberto |
| **RISK-007** | Segurança/Supply Chain | **Vulnerabilidade Dependência Crítica** — CVE Critical/High em dependência Maven/npm não detectada a tempo | Média | Alto | 6 | **OWASP Dep Check + Trivy + Snyk** CI (gate fail CVSS ≥ 7); Dependabot/Renovate auto-PR semanal; `mvn versions:display-plugin-updates` mensal | Isolamento dependência vulnerável (exclusão temporária); Patch manual; Upgrade forçado | DevOps/Security | Aberto |
| **RISK-008** | Operacional/Bus Factor | **Concentração Conhecimento** — Domínios críticos (Preditiva, Aegis Shield, Fuzzy Search) em 1-2 devs | Média | Alto | 6 | **Documentação Viva** (ADRs, READMEs módulo, Runbooks); Pair Programming obrigatório áreas críticas; Code Review 2+ reviewers áreas sensíveis; Onboarding estruturado 2 semanas | Cross-training sprint dedicado; Documentação vídeo/walkthrough | Engineering Lead | Aberto |
| **RISK-009** | Infra/Disponibilidade | **K8s Cluster Down / Cloud Provider Outage** — Indisponibilidade total (RTO > 30min) | Baixa | Crítico | 3 | **Multi-AZ Deployment** (3 AZs); HPA + PDB (minAvailable=2); Health Checks Liveness/Readiness; DNS Failover (Route53/Cloud DNS) + TTL 60s; Runbook DR testado mensal | Failover manual para região secundária (Terraform apply); Status page comunicação | DevOps | Aberto |
| **RISK-010** | Dados/Integridade | **Corrupção Silenciosa Dados** — Bit flip, bug MySQL, migração Flyway falha parcial, corrupção Envers | Baixa | Crítico | 3 | **Validação Integridade Semanal** (Job DOM 03:00 SHA-256 tabelas críticas vs baseline `integridade_checksum`); Envers append-only (trigger bloqueia DELETE/UPDATE); MySQL Binlog + PITR; Testes restore mensais | Alertas CRÍTICO imediato; Restore PITR point-in-time; Investigação forense | DBA/DevOps | Aberto |
| **RISK-011** | Negócio/Regulatório | **NR-10/12 Não Conformidade** — Termo Responsabilidade sem assinatura digital válida (ICP-Brasil), checklist incompleto, evidências não auditáveis | Média | Alto | 6 | **Termo PDF Assinatura Digital** (Placeholder ICP-Brasil/gov.br A1 v1.0 → Integração real v1.1); Checklist obrigatório fluxo Aprovar (BR-06); Evidências (foto/checklist/assinatura) armazenadas + hash; Auditoria Envers imutável | Controle paralelo planilha assinada até integração ICP-Brasil; Débito técnico registrado | Compliance/Legal | Aberto |
| **RISK-012** | Técnico/Migração | **Flyway Migration Conflict** — Conflitos migrações paralelas (dev/staging/prod), checksum mismatch, rollback falha | Média | Alto | 6 | Convenção nomenclatura `V{versao}__{descricao}.sql`; Revisão PR obrigatória (2 reviewers); `flyway:repair` apenas dev; Baseline produção; Testes migração CI (TestContainers) | `flyway:repair` emergencial (apenas dev); Rollback manual script `U{versao}__rollback.sql`; Restore PITR | DBA/Backend Lead | Aberto |
| **RISK-013** | Segurança/Container | **Imagem Docker Vulnerável** — Base image CVE Critical, dependência não atualizada, supply chain attack | Média | Alto | 6 | **Distroless Base** (`gcr.io/distroless/java21-debian12`); Trivy Scan CI (fail CRITICAL/HIGH); Cosign Sign + Verify; SBOM Syft; Renovate/Dependabot auto-PR; Base image update mensal | Rollback imagem anterior (`argocd app rollback`); Patch manual base image; Rebuild emergencial | DevOps/Security | Aberto |
| **RISK-014** | Negócio/Adoption | **Baixa Adoção Usuários** — Técnicos não usam PWA/Health Check, Gestores não confiam Preditiva, Admins não configuram RBAC | Média | Médio | 4 | **UX Research** (entrevistas, usabilidade); Treinamento hands-on (2h/filial); Champions por filial; Métricas adoção (DAU/MAU, feature usage); Feedback loop semanal | Treinamento extra; Ajustes UX baseados em dados; Suporte dedicado Slack/Teams | Product Lead | Aberto |
| **RISK-015** | Técnico/Integração | **Backend Externo (Legado) Incompatível** — API legada não entrega contratos esperados, latência alta, instabilidade | Baixa | Alto | 3 | **Contrato OpenAPI 3.1 First**; Mock Server (MSW) desenvolvimento paralelo; Contract Tests (Pact) validação; Circuit Breaker (Resilience4j) + Fallback cache | Modo "Mock Only" para demos/treinamento; Priorização endpoints críticos; SLA contratual com time backend | Backend Lead | Aberto |

---

## 2. Critérios de Priorização (ISO 31000)

| Probabilidade | Definição | Score |
| :--- | :--- | :---: |
| **Alta** | > 60% chance de ocorrer no próximo ano | 3 |
| **Média** | 30-60% chance | 2 |
| **Baixa** | < 30% chance | 1 |

| Impacto | Definição | Score |
| :--- | :--- | :---: |
| **Crítico** | Bloqueia release, vazamento dado sensível, perda financeira > R$ 100k, não conformidade legal grave, indisponibilidade total | 3 |
| **Alto** | Degradação severa experiência, performance > 50% abaixo SLA, regra negócio violada, segurança comprometida | 2 |
| **Médio** | Degradação moderada, workaround existe, impacto operacional controlado | 2 |
| **Baixo** | Cosmético, documentação, melhoria, tech debt não bloqueante | 1 |

**Score = Probabilidade × Impacto** (1-9)
- **Score ≥ 6:** Plano de mitigação ativo + Owner + Revisão semanal
- **Score 4-5:** Mitigação planejada + Revisão mensal
- **Score ≤ 3:** Monitoramento passivo + Revisão trimestral

---

## 3. Riscos por Categoria (Resumo)

### Técnicos (RISK-001, 002, 006, 012, 015)
| Risco | Mitigação Chave | Status |
| :--- | :--- | :--- |
| Busca Fuzzy Performance | Trigram Index + Redis Cache + Fallback | Aberto |
| Preditiva Precisão Inicial | Threshold Conservador + Fallback Regras | Aberto |
| PDF/QR Lote Timeout | Streaming + Async + Queue v1.1 | Aberto |
| Flyway Conflict | Convenção + PR Review + Baseline | Aberto |
| Backend Legado Incompatível | Contract First + Mock + Circuit Breaker | Aberto |

### Segurança (RISK-003, 004, 007, 013)
| Risco | Mitigação Chave | Status |
| :--- | :--- | :--- |
| Cross-Tenant Leak | Testes CI Obrigatórios + ArchUnit + Hibernate Filter | Aberto |
| Refresh Token Race | Mutex + Rotação + Blacklist Redis | Aberto |
| Dep Vulnerável | Dep Check + Trivy + Dependabot + Gate CI | Aberto |
| Container Vuln | Distroless + Trivy + Cosign + SBOM | Aberto |

### Negócio/Compliance (RISK-005, 011, 014)
| Risco | Mitigação Chave | Status |
| :--- | :--- | :--- |
| LGPD Anonimização | Hash Envers Preservado + DPO Validação | Aberto |
| NR-10/12 Conformidade | Termo Assinatura Digital + Checklist Obrigatório | Aberto |
| Baixa Adoção | UX Research + Champions + Métricas + Treinamento | Aberto |

### Operacional/Infra (RISK-008, 009, 010)
| Risco | Mitigação Chave | Status |
| :--- | :--- | :--- |
| Bus Factor | Docs Vivas + Pair Programming + Cross-training | Aberto |
| K8s/Cloud Outage | Multi-AZ + HPA/PDB + DNS Failover + Runbook DR | Aberto |
| Corrupção Dados | Integridade SHA-256 Semanal + Envers + PITR | Aberto |

---

## 4. Riscos Resolvidos (Histórico)

| ID | Risco Original | Resolução | Data | Commit/PR |
| :--- | :--- | :--- | :--- | :--- |
| **RISK-OLD-001** | `request` complexidade 13 (api.js) | Refatorado em 4 módulos (retry, timeout, parsing, auth) — ciclomática ≤ 10 | 2025-01-17 | `refactor(api): extract retry/timeout/parsing` |
| **RISK-OLD-002** | 3 `console.*` residuais (api.js:26,49,52) | Removidos + ESLint `no-console: error` CI gate | 2025-01-17 | `fix(api): remove console.*` |
| **RISK-OLD-003** | JWT `localStorage` vulnerável XSS | Migração HttpOnly Cookie + Refresh Rotation + RS256 | 2025-01-17 | `security(auth): jwt rs256 + refresh rotation` |
| **RISK-OLD-004** | Backend externo não entrega endpoints | Backend próprio Spring Boot 3.3 implementado | 2025-01-10 | `feat(backend): spring boot 3.3 modular monolith` |
| **RISK-OLD-005** | Busca exata sem fuzzy | Fuzzy Search Levenshtein + Trigram + Redis Cache | 2025-01-31 | `feat(busca): fuzzy search levenshtein` |
| **RISK-OLD-006** | Sem auditoria / LGPD | Hibernate Envers 100% + Anonimização Hash Preservado | 2025-01-17 | `feat(auditoria): envers 100% + lgpd anonimização` |
| **RISK-OLD-007** | Sem multi-tenancy real | Hibernate Filter + MultiTenancyFilter + Testes CI | 2025-01-17 | `feat(multi-tenancy): hibernate filter` |
| **RISK-OLD-008** | Sem RBAC granular | Aegis Shield (Role×Permission×Contexto) | 2025-01-17 | `feat(seguranca): aegis shield rbac` |
| **RISK-OLD-009** | Sem preditiva | Health Check + Regressão Linear OLS + Ordem Auto | 2025-02-14 | `feat(preditiva): health check + regressao linear` |
| **RISK-OLD-010** | Deploy apenas CDN estático | Full Stack K8s + ArgoCD GitOps + Blue-Green | 2025-01-10 | `feat(infra): k8s + argocd gitops` |

---

## 5. Monitoramento de Riscos (KPIs)

| KPI | Target | Fonte | Frequência |
| :--- | :--- | :--- | :--- |
| **Riscos Abertos Score ≥ 6** | 0 (todos mitigados) | Risk Register | Semanal |
| **Riscos Críticos (Score 9) Abertos** | 0 | Risk Register | Diário |
| **Tempo Médio Mitigação (MTTM)** | < 2 semanas (Score ≥ 6) | Jira/Risk Register | Mensal |
| **Riscos Reabertos** | 0 | Risk Register | Mensal |
| **Incidentes Relacionados a Risco Conhecido** | 0 | Incident Tracker | Mensal |
| **Cobertura Testes Cross-Tenant** | 100% combinações | CI Reports | Every PR |
| **Vulnerabilidades Critical/High Abertas** | 0 | GitHub Security / Trivy | Diário |
| **Drift Risk Register vs Incidentes** | 0% (todos incidentes mapeados) | Incident Tracker vs Risk Register | Mensal |

---

## 6. Governança de Riscos

| Fórum | Frequência | Participantes | Pauta |
| :--- | :--- | :--- | :--- |
| **Sprint Risk Review** | Semanal (Sprint Review) | PO, Tech Leads, QA, Security | Revisão riscos Score ≥ 6, novos riscos sprint, eficácia mitigações |
| **Monthly Risk Committee** | Mensal | Product Lead, Engineering Lead, Security Lead, DevOps, DPO, Compliance | Revisão completa Risk Register, novos riscos estratégicos, orçamento mitigação, compliance |
| **Quarterly Board Review** | Trimestral | Sponsor, CTO, CISO, Legal | Riscos estratégicos (Score 9), postura risco, orçamento, compliance regulatório |
| **Incident Post-Mortem** | Pós-incidente P0/P1 | Envolvidos + Tech Leads + Security | Root cause, atualização Risk Register, melhoria processos, runbook update |

---

## 7. Rastreabilidade Riscos ↔ Artefatos

| Risco | BRD | NFR | System Arch | Security Policies | Test Strategy | Runbook | Código |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| RISK-001 | BR-11,12 | NFR-P02, S03 | §9.3 | §5.1 | §5.1 (Performance) | — | `FuzzySearchService`, `LevenshteinDistance` |
| RISK-002 | BR-10 | NFR-P03, C04 | §9.2 | §5.2 | §5.1 (Performance) | `runbooks/predictive-alert.md` | `ManutencaoPreditivaService`, `HealthCheckService` |
| RISK-003 | BR-13 | NFR-SEC04, S03 | §7.3 | §2.1, §5.2 | §6 (Cross-tenant) | `runbooks/cross-tenant-leak.md` | `MultiTenancyFilter`, `@Filter` entidades |
| RISK-004 | BR-15 | NFR-SEC02, SEC03 | §7.1 | §4.1 | §6 (Security) | `runbooks/token-replay.md` | `JwtTokenProvider`, `authInterceptor` |
| RISK-005 | — | NFR-C01, C02 | §7.3 (LGPD) | §3.3 | §6 (LGPD) | — | `LgpdService`, `CustomRevisionListener` |
| RISK-006 | BR-17,18 | NFR-P04 | §9.1 | — | §6 (Performance) | — | `PdfGenerator`, `QRCodeGenerator` |
| RISK-007 | — | NFR-SEC10, M03 | §12 (Supply Chain) | §5.3 | §6 (Security) | `runbooks/container-vuln.md` | `pom.xml`, `package.json`, `Dockerfile*` |
| RISK-008 | — | NFR-M01, M04 | §5 (Architecture) | — | §6 (Quality) | — | `ADRs`, `READMEs`, `CODEOWNERS` |
| RISK-009 | — | NFR-A01, A02 | §10 (Deployment) | §6 (Infra) | §6 (Chaos) | `runbooks/dr-failover.md` | `k8s/`, `terraform/` |
| RISK-010 | BR-16 | NFR-C02, C04 | §6 (Audit) | §3.3 | §6 (Integridade) | `runbooks/audit-tamper.md` | `Envers`, `IntegridadeChecksumService` |
| RISK-011 | BR-17 | NFR-C03 | §9.1 | §5.1 | §6 (Compliance) | — | `PdfGenerator`, `TermoResponsabilidade` |
| RISK-012 | — | NFR-A06 | §6 (Data) | — | §6 (Integration) | `runbooks/flyway-repair.md` | `Flyway`, `V*__*.sql` |
| RISK-013 | — | NFR-SEC10 | §12 (Supply Chain) | §6 (Container) | §6 (Security) | `runbooks/container-vuln.md` | `Dockerfile*`, `trivy`, `cosign` |
| RISK-014 | — | NFR-U01, U02 | §4 (Frontend) | — | §6 (E2E/A11y) | — | `frontend/`, `cypress/`, `axe-core` |
| RISK-015 | — | NFR-S02 | §3 (High-Level) | — | §6 (Contract) | — | `OpenAPI`, `MSW`, `Resilience4j` |

---

*Documento regenerado completamente com base em BRD v2.0 + NFR v2.0 + System Architecture v2.0 + Security Policies v2.0 + Test Strategy v2.0 + Análise AST Java Completa. Substitui versão 1.0 que continha apenas 12 riscos frontend vanilla JS + API externa.*