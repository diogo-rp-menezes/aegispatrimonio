# Non-Functional Requirements (NFR) — Sistema de Gestão de Patrimônio (A4)

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Engenharia/Arquitetura

Este documento consolida os requisitos não funcionais do **Sistema de Gestão de Patrimônio (A4)** (backend no pacote `br.com.aegispatrimonio`, frontend web em `frontend/`). As metas derivam prioritariamente do **Business Requirements Document (BRD)** (seção 4 — Requisitos Não-Funcionais, seção 7 — Critérios de Aceitação e seção 8 — KPIs) e do **diagnóstico determinístico do codebase** (350 arquivos parseados, 1.268 funções, 344 classes, 27.537 LOC, varredura AST real). Itens sem respaldo direto no BRD ou no codebase estão marcados com **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, com as premissas declaradas.

## 0. Contexto Técnico Verificado (base de todas as metas)

Estado verificado por varredura determinística do workspace e extração do `package.json` — não inferido por convenção de mercado:

| Aspecto | Estado verificado |
|---|---|
| **Topologia** | Aplicação Web / Server-side (backend + frontend web; **sem** empacotamento desktop — não existe `src-tauri/Cargo.toml`) |
| **Backend** | 335 arquivos `.java` em `src/` (pacote `br.com.aegispatrimonio`) |
| **Frontend** | 15 arquivos `.js` em `frontend/`; o `package.json` do workspace declara **1 dependência de produção** (`@popperjs/core` ^2.11.8) e **nenhuma** de desenvolvimento |
| **Banco de dados** | **Não especificado** — nenhum motor de banco identificado nas dependências verificadas |
| **ORM / Query Builder** | **Não detectado** — acesso a dados presumivelmente via SQL cru |
| **Scripts de execução / CI** | Nenhum script declarado no `package.json`; nenhuma configuração de pipeline encontrada na varredura |

**Consequências diretas para os NFRs:**
1. A meta de latência (p95 < 200ms) depende de SQL cru bem indexado e de paginação obrigatória nas listagens — não há camada de ORM que abstraia ou otimize consultas; a qualidade de índices, planos de execução e parametrização é responsabilidade direta do código.
2. Metas de escrita concorrente, backup e RPO **não podem ser calibradas** até que o motor de banco real seja verificado (Seção 12).
3. A topologia é uma aplicação server-side única: a métrica primária de capacidade é o **throughput por instância** e a alavanca imediata é o dimensionamento vertical (CPU/memória). Arquiteturas distribuídas de alta disponibilidade (clusters multi-região, auto-scaling automático) **não são suportadas pela stack verificada** e não são requisito deste documento.

## 1. Performance

* **NFR-P01:** Tempo de resposta da API < **200ms no p95**, excluindo operações de relatório pesado (origem: BRD RNF-04 e KPI "Tempo de resposta API (p95) < 200ms — contínuo"); meta complementar de < **500ms no p99**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — o BRD especifica apenas o p95]**
* **NFR-P02:** Time to First Byte (TTFB) < **100ms** para endpoints de leitura simples (busca por ID — RF-04/RF-14) em condições normais de rede. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-P03:** Renderização inicial (LCP) < **2,5s** em conexão 4G para as telas principais — meta calibrada para o frontend verificado, que não possui framework SPA pesado (única dependência de produção: `@popperjs/core` ^2.11.8). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-P04:** Relatórios de custo total por ativo (`custoTotalPorAtivo` — RF-17) devem concluir em < **3s (p95)** para acervo de 10.000+ ativos; acima desse limite, paginação obrigatória. Origem: risco R-03 do BRD (timeout em consultas pesadas); as estratégias de mitigação citadas no BRD (cache externo e materialização de consultas) **não estão verificadas na stack atual** e exigem decisão de arquitetura própria.
* **Hotspots reais identificados no codebase (diagnóstico AST):**
  - `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` — TODO de performance: o caminho carrega até **1.000 candidatos (id+nome)** e faz ranking em memória; reavaliar contra NFR-P01/P04 antes do go-live.
  - `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` — construção dinâmica de consulta (complexidade 14), usada na filtragem de manutenção (RF-15/RF-22); validar índices e plano de execução no banco real.
* **Método de medição:** suíte de testes de carga e APM **a definir** — nenhuma ferramenta de APM/load testing foi encontrada no codebase verificado; candidatos a avaliar: k6 ou JMeter contra os endpoints autenticados (Admin/User), medindo p95/p99 por endpoint. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 2. Escalabilidade

* **NFR-S01:** Suportar **10.000+ ativos cadastrados** e **1.000+ usuários simultâneos** sem degradação de latência > 10% sobre o baseline (origem: BRD RNF-06). A concorrência deve ser validada na camada de aplicação server-side (throughput por instância), pois o motor de banco e seu modelo de concorrência de escrita ainda não foram verificados.
* **NFR-S02:** Preparação para escalonamento horizontal da camada de aplicação (origem: BRD RNF-06 — "horizontal scaling ready"), classificado como **Should**: a topologia verificada é uma aplicação server-side única, **sem** infraestrutura de orquestração/balanceamento verificada. Condições prévias a qualquer compromisso: (a) instâncias stateless — o JWT (RNF-01) favorece isso, porém a **blocklist server-side de logout (CA-08)** introduz estado compartilhado que precisará de mecanismo central a definir; (b) definição do motor de banco e sua estratégia de escala (Seção 12).
* **NFR-S03:** Throughput mínimo sustentado de **≥ 100 req/s por instância** em cenário misto (80% leitura / 20% escrita) sem erros 5xx. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-S04:** Toda listagem deve ser paginada server-side (origem: RF-05 — paginação explícita; a extensão a RF-15 e à fila de pendentes de aprovação — UC-03 — é recomendação deste documento). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — quanto à extensão]**
* **Método de medição:** teste de carga progressivo (ramp-up) até 1.000 usuários virtuais simultâneos; ferramenta a definir (ver NFR-P01). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 3. Disponibilidade & Confiabilidade

* **NFR-A01:** Uptime mínimo de **99,5% mensal** (SLA), excluindo janelas de manutenção programada (origem: BRD RNF-05 e KPI "Disponibilidade do sistema — 99,5%"). SLO interno sugerido: 99,9%. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — apenas o SLO interno]**
* **NFR-A02:** RTO (Recovery Time Objective) ≤ **1h** para o ambiente de produção. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-A03:** RPO (Recovery Point Objective) ≤ **15min**, via rotina de backup do banco de dados. **Atenção:** a estratégia de backup (frequência, full vs. incremental, teste de restore) **depende do motor de banco real, ainda não verificado** (Seção 12). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-A04:** Pontos únicos de falha devem ser **identificados e documentados**: a topologia verificada (aplicação server-side única + banco não especificado) implica SPOFs nas camadas de aplicação e de dados. A eliminação completa de SPOF é meta de evolução condicionada à infraestrutura de deploy e **não é exigível na topologia atual**. Prioridade de disponibilidade: fluxo de solicitação de manutenção (RF-18, UC-02) — maior exposição ao usuário final (RNF-08).
* **NFR-A05:** Degradação graciosa: falhas em componentes não críticos (ex.: notificações de alerta — `AlertNotificationService`) não devem impedir autenticação (RF-23), consulta de ativos (RF-14/RF-15) nem criação de solicitações (RF-18). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Método de medição:** monitor de uptime externo + teste de restore de backup trimestral; ferramentas a definir (nenhuma verificada no codebase). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 4. Segurança

* **NFR-SEC01:** Autenticação via **JWT** com access token expirando em **1h** e refresh token em **7 dias** (origem: BRD RNF-01, RF-23). Endpoints protegidos rejeitam requisições sem token com **401** (CA-06). Logout limpa a sessão local e invalida o token server-side via **blocklist** (CA-08, RF-25 `clearSession`).
* **NFR-SEC02:** Senhas armazenadas exclusivamente hasheadas — **nunca em plain text** (origem: BRD RNF-02, que especifica bcrypt/argon2). O mecanismo de hash efetivamente implementado no backend **não pôde ser verificado** na varredura de dependências (Seção 12) — verificação obrigatória antes de aprovar este requisito.
* **NFR-SEC03:** **HTTPS obrigatório em produção**, com TLS 1.2+ (origem: BRD RNF-03).
* **NFR-SEC04:** **RBAC** com as roles **Admin** (escrita total) e **User** (leitura + solicitações): toda operação de escrita em entidades mestres exige Admin; tentativas de User retornam **403 Forbidden** (origem: BRD RF-26, RN-01, RN-02, CA-01, CA-10). Cobertura de permissões deve alcançar 100% dos endpoints de escrita nos testes de integração.
* **NFR-SEC05:** Validação de entrada server-side em todos os endpoints: payloads inválidos retornam **400 Bad Request** com mensagens claras (RN-03, RF-28, CA-02); IDs inexistentes retornam **404 Not Found** padronizado (RN-04, RF-29, CA-03).
* **NFR-SEC06:** **Todas as consultas SQL devem ser parametrizadas** (proibida concatenação de strings com input do usuário) — requisito crítico dado o acesso a dados via **SQL cru, sem ORM**, verificado na stack; consultas dinâmicas (ex.: `ManutencaoSpecification.build`) são superfície de risco prioritária para revisão de segurança. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — derivado da stack verificada]**
* **NFR-SEC07:** Trilha de auditoria **imutável** de todas as operações de escrita (criar/atualizar/deletar) em todas as entidades (origem: BRD RNF-07).
* **NFR-SEC08:** Conformidade com **OWASP Top 10** avaliada por revisão de segurança a cada release maior. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-SEC09:** MFA obrigatório para acesso administrativo (role Admin). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Prioridade: Should.
* **NFR-SEC10:** Rotação de segredos (chave de assinatura do JWT, credenciais de banco) a cada **90 dias**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 5. Usabilidade & Acessibilidade

* **NFR-U01:** Interface responsiva para **desktop e tablet**, com abordagem **mobile-first no fluxo de solicitação de manutenção** (origem: BRD RNF-08 e risco R-04 — adoção pelos usuários).
* **NFR-U02:** Conformidade **WCAG 2.1 AA** nas telas principais, incluindo suporte a leitores de tela nos fluxos críticos (login, nova solicitação, aprovação). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-U03:** Layout funcional na faixa de **320px a 1920px**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **Método de medição:** auditoria de acessibilidade por release maior (varredura automatizada + teste manual com leitor de tela); ferramenta a definir. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 6. Observabilidade

* **NFR-O01:** Logs estruturados no backend e endpoint de health check (`/health`) exposto para monitor de uptime; métricas de aplicação expostas via `/metrics`. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — endpoints não confirmados no codebase]**
* **NFR-O02:** Correlação de requisições via **request ID** propagado do frontend (`frontend/src/services/api.js`) para o backend nos fluxos críticos (autenticação, solicitação, aprovação) — alternativa proporcional à topologia server-side única, na qual tracing distribuído multi-serviço não se aplica. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-O03:** Alertas configurados para: p95 de latência acima de 200ms sustentado, taxa de erro > 1% e saturação de recursos da instância. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**
* **NFR-O04:** Instrumentação dos KPIs de processo do BRD (seção 8): tempo médio de aprovação (< 4 horas úteis) e taxa de solicitações canceladas (< 10%) exigem registro de timestamps nas transições do fluxo de manutenção (RF-18 a RF-21).
* **Achado real do diagnóstico:** chamadas residuais `console.error` (`frontend/src/services/api.js:44`) e `console.debug` (`frontend/src/services/api.js:107`) devem ser removidas ou substituídas por tratamento de erro estruturado antes de produção.

## 7. Manutenibilidade & Qualidade de Código

* **NFR-M01:** Cobertura de testes **> 80%** (unit + integration), com pipeline de CI/CD que **bloqueia merge/deploy** abaixo do limite (origem: BRD RNF-10 e KPI "Cobertura de testes > 80% — por deploy"). Testes de integração devem cobrir todos os cenários de permissão Admin vs. User (CA-10). **Pendência:** nenhum script de CI foi encontrado no `package.json` verificado — o gate precisa ser implementado/verificado (Seção 12).
* **NFR-M02:** Complexidade ciclomática máxima por função: **≤ 10** (limite proposto). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — O diagnóstico sinalizou 12 funções com complexidade alta; as 5 detalhadas na varredura:
  - `checkResourceUsageAlerts` — `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96` — complexidade **17**
  - `run` — `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34` — complexidade **15**
  - `toDTO` — `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` — complexidade **14**
  - `build` — `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` — complexidade **14**
  - `request` — `frontend/src/services/api.js:54` — complexidade **13**

  Nota: o seeder de dados realistas (`RealisticDataSeeder`) também é relevante para o risco R-02 do BRD (migração de dados legados) — executar em staging com validação.
* **NFR-M03:** Documentação de API mantida via **OpenAPI/Swagger**, atualizada a cada release (origem: BRD RNF-09). **Pendência:** a ferramenta de documentação efetivamente utilizada não foi verificada na varredura.
* **NFR-M04:** Dívida técnica registrada deve ser rastreada e resolvida: **1 stub** — `Usuario.setUsername` com corpo vazio (`src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`) — e **1 TODO de performance** (`AtivoService.java:119`, ver NFR-P04). O stub em `Usuario` é potencialmente relevante para autenticação (RF-23) e deve ser investigado com prioridade.

## 8. Compliance & Regulatório

* **NFR-C01:** Conformidade **LGPD**: o sistema trata dados pessoais de funcionários e usuários (cadastro de funcionário com vínculo a usuário — RF-08/RN-06; credenciais de acesso). Devem estar implementados: base legal documentada, direitos de acesso e eliminação, e minimização de dados nos cadastros. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — o BRD não detalha requisitos de LGPD; a inferência parte do domínio (dados pessoais de colaboradores) e do contexto brasileiro do projeto.
* **NFR-C02:** Política de retenção de dados definida e documentada, resolvendo a **tensão** entre a trilha de auditoria imutável (RNF-07) e o direito de eliminação (LGPD) — ex.: anonimização de dados pessoais em registros de auditoria preservados. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 9. Portabilidade & Compatibilidade

* **NFR-PO01:** Compatibilidade com os **2 últimos major releases** dos navegadores principais (Chrome, Firefox, Safari, Edge). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — o frontend verificado utiliza JS com `@popperjs/core` ^2.11.8 como única dependência de produção; a compatibilidade real depende das APIs de navegador utilizadas no código.
* **NFR-PO02:** Entrega exclusivamente **web responsiva** (desktop/tablet — RNF-08); **não** há aplicativo desktop empacotado (sem Tauri — verificado na stack) nem apps nativos no escopo verificado.
* **NFR-PO03:** Deploy em servidor web/infraestrutura a definir; **não há configuração de deploy ou provedor verificados no codebase** — requisitos de multi-cloud não se aplicam à topologia atual. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 10. Custo (Cost Efficiency)

* **NFR-CO01:** Estrutura de custo proporcional à topologia verificada (aplicação server-side única + banco de dados + domínio/TLS): o custo mensal de infraestrutura deve ser monitorado e mantido dentro de orçamento a definir pela gestão. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — não há meta de valor absoluto descobrível no BRD ou no codebase.
* **NFR-CO02:** Otimizações de custo **não devem violar** NFR-P01/P04: as estratégias de performance para relatórios de custo (risco R-03 do BRD) devem ser avaliadas por relação custo-benefício antes de adoção.

## 11. Rastreabilidade

| ID | Requisito Relacionado (BRD) | Prioridade | Status de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01 | RNF-04; KPI "Tempo de resposta API (p95) < 200ms" | Must | Não validado |
| NFR-P02 | RNF-04 (derivado) | Should | Não validado |
| NFR-P03 | RNF-08 (derivado) | Should | Não validado |
| NFR-P04 | RF-17; Risco R-03 | Should | Não validado |
| NFR-S01 | RNF-06 | Must | Não validado |
| NFR-S02 | RNF-06 ("horizontal scaling ready") | Should | Não validado |
| NFR-S03 | RNF-06 (derivado) | Should | Não validado |
| NFR-S04 | RF-05; RF-15; UC-03 | Must | Não validado |
| NFR-A01 | RNF-05; KPI "Disponibilidade 99,5%" | Must | Não validado |
| NFR-A02 | RNF-05 (derivado) | Should | Não validado |
| NFR-A03 | RNF-05 (derivado) | Should | Não validado |
| NFR-A04 | RNF-05 (derivado) | Should | Não validado |
| NFR-A05 | RF-18; RF-23; UC-02 (derivado) | Should | Não validado |
| NFR-SEC01 | RNF-01; RF-23; RF-24; RF-25; CA-06; CA-08 | Must | Não validado |
| NFR-SEC02 | RNF-02 | Must | Não validado |
| NFR-SEC03 | RNF-03 | Must | Não validado |
| NFR-SEC04 | RF-26; RN-01; RN-02; CA-01; CA-10 | Must | Não validado |
| NFR-SEC05 | RF-28; RF-29; RN-03; RN-04; CA-02; CA-03 | Must | Não validado |
| NFR-SEC06 | RN-03 (derivado da stack: SQL cru sem ORM) | Must | Não validado |
| NFR-SEC07 | RNF-07 | Must | Não validado |
| NFR-SEC08 | (derivado — OWASP Top 10) | Should | Não validado |
| NFR-SEC09 | (inferido — MFA Admin) | Should | Não validado |
| NFR-SEC10 | (inferido — rotação de segredos) | Should | Não validado |
| NFR-U01 | RNF-08; Risco R-04 | Must | Não validado |
| NFR-U02 | (inferido — WCAG 2.1 AA) | Should | Não validado |
| NFR-U03 | RNF-08 (derivado) | Should | Não validado |
| NFR-O01 | (inferido — health/metrics) | Should | Não validado |
| NFR-O02 | RF-24 (derivado — correlação request ID) | Should | Não validado |
| NFR-O03 | RNF-04; KPIs (derivado) | Should | Não validado |
| NFR-O04 | KPIs "Tempo médio de aprovação" e "Taxa de solicitações canceladas" | Should | Não validado |
| NFR-M01 | RNF-10; KPI "Cobertura de testes > 80%"; CA-10 | Must | Não validado |
| NFR-M02 | (derivado do diagnóstico AST) | Should | Não validado |
| NFR-M03 | RNF-09 | Should | Não validado |
| NFR-M04 | (derivado do diagnóstico AST) | Should | Não validado |
| NFR-C01 | (inferido — LGPD; contexto RF-08/RN-06) | Should | Não validado |
| NFR-C02 | RNF-07 (tensão com LGPD) | Should | Não validado |
| NFR-PO01 | RNF-08 (derivado) | Should | Não validado |
| NFR-PO02 | RNF-08; stack verificada (sem Tauri) | Must | Não validado |
| NFR-PO03 | (topologia — deploy a definir) | Could | Não validado |
| NFR-CO01 | (inferido — custo) | Could | Não validado |
| NFR-CO02 | Risco R-03 | Could | Não validado |

## 12. Lacunas e Pendências de Verificação

Informações não descobríveis a partir do diagnóstico e das fontes disponíveis — verificação obrigatória antes de aprovar este documento:

1. **Motor de banco de dados:** a varredura de stack cobriu o `package.json` raiz (que declara apenas `@popperjs/core`); o driver/motor real do backend (335 arquivos Java) precisa ser identificado no arquivo de build do backend. Impacta NFR-A03 (backup/RPO), NFR-S01 (concorrência de escrita) e NFR-SEC06.
2. **Mecanismo de hash de senhas** (bcrypt vs. argon2 — BRD RNF-02): implementação real não verificada. Impacta NFR-SEC02.
3. **Pipeline de CI/CD e scripts de execução:** nenhum script declarado no `package.json` e nenhuma configuração de pipeline encontrada — o gate de cobertura > 80% (RNF-10/NFR-M01) não está implementado de forma verificável.
4. **Ferramentas de APM, load testing, uptime e acessibilidade:** nenhuma verificada no codebase — os métodos de medição das seções 1 a 6 permanecem "a definir".
5. **Endpoints `/health` e `/metrics`:** existência não confirmada. Impacta NFR-O01/NFR-O03.
6. **Configuração de deploy/infraestrutura de produção** (servidor, TLS, domínio): não encontrada no codebase. Impacta NFR-SEC03, NFR-A01/A02 e NFR-PO03.
7. **Comportamento do stub `Usuario.setUsername`** (`Usuario.java:86`): corpo vazio pode indicar bug funcional ou intencional — investigar antes de aprovar os requisitos de autenticação relacionados (RF-23/NFR-SEC01).

> **Nota:** Este é um documento vivo. Alterações nos NFRs devem passar por mudança controlada (change request) e refletidas no BRD, nos casos de teste e no plano de validação.