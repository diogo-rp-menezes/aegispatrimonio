# Story Map & Slicing Strategy — AegisPatrimônio (a6)

> Baseado na metodologia de *User Story Mapping* (Jeff Patton). O eixo horizontal representa a jornada do usuário; o eixo vertical representa a priorização por release/slice.
> **Fontes:** BRD v1.0 do AegisPatrimônio (funcionalidades In-Scope, regras de negócio BR-01 a BR-10, escopo, riscos e estratégia de rollout) + diagnóstico determinístico do codebase (353 arquivos, ~27.912 LOC, varredura + AST).
> **Nota de transparência:** o BRD não define priorização MoSCoW nem numeração de releases — todo o fatiamento vertical deste documento é inferência estruturada, sinalizada na Seção 3.

---

## 1. Backbone (Atividades do Usuário)

* **Autenticar e gerir sessão** → **Preparar cadastros base do domínio** → **Cadastrar e consultar ativos patrimoniais** → **Conduzir manutenção com aprovação** → **Monitorar saúde dos equipamentos** → **Tratar alertas de uso de recursos** → **Analisar custo por ativo e decidir** → **Auditar modificações e rastreabilidade**

O backbone é ancorado diretamente nas funcionalidades In-Scope do BRD (§5) e cobre ponta-a-ponta as quatro personas mapeadas (Administrador de Patrimônio, Gestor de Filial, Técnico de TI/Monitoramento e Auditor/Compliance — inferidas, BRD §3). As atividades 1 a 4 formam o núcleo obrigatório da jornada: sem cadastros base não há ativo cadastrável, e sem o fluxo de aprovação não há manutenção rastreável. As atividades 5 a 8 agregam os valores de antecipação de falhas, decisão apoiada em custo acumulado e rastreabilidade auditável.

---

## 2. Horizontal Axis (User Journey Steps)

| Etapa | Persona Principal | Objetivo do Usuário | Tarefas Associadas |
| :--- | :--- | :--- | :--- |
| **E1 — Autenticar-se** | Todas as personas | Acessar o sistema com credenciais próprias e encerrar a sessão com segurança | Login (incl. `mockLogin` para validação); logout com invalidação imediata do token (BR-07); encerramento de sessão (`clearSession`); anexação automática de credenciais a cada chamada (`authInterceptor`) |
| **E2 — Preparar Cadastros Base** | Administrador de Patrimônio | Estruturar o domínio patrimonial e o controle de acesso antes de cadastrar ativos | Filiais (`createFilial`); departamentos (`createDepartamento`); localizações prédio/andar/sala (`createLocalizacao`); tipos de ativo (`createTipoAtivo`); usuários (`createUsuario`); credenciais (`createUserAndToken`); papéis (`createRole`); permissões (`createPermission`); fornecedores (`createFornecedor`); funcionários (`createFuncionario`); vínculo funcionário↔credenciais (`createFuncionarioAndUsuario`); carga de dados realistas (`RealisticDataSeeder`) |
| **E3 — Cadastrar e Consultar Ativos** | Administrador (escrita) · Gestor de Filial (consulta) | Manter a fonte única de verdade do patrimônio, com cadastro íntegro e consultável | Cadastro de ativos com filial, departamento, localização e tipo (`createAtivo`); listagens completas (`listarTodos`, BR-02); busca por identificador (`buscarPorId`); recusa de dados inválidos e "não encontrado" para ID inexistente (BR-05); ranking de ativos (caminho atual com TODO em `AtivoService`:119) |
| **E4 — Conduzir Manutenção** | Gestor de Filial (solicita/conduz) · Administrador (aprova) | Solicitar, aprovar, concluir ou cancelar manutenções com fluxo rastreável e auditável | Início/solicitação (`iniciar`); aprovação (`aprovar`); conclusão (`concluir`); cancelamento (`cancelar`); consultas com filtros combinados (`ManutencaoSpecification`) |
| **E5 — Monitorar Saúde** | Técnico de TI / Monitoramento | Registrar health checks e acompanhar o histórico para antecipar falhas | Health check com dados de hardware e disco (`updateHealthCheck`); atualização de métricas (`updateScalars`); histórico de saúde com permissão de leitura por filial (`getHealthHistory`, BR-03); inventário de hardware — adaptadores de rede, discos e memórias (`findByAtivoDetalheHardwareId`/`deleteByAtivoDetalheHardwareId`, BR-10) |
| **E6 — Tratar Alertas** | Técnico de TI · Gestor de Filial | Reconhecer alertas de uso de recursos e registrar a baixa formal | Verificação de uso de recursos (`checkResourceUsageAlerts`); listagem geral e recentes (`listarAlertas`, `getRecentAlerts`); baixa de alerta (`markAsRead`) |
| **E7 — Analisar Custo e Decidir** | Gestor de Filial · Sponsor/Diretoria | Apoiar decisões de reparo, substituição ou desativação com o custo acumulado por bem | Custo total de manutenção por ativo (`custoTotalPorAtivo`) |
| **E8 — Auditar Modificações** | Auditor / Compliance | Verificar quem alterou o quê e quando, com trilha confiável por registro | Eventos de auditoria