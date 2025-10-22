package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest; // Removido
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional; // Removido

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Removido: Herda de BaseIT
@AutoConfigureMockMvc
// @Transactional // Removido: Este teste não precisa de transação, pois usa mocks e não interage diretamente com o DB para persistência.
class JwtAuthFilterTest extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UserDetails createCustomUserDetails(String email, String role) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setRole(role);
        usuario.setPassword("password");
        return new CustomUserDetails(usuario);
    }

    @Test
    @DisplayName("Filtro: Deve retornar 401 Unauthorized para requisição sem token")
    void doFilterInternal_semToken_deveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/ativos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Filtro: Deve retornar 401 Unauthorized para requisição com token inválido")
    void doFilterInternal_comTokenInvalido_deveRetornarUnauthorized() throws Exception {
        String token = "some-invalid-token";
        UserDetails userDetails = createCustomUserDetails("test@aegis.com", "ROLE_USER");

        when(jwtService.extractUsername(anyString())).thenReturn("test@aegis.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        mockMvc.perform(get("/api/v1/ativos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Filtro: Deve lançar UsernameNotFoundException se o usuário do token não for encontrado")
    void doFilterInternal_usuarioNaoEncontrado_deveLancarExcecao() {
        String token = "token-for-non-existing-user";
        String email = "non.existing@aegis.com";

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenThrow(new UsernameNotFoundException("User not found"));

        // CORREÇÃO: O MockMvc propaga a exceção original do mock, então a verificamos diretamente.
        assertThrows(UsernameNotFoundException.class, () -> {
            mockMvc.perform(get("/api/v1/ativos").header("Authorization", "Bearer " + token));
        });
    }

    @Test
    @DisplayName("Filtro: Deve permitir acesso com token válido para usuário com a role correta")
    void doFilterInternal_comTokenValido_devePermitirAcesso() throws Exception {
        String token = "valid-token";
        String email = "admin@aegis.com";
        UserDetails userDetails = createCustomUserDetails(email, "ROLE_ADMIN");

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        mockMvc.perform(get("/api/v1/ativos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
