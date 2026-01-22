# TECH DESIGN — Fase 1: RBAC Avançado (Granular com Contexto)

Meta
- Data/Hora: 2025-10-24 00:45
- Versão: 1.0 (inicial)
- Escopo: Implementar RBAC granular com contexto de negócio (filialId) aplicável aos domínios prioritários Ativos e Funcionários.

1. Arquitetura
- Componentes
  - Permission (entidade): representa permissão atômica contextual (resource, action, optional context key)
  - Role (entidade): agrega um conjunto de Permissions
  - Group (entidade) e UserGroup (associação): modelam agrupamento de usuários e permissões herdadas (opcional para v1 se necessário)
  - UserRole / GroupPermission / RolePermission (tabelas de associação)
  - IPermissionService (interface): verificação de permissão com/sem contexto
  - PermissionServiceImpl (service): implementação principal, com cache leve e logs estruturados
  - CustomPermissionEvaluator (Security): integração com Spring Security Expressions: hasPermission(targetId, resource, action[, filialId])
  - AuditLogger (componente): logs de auditoria (autorizado/negado) sem dados sensíveis
- Fluxo de Autorização
  1) Controller utiliza @PreAuthorize("hasPermission(#id, 'ATIVO', 'READ', #filialId)")
  2) Spring chama CustomPermissionEvaluator → IPermissionService
  3) Serviço resolve permissões do usuário (diretas via Role e indiretas via Group), aplica contexto (filialId), retorna ALLOW/DENY
  4) AuditLogger registra evento (userId, resource, action, filialId, outcome)
- Observabilidade
  - Métricas Micrometer:
    - counter aegis_authz_total{outcome="allow|deny", resource, action}
    - timer aegis_authz_eval_timer
  - Logs estruturados SLF4J (sem payloads sensíveis)

2. Decisões Técnicas
- Modelo de Contexto: uso de filialId como contexto principal (tenant). Campo opcional para outras extensões futuras
- Estratégia de Cache: cache leve (por usuário) das permissões agregadas (Role/Group); invalidar em alterações (limpar por userId)
- Strategy de Avaliação: permissions do tipo (resource, action) com escopo (global ou por filialId). Preferência por matching exato
- Falhas: fail-safe para DENY em caso de erro inesperado no serviço de autorização (com log de erro)

3. Modelo de Dados (DDL Proposta)
- Tabelas principais (nomes sugeridos)
  - rbac_permission: id (BIGINT, PK), resource (VARCHAR 64), action (VARCHAR 32), description (VARCHAR 255), context_key (VARCHAR 32 NULL)  -- ex.: context_key = 'filialId'
  - rbac_role: id, name (VARCHAR 64 UNIQUE), description
  - rbac_group: id, name (VARCHAR 64 UNIQUE), description
  - rbac_user_role: user_id (FK), role_id (FK), PK composta
  - rbac_role_permission: role_id (FK), permission_id (FK), PK composta
  - rbac_group_permission: group_id (FK), permission_id (FK), PK composta
  - rbac_user_group: user_id (FK), group_id (FK), PK composta
  - rbac_user_permission_context: id, user_id (FK), permission_id (FK), filial_id (BIGINT NULL)  -- para concessões específicas por contexto (opcional v1)
- Índices
  - idx_perm_resource_action (resource, action)
  - idx_user_role_user (user_id), idx_role_perm_role (role_id)
  - idx_group_perm_group (group_id), idx_user_group_user (user_id)
- Seeds (inicial)
  - Roles: ADMIN, USER
  - Permissions base por domínio: ATIVO/READ|CREATE|UPDATE|DELETE; FUNCIONARIO/READ|CREATE|UPDATE|DELETE
  - Atribuições: ADMIN → todas; USER → ATIVO/READ, FUNCIONARIO/READ (ajustável)

4. API de Autorização
- Interface IPermissionService
  - boolean hasPermission(Authentication auth, String resource, String action)
  - boolean hasPermission(Authentication auth, String resource, String action, Long filialId)
- CustomPermissionEvaluator
  - Suporte a quatro formas: hasPermission(#id, 'ATIVO', 'READ'), hasPermission('ATIVO','READ'), hasPermission(#id, 'ATIVO','READ', #filialId), hasPermission('ATIVO','READ', #filialId)
- Uso em Controllers
  - Substituir checks por role para permissions granulares gradualmente (iniciar com 2–3 endpoints de Ativos e 2 de Funcionários)

5. Segurança
- Princípios: mínimo privilégio, negativa por padrão, validação de entradas, logs sem dados sensíveis
- Tratamento de Erros: ApplicationControllerAdvice centraliza mensagens de acesso negado (sem revelar detalhes internos)
- Headers e rate limiting: manter políticas existentes; revisar após integração RBAC

6. Migrações Flyway (Plano)
- Vxxx__rbac_schema.sql: criação de tabelas e índices
- Vxxx__rbac_seeds.sql: inserir roles, permissions e atribuições iniciais
- Boas práticas: usar transações, chaves estrangeiras ON DELETE RESTRICT; comentários com rationale

7. Testes
- Unitários (≥80% serviços/policies)
  - PermissionServiceImpl: caminhos allow/deny, contexto filialId, cache hit/miss, erros do repositório
  - CustomPermissionEvaluator: mapeamento de parâmetros e integração com serviço
- Integração (MockMvc)
  - Endpoints críticos (Ativos/Funcionários): 200 quando permitido, 403 quando negado
- Dados de teste: builders/factories para usuário, roles, permissions e contextos

8. Métricas e Observabilidade
- Counters e timer conforme seção 1
- Exposição via /actuator/prometheus

9. Backlog Detalhado (Sprint 1)
- Modelagem & Migrações
  - DDL tabelas RBAC + índices + seeds (ADMIN/USER + CRUD Ativos/Funcionários)
- Infra de Autorização
  - IPermissionService + PermissionServiceImpl
  - CustomPermissionEvaluator + SecurityExpression
  - Cache leve por usuário + invalidação
- Integração Inicial
  - Alterar 2–3 endpoints de Ativos e 2 de Funcionários para usar hasPermission
- Testes e Observabilidade
  - Unitários de serviço e evaluator (≥80%)
  - Integração (MockMvc) dos endpoints alterados
  - Métricas Micrometer + logs de auditoria

10. Spike Controlado (1–2 dias)
- Objetivo: desriscar PermissionEvaluator + expressão @PreAuthorize com contexto filialId
- Entregáveis: PoC funcional em branch com 1 endpoint protegido, teste de integração e métrica de autorização
- Critérios de Sucesso: teste verde, latência de verificação aceitável (<5ms p95 em ambiente local), sem vazar dados sensíveis em logs

11. Go/No-Go para Execução Ampla
- Go se: DDL aprovada, PoC do evaluator aprovada, testes verdes, cobertura ≥80%, revisão SECURITY_ENGINEER sem críticas/altas
- No-Go: ajustar design conforme feedback e repetir spike se necessário

12. Dependências e Assunções
- Confirmações obtidas: domínios prioritários (Ativos, Funcionários), contexto (filialId), criação via Flyway com seeds
- Spring Security (JWT) ativo; Flyway ativo; ApplicationControllerAdvice presente

13. Riscos e Mitigações
- Complexidade/Regressões: integração incremental por domínio + testes de integração por fluxo
- Performance de checagem: cache leve por usuário, medir p95
- Consistência de contexto: validar filialId presente/consistente nas rotas relevantes

14. Próximos Passos
- Revisar/ajustar design com PO/Segurança
- Implementar Spike do PermissionEvaluator
- Preparar PR com DDL + seeds
- Iniciar Sprint 1 após Go
