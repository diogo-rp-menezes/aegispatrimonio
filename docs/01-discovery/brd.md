# Business Requirements Document (BRD) — Aegis1

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Product Lead · **Última atualização:** 2025-01-15
> **Stakeholders:** Sponsor, Product, Engenharia Backend, Engenharia Frontend, Design, Operações de Manutenção, Compliance, Segurança da Informação

---

## 1. Executive Summary & Vision

O **Aegis1** é um sistema completo de gestão patrimonial e de manutenção de ativos que centraliza o ciclo de vida de ativos (hardware, software, mobiliário, veículos) e ordens de serviço — da criação à conclusão — fornecendo visibilidade de custos por ativo, manutenção preditiva baseada em dados de hardware (SMART, temperatura, saúde de disco) e controle de acesso granular multi-tenant por filial.

**Problema atual:** Fragmentação de solicitações em planilhas, e-mails e controles manuais; ausência de rastreabilidade de estado (aberto, em andamento, aprovado, concluído, cancelado); custo acumulado por ativo desconhecido; falta de visão preditiva de falhas de hardware; controle de acesso grosseiro (sem multi-tenancy por filial).

**Visão:** Plataforma única, acessível via navegador (PWA responsivo) e futura app mobile nativa, que padroniza o fluxo **criar → iniciar → aprovar → concluir/cancelar**, expõe o indicador **custoTotalPorAtivo** para decisões de substituição/plano preventivo, implementa **manutenção preditiva** via regressão linear (mínimos quadrados) sobre métricas de disco (SMART), e oferece **busca fuzzy** (Levenshtein) tolerante a erros de digitação. Controle de acesso **Aegis Shield** com RBAC hierárquico, contextual e multi-tenancy por filial. Auditoria completa via Hibernate Envers.

---

## 2. Problem Statement

* **Problema central:** Organizações operam sem sistema unificado de gestão patrimonial e manutenção. Solicitações chegam por canais informais, não há rastreamento de estado padronizado, custo total por ativo (TCO) é desconhecido, falhas de hardware não são previstas, e controle de acesso não isola dados por filial/unidade de negócio.
* **Evidências técnicas:** Codebase completo (backend Java 21/Spring Boot 3.3 + frontend Vue 3) revela domínio rico: entidades de ativo com detalhes de hardware (CPU, memória, disco, rede), hierarquia filial→departamento→localização, fornecedores, funcionários, tipos de ativo, ordens de manutenção (corretiva, preventiva, preditiva), usuários com roles/permissions granulares. Backend implementa busca fuzzy nativa (Levenshtein), manutenção preditiva (regressão linear), RBAC Aegis Shield, multi-tenancy por filial, auditoria Envers, geração de QR Code/PDF.
* **Custo de não agir (Cost of Inaction):** Decisões reativas contínuas; impossibilidade de calcular TCO por ativo; paradas não planejadas por falhas de disco não previstas; risco de não conformidade (NR-10, NR-12, LGPD) por falta de rastreabilidade e auditoria; vazamento de dados entre filiais por ausência de multi-tenancy.

---

## 3. Target Audience & Personas

> **Personas validadas com base no domínio real extraído via AST Java + frontend**

| Persona | Perfil | Necessidade Principal | Ganho Esperado |
| :--- | :--- | :--- | :--- |
| **Gestor de Manutenção / Patrimônio** | Responsável por planejamento, KPIs, orçamento, auditoria | Visão consolidada de `custoTotalPorAtivo`, health checks preditivos, status de ordens, relatórios de auditoria | Decisão baseada em dados; redução de paradas não planejadas; conformidade auditável |
| **Técnico de Campo** | Executa serviços, registra início/fim, coleta dados de hardware | Interface simples para **iniciar** e **concluir** ordens; leitura de QR Code; registro de métricas SMART | Eliminação de papel; registro automático de tempo, materiais e saúde do ativo |
| **Aprovador/Supervisor** | Valida execução e autoriza fechamento | Ação **aprovar** com evidências (checklists, fotos, assinatura digital) | Conformidade auditável; redução de retrabalho |
| **Administrador de Cadastros (por Filial)** | Mantém departamentos, filiais, fornecedores, funcionários, tipos de ativo | CRUD completo com isolamento por filial (multi-tenancy) | Dados mestres consistentes; segregação de dados entre unidades |
| **Administrador Global (Aegis Shield)** | Gerencia roles, permissions, usuários, configurações de segurança | RBAC granular hierárquico; atribuição de permissões contextuais; auditoria de acessos | Princípio do menor privilégio; conformidade LGPD; trilha de auditoria imutável |
| **Analista de Segurança/Compliance** | Monitora acessos, auditoria, LGPD | Acesso a logs de auditoria (Envers); relatórios de acesso; anonimização | Rastreabilidade total; evidências para auditoria externa |

---

## 4. Core Objectives & Success Metrics

| Objetivo | KPI (Métrica) | Baseline Atual | Meta | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| Padronizar fluxo de ordens de manutenção | % de ordens tramitando 100% no sistema (sem canais paralelos) | 0% (sistema novo) | ≥ 90% | Q2/2025 |
| Visibilidade de custo por ativo (TCO) | % de ativos com `custoTotalPorAtivo` calculado automaticamente | 0% | 100% dos ativos ativos | Q2/2025 |
| Manutenção preditiva de disco | % de ativos com health check preditivo ativo | 0% | ≥ 80% ativos com disco | Q3/2025 |
| Busca inteligente (fuzzy) | Taxa de sucesso de busca com erros de digitação | N/A | ≥ 95% relevância | Q2/2025 |
| Controle de acesso granular multi-tenant | % de operações com autorização Aegis Shield | 0% | 100% | Q2/2025 |
| Auditoria completa | % de entidades auditadas (Envers) | 0% | 100% entidades de domínio | Q2/2025 |
| Reduzir tempo médio de aprovação | Lead time (iniciar → aprovar) em horas | Desconhecido | ≤ 24 h | Q3/2025 |
| Eliminar console logs em produção | Quantidade de chamadas `console.*` no bundle | 3 (diagnóstico) | 0 | Antes do go-live |

* **North Star Metric:** **Ordens concluídas no prazo SLA / Total de ordens** — reflete eficiência ponta-a-ponta.
* **Guardrail Metrics:**
  - Taxa de erro de integração API (`handleApiError`) < 1%
  - Latência P95 da chamada `request` < 800 ms
  - Zero vazamento de dados sensíveis no frontend (`authInterceptor`)
  - Cobertura de testes ≥ 80% (gate de CI)
  - Zero vulnerabilidades críticas/altas em dependências (OWASP/Dependabot)

---

## 5. Scope Boundaries

### In-Scope (Backend + Frontend)

**Gestão de Ativos (Core Domain)**
- CRUD completo de **Ativos** com depreciação, ciclo de vida, alocação
- **AtivoDetalheHardware**: especificações técnicas (CPU, memória, discos, adaptadores de rede)
- Hierarquia **Filial → Departamento → Localização** (multi-tenancy nativo)
- **Fornecedores** (cadastro, contratos, SLA, avaliação)
- **Funcionários** (vinculação a ativos, alertas, responsabilidades)
- **Tipos de Ativo** (classificação: Notebook, Servidor, Impressora, Veículo, Móvel, Software, etc.)

**Manutenção (Work Orders)**
- **SolicitacaoManutencao**: ciclo de vida completo (criar → iniciar → aprovar → concluir/cancelar)
- **ManutencaoPreventiva**: agendamento recorrente (cron, frequência, checklists)
- **ManutencaoPreditiva**: health check contínuo (SMART, temperatura, horas de uso) + regressão linear para prever falha de disco
- Cálculo automático de `custoTotalPorAtivo` (mão de obra + material + terceiros)

**Busca & Relatórios**
- **Busca Fuzzy** (Levenshtein) em ativos, ordens, funcionários, fornecedores
- **Relatórios**: Termos de Responsabilidade (PDF), Etiquetas QR Code, Dashboards analíticos com drill-down
- Alertas em tempo real (health check crítico, SLA vencendo, aprovação pendente)

**Segurança & Auditoria (Aegis Shield)**
- **Auth**: JWT stateless, refresh token, `authInterceptor` frontend
- **RBAC Granular**: Roles (ADMIN, GESTOR, TECNICO, USER, AUDITOR), Permissions granulares por recurso/ação/contexto
- **Multi-tenancy por Filial**: Isolamento de dados no nível de query (MultiTenancyFilter)
- **Auditoria Imutável**: Hibernate Envers em todas as entidades de domínio

**Infra & Qualidade**
- API base path: `/api/v1`, documentação OpenAPI/Swagger
- Flyway migrations versionadas
- TestContainers (MySQL real) para testes de integração
- CI/CD: build, test, security scan, deploy (K8s manifests + Docker Compose)

### Out-of-Scope
- App mobile nativo (roadmap futuro — PWA/responsivo apenas na v1)
- Módulo de compras/estoque de peças avançado (apenas fornecedores como cadastro)
- BI/Analytics avançado (apenas dashboards e indicadores citados)
- Integração com sensores IoT externos (health check usa dados do próprio ativo)
- Multi-idioma e white-label (v1: pt-BR apenas)

### Future Considerations (Roadmap)
- App mobile nativo para coletores (QR Code, offline-first)
- Checklists digitais e anexos fotográficos nas ordens
- Agendamento de manutenção preventiva recorrente avançado (gatilhos por tempo/uso/condição)
- Integração com CMDB/ITSM externos
- Anomaly detection via ML para manutenção preditiva (além de regressão linear)

---

## 6. Business Rules & Constraints

### Regras de Negócio — Ativos & Hierarquia
* **BR-01:** Ativo só pode ser alocado em **Localizacao** pertencente à mesma **Filial** do responsável (multi-tenancy).
* **BR-02:** `AtivoDetalheHardware` é obrigatório para ativos do tipo "Hardware" (Notebook, Servidor, etc.) — validação no backend.
* **BR-03:** Depreciação calculada automaticamente (método linear) com base em data de aquisição, valor residual e vida útil por `TipoAtivo`.
* **BR-04:** Exclusão de **Filial/Departamento/Localizacao** bloqueada se houver ativos ou ordens vinculadas (409 Conflict).

### Regras de Negócio — Manutenção
* **BR-05:** Ordem só pode ser **iniciada** se estado = "Aberta" E técnico responsável alocado E ativo disponível.
* **BR-06:** **Aprovar** exige evidência (checklist assinado digitalmente, foto, ou assinatura do técnico) — backend valida `canApprove: true`.
* **BR-07:** **Concluir** só permitido após **aprovar**; **cancelar** permitido em qualquer estado exceto "Concluída".
* **BR-08:** `custoTotalPorAtivo` = soma de custos (mão de obra + material + terceiros) de ordens **concluídas** vinculadas ao ativo — cálculo no backend, frontend apenas exibe.
* **BR-09:** **ManutencaoPreventiva** gera ordens automaticamente conforme cron (Spring Scheduler); técnico padrão opcional.
* **BR-10:** **Health Check** (ManutencaoPreditiva) roda periodicamente; se probabilidade de falha de disco > threshold (configurável), gera alerta + ordem de manutenção preditiva automática.

### Regras de Negócio — Busca & Fuzzy
* **BR-11:** Busca fuzzy (Levenshtein) aplicada a campos: tag, serial, modelo, fabricante, nome do funcionário, razão social do fornecedor. Threshold de similaridade configurável (default 0.7).
* **BR-12:** Busca combina fuzzy + filtros exatos (filial, status, tipo, data) com paginação server-side.

### Regras de Negócio — Segurança (Aegis Shield)
* **BR-13:** **Multi-tenancy**: Usuário só acessa dados da própria Filial (exceto ADMIN global). Isolamento no nível de query (Hibernate Filter / MultiTenancyFilter).
* **BR-14:** **Permissões hierárquicas**: ADMIN > GESTOR > TECNICO > USER > AUDITOR. Permissões contextuais (ex.: GESTOR pode aprovar ordens da sua filial; TECNICO só vê ordens atribuídas a si).
* **BR-15:** Todas as mutações passam por `authInterceptor` (frontend) + `JwtTokenProvider` + `SecurityConfig` (backend); expiração dispara refresh automático (retry único).
* **BR-16:** Auditoria (Envers) captura: quem, quando, o quê (diff), IP, user-agent. Imutável — não pode ser desativada nem alterada.

### Regras de Negócio — Relatórios & QR Code
* **BR-17:** **Termo de Responsabilidade** (PDF) gerado na alocação/transferência de ativo; assinatura digital do responsável.
* **BR-18:** **Etiqueta QR Code** contém: tag do ativo, URL pública de consulta (read-only), hash de integridade. Impressão em lote suportada.

---

## 7. Assumptions & Dependencies

* **Premissas Técnicas:**
  1. Java 21, Spring Boot 3.3, MySQL 8.0, Flyway, Hibernate Envers, Lombok, Maven
  2. Frontend: Vue 3, Bootstrap 5, Pinia, Vite
  3. Infra: Docker, Docker Compose, Kubernetes manifests
  4. TestContainers com reuso habilitado (`.testcontainers.properties`)
  5. Variáveis de ambiente via `.env` / `application.properties`
  6. Commits: Conventional Commits; Branches: main, develop, feature/*, bugfix/*, hotfix/*

* **Dependências Externas:**
  1. **Infra/DevOps** — K8s cluster, registry, CDN, TLS, secrets management
  2. **Segurança da Informação** — revisão de armazenamento de token (httpOnly cookie vs localStorage), LGPD, pentest
  3. **Dados mestres iniciais** — carga de filiais, departamentos, tipos de ativo, usuários iniciais (Flyway seed)

---

## 8. Risks & Mitigations (Business-Level)

| Risco | Probabilidade | Impacto | Mitigação |
| :--- | :--- | :--- | :--- |
| Backend não entrega endpoints a tempo | Baixa (já implementado) | Alto | Contrato OpenAPI já definido; mocks para frontend |
| Complexidade do Aegis Shield gera bugs de autorização | Média | Alto | Testes de integração abrangentes (TestContainers); matriz de permissão documentada |
| Regressão linear (preditiva) com poucos dados históricos | Alta (início) | Médio | Threshold conservador; fallback para health check baseado em regras; coleta contínua melhora modelo |
| Busca fuzzy (Levenshtein) performance em base grande | Média | Médio | Índices trigram/pg_trgm no MySQL; cache Redis para queries frequentes; paginação obrigatória |
| Console logs em produção expõem dados sensíveis | Baixa | Alto | Pipeline de build falha se `console.*` no bundle (eslint/no-console); `handleApiError` padronizado |
| Falha no `authInterceptor` / refresh token causa logout em massa | Baixa | Alto | Testes automatizados de expiração/refresh; fallback para tela de login limpa |
| Multi-tenancy vazamento de dados entre filiais | Baixa | Crítico | Testes de integração cross-tenant; Hibernate Filter obrigatório em todas as queries; auditoria de acesso |
| Cálculo de `custoTotalPorAtivo` divergente entre frontend/backend | Média | Médio | Cálculo centralizado no backend; frontend apenas exibe; contrato OpenAPI valida schema |
| Migrações Flyway conflitos em ambientes paralelos | Média | Alto | Convenção de nomenclatura V{versão}__{descrição}; revisão de PR obrigatória; baseline em produção |

---

## 9. Acceptance Criteria (High-Level)

* **AC-01:** Usuário cria ativo com detalhes de hardware → sistema calcula depreciação → gera QR Code → emite Termo de Responsabilidade PDF.
* **AC-02:** Técnico escaneia QR Code → abre ordem → inicia → conclui → aprovador aprova → custoTotalPorAtivo atualizado automaticamente.
* **AC-03:** Health check detecta disco com probabilidade de falha > 80% → alerta no dashboard → ordem preditiva criada automaticamente.
* **AC-04:** Busca por "notebok" (erro de digitação) retorna "Notebook Dell Latitude 5520" (Levenshtein distance ≤ 2).
* **AC-05:** Usuário da Filial A não vê ativos/ordens da Filial B (multi-tenancy); ADMIN global vê tudo.
* **AC-06:** Auditoria mostra diff completo de alteração em ativo (campo a campo, antes/depois, usuário, timestamp).
* **AC-07:** Pipeline CI: build → test (coverage ≥ 80%) → security scan → deploy staging → smoke tests → deploy prod.

---

## 10. Glossary Reference

Termos-chave definidos no **Glossário (Ubiquitous Language)** — `docs/00-foundation/glossario.md`:
- Domínio: `aprovar`, `atualizar`, `buscarPorId`, `cancelar`, `concluir`, `criar`, `custoTotalPorAtivo`, `deletar`, `iniciar`, `listar`, `filial`, `funcionario`, `fornecedor`, `manutencao`, `manutencaoPreventiva`, `manutencaoPreditiva`, `solicitacaoManutencao`, `tipoAtivo`, `usuario`
- Técnico/Backend: `Ativo`, `AtivoDetalheHardware`, `AdaptadorRede`, `Disco`, `Memoria`, `Filial`, `Departamento`, `Localizacao`, `Fornecedor`, `Funcionario`, `TipoAtivo`, `SolicitacaoManutencao`, `ManutencaoPreventiva`, `Usuario`, `Role`, `Permission`, `Auditoria`, `FuzzySearch`, `LevenshteinDistance`, `ManutencaoPreditivaService`, `HealthCheck`, `JwtTokenProvider`, `SecurityConfig`, `AegisShield`, `MultiTenancyFilter`, `FlywayMigration`, `TestContainers`, `QRCodeGenerator`, `PdfGenerator`

---

## 11. Traceability Matrix (Resumo)

| Requisito | Origem | Artefatos Relacionados | Testes |
| :--- | :--- | :--- | :--- |
| BR-01 a BR-04 (Ativos) | Glossário + Domain Model | `domain-model.md`, `schema-spec.md`, `api-spec.md` | Integration: AtivoControllerIT |
| BR-05 a BR-10 (Manutenção) | Glossário + Use Cases | `use-cases.md`, `state-machines.md`, `api-spec.md` | Integration: SolicitacaoManutencaoControllerIT |
| BR-11 a BR-12 (Busca) | Glossário + ADR (Fuzzy Search) | `adr.md` (Fuzzy Search), `api-spec.md` | Unit: FuzzySearchServiceTest; Integration: SearchControllerIT |
| BR-13 a BR-16 (Segurança) | Glossário + ADR (Aegis Shield) | `adr.md` (Aegis Shield), `security-policies.md`, `api-spec.md` | Integration: SecurityConfigIT, AegisShieldTest |
| BR-17 a BR-18 (Relatórios) | Glossário + Use Cases | `use-cases.md`, `api-spec.md` | Integration: ReportControllerIT |

---

*Documento regenerado com base em análise AST completa do backend Java (domain, security, predictive, search, audit) + frontend Vue. Substitui versão 1.0 que continha apenas visão frontend.*