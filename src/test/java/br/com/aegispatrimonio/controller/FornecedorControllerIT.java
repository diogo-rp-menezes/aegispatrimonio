package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
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
public class FornecedorControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FornecedorRepository fornecedorRepository;

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

    @BeforeEach
    void setUp() {
        // CNPJ Válido para a Matriz
        Filial matriz = createFilial("Matriz", "MTRZ", "53.436.470/0001-02");
        Departamento diretoria = createDepartamento("Diretoria", matriz);
        Pessoa admin = createUser("admin", "admin@aegis.com", "ROLE_ADMIN", matriz, diretoria);
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    @Test
    @DisplayName("Deve listar todos os fornecedores e retornar status 200")
    void testListarTodos() throws Exception {
        // CNPJ Válido
        createFornecedor("Fornecedor A", "87.353.221/0001-07");
        mockMvc.perform(get("/fornecedores").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Fornecedor A")));
    }

    @Test
    @DisplayName("Deve buscar um fornecedor por ID e retornar status 200")
    void testBuscarPorId() throws Exception {
        // CNPJ Válido
        Fornecedor fornecedor = createFornecedor("Fornecedor B", "23.612.237/0001-99");

        mockMvc.perform(get("/fornecedores/{id}", fornecedor.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fornecedor.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Fornecedor B")));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar fornecedor com ID inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/fornecedores/{id}", 999L).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar um novo fornecedor e retornar status 201")
    void testCriar() throws Exception {
        // CNPJ Válido fornecido pelo usuário.
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Fornecedor C", "38.002.762/0001-08", "Endereço", "Contato", "email@email.com", "123456", "Obs");

        mockMvc.perform(post("/fornecedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Fornecedor C")));
    }

    @Test
    @DisplayName("Deve atualizar um fornecedor existente e retornar status 200")
    void testAtualizar() throws Exception {
        // CNPJ Válido
        Fornecedor fornecedor = createFornecedor("Fornecedor D", "41.238.799/0001-36");
        FornecedorUpdateDTO updateDTO = new FornecedorUpdateDTO("Fornecedor D-Atualizado", "41.238.799/0001-36", "Endereço", "Contato", "email@email.com", "123456", "Obs", Status.INATIVO);

        mockMvc.perform(put("/fornecedores/{id}", fornecedor.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Fornecedor D-Atualizado")))
                .andExpect(jsonPath("$.status", is("INATIVO")));
    }

    @Test
    @DisplayName("Deve deletar um fornecedor e retornar status 204")
    void testDeletar() throws Exception {
        // CNPJ Válido
        Fornecedor fornecedor = createFornecedor("Fornecedor E", "33.041.260/0001-64");

        mockMvc.perform(delete("/fornecedores/{id}", fornecedor.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/fornecedores/{id}", fornecedor.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setCodigo(codigo);
        filial.setTipo(TipoFilial.MATRIZ);
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
        user.setCargo("Administrador");
        return pessoaRepository.save(user);
    }
}
