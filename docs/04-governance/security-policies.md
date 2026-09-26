# Application Security Policies & Boundary Enforcement — Aegis1

> **Owner:** Security/Eng Lead · **Classificação do documento:** Interno/Confidencial

## 1. Threat Model Overview
* **Ativos a proteger:** Token JWT de autenticação (armazenado em `localStorage` atualmente), dados de ordens de manutenção (criação, status, custos, evidências — checklist/foto), credenciais de refresh token, PII de usuários (nome, departamento, filial, função), integridade da UI servida via CDN.
* **Atores de ameaça considerados:** Atacante externo explorando XSS para roubar JWT do `localStorage`, usuário malicioso injetando payloads em campos de formulário (departamento, filial, fornecedor, funcionário, evidências), agente automatizado explorando ausência de CSP ou `console.*` vazando informações sensíveis, insider com acesso ao CDN/Object Storage tentando injetar código malicioso nos assets estáticos.
* **Superfícies de ataque principais:** 
  - **API pública (frontend → backend externo):** Endpoints REST/HTTPS para autenticação (`/auth/login`, `/auth/refresh`), ordens (`/ordens`, `/ordens/:id/iniciar`, `/ordens/:id/aprovar`, `/ordens/:id/concluir`, `/ordens/:id/cancelar`), custos (`/custos/total-por-ativo`), upload de evidências (checklist/foto no fluxo aprovar). <!-- source: SAD#6 -->
  - **Execução de código no browser:** `api.js:request` (complexidade 13, mistura auth/timeout/retry/parsing), formulários de cadastro e evidências, roteamento client-side. <!-- source: SAD#3, Diagnóstico: api.js:36 -->
  - **CDN/Static Hosting:** Assets imutáveis servidos via HTTPS; risco de cache poisoning ou injeção se bucket/object storage comprometido. <!-- source: SAD#11 -->
  - **Telemetria/RUM:** Endpoints de erro (Sentry) e Core Web Vitals — superfície de exfiltração se não validados. <!-- source: SAD#10 -->

## 2. Context Isolation Boundary
* **Princípio:** Cada sessão de usuário (aba/navegador) deve ser isolada — token JWT, estado de UI e `trace-id` não devem vazar entre abas ou origens. O frontend não mantém estado de sessão além do token (NFR-S03). <!-- source: SAD#5, SAD#7 -->
* **Mecanismo de enforcement:** 
  - **Same-Origin Policy (SOP)** nativo do browser — SPA servida de única origem (ex.: `https://aegis1.example.com`).
  - **`trace-id` por request** (UUID v4) gerado no frontend e propagado via header `trace-id` em toda chamada `api.js:request`; não persistido além da duração da requisição. <!-- source: SAD#9, SAD#10 -->
  - **Storage isolation:** `localStorage`/`sessionStorage` escopados à origem; migração planejada para cookie `HttpOnly; Secure; SameSite=Strict` remove acesso via JS (NFR-SEC02). <!-- source: SAD#2, SAD#9 -->
* **Validação:** Testes de penetração focados em XSS/CSRF; verificação de que `trace-id` não aparece em logs de frontend nem em `localStorage`; auditoria de headers `Set-Cookie` no login/refresh após migração.

## 3. Workspace / Filesystem Boundary Validation
* **Regra:** O frontend **não acessa filesystem local do usuário** — não há `FileSystem Access API`, `electron`, `tauri` ou qualquer bridge nativo. O diagnóstico confirma: projeto não tem `src-tauri/Cargo.toml` (sem empacotamento Tauri). <!-- source: Stack real: rust_cargo_dependencies -->
* **Upload de evidências (checklist/foto no fluxo aprovar):** 
  - Validação **client-side** antes de enviar ao backend: tipo MIME permitido (`image/jpeg`, `image/png`, `application/pdf`), tamanho máximo (ex.: 5 MB), sanitização de nome de arquivo (remover path traversal `../`, caracteres de controle).
  - **Nenhum processamento local de arquivo** — arquivo lido via `FileReader`/`FormData` e enviado diretamente via `fetch` multipart para backend externo.
* **Casos de teste obrigatórios:** 
  - Tentativa de upload com nome `../../etc/passwd` → rejeitado client-side.
  - Arquivo > 5 MB → rejeitado com mensagem amigável.
  - Tipo MIME não permitido (ex.: `application/x-msdownload`) → rejeitado.
  - Polyglot files (imagem com payload executável) → responsabilidade do backend (fora do escopo).

## 4. Command Execution Boundary
* **Não aplicável** — O frontend **não executa comandos de sistema, shell, subprocessos ou binários**. Arquitetura é Vanilla JS rodando no browser (SAD#2). Não há agentes, automação, `child_process`, `eval()` ou `Function()` constructor no código (diagnóstico não encontrou). <!-- source: SAD#2, Diagnóstico: 0 classes, 33 funções, sem eval/Function -->

## 5. Data Protection Rules
* **Classificação de dados:**
  | Dado | Classificação | Onde reside | Controle |
  | :--- | :--- | :--- | :--- |
  | JWT (access/refresh token) | **Restrito (Credencial)** | `localStorage` (atual) → Cookie `HttpOnly; Secure; SameSite=Strict` (planejado) | NFR-SEC02, NFR-SEC03 |
  | Dados de ordem (descrição, status, custo) | **Confidencial** | Backend externo; frontend apenas exibe em memória | HTTPS/TLS 1.2+ (NFR-SEC01), CSP (NFR-SEC05) |
  | Evidências (checklist, foto) | **Confidencial** | Backend externo; frontend coleta via `FormData` | Validação client-side + HTTPS |
  | PII (nome, departamento, filial, função) | **Restrito (PII)** | Backend externo; frontend coleta em formulários | Sanitização output (textContent), HTTPS |
  | `trace-id` (UUID v4) | **Interno** | Header HTTP, memória da requisição | Não logado no frontend (NFR-O01) |
* **Criptografia em repouso:** Não aplicável ao frontend — nenhum dado persistido localmente além do token (migração para cookie `HttpOnly` remove até isso). Backend externo responsável (fora do escopo). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Criptografia em trânsito:** **TLS 1.2+ obrigatório** para todas as conexões (CDN → Browser, Browser → Backend API, Browser → Telemetria/RUM). HSTS no CDN. NFR-SEC01. <!-- source: SAD#1, SAD#9 -->
* **Mascaramento/Anonimização:** Em ambientes não-produtivos (staging, homologação), dados de ordens e PII devem ser anonimizados ou sintéticos. Frontend não processa dados sensíveis localmente — responsabilidade do backend. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Retenção & Descarte:** 
  - Token JWT: expirado pelo backend (curto TTL, ex.: 15 min); refresh token rotacionado a cada uso (máx. 1 retry, NFR-SEC03). Logout limpa `localStorage`/`sessionStorage` e revoga cookie futuro.
  - Dados de formulário: mantidos apenas em memória durante edição; limpos ao navegar para fora (SPA routing).
  - Logs de erro (telemetria): retenção conforme política do provedor (ex.: Sentry 90 dias); sem PII/tokens no payload (NFR-O01).

## 6. Authentication & Authorization
* **Mecanismo de auth:** **JWT Bearer Token** via header `Authorization: Bearer <token>`. Implementado em `api.js:authInterceptor` — injeta token em toda request, implementa refresh automático (máx. 1 retry) e logout em falha. <!-- source: SAD#3, SAD#9 -->
* **Modelo de autorização:** **RBAC delegado ao backend** — frontend apenas reflete permissões recebidas (ex.: botões "Iniciar", "Aprovar", "Concluir", "Cancelar" condicionais ao papel do usuário). Nenhuma decisão de autorização no frontend. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Política de senha/MFA:** Fora do escopo do frontend — responsabilidade do Identity Provider/backend de autenticação (login/refresh endpoints). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Gestão de sessão:** 
  - Access token: TTL curto (ex.: 15 min), armazenado em `localStorage` (atual) / cookie `HttpOnly` (futuro).
  - Refresh token: armazenado apenas no backend (HttpOnly cookie) ou em `localStorage` (atual, transitório); rotação a cada uso, máx. 1 retry automático (NFR-SEC03). 
  - Expiração: inatividade > TTL do access token → redirect para login.
  - Revogação: logout chama endpoint de revogação (se houver) + limpa storage local + redireciona.

## 7. Secrets Management
* **Armazenamento:** **Nenhum segredo no frontend** — código servido publicamente via CDN. Variáveis de ambiente públicas (ex.: `VITE_API_BASE_URL`) injetadas no build como `window.__AEGIS1_CONFIG__` no `index.html` (SAD#11). Tokens JWT são credenciais de sessão, não segredos de aplicação.
* **Rotação:** Não aplicável (sem segredos estáticos no frontend). Rotação de chaves de assinatura JWT é responsabilidade do backend/IdP. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Acesso:** Princípio do menor privilégio — frontend só conhece URL base da API e endpoints públicos; não tem acesso a chaves de assinatura, segredos de banco, credenciais de telemetria (DSN do Sentry injetado no build, não no código).

## 8. Input Validation & Output Encoding
* **Validação (allowlist, não denylist):**
  - **Formulários de cadastro (departamento, filial, fornecedor, funcionário):** Validação client-side via schema (ex.: Zod ou validação nativa) — campos obrigatórios, comprimento máximo, charset permitido (ex.: `[a-zA-ZÀ-ÿ0-9\s\-_]`), sem HTML/JS.
  - **Evidências (checklist/foto):** Validação de tipo MIME, tamanho, nome de arquivo (Seção 3).
  - **Parâmetros de URL/rota:** Roteamento hash-based ou History API — parâmetros validados antes de usar em `api.js:request`.
* **Prevenção de injeção:**
  - **XSS:** Uso obrigatório de `textContent` / `innerText` vs `innerHTML`; se HTML rico for necessário (ex.: descrição de ordem com formatação), **DOMPurify** sanitizado antes de renderizar. NFR-SEC05. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
  - **SQL/Command Injection:** Não aplicável no frontend (sem DB, sem shell). Backend responsável.
  - **Prompt Injection:** Não aplicável (sem integração LLM no frontend).
* **Sanitização de output / Encoding contextual:**
  - **HTML context:** `textContent` para dados de usuário; DOMPurify se HTML.
  - **Attribute context:** `setAttribute` / `dataset` — escape automático do browser.
  - **URL context:** `encodeURIComponent` para query params.
  - **JSON context:** `JSON.stringify` + `Content-Type: application/json` — seguro por padrão.

## 9. Dependency & Supply Chain Security
* **Scan de vulnerabilidades:** **Dependabot** (GitHub) ou **npm audit** no CI — cadência: a cada PR + scan semanal agendado. Única dependência de produção: `@popperjs/core@^2.11.8` (MIT, amplamente auditado). <!-- source: Stack real: dependencies_reais -->
* **Política de atualização:** 
  - **Crítico (CVSS ≥ 9.0):** ≤ 48 h após publicação do patch.
  - **Alto (CVSS 7.0–8.9):** ≤ 7 dias.
  - **Médio/Baixo:** ≤ 30 dias ou próximo release planejado.
* **SBOM (Software Bill of Materials):** Gerado a cada release via `npm sbom` ou `@cyclonedx/bom` — armazenado como artifact do pipeline CI/CD (ex.: GitHub Actions artifact, S3 versionado). `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **Integridade de dependências:** `package-lock.json` commitado; `npm ci` no CI; verificação de assinatura `npm attest` se disponível. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 10. Logging & Auditability
* **O que deve ser logado (frontend → telemetria):**
  - Erros não tratados (`window.onerror`, `unhandledrejection`) — payload JSON estruturado: `{ timestamp, traceId, error: { message, stack, componentStack }, userAgent, url, userId? }`. NFR-O01. <!-- source: SAD#10 -->
  - Falhas de autenticação (401, refresh token falhou) — sem token no payload.
  - Mudanças de permissão visíveis na UI (ex.: botão "Aprovar" aparece/desaparece) — evento `permission_change` com `role`, `resource`, `allowed`.
  - Latência/erro de API (`api.js:request`) — `durationMs`, `status`, `retryCount`, `endpoint`, `traceId`. NFR-O04. <!-- source: SAD#10 -->
* **O que NUNCA deve ser logado:**
  - **Tokens JWT** (access/refresh) — nem em headers, nem em body, nem em `localStorage` dump.
  - **PII em texto claro** (nome, email, documento) — hashear (ex.: SHA-256 truncado) se necessário para correlação.
  - **Conteúdo de evidências** (base64 de imagem, texto de checklist).
  - **Senhas, chaves de API, segredos de configuração.**
  - **`console.*` em produção** — pipeline falha com `no-console: error` (NFR-M03, NFR-SEC04). <!-- source: Diagnóstico: 3 chamadas console.* residuais em api.js:26,49,52 -->
* **Retenção de logs de auditoria:** Conforme política do provedor de telemetria (ex.: Sentry 90 dias, Datadog 15 meses). Logs de acesso CDN (CloudFront/Azure) retidos 1 ano para forense. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

## 11. Incident Response (Segurança)
* **Processo de disclosure de vulnerabilidade:** Canal responsável: **security@aegis1.example.com** (ou domínio da organização). PGP key publicada em `/.well-known/security.txt`. Resposta inicial ≤ 24 h. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **SLA de resposta por severidade (frontend):**
  | Severidade | Exemplo | SLA Resposta | SLA Mitigação (deploy) |
  | :--- | :--- | :--- | :--- |
  | **Crítico** | XSS ativo roubando JWT, injeção de código no CDN, vazamento de token em log | ≤ 1 h | ≤ 4 h (rollback CDN + hotfix) |
  | **Alto** | CSP bypass, CSRF em mutação sensível, dependência com RCE | ≤ 4 h | ≤ 24 h |
  | **Médio** | Information disclosure em erro, header de segurança faltando | ≤ 1 dia útil | ≤ 5 dias úteis |
  | **Baixo** | `console.*` residual, header `X-Content-Type-Options` ausente | ≤ 5 dias úteis | Próximo release |
* **Playbooks específicos do frontend:**
  - **XSS/Token leak:** 1) Revogar tokens afetados no backend (forçar re-login), 2) Deploy hotfix com sanitização/CSP, 3) Invalidar cache CDN (novo hash), 4) Notificar usuários se PII exposta.
  - **CDN compromise:** 1) Rotacionar credenciais de deploy do CDN, 2) Rebuild + deploy de versão conhecida boa (RTO ≤ 15 min, NFR-A02), 3) Auditar logs de acesso ao bucket/object storage. <!-- source: SAD#11 -->

## 12. Compliance Mapping
| Requisito Regulatório | Como é atendido | Evidência |
| :--- | :--- | :--- |
| **LGPD Art. 7º (Consentimento)** | Formulários de cadastro com checkbox explícito para tratamento de dados; backend registra consentimento. Frontend apenas coleta e envia. | Print do formulário + log de consentimento no backend. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| **LGPD Art. 12 (Acesso), Art. 18 (Eliminação)** | Botão "Excluir meus dados" no perfil (planejado NFR-C01) → chama backend → limpa `localStorage`/`sessionStorage` → logout. | Testes de integração E2E + documentação do fluxo. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| **LGPD Art. 46 (Segurança)** | HTTPS/TLS 1.2+ (NFR-SEC01), CSP restritivo (NFR-SEC05), JWT em cookie HttpOnly (planejado NFR-SEC02), zero `console.*` (NFR-SEC04), sanitização input/output. | Relatório de pentest, headers `security.txt`, config CSP no CDN, pipeline `no-console: error`. |
| **LGPD Art. 48 (Comunicação de incidente)** | Processo de disclosure (Seção 11) + integração com telemetria para detecção precoce (alertas NFR-O04). | Runbook de incidente + logs de alerta. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| **ISO 27001 A.8.2.3 (Tratamento de mídia)** | Upload de evidências validado client-side (tipo/tamanho/nome) + HTTPS; backend responsável por armazenamento seguro. | Casos de teste Seção 3 + política de retenção backend. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |
| **PCI DSS Req 6.5.6 (XSS)** | CSP `script-src 'self'`, `textContent` vs `innerHTML`, DOMPurify se HTML, `no-console: error` no CI. | Config CSP, resultados SAST/DAST, pipeline CI. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` |