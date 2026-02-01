package br.com.aegispatrimonio.service.policy;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.service.IPermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultHealthCheckAuthorizationPolicyTest {

    @Mock
    private IPermissionService permissionService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DefaultHealthCheckAuthorizationPolicy policy;

    private Usuario usuario;
    private Ativo ativo;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        usuario = new Usuario();
        usuario.setId(1L);

        ativo = new Ativo();
        ativo.setId(100L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if no authentication in context")
    void assertCanUpdate_noAuth_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(usuario, ativo));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if not authenticated")
    void assertCanUpdate_notAuthenticated_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(usuario, ativo));
    }

    @Test
    @DisplayName("Should allow update if permissionService returns true")
    void assertCanUpdate_permissionGranted_shouldAllow() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(permissionService.hasAtivoPermission(authentication, ativo.getId(), "UPDATE")).thenReturn(true);

        assertDoesNotThrow(() -> policy.assertCanUpdate(usuario, ativo));

        verify(permissionService).hasAtivoPermission(authentication, ativo.getId(), "UPDATE");
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if permissionService returns false")
    void assertCanUpdate_permissionDenied_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(permissionService.hasAtivoPermission(authentication, ativo.getId(), "UPDATE")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> policy.assertCanUpdate(usuario, ativo));
    }
}
