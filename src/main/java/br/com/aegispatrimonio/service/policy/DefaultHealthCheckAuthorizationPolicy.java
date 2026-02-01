package br.com.aegispatrimonio.service.policy;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.service.IPermissionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DefaultHealthCheckAuthorizationPolicy implements HealthCheckAuthorizationPolicy {

    private final IPermissionService permissionService;

    public DefaultHealthCheckAuthorizationPolicy(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void assertCanUpdate(Usuario usuario, Ativo ativo) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
             throw new AccessDeniedException("Não há contexto de segurança autenticado.");
        }

        // Delegate to Granular RBAC service which handles Admin bypass, Role checks and Context (Filial) verification
        if (!permissionService.hasAtivoPermission(auth, ativo.getId(), "UPDATE")) {
             throw new AccessDeniedException("Você não tem permissão para acessar/modificar este ativo.");
        }
    }
}
