# ADR-001 — Vanilla JavaScript SPA sem Build Step servida via CDN com Backend API Externo

> **Data:** 15/01/2025 · **Status:** Accepted
> **Deciders:** Tech Lead, Arquiteto de Soluções · **Consulted:** Time de Frontend, Time de Backend (API), Segurança · **Informed:** Product Owner, DevOps, QA

## 1. Status
Accepted

## 2. Context & Problem Statement
O projeto **Aegis1** é uma aplicação frontend *single-page* (SPA) responsável pela interface de gestão de ordens de manutenção (criar, listar, iniciar, aprovar, concluir, cancelar, consultar custo total por ativo). O diagnóstico determinístico do codebase revela:
- 15 arquivos JavaScript (ES2020+), 627 LOC, 33 funções, 0 classes
- Única dependência de produção: `@popperjs/core@^2.11.8` (positioning para tooltips/dropdowns)
- **Nenhum script de build, bundler, TypeScript, framework UI (React/Vue/Svelte) ou runtime Rust/Tauri** no `package.json`
- 3 chamadas `console.*` residuais em `frontend/src/services/api.js` (linhas 26, 49, 52)
- Função `request` em `api.js:36` com complexidade ciclomática 13 (violando NFR-M01 ≤ 10)
- Backend, banco de dados, autenticação, mensageria e persistência **fora do escopo** deste repositório — expostos via API REST/HTTPS externa

Os requisitos não-funcionais (NFR) impõem:
- **Performance:** TTI ≤ 3,5 s (4G) / ≤ 2 s (desktop); bundle gzipped ≤ 150 kB; latência P95 `request` ≤ 800 ms (NFR-P01, P02, P03)
- **Escalabilidade:** CDN ≥ 500 usuários concorrentes sem degradação > 10% (NFR-S01)
- **Disponibilidade:** Frontend ≥ 99,9% mensal; RTO ≤ 15 min via rollback CDN (NFR-A01, A02)
- **Segurança:** HTTPS/TLS 1.2+; JWT em `localStorage` transitório (migração para cookie `HttpOnly; Secure; SameSite=Strict` planejada); zero `console.*` em produção; CSP restritivo (NFR-SEC01, SEC02, SEC04)
- **Manutenibilidade:** Complexidade ≤ 10; cobertura ≥ 80% em `api.js` e fluxos de ordem; ESLint/Prettier no CI com `no-console: error` (NFR-M01, M02, M03)
- **Custo:** Hospedagem estática ≤ USD 30/mês (NFR-CO01)

A decisão arquitetural central é **como entregar esta SPA** atendendo a esses NFRs com o codebase existente (vanilla JS, sem build, uma dependência).

## 3. Decision Drivers
* **Performance & Bundle Size (NFR-P03, NFR-CO02):** Bundle ≤ 150 kB gzipped; zero custo de licença; zero dependências de build/runtime.
* **Simplicidade Operacional & Custo (NFR-CO01, NFR-PO02):** Deploy em qualquer CDN/Object Storage sem pipeline de build; hospedagem ≤ USD 30/mês.
* **Escalabilidade Nativa (NFR-S01):** CDN edge cache absorve carga sem configuração adicional.
* **Segurança & Compliance (NFR-SEC01, SEC02, SEC04, SEC05):** TLS 1.2+, CSP, migração JWT → cookie HttpOnly, zero `console.*` em produção.
* **Manutenibilidade & Qualidade (NFR-M01, M02, M03):** Complexidade ciclomática ≤ 10; cobertura ≥ 80%; lint `no-console: error` no CI.
* **Separação de Responsabilidades:** Frontend = UI + integração; Backend = negócio, persistência, auth (fora do escopo).
* **Time-to-Market & Risco Técnico:** Codebase já existe em vanilla JS (15 arquivos, 627 LOC); reescrita em framework traria risco e atraso.

## 4. Considered Options

### Option A: Vanilla JavaScript (ES Modules) + @popperjs/core — **Sem Build Step** (Escolhida)
* **Descrição:** Manter arquitetura atual: 15 arquivos `.js` servidos diretamente como ES Modules (`<script type=module>`), `@popperjs/core` via CDN ou incluído no bundle manual, roteamento client-side nativo (History API ou hash), `api.js` encapsula `fetch` com auth/retry/timeout. Deploy = upload de arquivos versionados para CDN/Object Storage.
* **Prós:**
  - Bundle mínimo (apenas código próprio + Popper ~12 kB gzipped) → atende NFR-P03 (≤ 150 kB) com folga
  - Zero dependências de build, toolchain, CI/CD complexo → NFR-CO01, NFR-CO02, NFR-PO02
  - Deploy imutável versionado (hash no filename ou pasta) → rollback instantâneo (NFR-A02 RTO ≤ 15 min)
  - Escala nativa do CDN (edge cache global) → NFR-S01 (≥ 500 usuários) sem esforço
  - Alinhado ao codebase real (diagnóstico: 15 JS, 0 TS, 0 build scripts, 1 dep produção)
  - Controle total sobre CSP, headers, `trace-id`, sanitização (NFR-SEC05, NFR-O03)
* **Contras:**
  - Sem tree-shaking automático → código morto pode inflar bundle se não gerido manualmente
  - Code-splitting por rota requer `dynamic import()` manual + roteador leve (ainda não implementado — NFR-P03 risco)
  - Sem TypeScript → maior risco de regressão de tipos; NFR-M02 exige testes ≥ 80% para compensar
  - `api.js:request` monolítica (complexidade 13) viola NFR-M01 → refatoração obrigatória
  - `console.*` residuais (3 ocorrências) → pipeline deve falhar com `no-console: error` (NFR-M03, SEC04)

### Option B: Adotar Bundler (Vite/esbuild) + TypeScript + Framework Leve (Preact/Svelte/Vanilla TS)
* **Descrição:** Introduzir `package.json` com scripts, `vite.config.ts`, migrar para TypeScript, adicionar Preact (~3 kB) ou Svelte (compilado) ou manter vanilla TS com bundler. Build gera chunks com hash, code-splitting automático, tree-shaking.
* **Prós:**
  - Code-splitting por rota nativo (NFR-P03 atendido out-of-the-box)
  - TypeScript → detecção precoce de erros, refatoração segura (`request` complexidade 13)
  - Tree-shaking remove código morto; otimizações de produção (minify, compression)
  - Ecossistema de plugins (PWA, image optimization, etc.)
  - Testes mais fáceis (Vitest/Jest + Testing Library)
* **Contras:**
  - Adiciona etapa de build, CI/CD, dependências de dev (~200-500 MB `node_modules`) → viola NFR-CO02 (zero custo licença, mas custo operacional), NFR-PO02 (build independente de provedor)
  - Bundle inicial ainda precisa caber ≤ 150 kB gzipped; framework + runtime + Polyfills pode estourar
  - Reescrita parcial/total do codebase (15 arquivos, 627 LOC) → risco de regressão, atraso
  - Complexidade operacional: build, cache busting, source maps, variáveis de ambiente no build
  - Overkill para SPA de 15 arquivos com uma dependência

### Option C: Server-Side Rendering (Next.js/Remix/Astro) ou Edge Compute
* **Descrição:** Mover para framework full-stack com SSR/SSG/ISR, deploy em Vercel/Netlify/Cloudflare Pages, API routes como BFF.
* **Prós:**
  - SEO, First Paint otimizado, streaming, edge middleware
  - Integração nativa com backend (BFF), auth, cache
* **Contras:**
  - **Viola arquitetura definida no SAD:** "Static SPA + Backend-for-Frontend (BFF) externo", "zero server-side rendering", "zero edge compute no frontend"
  - Requer runtime Node.js no edge/origem → custo, complexidade, lock-in provedor (NFR-PO02)
  - Backend já existe externamente; duplicar lógica de negócio no frontend cria inconsistência
  - NFR-CO01 (≤ USD 30/mês) difícil com SSR/Edge Functions em escala
  - Fora do escopo do codebase atual (diagnóstico: apenas frontend estático)

## 5. Decision Outcome
**Opção escolhida:** Option A — Vanilla JavaScript (ES Modules) + @popperjs/core — Sem Build Step

**Rationale:** A Option A é a **única que satisfaz simultaneamente todos os drivers obrigatórios** derivados dos NFRs e do codebase real:
- **NFR-P03 (≤ 150 kB gzipped):** Código próprio (627 LOC ≈ ~25 kB min) + Popper (~12 kB gzipped) = ~37 kB — folga de 75% para code-splitting futuro.
- **NFR-CO01 (≤ USD 30/mês) & NFR-CO02 (zero custo licença):** Hospedagem estática S3+CloudFront / Azure Static Web Apps / Netlify free tier cobrem 500 usuários / 10k pageviews/dia.
- **NFR-PO02 (build independente de provedor):** Arquivos servidos diretamente; migração entre CDNs = copiar arquivos.
- **NFR-S01 (500 usuários concorrentes):** CDN edge cache nativo; zero configuração.
- **NFR-A02 (RTO ≤ 15 min):** Rollback = trocar prefixo de versão no CDN / apontar para pasta anterior.
- **Codebase real:** 15 arquivos JS, 0 TS, 0 build scripts, 1 dep produção — reescrever traria risco injustificado.
- **Segurança (NFR-SEC05):** Controle total sobre CSP, headers, `trace-id`, sanitização sem abstrações de framework.

Os **contras conhecidos** (code-splitting manual, refatoração `request`, remoção `console.*`, testes, TypeScript) são **dívidas técnicas explicitamente aceitas** com plano de mitigação no SAD (Seção 13) e NFRs (M01, M02, M03, P03). Eles não invalidam a decisão — são itens de backlog priorizados.

### Comparison Matrix
| Critério | Peso (1-5) | Option A: Vanilla JS Sem Build | Option B: Bundler + TS + Framework Leve | Option C: SSR/Edge Framework |
| :--- | :---: | :---: | :---: | :---: |
| Bundle Size ≤ 150 kB gzipped (NFR-P03) | 5 | **5** (37 kB estimado) | 3 (risco de estourar com runtime) | 2 (framework + runtime + hydration) |
| Custo Hospedagem ≤ USD 30/mês (NFR-CO01) | 5 | **5** (Static hosting free/barato) | 3 (build minutes, bandwidth) | 2 (SSR/Edge Functions custam mais) |
| Simplicidade Operacional / Deploy (NFR-PO02) | 5 | **5** (upload arquivos) | 2 (pipeline build, cache busting) | 1 (lock-in, config complexa) |
| Escalabilidade CDN Nativa (NFR-S01) | 4 | **5** (edge cache imutável) | 4 (assets estáticos ok) | 2 (SSR requer origem/edge compute) |
| Segurança: CSP, trace-id, sanitização (NFR-SEC05) | 5 | **5** (controle total) | 3 (framework pode injetar inline scripts) | 2 (middleware edge limita headers) |
| Manutenibilidade: Complexidade ≤ 10 (NFR-M01) | 4 | 2 (`request`=13 **viola**, mas refatorável) | **4** (TS + lint + refatoração segura) | 3 (complexidade de framework) |
| Cobertura Testes ≥ 80% (NFR-M02) | 4 | 2 (não configurado, vanilla harder) | **4** (Vitest/Jest + TL fácil) | 3 (testes SSR mais complexos) |
| Time-to-Market / Risco Regressão | 4 | **5** (codebase pronto, 15 arquivos) | 2 (migração 15 arquivos + config) | 1 (reescrita arquitetural) |
| Alinhamento Codebase Real (Diagnóstico) | 5 | **5** (15 JS, 0 TS, 0 build, 1 dep) | 1 (contradiz diagnóstico) | 1 (contradiz SAD + diagnóstico) |
| **TOTAL PONDERADO** | — | **4.6** | **2.8** | **1.9** |

## 6. Consequences

### Positive
* **Entrega imediata:** Codebase já funcional em vanilla JS; deploy hoje em qualquer CDN atende NFRs de performance, custo, escalabilidade, disponibilidade.
* **Zero lock-in:** Migração entre provedores de static hosting = `rsync`/`aws s3 sync`/`az storage blob upload`.
* **Superfície de ataque mínima:** Sem `node_modules` em produção (exceto Popper opcional), sem build pipeline, sem runtime server — apenas arquivos estáticos imutáveis servidos via HTTPS/TLS 1.2+.
* **Observabilidade nativa:** `trace-id` propagado em header `fetch`, Core Web Vitals via `web-vitals` lib, erros não tratados → telemetria (Sentry/Datadog) — tudo implementável em `api.js` e `index.html` sem dependências extras.

### Negative / Trade-offs
* **Code-splitting manual obrigatório (NFR-P03):** Sem bundler, `dynamic import()` por rota + roteador leve (ex.: `navigo` 2 kB ou custom) deve ser implementado **antes do go-live**. Risco: bundle único atual pode exceder 150 kB gzipped se crescer.
* **Refatoração `api.js:request` (NFR-M01):** Complexidade 13 → decompor em `withTimeout`, `withRetry`, `withAuth`, `parseJSON`, `handleError` (cada ≤ 10). Bloqueia cobertura ≥ 80% (NFR-M02).
* **Remoção `console.*` (NFR-M03, SEC04):** 3 chamadas em `api.js` (linhas 26, 49, 52) → pipeline CI deve falhar com `eslint: no-console: error`. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]` — assumido que ESLint será configurado.
* **Ausência de TypeScript:** Mitigado por testes ≥ 80% (NFR-M02) + JSDoc types + ESLint `no-implied-eval`, `no-unused-vars`. `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
* **JWT em `localStorage` (transitório):** Risco XSS até migração para cookie `HttpOnly; Secure; SameSite=Strict` (NFR-SEC02). Requer coordenação com backend (Set-Cookie no login/refresh).

### Neutral
* **Roteamento client-side:** History API ou hash-based — decisão adiada para implementação de code-splitting (roteador leve necessário).
* **Gerenciamento de estado:** Apenas token JWT + UI transitório (NFR-S03 proíbe estado de sessão além do token). Redux/Zustand desnecessários.
* **Componentização:** Module Pattern / ES Modules atual suficiente para 15 arquivos. Web Components ou framework não justificados.

## 7. Implementation Notes
* **Ações necessárias (ordem de prioridade):**
  1. **Remover `console.*` de `api.js`** (linhas 26, 49, 52) → substituir por logger condicional (`if (import.meta.env.DEV) console...`) ou telemetria estruturada. **Bloqueia pipeline CI (NFR-M03, SEC04).** <!-- source: Diagnóstico: console.* residuais -->
  2. **Refatorar `api.js:request` (complexidade 13)** → extrair `withTimeout(fetch, ms)`, `withRetry(fetch, opts)`, `withAuth(fetch, token)`, `parseJSON(response)`, `handleApiError(error)` → cada função ≤ complexidade 10. **Bloqueia NFR-M01 e testes NFR-M02.** <!-- source: Diagnóstico: api.js:36 -->
  3. **Configurar ESLint + Prettier no CI** com regras: `no-console: error`, `no-unused-vars: error`, `prefer-const: error`, `no-implied-eval: error`. **Pré-requisito para NFR-M03.**
  4. **Implementar code-splitting por rota:** Dynamic `import('./routes/ordens.js')` + roteador leve (`navigo` ou custom 50 LOC) → chunks carregados sob demanda. **Obrigatório para NFR-P03 (≤ 150 kB inicial).** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
  5. **Suite de testes (Jest/Vitest + Testing Library vanilla):** Cobertura ≥ 80% em `api.js` (authInterceptor, request, handleApiError) + componentes de fluxo de ordem (criar, listar, iniciar, aprovar, concluir, cancelar, custoTotalPorAtivo). **NFR-M02.** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
  6. **Contrato OpenAPI 3.0 + Testes de Contrato (Pact):** Validar integração frontend↔backend no CI. **NFR-M04.** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
  7. **Migração JWT → Cookie HttpOnly:** Coordenar com backend (Set-Cookie `Secure; SameSite=Strict; HttpOnly` no login/refresh); remover `localStorage` token; atualizar `authInterceptor` para não injetar `Authorization` header (cookie enviado automaticamente). **NFR-SEC02, antes do go-live.**
  8. **CSP Restritivo via CDN Headers:** `default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' https://api.aegis1.example.com https://telemetry.example.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self'`. **NFR-SEC05.** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`
  9. **Instrumentação Observabilidade:** `trace-id` (UUID v4) em header `traceparent` (W3C TraceContext) em toda `request`; duração, status, retry count → telemetria; Core Web Vitals via `web-vitals` lib → RUM. **NFR-O01, O02, O03, O04.** `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`

* **Prazo estimado de migração:** Itens 1-3 (críticos, bloqueiam CI) — **1 sprint**; Itens 4-6 (NFRs performance/qualidade) — **2 sprints**; Itens 7-9 (segurança/observabilidade) — **1 sprint** (paralelo com backend). Total: **~4 sprints** para go-live com todos NFRs atendidos.

* **Rollback plan:** Se qualquer item causar regressão em produção:
  - **CDN:** Apontar para versão anterior (pasta/hash anterior) — RTO ≤ 15 min (NFR-A02).
  - **Código:** `git revert` commit problemático → novo build (se houver) / novo upload → deploy versão anterior.
  - **Configuração (CSP, cookies):** Feature flag via `window.__AEGIS1_CONFIG__` injetado no `index.html` pelo CDN (CloudFront Function / Cloudflare Worker) → desabilitar CSP / voltar para `localStorage` instantaneamente sem novo deploy de código.

## 8. Links & References
* **System Architecture Document (SAD)** — Seções 1, 2, 3, 4, 7, 9, 10, 11, 12, 13 <!-- source: SAD -->
* **NFR Document** — NFR-P01, P02, P03, S01, A01, A02, SEC01, SEC02, SEC04, SEC05, M01, M02, M03, CO01, CO02, PO02, O01, O02, O03, O04 <!-- source: SAD#NFR references -->
* **Diagnóstico Determinístico** — 15 arquivos JS, 627 LOC, 33 funções, 1 dep produção (@popperjs/core), 3 `console.*`, complexidade 13 em `api.js:36` <!-- source: Diagnóstico -->
* **Stack Real Verificada** — `@popperjs/core@^2.11.8` apenas; sem build, sem TS, sem framework, sem backend, sem banco <!-- source: Stack JSON -->
* **ADRs Relacionadas:** Nenhuma formalizada até o momento (esta é a ADR-001) <!-- source: SAD#ADRs relacionadas -->
* **Issues/Tickets:** 
  - Refatorar `api.js:request` (complexidade 13) → NFR-M01
  - Remover `console.*` + configurar ESLint `no-console: error` → NFR-M03, SEC04
  - Code-splitting por rota → NFR-P03
  - Testes ≥ 80% `api.js` + fluxos ordem → NFR-M02
  - JWT → Cookie HttpOnly → NFR-SEC02
  - CSP + Sanitização → NFR-SEC05
  - Observabilidade (trace-id, RUM, telemetria) → NFR-O01..O04
  - Contrato OpenAPI + Pact → NFR-M04