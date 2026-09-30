# API & Integration Contract Specification — API Interna do Backend · Sistema de Gestão de Patrimônio (A4)

> **Versão da API:** v1 (proposta — nenhum mecanismo de versionamento verificado no código) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] · **Protocolo:** HTTP síncrono (verificado — SAD Seção 3); estilo REST presumido [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; sem IPC — o projeto não possui `src-tauri/Cargo.toml` (verificado na stack) · **Base URL:** **a definir** — nenhuma configuração de deploy, provedor, TLS ou domínio verificada no codebase (NFR-PO03)
> **Autenticação:** JWT — access token 1h, refresh token 7d, blocklist server-side no logout (NFR-SEC01); transporte via header `Authorization` presumido [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
> **Status:** Draft · **Owner:** Arquitetura/Engenharia · **Fontes:** [[nfr]] (Non-Functional Requirements v1.0), System Architecture Document (SAD v1.0), diagnóstico determinístico do codebase (varredura AST — 350 arquivos, 1.268 funções, 344 classes, 27.537 LOC), `package.json` do workspace

> ⚠️ **AVISO DE EXTRAÇÃO:** **as rotas reais não puderam ser localizadas automaticamente no workspace.** Nenhum arquivo de rotas foi encontrado e a varredura AST determinística **não detectou nenhum handler/rota declarado no código**. Consequentemente, **nenhum endpoint de negócio foi extraído do código**: este documento não propõe caminhos, métodos ou contratos por endpoint de negócio. As Seções 4.1–4.4 documentam honestamente essa lacuna, a superfície funcional referenciada pelas fontes e o único par de caminhos citado nelas (`/health`, `/metrics` — existência não confirmada).

## 1. Overview

**Propósito.** Formalizar o contrato de integração entre o frontend web (`frontend/`) e o backend Java (`src/`, pacote `br.com.aegispatrimonio`) do Sistema de Gestão de Patrimônio (A4), estabelecendo convenções de autenticação, autorização, paginação, tratamento de erros e observabilidade. Este documento materializa a pendência **NFR-M03** (documentação formal da API via OpenAPI/Swagger — ferramenta não verificada no codebase).

**Consumidores esperados:**

| Consumidor | Evidência | Status |
| :--- | :--- | :--- |
| Frontend web (`frontend/`) | Função `request` em `frontend/src/services/api.js:54` — ponto único de integração client-side (verificado) | Confirmado |
| Monitor externo de uptime/APM | `/health` e `/metrics` (NFR-O01) | **Existência não confirmada no codebase** (lacuna de verificação 5 do SAD) |
| Integrações externas (ERP, pagamento, e-mail etc.) | Nenhuma detectada no codebase | Não aplicável |

**Princípios de design:**

- Comunicação **síncrona via HTTP** (verificado — não há comunicação assíncrona, filas, eventos ou webhooks detectados no codebase); estilo **REST presumido** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
- **Paginação server-side obrigatória em toda listagem** (NFR-S04 — requisito verificado).
- **Erros via status HTTP padronizados**: 400/401/403/404 (NFR-SEC01, NFR-SEC04, NFR-SEC05 — requisitos verificados).
- **Propagação de request ID** do frontend para o backend nos fluxos críticos — autenticação, solicitação, aprovação (NFR-O02 — requisito verificado).
- **SQL 100% parametrizado** no backend (NFR-SEC06 — requisito crítico dado o acesso a dados via SQL cru sem ORM, verificado na stack).
- **Escopo de transporte:** API HTTP web — **não há superfície de IPC** a documentar: o projeto não possui `src-tauri/Cargo.toml` (verificado na stack), sem empacotamento desktop Tauri.

**Metas de performance aplicáveis aos endpoints** (do [[nfr]]):

| Meta | Valor | Requisito |
| :--- | :--- | :--- |
| Latência | p95 < 200ms; p99 < 500ms | NFR-P01 |
| TTFB | < 100ms em leituras simples | NFR-P02 |
| Relatórios de custo total por ativo | < 3s p95 para 10.000+ ativos | NFR-P04 |
| Capacidade | ≥ 100 req/s por instância (cenário misto 80/20) | NFR-S03 |
| Carga | 10.000+ ativos e 1.000+ usuários simultâneos com degradação < 10% sobre o baseline | NFR-S01 |

## 2. Conventions

* **Formato:** JSON (`application/json`) — a serialização efetiva não foi verificada no código (nenhum contrato de serialização localizado); JSON é a convenção adotada por este documento.
* **Versionamento:** via path `/v1/` — **proposto** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; nenhum mecanismo de versionamento (header `Accept-Version` ou path) verificado no código.
* **Convenção de nomes:** `camelCase` — **proposto** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]; premissa: alinhamento com a convenção de nomenclatura do backend Java (verificado no codebase) e dos DTOs (`AtivoMapper.toDTO`).
* **Paginação:** server-side e **obrigatória em toda listagem** (NFR-S04 — verificado). Estilo concreto (`?page=&size=` vs. cursor) **não especificado nas fontes** — proposta: `?page=&size=` com envelope paginado (Seção 5) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
* **Rate Limiting:** **nenhum requisito de rate limiting verificado** nos NFRs ou no código — nenhum limite por chave ou header (`X-RateLimit-*`) está definido, e este documento não propõe valores. A meta de capacidade verificada é **≥ 100 req/s por instância** em cenário misto 80/20 (NFR-S03). Caso rate limiting seja adotado no futuro, será decisão de arquitetura própria a formalizar em ADR.
* **Correlação (request ID):** propagação de request ID do frontend para o backend nos fluxos críticos (NFR-O02 — verificado). Header proposto: `X-Request-Id` [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
* **HTTPS:** obrigatório em produção com TLS 1.2+ (NFR-SEC03 — requisito verificado do BRD).
* **Auditoria:** toda operação de escrita é registrada em trilha de auditoria imutável (NFR-SEC07 — requisito verificado; tensão com a LGPD a resolver — SAD Seção 13).
* **Idempotência:** **nenhum mecanismo de idempotência verificado** no código ou nos requisitos — não garantido pelo contrato atual.

## 3. Authentication & Authorization

* **Mecanismo:** **JWT** (NFR-SEC01 — requisito verificado):
  * access token com expiração em **1 hora**; refresh token em **7 dias** (RF-24);
  * endpoints protegidos rejeitam requisições sem token com **401** (CA-06);
  * logout limpa a sessão local no frontend (RF-25 — `clearSession` em `frontend/src/services/api.js`) e **invalida o token server-side via blocklist** (CA-08);
  * transporte via header `Authorization: Bearer <token>` — **presumido** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
* **Hash de senhas:** o mecanismo efetivamente implementado no backend **não pôde ser verificado** na varredura de dependências — verificação obrigatória antes de aprovar o requisito (lacuna de verificação 2 do SAD/NFR). O BRD (RNF-02) especifica bcrypt/argon2 como requisito. (NFR-SEC02)
* **Escopos/Permissões:** modelo de autorização por **roles internas — `Admin` e `User`** (NFR-SEC04 — requisito verificado). **Não há escopos granulares** (tipo `read:orders` / `write:orders`):
  * toda operação de escrita em **entidades mestres** exige role `Admin`; tentativas de `User` retornam **403 Forbidden** (RF-26, RN-01/02, CA-01/CA-10);
  * cobertura de **100% dos endpoints de escrita** em testes de integração (NFR-SEC04);
  * o mapeamento role-por-endpoint não foi extraído do código e não é proposto neste documento (Seção 4.1).
* **MFA para acesso Admin:** requisito **Should** (NFR-SEC09) — evolução futura, não implementado no contrato atual.
* **Rotação de segredos:** chave de assinatura do JWT e credenciais de banco a cada 90 dias (NFR-SEC10) — item inferido no NFR, sem mecanismo verificado no código.
* **Achado de segurança funcional relevante:** o stub `Usuario.setUsername` com corpo vazio (`src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`) pode indicar bug funcional ou intencionalidade — **investigar com prioridade**, pois é potencialmente relevante para a autenticação (RF-23/NFR-SEC01) (SAD Seção 12, item 7).

## 4. Endpoints

### 4.1 Status da extração de rotas — rotas não localizadas automaticamente

> **Nenhum arquivo de rotas foi localizado no workspace** (o arquivo de rotas esperado pela rotina de extração — `server/express-app.ts` — não existe) e a **varredura AST determinística (350 arquivos, 1.268 funções, 344 classes, 27.537 LOC) não detectou nenhum handler/rota declarado no código**.

Consequências para este documento:

1. **Nenhum endpoint de negócio foi extraído do código** — não existe catálogo verificado de caminhos, métodos HTTP, payloads, schemas ou códigos de erro por endpoint.
2. Este documento **não propõe caminhos nem contratos por endpoint de negócio**, para não inventar contratos não verificáveis. A especificação detalhada por endpoint será produzida quando as rotas reais forem localizadas ou confirmadas pelo time (Seção 4.4).
3. A existência de uma superfície HTTP no backend é **de facto** — o frontend consome o backend via HTTP síncrono através do ponto de integração verificado (`frontend/src/services/api.js`, função `request`) — porém o contrato dessa superfície não foi extraído. A partir das fontes disponíveis **não é possível determinar** se as rotas existem e não foram cobertas pela varredura, ou se ainda não foram implementadas.
4. A superfície **funcional** que a API deve expor é derivável dos requisitos referenciados no SAD e está descrita na Seção 4.2 — em nível funcional, sem correspondência com rotas específicas.
5. Os únicos caminhos citados nas fontes são `/health` e `/metrics` (NFR-O01) — detalhados na Seção 4.3 com existência **não confirmada no codebase** (lacuna de verificação 5 do SAD).

### 4.2 Superfície funcional referenciada pelas fontes (não confirmada no código)

A tabela abaixo descreve, em nível funcional, as operações que os requisitos (referenciados no SAD) esperam da API. **Nenhum caminho, método HTTP ou payload foi extraído do código**; a correspondência com rotas específicas permanece em aberto.

| Área funcional | Requisitos de origem | Operações previstas (nível funcional) | Semântica contratual verificada |
| :--- | :--- | :--- | :--- |
| Autenticação | RF-23, RF-24, RF-25; NFR-SEC01 | Autenticar usuário; renovar access token a partir do refresh token (7d); logout com limpeza de sessão local (`clearSession`) e invalidação server-side do token via blocklist (CA-08) | Sem token em endpoint protegido → 401 (CA-06) |
| Ativos — consulta | RF-14, RF-15; NFR-S04 | Listagem de ativos com filtros e **paginação server-side obrigatória**; consulta de detalhe por identificador | ID inexistente → 404 padronizado (NFR-SEC05) |
| Ativos — escrita | RF-14, RF-26; NFR-SEC04, NFR-SEC07 | Criação/edição de ativos (entidades mestres) | Exige role Admin; tentativa de User → 403 (RF-26, RN-01/02, CA-01/CA-10); escrita auditada (NFR-SEC07) |
| Relatório de custo total por ativo | RF-17; NFR-P04 | Consulta analítica pesada (< 3s p95 para 10.000+ ativos) | Risco R-03 do BRD (timeout em consultas pesadas); estratégia de cache/materialização pendente (NFR-CO02) |
| Manutenção — solicitação e aprovação | RF-18, RF-19, RF-20, RF-21; UC-02, UC-03; NFR-O04 | Criação de solicitação de manutenção; listagem/filtragem (via `ManutencaoSpecification`); transições de status; aprovação | Timestamps nas transições para KPIs de processo: tempo médio de aprovação < 4h úteis e taxa de cancelamento < 10% (NFR-O04); fluxo de maior prioridade de disponibilidade (SAD Seção 8, RNF-08) |
| Observabilidade | NFR-O01, NFR-O03 | Health check (`/health`) e exposição de métricas (`/metrics`) | Únicos caminhos citados nas fontes; existência não confirmada (Seção 4.3) |

**Nota de filtragem dinâmica:** a construção dinâmica de consultas de manutenção (`ManutencaoSpecification.build`, `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26`, complexidade 14) é **superfície de risco prioritária** para SQL parametrizado e índices (NFR-SEC06, NFR-P01) — os parâmetros de filtro aceitos por esse mecanismo não foram extraídos do código e devem ser documentados quando as rotas forem confirmadas.

### 4.3 Endpoint referenciado nas fontes: `/health` e `/metrics`

Único par de caminhos citado nas fontes (NFR-O01). **Existência não confirmada no codebase** (SAD Seção 12, item 5 — lacuna de verificação).

#### `GET /health` e `GET /metrics` *(método GET presumido — não especificado nas fontes)* [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Purpose:** health check para monitor de uptime e exposição de métricas da instância (NFR-O01). Alertas previstos: p95 de latência acima de 200ms sustentado, taxa de erro > 1% e saturação de recursos da instância (NFR-O03) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].
* **Auth requerida:** não especificada nas fontes — proposta: `/health` público; `/metrics` a definir (público vs. restrito) [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].

#### Request
##### Path Parameters
Nenhum (os identificadores são fixos: `/health`, `/metrics`).

##### Query Parameters
Nenhum especificado nas fontes.

##### Headers
| Nome | Obrigatório | Descrição |
| :--- | :--- | :--- |
| `Authorization` | Depende da decisão de restrição de `/metrics` | `Bearer <token>` (NFR-SEC01) — se o endpoint for protegido |

#### Response

##### 200 OK — Schema
```typescript
// [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Nenhum schema de resposta foi extraído
// do código ou especificado nas fontes; proposta mínima a validar.
interface HealthResponse {
  status: string; // ex.: "ok" — valores e formato a definir
}
```

##### Exemplo de Response
```json
{ "status": "ok" }
```

##### Códigos de Erro
| Código HTTP | Código de Erro Interno (proposto) | Descrição | Ação Recomendada |
| :--- | :--- | :--- | :--- |
| 500 | `INTERNAL_ERROR` | Instância indisponível/degradada | Acionar runbook de disponibilidade (RTO ≤ 1h — NFR-A02) |

#### Idempotency
Não aplicável (leitura).

#### Webhooks/Eventos Relacionados
**Não aplicável** — não há comunicação assíncrona (filas/eventos/webhooks) detectada no codebase (SAD Seção 4); o processamento de alertas (`AlertNotificationService.checkResourceUsageAlerts`, linha 96) aparenta execução in-process.

### 4.4 Contratos detalhados por endpoint — pendência

Os contratos detalhados por endpoint de negócio (parâmetros de path/query, headers, body schema, exemplos de request/response, códigos de erro específicos, idempotência) **não puderam ser gerados a partir do código** e serão produzidos quando as rotas reais forem localizadas/confirmadas. Ao produzi-los, cada especificação deve cobrir:

- [ ] Mecanismo de autenticação e role exigida (Admin/User — NFR-SEC04), com teste de integração para 100% dos endpoints de escrita (NFR-SEC04);
- [ ] Paginação server-side obrigatória em listagens (NFR-S04) e o estilo escolhido;
- [ ] Códigos de erro 400/401/403/404 conforme o padrão da Seção 6 (NFR-SEC01, SEC04, SEC05);
- [ ] Propagação de request ID (NFR-O02) nos fluxos críticos (autenticação, solicitação, aprovação);
- [ ] Parâmetros de filtro aceitos pela construção dinâmica de consultas (`ManutencaoSpecification.build`) — com revisão de parametrização SQL (NFR-SEC06);
- [ ] Instrumentação de timestamps para KPIs de processo (NFR-O04) nos endpoints do fluxo de manutenção (RF-18 a RF-21);
- [ ] Definição de idempotência para operações de escrita (nenhum mecanismo verificado até o momento).

## 5. Data Models

**Status da modelagem (honesto):** a varredura determinística **não detectou nenhuma tabela no schema** — o contrato físico (tabelas, colunas, constraints, DDL) deve viver em [[db-schema-spec]] e o modelo conceitual em [[db-domain-model]] (ambos a produzir/validar). O acesso a dados é via **SQL cru, sem ORM** (verificado na stack — nenhum ORM/query builder nas dependências) e o **motor de banco não está especificado** (nenhum motor identificado nas dependências verificadas) — o que impede calibrar metas de escrita concorrente (NFR-S01), backup/RPO (NFR-A03) e comportamento de lock até a verificação do motor.

Entidades verificadas via nomes de classes no codebase: **Usuario** (autenticação/RBAC), **Ativo** (núcleo do domínio — `AtivoService`, `AtivoMapper`) e **Manutenção** (`ManutencaoSpecification`, fluxo RF-18–21). Os DTOs existem no backend (`AtivoMapper.toDTO` — complexidade 14), mas **seus campos não foram extraídos pela varredura**. Os modelos abaixo são **propostas mínimas**, derivadas de evidências pontuais do SAD, e requerem validação contra a implementação [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA].

```typescript
// [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Campos derivados de evidências mínimas:
// "id+nome" (AtivoService:119), stub setUsername (Usuario.java:86), transições de status
// (RF-19/RF-20) e timestamps de KPI (NFR-O04). Tipos primitivos propostos — os tipos
// reais não foram verificados.

interface UsuarioDTO {
  id: string;             // tipo real não verificado
  username: string;       // evidenciado pelo stub Usuario.setUsername (Usuario.java:86)
  role: "ADMIN" | "USER"; // RBAC Admin/User (NFR-SEC04) — valores literais presumidos
}

interface AtivoDTO {
  id: string;   // evidenciado por AtivoService:119 (candidatos "id+nome")
  nome: string; // idem
  // demais campos não verificados — AtivoMapper.toDTO (complexidade 14) indica
  // mapeamento condicional extenso a revisar (NFR-M02)
}

interface SolicitacaoManutencaoDTO {
  id: string;           // tipo real não verificado
  ativoId: string;      // relacionamento com Ativo presumido
  status: string;       // máquina de estados RF-19/RF-20 — valores não verificados
  criadoEm: string;     // timestamp exigido para KPIs de processo (NFR-O04)
  aprovadoEm?: string;  // tempo médio de aprovação < 4h úteis (NFR-O04)
  canceladoEm?: string; // taxa de solicitações canceladas < 10% (NFR-O04)
}

// Envelope de paginação server-side (NFR-S04) — formato proposto [INFERIDO]
interface PaginatedResponse<T> {
  data: T[];
  page: number;
  size: number;
  totalElements: number;
}
```

## 6. Error Handling Standard

**Requisitos verificados que ancoram o padrão:**

- Payloads inválidos → **400 Bad Request** com mensagens claras (NFR-SEC05);
- IDs inexistentes → **404 Not Found** padronizado (NFR-SEC05);
- Requisições sem token em endpoints protegidos → **401** (CA-06, NFR-SEC01);
- Operações de escrita sem role Admin → **403 Forbidden** (RF-26, NFR-SEC04).

**Envelope de erro proposto** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — o formato exato do corpo de erro não foi verificado no código; a proposta alinha-se ao requisito de mensagens claras (NFR-SEC05) e deve ser validada contra a implementação]:

```json
{
  "error": {
    "code": "STRING_CODE",
    "message": "Mensagem legível para humanos",
    "details": []
  }
}
```

**Mapa status HTTP → código interno (proposto):**

| Código HTTP | Código de Erro Interno (proposto) | Requisito de origem | Status do requisito |
| :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | NFR-SEC05 | Verificado |
| 401 | `UNAUTHENTICATED` | CA-06 / NFR-SEC01 | Verificado |
| 403 | `FORBIDDEN` | RF-26 / NFR-SEC04 | Verificado |
| 404 | `NOT_FOUND` | NFR-SEC05 | Verificado |
| 500 | `INTERNAL_ERROR` | — | Sem requisito específico verificado |

**Códigos 409 (Conflict) e 429 (Rate Limited):** **sem requisito verificado** nos NFRs ou no código — não são garantidos pelo contrato atual e não devem ser tratados como comportamento prometido até que exista requisito/implementação que os suporte.

**Dívida de observabilidade no client (achado real do diagnóstico):** chamadas residuais `console.error` (`frontend/src/services/api.js:44`) e `console.debug` (`frontend/src/services/api.js:107`) devem ser removidas ou substituídas por tratamento de erro estruturado antes de produção.

## 7. Contrato de Integração Client-Side (verificado)

O único ponto de integração client-side verificado no codebase é o serviço de API do frontend:

| Aspecto | Conteúdo verificado |
| :--- | :--- |
| Arquivo | `frontend/src/services/api.js` |
| Função central | `request` (linha 54, complexidade ciclomática 13) — centraliza todas as chamadas HTTP ao backend |
| Protocolo | HTTP síncrono (SAD Seção 3 — comunicação frontend↔backend síncrona) |
| Sessão | Logout limpa a sessão local (RF-25 — `clearSession`); o backend invalida o token via blocklist (CA-08) |
| Erros | Tratamento via status HTTP padronizados (401/403/400/404 — NFR-SEC01/SEC04/SEC05) |
| Dependências | `@popperjs/core` ^2.11.8 — única dependência de produção declarada no `package.json`; 0 dependências de desenvolvimento e nenhum script (sem toolchain de build/teste verificável) |

**Implicações contratuais:**

- Qualquer degradação na função `request` afeta **todos os fluxos** (gargalo 3 do SAD Seção 7) — mudanças no contrato de autenticação ou de erro devem ser coordenadas com este arquivo.
- A propagação de **request ID** (NFR-O02) deve ser implementada neste boundary para correlação nos fluxos críticos (autenticação, solicitação, aprovação).
- As chamadas residuais `console.*` (linhas 44 e 107) devem ser removidas ou substituídas por tratamento estruturado antes de produção (Seção 6).

## 8. Changelog da API

| Versão | Data | Mudança | Breaking? |
| :--- | :--- | :--- | :--- |
| 1.0 (draft) | — | Criação inicial da especificação do contrato de integração (materializa a pendência NFR-M03 — documentação formal via OpenAPI/Swagger, ferramenta não verificada). Nenhuma API publicada; **rotas reais não localizadas automaticamente** — nenhum endpoint de negócio foi extraído do código; contratos detalhados por endpoint pendentes (Seção 4.4). | — (não aplicável — nenhuma versão publicada) |