package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.RevokedToken;
import br.com.aegispatrimonio.repository.RevokedTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private TokenDenylistService denylist;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    // Segredo de teste em Base64. Deve ter pelo menos 256 bits.
    private final String testSecret = "c2VjcmV0b3NlY3JldG9zZWNyZXRvc2VjcmV0b3NlY3JldG9zZWNyZXRvMTIzNDU2Nzg=";
    private final Long oneHour = 3600000L;

    @BeforeEach
    void setUp() {
        denylist = new TokenDenylistService(revokedTokenRepository);
        jwtService = new JwtService(denylist);
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
    @DisplayName("C2: token gerado deve conter jti único e extraível")
    void generateToken_deveConterJtiUnico() {
        UserDetails userDetails = createTestUser("testuser@aegis.com");

        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(userDetails);

        String jti1 = jwtService.extractJti(token1);
        String jti2 = jwtService.extractJti(token2);

        assertNotNull(jti1);
        assertNotNull(jti2);
        assertNotEquals(jti1, jti2, "Cada token deve ter um jti único");
    }

    @Test
    @DisplayName("C2: token gerado para CustomUserDetails deve conter o uid do usuário")
    void generateToken_deveConterUid() {
        br.com.aegispatrimonio.model.Usuario usuario = new br.com.aegispatrimonio.model.Usuario();
        usuario.setId(77L);
        usuario.setEmail("uiduser@aegis.com");
        usuario.setPassword("password");
        usuario.setStatus(br.com.aegispatrimonio.model.Status.ATIVO);
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        String token = jwtService.generateToken(userDetails);

        assertEquals(77L, jwtService.extractUid(token));
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

    @Test
    @DisplayName("Logout: token revogado (por jti) deve ser rejeitado em isTokenValid")
    void isTokenValid_deveRetornarFalseParaTokenRevogado() {
        // Arrange
        UserDetails userDetails = createTestUser("testuser@aegis.com");
        String token = jwtService.generateToken(userDetails);
        String jti = jwtService.extractJti(token);

        when(revokedTokenRepository.findById(jti))
                .thenReturn(Optional.of(new RevokedToken(jti, null, jwtService.extractExpirationEpochMillis(token))));

        // Act + Assert
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Logout: token válido (não revogado) não deve ser afetado pela denylist")
    void isTokenValid_tokenNaoRevogado_deveContinuarValido() {
        // Arrange
        UserDetails userDetails = createTestUser("testuser@aegis.com");
        String token = jwtService.generateToken(userDetails);
        String jti = jwtService.extractJti(token);

        // Outro token revogado; o atual não
        String outroToken = jwtService.generateToken(createTestUser("outro@aegis.com"));
        String outroJti = jwtService.extractJti(outroToken);
        denylist.revoke(outroJti, jwtService.extractExpirationEpochMillis(outroToken), null);

        when(revokedTokenRepository.findById(jti)).thenReturn(Optional.empty());

        // Act + Assert
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("C2: revokeAllTokensForUser estende as revogações ativas do usuário")
    void revokeAllTokensForUser_deveRepasarParaDenylist() {
        // Arrange
        when(revokedTokenRepository.findByUsuarioId(42L)).thenReturn(java.util.List.of(
                new RevokedToken("jti-1", 42L, System.currentTimeMillis() + 1_000)));

        // Act
        jwtService.revokeAllTokensForUser(42L);

        // Assert
        verify(revokedTokenRepository).findByUsuarioId(42L);
        verify(revokedTokenRepository).save(any(RevokedToken.class));
    }
}
