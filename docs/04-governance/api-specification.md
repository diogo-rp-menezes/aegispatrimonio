# API & Integration Contract Specification — AegisPatrimônio (API HTTP interna do backend)

> **Versão da API:** não formalizada — a definir (NFR-M03) · **Protocolo:** HTTP síncrono sobre TLS 1.2+ (NFR-SEC01) · **Base URL:** não descobrível a partir dos fontes — a definir pela engenharia
> **Autenticação:** token de acesso emitido no login (`createUserAndToken`) e validado a cada requisição (`doFilterInternal` — NFR-SEC02)
> **Owner:** Engenharia/Arquitetura · **Status:** Draft — catálogo de endpoints pendente de instrumentação

> ⚠️ **Aviso central — rotas não localizadas automaticamente.** As rotas da API **não puderam ser localizadas automaticamente**: o arquivo de rotas referenciado pelo processo de extração (`server/express-app.ts`) não foi encontrado no workspace, e a varredura determinística do codebase reportou **"Nenhuma rota/IPC detectada no código"**. **Nenhum método HTTP, caminho ou schema de payload foi confirmado.** Em conformidade com isso, este documento **não inventa endpoints**: ele registra (i) as convenções de contrato, (ii) as capacidades de API evidenciadas por comportamento no código e nos NFRs (SAD §3/§6), e (iii) o plano de completude do catálogo (Seção 8).

---

## 1. Overview

**Propósito.** Expor as operações do sistema interno corporativo de gestão patrimonial multi-filial — **fonte única de verdade do patrimônio** (BRD §1) — ao frontend da aplicação. A API é a única fronteira de comunicação externa do backend monolítico Java (`src/`, 338 arquivos `.java`, pacotes `br.com.aegispatrimonio.*`): toda interação é **síncrona via HTTP com TLS 1.2+** (NFR-SEC01), com correlation ID propagado (NFR-O02). Não há filas, eventos assíncronos, cache identificável nem integrações externas no codebase (SAD §2/§4/§6).

**Consumidores esperados.** **Consumidor único verificado:** o frontend JavaScript (`frontend/`, 15 arquivos `.js`), desktop-first (1280–1920px — NFR-U03), construído sobre `@popperjs/core ^2.11.8` (única dependência de produção declarada em `package.json`; nenhuma devDependency). O acesso à API é centralizado na camada de serviços `api.js`: função `request` (complexidade ciclomática 13 — ponto de atenção, NFR-M02), `authInterceptor` (injeção do token) e `handleApiError` (tratamento uniforme de erros — NFR-U02). **Nenhuma integração externa** (pagamento, e-mail, ERPs etc.) e **nenhum consumidor machine-to-machine** foram detectados no código (SAD §6). O sistema é de **instância única**, sem empacotamento desktop (não há `src-tauri/Cargo.toml`).

**Princípios de design** (ancorados no SAD e nos NFRs):

1. Comunicação **síncrona HTTP** — nenhum mecanismo de mensageria/eventos assíncronos existe no codebase nem é requisito nesta fase (SAD §4);
2. **TLS 1.2+ (preferencialmente 1.3) em 100% do tráfego** frontend↔backend (NFR-SEC01);
3. **Autenticação por token validada a cada requisição** (NFR-SEC02); **logout invalida imediatamente o token** (BR-07 / NFR-SEC04);
4. **RBAC com contexto** (perfil + filial/departamento) cobrindo **100% das operações de escrita** (BR-01, BR-04) e a leitura do histórico de saúde por filial (BR-03);
5. **Correlation ID propagado** entre frontend e backend, presente em **100% dos logs de requisições que falharem** (NFR-O02; guardrail BRD §4);
6. **Tratamento uniforme de erros** sem exposição de detalhes internos (NFR-U02);
7. No backend, **100% das consultas SQL parametrizadas** — SQL cru, sem ORM/query builder; concatenação de strings em SQL é proibida (NFR-SEC03).

**Estilo de exposição.** A classificação formal do estilo HTTP (registro em ADR) **não existe** — SAD §12. Os comportamentos evidenciados (CRUD de ativos via DTO, filtros combinados de consulta, emissão/validação de token) definem o perfil da API, mas a confirmação do padrão de exposição integra o levantamento técnico pendente (C-01 do BRD).

**Estado do catálogo (gap NFR-M03).** O inventário de endpoints é um **gap de documentação confirmado** (NFR-M03 — não instrumentada no repositório; SAD §6). Este documento estabelece as convenções e os elementos de contrato conhecidos; o catálogo completo deve ser gerado na primeira onda de instrumentação de rotas, em conjunto com este documento (Seção 8). Ferramenta/formato da documentação de API: **a definir** — nada foi presumido neste documento.

### 1.1 Fronteiras de integração

| Integração | Tipo | Direção | Contrato | Criticidade |
| :--- | :--- | :--- | :--- | :--- |
| Frontend ↔ Backend | HTTP síncrono (TLS 1.2+ — NFR-SEC01) | Frontend → Backend, via `request`/`authInterceptor` | Este documento — catálogo de endpoints **pendente de instrumentação** (NFR-M03) | Alta |
| Backend ↔ Persistência | SQL cru, 100% parametrizado (NFR-SEC03) | Backend → Banco | Contrato físico em [[db-schema-spec]] — **nenhuma tabela detectada na varredura**; motor não especificado (C-01) | Alta |
| Integrações externas (pagamento, e-mail, ERPs etc.) | — | — | **Nenhuma integração externa detectada no código** | — |

> **Nota:** o mecanismo real de exposição do backend e de servir o frontend **não é descobrível a partir dos fontes** (SAD §11) — a Base URL e o modo de publicação devem ser confirmados pela engenharia.

---

## 2. Conventions

* **Formato:** JSON (`application/json`) — premissa de trabalho deste contrato [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]: o formato real dos payloads não está explicitado nos fontes verificados e deve ser confirmado na instrumentação das rotas (NFR-M03).
* **Versionamento:** **nenhum mecanismo identificado** (nem path `/v1/`, nem header de versão) — a definir na formalização do catálogo (NFR-M03). Trata-se de API interna, de consumidor único e instância única; a decisão de versionamento deve ser registrada quando da primeira versão formalizada.
* **Convenção de nomes:** **não descobrível para os campos de payload** — a confirmar na instrumentação. Observação verificada: os identificadores evidenciados no código (`createUserAndToken`, `getHealthHistory`, `checkResourceUsageAlerts`, `doFilterInternal`) seguem o padrão camelCase; nada indica a convenção dos corpos de requisição/resposta.
* **Correlation ID:** propagado entre frontend (`request`) e backend e presente em **100% dos logs de requisições que falharem** (NFR-O02; guardrail BRD §4). O nome do header/campo de propagação **não está definido** — a definir na instrumentação.
* **Paginação:** **nenhum padrão de paginação identificado** no código ou nos NFRs. Ponto de atenção: o ranking de ativos carrega até 1000 candidatos em memória (`AtivoService.java:119` — meta p95 < 1,5s, NFR-P04); a evolução prevista (SAD §13) é migrar o ranking para a camada de consulta **com paginação** caso a meta não seja atendida — a paginação pode entrar no contrato nessa refatoração.
* **Rate Limiting:** **nenhum mecanismo identificado** nos fontes ou NFRs — não é requisito nesta fase. As metas de carga conhecidas são capacidade, não limitação por cliente: **150 usuários concorrentes** com degradação < 10% (NFR-S01) e **50 req/s sustentados em leitura** (NFR-S03). Headers `X-RateLimit-*`: não aplicáveis hoje.
* **Idempotência:** **nenhum mecanismo identificado** — a definir na instrumentação, em especial para as operações de escrita (CRUD de ativos, fluxo de manutenção com aprovação).
* **Webhooks/eventos:** **não aplicável** — comunicação estritamente síncrona; nenhum mecanismo de mensageria/eventos existe no codebase nem é requisito nesta fase (SAD §4).

---

## 3. Authentication & Authorization

**Mecanismo.** Autenticação por **token de acesso**, com estado no servidor:

* **Emissão no login** — `createUserAndToken`;
* **Validação em cada requisição** — filtro do backend (`doFilterInternal`), com injeção do token no frontend via `authInterceptor` (NFR-SEC02);
* **Logout invalida imediatamente o token** (BR-07 / NFR-SEC04) — implica estado de sessão/token no servidor; consequência arquitetural: o escalonamento horizontal foi conscientemente adiado até que o armazenamento compartilhado de tokens seja resolvido (SAD §7/§13);
* **Encerramento de sessão** (`clearSession`) exige nova autenticação;
* **TLS 1.2+ (preferencialmente 1.3) obrigatório** em 100% do tráfego (NFR-SEC01);
* **MFA:** não evidenciado no código — evolução futura, não requisito desta fase (NFR-SEC02).

**Elementos de contrato pendentes** (não descobríveis a partir dos fontes — extrair na instrumentação, sem inventar): endpoint de login (método/caminho), endpoint de logout, nome e esquema do header de autorização, formato/opacidade e TTL do token, mecanismo de renovação (se existir).

**Escopos/Permissões.** RBAC **com contexto** — perfil + filial/departamento — verificado via `hasPermission` (NFR-SEC02):

* cobertura obrigatória de **100% das operações de escrita** (BR-01, BR-04);
* leitura do **histórico de saúde restrita por filial** (BR-03);
* nomenclatura dos perfis e das strings de permissão: **não descobrível a partir dos fontes** — a extrair do código; este documento não inventa catálogo de escopos.

**Boundary de rede** (premissa carregada do SAD §9): [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] sistema interno corporativo, acessível apenas pela rede corporativa por usuários autenticados; sem exposição pública de endpoints administrativos. Segmentação de rede e firewall: a documentar pela infraestrutura antes do go-live.

---

## 4. Catálogo de Endpoints — status: **não extraído**

> **Por que não há contratos de endpoint abaixo:** a varredura determinística não extraiu inventário de rotas/handlers ("Nenhuma rota/IPC detectada no código") e `server/express-app.ts` não foi encontrado no workspace. Os sub-capítulos registram as **capacidades de API evidenciadas por comportamento no código e nos NFRs** (SAD §3/§6) — **sem inventar caminhos, métodos ou payloads**. O contrato HTTP completo de cada capacidade deve ser extraído na instrumentação (Seção 8).

| # | Capacidade | Evidência no código/NFR | Contrato HTTP |
| :--- | :--- | :--- | :--- |
| 4.1 | Autenticação (emissão, validação, invalidação de token) | `createUserAndToken`, `doFilterInternal`, `clearSession` | Não extraído |
| 4.2 | Ativos — CRUD, busca e ranking | `AtivoService`, `AtivoMapper` | Não extraído |
| 4.3 | Manutenção — fluxo com aprovação e filtros combinados | `ManutencaoSpecification` | Não extraído |
| 4.4 | Histórico de saúde por filial | `getHealthHistory` | Não extraído |
| 4.5 | Alertas operacionais | `AlertNotificationService` | Não extraído (exposição HTTP a confirmar) |
| 4.6 | Trilha de auditoria | `onUpdate`/`preUpdate` (BR-08) | Não extraído (meio de consulta a confirmar) |
| 4.7 | Health check da aplicação | SAD §10 (arquitetura-alvo) | Não confirmado no código |

### 4.1 Autenticação — emissão, validação e invalidação de token
* **Capacidade evidenciada:** login com emissão de token (`createUserAndToken`); validação do token a cada requisição (`doFilterInternal`); logout com invalidação imediata (BR-07 / NFR-SEC04); encerramento de sessão (`clearSession`).
* **Contrato HTTP:** **não extraído** — método, caminho, payload de credenciais e formato de resposta não descobríveis.
* **Restrições conhecidas:** TLS 1.2+ obrigatório (NFR-SEC01); token validado a cada requisição (NFR-SEC02); invalidação imediata no logout (NFR-SEC04); RBAC com contexto aplica-se a todas as operações subsequentes.
* **Elementos a extrair:** endpoint de login, endpoint de logout, header de autorização, TTL/renovação do token, formato das credenciais, códigos de erro de autenticação.

### 4.2 Ativos — CRUD, busca e ranking
* **Capacidade evidenciada:** operações CRUD de ativos, busca e ranking (`AtivoService`, `AtivoMapper`).
* **Contrato HTTP:** **não extraído**.
* **Restrições conhecidas:**
  * RBAC com contexto cobre as operações de escrita (BR-01, BR-04);
  * latência: metas p95/p99 para CRUD e listagens (NFR-P01); ranking em memória de até 1000 candidatos (`AtivoService.java:119`) com meta p95 < 1,5s (NFR-P04) — débito técnico trackado com TODO; se a meta não for atendida, migrar o ranking para a camada de consulta com paginação (SAD §13);
  * mapeamento DTO via `AtivoMapper.toDTO` (complexidade ciclomática 14 — refatoração obrigatória antes de novas evoluções, NFR-M02);
  * auditoria de modificações via `onUpdate`/`preUpdate` (BR-08).

### 4.3 Manutenção — fluxo com aprovação e filtros combinados
* **Capacidade evidenciada:** fluxo de manutenção com aprovação (BRD §2/§4); consultas com filtros combinados (`ManutencaoSpecification.build`).
* **Contrato HTTP:** **não extraído**.
* **Restrições conhecidas:** latência p95 < 1s / p99 < 2s para consultas com filtros (NFR-P02); sem ORM/query builder, índices e plano de consulta são responsabilidade direta do código — definição de índices a revisar em conjunto com a refatoração de `ManutencaoSpecification.build` (complexidade 14) (SAD §7); RBAC com contexto nas operações de escrita, incluindo a aprovação (BR-01, BR-04).

### 4.4 Histórico de saúde por filial
* **Capacidade evidenciada:** `getHealthHistory` — leitura restrita por filial (BR-03).
* **Contrato HTTP:** **não extraído**.
* **Restrições conhecidas:** autorização por filial obrigatória (BR-03); backup diário deve incluir obrigatoriamente o histórico de saúde (NFR-A03); política de retenção compatível com a análise preditiva de falhas futura (BRD §5; NFR-C02).

### 4.5 Alertas operacionais
* **Capacidade evidenciada:** `AlertNotificationService` / `checkResourceUsageAlerts` (complexidade 17) — verificação de uso de recursos e alertas operacionais, incluindo saturação de recursos (NFR-O03).
* **Contrato HTTP:** **não extraído** — não há evidência de exposição HTTP destes alertas; podem ser mecanismos internos ao backend. A confirmar na instrumentação.

### 4.6 Trilha de auditoria
* **Capacidade evidenciada:** callbacks `onUpdate`/`preUpdate` (BR-08) — mecanismo **já implementado no código**; consultável por registro com data/hora da última modificação (NFR-O04).
* **Contrato HTTP:** **não extraído** — o meio de consulta (endpoint dedicado ou outro mecanismo) não é descobrível. A confirmar na instrumentação.

### 4.7 Health check da aplicação
* **Status:** previsto na **arquitetura-alvo** de observabilidade (SAD §10) — **não confirmado no código**. A implementar/confirmar na instrumentação de rotas.

### 4.8 Modelo de contrato por endpoint (a aplicar na instrumentação)
Para cada endpoint extraído, documentar: método HTTP e caminho; parâmetros de path/query; headers (autorização, correlation ID); schema de request/response; códigos de erro; idempotência; autorização requerida (perfil + contexto filial/departamento); metas de latência aplicáveis (NFR-P01/P02/P04); eventos de auditoria relacionados (BR-08).

---

## 5. Data Models

> **Estado:** **nenhuma tabela foi detectada no schema** pela varredura determinística (SAD §5). O contrato físico (tabelas, colunas, constraints, DDL) vive em [[db-schema-spec]] e ainda não foi extraído; o modelo conceitual vive em [[db-domain-model]]. **Nenhum schema de payload (DTO) foi extraído** — as entidades abaixo são evidenciadas por nomes de classes/funções e **não constituem DDL nem contrato de payload confirmado**.

| Entidade | Evidência no código | Observações de contrato |
| :--- | :--- | :--- |
| Ativo | `AtivoService`, `AtivoMapper` | CRUD, busca e ranking; DTO via `AtivoMapper.toDTO` (schema não extraído) |
| Usuario | `Usuario.java` | atenção ao stub `setUsername` (corpo vazio, linha 86 — NFR-M05 / C-04 do BRD) |
| Funcionario | `createFuncionarioAndUsuario` | vínculo funcionário↔credenciais — relevante para LGPD (NFR-C01) |
| Manutenção | `ManutencaoSpecification` | filtros combinados de consulta |
| Histórico de saúde por filial | `getHealthHistory` | leitura restrita por filial (BR-03) |
| Alertas | `AlertNotificationService` / `checkResourceUsageAlerts` | alertas operacionais |
| Trilha de auditoria | `onUpdate`/`preUpdate` (BR-08) | data/hora da última modificação (NFR-O04) |

**Relacionamentos indicados pelos requisitos** (não constitui DDL confirmado): ativo ↔ unidade organizacional (filial/departamento — contexto do RBAC, NFR-SEC02); funcionário ↔ usuário/credenciais (`createFuncionarioAndUsuario`); ativo ↔ manutenção e ativo ↔ histórico de saúde (fluxo de monitoramento — BRD §4/§5).

**Camada de acesso a dados (fronteira do contrato):** SQL cru, **100% parametrizado** — concatenação de strings em SQL é proibida (NFR-SEC03); **sem ORM/query builder**; motor de persistência **não especificado** (C-01 do BRD) — o modelo de concorrência/lock é desconhecido até a confirmação, o que condiciona as metas de escrita concorrente. Estratégia de migração de schema: ver [[db-migration-spec]] — nenhum mecanismo de migração identificado no repositório.

---

## 6. Error Handling Standard

* **Evidência verificada:** tratamento centralizado no frontend via `handleApiError` — cobre **100% dos erros de API apresentados ao usuário**, sem exposição de detalhes internos (NFR-U02).
* **Formato do corpo de erro:** **não descobrível a partir dos fontes** — nenhum handler de erro do backend foi extraído pela varredura. O envelope de erro, os códigos internos e a estrutura de detalhes **não estão definidos**; este documento **não inventa códigos de erro**. O padrão deve ser formalizado em conjunto com o catálogo de endpoints (NFR-M03).
* **Requisitos que o padrão deverá satisfazer** (dos NFRs/guardrails):
  1. **Não expor detalhes internos** ao usuário (NFR-U02);
  2. Viabilizar **correlation ID em 100% dos logs de requisições que falharem**, para investigação de incidentes de acesso indevido (NFR-O02; guardrail BRD §4);
  3. Expressar os desfechos do **RBAC com contexto** (requisição sem permissão — perfil ou filial/departamento) de forma distinguível de falhas de autenticação (NFR-SEC02);
  4. Permitir **classificação consistente de erros** para o alerta de **taxa de erro > 1%** previsto na arquitetura-alvo de observabilidade (NFR-O03).
* **Mapeamento para códigos HTTP:** a definir na instrumentação — nenhum mapeamento foi confirmado no código.

---

## 7. Changelog da API

Nenhuma versão formalizada da API foi identificada — não há changelog anterior. A primeira versão formalizada deve ser registrada nesta seção quando o catálogo de endpoints for extraído e o esquema de versionamento definido (NFR-M03).

| Versão | Data | Mudança | Breaking? |
| :--- | :--- | :--- | :--- |
| — | — | Nenhuma versão formalizada registrada; catálogo de endpoints pendente de instrumentação (NFR-M03) | — |

---

## 8. Plano de Completude do Catálogo (NFR-M03)

1. **Reextrair o inventário de rotas sobre os artefatos corretos** — a referência `server/express-app.ts` não corresponde à composição verificada do codebase (backend Java em `src/`, 338 arquivos `.java`); a varredura determinística sobre o codebase reportou "Nenhuma rota/IPC detectada no código".
2. **Definir ferramenta/formato da documentação de API** (NFR-M03 — a definir; nada presumido neste documento).
3. **Extrair os schemas de payload** (DTOs — ex.: `AtivoMapper.toDTO`) e o mapeamento de códigos de erro, preenchendo as seções 4–6 deste documento.
4. **Confirmar os elementos de autenticação e exposição** — header/esquema/TTL do token, base URL, mecanismo de servir o frontend (SAD §11).
5. **Nomear o pacote e criar scripts de build/verificação no `package.json`** (NFR-M06) — hoje sem nome e sem scripts, o que limita a verificação automatizada do frontend.
6. **Registrar a primeira versão formalizada no changelog** (Seção 7).
7. **Pré-condição transversal:** confirmação do motor de persistência (C-01 do BRD) e da versão do runtime Java (NFR-PO03) — não bloqueia a extração das rotas, mas bloqueia o fechamento do contrato físico ([[db-schema-spec]]), do backup (NFR-A03) e da criptografia em repouso (NFR-SEC01).