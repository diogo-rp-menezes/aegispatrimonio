# Product Roadmap & Sprint History — AegisPatrimônio (a6)

> **Horizonte de planejamento:** Trimestral · sprints de 2 semanas · **Última atualização:** 28/04/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
>
> **Fontes:** Story Map & Slicing Strategy do AegisPatrimônio (fonte primária — backbone E1–E8, tarefas associadas e regras de negócio BR-01 a BR-10) + diagnóstico determinístico do codebase (353 arquivos, 1.287 funções, 347 classes, ~27.912 LOC, varredura + AST).
>
> **Nota de transparência sobre estimativas:** nenhuma data, capacidade em pontos, velocity ou milestone está declarada no Story Map ou no diagnóstico do codebase. Todos os valores desse tipo neste documento são [INFERIDOS POR IA — REQUER VALIDAÇÃO HUMANA], com as premissas declaradas na Seção 2 e nas seções correspondentes.
>
> **Nota sobre códigos de stories:** o Story Map organiza a jornada em atividades (E1–E8) e tarefas associadas (nomes de funções/casos de uso), mas não define numeração explícita de User Stories; o BRD v1.0 também não define (conforme nota de transparência do próprio Story Map). Os códigos US-001…US-021 abaixo são numeração proposta, derivada diretamente das atividades e tarefas do Story Map — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — e devem ser substituídos pela numeração oficial (REQ-xxx) quando existir.

## 1. Strategic Themes (Now / Next / Later)

> Priorização Now/Next/Later derivada do fatiamento do Story Map, que declara que as atividades 1–4 formam o núcleo obrigatório da jornada e as atividades 5–8 agregam valor de antecipação de falhas, decisão apoiada em custo e rastreabilidade auditável — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].

### Now (Sprint Atual / Trimestre Atual)
* **Núcleo operacional do patrimônio (E1–E3)** — objetivo de negócio: estabelecer a fonte única de verdade do patrimônio, com autenticação por credenciais próprias e encerramento seguro de sessão (BR-07), cadastros base completos do domínio (filiais, departamentos, localizações, tipos de ativo, usuários, credenciais, papéis, permissões, fornecedores, funcionários) e ativos cadastrados de forma íntegra e consultável (BR-02, BR-05). É pré-requisito de todo o restante da jornada: sem cadastros base não há ativo cadastrável, e sem fluxo de aprovação não há manutenção rastreável.

### Next (Próximo Trimestre)
* **Manutenção com aprovação, saúde dos equipamentos e alertas (E4–E6)** — objetivo de negócio: conduzir manutenções solicitáveis, aprováveis, concluíveis e canceláveis com rastreabilidade ponta-a-ponta; registrar health checks e acompanhar o histórico para antecipação de falhas (BR-03, BR-10); reconhecer alertas de uso de recursos e registrar a baixa formal.

### Later (Explorando / Backlog Estratégico)
* **Decisão apoiada em custo e compliance auditável (E7–E8)** — objetivo de negócio: apoiar decisões de reparo, substituição ou desativação com o custo acumulado por bem (`custoTotalPorAtivo`) e garantir trilha de auditoria confiável por registro ("quem alterou o quê e quando"). Ainda sem compromisso de prazo: depende da validação das personas Sponsor/Diretoria e Auditor/Compliance (inferidas no BRD §3) e da maturidade das entregas de Now/Next.
* *Stories candidatas, sem sprint agendada:* US-021 — Trilha de auditoria de modificações (E8); desdobramentos adicionais de E7 (visões de apoio à decisão) a definir com as personas — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]. US-020 (custo por ativo) aparece antecipadamente na Sprint 3 como puxamento do tema Later, sujeito a confirmação.

## 2. Upcoming Sprints

> **Premissas de capacidade e velocity [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]:** sprints de 2 semanas; estimativas em pontos na escala Fibonacci; velocity inicial de 30 pontos/sprint assumida de forma conservadora por inexistência de histórico (composição e capacidade real do time não declaradas nos artefatos — validar no planning).
>
> **Nota de consistência com o codebase:** a varredura determinística mostra que funções associadas a diversas stories abaixo já existem no código (ex.: `createAtivo`, `iniciar`/`aprovar`/`concluir`/`cancelar`, `checkResourceUsageAlerts`, `getHealthHistory`). No planning de cada sprint, validar se a story corresponde a construção nova ou a completura/hardening do que já existe.

### Sprint 1 — 05/05/2025 a 16/05/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** habilitar o núcleo obrigatório da jornada do Story Map (E1–E3): autenticação com credenciais próprias e encerramento seguro de sessão (BR-07), cadastros base completos do domínio patrimonial e cadastro/consulta íntegros de ativos (BR-02, BR-05) — condição prévia para qualquer manutenção rastreável. Conecta ao objetivo do BRD/Story Map de manter a fonte única de verdade do patrimônio.
* **Capacidade do time:** 30 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] · total planejado: 30 pontos
* **Stories planejadas:**
  - [ ] US-001 — Login (incl. `mockLogin` para validação), logout com invalidação imediata do token, encerramento de sessão (`clearSession`) e anexação automática de credenciais às chamadas (`authInterceptor`) (E1; BR-07) (5)
  - [ ] US-002 — Cadastro de filiais, departamentos e localizações prédio/andar/sala (E2) (3)
  - [ ] US-003 — Cadastro de tipos de ativo, fornecedores e funcionários (E2) (3)
  - [ ] US-004 — Gestão de usuários, credenciais, papéis e permissões, incl. vínculo funcionário↔credenciais (`createFuncionarioAndUsuario`) (E2) (5)
  - [ ] US-005 — Cadastro e consulta de ativos com filial, departamento, localização e tipo; listagens completas (BR-02) e busca por identificador com recusa de dados inválidos e "não encontrado" para ID inexistente (BR-05) (E3) (8)
  - [ ] US-006 — Carga de dados realistas (`RealisticDataSeeder`) e refatoração do método `run` (complexidade ciclomática 15 — 2º maior valor do codebase) em métodos menores (E2) (3)
  - [ ] US-007 — Corrigir stub `setUsername` com corpo vazio em `Usuario.java:86` (1)
  - [ ] US-008 — Remover logs residuais do frontend (`console.error` em `api.js:44`, `console.debug` em `api.js:107`) e quebrar a função `request` (complexidade 13) em funções menores (2)
* **Riscos conhecidos:** decisão de persistência pendente — nenhum motor de banco de dados e nenhum ORM/query builder declarados nas dependências do projeto (ver Seção 6); velocity inicial estimada sem histórico; numeração oficial de stories ainda não definida no BRD.

### Sprint 2 — 19/05/2025 a 30/05/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** entregar o fluxo de manutenção com aprovação (E4) — iniciar/solicitar, aprovar, concluir e cancelar, com consultas filtráveis — e o ciclo de monitoramento de saúde (E5) com histórico legível por filial (BR-03) e inventário de hardware (BR-10), completando o núcleo obrigatório da jornada e iniciando o valor de antecipação de falhas.
* **Capacidade do time:** 30 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] · total planejado: 29 pontos
* **Stories planejadas:**
  - [ ] US-009 — Ciclo de manutenção rastreável e auditável: iniciar, aprovar, concluir e cancelar (E4) (8)
  - [ ] US-010 — Consultas de manutenção com filtros combinados (`ManutencaoSpecification`) e refatoração do método `build` (complexidade 14) em métodos menores (5)
  - [ ] US-011 — Health check com dados de hardware e disco e atualização de métricas (`updateHealthCheck`, `updateScalars`) (E5) (5)
  - [ ] US-012 — Histórico de saúde com permissão de leitura por filial (`getHealthHistory`; BR-03) (3)
  - [ ] US-013 — Inventário de hardware: adaptadores de rede, discos e memórias (BR-10) (5)
  - [ ] US-014 — Otimizar caminho de ranking de ativos — TODO de performance em `AtivoService.java:119` (carrega até 1000 candidatos id+nome e faz ranking) (3)
* **Riscos conhecidos:** acoplamento do fluxo de aprovação entre papéis (Gestor de Filial solicita/conduz, Administrador aprova); performance do ranking com alto volume de candidatos; complexidade da validação de permissão de leitura por filial.

### Sprint 3 — 02/06/2025 a 13/06/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** fechar o ciclo operacional com alertas de uso de recursos reconhecíveis e baixa formal (E6) e preparar a Release 1 (Beta Interno) com hardening dos achados da varredura AST — condição de qualidade para expor o sistema às personas mapeadas.
* **Capacidade do time:** 30 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] · total planejado: 21 pontos (folga intencional de ~9 pontos para estabilização da release)
* **Stories planejadas:**
  - [ ] US-015 — Verificação de uso de recursos e listagem de alertas gerais e recentes (`checkResourceUsageAlerts`, `listarAlertas`, `getRecentAlerts`) (E6) (5)
  - [ ] US-016 — Baixa formal de alerta (`markAsRead`) com rastreabilidade (E6) (2)
  - [ ] US-017 — Refatorar `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17 — maior do codebase) em métodos menores (3)
  - [ ] US-018 — Refatorar `AtivoMapper.toDTO` (complexidade 14) em métodos menores (3)
  - [ ] US-019 — Hardening de Release 1: revisão dos achados da varredura AST (12 funções com complexidade alta sinalizadas, stubs e logs residuais) e verificação dos critérios de aceite do BRD (3)
  - [ ] US-020 — Custo total de manutenção por ativo (`custoTotalPorAtivo`) (E7) (5) — puxamento antecipado do tema Later, sujeito a confirmação no planning
* **Riscos conhecidos:** `checkResourceUsageAlerts` é a função mais complexa do codebase (17) — a refatoração exige testes de caracterização para não desestabilizar os alertas; o puxamento de US-020 pode não caber se os alertas exigirem retrabalho.

## 3. Closed Sprints

### Sem registros de sprints concluídas
Nenhuma sprint concluída, ata de retrospectiva ou registro de velocity foi encontrado no workspace (varredura determinística de 353 arquivos) nem nos artefatos-fonte (Story Map, BRD). O histórico de execução não é reconstrutível com confiabilidade a partir do código: a existência de implementações (cadastros, fluxo de manutenção, health checks, alertas) indica desenvolvimento em curso, mas não permite atribuir entregas a sprints específicas nem medir velocity passada.

**O que falta verificar:**
* Ferramenta de gestão de projetos utilizada pelo time e seus registros de sprint/velocity;
* Atas de retrospectiva de ciclos anteriores;
* Se o time opera cerimônias de sprint ou outro ciclo de entrega.

Esta seção deve ser preenchida a partir da Sprint 1 deste roadmap (primeira sprint com datas planejadas).

## 4. Milestones & Releases

| Milestone | Data Alvo | Escopo | Status |
| :--- | :--- | :--- | :--- |
| Checkpoint — Núcleo de cadastros e ativos (E1–E3) | 16/05/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Autenticação (BR-07), cadastros base, ativos com consulta (BR-02, BR-05) e hardening inicial (seeder, stub, logs do frontend) | Planejado |
| Checkpoint — Manutenção e saúde (E4–E5) | 30/05/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Ciclo de manutenção com aprovação, health checks com histórico por filial (BR-03), inventário de hardware (BR-10) | Planejado |
| Beta Interno — Release 1 (E1–E6 + E7 parcial) | 13/06/2025 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Jornada operacional completa (autenticação → cadastros → ativos → manutenção → saúde → alertas), custo por ativo (se US-020 confirmada) e hardening AST | Planejado |
| Release 1 — Produção (GA operacional) | A definir após feedback do Beta Interno [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Ajustes de feedback do Beta, estabilização e critérios de aceite do BRD | Planejado |
| Release 2 — Decisão & Compliance (E7–E8) | Sem data (tema Later) | Custo acumulado por ativo para decisão de reparo/substituição/desativação + trilha de auditoria de modificações | Planejado (sem compromisso de prazo) |

## 5. Velocity Trend

> Sem histórico de sprints concluídas no workspace ou nos artefatos-fonte (ver Seção 3). A coluna "Planejado" reflete as estimativas [INFERIDAS POR IA — REQUER VALIDAÇÃO HUMANA] das sprints futuras; a tendência real só existirá a partir da Sprint 1.

| Sprint | Planejado | Entregue | Observação |
| :--- | :--- | :--- | :--- |
| Sprint 1 | 30 | — | Não iniciada; velocity estimada sem histórico |
| Sprint 2 | 29 | — | Não iniciada |
| Sprint 3 | 21 | — | Não iniciada; folga intencional para estabilização da Release 1 |

## 6. Dependencies & Blockers Ativos

> Donos e previsões de resolução são propostas — [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; nenhum dono está declarado nos artefatos-fonte.

| Item | Bloqueado por | Dono | Previsão de resolução |
| :--- | :--- | :--- | :--- |
| Decisão de persistência: nenhum motor de banco de dados e nenhum ORM/query builder declarados nas dependências do projeto (diagnóstico de stack) — impacta US-002 em diante | Decisão técnica/arquitetural de persistência não registrada nos artefatos | Tech Lead (a designar) | Antes do planning da Sprint 1 |
| Numeração oficial de User Stories (US-xxx/REQ-xxx) e priorização MoSCoW — o BRD v1.0 não define (nota de transparência do Story Map) | Revisão do BRD com stakeholders | Product Owner | Antes da Sprint 1 |
| US-014 — Ranking de ativos: critérios de ranking e limite de candidatos indefinidos (TODO em `AtivoService.java:119`) | Definição de produto (critérios) + investigação de performance | Gestor de Produto + dev responsável | Sprint 2 |
| Validação das personas (Administrador de Patrimônio, Gestor de Filial, Técnico de TI/Monitoramento, Auditor/Compliance — inferidas no BRD §3) | Confirmação com stakeholders reais | Product Owner | Antes da priorização do tema Later (E7–E8) |
| Datas de sprint, capacidade (30 pontos) e velocity — sem histórico no workspace | Confirmação de disponibilidade e capacidade real do time | Scrum Master / PO | Planning da Sprint 1 |