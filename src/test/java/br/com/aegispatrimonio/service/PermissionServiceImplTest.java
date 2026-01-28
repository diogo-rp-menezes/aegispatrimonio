package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.service.impl.PermissionServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Allow unnecessary stubbings (common in setup)
class PermissionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private br.com.aegispatrimonio.repository.FuncionarioRepository funcionarioRepository;

    private PermissionServiceImpl permissionService;
    private MeterRegistry meterRegistry;
    private Usuario usuario;
    private Authentication authentication;

    @Mock
    private br.com.aegispatrimonio.service.SecurityAuditService auditService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        permissionService = new PermissionServiceImpl(usuarioRepository, ativoRepository, funcionarioRepository, meterRegistry, auditService);
    }

    private void createAuthenticatedUser() {
        // Authenticated token requires 3 args (principal, credentials, authorities)
        authentication = new UsernamePasswordAuthenticationToken("user@test.com", "pass", Collections.emptyList());
    }

    @Test
    void testHasPermission_Granted() {
        setupUserWithPermission("ATIVO", "READ", null);
        createAuthenticatedUser();

        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);

        assertTrue(result);
    }

    @Test
    void testHasPermission_Denied_NoPermission() {
        setupUserWithPermission("ATIVO", "READ", null);
        createAuthenticatedUser();

        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "DELETE", null);

        assertFalse(result);
    }

    @Test
    void testHasPermission_Granted_Context() {
        setupUserWithPermission("ATIVO", "READ", "filialId");

        // Add Filial to User
        Filial filial = new Filial();
        filial.setId(100L);
        Funcionario funcionario = new Funcionario();
        funcionario.setFiliais(new HashSet<>(Collections.singletonList(filial)));
        usuario.setFuncionario(funcionario);

        createAuthenticatedUser();

        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", 100L);

        assertTrue(result);
    }

    @Test
    void testHasPermission_Denied_ContextMismatch() {
        setupUserWithPermission("ATIVO", "READ", "filialId");

        // Add Filial to User (different ID)
        Filial filial = new Filial();
        filial.setId(200L);
        Funcionario funcionario = new Funcionario();
        funcionario.setFiliais(new HashSet<>(Collections.singletonList(filial)));
        usuario.setFuncionario(funcionario);

        createAuthenticatedUser();

        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", 100L);

        assertFalse(result);
    }

    @Test
    void testHasPermission_Denied_ContextMissing() {
        setupUserWithPermission("ATIVO", "READ", "filialId");
        createAuthenticatedUser();

        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        // Missing context but permission requires it
        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);

        assertFalse(result);
    }

    @Test
    void testHasPermission_Exception() {
        createAuthenticatedUser();

        // Throw exception from repository
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenThrow(new RuntimeException("DB Error"));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);

        assertFalse(result);
    }

    private void setupUserWithPermission(String resource, String action, String contextKey) {
        usuario = new Usuario();
        usuario.setEmail("user@test.com");

        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setContextKey(contextKey);

        Role role = new Role();
        role.setPermissions(new HashSet<>(Collections.singletonList(permission)));

        usuario.setRoles(new HashSet<>(Collections.singletonList(role)));
    }
}
