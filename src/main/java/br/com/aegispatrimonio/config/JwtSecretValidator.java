package br.com.aegispatrimonio.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator {

    private final Environment env;
    private final String jwtSecret;

    public JwtSecretValidator(Environment env, @Value("${jwt.secret:}") String jwtSecret) {
        this.env = env;
        this.jwtSecret = jwtSecret;
    }

    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret is required. Set the JWT_SECRET environment variable or jwt.secret property. The application refuses to start without it (fail-fast).");
        }
        // L2 (audit): além de não-vazio, o secret decodificado (Base64) deve ter
        // >= 256 bits (32 bytes) — mínimo para HMAC-SHA256. Fail-fast com
        // mensagem clara.
        byte[] decoded;
        try {
            decoded = java.util.Base64.getDecoder().decode(jwtSecret.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "JWT secret must be a valid Base64 string. The application refuses to start without it (fail-fast).");
        }
        if (decoded.length < 32) {
            throw new IllegalStateException(String.format(
                "JWT secret too weak: decoded length is %d bytes; at least 32 bytes (256 bits) are required for HMAC-SHA256. "
                    + "Generate one with: openssl rand -base64 32 — and set it via the JWT_SECRET environment variable.",
                decoded.length));
        }
    }
}

