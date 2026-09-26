# Business Requirements Document (BRD) — Aegis1

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Product Lead · **Última atualização:** 15/01/2025  
> **Stakeholders:** Sponsor, Product, Engenharia Frontend, Design, Operações de Manutenção, Compliance

---

## 1. Executive Summary & Vision

O Aegis1 é um sistema de gestão de manutenção de ativos que centraliza o ciclo de vida de ordens de serviço — da criação à conclusão — e fornece visibilidade de custos por ativo. O problema atual é a fragmentação de solicitações em planilhas, e-mails e controles manuais, o que gera retrabalho, perda de rastreabilidade e impossibilidade de analisar custo total por equipamento. A visão é uma interface única, acessível via navegador, que padroniza o fluxo **criar → iniciar → aprovar → concluir/cancelar** e expõe o indicador **custoTotalPorAtivo** para decisões de substituição ou plano de manutenção preventiva.

---

## 2. Problem Statement

* **Problema central:** Equipes de manutenção operam sem sistema único: solicitações chegam por canais informais, não há rastreamento de estado (aberto, em andamento, aprovado, concluído, cancelado) e o custo acumulado por ativo é desconhecido, impedindo gestão baseada em dados.
* **Evidências:** Glossário do domínio revela vocabulário operacional completo (aprovar, iniciar, concluir, cancelar, custoTotalPorAtivo) mas o codebase contém apenas frontend — indicando que o backend existe fora deste repositório e a integração é ponto crítico. Chamadas `console.error/log` residuais em `frontend/src/services/api.js` mostram que tratamento de erro e observabilidade ainda não estão prontos para produção.
* **Custo de não agir (Cost of Inaction):** Continuidade de decisões reativas, impossibilidade de calcular TCO (Total Cost of Ownership) por ativo, risco de não conformidade com normas de manutenção (ex.: NR-10, NR-12) por falta de rastreabilidade documental.

---

## 3. Target Audience & Personas

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — personas são decisão de negócio/pesquisa de usuário, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: organização com equipe de manutenção própria, ativos industriais/facilities, necessidade de rastreabilidade para auditoria.

| Persona | Perfil | Necessidade Principal | Ganho Esperado |
| :--- | :--- | :--- | :--- |
| **Gestor de Manutenção** | Responsável por planejamento, KPIs e orçamento | Visão consolidada de custoTotalPorAtivo e status de ordens | Decisão baseada em dados; redução de paradas não planejadas |
| **Técnico de Campo** | Executa serviços, registra início/fim | Interface simples para **iniciar** e **concluir** ordens no mobile/desktop | Eliminação de papel; registro automático de tempo e materiais |
| **Aprovador/Supervisor** | Valida execução e autoriza fechamento | Ação **aprovar** com evidências (fotos, checklists) | Conformidade auditável; redução de retrabalho |
| **Administrador de Cadastros** | Mantém departamentos, filiais, fornecedores, funcionários | CRUD completo (**criar, listar, buscarPorId, atualizar, deletar**) | Dados mestres consistentes para relatórios e alocação |

---

## 4. Core Objectives & Success Metrics

| Objetivo | KPI (Métrica) | Baseline Atual | Meta | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| Padronizar fluxo de ordens de manutenção | % de ordens tramitando 100% no sistema (sem canais paralelos) | 0% (sistema novo) | ≥ 90% | Q2/2025 |
| Visibilidade de custo por ativo | % de ativos com custoTotalPorAtivo calculado automaticamente | 0% | 100% dos ativos ativos | Q2/2025 |
| Reduzir tempo médio de aprovação | Lead time (iniciar → aprovar) em horas | Desconhecido (sem sistema) | ≤ 24 h | Q3/2025 |
| Eliminar console logs em produção | Quantidade de chamadas `console.*` no bundle | 3 (diagnóstico) | 0 | Antes do go-live |

* **North Star Metric:** **Ordens concluídas no prazo SLA / Total de ordens** — reflete eficiência ponta-a-ponta.
* **Guardrail Metrics:**  
  - Taxa de erro de integração API (handleApiError) < 1%  
  - Latência P95 da chamada `request` < 800 ms  
  - Zero vazamento de dados sensíveis no frontend (authInterceptor)

---

## 5. Scope Boundaries

### In-Scope
* Frontend para gestão de **departamentos, filiais, fornecedores, funcionários** (CRUD completo).
* Ciclo de vida de **solicitação/ordem de manutenção**: criar, iniciar, aprovar, concluir, cancelar, buscarPorId, listar.
* Cálculo e exibição de **custoTotalPorAtivo** a partir de dados retornados pela API.
* Camada de serviço (`api.js`) com `request`, `authInterceptor`, `handleResponse`, `handleApiError`.
* Integração via HTTP com backend existente (fora deste repositório).

### Out-of-Scope
* Backend, banco de dados, autenticação/autorização server-side — responsabilidade de outro time/repositório.
* App mobile nativo (PWA/responsivo apenas).
* Módulo de compras/estoque de peças (apenas fornecedores como cadastro).
* Relatórios avançados/BI — apenas indicadores citados acima.

### Future Considerations (Not Now)
* Checklists digitais e anexos fotográficos nas ordens.
* Agendamento de manutenção preventiva recorrente.
* Integração com sensores IoT para gatilhos automáticos.
* Multi-idioma e white-label.

---

## 6. Business Rules & Constraints

* **BR-01:** Uma ordem só pode ser **iniciada** se estiver no estado "Aberta" e tiver técnico responsável alocado.
* **BR-02:** **Aprovar** exige evidência de execução (checklist assinado ou foto) — regra validada no backend; frontend apenas habilita botão quando API retorna `canApprove: true`.
* **BR-03:** **Concluir** só permitido após **aprovar**; **cancelar** permitido em qualquer estado exceto "Concluída".
* **BR-04:** `custoTotalPorAtivo` é soma de todos os custos (mão de obra + material + terceiros) de ordens **concluídas** vinculadas ao ativo — cálculo no backend, frontend apenas exibe.
* **BR-05:** Exclusão (**deletar**) de entidades mestras (departamento, filial, fornecedor, funcionário) bloqueada se houver ordens vinculadas — backend retorna 409, frontend trata via `handleApiError`.
* **BR-06:** Todas as mutações passam por `authInterceptor` para anexar token JWT; expiração dispara refresh automático antes de retry único.

---

## 7. Assumptions & Dependencies

* **Premissas:**
  1. Backend expõe API REST/JSON compatível com os contratos implícitos no `api.js` (endpoints para cada entidade e ação do glossário).
  2. Backend implementa controle de estado das ordens e cálculo de `custoTotalPorAtivo`.
  3. Autenticação baseada em JWT com refresh token; `authInterceptor` no frontend é suficiente.
  4. Navegadores alvo: últimas 2 versões de Chrome, Edge, Firefox, Safari (desktop e mobile).
* **Dependências externas:**
  1. **Time de Backend** — entrega de endpoints, validações BR-01 a BR-05, estabilidade da API.
  2. **Infra/DevOps** — hospedagem estática (CDN), configuração de CORS, certificados TLS.
  3. **Segurança da Informação** — revisão de armazenamento de token no frontend (localStorage vs. httpOnly cookie).

---

## 8. Risks & Mitigations (Business-Level)

| Risco | Probabilidade | Impacto | Mitigação |
| :--- | :--- | :--- | :--- |
| Backend não entrega endpoints a tempo | Média | Alto | Definir contrato OpenAPI antecipado; mockar API no frontend para desenvolvimento paralelo |
| `request` com complexidade ciclomática 13 (`api.js:36`) gera bugs de integração | Alta | Médio | Refatorar `request` em funções menores (retry, timeout, parsing) antes de testes integrados |
| Console logs em produção expõem dados sensíveis | Baixa | Alto | Pipeline de build deve falhar se `console.*` existir no bundle (eslint/no-console) |
| Falha no `authInterceptor` causa logout em massa | Baixa | Alto | Testes automatizados de expiração/refresh; fallback para tela de login limpa |
| Cálculo de `custoTotalPorAtivo` divergente entre frontend/backend | Média | Médio | Contrato de API define fórmula; teste de contrato (Pact) no CI |

---

## 9. Financial Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — orçamento, ROI e headcount são decisão de negócio; não há nenhuma fonte no repositório que sustente um número aqui.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: equipe de 2 devs frontend + 1 QA por 3 sprints; hospedagem estática de baixo custo.

* **Investimento estimado:** ~3 pessoas-sprint (frontend) + 2 pessoas-sprint (backend, fora deste escopo) + infra mínima.
* **ROI esperado / Payback:** Redução de 15% em horas extras de manutenção corretiva no 1º ano → payback estimado em 6 meses pós-go-live.
* **Modelo de custo recorrente:** Hospedagem CDN (~USD 20/mês), monitoramento de erro (Sentry free tier), renovação de certificados TLS.

---

## 10. Go-to-Market Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — estratégia de lançamento/enablement é decisão de negócio, não algo derivável de `server/`.  
> `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — Premissas: implantação em planta piloto única, depois expansão.

* **Estratégia de lançamento:** **Rollout gradual** — 1ª onda: uma filial piloto + gestor + 5 técnicos (2 semanas); 2ª onda: demais filiais (4 semanas). Critério de passagem: ≥ 80% das ordens da piloto tramitando no sistema.
* **Comunicação & Enablement:**  
  - Treinamento presencial/remoto de 2 h por perfil (gestor, técnico, aprovador, admin).  
  - Quick-reference cards (PDF 1 pág.) para ações principais: iniciar, aprovar, concluir, cancelar.  
  - Canal Slack/Teams dedicado para suporte nas 2 primeiras semanas de cada onda.

---

## 11. Approval & Sign-off

| Papel | Nome | Status | Data |
| :--- | :--- | :--- | :--- |
| Sponsor | | Pendente | |
| Product Lead | | Pendente | |
| Engineering Lead (Frontend) | | Pendente | |
| Engineering Lead (Backend) | | Pendente | |
| Segurança da Informação | | Pendente | |

---

## 12. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | 15/01/2025 | Product Lead (gerado por pipeline) | Criação inicial baseada em glossário ubíquo e diagnóstico determinístico do codebase |