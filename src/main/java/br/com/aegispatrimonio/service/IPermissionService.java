package br.com.aegispatrimonio.service;

import org.springframework.security.core.Authentication;

/**
 * Service de autorização contextual (RBAC granular) — SPI mínima para PoC.
 * Implementação inicial concede:
 * - ADMIN: acesso total a todos os recursos/ações
 * - USER: acesso de leitura (READ) por padrão
 * Futuras extensões: verificar permissões persistidas e contexto (filialId, etc.).
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
}
