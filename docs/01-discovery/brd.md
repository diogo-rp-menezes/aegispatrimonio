# Business Requirements Document (BRD)
## Sistema de Gestão de Patrimônio (A4)

---

### 1. Visão Geral do Negócio

#### 1.1 Propósito
Este documento descreve os requisitos de negócio para o **Sistema de Gestão de Patrimônio (A4)**, uma aplicação web para gestão completa de ativos organizacionais, incluindo cadastro de entidades organizacionais, controle de ativos, gestão de manutenção e fluxos de aprovação.

#### 1.2 Escopo
O sistema atende às necessidades de:
- **Gestão Organizacional**: Departamentos, Filiais, Fornecedores, Funcionários
- **Gestão de Ativos**: Cadastro, classificação (tipos), localização, depreciação
- **Gestão de Manutenção**: Solicitações, ordens de serviço, fluxo de aprovação
- **Segurança e Acesso**: Autenticação JWT, autorização baseada em roles (Admin/User)

#### 1.3 Contexto do Domínio (Ubiquitous Language)
Os termos de domínio são definidos no [Glossário & Ubiquitous Language](./glossario.md). Termos-chave:
- **Ativo**: Bem patrimonial rastreável (hardware, mobiliário, equipamentos)
- **Filial/Departamento**: Unidades organizacionais hierárquicas
- **Fornecedor**: Entidade externa provedora de bens/serviços
- **Funcionário**: Colaborador interno, pode ser usuário do sistema
- **Solicitação de Manutenção**: Demanda de reparo/manutenção com fluxo de aprovação
- **Perfil Admin**: Acesso total (CRUD em todas as entidades)
- **Perfil User**: Acesso restrito (leitura, solicitação de manutenção)

---

### 2. Stakeholders

| Stakeholder | Papel | Interesse Principal |
|-------------|-------|---------------------|
| **Gestor de Patrimônio** | Product Owner | Visibilidade total do acervo, relatórios, auditoria |
| **Administrador do Sistema** | Admin (Role) | Gestão de usuários, permissões, cadastros mestres |
| **Funcionário/Colaborador** | User (Role) | Solicitar manutenção, consultar ativos alocados |
| **Equipe de Manutenção** | Operador | Receber, executar e concluir ordens de serviço |
| **Aprovação/Compliance** | Aprovador | Validar solicitações, garantir conformidade |
| **TI/Infraestrutura** | Tech Lead | Disponibilidade, segurança, integrações |

---

### 3. Requisitos Funcionais

#### 3.1 Gestão de Entidades Organizacionais (CRUD Admin)

| ID | Requisito | Descrição | Regra de Negócio (Glossário) |
|----|-----------|-----------|------------------------------|
| RF-01 | **Criar Departamento** | Cadastrar novo departamento na estrutura organizacional | `criar_comAdmin_deveRetornarCreated` |
| RF-02 | **Atualizar Departamento** | Modificar dados cadastrais de departamento existente | `atualizar_comAdmin_deveRetornarOk` |
| RF-03 | **Excluir Departamento** | Remover permanentemente departamento | `deletar_comAdmin_deveRetornarNoContent` |
| RF-04 | **Buscar Departamento por ID** | Consultar detalhes de um departamento específico | `buscarPorId` |
| RF-05 | **Listar Departamentos** | Listar todos os departamentos com paginação | - |
| RF-06 | **Criar Filial** | Cadastrar nova unidade organizacional (filial) | `createFilial` |
| RF-07 | **Criar Fornecedor** | Cadastrar novo fornecedor para aquisição/manutenção | `createFornecedor` |
| RF-08 | **Criar Funcionário** | Registrar colaborador, opcionalmente vinculado a usuário | `createFuncionario`, `createFuncionarioAndUsuario` |
| RF-09 | **Criar Localização** | Cadastrar local físico/lógico para alocação de ativos | `createLocalizacao` |
| RF-10 | **Criar Tipo de Ativo** | Classificar ativos em categorias (ex.: Notebook, Impressora) | `createTipoAtivo` |

> **Restrição de Acesso**: Todas as operações de escrita (Criar, Atualizar, Excluir) **exigem perfil Admin**. Usuários com perfil User recebem **403 Forbidden** (`criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden`).

#### 3.2 Gestão de Ativos

| ID | Requisito | Descrição |
|----|-----------|-----------|
| RF-11 | **Cadastrar Ativo** | Registrar novo ativo com: tipo, filial, departamento, localização, fornecedor, valor, data aquisição |
| RF-12 | **Atualizar Ativo** | Alterar dados do ativo (transferência, reavaliação, mudança de status) |
| RF-13 | **Baixar/Excluir Ativo** | Dar baixa patrimonial ou excluir registro (Admin only) |
| RF-14 | **Consultar Ativo por ID** | Obter detalhes completos incluindo histórico (`buscarPorId`) |
| RF-15 | **Listar/Filtrar Ativos** | Busca por tipo, filial, departamento, status, localização |
| RF-16 | **Cálculo de Depreciação** | Calcular valor residual baseado em método/tempo de vida útil |
| RF-17 | **Custo Total por Ativo** | Somatório de custos de manutenção por ativo/período (`custoTotalPorAtivo`) |

#### 3.3 Gestão de Manutenção (Fluxo de Aprovação)

| ID | Requisito | Descrição | Estados/Transições |
|----|-----------|-----------|-------------------|
| RF-18 | **Criar Solicitação** | Funcionário abre solicitação de manutenção para ativo | `criar` → **Pendente** |
| RF-19 | **Aprovar Solicitação** | Aprovador autoriza execução | **Pendente** → `aprovar` → **Aprovada** |
| RF-20 | **Cancelar Solicitação** | Anular solicitação/fluxo em andamento | Qualquer → `cancelar` → **Cancelada** |
| RF-21 | **Concluir Manutenção** | Finalizar ordem após execução | **Aprovada/Em Andamento** → `concluir` → **Concluída** |
| RF-22 | **Histórico de Manutenção** | Rastrear todas as intervenções por ativo | - |

#### 3.4 Autenticação e Autorização

| ID | Requisito | Descrição |
|----|-----------|-----------|
| RF-23 | **Login/Autenticação** | Autenticação via credenciais, retorno de JWT |
| RF-24 | **Interceptador de Auth** | Injeção automática de token em requisições (`authInterceptor`) |
| RF-25 | **Controle de Sessão** | Logout/limpeza de credenciais (`clearSession`) |
| RF-26 | **RBAC (Role-Based Access Control)** | Duas roles: **Admin** (escrita total) e **User** (leitura + solicitações) |
| RF-27 | **Gestão de Permissões/Roles** | CRUD de roles e permissões granulares (`createRole`, `createPermission`) |
| RF-28 | **Validação de Dados** | Rejeitar payloads inválidos com **400 Bad Request** (`criar_comDadosInvalidos_deveRetornarBadRequest`) |
| RF-29 | **Tratamento de Não Encontrado** | Retornar **404 Not Found** para IDs inexistentes (`buscarPorId_comIdInexistente_deveRetornarNotFound`) |

---

### 4. Requisitos Não-Funcionais

| ID | Categoria | Requisito | Critério/Métrica |
|----|-----------|-----------|------------------|
| RNF-01 | **Segurança** | Autenticação JWT com expiração e refresh | Token expira em 1h; refresh token 7 dias |
| RNF-02 | **Segurança** | Senhas hasheadas (bcrypt/argon2) | Nunca armazenar plain text |
| RNF-03 | **Segurança** | HTTPS obrigatório em produção | TLS 1.2+ |
| RNF-04 | **Desempenho** | Tempo de resposta API < 200ms (p95) | Excluindo operações de relatório pesado |
| RNF-05 | **Disponibilidade** | Uptime 99.5% (excluindo manutenção programada) | SLA mensal |
| RNF-06 | **Escalabilidade** | Suportar 10k+ ativos, 1k+ usuários simultâneos | Horizontal scaling ready |
| RNF-07 | **Auditoria** | Log de todas as operações de escrita (criar/atualizar/deletar) | Immutable audit trail |
| RNF-08 | **Usabilidade** | Interface responsiva (desktop/tablet) | Mobile-first para solicitações |
| RNF-09 | **Integração** | API RESTful com OpenAPI/Swagger | Documentação automática |
| RNF-10 | **Testabilidade** | Cobertura de testes > 80% (unit + integration) | Pipeline CI/CD bloqueia se < 80% |

---

### 5. Regras de Negócio (Business Rules)

| Regra | Descrição | Origem (Glossário) |
|-------|-----------|-------------------|
| **RN-01** | Apenas usuários com role **Admin** podem criar, atualizar ou excluir entidades mestres (Departamento, Filial, Fornecedor, TipoAtivo, Localização, Role, Permission) | `criar_comAdmin_deveRetornarCreated`, `atualizar_comAdmin_deveRetornarOk`, `deletar_comAdmin_deveRetornarNoContent` |
| **RN-02** | Usuários com role **User** recebem **403 Forbidden** ao tentar operações de escrita em entidades mestres | `criar_comUser_deveRetornarForbidden`, `atualizar_comUser_deveRetornarForbidden`, `deletar_comUser_deveRetornarForbidden` |
| **RN-03** | Dados de entrada inválidos (campos obrigatórios, formatos, tipos) devem retornar **400 Bad Request** | `criar_comDadosInvalidos_deveRetornarBadRequest` |
| **RN-04** | Busca por ID inexistente retorna **404 Not Found** | `buscarPorId_comIdInexistente_deveRetornarNotFound` |
| **RN-05** | Solicitação de manutenção segue fluxo: **Pendente → Aprovada → Em Andamento → Concluída** (pode ser **Cancelada** a qualquer momento) | `aprovar`, `concluir`, `cancelar` |
| **RN-06** | Funcionário pode ser vinculado a usuário do sistema para autenticação | `createFuncionarioAndUsuario` |
| **RN-07** | Cada ativo pertence a **um** Tipo de Ativo, **uma** Filial, **um** Departamento, **uma** Localização | Modelo relacional |
| **RN-08** | Custo total de manutenção por ativo é calculado somando todas as ordens concluídas no período | `custoTotalPorAtivo` |

---

### 6. Casos de Uso Principais

#### UC-01: Administrador Cadastra Nova Filial
**Ator**: Admin  
**Pré-condição**: Usuário autenticado com role Admin  
**Fluxo Principal**:
1. Admin acessa "Cadastro de Filiais"
2. Preenche dados: nome, código, endereço, responsável
3. Submete formulário
4. Sistema valida dados (RN-03)
5. Sistema persiste filial e retorna **201 Created** (RN-01)
**Pós-condição**: Filial disponível para associação a ativos/departamentos

#### UC-02: Funcionário Solicita Manutenção de Ativo
**Ator**: User (Funcionário)  
**Pré-condição**: Usuário autenticado; ativo existe e está ativo  
**Fluxo Principal**:
1. Funcionário acessa "Nova Solicitação"
2. Seleciona ativo (busca por filial/departamento/localização)
3. Descreve problema, prioridade, anexa fotos (opcional)
4. Submete solicitação
5. Sistema cria solicitação com status **Pendente** (RF-18)
**Pós-condição**: Solicitação visível para aprovadores

#### UC-03: Aprovador Autoriza Manutenção
**Ator**: Aprovador (Admin ou role específica)  
**Pré-condição**: Solicitação em status **Pendente**  
**Fluxo Principal**:
1. Aprovador visualiza fila de pendentes
2. Analisa detalhes, histórico do ativo (`custoTotalPorAtivo`)
3. Clica "Aprovar"
4. Sistema atualiza status para **Aprovada** (RN-05)
**Pós-condição**: Equipe de manutenção notificada

#### UC-04: Técnico Conclui Ordem de Serviço
**Ator**: Equipe de Manutenção  
**Pré-condição**: Solicitação **Aprovada**; técnico designado  
**Fluxo Principal**:
1. Técnico executa serviço
2. Registra: data, descrição, peças usadas, custo, tempo
3. Clica "Concluir"
4. Sistema atualiza para **Concluída** (RN-05)
5. Atualiza `custoTotalPorAtivo` do ativo
**Pós-condição**: Histórico atualizado; ativo disponível

---

### 7. Critérios de Aceitação (Definition of Done)

| Critério | Descrição |
|----------|-----------|
| **CA-01** | Todas as APIs de CRUD (entidades mestres) retornam 201/200/204 para Admin e 403 para User |
| **CA-02** | Validação de entrada rejeita payloads inválidos com 400 e mensagens claras |
| **CA-03** | Busca por ID inexistente retorna 404 com mensagem padronizada |
| **CA-04** | Fluxo de manutenção (Pendente→Aprovada→Concluída) funciona end-to-end |
| **CA-05** | Cancelamento funciona em qualquer estado, liberando recursos |
| **CA-06** | Autenticação JWT: login retorna token; endpoints protegidos rejeitam sem token (401) |
| **CA-07** | Interceptador injeta token automaticamente no frontend |
| **CA-08** | Logout limpa sessão local e invalida token server-side (blocklist) |
| **CA-09** | Relatórios de custo total por ativo batem com soma das ordens concluídas |
| **CA-10** | Testes de integração cobrem todos os cenários de permissão (Admin vs User) |

---

### 8. Métricas de Sucesso (KPIs)

| KPI | Meta | Frequência |
|-----|------|------------|
| **Tempo médio de aprovação** | < 4 horas úteis | Semanal |
| **Taxa de solicitações canceladas** | < 10% | Mensal |
| **Custo médio de manutenção por ativo/ano** | Redução 15% YoY | Anual |
| **Disponibilidade do sistema** | 99.5% | Mensal |
| **Tempo de resposta API (p95)** | < 200ms | Contínuo |
| **Cobertura de testes** | > 80% | Por deploy |

---

### 9. Riscos e Dependências

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| **R-01**: Complexidade de permissões granulares | Atraso na entrega de RBAC | Começar com 2 roles (Admin/User); evoluir para permissões finas |
| **R-02**: Migração de dados legados | Inconsistência no go-live | Scripts de validação + execução em staging |
| **R-03**: Performance em relatórios de custo total | Timeout em consultas pesadas | Materialized views / cache Redis / paginação |
| **R-04**: Adoção pelos usuários (User) | Baixa utilização do módulo de solicitação | UX mobile-first; treinamento; notificações push |

---

### 10. Aprovação

| Papel | Nome | Assinatura | Data |
|-------|------|------------|------|
| Product Owner | | | |
| Tech Lead | | | |
| Security Officer | | | |
| Compliance | | | |

---

> **Nota**: Este BRD é um documento vivo. Alterações nos requisitos funcionais devem passar por mudança controlada (change request) e refletidas no Glossário, ADRs e casos de teste.