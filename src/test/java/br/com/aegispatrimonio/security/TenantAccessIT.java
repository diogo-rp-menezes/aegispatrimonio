package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.LoginRequestDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@AutoConfigureMockMvc
public class TenantAccessIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Filial filialA;
    private Filial filialB;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        localizacaoRepository.deleteAll();
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        filialA = createFilial("Filial A", "FLA", "36.216.790/0001-26");
        filialB = createFilial("Filial B", "FLB", "75.050.596/0001-77");

        createLocalizacao("Sala Reunião A", filialA);
        createLocalizacao("Sala Reunião B", filialB);

        Departamento depto = createDepartamento("TI", filialA);

        // User has access only to Filial A
        Set<Filial> allowedFiliais = new HashSet<>();
        allowedFiliais.add(filialA);
        createFuncionarioAndUsuario("Test User", "user@test.com", "ROLE_USER", depto, allowedFiliais);

        // Login to get token
        LoginRequestDTO loginRequest = new LoginRequestDTO("user@test.com", "password");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        userToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void access_authorizedFilial_shouldReturn200AndFilteredData() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes")
                        .header("Authorization", "Bearer " + userToken)
                        .header("X-Filial-ID", filialA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Sala Reunião A"));
    }

    @Test
    void access_unauthorizedFilial_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/localizacoes")
                        .header("Authorization", "Bearer " + userToken)
                        .header("X-Filial-ID", filialB.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void access_withoutHeader_shouldReturnAllAllowed() throws Exception {
        // Fallback behavior: returns everything user has access to (Filial A only in this case)
        mockMvc.perform(get("/api/v1/localizacoes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Sala Reunião A"));
    }

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

    private void createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Filial> filiais) {
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

        funcionarioRepository.save(func);
    }

    private void createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filial);
        loc.setStatus(Status.ATIVO);
        localizacaoRepository.save(loc);
    }
}
