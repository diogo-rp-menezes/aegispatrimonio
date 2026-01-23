package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.service.IPermissionService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private final IPermissionService permissionService;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication, IPermissionService permissionService) {
        super(authentication);
        this.permissionService = permissionService;
    }

    /**
     * RBAC with Context: hasPermission(#id, 'RESOURCE', 'ACTION', #context)
     */
    public boolean hasPermission(Object targetId, String resource, String action, Object context) {
        return permissionService.hasPermission(this.getAuthentication(), targetId, resource, action, context);
    }

    // Override standard hasPermission to delegate to our service as well if needed,
    // but default implementation uses permissionEvaluator.
    // If we want to use our service for standard calls too:
    @Override
    public boolean hasPermission(Object target, Object permission) {
        // Map to (null, target.toString(), permission.toString(), null) or similar?
        // Standard behavior is to use PermissionEvaluator.
        return super.hasPermission(target, permission);
    }

    @Override
    public boolean hasPermission(Object targetId, String targetType, Object permission) {
        return super.hasPermission(targetId, targetType, permission);
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return target;
    }

    public void setThis(Object target) {
        this.target = target;
    }
}
