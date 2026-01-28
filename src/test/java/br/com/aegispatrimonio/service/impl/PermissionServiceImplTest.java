package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private PermissionServiceImpl permissionService;

    @Mock
    private br.com.aegispatrimonio.service.SecurityAuditService auditService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(usuarioRepository, ativoRepository, funcionarioRepository, meterRegistry, auditService);
        ReflectionTestUtils.setField(permissionService, "self", permissionService);
    }

    @Test
    void testHasPermission_Granted() {
        String username = "user@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        Role role = new Role();
        role.setName("ROLE_USER");
        Permission permission = new Permission(1L, "ATIVO", "READ", "Read Asset", null);
        role.setPermissions(Set.of(permission));
        usuario.setRoles(Set.of(role));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        assertTrue(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }

    @Test
    void testHasPermission_Denied_NoPermission() {
        String username = "user@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        Role role = new Role();
        role.setName("ROLE_USER");
        // No permissions
        usuario.setRoles(Set.of(role));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "WRITE", null));
    }

    @Test
    void testHasPermission_Denied_ContextMismatch() {
        String username = "user@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        Role role = new Role();
        role.setName("ROLE_USER");
        Permission permission = new Permission(1L, "ATIVO", "READ", "Read Asset", "filialId");
        role.setPermissions(Set.of(permission));
        usuario.setRoles(Set.of(role));

        Funcionario funcionario = new Funcionario();
        Filial filial1 = new Filial();
        filial1.setId(1L);
        funcionario.setFiliais(Set.of(filial1));
        usuario.setFuncionario(funcionario);

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        // Requesting access for context 2L, but user has access to 1L
        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", 2L));
    }

    @Test
    void testHasPermission_Granted_ContextMatch() {
        String username = "user@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        Role role = new Role();
        role.setName("ROLE_USER");
        Permission permission = new Permission(1L, "ATIVO", "READ", "Read Asset", "filialId");
        role.setPermissions(Set.of(permission));
        usuario.setRoles(Set.of(role));

        Funcionario funcionario = new Funcionario();
        Filial filial1 = new Filial();
        filial1.setId(1L);
        funcionario.setFiliais(Set.of(filial1));
        usuario.setFuncionario(funcionario);

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        // Requesting access for context 1L, user has access to 1L
        assertTrue(permissionService.hasPermission(authentication, null, "ATIVO", "READ", 1L));
    }

    @Test
    void testHasPermission_AdminBypass() {
        String username = "admin@example.com";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        usuario.setRoles(Set.of(role));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        assertTrue(permissionService.hasPermission(authentication, null, "ANY_RESOURCE", "ANY_ACTION", null));
    }

    @Test
    void testHasPermission_Unauthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        assertFalse(permissionService.hasPermission(authentication, null, "ATIVO", "READ", null));
    }
}
