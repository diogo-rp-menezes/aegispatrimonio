package br.com.aegispatrimonio.service.impl;

import br.com.aegispatrimonio.model.*;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

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

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private br.com.aegispatrimonio.service.SecurityAuditService auditService;

    @Mock
    private Authentication authentication;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(usuarioRepository, ativoRepository, funcionarioRepository, new SimpleMeterRegistry(), auditService);
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
        Permission permission = new Permission();
        permission.setResource("RESOURCE");
        permission.setAction("READ");
        role.setPermissions(new HashSet<>(Collections.singletonList(permission)));
        usuario.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasPermission(authentication, null, "RESOURCE", "READ", null);
        assertTrue(result);
    }

    @Test
    void testHasAtivoPermission_Granted() {
        String username = "user@example.com";
        Long ativoId = 1L;
        Long filialId = 10L;

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        // Setup Ativo and Filial
        Ativo ativo = new Ativo();
        ativo.setId(ativoId);
        Filial filial = new Filial();
        filial.setId(filialId);
        ativo.setFilial(filial);

        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        // Setup User, Funcionario, Role, Permission
        Usuario usuario = new Usuario();
        usuario.setEmail(username);

        Funcionario funcionario = new Funcionario();
        funcionario.setFiliais(new HashSet<>(Collections.singletonList(filial)));
        usuario.setFuncionario(funcionario);

        Role role = new Role();
        Permission permission = new Permission();
        permission.setResource("ATIVO");
        permission.setAction("READ");
        permission.setContextKey("filialId");
        role.setPermissions(new HashSet<>(Collections.singletonList(permission)));
        usuario.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasAtivoPermission(authentication, ativoId, "READ");
        assertTrue(result);
    }

    @Test
    void testHasAtivoPermission_Denied_AtivoNotFound() {
        Long ativoId = 1L;
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.empty());

        boolean result = permissionService.hasAtivoPermission(authentication, ativoId, "READ");
        assertFalse(result);
    }

    @Test
    void testHasAtivoPermission_Denied_NoPermission() {
        String username = "user@example.com";
        Long ativoId = 1L;
        Long filialId = 10L;

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        // Setup Ativo and Filial
        Ativo ativo = new Ativo();
        ativo.setId(ativoId);
        Filial filial = new Filial();
        filial.setId(filialId);
        ativo.setFilial(filial);

        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        // Setup User without Permission
        Usuario usuario = new Usuario();
        usuario.setEmail(username);
        // No roles/permissions
        usuario.setRoles(new HashSet<>());

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasAtivoPermission(authentication, ativoId, "READ");
        assertFalse(result);
    }

    @Test
    void testHasAtivoPermission_Denied_ContextMismatch() {
        String username = "user@example.com";
        Long ativoId = 1L;
        Long filialId = 10L;
        Long otherFilialId = 20L;

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        // Setup Ativo and Filial
        Ativo ativo = new Ativo();
        ativo.setId(ativoId);
        Filial filial = new Filial();
        filial.setId(filialId);
        ativo.setFilial(filial);

        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        // Setup User with Permission but different Filial context
        Usuario usuario = new Usuario();
        usuario.setEmail(username);

        Funcionario funcionario = new Funcionario();
        Filial otherFilial = new Filial();
        otherFilial.setId(otherFilialId);
        funcionario.setFiliais(new HashSet<>(Collections.singletonList(otherFilial)));
        usuario.setFuncionario(funcionario);

        Role role = new Role();
        Permission permission = new Permission();
        permission.setResource("ATIVO");
        permission.setAction("READ");
        permission.setContextKey("filialId");
        role.setPermissions(new HashSet<>(Collections.singletonList(permission)));
        usuario.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(usuarioRepository.findWithDetailsByEmail(username)).thenReturn(Optional.of(usuario));

        boolean result = permissionService.hasAtivoPermission(authentication, ativoId, "READ");
        assertFalse(result);
    }
}
