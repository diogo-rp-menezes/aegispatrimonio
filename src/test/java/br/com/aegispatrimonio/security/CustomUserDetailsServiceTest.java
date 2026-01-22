package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario testUser;

    @BeforeEach
    void setUp() {
        testUser = new Usuario();
        testUser.setId(1L);
        testUser.setEmail("test@aegis.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("Deve carregar o usuário pelo email com sucesso")
    void loadUserByUsername_deveRetornarUserDetails_quandoUsuarioExiste() {
        // Arrange
        String email = "test@aegis.com";
        when(usuarioRepository.findWithDetailsByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não existe")
    void loadUserByUsername_deveLancarExcecao_quandoUsuarioNaoExiste() {
        // Arrange
        String email = "notfound@aegis.com";
        when(usuarioRepository.findWithDetailsByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(email);
        });

        assertEquals("Usuário não encontrado com o e-mail: " + email, exception.getMessage());
    }
}
