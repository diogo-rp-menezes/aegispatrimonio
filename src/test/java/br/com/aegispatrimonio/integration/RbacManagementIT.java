package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.PermissionCreateDTO;
import br.com.aegispatrimonio.dto.RoleCreateDTO;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.PermissionRepository;
import br.com.aegispatrimonio.repository.RoleRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class RbacManagementIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private Long createdPermissionId;

    @BeforeEach
    void setup() {
        // Create Admin Role
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setDescription("Admin Role");
        roleRepository.save(adminRole);

        // Create User Role
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole.setDescription("User Role");
        roleRepository.save(userRole);

        // Create Admin User
        Usuario admin = new Usuario();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN"); // Legacy
        admin.setRoles(Collections.singleton(adminRole)); // Granular
        admin.setStatus(Status.ATIVO);
        usuarioRepository.save(admin);
        adminToken = jwtService.generateToken(new br.com.aegispatrimonio.security.CustomUserDetails(admin));

        // Create Normal User
        Usuario user = new Usuario();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("ROLE_USER"); // Legacy
        user.setRoles(Collections.singleton(userRole)); // Granular
        user.setStatus(Status.ATIVO);
        usuarioRepository.save(user);
        userToken = jwtService.generateToken(new br.com.aegispatrimonio.security.CustomUserDetails(user));

        // Create Initial Permission
        Permission p = new Permission();
        p.setResource("TEST");
        p.setAction("READ");
        p.setDescription("Read Test");
        createdPermissionId = permissionRepository.save(p).getId();
    }

    @Test
    void shouldCreateRoleAsAdmin() throws Exception {
        RoleCreateDTO dto = new RoleCreateDTO("ROLE_NEW", "New Role", Set.of(createdPermissionId));

        mockMvc.perform(post("/api/v1/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ROLE_NEW"))
                .andExpect(jsonPath("$.permissions[0].id").value(createdPermissionId));
    }

    @Test
    void shouldNotCreateRoleAsUser() throws Exception {
        RoleCreateDTO dto = new RoleCreateDTO("ROLE_NEW_2", "New Role 2", Set.of(createdPermissionId));

        mockMvc.perform(post("/api/v1/roles")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreatePermissionAsAdmin() throws Exception {
        PermissionCreateDTO dto = new PermissionCreateDTO("NEW_RES", "WRITE", "Desc", "ctx");

        mockMvc.perform(post("/api/v1/permissions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resource").value("NEW_RES"))
                .andExpect(jsonPath("$.action").value("WRITE"));
    }
}
