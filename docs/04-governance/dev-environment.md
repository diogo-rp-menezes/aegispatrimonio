# Unified Development Environment Configuration — Aegis1

## 1. Local Prerequisites & Tooling
* **Runtime:** Nenhum runtime obrigatório (aplicação é vanilla JS/ES Modules servida estaticamente). Opcional: **Node.js ≥ 18.x** apenas para ferramentas de desenvolvimento (servidor estático, lint, testes futuros).
* **Gerenciador de pacotes:** `npm` (v9+) ou `pnpm` (v8+) — usado apenas para instalar ferramentas de dev (ESLint, Prettier, servidor estático, Jest/Vitest quando configurados). Não há `package.json` com scripts no repositório atual.
* **Ferramentas obrigatórias:**
  - **Editor:** VS Code (recomendado) com extensões: *ESLint*, *Prettier*, *Live Server* (ou similar para servir arquivos estáticos).
  - **Git** ≥ 2.40.
  - **Navegador** com DevTools (Chrome/Edge/Firefox) para debug de ES Modules, Service Workers, CSP, Network.
* **Versão mínima do SO/Shell:** Qualquer SO com suporte a Node.js 18+ (Windows 10+, macOS 12+, Linux glibc 2.28+). Shell: Bash (Linux/macOS/WSL) ou PowerShell 7+ (Windows).

> **Nota:** O diagnóstico confirma que o repositório **não possui** `package.json` com scripts, `devDependencies`, bundler (Vite/Webpack), TypeScript, nem backend/local DB. O desenvolvimento roda 100% no navegador contra a API externa.

## 2. Setup Passo a Passo
1. **Instalar pré-requisitos** listados acima (Node.js + Git + VS Code).
2. **Clonar o repositório:**
   ```bash
   git clone <url-do-repo> aegis1
   cd aegis1
   ```
3. **Nenhum arquivo `.env.example` existe** — variáveis de ambiente de build não se aplicam (sem build step). A URL da API backend é injetada em tempo de build via variável pública (ex.: `VITE_API_BASE_URL`) ou configurada no `index.html` servido pelo CDN. Para desenvolvimento local, edite `frontend/src/services/api.js` (constante `API_BASE_URL`) ou use um proxy de dev (ver seção 5).
4. **Instalar ferramentas de desenvolvimento (opcional, recomendado):**
   ```bash
   # Na raiz do projeto (criar package.json se não existir)
   npm init -y
   npm install -D eslint prettier @eslint/js eslint-plugin-import eslint-plugin-promise serve
   # Ou com pnpm:
   pnpm init -y
   pnpm add -D eslint prettier @eslint/js eslint-plugin-import eslint-plugin-promise serve
   ```
5. **Subir servidor estático local** (escolha uma opção):
   - **Opção A (npx, sem instalar):** `npx serve frontend -l 3000 --cors`
   - **Opção B (Python built-in):** `cd frontend && python -m http.server 3000`
   - **Opção C (VS Code Live Server):** Botão direito em `frontend/index.html` → "Open with Live Server".
   - **Opção D (Node http-server global):** `npm i -g http-server && http-server frontend -p 3000 -c-1`
6. **Acessar a aplicação:** `http://localhost:3000` (ou porta indicada pela ferramenta).
7. **Configurar proxy para API backend (opcional, evita CORS):** Se o backend não permite CORS do `localhost:3000`, use `serve` com proxy ou configure o backend para aceitar `Origin: http://localhost:3000`. Exemplo com `serve`:
   ```bash
   npx serve frontend -l 3000 --cors --proxy "/api:https://api.aegis1.example.com"
   ```
   Ajuste `frontend/src/services/api.js` para chamar `/api/...` em vez da URL absoluta.

## 3. Variáveis de Ambiente
| Variável | Obrigatória | Descrição | Valor de exemplo |
| :--- | :--- | :--- | :--- |
| `API_BASE_URL` | Sim (em produção) | URL base da API REST backend (HTTPS). Em dev local, pode ser sobrescrita no `api.js` ou via proxy. | `https://api.aegis1.example.com` |
| `TELEMETRY_DSN` | Não | DSN do Sentry/Datadog para error tracking (injeção no build via CDN). | `https://xxx@sentry.io/123` |
| `FEATURE_FLAGS` | Não | JSON string com flags de feature (ex.: `{"newCostFlow":true}`). Injetada no `index.html` via `window.__AEGIS1_CONFIG__`. | `{"newCostFlow":true}` |

> **Observação:** Como não há bundler, variáveis **não** são lidas de `.env` em tempo de execução. Valores de produção são injetados no `index.html` pelo pipeline de CDN (ex.: `sed` no build, CloudFront Function, Cloudflare Worker). Para desenvolvimento local, edite diretamente `frontend/src/services/api.js` ou use o proxy da seção 2.

## 4. Secure Command Sandbox Recipes
> Comandos permitidos para automação/agentes, com limites de segurança explícitos.

| Target Tool | Command Type | Recipe | Timeout | Restrições |
| :--- | :--- | :--- | :--- | :--- |
| `npm` / `pnpm` | `install` | `npm install --no-audit --prefer-offline` | 60s | Acesso apenas ao registry configurado (`npm config get registry`); sem `npm exec`/`npx` de pacotes não listados em `devDependencies`. |
| `npx` | `serve` | `npx serve frontend -l 3000 --cors` | 10s (startup) | Bind apenas em `127.0.0.1:3000`; sem exposição em `0.0.0.0`. |
| `python` | `http.server` | `python -m http.server 3000 --bind 127.0.0.1` | 10s (startup) | Diretório restrito a `frontend/`; sem CGI. |
| `eslint` | `lint` | `npx eslint frontend/src --ext .js --max-warnings=0` | 30s | Lê apenas arquivos do workspace; sem `--fix` automático em pipeline. |
| `prettier` | `format` | `npx prettier --check frontend/src` | 20s | Modo check-only; `--write` apenas em PR local. |
| `jest` / `vitest` | `test` | `npx vitest run --reporter=verbose` | 120s | Quando configurado (NFR-M02); sem watch mode em CI. |

## 5. Serviços Locais & Portas
| Serviço | Porta | Descrição |
| :--- | :--- | :--- |
| **Frontend (static server)** | 3000 (configurável) | Servidor estático servindo `frontend/` (index.html, JS, CSS, assets). |
| **Backend API (externo)** | 443 (HTTPS) | **Não roda localmente** — aponta para ambiente de dev/staging do backend (fora deste repositório). |
| **Proxy local (opcional)** | 3001 | Se usado `serve --proxy` ou outro proxy (ex.: `http-proxy-middleware`) para contornar CORS. |

> **Nenhum banco de dados, cache, message broker, ou serviço auxiliar roda localmente** — o diagnóstico confirma: *"nenhum motor de banco conhecido encontrado nas dependências"*, *"projeto não tem src-tauri/Cargo.toml"*.

## 6. Environment Verification Checklist
- [ ] `npx serve frontend -l 3000` (ou equivalente) inicia sem erros e serve `index.html` em `http://localhost:3000`.
- [ ] Console do navegador **não mostra erros de carregamento de módulos** (ES Modules via `<script type="module">`).
- [ ] Chamada de rede para `API_BASE_URL` (ou proxy `/api`) retorna 200/401 (não 0/CORS error/ERR_CONNECTION_REFUSED).
- [ ] `npx eslint frontend/src --ext .js` executa sem erros (após configurar ESLint com `no-console: error` — NFR-M03).
- [ ] `npx prettier --check frontend/src` passa (após configurar Prettier).
- [ ] `npm test` / `npx vitest run` executa e passa **quando suite de testes for implementada** (NFR-M02: ≥ 80% cobertura em `api.js` e fluxos de ordem) — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.
- [ ] Variáveis sensíveis (`TELEMETRY_DSN`) **não** estão commitadas; `.env*` no `.gitignore` (se vier a existir).
- [ ] CSP headers funcionam (testar via `serve` com headers customizados ou extensão VS Code "Live Server" com config de headers).

## 7. Troubleshooting Comum
| Sintoma | Causa Provável | Solução |
| :--- | :--- | :--- |
| **Erro CORS** ao chamar API | Backend não permite `Origin: http://localhost:3000` | 1) Configurar backend para aceitar origin de dev. 2) Usar proxy local (`serve --proxy "/api:https://api.backend"`). 3) Ajustar `api.js` para chamar `/api/...`. |
| **`TypeError: Failed to fetch` / Network Error** | API_BASE_URL incorreta, backend down, ou proxy mal configurado | Verificar `frontend/src/services/api.js` (linha ~10: `const API_BASE_URL`). Testar endpoint no curl/Postman. |
| **Módulos ES não carregam (`ERR_UNKNOWN_URL_SCHEME` ou 404 em `.js`)** | Servidor não serve `Content-Type: application/javascript` ou paths incorretos | Usar `serve` / `http-server` / Live Server (não `file://`). Verificar `index.html` usa `<script type="module" src="/src/main.js">` com paths relativos à raiz servida. |
| **`console.error`/`console.log` aparecem em produção** | Código não limpo (diagnóstico: `api.js:26,49,52`) | Remover chamadas `console.*` de `api.js`; configurar ESLint `no-console: error` no CI (NFR-M03, NFR-SEC04). |
| **Complexidade ciclomática alta em `api.js:request` (13)** | Função monolítica (diagnóstico AST) | Refatorar em `withTimeout`, `withRetry`, `withAuth`, `parseResponse`, `handleError` (NFR-M01). |
| **Token JWT não persiste / refresh falha** | `localStorage` bloqueado (modo privado), ou endpoint de refresh indisponível | Testar em janela normal; verificar `authInterceptor` em `api.js`; confirmar backend de auth acessível. |
| **Bundle > 150 kB gzipped** | Sem code-splitting (NFR-P03) | Implementar dynamic `import()` por rota + roteador leve (NFR-P03, Future Evolution #3). |

## 8. Comandos Úteis
| Comando | Descrição |
| :--- | :--- |
| `npx serve frontend -l 3000 --cors` | Sobe servidor estático local com CORS habilitado (dev rápido). |
| `cd frontend && python -m http.server 3000` | Alternativa sem Node.js (Python built-in). |
| `npx eslint frontend/src --ext .js --max-warnings=0` | Lint rigoroso (configurar `.eslintrc.cjs` com `no-console: error`, `complexity: [error, 10]`). |
| `npx prettier --write frontend/src` | Formata código (após configurar `.prettierrc`). |
| `npx vitest run` | Executa testes (quando configurado — NFR-M02). |
| `git status && git diff` | Verifica alterações locais antes de commit. |
| `curl -I https://api.aegis1.example.com/health` | Testa conectividade com backend (ajustar URL). |

---

### Observações Finais
- **Este documento reflete o estado real do codebase** (diagnóstico determinístico): 15 arquivos JS, 627 LOC, zero build, zero dependências de dev, zero backend local.
- **Itens marcados `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`** correspondem a práticas recomendadas pelos NFRs (testes, code-splitting, CSP, telemetria) que **ainda não estão implementadas** no repositório.
- **Próximos passos obrigatórios** (derivados do SAD/NFR): configurar `package.json` com scripts `lint`, `test`, `format`; adicionar ESLint/Prettier/Vitest; remover `console.*` de `api.js`; refatorar `request` (complexidade 13 → ≤10); implementar code-splitting por rota.