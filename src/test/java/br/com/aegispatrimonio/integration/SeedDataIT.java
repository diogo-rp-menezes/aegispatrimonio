package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.AegispatrimonioApplication;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest(classes = AegispatrimonioApplication.class)
@ActiveProfiles("dev")
@Transactional
public class SeedDataIT {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void testSeedDataPresent() {
        Optional<Usuario> admin = usuarioRepository.findByEmail("admin@aegis.com");
        Assertions.assertTrue(admin.isPresent(), "Admin user should be seeded by Flyway V4 in dev profile");

        // Verify Legacy Role
        Assertions.assertEquals("ADMIN", admin.get().getRole(), "User legacy role should be ADMIN");

        // Verify RBAC Role (seeded by V6)
        boolean hasAdminRole = admin.get().getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        Assertions.assertTrue(hasAdminRole, "User should have ROLE_ADMIN assigned via RBAC (V6)");
    }
}
