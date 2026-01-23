package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class FuncionarioControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private String adminToken;
    private String userToken;
    private Filial filialA;
    private Departamento deptoA;
    private Funcionario funcionarioExistente;

    private static Long nextFuncionarioId = 1L;

    @BeforeEach
    void setUp() {
        // Limpar repositórios para garantir um estado limpo para cada teste
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        // Resetar auto-incremento para tabelas relevantes
        resetAutoIncrement();

        // Resetar o contador de ID para cada execução do setUp
        nextFuncionarioId = 1L;

        filialA = createFilial("Filial A", "FL-A", "01.000.000/0001-01");
        deptoA = createDepartamento("TI A", filialA);

        String adminEmail = "admin." + UUID.randomUUID().toString() + "@aegis.com";
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", adminEmail, "ROLE_ADMIN", deptoA, new HashSet<>(Set.of(filialA)));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        String userEmail = "user." + UUID.randomUUID().toString() + "@aegis.com";
        this.funcionarioExistente = createFuncionarioAndUsuario("User", userEmail, "ROLE_USER", deptoA, new HashSet<>(Set.of(filialA)));
        this.userToken = jwtService.generateToken(new CustomUserDetails(this.funcionarioExistente.getUsuario()));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de funcionários para ADMIN")
    void listarTodos_comAdmin_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Admin")));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de funcionários para USER")
    void listarTodos_comUser_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Admin")));
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar 404 Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        long idInexistente = 999L;
        mockMvc.perform(get("/api/v1/funcionarios/{id}", idInexistente)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Criar: Deve retornar 201 Created para ADMIN com dados válidos")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        FuncionarioCreateDTO createDTO = new FuncionarioCreateDTO("Novo Func", "M-003", "Cargo", deptoA.getId(), Set.of(filialA.getId()), "novo@aegis.com", "senha1234", "ROLE_USER");

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Novo Func")))
                .andExpect(jsonPath("$.email", is("novo@aegis.com")));
    }

    @Test
    @DisplayName("Criar: Deve retornar 403 Forbidden para USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        FuncionarioCreateDTO createDTO = new FuncionarioCreateDTO("Novo Func", "M-003", "Cargo", deptoA.getId(), Set.of(filialA.getId()), "novo@aegis.com", "senha1234", "ROLE_USER");

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criar: Deve retornar 400 Bad Request para dados inválidos (email inválido)")
    void criar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        FuncionarioCreateDTO createDTO = new FuncionarioCreateDTO("Nome Valido", "M-004", "Cargo", deptoA.getId(), Set.of(filialA.getId()), "email-invalido", "senha1234", "ROLE_USER");

        mockMvc.perform(post("/api/v1/funcionarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 403 Forbidden para USER")
    void atualizar_comUser_deveRetornarForbidden() throws Exception {
        FuncionarioUpdateDTO updateDTO = new FuncionarioUpdateDTO("Nome Atualizado", "M-001", "Cargo Novo", deptoA.getId(), Status.ATIVO, Set.of(filialA.getId()), "user.updated@aegis.com", "newpassword", "ROLE_USER");

        mockMvc.perform(put("/api/v1/funcionarios/{id}", funcionarioExistente.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 204 NoContent para ADMIN")
    void deletar_comAdmin_deveRetornarNoContent() throws Exception {
        Funcionario funcParaDeletar = createFuncionarioAndUsuario("Para Deletar", "deletar@aegis.com", "ROLE_USER", deptoA, new HashSet<>(Set.of(filialA)));

        mockMvc.perform(delete("/api/v1/funcionarios/{id}", funcParaDeletar.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
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
        func.setCargo("Analista");
        func.setDepartamento(depto);
        func.setStatus(Status.ATIVO);

        Usuario user = new Usuario();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setStatus(Status.ATIVO);
        user.setFuncionario(func);
        func.setUsuario(user);

        Funcionario savedFunc = funcionarioRepository.save(func);
        entityManager.flush();

        savedFunc.setFiliais(filiais);
        return funcionarioRepository.save(savedFunc);
    }

    private void resetAutoIncrement() {
        try {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM funcionario_filial").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM funcionarios").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuarios").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM departamentos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM filiais").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            entityManager.flush();
        } catch (Exception e) {
            System.out.println("Warning: Could not reset auto-increment: " + e.getMessage());
        }
    }
}