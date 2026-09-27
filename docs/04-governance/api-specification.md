# API & Integration Contract Specification — Aegis Patrimônio

> **Versão da API:** Não definida · **Protocolo:** REST (previsto) · **Base URL:** Não configurada  
> **Autenticação:** JWT stateless (previsto, NFR-SEC02) — implementação pendente

---

## 1. Overview

Este documento descreveria o contrato da API REST do **Aegis Patrimônio** (endpoints, schemas, códigos de erro, paginação, versionamento, autenticação).  
**No entanto, nenhum arquivo de rotas/backend foi localizado no workspace atual** (`server/express-app.ts` não encontrado; nenhum `*Controller.java`, `*Router.ts`, `routes/*.js` ou `openapi.yaml` detectado).  

O System Architecture Document (SAD) prevê um monolito **Spring Boot 3.x** expondo API REST consumida pelo frontend Vanilla JS, com OpenAPI 3.0 via SpringDoc (NFR-M03). Essa especificação **não pôde ser gerada a partir de código real** porque a camada de backend não está presente nos artefatos analisados.

> **Ação requerida:** Localizar o código-fonte do backend (repositório raiz, branch `backend`, ou módulo `server/`) e executar novamente a extração de rotas. Até lá, este documento serve como *placeholder* rastreável.

---

## 2. Conventions (Previstas no SAD / NFRs)

| Item | Definição Prevista | Fonte |
| :--- | :--- | :--- |
| **Formato** | `application/json` | SAD §6, NFR-M03 |
| **Versionamento** | Path `/v1/` (ex.: `/api/v1/ativos`) | Padrão SpringDoc / RESTful |
| **Convenção de nomes** | `camelCase` para JSON (Java DTOs) | Spring Boot default |
| **Paginação** | `Pageable` Spring Data (`page`, `size`, `sort`) → resposta `Page<T>` | Spring Data REST |
| **Rate Limiting** | Não implementado — a definir (NFR-S03: 500 conn HikariCP) | — |
| **Idempotência** | Header `Idempotency-Key` em `POST`/`PUT`/`PATCH` críticos (aprovações, cancelamentos) | BR-02, NFR-SEC05 |
| **Correlação de traces** | Header `traceparent` (W3C) propagado em 100% requisições | NFR-O03 |

---

## 3. Authentication & Authorization (Previsto)

| Mecanismo | Detalhe | Status |
| :--- | :--- | :--- |
| **Tipo** | JWT stateless (HS256 ou RS256) | NFR-SEC02 |
| **Expiração access token** | ≤ 1 hora | NFR-SEC02 |
| **Refresh token** | Rotação + blocklist (`jti`) em cache/Redis | NFR-SEC02 |
| **RBAC** | Roles: `ADMIN`, `AUDITOR`, `GESTOR`, `OPERADOR` — `@PreAuthorize` em controllers | BR-01, NFR-SEC03 |
| **MFA** | Obrigatório para `ADMIN` via IdP corporativo (OIDC/SAML) | NFR-SEC02, BRD#7 |
| **Escopos sugeridos** | `ativos:read`, `ativos:write`, `manutencoes:read`, `manutencoes:write`, `alertas:read`, `auditoria:read`, `admin:config` | [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |

---

## 4. Endpoints — **Não Disponíveis (Rotas Não Localizadas)**

> **Fato:** A varredura automática não encontrou **nenhum** arquivo de definição de rotas (controllers, routers, OpenAPI spec, Swagger annotations).  
> **Consequência:** Não é possível listar endpoints, parâmetros, schemas de request/response, códigos de erro reais ou exemplos de curl.

### O que seria necessário para preencher esta seção

| Artefato Esperado | Localização Típica | Status |
| :--- | :--- | :--- |
| Controllers Spring (`@RestController`) | `src/main/java/.../controller/` ou `.../web/` | ❌ Não encontrado |
| Rotas Express/Fastify (se Node) | `server/routes/`, `src/routes/`, `express-app.ts` | ❌ Não encontrado |
| OpenAPI/Swagger YAML/JSON | `docs/openapi.yaml`, `src/main/resources/openapi/` | ❌ Não encontrado |
| SpringDoc ativo (`springdoc-openapi-starter-webmvc-ui`) | `pom.xml` / `build.gradle` | ❌ Não verificado (build file ausente) |
| Testes de contrato (Pact, Spring Cloud Contract) | `src/test/.../contract/` | ❌ Não encontrado |

### Próximos Passos Recomendados

1. **Confirmar localização do backend** — monorepo? repositório separado? branch dedicada?
2. **Executar extração de rotas** no código-fonte real (Java: `springdoc-openapi-maven-plugin` goal `generate`; Node: `swagger-jsdoc`/`@nestjs/swagger`).
3. **Publicar spec OpenAPI 3.0** em `/v3/api-docs` (SpringDoc) ou arquivo versionado no repo.
4. **Popular este documento** a partir da spec gerada (ferramenta: `openapi-typescript`, `swagger-codegen`, ou script interno).

---

## 5. Data Models — **Não Disponíveis**

Os DTOs/entidades (`AtivoDTO`, `ManutencaoDTO`, `HealthCheckDTO`, `AlertaDTO`, `AuditoriaLogDTO`, `UsuarioDTO`, `PageResponse<T>`) não puderam ser extraídos.  
O SAD §5 menciona entidades conceituais (`Ativo`, `Manutencao`, `HealthCheck`, `Funcionario`, `Usuario`, `AuditoriaLog`, `Alerta`, `Configuracao`), mas o contrato físico (campos, tipos, validações, enumerações) depende do código ou do schema de banco (Gap #1 do SAD).

---

## 6. Error Handling Standard (Previsto pelo Padrão Spring Boot + RFC 7807)

```json
{
  "timestamp": "2025-01-15T14:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Payload inválido",
  "path": "/api/v1/ativos",
  "traceId": "a1b2c3d4e5f6",
  "details": [
    { "field": "patrimonio", "issue": "must not be blank" },
    { "field": "dataAquisicao", "issue": "must be a past date" }
  ]
}
```

| Código HTTP | Código Interno Previsto | Quando Ocorre |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | `@Valid` falhou / constraint violation |
| 401 | `UNAUTHENTICATED` | JWT ausente, expirado, assinatura inválida |
| 403 | `FORBIDDEN` | Role/permission insuficiente (`@PreAuthorize`) |
| 404 | `NOT_FOUND` | Recurso inexistente (ativo, manutenção, etc.) |
| 409 | `CONFLICT` | Otimistic lock (`@Version`), duplicidade de chave única, transição de estado inválida |
| 422 | `UNPROCESSABLE_ENTITY` | Regra de negócio violada (ex.: manutenção já concluída) |
| 429 | `RATE_LIMITED` | Limite de requisições excedido (se implementado) |
| 500 | `INTERNAL_ERROR` | Exceção não tratada (logada com `traceId`) |
| 503 | `SERVICE_UNAVAILABLE` | Readiness probe falhou (DB, scheduler, disco) |

> **Nota:** O formato exato depende da implementação de `@ControllerAdvice` / `ErrorController` — não verificado.

---

## 7. Changelog da API

| Versão | Data | Mudança | Breaking? |
| :--- | :--- | :--- | :--- |
| — | — | **Nenhuma versão publicada** — spec não gerada | — |

---

## 8. Rastreabilidade & Gaps Conhecidos

| Item | Descrição | Ação / Responsável |
| :--- | :--- | :--- |
| **Gap #1** | Backend source code não localizado no workspace analisado | Eng Lead / DevOps: confirmar repo/branch/módulo |
| **Gap #2** | Motor de banco não definido → afeta DDL, índices, paginação nativa | DBA / Infra (Sprint 0) |
| **Gap #3** | SpringDoc / OpenAPI não configurado → sem spec automática | Backend Team: adicionar `springdoc-openapi-starter-webmvc-ui` |
| **Gap #4** | Build file (`pom.xml`/`build.gradle`) não detectado → versões exatas de Spring Boot, Spring Security, SpringDoc desconhecidas | Verificar repositório raiz |
| **Gap #5** | Contrato de integração com IdP (OIDC/SAML) não documentado | Security Team / Arquitetura |

---

## 9. Observação Final

Este documento **não contém endpoints inventados**. Ele reflete fielmente o estado da extração automatizada: **rotas não encontradas**.  
Assim que o backend for localizado e a spec OpenAPI gerada, este arquivo deve ser **regenerado integralmente** a partir da fonte real (código/anotações/SpringDoc), substituindo esta versão placeholder.