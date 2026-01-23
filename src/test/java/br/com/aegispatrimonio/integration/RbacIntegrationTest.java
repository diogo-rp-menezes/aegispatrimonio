package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Permission;
import br.com.aegispatrimonio.model.Role;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.PermissionRepository;
import br.com.aegispatrimonio.repository.RoleRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import br.com.aegispatrimonio.service.IPermissionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(RbacTestController.class)
public class RbacIntegrationTest extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private IPermissionService permissionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String validToken;
    private Usuario testUser;
    private Long filialId;
    private Long otherFilialId;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).execute(status -> {
            // Clear Caches
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(name -> {
                    org.springframework.cache.Cache cache = cacheManager.getCache(name);
                    if (cache != null) cache.clear();
                });
            }

            // Clear DB - Order matters
            // Manually clear join tables first to avoid FK issues
            entityManager.createNativeQuery("DELETE FROM rbac_user_role").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM rbac_role_permission").executeUpdate();

            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            roleRepository.deleteAll();
            permissionRepository.deleteAll();
            entityManager.flush();

        // Create Permission
        Permission p = new Permission();
        p.setResource("ATIVO");
        p.setAction("READ");
        p.setContextKey("filialId");
        Permission savedP = permissionRepository.save(p);

        // Create Role
        Role r = new Role();
        r.setName("ROLE_TESTER");
        r.setPermissions(new HashSet<>(Collections.singletonList(savedP)));
        Role savedR = roleRepository.save(r);

        // Create Filial 1 (Access Granted)
        Filial filial = new Filial();
        filial.setNome("Filial Teste");
        filial.setCodigo("F01");
        filial.setCnpj("11.111.111/0001-11");
        filial.setStatus(Status.ATIVO);
        Filial savedFilial = filialRepository.save(filial);
        this.filialId = savedFilial.getId();

        // Create Filial 2 (Access Denied)
        Filial filial2 = new Filial();
        filial2.setNome("Filial Outra");
        filial2.setCodigo("F02");
        filial2.setCnpj("22.222.222/0001-22");
        filial2.setStatus(Status.ATIVO);
        Filial savedFilial2 = filialRepository.save(filial2);
        this.otherFilialId = savedFilial2.getId();

        // Create Departamento
        Departamento dept = new Departamento();
        dept.setNome("TI");
        dept.setFilial(savedFilial);
        dept.setStatus(Status.ATIVO);
        Departamento savedDept = departamentoRepository.save(dept);

        // Create Funcionario with Access to Filial 1
        Funcionario func = new Funcionario();
        func.setNome("Tester");
        func.setCargo("Tester");
        func.setMatricula("12345");
        func.setStatus(Status.ATIVO);
        func.setDepartamento(savedDept);
        func.setFiliais(new HashSet<>(Collections.singletonList(savedFilial)));
        Funcionario savedFunc = funcionarioRepository.save(func);

        // Create Usuario
        testUser = new Usuario();
        testUser.setEmail("tester@aegis.com");
        testUser.setPassword("password");
        testUser.setRole("TESTER"); // Legacy
        testUser.setRoles(new HashSet<>(Collections.singletonList(savedR)));
        testUser.setFuncionario(savedFunc);
        testUser = usuarioRepository.save(testUser);

        // Generate Token
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        validToken = "Bearer " + jwtService.generateToken(userDetails);

        entityManager.flush();
        entityManager.clear();
        return null;
    });

        // Ensure data is committed
    }

    @Test
    void shouldAllowAccess_WhenPermissionAndContextMatch() throws Exception {
        mockMvc.perform(get("/api/v1/test/rbac/read")
                .param("id", "1")
                .param("filialId", String.valueOf(filialId))
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Allowed"));
    }

    @Test
    void shouldAllowAccess_DirectServiceCall() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        boolean result = permissionService.hasPermission(auth, 1L, "ATIVO", "READ", filialId);
        assertTrue(result, "Direct service call should allow access");
    }

    @Test
    void shouldDenyAccess_WhenContextDoesNotMatch() throws Exception {
        mockMvc.perform(get("/api/v1/test/rbac/read")
                .param("id", "1")
                .param("filialId", String.valueOf(otherFilialId))
                .header("Authorization", validToken))
                .andExpect(status().isForbidden());
    }
}
