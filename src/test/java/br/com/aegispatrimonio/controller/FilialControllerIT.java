package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
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
public class FilialControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setUp() {
        // Cria um usuário admin para os testes
        Filial matriz = createFilial("Matriz", "MTRZ", "00.000.000/0001-00", TipoFilial.MATRIZ);
        Departamento diretoria = createDepartamento("Diretoria", matriz);
        Pessoa admin = createUser("admin", "admin@aegis.com", "ROLE_ADMIN", matriz, diretoria);
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    @Test
    @DisplayName("Deve listar todas as filiais e retornar status 200")
    void testListarTodos() throws Exception {
        createFilial("Filial SP", "FLSP", "11.111.111/0001-11", TipoFilial.FILIAL);
        mockMvc.perform(get("/filiais").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Matriz")))
                .andExpect(jsonPath("$[1].nome", is("Filial SP")));
    }

    @Test
    @DisplayName("Deve buscar uma filial por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        Filial filial = createFilial("Filial RJ", "FLRJ", "22.222.222/0001-22", TipoFilial.FILIAL);

        mockMvc.perform(get("/filiais/{id}", filial.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(filial.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Filial RJ")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar filial com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/filiais/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar uma nova filial e retornar status 201")
    void testCriar() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("Filial MG", "FLMG", TipoFilial.FILIAL, "33.333.333/0001-33", "Endereço MG");

        mockMvc.perform(post("/filiais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Filial MG")));
    }

    @Test
    @DisplayName("Deve atualizar uma filial existente e retornar status 200")
    void testAtualizar() throws Exception {
        Filial filial = createFilial("Filial BA", "FLBA", "44.444.444/0001-44", TipoFilial.FILIAL);
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Filial Bahia", "FLBA", TipoFilial.FILIAL, "44.444.444/0001-44", "Endereço BA", Status.INATIVO);

        mockMvc.perform(put("/filiais/{id}", filial.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Filial Bahia")))
                .andExpect(jsonPath("$.status", is("INATIVO")));
    }

    @Test
    @DisplayName("Deve deletar uma filial e retornar status 204")
    void testDeletar() throws Exception {
        Filial filial = createFilial("Filial RS", "FLRS", "55.555.555/0001-55", TipoFilial.FILIAL);

        mockMvc.perform(delete("/filiais/{id}", filial.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/filiais/{id}", filial.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---

    private Filial createFilial(String nome, String codigo, String cnpj, TipoFilial tipo) {
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setCodigo(codigo);
        filial.setTipo(tipo);
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

    private Pessoa createUser(String nome, String email, String role, Filial filial, Departamento depto) {
        Pessoa user = new Pessoa();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setFilial(filial);
        user.setDepartamento(depto);
        user.setStatus(Status.ATIVO);
        user.setCargo("Analista");
        return pessoaRepository.save(user);
    }
}
