# Product Roadmap & Sprint History — Sistema de Gestão de Patrimônio (AegisPatrimonio)

> **Horizonte de planejamento:** Trimestral · **Última atualização:** baseline inicial gerado por IA — data de calendário real a registrar na consolidação [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

> **Fonte primária:** Story Map & Slicing Strategy (A4) do Sistema de Gestão de Patrimônio — codebase `AegisPatrimonio` (pacote `br.com.aegispatrimonio`). Todos os itens de backlog são referenciados pelos identificadores do BRD (RF-XX, RNF-XX, UC-XX, CA-XX, RN-XX, R-XX) para rastreabilidade direta.

### Premissas de Planejamento [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

O Story Map (A4) define slices, stories e riscos de slicing, mas **não define sprints, datas, capacidade em pontos nem velocity**. Todo o sequenciamento deste roadmap deriva dos slices MoSCoW do Story Map (Slice 1/MVP → Slice 2/Should → Slice 3/Could) e das stories RF-XX/RNF-XX neles listadas. Premissas adotadas:

1. **Sprints de 2 semanas**, iniciadas após a validação do fatiamento MoSCoW com o Product Owner (premissa do próprio Story Map).
2. **Squad único dedicado**, com capacidade inicial de referência de **24 pontos/sprint** (escala Fibonacci), a ser calibrada com velocity real a partir da Sprint 1.
3. **Pontos por story são estimativas relativas de complexidade** — não declaradas no Story Map — e devem ser refinados em cerimônia de planning.
4. **Datas de milestone são relativas ao kick-off** (em semanas), não de calendário — o BRD não define prazos.
5. **Partes das stories RF-XX podem já estar implementadas** no codebase existente (350 arquivos de código, 27.537 linhas de código (LOC), conforme diagnóstico determinístico do workspace) — a verificação story-a-story é pré-requisito da Sprint 1 (ver seção 6).

---

## 1. Strategic Themes (Now / Next / Later)

### Now (Sprint Atual / Trimestre Atual)
* **MVP — Jornada Ponta-a-Ponta de Gestão de Manutenção (Release 1 · Sprints 1–5)** — entregar o fluxo de manutenção **Pendente → Aprovada → Concluída** (CA-04), precedido de autenticação JWT com RBAC Admin/User (CA-06..CA-08) e dos cadastros mestres mínimos para registrar e consultar ativos; portão de saída CA-01 (escopo fatiado), CA-02, CA-03, CA-05, CA-10 e gates de qualidade RNF-04/RNF-05/RNF-10. *(Story Map: Slice 1 — Must Have)*

### Next (Próximo Trimestre)
* **Integridade Cadastral e Inteligência de Custo (Release 2 · Sprints 6–8)** — fechar o CRUD integral das entidades mestres (CA-01 completo), completar o ciclo de vida do ativo (baixa RF-13 e depreciação RF-16), agregar custo total por ativo (RF-17/CA-09), evoluir o RBAC para permissões granulares (RF-27 — mitigação R-01 do BRD) e implantar a trilha de auditoria imutável (RNF-07). *(Story Map: Slice 2 — Should Have)*

### Later (Explorando / Backlog Estratégico)
* **Experiência, Adoção e Visibilidade Gerencial (Backlog Could — sem compromisso de prazo)** — interface responsiva (desktop/tablet) com experiência **mobile-first para solicitações** (RNF-08); notificação à Equipe de Manutenção após aprovação (derivada da pós-condição de UC-03 — meio de entrega não definido no BRD) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; relatórios gerenciais de custo médio por ativo/ano (KPI do BRD: redução de 15% YoY — escopo detalhado do relatório não definido no BRD) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; reavaliação da estratégia de escalabilidade (RNF-06) após a definição da arquitetura de deploy.

> **Fora de escopo neste ciclo (Won't Have, conforme Story Map):** permissões granulares finas além do escopo de RF-27; migração de dados legados (R-02 — tratada como trabalho de implantação separado); notificações push (mitigação R-04); escalabilidade horizontal dedicada (RNF-06 — adiada até a definição da arquitetura de deploy).

---

## 2. Upcoming Sprints

> Sequenciamento derivado dos slices do Story Map: as **35 stories RF/RNF dos Slices 1 e 2** estão integralmente alocadas às Sprints 1–8; os itens do Slice 3 permanecem no Later (seção 1). Datas em semanas relativas ao kick-off — ver Premissas de Planejamento.

### Sprint 1 — Fundações: Autenticação, RBAC e Contratos de Erro
* **Período:** semanas 1–2 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Usuários autenticam via JWT com sessão segura e atuam conforme o RBAC Admin/User, e a API responde com erros 400/404 padronizados — base transversal de todas as jornadas do Story Map (Etapa 1 do backbone; BRD: CA-06, CA-07, CA-08, RN-03, RN-04).
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-23 — Login/Autenticação com retorno de JWT (5 pts)
  - [ ] RF-24 — Interceptador de Auth com injeção automática de token no frontend (3 pts)
  - [ ] RF-25 — Controle de Sessão (logout / `clearSession`) (2 pts)
  - [ ] RF-26 — RBAC com roles **Admin** e **User** — 403 para User em operações de escrita (5 pts)
  - [ ] RF-28 — Validação de dados: 400 Bad Request com mensagens claras (2 pts)
  - [ ] RF-29 — Tratamento de ID inexistente: 404 Not Found padronizado (2 pts)
  - [ ] RNF-01 — JWT com expiração (1h) e refresh (7 dias) (3 pts)
  - [ ] RNF-02 — Senhas hasheadas, nunca armazenadas em plain text (2 pts)
* **Riscos conhecidos:** MVP sem gestão de Funcionários (RF-08 fatiado para a Release 2) — usuários precisarão existir para login; mitigação via carga/ajuste manual dos cadastros, apoiada pelo seeder existente no código (`RealisticDataSeeder`). Risco residual de retrabalho nas regras de 403 quando roles adicionais forem introduzidas (R-01).

### Sprint 2 — Cadastros Mestres Mínimos
* **Período:** semanas 3–4 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Admin cadastra a estrutura organizacional mínima (departamentos, filiais, fornecedores, localizações e tipos de ativo) que sustenta o cadastro e a alocação de ativos — Etapa 2 do backbone; escrita exclusiva Admin com 403 para User (RN-01/RN-02).
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-01 — Criar Departamento (3 pts)
  - [ ] RF-04 — Buscar Departamento por ID (2 pts)
  - [ ] RF-05 — Listar Departamentos com paginação (3 pts)
  - [ ] RF-06 — Criar Filial (3 pts)
  - [ ] RF-07 — Criar Fornecedor (3 pts) — Must por ser exigido pelo cadastro de ativo (RF-11)
  - [ ] RF-09 — Criar Localização (3 pts)
  - [ ] RF-10 — Criar Tipo de Ativo (3 pts)
* **Riscos conhecidos:** RN-01/RN-02 devem ser aplicadas consistentemente em todos os endpoints de escrita (dependência direta do RBAC entregue na Sprint 1); folga de capacidade (20 de 24 pts) reservada para correções carry-over da Sprint 1.

### Sprint 3 — Acervo de Ativos
* **Período:** semanas 5–6 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Acervo patrimonial operacional — registrar, corrigir/transferir e consultar ativos com filtros por tipo, filial, departamento, status e localização — Etapa 3 do backbone; pré-requisito do passo 2 de UC-02 (seleção de ativo na solicitação de manutenção).
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-11 — Cadastrar Ativo (tipo, filial, departamento, localização, fornecedor, valor, data de aquisição) (5 pts)
  - [ ] RF-12 — Atualizar Ativo (correção cadastral, transferência, status) (5 pts)
  - [ ] RF-14 — Consultar Ativo por ID com histórico (3 pts)
  - [ ] RF-15 — Listar/Filtrar Ativos (5 pts)
* **Riscos conhecidos:** TODO de performance no caminho de listagem/ranking de ativos (`AtivoService.java:119` — carrega até 1000 candidatos id+nome) — monitorar o p95 (RNF-04) durante o MVP e otimizar paginação/filtragem antes da Release 2.

### Sprint 4 — Ciclo de Manutenção Ponta-a-Ponta
* **Período:** semanas 7–8 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Fluxo **Pendente → Aprovada → Concluída** funcional com histórico de manutenção visível — fecha a jornada ponta-a-ponta mínima do MVP (CA-04); etapas 4–7 do backbone (UC-02, UC-03, UC-04).
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-18 — Criar Solicitação de Manutenção (→ **Pendente**) (5 pts)
  - [ ] RF-19 — Aprovar Solicitação (**Pendente** → **Aprovada**) (3 pts)
  - [ ] RF-20 — Cancelar Solicitação em qualquer estado (CA-05) (2 pts)
  - [ ] RF-21 — Concluir Manutenção (**Aprovada/Em Andamento** → **Concluída**) (3 pts)
  - [ ] RF-22 — Histórico de Manutenção por ativo (3 pts)
* **Riscos conhecidos:** Experiência degradada para o Funcionário (R-04 do BRD) — o MVP entrega a solicitação sem a experiência mobile-first (RNF-08 no Could); mitigação: validar o fluxo de solicitação com usuários reais já nesta sprint e antecipar RNF-08 para a Release 2 caso a adoção fique abaixo do esperado.

### Sprint 5 — Hardening e Portão de Saída do MVP
* **Período:** semanas 9–10 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** MVP pronto para lançar — portão de saída do Story Map atendido (CA-01 no escopo fatiado, CA-02, CA-03, CA-04, CA-05, CA-10) e gates de qualidade RNF-04/RNF-05/RNF-10 verificados; dívida técnica estática crítica resolvida antes de produção.
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RNF-03 — HTTPS obrigatório em produção (TLS 1.2+) (2 pts)
  - [ ] RNF-10 — Cobertura de testes > 80%, bloqueando o pipeline de CI/CD (5 pts)
  - [ ] CA-10 — Testes de integração cobrindo os cenários de permissão Admin vs User (3 pts)
  - [ ] Verificação de RNF-04 (tempo de resposta p95 < 200ms) e RNF-05 (uptime 99,5%) em staging/produção (2 pts)
  - [ ] Dívida técnica estática do diagnóstico do workspace (5 pts) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — derivado do diagnóstico do workspace, não do Story Map]**: refatorar as funções com complexidade ciclomática alta (`frontend/src/services/api.js:54` — `request`, 13; `RealisticDataSeeder.java:34` — `run`, 15; `AtivoMapper.java:15` — `toDTO`, 14; `ManutencaoSpecification.java:26` — `build`, 14; `AlertNotificationService.java:96` — `checkResourceUsageAlerts`, 17); remover as chamadas `console.error`/`console.debug` residuais (`api.js:44`, `api.js:107`); implementar o stub `Usuario.setUsername` (`Usuario.java:86`).
* **Riscos conhecidos:** Adiamento da escalabilidade (RNF-06) — a reavaliação da arquitetura de deploy pressupõe decisão pós-MVP; risco de retrabalho se os requisitos de escala (10k+ ativos, 1k+ usuários simultâneos) se tornarem prioritários antes da Release 2. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

### Sprint 6 — CRUD Integral das Entidades Mestres (Release 2)
* **Período:** semanas 11–12 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Fechar o CA-01 integral (CRUD completo das entidades mestres) e documentar automaticamente a API — objetivo declarado do Slice 2 do Story Map.
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-02 — Atualizar Departamento (2 pts)
  - [ ] RF-03 — Excluir Departamento (retorna 204 NoContent) (2 pts)
  - [ ] RF-08 — Criar Funcionário, com vinculação opcional a usuário (`createFuncionarioAndUsuario`, RN-06) (5 pts)
  - [ ] RNF-09 — Documentação automática da API REST (3 pts)
* **Riscos conhecidos:** Folga de capacidade (12 de 24 pts) reservada para antecipar RNF-08 (mobile-first) caso a adoção do MVP fique abaixo do esperado (risco R-04 do BRD).

### Sprint 7 — Ciclo de Vida do Ativo e Inteligência de Custo (Release 2)
* **Período:** semanas 13–14 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Completar o ciclo de vida do ativo (baixa e depreciação) e entregar o custo total por ativo batendo com a soma das ordens concluídas (CA-09).
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-13 — Baixar/Excluir Ativo (Admin only) (3 pts)
  - [ ] RF-16 — Cálculo de Depreciação (valor residual por método/vida útil) (5 pts)
  - [ ] RF-17 — Custo Total por Ativo (`custoTotalPorAtivo`) (5 pts)
* **Riscos conhecidos:** Performance em consultas pesadas (R-03 do BRD) — RF-17 pode sofrer timeout; mitigação via paginação e avaliação de performance conforme previsto no próprio BRD.

### Sprint 8 — RBAC Granular e Auditoria (Release 2)
* **Período:** semanas 15–16 do plano [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Meta Central (Sprint Goal):** Evoluir o RBAC para permissões granulares (mitigação R-01 do BRD) e implantar a trilha de auditoria imutável de todas as operações de escrita (RNF-07) — fecha o Slice 2.
* **Capacidade do time:** 24 pontos [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Stories planejadas:**
  - [ ] RF-27 — Gestão de Permissões/Roles (`createRole`, `createPermission`) (5 pts)
  - [ ] RNF-07 — Trilha de auditoria imutável de todas as operações de escrita (5 pts)
* **Riscos conhecidos:** Risco residual de retrabalho nas regras de 403 ao introduzir roles adicionais (R-01); folga (10 de 24 pts) reservada para estabilização da Release 2.

---

## 3. Closed Sprints

> **Nenhum registro de sprints concluídas foi encontrado** no workspace (varredura determinística) nem nos artefatos-fonte (Story Map A4 / BRD). O codebase já contém 350 arquivos de código (27.537 LOC), o que indica esforço de desenvolvimento prévio, mas **sem histórico de sprints, velocity ou retrospectivas documentado** — este histórico precisa ser verificado com o time (ferramenta de gestão de projetos, board e atas de retrospectiva) e consolidado nesta seção.

* **Ação requerida:** popular esta seção a partir dos registros reais da ferramenta de gestão ou, na ausência deles, a partir da primeira retrospectiva realizada sob este roadmap. Enquanto isso, a **Sprint 1** (seção 2) é tratada como a primeira sprint rastreável deste plano.
* **Status do baseline:** este documento estabelece o primeiro baseline de planejamento do projeto; não existem dados de "planejado vs. entregue" anteriores a ele.

---

## 4. Milestones & Releases

| Milestone | Data Alvo | Escopo | Status |
| :--- | :--- | :--- | :--- |
| **MVP funcional (Release 1)** | ~10 semanas após o kick-off (5 sprints de 2 semanas) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Slice 1 (Must): RF-23..RF-26, RF-01/04/05/06/07/09/10, RF-11/12/14/15, RF-18..RF-22, RF-28/29, RNF-01/02/03 — ~94 pontos [INFERIDO POR IA] | Planejado |
| **Portão de Saída do MVP (Release Gate)** | Imediatamente após a Sprint 5 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | CA-01 (escopo fatiado), CA-02, CA-03, CA-04, CA-05, CA-10; RNF-04 (p95 < 200ms), RNF-05 (uptime 99,5%), RNF-10 (cobertura > 80%) | Planejado |
| **Release 2 — Integridade Cadastral e Inteligência de Custo** | ~16 semanas após o kick-off (3 sprints adicionais) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Slice 2 (Should): RF-02/03/08, RF-13/16/17, RF-27, RNF-07, RNF-09 — 35 pontos [INFERIDO POR IA] | Planejado |
| **Backlog Could (Release 3 exploratória)** | Sem data comprometida | RNF-08 (mobile-first), notificação pós-aprovação, relatórios de custo médio por ativo/ano; reavaliação de RNF-06 | Exploratório |

---

## 5. Velocity Trend

> Não há histórico de velocity anterior a este baseline — nenhum registro de sprints concluídas foi encontrado no workspace ou nos artefatos-fonte. Os valores de "Entregue" serão preenchidos ao fechamento de cada sprint; os valores "Planejado" são estimativas [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].

| Sprint | Planejado | Entregue | Observação |
| :--- | :--- | :--- | :--- |
| 1 | 24 pts | — | Fundações de acesso; velocity de referência a calibrar |
| 2 | 20 pts | — | Cadastros mestres mínimos |
| 3 | 18 pts | — | Acervo de ativos |
| 4 | 16 pts | — | Ciclo de manutenção ponta-a-ponta |
| 5 | 17 pts | — | Hardening e portão de saída do MVP |
| 6 | 12 pts | — | CRUD integral (Release 2) |
| 7 | 13 pts | — | Ciclo de vida do ativo e custo (Release 2) |
| 8 | 10 pts | — | RBAC granular e auditoria (Release 2) |

---

## 6. Dependencies & Blockers Ativos

| Item | Bloqueado por | Dono | Previsão de resolução |
| :--- | :--- | :--- | :--- |
| Verificação story-a-story das stories RF-XX contra o codebase existente (350 arquivos — 335 `.java` em `src/`, 15 `.js` em `frontend/`, 27.537 LOC) | Ausência de rastreabilidade story↔código; o diagnóstico determinístico do workspace é o único ponto de partida disponível | Product Owner + squad de desenvolvimento [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Antes da Sprint 1 [INFERIDO POR IA] |
| Validação do fatiamento MoSCoW e da necessidade de custo acumulado (RF-17) na aprovação do MVP | Decisão pendente do Product Owner (premissa 6 do Story Map) | Product Owner | Antes da Sprint 2 [INFERIDO POR IA] |
| Definição da arquitetura de deploy e persistência — pré-requisito para RNF-03 (HTTPS), RNF-05 (uptime 99,5%) e reavaliação de RNF-06 (escalabilidade) | Infraestrutura não definida: o diagnóstico não identificou motor de banco nem ORM nas dependências declaradas do projeto | Responsável por infraestrutura/deploy [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Antes do portão de saída do MVP (Sprint 5) [INFERIDO POR IA] |
| Carga de cadastros de usuários no MVP (RN-06 não coberta — RF-08 na Release 2) | Fatiamento do Story Map (RF-08 no Slice 2) | Administrador do Sistema | Durante a implantação do MVP [INFERIDO POR IA] |
| Performance da listagem de ativos (TODO `AtivoService.java:119` — até 1000 candidatos por requisição) | Otimização de paginação/filtragem ainda não realizada | Squad de desenvolvimento [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] | Monitorar p95 (RNF-04) durante o MVP; otimizar antes da Release 2 [INFERIDO POR IA] |
| Meio de entrega da notificação pós-aprovação (pós-condição de UC-03) | Meio não definido no BRD (e-mail, in-app ou push) | Product Owner | Backlog Could — sem prazo comprometido |
| Antecipação de RNF-08 (mobile-first) para a Release 2 | Decisão condicionada à adoção do MVP pelos usuários (risco R-04 do BRD) | Product Owner | Avaliar ao fechamento do MVP (Sprint 5) [INFERIDO POR IA] |