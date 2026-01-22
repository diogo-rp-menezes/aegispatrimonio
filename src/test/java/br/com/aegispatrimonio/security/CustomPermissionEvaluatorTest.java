package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.service.IPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomPermissionEvaluatorTest {

    private IPermissionService permissionService;
    private CustomPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        permissionService = Mockito.mock(IPermissionService.class);
        evaluator = new CustomPermissionEvaluator(permissionService);
    }

    @Test
    void shouldDelegateToService_whenUsingTargetObjectOverload() {
        var auth = new UsernamePasswordAuthenticationToken("user", "N/A");
        when(permissionService.hasPermission(any(), any(), anyString(), anyString(), isNull())).thenReturn(true);

        boolean result = evaluator.hasPermission(auth, "ATIVO", "READ");

        assertTrue(result);
        ArgumentCaptor<String> resourceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(permissionService).hasPermission(eq(auth), isNull(), resourceCaptor.capture(), actionCaptor.capture(), isNull());
        // Expect resource="ATIVO" and action="READ"
        assertTrue("ATIVO".equals(resourceCaptor.getValue()));
        assertTrue("READ".equals(actionCaptor.getValue()));
    }

    @Test
    void shouldDelegateToService_whenUsingTargetIdOverload() {
        var auth = new UsernamePasswordAuthenticationToken("admin", "N/A");
        when(permissionService.hasPermission(any(), any(), anyString(), anyString(), isNull())).thenReturn(true);

        Serializable targetId = 42L;
        boolean result = evaluator.hasPermission(auth, targetId, "FUNCIONARIO", "UPDATE");

        assertTrue(result);
        ArgumentCaptor<Object> idCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> resourceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(permissionService).hasPermission(eq(auth), idCaptor.capture(), resourceCaptor.capture(), actionCaptor.capture(), isNull());
        assertTrue(42L == (Long) idCaptor.getValue());
        assertTrue("FUNCIONARIO".equals(resourceCaptor.getValue()));
        assertTrue("UPDATE".equals(actionCaptor.getValue()));
    }
}
