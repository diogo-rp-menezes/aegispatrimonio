package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.service.IPermissionService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the Permission Service for Granular RBAC.
 * <p>
 * This service handles authorization logic by checking user roles and permissions persisted in the database.
 * It supports:
 * <ul>
 *     <li>Role-based permissions (Resource + Action)</li>
 *     <li>Contextual verification (e.g. Filial access)</li>
 *     <li>Caching of user permissions and context access for performance</li>
 *     <li>Micrometer metrics for observability (allow/deny counts, timing)</li>
 * </ul>
 */
@Service("permissionService")
public class PermissionServiceImpl implements IPermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final AtivoRepository ativoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final MeterRegistry meterRegistry;
    private final br.com.aegispatrimonio.service.SecurityAuditService auditService;

    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private PermissionServiceImpl self;

    public PermissionServiceImpl(UsuarioRepository usuarioRepository, AtivoRepository ativoRepository, FuncionarioRepository funcionarioRepository, MeterRegistry meterRegistry, br.com.aegispatrimonio.service.SecurityAuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.ativoRepository = ativoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.meterRegistry = meterRegistry;
        this.auditService = auditService;
    }

    /**
     * Checks if the authenticated user has the required permission.
     *
     * @param authentication The current security authentication.
     * @param targetId       The ID of the target object (optional).
     * @param resource       The resource being accessed (e.g., 'ATIVO').
     * @param action         The action being performed (e.g., 'READ').
     * @param context        The context for the permission (e.g., filialId).
     * @return true if allowed, false otherwise.
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetId, String resource, String action, Object context) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean allowed = false;
        String denialReason = null;

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                denialReason = "Unauthenticated";
                log.debug("[AUTHZ] Deny: {}", denialReason);
                return false;
            }

            String username = authentication.getName();
            // Use 'self' to trigger cache proxy if available
            PermissionServiceImpl effectiveSelf = (self != null) ? self : this;

            // 0. Admin Bypass
            if (effectiveSelf.hasRole(username, "ROLE_ADMIN")) {
                log.debug("[AUTHZ] Allow: Admin Bypass for {}", username);
                allowed = true;
                return true;
            }

            Set<Permission> permissions = effectiveSelf.getUserPermissions(username);

            // 1. Check if user has the specific Permission (Resource + Action)
            Permission matchedPermission = permissions.stream()
                    .filter(p -> p.getResource().equalsIgnoreCase(resource) && p.getAction().equalsIgnoreCase(action))
                    .findFirst()
                    .orElse(null);

            if (matchedPermission == null) {
                denialReason = String.format("No permission found for %s on %s/%s", username, resource, action);
                log.debug("[AUTHZ] Deny: {}", denialReason);
                return false;
            }

            // 2. Check Context
            if (matchedPermission.getContextKey() != null && !matchedPermission.getContextKey().isEmpty()) {
                if (context == null) {
                    denialReason = String.format("Context required (%s) but missing for %s on %s/%s",
                            matchedPermission.getContextKey(), username, resource, action);
                    log.warn("[AUTHZ] Deny: {}", denialReason);
                    return false;
                }

                if ("filialId".equalsIgnoreCase(matchedPermission.getContextKey())) {
                    if (!effectiveSelf.hasContextAccess(username, context)) {
                        denialReason = String.format("Context mismatch. User %s has no access to context %s", username, context);
                        log.debug("[AUTHZ] Deny: {}", denialReason);
                        return false;
                    }
                }
            }

            allowed = true;
            return true;

        } catch (Exception e) {
            denialReason = "Error evaluating permission: " + e.getMessage();
            log.error("[AUTHZ] {}", denialReason, e);
            return false; // Fail safe
        } finally {
             try {
                 String username = (authentication != null) ? authentication.getName() : "anonymous";
                 String contextStr = (context != null) ? context.toString() : null;
                 if (auditService != null) {
                    auditService.logAuthorization(username, resource, action, contextStr, allowed, denialReason);
                 }
             } catch (Exception ex) {
                 log.error("Failed to trigger audit log", ex);
             }

             try {
                meterRegistry.counter(
                        "aegis_authz_total",
                        Tags.of("outcome", allowed ? "allow" : "deny",
                                "resource", nullSafe(resource),
                                "action", nullSafe(action))
                ).increment();
                sample.stop(Timer.builder("aegis_authz_eval_timer")
                        .tags("resource", nullSafe(resource), "action", nullSafe(action))
                        .register(meterRegistry));
            } catch (Exception e) {
                log.debug("[AUTHZ][METRICS] Failed to record metrics", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAtivoPermission(Authentication authentication, Long ativoId, String action) {
        if (ativoId == null) return false;

        // Lookup Ativo to get Filial ID
        return ativoRepository.findById(ativoId)
                .map(ativo -> {
                    Long filialId = (ativo.getFilial() != null) ? ativo.getFilial().getId() : null;
                    return hasPermission(authentication, ativoId, "ATIVO", action, filialId);
                })
                .orElse(false); // If Ativo not found, deny access
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFuncionarioPermission(Authentication authentication, Long funcionarioId, String action) {
        if (funcionarioId == null) return false;

        return funcionarioRepository.findById(funcionarioId)
                .map(funcionario -> {
                    Set<Long> filiaisIds = new HashSet<>();
                    if (funcionario.getFiliais() != null) {
                        for (Filial f : funcionario.getFiliais()) {
                            filiaisIds.add(f.getId());
                        }
                    }
                    return hasPermission(authentication, funcionarioId, "FUNCIONARIO", action, filiaisIds);
                })
                .orElse(false);
    }

    /**
     * Retrieves all permissions for a user, aggregated from their roles.
     * Cached by username.
     *
     * @param username The username (email).
     * @return A set of permissions.
     */
    @Cacheable(value = "userPermissions", key = "#username", unless = "#result == null")
    @Transactional(readOnly = true)
    public Set<Permission> getUserPermissions(String username) {
        return usuarioRepository.findWithDetailsByEmail(username)
                .map(u -> {
                    Set<Permission> perms = new HashSet<>();
                    if (u.getRoles() != null) {
                        for (Role role : u.getRoles()) {
                            if (role.getPermissions() != null) {
                                perms.addAll(role.getPermissions());
                            }
                        }
                    }
                    return perms;
                })
                .orElse(Collections.emptySet());
    }

    /**
     * Checks if a user has a specific role.
     * Cached by username and role name.
     *
     * @param username The username.
     * @param roleName The role name to check.
     * @return true if the user has the role.
     */
    @Cacheable(value = "userRoles", key = "#username + '-' + #roleName")
    @Transactional(readOnly = true)
    public boolean hasRole(String username, String roleName) {
        return usuarioRepository.findWithDetailsByEmail(username)
                .map(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> roleName.equals(r.getName())))
                .orElse(false);
    }

    /**
     * Checks if a user has access to a specific context (e.g., Filial ID).
     * If context is a Collection, checks access to ALL elements.
     * Cached by username and context.
     *
     * @param username The username.
     * @param context  The context ID or Collection of IDs.
     * @return true if user has access.
     */
    @Cacheable(value = "userContext", key = "#username + '-' + #context.toString()", unless = "#result == false")
    @Transactional(readOnly = true)
    public boolean hasContextAccess(String username, Object context) {
         return usuarioRepository.findWithDetailsByEmail(username)
                .map(u -> {
                    if (u.getFuncionario() == null || u.getFuncionario().getFiliais() == null) {
                        return false;
                    }

                    Set<Long> allowedFiliais = new HashSet<>();
                    u.getFuncionario().getFiliais().forEach(f -> allowedFiliais.add(f.getId()));

                    if (context instanceof Collection<?>) {
                        Collection<?> requestedContexts = (Collection<?>) context;
                        if (requestedContexts.isEmpty()) return true;

                        for (Object item : requestedContexts) {
                            Long id = parseId(item);
                            if (id == null || !allowedFiliais.contains(id)) {
                                return false;
                            }
                        }
                        return true;
                    } else if (context instanceof Object[]) {
                        Object[] requestedContexts = (Object[]) context;
                        if (requestedContexts.length == 0) return true;

                        for (Object item : requestedContexts) {
                            Long id = parseId(item);
                            if (id == null || !allowedFiliais.contains(id)) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        Long id = parseId(context);
                        return id != null && allowedFiliais.contains(id);
                    }
                })
                .orElse(false);
    }

    private Long parseId(Object item) {
        try {
            return Long.valueOf(String.valueOf(item));
        } catch (NumberFormatException e) {
            log.warn("Invalid context ID format: {}", item);
            return null;
        }
    }

    private String nullSafe(String v) {
        return v == null ? "UNKNOWN" : v;
    }
}
