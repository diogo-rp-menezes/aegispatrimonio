# Unified Development Environment Configuration — Sistema de Gestão de Patrimônio (A4)

> **Versão:** 1.0 · **Owner:** Engenharia · **Status:** Draft
> **Fontes:** [[system-architecture]] (SAD v1.0), diagnóstico determinístico do codebase (varredura AST real — 350 arquivos, 1.268 funções, 344 classes, 27.537 LOC), `package.json` do workspace.
> **Aviso de escopo:** este documento reflete **exclusivamente** o que foi verificado no workspace. O `package.json` da raiz declara **1 dependência de produção** (`@popperjs/core` ^2.11.8), **0 de desenvolvimento** e **nenhum script** — não há toolchain de build/teste/execução verificável pelos artefatos atuais. Onde um passo de setup depende de informação não descobrível (versão do Java, ferramenta de build do backend, motor de banco, portas, variáveis de ambiente), o item é registrado como **pendência de verificação** em vez de presumido.

## 1. Local Prerequisites & Tooling

* **Runtime:**
  * **Java** (backend) — 335 arquivos `.java` em `src/`, pacote `br.com.aegispatrimonio`. **Versão não especificada** no diagnóstico — confirmar no arquivo de build do backend antes de instalar o JDK (o arquivo de build do backend não foi coberto pela varredura de stack, que analisou apenas o `package.json` raiz).
  * **JavaScript** (frontend) — 15 arquivos `.js` em `frontend/`. A forma de execução/servimento do frontend **não é verificada** (nenhuma dev dependency e nenhum script no `package.json`).
* **Gerenciador de pacotes:** **npm** — compatível com o `package.json` presente na raiz do workspace **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — a existência do `package.json` é fato verificado; o gerenciador efetivamente utilizado pelo time não está registrado nos artefatos]**. Nenhuma lockfile foi reportada pela varredura.
* **Ferramentas obrigatórias:**
  * **Git** — clonagem e versionamento do repositório.
  * **Ferramenta de build do backend Java** — obrigatória para compilar e executar os 335 arquivos `.java`, porém **não identificada** pela varredura de stack — **identificar e registrar antes de concluir o setup** (lacuna de verificação do SAD, Seção 2).
  * **Ferramentas de containerização (Docker/Docker Compose): não verificadas** — nenhuma configuração de container existe no codebase; **não são pré-requisito** deste ambiente.
* **Editores recomendados & extensões:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — recomendação genérica para as duas linguagens presentes (Java e JavaScript): VS Code com extensões de suporte a Java e JavaScript, ou IDE Java dedicada. **Nenhum linter/formatador está configurado no projeto** (0 dev dependencies no `package.json`) — a padronização automatizada de código é pendência (relacionada ao NFR-M02).
* **Versão mínima do SO/Shell:** **Não descobrível** — nenhuma configuração de SO, shell ou CI foi encontrada no codebase. Qualquer SO com suporte ao JDK e ao npm é adequado; confirmar com o time se existe restrição de ambiente de desenvolvimento.

Observação de higiene: o `package.json` da raiz **não possui campo `name`** — recomenda-se preenchê-lo para facilitar a identificação do workspace em ferramentas.

## 2. Setup Passo a Passo

1. **Instalar os pré-requisitos** listados na Seção 1 — JDK (versão a confirmar no arquivo de build do backend), npm e Git.
2. **Clonar o repositório:** `git clone <URL_DO_REPOSITÓRIO>` — a URL não está registrada nos artefatos verificados; obter com o time. Estrutura esperada: `src/` (backend Java, pacote `br.com.aegispatrimonio`) e `frontend/` (15 arquivos `.js`).
3. **Variáveis de ambiente:** **nenhum arquivo `.env.example` foi identificado na varredura**. Se o time mantiver um modelo, copiá-lo para `.env`; caso contrário, usar a Seção 3 (variáveis candidatas inferidas — requerem validação).
4. **Instalar dependências JavaScript:** `npm install` na raiz do workspace (instala `@popperjs/core` ^2.11.8, única dependência de produção declarada). **Dependências do backend Java:** residem no arquivo de build do backend, **não coberto pela varredura** — o comando para resolvê-las é pendência de verificação.
5. **Subir serviços auxiliares (banco, cache):** **nenhum passo verificável** — não há configuração de containers nem de serviços auxiliares no codebase, e o **motor de banco não está especificado** (lacuna 1 do SAD/NFR — pendência de maior impacto). O passo genérico de subida de containers do template **não se aplica** à stack verificada. O provisionamento local do banco só será possível após a definição do motor.
6. **Rodar migrações:** **nenhuma ferramenta de migração foi verificada no codebase** (SAD, Seção 5) — estratégia a definir em [[db-migration-spec]]. Não confundir com o seeder (item 7), que é carga de dados, não migração de schema.
7. **Popular dados de exemplo (seed):** existe a classe `RealisticDataSeeder` (`src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34`), porém **o método de invocação não é verificável** pelos artefatos atuais (nenhum script declarado; forma de disparo — automática na inicialização vs. manual — não detectável pela varredura). Atenção: por ser relevante ao risco R-02 do BRD (migração de dados legados), o SAD recomenda executar o seeder em **staging, com validação**, antes de qualquer carga em produção.
8. **Iniciar a aplicação:** **comando não descoberto** — o `package.json` não declara scripts e o arquivo de build do backend não foi coberto pela varredura (lacuna 3 do SAD — pipeline/scripts de execução). Este passo será preenchido após a verificação da toolchain de build/execução.

## 3. Variáveis de Ambiente

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Nenhum arquivo `.env`/`.env.example` e nenhuma variável de ambiente foram identificados na varredura do workspace. A tabela abaixo lista **apenas variáveis candidatas inferidas** dos requisitos arquiteturais (SAD/[[nfr]]) — **nenhuma delas está confirmada no código**. Premissas adotadas: autenticação JWT (NFR-SEC01) exige segredo de assinatura; o acesso a dados via SQL cru exige configuração de conexão; a blocklist de logout (CA-08) pode exigir configuração própria.

| Variável | Obrigatória | Descrição | Valor de exemplo |
| :--- | :--- | :--- | :--- |
| `JWT_SECRET` | Sim (inferido) | Chave de assinatura dos tokens JWT (NFR-SEC01); rotação a cada 90 dias (NFR-SEC10) | `<string-secreta-forte-única-por-ambiente>` |
| `DB_URL` | Sim (inferido) | URL de conexão do banco — **motor ainda não definido** (lacuna 1 do SAD) | `<url-de-conexão-do-motor-a-definir>` |
| `DB_USER` | Sim (inferido) | Usuário do banco — usar credencial de menor privilégio | `<usuario-do-banco>` |
| `DB_PASSWORD` | Sim (inferido) | Senha do banco — nunca commitar; gerenciar via mecanismo de segredos | `<senha-do-banco>` |
| `SERVER_PORT` | Não (inferido) | Porta de escuta da aplicação — **nenhuma porta verificada no codebase** | `<porta-a-definir>` |

Nota: os nomes exatos das variáveis (incluindo convenção de prefixo) devem ser confirmados no código/arquivo de build do backend quando verificados.

## 4. Secure Command Sandbox Recipes

> Comandos permitidos para automação/agentes, com limites de segurança explícitos. Receitas de build/execução do backend **não podem ser definidas** até que a ferramenta de build do backend seja verificada (lacuna 3 do SAD).

| Target Tool | Command Type | Recipe | Timeout | Restrições |
| :--- | :--- | :--- | :--- | :--- |
| npm | install | `npm install --no-audit --no-fund` | 120s | Acesso à rede somente ao registry configurado; executar apenas na raiz do workspace |
| git | consulta (somente leitura) | `git status --porcelain` · `git log --oneline -n 20` | 15s | Sem `commit`, `push` ou alteração de histórico |
| shell (grep) | busca estática no código | `grep -rn "TODO\|FIXME" src/ frontend/` | 15s | Somente leitura dentro do workspace; sem modificação de arquivos |
| Build backend | compile | **Não definível** — ferramenta de build do backend não verificada | — | Pendência: definir receita após verificação da ferramenta (lacuna 3) |
| App runtime | run | **Não definível** — comando de inicialização não descoberto | — | Pendência: definir receita após verificação da toolchain (lacuna 3) |

## 5. Serviços Locais & Portas

**Nenhuma porta foi verificada no codebase** — não há configuração de servidor, de banco ou de deploy nos artefatos analisados. A tabela abaixo registra os serviços esperados e o estado de verificação de cada um:

| Serviço | Porta | Descrição |
| :--- | :--- | :--- |
| Backend/API (`src/`, `br.com.aegispatrimonio`) | **A verificar** | Aplicação principal — nenhuma configuração de porta encontrada |
| Banco de dados | **A verificar** | Motor **não especificado** (lacuna 1 do SAD) — porta indefinida até a escolha do motor |
| Frontend (`frontend/`) | **A verificar** | Forma de servir/acessar o frontend não verificada (nenhum script nem configuração) |

Observações:
- Os endpoints de observabilidade `/health` e `/metrics` (NFR-O01) **não estão confirmados no codebase** (lacuna 5 do SAD) — a verificação de saúde local depende da confirmação deles.
- A integração frontend↔backend ocorre via `frontend/src/services/api.js` (função `request`, linha 54) — a URL base do backend usada por esse client não é descobrível pelos artefatos atuais.

## 6. Environment Verification Checklist

> Itens adaptados à stack verificada. Os itens genéricos do template `npm run dev`, `npm test` e `npm run lint` foram substituídos porque o `package.json` **não declara nenhum script** (lacuna 3 do SAD).

- [ ] `npm install` executa sem erros na raiz do workspace (instala `@popperjs/core` ^2.11.8)
- [ ] Build do backend Java compila sem erros — **comando a definir** (ferramenta de build não verificada)
- [ ] Aplicação inicia e responde localmente — **comando a definir** (lacuna 3)
- [ ] Banco de dados acessível e com schema aplicado — **motor a definir** (lacuna 1)
- [ ] Seeder `RealisticDataSeeder` executado com sucesso (método de invocação a verificar); quando envolver dados legados, executar em staging com validação (risco R-02)
- [ ] Endpoints `/health` e `/metrics` respondem (existência não confirmada — lacuna 5)
- [ ] Variáveis de ambiente sensíveis não commitadas — confirmar que `.env` (se usado) está no `.gitignore`
- [ ] Dependências do backend resolvidas pelo arquivo de build do backend (não coberto pela varredura de stack)

## 7. Troubleshooting Comum

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — Nenhum registro de problemas de ambiente existe nos artefatos verificados; as entradas abaixo são hipóteses genéricas de troubleshooting, com as pendências de verificação reais do projeto destacadas.

| Sintoma | Causa Provável | Solução |
| :--- | :--- | :--- |
| `npm install` falha com erro de rede | Registry inacessível ou proxy corporativo | Verificar conectividade com o registry configurado e configuração de proxy |
| Erro de compilação no backend Java | Versão do JDK incompatível | Confirmar a versão exigida no arquivo de build do backend — **versão não especificada no diagnóstico** |
| Porta em uso ao iniciar a aplicação | Outro processo ocupando a porta | Encerrar o processo: `kill -9 $(lsof -ti:PORTA)` (ou equivalente do SO) |
| Falha de conexão com o banco de dados | Motor/credenciais não configurados | **Motor ainda não definido (lacuna 1)** — definir a conexão conforme Seção 3 após a escolha do motor |
| Seeder não executa | Método de invocação não documentado | Verificar como `RealisticDataSeeder` (`config/seeder/`) é disparado — nenhum script declarado no `package.json` |
| Frontend não alcança o backend | URL base incorreta em `frontend/src/services/api.js` | Conferir a URL base do backend na função `request` (linha 54) — valor não descobrível pelos artefatos atuais |

## 8. Comandos Úteis

| Comando | Descrição |
| :--- | :--- |
| `npm install` | Instala as dependências declaradas no `package.json` da raiz (`@popperjs/core` ^2.11.8) |
| `git status` / `git log --oneline` | Inspeção do estado do repositório |
| `grep -rn "TODO\|FIXME" src/ frontend/` | Localiza débito técnico registrado (ex.: `AtivoService.java:119`) |
| `grep -rn "console\." frontend/src` | Localiza chamadas `console.*` residuais (ex.: `frontend/src/services/api.js:44` e `:107`) |
| Build do backend | **Comando a definir** — ferramenta de build do backend não verificada (lacuna 3) |
| Execução do seeder | **Comando a definir** — ver Seção 2, item 7 |

Observação final: **nenhum script npm de desenvolvimento existe** (`dev`, `build`, `test`, `lint` — todos ausentes do `package.json`); a implementação do pipeline com gate de cobertura > 80% (NFR-M01) e dos scripts de execução é pendência registrada (lacuna 3 do SAD).

## 9. Lacunas de Verificação que Bloqueiam Este Documento

| # | Lacuna | Impacto neste documento | Fonte |
| :--- | :--- | :--- | :--- |
| 1 | Motor de banco de dados não especificado | Bloqueia os passos 5–6 do setup, variáveis `DB_*`, portas e troubleshooting de conexão | SAD, Seções 2, 5 e 12 |
| — | Versão do Java não especificada | Bloqueia a instalação do JDK (passo 1 do setup) | SAD, Seção 2 |
| — | Ferramenta de build do backend não identificada | Bloqueia passos 4 e 8 do setup, receitas de sandbox e comandos úteis | SAD, Seção 2 |
| 3 | Pipeline CI/CD e scripts de execução ausentes | `package.json` sem scripts — bloqueia comandos de dev/build/test/lint e itens do checklist | SAD, Seções 2 e 11 |
| 4 | Ferramentas de APM/load testing/uptime/acessibilidade não definidas | Impede incluir comandos de verificação de performance no checklist | SAD, Seção 12 |
| 5 | Endpoints `/health` e `/metrics` não confirmados | Bloqueia o item de health check do checklist e a Seção 5 | SAD, Seções 10 e 12 |
| 6 | Configuração de deploy/infraestrutura de produção ausente | Impede documentar paridade entre ambiente local e produção | SAD, Seções 9 e 11 |