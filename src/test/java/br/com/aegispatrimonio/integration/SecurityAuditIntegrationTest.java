package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SecurityAuditIntegrationTest extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private SecurityAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    void shouldLogAllowedAccess() throws Exception {
        // Setup User and Permission
        Permission p = new Permission();
        p.setResource("ATIVO");
        p.setAction("CREATE");
        p = permissionRepository.save(p);

        Role role = new Role();
        role.setName("ROLE_TEST_AUDIT");
        role.setPermissions(Set.of(p));
        role = roleRepository.save(role);

        Usuario user = new Usuario();
        user.setEmail("audit_allow@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.of(role));
        user.setRole(role.getName());
        user.setStatus(Status.ATIVO);
        usuarioRepository.save(user);

        String token = jwtService.generateToken(new CustomUserDetails(user));

        AtivoCreateDTO dto = new AtivoCreateDTO(
            1L, "Laptop Test", 1L, "PAT-123", null,
            LocalDate.now(), 1L, new BigDecimal("1000.00"), null,
            "Obs", "Warranty", null, null
        );

        // Act
        // Expect 404 because Filial/TipoAtivo don't exist, but permission check happens before
        mockMvc.perform(post("/api/v1/ativos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        // Assert
        Thread.sleep(1500); // Async wait

        List<SecurityAuditLog> logs = auditLogRepository.findAll();
        boolean found = logs.stream().anyMatch(l ->
            l.getUsername().equals("audit_allow@test.com") &&
            l.getResource().equals("ATIVO") &&
            l.getAction().equals("CREATE") &&
            l.getOutcome().equals("ALLOW")
        );
        assertTrue(found, "Should have found an ALLOW log entry. Logs found: " + logs);
    }

    @Test
    void shouldLogDeniedAccess() throws Exception {
        // Setup User with NO Permission
        Role role = new Role();
        role.setName("ROLE_TEST_DENY_AUDIT");
        role = roleRepository.save(role);

        Usuario user = new Usuario();
        user.setEmail("audit_deny@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.of(role));
        user.setRole(role.getName());
        user.setStatus(Status.ATIVO);
        usuarioRepository.save(user);

        String token = jwtService.generateToken(new CustomUserDetails(user));

        AtivoCreateDTO dto = new AtivoCreateDTO(
            1L, "Laptop Deny", 1L, "PAT-999", null,
            LocalDate.now(), 1L, new BigDecimal("1000.00"), null,
            "Obs", "Warranty", null, null
        );

        // Act
        mockMvc.perform(post("/api/v1/ativos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        // Assert
        Thread.sleep(1500);

        List<SecurityAuditLog> logs = auditLogRepository.findAll();
        boolean found = logs.stream().anyMatch(l ->
            l.getUsername().equals("audit_deny@test.com") &&
            l.getResource().equals("ATIVO") &&
            l.getAction().equals("CREATE") &&
            l.getOutcome().equals("DENY")
        );
        assertTrue(found, "Should have found a DENY log entry. Logs found: " + logs);
    }
}
