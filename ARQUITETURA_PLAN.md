# Plano de Arquitetura e Evolução — Aegis Patrimônio

## 1. Objetivos do Plano
- Consolidar visão por camadas (Controllers, Services, Mappers, Repositories, Security/Config, Exception Handling) e seu estado atual.
- Traçar ações de curto prazo por componente, com foco em: correção de lacunas funcionais, consistência de contratos REST, validações, segurança (RBAC), transações e observabilidade.
- Alinhar critérios de aceite com a suíte de testes planejada (TEST_PLAN.md), garantindo rastreabilidade cenário ↔ componente.

## 2. Inventário por Camada (Estado Atualizado)

### 2.1 Controllers (REST)
- AuthController: Autenticação, emissão de JWT. Documentado com OpenAPI/Swagger.
- AtivoController: CRUD + filtros/paginação; patch health-check. Totalmente documentado com OpenAPI/Swagger, utilizando `AtivoQueryParams` e limites de paginação.
- DepartamentoController: CRUD de departamentos. Documentado com OpenAPI/Swagger.
- DepreciacaoController: Cálculos/consultas de depreciação. Documentado com OpenAPI/Swagger.
- FilialController: CRUD de filiais. Documentado com OpenAPI/Swagger.
- FornecedorController: CRUD de fornecedores. Documentado com OpenAPI/Swagger.
- FuncionarioController: CRUD de funcionários e vínculo com usuários. Documentado com OpenAPI/Swagger.
- LocalizacaoController: CRUD de localizações. Documentado com OpenAPI/Swagger.
- ManutencaoController: CRUD/fluxos de manutenção de ativos. **Segurança (@PreAuthorize) implementada e documentada com OpenAPI/Swagger.**
- MovimentacaoController: Movimentação de ativos entre filiais/locais/responsáveis. Documentado com OpenAPI/Swagger.
- TipoAtivoController: CRUD de tipos de ativo. Documentado com OpenAPI/Swagger.
- ApplicationControllerAdvice (exception): Tratamento global de erros.

Diretrizes gerais controllers:
- Anotações @Validated, @PreAuthorize consistentes e auditadas.
- Padrão de resposta (status codes + body) consistente; mensagens de erro via ControllerAdvice.
- Paginação/ordenação: especificar page,size,sort com validações e limites de size (ex: `AtivoController`).
- **Todos os controllers estão documentados com OpenAPI/Swagger, incluindo requisitos de segurança e roles.**

### 2.2 Services (Estado Atualizado)
- AtivoService: Regras de negócio de ativos (unicidade patrimônio, consistência Filial-Localização-Responsável, filtros, exclusão lógica BAIXADO). Interage com DepreciacaoService. **Implementa `IAtivoService`. Logging de auditoria implementado. Métricas customizadas adicionadas.**
- HealthCheckService: **Refatorado para ser um orquestrador enxuto, delegando responsabilidades a `CurrentUserProvider`, `HealthCheckAuthorizationPolicy`, `HealthCheckUpdater` e `HealthCheckCollectionsManager`. Implementa `IHealthCheckService`.**
- DepartamentoService, FilialService, FornecedorService, FuncionarioService, LocalizacaoService, ManutencaoService, MovimentacaoService, TipoAtivoService, DepreciacaoService: regras específicas de seus domínios. **Logging de auditoria implementado nos serviços de CRUD mais importantes.**
- Security: JwtService, CustomUserDetailsService; filtros e contexto de segurança. **`CurrentUserProvider` introduzido para abstrair acesso ao usuário logado.**

Diretrizes gerais services:
- @Transactional coerente (readOnly em consultas; gravações cercadas; flush quando necessário).
- Autorização e integridade: validar vínculos de filial e ownership onde aplicável. **Lógica de autorização extraída para `HealthCheckAuthorizationPolicy`.**
- Regras de domínio centralizadas aqui; controllers apenas coordenam.
- **Interfaces de serviço (`IAtivoService`, `IHealthCheckService`) introduzidas para desacoplamento e testabilidade.**
- **Logging de auditoria implementado em serviços críticos.**

### 2.3 Mappers (Estado Atualizado)
- HealthCheckMapper: mapeia HealthCheckDTO ↔ entidades Disco/Memoria/AdaptadorRede/AtivoDetalheHardware; updateEntityFromDto.
- TipoAtivoMapper e outros mapeadores de DTOs (quando presentes) para controllers/services.

Diretrizes gerais mappers:
- Métodos toDto/toEntity/updateFromDto com NullValuePropertyMappingStrategy apropriado para updates parciais quando suportado.
- Garantir que IDs não sejam sobrescritos indevidamente em updates; coleções: estratégia de clear+add vs. merge conforme regra do serviço.

### 2.4 Repositories (Estado Atualizado)
- Repositórios JPA para entidades principais (AtivoRepository com findByIdWithDetails; AtivoDetalheHardwareRepository; DiscoRepository; MemoriaRepository; AdaptadorRedeRepository; demais repos de domínio: Filial, Departamento, Fornecedor, Funcionario, Localizacao, Manutencao, Movimentacao, TipoAtivo, etc.).

Diretrizes gerais repositories:
- Consultas com fetch join quando necessário (evitar N+1 em telas críticas).
- Assinaturas que suportem filtros/paginação para a camada de serviço.
- **`HealthCheckUpdater` e `HealthCheckCollectionsManager` encapsulam detalhes de persistência específicos.**

### 2.5 Security/Config (Estado Atualizado)
- SecurityConfig, JwtAuthFilter, JwtService, CustomUserDetailsService, CorsConfig, SwaggerConfig, DevConfig, JwtSecretValidator.

Diretrizes gerais segurança:
- Garantir escopos: hasRole/hasAnyRole conforme endpoints. **@PreAuthorize auditado e padronizado em todos os controllers.**
- Tokens com expiração e renovação (refresh) se aplicável; propriedades externas seguras.
- ControllerAdvice não deve vazar detalhes sensíveis.
- **Endpoint `/actuator/**` liberado para acesso de métricas.**

### 2.6 Exception Handling (Estado Atualizado)
- ApplicationControllerAdvice para mapear: EntityNotFoundException → 404; AccessDeniedException → 403; Bean Validation → 400; Conflitos de negócio (ex.: patrimônio duplicado) → 409. **Tratamento de erros padronizado e centralizado.**

## 3. Evolução da Arquitetura (Pós-Refatoração)

As ações do plano de refatoração resultaram nas seguintes melhorias arquiteturais:

- **Modularização do `HealthCheckService`:** O serviço foi decomposto em componentes menores e mais coesos (`CurrentUserProvider`, `HealthCheckAuthorizationPolicy`, `HealthCheckUpdater`, `HealthCheckCollectionsManager`), aumentando a testabilidade e reduzindo o acoplamento.
- **Inversão de Dependência:** A introdução de interfaces de serviço (`IAtivoService`, `IHealthCheckService`) e a injeção de dependências para componentes internos do `HealthCheckService` aprimoraram a aderência ao Princípio da Inversão de Dependência (DIP).
- **Padronização de APIs:** O uso de `AtivoQueryParams` para filtros e paginação no `AtivoController` padroniza a interface dos endpoints de listagem.
- **Segurança Reforçada:** Auditoria e padronização das anotações `@PreAuthorize` em todos os controllers, incluindo a correção de uma vulnerabilidade crítica no `ManutencaoController`.
- **Tratamento de Erros Consistente:** Centralização e padronização do mapeamento de exceções para códigos HTTP apropriados via `ApplicationControllerAdvice`.
- **Observabilidade Aprimorada:** Implementação de logging de auditoria em serviços críticos e configuração de métricas básicas com Micrometer/Prometheus.
- **Documentação Abrangente:** Todos os endpoints da API estão agora documentados com OpenAPI/Swagger, incluindo detalhes de DTOs, códigos de status e requisitos de segurança.

## 4. Critérios Globais de Qualidade (Estado Atualizado)
- Cobertura de testes alinhada a TEST_PLAN.md (prioridades críticas ≥ 80% em services/controle de acesso) - **Verificado e Atingido.**
- Contratos REST documentados (Swagger) e estáveis; versionamento /api/v1 mantido - **Documentação completa com OpenAPI/Swagger.**
- Sem N+1 em recursos críticos; paginação consistente; ordenação por múltiplos campos válida - **Ações para otimização de queries e paginação implementadas.**
- Segurança: RBAC aplicado em todos endpoints sensíveis; tratamento de erros não vaza detalhes - **RBAC auditado e tratamento de erros padronizado.**

---
Este documento reflete o estado atual da arquitetura após a execução do plano de refatoração e melhoria contínua.