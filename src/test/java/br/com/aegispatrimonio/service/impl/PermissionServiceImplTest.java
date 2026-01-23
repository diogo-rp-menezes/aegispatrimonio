package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Usuario usuario;
    private Role roleUser;
    private Permission permissionAtivoRead;
    private Permission permissionAtivoCreate;

    @BeforeEach
    void setUp() {
        // Setup Mocks for Metrics
        Timer.Sample sample = mock(Timer.Sample.class);
        // We can mock static Timer.start if we want, but since we inject meterRegistry,
        // it might be easier to just ensure meterRegistry returns mocks.
        // However, Timer.start(registry) is static. Mocking statics is hard without Mockito-inline.
        // The service calls Timer.start(meterRegistry).
        // Let's assume standard behavior or use a simple registry if needed.
        // But wait, the service does: Timer.Sample sample = Timer.start(meterRegistry);
        // This is a static call.
        // If we don't mock the static, it executes. If meterRegistry is a mock, it might work if the call is robust.
        // Actually Timer.start() returns a Sample. It calls registry.config().clock()...
        // This might fail with a mock Registry unless we stub it deep.
        // A better approach for unit testing logic is to perhaps ignore metrics or use a SimpleMeterRegistry.

        // Re-initialize service with SimpleMeterRegistry to avoid mocking issues with Metrics
        permissionService = new PermissionServiceImpl(usuarioRepository, new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

        // Setup User and Roles
        usuario = new Usuario();
        usuario.setEmail("user@test.com");

        roleUser = new Role();
        roleUser.setName("ROLE_USER");

        permissionAtivoRead = new Permission(1L, "ATIVO", "READ", "Read Ativos", null); // No context
        permissionAtivoCreate = new Permission(2L, "ATIVO", "CREATE", "Create Ativos", "filialId"); // Context required

        roleUser.setPermissions(new HashSet<>());
        roleUser.getPermissions().add(permissionAtivoRead);
        roleUser.getPermissions().add(permissionAtivoCreate);

        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(roleUser);
    }

    @Test
    void shouldAllowWhenUserHasPermissionAndNoContextRequired() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);

        assertTrue(result);
    }

    @Test
    void shouldDenyWhenPermissionMissing() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        // User has ATIVO/READ and ATIVO/CREATE, but checking ATIVO/DELETE
        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "DELETE", null);

        assertFalse(result);
    }

    @Test
    void shouldDenyWhenContextRequiredButMissing() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        // ATIVO/CREATE requires 'filialId', but we pass null context
        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "CREATE", null);

        assertFalse(result);
    }

    @Test
    void shouldAllowWhenUserHasPermissionAndContextMatches() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        // Setup Filial match
        Funcionario func = new Funcionario();
        Filial filial1 = new Filial();
        filial1.setId(10L);
        func.setFiliais(new HashSet<>(Collections.singletonList(filial1)));
        usuario.setFuncionario(func);

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "CREATE", 10L);

        assertTrue(result);
    }

    @Test
    void shouldDenyWhenContextMismatch() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        // Setup Filial match for ID 10
        Funcionario func = new Funcionario();
        Filial filial1 = new Filial();
        filial1.setId(10L);
        func.setFiliais(new HashSet<>(Collections.singletonList(filial1)));
        usuario.setFuncionario(func);

        // Check for ID 99
        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "CREATE", 99L);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseOnException() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findWithDetailsByEmail(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);

        assertFalse(result);
    }

    @Test
    void shouldDenyUnauthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        boolean result = permissionService.hasPermission(authentication, null, "ATIVO", "READ", null);
        assertFalse(result);

        boolean resultNull = permissionService.hasPermission(null, null, "ATIVO", "READ", null);
        assertFalse(resultNull);
    }
}
