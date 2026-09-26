# Contributing & Onboarding Guide — Aegis1

> **Versão:** 2.0 · **Status:** Draft · **Owner:** Tech Leads / DevOps
> **Base:** System Architecture v2.0 + Dev Environment v2.0 + NFR v2.0 + Análise AST Java Completa
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/Vite/PWA) + Infra (Docker/K8s/TestContainers)

---

## 1. Primeiros Passos (Zero to Contributing)

### 1.1 Pré-requisitos Obrigatórios

| Ferramenta | Versão Mínima | Finalidade |
| :--- | :--- | :--- |
| **JDK** | **21 LTS** (Temurin/Eclipse Adoptium) | Compilar/rodar backend Spring Boot |
| **Maven** | **3.9+** (Wrapper `mvnw` incluído) | Build backend, gerenciar dependências |
| **Node.js** | **20 LTS** (Iron) | Frontend build (Vite), ferramentas dev |
| **pnpm** | **9+** | Gerenciador pacotes frontend (rápido, disk-efficient) |
| **Docker** | **24+** (Desktop/Engine) | TestContainers, Docker Compose, Build images |
| **Docker Compose** | **v2.20+** (plugin Docker) | Orquestração local (MySQL, Redis, Backend, Frontend) |
| **Git** | **2.40+** | Versionamento, hooks, CI |
| **kubectl** | **1.28+** | Deploy/debug K8s local (kind/k3d) ou remoto |
| **kind** ou **k3d** | **0.22+** / **5.5+** | Cluster K8s local para testes integração/infra |
| **Helm** | **3.12+** | Charts K8s (ingress, cert-manager, prometheus, etc.) |
| **VS Code** | **Latest** | IDE recomendada |

### 1.2 VS Code Extensions (Obrigatórias)

```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vmware.vscode-spring-boot",
    "redhat.vscode-yaml",
    "ms-kubernetes-tools.vscode-kubernetes-tools",
    "vue.volar",
    "dbaeumer.vscode-eslint",
    "esbenp.prettier-vscode",
    "ms-azuretools.vscode-docker",
    "github.vscode-github-actions",
    "sonarsource.sonarlint-vscode",
    "editorconfig.editorconfig"
  ]
}
```

### 1.3 Clone & Setup Inicial

```bash
git clone https://github.com/diogo-rp-menezes/aegis1.git
cd aegis1

# Git hooks (pre-commit: lint, format, test)
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
set -e
echo "🔍 Running pre-commit checks..."
./mvnw checkstyle:check spotbugs:check -q -DskipTests || exit 1
cd frontend && pnpm lint || exit 1 && pnpm format:check || exit 1 && cd ..
echo "✅ Pre-commit checks passed"
EOF
chmod +x .git/hooks/pre-commit

# direnv (opcional, recomendado)
cat > .envrc << 'EOF'
source_env_if_exists .env.local
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "/usr/lib/jvm/temurin-21-jdk")
export PNPM_HOME="$HOME/.local/share/pnpm"
export PATH="$PNPM_HOME:$PATH"
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
export TESTCONTAINERS_REUSE_ENABLE=true
export TESTCONTAINERS_RYUK_DISABLED=true
EOF
direnv allow
```

### 1.4 Verificação de Ambiente (Definition of Ready para Dev)

- [ ] `./mvnw clean verify` passa (testes unitários + integração + quality gates)
- [ ] `cd frontend && pnpm install && pnpm lint && pnpm format:check && pnpm typecheck && pnpm test:coverage` passa
- [ ] `docker compose --profile dev up -d` sobe todos serviços saudáveis
- [ ] Backend acessível em `http://localhost:8080/actuator/health` → `{"status":"UP"}`
- [ ] Frontend acessível em `http://localhost:5173` → carrega sem erros console
- [ ] Login funciona: `POST /api/v1/auth/login` → retorna `accessToken` + `refreshToken` cookie
- [ ] Multi-tenancy: Header `X-Tenant-Id: 1` filtra dados corretamente
- [ ] Busca fuzzy: `GET /api/v1/busca?q=notebok` → retorna resultados com highlight
- [ ] PWA: Service Worker registrado (`Application → Service Workers` → `activated`)
- [ ] Observabilidade: `http://localhost:8080/actuator/prometheus` expõe métricas
- [ ] Testes contrato: `./mvnw test -Dtest=*ContractTest` + `cd frontend && pnpm pact:verify` passam
- [ ] Segurança: `./mvnw org.owasp:dependency-check-maven:check` → zero CVSS ≥ 7

---

## 2. Estrutura do Projeto (Resumo)

```
aegis1/
├── .github/workflows/         # GitHub Actions CI/CD
├── frontend/                  # Vue 3 + Vite + PWA
│   ├── src/
│   │   ├── main.ts            # Bootstrap (Pinia, Router, Axios, i18n)
│   │   ├── router/            # Vue Router (lazy-loaded + guards)
│   │   ├── stores/            # Pinia stores (auth, order, asset, search, ui, tenant)
│   │   ├── api/               # Axios instance + interceptors
│   │   ├── components/        # UI Kit (Button, Table, Modal, Form, Chart, QR, PDF)
│   │   ├── pages/             # Page components (Dashboard, Assets, Orders, etc.)
│   │   ├── composables/       # Vue Composition API utilities
│   │   ├── utils/             # Helpers (date, currency, format, permissions)
│   │   └── sw/                # Service Worker, IndexedDB, Background Sync
│   ├── vite.config.ts         # Vite + PWA Plugin (Workbox) + Proxy
│   └── package.json
├── src/main/java/com/aegis1/  # Backend Java
│   ├── config/                # Security, Auditoria, MultiTenancy, Scheduler, OpenAPI
│   ├── security/              # JwtTokenProvider, AegisShield, MultiTenancyFilter
│   ├── domain/                # Módulos: ativo, ordem, preventiva, preditiva, busca, cadastro, relatorio, auditoria, seguranca, lgpd
│   └── shared/                # Kernel compartilhado (exceptions, dto, util, events)
├── src/main/resources/
│   ├── db/migration/          # Flyway (V1__init.sql, V2__..., R__repeatable)
│   ├── templates/pdf/         # Thymeleaf templates (Termo, Etiquetas)
│   ├── application*.yml       # Configs
│   └── keys/                  # JWT keys (dev only)
├── k8s/                       # Kubernetes manifests (Kustomize overlays)
├── docker-compose*.yml        # Full stack local
├── Dockerfile*                # Multi-stage builds
├── pom.xml                    # Maven POM
└── .editorconfig              # EditorConfig
```

---

## 3. Padrões de Commit (Conventional Commits 1.0 — Português)

```
<tipo>(<escopo>): <descrição curta no imperativo>

[corpo opcional: motivação, contexto, breaking changes]

[rodapé opcional: Refs #123, Closes #456]
```

### Tipos Permitidos
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
| `ci` | Mudanças em CI/CD pipelines |

### Escopos Sugeridos
**Backend:** `ativo`, `ordem`, `preventiva`, `preditiva`, `busca`, `cadastro`, `relatorio`, `auditoria`, `seguranca`, `lgpd`, `auth`, `config`, `infra`
**Frontend:** `dashboard`, `assets`, `orders`, `preventiva`, `preditiva`, `busca`, `cadastros`, `admin`, `relatorios`, `lgpd`, `ui`, `pwa`, `auth`, `router`, `stores`
**Infra/Transversal:** `ci`, `docker`, `k8s`, `db`, `security`, `obs`, `docs`, `deps`

### Exemplos
```
feat(ativo): adiciona suporte a QR Code unitário + Termo PDF
fix(ordem): corrige validação BR-06 (evidência obrigatória na aprovação)
refactor(api): extrai retryLogic e timeoutHandler de request() (complexidade 13→8)
docs(arquitetura): atualiza ADR-003 com decisão Aegis Shield
chore(deps): atualiza Spring Boot 3.3.0 → 3.3.1
test(preditiva): cobre regressão linear OLS com IC 95%
security(auth): implementa rotação de refresh token (HttpOnly cookie)
perf(busca): adiciona cache Redis TTL 5min para queries fuzzy frequentes
ci(pipeline): adiciona contract tests Pact no stage validate
```

### Regras Adicionais
- Commits atômicos: uma mudança lógica por commit
- Mensagens em português (padrão da equipe)
- Máx. 72 chars na linha de assunto; corpo quebrado em 80 chars
- Breaking changes: `BREAKING CHANGE:` no corpo ou `!` após tipo/escopo
- Signed commits: `git commit -s` (DCO - Developer Certificate of Origin)

---

## 4. Fluxo de Contribuição (Branching & PRs)

### 4.1 Branching Model: GitHub Flow + Release Branches

```
main (protegida, deploy automático staging → prod via ArgoCD)
  │
  ├── develop (integração contínua, deploy automático staging)
  │     ├── feature/ativo-qr-pdf
  │     ├── fix/ordem-aprovacao-evidencia
  │     ├── refactor/api-request-complexity
  │     ├── docs/adr-aegis-shield
  │     └── chore/deps-spring-boot-331
  │
  ├── release/1.0.0 (stabilization, apenas fixes)
  │     ├── fix/release-1.0.0-login-refresh
  │     └── fix/release-1.0.0-fuzzy-search-cache
  │
  └── hotfix/1.0.1 (apenas main → prod urgente)
        └── fix/hotfix-1.0.1-jwt-key-rotation
```

### 4.2 Branch Naming Convention
```
<tipo>/<escopo>-<descrição-kebab-case>
# Exemplos:
feature/ativo-qr-code-pdf
fix/ordem-aprovacao-evidencia-obrigatoria
refactor/api-request-extract-retry-timeout
docs/adr-003-aegis-shield-decision
chore/deps-update-spring-boot-331
test/preditiva-regressao-linear-ols
security/auth-refresh-token-rotation
perf/busca-fuzzy-redis-cache-ttl
ci/github-actions-add-contract-tests
```

### 4.3 Passo a Passo do PR
1. Atualize `develop`: `git checkout develop && git pull origin develop`
2. Crie branch: `git checkout -b feature/ativo-qr-code-pdf`
3. Desenvolva seguindo padrões de código (seção 5) e testes (seção 6)
4. Rode qualidade localmente:
   ```bash
   ./mvnw checkstyle:check spotbugs:check test -DskipITs -q
   cd frontend && pnpm lint && pnpm format:check && pnpm typecheck && pnpm test:coverage
   ```
5. Commit & Push: `git push -u origin feature/ativo-qr-code-pdf`
6. Abra Pull Request contra `develop` com template
7. Solicite revisão de **pelo menos 1 revisor** (code owner do módulo)
8. CI deve passar (GitHub Actions: build, test, security, contract, bundle size)
9. Merge (Squash and merge) → `develop` → deploy automático staging via ArgoCD
10. Release: Quando `develop` estável → PR `develop` → `main` → tag `v1.0.0` → deploy prod

---

## 5. Padrões de Código

### 5.1 Backend (Java 21 + Spring Boot 3.3)

| Padrão | Ferramenta | Configuração |
| :--- | :--- | :--- |
| **Estilo** | Checkstyle | Google Java Style (`checkstyle.xml`) |
| **Bugs** | SpotBugs | `spotbugs.xml` (max rank 15) |
| **Code Smells** | PMD | `pmd-ruleset.xml` |
| **Complexidade** | Checkstyle/PMD | Ciclomática ≤ 10 por método; Classe ≤ 500 linhas |
| **Null Safety** | Annotations | `@NonNull` / `@Nullable` (Lombok `@NonNull` em construtores) |
| **Imutabilidade** | Records / `final` | Preferir `record` para DTOs; `final` em campos/parâmetros |
| **Exceções** | Custom Exceptions | `BusinessException` (409), `NotFoundException` (404), `ValidationException` (400) |
| **Transações** | `@Transactional` | Read-only para queries; `rollbackFor = Exception.class` em comandos |
| **Validação** | Bean Validation | `@Valid` em DTOs; `@NotNull`, `@Size`, `@Pattern`, custom validators |
| **Documentação** | JavaDoc | Público API (Controllers, Services, DTOs) — obrigatório |
| **Arquitetura** | ArchUnit | Testes: `ArchUnitTest` valida boundaries |

### 5.2 Frontend (Vue 3 + TypeScript + Vite)

| Padrão | Ferramenta | Configuração |
| :--- | :--- | :--- |
| **Lint** | ESLint | `eslint.config.js` (Airbnb/Standard + Vue + TypeScript + Prettier) |
| **Format** | Prettier | `.prettierrc` (single quote, trailing comma, printWidth 100) |
| **Types** | TypeScript | `tsconfig.json` (strict: true, noImplicitAny, strictNullChecks) |
| **Complexidade** | ESLint | `complexity: ["error", 10]` (ciclomática ≤ 10) |
| **Console** | ESLint | `no-console: "error"` (produção) / `warn` (dev) |
| **Componentes** | Vue 3 | `<script setup>` + Composition API + `defineProps`/`defineEmits` tipados |
| **Estado** | Pinia | Stores tipados (`defineStore` com `state`, `getters`, `actions` tipados) |
| **Roteamento** | Vue Router | Lazy-loaded routes + Guards (auth, permission, tenant) |
| **API Client** | Axios | Instância única + Interceptors (auth, error, tracing, retry) |
| **Testes** | Vitest + Vue Test Utils | Unit (stores, utils, composables) + Component (mount + props + events) |
| **E2E** | Cypress | Fluxos críticos (login, criar ativo, ordem completa, health check) |
| **Acessibilidade** | axe-core | `cy.injectAxe()` + `cy.checkA11y()` em testes E2E |

### 5.3 Database (Flyway + MySQL 8.0)

| Padrão | Regra |
| :--- | :--- |
| **Nomenclatura** | `V{versao}__{descricao}.sql` (ex.: `V1.0.1__add_health_check_table.sql`) |
| **Repeatable** | `R__{descricao}.sql` (views, functions, dados de referência) |
| **Undo** | `U{versao}__{descricao}.sql` (opcional, para rollback manual) |
| **Baseline** | `flyway baseline` em produção (versão inicial) |
| **Transacional** | Cada migration em transação (Flyway default) |
| **Naming** | `snake_case` tabelas/colunas; `UPPER_CASE` enums; FK: `fk_{tabela}_{coluna}` |
| **Índices** | `idx_{tabela}_{colunas}`; Trigram: `idx_{tabela}_trgm_{coluna}` |
| **Partitioning** | `REVINFO` por mês; `auditoria` por ano (se volume alto) |
| **Dados Sensíveis** | Nunca em migrations (usar Vault/SealedSecrets + `application-prod.yml`) |

---

## 6. Testes (Quality Gates)

### 6.1 Pirâmide de Testes
```
         /\
        /  \  E2E (Cypress) — 5-10% — Fluxos críticos user-facing
       /----\ Integration (TestContainers) — 20-30% — Controllers, Services, Repositories, Security
      /------\ Unit (JUnit 5 / Vitest) — 60-70% — Domain logic, Services, Utils, Stores, Composables
     /________\
```

### 6.2 Gates de CI (Obrigatórios para Merge)

| Gate | Backend | Frontend | Threshold |
| :--- | :--- | :--- | :--- |
| **Build** | `./mvnw compile` | `pnpm build` | Sucesso |
| **Unit Tests** | `./mvnw test -DskipITs` | `pnpm test` | Pass |
| **Integration Tests** | `./mvnw verify -DskipUnitTests=false` | — | Pass (TestContainers) |
| **Contract Tests** | `./mvnw test -Dtest=*ContractTest` | `pnpm pact:verify` | Pass |
| **E2E Tests** | — | `pnpm cypress:run` | Pass (fluxos críticos) |
| **Coverage** | JaCoCo | Vitest (V8) | **≥ 80%** (instruções/linhas) |
| **Static Analysis** | Checkstyle + SpotBugs + PMD | ESLint + Prettier + TypeScript | Zero errors |
| **Security Scan** | OWASP Dep Check (CVSS ≥ 7) | npm audit / Snyk | Zero critical/high |
| **Bundle Size** | — | `gzip dist/assets/*.js` | **≤ 150 kB** gzipped |
| **Accessibility** | — | axe-core (Cypress) | Zero violations WCAG 2.1 AA |

### 6.3 TestContainers Configuration (`.testcontainers.properties`)
```properties
testcontainers.reuse.enable=true
testcontainers.ryuk.disabled=true
```

### 6.4 Convenções de Nomenclatura de Testes
| Tipo | Padrão | Exemplo |
| :--- | :--- | :--- |
| **Unit** | `*Test.java` / `*.test.ts` | `AtivoServiceTest.java`, `orderStore.test.ts` |
| **Integration** | `*ControllerIT.java` / `*ServiceIT.java` | `AtivoControllerIT.java`, `OrdemServiceIT.java` |
| **Contract** | `*ContractTest.java` / `*.pact.test.ts` | `AtivoControllerContractTest.java` |
| **E2E** | `*.cy.ts` | `order-complete-flow.cy.ts` |

---

## 7. Segurança no Desenvolvimento

### 7.1 Secrets Management
- **NUNCA** commitar segredos no Git
- **Local:** `.env.local` (gitignored) + `direnv` + Vault CLI
- **CI/CD:** GitHub Actions Secrets / Vault / AWS Secrets Manager
- **K8s:** SealedSecrets / External Secrets Operator / Vault Agent Injector
- **Rotação:** Chaves JWT (RS256) rotacionadas a cada 90 dias via cert-manager / Vault

### 7.2 Dependency Security
```bash
# Backend: OWASP Dependency Check (CI)
./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7

# Frontend: npm audit + Snyk (CI)
cd frontend && pnpm audit --prod --audit-level=high
```

### 7.3 Code Security (SAST/DAST)
- **SAST:** SpotBugs + Semgrep + CodeQL (GitHub Actions)
- **DAST:** OWASP ZAP (staging, pré-deploy prod)
- **Secrets Scan:** TruffleHog / GitLeaks (pre-commit + CI)
- **Container Scan:** Trivy (imagens Docker) — fail se CRITICAL/HIGH

---

## 8. Documentação

### 8.1 Obrigatória por PR
- **OpenAPI:** Atualizar `src/main/resources/openapi/openapi.yaml` se endpoints alterados
- **ADR:** Nova ADR se decisão arquitetural (template em `docs/03-architecture/adr-template.md`)
- **README Módulo:** `domain/*/README.md` (visão geral, endpoints, config, testes)
- **CHANGELOG:** Entrada em `CHANGELOG.md` (Conventional Commits → `git cliff` ou manual)

### 8.2 Diagramas (Diagram as Code)
- **Mermaid** em `docs/03-architecture/` (C4, Sequência, ER, Activity)
- **Atualizar** no mesmo PR que altera código relacionado
- **Nunca** imagens estáticas coladas (PNG/JPG) — apenas Mermaid versionado

---

## 9. Code Review Guidelines

### 9.1 Para Autores (Self-Review Antes do PR)
- [ ] Código compila e testes passam localmente
- [ ] Commits atômicos, mensagens Conventional Commits em português
- [ ] Breaking changes documentados + `BREAKING CHANGE:` no commit
- [ ] Documentação atualizada (OpenAPI, ADR, README, CHANGELOG)
- [ ] Métricas/Observabilidade instrumentadas (metrics, logs, tracing)
- [ ] Segurança: sem secrets, validação entrada, autorização verificada

### 9.2 Para Revisores (Checklist)
- [ ] **Corretude:** Lógica de negócio implementada conforme Use Cases/ACs
- [ ] **Arquitetura:** Respeita boundaries (ArchUnit), DDD tático, Clean Architecture
- [ ] **Qualidade:** Ciclomática ≤ 10, nomes claros, sem código morto, DRY
- [ ] **Testes:** Cobertura ≥ 80% nas mudanças; cenários BDD cobertos; edge cases
- [ ] **Segurança:** Autorização (Aegis Shield), validação entrada, sem secrets, SQL injection safe
- [ ] **Performance:** Queries otimizadas (EXPLAIN), índices, cache, paginação, N+1 evitado
- [ ] **Observabilidade:** Logs JSON (correlation ID), métricas Prometheus, tracing headers
- [ ] **Acessibilidade:** WCAG 2.1 AA (se UI), aria-labels, focus order, contraste
- [ ] **Documentação:** OpenAPI, ADR, README, CHANGELOG atualizados
- [ ] **DX:** Mensagens de erro claras, DX para desenvolvedores futuros

### 9.3 Aprovação
- **Mínimo 1 aprovação** de code owner do módulo (definido em `CODEOWNERS`)
- **Todos os checks CI verdes** (build, test, security, contract, bundle, accessibility)
- **Nenhum comentário `Request Changes` pendente** (resolvido ou `Dismiss` com justificativa)
- **Merge:** Squash and merge (mantém histórico limpo) → `develop`

---

## 10. Release Process

### 10.1 Versionamento: SemVer (Semantic Versioning)
| Tipo | Exemplo | Quando |
| :--- | :--- | :--- |
| **Major** | `1.0.0` → `2.0.0` | Breaking changes (API, DB schema, config) |
| **Minor** | `1.0.0` → `1.1.0` | Novas features backward-compatible |
| **Patch** | `1.0.0` → `1.0.1` | Bug fixes backward-compatible |

### 10.2 Release Flow
1. **Feature Freeze:** PR `develop` → `release/x.y.0` (apenas fixes)
2. **Stabilization:** Testes extensivos em staging (load, chaos, security)
3. **Release Candidate:** Tag `v1.0.0-rc.1` → deploy staging → validação UAT
4. **Release:** Tag `v1.0.0` → `main` → ArgoCD sync prod → Smoke tests
5. **Post-Release:** Hotfix branch se necessário (`hotfix/1.0.1`)

### 10.3 Changelog
- Gerado automaticamente via `git cliff` (Conventional Commits → CHANGELOG.md)
- Categorias: `Features`, `Bug Fixes`, `Breaking Changes`, `Security`, `Performance`, `Documentation`, `Chore`
- Publicado no GitHub Releases + Notificação Slack/Email

---

## 11. Comunicação & Suporte

| Canal | Finalidade |
| :--- | :--- |
| **GitHub Issues** | Bug reports, feature requests, tasks (templates obrigatórios) |
| **GitHub Discussions** | Perguntas técnicas, RFCs, decisões arquiteturais |
| **Slack #aegis1-dev** | Daily sync, blockers, pair programming, deploy coordination |
| **Confluence / Notion** | Documentação viva, runbooks, ADRs, onboarding wiki |
| **Email: diogorpm@gmail.com** | Contato direto com maintainer (Diogo Menezes) |

---

## 12. Licença & Código de Conduta

- **Licença:** MIT (ver `LICENSE`)
- **Código de Conduta:** [Contributor Covenant v2.1](https://www.contributor-covenant.org/version/2/1/code_of_conduct/) — aplicado a todos participantes
- **DCO:** Developer Certificate of Origin — `git commit -s` obrigatório

---

*Documento regenerado completamente para stack Full Stack (Java 21/Spring Boot 3.3 + Vue 3/Vite/PWA + Docker/K8s/TestContainers). Substitui versão 1.0 que descrevia apenas frontend vanilla JS + API externa.*