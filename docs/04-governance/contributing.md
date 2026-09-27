# Contributing & Onboarding Guide — Aegis Patrimônio

> Bem-vindo(a)! Este guia leva você do zero ao primeiro PR mergeado.

## 1. Primeiros Passos
1. Clonar o repositório: `git clone <url-do-repositorio>`
2. Instalar pré-requisitos: Java 17+ (Eclipse Temurin 17 JRE recomendado), Node.js 20.x LTS, Docker 24.x + Docker Compose 2.x, Git 2.40+ <!-- source: dev-environment#L1-L10 -->
3. Configurar variáveis de ambiente: copiar `.env.example` para `.env` e preencher os valores (ver seção 3 do Development Environment) <!-- source: dev-environment#L55-L95 -->
4. Instalar dependências do frontend: `cd frontend && npm install --no-audit && cd ..` <!-- source: dev-environment#L22-L24 -->
5. Subir serviços auxiliares: `docker compose -f docker-compose.dev.yml up -d` <!-- source: dev-environment#L26-L31 -->
6. Configurar banco de dados e rodar migrações (Flyway/Liquibase — a configurar na Sprint 0) <!-- source: dev-environment#L33-L37 -->
7. Build do frontend: `cd frontend && npm run build && cd ..` <!-- source: dev-environment#L39-L43 -->
8. Iniciar aplicação (modo desenvolvimento): `./mvnw spring-boot:run -Dspring.profiles.active=dev` (Maven) ou `./gradlew bootRun --args='--spring.profiles.active=dev'` (Gradle) <!-- source: dev-environment#L45-L50 -->
9. Rodar a suíte de testes: `./mvnw test` ou `./gradlew test` <!-- source: dev-environment#L78 -->
10. Verificar se o ambiente está saudável: executar checklist da seção 6 do Development Environment <!-- source: dev-environment#L78-L95 -->

## 2. Estrutura do Projeto
```
src/
├── main/
│   ├── java/
│   │   └── br/com/aegispatrimonio/
│   │       ├── config/          # Configurações Spring (seeder, security, etc.)
│   │       ├── controller/      # REST Controllers
│   │       ├── mapper/          # MapStruct mappers (ex: AtivoMapper)
│   │       ├── model/           # Entidades JPA (ex: Usuario)
│   │       ├── repository/      # Spring Data JPA Repositories + Specifications
│   │       ├── service/         # Regras de negócio (ex: AlertNotificationService)
│   │       └── AegisPatrimonioApplication.java
│   └── resources/
│       ├── application.yml      # Configuração base
│       ├── application-dev.yml  # Perfil desenvolvimento
│       └── static/              # Assets frontend buildados (copiados de frontend/dist/)
└── test/                        # Testes unitários e de integração

frontend/
├── src/
│   ├── services/
│   │   └── api.js               # Cliente HTTP (complexidade ciclomática alta em request())
│   ├── components/              # Componentes Vanilla JS
│   └── styles/                  # CSS/SCSS
├── dist/                        # Build de produção (gerado por npm run build)
├── package.json
├── vite.config.js               # Configuração Vite (proxy para API em dev)
└── .eslintrc.js                 # ESLint config (no-console recomendado)

docker-compose.dev.yml           # [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] A criar na Sprint 0
.env.example                     # [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] A versionar na Sprint 0
pom.xml OU build.gradle.kts      # [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] Build tool backend — ausente no diagnóstico
```

## 3. Padrões de Commit
* **Convenção:** Conventional Commits — `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`, `ci:`
* **Exemplo:** `feat(ativo): adiciona endpoint de listagem com paginação e filtros`
* **Regras adicionais:**
  * Commits atômicos (uma mudança lógica por commit)
  * Mensagens em português (time brasileiro) ou inglês (conforme acordado na Sprint 0)
  * Referenciar issue/Jira quando aplicável: `feat(ativo): adiciona filtro por status #123`
  * **Obrigatório:** `git commit --signoff` (DCO) para rastreabilidade de autoria

## 4. Fluxo de Contribuição (Branching & PRs)
1. Criar branch a partir de `main`: `git checkout -b feature/nome-da-feature` ou `fix/nome-do-bug`
2. Desenvolver seguindo os padrões de código do projeto (ver seção 5)
3. Garantir que testes e lint passam localmente:
   * Backend: `./mvnw verify` ou `./gradlew check` (compila, testa, valida SpotBugs/Checkstyle/OpenAPI) <!-- source: dev-environment#L128 -->
   * Frontend: `cd frontend && npm run lint && npm run build` <!-- source: dev-environment#L129 -->
4. Abrir Pull Request com descrição clara (o quê, por quê, como testar)
5. Solicitar revisão de pelo menos **2 revisores** (um do time backend, um do time frontend se tocar frontend)
6. Endereçar comentários e aguardar aprovação
7. Merge via **squash** após CI verde (mantém histórico limpo)

### Template de PR sugerido
```markdown
## O que este PR faz
<!-- Descrição clara e concisa da mudança -->

## Como testar
<!-- Passos manuais ou comandos para validar a mudança -->
1. ...
2. ...

## Checklist
- [ ] Testes adicionados/atualizados (cobertura ≥ 80% novo código — NFR-M02)
- [ ] Documentação atualizada (README, OpenAPI, CHANGELOG se aplicável)
- [ ] Sem breaking changes (ou documentado em `BREAKING_CHANGES.md`)
- [ ] `console.error`/`console.debug` removidos do frontend (NFR-M04) <!-- source: dev-environment#L13-L14 -->
- [ ] Stubs vazios eliminados no backend (ex: Usuario.setUsername) <!-- source: dev-environment#L17-L18 -->
- [ ] Complexidade ciclomática ≤ 10 em novas funções (refatorar se > 10) <!-- source: dev-environment#L7-L11 -->
```

## 5. Padrões de Código
* **Linguagem/Framework:** Java 17 (Spring Boot 3.x), Vanilla JavaScript (ES2022+) com Vite
* **Estilo:**
  * Backend: Google Java Format + Checkstyle (configurar na Sprint 0) + SpotBugs
  * Frontend: ESLint (recommended + `no-console`, `no-debugger`) + Prettier (single quotes, 2 spaces, trailing comma es5)
* **Convenções de nomenclatura:**
  * Java: PascalCase classes, camelCase métodos/variáveis, UPPER_SNAKE_CASE constantes
  * JavaScript: camelCase variáveis/funções, PascalCase componentes, kebab-case arquivos
  * Pacotes: `br.com.aegispatrimonio.<camada>.<dominio>`
* **Testes obrigatórios para:**
  * Novas features (unitários + integração para controllers/services)
  * Correções de bugs (teste de regressão)
  * Mudanças em mappers, specifications, services de notificação
* **Arquitetura:** Camadas estritas (Controller → Service → Repository → Model); DTOs para API; MapStruct para mapeamento; Specifications para queries dinâmicas

## 6. Revisão de Código (Code Review Guidelines)
* **O que revisores devem verificar:**
  * Corretude: lógica atende requisitos, edge cases cobertos
  * Legibilidade: nomes claros, comentários em "porquê" não "o quê", funções pequenas (complexidade ≤ 10)
  * Testes: cobertura ≥ 80% novo código, testes de integração para endpoints, mocks apropriados
  * Segurança: validação de entrada, autorização (RBAC: ADMIN/AUDITOR/GESTOR/OPERADOR), secrets não hardcoded, JWT HS256/RS256
  * Performance: N+1 evitado (fetch joins), índices em colunas filtradas, pool HikariCP configurado (dev: 20, prod: 500) <!-- source: dev-environment#L63 -->
  * Observabilidade: logs JSON com traceId/spanId, métricas Prometheus expostas, OpenAPI atualizado
* **Tempo esperado de resposta a um PR:** 1 dia útil (SLA interno)
* **Como dar feedback construtivo:**
  * Tom colaborativo: "Sugiro..." / "Considere..." em vez de "Está errado"
  * Separar bloqueadores (bugs, segurança, breaking changes) de sugestões (estilo, refatoração opcional)
  * Apontar para documentação/padrões existentes quando sugerir mudanças
  * Aprovar com "LGTM" apenas quando confiante; solicitar mudanças se houver dúvidas

## 7. Reportando Bugs
* **Onde reportar:** GitHub Issues (ou tracker configurado na Sprint 0)
* **Template:**
  ```markdown
  ## Descrição do Bug
  <!-- O que acontece vs. o que deveria acontecer -->

  ## Passos para Reproduzir
  1. ...
  2. ...
  3. ...

  ## Comportamento Esperado
  <!-- Descrição clara -->

  ## Comportamento Atual
  <!-- O que ocorre de fato -->

  ## Ambiente
  - OS: [ex: Ubuntu 22.04 / Windows 11 WSL2]
  - Java: [ex: 17.0.10-tem]
  - Node: [ex: 20.12.0]
  - Perfil Spring: [dev/staging/prod]
  - Banco: [motor + versão — TBD na Sprint 0]

  ## Logs/Stacktrace
  <!-- Colar logs relevantes (sem secrets) -->

  ## Checklist
  - [ ] Já verifiquei issues existentes (duplicatas)
  - [ ] Incluí steps mínimos reproduzíveis
  ```

## 8. Propondo Novas Funcionalidades
* **Processo:** Abrir Issue com label `enhancement` → Discussão no Issue → Se complexo, criar RFC (Markdown em `docs/rfc/`) → Aprovação do Tech Lead/Arquiteto → Implementação
* **Critérios para RFC obrigatório:** Mudança de arquitetura, novo domínio, breaking change de API, nova dependência externa, impacto em NFRs (performance, segurança, disponibilidade)

## 9. Código de Conduta
Respeito, inclusão, comunicação profissional. Baseado no [Contributor Covenant v2.1](https://www.contributor-covenant.org/version/2/1/code_of_conduct/).
* **Resumo:** Seja acolhedor, assuma boa intenção, foque no código não na pessoa, reporte comportamentos inaceitáveis para `conduct@aegispatrimonio.local` (canal confidencial)
* **Arquivo completo:** `CODE_OF_CONDUCT.md` na raiz do repositório (a criar na Sprint 0)

## 10. Contatos & Suporte
| Canal | Uso |
| :--- | :--- |
| **Slack/Discord** (a definir na Sprint 0) | Dúvidas rápidas, pair programming, avisos de deploy |
| **GitHub Issues** | Bugs, features, dúvidas técnicas assíncronas |
| **Email da equipe** `team@aegispatrimonio.local` | Questões sensíveis (segurança, dados, compliance) |
| **Wiki/Confluence** (a definir) | Documentação arquitetural, ADRs, runbooks |

---

## Notas de Onboarding Específicas deste Projeto

### Gaps Bloqueadores da Sprint 0 (devem ser resolvidos antes de onboarding em massa)
1. **Build Tool Backend:** Confirmar Maven (`pom.xml`) ou Gradle (`build.gradle.kts`) — comandos nas seções 1, 4, 5 dependem disto <!-- source: dev-environment#L5-L6 -->
2. **Motor de Banco de Dados:** Definir PostgreSQL / Oracle / SQL Server / MySQL — afeta JDBC URL, dialecto, docker-compose <!-- source: dev-environment#L58 -->
3. **`docker-compose.dev.yml`:** Criar com todos os serviços (DB, read-replica, MinIO, Prometheus, Grafana, Loki, Tempo, OTEL Collector, Keycloak) <!-- source: dev-environment#L26-L31 -->
4. **Migrações de Schema:** Configurar Flyway ou Liquibase + baseline inicial <!-- source: dev-environment#L33-L37 -->
5. **Observabilidade Stack:** Definir stack local vs. SaaS <!-- source: dev-environment#L31 -->
6. **Identity Provider Local:** Subir Keycloak com realm `aegis`, roles RBAC, MFA para ADMIN <!-- source: dev-environment#L31 -->
7. **OpenAPI / SpringDoc:** Adicionar dependência e configurar endpoints <!-- source: dev-environment#L93 -->
8. **CI/CD Pipeline:** Implementar com validações NFR-M03/M04/M05 <!-- source: dev-environment#L128-L129 -->
9. **Secret Management:** Planejar Vault / AWS Secrets Manager para staging/prod <!-- source: dev-environment#L95 -->
10. **Frontend Build Integration:** Decidir `src/main/resources/static/` vs. CDN/Nginx separado <!-- source: dev-environment#L43 -->

### Pontos de Atenção Técnica (do diagnóstico)
* **Complexidade ciclomática alta** em 5 locais — refatorar ao tocar esses arquivos: `api.js:46`, `RealisticDataSeeder.java:34`, `AtivoMapper.java:15`, `ManutencaoSpecification.java:26`, `AlertNotificationService.java:96` <!-- source: dev-environment#L7-L11 -->
* **`console.error`/`console.debug` residuais** em `frontend/src/services/api.js:36,99` — remover antes de commit <!-- source: dev-environment#L13-L14 -->
* **Stub vazio** em `Usuario.setUsername()` — implementar ou remover <!-- source: dev-environment#L17-L18 -->
* **Nenhum build tool detectado** no diagnóstico — verificar raiz do repositório/branches <!-- source: dev-environment#L5 -->

### Comandos Rápidos de Referência
| Ação | Comando |
| :--- | :--- |
| Iniciar backend (dev) | `./mvnw spring-boot:run -Dspring.profiles.active=dev` |
| Iniciar frontend (HMR) | `cd frontend && npm run dev` (configurar proxy Vite → `http://localhost:8080`) |
| Testes completos | `./mvnw verify` / `./gradlew check` |
| Lint frontend | `cd frontend && npm run lint` |
| Logs app | `docker compose -f docker-compose.dev.yml logs -f app` |
| Health check | `curl -f http://localhost:8080/actuator/health/readiness` |
| OpenAPI spec | `http://localhost:8080/v3/api-docs` |
| Métricas Prometheus | `http://localhost:8080/actuator/prometheus` |
| MinIO Console | `http://localhost:9001` (bucket `aegis-audit-dev`) |
| Reset ambiente local | `docker compose -f docker-compose.dev.yml down -v && docker compose -f docker-compose.dev.yml up -d` |

> **Dica:** Adicione aliases no seu `.bashrc`/`.zshrc` para comandos frequentes. Ex: `alias aegis-dev='./mvnw spring-boot:run -Dspring.profiles.active=dev'`.