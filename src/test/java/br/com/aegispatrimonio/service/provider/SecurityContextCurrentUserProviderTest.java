package br.com.aegispatrimonio.service.provider;

import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextCurrentUserProviderTest {

    @InjectMocks
    private SecurityContextCurrentUserProvider currentUserProvider;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private Usuario expectedUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        // Mock the static SecurityContextHolder
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);

        // Configure SecurityContextHolder to return our mocked securityContext
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        // Configure securityContext to return our mocked authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Create a dummy user and CustomUserDetails
        expectedUser = new Usuario();
        expectedUser.setId(1L);
        expectedUser.setEmail("test@example.com");
        expectedUser.setRole("ROLE_USER");
        customUserDetails = new CustomUserDetails(expectedUser);

        // Configure authentication to return our CustomUserDetails as the principal
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
    }

    @AfterEach
    void tearDown() {
        // Close the mocked static to avoid interference with other tests
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("Should return the current authenticated user from SecurityContextHolder")
    void getCurrentUsuario_shouldReturnAuthenticatedUser() {
        // When
        Usuario actualUser = currentUserProvider.getCurrentUsuario();

        // Then
        assertEquals(expectedUser, actualUser);
    }
}
