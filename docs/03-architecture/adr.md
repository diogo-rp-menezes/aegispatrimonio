# ADR-001 — Acesso a dados via SQL cru sem ORM no backend Java

> **Data:** Não registrada nos artefatos-fonte — preencher com a data de ratificação [REQUER VALIDAÇÃO HUMANA] · **Status:** Proposed
> **Deciders:** A definir — nenhum decisor registrado no SAD v1.0 ou no diagnóstico do workspace [REQUER VALIDAÇÃO HUMANA] · **Consulted:** Arquitetura/Engenharia (owner do SAD v1.0) · **Informed:** Equipes de desenvolvimento backend (Java) e frontend (JavaScript)

## 1. Status

**Proposed** — Esta ADR formaliza a estratégia de acesso a dados já implementada de facto no codebase (SQL cru, sem ORM/query builder — verificado na varredura de dependências e registrado no diagnóstico de stack) e propõe sua ratificação pelos decidores. Nenhuma ADR formal anterior existe no projeto (verificado no SAD — cabeçalho e Seção 12). Enquanto não ratificada, a decisão permanece registrada apenas inline no SAD (Seções 12 e 13).

## 2. Context & Problem Statement

O backend do Sistema de Gestão de Patrimônio (A4) — 335 arquivos `.java` em `src/`, pacote `br.com.aegispatrimonio`, com camadas visíveis na varredura (`config`, `mapper`, `model`, `repository`, `service`) — acessa a camada de dados **sem ORM ou query builder**: a varredura de dependências não encontrou nenhum ORM/query builder nas dependências verificadas (o diagnóstico registra "provavelmente SQL cru" e o SAD trata a caracterização como verificada). A abordagem está implementada de facto, com evidências concretas da varredura AST:

- `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` — método `build` (complexidade ciclomática 14) constrói consultas dinamicamente para a filtragem de manutenção (RF-15/RF-22);
- `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` — método `toDTO` (complexidade 14) converte entidades em DTOs manualmente;
- `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` — TODO de performance: caminho que carrega até 1.000 candidatos (id+nome) e faz ranking em memória.

O SAD v1.0 (Seção 12) analisa o trade-off dessa abordagem e registra explicitamente: "Nenhuma ADR formal registrada". A decisão a tomar, em termos neutros: **manter o acesso a dados sem camada de ORM/query builder, ou introduzir uma**. A estratégia atual concentra no código de aplicação responsabilidades que uma camada de persistência normalmente absorveria — parametrização de consultas, construção dinâmica de filtros e mapeamento entidade↔DTO — com efeitos opostos sobre os objetivos arquiteturais:

- **Favorável à performance:** controle direto sobre índices e planos de execução, alinhado às metas NFR-P01 (p95 < 200ms) e NFR-P04 (relatórios de custo total por ativo < 3s p95 para 10.000+ ativos);
- **Desfavorável à segurança e à manutenibilidade:** superfície de SQL injection sensível a erro humano (NFR-SEC06 — requisito crítico), produtividade menor e complexidade acima do limite proposto (NFR-M02: `toDTO` = 14 e `build` = 14 vs. limite ≤ 10).

Dois condicionantes verificados agravam a decisão:

1. **Não há gate de testes verificável** — o `package.json` não declara scripts e não há configuração de pipeline encontrada (NFR-M01, lacuna 3): qualquer migração de acesso a dados hoje ocorreria sem rede de segurança contra regressões.
2. **O motor de banco de dados não está especificado** (lacuna 1 do NFR) — metas de escrita concorrente (NFR-S01), backup/RPO (NFR-A03) e comportamento de lock não podem ser calibradas; a validação de índices e planos de execução depende do motor real.

Nenhuma tabela foi detectada no schema pela varredura — o contrato físico (tabelas, colunas, constraints, DDL) deve viver em [[db-schema-spec]] e o modelo conceitual em [[db-domain-model]], ambos a produzir.

**Escopo desta ADR:** a estratégia de acesso a dados (SQL cru vs. ORM/query builder). **Fora do escopo:** a escolha do motor de banco de dados (decisão dependente e separada — pendência de maior impacto arquitetural segundo o SAD, Seção 13), a estratégia de cache para relatórios (risco R-03 do BRD) e o escalonamento horizontal (NFR-S02) — cada uma com ADR própria prevista.

## 3. Decision Drivers

* **Performance com controle explícito (NFR-P01/NFR-P04)** — p95 < 200ms / p99 < 500ms e relatórios < 3s p95 para 10.000+ ativos; sem ORM, índices e planos de execução são responsabilidade direta do código (`ManutencaoSpecification.build`).
* **Segurança — SQL 100% parametrizado (NFR-SEC06, crítico)** — consultas dinâmicas são superfície de risco prioritária para injection; a garantia depende de disciplina de revisão e de testes.
* **Manutenibilidade (NFR-M01/NFR-M02)** — complexidade ciclomática ≤ 10 (`toDTO` e `build` já excedem o limite); cobertura de testes > 80% com gate de CI não implementada de forma verificável (lacuna 3).
* **Compatibilidade com a stack verificada** — o `package.json` declara 1 dependência de produção (`@popperjs/core` ^2.11.8) e 0 de desenvolvimento; introduzir ORM adiciona dependência e camada de abstração inexistentes hoje.
* **Custo/risco de migração** — 27.537 LOC e 344 classes; nenhuma ferramenta de migração de schema verificada ([[db-migration-spec]] a produzir); o seeder `RealisticDataSeeder` (complexidade 15) é carga de dados, não migração de schema.

## 4. Considered Options

### Option A: Manter SQL cru sem ORM (formalizar o status quo)

* **Descrição:** manter o acesso a dados via SQL cru, sem ORM/query builder, com consultas dinâmicas construídas no código (`ManutencaoSpecification.build`), mapeamento entidade↔DTO manual (`AtivoMapper.toDTO`) e parametrização obrigatória em 100% das consultas (NFR-SEC06). É a abordagem de facto implementada nos 335 arquivos `.java` verificados.
* **Prós:**
  * Controle total e explícito sobre índices, planos de execução e parametrização — alinhado às metas de p95 < 200ms (NFR-P01) e relatórios < 3s (NFR-P04);
  * Zero dependência adicional de produção — preserva a stack verificada mínima;
  * Sem camada de abstração que possa mascarar consultas ineficientes (ex.: N+1, planos de execução ruins);
  * Compatibilidade total com o codebase existente — nenhuma refatoração massiva necessária.
* **Contras:**
  * Risco elevado de SQL injection se a parametrização falhar — NFR-SEC06 é crítico e depende de disciplina de revisão; `ManutencaoSpecification.build` é a superfície de risco prioritária;
  * Produtividade menor: mapeamento manual (`AtivoMapper.toDTO`, complexidade 14 — acima do limite ≤ 10, NFR-M02);
  * Qualidade de acesso a dados condicionada ao gate de testes (NFR-M01), hoje não verificável (lacuna 3).

### Option B: Adotar ORM/query builder no backend Java [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

* **Descrição:** introduzir um ORM ou query builder nas camadas `repository/` e `mapper/` para abstrair consultas e o mapeamento entidade↔DTO. **Premissas adotadas:** (1) nenhum produto específico é nomeado nesta ADR — o diagnóstico confirma a ausência de ORM/query builder nas dependências e nenhum candidato está documentado nos artefatos-fonte; a seleção do produto exigiria spike/POC própria; (2) a adoção seria incremental, módulo a módulo, a partir do CRUD de menor risco.
* **Prós:**
  * Produtividade de desenvolvimento e redução de código repetitivo de mapeamento;
  * Parametrização por padrão na maioria dos ORMs — reduz a superfície de injection decorrente de erro humano;
  * Abstrações de consulta dinâmica prontas — substituiriam a construção manual do `build`.
* **Contras:**
  * Adiciona dependência de produção inexistente e nova camada de abstração à stack verificada;
  * Risco de consultas ineficientes mascaradas pela abstração — contra p95 < 200ms (NFR-P01) e relatórios < 3s (NFR-P04);
  * Superfície de migração grande: 27.537 LOC, 344 classes, sem gate de testes verificado (NFR-M01, lacuna 3) — risco de regressões silenciosas;
  * Curva de aprendizado e acoplamento ao ciclo de vida do produto escolhido (a definir em POC).

### Option C: Híbrido — SQL cru em caminhos críticos + ORM em CRUD simples [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

* **Descrição:** manter SQL cru onde o controle de performance é crítico — relatórios de custo total por ativo (RF-17, risco R-03 do BRD) e consultas dinâmicas de filtragem de manutenção (RF-15/RF-22) — e adotar ORM/query builder apenas para operações CRUD simples das entidades mestres. **Premissa adotada:** a fronteira entre "caminho crítico" e "CRUD simples" seria definida por medição (p95 por endpoint — NFR-P01) e revisada periodicamente.
* **Prós:**
  * Preserva o controle de performance nos caminhos críticos (NFR-P01/P04);
  * Ganha produtividade no CRUD de baixa complexidade.
* **Contras:**
  * Dois paradigmas de acesso a dados coexistindo — custo cognitivo e inconsistência de padrões (NFR-M02);
  * A fronteira entre "crítico" e "simples" é subjetiva e sujeita a erosão ao longo do tempo;
  * Herda os contras de migração da Option B (dependência nova, sem gate de testes verificado) com ganho parcial.

## 5. Decision Outcome

**Opção escolhida:** Option A — Manter SQL cru sem ORM (formalizar o status quo).

**Rationale:**

- **Custo/risco de migração (driver decisivo):** a abordagem já está implementada de facto em 335 arquivos `.java` (27.537 LOC, 344 classes — verificados) e **não há gate de testes verificado** (NFR-M01, lacuna 3). Migrar para ORM (Options B/C) hoje seria uma mudança de grande superfície sem rede de segurança — o risco de regressão silenciosa supera o ganho de produtividade esperado.
- **Performance:** as metas NFR-P01 (p95 < 200ms) e NFR-P04 (relatórios < 3s para 10.000+ ativos) favorecem controle direto sobre índices e planos de execução — com SQL cru, essa responsabilidade é explícita no código (`ManutencaoSpecification.build`), sem abstração que possa mascarar consultas ineficientes. O TODO de performance em `AtivoService.java:119` (até 1.000 candidatos + ranking em memória) demonstra que os caminhos sensíveis já exigem ajuste fino — o controle direto é pré-requisito para esse ajuste.
- **Segurança:** o requisito NFR-SEC06 (SQL 100% parametrizado) é enforcementável em ambas as opções; com SQL cru, aceita-se o custo de disciplina de revisão em troca de uma superfície de risco conhecida e priorizada (`ManutencaoSpecification.build`), em vez de distribuída por uma abstração.
- **Compatibilidade com a stack:** a stack verificada tem exatamente 1 dependência de produção (`@popperjs/core` ^2.11.8) e 0 de desenvolvimento; introduzir ORM contraria a minimização de dependências e adiciona abstração inexistente.
- **Leitura honesta da matriz:** a Option A vence com folga condicionada ao peso do custo/risco de migração (20 dos 21 pontos de diferença sobre a Option B vêm desse critério). Se o gate de cobertura > 80% (NFR-M01) for implementado e uma ferramenta de migração for definida — reduzindo o peso desse driver —, a Option C torna-se o caminho de reversão natural (ver Rollback plan na Seção 7).

### Comparison Matrix

Escala de score: 1 (pior) a 5 (melhor). Os pesos refletem a prioridade dos NFRs correspondentes e o estágio atual do projeto (sem gate de testes verificado).

| Critério | Peso | Option A (SQL cru) | Option B (ORM) | Option C (Híbrido) |
| :--- | :--- | :--- | :--- | :--- |
| Controle sobre performance/planos de execução (NFR-P01/P04) | 5 | 5 | 3 | 4 |
| Segurança — superfície de SQL injection (NFR-SEC06) | 5 | 3 | 4 | 3 |
| Custo/risco de migração (27.537 LOC, sem gate de testes) | 5 | 5 | 1 | 2 |
| Manutenibilidade/consistência de padrões (NFR-M02) | 4 | 3 | 4 | 2 |
| Produtividade de desenvolvimento | 3 | 2 | 5 | 3 |
| Compatibilidade com a stack verificada (zero dependências novas) | 3 | 5 | 2 | 3 |
| **Total ponderado** | — | **98** | **77** | **71** |

## 6. Consequences

### Positive

* Controle total e explícito sobre índices, planos de execução e parametrização — alinhado às metas de p95 < 200ms (NFR-P01) e relatórios < 3s p95 para 10.000+ ativos (NFR-P04).
* Zero dependência adicional de produção — a stack verificada permanece mínima (única dependência de produção declarada: `@popperjs/core` ^2.11.8).
* Nenhuma refatoração massiva do codebase existente (335 arquivos `.java`, 344 classes) — o esforço de engenharia concentra-se nos hotspots conhecidos em vez de numa migração.
* Superfície de risco de injection conhecida, mapeável e priorizada (`ManutencaoSpecification.build`), em vez de distribuída por uma camada de abstração.

### Negative / Trade-offs

* **Risco elevado de SQL injection se a parametrização falhar** — NFR-SEC06 é crítico; a garantia depende de disciplina de revisão e de testes de integração, e o gate de cobertura > 80% (NFR-M01) não está implementado de forma verificável (lacuna 3).
* **Produtividade de desenvolvimento menor** — mapeamento manual entidade↔DTO (`AtivoMapper.toDTO`, complexidade 14, acima do limite ≤ 10 — NFR-M02) e construção manual de consultas dinâmicas.
* **Qualidade de acesso a dados acoplada à competência do time** — índices, DDL e planos de execução são responsabilidade direta do código/time, sem abstração que mitigue erros.
* **Débito técnico consciente registrado (NFR-M04) permanece:** stub `Usuario.setUsername` (`src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`), TODO de performance (`AtivoService.java:119`) e 12 funções com complexidade acima do limite proposto.

### Neutral

* A responsabilidade pelo contrato físico (tabelas, colunas, constraints, DDL) fica explicitamente no time — deve ser documentada em [[db-schema-spec]] e [[db-domain-model]] (a produzir; nenhuma tabela detectada na varredura).
* O SQL cru pode acoplar partes do código ao dialeto do motor de banco eventualmente escolhido — ponto a considerar na ADR do motor (lacuna 1).
* Migrações de schema continuam sem ferramenta verificada — ver [[db-migration-spec]]; o seeder `RealisticDataSeeder` (complexidade 15) é carga de dados, não migração de schema, e deve executar em staging com validação (risco R-02 do BRD).
* A decisão não altera a topologia (aplicação server-side única — verificada no SAD) nem resolve o motor de banco de dados (lacuna 1) — a validação de índices e planos de execução permanece condicionada ao motor real.

## 7. Implementation Notes

* **Ações necessárias:**
  1. **Parametrização 100% (NFR-SEC06):** revisão prioritária de `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` e auditoria de concatenação de strings SQL nas camadas `repository/` e `service/` — superfície de risco prioritária desta decisão.
  2. **Gate de testes (NFR-M01):** implementar pipeline com gate de cobertura > 80% antes de qualquer evolução do acesso a dados — hoje não há scripts no `package.json` nem configuração de pipeline verificável (lacuna 3).
  3. **Refatoração de hotspots (NFR-M02):** quebrar `AtivoMapper.toDTO` (complexidade 14) e `ManutencaoSpecification.build` (14) em unidades menores, preservando os contratos — alvo ≤ 10.
  4. **Validação de performance no motor real:** quando o motor de banco for verificado (lacuna 1), validar índices e planos de execução das consultas dinâmicas contra NFR-P01/P04 e reavaliar o TODO de `AtivoService.java:119` (até 1.000 candidatos + ranking em memória) antes do go-live.
  5. **Documentação do contrato de dados:** produzir [[db-schema-spec]] e [[db-domain-model]] — nenhuma tabela foi detectada na varredura.
  6. **ADRs dependentes:** registrar em ADRs próprias a decisão do motor de banco de dados (lacuna 1 — pendência de maior impacto arquitetural segundo o SAD, Seção 13), a estratégia de cache para relatórios (R-03/NFR-P04), o escalonamento horizontal (NFR-S02) e a estratégia de backup/restore (NFR-A03).
* **Prazo estimado de migração:** não aplicável como migração — a decisão formaliza a abordagem existente. Para as ações 1–3: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — nenhum prazo ou planejamento de sprint está registrado nos artefatos-fonte; sequenciamento sugerido: ação 1 (parametrização) antes do go-live; ação 2 (gate de testes) imediatamente em seguida; ação 3 (refatoração) condicionada ao gate ativo.
* **Rollback plan:** se os trade-offs de produtividade/qualidade se provarem insustentáveis após o gate de testes estar ativo (NFR-M01), reverter incrementalmente para a Option C (híbrido) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]: adotar ORM/query builder (produto a selecionar em POC — nenhum candidato documentado nos artefatos-fonte) começando por um módulo de CRUD de baixo risco, mantendo SQL cru nos caminhos críticos (relatórios RF-17, filtragem de manutenção RF-15/RF-22). Pré-condições objetivas de reversão: (a) gate de cobertura > 80% ativo; (b) POC comparando planos de execução e latência contra NFR-P01/P04; (c) motor de banco verificado (lacuna 1). A reversão total (Option B) só seria reavaliada se a Option C também não atender aos critérios.

## 8. Links & References

* **System Architecture Document (SAD) v1.0** — Seção 2 (Tech Stack Justification), Seção 5 (Data Modeling), Seção 12 (Trade-offs & Known Limitations — análise do trade-off SQL cru sem ORM), Seção 13 (Future Evolution — ADRs a formalizar).
* **Non-Functional Requirements ([[nfr]]) v1.0** — NFR-P01, NFR-P04 (performance); NFR-SEC06 (SQL 100% parametrizado); NFR-M01 (gate de cobertura > 80%), NFR-M02 (complexidade ≤ 10), NFR-M04 (débito técnico); NFR-S01 (escrita concorrente), NFR-A03 (backup/RPO).
* **Diagnóstico determinístico do codebase** — varredura AST real: 350 arquivos, 1.268 funções, 344 classes, 27.537 LOC; achados estruturais citados: `ManutencaoSpecification.java:26`, `AtivoMapper.java:15`, `AtivoService.java:119` (TODO), `Usuario.java:86` (stub), `RealisticDataSeeder.java:34`.
* **Código-fonte:** `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java`, `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java`, `src/main/java/br/com/aegispatrimonio/service/AtivoService.java`, `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java`.
* **Artefatos relacionados a produzir:** [[db-schema-spec]], [[db-domain-model]], [[db-migration-spec]], [[uml-diagrams]].
* **ADRs relacionadas:** nenhuma ADR formal anterior existe (verificado no SAD) — esta é a primeira (ADR-001). Pendentes de formalização (SAD, Seção 13): motor de banco de dados (lacuna 1), estratégia de cache para relatórios (R-03/NFR-P04), escalonamento horizontal (NFR-S02), estratégia de backup/restore (NFR-A03).
* **Issues/tickets:** nenhum ticket registrado nos artefatos-fonte — vincular a issue correspondente na ratificação, se existir [REQUER VALIDAÇÃO HUMANA].