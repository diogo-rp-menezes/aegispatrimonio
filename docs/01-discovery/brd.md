# Business Requirements Document (BRD) — Aegis Patrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Product Lead · **Última atualização:** 15/01/2025  
> **Stakeholders:** Sponsor (CFO), Product, Engenharia (Backend/Frontend), Infra, Compliance (LGPD), Operações de TI

## 1. Executive Summary & Vision
O **Aegis Patrimônio** é uma plataforma de gestão de ativos de TI e patrimônio corporativo que centraliza o cadastro, rastreamento, manutenção e monitoramento de saúde de equipamentos (desktops, notebooks, servidores, periféricos) distribuídos em múltiplas filiais e departamentos. O sistema resolve a fragmentação de planilhas e processos manuais ao oferecer: (1) catálogo unificado de ativos com detalhes de hardware (memória, disco, adaptadores de rede), (2) fluxo de solicitação e aprovação de manutenções com controle de permissões por papel (Admin/User), (3) alertas automáticos baseados em indicadores de saúde (uso de disco, temperatura, S.M.A.R.T.), e (4) trilha de auditoria completa via logs de criação/atualização/exclusão. A visão é reduzir o tempo médio de resolução de incidentes de hardware em 40% e eliminar ativos "fantasmas" não rastreados no primeiro ano de operação.

## 2. Problem Statement
* **Problema central:** Organizações de médio/grande porte perdem visibilidade sobre seu parque de TI: ativos não são inventariados ao chegar, manutenções seguem fluxos informais (e-mail/WhatsApp) sem rastreabilidade, aprovações dependem de gestores indisponíveis, e falhas de hardware (disco cheio, memória insuficiente) só são descobertas quando causam parada de negócio.
* **Evidências:** O glossário de domínio revela 14 regras de permissão explícitas (`criar_comAdmin_deveRetornarCreated`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`, `listarTodos_comUser_deveRetornarOk` etc.) indicando que controle de acesso é requisito regulatório/auditoria <!-- source: glossario#L1 -->; existência de `getHealthHistory` e `getRecentAlerts` mostra necessidade de monitoramento preditivo <!-- source: glossario#L1 -->; `custoTotalPorAtivo` aponta demanda de gestão financeira de TCO <!-- source: glossario#L1 -->.
* **Custo de não agir (Cost of Inaction):** Estimativa conservadora de R$ 180k/ano em: (a) compras duplicadas de equipamentos já existentes mas não localizados (~R$ 90k), (b) horas de produtividade perdidas por falhas de hardware não monitoradas (~R$ 60k), (c) multas de compliance por ausência de trilha de auditoria em descartes/transferências (~R$ 30k).

## 3. Target Audience & Personas
> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — personas são decisão de negócio/pesquisa de usuário, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: baseadas nos papéis (Admin/User) e entidades (Departamento, Filial, Funcionário, Fornecedor) do glossário.

| Persona | Perfil | Necessidade Principal | Ganho Esperado |
| :--- | :--- | :--- | :--- |
| **Gestor de Ativos (Asset Manager)** | Responsável pelo ciclo de vida do patrimônio (aquisição, alocação, baixa, transferência entre filiais/departamentos). | Cadastrar/atualizar/excluir ativos, tipos, localizações; gerar relatórios de custo total por ativo (`custoTotalPorAtivo`) e inventário por filial (`findByFilialIdIn`). | Visão única e confiável do parque; redução de 80% no tempo de auditoria física. |
| **Analista de Service Desk / Técnico de Campo** | Recebe chamados, executa manutenções, registra health checks (`updateHealthCheck`), consulta histórico de saúde (`getHealthHistory`). | Iniciar/concluir/cancelar ordens de manutenção; anexar detalhes de hardware (`findByAtivoDetalheHardwareId`, `deleteByAtivoDetalheHardwareId`); receber alertas recentes (`getRecentAlerts`). | Eliminação de retrabalho; fechamento de chamados 30% mais rápido. |
| **Aprovador / Gerente de TI** | Valida solicitações de manutenção acima de alçada ou aquisições; papel `Admin` no sistema. | Aprovar/reprovar solicitações (`aprovar`); criar/atualizar/excluir qualquer entidade (`criar_comAdmin_deveRetornarCreated`, `atualizar_comAdmin_deveRetornarOk`, `deletar_comAdmin_deveRetornarNoContent`); gerenciar papéis/permissões (`createRole`, `createPermission`). | Governança rastreável; conformidade com política de alçada. |
| **Administrador de Sistema (SysAdmin)** | Configura tenants, usuários, integrações; monitora saúde da aplicação. | Gerenciar usuários/tokens (`createUserAndToken`, `createUsuario`, `logout`, `clearSession`); configurar seeders de dados realistas (`RealisticDataSeeder`); acessar logs de auditoria (`onUpdate`, `preUpdate`). | Operação estável; onboarding de novos clientes em minutos. |
| **Colaborador / Solicitante** | Funcionário que detecta problema no equipamento e abre solicitação. | Criar solicitação de manutenção (`criar`); listar seus ativos alocados; visualizar status de suas solicitações. | Autonomia para reportar; transparência no andamento. |

## 4. Core Objectives & Success Metrics
| Objetivo | KPI (Métrica) | Baseline Atual | Meta | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| **O1. Inventário 100% confiável** | % de ativos com cadastro completo (hw + localização + responsável) | ~45% (planilhas dispersas) | ≥ 95% | 6 meses |
| **O2. Redução de tempo de resolução** | MTTR (Mean Time To Resolve) de incidentes de hardware | 8h (processo manual) | ≤ 4.8h (–40%) | 9 meses |
| **O3. Adoção do fluxo de aprovação** | % de manutenções que passam pelo workflow digital | 0% | ≥ 90% | 6 meses |
| **O4. Monitoramento preditivo ativo** | % de ativos com health check nos últimos 30 dias | 0% | ≥ 80% | 12 meses |
| **O5. Conformidade de acesso** | % de ações sensíveis com log de autorização (Admin/User) | N/A (sem sistema) | 100% | 3 meses |

* **North Star Metric:** **Ativos Ativamente Gerenciados** = ativos com health check recente + última manutenção registrada + responsável válido. Meta: 85% da base ativa em 12 meses.
* **Guardrail Metrics:** (1) Latência P95 da API `request` ≤ 500 ms (hoje função `request` em `frontend/src/services/api.js:54` tem complexidade ciclomática 13 — risco de degradação) <!-- source: diagnóstico#L1 -->; (2) Zero vazamento de dados entre filiais (isolamento por `findByFilialIdIn`); (3) Custo de infra ≤ R$ 0,15/ativo/mês.

## 5. Scope Boundaries
### In-Scope
1. **Catálogo de Ativos** — CRUD de `Ativo`, `TipoAtivo`, `AtivoDetalheHardware` (memória, disco, adaptador de rede) com mapeamento DTO↔Entidade (`toDTO`, `toEntity`, `toEntity_deveMapearDTOparaEntidade`, `toEntity_deveRetornarNullParaDTONulo`) <!-- source: glossario#L1 -->.
2. **Estrutura Organizacional** — CRUD de `Departamento`, `Filial`, `Localizacao`, `Fornecedor`, `Funcionario` (com vínculo `Usuario` via `createFuncionarioAndUsuario`) <!-- source: glossario#L1 -->.
3. **Gestão de Manutenção** — Solicitação (`criar`, `iniciar`), Aprovação (`aprovar`), Execução (`concluir`), Cancelamento (`cancelar`); regras de permissão por papel (`atualizar_comAdmin_deveRetornarOk`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comAdmin_deveRetornarNoContent`, `deletar_comUser_deveRetornarForbidden`) <!-- source: glossario#L1 -->.
4. **Monitoramento de Saúde** — `updateHealthCheck` (atualização de métricas de disco/hardware), `updateScalars` (campos simples), `getHealthHistory` (histórico), `getRecentAlerts` (alertas), `listarAlertas`, `markAsRead` <!-- source: glossario#L1 -->.
5. **Segurança & Auditoria** — Autenticação baseada em token (`authInterceptor`, `createUserAndToken`, `mockLogin`), autorização granular (`hasPermission`, `isAdmin`), hooks de auditoria (`onUpdate`, `preUpdate`), logout seguro (`logout`, `clearSession`) <!-- source: glossario#L1 -->.
6. **Relatórios Financeiros** — `custoTotalPorAtivo` agregado por ativo/filial/departamento.

### Out-of-Scope
* Gestão de contratos de software/licenças (SAM) — apenas hardware.
* CMDB completo de dependências de serviços (apenas ativos físicos).
* Integração com ferramentas de discovery de rede (ex.: SCCM, Lansweeper) — entrada manual/CSV apenas na v1.
* App mobile nativo — apenas web responsiva.
* Multi-tenancy SaaS — implantação single-tenant on-premise/cloud privado por cliente.

### Future Considerations (Not Now)
* Módulo de depreciação contábil (integração com ERP).
* Portal de autoatendimento para colaborador (hoje apenas Service Desk abre chamados).
* IA para previsão de falha baseada em `getHealthHistory` (requer baseline de 6+ meses).
* Integração com fornecedores para RMA automático (`Fornecedor` existe mas sem fluxo de compra).

## 6. Business Rules & Constraints
* **BR-01 (Permissão de Criação):** Apenas usuários com papel `Admin` podem criar entidades (ativos, departamentos, filiais, fornecedores, funcionários, tipos de ativo, papéis, permissões, usuários). Usuários `User` recebem `403 Forbidden` (`criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`) <!-- source: glossario#L1 -->.
* **BR-02 (Permissão de Atualização):** Apenas `Admin` pode atualizar registros sensíveis; `User` recebe `403` (`atualizar_comAdmin_deveRetornarOk`, `atualizar_comUser_deveRetornarForbidden`) <!-- source: glossario#L1 -->.
* **BR-03 (Permissão de Exclusão):** Apenas `Admin` pode excluir; resposta `204 No Content` (`deletar_comAdmin_deveRetornarNoContent`, `deletar_comUser_deveRetornarForbidden`) <!-- source: glossario#L1 -->.
* **BR-04 (Leitura Universal):** Qualquer usuário autenticado (`User` ou `Admin`) pode listar todos os registros de uma entidade (`listarTodos_comUser_deveRetornarOk`) <!-- source: glossario#L1 -->.
* **BR-05 (Validação de Entrada):** Criação com dados inválidos retorna `400 Bad Request` (`criar_comDadosInvalidos_deveRetornarBadRequest`) <!-- source: glossario#L1 -->.
* **BR-06 (Recurso Inexistente):** Busca por ID inexistente retorna `404 Not Found` (`buscarPorId_comIdInexistente_deveRetornarNotFound`) <!-- source: glossario#L1 -->.
* **BR-07 (Isolamento por Filial):** Consultas de ativos, departamentos, localizações devem respeitar escopo de filiais do usuário (`findByFilialIdIn`) <!-- source: glossario#L1 -->.
* **BR-08 (Health Check Requer Permissão de Filial):** `updateHealthCheck` exige permissão de atualização no contexto da filial do ativo <!-- source: glossario#L1 -->.
* **BR-09 (Hardware Vinculado a Detalhe de Ativo):** Componentes (memória, disco, rede) só existem vinculados a um `AtivoDetalheHardware`; exclusão em cascata via `deleteByAtivoDetalheHardwareId` <!-- source: glossario#L1 -->.
* **BR-10 (Auditoria Automática):** Toda atualização dispara `preUpdate` e `onUpdate` para registrar timestamp/usuário <!-- source: glossario#L1 -->.
* **BR-11 (Tratamento Defensivo de Nulos):** Mappers devem retornar `null` para DTO nulo (`toEntity_deveRetornarNullParaDTONulo`) <!-- source: glossario#L1 -->.
* **BR-12 (Sessão Idempotente):** `logout` e `clearSession` devem ser idempotentes e revogar token imediatamente <!-- source: glossario#L1 -->.
* **BR-13 (Alertas Marcados como Lidos):** `markAsRead` remove status de pendente; alertas lidos não reaparecem em `getRecentAlerts` <!-- source: glossario#L1 -->.
* **BR-14 (Seed de Dados Realistas):** Ambientes de teste/homologação devem ser populados via `RealisticDataSeeder` (complexidade ciclomática 15 — atenção a performance) <!-- source: diagnóstico#L1 -->.

## 7. Assumptions & Dependencies
* **Premissas:**
  1. Backend roda em Java (Spring Boot implícito pela estrutura `src/main/java/br/com/aegispatrimonio/...`) com banco relacional (SQL cru — sem ORM detectado) <!-- source: stack#L1 -->.
  2. Frontend é SPA JavaScript vanilla/ESM (15 arquivos `.js` em `frontend/`) consumindo API REST via `request` (`frontend/src/services/api.js`) <!-- source: stack#L1 -->.
  3. Autenticação stateless via JWT (token no `authInterceptor`) <!-- source: glossario#L1 -->.
  4. Implantação em VM/container único (monolito) — sem k8s, sem auto-scaling horizontal.
  5. Dados de hardware coletados por agente local (fora do escopo) e enviados via `updateHealthCheck`.
  6. LGPD se aplica: logs de auditoria (`onUpdate`/`preUpdate`) não devem gravar dados sensíveis desnecessários.
* **Dependências externas:**
  1. **Infra/Cloud** — provisionamento de VM, banco (PostgreSQL/MySQL/Oracle — a definir), DNS, certificado TLS.
  2. **AD/LDAP** — sincronização de usuários/grupos (futuro; v1 usa `createUsuario` manual).
  3. **E-mail/SMTP** — notificações de aprovação/alerta (não há dependência no código atual).
  4. **ERP/Contábil** — integração de custo/baixa patrimonial (futuro; `custoTotalPorAtivo` é somente leitura no sistema).

## 8. Risks & Mitigations (Business-Level)
| Risco | Probabilidade | Impacto | Mitigação |
| :--- | :--- | :--- | :--- |
| **R1. Complexidade ciclomática alta em `request` (13) e `RealisticDataSeeder.run` (15) causa bugs de regressão e lentidão** | Alta | Alto | Refatorar `request` em funções menores (auth, retry, error handling); dividir `RealisticDataSeeder` em seeders por entidade; adicionar testes de contrato e carga. |
| **R2. Ausência de ORM/migrações versionadas gera drift de schema entre ambientes** | Alta | Alto | Adotar Flyway/Liquibase imediatamente; scripts de migração versionados no repo. |
| **R3. Isolamento por filial (`findByFilialIdIn`) implementado apenas em query, não em camada de serviço → vazamento de dados** | Média | Crítico | Implementar *tenant filter* no `doFilterInternal` (já intercepta requisições) e validar com testes de penetração. |
| **R4. `AtivoMapper.toDTO` com complexidade 14 — risco de N+1 queries e payloads inchados** | Média | Médio | Introduzir DTOs enxutos (list vs detail); usar *fetch join* ou *batch loading*; monitorar tamanho de resposta. |
| **R5. `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17) — lógica de alerta acoplada, difícil de testar/estender** | Média | Médio | Extrair *rules engine* simples (threshold por tipo de ativo); testes unitários por regra. |
| **R6. `Usuario.setUsername` é stub vazio — falha silenciosa ao atualizar login** | Baixa | Alto | Corrigir imediatamente; adicionar teste de regressão. |
| **R7. Console.error/debug em produção (`frontend/src/services/api.js:44,107`) — vazamento de stack trace/tokens** | Baixa | Médio | Remover antes de *release*; configurar *source maps* apenas em dev. |
| **R8. Dependência única `@popperjs/core` sugere UI frágil (sem framework reativo) — manutenibilidade baixa** | Média | Médio | Avaliar migração para React/Vue/Svelte na v2; enquanto isso, padronizar *state management* e componentes. |

## 9. Financial Considerations
> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — orçamento, ROI e headcount são decisão de negócio; não há nenhuma fonte no repositório que sustente um número aqui.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: equipe de 3 devs (2 backend, 1 fullstack), 1 QA, 1 PO; infra AWS t3.medium + RDS db.t3.medium; 12 meses.

* **Investimento estimado:** R$ 720k (12 meses) — breakdown: Pessoal R$ 540k, Infra R$ 60k, Licenças/Ferramentas R$ 30k, Contingência 15% R$ 90k.
* **ROI esperado / Payback:** Break-even no mês 10 (economia projetada R$ 180k/ano vs. custo operacional pós-go-live ~R$ 120k/ano).
* **Modelo de custo recorrente:** Infra ~R$ 5k/mês (compute + DB + backup + monitoramento); Suporte L2 interno 0,5 FTE ~R$ 8k/mês; Evolução contínua 1 dev ~R$ 18k/mês.

## 10. Go-to-Market Considerations
> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — estratégia de lançamento/enablement é decisão de negócio, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: produto interno (não SaaS), rollout por ondas geográficas.

* **Estratégia de lançamento:** **Rollout gradual por filial** — Piloto na matriz (filial 01) por 60 dias → expansão para 3 filiais regionais → nacional. Critério de go/no-go: ≥ 90% dos ativos da filial cadastrados + MTTR ≤ 5h.
* **Comunicação & Enablement:** (1) Workshop de 4h para Gestores de Ativos (cadastro, relatórios, `custoTotalPorAtivo`); (2) Treino de 2h para Service Desk (fluxo de manutenção, `updateHealthCheck`, alertas); (3) Cartilha rápida para Aprovadores (apenas `aprovar`/`cancelar`); (4) Runbook de SysAdmin (seed, backup, `doFilterInternal`, troubleshooting de `authInterceptor`).

## 11. Approval & Sign-off
| Papel | Nome | Status | Data |
| :--- | :--- | :--- | :--- |
| Sponsor (CFO) | [Aguardando] | Pendente | |
| Product Lead | [Aguardando] | Pendente | |
| Engineering Lead (Backend) | [Aguardando] | Pendente | |
| Engineering Lead (Frontend) | [Aguardando] | Pendente | |
| InfoSec / LGPD | [Aguardando] | Pendente | |
| Operações de TI | [Aguardando] | Pendente | |

## 12. Revision History
| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | Product Lead (IA-assisted) | Criação inicial baseada em glossário ubíquo, diagnóstico de código e stack verificada. |