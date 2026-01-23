package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
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
@Service
public class PermissionServiceImpl implements IPermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final MeterRegistry meterRegistry;

    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private PermissionServiceImpl self;

    public PermissionServiceImpl(UsuarioRepository usuarioRepository, MeterRegistry meterRegistry) {
        this.usuarioRepository = usuarioRepository;
        this.meterRegistry = meterRegistry;
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

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("[AUTHZ] Deny: Unauthenticated");
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
                log.debug("[AUTHZ] Deny: No permission found for {} on {}/{}", username, resource, action);
                return false;
            }

            // 2. Check Context
            if (matchedPermission.getContextKey() != null && !matchedPermission.getContextKey().isEmpty()) {
                if (context == null) {
                    log.warn("[AUTHZ] Deny: Context required ({}) but missing for {} on {}/{}",
                            matchedPermission.getContextKey(), username, resource, action);
                    return false;
                }

                if ("filialId".equalsIgnoreCase(matchedPermission.getContextKey())) {
                    if (!effectiveSelf.hasContextAccess(username, context)) {
                        log.debug("[AUTHZ] Deny: Context mismatch. User {} has no access to filial {}", username, context);
                        return false;
                    }
                }
            }

            allowed = true;
            return true;

        } catch (Exception e) {
            log.error("[AUTHZ] Error evaluating permission: {}", e.getMessage(), e);
            return false; // Fail safe
        } finally {
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
     * Cached by username and context ID.
     *
     * @param username The username.
     * @param context  The context ID (usually Filial ID).
     * @return true if user has access.
     */
    @Cacheable(value = "userContext", key = "#username + '-' + #context", unless = "#result == false")
    @Transactional(readOnly = true)
    public boolean hasContextAccess(String username, Object context) {
         return usuarioRepository.findWithDetailsByEmail(username)
                .map(u -> {
                    if (u.getFuncionario() == null || u.getFuncionario().getFiliais() == null) {
                        return false;
                    }
                    Long filialId;
                    try {
                        filialId = Long.valueOf(String.valueOf(context));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid context ID format: {}", context);
                        return false;
                    }

                    return u.getFuncionario().getFiliais().stream()
                            .anyMatch(f -> f.getId().equals(filialId));
                })
                .orElse(false);
    }

    private String nullSafe(String v) {
        return v == null ? "UNKNOWN" : v;
    }
}
