# API Specification (OpenAPI 3.1) — Aegis1

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Backend Lead / Arquitetura
> **Base:** System Architecture v2.0 + Use Cases v2.0 + Domain Model (AST Java) + State Machines v2.0
> **Protocolo:** REST/HTTPS · **Base Path:** `/api/v1` · **Formato:** JSON (`application/json`)
> **Autenticação:** JWT RS256 Bearer Token + Refresh Token Rotation (HttpOnly Cookie)
> **Multi-tenancy:** Header `X-Tenant-Id` (opcional, Admin Global) ou Claim `filialId` no JWT
> **Tracing:** Header `X-Trace-Id` (UUID v4) obrigatório em todas requisições
> **Versionamento:** Path `/api/v1/`; Breaking changes = `/api/v2/`
> **Documentação Interativa:** Swagger UI em `/swagger-ui.html` · OpenAPI JSON em `/api/v1/openapi.json`

---

## 1. Convenções Globais

| Aspecto | Convenção |
| :--- | :--- |
| **Nomenclatura campos** | `camelCase` (JSON) ↔ `snake_case` (Java/DB) — mapeamento automático Jackson |
| **Datas/Horários** | ISO 8601 UTC: `YYYY-MM-DDTHH:mm:ss.sssZ` (ex.: `2025-01-15T14:30:00.000Z`) |
| **Datas (apenas data)** | ISO 8601: `YYYY-MM-DD` |
| **Valores Monetários** | Inteiro em **centavos** (ex.: `15000` = R$ 150,00) — evita ponto flutuante |
| **Paginação** | Offset-based: `?page=0&size=20` (default size=20, max=100); Response: `Page<T>` (content, totalElements, totalPages, number, size) |
| **Ordenação** | `?sort=campo,asc|desc` (múltiplo: `sort=createdAt,desc&sort=prioridade,asc`) |
| **Filtros** | Query params tipados: `?filialId=1&status=ABERTA&prioridade=ALTA` |
| **Busca Fuzzy** | `?q=termo&types=ativo,ordem,funcionario,fornecedor` (Levenshtein + boost) |
| **Idempotência** | Header `Idempotency-Key` (UUID v4) obrigatório em `POST`/`PATCH`/`PUT`/`DELETE` mutações |
| **Rate Limiting Headers** | `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `Retry-After` |
| **Erros** | RFC 7807 (Problem Details): `type`, `title`, `status`, `detail`, `instance`, `traceId`, `errors[]` |
| **Códigos HTTP** | 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity, 429 Too Many Requests, 500 Internal Server Error, 503 Service Unavailable |

---

## 2. Autenticação & Autorização

### 2.1 Login
```http
POST /api/v1/auth/login
Content-Type: application/json
Idempotency-Key: <uuid>
X-Trace-Id: <uuid>

{
  "email": "tecnico@empresa.com",
  "senha": "senhaSegura123"
}
```

**Response 200 OK**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "usuario": {
    "id": 123,
    "nome": "João Silva",
    "email": "tecnico@empresa.com",
    "filialId": 1,
    "roles": ["TECNICO"],
    "permissions": ["ORDEM_INICIAR", "ORDEM_CONCLUIR", "HEALTH_CHECK_COLETAR"]
  }
}
```
**Cookies:** `refreshToken=<opaque>; HttpOnly; Secure; SameSite=Strict; Max-Age=604800; Path=/`

### 2.2 Refresh Token (Rotação Obrigatória)
```http
POST /api/v1/auth/refresh
Cookie: refreshToken=<opaque>
X-Trace-Id: <uuid>
```

**Response 200 OK**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```
**Cookies:** Novo `refreshToken` (rotação — anterior invalidado)

### 2.3 Logout / Revogação
```http
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
Cookie: refreshToken=<opaque>
X-Trace-Id: <uuid>
```
**Response 204 No Content** — Revoga refresh token, invalida sessão

### 2.4 Perfil do Usuário Logado
```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
X-Trace-Id: <uuid>
```

**Response 200 OK**
```json
{
  "id": 123,
  "nome": "João Silva",
  "email": "tecnico@empresa.com",
  "filialId": 1,
  "filialNome": "Filial São Paulo",
  "roles": ["TECNICO"],
  "permissions": ["ORDEM_INICIAR", "ORDEM_CONCLUIR", "HEALTH_CHECK_COLETAR"],
  "status": "ATIVO",
  "primeiroAcesso": false,
  "ultimoLogin": "2025-01-15T14:30:00.000Z"
}
```

---

## 3. Cadastros Mestres (Multi-tenancy Scoped)

### 3.1 Filiais (Admin Global)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/filiais` | Listar paginado + busca fuzzy | `FILIAL_LER` (global) |
| `GET` | `/api/v1/filiais/{id}` | Buscar por ID | `FILIAL_LER` (global) |
| `POST` | `/api/v1/filiais` | Criar | `FILIAL_CRIAR` (global) |
| `PUT` | `/api/v1/filiais/{id}` | Atualizar | `FILIAL_ATUALIZAR` (global) |
| `DELETE` | `/api/v1/filiais/{id}` | Excluir (bloqueia se vínculos) | `FILIAL_EXCLUIR` (global) |

**Filial Request/Response**
```json
{
  "id": 1,
  "razaoSocial": "Empresa Matriz Ltda",
  "cnpj": "12345678000199",
  "codigo": "MATRIZ",
  "endereco": "Av. Paulista, 1000",
  "telefone": "11999999999",
  "email": "matriz@empresa.com",
  "ativo": true,
  "createdAt": "2025-01-01T00:00:00.000Z",
  "updatedAt": "2025-01-15T10:00:00.000Z"
}
```

### 3.2 Departamentos (Scoped Filial)
```http
GET    /api/v1/filiais/{filialId}/departamentos
GET    /api/v1/filiais/{filialId}/departamentos/{id}
POST   /api/v1/filiais/{filialId}/departamentos
PUT    /api/v1/filiais/{filialId}/departamentos/{id}
DELETE /api/v1/filiais/{filialId}/departamentos/{id}
```
**Permissões:** `DEPARTAMENTO_*` (contexto filial)

### 3.3 Fornecedores (Scoped Filial)
```http
GET    /api/v1/filiais/{filialId}/fornecedores
GET    /api/v1/filiais/{filialId}/fornecedores/{id}
POST   /api/v1/filiais/{filialId}/fornecedores
PUT    /api/v1/filiais/{filialId}/fornecedores/{id}
DELETE /api/v1/filiais/{filialId}/fornecedores/{id}
```
**Campos Estendidos:** `categoria`, `slaPadraoHoras`, `avaliacao` (1-5), `certificacoes`

### 3.4 Funcionários + Provisionamento Usuario (Scoped Filial)
```http
GET    /api/v1/filiais/{filialId}/funcionarios
GET    /api/v1/filiais/{filialId}/funcionarios/{id}
POST   /api/v1/filiais/{filialId}/funcionarios
PUT    /api/v1/filiais/{filialId}/funcionarios/{id}
DELETE /api/v1/filiais/{filialId}/funcionarios/{id}
```
**Campos:** `nome`, `cpf`, `matricula`, `cargo`, `email`, `telefone`, `funcaoManutencao` (TECNICO/APROVADOR/SOLICITANTE), `usuario` (provisiona login)

### 3.5 Tipos de Ativo (Scoped Filial)
```http
GET    /api/v1/filiais/{filialId}/tipos-ativo
GET    /api/v1/filiais/{filialId}/tipos-ativo/{id}
POST   /api/v1/filiais/{filialId}/tipos-ativo
PUT    /api/v1/filiais/{filialId}/tipos-ativo/{id}
DELETE /api/v1/filiais/{filialId}/tipos-ativo/{id}
```
**Campos:** `codigo`, `nome`, `categoria` (HARDWARE/SOFTWARE/MOBILIARIO/VEICULO/OUTRO), `vidaUtilAnos`, `valorResidualPct`, `requerDetalheHardware`

---

## 4. Ativos & Hardware

### 4.1 Ativos (CRUD + Hardware + QR + PDF + Transferência + Baixa)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/ativos` | Listar paginado + filtros + busca fuzzy | `ATIVO_LER` (filial) |
| `GET` | `/api/v1/ativos/{id}` | Detalhe completo (hardware, depreciação, custoTotal, ordens, health check) | `ATIVO_LER` (filial) |
| `POST` | `/api/v1/ativos` | Criar ativo + hardware opcional + QR + Termo PDF | `ATIVO_CRIAR` (filial) |
| `PUT` | `/api/v1/ativos/{id}` | Atualizar dados cadastrais permitidos | `ATIVO_ATUALIZAR` (filial) |
| `PATCH` | `/api/v1/ativos/{id}/transferir` | Transferir filial/depto/local/responsável + novo Termo | `ATIVO_ATUALIZAR` (filial) |
| `PATCH` | `/api/v1/ativos/{id}/baixar` | Baixar/desativar ativo + TCO final | `ATIVO_EXCLUIR` (filial) |
| `DELETE` | `/api/v1/ativos/{id}` | Excluir (bloqueia se ordens vinculadas) | `ATIVO_EXCLUIR` (filial) |
| `GET` | `/api/v1/ativos/{id}/auditoria` | Timeline Envers (diff campo-a-campo) | `AUDITORIA_LER` (filial) |
| `GET` | `/api/v1/ativos/{id}/qr-code` | QR Code SVG/PNG (unitário) | `ATIVO_LER` (filial) |
| `GET` | `/api/v1/public/ativo/{tag}?h={hash}` | Página pública read-only (QR Code scan) | Pública (hash validation) |

### 4.2 Hardware Sub-recursos (Discos, Memória, Rede)
```http
GET    /api/v1/ativos/{ativoId}/hardware/discos
POST   /api/v1/ativos/{ativoId}/hardware/discos
PUT    /api/v1/ativos/{ativoId}/hardware/discos/{discoId}
DELETE /api/v1/ativos/{ativoId}/hardware/discos/{discoId}

GET    /api/v1/ativos/{ativoId}/hardware/memorias
POST   /api/v1/ativos/{ativoId}/hardware/memorias
PUT    /api/v1/ativos/{ativoId}/hardware/memorias/{memoriaId}
DELETE /api/v1/ativos/{ativoId}/hardware/memorias/{memoriaId}

GET    /api/v1/ativos/{ativoId}/hardware/adaptadores-rede
POST   /api/v1/ativos/{ativoId}/hardware/adaptadores-rede
PUT    /api/v1/ativos/{ativoId}/hardware/adaptadores-rede/{adaptadorId}
DELETE /api/v1/ativos/{ativoId}/hardware/adaptadores-rede/{adaptadorId}
```

### 4.3 Health Check (Coleta PWA + Análise)
```http
POST   /api/v1/ativos/{ativoId}/health-check
GET    /api/v1/ativos/{ativoId}/health-checks
```
**Request Body:**
```json
{
  "discoId": 456,
  "reallocatedSectors": 12,
  "seekErrorRate": 0,
  "spinRetryCount": 0,
  "temperaturaC": 42,
  "powerOnHours": 25000,
  "fonte": "MANUAL"
}
```
**Response 200 OK:**
```json
{
  "id": 789,
  "score": 75,
  "alerta": "ATENCAO",
  "thresholds": { "critico": 40, "atencao": 60 },
  "coletadoEm": "2025-01-15T14:30:00.000Z"
}
```

### 4.4 Custo Total por Ativo / TCO (Relatório)
```http
GET /api/v1/relatorios/custo-total-por-ativo?filialId=1&tipoAtivoId=2&page=0&size=20
```
**Response 200 OK:**
```json
{
  "content": [
    {
      "ativoId": 1,
      "tag": "NB-001",
      "modelo": "Dell Latitude 5520",
      "valorAquisicao": 850000,
      "valorResidual": 85000,
      "depreciacaoAcumulada": 340000,
      "valorContabilAtual": 510000,
      "custoTotalPorAtivo": 125000,
      "tco": 890000,
      "ultimaOrdem": "2025-01-10T10:00:00.000Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

---

## 5. Ordens de Manutenção (Corretiva, Preventiva, Preditiva)

### 5.1 CRUD + State Machine Transitions

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/ordens` | Listar paginado + filtros (estado, prioridade, filial, técnico, ativo fuzzy, período, tipo) | `ORDEM_LER` (filial) |
| `GET` | `/api/v1/ordens/{id}` | Detalhe completo (timeline, evidências, custos, auditoria) | `ORDEM_LER` (filial) |
| `POST` | `/api/v1/ordens` | Criar ordem (corretiva/preventiva/preditiva) | `ORDEM_CRIAR` (filial) |
| `PATCH` | `/api/v1/ordens/{id}/iniciar` | Iniciar execução (ABERTA → EM_ANDAMENTO) | `ORDEM_INICIAR` (filial) |
| `PATCH` | `/api/v1/ordens/{id}/submeter-aprovacao` | Submeter evidências (EM_ANDAMENTO → AGUARDANDO_APROVACAO) | `ORDEM_CONCLUIR` (própria) |
| `PATCH` | `/api/v1/ordens/{id}/aprovar` | Aprovar com evidências (AGUARDANDO_APROVACAO → APROVADA) | `ORDEM_APROVAR` (filial) |
| `PATCH` | `/api/v1/ordens/{id}/rejeitar` | Rejeitar (AGUARDANDO_APROVACAO → EM_ANDAMENTO) | `ORDEM_APROVAR` (filial) |
| `PATCH` | `/api/v1/ordens/{id}/concluir` | Concluir com custos finais (APROVADA → CONCLUIDA) | `ORDEM_CONCLUIR` (filial) |
| `PATCH` | `/api/v1/ordens/{id}/cancelar` | Cancelar (qualquer exceto CONCLUIDA → CANCELADA) | `ORDEM_CANCELAR` (filial/global) |
| `PATCH` | `/api/v1/ordens/{id}/reatribuir` | Reatribuir técnico responsável | `ORDEM_ATUALIZAR` (filial) |
| `GET` | `/api/v1/ordens/{id}/auditoria` | Timeline Envers | `AUDITORIA_LER` (filial) |

### 5.2 Criar Ordem (Request)
```json
{
  "ativoId": 1,
  "tipo": "CORRETIVA",
  "descricao": "Notebook não liga, suspeita de placa-mãe",
  "prioridade": "ALTA",
  "tecnicoResponsavelId": 5,
  "fornecedorId": null,
  "custoEstimado": 50000,
  "observacoes": "Cliente relatou cheiro de queimado"
}
```

### 5.3 Concluir Ordem (Request)
```json
{
  "custoMaoDeObra": 15000,
  "custoMaterial": 35000,
  "custoTerceiros": 0,
  "materiais": [
    { "descricao": "Fonte ATX 500W", "quantidade": 1, "valorUnitario": 35000 }
  ],
  "terceiros": [],
  "observacoes": "Fonte substituída, teste OK"
}
```

### 5.4 Dashboard Ordens (KPIs + Tempo Real)
```http
GET /api/v1/dashboard/ordens?filialId=1
```
**Response 200 OK:**
```json
{
  "kpis": {
    "abertas": 12,
    "emAndamento": 8,
    "aguardandoAprovacao": 3,
    "vencendoSla24h": 2,
    "concluidasMes": 45,
    "custoTotalMes": 1250000
  },
  "tendencia30d": [
    { "data": "2025-01-01", "criadas": 5, "concluidas": 4 },
    { "data": "2025-01-02", "criadas": 3, "concluidas": 5 }
  ],
  "porPrioridade": { "CRITICA": 2, "ALTA": 8, "MEDIA": 15, "BAIXA": 5 },
  "porFilial": { "1": 20, "2": 10 },
  "porTipoAtivo": { "NOTEBOOK": 12, "SERVIDOR": 8 }
}
```
**WebSocket:** `/ws/dashboard/ordens` — Events: `ordem.criada`, `ordem.iniciada`, `ordem.aprovada`, `ordem.concluida`, `ordem.cancelada`, `sla.breach`

---

## 6. Manutenção Preventiva

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/preventivas` | Listar planos paginado | `PREVENTIVA_LER` (filial) |
| `GET` | `/api/v1/preventivas/{id}` | Detalhe plano + próximas execuções | `PREVENTIVA_LER` (filial) |
| `POST` | `/api/v1/preventivas` | Criar plano (CRON, técnico padrão, ativos/tipo) | `PREVENTIVA_GERENCIAR` (filial) |
| `PUT` | `/api/v1/preventivas/{id}` | Atualizar plano | `PREVENTIVA_GERENCIAR` (filial) |
| `PATCH` | `/api/v1/preventivas/{id}/pausar` | Pausar geração | `PREVENTIVA_GERENCIAR` (filial) |
| `PATCH` | `/api/v1/preventivas/{id}/reativar` | Reativar + recalcula próxima | `PREVENTIVA_GERENCIAR` (filial) |
| `DELETE` | `/api/v1/preventivas/{id}` | Cancelar (bloqueia se ordens geradas) | `PREVENTIVA_GERENCIAR` (filial) |
| `GET` | `/api/v1/relatorios/preventiva-aderencia` | % concluídas no prazo / total geradas | `RELATORIO_GERAR` (filial) |

### 6.1 Criar Plano Preventivo (Request)
```json
{
  "filialId": 1,
  "ativoIds": [1, 2, 3],
  "tipoAtivoId": null,
  "cronExpression": "0 8 1 * ?",  // Todo dia 1 às 08:00
  "diaHoraPreferencial": "08:00",
  "tecnicoPadraoId": 5,
  "descricaoPadrao": "Manutenção preventiva mensal - limpeza, verificação térmica, teste bateria",
  "dataFim": "2025-12-31"
}
```

---

## 7. Manutenção Preditiva (Health Check + Previsão Falha)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/ativos/{ativoId}/health-check` | Coletar métricas SMART + análise score | `HEALTH_CHECK_COLETAR` (filial) |
| `GET` | `/api/v1/ativos/{ativoId}/health-checks` | Histórico health checks do ativo | `PREDITIVA_LER` (filial) |
| `GET` | `/api/v1/dashboard/preditiva` | Dashboard preditivo (riscos, alertas, previsões) | `PREDITIVA_LER` (filial) |
| `GET` | `/api/v1/previsoes-falha` | Listar previsões paginado + filtros | `PREDITIVA_LER` (filial) |
| `PATCH` | `/api/v1/previsoes-falha/{id}/tratar` | "Agendar Substituição" → cria preventiva one-shot | `PREVENTIVA_GERENCIAR` (filial) |

### 7.1 Dashboard Preditivo (Response)
```json
{
  "cards": {
    "ativosMonitorados": 850,
    "alertasAtivos": 12,
    "ordensPreditivasAbertas": 5,
    "previsoesMenos30Dias": 8
  },
  "previsoes": [
    {
      "id": 1,
      "ativoId": 1,
      "tag": "SRV-001",
      "discoId": 10,
      "discoSerial": "WD123456",
      "healthScore": 35,
      "probabilidade": 0.92,
      "dataPrevista": "2025-02-15",
      "icInferior": "2025-02-10",
      "icSuperior": "2025-02-20",
      "modeloUsado": "LINEAR_OLS",
      "rQuadrado": 0.87,
      "status": "ATIVA",
      "acao": "AGENDAR_SUBSTITUICAO"
    }
  ]
}
```

**WebSocket:** `/ws/dashboard/preditiva` — Events: `health.check.critico`, `preditiva.alerta`, `preditiva.tratada`

---

## 8. Busca Inteligente (Fuzzy Search)

```http
GET /api/v1/busca?q=notebok&types=ativo,ordem,funcionario,fornecedor&filialId=1&page=0&size=10
```

**Response 200 OK:**
```json
{
  "content": [
    {
      "type": "ATIVO",
      "id": 1,
      "tag": "NB-001",
      "label": "Notebook Dell Latitude 5520",
      "subLabel": "Filial SP / TI / Sala 101",
      "score": 0.94,
      "highlight": "Notebook Dell Latitude 5520",
      "actions": [
        { "label": "Ver Detalhe", "href": "/ativos/1" },
        { "label": "Criar Ordem", "href": "/ordens/novo?ativoId=1" }
      ]
    },
    {
      "type": "FUNCIONARIO",
      "id": 5,
      "label": "João Silva",
      "subLabel": "Técnico - Filial SP",
      "score": 0.72,
      "highlight": "João Silva"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### 8.2 Configuração Fuzzy (Admin)
```http
GET    /api/v1/admin/busca/config
PUT    /api/v1/admin/busca/config
```
**Config Request/Response:**
```json
{
  "thresholdSimilaridade": 0.7,
  "maxDistance": 2,
  "camposIndexados": {
    "ativo": ["tag", "serial", "modelo", "fabricante"],
    "ordem": ["numero", "descricao"],
    "funcionario": ["nome", "matricula"],
    "fornecedor": ["razaoSocial", "cnpj"]
  },
  "pesosBoost": {
    "ativo": 1.0,
    "ordem": 0.9,
    "funcionario": 0.8,
    "fornecedor": 0.7
  }
}
```

---

## 9. Segurança Aegis Shield (Admin)

### 9.1 Roles & Permissions Matrix
```http
GET    /api/v1/admin/roles
GET    /api/v1/admin/roles/{roleId}/permissions
PUT    /api/v1/admin/roles/{roleId}/permissions
```

**Permissions Matrix (Request/Response):**
```json
{
  "roleId": 2,
  "roleNome": "GESTOR",
  "permissions": [
    { "recurso": "ATIVO", "acao": "CRIAR", "contexto": "FILIAL", "concedida": true },
    { "recurso": "ATIVO", "acao": "LER", "contexto": "FILIAL", "concedida": true },
    { "recurso": "ATIVO", "acao": "ATUALIZAR", "contexto": "FILIAL", "concedida": true },
    { "recurso": "ATIVO", "acao": "EXCLUIR", "contexto": "FILIAL", "concedida": true },
    { "recurso": "ORDEM", "acao": "APROVAR", "contexto": "FILIAL", "concedida": true },
    { "recurso": "ORDEM", "acao": "LER_TODAS", "contexto": "FILIAL", "concedida": true },
    { "recurso": "RELATORIO", "acao": "GERAR", "contexto": "FILIAL", "concedida": true }
  ]
}
```

### 9.2 Usuários (Provisionamento + Roles + Filial)
```http
GET    /api/v1/admin/usuarios
GET    /api/v1/admin/usuarios/{id}
POST   /api/v1/admin/usuarios
PUT    /api/v1/admin/usuarios/{id}
PATCH  /api/v1/admin/usuarios/{id}/bloquear
PATCH  /api/v1/admin/usuarios/{id}/desbloquear
PATCH  /api/v1/admin/usuarios/{id}/reset-senha
DELETE /api/v1/admin/usuarios/{id}  (soft delete → INATIVO)
```

### 9.3 Troca Contexto Filial (Admin Global)
```http
GET    /api/v1/admin/filiais-disponiveis
POST   /api/v1/admin/contexto-filial  { "filialId": 2 }
```
**Response:** Define `X-Tenant-Id` para próximas requisições (ou claim no JWT)

### 9.4 Auditoria (Envers Query + Diff + Export)
```http
GET /api/v1/auditoria?entidade=Ativo&id=1&page=0&size=50&usuarioId=5&dataInicio=2025-01-01&dataFim=2025-01-31&acao=UPDATE
GET /api/v1/auditoria/exportar?entidade=Ativo&id=1&formato=PDF
GET /api/v1/auditoria/acessos-negados?periodo=24h
GET /api/v1/auditoria/integridade
```

**Response Auditoria (Timeline):**
```json
{
  "content": [
    {
      "revisao": 1245,
      "timestamp": "2025-01-15T14:30:00.000Z",
      "usuarioId": 5,
      "usuarioNome": "João Silva",
      "acao": "UPDATE",
      "entidade": "Ativo",
      "entidadeId": 1,
      "diff": {
        "localizacaoId": { "antes": 10, "depois": 15 },
        "responsavelId": { "antes": 5, "depois": 8 }
      },
      "ip": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 10. Relatórios & QR Code / PDF

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/relatorios/termo-responsabilidade` | Gerar Termo PDF (ALOCACAO/TRANSFERENCIA/BAIXA) | `RELATORIO_GERAR` (filial) |
| `GET` | `/api/v1/ativos/{id}/qr-code` | QR Code unitário (SVG/PNG) | `ATIVO_LER` (filial) |
| `POST` | `/api/v1/relatorios/etiquetas-lote` | Etiquetas lote (filtros → PDF A4 24/folha ou ZIP SVG) | `RELATORIO_GERAR` (filial) |
| `GET` | `/api/v1/relatorios/compliance?tipo=NR12&periodo=2025-01` | Relatórios compliance (NR-10/12, LGPD, ISO 27001) | `RELATORIO_GERAR` (filial) |

### 10.1 Termo Responsabilidade (Request)
```json
{
  "ativoId": 1,
  "responsavelId": 5,
  "tipo": "ALOCACAO",
  "observacoes": "Entregue em perfeito estado"
}
```
**Response:** `application/pdf` (stream) ou `base64` + `filename`

### 10.2 Etiquetas Lote (Request)
```json
{
  "filialId": 1,
  "tipoAtivoId": 2,
  "status": "ATIVO",
  "localizacaoId": 10,
  "formato": "PDF_A4"
}
```
**Response:** `application/pdf` (24 etiquetas/folha A4) ou `application/zip` (SVGs individuais)

---

## 11. LGPD (Direito ao Esquecimento + Portabilidade)

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/usuarios/me/solicitar-exclusao` | Anonimização PII + Auditoria preservada (hash) | Usuario (self) |
| `GET` | `/api/v1/usuarios/me/exportar` | Export JSON completo (portabilidade) | Usuario (self) |
| `GET` | `/api/v1/admin/lgpd/consentimentos` | Listar consentimentos (Admin/DPO) | `LGPD_GERENCIAR` (global) |
| `PUT` | `/api/v1/admin/lgpd/consentimentos/{usuarioId}` | Atualizar consentimentos | `LGPD_GERENCIAR` (global) |

### 11.1 Export Dados (Response)
```json
{
  "perfil": { "id": 123, "nome": "USUARIO_ANON_A1B2C3D4", "email": "a1b2c3d4@anonymized.local" },
  "ordens": [ { "id": 1, "numero": "OS-2025-001", "estado": "CONCLUIDA", ... } ],
  "ativosResponsavel": [ { "id": 1, "tag": "NB-001", ... } ],
  "healthChecks": [ { "id": 1, "score": 85, "coletadoEm": "2025-01-10T10:00:00.000Z" } ],
  "auditoria": [ { "revisao": 100, "acao": "UPDATE", "entidade": "Usuario", ... } ],
  "consentimentos": [ { "finalidade": "MARKETING", "concedido": true, "data": "2025-01-01" } ],
  "hashCorrelacao": "a1b2c3d4"
}
```

---

## 12. Error Responses (RFC 7807 Problem Details)

### 12.1 Validation Error (400)
```json
{
  "type": "https://aegis1.com/errors/validation-error",
  "title": "Dados de entrada inválidos",
  "status": 400,
  "detail": "Um ou mais campos falharam na validação",
  "instance": "/api/v1/ativos",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "errors": [
    { "field": "tag", "code": "UNIQUE_CONSTRAINT", "message": "Tag já cadastrada" },
    { "field": "dataAquisicao", "code": "FUTURE_DATE", "message": "Data de aquisição não pode ser futura" }
  ]
}
```

### 12.2 Business Rule Violation (409)
```json
{
  "type": "https://aegis1.com/errors/business-rule-violation",
  "title": "Regra de negócio violada",
  "status": 409,
  "detail": "Não é possível excluir — existem ordens de manutenção associadas. Reatribua ou conclua as ordens antes.",
  "instance": "/api/v1/filiais/1",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "ruleCode": "BR-04",
  "ruleDescription": "Exclusão de entidades mestras bloqueada se houver ordens vinculadas"
}
```

### 12.3 Unauthorized (401)
```json
{
  "type": "https://aegis1.com/errors/unauthorized",
  "title": "Não autenticado",
  "status": 401,
  "detail": "Access token expirado ou inválido",
  "instance": "/api/v1/ativos",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 12.4 Forbidden (403)
```json
{
  "type": "https://aegis1.com/errors/forbidden",
  "title": "Acesso negado",
  "status": 403,
  "detail": "Usuário não possui permissão ORDEM_APROVAR na filial 1",
  "instance": "/api/v1/ordens/123/aprovar",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requiredPermission": "ORDEM_APROVAR",
  "requiredContext": "FILIAL:1"
}
```

### 12.5 Rate Limited (429)
```json
{
  "type": "https://aegis1.com/errors/rate-limited",
  "title": "Muitas requisições",
  "status": 429,
  "detail": "Limite de 500 req/min excedido para /api/v1/ordens",
  "instance": "/api/v1/ordens",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "retryAfter": 45
}
```

---

## 13. WebSocket / SSE Endpoints (Tempo Real)

| Endpoint | Eventos | Autenticação |
| :--- | :--- | :--- |
| `/ws/dashboard/ordens` | `ordem.criada`, `ordem.iniciada`, `ordem.aprovada`, `ordem.concluida`, `ordem.cancelada`, `sla.breach` | JWT (handshake) + filialId claim |
| `/ws/dashboard/preditiva` | `health.check.critico`, `preditiva.alerta`, `preditiva.tratada` | JWT + filialId |
| `/ws/notificacoes` | `notificacao.nova` (push/email fallback) | JWT + userId |

**Formato Evento:**
```json
{
  "event": "ordem.aprovada",
  "timestamp": "2025-01-15T14:30:00.000Z",
  "payload": { "ordemId": 123, "estado": "APROVADA", "aprovador": "Maria Santos" },
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## 14. OpenAPI Document (Referência)

- **Arquivo:** `src/main/resources/openapi/openapi.yaml` (versionado no Git)
- **Geração:** SpringDoc OpenAPI 3.1 (Spring Boot 3.3)
- **Validação CI:** Contract Tests (Spring Cloud Contract / Pact) frontend↔backend
- **Breaking Change Policy:** Aditivo apenas em v1; Quebra = `/api/v2/` + 3 sprints deprecation notice

---

## 15. Rastreabilidade API ↔ Artefatos

| Endpoint Grupo | Use Cases | State Machine | User Stories | Controller | Testes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/auth/*` | UC-32 | Usuario FSM | US-SEC-001, 002 | `AuthController` | AuthControllerIT, SecurityConfigIT |
| `/filiais`, `/departamentos`, `/fornecedores`, `/funcionarios`, `/tipos-ativo` | UC-01 a UC-05 | — | US-CAD-* | `CadastroController` | *ControllerIT |
| `/ativos` + hardware + QR + PDF | UC-06 a UC-12 | Ativo FSM | US-ASV-* | `AtivoController`, `HardwareController`, `RelatorioController` | AtivoControllerIT, HardwareControllerIT |
| `/ativos/{id}/health-check` | UC-25 | HealthCheck FSM | US-PRD-001, 002 | `HealthCheckController` | HealthCheckServiceTest, HealthCheckControllerIT |
| `/ordens` + transições | UC-13 a UC-20 | Ordem FSM | US-ORD-* | `OrdemController` | OrdemControllerIT, SlaSchedulerIT |
| `/preventivas` + scheduler | UC-21 a UC-24 | Preventiva FSM | US-PRE-* | `PreventivaController` | PreventivaSchedulerIT, PreventivaControllerIT |
| `/dashboard/preditiva`, `/previsoes-falha` | UC-26 a UC-28 | PrevisaoFalha FSM | US-PRD-003 a 008 | `PreditivaController` | ManutencaoPreditivaServiceTest |
| `/busca` | UC-29 a UC-31 | — | US-BUS-* | `BuscaController` | FuzzySearchServiceTest, BuscaControllerIT |
| `/admin/roles`, `/admin/usuarios`, `/auditoria` | UC-33 a UC-38 | Usuario FSM | US-SEC-* | `AdminController`, `AuditoriaController` | SecurityConfigIT, AegisShieldTest, MultiTenancyIT |
| `/relatorios/termo`, `/qr-code`, `/etiquetas-lote`, `/compliance` | UC-39 a UC-42 | — | US-REL-* | `RelatorioController` | RelatorioControllerIT, QRCodeGeneratorTest |
| `/usuarios/me/exclusao`, `/exportar` | UC-37 | Usuario FSM (LGPD) | US-LGD-* | `LgpdController` | UsuarioControllerIT, LgpdServiceTest |

---

*Documento regenerado completamente com especificação OpenAPI 3.1 baseada em análise AST Java (Controllers, Services, DTOs, Security, Validation) + Use Cases v2.0 + State Machines v2.0. Substitui versão 1.0 que continha apenas contratos inferidos do frontend vanilla JS.*