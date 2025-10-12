package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
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
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Filial filialExistente;

    // CNPJs Válidos para os testes
    private static final String CNPJ_VALIDO_1 = "45543915000181";
    private static final String CNPJ_VALIDO_2 = "17298092000130";
    private static final String CNPJ_VALIDO_3 = "15302930000177"; // Fornecido pelo usuário
    private static final String CNPJ_VALIDO_4 = "73110356000108";
    private static final String CNPJ_VALIDO_5 = "87353221000107";

    @BeforeEach
    void setUp() {
        this.filialExistente = createFilial("Matriz Teste", "MTRZ", CNPJ_VALIDO_1);
        Departamento diretoria = createDepartamento("Diretoria", filialExistente);

        Funcionario adminFunc = createFuncionarioAndUsuario("Admin Filial", "admin.filial@aegis.com", "ROLE_ADMIN", diretoria, Set.of(filialExistente));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        Funcionario userFunc = createFuncionarioAndUsuario("User Filial", "user.filial@aegis.com", "ROLE_USER", diretoria, Set.of(filialExistente));
        this.userToken = jwtService.generateToken(new CustomUserDetails(userFunc.getUsuario()));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de filiais para ADMIN")
    void listarTodos_comAdmin_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/filiais").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Matriz Teste")));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de filiais para USER")
    void listarTodos_comUser_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/filiais").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Matriz Teste")));
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar 404 Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        long idInexistente = 999L;
        mockMvc.perform(get("/filiais/{id}", idInexistente)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Criar: Deve retornar 201 Created para ADMIN com dados válidos")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("Filial RJ", "RJ-01", TipoFilial.FILIAL, CNPJ_VALIDO_2, "Endereço RJ");

        mockMvc.perform(post("/filiais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Filial RJ")));
    }

    @Test
    @DisplayName("Criar: Deve retornar 403 Forbidden para USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("Filial Proibida", "FL-P", TipoFilial.FILIAL, CNPJ_VALIDO_3, "Endereço");

        mockMvc.perform(post("/filiais")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criar: Deve retornar 400 Bad Request para dados inválidos (nome em branco)")
    void criar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("", "FL-BR", TipoFilial.FILIAL, CNPJ_VALIDO_4, "Endereço");

        mockMvc.perform(post("/filiais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 200 OK para ADMIN com dados válidos")
    void atualizar_comAdmin_deveRetornarOk() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Matriz SP", "MTRZ-SP", TipoFilial.MATRIZ, CNPJ_VALIDO_1, "Novo Endereço", Status.ATIVO);

        mockMvc.perform(put("/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Matriz SP")));
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 400 Bad Request para dados inválidos")
    void atualizar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("", "MTRZ-NEW", TipoFilial.MATRIZ, CNPJ_VALIDO_1, "Endereço Novo", Status.INATIVO);

        mockMvc.perform(put("/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 404 Not Found para ID inexistente")
    void atualizar_comIdInexistente_deveRetornarNotFound() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Nome Fantasma", "FANTASMA", TipoFilial.FILIAL, CNPJ_VALIDO_3, "Endereço Fantasma", Status.ATIVO);

        mockMvc.perform(put("/filiais/{id}", 999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 403 Forbidden para USER")
    void atualizar_comUser_deveRetornarForbidden() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Novo Nome", "MTRZ-NEW", TipoFilial.MATRIZ, CNPJ_VALIDO_1, "Endereço Novo", Status.INATIVO);

        mockMvc.perform(put("/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 204 NoContent para ADMIN")
    void deletar_comAdmin_deveRetornarNoContent() throws Exception {
        Filial filialParaDeletar = createFilial("Filial a Deletar", "FL-DEL", CNPJ_VALIDO_5);

        mockMvc.perform(delete("/filiais/{id}", filialParaDeletar.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 403 Forbidden para USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/filiais/{id}", filialExistente.getId())
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
        return filialRepository.saveAndFlush(filial);
    }

    private Departamento createDepartamento(String nome, Filial filial) {
        Departamento depto = new Departamento();
        depto.setNome(nome);
        depto.setFilial(filial);
        return departamentoRepository.saveAndFlush(depto);
    }

    private Funcionario createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome(nome);
        func.setMatricula(nome.replaceAll("\\s+", "") + "-001");
        func.setCargo("Administrador");
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

        return funcionarioRepository.saveAndFlush(func);
    }
}
