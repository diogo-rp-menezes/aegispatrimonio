# Test Strategy — Sistema de Gestão de Patrimônio (AegisPatrimônio)

> **Versão:** 1.0 · **Owner:** A designar (QA Lead/Eng Lead) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** · **Status:** Draft
>
> *Nota de Transparência:* Este documento é ancorado na varredura determinística do codebase (**A5** — 350 arquivos, 27.537 LOC, 1268 funções, 344 classes), nas dependências reais capturadas (`@popperjs/core` em produção; **0 dependências de desenvolvimento**) e no artefato **User Stories (BDD)** (A3, com seus requisitos de NFRs, riscos e critérios de aceite do BRD). **Estado verificado da base de testes:** nenhuma biblioteca de teste/runner detectada nas dependências, nenhum script de teste em `package.json`, engine de banco de dados não especificada, nenhum ORM/query builder. Stack verificada: backend Java (335 arquivos `.java` em `src/`), frontend JavaScript (15 arquivos `.js` em `frontend/`), topologia **Aplicação Web / Server-side**, sem empacotamento Tauri. Toda ferramenta, comando ou parâmetro não detectado diretamente nos fontes contém o marcador `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`.

---

## 1. Objectives & Quality Goals

* **Garantir cobertura de testes > 80%** (RNF-10 do BRD), incluindo obrigatoriamente os cenários de permissão **Admin vs User** (CA-10) e a conciliação de custos (CA-09) — meta declarada no BRD e referenciada no DoD de todas as stories (US-001..US-007). Hoje a meta **não é mensurável**: não há runner de testes nem ferramenta de cobertura no codebase (A5).
* **Zero regressões nos contratos comportamentais verificados:** `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `criar_comDadosInvalidos_deveRetornarBadRequest`, `buscarPorId_comIdInexistente_deveRetornarNotFound`, `aprovar`, `cancelar`, `concluir` e `custoTotalPorAtivo` — base dos critérios de aceite BDD (US-001..US-004) e das regras RN-03 (400 Bad Request), RN-04 (404 padronizado), RN-05 (ciclo Pendente → Aprovada → Em Andamento → Concluída) e RN-08 (custo total = somatório das ordens concluídas).
* **Verificar as metas não-funcionais do BRD pendentes de instrumentação:** RNF-04 (< 200ms p95, excluindo relatórios pesados — risco R-03), RNF-01 (token JWT com expiração de 1h — meta pendente de verificação no codebase), RNF-07 (audit trail imutável em toda escrita, incluindo tentativas rejeitadas — US-006) e RNF-08 (UX mobile-first — risco R-04). Nenhuma instrumentação de métricas foi detectada na varredura A5 — a verificação requer instrumentação nova.
* **Reduzir o tempo de detecção de bugs (MTTD) para < 24h** via execução automatizada a cada commit/PR **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: não há pipeline de CI detectado no codebase; o valor de 24h é hipótese de governança, não requisito dos fontes.
* **Fechar os achados de qualidade da varredura A5** como objetivo contínuo: stub `setUsername` (`Usuario.java:86`), logs residuais `console.error`/`console.debug` (`api.js:44`/`:107`) e funções com complexidade ciclomática alta (`request` 13, `toDTO` 14, `build` 14, `checkResourceUsageAlerts` 17, `run` 15) — tratados pelas stories US-005 e US-007 e por gate de revisão de código.

---

## 2. Test Pyramid

| Camada | Cobertura Mínima | Ferramenta | Frequência de Execução | Responsável |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests** | 80% (RNF-10) | Backend Java: JUnit 5 **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** · Frontend JS: Jest **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (script `test` a criar no `package.json`) | A cada commit (CI) — pipeline inexistente hoje, a criar **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | Dev |
| **Integration Tests** | Fluxos críticos UC-01..UC-04 + contratos nomeados (ver notas) | JUnit 5 (testes de integração) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — estratégia de banco de dados de teste pendente da confirmação do mecanismo de persistência (não discoverable nas fontes) | A cada PR | Dev |
| **E2E Tests** | Jornadas principais: US-001 (cadastro de filial), US-002 (solicitação de manutenção), US-003 (aprovação), US-004 (conclusão da ordem) | Playwright **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — recomendado por suportar emulação de viewport mobile (RNF-08 / risco R-04) | A cada deploy para staging | QA |
| **Contract Tests** | Todos os endpoints públicos — lista de endpoints **pendente** (`api-specification.md` não gerado; nenhuma rota detectada na varredura A5) | Pact **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | A cada release | Dev |

**Notas da pirâmide (ancoragem na base real):**

* **Nenhum runner/framework de testes está instalado.** As dependências reais capturadas limitam-se a `@popperjs/core` (produção), sem dev-dependencies e sem scripts de teste em `package.json`. Todas as ferramentas da tabela são sugestões para a stack verificada (Java + JavaScript, aplicação web/server-side) e exigem decisão explícita de adoção antes de qualquer uso.
* **Contratos de integração nomeados** derivados das User Stories (BDD): `criar_comAdmin_deveRetornarCreated`, `criar_comUser_deveRetornarForbidden`, `criar_comDadosInvalidos_deveRetornarBadRequest` (US-001); `criar_comDadosInvalidos_deveRetornarBadRequest`, `buscarPorId_comIdInexistente_deveRetornarNotFound` (US-002); `aprovar`, `cancelar`, `custoTotalPorAtivo` (US-003); `concluir` + conciliação do `custoTotalPorAtivo` (CA-09) (US-004). Como a varredura não identificou rotas/endpoints, os testes de integração devem ser ancorados a esses contratos comportamentais até a publicação do `api-specification.md`.
* **BDD:** as stories estão escritas em Gherkin; um framework BDD (ex.: Cucumber para o backend Java) pode automatizar os cenários diretamente **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Camada de integração e persistência:** nenhum motor de banco ou ORM/query builder foi encontrado nas dependências — a escolha da estratégia de dados de teste de integração (banco em memória, container, etc.) **não pode ser presumida** e depende da confirmação do mecanismo real de persistência no codebase.
* **Prioridade de cobertura por risco** (caminhos apontados pela varredura A5, a cobrir primeiro): `AtivoService.java:119` (TODO de performance — busca com até 1000 candidatos, US-005), `AtivoMapper.toDTO` (complexidade 14 — RF-14), `ManutencaoSpecification.build` (complexidade 14 — filtros da fila, US-003), `AlertNotificationService.checkResourceUsageAlerts` (complexidade 17) e a função `request` de `frontend/src/services/api.js` (complexidade 13, comum a todos os fluxos UC-01..US-004).

---

## 3. Test Environments

| Ambiente | Propósito | Dados | Acesso |
| :--- | :--- | :--- | :--- |
| **Local** | Desenvolvimento e execução de testes unitários/integração | Seed via `RealisticDataSeeder` (detectado: `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java`, método `run`, complexidade 15) | Dev |
| **CI** | Validação automatizada (unit + integração + higiene estática) | Efêmero (criado e destruído por execução) | Pipeline — **pipeline de CI não detectado no codebase, a criar [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |
| **Staging** | Homologação pré-produção — DoD das stories exige "Feature testada em staging (com dados do `RealisticDataSeeder`)" e medição de tempo de resposta (US-005) | Seed realista (`RealisticDataSeeder`) / anonimizado **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | QA/PO |
| **Produção** | Smoke tests pós-deploy | Real | Automação **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |

**Notas de ambientes:**

* **Provisionamento de banco:** engine de banco de dados não especificada nas dependências — o provisionamento de local/CI/staging depende da confirmação do mecanismo de persistência real.
* **Mobile-first (RNF-08):** o staging e a suíte E2E devem incluir validação em viewport mobile, dado que a adoção do módulo de manutenção depende de UX mobile-first (risco R-04 do BRD).
* **Massa de dados para performance:** a validação da meta RNF-06 (10k+ ativos / 1k+ usuários simultâneos) exige ambiente com volume compatível — a criar **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; aplicação backend única, sem mecanismos de scaling horizontal detectados no codebase, logo os testes de carga validam os limites do caminho de aplicação existente.

---

## 4. Coverage Targets

* **Mínimo global:** **80%** — RNF-10 do BRD ("cobertura > 80%"), referenciado no DoD de US-001..US-004. Ferramenta de medição inexistente hoje; sugerido: JaCoCo (Java) e cobertura nativa do runner JS **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Crítico (regras de negócio):** **95%** para RN-03 (validação/400), RN-04 (404 padronizado), RN-05 (ciclo de manutenção e transições de estado), RN-08/CA-09 (conciliação do custo total) e CA-10 (permissões Admin vs User) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: o BRD fixa apenas o piso global de 80% (RNF-10); o piso de 95% para caminhos críticos é hipótese de governança alinhada à criticidade das stories Must (US-002..US-004).
* **Exclusões justificadas:**
  * `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java` — código de carga de dados de teste (método `run`, complexidade 15), não é caminho de negócio; sua qualidade é tratada por revisão e pela verificação de integridade dos dados gerados (seção 6).
  * Classes de configuração e código puramente declarativo **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: exclusão padrão de código de configuração; a lista concreta de classes excluídas deve ser definida na adoção do runner.
  * `frontend/src/services/api.js` **não** é excluído — a função `request` (complexidade 13) é comum a todos os fluxos (US-001..US-004) e deve ser coberta, incluindo injeção de token (RF-24/CA-07) e `clearSession` (RF-25/CA-08).

---

## 5. Non-Functional Testing

* **Performance:**
  * **Critério de aceite (RNF-04 do BRD):** resposta < 200ms (p95) nas operações de escrita e busca, **excluindo operações de relatório pesado** — a consulta `custoTotalPorAtivo` (RF-17/RN-08) está explicitamente fora dessa garantia (risco R-03: timeout em consultas pesadas).
  * **Foco obrigatório:** caminho de busca de ativos (`AtivoService.java:119` — TODO: carrega até 1000 candidatos e faz ranking; US-005) e detalhes do ativo (`AtivoMapper.toDTO`, complexidade 14 — RF-14).
  * **Carga alvo (RNF-06):** 10k+ ativos / 1k+ usuários simultâneos — meta do BRD pendente de verificação no codebase; sem mecanismos de scaling detectados, os testes devem medir os limites reais do caminho de aplicação.
  * **Ferramenta:** Gatling (JVM, aderente à stack Java) ou k6 **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; nenhuma instrumentação de métricas detectada na varredura — a medição do p95 requer instrumentação nova (DoD de US-005).
* **Segurança:**
  * **Requisitos dos fontes:** RNF-01 — token JWT com expiração de 1h (meta do BRD pendente de verificação no codebase); RN-02 — 403 Forbidden para escrita sem permissão; CA-06 — 401 Unauthorized com sessão expirada; CA-07 — injeção automática de token no cliente (`api.js`, função `request`); CA-08 — `clearSession`; CA-10 — cobertura de permissões Admin vs User em todas as escritas.
  * **SAST:** nenhuma ferramenta detectada; sugerido Semgrep a cada PR **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. A varredura A5 já cobre verificação estática de higiene (logs residuais `api.js:44`/`:107` — exposição potencial de dados em console, US-007).
  * **DAST:** sugerido OWASP ZAP em staging, a cada release **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Acessibilidade:**
  * **Requisitos dos fontes:** RNF-08 — UX mobile-first (risco R-04); DoD de todas as stories exige "Sem regressões de acessibilidade".
  * **Ferramenta:** axe-core integrada à suíte E2E, com execução em viewport mobile **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**; critérios WCAG — nível não especificado nos fontes; premissa: WCAG 2.1 nível AA **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Resiliência/Chaos:**
  * **Não aplicável no nível de infraestrutura:** aplicação web/server-side única, sem mecanismos de scaling, réplicas ou infraestrutura distribuída detectados no codebase — chaos engineering de infraestrutura não se aplica ao estado atual **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
  * **Aplicável no nível de aplicação** (falha induzida, ancorada aos cenários BDD): falha no upload de anexos sem bloquear a solicitação (US-002, Scenario 3); falha na gravação do audit trail sem corromper a operação de negócio (US-006, Scenario 3); timeout na consulta de custo (R-03 — US-003); sessão expirada durante submissão (CA-06 — US-001/US-002/US-004); **condição de corrida** na transição Pendente → Aprovada com dois aprovadores simultâneos (US-003, Scenario 4) — requer teste de concorrência dedicado.

---

## 6. Test Data Management

* **Estratégia de geração:**
  * **Seed verificado:** `RealisticDataSeeder` (`src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java`, método `run`, complexidade 15) — gerador de dados realistas usado no DoD das stories para staging (US-001..US-007). Deve garantir as pré-condições dos fluxos: filiais cadastradas (RN-01/RN-07), ativos vinculados a filial/departamento/localização (RN-07) e usuários com roles Admin/User (RN-02/CA-10).
  * **Fixtures/factories** para testes unitários e de integração, cobrindo os cenários de erro: dados inválidos (RN-03), IDs inexistentes (RN-04), transições inválidas (RN-05) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
  * **Massa de volume** para testes de performance: 10k+ ativos (RNF-06) e cenário de busca com ~1000 candidatos (TODO em `AtivoService.java:119`) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
  * **Integridade do seed:** o método `run` (complexidade 15) deve ser acompanhado de verificação de integridade dos dados gerados (contagens e vínculos RN-07) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
* **Dados sensíveis:**
  * O domínio envolve dados de pessoas (vínculo Funcionário↔Usuário — RN-06; RF-08: `createFuncionario` / `createFuncionarioAndUsuario`), portanto **anonimização/mascaramento é obrigatório fora de produção** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — o BRD não define política de anonimização nem de retenção de dados de teste; premissa adotada: nenhum dado pessoal real em local/CI/staging.
  * O seeder gera "dados realistas" — validar na adoção que não incorpora dados pessoais reais.
  * Logs residuais no cliente API (`console.error` em `api.js:44`, `console.debug` em `api.js:107`) podem expor payloads em console — remoção tratada pela US-007; a verificação estática da varredura A5 opera como controle complementar.

---

## 7. Regression Strategy

* **Suite de regressão:**
  * **Escopo:** todos os contratos comportamentais dos UC-01..UC-04 (criar filial, solicitar manutenção, aprovar/cancelar, concluir ordem), as regras RN-03/RN-04/RN-05/RN-08, as permissões Admin vs User (CA-10) e a conciliação do `custoTotalPorAtivo` (CA-09); inclui a suíte E2E das jornadas principais (US-001..US-004).
  * **Frequência:** integração a cada PR; E2E a cada deploy em staging **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: não há pipeline detectado; cadência a formalizar na criação do CI.
  * **Gatilho obrigatório para refatorações:** US-005 (otimização da busca — `AtivoService.java:119`, `AtivoMapper.toDTO`) e US-007 (higiene — `Usuario.java:86`, `api.js:44`/`:107`) exigem regressão completa dos contratos existentes antes do merge, pois alteram caminhos comuns aos fluxos Must.
* **Critérios de entrada/saída de release:**
  * **Entrada:** código revisado (PR aprovado) e suíte de integração verde em CI.
  * **Saída:** 0 bugs críticos (P0) e altos (P1) abertos; suíte de regressão verde; cobertura ≥ 80% (RNF-10) incluindo CA-10; verificação da conciliação de custos (CA-09); smoke tests em produção executados **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — premissa: critérios de governança não explícitos nos fontes, alinhados ao DoD das stories.

---

## 8. Bug Triage & Severity

> Os SLAs de correção **não estão definidos nos artefatos-fonte** — a coluna de SLA é hipótese de governança **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, a validar com o time. As definições de severidade são ancoradas aos fluxos e regras do BRD.

| Severidade | Definição | SLA de correção |
| :--- | :--- | :--- |
| Crítica (P0) | Sistema fora do ar / perda de dados — ex.: perda de registros de audit trail (RNF-07), corrupção do custo total por ativo (RN-08/CA-09), escrita persistida sem permissão (RN-02) | Imediato **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |
| Alta (P1) | Funcionalidade principal quebrada — ex.: ciclo de manutenção bloqueado (RN-05 — US-002..US-004), 403 indevido para Admin (RN-02/CA-01), 401 em sessão válida (CA-06) | 24h **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |
| Média (P2) | Funcionalidade secundária afetada — ex.: filtros dinâmicos da fila (`ManutencaoSpecification.build`), busca fora da meta RNF-04 sem bloquear o fluxo, mensagens de erro não padronizadas (RN-04) | Próxima sprint **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |
| Baixa (P3) | Cosmético/menor — ex.: logs residuais em console (`api.js:44`/`:107` — US-007), stub `setUsername` sem fluxo dependente ativo | Backlog **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |

---

## 9. Reporting & Metrics

* **Dashboards:**
  * Cobertura de testes (global ≥ 80% — RNF-10; crítico ≥ 95% — seção 4) — requer instrumentação nova (JaCoCo/runner JS **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**).
  * Taxa de falha de build e flakiness da suíte E2E — requer pipeline de CI, inexistente hoje **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
  * Nenhuma instrumentação de observabilidade foi detectada na varredura A5 — todos os dashboards dependem de instrumentação a criar (DoD das stories: "Métricas/observabilidade instrumentadas (se aplicável)").
* **Métricas de qualidade acompanhadas:**
  * Escape rate (bugs em produção vs staging), MTTR e defect density **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — não definidos nos fontes.
  * **KPIs do BRD que a estratégia de testes deve sustentar (medidos em produção):** tempo médio de aprovação < 4 horas úteis (US-003) e taxa de solicitações canceladas < 10% (US-004) — fora do escopo da suíte automatizada, mas os testes de integração/E2E devem garantir os comportamentos que os habilitam (fila de pendentes visível, transições RN-05, conciliação CA-09).
  * **Achados da varredura A5 como métrica de dívida:** funções com complexidade ciclomática alta apontadas pela varredura (12; detalhadas: `request` 13, `toDTO` 14, `build` 14, `checkResourceUsageAlerts` 17, `run` 15) e achados de higiene (stub + logs residuais: 3) — meta: redução a zero via US-005/US-007.

---

## 10. Roles & Responsibilities

| Papel | Responsabilidade |
| :--- | :--- |
| **Dev** | Testes unitários e de integração cobrindo os contratos nomeados (`criar_*`, `buscarPorId_*`, `aprovar`, `cancelar`, `concluir`, `custoTotalPorAtivo`) e os cenários BDD (incl. permissões CA-10 e condição de corrida US-003); correção dos achados A5 (US-005/US-007); manutenção de fixtures/factories |
| **QA** | E2E das jornadas principais (US-001..US-004), testes exploratórios, regressão em staging; validação mobile-first (RNF-08/risco R-04) e acessibilidade; execução com dados do `RealisticDataSeeder` |
| **Eng Lead** | Gate de qualidade em releases (critérios da seção 7); aprovação da adoção das ferramentas de teste inferidas; priorização da dívida de complexidade |
| **PO** | Validação dos critérios de aceite BDD e das lacunas abertas que afetam os testes: unicidade de código de filial (US-001, Scenario 2), role de aprovação (US-003), código HTTP de transição inválida (US-003/US-004, Scenario 4), comportamento de falha do audit trail (US-006, Scenario 3) **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** |