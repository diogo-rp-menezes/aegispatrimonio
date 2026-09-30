package br.com.aegispatrimonio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final TokenDenylistService tokenDenylistService;

    public JwtService(TokenDenylistService tokenDenylistService) {
        this.tokenDenylistService = tokenDenylistService;
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrai o claim jti (id único do token), usado como chave da denylist. */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /** Extrai o claim uid (id do usuário dono do token). */
    public Long extractUid(String token) {
        Object uid = extractClaim(token, claims -> claims.get("uid"));
        return uid instanceof Number n ? n.longValue() : null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Long uid = resolveUsuarioId(userDetails);
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti: chave da denylist
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration));
        if (uid != null) {
            builder.claim("uid", uid);
        }
        return builder
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Denylist indexada por jti (C2): nunca pela string completa do token
        if (tokenDenylistService.isRevoked(extractJti(token))) {
            return false;
        }
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Revoga todos os tokens de um usuário (por uid) até o horizonte de
     * expiração padrão. Usado, por exemplo, em troca de senha/roles.
     */
    public void revokeAllTokensForUser(Long usuarioId) {
        tokenDenylistService.revokeAllForUser(usuarioId,
                System.currentTimeMillis() + jwtExpiration);
    }

    /**
     * Extrai o timestamp de expiração (epoch millis) do token, para uso na
     * denylist do logout.
     */
    public long extractExpirationEpochMillis(String token) {
        return extractExpiration(token).getTime();
    }

    private Long resolveUsuarioId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails custom
                && custom.getUsuario() != null) {
            return custom.getUsuario().getId();
        }
        return null;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
