package br.com.aegispatrimonio.service;

import org.springframework.security.core.Authentication;

/**
 * Service de autorização contextual (RBAC granular).
 */
public interface IPermissionService {

    /**
     * Verifica se o usuário autenticado possui permissão para a ação no recurso.
     * @param authentication Authentication do Spring Security
     * @param targetId identificador do alvo (opcional)
     * @param resource tipo do recurso (ex.: "ATIVO")
     * @param action ação desejada (ex.: "READ", "CREATE", "UPDATE", "DELETE")
     * @param context contexto opcional (ex.: filialId)
     * @return true se permitido, false caso contrário
     */
    boolean hasPermission(Authentication authentication,
                          Object targetId,
                          String resource,
                          String action,
                          Object context);

    /**
     * Verifica permissão em um Ativo específico, resolvendo o contexto (Filial) automaticamente.
     */
    boolean hasAtivoPermission(Authentication authentication, Long ativoId, String action);

    /**
     * Verifica permissão em um Funcionário específico, resolvendo o contexto (Filiais) automaticamente.
     */
    boolean hasFuncionarioPermission(Authentication authentication, Long funcionarioId, String action);
}
