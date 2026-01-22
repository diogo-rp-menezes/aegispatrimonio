package br.com.aegispatrimonio.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceMetricsTest {

    private MeterRegistry meterRegistry;
    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new PermissionServiceImpl(meterRegistry);
    }

    @Test
    void shouldIncrementAllowAndTimerForAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        boolean result = service.hasPermission(auth, 1L, "ATIVO", "UPDATE", null);
        assertTrue(result);
        double count = meterRegistry.get("aegis_authz_total")
                .tag("outcome", "allow")
                .tag("resource", "ATIVO")
                .tag("action", "UPDATE")
                .counter().count();
        assertEquals(1.0, count, 0.0001);
        // Timer existence check
        assertNotNull(meterRegistry.find("aegis_authz_eval_timer").tag("resource", "ATIVO").tag("action", "UPDATE").timer());
    }

    @Test
    void shouldIncrementDenyForUserUpdate() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user", "N/A", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        boolean result = service.hasPermission(auth, 1L, "ATIVO", "UPDATE", null);
        assertFalse(result);
        double count = meterRegistry.get("aegis_authz_total")
                .tag("outcome", "deny")
                .tag("resource", "ATIVO")
                .tag("action", "UPDATE")
                .counter().count();
        assertEquals(1.0, count, 0.0001);
    }

    @Test
    void shouldIncrementAllowForUserRead() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user", "N/A", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        boolean result = service.hasPermission(auth, 2L, "ATIVO", "READ", null);
        assertTrue(result);
        double count = meterRegistry.get("aegis_authz_total")
                .tag("outcome", "allow")
                .tag("resource", "ATIVO")
                .tag("action", "READ")
                .counter().count();
        assertEquals(1.0, count, 0.0001);
    }
}
