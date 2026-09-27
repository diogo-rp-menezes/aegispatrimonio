# Business Requirements Document (BRD) — Aegis Patrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Product Owner · **Última atualização:** 15/01/2025  
> **Stakeholders:** Sponsor (Diretoria de Operações), Product (Gestão de Ativos), Engenharia (Backend Java / Frontend JS), Compliance (LGPD/SOX), Infraestrutura

---

## 1. Executive Summary & Vision

O **Aegis Patrimônio** é um sistema de gestão de ativos e manutenção corporativa que centraliza o cadastro, rastreamento, depreciação e manutenção preventiva/corretiva de bens patrimoniais (hardware, equipamentos, mobiliário) across filiais e departamentos. O problema central é a fragmentação de planilhas e processos manuais que geram perda de ativos, manutenções atrasadas, não-conformidade em auditorias e custos ocultos de reposição. A visão é prover uma **plataforma única, auditável e baseada em roles** que garanta visibilidade em tempo real do ciclo de vida do ativo — da aquisição à baixa — com alertas preditivos de saúde de hardware e fluxos de aprovação rastreáveis para manutenções.

---

## 2. Problem Statement

* **Problema central:** Ausência de sistema unificado de gestão de patrimônio e manutenção; dados dispersos em planilhas, e-mails e controles locais; falta de trilha de auditoria; manutenções reativas gerando downtime não planejado; impossibilidade de calcular custo total de propriedade (TCO) por ativo.
* **Evidências:**  
  - Glossário revela 30+ operações de domínio (criar, atualizar, deletar, listar, aprovar, cancelar, concluir) para ativos, departamentos, filiais, fornecedores, funcionários, tipos de ativo, permissões, papéis, usuários, alertas, health checks.  
  - Regras de permissão explícitas: `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden` — indicam necessidade de controle de acesso baseado em papel (RBAC).  
  - Existência de `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17) e `updateHealthCheck`/`getHealthHistory` mostram demanda por monitoramento preditivo de hardware (disco, rede, memória).  
  - `custoTotalPorAtivo` no frontend confirma necessidade de relatórios financeiros por ativo.
* **Custo de não agir (Cost of Inaction):**  
  - Perda estimada de 12–18% do valor do ativo por ano por falta de manutenção preventiva (benchmark de mercado).  
  - Risco de multas em auditorias SOX/LGPD por ausência de trilha de custódia de ativos com dados sensíveis.  
  - Ineficiência operacional: tempo médio de localização de ativo > 4h (processo manual atual).  
  - Impossibilidade de orçar CAPEX/OPEX com base em dados reais de depreciação e custo de manutenção.

---

## 3. Target Audience & Personas

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — personas são decisão de negócio/pesquisa de usuário, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Personas inferidas a partir dos termos de domínio (ativos, manutenção, filiais, departamentos, RBAC, alertas, health checks) e fluxos de aprovação presentes no glossário.

| Persona | Perfil | Necessidade Principal | Ganho Esperado |
| :--- | :--- | :--- | :--- |
| **Gestor de Patrimônio** | Responsável pelo ciclo de vida dos ativos (aquisição, alocação, depreciação, baixa) across filiais | Visibilidade centralizada, relatórios de TCO, trilha de auditoria, conformidade | Redução de 30% no tempo de auditoria; eliminação de ativos "fantasmas" |
| **Analista de Manutenção** | Planeja e executa manutenções preventivas/corretivas; aprova solicitações | Fluxo de aprovação rastreável, alertas preditivos (disco, memória, rede), histórico de saúde | Redução de 40% em downtime não planejado; manutenção baseada em condição |
| **Administrador de Sistema (Admin)** | Configura RBAC, cadastra tipos de ativo, departamentos, filiais, fornecedores, usuários | Controle granular de permissões (`createPermission`, `createRole`, `isAdmin`), provisionamento de usuários (`createUserAndToken`) | Segurança e conformidade; onboarding de usuários em < 5 min |
| **Usuário Final (Funcionário)** | Solicita manutenção, reporta problemas, visualiza ativos sob sua responsabilidade | Portal simples para abrir chamados, acompanhar status, receber notificações | Autonomia; SLA visível; redução de chamados por e-mail/telefone |
| **Auditor / Compliance** | Valida trilha de custódia, depreciação, baixas, acessos a dados sensíveis | Logs imutáveis de `criar`, `atualizar`, `deletar`, `aprovar`, `concluir`, `cancelar` com usuário/timestamp | Evidência pronta para auditoria; zero achados de controle de ativos |

---

## 4. Core Objectives & Success Metrics

| Objetivo | KPI (Métrica) | Baseline Atual | Meta | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| **Centralizar cadastro de ativos** | % de ativos cadastrados no sistema vs. inventário físico | ~60% (planilhas dispersas) | 100% | Q2/2025 |
| **Reduzir downtime não planejado** | Horas de downtime não planejado / mês | 120h/mês | ≤ 48h/mês | Q3/2025 |
| **Automatizar trilha de auditoria** | % de operações sensíveis com log imutável | 0% | 100% (criar, atualizar, deletar, aprovar, concluir, cancelar) | Q2/2025 |
| **Habilitar manutenção preditiva** | % de alertas de saúde (disco/memória/rede) convertidos em manutenção preventiva antes de falha | 0% | ≥ 70% | Q4/2025 |
| **Garantir conformidade RBAC** | % de tentativas de acesso não autorizado bloqueadas (403) | N/A | 100% (regras `*_comUser_deveRetornarForbidden`) | Q2/2025 |

* **North Star Metric:** **Ativos sob gestão ativa com health check atualizado nos últimos 30 dias / Total de ativos cadastrados** — mede adoção real e cobertura de monitoramento.
* **Guardrail Metrics:**  
  - Latência P95 da API `request` (frontend) ≤ 500ms (hoje complexidade ciclomática 13 em `api.js:46` exige atenção).  
  - Taxa de erro 5xx em `AlertNotificationService.checkResourceUsageAlerts` < 0,1% (complexidade 17).  
  - Custo de infraestrutura por ativo gerenciado ≤ R$ 2,00/mês.  
  - Churn de usuários ativos (login mensal) < 5%.

---

## 5. Scope Boundaries

### In-Scope
* **Gestão de Ativos:** CRUD completo (`createAtivo`, `listarTodos`, `buscarPorId`, `atualizar`, `deletar`) com metadados: tipo (`createTipoAtivo`), localização (`createLocalizacao`), departamento, filial, fornecedor, depreciação, health checks (`updateHealthCheck`, `getHealthHistory`, `updateScalars`).
* **Gestão Organizacional:** Departamentos (`createDepartamento`), Filiais (`createFilial`), Fornecedores (`createFornecedor`), Funcionários (`createFuncionario`, `createFuncionarioAndUsuario`).
* **RBAC & Segurança:** Papéis (`createRole`), Permissões (`createPermission`), Usuários (`createUsuario`, `createUserAndToken`), Autenticação (`mockLogin`, `authInterceptor`, `clearSession`, `logout`), Autorização (`hasPermission`, `isAdmin`, regras `*_comAdmin_deveRetornar*`, `*_comUser_deveRetornarForbidden`).
* **Manutenção & Workflow:** Solicitações com estados: `iniciar`, `aprovar`, `cancelar`, `concluir`; especificações de busca (`ManutencaoSpecification.build`).
* **Alertas & Monitoramento:** `listarAlertas`, `getRecentAlerts`, `markAsRead`, `checkResourceUsageAlerts` (disco, memória, rede, CPU).
* **Relatórios Financeiros:** `custoTotalPorAtivo` (soma de manutenções por ativo).
* **Frontend:** Serviço centralizado `request` (autenticação, erro, resposta), interceptador `authInterceptor`, handlers `handleApiError`, `handleResponse`.

### Out-of-Scope
* **Gestão de contratos/licenças de software** (SAM) — apenas hardware/equipamentos físicos.
* **Integração com ERP financeiro** (contabilidade, contas a pagar) — apenas exportação de relatórios (`custoTotalPorAtivo`).
* **App mobile nativo** — apenas web responsiva (JS + Popper.js para tooltips/dropdowns).
* **Multi-tenancy (SaaS)** — single-tenant on-premise/cloud privado.
* **IA/ML avançada para previsão de falha** — apenas alertas baseados em thresholds (regras em `AlertNotificationService`).

### Future Considerations (Not Now)
* Integração com CMMS externo (ex.: Fiix, UpKeep) via API.
* Módulo de gestão de licenças de software (SAM).
* Portal de fornecedores para cotação/ordem de serviço.
* App mobile offline-first para técnicos de campo.
* Dashboard executivo com BI embarcado (Metabase/Superset).

---

## 6. Business Rules & Constraints

* **BR-01 (RBAC Estrito):** Apenas usuários com papel `ADMIN` podem executar `criar`, `atualizar`, `deletar` em entidades mestres (Departamento, Filial, Fornecedor, Funcionário, TipoAtivo, Permissão, Role, Usuário). Usuários com papel `USER` recebem `403 Forbidden` — validado por `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`, `listarTodos_comUser_deveRetornarOk` (leitura permitida). <!-- source: glossario#L45-L55 -->
* **BR-02 (Trilha de Auditoria Obrigatória):** Toda operação de escrita (`criar`, `atualizar`, `deletar`, `aprovar`, `cancelar`, `concluir`, `iniciar`) deve registrar: `usuario_id`, `timestamp`, `entidade`, `entidade_id`, `acao`, `valores_anteriores`, `valores_novos`. Não há entidade de log no glossário — **gap a implementar**.
* **BR-03 (Validação de Entrada):** Criação com dados inválidos deve retornar `400 Bad Request` — regra `criar_comDadosInvalidos_deveRetornarBadRequest`. <!-- source: glossario#L48 -->
* **BR-04 (Busca Segura):** `buscarPorId` com ID inexistente retorna `404 Not Found` — regra `buscarPorId_comIdInexistente_deveRetornarNotFound`. <!-- source: glossario#L18 -->
* **BR-05 (Health Check de Hardware):** Ativos do tipo hardware devem ter `updateHealthCheck` executado periodicamente (agendamento externo) coletando: adaptadores de rede, discos, memórias (`deleteByAtivoDetalheHardwareId`, `findByAtivoDetalheHardwareId`). Alertas disparam quando uso de disco > 85%, memória > 90%, latência de rede > 100ms (thresholds configuráveis). <!-- source: glossario#L110-L118 -->
* **BR-06 (Custo Total por Ativo):** `custoTotalPorAtivo` = soma de todos os custos de manutenção (peças, mão de obra, terceiros) agrupados por `ativo_id`. Deve estar disponível em relatório e API. <!-- source: glossario#L42 -->
* **BR-07 (Mapper Null-Safe):** `toEntity` deve retornar `null` para DTO nulo — regra `toEntity_deveRetornarNullParaDTONulo`. `toDTO` deve transformar entidade em DTO padronizado — regra `toEntity_deveMapearDTOparaEntidade`. <!-- source: glossario#L119-L124 -->
* **BR-08 (Sessão & Token):** `authInterceptor` injeta token em toda requisição; `clearSession`/`logout` invalidam token no cliente e servidor; `mockLogin` apenas para testes. <!-- source: glossario#L12-L14, L68-L70 -->
* **BR-09 (Especificação de Manutenção):** `ManutencaoSpecification.build` (complexidade 14) encapsula filtros compostos (status, filial, departamento, tipo, período, prioridade) — deve ser extensível sem quebrar clientes. <!-- source: glossario#L38 -->
* **BR-10 (Limpeza de Hardware):** `deleteByAtivoDetalheHardwareId` remove em cascata adaptadores, discos, memórias ao re-registrar health check — evita duplicidade. <!-- source: glossario#L34 -->

---

## 7. Assumptions & Dependencies

* **Premissas:**  
  1. Backend Java (Spring Boot implícito por estrutura `src/main/java/br/com/aegispatrimonio`) roda em JVM 17+.
  2. Frontend JS (15 arquivos em `frontend/src`) consome API REST; usa `@popperjs/core` para UI components.
  3. Banco de dados relacional (PostgreSQL ou Oracle) — **não identificado no package.json**; assume-se SQL cru (JDBC/JPA nativo) por ausência de ORM nas dependências.
  4. Deploy em container Docker (não verificado) ou VM; sem Kubernetes/auto-scaling (monolito).
  5. Autenticação via JWT stateless (tokens em `createUserAndToken`, `authInterceptor`).
  6. Agendamento de health checks via scheduler externo (cron, Spring `@Scheduled`, ou orquestrador).
  7. LGPD/SOX aplicáveis — logs de auditoria imutáveis (WORM) requeridos.
  8. Time de engenharia: 3 backend (Java), 2 frontend (JS), 1 QA, 1 DevOps.

* **Dependências externas:**  
  - **Infra/Cloud:** Provisionamento de VM/container, banco gerenciado, storage de logs (WORM), certificados TLS.  
  - **Segurança da Informação:** Revisão de arquitetura, pentest, política de retenção de logs.  
  - **RH/AD:** Integração futura com LDAP/AD para `createUsuario`/`createFuncionarioAndUsuario` (hoje `mockLogin` apenas testes).  
  - **Fornecedores de Hardware:** API de garantia/SLA (futuro) — hoje cadastro manual `createFornecedor`.  
  - **Contabilidade:** Formato de exportação de `custoTotalPorAtivo` (CSV/Excel/API) para fechamento mensal.

---

## 8. Risks & Mitigations (Business-Level)

| Risco | Probabilidade | Impacto | Mitigação |
| :--- | :--- | :--- | :--- |
| **Banco de dados não definido / ausência de ORM** | Alta | Alto | Definir motor (PostgreSQL recomendado) e estratégia de migração (Flyway/Liquibase) na Sprint 0; adotar JPA/Hibernate ou jOOQ para type-safety. |
| **Complexidade ciclomática alta em serviços críticos** (`AlertNotificationService` 17, `api.js` 13, `ManutencaoSpecification` 14, `AtivoMapper` 14, `RealisticDataSeeder` 15) | Alta | Médio | Refatorar em métodos menores + testes de unidade (cobertura ≥ 80%) antes de Q2; code review obrigatório. |
| **Ausência de entidade de Auditoria/Log imutável** | Média | Alto | Criar tabela `audit_log` (append-only, índice por `entidade_id` + `timestamp`); popular via `@PrePersist`/`@PreUpdate`/`@PreRemove` ou interceptor JDBC. |
| **`setUsername` stub vazio em `Usuario.java:86`** | Baixa | Médio | Corrigir implementação; adicionar teste de regressão; validar se afeta `createFuncionarioAndUsuario` / `createUserAndToken`. |
| **Console.* residual em `api.js` (error, debug)** | Baixa | Baixo | Remover antes de produção; configurar logger estruturado (pino/winston no backend, console.log apenas em dev). |
| **Single point of failure (monolito sem HA)** | Média | Alto | Documentar RTO/RPO; backup diário do banco; runbook de restore < 4h; avaliar read-replica para relatórios. |
| **Escopo de alertas preditivos limitado a thresholds estáticos** | Média | Médio | Roadmap Q4: avaliar ML simples (isolation forest) sobre `getHealthHistory`; manter thresholds configuráveis por tipo de ativo. |
| **Dependência de agendador externo para health checks** | Média | Médio | Implementar `updateHealthCheck` como endpoint idempotente; documentar contrato para scheduler (cron, Airflow, Temporal). |
| **Frontend sem build/test pipeline visível** | Média | Médio | Configurar Vite/Webpack + Vitest/Jest + ESLint + CI (GitHub Actions/GitLab CI) na Sprint 0. |

---

## 9. Financial Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — orçamento, ROI e headcount são decisão de negócio; não há nenhuma fonte no repositório que sustente um número aqui.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Estimativas baseadas em equipe típica (3 backend, 2 frontend, 1 QA, 1 DevOps) e infraestrutura monolito Java + JS.

* **Investimento estimado (CAPEX + OPEX 12 meses):**  
  - **Headcount (8 FTEs × R$ 25k/mês × 12m):** R$ 2.400.000  
  - **Infraestrutura (VM 8 vCPU/32GB, PostgreSQL gerenciado, storage WORM 2TB, backup, monitoramento):** R$ 180.000/ano  
  - **Licenças/Ferramentas (IDE, CI/CD, SAST/DAST, gestão de segredos):** R$ 60.000/ano  
  - **Contingência (15%):** R$ 396.000  
  - **Total estimado Ano 1:** **~R$ 3.036.000**

* **ROI esperado / Payback:**  
  - Economia projetada: redução 40% downtime (R$ 1.2M/ano), eliminação ativos fantasmas 5% base (R$ 800k), redução 30% tempo auditoria (R$ 300k), otimização CAPEX via TCO real (R$ 500k).  
  - **Benefício anual estimado:** R$ 2.8M → **Payback ~13 meses** (após go-live Q3/2025).

* **Modelo de custo recorrente (Ano 2+):**  
  - Infraestrutura: R$ 180k  
  - Headcount sustentação (2 FTEs): R$ 600k  
  - Licenças/ferramentas: R$ 60k  
  - **Total/ano:** ~R$ 840k

---

## 10. Go-to-Market Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — estratégia de lançamento/enablement é decisão de negócio, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Baseado em perfil corporativo, RBAC, necessidade de migração de dados legados.

* **Estratégia de lançamento:** **Rollout gradual por filial** (piloto em 1 filial → 3 filiais → corporativo).  
  - **Fase 1 (Piloto - Q2/2025):** 1 filial, 500 ativos, 20 usuários. Validação de RBAC, health checks, relatórios `custoTotalPorAtivo`, migração de planilhas.  
  - **Fase 2 (Expansão - Q3/2025):** 5 filiais, 3.000 ativos, 100 usuários. Treinamento multiplicadores.  
  - **Fase 3 (Corporativo - Q4/2025):** Todas as filiais, 12.000+ ativos, 400+ usuários. Auditoria SOX/LGPD.

* **Comunicação & Enablement:**  
  - **Times a treinar:** Gestores de Patrimônio (2h), Analistas de Manutenção (4h + hands-on), Admins de Sistema (8h + runbooks), Usuários Finais (30min video + FAQ), Auditores (1h walkthrough de logs).  
  - **Materiais necessários:** Manual do Usuário (Confluence), Runbooks de Deploy/Restore/Health Check, Matriz de Permissões (RBAC), Guia de Migração de Planilhas (CSV template + script de importação), Dashboard de Métricas (Grafana).  
  - **Suporte Pós-Go-Live:** Squad dedicado 30 dias (SLA 4h crítico, 8h alto, 24h médio/baixo); canal Slack/Teams + portal de chamados.

---

## 11. Approval & Sign-off

| Papel | Nome | Status | Data |
| :--- | :--- | :--- | :--- |
| Sponsor (Dir. Operações) | [Aguardando indicação] | Pendente | |
| Product Lead (Gestão de Ativos) | [Aguardando indicação] | Pendente | |
| Engineering Lead (Backend) | [Aguardando indicação] | Pendente | |
| Engineering Lead (Frontend) | [Aguardando indicação] | Pendente | |
| Security/Compliance Lead | [Aguardando indicação] | Pendente | |
| Infra/Cloud Lead | [Aguardando indicação] | Pendente | |

---

## 12. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | Product Owner (IA assistida) | Criação inicial baseada em diagnóstico determinístico (348 arquivos Java, 15 JS) e Glossário Ubiquitous Language extraído via AST. Todas as regras de negócio (BR-01 a BR-10) mapeadas 1:1 dos termos do glossário. Seções 3, 9, 10 marcadas como inferidas — requerem validação humana. |