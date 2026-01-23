package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.service.IPermissionService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final IPermissionService permissionService;

    public CustomPermissionEvaluator(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (permission == null) return false;
        String action = String.valueOf(permission);
        // Quando não passamos um type explícito, tratamos o próprio target como "resource"
        String resource = targetDomainObject != null ? String.valueOf(targetDomainObject) : "UNKNOWN";
        return permissionService.hasPermission(authentication, null, resource, action, null);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (permission == null) return false;

        String action;
        Object context = null;

        if (permission instanceof Object[] params && params.length >= 1) {
             action = String.valueOf(params[0]);
             if (params.length > 1) {
                 context = params[1];
             }
        } else if (permission instanceof java.util.List<?> params && !params.isEmpty()) {
             action = String.valueOf(params.get(0));
             if (params.size() > 1) {
                 context = params.get(1);
             }
        } else {
             action = String.valueOf(permission);
        }

        String resource = targetType;
        return permissionService.hasPermission(authentication, targetId, resource, action, context);
    }
}
