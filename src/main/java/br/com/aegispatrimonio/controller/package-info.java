/**
 * Controllers da API v1.
 *
 * <h2>Modelo de autorização unificado (resolução do achado H1 do production-code-audit
 * 2026-09-26)</h2>
 *
 * <p>Padrão obrigatório: todos os controllers de domínio usam
 * {@code @PreAuthorize("@permissionService.hasPermission(authentication, targetId, RESOURCE, ACTION, context)")}.
 * O {@code PermissionServiceImpl} aplica admin bypass interno ({@code ROLE_ADMIN} → allow),
 * portanto ADMIN não perde acesso com a migração de {@code hasRole} para {@code permissionService}.</p>
 *
 * <h3>Mapeamento controller → recurso/ação</h3>
 * <ul>
 *   <li>ManutencaoController — {@code ATIVO:CREATE/READ/UPDATE/DELETE}. Par {@code MANUTENCAO:*}
 *       não existe no seed (V6); manutenção é operação sobre ativo. Workflow
 *       (aprovar/iniciar/concluir/cancelar) = {@code ATIVO:UPDATE}.</li>
 *   <li>MovimentacaoController — {@code ATIVO:CREATE/READ/UPDATE/DELETE}. Par
 *       {@code MOVIMENTACAO:*} não existe no seed; efetivar/cancelar = {@code ATIVO:UPDATE}.</li>
 *   <li>DepreciacaoController — {@code ATIVO:UPDATE} (recalcular), {@code ATIVO:READ}
 *       (calcular-mensal). Par {@code DEPRECIACAO:*} não existe no seed.</li>
 *   <li>TipoAtivoController — {@code ATIVO:READ/CREATE/UPDATE/DELETE} (par
 *       {@code TIPO_ATIVO:*} ausente no seed).</li>
 *   <li>FilialController — {@code ATIVO:READ/CREATE/UPDATE/DELETE} (par {@code FILIAL:*} ausente).</li>
 *   <li>DepartamentoController — {@code ATIVO:READ/CREATE/UPDATE/DELETE}; CREATE com context
 *       {@code #departamentoCreateDTO.filialId} (par {@code DEPARTAMENTO:*} ausente).</li>
 *   <li>LocalizacaoController — {@code ATIVO:READ/CREATE/UPDATE/DELETE}; CREATE com context
 *       {@code #localizacaoCreateDTO.filialId} (par {@code LOCALIZACAO:*} ausente).</li>
 *   <li>DashboardController — {@code ATIVO:READ} (par {@code DASHBOARD:*} ausente).</li>
 *   <li>AlertaController — {@code ATIVO:READ} (listar/recent), {@code ATIVO:UPDATE}
 *       (markAsRead) (par {@code ALERTA:*} ausente).</li>
 *   <li>AtivoController / FornecedorController / FuncionarioController — padrão de referência,
 *       já migrados anteriormente ({@code ATIVO:*} / {@code FORNECEDOR:*} / {@code FUNCIONARIO:*}).</li>
 * </ul>
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
 *
 * <h3>Pendências</h3>
 * <p>Pares RESOURCE:ACTION ausentes no seed V6: {@code MANUTENCAO:*}, {@code MOVIMENTACAO:*},
 * {@code DEPRECIACAO:*}, {@code TIPO_ATIVO:*}, {@code FILIAL:*}, {@code DEPARTAMENTO:*},
 * {@code LOCALIZACAO:*}, {@code DASHBOARD:*}, {@code ALERTA:*}. Enquanto não forem criados em
 * migration futura, o acesso granular a esses domínios é concedido via {@code ATIVO:*}
 * (solução de contorno registrada; nenhum dado de seed foi criado nesta subtask).</p>
 */
package br.com.aegispatrimonio.controller;
