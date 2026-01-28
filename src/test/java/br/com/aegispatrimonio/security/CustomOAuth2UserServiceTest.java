package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.security.oauth2.CustomOAuth2UserService;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private TestableCustomOAuth2UserService userService;

    @BeforeEach
    void setUp() {
        userService = new TestableCustomOAuth2UserService(usuarioRepository);
    }

    @Test
    @DisplayName("loadUser: Deve criar novo usuário quando não existe")
    void loadUser_shouldCreateNewUser_whenUserDoesNotExist() {
        // Arrange
        String email = "newuser@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("sub", "123456");

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptySet(), attributes, "sub");

        userService.setMockUser(oauth2User);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        OAuth2User result = userService.loadUser(mock(OAuth2UserRequest.class));

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("loadUser: Deve atualizar (ou retornar) usuário existente")
    void loadUser_shouldReturnExistingUser_whenUserExists() {
        // Arrange
        String email = "existing@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("sub", "123456");

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptySet(), attributes, "sub");

        userService.setMockUser(oauth2User);

        Usuario existingUser = new Usuario();
        existingUser.setId(10L);
        existingUser.setEmail(email);
        existingUser.setStatus(Status.ATIVO);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(existingUser);

        // Act
        OAuth2User result = userService.loadUser(mock(OAuth2UserRequest.class));

        // Assert
        assertNotNull(result);
        verify(usuarioRepository).save(existingUser);
    }

    @Test
    @DisplayName("loadUser: Deve lançar exceção se email não vier do provider")
    void loadUser_shouldThrowException_whenEmailIsMissing() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456");
        // No email

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptySet(), attributes, "sub");

        userService.setMockUser(oauth2User);

        // Act & Assert
        // The wrapper rethrows exceptions, but our Testable class needs to handle the logic
        // processOAuth2User throws OAuth2AuthenticationException
        // And CustomOAuth2UserService.loadUser catches generic Exception and wraps in InternalAuthenticationServiceException

        // Since we are bypassing loadUser in the test subclass to call processOAuth2User,
        // we need to see how the test subclass calls it.

        assertThrows(InternalAuthenticationServiceException.class, () -> {
            userService.loadUser(mock(OAuth2UserRequest.class));
        });
    }

    // Helper class
    static class TestableCustomOAuth2UserService extends CustomOAuth2UserService {
        private OAuth2User mockUser;

        public TestableCustomOAuth2UserService(UsuarioRepository usuarioRepository) {
            super(usuarioRepository);
        }

        public void setMockUser(OAuth2User mockUser) {
            this.mockUser = mockUser;
        }

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {
            // Bypass super.loadUser() which makes HTTP call
            try {
                // Call the protected method we want to test
                return processOAuth2User(userRequest, mockUser);
            } catch (Exception e) {
                 // Simulate the wrapping done in the real class
                 throw new InternalAuthenticationServiceException(e.getMessage(), e);
            }
        }
    }
}
