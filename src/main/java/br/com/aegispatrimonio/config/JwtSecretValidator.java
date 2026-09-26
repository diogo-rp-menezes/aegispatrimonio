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
    }
}

