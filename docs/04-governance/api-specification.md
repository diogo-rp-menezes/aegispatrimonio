# API & Integration Contract Specification — Aegis1 Frontend

> **Versão da API:** v1 (externo, não versionado neste repositório) · **Protocolo:** REST/HTTPS · **Base URL:** `https://api.aegis1.example.com/v1` (configurável via variável de ambiente pública)  
> **Autenticação:** Bearer Token (JWT) — atualmente armazenado em `localStorage`; migração planejada para cookie `HttpOnly; Secure; SameSite=Strict` (NFR-SEC02)

---

## 1. Overview

Este documento descreve o **contrato de integração esperado** entre o frontend **Aegis1** (SPA vanilla JS servida estaticamente) e o **backend externo** (fora do escopo deste repositório).  

**Importante:** Nenhum arquivo de rotas ou definição de API (OpenAPI, Swagger, controllers, etc.) foi localizado neste codebase. O diagnóstico automatizado confirmou: *"Nenhum arquivo de rotas foi localizado. Não invente endpoints — escreva uma seção honesta indicando que as rotas não puderam ser localizadas automaticamente."*  

Portanto, **este documento não especifica endpoints concretos implementados**; ele registra o que o frontend **espera** do backend com base no System Architecture Document (SAD) e no código de integração existente (`frontend/src/services/api.js`). A formalização do contrato (OpenAPI 3.0 + testes de contrato Pact) é um requisito pendente (NFR-M04) e deve ser conduzida pelo time dono do backend.

**Consumidores esperados:**  
- Aegis1 Frontend (este repositório) — único consumidor conhecido no momento.  
- Futuramente: aplicativos móveis, integrações de parceiros, ferramentas internas de BI.

**Princípios de design seguidos pelo frontend:**  
- RESTful com JSON (`application/json`).  
- Versionamento via path (`/v1/`).  
- Autenticação stateless via JWT no header `Authorization: Bearer <token>`.  
- Propagação de `trace-id` (UUID v4) em todas as chamadas para tracing distribuído (NFR-O03).  
- Timeout configurável (padrão sugerido 10 s) via `AbortController`.  
- Retry automático com backoff exponencial (máx. 2 tentativas) para 5xx/timeout.  
- Refresh de token automático (máx. 1 retry) em 401; logout limpo em falha definitiva.

---

## 2. Conventions

| Aspecto | Convenção Adotada / Esperada |
| :--- | :--- |
| **Formato de payload** | JSON (`application/json`) |
| **Versionamento** | Path prefix `/v1/` (ex.: `/v1/ordens`) |
| **Nomenclatura de campos** | **camelCase** (JavaScript nativo) — o frontend serializa/deserializa diretamente |
| **Paginação** | Não documentada no frontend atual; espera-se cursor-based (`?cursor=&limit=`) ou offset (`?page=&size=`) — a definir com backend |
| **Rate Limiting** | Não implementado no frontend; espera-se headers `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After` no backend |
| **Idempotência** | Frontend não envia `Idempotency-Key` hoje; recomendado para mutações (POST/PUT/PATCH) — a coordenar com backend |
| **Datas/Horários** | ISO 8601 UTC (`YYYY-MM-DDTHH:mm:ss.sssZ`) |
| **Moeda/Valores monetários** | Inteiro em centavos (ex.: `15000` = R$ 150,00) ou `decimal` string — a confirmar com backend |
| **Erros** | Padronização esperada (ver Seção 6) |

---

## 3. Authentication & Authorization

| Item | Detalhe |
| :--- | :--- |
| **Mecanismo** | JWT Bearer Token no header `Authorization: Bearer <access_token>` |
| **Emissão** | Endpoint de login do backend (fora do escopo) — retorna `access_token` + `refresh_token` |
| **Armazenamento atual (frontend)** | `localStorage` (`access_token`, `refresh_token`, `expires_at`) — **risco XSS conhecido** |
| **Migração planejada (NFR-SEC02)** | Cookie `HttpOnly; Secure; SameSite=Strict` definido pelo backend no login/refresh; frontend deixa de manipular tokens diretamente |
| **Refresh automático** | Implementado em `api.js:authInterceptor` — ao receber 401, chama endpoint de refresh (máx. 1 retry), atualiza tokens e reenvia request original |
| **Logout** | Limpa `localStorage`/`sessionStorage` + chamda opcional a endpoint de revogação no backend |
| **Escopos/Permissões** | Não expostos no frontend atual; o backend deve validar autorização por role/permissão (ex.: `tecnico`, `aprovador`, `gestor`) |

> **Rastreabilidade:** `authInterceptor` e `handleApiError` em `frontend/src/services/api.js` <!-- source: SAD#3, Diagnóstico: api.js -->

---

## 4. Endpoints Esperados (Baseados no SAD e `api.js`)

> **AVISO:** Os endpoints abaixo **não foram extraídos de código de rotas do backend** (inexistente neste repositório). Eles são **inferidos** a partir dos fluxos de negócio descritos no SAD (Seção 1: "criar, listar, iniciar, aprovar, concluir, cancelar, consultar custo total por ativo") e das chamadas realizadas pelo frontend. **Requerem validação e formalização com o time do backend.**  
> **Marcador:** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

### 4.1 Autenticação

#### `POST /v1/auth/login`
* **Purpose:** Autenticar usuário e obter tokens de acesso/refresh.
* **Auth requerida:** Não.

**Request Body**
```typescript
interface LoginRequest {
  email: string;
  senha: string;
  // opcional: rememberMe?: boolean;
}
```

**Response 200 OK**
```typescript
interface LoginResponse {
  accessToken: string;      // JWT
  refreshToken: string;     // opaque token ou JWT
  expiresIn: number;        // segundos até expiração do accessToken
  tokenType: "Bearer";
  usuario: {
    id: string;
    nome: string;
    email: string;
    roles: string[];        // ex.: ["tecnico", "aprovador"]
  };
}
```

**Error Codes**
| HTTP | Código Interno | Descrição |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Credenciais malformadas |
| 401 | `INVALID_CREDENTIALS` | Email/senha incorretos |
| 429 | `RATE_LIMITED` | Tentativas excessivas |

---

#### `POST /v1/auth/refresh`
* **Purpose:** Renovar access token usando refresh token.
* **Auth requerida:** Sim — `Authorization: Bearer <refresh_token>` (ou body com refresh token, a definir).

**Request Body** (se não via header)
```typescript
interface RefreshRequest {
  refreshToken: string;
}
```

**Response 200 OK**
```typescript
interface RefreshResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: "Bearer";
}
```

**Error Codes**
| HTTP | Código Interno | Descrição | Ação Frontend |
| :--- | :--- | :--- | :--- |
| 401 | `REFRESH_EXPIRED` | Refresh token expirado/revogado | Logout forçado + redirect login |
| 401 | `REFRESH_INVALID` | Token malformado | Logout forçado |

---

#### `POST /v1/auth/logout` (opcional)
* **Purpose:** Revogar refresh token no backend (single-sign-out, compliance).
* **Auth requerida:** Sim — access token válido.

---

### 4.2 Ordens de Manutenção

> **Contexto:** Fluxo completo: **Criar → Listar → Iniciar → Aprovar (com evidências) → Concluir** ou **Cancelar**.  
> **Evidências no fluxo "Aprovar":** Checklist + Foto (NFR-C03).

#### `POST /v1/ordens`
* **Purpose:** Criar nova ordem de manutenção.
* **Auth requerida:** Sim — escopo `write:ordens` (presumido).

**Request Body**
```typescript
interface CreateOrdemRequest {
  ativoId: string;              // UUID do ativo/equipamento
  descricao: string;            // Descrição do problema/solicitação
  prioridade: "baixa" | "media" | "alta" | "critica";
  solicitanteId: string;        // UUID do funcionário solicitante
  departamentoId: string;       // UUID do departamento
  filialId: string;             // UUID da filial
  // Campos opcionais:
  fornecedorId?: string;        // UUID do fornecedor (se terceirizado)
  dataPrevistaInicio?: string;  // ISO 8601
  dataPrevistaFim?: string;     // ISO 8601
  observacoes?: string;
}
```

**Response 201 Created**
```typescript
interface OrdemResponse {
  id: string;                   // UUID
  numero: string;               // Número sequencial legível (ex.: "OS-2025-00123")
  status: "aberta" | "em_andamento" | "aprovada" | "concluida" | "cancelada";
  createdAt: string;            // ISO 8601
  updatedAt: string;
  // ... eco dos campos de entrada + campos calculados
}
```

**Error Codes**
| HTTP | Código Interno | Descrição |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Campos obrigatórios ausentes/inválidos |
| 403 | `FORBIDDEN` | Usuário sem permissão para criar ordens |
| 409 | `CONFLICT` | Ordem duplicada (regra de negócio) |

---

#### `GET /v1/ordens`
* **Purpose:** Listar ordens com filtros e paginação.
* **Auth requerida:** Sim — escopo `read:ordens`.

**Query Parameters** (esperados, não confirmados)
| Nome | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `status` | string | Não | Filtro por status (csv: `aberta,em_andamento`) |
| `ativoId` | string | Não | Filtrar por ativo |
| `tecnicoId` | string | Não | Filtrar por técnico responsável |
| `dataInicio` | string | Não | Data inicial (ISO 8601) |
| `dataFim` | string | Não | Data final (ISO 8601) |
| `cursor` | string | Não | Cursor para paginação |
| `limit` | integer | Não | Tamanho da página (default: 20, max: 100) |

**Response 200 OK**
```typescript
interface ListOrdensResponse {
  data: OrdemResponse[];
  pagination: {
    nextCursor?: string;
    hasMore: boolean;
    total?: number;   // se backend suportar contagem total
  };
}
```

---

#### `GET /v1/ordens/{id}`
* **Purpose:** Obter detalhes completos de uma ordem.
* **Auth requerida:** Sim — `read:ordens`.

**Path Parameters**
| Nome | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `id` | string (UUID) | Sim | Identificador da ordem |

**Response 200 OK** — `OrdemResponse` estendido com:
```typescript
interface OrdemDetalhada extends OrdemResponse {
  evidencias?: {
    checklist: ChecklistItem[];
    fotos: FotoEvidencia[];
  };
  historicoStatus: HistoricoStatus[];
  custoTotal?: number;        // em centavos
  tecnicoResponsavelId?: string;
}
```

---

#### `POST /v1/ordens/{id}/iniciar`
* **Purpose:** Iniciar execução da ordem (transição `aberta` → `em_andamento`).
* **Auth requerida:** Sim — `write:ordens`, role `tecnico`.

**Request Body**
```typescript
interface IniciarOrdemRequest {
  tecnicoId: string;          // UUID do técnico que assume
  observacaoInicial?: string;
}
```

**Response 200 OK** — `OrdemResponse` com `status: "em_andamento"`.

**Error Codes**
| HTTP | Código Interno | Descrição |
| :--- | :--- | :--- |
| 409 | `INVALID_TRANSITION` | Ordem não está em status `aberta` |
| 403 | `FORBIDDEN` | Técnico não autorizado para esta ordem |

---

#### `POST /v1/ordens/{id}/aprovar`
* **Purpose:** Aprovar ordem com evidências (checklist + foto) — transição `em_andamento` → `aprovada`.
* **Auth requerida:** Sim — `write:ordens`, role `aprovador`.

**Request Body**
```typescript
interface AprovarOrdemRequest {
  aprovadorId: string;        // UUID do aprovador
  checklist: ChecklistItem[]; // itens verificados
  fotos: FotoEvidencia[];     // array de URLs ou base64 (a definir)
  observacaoAprovacao?: string;
}

interface ChecklistItem {
  itemId: string;             // ID do item de checklist padrão
  descricao: string;
  conformidade: "conforme" | "nao_conforme" | "na";
  observacao?: string;
}

interface FotoEvidencia {
  url: string;                // URL pré-assinada ou endpoint de upload
  legenda?: string;
  tipo: "antes" | "durante" | "depois";
}
```

**Response 200 OK** — `OrdemResponse` com `status: "aprovada"` e evidências anexadas.

**Error Codes**
| HTTP | Código Interno | Descrição |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Checklist incompleto ou fotos ausentes |
| 409 | `INVALID_TRANSITION` | Ordem não está em `em_andamento` |

---

#### `POST /v1/ordens/{id}/concluir`
* **Purpose:** Concluir ordem aprovada — transição `aprovada` → `concluida`.
* **Auth requerida:** Sim — `write:ordens`, role `tecnico` ou `gestor`.

**Request Body**
```typescript
interface ConcluirOrdemRequest {
  tecnicoId: string;
  relatorioFinal: string;     // Relatório de execução
  custoRealizado?: number;    // Centavos (opcional, se diferente do estimado)
  dataConclusao: string;      // ISO 8601
}
```

**Response 200 OK** — `OrdemResponse` com `status: "concluida"`.

---

#### `POST /v1/ordens/{id}/cancelar`
* **Purpose:** Cancelar ordem (qualquer status não final).
* **Auth requerida:** Sim — `write:ordens`, role `gestor`.

**Request Body**
```typescript
interface CancelarOrdemRequest {
  gestorId: string;
  motivo: string;
}
```

**Response 200 OK** — `OrdemResponse` com `status: "cancelada"`.

---

### 4.3 Custos por Ativo

#### `GET /v1/ativos/{ativoId}/custo-total`
* **Purpose:** Consultar custo total acumulado de manutenção por ativo.
* **Auth requerida:** Sim — `read:custos`.

**Path Parameters**
| Nome | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `ativoId` | string (UUID) | Sim | Identificador do ativo |

**Query Parameters**
| Nome | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `dataInicio` | string | Não | Período inicial (ISO 8601) |
| `dataFim` | string | Não | Período final (ISO 8601) |

**Response 200 OK**
```typescript
interface CustoTotalAtivoResponse {
  ativoId: string;
  periodo: { inicio: string; fim: string };
  custoTotal: number;         // Centavos
  quantidadeOrdens: number;
  breakdown?: {
    pecas: number;
    maoDeObra: number;
    terceiros: number;
  };
}
```

---

### 4.4 Cadastros Auxiliares (Referências para Formulários)

> O SAD menciona formulários de cadastro: **Departamento, Filial, Fornecedor, Funcionário**. Espera-se endpoints CRUD simples para popular selects/autocompletes.

#### `GET /v1/departamentos` | `GET /v1/filiais` | `GET /v1/fornecedores` | `GET /v1/funcionarios`
* **Purpose:** Listar entidades para preenchimento de formulários.
* **Auth requerida:** Sim — `read:cadastros`.
* **Response:** Array de `{ id: string, nome: string, ... }` — campos mínimos para UI.

---

## 5. Data Models (Compartilhados)

```typescript
// ===== Entidades Principais =====

interface OrdemBase {
  id: string;                 // UUID
  numero: string;             // Legível: "OS-2025-00123"
  ativoId: string;
  ativoNome?: string;         // Denormalizado para exibição
  descricao: string;
  prioridade: "baixa" | "media" | "alta" | "critica";
  status: "aberta" | "em_andamento" | "aprovada" | "concluida" | "cancelada";
  solicitanteId: string;
  solicitanteNome?: string;
  departamentoId: string;
  filialId: string;
  fornecedorId?: string;
  tecnicoResponsavelId?: string;
  dataPrevistaInicio?: string;
  dataPrevistaFim?: string;
  dataInicioReal?: string;
  dataFimReal?: string;
  custoEstimado?: number;     // Centavos
  custoRealizado?: number;    // Centavos
  observacoes?: string;
  createdAt: string;
  updatedAt: string;
}

interface HistoricoStatus {
  status: OrdemBase["status"];
  usuarioId: string;
  usuarioNome: string;
  timestamp: string;
  observacao?: string;
}

// ===== Evidências (NFR-C03) =====

interface ChecklistItem {
  itemId: string;
  descricao: string;
  conformidade: "conforme" | "nao_conforme" | "na";
  observacao?: string;
}

interface FotoEvidencia {
  url: string;
  legenda?: string;
  tipo: "antes" | "durante" | "depois";
  uploadedAt: string;
}

// ===== Usuário/Autenticação =====

interface Usuario {
  id: string;
  nome: string;
  email: string;
  roles: string[];            // "tecnico", "aprovador", "gestor", "admin"
  departamentoId?: string;
  filialId?: string;
  ativo: boolean;
}

// ===== Cadastros Auxiliares =====

interface EntidadeReferencia {
  id: string;
  nome: string;
  codigo?: string;            // Código legível (ex.: "DEP-01")
  ativo: boolean;
}

type Departamento = EntidadeReferencia;
type Filial = EntidadeReferencia;
type Fornecedor = EntidadeReferencia & { cnpj?: string; contato?: string };
type Funcionario = EntidadeReferencia & { email: string; cargo: string; departamentoId: string };
```

---

## 6. Error Handling Standard

O frontend espera que **todas as respostas de erro (4xx, 5xx)** sigam o envelope padronizado abaixo. O `api.js:handleApiError` normaliza para exibição amigável na UI.

```json
{
  "error": {
    "code": "STRING_CODE",           // Código legível por máquina (ex.: "VALIDATION_ERROR")
    "message": "Mensagem legível para humanos, adequada para exibição direta ao usuário",
    "details": [                     // Opcional: array de erros de campo (validação)
      {
        "field": "campo.nome",
        "code": "REQUIRED",
        "message": "O campo 'nome' é obrigatório"
      }
    ],
    "traceId": "uuid-v4"             // Obrigatório: correlaciona com logs do backend (NFR-O03)
  }
}
```

### Códigos de Erro Esperados

| HTTP | `error.code` | Cenário | Ação Frontend (`handleApiError`) |
| :--- | :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Payload inválido (campos obrigatórios, tipos, regras) | Exibir `details` inline nos campos do formulário |
| 401 | `UNAUTHENTICATED` | Access token ausente/expirado | Disparar `authInterceptor` → refresh → retry (1x) |
| 401 | `REFRESH_EXPIRED` | Refresh token inválido/expirado | Logout forçado + redirect `/login` com toast "Sessão expirada" |
| 403 | `FORBIDDEN` | Usuário autenticado sem permissão para a ação | Toast "Você não tem permissão para esta ação" |
| 404 | `NOT_FOUND` | Recurso não encontrado (ordem, ativo, usuário) | Toast "Registro não encontrado" + botão "Voltar à lista" |
| 409 | `CONFLICT` | Violação de regra de negócio (transição inválida, duplicidade) | Toast com `message` do backend (ex.: "Ordem já foi concluída") |
| 409 | `INVALID_TRANSITION` | Tentativa de mudar status fora do fluxo permitido | Toast explicativo + desabilitar botões inválidos na UI |
| 422 | `UNPROCESSABLE_ENTITY` | Semântica inválida (ex.: checklist incompleto ao aprovar) | Exibir `details` ou `message` em modal/alert |
| 429 | `RATE_LIMITED` | Limite de requisições excedido | Retry automático com backoff (respeitar `Retry-After`) |
| 500 | `INTERNAL_ERROR` | Erro inesperado no backend | Toast "Erro interno. Tente novamente em instantes" + log em telemetria |
| 503 | `SERVICE_UNAVAILABLE` | Backend indisponível (manutenção, overload) | Toast "Serviço temporariamente indisponível" + retry manual |

> **Nota:** O frontend **não travam a UI** em erros (NFR-A04) — `handleApiError` exibe toast/alert, mantém navegação funcional e oferece botão "Tentar novamente" quando aplicável.

---

## 7. Idempotency

- **Estado atual:** Frontend **não envia** header `Idempotency-Key`.
- **Recomendação (NFR):** Implementar para todas as mutações (`POST`, `PUT`, `PATCH`, `DELETE`) — gerar UUID v4 por ação do usuário (clique em botão) e enviar no header. Backend deve garantir processamento exactly-once.
- **Chaves sugeridas:** `Idempotency-Key: <uuid>` + `Idempotency-Key-Expires: <timestamp>` (opcional).

---

## 8. Webhooks / Eventos (Não Implementados no Frontend)

> O frontend **não consome webhooks** atualmente (SPA stateless, sem servidor próprio). Se o backend emitir eventos (ex.: `ordem.status_changed`, `ordem.custo_atualizado`), a integração futura exigiria:
- Backend-for-Frontend (BFF) ou WebSocket/Server-Sent Events para push em tempo real.
- Ou polling de curta duração (não recomendado para escala).

**Eventos de domínio esperados (para documentação futura):**
| Evento | Gatilho | Payload Mínimo |
| :--- | :--- | :--- |
| `ordem.criada` | `POST /ordens` 201 | `{ ordemId, numero, status: "aberta" }` |
| `ordem.iniciada` | `POST /ordens/{id}/iniciar` | `{ ordemId, status: "em_andamento", tecnicoId }` |
| `ordem.aprovada` | `POST /ordens/{id}/aprovar` | `{ ordemId, status: "aprovada", aprovadorId, evidencias }` |
| `ordem.concluida` | `POST /ordens/{id}/concluir` | `{ ordemId, status: "concluida", custoRealizado }` |
| `ordem.cancelada` | `POST /ordens/{id}/cancelar` | `{ ordemId, status: "cancelada", motivo }` |

---

## 9. Changelog da API (Este Documento)

| Versão | Data | Mudança | Breaking? |
| :--- | :--- | :--- | :--- |
| 0.1 (draft) | 2025-01-XX | Criação inicial baseada em SAD + `api.js` — **endpoints inferidos, não validados com backend** | — |

---

## 10. Próximos Passos / Ações Requeridas

| Ação | Responsável | Prazo / Critério | Referência |
| :--- | :--- | :--- | :--- |
| **Publicar especificação OpenAPI 3.0** do backend | Time Backend | Antes do go-live | NFR-M04 |
| **Implementar testes de contrato (Pact)** no CI do frontend | Time Frontend | Após OpenAPI disponível | NFR-M04 |
| **Validar endpoints, payloads e códigos de erro** com backend | Ambos os times | Sprint de integração | — |
| **Definir paginação padrão** (cursor vs offset) | Backend | Antes do go-live | — |
| **Implementar `Idempotency-Key`** no frontend para mutações | Frontend | Após validação de contrato | NFR (recomendado) |
| **Migração JWT → Cookie HttpOnly** (login/refresh/set-cookie) | Backend + Frontend | Antes do go-live | NFR-SEC02 |
| **Definir formato de upload de fotos** (multipart, pre-signed URL, base64) | Backend | Fluxo "Aprovar" | NFR-C03 |

---

> **Fim do documento.**  
> Este artefato reflete **apenas o que é conhecível a partir do repositório frontend (SAD + `api.js`)**. Qualquer endpoint, campo, regra ou comportamento não listado aqui **não deve ser assumido** — requer especificação formal pelo time do backend e validação conjunta.