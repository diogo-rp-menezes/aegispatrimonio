package br.com.aegispatrimonio.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    // Segredo de teste em Base64. Deve ter pelo menos 256 bits.
    private final String testSecret = "c2VjcmV0b3NlY3JldG9zZWNyZXRvc2VjcmV0b3NlY3JldG9zZWNyZXRvMTIzNDU2Nzg=";
    private final Long oneHour = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injeta os valores que seriam preenchidos pelo @Value do Spring
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", oneHour);
    }

    private UserDetails createTestUser(String username) {
        return new User(username, "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Deve gerar um token e extrair o username corretamente")
    void generateToken_deveExtrairUsernameCorreto() {
        // Arrange
        UserDetails userDetails = createTestUser("testuser@aegis.com");

        // Act
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertNotNull(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    @DisplayName("Deve validar um token válido com sucesso")
    void isTokenValid_deveRetornarTrueParaTokenValido() {
        // Arrange
        UserDetails userDetails = createTestUser("testuser@aegis.com");
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve invalidar um token para um usuário diferente")
    void isTokenValid_deveRetornarFalseParaUsuarioDiferente() {
        // Arrange
        UserDetails originalUser = createTestUser("original@aegis.com");
        UserDetails otherUser = createTestUser("outro@aegis.com");
        String token = jwtService.generateToken(originalUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve invalidar um token expirado")
    void isTokenValid_deveRetornarFalseParaTokenExpirado() throws InterruptedException {
        // Arrange
        // Injeta um tempo de expiração muito curto para o teste
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        UserDetails userDetails = createTestUser("testuser@aegis.com");
        String token = jwtService.generateToken(userDetails);

        // Act
        // Espera o token expirar
        Thread.sleep(5);

        // Assert
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Deve lançar exceção para token malformado")
    void isTokenValid_deveLancarExcecaoParaTokenMalformado() {
        // Arrange
        String malformedToken = "um.token.invalido";
        UserDetails userDetails = createTestUser("testuser@aegis.com");

        // Assert
        assertThrows(Exception.class, () -> jwtService.isTokenValid(malformedToken, userDetails));
    }
}
