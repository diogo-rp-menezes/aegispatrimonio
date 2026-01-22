# Suíte de Testes Exaustiva — Aegis Patrimônio

## 🎯 ABORDAGEM: Exaustiva e orientada a riscos (funcional + segurança + validação + estados)
- Cobertura por funcionalidade (Controller/Service) e por endpoint.
- Casos positivos (happy paths) e negativos (erros esperados) com foco em autorização, validação e consistência de regras de negócio.
- Combinações de parâmetros (filtros/paginação) e estados alternativos (entidades inexistentes, relacionamentos inconsistentes, permissões cruzadas por filial/role).
- Priorização de cenários críticos: segurança (RBAC), unicidade de patrimônio, consistência filial/local/responsável, e atualização de health-check.

## 📋 ANÁLISE (Estado Atualizado)
Contexto identificado no código:
- Endpoints/Serviços principais:
  - AtivoController: CRUD + filtros/paginação; patch health-check. **Agora utiliza `AtivoQueryParams` e limites de paginação.**
  - AtivoService: RBAC (ADMIN/USER), filtros (filialId, tipoAtivoId, status), validações: unicidade de número de patrimônio; consistência de Filial com Localização e Responsável. **Implementa `IAtivoService`.**
  - HealthCheckService: **Totalmente refatorado e modularizado, delegando responsabilidades a `CurrentUserProvider`, `HealthCheckAuthorizationPolicy`, `HealthCheckUpdater` e `HealthCheckCollectionsManager`. Implementa `IHealthCheckService`.**
- **Todos os serviços críticos (`AtivoService`, `FilialService`, `FuncionarioService`, `TipoAtivoService`, `LocalizacaoService`, `FornecedorService`, `ManutencaoService`, `MovimentacaoService`, `DepreciacaoService`) possuem logging de auditoria implementado.**
- **Todos os controllers possuem anotações `@PreAuthorize` auditadas e padronizadas.**
- Já existe AtivoControllerIT com vários cenários de integração. **Lacunas identificadas foram cobertas, incluindo matriz completa de filtros/paginação, todos os erros de validação/composição, e estados alternativos.**

## 🛠️ IMPLEMENTAÇÃO (Sugestões de Código de Teste - Status Atualizado)
- Local dos testes: `src/test/java/br/com/aegispatrimonio/**`
- Tipos de teste:
  - Integração (SpringBootTest + MockMvc) para Controller.
  - Unidade para regras de Service e componentes extraídos (mock de repositórios/mapper).
- Padrões:
  - Usuários: `ROLE_ADMIN`, `ROLE_USER` com/sem associação de Funcionário e filiais.
  - Builders/Factories de entidades para facilitar combinações.
- Exemplos (pseudocódigo resumido):
  - Listagem com filtros: chamar `GET /api/v1/ativos?filialId=...&tipoAtivoId=...&status=...` com paginação `page,size,sort` e validar conteúdo/ordem/total. **Coberto.**
  - Criação: `POST /api/v1/ativos` com payloads válidos e inválidos; checar 201/400/409. **Coberto.**
  - Health-check: `PATCH /api/v1/ativos/{id}/health-check` → validar 204, 404, 403 (filial não correspondente), 401 (sem auth). **Coberto.**

## ✅ CASOS DE TESTE (Completa — Positivos e Negativos - Status: COBERTOS)

### 1. Listar Ativos — GET /api/v1/ativos
- Autorização: **Coberto.**
  1. ADMIN autenticado deve retornar 200 com lista. (happy) - [x]
  2. USER autenticado deve retornar 200 com lista. (happy) - [x]
  3. Sem autenticação deve retornar 401. (negativo) - [x]
- Paginação/Ordenação: **Coberto.**
  4. page=0,size=10,sort=nome,asc retorna primeira página ordenada por nome ascendente. (happy) - [x]
  5. page=1,size=5 retorna segunda página com 5 itens. (happy) - [x]
  6. Tamanho maior que limite permitido retorna ajuste/erro conforme configuração (se houver validação global). (negativo/edge) - [x]
  7. sort por múltiplos campos (ex.: status,numeroPatrimonio) funciona. (happy) - [x]
- Filtros combinados: **Coberto.**
  8. Somente filialId. (happy) - [x]
  9. Somente tipoAtivoId. (happy) - [x]
  10. Somente status. (happy) - [x]
  11. filialId + tipoAtivoId. (happy) - [x]
  12. filialId + status. (happy) - [x]
  13. tipoAtivoId + status. (happy) - [x]
  14. filialId + tipoAtivoId + status. (happy) - [x]
  15. Valores de filtro inexistentes retornam lista vazia. (negativo esperado) - [x]
  16. Valores de filtro com tipos inválidos retornam 400 (binding). (negativo) - [x]
- Integridade de dados projetados (DTO): **Coberto.**
  17. Campos obrigatórios do DTO presentes e coerentes. (happy) - [x]

### 2. Buscar por ID — GET /api/v1/ativos/{id}
- Autorização: **Coberto.**
  18. ADMIN autenticado retorna 200 e DTO correto. (happy) - [x]
  19. USER autenticado retorna 200 e DTO correto. (happy) - [x]
  20. Sem autenticação retorna 401. (negativo) - [x]
- Existência: **Coberto.**
  21. ID existente retorna 200. (happy) - [x]
  22. ID inexistente retorna 404 (EntityNotFoundException). (negativo) - [x]

### 3. Criar Ativo — POST /api/v1/ativos
- Autorização: **Coberto.**
  23. ADMIN cria com sucesso (201). (happy) - [x]
  24. USER retorna 403 (forbidden). (negativo) - [x]
  25. Sem autenticação retorna 401. (negativo) - [x]
- Validação de payload: **Coberto.**
  26. Payload mínimo válido cria ativo. (happy) - [x]
  27. Campos obrigatórios ausentes retornam 400 (Bean Validation). (negativo) - [x]
  28. Formatos inválidos (ex.: CNPJ, tamanhos, enums inválidos) retornam 400. (negativo) - [x]
- Regras de negócio: **Coberto.**
  29. Número de patrimônio único — criar com patrimônio já existente retorna 409/400 conforme implementação (validar exceção lançada por `validarNumeroPatrimonio`). (negativo) - [x]
  30. Consistência Filial e Localização — Local de outra filial retorna 400. (negativo) - [x]
  31. Consistência Filial e Responsável — Responsável de outra filial retorna 400. (negativo) - [x]
  32. Tipo de Ativo inexistente retorna 400/404 (conforme repo). (negativo) - [x]
  33. Filial inexistente retorna 400/404. (negativo) - [x]
  34. Fornecedor inexistente retorna 400/404. (negativo) - [x]
  35. Responsável inexistente retorna 400/404. (negativo) - [x]
  36. Localização inexistente retorna 400/404. (negativo) - [x]
- Estados alternativos: **Coberto.**
  37. Criar com status inicial específico (ex.: ATIVO/EM_MANUTENCAO) quando suportado. (happy) - [x]

### 4. Atualizar Ativo — PUT /api/v1/ativos/{id}
- Autorização: **Coberto.**
  38. ADMIN atualiza com sucesso (200). (happy) - [x]
  39. USER retorna 403. (negativo) - [x]
  40. Sem autenticação retorna 401. (negativo) - [x]
- Existência: **Coberto.**
  41. ID inexistente retorna 404. (negativo) - [x]
- Validações/Regras: **Coberto.**
  42. Atualizar número de patrimônio para um que já existe em outro ativo retorna 409/400. (negativo) - [x]
  43. Atualizar Localização para filial diferente retorna 400. (negativo) - [x]
  44. Atualizar Responsável para filial diferente retorna 400. (negativo) - [x]
  45. Atualizar com payload mínimo válido mantém integridade. (happy) - [x]
  46. Atualizar com campos nulos opcionais funciona conforme regra (ex.: descrição). (happy) - [x]

### 5. Deletar Ativo (Exclusão Lógica: BAIXADO) — DELETE /api/v1/ativos/{id}
- Autorização: **Coberto.**
  47. ADMIN deleta (204). (happy) - [x]
  48. USER retorna 403. (negativo) - [x]
  49. Sem autenticação retorna 401. (negativo) - [x]
- Existência: **Coberto.**
  50. ID inexistente retorna 404. (negativo) - [x]
- Estado: **Coberto.**
  51. Deletar ativo já BAIXADO é idempotente ou retorna erro conforme regra — validar comportamento. (edge) - [x]

### 6. Health Check do Ativo — PATCH /api/v1/ativos/{id}/health-check
- Autorização na Controller (usa buscarPorId no service): **Coberto.**
  52. ADMIN retorna 204. (happy) - [x]
  53. USER com acesso válido retorna 204. (happy) - [x]
  54. USER sem acesso à filial do ativo retorna 403 (no service específico; ao menos deve falhar ao buscar/autorizar). (negativo) - [x]
  55. Sem autenticação retorna 401. (negativo) - [x]
- Existência: **Coberto.**
  56. Ativo inexistente retorna 404. (negativo) - [x]
- Payload: **Coberto.**
  57. Payload ignorado na Controller (no-op), mas na implementação futura/HealthCheckService deve aceitar DTOs com listas vazias/nulas. Validar ambos cenários. (edge) - [x]

### 7. HealthCheckService.updateHealthCheck (unidade)
- RBAC e Associação de Filial: **Coberto.**
  58. ADMIN pode atualizar qualquer ativo. (happy) - [x]
  59. USER sem Funcionario associado → AccessDeniedException. (negativo) - [x]
  60. USER com Funcionario inexistente no repositório → AccessDeniedException. (negativo) - [x]
  61. USER com filial não correspondente ao ativo → AccessDeniedException. (negativo) - [x]
  62. USER com filial correspondente → sucesso. (happy) - [x]
- Existência de Ativo/Detalhes: **Coberto.**
  63. Ativo inexistente → EntityNotFoundException. (negativo) - [x]
  64. Sem detalhes existentes: cria em memória e persiste ao salvar, depois flush. (happy) - [x]
  65. Com detalhes existentes: atualiza e persiste. (happy) - [x]
- Coleções: **Coberto.**
  66. Limpa discos/memórias/adaptadores anteriores quando atualiza. (happy) - [x]
  67. DTO com listas nulas → não cria novos registros. (edge) - [x]
  68. DTO com listas vazias → após limpeza, mantém sem registros. (edge) - [x]
- Mapeamento: **Coberto.**
  69. healthCheckMapper.updateEntityFromDto chamado com entidade gerenciada. (happy) - [x]
  70. healthCheckMapper.toEntity aplicado para cada item das listas. (happy) - [x]

### 8. AtivoService.listarTodos (unidade)
- Filtros e paginação do repositório mapeados corretamente para DTOs. (happy) - [x]
- Usuário ADMIN/USER não altera resultado desta listagem (apenas roles permitidas). (happy) - [x]
- Parâmetros nulos não filtram. (happy) - [x]
- Parâmetros inválidos tratados por camada web (fora do service). (nota) - [x]

### 9. AtivoService.criar/atualizar/deletar (unidade)
- Unicidade de número de patrimônio: conflito detectado. (negativo) - [x]
- Consistência Localização/Filial e Responsável/Filial: violações geram erro. (negativo) - [x]
- Atualizações parciais válidas. (happy) - [x]
- Exclusão lógica altera status para BAIXADO. (happy) - [x]

### 10. Segurança/Autorização (integração)
- Endpoints com @PreAuthorize exigem roles corretas: **Coberto.**
  71. Verificar hasAnyRole/hasRole em todos endpoints do AtivoController. (happy/negativo conforme role) - [x]

### 11. Robustez de Binding/Validação (integração)
- Enums inválidos em query param (status) → 400. (negativo) - [x]
- ID não numérico em path → 400 (binding). (negativo) - [x]
- Campos @Valid nos DTOs com violações → 400 contendo mensagens. (negativo) - [x]

## 🔧 CONFIGURAÇÃO (Estado Atualizado)
- Dependências: Spring Boot Test, MockMvc, Mockito/JUnit 5 já presentes no projeto (pom.xml). **Confirmado.**
- Perfis: usar profile de teste com banco em memória (se configurado) ou containers; reutilizar base dos ITs existentes. **Confirmado.**
- Dados: utilizar factories utilitárias no próprio teste (vide AtivoControllerIT) para criar Filial, Departamento, Funcionario, Localização, TipoAtivo, Fornecedor, Ativo. **Confirmado.**
- Autenticação: criar usuários em memória ou mocks de SecurityContext para testes de unidade; para integração, usar configuração de segurança de teste já existente na base. **Confirmado.**

## 🚨 CENÁRIOS CRÍTICOS (Edge Cases - Status: ABORDADOS)
- USER sem associação de Funcionário tentando health-check (AccessDeniedException). - [x]
- USER associado a Filial A tentando atualizar health-check de ativo da Filial B. - [x]
- Número de patrimônio duplicado entre dois ativos ao criar/atualizar. - [x]
- Localização/Responsável de filial diferente do ativo na criação/atualização. - [x]
- Exclusão lógica repetida (idempotência x erro). - [x]
- Filtros combinados que resultam em lista vazia, com paginação avançando além da última página. - [x]
- Enums e IDs inválidos em query/path causando 400 (binding/validation). - [x]
- Payloads com listas nulas/vazias para componentes de hardware (HealthCheckService). - [x]

---

## Status Geral da Execução do Plano de Testes (Pós-Refatoração)
Todas as ações propostas neste plano foram executadas. Os testes unitários para os novos componentes foram criados, os testes de integração existentes foram revisados e complementados, e a cobertura para cenários críticos de RBAC, validação e regras de negócio foi garantida. A aplicação está agora com uma suíte de testes mais robusta e abrangente.

## Conclusão
A suíte de testes foi aprimorada para garantir a exaustividade e a rastreabilidade com as refatorações implementadas. O código está mais testável, e a confiança na correção funcional e de segurança foi significativamente elevada. Os objetivos de cobertura e qualidade de testes foram atingidos.