# Modelos e Padrões de Desenvolvimento — Aegis Patrimônio

Este documento serve como um repositório central de modelos e padrões de código e design para garantir a coesão, manutenibilidade e qualidade em todo o projeto Aegis Patrimônio. Ele complementa as `rules.md` e os planos de arquitetura e refatoração.

## 1. Estrutura de Projeto e Nomenclatura

### 1.1 Pacotes e Módulos
- **Organização por Camadas:** `controller`, `service`, `repository`, `mapper`, `dto`, `model`, `config`, `security`, `exception`, `util`.
- **Subpacotes:** Utilizar subpacotes para organizar componentes relacionados dentro de uma camada (ex: `service.policy`, `service.updater`, `dto.request`, `dto.response`, `dto.query`).
- **Nomenclatura:**
    - Interfaces: Prefixo `I` (ex: `IAtivoService`).
    - Implementações: Sufixo `Impl` ou `Default` (ex: `AtivoServiceImpl`, `DefaultHealthCheckUpdater`).
    - DTOs: Sufixo `DTO` (ex: `AtivoDTO`, `AtivoCreateDTO`, `AtivoUpdateDTO`).
    - Mappers: Sufixo `Mapper` (ex: `AtivoMapper`).
    - Repositórios: Sufixo `Repository` (ex: `AtivoRepository`).
    - Controllers: Sufixo `Controller` (ex: `AtivoController`).

### 1.2 Nomes de Classes e Métodos
- **Classes:** Substantivos, CamelCase (ex: `AtivoService`, `HealthCheckAuthorizationPolicy`).
- **Métodos:** Verbos, camelCase (ex: `criar`, `atualizar`, `buscarPorId`, `listarTodos`).
- **Variáveis:** camelCase (ex: `ativoId`, `usuarioLogado`).
- **Constantes:** UPPER_SNAKE_CASE (ex: `MAX_PAGE_SIZE`).

## 2. Padrões de Design e Implementação

### 2.1 Services
- **Princípio da Responsabilidade Única (SRP):** Cada serviço deve ter uma única razão para mudar. Se um serviço começar a acumular muitas responsabilidades, considerar a extração para componentes menores (ex: `HealthCheckUpdater`, `HealthCheckCollectionsManager`).
- **Inversão de Dependência (DIP):** Controllers devem depender de interfaces de serviço, não de implementações concretas (ex: `IAtivoService`).
- **Transacionalidade:** Utilizar `@Transactional` de forma granular e consciente. Métodos de leitura devem ser `@Transactional(readOnly = true)`. Métodos de escrita devem garantir a atomicidade da operação.
- **Validação:** Realizar validações de negócio nos serviços, após a validação de entrada do controller.
- **Logging de Auditoria:** Implementar logging de auditoria em operações críticas (criar, atualizar, deletar, mudanças de status) conforme o item 4.1 do `ACTION_PLAN.md`.

### 2.2 Controllers
- **Finos (Thin Controllers):** Controllers devem ser responsáveis apenas por:
    - Receber requisições HTTP.
    - Validar DTOs de entrada (com `@Valid` e `@Validated`).
    - Chamar o serviço apropriado.
    - Traduzir o resultado do serviço para a resposta HTTP (status code, body).
- **Tratamento de Erros:** Delegar para `ApplicationControllerAdvice`.
- **Documentação OpenAPI/Swagger:** Utilizar anotações (`@Tag`, `@Operation`, `@ApiResponses`, `@Schema`, `@Parameter`, `@SecurityRequirement`) para documentar completamente os endpoints.

### 2.3 DTOs
- **Records:** Preferir `record`s para DTOs imutáveis, especialmente para requisições e respostas simples.
- **Validação:** Utilizar anotações de Bean Validation (ex: `@NotNull`, `@Size`, `@Email`, `@Min`, `@Max`) nos campos dos DTOs.
- **Separação:** Utilizar DTOs específicos para criação (`*CreateDTO`), atualização (`*UpdateDTO`) e resposta (`*DTO`) para evitar vazamento de dados e garantir flexibilidade.
- **Query Objects:** Encapsular parâmetros de query em `record`s dedicados (ex: `AtivoQueryParams`) para endpoints de listagem.

### 2.4 Mappers
- **MapStruct:** Utilizar MapStruct para gerar mappers de forma eficiente e segura.
- **Estratégias de Mapeamento:** Definir `NullValuePropertyMappingStrategy` e `NullValueCheckStrategy` apropriados para cada mapper, especialmente em operações de atualização.
- **Coleções:** Mapear coleções de forma a suportar as estratégias de persistência (ex: `clear+add` vs. `merge`).

### 2.5 Segurança
- **RBAC:** Utilizar `@PreAuthorize` em controllers e/ou serviços para aplicar controle de acesso baseado em roles.
- **Autenticação:** JWT para autenticação stateless.
- **Validação de Input:** Sempre validar inputs do usuário em todas as camadas (controller com `@Valid`, serviço com validações de negócio).

## 3. Padrões de Teste

### 3.1 Testes Unitários
- **Foco:** Testar unidades de código isoladamente (classes de serviço, políticas, managers, mappers).
- **Mocks:** Utilizar Mockito para mockar dependências externas.
- **Cobertura:** Manter alta cobertura de linhas e branches.

### 3.2 Testes de Integração
- **Foco:** Testar a interação entre componentes (controller-service-repository, fluxo completo de um endpoint).
- **Contexto:** Utilizar `@SpringBootTest` com `MockMvc` para testes de controller, ou `@DataJpaTest` para testes de repositório.
- **Dados de Teste:** Utilizar builders ou factories para criar dados de teste de forma consistente.

## 4. Observabilidade

### 4.1 Logging
- **SLF4J:** Utilizar `org.slf4j.Logger` para todos os logs, configurando níveis apropriados (DEBUG, INFO, WARN, ERROR).
- **Logs de Auditoria:** Registrar ações críticas (criar, atualizar, deletar, mudanças de status) com informações sobre o usuário, ação e entidade afetada, sem expor dados sensíveis.

### 4.2 Métricas
- **Micrometer/Prometheus:** Utilizar Micrometer para coletar métricas, expondo-as via Prometheus.
- **Métricas Customizadas:** Adicionar métricas de negócio em pontos chave (ex: contador de criações de ativos, tempo de execução de operações críticas).

## 5. Documentação

### 5.1 OpenAPI/Swagger
- **Anotações:** Utilizar `@Tag`, `@Operation`, `@ApiResponses`, `@Schema`, `@Parameter`, `@SecurityRequirement` para documentar todos os endpoints da API de forma completa e precisa.
- **Detalhes:** Incluir descrições claras, exemplos, DTOs de requisição/resposta, códigos de status HTTP esperados e requisitos de segurança (roles).

---

Este documento será atualizado conforme novas diretrizes e padrões forem estabelecidos ou evoluídos.