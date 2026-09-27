# Test Strategy — Aegis Patrimônio

> **Versão:** 1.0 · **Owner:** QA Lead / Eng Lead · **Status:** Draft

## 1. Objectives & Quality Goals
* Garantir zero regressões críticas (P0) em produção — cobertura de fluxos de auditoria, RBAC e health checks
* Reduzir MTTD (Mean Time To Detect) para < 2 horas via testes automatizados em CI/CD e monitoramento de métricas de qualidade
* Validar conformidade com BR-02 (auditoria WORM imutável), BR-01 (RBAC estrito) e NFRs de latência (P95) antes de cada release
* Eliminar bugs bloqueantes conhecidos (RISK-03: `setUsername` stub, RISK-02: `AuditLog` ausente, RISK-16: `AlertNotificationService` complexidade 17) antes de homologação

## 2. Test Pyramid
| Camada | Cobertura Mínima | Ferramenta | Frequência de Execução | Responsável |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | 80% global; 95% em regras de negócio (services, mappers, specifications, evaluators) | JUnit 5 + Mockito (backend), Vitest (frontend) | A cada commit (CI) | Dev |
| **Integration Tests** | 100% dos fluxos críticos listados nas US (cadastro ativo, manutenção, health check, auth, auditoria) | Testcontainers (PostgreSQL assumido para testes), SpringBootTest, MockMvc | A cada PR (pipeline obrigatório) | Dev |
| **E2E Tests** | Jornadas principais: onboarding funcionário+usuário, solicitação→aprovação→conclusão manutenção, health check→alerta→manutenção, dashboard executivo | Playwright (JavaScript) | A cada deploy para staging; nightly em staging | QA |
| **Contract Tests** | Todos os endpoints públicos OpenAPI (30+ endpoints mapeados nas US) | Pact (provider: Spring, consumer: frontend) | A cada release candidate | Dev |
| **Performance Tests** | Cenários NFR: login P95 ≤ 800ms, listagem ativos P95 ≤ 300ms, health check throughput 11 req/s, job alertas 10k ativos < 2 min | k6 (scripts versionados) | Semanal em staging; obrigatório antes de release major | QA/Dev |
| **Security Tests** | SAST no pipeline (Semgrep rules Java/JS), DAST em staging (OWASP ZAP), validação RBAC (403/401), tokens JWT RS256 | Semgrep, OWASP ZAP, testes de integração RBAC | SAST: a cada commit; DAST: semanal; RBAC: a cada PR | Sec/Dev |

## 3. Test Environments
| Ambiente | Propósito | Dados | Acesso |
| :--- | :--- | :--- | :--- |
| **Local** | Desenvolvimento e debug | Seed `RealisticDataSeeder` (refatorado US-TECH-007) + mocks | Dev |
| **CI (GitHub Actions/GitLab CI)** | Validação automatizada unit/integration/contract | Banco efêmero Testcontainers (PostgreSQL 15), schema Flyway/Liquibase | Pipeline |
| **Staging** | Homologação pré-produção, E2E, performance, security | Dados anonimizados (LGPD) — dump produção sanitizado + seed controlado | QA, PO, Dev |
| **Produção** | Smoke tests pós-deploy, canary metrics | Real (somente leitura para testes) | Automação monitorada |

> **Nota:** O diagnóstico não identificou motor de banco nas dependências (`package.json` sem driver JDBC). Assume-se PostgreSQL para testes via Testcontainers por ser padrão em projetos Spring Boot — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. Confirmar com arquitetura o banco alvo (PostgreSQL, Oracle, SQL Server) e ajustar Testcontainers module conforme.

## 4. Coverage Targets
* **Mínimo global:** 80% (linhas, branches, métodos) — gate no SonarQube/JaCoCo
* **Crítico (regras de negócio):** 95% em:
  - `AtivoService`, `AtivoMapper` (US-ATIVO-001 a 005, US-TECH-001)
  - `ManutencaoService`, `ManutencaoSpecification` (US-MAN-001 a 005, US-TECH-002)
  - `AlertNotificationService` + evaluators (US-MON-001, 004, US-TECH-003)
  - `AuthService`, `JwtProvider`, `RefreshTokenRepository` (US-ATIVO-014)
  - `AuditLogService`, `AuditInterceptor`, `AuditAspect` (US-AUD-001)
  - `UsuarioService`, `Usuario.setUsername` (US-ATIVO-011, 013, US-TECH-004)
* **Exclusões justificadas:** DTOs/records anêmicos, configurações (YAML/Properties), código gerado (OpenAPI), `RealisticDataSeeder` (apenas testes), `mockLogin` (profile `test`)

## 5. Non-Functional Testing
### Performance
| Cenário | Ferramenta | Critério de Aceite | Origem |
| :--- | :--- | :--- | :--- |
| Login (`POST /api/auth/login`) | k6 | P95 ≤ 800ms, 100 VUs simultâneos | US-ATIVO-014, NFR-20 |
| Listagem ativos (`GET /api/ativos`) | k6 | P95 ≤ 300ms (página 1, cache quente), 10k ativos | US-ATIVO-002, NFR-04 |
| Detalhe ativo (`GET /api/ativos/{id}`) | k6 | P95 ≤ 200ms | US-ATIVO-003, NFR-06 |
| Health check ingest (`POST /api/ativos/{id}/health-check`) | k6 | Throughput 11 req/s sustentado, P95 ≤ 1s | US-MON-001, NFR-32, NFR-33 |
| Job verificação alertas (10k ativos) | k6 | Execução completa < 2 min, taxa erro 5xx < 0.1% | US-MON-004, NFR-39, NFR-40 |
| Exportação CSV 50k registros | k6 | < 10s streaming | US-ATIVO-002, NFR-05 |
| Dashboard executivo | k6 | P95 ≤ 1s | US-REL-002, NFR-44 |

### Segurança
* **SAST:** Semgrep com ruleset Java (Spring Security, SQLi, XSS, path traversal) + JavaScript (prototype pollution, unsafe eval) — executado no pipeline a cada commit
* **DAST:** OWASP ZAP scan autenticado em staging (credenciais de teste) — varredura semanal + antes de release
* **RBAC Validation:** Testes de integração cobrindo matriz de permissões (ADMIN, GESTOR_PATRIMONIO, ANALISTA_MANUTENCAO, USER, AUDITOR, HEALTH_COLLECTOR) — 403/401 esperados por endpoint
* **JWT:** Validação RS256, expiração access token (8h), refresh rotation sem race condition, invalidação no logout/desativação
* **AuditLog Imutabilidade:** Teste de tentativa `UPDATE/DELETE` em `audit_log` deve falhar (grants restritos) — US-AUD-001

### Acessibilidade
* **Ferramenta:** axe-core integrado nos testes E2E (Playwright) + Storybook a11y addon
* **Critério:** WCAG 2.1 AA em todas as telas de formulário (cadastro ativo, manutenção, login, usuários) e tabelas (listagens, auditoria, alertas)
* **Validação:** Automatizada no pipeline E2E; revisão manual em staging para fluxos complexos (wizard conclusão manutenção, modal baixa ativo 2 etapas)

### Resiliência / Chaos
* **Cenários validados em staging (mensal):**
  - Falha de conexão DB durante `AtivoRepository.save` → 500 + rollback + alerta infra (US-ATIVO-001 EX-5)
  - Storage WORM indisponível ao auditar → transação principal commitada, alerta CRITICAL disparado (US-ATIVO-001 EX-6, US-ATIVO-004 EX-5)
  - `AlertNotificationService` falha parcial → health check persistido, erro logado, job reprocessamento (US-MON-001 EX-5)
  - SMTP down ao notificar → `NotificationOutbox` enfileira, retry exponencial worker (US-MON-004 EX-4)
  - Refresh token race condition → 10 requests paralelos com token expirado → 1 refresh, 9 reutilizam (US-ATIVO-014 EX-4)

## 6. Test Data Management
* **Estratégia de geração:**
  - **Unit/Integration:** Builders/Factories (pattern Builder) para `Ativo`, `Manutencao`, `Usuario`, `Filial`, `TipoAtivo`, `HealthCheck`, `Alerta` — refatorar `RealisticDataSeeder` (US-TECH-007) para servir como factory library
  - **E2E/Staging:** Dump anonimizado de produção (scripts de sanitização: CPF/CNPJ/email/username mascarados, valores financeiros perturbados ±10%, IPs anonimizados) + seed controlado para cenários específicos (ex: ativo com health check crítico, manutenção em cada status)
* **Dados sensíveis:** **Obrigatório** anonimização/mascaramento fora de produção. Nenhum dado real de CPF, CNPJ, email corporativo, token JWT, senha (mesmo hash) em CI/staging. Scripts de sanitização versionados e auditados.

## 7. Regression Strategy
* **Suite de regressão automatizada:**
  - **Nível 1 (CI - obrigatório a cada PR):** Unit + Integration + Contract tests (~15 min)
  - **Nível 2 (Staging deploy - obrigatório):** E2E jornadas críticas + Performance smoke (subset k6) + Acessibilidade (~30 min)
  - **Nível 3 (Semanal/Release):** Performance suite completa + DAST + Chaos scenarios (~2h)
* **Critérios de entrada para release (Definition of Release Ready):**
  - 0 bugs P0/P1 abertos
  - Pipeline Nível 1 e 2 verdes
  - Cobertura ≥ targets (Seção 4)
  - Performance benchmarks dentro do NFR (Seção 5)
  - `AuditLog` implementado e testado (RISK-02 resolvido) — **bloqueante**
  - `setUsername` corrigido e testado (RISK-03 resolvido) — **bloqueante**
  - `AlertNotificationService` refatorado (RISK-016 resolvido) — **bloqueante para Q2**
* **Critérios de saída (pós-deploy produção):**
  - Smoke tests: health endpoint, login, listagem ativos, health check ingest, dashboard — todos verdes em < 10 min
  - Métricas de erro 5xx < 0.1% nos primeiros 30 min (guardrail BRD)

## 8. Bug Triage & Severity
| Severidade | Definição | SLA de Correção | Exemplos no Projeto |
| :--- | :--- | :--- | :--- |
| **Crítica (P0)** | Sistema fora do ar, perda de dados, vazamento segurança, não conformidade legal (LGPD/SOX), bug bloqueante conhecido | Imediato (hotfix ou rollback < 1h) | `AuditLog` ausente (RISK-02), `setUsername` stub (RISK-03), taxa erro 5xx > 0.1% (RISK-16), token JWT não invalida no logout (RISK-09) |
| **Alta (P1)** | Funcionalidade principal quebrada (CRUD ativos, manutenção, auth, health check, alertas), performance fora do NFR, RBAC bypass | 24h (sprint atual) | Mapper complexidade causa bug mapeamento (RISK-01), Specification gera query ineficiente (RISK-04), lazy loading exception (RISK-05), soft/hard delete inconsistente (RISK-06) |
| **Média (P2)** | Funcionalidade secundária afetada (relatórios, dashboard, export, filtros salvos), UX degradada, tech debt não bloqueante | Próxima sprint | Console.* em produção (RISK-11), request complexidade (RISK-10), seeder quality gate (RISK-30), depreciação linear não validada (RISK-14/23) |
| **Baixa (P3)** | Cosmético, melhorias, documentação, refatoração não urgente | Backlog (priorizado por ROI) | Popper.js tooltips styling, cache TTL ajustes, logs estruturados frontend |

## 9. Reporting & Metrics
* **Dashboards (Grafana/Datadog):**
  - Cobertura por módulo (JaCoCo + Vitest) — meta 80%/95%
  - Taxa de falha de build (CI) — target < 5%
  - Flakiness rate (testes instáveis) — target < 1%
  - Performance trends (P95, throughput) por endpoint crítico
  - Bug escape rate (produção vs staging) — target < 2%
  - MTTR (Mean Time To Resolve) por severidade
  - Defect density por componente (mapper, specification, alert service, auth)
* **Métricas de qualidade acompanhadas semanalmente:**
  - **Escape Rate:** Bugs encontrados em produção / Total bugs (meta < 5%)
  - **MTTR:** P0 < 4h, P1 < 24h, P2 < 5 dias
  - **Defect Density:** Bugs/KLOC por módulo (foco: `AtivoMapper`, `ManutencaoSpecification`, `AlertNotificationService`)
  - **Technical Debt Ratio:** SonarQube (meta < 5%)
  - **NFR Compliance:** % endpoints dentro do P95 alvo

## 10. Roles & Responsibilities
| Papel | Responsabilidade |
| :--- | :--- |
| **Dev (Backend)** | Testes unitários (services, mappers, specifications, evaluators), integração (MockMvc + Testcontainers), contract tests (Pact provider), correção bugs P0/P1, refatoração tech debt (US-TECH-001 a 007) |
| **Dev (Frontend)** | Testes unitários (Vitest: hooks, utils, components), integração (MSW mocks), E2E (Playwright: jornadas), acessibilidade (axe-core), remoção console.* (US-TECH-005), refatoração `request` (US-TECH-006) |
| **QA Lead** | Estratégia de teste, planejamento E2E, gestão dados de teste staging, triagem bugs, validação performance/security, relatórios qualidade, gate release |
| **Eng Lead** | Quality gate em releases (aprovação Definition of Release Ready), priorização tech debt, alinhamento NFR com arquitetura, decisões bloqueantes (RISK-02, 03, 09, 16) |
| **Sec/DevSecOps** | SAST/DAST pipeline, regras Semgrep, ZAP scans, validação RBAC/JWT, auditoria grants `audit_log`, rotação chaves RSA (90 dias) |
| **DBA/Infra** | Índices compostos validação (CI `EXPLAIN ANALYZE`), particionamento `audit_log`/`ativo_detalhe_hardware`, tiering cold storage, Testcontainers config, staging data refresh |

## 11. Traceability Matrix (Resumo)
| User Story / Tech Debt | Test Types | Critical Path | Blocking Risks |
| :--- | :--- | :--- | :--- |
| US-ATIVO-001 a 005 (CRUD Ativos) | Unit, Integration, E2E, Contract | Sim | RISK-01, RISK-02, RISK-05, RISK-06 |
| US-ATIVO-011, 013 (Funcionário/Usuário) | Unit, Integration, E2E | Sim | **RISK-03 (BLOQUEANTE)**, RISK-02, RISK-09 |
| US-ATIVO-014 (Auth) | Unit, Integration, E2E, Contract, Performance, Security | Sim | RISK-09, RISK-10, RISK-11 |
| US-MAN-001 a 005 (Manutenção) | Unit, Integration, E2E, Contract | Sim | RISK-02, RISK-04, RISK-12, RISK-13 |
| US-MON-001, 004 (Health Check + Job Alertas) | Unit, Integration, Performance, Chaos | Sim | **RISK-16 (CRÍTICO)**, RISK-17, RISK-18, RISK-20, RISK-21, RISK-22 |
| US-MON-002, 003 (Histórico + Alertas UI) | Unit, Integration, E2E, Acessibilidade | Não | RISK-15, RISK-19 |
| US-REL-001, 002 (Relatórios + Dashboard) | Unit, Integration, E2E, Performance | Não | RISK-14, RISK-23, RISK-24, RISK-25, RISK-26 |
| US-AUD-001 (Auditoria) | Unit, Integration, E2E, Security, Performance | Sim | **RISK-02 (BLOQUEANTE)**, RISK-27, RISK-28, RISK-29 |
| US-TECH-001 a 007 (Tech Debt) | Unit, Integration (regressão) | Sim (001, 002, 003, 004) | RISK-01, RISK-04, RISK-16, RISK-03, RISK-11, RISK-10, RISK-30 |

## 12. Open Issues Requerendo Decisão Humana (Impactam Testes)
| Item | Descrição | Impacto no Teste | Decisão Necessária |
| :--- | :--- | :--- | :--- |
| Banco de dados alvo | Diagnóstico não encontrou driver JDBC | Testcontainers module, CI schema, performance baselines | Confirmar: PostgreSQL, Oracle, SQL Server? |
| Soft vs Hard delete ativo | LGPD vs auditoria (RISK-06) | Cenários de teste US-ATIVO-005, US-AUD-001 | Definir com Compliance/Arquitetura |
| Filtro implícito por escopo (filial/departamento) | USER vê apenas sua filial? (RISK-15) | Testes RBAC US-ATIVO-002, US-MAN-005, US-MON-003 | Definir com PO |
| Matriz de permissões por role | Roles/permissões não definidas (RISK-08) | Testes RBAC todos os endpoints | Workshop stakeholders |
| Estratégia invalidação token | Blacklist Redis vs versioning (RISK-09) | Testes logout, desativação, segurança | Definir com Arquitetura/Segurança |
| Alçadas aprovação manutenção | Valores por filial/valor/tipo (RISK-12) | Testes US-MAN-001, 002 | Workshop Gestão/Financeiro |
| Método depreciação | Linear vs fiscal (RISK-14, 23) | Testes US-REL-001 (TCO) | Validar com Financeiro |
| Permissão `relatorio:custo-total` | Roles exatas (RISK-24) | Testes RBAC US-REL-001 | Definir roles |
| Hash encadeado vs assinatura digital | Integridade `audit_log` (RISK-27) | Testes segurança US-AUD-001 | Avaliar com Segurança |
| Sampling health checks auditoria | Volume alto (RISK-29) | Testes US-AUD-001, US-MON-001 | Definir política |
| Role `HEALTH_COLLECTOR` | RBAC granular endpoint health check (RISK-18) | Testes auth US-MON-001 | Definir role + permissões |
| Particionamento `ativo_detalhe_hardware` | Por data? por ativo? (RISK-17) | Testes performance US-MON-001, 002 | Decidir com DBA |
| Retenção health checks | 13 meses — job purge? (RISK-19) | Testes US-MON-002, US-AUD-001 | Definir política + job |
| Canais notificação por severidade | Email CRITICA, push ALTA, log BAIXA (RISK-21) | Testes US-MON-001, 003, 004 | Definir com PO |
| Permissão dashboard por filial | Gestor vê apenas sua filial? (RISK-26) | Testes US-REL-002 | Definir com PO |

---
*Documento gerado a partir de diagnóstico determinístico do codebase (348 arquivos, 1257 funções, 342 classes) e 30 User Stories BDD com critérios de aceite, rastreabilidade NFR/UC/Risk. Tecnologias restritas ao stack verificado: Java (backend), JavaScript (frontend), @popperjs/core. Nenhuma tecnologia externa assumida sem validação.*