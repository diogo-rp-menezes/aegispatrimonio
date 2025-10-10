package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.PessoaCreateDTO;
import br.com.aegispatrimonio.dto.PessoaUpdateDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PessoaControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Filial filial;
    private Departamento departamento;

    @BeforeEach
    void setUp() {
        this.filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00", TipoFilial.MATRIZ);
        this.departamento = createDepartamento("TI", this.filial);
        Pessoa admin = createUser("admin", "admin@aegis.com", "password", "ROLE_ADMIN", "ADM-001", this.filial, this.departamento);
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    @Test
    @DisplayName("Deve listar todas as pessoas e retornar status 200")
    void testListarTodos() throws Exception {
        createUser("User Test", "user@test.com", "password", "ROLE_USER", "USR-002", this.filial, this.departamento);
        mockMvc.perform(get("/pessoas").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("admin")))
                .andExpect(jsonPath("$[1].nome", is("User Test")));
    }

    @Test
    @DisplayName("Deve buscar uma pessoa por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        Pessoa pessoa = createUser("Find User", "find@test.com", "password", "ROLE_USER", "FND-003", this.filial, this.departamento);

        mockMvc.perform(get("/pessoas/{id}", pessoa.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pessoa.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Find User")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar pessoa com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/pessoas/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar uma nova pessoa e retornar status 201")
    void testCriar() throws Exception {
        PessoaCreateDTO createDTO = new PessoaCreateDTO(filial.getId(), "New User", "NEW-004", "Analista", "new@user.com", "newpassword", "ROLE_USER", departamento.getId());

        mockMvc.perform(post("/pessoas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("New User")))
                .andExpect(jsonPath("$.email", is("new@user.com")));
    }

    @Test
    @DisplayName("Deve atualizar uma pessoa existente e retornar status 200")
    void testAtualizar() throws Exception {
        Pessoa pessoa = createUser("Update User", "update@test.com", "password", "ROLE_USER", "UPD-005", this.filial, this.departamento);
        PessoaUpdateDTO updateDTO = new PessoaUpdateDTO(filial.getId(), "Updated User", "UPD-005", "Gerente", "update@test.com", null, "ROLE_USER", departamento.getId(), Status.INATIVO);

        mockMvc.perform(put("/pessoas/{id}", pessoa.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Updated User")))
                .andExpect(jsonPath("$.status", is("INATIVO")));
    }

    @Test
    @DisplayName("Deve deletar uma pessoa e retornar status 204")
    void testDeletar() throws Exception {
        Pessoa pessoa = createUser("Delete User", "delete@test.com", "password", "ROLE_USER", "DEL-006", this.filial, this.departamento);

        mockMvc.perform(delete("/pessoas/{id}", pessoa.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/pessoas/{id}", pessoa.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---

    private Filial createFilial(String nome, String codigo, String cnpj, TipoFilial tipo) {
        Filial f = new Filial();
        f.setNome(nome);
        f.setCodigo(codigo);
        f.setTipo(tipo);
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

    private Pessoa createUser(String nome, String email, String password, String role, String matricula, Filial filial, Departamento depto) {
        Pessoa user = new Pessoa();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setMatricula(matricula);
        user.setFilial(filial);
        user.setDepartamento(depto);
        user.setStatus(Status.ATIVO);
        user.setCargo("Analista");
        return pessoaRepository.save(user);
    }
}
