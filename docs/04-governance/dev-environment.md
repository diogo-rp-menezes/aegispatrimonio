# Unified Development Environment Configuration — Aegis Patrimônio

## 1. Local Prerequisites & Tooling

* **Runtime (Backend):** Java 17+ (Eclipse Temurin 17 JRE recomendado, alinhado ao NFR-PO02)
* **Runtime (Frontend):** Node.js 20.x LTS (necessário para build do frontend Vanilla JS com Vite/Webpack — NFR-PO03)
* **Gerenciador de pacotes (Frontend):** npm 10.x (vem com Node.js 20.x)
* **Build Tool (Backend):** [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Maven 3.9+ **ou** Gradle 8.x — *não detectado no diagnóstico (ausência de `pom.xml`/`build.gradle` nos arquivos escaneados); verificar repositório raiz/branches*
* **Ferramentas obrigatórias:**
  * Docker 24.x + Docker Compose 2.x (para subir banco de dados, read-replica, WORM storage local, observabilidade stack)
  * Git 2.40+
  * CLI do provedor de cloud alvo (AWS CLI / Azure CLI / gcloud) — *pendente definição de infra (Gap #2 do SAD)*
* **Editores recomendados & extensões:**
  * **VS Code** + Extension Pack for Java (Microsoft), Spring Boot Extension Pack (VMware), ESLint, Prettier, EditorConfig
  * **IntelliJ IDEA Ultimate** (suporte nativo Spring Boot, JPA, Database tools)
* **Versão mínima do SO/Shell:** Linux (Ubuntu 22.04 LTS / RHEL 9), macOS 13+ (Ventura), Windows 10/11 com WSL2 Ubuntu 22.04

## 2. Setup Passo a Passo

1. **Instalar pré-requisitos listados acima** (Java 17, Node.js 20, Docker, Git, build tool backend)
2. **Clonar o repositório**
   ```bash
   git clone <url-do-repositorio>
   cd aegis-patrimonio
   ```
3. **Copiar `.env.example` para `.env`** (criar arquivo se não existir — ver seção 3)
4. **Instalar dependências do frontend:**
   ```bash
   cd frontend
   npm install --no-audit
   cd ..
   ```
5. **Subir serviços auxiliares (DB, read-replica, WORM storage local, observabilidade):**
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```
   > **Nota:** `docker-compose.dev.yml` deve ser criado na Sprint 0 (Gap #2, #4 do SAD). Incluir: banco relacional (motor TBD), MinIO (S3-compatível com Object Lock para WORM local), Prometheus, Grafana, Loki, Tempo, OpenTelemetry Collector.
6. **Configurar banco de dados (motor a definir — Gap #1 do SAD):**
   * Criar schema `aegis_patrimonio`
   * Rodar migrações (Flyway/Liquibase — a configurar na Sprint 0, Gap #5 do SAD)
   * Popular dados de exemplo (seed): `./mvnw spring-boot:run -Dspring-boot.run.arguments=--seeder.enabled=true` (Maven) ou `./gradlew bootRun --args='--seeder.enabled=true'` (Gradle) — *comando depende do build tool*
7. **Build do frontend (assets estáticos para Spring Boot servir):**
   ```bash
   cd frontend
   npm run build
   cd ..
   ```
   > Gera assets em `frontend/dist/` — copiar para `src/main/resources/static/` (Spring Boot) ou configurar `resources.static-locations` no `application-dev.yml`.
8. **Iniciar aplicação (modo desenvolvimento):**
   * **Maven:** `./mvnw spring-boot:run -Dspring.profiles.active=dev`
   * **Gradle:** `./gradlew bootRun --args='--spring.profiles.active=dev'`
   * **Alternativa (JAR pré-buildado):** `java -jar target/aegis-patrimonio-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`

## 3. Variáveis de Ambiente

| Variável | Obrigatória | Descrição | Valor de exemplo |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Sim | Perfil Spring ativo (`dev`, `staging`, `prod`) | `dev` |
| `SPRING_DATASOURCE_URL` | Sim | JDBC URL do banco primário | `jdbc:postgresql://localhost:5432/aegis_patrimonio` |
| `SPRING_DATASOURCE_USERNAME` | Sim | Usuário do banco | `aegis_user` |
| `SPRING_DATASOURCE_PASSWORD` | Sim | Senha do banco (usar secret manager em prod) | `dev_password_only` |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | Não | Tamanho máximo do pool HikariCP (NFR-S03: 500) | `20` (dev) / `500` (prod) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Não | Estratégia DDL Hibernate (`validate`, `update`, `none`) | `validate` |
| `SPRING_FLYWAY_ENABLED` | Não | Habilitar Flyway migrations | `true` |
| `JWT_SECRET` | Sim | Chave secreta HS256/RS256 para assinar JWT (NFR-SEC02) | `base64_encoded_256bit_key` |
| `JWT_EXPIRATION_MINUTES` | Não | Expiração access token (NFR-SEC02: ≤ 60 min) | `60` |
| `WORM_STORAGE_ENDPOINT` | Sim | Endpoint S3-compatível para auditoria (NFR-SEC05) | `http://localhost:9000` (MinIO local) |
| `WORM_STORAGE_BUCKET` | Sim | Bucket WORM com Object Lock habilitado | `aegis-audit-dev` |
| `WORM_STORAGE_ACCESS_KEY` | Sim | Access key MinIO/S3 | `minioadmin` |
| `WORM_STORAGE_SECRET_KEY` | Sim | Secret key MinIO/S3 | `minioadmin` |
| `OIDC_ISSUER_URI` | Não | Issuer do Identity Provider (Gap #3 do SAD) | `https://keycloak.local/realms/aegis` |
| `OIDC_CLIENT_ID` | Não | Client ID OIDC | `aegis-patrimonio` |
| `OIDC_CLIENT_SECRET` | Não | Client secret OIDC (usar secret manager) | `***` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Não | Endpoint OpenTelemetry Collector (Gap #4 do SAD) | `http://localhost:4317` |
| `SPRING_DATASOURCE_READ_REPLICA_URL` | Não | JDBC URL da read-replica (NFR-A04) | `jdbc:postgresql://localhost:5433/aegis_patrimonio` |
| `LOG_LEVEL_ROOT` | Não | Nível de log raiz | `INFO` |
| `LOG_LEVEL_BR_COM_AEGISPATRIMONIO` | Não | Nível de log do pacote da aplicação | `DEBUG` |

> **Arquivo `.env.example` deve ser versionado; `.env` real deve estar no `.gitignore`.**

## 4. Secure Command Sandbox Recipes

> Comandos permitidos para automação/agentes, com limites de segurança explícitos.

| Target Tool | Command Type | Recipe | Timeout | Restrições |
| :--- | :--- | :--- | :--- | :--- |
| `npm` | `install` | `npm install --no-audit --prefer-offline` | 120s | Sem acesso à rede externa fora do registry configurado (`npm config get registry`) |
| `npm` | `run build` | `npm run build --if-present` | 60s | Apenas leitura de `frontend/`, escrita em `frontend/dist/` |
| `mvn` / `gradle` | `compile` | `./mvnw compile -q` / `./gradlew compileJava --quiet` | 180s | Apenas leitura de `src/main/java/`, `src/main/resources/`; escrita em `target/` ou `build/` |
| `mvn` / `gradle` | `test` | `./mvnw test -q` / `./gradlew test --quiet` | 300s | Acesso a banco de teste (Testcontainers ou H2 em memória); sem rede externa |
| `docker compose` | `up` | `docker compose -f docker-compose.dev.yml up -d --wait` | 120s | Apenas imagens definidas no `docker-compose.dev.yml`; volumes locais mapeados |
| `docker compose` | `down` | `docker compose -f docker-compose.dev.yml down -v` | 60s | Remove volumes anônimos; preserva volumes nomeados se `preserve_volumes=true` |
| `java` | `run` | `java -jar target/*.jar --spring.profiles.active=dev` | Contínuo | Bind apenas em `localhost:8080`; variáveis de ambiente do `.env` |
| `flyway` / `liquibase` | `migrate` | `./mvnw flyway:migrate` / `./gradlew liquibaseUpdate` | 60s | Apenas contra `SPRING_DATASOURCE_URL` do perfil ativo; transacional |

## 5. Serviços Locais & Portas

| Serviço | Porta | Descrição |
| :--- | :--- | :--- |
| Aegis Patrimônio (App) | 8080 | Aplicação principal (Spring Boot + static frontend) |
| Banco de Dados Primário | 5432 | PostgreSQL (exemplo — **motor a confirmar**, Gap #1) |
| Read Replica | 5433 | Réplica de leitura (mesmo motor, porta distinta) |
| MinIO (WORM Storage Local) | 9000 (API), 9001 (Console) | S3-compatível com Object Lock para auditoria local |
| Prometheus | 9090 | Coleta de métricas (`/actuator/prometheus`) |
| Grafana | 3000 | Dashboards (login admin/admin, trocar no primeiro acesso) |
| Loki | 3100 | Agregação de logs JSON |
| Tempo | 3200 | Traces distribuídos (OTLP/gRPC 4317, HTTP 4318) |
| OpenTelemetry Collector | 4317 (gRPC), 4318 (HTTP), 8888 (Prometheus metrics) | Recebe telemetria da app |
| Keycloak (IdP Local) | 8081 | Identity Provider para testes OIDC/MFA (Gap #3) |

> **Conflitos de porta:** Se 8080/8081/5432/9000 estiverem em uso, ajustar `docker-compose.dev.yml` e `application-dev.yml` consistentemente.

## 6. Environment Verification Checklist

- [ ] `npm install` no `frontend/` funciona sem erros (zero vulnerabilidades `high`/`critical` em `npm audit`)
- [ ] `npm run build` no `frontend/` gera assets em `frontend/dist/` sem erros TypeScript/ESLint (se configurado)
- [ ] `./mvnw compile` **ou** `./gradlew compileJava` compila backend sem erros (Java 17)
- [ ] `./mvnw test` **ou** `./gradlew test` executa testes unitários e passa (cobertura ≥ 80% novo código — NFR-M02)
- [ ] `docker compose -f docker-compose.dev.yml up -d` sobe todos os containers saudáveis (`docker compose ps` mostra `healthy` ou `running`)
- [ ] Aplicação inicia com `./mvnw spring-boot:run -Dspring.profiles.active=dev` (ou Gradle equivalente) e loga `Started AegisPatrimonioApplication in X seconds`
- [ ] Health check responde: `curl -f http://localhost:8080/actuator/health/liveness` → `{"status":"UP"}`
- [ ] Readiness check responde: `curl -f http://localhost:8080/actuator/health/readiness` → `{"status":"UP"}` (inclui DB, scheduler, disco)
- [ ] Frontend acessível em `http://localhost:8080/` (login carrega, sem erros de console `console.error`/`console.debug` — NFR-M04)
- [ ] Banco de dados conectado e migrado: `flyway info` ou `liquibase status` mostra migrations aplicadas
- [ ] Variáveis de ambiente sensíveis não commitadas (`.env` no `.gitignore`; `.env.example` versionado)
- [ ] OpenAPI spec disponível em `http://localhost:8080/v3/api-docs` (SpringDoc — NFR-M03)
- [ ] Métricas Prometheus expostas em `http://localhost:8080/actuator/prometheus` (NFR-O01)
- [ ] Logs JSON estruturados no stdout com `traceId`/`spanId` (NFR-O01)
- [ ] MinIO console acessível em `http://localhost:9001` (bucket `aegis-audit-dev` com Object Lock habilitado)

## 7. Troubleshooting Comum

| Sintoma | Causa Provável | Solução |
| :--- | :--- | :--- |
| `java: command not found` / versão errada | Java 17 não no PATH ou versão incorreta | `sdk install java 17.0.10-tem` (SDKMAN) ou ajustar `JAVA_HOME`; `java -version` deve mostrar 17.x |
| `npm install` falha com `EACCES` / permissão | Permissões em `node_modules` ou prefixo npm | `npm config set prefix ~/.npm-global` + adicionar ao PATH; ou `sudo chown -R $USER ~/.npm` |
| Porta 8080 já em uso | Outro processo (ex: outro Spring Boot, Jenkins) | `lsof -ti:8080 \| xargs kill -9` ou alterar `server.port` no `application-dev.yml` |
| `Connection refused` ao banco | Container DB não subiu / porta errada / credenciais | `docker compose logs db`; verificar `SPRING_DATASOURCE_URL` porta; `docker compose restart db` |
| `Flyway migration failed` / `Liquibase lock` | Migration quebrada / lock não liberado | `flyway repair` / `liquibase releaseLocks`; corrigir SQL da migration falha |
| `JWT signature verification failed` | `JWT_SECRET` diferente entre app e testes / chave curta | Gerar chave Base64 de 256 bits: `openssl rand -base64 32`; usar mesma em `.env` e testes |
| MinIO `AccessDenied` ao gravar auditoria | Bucket sem Object Lock / policy errada / credenciais | `mc admin bucket lock enable minio/aegis-audit-dev`; policy `PutObject` + `PutObjectLegalHold` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` connection refused | Collector não subiu / porta errada | `docker compose logs otelcol`; verificar porta 4317/4318 no `docker-compose.dev.yml` |
| Frontend carrega mas API retorna 401/403 | Token expirado / CORS / `SecurityFilterChain` | Verificar `JWT_EXPIRATION_MINUTES`; CORS configurado em `WebMvcConfigurer`; logs de `SecurityFilterChain` |
| `HikariPool-1 - Connection is not available` | Pool exaurido (vazamento / pool pequeno) | Aumentar `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`; revisar `@Transactional` sem `readOnly=true` em leituras |
| `console.error` / `console.debug` no browser | Código residual em `frontend/src/services/api.js:36,99` | Remover antes de commit (NFR-M04); `npm run lint` deve falhar se `no-console` configurado |

## 8. Comandos Úteis

| Comando | Descrição |
| :--- | :--- |
| `./mvnw spring-boot:run -Dspring.profiles.active=dev` | Inicia backend em modo dev (Maven) |
| `./gradlew bootRun --args='--spring.profiles.active=dev'` | Inicia backend em modo dev (Gradle) |
| `cd frontend && npm run dev` | Inicia frontend com Vite (HMR) — **requer proxy para API** (`vite.config.js`: `server.proxy: { '/api': 'http://localhost:8080' }`) |
| `./mvnw test` / `./gradlew test` | Executa testes unitários + integração |
| `./mvnw verify` / `./gradlew check` | Build completo + testes + validações (SpotBugs, Checkstyle, OpenAPI breaking changes — NFR-M03) |
| `cd frontend && npm run lint` | ESLint + Prettier check (configurar `no-console`, `no-debugger`) |
| `docker compose -f docker-compose.dev.yml logs -f app` | Logs da aplicação em tempo real |
| `docker compose -f docker-compose.dev.yml exec db psql -U aegis_user -d aegis_patrimonio` | Shell SQL no banco primário |
| `curl -s http://localhost:8080/actuator/health/readiness \| jq` | Verifica readiness com formatação |
| `curl -s http://localhost:8080/actuator/prometheus \| grep -E '^jvm_|^http_server_requests_'` | Inspeciona métricas chave |
| `./mvnw flyway:migrate` / `./gradlew liquibaseUpdate` | Aplica migrations pendentes |
| `./mvnw spring-boot:run -Dspring-boot.run.arguments=--seeder.enabled=true` | Popula dados realistas (dev/test) — `RealisticDataSeeder` |
| `docker compose -f docker-compose.dev.yml down -v && docker compose -f docker-compose.dev.yml up -d` | Reset completo do ambiente local (limpa volumes) |

---

## O que falta verificar / Configurar na Sprint 0 (Gaps Bloqueadores)

1. **Build Tool Backend:** Confirmar Maven (`pom.xml`) ou Gradle (`build.gradle.kts`) no repositório raiz. Ajustar comandos nas seções 2, 4, 6, 8 conforme ferramenta real.
2. **Motor de Banco de Dados:** Definir PostgreSQL / Oracle / SQL Server / MySQL (Gap #1 SAD). Ajustar `SPRING_DATASOURCE_URL`, driver JDBC, dialecto Hibernate/Flyway, portas no `docker-compose.dev.yml`.
3. **`docker-compose.dev.yml`:** Criar arquivo com todos os serviços da seção 5 (DB, read-replica, MinIO, Prometheus, Grafana, Loki, Tempo, OTEL Collector, Keycloak). Definir volumes nomeados para persistência local.
4. **Migrações de Schema:** Configurar Flyway ou Liquibase (Gap #5 SAD). Criar baseline `V1__init.sql` ou `changelog-master.xml` a partir do schema atual (extrair via `schemacrawler` ou DBA).
5. **Observabilidade Stack Local:** Definir stack (Prometheus/Grafana/Loki/Tempo vs Datadog vs New Relic — Gap #4 SAD). Configurar `docker-compose.dev.yml` e `application-dev.yml` (OTel Java Agent, `management.otlp.tracing.endpoint`).
6. **Identity Provider Local:** Subir Keycloak no `docker-compose.dev.yml` (Gap #3 SAD). Criar realm `aegis`, client `aegis-patrimonio`, roles `ADMIN`/`AUDITOR`/`GESTOR`/`OPERADOR`, fluxo MFA para ADMIN.
7. **OpenAPI / SpringDoc:** Adicionar dependência `springdoc-openapi-starter-webmvc-ui` no build tool. Configurar `springdoc.api-docs.path=/v3/api-docs`, `springdoc.swagger-ui.path=/swagger-ui.html`.
8. **CI/CD Pipeline:** Implementar pipeline (GitHub Actions / GitLab CI / Jenkins) com validações NFR-M03/M04/M05 (quebra de build em: OpenAPI breaking changes, `console.*` no frontend, stubs vazios no backend).
9. **Secret Management Local:** Usar `.env` + `docker-compose.dev.yml` `env_file` para dev; planejar Vault / AWS Secrets Manager para staging/prod (NFR-SEC04).
10. **Frontend Build Integration:** Decidir se assets vão em `src/main/resources/static/` (Spring Boot serve) ou CDN/Nginx separado (NFR-PO03). Configurar `vite.config.js` `build.outDir` e `base` accordingly.

> **Todas as seções marcadas com [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] dependem das decisões acima e devem ser revisadas pelo Tech Lead / Arquiteto / DevOps na Sprint 0.**