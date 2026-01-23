package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "spring.jackson.serialization.write-dates-as-timestamps=false")
public class AtivoControllerGranularSecurityIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
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
    private Long ativoFilial1Id;
    private Long tipoAtivoId;
    private Long fornecedorId;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        new TransactionTemplate(transactionManager).execute(status -> {
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(name -> {
                    org.springframework.cache.Cache cache = cacheManager.getCache(name);
                    if (cache != null) cache.clear();
                });
            }

            entityManager.createNativeQuery("DELETE FROM rbac_user_role").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM rbac_role_permission").executeUpdate();

            ativoRepository.deleteAll();
            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            roleRepository.deleteAll();
            permissionRepository.deleteAll();
            tipoAtivoRepository.deleteAll();
            fornecedorRepository.deleteAll();
            entityManager.flush();

            // Permissions
            Permission pRead = createPermission("ATIVO", "READ", "filialId");
            Permission pCreate = createPermission("ATIVO", "CREATE", "filialId");
            Permission pUpdate = createPermission("ATIVO", "UPDATE", "filialId");
            Permission pDelete = createPermission("ATIVO", "DELETE", "filialId");

            // Roles
            Role roleUser = createRole("ROLE_USER", Set.of(pRead, pCreate, pUpdate, pDelete));
            Role roleAdmin = createRole("ROLE_ADMIN", Collections.emptySet()); // Admin has bypass

            // Filiais
            Filial f1 = createFilial("Filial 1", "F01", "11.111.111/0001-11");
            Filial f2 = createFilial("Filial 2", "F02", "22.222.222/0001-22");
            filial1Id = f1.getId();
            filial2Id = f2.getId();

            // Dept
            Departamento dept = createDepartamento("TI", f1);

            // Users
            tokenUserFilial1 = createUserAndToken("user1@aegis.com", roleUser, dept, Set.of(f1));
            tokenUserFilial2 = createUserAndToken("user2@aegis.com", roleUser, dept, Set.of(f2)); // Linked to f2 only
            tokenAdmin = createUserAndToken("admin@aegis.com", roleAdmin, dept, Set.of(f1, f2));

            // Dependencies for Ativo
            TipoAtivo tipo = new TipoAtivo();
            tipo.setNome("Notebook");
            tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
            tipo = tipoAtivoRepository.save(tipo);
            tipoAtivoId = tipo.getId();

            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome("Dell");
            fornecedor.setCnpj("00.000.000/0001-00");
            fornecedor = fornecedorRepository.save(fornecedor);
            fornecedorId = fornecedor.getId();

            // Ativo in Filial 1
            Ativo ativo = new Ativo();
            ativo.setNome("Laptop User 1");
            ativo.setNumeroPatrimonio("PAT-001");
            ativo.setFilial(f1);
            ativo.setTipoAtivo(tipo);
            ativo.setFornecedor(fornecedor);
            ativo.setValorAquisicao(BigDecimal.valueOf(5000));
            ativo.setDataAquisicao(LocalDate.now());
            ativo.setStatus(StatusAtivo.ATIVO);
            ativo = ativoRepository.save(ativo);
            ativoFilial1Id = ativo.getId();

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
        mockMvc.perform(get("/api/v1/ativos/{id}", ativoFilial1Id)
                .header("Authorization", tokenUserFilial1))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldListAll_WithAdminToken() throws Exception {
        mockMvc.perform(get("/api/v1/ativos")
                .header("Authorization", tokenAdmin))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyRead_WhenUserHasNoAccessToFilial() throws Exception {
        mockMvc.perform(get("/api/v1/ativos/{id}", ativoFilial1Id)
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowCreate_WhenUserHasAccessToTargetFilial() throws Exception {
        AtivoCreateDTO dto = new AtivoCreateDTO(
                filial1Id, "New Asset", tipoAtivoId, "PAT-NEW", null,
                LocalDate.now(), fornecedorId, BigDecimal.valueOf(1000), null, "Obs", "Warranty", null, null
        );

        mockMvc.perform(post("/api/v1/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyCreate_WhenUserHasNoAccessToTargetFilial() throws Exception {
        AtivoCreateDTO dto = new AtivoCreateDTO(
                filial1Id, "New Asset", tipoAtivoId, "PAT-NEW-2", null,
                LocalDate.now(), fornecedorId, BigDecimal.valueOf(1000), null, "Obs", "Warranty", null, null
        );

        mockMvc.perform(post("/api/v1/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial2)) // User 2 only has access to Filial 2
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowUpdate_WhenUserHasAccessToTargetFilial() throws Exception {
        AtivoUpdateDTO dto = new AtivoUpdateDTO(
                filial1Id, "Updated Name", "PAT-001", tipoAtivoId, null, StatusAtivo.ATIVO,
                LocalDate.now(), fornecedorId, BigDecimal.valueOf(6000), null, "Obs", "Warranty", null, null
        );

        mockMvc.perform(put("/api/v1/ativos/{id}", ativoFilial1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUpdate_WhenUserHasNoAccessToTargetFilial() throws Exception {
        AtivoUpdateDTO dto = new AtivoUpdateDTO(
                filial1Id, "Updated Name", "PAT-001", tipoAtivoId, null, StatusAtivo.ATIVO,
                LocalDate.now(), fornecedorId, BigDecimal.valueOf(6000), null, "Obs", "Warranty", null, null
        );

        mockMvc.perform(put("/api/v1/ativos/{id}", ativoFilial1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowDelete_WhenUserHasAccessToFilial() throws Exception {
        mockMvc.perform(delete("/api/v1/ativos/{id}", ativoFilial1Id)
                .header("Authorization", tokenUserFilial1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyDelete_WhenUserHasNoAccessToFilial() throws Exception {
        mockMvc.perform(delete("/api/v1/ativos/{id}", ativoFilial1Id)
                .header("Authorization", tokenUserFilial2))
                .andExpect(status().isForbidden());
    }
}
