package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class HealthCheckSystemControllerIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private HealthCheckHistoryRepository healthCheckHistoryRepository;
    @Autowired private JwtService jwtService;
    @Autowired private EntityManager entityManager;
    @Autowired private CacheManager cacheManager;
    @Autowired private PlatformTransactionManager transactionManager;

    private String tokenAdmin;
    private String tokenUser;

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

            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            roleRepository.deleteAll();
            healthCheckHistoryRepository.deleteAll();
            entityManager.flush();

            // Roles
            Role roleUser = createRole("ROLE_USER");
            Role roleAdmin = createRole("ROLE_ADMIN");

            // Filial & Dept
            Filial f1 = createFilial("Filial HQ", "HQ", "11.111.111/0001-11");
            Departamento dept = createDepartamento("IT", f1);

            // Users
            tokenUser = createUserAndToken("user@aegis.com", roleUser, dept, Set.of(f1));
            tokenAdmin = createUserAndToken("admin@aegis.com", roleAdmin, dept, Set.of(f1));

            // Data
            createHealthHistory("host1", new BigDecimal("0.5"), new BigDecimal("0.5")); // Normal
            createHealthHistory("host1", new BigDecimal("0.95"), new BigDecimal("0.05")); // Critical

            return null;
        });
    }

    private Role createRole(String name) {
        Role r = new Role();
        r.setName(name);
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

    private void createHealthHistory(String host, BigDecimal cpu, BigDecimal memFree) {
        HealthCheckHistory h = new HealthCheckHistory();
        h.setHost(host);
        h.setCpuUsage(cpu);
        h.setMemFreePercent(memFree);
        h.setDisks("[]");
        h.setNets("[]");
        healthCheckHistoryRepository.save(h);
    }

    @Test
    void shouldGetLast_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/health-check/system/last")
                .header("Authorization", tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyGetLast_WhenUser() throws Exception {
        mockMvc.perform(get("/api/v1/health-check/system/last")
                .header("Authorization", tokenUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetHistory_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/health-check/system/history")
                .header("Authorization", tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAlerts_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/health-check/system/alerts")
                .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print());
    }
}
