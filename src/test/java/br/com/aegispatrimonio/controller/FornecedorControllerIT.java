package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class FornecedorControllerIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Fornecedor fornecedorAtivo;
    private String adminToken;
    private String userToken;
    private Filial filial;

    @BeforeEach
    void setUp() {
        // Clean up
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();
        fornecedorRepository.deleteAll();

        // Create initial Fornecedor
        Fornecedor novoFornecedor = new Fornecedor();
        novoFornecedor.setNome("Fornecedor Existente");
        novoFornecedor.setCnpj("41238799000136");
        novoFornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedorAtivo = fornecedorRepository.save(novoFornecedor);

        // Setup Users/Tokens
        this.filial = createFilial("Matriz", "MTRZ", "45543915000181");
        Departamento depto = createDepartamento("TI", filial);

        String adminEmail = "admin." + UUID.randomUUID() + "@aegis.com";
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", adminEmail, "ROLE_ADMIN", depto, Set.of(filial));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        String userEmail = "user." + UUID.randomUUID() + "@aegis.com";
        Funcionario userFunc = createFuncionarioAndUsuario("User", userEmail, "ROLE_USER", depto, Set.of(filial));
        this.userToken = jwtService.generateToken(new CustomUserDetails(userFunc.getUsuario()));
    }

    @Test
    @DisplayName("POST /api/v1/fornecedores - Deve criar um fornecedor com sucesso para usuário ADMIN")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor", "08969361000152", "Endereço", "Contato", "contato@novo.com", "987654321", "Obs Nova");

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Fornecedor")))
                .andExpect(jsonPath("$.cnpj", is("08969361000152")));
    }

    @Test
    @DisplayName("DELETE /api/v1/fornecedores/{id} - Deve deletar (soft delete) o fornecedor com sucesso para usuário ADMIN")
    void deletar_comAdmin_deveMoverParaInativo() throws Exception {
        mockMvc.perform(delete("/api/v1/fornecedores/{id}", fornecedorAtivo.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        String statusAposDelete = (String) entityManager.createNativeQuery(
                "SELECT status FROM fornecedores WHERE id = :id")
                .setParameter("id", fornecedorAtivo.getId())
                .getSingleResult();

        assertEquals("INATIVO", statusAposDelete);
    }

    @Test
    @DisplayName("POST /api/v1/fornecedores - Deve retornar Forbidden para usuário USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor User", "15302930000177", "Endereço", "Contato", "contato@novo.com", "987654321", "Obs Nova");

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/fornecedores - Deve retornar Bad Request para CNPJ duplicado")
    void criar_comCnpjDuplicado_deveRetornarBadRequest() throws Exception {
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO("Novo Fornecedor Duplicado", fornecedorAtivo.getCnpj(), "Endereço Duplicado", null, null, null, null);

        mockMvc.perform(post("/api/v1/fornecedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/fornecedores/{id} - Deve encontrar o fornecedor pelo ID")
    void buscarPorId_deveRetornarFornecedor() throws Exception {
        mockMvc.perform(get("/api/v1/fornecedores/{id}", fornecedorAtivo.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fornecedorAtivo.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(fornecedorAtivo.getNome())));
    }

    @Test
    @DisplayName("GET /api/v1/fornecedores/{id} - Deve retornar Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/fornecedores/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/fornecedores/{id} - Deve atualizar o fornecedor com sucesso para usuário ADMIN")
    void atualizar_comAdmin_deveRetornarOk() throws Exception {
        FornecedorUpdateDTO updateDTO = new FornecedorUpdateDTO("Fornecedor Atualizado", "28803082000121", "Endereço Novo", "Contato Update", "contato@update.com", "112233445", "Obs Update", StatusFornecedor.INATIVO);

        mockMvc.perform(put("/api/v1/fornecedores/{id}", fornecedorAtivo.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Fornecedor Atualizado")))
                .andExpect(jsonPath("$.status", is("INATIVO")));
    }

    @Test
    @DisplayName("DELETE /api/v1/fornecedores/{id} - Deve retornar Forbidden para usuário USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/fornecedores/{id}", fornecedorAtivo.getId())
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

    private Funcionario createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome(nome);
        func.setMatricula(nome.replaceAll("\\s+", "") + "-" + UUID.randomUUID().toString().substring(0, 8));
        func.setCargo("Administrador");
        func.setDepartamento(depto);
        func.setStatus(Status.ATIVO);
        func.setId(null);

        Funcionario savedFunc = funcionarioRepository.save(func);
        funcionarioRepository.flush();

        Usuario user = new Usuario();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setStatus(Status.ATIVO);
        user.setFuncionario(savedFunc);
        savedFunc.setUsuario(user);

        savedFunc.setFiliais(filiais);
        return funcionarioRepository.save(savedFunc);
    }
}
