# Unified Development Environment Configuration — Aegis1

> **Versão:** 2.0 · **Status:** Draft · **Owner:** DevOps / Tech Leads
> **Base:** System Architecture v2.0 + NFR v2.0 + Análise AST Java Completa
> **Cobertura:** Full Stack — Backend (Java 21/Spring Boot 3.3) + Frontend (Vue 3/Vite/PWA) + Infra (Docker/K8s/TestContainers)

---

## 1. Local Prerequisites & Tooling

### 1.1 Obrigatórios (Todos os Desenvolvedores)

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

---

## 2. Setup Passo a Passo (Zero to Running)

### 2.1 Clone & Configuração Inicial

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

### 2.2 Backend — Build & Test Local

```bash
# Build completo (compila, testes unitários, checkstyle, spotbugs)
./mvnw clean verify -DskipITs

# Apenas compilar + testes unitários (rápido)
./mvnw test -DskipITs

# Testes de integração com TestContainers (MySQL real)
./mvnw verify -DskipUnitTests=false

# Rodar aplicação local (perfil dev + Docker Compose para DB)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2.3 Frontend — Dev Server + Build

```bash
cd frontend
pnpm install
pnpm dev          # http://localhost:5173 (proxy /api → http://localhost:8080)
pnpm build        # dist/ pronto para deploy estático
pnpm preview      # Preview build local
pnpm lint         # ESLint (no-console: error, complexity, etc.)
pnpm format       # Prettier --write
pnpm format:check # Prettier --check (CI)
```

### 2.4 Full Stack Local (Docker Compose — Recomendado)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build

# Serviços:
# - mysql:3306 (MySQL 8.0, healthcheck)
# - redis:6379 (Redis 7)
# - backend:8080 (Spring Boot dev profile, live reload)
# - frontend:5173 (Vite dev server + HMR, proxy /api → backend)
# - mailhog:8025 (SMTP testing UI)
# - minio:9000/9001 (S3 compatible storage + console)

docker compose logs -f backend
docker compose down -v  # -v remove volumes (limpa DB)
```

---

## 3. Docker Compose Profiles

| Profile | Comando | Serviços | Uso |
| :--- | :--- | :--- | :--- |
| **dev** | `docker compose --profile dev up` | mysql, redis, backend, frontend, mailhog, minio | Desenvolvimento diário (HMR backend + frontend) |
| **test** | `docker compose --profile test up` | mysql, redis, backend (test profile) | Testes integração CI-like local |
| **prod-local** | `docker compose --profile prod up` | mysql, redis, backend (prod), frontend (nginx), nginx, prometheus, grafana, loki, tempo | Simulação produção local |
| **infra** | `docker compose --profile infra up` | mysql, redis, minio, mailhog | Apenas infra (backend/frontend rodam no IDE) |

---

## 4. Variáveis de Ambiente

### 4.1 Backend (`application.yml` + `.env` / `application-dev.yml`)

```yaml
spring:
  application:
    name: aegis1
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:aegis1}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${MYSQL_USER:aegis1}
    password: ${MYSQL_PASSWORD:secret}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: UTC
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

aegis:
  jwt:
    private-key: ${JWT_PRIVATE_KEY:classpath:keys/private.pem}
    public-key: ${JWT_PUBLIC_KEY:classpath:keys/public.pem}
    access-token-ttl: 900
    refresh-token-ttl: 604800
    issuer: aegis1
  multi-tenancy:
    header-name: X-Tenant-Id
    admin-role: ADMIN_GLOBAL

storage:
  s3:
    endpoint: ${S3_ENDPOINT:http://localhost:9000}
    region: ${S3_REGION:us-east-1}
    bucket: ${S3_BUCKET:aegis1}
    access-key: ${S3_ACCESS_KEY:minioadmin}
    secret-key: ${S3_SECRET_KEY:minioadmin}
    path-style-access: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers,httptrace
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 0.1
```

### 4.2 Frontend (`.env` + `.env.local` + `.env.production`)

```bash
# .env (comum, commitado)
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Aegis1
VITE_APP_VERSION=1.0.0
VITE_ENABLE_PWA=true
VITE_ENABLE_MOCK_API=false

# .env.local (NÃO commitado - segredos locais)
VITE_SENTRY_DSN=https://xxx@sentry.io/123
VITE_FEATURE_FLAGS='{"newCostFlow":true,"predictiveDashboard":true}'

# .env.production (commitado - valores prod)
VITE_API_BASE_URL=https://api.aegis1.empresa.com
VITE_SENTRY_DSN=https://xxx@sentry.io/123
VITE_FEATURE_FLAGS='{}'
```

### 4.3 Segredos (Não Commitados — Vault/SealedSecrets)

| Segredo | Onde | Gerenciamento |
| :--- | :--- | :--- |
| `MYSQL_PASSWORD` | K8s Secret / Vault | `kubectl create secret generic aegis1-db --from-literal=password=...` |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | Vault / SealedSecret | `kubeseal --cert=sealedsecret.pem < private.pem > sealed-private.yaml` |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | Vault / AWS Secrets Manager | External Secrets Operator |
| `SENTRY_DSN` | GitHub Actions Secrets / Vault | Injetado no build frontend |
| `SMTP_PASSWORD` | Vault | Injetado no backend via External Secrets |

---

## 5. Testes Locais

### 5.1 Backend

```bash
# Testes unitários (rápido, sem containers)
./mvnw test -DskipITs

# Testes integração (TestContainers MySQL real)
./mvnw verify -DskipUnitTests=false

# Testes específicos
./mvnw test -Dtest=AtivoControllerIT#criarAtivoComHardware
./mvnw test -Dtest=*ControllerIT -DfailIfNoTests=false

# Coverage report (JaCoCo)
./mvnw jacoco:report
# → target/site/jacoco/index.html
```

### 5.2 Frontend

```bash
cd frontend
pnpm test                    # Unit + Component tests (Vitest + Vue Test Utils)
pnpm test:coverage           # Coverage (V8 provider) → coverage/index.html
pnpm cypress:open            # E2E UI interativo (requer backend rodando)
pnpm cypress:run             # Headless (CI)
pnpm typecheck               # TypeScript strict (tsc --noEmit)
```

### 5.3 Contract Tests (Pact / Spring Cloud Contract)

```bash
# Backend: Gera contratos (provider)
./mvnw test -Dtest=*ContractTest

# Frontend: Valida contratos (consumer)
cd frontend && pnpm pact:verify
```

---

## 6. Debugging & Profiling

### 6.1 Backend (Java)

| Técnica | Como Fazer |
| :--- | :--- |
| **Remote Debug (IDE)** | `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"` → VS Code `Attach to Remote (5005)` |
| **Docker Debug** | `docker compose up backend` expõe porta 5005 → `Attach to Remote (5005)` |
| **Heap Dump** | `jcmd <pid> GC.heap_dump /tmp/heap.hprof` → Eclipse MAT / VisualVM |
| **Thread Dump** | `jcmd <pid> Thread.print` ou `kill -3 <pid>` |
| **JFR (Flight Recorder)** | `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-XX:StartFlightRecording=duration=60s,filename=recording.jfr"` → JMC |
| **SQL Logging** | `logging.level.org.hibernate.SQL=DEBUG` + `logging.level.org.hibernate.orm.jdbc.bind=TRACE` |

### 6.2 Frontend (Vue/Vite)

| Técnica | Como Fazer |
| :--- | :--- |
| **Vue DevTools** | Extensão navegador (Vue 3) — inspeciona componentes, Pinia, router |
| **Vite HMR** | Automático no `pnpm dev` — preserva estado Pinia |
| **Debug Tests** | `pnpm test -- --inspect-brk` → Chrome DevTools `chrome://inspect` |
| **Network Tab** | Inspeciona requisições Axios, headers `X-Trace-Id`, `Authorization` |
| **PWA / SW Debug** | Chrome DevTools → Application → Service Workers |
| **IndexedDB** | Application → IndexedDB → `health-check-queue`, `offline-cache` |

### 6.3 K8s Local (kind/k3d)

```bash
kind create cluster --name aegis1-dev --config kind-config.yaml

# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 8080
  - containerPort: 30443
    hostPort: 8443

kubectl config use-context kind-aegis1-dev
kubectl apply -k k8s/overlays/dev

kubectl port-forward -n aegis1 svc/aegis1-frontend 3000:80 &
kubectl port-forward -n aegis1 svc/aegis1-backend 8080:8080 &
```

---

## 7. CI/CD Pipeline (GitHub Actions — Resumo)

```yaml
# .github/workflows/ci.yml
jobs:
  backend:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env: { MYSQL_ROOT_PASSWORD: root, MYSQL_DATABASE: aegis1_test }
        ports: [3306:3306]
        options: --health-cmd="mysqladmin ping" --health-interval=10s
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: maven }
      - run: ./mvnw verify -DskipUnitTests=false -B
      - run: ./mvnw jacoco:report && bash -c "COV=$(xmllint --xpath 'string(//report/counter[@type=\"INSTRUCTION\"]/@covered)' target/site/jacoco/jacoco.xml); TOTAL=$(xmllint --xpath 'string(//report/counter[@type=\"INSTRUCTION\"]/@missed)' target/site/jacoco/jacoco.xml); PCT=$((COV*100/(COV+TOTAL))); [ $PCT -ge 80 ]"
      - run: ./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
      - run: ./mvnw test -Dtest=*ContractTest

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: pnpm/action-setup@v2
      - uses: actions/setup-node@v4
        with: { node-version: 20, cache: pnpm, cache-dependency-path: frontend/pnpm-lock.yaml }
      - run: cd frontend && pnpm install --frozen-lockfile
      - run: cd frontend && pnpm lint && pnpm format:check && pnpm typecheck
      - run: cd frontend && pnpm test:coverage
      - run: cd frontend && pnpm pact:verify
      - run: cd frontend && pnpm build
      - run: bash -c "SIZE=$(gzip -c frontend/dist/assets/*.js | wc -c); [ $SIZE -le 153600 ]"

  docker:
    needs: [backend, frontend]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: docker/build-push-action@v5
        with: { context: ., file: Dockerfile, push: true, tags: ghcr.io/owner/aegis1-backend:${{ github.sha }}, cache-from: type=gha, cache-to: type=gha, mode=max, sbom: true }
      - uses: docker/build-push-action@v5
        with: { context: ./frontend, file: Dockerfile, push: true, tags: ghcr.io/owner/aegis1-frontend:${{ github.sha }}, cache-from: type=gha, cache-to: type=gha, mode=max }
      - run: cosign sign --yes ghcr.io/owner/aegis1-backend:${{ github.sha }} ghcr.io/owner/aegis1-frontend:${{ github.sha }}
      - run: trivy image --severity HIGH,CRITICAL --exit-code 1 ghcr.io/owner/aegis1-backend:${{ github.sha }} ghcr.io/owner/aegis1-frontend:${{ github.sha }}
```

### 7.1 Rodar CI Local (`act`)

```bash
curl -s https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash
act -j backend -P ubuntu-latest=catthehacker/ubuntu:act-22.04 --container-architecture linux/amd64
act -j frontend -P ubuntu-latest=catthehacker/ubuntu:act-22.04
```

---

## 8. Troubleshooting Comum

| Sintoma | Causa Provável | Solução |
| :--- | :--- | :--- |
| **TestContainers: "Could not find a valid Docker environment"** | Docker não rodando / socket permission | `systemctl start docker` / `sudo usermod -aG docker $USER` (re-login) |
| **TestContainers: "Port 3306 already in use"** | MySQL local rodando na porta | `docker stop $(docker ps -q --filter publish=3306)` ou mude porta no `application-test.yml` |
| **Maven: "Cannot find symbol" / "Package not found"** | Cache corrompido / dependência faltando | `./mvnw clean compile -U` |
| **Frontend: "Module not found" / "Cannot resolve"** | `node_modules` desatualizado / lockfile mismatch | `cd frontend && rm -rf node_modules pnpm-lock.yaml && pnpm install` |
| **Vite: "CORS error" ao chamar backend** | Proxy não configurado / backend CORS | `vite.config.ts`: `server: { proxy: { '/api': 'http://localhost:8080' } }` |
| **K8s kind: "connection refused" ao port-forward** | Pod não Ready / Service selector errado | `kubectl get pods -n aegis1 -o wide` → verifica `READY` e `LABELS` |
| **MySQL: "Communications link failure"** | Container MySQL não healthy / rede | `docker compose logs mysql` → verifica `healthcheck`; `docker compose restart mysql` |
| **Redis: "Connection refused"** | Redis não subiu / porta errada | `docker compose logs redis`; `docker compose ps` |
| **Flyway: "Migration checksum mismatch"** | Arquivo migration alterado após apply | `./mvnw flyway:repair` (apenas dev!) ou reverta alteração no arquivo |
| **JWT: "Invalid signature" / "Token expired"** | Chaves dessincronizadas / clock skew | Verifica `JWT_PRIVATE_KEY`/`PUBLIC_KEY` iguais em todos pods; NTP sync |
| **Multi-tenancy: "Dados de outra filial vazando"** | `@Filter` faltando em entidade / `TenantContextHolder` não limpo | ArchUnit test falha → adiciona `@Filter` na entidade; `TenantContextHolder.clear()` no filter `afterCompletion` |

---

## 9. Performance & Profiling Local

```bash
# Backend: JFR Recording (60s)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-XX:StartFlightRecording=duration=60s,filename=profile.jfr,settings=profile"
# Abrir profile.jfr no JDK Mission Control (JMC)

# Frontend: Lighthouse CI Local
cd frontend && pnpm build && npx lighthouse http://localhost:4173 --preset=desktop --output=html --output-path=./lighthouse-report.html

# Bundle Analyzer
cd frontend && pnpm build --mode=analyze
# Abre browser com treemap do bundle (rollup-plugin-visualizer)

# K6 Load Test Local (contra backend local)
docker run --rm -i grafana/k6 run - <(cat <<'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';
export const options = { stages: [{ duration: '30s', target: 50 }, { duration: '1m', target: 100 }] };
export default function() {
  const res = http.get('http://host.docker.internal:8080/api/v1/ativos?page=0&size=10', { headers: { Authorization: 'Bearer <token>' } });
  check(res, { 'status 200': (r) => r.status === 200, 'p95 < 500ms': (r) => r.timings.duration < 500 });
  sleep(1);
}
EOF
)
```

---

## 10. Checklist de Verificação de Ambiente (Definition of Ready para Dev)

- [ ] `./mvnw clean verify` passa (testes unitários + integração + quality gates)
- [ ] `cd frontend && pnpm install && pnpm lint && pnpm format:check && pnpm typecheck && pnpm test:coverage` passa
- [ ] `docker compose --profile dev up -d` sobe todos serviços saudáveis (`docker compose ps` → todos `Up (healthy)`)
- [ ] Backend acessível em `http://localhost:8080/actuator/health` → `{"status":"UP"}`
- [ ] Frontend acessível em `http://localhost:5173` → carrega sem erros console
- [ ] Login funciona: `POST /api/v1/auth/login` → retorna `accessToken` + `refreshToken` cookie
- [ ] Multi-tenancy: Header `X-Tenant-Id: 1` filtra dados corretamente
- [ ] Busca fuzzy: `GET /api/v1/busca?q=notebok` → retorna resultados com highlight
- [ ] PWA: Service Worker registrado (`Application → Service Workers` → `activated`)
- [ ] Observabilidade: `http://localhost:8080/actuator/prometheus` expõe métricas; `http://localhost:9090` (Prometheus) scrape OK
- [ ] Testes contrato: `./mvnw test -Dtest=*ContractTest` + `cd frontend && pnpm pact:verify` passam
- [ ] Segurança: `./mvnw org.owasp:dependency-check-maven:check` → zero CVSS ≥ 7

---

*Documento regenerado completamente para stack Full Stack (Java 21/Spring Boot 3.3 + Vue 3/Vite/PWA + Docker/K8s/TestContainers). Substitui versão 1.0 que descrevia apenas frontend vanilla JS + API externa.*