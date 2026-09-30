# Unified Development Environment Configuration — AegisPatrimônio (a6)

> **Versão:** 1.0 · **Owner:** Engenharia · **Status:** Draft
> **Método:** conteúdo derivado da varredura determinística do workspace + AST (`server/analyze-pipeline.ts`) e do System Architecture Document (SAD) v1.0. Itens não descobríveis nos fontes estão marcados como **[PENDENTE]** ou **[NÃO APLICÁVEL HOJE]** — nenhum dado de stack foi presumido além do verificado: `@popperjs/core ^2.11.8` (única dependência de produção), 0 devDependencies, nenhum motor de banco, nenhum ORM/query builder, sem empacotamento desktop.
> **Identificação:** o `package.json` do projeto está **sem nome** (gap confirmado — NFR-M06); o nome do produto segue o SAD v1.0.

---

## 1. Local Prerequisites & Tooling

* **Runtime (backend):** **Java** — 338 arquivos `.java` em `src/`, pacotes `br.com.aegispatrimonio.{model, repository, mapper, service, config.seeder}`. **Versão do JDK/runtime: não confirmada** — pendência registrada no SAD §2 ("a confirmar e documentar antes do go-live" — NFR-PO03).
* **Runtime (frontend):** **JavaScript** — 15 arquivos `.js` em `frontend/`, com camada de serviços centralizada em `frontend/src/services/api.js` (`request`, `authInterceptor`, `handleApiError`). Nenhum runtime de build/execução do frontend é descobrível a partir dos fontes (`package.json` sem scripts; sem lockfile verificado) — **[PENDENTE]**.
* **Gerenciador de pacotes (frontend):** `npm` — gerenciador canônico do manifesto `package.json` verificado. **Caveat:** nenhum lockfile foi registrado na varredura; o gerenciador efetivamente usado pela equipe requer confirmação. [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]
* **Gerenciador de dependências / ferramenta de build (backend):** **não documentada em nenhum artefato-fonte** (SAD §2/§12 não registram ferramenta). A árvore `src/main/java/br/com/aegispatrimonio/...` segue o layout de diretórios convencional de projetos Java, mas nenhum arquivo de build foi registrado na varredura — confirmação urgente com a engenharia. **[PENDENTE]**
* **Ferramentas obrigatórias (estado verificado):**
  * JDK Java — versão a confirmar (NFR-PO03) — para compilar e executar o backend;
  * `npm` (ou gerenciador compatível com `package.json`) — para instalar `@popperjs/core ^2.11.8`;
  * Pipeline de análise estática (`server/analyze-pipeline.ts`) — reproduz o baseline determinístico deste documento (353 arquivos · 1.287 funções · 347 classes · 27.912 LOC).
* **Ferramentas NÃO detectadas nos fontes (não presumir):** containerização/orquestração (nenhum arquivo verificado no workspace), banco de dados local (nenhum motor nas dependências — C-01 do BRD), ferramenta de build do backend, ferramentas de lint/formatação e suíte de testes (0 devDependencies; `package.json` sem scripts — NFR-M06).
* **Editores recomendados & extensões:** nenhuma configuração de editor foi registrada na varredura — recomendações ficam pendentes de definição pela equipe; nada foi presumido.
* **Versão mínima do SO/Shell:** não especificada em nenhum artefato-fonte; o sistema é uma aplicação web server-side de instância única (SAD §11), sem requisitos de SO detectáveis no código — **[PENDENTE]**.

---

## 2. Setup Passo a Passo

> Legenda de status: **[VERIFICADO]** = derivado dos fontes; **[PENDENTE]** = não descobrível, requer confirmação; **[NÃO APLICÁVEL HOJE]** = condição inexistente no estado atual do codebase.

1. **Instalar pré-requisitos** **[PENDENTE — parcialmente bloqueado]** — JDK Java (versão a confirmar, NFR-PO03) e `npm`/gerenciador compatível. Sem a versão do JDK confirmada, a compilação do backend não pode ser validada.
2. **Clonar o repositório** **[PENDENTE]** — a URL remota e o cliente de versionamento não estão documentados nos artefatos-fonte.
3. **Copiar `.env.example` para `.env`** **[NÃO APLICÁVEL HOJE]** — nenhum arquivo `.env`/`.env.example` foi detectado na varredura. Ao introduzir configuração externa, criar o `.env.example` documentado e manter o `.env` fora do versionamento (SAD §9 — nenhum segredo versionado no repositório).
4. **Instalar dependências:**
   * **Frontend** **[VERIFICADO]** — `npm install` no diretório que contém o `package.json` (instala `@popperjs/core ^2.11.8`, única dependência de produção; 0 devDependencies). Sem lockfile verificado — revisar artefatos de lock gerados antes de versionar.
   * **Backend** **[PENDENTE]** — comando não definível: nenhuma ferramenta de build/gerenciador de dependências documentada nos artefatos-fonte.
5. **Subir serviços auxiliares (DB, cache)** **[NÃO APLICÁVEL HOJE]** — nenhum motor de banco nas dependências (C-01), nenhuma camada de cache nos fontes (SAD §7), nenhum arquivo de composição de serviços/containers detectado.
6. **Rodar migrações** **[NÃO APLICÁVEL HOJE]** — nenhum mecanismo de migração identificado no repositório; a definir junto com a confirmação do motor de persistência (C-01). Ver [[db-migration-spec]].
7. **Popular dados de exemplo (seed)** **[PENDENTE]** — o seeder existe: `src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java` (método `run`), destinado à população de dados realistas para homologação e primeira onda de medição de performance (SAD §3/§11). O comando de invocação **não é descobrível** (sem scripts; sem ferramenta de build verificada) — confirmar com a engenharia. Uso restrito a homologação.
8. **Iniciar aplicação** **[PENDENTE]** — comando não definível: `package.json` sem scripts (frontend — NFR-M06) e sem mecanismo de execução/build verificado (backend). O mecanismo de servir o frontend não é descobrível a partir dos fontes (SAD §11) e a varredura não extraiu inventário de rotas para smoke-test.

---

## 3. Variáveis de Ambiente

> **Status:** nenhum arquivo `.env`/`.env.example` ou mecanismo externo de configuração foi detectado na varredura determinística. Nenhuma variável é, portanto, discoverável no estado atual — a tabela registra o gap; os nomes de variáveis **não foram inventados**.

| Variável | Obrigatória | Descrição | Valor de exemplo |
| :--- | :--- | :--- | :--- |
| *(nenhuma variável discoverável)* | — | Nenhum `.env`/`.env.example` detectado; mecanismo de configuração não identificável nos fontes | — |

**Áreas que demandarão variáveis de ambiente após as confirmações pendentes (nomes a definir):**
* Conexão com a camada de persistência — condicionada à confirmação do motor (C-01 do BRD);
* Configuração de TLS 1.2+ do tráfego frontend↔backend (NFR-SEC01);
* Armazenamento de tokens de sessão — o logout invalida o token imediatamente (NFR-SEC04), implicando estado no servidor;
* Segredos de configuração da aplicação — nenhum segredo versionado no repositório (SAD §9); rotação a cada 90 dias [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — premissa herdada do SAD §9; não há segredos identificados nos fontes].

Ao introduzir o `.env`: garantir entrada correspondente no `.gitignore` (arquivo não verificado na varredura — confirmar).

---

## 4. Secure Command Sandbox Recipes

> Comandos permitidos para automação/agentes, com limites de segurança explícitos. Timeouts são sugestões operacionais. Onde o comando não é definível no estado atual, a linha registra a pendência.

| Target Tool | Command Type | Recipe | Timeout | Restrições |
| :--- | :--- | :--- | :--- | :--- |
| `npm` (diretório do `package.json`) | install | `npm install --no-audit --ignore-scripts` | 120s | Acesso à rede externa apenas ao registry configurado, para resolver `@popperjs/core ^2.11.8`; sem lockfile verificado — revisar artefatos de lock gerados antes de versionar |
| Pipeline de análise (`server/analyze-pipeline.ts`) | scan (somente leitura) | Executar a varredura determinística do workspace + AST e comparar a saída com o baseline (353 arquivos · 27.912 LOC · 1.287 funções · 347 classes) — comando de invocação não documentado nos fontes **[PENDENTE]** | — | Somente leitura; proibido modificar fontes durante o scan |
| Backend Java | build / run | **Não definível** — nenhuma ferramenta de build ou mecanismo de execução documentado nos artefatos-fonte | — | Pendente de levantamento técnico; versão do JDK também a confirmar (NFR-PO03) |
| Qualidade de código | check (somente leitura) | Verificar as 2 chamadas `console.*` residuais em `frontend/src/services/api.js` (linhas 44 e 107) e o stub `setUsername` em `src/main/java/br/com/aegispatrimonio/model/Usuario.java:86` | — | Somente leitura; reportar sem alterar código |

---

## 5. Serviços Locais & Portas

> Nenhuma porta ou configuração de rede foi detectada na varredura; o mecanismo de servir o frontend não é descobrível (SAD §11). A tabela registra o estado atual, não uma configuração funcional.

| Serviço | Porta | Descrição |
| :--- | :--- | :--- |
| Backend Java (instância única) | não detectada | Endpoints HTTP, filtro de autenticação (`doFilterInternal`), regras de negócio e seeder de homologação — porta pendente de documentação |
| Frontend JavaScript | não detectada | Interface desktop-first (1280–1920px — NFR-U03) — mecanismo de servir não descobrível (SAD §11) |
| Persistência | não aplicável | Nenhum motor de banco nas dependências (C-01) — sem serviço local de banco |
| Cache | não aplicável | Nenhuma camada de cache identificada (SAD §7) |

---

## 6. Environment Verification Checklist

- [ ] `npm install` executa sem erros no diretório do `package.json` e instala `@popperjs/core ^2.11.8` *(verificável hoje)*
- [ ] A varredura determinística (`server/analyze-pipeline.ts`) reproduz o baseline: 353 arquivos · 1.287 funções · 347 classes · 27.912 LOC *(verificável hoje)*
- [ ] Zero chamadas `console.*` no frontend de produção — **falha confirmada hoje**: 2 residuais em `frontend/src/services/api.js` (linhas 44 — `console.error`; 107 — `console.debug`; NFR-M04)
- [ ] Zero métodos stub — **falha confirmada hoje**: `Usuario.setUsername` (linha 86, corpo vazio — NFR-M05 / C-04 do BRD)
- [ ] `npm run dev` sobe o servidor local — **inexistente**: `package.json` sem scripts (NFR-M06)
- [ ] `npm test` executa e passa — **inexistente**: nenhuma suíte/scripts declarados (0 devDependencies); meta de cobertura ≥ 80% em service/mapper pendente de instrumentação (NFR-M01–M06)
- [ ] `npm run lint` sem erros — **inexistente**: nenhuma ferramenta de lint declarada nos fontes
- [ ] Compilação do backend executada com sucesso — **comando pendente** (ferramenta de build não documentada; versão do JDK a confirmar — NFR-PO03)
- [ ] Aplicação acessível em `http://localhost:[porta]` — **porta não detectada** nos fontes (SAD §11)
- [ ] Banco de dados conectado e migrado — **não aplicável hoje** (motor não especificado — C-01 do BRD)
- [ ] Seeder executado em homologação — **comando de invocação pendente**
- [ ] Variáveis de ambiente sensíveis não commitadas — `.env` inexistente hoje; ao criar, incluir no `.gitignore` (arquivo não verificado na varredura)

---

## 7. Troubleshooting Comum

> Entradas ancoradas em fatos verificados (diagnóstico AST + SAD v1.0). Causas prováveis são hipóteses de trabalho, não diagnósticos fechados.

| Sintoma | Causa Provável | Solução |
| :--- | :--- | :--- |
| `npm install` falha ao resolver `@popperjs/core` | Registry inacessível ou `package.json` sem lockfile de referência (arquivo sem nome — gap NFR-M06) | Verificar conectividade ao registry configurado; validar integridade do `package.json`; reinstalar |
| Não existe comando de build/execução do backend | Nenhuma ferramenta de build documentada nos artefatos-fonte; versão do JDK não confirmada (NFR-PO03) | Levantamento técnico urgente: confirmar ferramenta de build e versão do runtime; atualizar as Seções 1, 2 e 8 deste documento |
| Seeder não popula dados de homologação | `RealisticDataSeeder` restrito a homologação; mecanismo de invocação não documentado (método `run`, complexidade ciclomática 15) | Confirmar o comando de execução com a engenharia; validar os dados gerados antes da primeira onda de medição de performance (SAD §3/§11) |
| Frontend não carrega no navegador | Mecanismo de servir os assets não é descobrível a partir dos fontes (SAD §11) | Confirmar com a engenharia como o frontend é servido; documentar porta e comando de inicialização |
| Erros de API exibidos sem tratamento uniforme | Fluxo não passando pelo tratamento centralizado `handleApiError` (NFR-U02); chamadas `console.*` residuais (linhas 44/107) | Verificar o fluxo de erro em `frontend/src/services/api.js`; remover as chamadas residuais com verificação automatizada no pipeline (NFR-M04) |
| Listagens com filtros combinados lentas em desenvolvimento | `ManutencaoSpecification.build` (complexidade ciclomática 14); sem ORM, índices e plano de consulta são responsabilidade direta do código (NFR-P02) | Revisar índices em conjunto com a refatoração do método (SAD §7); monitorar as metas p95 < 1s / p99 < 2s (NFR-P02) |

---

## 8. Comandos Úteis

| Comando | Descrição |
| :--- | :--- |
| `npm install` (diretório do `package.json`) | Instala a única dependência de produção declarada (`@popperjs/core ^2.11.8`) |
| `npm run <script>` | **Indisponível** — `package.json` sem scripts (gap NFR-M06); criação de scripts de dev/build/verificação é ação recomendada |
| Build/execução do backend | **Comando não definido** — nenhuma ferramenta de build documentada; pendente de levantamento técnico |
| Varredura determinística (`server/analyze-pipeline.ts`) | Reproduz o diagnóstico baseline do workspace (353 arquivos, 27.912 LOC) — comando de invocação a documentar |
| Seed de homologação | **Não documentado** — `RealisticDataSeeder` (restrito a homologação; SAD §3/§11) |

---

## 9. Pendências de Verificação Consolidadas

> Consolidado dos gaps que impedem um ambiente de desenvolvimento plenamente reproduzível. Nenhum item abaixo foi inventado — todos derivam da varredura determinística e do SAD v1.0. Este documento deve ser revisado imediatamente após o levantamento técnico urgente (C-01 do BRD) e a confirmação da ferramenta de build/versão do runtime.

| # | Pendência | Impacto no ambiente de desenvolvimento | Referência |
| :--- | :--- | :--- | :--- |
| 1 | Confirmar a versão do runtime Java (JDK) | Bloqueia validação de compilação/execução do backend | NFR-PO03; SAD §2 |
| 2 | Identificar a ferramenta de build/gerenciador de dependências do backend | Bloqueia instalação de dependências, build, execução e seed | SAD §2/§12 |
| 3 | Confirmar o motor de persistência | Bloqueia serviço local de banco, migrações e variáveis de conexão | C-01 do BRD; SAD §5 |
| 4 | Nomear o pacote e criar scripts no `package.json` | Bloqueia `npm run dev/build/test/lint`; limita a reprodutibilidade do build do frontend | NFR-M06; SAD §12 |
| 5 | Documentar o mecanismo de servir o frontend e as portas locais | Bloqueia o checklist de acesso local (`http://localhost:[porta]`) | SAD §11 |
| 6 | Documentar o comando de invocação do `RealisticDataSeeder` | Bloqueia população de dados de homologação e medição de performance | SAD §3/§11 |
| 7 | Confirmar lockfile/gerenciador de pacotes do frontend | Define o comando canônico de instalação | Seção 1 [INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA] |
| 8 | Remover `console.*` residuais e resolver o stub `setUsername` | Higiene de código verificável no ambiente local | NFR-M04/NFR-M05; C-04 do BRD |

---

**Documentos relacionados:** [[db-migration-spec]] · [[db-schema-spec]] · [[api-specification]] · [[uml-diagrams]] · security-policies.md · NFR v1.0