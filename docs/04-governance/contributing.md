# Contributing & Onboarding Guide — AegisPatrimônio (Sistema de Gestão de Patrimônio)

> Bem-vindo(a)! Este guia leva você do zero ao primeiro PR mergeado — com transparência total sobre o que já está automatizado no projeto e o que ainda depende de verificação ou de decisão do time.

> **Versão:** 1.0 · **Owner:** Engenharia · **Status:** Draft
> **Fontes:** artefato **Development Environment (dev-environment)** v1.0; diagnóstico determinístico do codebase (varredura AST real — 350 arquivos, 1.268 funções, 344 classes, 27.537 LOC); `package.json` do workspace.
> **Aviso de escopo:** o `package.json` da raiz declara **1 dependência de produção** (`@popperjs/core` ^2.11.8), **0 de desenvolvimento** e **nenhum script** (`dev`, `build`, `start`, `test`, `lint` — todos ausentes). Onde um passo depende de informação não descobrível (versão do Java, ferramenta de build do backend, motor de banco, canais do time), o item é registrado como **pendência** ou com o marcador **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**, em vez de presumido.

## 1. Primeiros Passos

> Fonte primária: **dev-environment**, Seções 1, 2 e 6. Nenhum comando é inventado neste guia.

1. **Instalar os pré-requisitos** (dev-environment, Seção 1):
   - **Java (JDK)** — backend com 335 arquivos `.java` em `src/`, pacote raiz `br.com.aegispatrimonio`. **Versão não especificada** — confirmar no arquivo de build do backend antes de instalar.
   - **npm** — compatível com o `package.json` presente na raiz **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (a existência do `package.json` é fato verificado; o gerenciador efetivamente usado pelo time não está registrado nos artefatos).
   - **Git** — clonagem e versionamento do repositório.
   - **Ferramenta de build do backend Java** — obrigatória para compilar os 335 arquivos `.java`, porém **ainda não identificada** — identificar com o time antes de concluir o setup.
   - **Docker/Docker Compose não são pré-requisitos** — nenhuma configuração de container existe no codebase.
2. **Clonar o repositório:** `git clone <URL_DO_REPOSITÓRIO>` — a URL não está registrada nos artefatos verificados; obter com o time. Estrutura esperada: `src/` (backend Java) e `frontend/` (15 arquivos `.js`) — ver Seção 2.
3. **Instalar dependências JavaScript:** `npm install` na raiz do workspace (instala `@popperjs/core` ^2.11.8, única dependência de produção declarada). **Nenhuma lockfile foi reportada pela varredura** — versões resolvidas podem variar entre instalações. **Dependências do backend Java** residem no arquivo de build do backend, não coberto pela varredura de stack — o comando para resolvê-las é **pendência de verificação**.
4. **Configurar variáveis de ambiente:** **nenhum `.env.example` foi encontrado no workspace**. Se o time mantiver um modelo, copiá-lo para `.env` e preencher os valores; caso contrário, consultar a **Seção 3 do dev-environment**, que lista variáveis candidatas inferidas (conexão com banco de dados, segredos de autenticação, porta) — **todas requerem validação humana antes do uso**.
5. **Popular dados de exemplo (seed):** existe a classe `RealisticDataSeeder` (`src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34`), porém **o método de invocação não é verificável** (nenhum script declarado; disparo automático na inicialização vs. manual não detectável pela varredura). Recomendação do dev-environment: executar o seeder em **staging, com validação**, antes de qualquer carga em produção. Não confundir o seeder (carga de dados) com migração de schema — nenhuma ferramenta de migração foi verificada no codebase (dev-environment, Seção 2, item 6).
6. **Rodar o projeto localmente:** ⚠️ **comando não descoberto** — o `package.json` não declara nenhum script e a ferramenta de build do backend não foi identificada. Este passo será preenchido após a verificação da toolchain (dev-environment, Seção 2, item 8).
7. **Rodar a suíte de testes:** ⚠️ **Este projeto NÃO possui suíte de testes configurada** — não existe script `test` no `package.json` (executar `npm test` falhará) e nenhuma ferramenta de teste foi encontrada nas dependências. As verificações devem ser feitas via **compilação do backend (quando a ferramenta de build for definida) e inspeção manual** — ver Seção 5.5.
8. **Verificar se o ambiente está saudável:** seguir o **Environment Verification Checklist** do dev-environment (Seção 6):
   - [ ] `npm install` executa sem erros na raiz do workspace (instala `@popperjs/core` ^2.11.8)
   - [ ] Build do backend Java compila sem erros — **comando a definir** (ferramenta de build não verificada)
   - [ ] Aplicação inicia e responde localmente — **comando a definir**
   - [ ] Banco de dados acessível e com schema aplicado — **motor a definir** (lacuna 1 do SAD)
   - [ ] Seeder `RealisticDataSeeder` executado com sucesso (método de invocação a verificar); quando envolver dados legados, executar em staging com validação
   - [ ] Endpoints `/health` e `/metrics` respondem (existência não confirmada no codebase — lacuna 5 do SAD)
   - [ ] Variáveis de ambiente sensíveis não commitadas — confirmar que `.env` (se usado) está no `.gitignore`
   - [ ] Dependências do backend resolvidas pelo arquivo de build do backend

## 2. Estrutura do Projeto

```
.
├── package.json                # Raiz — 1 dependência de produção (@popperjs/core ^2.11.8), 0 de desenvolvimento, nenhum script
├── src/                        # Backend Java — 335 arquivos .java, pacote raiz br.com.aegispatrimonio
│   └── main/
│       └── java/br/com/aegispatrimonio/
│           ├── config/
│           │   └── seeder/     # Carga de dados de exemplo (RealisticDataSeeder)
│           ├── mapper/         # Conversão para DTO (ex.: AtivoMapper.toDTO)
│           ├── model/          # Entidades de domínio (ex.: Usuario)
│           ├── repository/     # Acesso a dados e specifications (ex.: ManutencaoSpecification)
│           └── service/        # Regras de negócio (ex.: AtivoService, AlertNotificationService)
└── frontend/                   # Frontend JavaScript — 15 arquivos .js
    └── src/
        └── services/           # Integração com o backend (api.js — função request, linha 54)
```

Notas:
- A árvore destaca apenas os diretórios **referenciados nos achados estruturais** da varredura; os demais subdiretórios (backend e frontend) não foram enumerados no diagnóstico. Apenas `src/main/java` está evidenciado — a existência de `src/test/` não foi verificada.
- O `package.json` da raiz **não possui campo `name`** — recomenda-se preenchê-lo para facilitar a identificação do workspace em ferramentas.
- A integração frontend↔backend ocorre via `frontend/src/services/api.js` (função `request`, linha 54); a URL base do backend usada por esse client **não é descobrível** pelos artefatos atuais.
- Nenhuma porta de servidor, de banco ou de deploy foi verificada no codebase — ver dev-environment, Seção 5.

## 3. Padrões de Commit

**[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** — nenhuma convenção de commit é deduzível do codebase: não foram encontrados hooks de Git, configuração de CI nem documentação de contribuição no workspace. A convenção abaixo é uma **sugestão** que precisa ser ratificada pelo time antes de se tornar oficial.

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Premissas adotadas: adoção de Conventional Commits e mensagens em português, por se tratar de equipe e domínio em português (`br.com.aegispatrimonio`).

* **Convenção sugerida:** Conventional Commits — `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
* **Exemplo:** `fix(manutencoes): corrige filtro de especificação por status`
* **Regras adicionais sugeridas:** commits atômicos (um propósito por commit); referenciar a issue relacionada no rodapé da mensagem quando aplicável.

## 4. Fluxo de Contribuição (Branching & PRs)

1. **Criar branch a partir de `main`:** `git checkout -b feature/nome-da-feature` — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (branch padrão `main` e convenção de nomes de branch não são verificáveis; nenhuma configuração de branching/CI existe no codebase).
2. **Desenvolver** seguindo os padrões de código da Seção 5.
3. **Verificação local:** ⚠️ **não há testes nem lint automatizados** — garantir que (a) o backend compila (ferramenta de build a definir), (b) nenhuma nova chamada `console.*` foi adicionada ao frontend, (c) o checklist do dev-environment (Seção 6) foi percorrido.
4. **Abrir Pull Request** com descrição clara (o quê, por quê, como testar) — usar o template abaixo.
5. **Solicitar revisão** de pelo menos **[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** revisor(es) — o número mínimo de revisores é decisão do time.
6. **Endereçar comentários** e aguardar aprovação.
7. **Merge** via **[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** (squash/rebase — estratégia não deduzível do código). O requisito de "CI verde" **não se aplica hoje**: não há pipeline de CI configurado (lacuna 3 do SAD, conforme dev-environment).

### Template de PR sugerido

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — adaptado à realidade do projeto (sem suíte de testes automatizada):

```markdown
## O que este PR faz
## Por que é necessário
## Como testar
(Descrever a verificação manual realizada — este projeto não possui suíte de testes automatizada)
## Checklist
- [ ] Backend compila sem erros (ferramenta de build definida pelo time)
- [ ] Verificação manual do fluxo afetado realizada
- [ ] Nenhuma nova chamada `console.*` adicionada ao frontend
- [ ] Complexidade ciclomática não aumentada nos hotspots (Seção 5.4)
- [ ] Nenhuma variável de ambiente/credencial commitada
- [ ] Documentação atualizada
- [ ] Breaking changes documentados (se houver)
```

## 5. Padrões de Código

### 5.1 Linguagens e stack verificada
* **Backend:** Java — 335 arquivos `.java` em `src/`, pacote raiz `br.com.aegispatrimonio`. Versão do JDK: **a confirmar** no arquivo de build do backend.
* **Frontend:** JavaScript — 15 arquivos `.js` em `frontend/`.
* **Topologia:** aplicação web/server-side.
* **Dependências:** 1 de produção (`@popperjs/core` ^2.11.8), 0 de desenvolvimento. Nenhum ORM/query builder declarado — o acesso a dados provavelmente ocorre via SQL cru (**a confirmar**). Motor de banco: **não especificado** (lacuna 1 do SAD).

### 5.2 Estilo e lint
* **Nenhum linter/formatador está configurado** — 0 dev dependencies no `package.json` e nenhuma configuração de estilo automatizado encontrada no workspace. A padronização automatizada de código é **pendência** (relacionada ao NFR-M01, conforme dev-environment).
* Até a implementação de um linter, o estilo é verificado **manualmente na revisão de código** (Seção 6), usando as convenções observadas (5.3) como referência.

### 5.3 Convenções de nomenclatura (observadas no código real)
* **Java:** pacotes em minúsculas com namespace reverso (`br.com.aegispatrimonio`); classes em PascalCase nomeadas por responsabilidade (`RealisticDataSeeder`, `AtivoMapper`, `AlertNotificationService`, `ManutencaoSpecification`); métodos em camelCase (`toDTO`, `build`, `checkResourceUsageAlerts`, `setUsername`).
* **Organização em pacotes por responsabilidade:** `config/seeder/` (carga de dados), `mapper/` (conversão para DTO), `model/` (entidades), `repository/` (acesso a dados/specifications), `service/` (regras de negócio).
* **Frontend:** clients de integração em `frontend/src/services/` (ex.: `api.js`).

### 5.4 Pontos de atenção no código atual (hotspots)
A varredura AST detectou **12 funções com complexidade ciclomática alta** (5 listadas no diagnóstico), 1 stub, 1 TODO de performance e 2 chamadas `console.*` residuais. Ao contribuir, **não aumente a complexidade** desses pontos e, sempre que tocá-los, considere quebrar em funções/métodos menores:

| Local | Achado | Orientação ao contribuir |
| :--- | :--- | :--- |
| `frontend/src/services/api.js:54` | Função `request` — complexidade 13 | Ao estender o client HTTP, extrair funções menores |
| `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34` | Método `run` — complexidade 15 | Quebrar em métodos menores |
| `src/main/java/br/com/aegispatrimonio/mapper/AtivoMapper.java:15` | Método `toDTO` — complexidade 14 | Quebrar em métodos menores |
| `src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26` | Método `build` — complexidade 14 | Quebrar em métodos menores |
| `src/main/java/br/com/aegispatrimonio/service/AlertNotificationService.java:96` | Método `checkResourceUsageAlerts` — complexidade 17 | Quebrar em métodos menores |
| `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86` | `setUsername` com corpo vazio (stub) | Verificar se é intencional antes de alterar comportamento |
| `src/main/java/br/com/aegispatrimonio/service/AtivoService.java:119` | TODO (perf): carrega até 1000 candidatos (id+nome) e faz ranking | Cuidado com performance ao modificar este caminho |
| `frontend/src/services/api.js:44` e `:107` | `console.error(...)` e `console.debug(...)` residuais | Não adicionar novas chamadas `console.*`; remover as existentes antes de produção |

Para localizar débito técnico registrado: `grep -rn "TODO\|FIXME" src/ frontend/` e `grep -rn "console\." frontend/src` (dev-environment, Seção 8).

### 5.5 Testes
* **Este projeto NÃO possui suíte de testes configurada** — não existe script `test` no `package.json` (não execute `npm test`; o comando falhará) e nenhuma ferramenta de teste foi encontrada nas dependências.
* **Verificação obrigatória atual:** compilação do backend (ferramenta de build a definir) + inspeção manual do fluxo afetado + revisão de código (Seção 6).
* A implementação da suíte de testes com gate de cobertura > 80% (NFR-M01) e dos scripts de execução é **pendência registrada** (lacuna 3 do SAD, conforme dev-environment).

## 6. Revisão de Código (Code Review Guidelines)

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — nenhuma diretriz de revisão existe nos artefatos verificados; as premissas adotadas abaixo requerem ratificação do time.

* **O que revisores devem verificar:**
  * **Corretude** e legibilidade do código.
  * **Complexidade:** o PR não deve aumentar a complexidade ciclomática dos hotspots listados na Seção 5.4.
  * **Segurança:** nenhuma variável de ambiente/credencial commitada; nenhuma nova chamada `console.*` com dados potencialmente sensíveis no frontend.
  * **Consistência** com as convenções observadas (Seção 5.3) e com a organização de pacotes existente.
  * **Como o PR foi testado manualmente** — como não há suíte automatizada, a descrição de teste no PR é a principal evidência de validação.
* **Tempo esperado de resposta a um PR:** **[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** — sugestão inferida: 1 dia útil.
* **Como dar feedback construtivo:** sugerir mudanças com justificativa técnica; distinguir explicitamente **bloqueios** (corretude, segurança) de **preferências** (estilo), evitando ambiguidade sobre o que impede o merge.

## 7. Reportando Bugs

* **Onde reportar:** **[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** — nenhum issue tracker está registrado nos artefatos verificados; o canal oficial deve ser definido pelo time.
* **Template sugerido** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**:
  * Passos para reproduzir (numerados e mínimos);
  * Comportamento esperado vs. comportamento atual;
  * Ambiente: SO, versão do JDK (a confirmar no arquivo de build do backend), trecho de log/erro;
  * Arquivo/fluxo afetado, se conhecido (ex.: `frontend/src/services/api.js`, `AtivoService`, `ManutencaoSpecification`).

## 8. Propondo Novas Funcionalidades

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — nenhum processo de RFC/discussão prévia existe nos artefatos verificados. Premissa adotada: proposta registrada em issue para discussão com o time **antes** da implementação. Pontos que exigem decisão explícita da equipe, dado o estado atual da stack:

* **Definição do motor de banco de dados** — não especificado (lacuna 1 do SAD); qualquer feature que dependa de persistência fica bloqueada até a decisão.
* **Ferramenta de build do backend e scripts de execução** — não identificados (lacuna 3); features que alterem a toolchain precisam ser acordadas antes.
* **Inclusão de novas dependências** — o `package.json` declara apenas `@popperjs/core` ^2.11.8; toda nova dependência deve ser justificada na proposta.

## 9. Código de Conduta

**[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** — nenhum `CODE_OF_CONDUCT.md` foi encontrado no workspace; a criação do documento formal é pendência do time. Resumo do comportamento esperado **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**: respeito mútuo, comunicação profissional e inclusiva em issues e revisões de código, e canal de denúncia a definir pela equipe.

## 10. Contatos & Suporte

**[ENTRADA HUMANA NECESSÁRIA — não gerado a partir do código]** — nenhum canal de suporte está registrado nos artefatos verificados. Preencher com o time:

| Canal | Uso | Status |
| :--- | :--- | :--- |
| Chat da equipe (ferramenta a definir) | Dúvidas rápidas de desenvolvimento | A definir pelo time |
| Issue tracker (a definir) | Bugs e propostas de features | A definir pelo time |
| Email da equipe (a definir) | Questões sensíveis (segurança, conduta) | A definir pelo time |

## 11. Lacunas de Verificação que Impactam o Onboarding

> Espelho adaptado da Seção 9 do dev-environment. Estes itens bloqueiam a conclusão do onboarding e devem ser resolvidos para que este guia possa ser completado.

| # | Lacuna | Impacto no onboarding |
| :--- | :--- | :--- |
| 1 | Motor de banco de dados não especificado | Bloqueia setup (banco, migrações), variáveis de conexão e troubleshooting — Seção 1, itens 4, 5 e 8 |
| — | Versão do Java não especificada | Bloqueia a instalação do JDK — Seção 1, item 1 |
| — | Ferramenta de build do backend não identificada | Bloqueia compilação, execução local e resolução de dependências do backend — Seção 1, itens 1, 3, 6 e 8 |
| 3 | Pipeline CI/CD e scripts ausentes (`package.json` sem scripts) | Sem `npm run dev/build/test/lint`; sem gate de CI no fluxo de PR — Seções 1, 4 e 5.5 |
| 5 | Endpoints `/health` e `/metrics` não confirmados | Health check local pendente — Seção 1, item 8 |
| 6 | Configuração de deploy/infraestrutura de produção ausente | Paridade entre ambiente local e produção não documentável |
| — | Convenções de commit/branching/revisão/canais de suporte não definidas | Seções 3, 4, 6, 9 e 10 dependem de decisão da equipe |