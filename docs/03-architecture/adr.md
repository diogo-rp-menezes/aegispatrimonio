# ADR-001 — Monólito modular em camadas, instância única e acesso a dados via SQL cru

> **Data:** Não registrada nos fontes — preencher com a data de aprovação formal desta ADR (o SAD v1.0, artefato de origem, também não registra data de emissão).
> **Status:** Accepted (formalização retroativa de decisão já implementada — ver Seção 1) · **Deciders:** Engenharia/Arquitetura (owner do SAD v1.0) — nomes individuais não registrados nos fontes, preencher na aprovação formal · **Consulted:** Responsável pelo levantamento técnico urgente do BRD §8 (pendência C-01 — motor de persistência) · **Informed:** Equipes de desenvolvimento do backend (`src/`) e do frontend (`frontend/`)

## 1. Status

**Accepted.**

Esta é a primeira ADR formalizada do repositório — o SAD v1.0 registra que "nenhuma ADR formalizada foi identificada" e que as decisões nele registradas servem de base de governança até a formalização das primeiras ADRs. Este documento formaliza **retroativamente** a decisão de arquitetura já materializada no codebase verificado (353 arquivos — 338 `.java` em `src/` e 15 `.js` em `frontend/`; ~27.912 LOC; 1.287 funções; 347 classes) e consolidada no SAD v1.0.

**Escopo explícito:** a **sub-decisão sobre o motor de persistência não é abrangida por esta ADR** — permanece pendente de confirmação técnica (C-01 do BRD) e será registrada em ADR própria (ver Seção 8).

## 2. Context & Problem Statement

O AegisPatrimônio é um sistema interno corporativo de gestão patrimonial multi-filial, posicionado como **fonte única de verdade do patrimônio** (BRD §1). No momento da decisão, a engenharia precisava definir de forma combinada:

1. **Como estruturar o backend** — um único deployável em camadas ou serviços distribuídos por domínio (ativos, manutenção, alertas, auditoria);
2. **Qual a topologia de deploy** — instância única ou múltiplas instâncias com balanceamento;
3. **Como o frontend comunica com o backend** — comunicação síncrona HTTP ou mecanismos assíncronos;
4. **Como acessar os dados** — via ORM/query builder ou SQL cru.

**Problema em termos neutros:** como estruturar e implantar um sistema interno multi-filial com até 150 usuários concorrentes (NFR-S01), disponibilidade de 99,0% em horário comercial (NFR-A01), custo de infraestrutura abaixo de USD 2,50/usuário ativo/mês (NFR-CO01), autorização RBAC com **invalidação imediata de token no logout** (BR-07/NFR-SEC04) e **100% de consultas SQL parametrizadas** (NFR-SEC03), sem incorrer em complexidade operacional desproporcional ao escopo interno e aos sinais de equipe enxuta observados no repositório (`package.json` sem nome e sem scripts, zero devDependencies, nenhuma dependência de produção além de `@popperjs/core ^2.11.8`).

Restrições adicionais verificadas: nenhuma infraestrutura de mensageria ou cache nas dependências; nenhuma integração externa detectada no código; empacotamento desktop descartado (não há `src-tauri/Cargo.toml`); multi-cloud/multi-região fora de escopo nesta fase (NFR-PO02).

## 3. Decision Drivers

* **Custo de infraestrutura** — alvo abaixo de USD 2,50/usuário ativo/mês (NFR-CO01); sistema interno com até 150 usuários concorrentes (NFR-S01).
* **Segurança e modelo de sessão** — autenticação por token validado a cada requisição (NFR-SEC02) e **invalidação imediata do token no logout** (BR-07/NFR-SEC04), que implica estado de sessão/token no servidor.
* **Performance e escala** — 150 usuários concorrentes com degradação < 10% (NFR-S01), 50 req/s sustentados em leitura (NFR-S03), metas de latência p95/p99 (NFR-P01–P04), com escalonamento vertical como estratégia primária (NFR-S02).
* **Disponibilidade** — 99,0% em horário comercial (08:00–18:00, dias úteis), RTO ≤ 4h com restauração manual testada semestralmente, RPO ≤ 24h (NFR-A01–A04); SPOF único aceito e documentado (NFR-A04).
* **Manutenibilidade / tamanho da equipe** — complexidade ciclomática ≤ 10 (NFR-M01); sinais de equipe enxuta: `package.json` sem scripts de build/verificação e zero devDependencies (NFR-M06).
* **Compatibilidade com a stack existente** — backend Java (338 arquivos em `src/`, pacotes `br.com.aegispatrimonio.{model, repository, mapper, service, config.seeder}`), frontend JavaScript (15 arquivos em `frontend/`), nenhuma dependência de mensageria/cache.
* **Compliance** — LGPD para dados pessoais de funcionários (NFR-C01–C03); trilha de auditoria obrigatória (BR-08).
* **Time-to-market / escopo interno** — sistema interno de instância única, sem requisito de distribuição de dados ou multi-região (NFR-PO02, NFR-S02).

## 4. Considered Options

> **Nota de origem:** a Option A é a opção **evidenciada no codebase** e documentada no SAD v1.0. As Options B e C são alternativas hipotéticas formuladas para esta ADR a partir dos trade-offs registrados no SAD v1.0 (§12 — "Instância única vs. alta disponibilidade", "Escalonamento vertical vs. horizontal", "sem indícios de microsserviços") — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. Premissa: alternativas de mercado foram estruturadas apenas para tornar o trade-off auditável; nenhuma delas existe no código e nenhuma foi implementada.

### Option A: Monólito modular em camadas em instância única, com acesso a dados via SQL cru (sem ORM)
* **Descrição:** Backend Java único e deployável, organizado em camadas (`model` → `repository` → `mapper` → `service`), com filtro de autenticação por requisição (`doFilterInternal`), autorização RBAC com contexto (`hasPermission`), acesso a dados via SQL cru 100% parametrizado (NFR-SEC03), filtros combinados via `ManutencaoSpecification` e mapeamento DTO via `AtivoMapper`. Frontend JavaScript separado (`frontend/`), desktop-first (1280–1920px — NFR-U03), com camada de serviços centralizada (`request`, `authInterceptor`, `handleApiError`), comunicando-se de forma **síncrona via HTTP com TLS 1.2+** (NFR-SEC01). Deploy em **instância única** (servidor on-premises ou VM de nuvem única), escalonamento vertical (NFR-S02) e SPOF aceito (NFR-A04).
* **Prós:**
  * Simplicidade operacional máxima: um único deployável, sem orquestração, sem balanceador, sem mensageria e sem cache para operar — compatível com o custo alvo (NFR-CO01) e com equipe enxuta.
  * Estado de sessão/token no servidor é naturalmente suportado, preservando a **invalidação imediata do token** (BR-07/NFR-SEC04) sem armazenamento compartilhado.
  * Controle direto do plano de consulta e dos índices (NFR-P02) — relevante dado que `ManutencaoSpecification.build` (complexidade 14) concentra a lógica de filtros e que não há ORM gerando consultas.
  * Estrutura em camadas já existente fornece fronteiras claras de responsabilidade e pontos de extração futuros.
  * Comunicação síncrona HTTP dispensa tracing distribuído (NFR-O02) e infraestrutura de filas.
* **Contras:**
  * SPOF aceito e documentado (NFR-A04): aplicação e persistência no mesmo deployment; indisponibilidade bloqueia cadastro patrimonial e fluxo de manutenção (NFR-A01).
  * Nenhum componente escala independentemente; escalonamento horizontal adiado até resolver o armazenamento compartilhado de tokens (NFR-S02).
  * Responsabilidade total pela parametrização SQL (NFR-SEC03) — sem sanitização de ORM; exige revisão de código e testes dedicados.
  * Maior custo de manutenção das queries em SQL cru.

### Option B: Backend distribuído em microsserviços por domínio
* **Descrição:** Decompor o backend em serviços independentemente deployáveis por domínio (ativos, manutenção, alertas, auditoria), com comunicação assíncrona por mensageria e autenticação distribuída.
* **Prós:**
  * Escala independente por domínio e isolamento de falhas.
  * Deployments desacoplados por domínio.
* **Contras:**
  * Exige infraestrutura de mensageria — **nenhum broker existe nas dependências** e nenhum é requisito nesta fase.
  * Exige armazenamento compartilhado/distribuído de tokens para validar credenciais em cada serviço e preservar a invalidação imediata (BR-07) — mecanismo inexistente e condicionado ao motor de persistência não confirmado (C-01).
  * Tracing distribuído passaria a ser obrigatório (hoje explicitamente não aplicável — NFR-O02), adicionando custo de instrumentação.
  * Complexidade operacional desproporcional a 150 usuários concorrentes e escopo interno; custo provavelmente acima do alvo de USD 2,50/usuário/mês (NFR-CO01).
  * Incompatível com os sinais de equipe enxuta do repositório (zero devDependencies, sem scripts de build — NFR-M06).

### Option C: Monólito multi-instância com balanceamento de carga desde o início
* **Descrição:** Manter o monólito em camadas, mas implantar múltiplas instâncias atrás de um balanceador de carga desde o primeiro deploy, eliminando o SPOF na camada de aplicação.
* **Prós:**
  * Elimina o SPOF da camada de aplicação; maior folga de disponibilidade.
  * Reaproveita o monólito existente (replicação em vez de decomposição).
* **Contras:**
  * Exige **armazenamento compartilhado de sessão/token** — a invalidação imediata no logout (BR-07/NFR-SEC04) não funciona com sessões locais em cada instância; o mecanismo de persistência que hospedaria esse estado **ainda não está confirmado (C-01)**.
  * Exige balanceador de carga e operação de múltiplas instâncias — custo adicional em tensão com NFR-CO01 e com NFR-PO02 (topologia de servidor único; multi-região/multi-AZ fora de escopo).
  * Prematuro: a meta de disponibilidade (99,0% em horário comercial, RTO ≤ 4h com restauração manual — NFR-A02/A04) é atingível com instância única; o escalonamento horizontal foi **conscientemente adiado** (NFR-S02).

## 5. Decision Outcome

**Opção escolhida:** **Option A — Monólito modular em camadas em instância única, com acesso a dados via SQL cru (sem ORM).**

**Rationale:**

1. **Custo e escopo (drivers 1, 8):** sistema interno com até 150 usuários concorrentes e alvo de USD 2,50/usuário/mês (NFR-CO01) não justifica o custo de mensageria, balanceador e orquestração das opções B e C. A instância única é a única opção compatível com o alvo de custo sem comprometer as metas funcionais.
2. **Modelo de sessão (driver 2):** a invalidação imediata do token no logout (BR-07/NFR-SEC04) exige estado de sessão/token no servidor. A Option A suporta isso nativamente; B e C exigem armazenamento compartilhado de tokens que depende do motor de persistência ainda não confirmado (C-01) — bloqueio explícito registrado no NFR-S02.
3. **Performance atingível (driver 3):** as metas (150 usuários com degradação < 10%, 50 req/s em leitura — NFR-S01/S03) são definidas **por instância única** e endereçadas por escalonamento vertical (NFR-S02). Os gargalos conhecidos são pontuais e endereçáveis dentro do monólito: ranking em memória de até 1000 candidatos (`AtivoService.java:119`, meta p95 < 1,5s — NFR-P04) e consultas com filtros combinados (`ManutencaoSpecification.build`, meta p95 < 1s / p99 < 2s — NFR-P02).
4. **Disponibilidade suficiente (driver 4):** 99,0% em horário comercial com RTO ≤ 4h e restauração manual testada semestralmente (NFR-A02/A04) é atingível em instância única; o SPOF resultante é aceito e documentado (NFR-A04). Zero SPOF não é exigível nesta fase.
5. **Manutenibilidade e compatibilidade (drivers 5, 6):** a estrutura em camadas já existe no codebase (338 arquivos Java em pacotes `model → repository → mapper → service`); a Option A formaliza o que está implementado, enquanto B exigiria recomposição radical incompatível com os sinais de equipe enxuta (NFR-M06).
6. **SQL cru sem ORM (sub-decisão da Option A):** sem ORM/query builder nas dependências, o plano de consulta é responsabilidade direta do código (NFR-P02) — controle fino de índices e consultas em troca da obrigação de **100% de consultas parametrizadas** (NFR-SEC03), verificada por revisão de código e testes dedicados. Esta sub-decisão permanece **condicionada à confirmação do motor de persistência (C-01)**: o padrão de acesso (SQL cru parametrizado) vale para qualquer motor SQL; a escolha do motor específico será registrada em ADR própria.

### Sub-decisões abrangidas por esta ADR
* **Comunicação frontend ↔ backend:** síncrona via HTTP com TLS 1.2+ (NFR-SEC01), sem mensageria — evidenciada por `request`/`authInterceptor` no frontend e `doFilterInternal` no backend; nenhuma rota/IPC catalogada na varredura (gap de documentação — NFR-M03).
* **Acesso a dados:** SQL cru, 100% parametrizado, sem ORM/query builder (NFR-SEC03).
* **Estratégia de escala:** vertical (NFR-S02); horizontal adiada.
* **SPOF:** aceito e documentado (NFR-A04).

### Comparison Matrix

> Pesos e scores atribuídos de forma estruturada a partir dos drivers e NFRs citados. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a decisão em si (Option A) é evidenciada pelo codebase e pelo SAD v1.0; a quantificação abaixo é uma formalização inferida para tornar o trade-off auditável e deve ser validada pela engenharia.

| Critério | Peso | Option A | Option B | Option C |
| :--- | :--- | :--- | :--- | :--- |
| Custo de infraestrutura (alvo < USD 2,50/usuário/mês — NFR-CO01) | 5 | 5 | 1 | 2 |
| Compatibilidade com invalidação imediata de token (BR-07/NFR-SEC04) | 5 | 5 | 2 | 2 |
| Atendimento às metas de performance/escala (150 usuários, 50 req/s — NFR-S01–S03) | 5 | 4 | 5 | 4 |
| Simplicidade operacional para equipe enxuta (NFR-M06) | 4 | 5 | 1 | 3 |
| Compatibilidade com a stack existente (sem mensageria/cache nas dependências) | 4 | 5 | 1 | 4 |
| Disponibilidade em horário comercial (99,0% — NFR-A01/A04) | 4 | 3 | 4 | 4 |
| Manutenibilidade e velocidade de evolução (NFR-M01–M02) | 4 | 4 | 2 | 4 |
| Caminho de evolução futura (escala horizontal, novos domínios) | 3 | 3 | 5 | 4 |
| **Total ponderado (máx. 170)** | — | **147** | **87** | **112** |

Leitura: a Option A vence nos critérios de maior peso (custo, modelo de sessão, escala definida por instância, simplicidade operacional); a Option C é o fallback natural caso a disponibilidade passe a exigir zero SPOF na camada de aplicação; a Option B só se justificaria com mudança de escala/escopo (ver gatilhos de revisão na Seção 7).

## 6. Consequences

### Positive
* Um único deployável para operar, monitorar e restaurar — compatível com RTO ≤ 4h via procedimento manual (NFR-A02) e com o custo alvo (NFR-CO01).
* Estado de sessão/token no servidor preserva a invalidação imediata no logout (BR-07/NFR-SEC04) sem infraestrutura adicional.
* Controle direto do plano de consulta e índices (NFR-P02), com a parametrização obrigatória (NFR-SEC03) como guardrail de segurança verificável.
* Camadas `model → repository → mapper → service` dão fronteiras claras para a refatoração obrigatória dos 5 pontos de complexidade > 10 (NFR-M02) e para eventuais extrações futuras de serviços.
* Comunicação síncrona HTTP dispensa tracing distribuído (NFR-O02) e infraestrutura de filas/cache.
* Estrutura de pacotes e topologia formalmente documentadas — encerra o estado "sem ADRs" apontado no SAD v1.0 como agravante da pendência C-01.

### Negative / Trade-offs
* **SPOF aceito e documentado** (NFR-A04): aplicação + persistência no mesmo deployment; indisponibilidade bloqueia o cadastro patrimonial e o fluxo de manutenção com aprovação (NFR-A01; BRD §1/§2).
* **Escalonamento horizontal conscientemente adiado** até que o armazenamento compartilhado de tokens seja resolvido e o NFR-SEC02 revalidado (NFR-S02).
* **Responsabilidade total pela parametrização SQL** (NFR-SEC03): concatenação de strings em SQL é proibida; verificação por revisão de código e testes dedicados — ênfase especial por operar sem ORM.
* **Ranking em memória de até 1000 candidatos** (`AtivoService.java:119` — TODO em código): risco de latência (meta p95 < 1,5s — NFR-P04); se não atendida, o ranking deve migrar para a camada de consulta com paginação.
* **Metas de performance atualmente não mensuráveis** — não há telemetria/APM instrumentada (BRD §4); instrumentação recomendada em homologação, usando o `RealisticDataSeeder` para a primeira onda de medição.
* **5 pontos com complexidade ciclomática acima do limite (10)** — `checkResourceUsageAlerts` (17), `RealisticDataSeeder.run` (15), `AtivoMapper.toDTO` (14), `ManutencaoSpecification.build` (14), `request` (13): refatoração obrigatória antes de novas evoluções nessas áreas (NFR-M02).

### Neutral
* **Motor de persistência não especificado (C-01):** esta ADR fixa o padrão de acesso (SQL cru parametrizado, válido para qualquer motor SQL), mas não o motor. A confirmação condiciona backup (NFR-A03), criptografia em repouso (NFR-SEC01), modelo de concorrência de escrita e eventual estado compartilhado de tokens — e será registrada em ADR própria. O contrato físico (tabelas, DDL) vive em [[db-schema-spec]] e ainda não foi extraído do código (nenhuma tabela detectada na varredura do schema).
* **Versão do runtime Java a confirmar** (NFR-PO03) — pré-requisito de go-live.
* **`package.json` sem nome e sem scripts** (NFR-M06): gap de automação/reprodutibilidade do build do frontend, a resolver independentemente desta decisão.
* **Mecanismo de servir o frontend não descobrível nos fontes** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]: o SAD assume servidor web estático distinto do processo de aplicação; a confirmar pela engenharia.
* **Catálogo de endpoints não extraído** (NFR-M03): a varredura não detectou rotas/IPC; a documentação de API será gerada em conjunto com a [[api-specification]], sem alterar esta decisão.

## 7. Implementation Notes

* **Ações necessárias** (a arquitetura já está implementada no codebase — nenhuma mudança estrutural é exigida para efetivar a decisão; as ações abaixo consolidam e encerram as pendências associadas):
  1. **Confirmar o motor de persistência e a versão do runtime Java** (C-01 / NFR-PO03 — levantamento técnico urgente, BRD §8) e registrar a decisão em ADR própria (ver Seção 8). Pré-requisito de go-live e desbloqueio de: backup (NFR-A03), criptografia em repouso (NFR-SEC01), modelo de concorrência de escrita e estado compartilhado de tokens.
  2. **Manter 100% das consultas SQL parametrizadas** (NFR-SEC03): checklist de revisão de código + testes dedicados, com ênfase em `ManutencaoSpecification.build` e na camada de acesso a dados em geral.
  3. **Refatorar os 5 pontos de complexidade > 10** (NFR-M02) antes de novas evoluções nessas áreas; prioridade para `ManutencaoSpecification.build` (impacto direto em NFR-P02) e para `request` em `api.js:54`.
  4. **Instrumentar medição de performance em homologação** com o `RealisticDataSeeder` (NFR §1) para validar NFR-P01–P04 e os guardrails de latência do BRD §4.
  5. **Documentar e testar o procedimento de backup/restauração** (RTO ≤ 4h testado semestralmente — NFR-A02/A04; RPO ≤ 24h — NFR-A03), incluindo obrigatoriamente a trilha de auditoria (BR-08) e o histórico de saúde (`getHealthHistory`), após a confirmação do motor (C-01).
  6. **Nomear o pacote e adicionar scripts de build/verificação no `package.json`** (NFR-M06); remover as chamadas `console.*` residuais (`api.js:44` e `api.js:107`) com verificação automatizada no pipeline (NFR-M04).
  7. **Implementar ou remover o stub `Usuario.setUsername`** (corpo vazio, linha 86 — NFR-M05 / C-04 do BRD).
* **Prazo estimado de migração:** não aplicável como "migração" — a decisão está implementada. Para as ações de consolidação, **nenhum prazo está registrado nos fontes**; o único prazo qualificado é a urgência do levantamento do motor de persistência (BRD §8 — "levantamento técnico urgente"), pré-requisito de go-live (NFR-PO03). [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] *Premissa: as ações 2–7 devem ser concluídas antes do go-live, na primeira onda de estabilização; cabe à engenharia datar o plano.*
* **Rollback plan:** reverter esta decisão significa migrar para a Option C ou Option B. Critérios e caminho:
  * **Gatilhos de revisão da decisão:** (a) demanda sustentada acima de 150 usuários concorrentes ou de 50 req/s que o scale-up vertical não atenda (NFR-S01/S03); (b) requisito de disponibilidade além de 99,0% em horário comercial ou exigência de zero SPOF (NFR-A01/A04); (c) necessidade de escala independente por domínio.
  * **Rollback para Option C (multi-instância):** pré-condições = armazenamento compartilhado de sessão/token que preserve a invalidação imediata (BR-07) + revalidação do NFR-SEC02 + decisão de infraestrutura de balanceamento. Custo moderado: o monólito é replicado, não decomposto.
  * **Rollback para Option B (microsserviços):** custo alto — exige mensageria (inexistente nas dependências), tracing distribuído (hoje não aplicável — NFR-O02) e autenticação distribuída. Mitigação existente: as camadas `model → repository → mapper → service` fornecem pontos de extração por domínio.
  * **Dimensão de dados:** enquanto o motor de persistência (C-01) não estiver confirmado e o sistema não entrar em produção, a reversão da estratégia de acesso a dados não tem custo de migração de dados; após o go-live, o custo dependerá do motor escolhido na ADR de persistência.

## 8. Links & References

* **Artefato-fonte:** System Architecture Document (SAD) v1.0 — a6 · AegisPatrimônio (seções 1–5, 7, 8, 11 e 12); diagrama de containers (C4) mantido em [[uml-diagrams]] — não duplicado aqui.
* **Requisitos não funcionais (NFR v1.0):** NFR-P01–P04 (performance), NFR-S01–S03 (escalabilidade), NFR-A01–A04 (disponibilidade), NFR-SEC01–SEC04 (segurança), NFR-O01–O04 (observabilidade), NFR-M01–M06 (manutenibilidade), NFR-C01–C03 (LGPD), NFR-CO01 (custo), NFR-U02/U03 (UX), NFR-PO02/PO03 (restrições de produto/infra).
* **BRD:** C-01 (motor de persistência pendente), C-04 (stub `setUsername`), BR-01/BR-04 (RBAC em 100% das escritas), BR-03 (histórico de saúde por filial), BR-07 (invalidação imediata de token), BR-08 (trilha de auditoria), §1 (fonte única de verdade), §2, §4 (guardrails/telemetria), §5 (Future Considerations), §8 (levantamento técnico urgente).
* **Diagnóstico do codebase:** varredura determinística do workspace + AST (gerada por `server/analyze-pipeline.ts`) — 353 arquivos (338 `.java`, 15 `.js`), 1.287 funções, 347 classes, 27.912 LOC.
* **Âncoras de código citadas:** `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` (TODO — ranking de até 1000 candidatos); `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` (complexidade 14); `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96` (complexidade 17); `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` (complexidade 14); `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34` (complexidade 15); `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86` (stub `setUsername`); `frontend/src/services/api.js:54` (função `request`, complexidade 13), `:44` e `:107` (`console.*` residuais).
* **Issues/tickets:** nenhum issue/ticket registrado nos fontes; o único débito trackado em código é o TODO de `AtivoService.java:119` (responsável e prazo pendentes — NFR-M06).
* **ADRs relacionadas:** **ADR-002 — Seleção do motor de persistência** (a criar; pendência C-01 do BRD — condiciona backup, criptografia em repouso, concorrência de escrita e estado compartilhado de tokens). Nenhuma ADR superseded ou deprecated por esta.