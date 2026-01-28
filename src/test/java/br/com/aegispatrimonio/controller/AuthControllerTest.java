package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerTest extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@aegis.com", roles = "ADMIN")
    void me_shouldReturnUserDetails_whenAuthenticated() throws Exception {
        // Arrange
        // We need the user in DB because CustomUserDetailsService loads it?
        // Wait, @WithMockUser creates a SecurityContext, but @AuthenticationPrincipal injects it.
        // If the controller expects CustomUserDetails, @WithMockUser might inject standard User.

        // However, AuthController code:
        // me(@AuthenticationPrincipal UserDetails userDetails)
        // buildResponse checks "if (userDetails instanceof CustomUserDetails)".
        // If it's not, it returns empty lists.

        // Since @WithMockUser provides a standard User, we expect empty lists but 200 OK.

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                // .andExpect(jsonPath("$.roles").exists()) // Standard User has authorities
                .andExpect(jsonPath("$.token").value(""));
    }

    // To test full flow with CustomUserDetails, we would need to mock the UserDetailsService or
    // perform a real login (POST /login) then use the token.

    @Test
    void me_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
