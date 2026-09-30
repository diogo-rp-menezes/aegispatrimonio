# Business Requirements Document (BRD) — a6 · AegisPatrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** A designar (a ser atribuído pelo sponsor) · **Última atualização:** (data da geração)
> **Stakeholders:** Sponsor (Diretoria Administrativa/Financeira), Product, Engenharia (backend e frontend), Gestores de Patrimônio das filiais, TI/Infraestrutura, Auditoria/Compliance

---

## 1. Executive Summary & Vision

A gestão patrimonial da empresa está fragmentada: ativos distribuídos por filiais, departamentos e localizações físicas, manutenções conduzidas de forma reativa e custo por bem invisível para quem decide. O **a6 (AegisPatrimônio)** é o sistema que centraliza o cadastro patrimonial, o fluxo de manutenção com aprovação, os alertas de uso de recursos e o monitoramento de saúde dos equipamentos — com acesso controlado por perfil de usuário e por filial. O projeto já possui implementação substancial verificada em código: 353 arquivos e ~27,9 mil linhas, cobrindo cadastros, manutenção, alertas, saúde de ativos e relatório de custo total por ativo. A visão é ser a **fonte única de verdade do patrimônio**, permitindo decisões de reparo, substituição e desativação apoiadas em dados, e antecipar falhas a partir do histórico de saúde registrado por ativo.

## 2. Problem Statement

* **Problema central:** não existe uma fonte única de verdade para o patrimônio da empresa — os bens estão espalhados por filiais, departamentos e localizações físicas; as manutenções não têm fluxo de aprovação rastreável; o custo acumulado por ativo não é visível; e a condição operacional dos equipamentos não possui histórico que permita antecipar falhas.
* **Evidências:** as funcionalidades já implementadas no código evidenciam as dores de negócio que atendem: (a) cadastro estruturado de ativos com filial, departamento, localização e tipo (`createAtivo`, `createLocalizacao`, `createTipoAtivo`); (b) fluxo de manutenção com aprovação (`iniciar`, `aprovar`, `concluir`, `cancelar`) e consultas com filtros combinados (`ManutencaoSpecification`); (c) alertas de uso de recursos com baixa formal (`checkResourceUsageAlerts`, `getRecentAlerts`, `markAsRead`); (d) histórico de saúde com dados de hardware e disco registrados "para viabilizar análise preditiva de falhas" (`updateHealthCheck`, `getHealthHistory`); (e) indicador de custo total por ativo "para apoiar decisões sobre substituição, reparo ou desativação" (`custoTotalPorAtivo`); (f) trilha de auditoria com registro automático de data/hora de modificação (`onUpdate`/`preUpdate`). A existência de um seeder de dados realistas (`RealisticDataSeeder`) indica a necessidade de homologação com dados próximos da operação real.
* **Custo de não agir (Cost of Inaction):** manutenção reativa consistentemente mais cara que a preditiva; decisões de substituição tomadas sem o custo acumulado por ativo; risco de perda e desvio de bens sem rastreabilidade por filial/localização; auditorias sem trilha confiável de quem alterou o quê e quando.

## 3. Target Audience & Personas

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
> *Premissas adotadas:* os perfis técnicos ADMIN/USER e o modelo de permissão contextual (por filial/departamento) foram verificados nos cenários de teste extraídos no Glossário; os cargos/ personas abaixo são inferência do domínio patrimonial — não há pesquisa de usuário no repositório.

| Persona | Perfil | Necessidade Principal | Ganho Esperado |
| :--- | :--- | :--- | :--- |
| Administrador de Patrimônio | Perfil ADMIN — responsável pelo cadastro central | Criar, atualizar e excluir cadastros de ativos, filiais, departamentos, localizações, tipos de ativo, fornecedores, funcionários e usuários | Cadastro íntegro e centralizado, com validação de dados recusando entradas inválidas e trilha de auditoria automática |
| Gestor de Filial | Perfil USER — opera uma unidade | Consultar listagens autorizadas à sua filial, acompanhar alertas e conduzir solicitações de manutenção (iniciar, acompanhar aprovação, concluir) | Visibilidade do patrimônio da sua unidade e fluxo de manutenção com aprovação formal |
| Técnico de TI / Monitoramento | Responsável pela saúde dos equipamentos | Registrar health checks (dados de hardware e disco), manter métricas atualizadas e acompanhar histórico e alertas de uso de recursos | Antecipação de falhas e priorização de manutenção baseada em evidência |
| Auditor / Compliance | Auditoria interna | Consultar eventos de auditoria e histórico de modificações dos registros | Trilha de rastreabilidade por registro, com data/hora de última alteração garantida |

## 4. Core Objectives & Success Metrics

| Objetivo | KPI (Métrica) | Baseline Atual | Meta | Prazo |
| :--- | :--- | :--- | :--- | :--- |
| Centralizar e completar o cadastro patrimonial | % de ativos com cadastro completo (filial, departamento, localização, tipo de ativo) | Não medido (sem telemetria no repositório) | ≥ 95% *[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]* | 1º trimestre pós-go-live |
| Restringir escrita a administradores com acesso contextual | % de operações de escrita cobertas por `hasPermission`; nº de incidentes de acesso indevido | Cobertura já verificada em cenários de teste (criar/atualizar/deletar com USER → Forbidden) | 0 incidentes em produção | Contínuo |
| Gerir manutenção com fluxo de aprovação auditável | `custoTotalPorAtivo` disponível por ativo; tempo médio do ciclo `iniciar` → `concluir` | Não medido | 100% dos ativos com custo acumulado visível | 2º trimestre pós-go-live |
| Antecipar falhas com monitoramento de saúde | % de ativos com `updateHealthCheck` atualizado; % de alertas de uso de recursos reconhecidos (`markAsRead`) dentro do SLA | Não medido | ≥ 90% dos ativos monitorados | 2º trimestre pós-go-live |
| Garantir rastreabilidade e auditoria | % de registros com data/hora de última modificação (`onUpdate`/`preUpdate`) | Mecanismo implementado no código | 100% | Go-live |

* **North Star Metric:** nº de **ativos sob gestão completa** — ativos com cadastro completo + manutenção rastreada no fluxo de aprovação + saúde monitorada via health check.
* **Guardrail Metrics:** latência das consultas de listagem (área sensível — ver TODO de performance em `AtivoService`), taxa de erros de API tratados ao usuário (`handleApiError`), incidentes de acesso indevido, custo de infraestrutura por usuário.

> *Nota de transparência: os baselines não existem no repositório (não há telemetria instrumentada); devem ser medidos na primeira onda de operação. As metas são propostas derivadas do escopo funcional real e requerem validação do sponsor.*

## 5. Scope Boundaries

### In-Scope
* **Cadastros base:** ativos patrimoniais (`createAtivo`), tipos de ativo (`createTipoAtivo`), filiais (`createFilial`), departamentos (`createDepartamento`), localizações físicas — prédio, andar, sala (`createLocalizacao`), fornecedores (`createFornecedor`), funcionários (`createFuncionario`) e vínculo funcionário↔credenciais (`createFuncionarioAndUsuario`).
* **Usuários e controle de acesso:** usuários (`createUsuario`), emissão de credenciais (`createUserAndToken`), papéis (`createRole`), permissões (`createPermission`), verificação contextual perfil + filial/departamento (`hasPermission`), filtro de controle de acesso por requisição (`doFilterInternal`) e interceptador de credenciais no frontend (`authInterceptor`).
* **Fluxo de manutenção:** solicitação/início (`iniciar`), aprovação (`aprovar`), conclusão (`concluir`), cancelamento (`cancelar`); consultas com filtros combinados (`ManutencaoSpecification`).
* **Monitoramento de saúde dos ativos:** registro de health check com dados de hardware e disco (`updateHealthCheck`), atualização de métricas (`updateScalars`), histórico de saúde com permissão de leitura por filial (`getHealthHistory`), inventário de hardware — adaptadores de rede, discos e memórias (`findByAtivoDetalheHardwareId` / `deleteByAtivoDetalheHardwareId`).
* **Alertas:** verificação de uso de recursos (`checkResourceUsageAlerts`), listagem geral e recentes (`listarAlertas`, `getRecentAlerts`), baixa de alerta (`markAsRead`).
* **Relatórios gerenciais:** custo total de manutenção por ativo (`custoTotalPorAtivo`).
* **Auditoria e rastreabilidade:** eventos de auditoria, registro automático de data/hora de modificação (`onUpdate`/`preUpdate`).
* **Sessão:** login (incl. `mockLogin` para validação), logout com invalidação imediata do token, encerramento de sessão (`clearSession`).
* **Carga de dados realista para homologação** (`RealisticDataSeeder`).

### Out-of-Scope
* **Empacotamento desktop via Tauri** — o repositório não possui `src-tauri/Cargo.toml`.
* **Integrações com sistemas de terceiros** (ERP, financeiro, gateways) — nenhuma dependência de produção além de `@popperjs/core` está declarada.
* **Canais de notificação externos** (e-mail, SMS, push) — não evidenciados no código analisado.
* **Aplicativo mobile nativo.**

### Future Considerations (Not Now)
* **Análise preditiva de falhas** sobre o histórico de saúde — os dados de hardware e disco já são registrados para viabilizar esse fim, mas a análise avançada é adiada.
* **Otimização do caminho de ranking de ativos** — TODO em `AtivoService` (linha 119): o caminho carrega até 1000 candidatos (id+nome) e faz ranking em memória.
* **Empacotamento desktop/mobile** e distribuição instalável.
* **Integrações externas** (ERP/financeiro), cogitadas mas conscientemente adiadas.

## 6. Business Rules & Constraints

* **BR-01:** Criação, alteração e exclusão de cadastros (ativos, filiais, departamentos, fornecedores, funcionários, tipos de ativo) são **restritas a administradores** — usuários com perfil comum (USER) recebem acesso negado. Evidência: `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`.
* **BR-02:** Usuários autenticados **podem consultar** listagens completas de registros. Evidência: `listarTodos_comUser_deveRetornarOk`.
* **BR-03:** O acesso ao **histórico de saúde de um ativo** exige permissão de leitura **na filial à qual o ativo pertence**. Evidência: `getHealthHistory`.
* **BR-04:** A autorização (`hasPermission`) considera sempre o **perfil de acesso e o contexto** (filial/departamento) — RBAC com contexto; é a regra central que protege as operações sobre o patrimônio.
* **BR-05:** Cadastros com **dados inválidos ou incompletos são recusados**; consultas por identificador inexistente retornam "não encontrado", nunca dados incorretos. Evidência: `criar_comDadosInvalidos_deveRetornarBadRequest`, `buscarPorId_comIdInexistente_deveRetornarNotFound`.
* **BR-06:** Operações bem-sucedidas de criação e exclusão por administrador retornam **Created** e **NoContent**, respectivamente. Evidência: `criar_comAdmin_deveRetornarCreated`, `deletar_comAdmin_deveRetornarNoContent`.
* **BR-07:** O **logout invalida imediatamente o token de acesso**; sessão encerrada (`clearSession`) exige nova autenticação.
* **BR-08:** Todo registro alterado mantém **data/hora da última modificação** registrada automaticamente — rastreabilidade obrigatória. Evidência: `onUpdate`, `preUpdate`.
* **BR-09:** A conversão DTO→entidade só ocorre com dados válidos; **DTO nulo não gera registro**. Evidência: `toEntity_deveRetornarNullParaDTONulo`, `toEntity_deveMapearDTOparaEntidade`.
* **BR-10:** A atualização do **inventário de hardware** de um ativo limpa os componentes antigos (adaptadores de rede, discos, memórias) antes de registrar os novos. Evidência: `deleteByAtivoDetalheHardwareId` + `findByAtivoDetalheHardwareId`.

**Constraints (restrições técnicas e de processo):**
* **C-01:** Stack declarada verificada: única dependência de produção é `@popperjs/core ^2.11.8`; **nenhum motor de banco ou ORM/query builder está declarado nas dependências** — o mecanismo real de persistência precisa ser confirmado pela engenharia antes de qualquer decisão de infraestrutura.
* **C-02:** Código composto por 338 arquivos `.java` (em `src/`) e 15 arquivos `.js` (em `frontend/`); nomenclatura em inglês no código, comentários e documentação em português (regra do Glossário).
* **C-03:** Termos canônicos do Glossário devem ser usados em código e documentação (ex.: usar "aprovar", nunca "homologar/deferir/ratificar").
* **C-04:** O método `Usuario.setUsername` está com **corpo vazio (stub)** — potencial comportamento incompleto na atualização de nome de usuário até sua resolução.

## 7. Assumptions & Dependencies

* **Premissas:**
  * O produto é o sistema **AegisPatrimônio** (namespace `br.com.aegispatrimonio` nos fontes; codinome "a6" no pipeline de documentação).
  * Backend e frontend se comunicam pela camada de serviços do frontend (`request`/`handleResponse`/`handleApiError` em `frontend/src/services/api.js`), com credenciais anexadas automaticamente a cada chamada (`authInterceptor`).
  * Os perfis ADMIN/USER e o modelo de permissão contextual descritos neste BRD refletem o comportamento verificado nos cenários de teste extraídos do Glossário.
  * Existe persistência de dados em algum mecanismo **não declarado** nas dependências do `package.json` — premissa a ser esclarecida pela engenharia antes da fase de infraestrutura.
* **Dependências externas:**
  * **Nenhuma** dependência de produção declarada além de `@popperjs/core ^2.11.8`; **nenhuma** API de terceiros identificada nos fontes.
  * Nenhum fornecedor externo ou exigência regulatória específica identificada no código — verificar com Compliance quais normas de gestão patrimonial aplicáveis ao setor devem ser atendidas.

## 8. Risks & Mitigations (Business-Level)

| Risco | Probabilidade | Impacto | Mitigação |
| :--- | :--- | :--- | :--- |
| Complexidade ciclomática alta em pontos críticos: `checkResourceUsageAlerts` (17), `RealisticDataSeeder.run` (15), `AtivoMapper.toDTO` (14), `ManutencaoSpecification.build` (14), `request` no frontend (13) | Média | Alto | Refatorar em funções/métodos menores antes de evoluir essas áreas; manter testes de regressão (os cenários de permissão já extraídos servem de base) |
| TODO de performance em `AtivoService` (carrega até 1000 candidatos e faz ranking em memória) | Média | Médio | Empurrar ranking/filtragem para a camada de consulta e paginar; monitorar latência das listagens como guardrail |
| `console.error`/`console.debug` residuais em `frontend/src/services/api.js` (linhas 44 e 107) | Alta | Baixo | Remover antes de produção e adicionar verificação automatizada no pipeline |
| Stub `Usuario.setUsername` com corpo vazio | Média | Médio | Implementar o comportamento ou remover o campo do fluxo de atualização; cobrir com teste de atualização de usuário |
| Persistência não declarada nas dependências (sem motor de banco/ORM conhecido) | Média | Alto | Levantamento técnico urgente da stack real de dados; documentar antes de decisões de infraestrutura ou escalabilidade |
| `package.json` sem nome e sem scripts | Alta | Médio | Nomear o pacote e automatizar scripts de build/verificação |
| Configuração incorreta de permissões por filial bloqueando consultas legítimas (`findByFilialIdIn` restringe por filiais autorizadas) | Baixa | Médio | Manter matriz de permissões por filial e testes dedicados de acesso por filial |

## 9. Financial Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
> *Premissas adotadas:* não há nenhum dado de orçamento, custo ou headcount no repositório; a estimativa abaixo usa apenas o porte verificado do codebase (353 arquivos, ~27.912 LOC, 1.287 funções, 347 classes) como proxy de esforço.

* **Investimento estimado:** evolução e sustentação contínua de um sistema deste porte por um time pequeno (cenário de referência: 3–5 pessoas entre backend, frontend e QA). Número a validar pelo sponsor.
* **ROI esperado / Payback:** redução do custo reativo de manutenção via monitoramento de saúde e alertas de uso de recursos; decisões de substituição/desativação apoiadas pelo custo acumulado por ativo (`custoTotalPorAtivo`). Payback a estimar com dados reais de perda de bens e tempo de parada — não estimável a partir do repositório.
* **Modelo de custo recorrente:** hospedagem da aplicação (backend + frontend); **sem licenças de terceiros identificadas** — a única dependência de produção declarada (`@popperjs/core`) não implica custo de licenciamento. Headcount de sustentação conforme cenário acima.

## 10. Go-to-Market Considerations

> `[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
> *Premissas adotadas:* produto de uso interno corporativo (gestão patrimonial multi-filial); a existência do `RealisticDataSeeder` sugere homologação com dados realistas antes da operação.

* **Estratégia de lançamento:** **rollout gradual por filial** — piloto em uma filial com carga de dados realista, validação do fluxo de aprovação e das permissões por filial, seguida de expansão para as demais unidades. Big bang não recomendado dado o modelo de permissão contextual (risco de bloquear consultas legítimas em escala).
* **Comunicação & Enablement:** treinamento de administradores de patrimônio (perfil ADMIN) nos cadastros e no fluxo de manutenção; material de consulta canônico baseado no Glossário (Ubiquitous Language) para uniformizar termos entre áreas; orientação aos usuários comuns (perfil USER) sobre o que podem consultar (listagens, alertas, histórico de saúde da própria filial) e solicitar (manutenções).

## 11. Approval & Sign-off

> Nomes dos aprovadores a preencher pelos stakeholders responsáveis — não há sign-off definido nos fontes analisados.

| Papel | Nome | Status | Data |
| :--- | :--- | :--- | :--- |
| Sponsor | | Pendente | |
| Product Lead | | Pendente | |
| Engineering Lead | | Pendente | |

## 12. Revision History

| Versão | Data | Autor | Mudanças |
| :--- | :--- | :--- | :--- |
| 1.0 | (data da geração) | Pipeline de documentação a6 | Criação inicial com base no diagnóstico determinístico do codebase (varredura + AST de 353 arquivos) e no Glossário (Ubiquitous Language) |