package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.service.impl.PermissionServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Handle self-injection for cacheable methods calling
        ReflectionTestUtils.setField(permissionService, "self", permissionService);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUnauthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasAdminRole() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@aegis.com");

        Usuario admin = new Usuario();
        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        admin.setRoles(Set.of(roleAdmin));

        when(usuarioRepository.findWithDetailsByEmail("admin@aegis.com")).thenReturn(Optional.of(admin));

        assertTrue(permissionService.hasPermission(authentication, null, "ANY", "ANY", null));
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasPermission() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@aegis.com");

        Usuario user = new Usuario();
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        Permission perm = new Permission();
        perm.setResource("ATIVO");
        perm.setAction("READ");
        roleUser.setPermissions(Set.of(perm));
        user.setRoles(Set.of(roleUser));

        when(usuarioRepository.findWithDetailsByEmail("user@aegis.com")).thenReturn(Optional.of(user));

        assertTrue(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserDoesNotHavePermission() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@aegis.com");

        Usuario user = new Usuario();
        user.setRoles(Collections.emptySet());

        when(usuarioRepository.findWithDetailsByEmail("user@aegis.com")).thenReturn(Optional.of(user));

        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }

    @Test
    void hasPermission_ShouldCheckContext_WhenRequired() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@aegis.com");

        Usuario user = new Usuario();
        Role roleUser = new Role();
        Permission perm = new Permission();
        perm.setResource("ATIVO");
        perm.setAction("READ");
        perm.setContextKey("filialId");
        roleUser.setPermissions(Set.of(perm));
        user.setRoles(Set.of(roleUser));

        Funcionario func = new Funcionario();
        Filial f1 = new Filial();
        f1.setId(1L);
        func.setFiliais(Set.of(f1));
        user.setFuncionario(func);

        when(usuarioRepository.findWithDetailsByEmail("user@aegis.com")).thenReturn(Optional.of(user));

        // Allowed context
        assertTrue(permissionService.hasPermission(authentication, null, "ATIVO", "READ", 1L));

        // Denied context
        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", 2L));

        // Missing context
        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }
}
