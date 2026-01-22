package br.com.aegispatrimonio.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceImplTest {

    private final PermissionServiceImpl service = new PermissionServiceImpl();

    @Test
    void adminShouldAllowAll() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        assertTrue(service.hasPermission(auth, 1L, "ATIVO", "READ", null));
        assertTrue(service.hasPermission(auth, 1L, "ATIVO", "UPDATE", null));
        assertTrue(service.hasPermission(auth, 1L, "ATIVO", "DELETE", null));
    }

    @Test
    void userShouldAllowReadOnly() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        assertTrue(service.hasPermission(auth, 2L, "ATIVO", "READ", null));
        assertFalse(service.hasPermission(auth, 2L, "ATIVO", "UPDATE", null));
        assertFalse(service.hasPermission(auth, 2L, "ATIVO", "DELETE", null));
    }

    @Test
    void unauthenticatedShouldDeny() {
        var auth = new UsernamePasswordAuthenticationToken("anon", "N/A");
        assertFalse(service.hasPermission(auth, 3L, "ATIVO", "READ", null));
    }
}
