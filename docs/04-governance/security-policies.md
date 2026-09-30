# Application Security Policies & Boundary Enforcement — Sistema de Gestão de Patrimônio (A4)

> **Versão:** 1.0 · **Owner:** Segurança/Engenharia · **Status:** Draft · **Classificação do documento:** Interno — Confidencial
> **Fontes:** [[nfr]] (Non-Functional Requirements v1.0), [[system-architecture]] (SAD v1.0), diagnóstico determinístico do codebase (varredura AST real — 350 arquivos, 1.268 funções, 344 classes, 27.537 LOC), `package.json` do workspace.
> **Topologia verificada:** aplicação web server-side única — backend Java (`src/`, pacote `br.com.aegispatrimonio`, 335 arquivos `.java`) + frontend JavaScript (`frontend/`, 15 arquivos `.js`, única dependência de produção `@popperjs/core` ^2.11.8) + banco de dados de motor **não especificado** (acesso presumivelmente via SQL cru, sem ORM). **Nenhuma rota/IPC declarada foi detectada no código.**

## 1. Threat Model Overview

**Ativos a proteger:**
- **Credenciais e contas de usuário** — entidade `Usuario` (autenticação/RBAC): hashes de senha, username, tokens JWT (access 1h / refresh 7d — NFR-SEC01);
- **Dados pessoais (PII)** — dados cadastrais de usuários sujeitos à LGPD (NFR-C01/C02; minimização RF-08/RN-06);
- **Dados patrimoniais** — entidades `Ativo` e `Manutenção` (núcleo do domínio: `AtivoService`, `AtivoMapper`, `ManutencaoSpecification`) e a trilha de auditoria imutável (NFR-SEC07);
- **Segredos de infraestrutura** — chave de assinatura do JWT e credenciais do banco de dados (rotação a cada 90 dias — NFR-SEC10);
- **Integridade do código e da cadeia de suprimentos** — 350 arquivos de código; inventário de dependências do backend **não verificado** (Seção 9).

**Atores de ameaça considerados:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a tipologia de atores não está registrada nos artefatos-fonte. Premissas adotadas: (a) usuário autenticado malicioso tentando escalação de privilégio (User → Admin) ou acesso a dados de outros usuários; (b) atacante externo não autenticado explorando a API HTTP e o fluxo de autenticação (RF-23/24/25); (c) agente automatizado executando credential stuffing, força bruta e varredura de endpoints; (d) insider com acesso a segredos de deploy/banco.

**Superfícies de ataque principais** (ancoradas na topologia verificada — nenhuma rota/IPC declarada foi detectada no código; os endpoints referenciados derivam dos NFRs e requerem confirmação — SAD Seção 12, item 5):
1. **API HTTP interna (frontend ↔ backend)** — boundary único de entrada, centralizado em `frontend/src/services/api.js` (função `request`, linha 54, complexidade ciclomática 13); autenticação JWT, RBAC e validação de entrada server-side são os controles de borda (NFR-SEC01/04/05);
2. **Fluxo de autenticação** — login/logout (RF-23/24/25), emissão e revogação de tokens via blocklist (CA-08); alvo natural de força bruta e fixation;
3. **Consultas dinâmicas ao banco** — `ManutencaoSpecification.build` (`src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26`, complexidade 14): superfície prioritária de SQL injection, dado o acesso via SQL cru sem ORM (verificado na stack — NFR-SEC06);
4. **Uploads de arquivo / execução de comandos / integrações de terceiros** — **nenhuma dessas superfícies foi detectada no codebase** (nenhuma rota declarada, nenhuma integração externa, nenhuma evidência de execução de comandos na varredura AST); as Seções 3 e 4 estabelecem políticas preventivas obrigatórias caso sejam introduzidas;
5. **Frontend no navegador** — 15 arquivos `.js` sem framework SPA; risco de XSS via manipulação de DOM (Seção 8) e vazamento de dados via `console.*` residual (Seção 10).

**Achado de segurança com prioridade de investigação:** o stub `Usuario.setUsername` com corpo vazio (`src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`) pode indicar bug funcional ou intencionalidade — potencialmente relevante para autenticação (RF-23/NFR-SEC01) (SAD Seção 12, item 7).

**Lacunas de verificação que bloqueiam políticas deste documento** (do [[nfr]]/SAD, Seção 12): motor de banco de dados (item 1 — impacta criptografia em repouso e descarte seguro), mecanismo de hash de senhas efetivamente implementado (item 2), configuração de deploy/TLS/domínio de produção (item 6), comportamento do stub `Usuario.setUsername` (item 7), existência dos endpoints `/health` e `/metrics` (item 5).

## 2. Context Isolation Boundary

**Princípio:** o sistema **não implementa multi-tenancy** (verificado — o modelo de acesso é por roles internas Admin/User, NFR-SEC04). O contexto de isolamento é, portanto, **a sessão autenticada do usuário e seu papel (role)**: cada requisição deve ser avaliada isoladamente contra o token apresentado e o papel do usuário, e nenhum usuário deve acessar dados patrimoniais, registros ou operações além do que seu papel permite — tentativas fora do escopo retornam 403 (operação Admin por User) ou 404 (recurso inexistente, sem vazar existência). Não há contexto de execução de agente/tenant a isolar na topologia atual (aplicação web server-side única, sem IPC detectado).

**Mecanismo de enforcement:**
- **Tokens de sessão escopados (JWT)** — access token expirando em 1h e refresh token em 7 dias (NFR-SEC01); toda requisição a endpoint protegido é autenticada independentemente; requisições sem token → **401 Unauthorized** (CA-06);
- **RBAC enforcement server-side em todo endpoint** — operações de escrita em entidades mestres exigem role Admin; tentativas de role User → **403 Forbidden** (RF-26, RN-01/02, CA-01/CA-10); o enforcement **nunca** deve ser delegado ao frontend (`frontend/src/services/api.js` é client-side e não é controle de segurança);
- **Requisições stateless** — cada requisição HTTP é processada sem afinidade de sessão server-side; o **único estado compartilhado** permitido é a **blocklist de logout** (CA-08), que invalida tokens server-side (tensão com escalação horizontal registrada — SAD Seção 12);
- **Isolamento por sandbox/container ou network policy:** **não aplicável como mecanismo atual** — nenhuma configuração de deploy/infraestrutura foi verificada no codebase (NFR-PO03). Caso a topologia evolua para múltiplas instâncias (NFR-S02), o isolamento entre instâncias continua não sendo por tenant, e a blocklist passará a exigir mecanismo central.

**Validação:**
- Cobertura de **100% dos endpoints de escrita** em testes de integração verificando o RBAC (NFR-SEC04) — incluindo testes de 401 (sem token) e 403 (User em operação Admin);
- Testes de expiração de token (1h/7d) e de revogação real via blocklist no logout (CA-08/RF-25 `clearSession`);
- Investigação obrigatória do stub `Usuario.setUsername` (`src/main/java/br/com/aegispatrimonio/model/Usuario.java:86`) antes de aprovar os requisitos de autenticação relacionados.

## 3. Workspace / Filesystem Boundary Validation

**Status verificado:** a varredura AST **não detectou superfície de acesso a filesystem** no codebase (nenhuma rota declarada, nenhum endpoint de upload/download confirmado). A aplicação é server-side e não empacota como desktop (não existe `src-tauri/Cargo.toml` — verificado na stack). **Esta seção estabelece a política preventiva obrigatória** para qualquer código futuro que manipule arquivos (uploads, importações/exportações, carga de arquivos pelo seeder `RealisticDataSeeder`, relatórios exportados).

**Regra:** todo acesso a arquivo deve ser validado contra um diretório-raiz permitido (base directory) configurado por ambiente — **sem traversal (`../`)**, sem resolução fora da base, sem caminhos absolutos injetados. O caminho resolvido e normalizado deve estar sempre contido na base.

```java
// Regra algorítmica de referência (server-side — backend Java)
public static boolean isPathAllowed(Path basePath, Path targetPath) {
    Path resolvedBase   = basePath.toAbsolutePath().normalize();
    Path resolvedTarget = resolvedBase.resolve(targetPath).normalize();
    // Para cobertura de symlinks, resolver o destino real com toRealPath() antes da comparação
    return resolvedTarget.startsWith(resolvedBase);
}
```

**Casos de teste obrigatórios** (todo código de manipulação de arquivos deve cobrir):
- Path traversal clássico (`../../etc/passwd`, `..\..\windows`);
- Caminhos absolutos injetados (`/etc/passwd`, `C:\Windows`);
- Symlinks maliciosos apontando para fora da base (validar o destino real resolvido, não o link);
- Encoding duplo/percentual (`%2e%2e%2f`, `..%252f`) e null bytes (`%00`);
- Nomes de arquivo controlados pelo usuário — proibido usar input bruto como nome de arquivo em disco; usar identificadores gerados pelo servidor.

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a lista de casos de teste e a exigência de identificadores gerados pelo servidor são políticas preventivas propostas, pois nenhuma superfície de arquivo existe hoje no codebase para ancorá-las.

## 4. Command Execution Boundary

**Status verificado:** **não aplicável atualmente** — a varredura AST não detectou execução de comandos, automação de processos externos ou spawn de processos no codebase; não há filas/mensageria (SAD Seção 2) e nenhuma integração externa foi detectada. **Política preventiva obrigatória** caso execução de comandos ou automação seja introduzida:

- **Allowlist de comandos:** lista explícita e revisada de binários/comandos permitidos, versionada em repositório; **proibido** invocar interpretadores de shell (`sh`, `bash`, `cmd`, `powershell`) com input concatenado; invocação direta de binário com argumentos vetoriais (sem concatenação de string);
- **Timeout máximo:** 30s por comando, com kill do processo e registro do timeout. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — valor padrão do template; nenhum requisito do codebase informa o valor]**
- **Isolamento de rede:** egress restrito a domínios permitidos por allowlist. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA — depende de infraestrutura de deploy não verificada (NFR-PO03)]**
- **Auditoria:** todo comando executado é registrado na trilha de auditoria (NFR-SEC07) com comando, argumentos (sanitizados — sem segredos), actor, timestamp, resultado e duração.

## 5. Data Protection Rules

**Classificação de dados** (níveis do template aplicados às entidades verificadas via varredura — `Usuario`, `Ativo`, `Manutenção`; o mapeamento entidade→nível é **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**):

| Nível | Dados do projeto |
| :--- | :--- |
| **Restrito (PII/Sensível)** | Dados pessoais de usuários na entidade `Usuario` (username, dados cadastrais sujeitos à LGPD — NFR-C01/C02) |
| **Confidencial** | Credenciais e hashes de senha, tokens JWT (access/refresh), chave de assinatura do JWT, credenciais do banco de dados |
| **Interno** | Dados de ativos (`Ativo`), registros de manutenção (`Manutenção`), trilha de auditoria (NFR-SEC07), dados gerados pelo seeder `RealisticDataSeeder` |
| **Público** | Nenhum dado identificado como público; o endpoint `/health` (existência não confirmada — SAD Seção 12, item 5) deve expor apenas status, sem metadados sensíveis |

**Criptografia em trânsito:** **HTTPS obrigatório em produção com TLS 1.2+** (NFR-SEC03 — requisito verificado do BRD). Nenhuma configuração de TLS/domínio foi encontrada no codebase (NFR-PO03) — a implementação é lacuna de verificação. HSTS e preferência por TLS 1.3 onde disponível: recomendação **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**.

**Criptografia em repouso:** **não especificada** nos artefatos-fonte. Política-alvo: AES-256 (ou mecanismo equivalente fornecido pelo motor de banco) para dados classificados como Restrito/Confidencial. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a decisão está **bloqueada pela lacuna 1 do NFR (motor de banco não verificado)**: criptografia em repouso pode ser responsabilidade do motor, do filesystem ou da aplicação, e não pode ser definida até que o motor real seja identificado.

**Hash de senhas:** **bcrypt ou argon2** (BRD RNF-02 / NFR-SEC02). O mecanismo efetivamente implementado no backend **não pôde ser verificado** na varredura de dependências — **verificação obrigatória antes de aprovar o requisito** (SAD Seção 12, item 2). Proibido armazenar senhas em texto claro, MD5, SHA-1 ou SHA-256 puro sem salt/work-factor.

**Mascaramento/Anonimização (ambientes não-produtivos):** proibido carregar PII real de produção em staging/desenvolvimento; dados de teste devem ser sintéticos — o seeder `RealisticDataSeeder` (`src/main/java/br/com/aegispatrimonio/config/seeder/RealisticDataSeeder.java:34`) existe para esse fim (verificado) e deve ser executado em staging com validação (risco R-02 do BRD). **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** para a proibição explícita de PII real em não-prod.

**Retenção & Descarte:** política de retenção por tipo de dado **a definir** (NFR-C02), resolvendo a tensão entre a **trilha de auditoria imutável** (NFR-SEC07/RNF-07) e o **direito de eliminação do titular** (LGPD). Direção proposta: anonimização de dados pessoais em registros de auditoria preservados, mantendo a integridade do registro operacional. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**. O processo de exclusão física segura depende do motor de banco (lacuna 1 do NFR).

## 6. Authentication & Authorization

**Mecanismo de auth:** **JWT stateless** (NFR-SEC01) — access token expirando em **1h**, refresh token em **7 dias**. OAuth2/OIDC/SAML: não requeridos pelos artefatos-fonte e não verificados no codebase. Endpoints protegidos rejeitam requisições sem token com **401** (CA-06).

**Modelo de autorização:** **RBAC com dois papéis internos — Admin e User** (NFR-SEC04):
- Toda operação de escrita em entidades mestres exige **Admin**; tentativas de User → **403 Forbidden** (RF-26, RN-01/02, CA-01/CA-10);
- Cobertura de **100% dos endpoints de escrita** em testes de integração (NFR-SEC04);
- ABAC: não requerido pelos artefatos-fonte.

**Política de senha/MFA:**
- Hash: bcrypt/argon2 (RNF-02 — implementação não verificada, NFR-SEC02 — ver Seção 5);
- **MFA para acesso Admin** (NFR-SEC09, classificado como **Should**) — **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (item inferido no NFR; mecanismo não verificado no codebase);
- Política de complexidade/idade de senha: **não especificada** nos artefatos-fonte — a definir. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

**Gestão de sessão:**
- **Expiração:** access 1h / refresh 7d (NFR-SEC01);
- **Revogação:** o logout limpa a sessão local no frontend (RF-25 `clearSession`) e **invalida o token server-side via blocklist** (CA-08) — a limpeza local no navegador não é suficiente;
- **Refresh tokens:** emissão de novo access token via refresh; a rotação da chave de assinatura (NFR-SEC10) deve definir o tratamento de refresh tokens emitidos com chave anterior;
- **Tensão registrada:** a blocklist introduz estado compartilhado que complica a premissa de instâncias stateless necessária à escala horizontal (NFR-S02) — mecanismo central a definir na evolução da topologia (SAD Seções 7 e 12).

## 7. Secrets Management

**Armazenamento:** **nenhum cofre de segredos ou Secrets Manager verificado no codebase**; nenhuma configuração de deploy foi encontrada (NFR-PO03). Política obrigatória: segredos (chave de assinatura do JWT, credenciais do banco, credenciais de deploy) **nunca em código-fonte, repositório ou logs**; armazenamento em cofre de segredos (Secrets Manager/Vault) com injeção via variável de ambiente/segredo de runtime no deploy. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — a ferramenta específica é decisão pendente, pois a infraestrutura de deploy não está verificada.

**Rotação:** **a cada 90 dias** (NFR-SEC10 — item inferido no NFR) para a chave de assinatura do JWT e credenciais do banco; rotação imediata (fora da cadência) em caso de suspeita de comprometimento. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** para o procedimento de rotação imediata.

**Acesso:** princípio do menor privilégio — credenciais de banco com permissões mínimas necessárias à aplicação; acesso ao cofre restrito ao pipeline de deploy e à administração. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** (controle organizacional não explícito no código).

## 8. Input Validation & Output Encoding

**Validação:** **validação de entrada server-side obrigatória em toda borda** (NFR-SEC05) — abordagem **allowlist, não denylist**:
- Payloads inválidos → **400 Bad Request** com mensagens claras;
- IDs inexistentes → **404 Not Found** padronizado;
- A validação nunca deve depender exclusivamente do frontend (`frontend/src/services/api.js` é client-side e não é controle de segurança).

**Prevenção de injeção:**
- **SQL injection — risco crítico e prioritário:** o acesso a dados é via **SQL cru, sem ORM** (verificado na stack); **100% das consultas devem ser parametrizadas** (NFR-SEC06) — proibida concatenação de string SQL com input do usuário. A superfície de maior risco são as consultas dinâmicas: `ManutencaoSpecification.build` (`src/main/java/br/com/aegispatrimonio/repository/ManutencaoSpecification.java:26`, complexidade ciclomática 14) deve passar por **revisão de segurança prioritária**, verificando que os filtros dinâmicos (RF-15/RF-22) usam apenas parâmetros vinculados e colunas de allowlist;
- **Command injection:** nenhuma execução de comandos detectada no codebase — política preventiva na Seção 4;
- **Prompt injection:** **não aplicável** — não há LLM/IA generativa na stack verificada.

**Sanitização de output:**
- **XSS:** o frontend é JavaScript sem framework SPA (única dependência de produção `@popperjs/core` ^2.11.8) — toda inserção de dados no DOM deve usar APIs seguras (ex.: `textContent`) em vez de `innerHTML`/concatenação de HTML; encoding contextual (HTML, atributo, URL, JS) para todo dado originado do backend;
- Contratos de erro padronizados (401/403/400/404 — NFR-SEC01/04/05) não devem ecoar detalhes internos ao cliente (stack traces, SQL, caminhos de arquivo).

## 9. Dependency & Supply Chain Security

**Inventário verificado:** o `package.json` raiz declara **1 dependência de produção** (`@popperjs/core` ^2.11.8) e **0 de desenvolvimento**, sem scripts. **Lacuna relevante de supply chain:** o arquivo de build do backend Java **não foi coberto pela varredura de stack** — o inventário de dependências do backend (e suas vulnerabilidades) é **desconhecido** e constitui lacuna de verificação obrigatória (SAD Seção 2 e Seção 12).

**Scan de vulnerabilidades:** nenhuma ferramenta verificada no codebase; não há pipeline de CI/CD (nenhum script no `package.json` — NFR-M01, lacuna 3). Política: adotar scanner automatizado (ex.: Dependabot/Snyk ou equivalente) com varredura a cada push/PR e varredura completa semanal, cobrindo **tanto o `package.json` quanto o arquivo de build do backend Java**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — ferramenta e cadência são propostas.

**Política de atualização:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — SLA proposto: vulnerabilidades críticas corrigidas em ≤ 7 dias, altas em ≤ 30 dias, médias/baixas na próxima release planejada; avaliação do OWASP Top 10 por release maior (NFR-SEC08).

**SBOM (Software Bill of Materials):** **não gerado atualmente**; política-alvo: gerar SBOM a cada release e armazená-la como artefato versionado do pipeline. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — depende da implementação do pipeline de CI/CD (lacuna 3 do NFR).

## 10. Logging & Auditability

**O que deve ser logado:**
- **Trilha de auditoria imutável** de **todas as operações de escrita em todas as entidades** (NFR-SEC07) — com a tensão LGPD a resolver (Seções 5 e 12);
- Falhas de autenticação (401), negações de autorização (403) e payloads inválidos rejeitados (400) — sem conteúdo sensível;
- Mudanças de permissão/papel e operações administrativas;
- Correlação de requisições via **request ID** propagado do frontend (`frontend/src/services/api.js`) para o backend nos fluxos críticos — autenticação, solicitação, aprovação (NFR-O02);
- Logs estruturados no backend (NFR-O01) — **ferramenta a definir**. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

**O que NUNCA deve ser logado (proibição absoluta):**
- **Senhas** (em qualquer forma) e **hashes de senha**;
- **Tokens JWT** (access e refresh) e chaves de assinatura;
- **Credenciais de banco de dados** e segredos de deploy;
- **PII em texto claro** (dados cadastrais de usuários — nível Restrito, Seção 5);
- Conteúdo completo de payloads em endpoints de autenticação (RF-23/24).

**Débito de logging verificado (achado real do diagnóstico):** as chamadas residuais `console.error` (`frontend/src/services/api.js:44`) e `console.debug` (`frontend/src/services/api.js:107`) devem ser **removidas ou substituídas por tratamento de erro estruturado antes de produção** — logging via `console` no navegador expõe dados a qualquer usuário com devtools e não é auditável.

**Retenção de logs de auditoria:** período **a definir** na política de retenção LGPD (NFR-C02), resolvendo a tensão entre imutabilidade (RNF-07) e direito de eliminação; direção proposta: anonimização de dados pessoais em registros preservados. **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]**

## 11. Incident Response (Segurança)

**[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — nenhum canal de resposta a incidentes, contato de segurança ou processo de disclosure está explícito no código ou nos artefatos-fonte. Premissas adotadas: existência de um responsável de segurança (Security/Eng Lead) e de um domínio de e-mail corporativo (não verificado no codebase — NFR-PO03); alinhamento com o processo de comunicação de incidentes da LGPD (Art. 48).

* **Processo de disclosure de vulnerabilidade:** canal de divulgação responsável (responsible disclosure) — **security@** no domínio corporativo a definir; triage inicial pelo Security/Eng Lead, com confirmação, contenção, correção e comunicação aos afetados; incidentes de segurança com risco ou dano relevante a titulares de dados devem ser comunicados à ANPD e aos titulares em prazo razoável, conforme LGPD Art. 48.
* **SLA de resposta por severidade:** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** — proposta: **Crítico: 4 horas** (triage e contenção), Alto: 24 horas, Médio: 5 dias úteis, Baixo: próxima release; incidentes que derrubem o serviço devem alinhar-se ao RTO ≤ 1h de produção (NFR-A02).

## 12. Compliance Mapping

| Requisito Regulatório | Como é atendido | Evidência |
| :--- | :--- | :--- |
| **LGPD Art. 46** — medidas de segurança técnicas e administrativas | HTTPS obrigatório com TLS 1.2+ (NFR-SEC03); SQL 100% parametrizado (NFR-SEC06); RBAC Admin/User server-side (NFR-SEC04); validação de entrada server-side (NFR-SEC05); trilha de auditoria imutável (NFR-SEC07) | [[nfr]] Seção 4; este documento (Seções 2, 5, 6, 8) |
| **LGPD Art. 48** — comunicação de incidentes de segurança | Processo de resposta a incidentes com canal de disclosure e comunicação à ANPD/titulares **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | Este documento (Seção 11) |
| **LGPD Art. 18** — direitos do titular (acesso, eliminação) | Direitos de acesso e eliminação previstos (NFR-C02); processos operacionais de atendimento **a definir** **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | [[nfr]] (NFR-C01/C02); SAD Seção 13 |
| **LGPD Art. 6º, III** — necessidade (minimização de dados) | Minimização de dados nos cadastros (RF-08/RN-06) | [[nfr]]/SAD Seção 13 |
| **LGPD Art. 16** — eliminação após término do tratamento | Política de retenção com anonimização de dados pessoais em registros de auditoria preservados **[INFERIDO POR IA — REQUER VALIDAÇÃO HUMANA]** | Este documento (Seções 5 e 10); SAD Seção 13 |
| **OWASP Top 10** (NFR-SEC08) | Avaliação por release maior; controles de borda deste documento (Seções 2, 3, 5, 6, 8) cobrem injection, broken access control, cryptographic failures e XSS | [[nfr]] Seção 4; este documento |