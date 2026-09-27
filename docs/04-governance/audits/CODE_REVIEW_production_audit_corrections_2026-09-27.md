# Code Review — Correções do Production Audit (qualidade, não segurança)

**Data:** 2026-09-27
**Escopo:** Mudanças do working tree + commits da sessão de correções do production-audit (35bc7ee, 7ea3249, a8c0c91 e working tree não commitado).
**Revisor:** Code Reviewer (qualidade). Segurança já verificada em `SECURITY_REPORT_reaudit_corrections_2026-09-26.md` — não re-auditada aqui.
**Modo:** Read-only. Nenhum fix aplicado.

---

## Evidência de execução (evidence over assertion)

| Verificação | Resultado |
|---|---|
| `mvnw compile` (Java 21) | ✅ PASS |
| `mvnw test` (unit, 308 testes) | ⚠️ 3 falhas — **todas pré-existentes no HEAD** (verificadas em worktree limpo do commit 3e962fe): `JwtAuthFilterTest.doFilterInternal_comTokenValido_devePermitirAcesso` (403 vs 200), `AlertNotificationServicePerformanceTest` (mock `save` vs `saveAll`), `OSHIHealthCheckCollectorBenchmarkTest` (benchmark de tempo, flaky por hardware) |
| Testes novos/alterados da sessão (`TokenDenylistServiceTest`, `JwtServiceTest`, `ManutencaoServiceTest`, `MovimentacaoServiceTest`, `QRCodeServiceTest`, `DashboardServiceTest`) | ✅ PASS |
| `FilialControllerIT` (valida V16 + setup RBAC em H2) | ✅ PASS |
| `npm run build` (frontend) | ✅ PASS (built in 6.44s) |
| Paridade N4 (V16 vs pre-H1, commit 4d815dd) | ✅ Verificada controller a controller (Manutencao, Movimentacao, Depreciacao, TipoAtivo, Filial, Departamento, Localizacao, Dashboard, Alerta) |

---

## 1. Corretude

**Pontos positivos (done well):**
- [`TokenDenylistService`](src/main/java/br/com/aegispatrimonio/security/TokenDenylistService.java:31): eviction lazy + periódico corretos; `ConcurrentHashMap` adequado; `revoke()` com token já expirado não armazena; `remove(token, expiresAt)` evita remoção condicional incorreta. Thread-safety OK.
- [`AuthController.logout`](src/main/java/br/com/aegispatrimonio/controller/AuthController.java:116): idempotente, valida o token antes de revogar, 204 sem body — contrato coerente com o frontend fire-and-forget.
- [`AtivoService.getAllAtivos`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:173) loop paginado: `total = page.getTotalElements()` é correto (o count não muda entre páginas dentro da mesma transação `readOnly`); `PageImpl(allContent, Pageable.unpaged(), total)` preserva o contrato. `pageNumber < page.getTotalPages()` termina corretamente.
- [`DashboardService.getStats`](src/main/java/br/com/aegispatrimonio/service/DashboardService.java:38): guard de TenantContext falha explicitamente em vez de filtrar por `filial.id = null` silenciosamente. Bom.
- [`handleResponse`](frontend/src/services/api.js:12): guarda de `pathname !== '/login'` evita loop de redirect; parse de ProblemDetail com fallback para texto bruto; 204 → null. Edge cases cobertos.
- [`ManutencaoSpecification.build`](src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:16): overload preserva o contrato antigo; predicado `filiaisIds` só aplicado quando não-vazio.

**Findings:**

- **[MAJOR-1] Loop paginado do `AtivoService` não é consistente sob escrita concorrente e ignora ordenação** — [`AtivoService.java:173-191`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:173). O `PageRequest.of(pageNumber, 500)` sem `Sort` usa a ordenação natural do banco (por PK na prática, mas não garantida). Se registros forem inseridos/removidos durante a iteração, páginas podem pular/duplicar ativos. Em `@Transactional(readOnly=true)` com MySQL/InnoDB MVCC a leitura é consistente, então o risco é baixo no fluxo atual (export/relatório), mas o contrato não está documentado. **Ação:** adicionar `Sort.by("id")` explícito ao `PageRequest` e um comentário de uma linha sobre a consistência. Não bloqueia merge.
- **[MINOR-2] `requireAcessoFilialAtivo` pode lançar NPE** — [`ManutencaoService.java:210-218`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:210) e [`MovimentacaoService.java:191-199`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:191). `ativo.getFilial().getId()` assume que `filial` está populada; se a entidade vier com filial lazy não inicializada fora de contexto transacional, lança `LazyInitializationException` (que o advice mapeia para 500). Na prática `convertToEntity` carrega o ativo via repositório dentro da transação, então é seguro hoje — mas frágil a refatorações. **Ação:** extrair `ativo.getFilial().getId()` com null-check ou confiar no `@Transactional` e documentar.
- **[MINOR-3] `JwtAuthFilterTest.doFilterInternal_comTokenValido_devePermitirAcesso` falha (403) — pré-existente, mas o teste novo do guard de tenant não o cobre** — [`JwtAuthFilterTest.java:86-102`](src/test/java/br/com/aegispatrimonio/security/JwtAuthFilterTest.java:86). Root cause: o mock cria `Usuario` com `setRole("ROLE_ADMIN")` (coluna legada), mas [`UserContextService.isAdmin()`](src/main/java/br/com/aegispatrimonio/service/UserContextService.java:33) lê `usuario.getRoles()` (RBAC), que está vazio → não-admin → `getUserFiliais()` → usuário sem funcionário → `AccessDeniedException` → 403. O teste mocka `permissionService.hasRole` (caminho antigo, commit ee3b44c removeu essa dependência). **Ação:** atualizar o teste para popular `usuario.setRoles(...)` com uma `Role` ROLE_ADMIN. Não foi introduzido por esta sessão, mas o novo guard de tenant (`DashboardService`) torna o caminho `getUserFiliais` mais crítico — corrigir em follow-up.

## 2. Clareza / Manutenibilidade

**Pontos positivos:**
- Comentários justificam o *porquê* (ex.: nota L6 em [`SecurityConfig.java:30-32`](src/main/java/br/com/aegispatrimonio/config/SecurityConfig.java:30) sobre MvcRequestMatcher vs PathPatternRequestMatcher; TODO(perf) com critério de gatilho mensurável em [`AtivoService.java:119-122`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119)). Padrão exemplar.
- [`package-info.java`](src/main/java/br/com/aegispatrimonio/controller/package-info.java:1) atualizado removendo a seção "Pendências" — documentação acompanhou o código (H1c).

**Findings:**

- **[MAJOR-4] Duplicação dos guards `requireAcessoFilialAtivo` entre ManutencaoService e MovimentacaoService** — [`ManutencaoService.java:205-218`](src/main/java/br/com/aegispatrimonio/service/ManutencaoService.java:205) e [`MovimentacaoService.java:191-199`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:191). Implementações semanticamente idênticas (diferem só em `userContextService.isAdmin()` direto vs wrappers `isAdmin()`/`getUserFiliais()` privados). Violação de DRY com risco real: uma correção no guard (ex.: null-check do MINOR-2) precisará ser feita duas vezes. **Ação:** extrair para `UserContextService.requireAcessoFilial(Ativo ativo)` (ou um `FilialAccessGuard` component) e usar nos dois serviços. Aceitável adiar para follow-up, mas registrar como débito.
- **[MINOR-5] Wrappers privados `isAdmin()`/`getUserFiliais()` no MovimentacaoService** — [`MovimentacaoService.java:177-183`](src/main/java/br/com/aegispatrimonio/service/MovimentacaoService.java:177). Adicionam indireção sem valor (o ManutencaoService chama `userContextService.isAdmin()` direto). Inconsistência entre os dois serviços que duplicam lógica. Resolve-se junto com MAJOR-4.
- **[NIT-6] Fully-qualified names inline no novo código do loop paginado** — [`AtivoService.java:174-184`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:174): `new java.util.ArrayList<>()`, `org.springframework.data.domain.Page`, `org.springframework.data.domain.PageRequest.of(...)`. O arquivo já usa imports; os FQNs poluem a leitura. **Ação:** mover para imports.

## 3. Consistência de Convenções

**Pontos positivos:**
- Constructor injection mantido em todos os serviços novos/alterados; `@RequiredArgsConstructor` onde já era usado.
- `@PreAuthorize` com `permissionService` uniformizado nos 9 controllers, mapeamento RESOURCE:ACTION idêntico ao seed V16 e ao `package-info` — três fontes coerentes entre si.
- Properties seguem o estilo existente (comentário + chave por linha); `application-prod.properties` desliga springdoc (N1) e reduz log de `DEBUG`→`INFO` coerente com `application.properties`.
- CI: actions pinadas por SHA com comentário de versão; job frontend novo espelha o job backend.

**Findings:**

- **[MINOR-7] `.env.example` aponta `VITE_API_BASE_URL=http://localhost:8080/api` mas o frontend real usa `/api/v1`** — [`.env.example:23`](.env.example:23) vs [`frontend/.env:1`](frontend/.env:1) (`http://localhost:8080/api/v1`). Quem copiar o example e rodar o frontend terá 404 em todos os endpoints (controllers mapeiam `/api/v1/*`). O `AuthController` aceita `/api/auth` legado, mas os demais controllers não. **Ação:** corrigir o example para `/api/v1`.
- **[NIT-8] `k8s/aegis-app.yaml` placeholder `aegispatrimonio:0.0.1`** — [`k8s/aegis-app.yaml:58`](k8s/aegis-app.yaml:58). O pom versiona `0.0.1-SNAPSHOT`; o comentário já explica que o CI deve injetar a tag real, então é aceitável — apenas registrar que o deploy manual com esse valor falhará no pull se a imagem não existir localmente.

## 4. Cobertura de Testes

**Pontos positivos:**
- [`TokenDenylistServiceTest`](src/test/java/br/com/aegispatrimonio/security/TokenDenylistServiceTest.java:1): cobre revoke, idempotência, token expirado, eviction lazy e periódico — excelente para uma classe nova.
- Guards de filial: [`ManutencaoServiceTest`](src/test/java/br/com/aegispatrimonio/service/ManutencaoServiceTest.java:388) e [`MovimentacaoServiceTest`](src/test/java/br/com/aegispatrimonio/service/MovimentacaoServiceTest.java:192) cobrem caminho negado (AccessDenied sem save) e permitido (save executado) — os dois caminhos críticos.
- [`DashboardServiceTest.shouldThrowWhenTenantContextIsNotSet`](src/test/java/br/com/aegispatrimonio/service/DashboardServiceTest.java:78) cobre o guard novo.
- ITs atualizados para o modelo RBAC granular (setup de `rbac_user_role`/`rbac_role_permission`), validando V16 end-to-end em H2.

**Findings:**

- **[MAJOR-9] Logout server-side sem teste de integração** — nenhum IT cobre `POST /auth/logout` seguido de request com o mesmo token esperando 401 (o fluxo completo denylist→JwtService→filtro). [`TokenDenylistServiceTest`](src/test/java/br/com/aegispatrimonio/security/TokenDenylistServiceTest.java:1) cobre a unidade e [`JwtServiceTest`](src/test/java/br/com/aegispatrimonio/security/JwtServiceTest.java:116) cobre `isTokenValid` com denylist, mas a costura HTTP (header parse, 204 idempotente, token inválido) não tem cobertura. É o único comportamento novo sem IT. **Ação:** adicionar caso no `AuthControllerIT` (login → logout → reuso do token → 401; logout sem token → 204).
- **[MINOR-10] Loop paginado unpaged do `AtivoService` sem teste** — [`AtivoService.java:173-191`](src/main/java/br/com/aegispatrimonio/service/AtivoService.java:173) não tem teste unitário (o `AtivoServiceTest` existente cobre só o caminho paged/filters). O total do `PageImpl` e a terminação do loop são sutis o suficiente para merecer um teste com mock retornando 2 páginas. **Ação:** teste com `findByFilters` mockado retornando páginas de 500/2 registros, assert de `getTotalElements()` e conteúdo completo.
- **[MINOR-11] `handleResponse` 401 global sem teste frontend** — nenhum e2e/unit cobre o redirect para `/login` em 401 (o `login.spec.js` cobre só o fluxo feliz). Aceitável dado não haver suíte unit frontend, mas registrar.

## 5. Migrations (V15/V16)

**Pontos positivos:**
- V15: `CREATE TABLE IF NOT EXISTS` com comentário explicando o porquê (tabela pode existir por ddl-auto histórico); estrutura fiel à entidade [`Alerta`](src/main/java/br/com/aegispatrimonio/model/Alerta.java:17); `alertas_aud` segue o padrão Envers de V2/V14.
- V16: idempotência real via `INSERT ... SELECT ... WHERE NOT EXISTS` sobre as UNIQUEs de V6 (`uk_permission_resource_action`, PK de `rbac_role_permission`) — verificado; comentários rastreiam decisões (N4, N5) ao git history; concessão USER espelha exatamente o modelo pre-H1 (verificado contra commit 4d815dd).

**Findings:**

- **[MAJOR-12] `CREATE INDEX idx_alertas_ativo_id` não é idempotente** — [`V15__create_alertas_table.sql:19`](src/main/resources/db/migration/V15__create_alertas_table.sql:19). As tabelas usam `IF NOT EXISTS`, mas o `CREATE INDEX` não tem guarda. Em produção onde a tabela já existia (o cenário que o `IF NOT EXISTS` explicitamente cobre), o índice provavelmente também já existe → migration falha com "duplicate index". O comentário do cabeçalho declara a migration "idempotente para produção", o que é falso para esta linha. **Ação:** `CREATE INDEX IF NOT EXISTS` (suportado no MySQL 8.0.29+ e H2) ou dropar/recriar com guarda. Este é o achado mais próximo de bloqueio: falha condicional de migração em produção. Nota: como Flyway grava checksum e roda V15 uma única vez, o risco só se materializa no primeiro deploy pós-correção em banco legado — exatamente o cenário-alvo da correção.
- **[NIT-13] V16 verbosidade** — 194 linhas com 30+ INSERTs individuais poderiam ser uma tabela temporária + JOIN, mas a forma atual é explícita, auditável e alinhada ao estilo de V4/V6. Manter como está.

## 6. Dead code / resíduos

- **[MINOR-14] `findAllWithDetails` e `findByFilialIdInWithDetails` órfãos** — [`AtivoRepository.java:29,48`](src/main/java/br/com/aegispatrimonio/repository/AtivoRepository.java:29). Com a substituição do caminho unpaged (M2), nenhum caller resta em `src` (verificado por busca). Deixar queries FETCH pesadas sem uso convida a reintroduzir o problema que a correção eliminou. **Ação:** remover na próxima limpeza (ou marcar `@Deprecated` com referência ao M2).
- **[NIT-15] `authInterceptor` legado mantido** — [`api.js:115`](frontend/src/services/api.js:115). Comentario já declara que é legado; ainda é reexportado por `services/index.js`. Remover quando confirmado sem uso.
- ✅ Resíduos limpos: `application-test.properties-bkp` deletado; `console.log` de token removidos de `LoginView`/`ScannerView`/`api.js`; botão Google duplicado removido de `LoginView` (o bloco "ou continue com" permanece, correto).

---

## Veredicto

### **APROVADO COM RESSALVAS**

**Justificativa:** As correções são corretas nos pontos críticos que pediam verificação (denylist thread-safe com eviction, total do `PageImpl` no loop paginado, paridade de permissões N4 verificada contra o git history, handleResponse com edge cases cobertos), bem testadas nos guards novos, e seguem as convenções do codebase. Os 3 testes que falham são pré-existentes no HEAD (evidenciado em worktree limpo) e não foram introduzidos por esta sessão.

**Ressalvas (não bloqueiam merge, devem entrar no backlog):**
1. **MAJOR-12** — `CREATE INDEX` sem guarda em V15: falha condicional de migração no cenário legado que a própria migration declara cobrir. Corrigir antes do primeiro deploy em banco existente (se o deploy for só em banco novo, pode seguir).
2. **MAJOR-9** — IT do logout server-side (fluxo 401 pós-revogação).
3. **MAJOR-4** — extrair guard de filial duplicado (DRY).
4. **MAJOR-1** — `Sort` explícito no loop paginado.
5. **MINOR-7** — `.env.example` com base URL errada (`/api` vs `/api/v1`).
6. **MINOR-3/10/14** — teste `JwtAuthFilterTest` desatualizado, teste do loop unpaged, repositórios FETCH órfãos.

Nada foi corrigido nesta revisão (modo read-only). Findings devem ser roteados ao Software Engineer / Backend / Database Specialist via `new_task`.
