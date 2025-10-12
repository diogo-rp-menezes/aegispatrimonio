package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    @DisplayName("Deve refletir os dados de um usuário ativo corretamente")
    void userDetails_deveRefletirUsuarioAtivo() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ativo@aegis.com");
        usuario.setPassword("senha123");
        usuario.setRole("ROLE_USER");
        usuario.setStatus(Status.ATIVO);

        // Act
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        // Assert
        assertEquals("ativo@aegis.com", userDetails.getUsername());
        assertEquals("senha123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
        
        // Os outros métodos sempre retornam true
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Deve refletir o status de um usuário inativo")
    void userDetails_deveRefletirUsuarioInativo() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("inativo@aegis.com");
        usuario.setStatus(Status.INATIVO);

        // Act
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        // Assert
        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Deve retornar o objeto Usuario encapsulado")
    void getUsuario_deveRetornarUsuarioOriginal() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(100L);

        // Act
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        // Assert
        assertSame(usuario, userDetails.getUsuario());
    }
}
