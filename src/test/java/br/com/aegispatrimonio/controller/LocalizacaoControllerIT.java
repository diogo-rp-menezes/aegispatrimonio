package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
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

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Filial filial;

    @BeforeEach
    void setUp() {
        this.filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00");
        Departamento diretoria = createDepartamento("Diretoria", this.filial);
        Pessoa admin = createUser("admin", "admin@aegis.com", "ROLE_ADMIN", this.filial, diretoria);
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    @Test
    @DisplayName("Deve listar todas as localizações e retornar status 200")
    void testListarTodos() throws Exception {
        createLocalizacao("Sala de Reuniões", this.filial);
        mockMvc.perform(get("/localizacoes").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Sala de Reuniões")));
    }

    @Test
    @DisplayName("Deve buscar uma localização por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        Localizacao localizacao = createLocalizacao("Recepção", this.filial);

        mockMvc.perform(get("/localizacoes/{id}", localizacao.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(localizacao.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Recepção")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar localização com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/localizacoes/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar uma nova localização e retornar status 201")
    void testCriar() throws Exception {
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Almoxarifado", null, filial.getId(), null);

        mockMvc.perform(post("/localizacoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Almoxarifado")));
    }

    @Test
    @DisplayName("Deve atualizar uma localização existente e retornar status 200")
    void testAtualizar() throws Exception {
        Localizacao localizacao = createLocalizacao("Copa", this.filial);
        LocalizacaoUpdateDTO updateDTO = new LocalizacaoUpdateDTO("Refeitório", null, filial.getId(), null, Status.ATIVO);

        mockMvc.perform(put("/localizacoes/{id}", localizacao.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Refeitório")));
    }

    @Test
    @DisplayName("Deve deletar uma localização e retornar status 204")
    void testDeletar() throws Exception {
        Localizacao localizacao = createLocalizacao("Arquivo", this.filial);

        mockMvc.perform(delete("/localizacoes/{id}", localizacao.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/localizacoes/{id}", localizacao.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao localizacao = new Localizacao();
        localizacao.setNome(nome);
        localizacao.setFilial(filial);
        localizacao.setStatus(Status.ATIVO);
        return localizacaoRepository.save(localizacao);
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial f = new Filial();
        f.setNome(nome);
        f.setCodigo(codigo);
        f.setTipo(TipoFilial.MATRIZ);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return filialRepository.save(f);
    }

    private Departamento createDepartamento(String nome, Filial filial) {
        Departamento depto = new Departamento();
        depto.setNome(nome);
        depto.setFilial(filial);
        return departamentoRepository.save(depto);
    }

    private Pessoa createUser(String nome, String email, String role, Filial filial, Departamento depto) {
        Pessoa user = new Pessoa();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setFilial(filial);
        user.setDepartamento(depto);
        user.setStatus(Status.ATIVO);
        user.setCargo("Administrador");
        return pessoaRepository.save(user);
    }
}
