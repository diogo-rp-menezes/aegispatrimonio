package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
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
public class DepartamentoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

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
    @DisplayName("Deve listar todos os departamentos e retornar status 200")
    void testListarTodos() throws Exception {
        createDepartamento("Financeiro", this.filial);
        mockMvc.perform(get("/departamentos").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Diretoria")))
                .andExpect(jsonPath("$[1].nome", is("Financeiro")));
    }

    @Test
    @DisplayName("Deve buscar um departamento por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        Departamento depto = createDepartamento("RH", this.filial);

        mockMvc.perform(get("/departamentos/{id}", depto.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(depto.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("RH")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar departamento com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/departamentos/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar um novo departamento e retornar status 201")
    void testCriar() throws Exception {
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("Comercial", filial.getId());

        mockMvc.perform(post("/departamentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Comercial")));
    }

    @Test
    @DisplayName("Deve atualizar um departamento existente e retornar status 200")
    void testAtualizar() throws Exception {
        Departamento depto = createDepartamento("Marketing", this.filial);
        DepartamentoUpdateDTO updateDTO = new DepartamentoUpdateDTO("Marketing Digital", filial.getId());

        mockMvc.perform(put("/departamentos/{id}", depto.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Marketing Digital")));
    }

    @Test
    @DisplayName("Deve deletar um departamento e retornar status 204")
    void testDeletar() throws Exception {
        Departamento depto = createDepartamento("Logística", this.filial);

        mockMvc.perform(delete("/departamentos/{id}", depto.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/departamentos/{id}", depto.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---

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
