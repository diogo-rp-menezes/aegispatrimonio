package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceMetricsTest {

    private MeterRegistry meterRegistry;
    private UsuarioRepository usuarioRepository;
    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        usuarioRepository = mock(UsuarioRepository.class);
        service = new PermissionServiceImpl(usuarioRepository, meterRegistry);
        ReflectionTestUtils.setField(service, "self", service);
    }

    @Test
    void shouldIncrementAllowAndTimerForAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        Usuario admin = new Usuario();
        Role r = new Role();
        r.setName("ROLE_ADMIN");
        admin.setRoles(new HashSet<>(Collections.singletonList(r)));
        when(usuarioRepository.findWithDetailsByEmail("admin")).thenReturn(Optional.of(admin));

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

        Usuario user = new Usuario();
        user.setRoles(Collections.emptySet());
        when(usuarioRepository.findWithDetailsByEmail("user")).thenReturn(Optional.of(user));

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

        Usuario user = new Usuario();
        Role r = new Role();
        r.setName("ROLE_USER");
        Permission p = new Permission();
        p.setResource("ATIVO");
        p.setAction("READ");
        r.setPermissions(new HashSet<>(Collections.singletonList(p)));
        user.setRoles(new HashSet<>(Collections.singletonList(r)));

        when(usuarioRepository.findWithDetailsByEmail("user")).thenReturn(Optional.of(user));

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
