/**
 * Controllers da API v1.
 *
 * <h2>Modelo de autorização unificado (resolução do achado H1 do production-code-audit
 * 2026-09-26, concluído na H1c)</h2>
 *
 * <p>Padrão obrigatório: todos os controllers de domínio usam
 * {@code @PreAuthorize("@permissionService.hasPermission(authentication, targetId, RESOURCE, ACTION, context)")}.
 * O {@code PermissionServiceImpl} aplica admin bypass interno ({@code ROLE_ADMIN} → allow),
 * portanto ADMIN não perde acesso com a migração de {@code hasRole} para {@code permissionService}.</p>
 *
 * <h3>Mapeamento controller → recurso/ação (pares granulares da migration V16)</h3>
 * <ul>
 *   <li>ManutencaoController — {@code MANUTENCAO:CREATE/READ/UPDATE/DELETE}. Workflow
 *       (aprovar/iniciar/concluir/cancelar) = {@code MANUTENCAO:UPDATE}.</li>
 *   <li>MovimentacaoController — {@code MOVIMENTACAO:CREATE/READ/UPDATE/DELETE};
 *       efetivar/cancelar = {@code MOVIMENTACAO:UPDATE}.</li>
 *   <li>DepreciacaoController — {@code DEPRECIACAO:UPDATE} (recalcular-todos,
 *       recalcular/{id}), {@code DEPRECIACAO:READ} (calcular-mensal).</li>
 *   <li>TipoAtivoController — {@code TIPO_ATIVO:READ/CREATE/UPDATE/DELETE}.</li>
 *   <li>FilialController — {@code FILIAL:READ/CREATE/UPDATE/DELETE}.</li>
 *   <li>DepartamentoController — {@code DEPARTAMENTO:READ/CREATE/UPDATE/DELETE}; CREATE com
 *       context {@code #departamentoCreateDTO.filialId}.</li>
 *   <li>LocalizacaoController — {@code LOCALIZACAO:READ/CREATE/UPDATE/DELETE}; CREATE com
 *       context {@code #localizacaoCreateDTO.filialId}.</li>
 *   <li>DashboardController — {@code DASHBOARD:READ}.</li>
 *   <li>AlertaController — {@code ALERTA:READ} (listar/recent), {@code ALERTA:UPDATE}
 *       (markAsRead).</li>
 *   <li>AtivoController / FornecedorController / FuncionarioController — padrão de referência,
 *       já migrados anteriormente ({@code ATIVO:*} / {@code FORNECEDOR:*} / {@code FUNCIONARIO:*}).</li>
 * </ul>
 *
 * <p>Os pares granulares acima são seedados de forma idempotente em
 * {@code V16__seed_granular_permissions.sql} e vinculados a {@code ROLE_ADMIN} e
 * {@code ROLE_USER}. A solução de contorno histórica (usar {@code ATIVO:*} como recurso
 * substituto nos 9 controllers acima) foi removida na H1c.</p>
 *
 * <h3>Exceções deliberadas (mantêm {@code hasRole('ADMIN')})</h3>
 * <ul>
 *   <li>RoleController, PermissionController, GroupController: administração do próprio RBAC é
 *       exclusiva de ADMIN por design — não existe permissão granular para gerenciar permissões
 *       (evita escalada de privilégio).</li>
 *   <li>HealthCheckSystemController: operação de sistema/infraestrutura, admin-only por design.</li>
 *   <li>AuditController./logs: audit log é admin-only por design; /ativos/{id} já usa
 *       permissionService.</li>
 * </ul>
 */
package br.com.aegispatrimonio.controller;
