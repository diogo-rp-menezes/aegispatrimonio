package br.com.aegispatrimonio;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordCheckTest {
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2b$12$gdg2CRcFwnztxE3UV1hw8OA8Pre3eSIrCKgK7q.xumHPKC3AKLNGi";
        boolean match = encoder.matches("123456", hash);
        System.out.println("MATCH RESULT: " + match);
        assertTrue(match, "Password 123456 should match the hash");
    }
}
