package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class LocalizacaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositórios para setup de dados
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private br.com.aegispatrimonio.repository.RoleRepository roleRepository;
    @Autowired private br.com.aegispatrimonio.repository.PermissionRepository permissionRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Filial filialA, filialB;

    @BeforeEach
    void setUp() {
        // Limpeza geral para garantir isolamento do teste
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        filialRepository.deleteAll();

        // Criação de dados base para os testes
        this.filialA = createFilial("Filial A", "FL-A", "01.000.000/0001-01");
        this.filialB = createFilial("Filial B", "FL-B", "02.000.000/0001-02");

        Departamento deptoA = createDepartamento("TI A", this.filialA);
        createLocalizacao("Sala 101", filialA);
        createLocalizacao("Sala 201", filialB);

        Funcionario adminFunc = createFuncionarioAndUsuario("Admin",
                "admin.loc." + java.util.UUID.randomUUID() + "@aegis.com", "ROLE_ADMIN", deptoA, Set.of(filialA, filialB));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        Funcionario userFunc = createFuncionarioAndUsuario("User",
                "user.loc." + java.util.UUID.randomUUID() + "@aegis.com", "ROLE_USER", deptoA, Set.of(filialA));
        this.userToken = jwtService.generateToken(new CustomUserDetails(userFunc.getUsuario()));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e todas as localizações para ADMIN")
    void listarTodos_comAdmin_deveRetornarTodas() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e apenas localizações da sua filial para USER")
    void listarTodos_comUser_deveRetornarLocalizacoesDaFilial() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Sala 101")));
    }

    @Test
    @DisplayName("Criar: Deve retornar 201 Created para ADMIN com dados válidos")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Almoxarifado", "Piso -1", filialA.getId(), null);

        mockMvc.perform(post("/api/v1/localizacoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Almoxarifado")));
    }

    @Test
    @DisplayName("Deletar: Deve retornar 403 Forbidden para USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        Localizacao loc = localizacaoRepository.findAll().get(0);
        mockMvc.perform(delete("/api/v1/localizacoes/{id}", loc.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // --- Helper Methods ---

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setCodigo(codigo);
        filial.setTipo(TipoFilial.FILIAL);
        filial.setCnpj(cnpj);
        filial.setStatus(Status.ATIVO);
        return filialRepository.save(filial);
    }

    private Departamento createDepartamento(String nome, Filial filial) {
        Departamento depto = new Departamento();
        depto.setNome(nome);
        depto.setFilial(filial);
        return departamentoRepository.save(depto);
    }

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filial);
        loc.setStatus(Status.ATIVO);
        return localizacaoRepository.save(loc);
    }

    private Funcionario createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome(nome);
        func.setMatricula(nome.replaceAll("\\s+", "") + "-001");
        func.setCargo("Analista");
        func.setDepartamento(depto);
        func.setFiliais(filiais);
        func.setStatus(Status.ATIVO);

        Usuario user = new Usuario();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setStatus(Status.ATIVO);
        user.setFuncionario(func);
        func.setUsuario(user);
        // Autorização granular: o admin bypass do PermissionServiceImpl exige o vínculo
        // rbac_user_role com a role ROLE_ADMIN (coluna legada 'role' não é consultada).
        if ("ROLE_ADMIN".equals(role)) {
            br.com.aegispatrimonio.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        br.com.aegispatrimonio.model.Role r = new br.com.aegispatrimonio.model.Role();
                        r.setName("ROLE_ADMIN");
                        r.setDescription("Administrador com acesso total");
                        return roleRepository.save(r);
                    });
            user.setRoles(new java.util.HashSet<>(Set.of(adminRole)));
        } else if ("ROLE_USER".equals(role)) {
            // H1c: controller usa LOCALIZACAO:READ; USER precisa do par granular.
            br.com.aegispatrimonio.model.Permission pRead = permissionRepository
                    .findByResourceAndAction("LOCALIZACAO", "READ")
                    .orElseGet(() -> permissionRepository.save(
                            new br.com.aegispatrimonio.model.Permission(null, "LOCALIZACAO", "READ", "Ler Localizações", null)));
            br.com.aegispatrimonio.model.Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        br.com.aegispatrimonio.model.Role r = new br.com.aegispatrimonio.model.Role();
                        r.setName("ROLE_USER");
                        r.setDescription("Usuário padrão");
                        return roleRepository.save(r);
                    });
            userRole.setPermissions(new java.util.HashSet<>(Set.of(pRead)));
            roleRepository.save(userRole);
            user.setRoles(new java.util.HashSet<>(Set.of(userRole)));
        }

        return funcionarioRepository.save(func);
    }
}
