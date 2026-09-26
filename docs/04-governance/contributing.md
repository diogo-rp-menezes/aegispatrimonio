# Contributing & Onboarding Guide — Aegis1

> Bem-vindo(a)! Este guia leva você do zero ao primeiro PR mergeado no **Aegis1** — uma aplicação frontend vanilla JS/ES Modules servida estaticamente, sem build step, que consome uma API REST externa.

---

## 1. Primeiros Passos

### 1.1 Pré-requisitos Obrigatórios
| Ferramenta | Versão Mínima | Finalidade |
| :--- | :--- | :--- |
| **Git** | 2.40+ | Controle de versão |
| **VS Code** (recomendado) | Latest | Editor com extensões: *ESLint*, *Prettier*, *Live Server* |
| **Navegador** | Chrome/Edge/Firefox recentes | DevTools para debug de ES Modules, CSP, Network |
| **Node.js** | 18.x+ | **Opcional** — apenas para ferramentas de dev (lint, testes, servidor estático `serve`) |
| **pnpm** ou **npm** | pnpm 8+ / npm 9+ | **Opcional** — gerenciador para instalar ferramentas de dev |

> **Nota:** O repositório **não possui** `package.json` com scripts, `devDependencies`, bundler (Vite/Webpack), TypeScript, nem backend/banco local. O desenvolvimento roda 100% no navegador contra a API externa.

### 1.2 Clone e Setup Inicial
```bash
# 1. Clonar
git clone <url-do-repo> aegis1
cd aegis1

# 2. (Opcional) Inicializar package.json para ferramentas de dev
npm init -y
# ou
pnpm init -y

# 3. (Opcional) Instalar ferramentas de qualidade de código
npm install -D eslint prettier @eslint/js eslint-plugin-import eslint-plugin-promise serve
# ou
pnpm add -D eslint prettier @eslint/js eslint-plugin-import eslint-plugin-promise serve
```

### 1.3 Configurar Variáveis de Ambiente
**Não existe `.env.example`** — variáveis de build não se aplicam (sem build step).  
A URL da API backend é definida em `frontend/src/services/api.js`:

```js
// frontend/src/services/api.js (linha ~10)
const API_BASE_URL = 'https://api.aegis1.example.com'; // ← altere para dev/staging
```

**Alternativa (recomendada para evitar CORS):** use proxy local (ver seção 1.5).

### 1.4 Subir Servidor Estático Local
Escolha **uma** opção:

| Opção | Comando | Porta Padrão |
| :--- | :--- | :--- |
| **A. npx serve** (rápido, com CORS) | `npx serve frontend -l 3000 --cors` | 3000 |
| **B. Python built-in** (sem Node) | `cd frontend && python -m http.server 3000` | 3000 |
| **C. VS Code Live Server** | Botão direito em `frontend/index.html` → "Open with Live Server" | 5500 |
| **D. http-server global** | `npm i -g http-server && http-server frontend -p 3000 -c-1` | 3000 |

Acesse: `http://localhost:3000` (ou porta indicada).

### 1.5 Proxy Local para API (Evita CORS)
Se o backend não permite `Origin: http://localhost:3000`:

```bash
# Com serve (opção A acima)
npx serve frontend -l 3000 --cors --proxy "/api:https://api.aegis1.example.com"
```

Ajuste `frontend/src/services/api.js` para chamar `/api/...` em vez da URL absoluta.

### 1.6 Checklist de Verificação do Ambiente
- [ ] Servidor estático inicia sem erros e serve `index.html` em `http://localhost:3000`
- [ ] Console do navegador **não mostra erros de carregamento de módulos** (ES Modules via `<script type="module">`)
- [ ] Chamada de rede para `API_BASE_URL` (ou proxy `/api`) retorna 200/401 (não 0/CORS error/ERR_CONNECTION_REFUSED)
- [ ] `npx eslint frontend/src --ext .js` executa sem erros **(após configurar ESLint — ver seção 5)**
- [ ] `npx prettier --check frontend/src` passa **(após configurar Prettier — ver seção 5)**
- [ ] `npm test` / `npx vitest run` executa e passa **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA: suite de testes ainda não implementada; NFR-M02 exige ≥ 80% cobertura em `api.js` e fluxos de ordem]**
- [ ] Variáveis sensíveis (`TELEMETRY_DSN`) **não** estão commitadas; `.env*` no `.gitignore` (se vier a existir)
- [ ] CSP headers funcionam (testar via `serve` com headers customizados ou extensão VS Code "Live Server" com config de headers)

---

## 2. Estrutura do Projeto

```
aegis1/
├── frontend/                    # Raiz servida estaticamente
│   ├── index.html               # Entry point HTML (carrega main.js como module)
│   ├── src/
│   │   ├── main.js              # Bootstrap da aplicação (importa rotas, componentes)
│   │   ├── services/
│   │   │   └── api.js           # Cliente HTTP central (fetch + auth + retry + timeout) — 33 funções, 627 LOC total
│   │   ├── components/          # Componentes UI vanilla JS (se existirem)
│   │   ├── pages/               # Páginas/rotas (se existirem)
│   │   └── utils/               # Helpers puros (formatação, validação, etc.)
│   ├── assets/                  # Imagens, fontes, ícones estáticos
│   └── styles/                  # CSS global / variáveis / reset
├── .gitignore                   # Deve ignorar node_modules/, .env*, dist/, coverage/
├── README.md                    # Visão geral do produto (fora do escopo deste doc)
└── CONTRIBUTING.md              # Este arquivo
```

> **Diagnóstico real:** 15 arquivos `.js` sob `frontend/`, 33 funções, 627 LOC. Zero classes. Zero arquivos de teste, config, ou backend.

---

## 3. Padrões de Commit

### 3.1 Convenção Adotada: **Conventional Commits 1.0** (em português)
```
<tipo>(<escopo>): <descrição curta no imperativo>

[corpo opcional: motivação, contexto, breaking changes]

[rodapé opcional: Refs #123, Closes #456]
```

### 3.2 Tipos Permitidos
| Tipo | Quando Usar |
| :--- | :--- |
| `feat` | Nova funcionalidade visível ao usuário |
| `fix` | Correção de bug |
| `docs` | Alterações apenas em documentação |
| `refactor` | Refatoração sem mudança de comportamento |
| `test` | Adição/alteração de testes |
| `chore` | Tarefas de manutenção (config, deps, tooling) |
| `perf` | Melhoria de performance |
| `security` | Correção de vulnerabilidade |
| `style` | Formatação, lint, sem mudança lógica |

### 3.3 Escopos Sugeridos (baseados na estrutura real)
- `api` — `frontend/src/services/api.js`
- `ui` — componentes/páginas em `frontend/src/components/`, `frontend/src/pages/`
- `config` — ESLint, Prettier, Vitest, package.json, scripts
- `docs` — README, CONTRIBUTING, arquitetura
- `devops` — CI/CD, proxy, CDN, variáveis de ambiente

### 3.4 Exemplos
```
feat(api): adiciona interceptador de refresh token automático
fix(api): corrige timeout em requisições longas (>30s)
refactor(api): extrai withRetry e withTimeout de request() (complexidade 13→8)
docs(onboarding): adiciona seção de proxy local para CORS
chore(config): adiciona ESLint + Prettier + Vitest ao package.json
test(api): cobre fluxo de erro 401 + refresh token
```

### 3.5 Regras Adicionais
- **Commits atômicos:** uma mudança lógica por commit.
- **Mensagens em português** (padrão da equipe).
- **Máx. 72 chars** na linha de assunto; corpo quebrado em 80 chars.
- **Breaking changes:** indicar `BREAKING CHANGE:` no corpo ou `!` após tipo/escopo (`feat(api)!: ...`).

---

## 4. Fluxo de Contribuição (Branching & PRs)

### 4.1 Branching Model: **GitHub Flow Simplificado**
```
main (protegida, deploy automático em CDN)
  │
  ├── feature/nome-da-feature     # novas funcionalidades
  ├── fix/nome-do-bug             # correções
  ├── refactor/nome-da-refatoracao
  ├── docs/nome-da-doc
  └── chore/nome-da-tarefa
```

### 4.2 Passo a Passo
1. **Atualize `main`:** `git checkout main && git pull origin main`
2. **Crie branch:** `git checkout -b feature/nome-da-feature` (use tipo/escopo do commit)
3. **Desenvolva** seguindo padrões de código (seção 5) e testes (seção 5.3).
4. **Rode qualidade localmente** (antes de push):
   ```bash
   npx eslint frontend/src --ext .js --max-warnings=0
   npx prettier --check frontend/src
   npx vitest run                 # quando configurado [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
   ```
5. **Commit & Push:** `git push -u origin feature/nome-da-feature`
6. **Abra Pull Request** contra `main` com template (abaixo).
7. **Solicite revisão** de **pelo menos 1 revisor** (owner ou maintainer).
8. **Enderece comentários** (novos commits na mesma branch).
9. **Merge via Squash** após CI verde e aprovação — mantém histórico limpo.

### 4.3 Template de PR Obrigatório
```markdown
## O que este PR faz
<!-- Descrição clara e concisa da mudança -->

## Como testar
<!-- Passos manuais ou comandos para validar a mudança -->
1. ...
2. ...

## Checklist
- [ ] Testes adicionados/atualizados (ou justificativa se não aplicável)
- [ ] Documentação atualizada (README, CONTRIBUTING, comentários de código)
- [ ] Sem breaking changes (ou documentado em "Breaking Changes" abaixo)
- [ ] Lint e formatação passam localmente
- [ ] Console limpo (sem `console.log/error` em código de produção)

## Breaking Changes (se houver)
<!-- Descreva o que quebra e migração necessária -->
```

---

## 5. Padrões de Código

### 5.1 Linguagem & Runtime
- **JavaScript Vanilla (ES2022+)** — ES Modules nativos (`<script type="module">`).
- **Sem TypeScript, sem bundler, sem transpilação.**
- **Runtime alvo:** Navegadores modernos (últimas 2 versões major de Chrome, Edge, Firefox, Safari).

### 5.2 Estilo & Lint (Obrigatório — Configurar no Primeiro PR de Tooling)
> **Estado atual:** **Não configurado**. O diagnóstico encontrou `console.error/log` em `api.js:26,49,52` e complexidade ciclomática 13 em `request()`.

**Configuração alvo (`.eslintrc.cjs` na raiz):**
```js
module.exports = {
  root: true,
  env: { browser: true, es2022: true },
  parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
  plugins: ['import', 'promise'],
  extends: ['eslint:recommended', 'plugin:import/recommended', 'plugin:promise/recommended'],
  rules: {
    'no-console': 'error',                    // NFR-M03, NFR-SEC04
    'complexity': ['error', 10],              // NFR-M01: request() hoje = 13
    'max-depth': ['error', 4],
    'max-lines-per-function': ['error', 50],
    'import/order': ['error', { 'newlines-between': 'always' }],
    'promise/always-return': 'error',
    'promise/no-return-wrap': 'error',
    'promise/param-names': 'error'
  },
  overrides: [
    { files: ['**/*.test.js'], env: { jest: true } }
  ]
};
```

**Prettier (`.prettierrc`):**
```json
{
  "singleQuote": true,
  "trailingComma": "es5",
  "printWidth": 100,
  "tabWidth": 2,
  "semi": true
}
```

**Scripts no `package.json` (a criar):**
```json
{
  "scripts": {
    "lint": "eslint frontend/src --ext .js --max-warnings=0",
    "format": "prettier --write frontend/src",
    "format:check": "prettier --check frontend/src",
    "test": "vitest run --reporter=verbose",
    "test:watch": "vitest",
    "dev": "serve frontend -l 3000 --cors"
  }
}
```

### 5.3 Convenções de Nomenclatura
| Entidade | Convenção | Exemplo |
| :--- | :--- | :--- |
| Arquivos JS | `kebab-case.js` | `api.js`, `order-form.js` |
| Funções/Variáveis | `camelCase` | `fetchOrders`, `apiBaseUrl` |
| Constantes globais | `UPPER_SNAKE_CASE` | `API_BASE_URL`, `DEFAULT_TIMEOUT_MS` |
| Componentes (se houver) | `PascalCase` | `OrderCard`, `CostBreakdown` |
| Eventos customizados | `kebab-case` | `order:created`, `auth:token-refreshed` |

### 5.4 Testes Obrigatórios [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
> **NFR-M02 exige ≥ 80% cobertura em `api.js` e fluxos de ordem.**  
> **Stack de teste alvo:** Vitest (rápido, ESM nativo, compatível com navegador via JSDOM/happy-dom).

**O que testar (mínimo):**
- `api.js`: `request()` — sucesso, 401+refresh, 4xx/5xx, timeout, abort, retry.
- Fluxos de ordem: criação, listagem, cancelamento, cálculo de custo.
- Utils puros: formatação de moeda, datas, validação de CPF/CNPJ.

**Estrutura sugerida:**
```
frontend/
├── src/
│   └── services/
│       ├── api.js
│       └── api.test.js          # co-locado
└── test/
    ├── setup.js                 # globals, mocks (fetch, localStorage)
    └── utils/
        └── currency.test.js
```

---

## 6. Revisão de Código (Code Review Guidelines)

### 6.1 O Que Revisores Devem Verificar
| Critério | Perguntas-Chave |
| :--- | :--- |
| **Corretude** | Resolve o problema? Edge cases cobertos? Tratamento de erro adequado? |
| **Legibilidade** | Nomes claros? Funções pequenas (<50 linhas)? Comentários *por que*, não *o que*? |
| **Testes** | Novos testes para features/fixes? Cobertura ≥ 80% nos arquivos alterados? |
| **Segurança** | Sem segredos no código? Sanitização de entrada? CSP compatível? Sem `eval`/`innerHTML` inseguro? |
| **Performance** | Evita re-renders desnecessários? `request()` usa `AbortController`? Payloads mínimos? |
| **Padrões** | Segue ESLint/Prettier? Commits Conventional? Escopo correto? |

### 6.2 Tempo de Resposta Esperado
- **Primeira revisão:** ≤ 1 dia útil após PR aberto.
- **Revisões subsequentes:** ≤ 4 horas úteis.

### 6.3 Como Dar Feedback Construtivo
- **Tom:** Respeitoso, impessoal, focado no código.
- **Sugestões vs. Bloqueios:** Use `Suggestion:` para melhorias opcionais; `Must fix:` para bloqueios (bugs, segurança, breaking changes não documentados).
- **Exemplo bom:**  
  > `Suggestion:` Extrair `withRetry` para função separada reduz complexidade de `request` de 13 para ≤10 (NFR-M01).  
  > `Must fix:` `console.log` em `api.js:49` vaza token em produção — remover antes do merge.

---

## 7. Reportando Bugs

### 7.1 Onde Reportar
**GitHub Issues** do repositório (aba *Issues* → *New Issue* → *Bug Report*).

### 7.2 Template Obrigatório
```markdown
## Descrição
<!-- O que acontece vs. o que deveria acontecer -->

## Passos para Reproduzir
1. ...
2. ...
3. ...

## Comportamento Esperado
<!-- Descrição clara -->

## Comportamento Atual
<!-- Logs, screenshots, vídeo (se aplicável) -->

## Ambiente
- OS: [ex: Windows 11, macOS 14, Ubuntu 22.04]
- Navegador: [ex: Chrome 126, Firefox 127]
- Versão do frontend: [commit hash ou tag]
- Backend: [dev/staging/prod + versão se conhecida]

## Contexto Adicional
<!-- Configurações especiais, proxy, flags de feature, etc. -->
```

---

## 8. Propondo Novas Funcionalidades

### 8.1 Processo: **RFC Leve (Issue + Discussão)**
1. Abra **Issue** com label `rfc` ou `feature-proposal`.
2. Preencha:
   - **Problema:** Qual dor do usuário/negócio resolve?
   - **Solução proposta:** Fluxo, API, UI, mudanças de dados.
   - **Alternativas consideradas:** Por que esta é a melhor?
   - **Impacto:** Performance, bundle size, breaking changes, migração.
   - **Esforço estimado:** T-shirt size (XS/S/M/L/XL).
3. Discussão assíncrona (mínimo 2 dias úteis para comentários).
4. Aprovação de **1 maintainer** → autorizado a implementar.

> **Exemplo real:** "Code-splitting por rota + roteador leve" (Future Evolution #3 do SAD) — requer RFC antes de iniciar.

---

## 9. Código de Conduta

**Referência:** `CODE_OF_CONDUCT.md` (raiz do repositório — **não existe ainda**; criar baseado no [Contributor Covenant v2.1](https://www.contributor-covenant.org/version/2/1/code_of_conduct/)).

### Resumo Operacional
| Princípio | Ação Prática |
| :--- | :--- |
| **Respeito** | Linguagem inclusiva; sem ataques pessoais; assumir boa intenção. |
| **Inclusão** | Acolher contribuições de qualquer nível de experiência; mentoria ativa. |
| **Profissionalismo** | Feedback técnico, não pessoal; resolver conflitos em privado primeiro. |
| **Denúncia** | Canal: `conduct@aegis1.example.com` (ou issue privada para maintainers). Resposta em ≤ 48h. |

> **Ação:** Primeiro PR de *chore* deve adicionar `CODE_OF_CONDUCT.md` e linkar aqui.

---

## 10. Contatos & Suporte

| Canal | Uso | Tempo de Resposta Esperado |
| :--- | :--- | :--- |
| **GitHub Issues** | Bugs, features, RFCs, dúvidas técnicas públicas | ≤ 2 dias úteis (triage) |
| **Slack/Discord da equipe** (se houver) | Dúvidas rápidas, pair programming, alinhamento diário | Tempo real (horário comercial) |
| **Email da equipe** (`team@aegis1.example.com`) | Questões sensíveis (segurança, dados, conduta) | ≤ 24h |
| **Documentação viva** | `README.md`, `CONTRIBUTING.md`, `docs/architecture/` | Sempre atualizada no PR que muda comportamento |

---

## Apêndice A — Comandos Úteis de Referência Rápida

| Comando | Descrição |
| :--- | :--- |
| `npx serve frontend -l 3000 --cors` | Sobe servidor estático local com CORS (dev rápido) |
| `cd frontend && python -m http.server 3000` | Alternativa sem Node.js |
| `npx eslint frontend/src --ext .js --max-warnings=0` | Lint rigoroso (configurar `.eslintrc.cjs` primeiro) |
| `npx prettier --write frontend/src` | Formata código (configurar `.prettierrc` primeiro) |
| `npx vitest run` | Executa testes (quando configurado — NFR-M02) |
| `git status && git diff` | Verifica alterações locais antes de commit |
| `curl -I https://api.aegis1.example.com/health` | Testa conectividade com backend (ajustar URL) |

---

## Apêndice B — Troubleshooting Comum (Resumo)

| Sintoma | Causa Provável | Solução Rápida |
| :--- | :--- | :--- |
| **Erro CORS** | Backend não permite `localhost:3000` | Usar proxy `serve --proxy "/api:https://api.backend"` + ajustar `api.js` |
| **`TypeError: Failed to fetch`** | `API_BASE_URL` errada / backend down / proxy | Verificar `api.js:10`; testar no `curl`/Postman |
| **Módulos ES não carregam** | Servidor não serve `application/javascript` ou paths errados | Usar `serve`/`http-server`/Live Server (não `file://`); checar `<script type="module" src="/src/main.js">` |
| **`console.*` em produção** | Código não limpo (`api.js:26,49,52`) | Remover `console.*`; ESLint `no-console: error` no CI |
| **Complexidade `request()` = 13** | Função monolítica | Refatorar em `withTimeout`, `withRetry`, `withAuth`, `parseResponse`, `handleError` (NFR-M01) |
| **Token JWT não persiste** | `localStorage` bloqueado (modo privado) / refresh down | Testar janela normal; checar `authInterceptor` em `api.js` |
| **Bundle > 150 kB gzipped** | Sem code-splitting (NFR-P03) | Dynamic `import()` por rota + roteador leve (Future Evolution #3) |

---

> **Este documento reflete o estado real do codebase** (diagnóstico determinístico: 15 arquivos JS, 627 LOC, zero build, zero deps de dev, zero backend local).  
> **Itens marcados `[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]`** correspondem a práticas recomendadas pelos NFRs (testes, code-splitting, CSP, telemetria, tooling) que **ainda não estão implementadas** no repositório.  
> **Próximos passos obrigatórios** (derivados do SAD/NFR): configurar `package.json` com scripts `lint`, `test`, `format`; adicionar ESLint/Prettier/Vitest; remover `console.*` de `api.js`; refatorar `request` (complexidade 13 → ≤10); implementar code-splitting por rota.