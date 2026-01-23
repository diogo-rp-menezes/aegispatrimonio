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

        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", "admin@aegis.com", "ROLE_ADMIN", deptoA, Set.of(filialA, filialB));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        Funcionario userFunc = createFuncionarioAndUsuario("User", "user@aegis.com", "ROLE_USER", deptoA, Set.of(filialA));
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
        func.setFiliais(new java.util.HashSet<>(filiais));
        func.setStatus(Status.ATIVO);

        Usuario user = new Usuario();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setStatus(Status.ATIVO);
        user.setFuncionario(func);
        func.setUsuario(user);

        return funcionarioRepository.save(func);
    }
}
