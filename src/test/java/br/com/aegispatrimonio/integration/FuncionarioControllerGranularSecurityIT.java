package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class FuncionarioControllerGranularSecurityIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private JwtService jwtService;
    @Autowired private EntityManager entityManager;
    @Autowired private CacheManager cacheManager;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ObjectMapper objectMapper;

    private String tokenUserFilial1;
    private String tokenUserFilial2;
    private String tokenAdmin;
    private Long filial1Id;
    private Long filial2Id;
    private Long deptId;
    private Long funcionarioFilial1Id;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).execute(status -> {
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(name -> {
                    org.springframework.cache.Cache cache = cacheManager.getCache(name);
                    if (cache != null) cache.clear();
                });
            }

            entityManager.createNativeQuery("DELETE FROM rbac_user_role").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM rbac_role_permission").executeUpdate();

            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            roleRepository.deleteAll();
            permissionRepository.deleteAll();
            entityManager.flush();

            // Permissions
            Permission pRead = createPermission("FUNCIONARIO", "READ", "filialId");
            Permission pCreate = createPermission("FUNCIONARIO", "CREATE", "filialId");
            Permission pUpdate = createPermission("FUNCIONARIO", "UPDATE", "filialId");
            Permission pDelete = createPermission("FUNCIONARIO", "DELETE", "filialId");

            // Roles
            Role roleUser = createRole("ROLE_USER", Set.of(pRead, pCreate, pUpdate, pDelete));
            Role roleAdmin = createRole("ROLE_ADMIN", Collections.emptySet()); // Admin has bypass

            // Filiais
            Filial f1 = createFilial("Filial 1", "F01", "11.111.111/0001-11");
            Filial f2 = createFilial("Filial 2", "F02", "22.222.222/0001-22");
            filial1Id = f1.getId();
            filial2Id = f2.getId();

            // Dept
            Departamento dept = createDepartamento("RH", f1);
            deptId = dept.getId();

            // Users
            tokenUserFilial1 = createUserAndToken("user1@aegis.com", roleUser, dept, Set.of(f1));
            tokenUserFilial2 = createUserAndToken("user2@aegis.com", roleUser, dept, Set.of(f2)); // Linked to f2 only
            tokenAdmin = createUserAndToken("admin@aegis.com", roleAdmin, dept, Set.of(f1, f2));

            // Target Funcionario in Filial 1
            Funcionario target = new Funcionario();
            target.setNome("Target Funcionario");
            target.setMatricula("MAT-TARGET");
            target.setCargo("Analista");
            target.setDepartamento(dept);
            target.setFiliais(Set.of(f1));
            target.setStatus(Status.ATIVO);
            target = funcionarioRepository.save(target);

            // Create user for target (optional but good practice)
            Usuario uTarget = new Usuario();
            uTarget.setEmail("target@aegis.com");
            uTarget.setPassword("password");
            uTarget.setRole("ROLE_USER");
            uTarget.setFuncionario(target);
            usuarioRepository.save(uTarget);

            funcionarioFilial1Id = target.getId();

            entityManager.flush();
            return null;
        });
    }

    private Permission createPermission(String resource, String action, String contextKey) {
        Permission p = new Permission();
        p.setResource(resource);
        p.setAction(action);
        p.setContextKey(contextKey);
        return permissionRepository.save(p);
    }

    private Role createRole(String name, Set<Permission> permissions) {
        Role r = new Role();
        r.setName(name);
        r.setPermissions(permissions);
        return roleRepository.save(r);
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial f = new Filial();
        f.setNome(nome);
        f.setCodigo(codigo);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return filialRepository.save(f);
    }

    private Departamento createDepartamento(String nome, Filial filial) {
        Departamento d = new Departamento();
        d.setNome(nome);
        d.setFilial(filial);
        d.setStatus(Status.ATIVO);
        return departamentoRepository.save(d);
    }

    private String createUserAndToken(String email, Role role, Departamento dept, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome("Func " + email);
        func.setDepartamento(dept);
        func.setFiliais(filiais);
        func.setMatricula("MAT-" + email.hashCode());
        func.setCargo("Tester");
        func.setStatus(Status.ATIVO);
        funcionarioRepository.save(func);

        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword("password");
        u.setRole(role.getName());
        u.setRoles(Set.of(role));
        u.setFuncionario(func);
        usuarioRepository.save(u);

        CustomUserDetails userDetails = new CustomUserDetails(u);
        return "Bearer " + jwtService.generateToken(userDetails);
    }

    @Test
    void shouldAllowRead_WhenUserHasAccessToFilial() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyRead_WhenUserHasNoAccessToFilial() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowCreate_WhenUserHasAccessToTargetFilial() throws Exception {
        FuncionarioCreateDTO dto = new FuncionarioCreateDTO(
            "New Func", "MAT-NEW", "Dev", deptId, Set.of(filial1Id), "new@aegis.com", "password", "ROLE_USER"
        );

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyCreate_WhenUserHasNoAccessToTargetFilial() throws Exception {
        FuncionarioCreateDTO dto = new FuncionarioCreateDTO(
            "New Func Deny", "MAT-DENY", "Dev", deptId, Set.of(filial1Id), "deny@aegis.com", "password", "ROLE_USER"
        );

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowUpdate_WhenUserHasAccessToTargetFilial() throws Exception {
        FuncionarioUpdateDTO dto = new FuncionarioUpdateDTO(
            "Updated Func", "MAT-UPD", "Dev Senior", deptId, Status.ATIVO, Set.of(filial1Id), "target@aegis.com", null, "ROLE_USER"
        );

        mockMvc.perform(put("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUpdate_WhenUserHasNoAccessToTargetFilial() throws Exception {
        FuncionarioUpdateDTO dto = new FuncionarioUpdateDTO(
            "Updated Func", "MAT-UPD", "Dev Senior", deptId, Status.ATIVO, Set.of(filial1Id), "target@aegis.com", null, "ROLE_USER"
        );

        mockMvc.perform(put("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUpdate_WhenUserHasAccessToTargetButNotDestinationFilial() throws Exception {
        // User 1 tries to move user to Filial 2 (which they don't have access to)
        FuncionarioUpdateDTO dto = new FuncionarioUpdateDTO(
            "Updated Func", "MAT-UPD", "Dev Senior", deptId, Status.ATIVO, Set.of(filial2Id), "target@aegis.com", null, "ROLE_USER"
        );

        mockMvc.perform(put("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDelete_WhenUserHasAccessToFilial() throws Exception {
        mockMvc.perform(delete("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyDelete_WhenUserHasNoAccessToFilial() throws Exception {
        mockMvc.perform(delete("/api/v1/funcionarios/{id}", funcionarioFilial1Id)
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListOnlyAllowedFuncionarios() throws Exception {
        // Create another employee in Filial 2 to check visibility
        new TransactionTemplate(transactionManager).execute(status -> {
            Filial f2 = filialRepository.findById(filial2Id).orElseThrow();
            Departamento dept = departamentoRepository.findById(deptId).orElseThrow();

            Funcionario f2Emp = new Funcionario();
            f2Emp.setNome("Func Filial 2");
            f2Emp.setMatricula("MAT-F2");
            f2Emp.setCargo("Analista");
            f2Emp.setDepartamento(dept);
            f2Emp.setFiliais(Set.of(f2));
            f2Emp.setStatus(Status.ATIVO);
            funcionarioRepository.save(f2Emp);

            entityManager.flush();
            return null;
        });

        // Verify User 1 (Filial 1) - Should see Target (F1) but NOT F2 employee
        mockMvc.perform(get("/api/v1/funcionarios")
                        .header("Authorization", tokenUserFilial1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty())
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Target Funcionario")))
                .andExpect(jsonPath("$.content[*].nome").value(not(hasItem("Func Filial 2"))));

        // Verify User 2 (Filial 2) - Should see F2 Employee but NOT Target (F1)
        mockMvc.perform(get("/api/v1/funcionarios")
                        .header("Authorization", tokenUserFilial2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty())
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Func Filial 2")))
                .andExpect(jsonPath("$.content[*].nome").value(not(hasItem("Target Funcionario"))));

        // Verify Admin - Should see everyone
        mockMvc.perform(get("/api/v1/funcionarios")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty())
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Target Funcionario")))
                .andExpect(jsonPath("$.content[*].nome").value(hasItem("Func Filial 2")));
    }
}
