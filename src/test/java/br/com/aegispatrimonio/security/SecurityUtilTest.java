package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    @BeforeEach
    void setUp() {
        // Limpa o contexto antes de cada teste para garantir o isolamento
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Limpa o contexto após cada teste
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String role, Set<Filial> filiais) {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@test.com");
        usuario.setRole(role);

        Funcionario funcionario = new Funcionario();
        funcionario.setFiliais(filiais);
        usuario.setFuncionario(funcionario);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("getAuthenticatedUser deve retornar CustomUserDetails quando usuário está autenticado")
    void getAuthenticatedUser_deveRetornarUserDetails_quandoAutenticado() {
        // Arrange
        mockSecurityContext("ROLE_USER", Collections.emptySet());

        // Act
        CustomUserDetails userDetails = SecurityUtil.getAuthenticatedUser();

        // Assert
        assertNotNull(userDetails);
        assertEquals("user@test.com", userDetails.getUsername());
    }

    @Test
    @DisplayName("getAuthenticatedUser deve retornar null quando não há autenticação")
    void getAuthenticatedUser_deveRetornarNull_quandoNaoAutenticado() {
        // Act
        CustomUserDetails userDetails = SecurityUtil.getAuthenticatedUser();

        // Assert
        assertNull(userDetails);
    }

    @Test
    @DisplayName("isCurrentUserAdmin deve retornar true para usuário com ROLE_ADMIN")
    void isCurrentUserAdmin_deveRetornarTrue_paraAdmin() {
        // Arrange
        mockSecurityContext("ROLE_ADMIN", Collections.emptySet());

        // Act & Assert
        assertTrue(SecurityUtil.isCurrentUserAdmin());
    }

    @Test
    @DisplayName("isCurrentUserAdmin deve retornar false para usuário sem ROLE_ADMIN")
    void isCurrentUserAdmin_deveRetornarFalse_paraNaoAdmin() {
        // Arrange
        mockSecurityContext("ROLE_USER", Collections.emptySet());

        // Act & Assert
        assertFalse(SecurityUtil.isCurrentUserAdmin());
    }

    @Test
    @DisplayName("isCurrentUserAdmin deve retornar false quando não há autenticação")
    void isCurrentUserAdmin_deveRetornarFalse_paraNaoAutenticado() {
        // Act & Assert
        assertFalse(SecurityUtil.isCurrentUserAdmin());
    }

    @Test
    @DisplayName("getCurrentUserFilialIds deve retornar os IDs das filiais do usuário")
    void getCurrentUserFilialIds_deveRetornarSetDeIds() {
        // Arrange
        Filial f1 = new Filial();
        f1.setId(10L);
        Filial f2 = new Filial();
        f2.setId(20L);
        mockSecurityContext("ROLE_USER", Set.of(f1, f2));

        // Act
        Set<Long> filialIds = SecurityUtil.getCurrentUserFilialIds();

        // Assert
        assertNotNull(filialIds);
        assertEquals(2, filialIds.size());
        assertTrue(filialIds.contains(10L));
        assertTrue(filialIds.contains(20L));
    }

    @Test
    @DisplayName("getCurrentUserFilialIds deve retornar Set vazio se usuário não tem filiais")
    void getCurrentUserFilialIds_deveRetornarSetVazio_seNaoTemFiliais() {
        // Arrange
        mockSecurityContext("ROLE_USER", Collections.emptySet());

        // Act
        Set<Long> filialIds = SecurityUtil.getCurrentUserFilialIds();

        // Assert
        assertNotNull(filialIds);
        assertTrue(filialIds.isEmpty());
    }

    @Test
    @DisplayName("getCurrentUserFilialIds deve retornar Set vazio se não há autenticação")
    void getCurrentUserFilialIds_deveRetornarSetVazio_seNaoAutenticado() {
        // Act
        Set<Long> filialIds = SecurityUtil.getCurrentUserFilialIds();

        // Assert
        assertNotNull(filialIds);
        assertTrue(filialIds.isEmpty());
    }
}
