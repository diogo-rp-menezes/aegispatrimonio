# Non-Functional Requirements (NFR) — a6 · AegisPatrimônio

> **Versão:** 1.0 · **Status:** Draft · **Owner:** Engenharia/Arquitetura
> **Base de evidência:** diagnóstico determinístico do codebase (varredura + AST de 353 arquivos, ~27.912 LOC, 1.287 funções, 347 classes) e BRD v1.0 do AegisPatrimônio.
> **Topologia verificada:** Aplicação Web / Server-side — backend Java (338 arquivos `.java` em `src/`), frontend JavaScript (15 arquivos `.js` em `frontend/`), sem empacotamento desktop (não há `src-tauri/Cargo.toml`).
> **Persistência:** nenhum motor de banco ou ORM/query builder declarado nas dependências (única dependência de produção: `@popperjs/core ^2.11.8`); hipótese de trabalho é SQL cru. **Todos os requisitos condicionados a persistência estão marcados e dependem da confirmação técnica (C-01 do BRD).**

---

## 1. Performance

> Metas numéricas: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *premissas: sistema interno corporativo multi-filial, servidor de aplicação Java único, consultas em SQL cru (sem ORM) com índices apropriados, ambiente de homologação populado pelo `RealisticDataSeeder`.*

* **NFR-P01:** Operações CRUD e listagens simples (criar, atualizar, buscar por id, `listarTodos`) com tempo de resposta **p95 < 300ms e p99 < 800ms**, medidos no backend.
* **NFR-P02:** Consultas com filtros combinados (`ManutencaoSpecification`) e histórico de saúde por filial (`getHealthHistory`) com **p95 < 1s e p99 < 2s**. Sem ORM, o plano de consulta é responsabilidade direta do código — a definição de índices deve ser revisada em conjunto com a refatoração de `ManutencaoSpecification.build` (complexidade ciclomática 14, apontada pelo diagnóstico AST).
* **NFR-P03:** Entrega do frontend: **TTFB < 200ms** para assets estáticos e **LCP < 2,5s** no primeiro carregamento em rede corporativa/4G.
* **NFR-P04:** Caminho de busca/ranking de ativos (`AtivoService.java:119` — carrega até 1000 candidatos id+nome e ranqueia em memória, conforme TODO e diagnóstico): **p95 < 1,5s com 1000 candidatos**; latência monitorada como guardrail (BRD §4). Se a meta não for atendida, o ranking deve ser empurrado para a camada de consulta e paginado.

**Método de medição:** a definir pela engenharia — **não há APM, agente de load testing ou telemetria instrumentados no repositório** (`package.json` sem scripts e sem devDependencies; BRD §4 registra ausência de telemetria). Recomenda-se instrumentar a medição em homologação com dados do `RealisticDataSeeder` antes do go-live. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]

## 2. Escalabilidade

> Metas numéricas: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *premissas: uso interno (todas as filiais), pico em horário comercial, topologia de instância única.*

* **NFR-S01:** Suportar **150 usuários concorrentes** por instância única sem degradação > 10% nas metas de NFR-P01/NFR-P02.
* **NFR-S02:** A estratégia primária de capacidade é o **escalonamento vertical** (scale-up da instância). **Escalonamento horizontal NÃO é requisito nesta fase**: a invalidação imediata do token no logout (BR-07) implica estado de sessão/token no servidor, e o mecanismo de persistência — que hospedaria esse estado compartilhado — ainda não está confirmado (C-01). Qualquer evolução para múltiplas instâncias exige antes resolver o armazenamento compartilhado de tokens e revalidar o NFR-SEC02.
* **NFR-S03:** Throughput mínimo de **50 req/s sustentados por instância** nas operações de leitura (listagens e consultas).
* **Nota de escrita concorrente:** as metas de escrita concorrente dependem do motor de persistência ainda não identificado (C-01) — até a confirmação, assume-se escrita transacional moderada típica da operação patrimonial (cadastros, fluxo de manutenção, health checks). Meta a ser redefinida após o levantamento técnico urgente da stack real de dados (risco registrado no BRD §8).

## 3. Disponibilidade & Confiabilidade

> Metas: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *premissas: sistema interno de instância única, sem alta disponibilidade distribuída (não suportada pela stack identificada).*

* **NFR-A01:** Uptime mínimo de **99,0% em horário comercial** (08:00–18:00, dias úteis), com janela de manutenção fora do expediente. O sistema é a fonte única de verdade do patrimônio (BRD §1) — indisponibilidade bloqueia cadastro e o fluxo de manutenção com aprovação.
* **NFR-A02:** **RTO ≤ 4h** — restauração em servidor único com procedimento manual documentado.
* **NFR-A03:** **RPO ≤ 24h** (backup diário). A estratégia de backup **depende da confirmação do motor de persistência** (C-01) e deve incluir obrigatoriamente a trilha de auditoria (BR-08) e o histórico de saúde (`getHealthHistory`).
* **NFR-A04:** Zero single points of failure **não é exigível nesta fase** — a topologia de instância única implica SPOF aceito e documentado. Requisito correspondente: procedimento de restauração (NFR-A02/A03) **testado a cada semestre** e monitoramento da saúde da própria aplicação (alertas internos já existentes via `checkResourceUsageAlerts`).

## 4. Segurança

* **NFR-SEC01:** **TLS 1.2+ (preferencialmente 1.3) em 100% do tráfego** entre frontend e backend (camada `request`/`authInterceptor`). Criptografia em repouso de credenciais e dados pessoais de funcionários: mecanismo **a definir junto com a confirmação do motor de persistência** (C-01). [parcialmente inferido — REQUER VALIDAÇÃO HUMANA]
* **NFR-SEC02:** Autenticação por **token emitido no login** (`createUserAndToken`) e validado em **cada requisição** (`doFilterInternal` no backend; `authInterceptor` no frontend). Autorização **RBAC com contexto** (perfil + filial/departamento) via `hasPermission` cobrindo **100% das operações de escrita** (BR-01, BR-04) e a leitura do histórico de saúde por filial (BR-03). MFA **não é evidenciado no código** — tratado como evolução futura, não requisito desta fase.
* **NFR-SEC03:** Conformidade com **OWASP Top 10** nas categorias aplicáveis, com ênfase obrigatória em **injeção de SQL**: como o projeto opera com SQL cru (sem ORM/query builder), **100% das consultas devem ser parametrizadas** — concatenação de strings em SQL é proibida. Verificação por revisão de código e testes dedicados.
* **NFR-SEC04:** **Logout invalida imediatamente o token** (BR-07); tokens de acesso com tempo de expiração definido; sessão encerrada (`clearSession`) exige nova autenticação; nenhum segredo/credencial versionado no repositório; rotação de segredos de configuração a cada 90 dias [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — não há segredos identificados nos fontes].

## 5. Usabilidade & Acessibilidade

> Metas: [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *premissas: ferramenta interna desktop-first; aplicativo mobile nativo fora de escopo (BRD §5); não há auditoria de acessibilidade no repositório.*

* **NFR-U01:** Fluxos críticos (cadastro patrimonial, manutenção com aprovação, alertas, histórico de saúde) aderentes a **WCAG 2.1 nível AA**.
* **NFR-U02:** **100% dos erros de API apresentados ao usuário de forma tratada** via `handleApiError` (guardrail do BRD §4); mensagens compreensíveis e **sem exposição de detalhes internos** (stack traces, SQL, estrutura de diretórios).
* **NFR-U03:** Interface utilizável de **1280px a 1920px** (desktop corporativo), com degradação aceitável até 1024px.
* **NFR-U04:** Terminologia da interface e mensagens aderente ao **Glossário (Ubiquitous Language)** — usar os termos canônicos (ex.: "aprovar", nunca "homologar/deferir/ratificar"), conforme C-03 do BRD.

## 6. Observabilidade

> [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *não há telemetria instrumentada no repositório (BRD §4); os itens abaixo são requisitos de implementação, não estado atual.*

* **NFR-O01:** Logs estruturados no backend com **correlation ID por requisição** e endpoint de health check da aplicação; frontend sem `console.*` em produção (ver NFR-M04).
* **NFR-O02:** **Tracing distribuído NÃO se aplica** à topologia de instância única (não há microsserviços). Requisito correspondente: correlation ID propagado entre frontend (`request`) e backend e presente em **100% dos logs de requisições que falharem**, viabilizando investigação de incidentes de acesso indevido (guardrail do BRD §4).
* **NFR-O03:** Alertas operacionais para: **p95 de latência das listagens** (guardrail do BRD — área sensível do TODO em `AtivoService`), **taxa de erro > 1%** e **saturação de recursos** do servidor. Instrumentação a definir pela engenharia (nada no repositório).
* **NFR-O04:** Eventos de auditoria (`onUpdate`/`preUpdate`, BR-08) consultáveis por registro com data/hora da última modificação — rastreabilidade auditável exigida pelo perfil Auditor/Compliance (BRD §3).

## 7. Manutenibilidade & Qualidade de Código

* **NFR-M01:** Cobertura mínima de testes de **80%** nos módulos de serviço e mapper [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; suíte de regressão de permissões (cenários já verificados: criar/atualizar/deletar com USER → Forbidden; `listarTodos_comUser_deveRetornarOk`) executada a 100% em cada release.
* **NFR-M02:** Complexidade ciclomática máxima de **10 por função/método**. Os **5 pontos acima do limite** identificados pelo diagnóstico AST — `checkResourceUsageAlerts` (17), `RealisticDataSeeder.run` (15), `AtivoMapper.toDTO` (14), `ManutencaoSpecification.build` (14), `request` no frontend (13) — devem ser **refatorados antes de novas evoluções** nessas áreas (risco no BRD §8).
* **NFR-M03:** Documentação dos endpoints da API mantida e atualizada a cada release; formato/ferramenta a definir — **não há documentação de API instrumentada no repositório**.
* **NFR-M04:** **Zero chamadas `console.*`** em código de frontend de produção, por higiene de informação e qualidade; as 2 residuais (`frontend/src/services/api.js:44` — `console.error`, e `:107` — `console.debug`) removidas e verificação automatizada adicionada ao pipeline.
* **NFR-M05:** **Nenhum método stub (corpo vazio) em produção**; `Usuario.setUsername` (linha 86) implementado ou removido do fluxo de atualização, coberto por teste de atualização de usuário (C-04 do BRD).
* **NFR-M06:** Build e verificação automatizados: pacote do frontend **nomeado** e **scripts de build/verificação definidos** no `package.json` (atualmente sem nome e sem scripts — risco no BRD §8); TODO de performance em `AtivoService.java:119` trackado com responsável e prazo.

## 8. Compliance & Regulatório

> [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] — *nenhuma exigência regulatória específica identificada no código; os itens abaixo derivam do domínio (dados pessoais de funcionários) e devem ser validados com Compliance (BRD §7).*

* **NFR-C01:** **LGPD** — o sistema trata dados pessoais de funcionários (vínculo funcionário↔credenciais via `createFuncionarioAndUsuario`): acesso restrito por perfil (BR-01/BR-04), trilha de auditoria de modificações (BR-08) e mecanismo de atendimento a direitos do titular (ex.: anonimização/exclusão de ex-funcionários) a definir.
* **NFR-C02:** **Retenção de dados**: política de retenção da trilha de auditoria e do histórico de saúde a definir pela Compliance; o histórico de saúde (`getHealthHistory`) deve ser retido de forma a viabilizar a análise preditiva de falhas futura (BRD §5 — Future Considerations).
* **NFR-C03:** Normas de gestão patrimonial aplicáveis ao setor — **a verificar com Compliance**; nenhuma norma identificada nos fontes analisados.

## 9. Portabilidade & Compatibilidade

* **NFR-PO01:** Compatibilidade com os **2 últimos major releases** dos navegadores do ambiente corporativo (Chrome, Firefox, Edge) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — validar matriz de navegadores homologados pela TI].
* **NFR-PO02:** Deploy suportado em **servidor único** (on-premises ou VM de nuvem única), conforme a topologia Aplicação Web/Server-side; **multi-cloud e multi-região não são requisitos nesta fase** (alta disponibilidade distribuída não suportada pela stack identificada).
* **NFR-PO03:** Versão do runtime Java e mecanismo real de persistência **confirmados e documentados** pela engenharia antes do go-live — nenhum dos dois está declarado nas dependências (C-01 do BRD; levantamento técnico urgente registrado no BRD §8).

## 10. Custo (Cost Efficiency)

* **NFR-CO01:** Custo de infraestrutura por usuário ativo abaixo de **USD 2,50/mês** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — *premissas: instância única de servidor para ~150 usuários concorrentes; sem licenciamento de terceiros: a única dependência de produção (`@popperjs/core ^2.11.8`) não implica custo de licença*]. Guardrail registrado no BRD §4.

## 11. Rastreabilidade

| ID | Requisito Relacionado (User Story/BRD) | Prioridade | Status de Validação |
| :--- | :--- | :--- | :--- |
| NFR-P01 | BRD §4 — Guardrail: latência das consultas de listagem | Must | Não validado |
| NFR-P02 | BRD §5 — Consultas com filtros combinados (`ManutencaoSpecification`); BRD §8 — risco de complexidade | Must | Não validado |
| NFR-P03 | BRD §3 — Personas (uso diário por Gestor de Filial e Administrador de Patrimônio) | Should | Não validado |
| NFR-P04 | BRD §5/§8 — TODO de performance em `AtivoService` (ranking de até 1000 candidatos) | Should | Gap validado (TODO identificado no diagnóstico AST) |
| NFR-S01 | BRD §1 — gestão patrimonial multi-filial | Should | Não validado |
| NFR-S02 | BR-07 (invalidação imediata de token); C-01 (persistência não confirmada) | Must | Não validado (condicionado a C-01) |
| NFR-S03 | BRD §4 — meta de adoção (≥ 90% dos ativos monitorados via health check) | Should | Não validado |
| NFR-A01 | BRD §1 — fonte única de verdade do patrimônio | Must | Não validado |
| NFR-A02 | BRD §2 — Cost of Inaction (parada da operação patrimonial) | Must | Não validado |
| NFR-A03 | BR-08 (trilha de auditoria); BRD §5 — histórico de saúde para análise preditiva | Must | Não validado |
| NFR-A04 | Contexto NFR — Topologia Aplicação Web/Server-side (instância única) | Should | Não validado |
| NFR-SEC01 | BRD §7 — Premissas e dependências de segurança | Must | Não validado |
| NFR-SEC02 | BR-01, BR-03, BR-04, BR-07 | Must | Parcialmente validado (cenários de permissão verificados em teste: USER → Forbidden) |
| NFR-SEC03 | BRD §7; C-01 (SQL cru, sem ORM) | Must | Não validado |
| NFR-SEC04 | BR-07 (logout invalida token); BRD §5 — Sessão (`clearSession`) | Must | Não validado |
| NFR-U01 | BRD §3 — Personas | Should | Não validado |
| NFR-U02 | BRD §4 — Guardrail: taxa de erros de API tratados (`handleApiError`) | Must | Não validado |
| NFR-U03 | BRD §3 — Personas; BRD §5 — mobile nativo fora de escopo | Could | Não validado |
| NFR-U04 | C-03 — termos canônicos do Glossário em código e documentação | Must | Não validado |
| NFR-O01 | BRD §4 — nota de transparência (sem telemetria instrumentada) | Should | Não validado |
| NFR-O02 | BRD §4 — Guardrail: incidentes de acesso indevido | Should | Não validado |
| NFR-O03 | BRD §4 — Guardrail: latência das listagens; TODO em `AtivoService` | Should | Não validado |
| NFR-O04 | BR-08 — data/hora de modificação automática (`onUpdate`/`preUpdate`) | Must | Parcialmente validado (mecanismo implementado no código — BRD §4) |
| NFR-M01 | BRD §8 — risco de complexidade (mitigação: testes de regressão de permissões) | Must | Não validado |
| NFR-M02 | Diagnóstico AST — 5 funções acima do limite; BRD §8 | Must | Gap validado (violação atual confirmada pelo diagnóstico) |
| NFR-M03 | C-02 — documentação em português; BRD §12 — histórico de revisão | Should | Não validado |
| NFR-M04 | Diagnóstico — 2 chamadas `console.*` residuais em `api.js`; BRD §8 | Must | Gap validado (residuais confirmadas nas linhas 44 e 107) |
| NFR-M05 | C-04 — stub `Usuario.setUsername` com corpo vazio | Must | Gap validado (stub confirmado na linha 86) |
| NFR-M06 | BRD §8 — `package.json` sem nome e sem scripts | Should | Gap validado (confirmado no diagnóstico) |
| NFR-C01 | BRD §5 — `createFuncionarioAndUsuario`; BRD §7 — verificar Compliance | Must | Não validado |
| NFR-C02 | BR-08; BRD §5 — análise preditiva futura sobre histórico de saúde | Should | Não validado |
| NFR-C03 | BRD §7 — normas de gestão patrimonial a verificar com Compliance | Should | Não validado |
| NFR-PO01 | BRD §3 — Personas (acesso via navegadores corporativos) | Should | Não validado |
| NFR-PO02 | Contexto NFR — Topologia Aplicação Web/Server-side | Should | Não validado |
| NFR-PO03 | C-01 — persistência não declarada; BRD §8 — levantamento técnico urgente | Must | Não validado |
| NFR-CO01 | BRD §4 — Guardrail: custo de infraestrutura por usuário; BRD §9 — modelo de custo recorrente | Could | Não validado |

> **Nota de validação:** os status "Gap validado" indicam que a violação/atendimento parcial do requisito foi confirmado pelo diagnóstico determinístico do codebase; os demais requisitos devem ser validados na primeira onda de medição em homologação (populada pelo `RealisticDataSeeder`), após a confirmação da stack de persistência (C-01).