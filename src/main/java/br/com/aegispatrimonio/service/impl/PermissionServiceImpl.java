package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.service.IPermissionService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements IPermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final MeterRegistry meterRegistry;

    public PermissionServiceImpl() {
        // Construtor padrão para testes simples; utiliza um registry simples em memória
        this(new SimpleMeterRegistry());
    }

    public PermissionServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetId, String resource, String action, Object context) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean allowed = false;
        String outcomeLabel;
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                outcomeLabel = "deny";
                return false;
            }
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // ADMIN tem acesso total
            if (roles.contains(ROLE_ADMIN)) {
                allowed = true;
                outcomeLabel = "allow";
                log.debug("[AUTHZ] allow=TRUE subject={} roles={} resource={} action={} targetId={} context={}",
                        authentication.getName(), roles, resource, action, targetId, safeContext(context));
                return true;
            }

            // PoC: USER tem apenas READ
            if (roles.contains(ROLE_USER) && "READ".equalsIgnoreCase(action)) {
                allowed = true;
                outcomeLabel = "allow";
                log.debug("[AUTHZ] allow=TRUE (READ by USER) subject={} roles={} resource={} action={} targetId={} context={}",
                        authentication.getName(), roles, resource, action, targetId, safeContext(context));
                return true;
            }

            outcomeLabel = "deny";
            log.info("[AUTHZ] allow=FALSE subject={} roles={} resource={} action={} targetId={} context={}",
                    authentication.getName(), roles, resource, action, targetId, safeContext(context));
            return false;
        } finally {
            // Métricas Micrometer
            try {
                meterRegistry.counter(
                        "aegis_authz_total",
                        Tags.of("outcome", (authentication == null || !authentication.isAuthenticated()) ? "deny" : (allowed ? "allow" : "deny"),
                                "resource", nullSafe(resource),
                                "action", nullSafe(action))
                ).increment();
                sample.stop(Timer.builder("aegis_authz_eval_timer")
                        .tags("resource", nullSafe(resource), "action", nullSafe(action))
                        .register(meterRegistry));
            } catch (Exception e) {
                // Nunca falhar a autorização por problemas de métricas
                log.debug("[AUTHZ][METRICS] Falha ao registrar métricas: {}", e.getMessage());
            }
        }
    }

    private String nullSafe(String v) {
        return v == null ? "UNKNOWN" : v;
    }

    private Object safeContext(Object context) {
        // Garantir que não vazamos dados sensíveis (PoC simples)
        if (context == null) return null;
        String s = String.valueOf(context);
        return s.length() > 128 ? s.substring(0, 128) + "..." : s;
    }
}
