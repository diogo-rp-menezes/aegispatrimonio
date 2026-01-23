package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
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

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class FilialControllerIT extends BaseIT {

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

    @Autowired private AtivoRepository ativoRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Filial filialExistente;
    private String cnpjExistente;

    @BeforeEach
    void setUp() {
        // Limpar repositórios para garantir um estado limpo para cada teste
        ativoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        this.cnpjExistente = generateRandomCNPJ();
        this.filialExistente = createFilial("Matriz Teste", "MTRZ-" + UUID.randomUUID().toString().substring(0,5), cnpjExistente);
        Departamento diretoria = createDepartamento("Diretoria", filialExistente);

        String adminEmail = "admin.filial." + UUID.randomUUID().toString() + "@aegis.com";
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin Filial", adminEmail, "ROLE_ADMIN", diretoria, Set.of(filialExistente));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        String userEmail = "user.filial." + UUID.randomUUID().toString() + "@aegis.com";
        Funcionario userFunc = createFuncionarioAndUsuario("User Filial", userEmail, "ROLE_USER", diretoria, Set.of(filialExistente));
        this.userToken = jwtService.generateToken(new CustomUserDetails(userFunc.getUsuario()));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de filiais para ADMIN")
    void listarTodos_comAdmin_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/api/v1/filiais").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Matriz Teste")));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de filiais para USER")
    void listarTodos_comUser_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/api/v1/filiais").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Matriz Teste")));
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar 404 Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        long idInexistente = 999L;
        mockMvc.perform(get("/api/v1/filiais/{id}", idInexistente)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Criar: Deve retornar 201 Created para ADMIN com dados válidos")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("Filial RJ", "RJ-01", TipoFilial.FILIAL, generateRandomCNPJ(), "Endereço RJ");

        mockMvc.perform(post("/api/v1/filiais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Filial RJ")));
    }

    @Test
    @DisplayName("Criar: Deve retornar 403 Forbidden para USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("Filial Proibida", "FL-P", TipoFilial.FILIAL, generateRandomCNPJ(), "Endereço");

        mockMvc.perform(post("/api/v1/filiais")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criar: Deve retornar 400 Bad Request para dados inválidos (nome em branco)")
    void criar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        FilialCreateDTO createDTO = new FilialCreateDTO("", "FL-BR", TipoFilial.FILIAL, generateRandomCNPJ(), "Endereço");

        mockMvc.perform(post("/api/v1/filiais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 200 OK para ADMIN com dados válidos")
    void atualizar_comAdmin_deveRetornarOk() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Matriz SP", "MTRZ-SP", TipoFilial.MATRIZ, cnpjExistente, "Novo Endereço", Status.ATIVO);

        mockMvc.perform(put("/api/v1/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Matriz SP")));
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 400 Bad Request para dados inválidos")
    void atualizar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("", "MTRZ-NEW", TipoFilial.MATRIZ, cnpjExistente, "Endereço Novo", Status.INATIVO);

        mockMvc.perform(put("/api/v1/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 404 Not Found para ID inexistente")
    void atualizar_comIdInexistente_deveRetornarNotFound() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Nome Fantasma", "FANTASMA", TipoFilial.FILIAL, generateRandomCNPJ(), "Endereço Fantasma", Status.ATIVO);

        mockMvc.perform(put("/api/v1/filiais/{id}", 999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualizar: Deve retornar 403 Forbidden para USER")
    void atualizar_comUser_deveRetornarForbidden() throws Exception {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Novo Nome", "MTRZ-NEW", TipoFilial.MATRIZ, cnpjExistente, "Endereço Novo", Status.INATIVO);

        mockMvc.perform(put("/api/v1/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 204 NoContent para ADMIN")
    void deletar_comAdmin_deveRetornarNoContent() throws Exception {
        Filial filialParaDeletar = createFilial("Filial a Deletar", "FL-DEL", generateRandomCNPJ());

        mockMvc.perform(delete("/api/v1/filiais/{id}", filialParaDeletar.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 403 Forbidden para USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/filiais/{id}", filialExistente.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // --- Helper Methods ---

    private String generateRandomCNPJ() {
        // Gera um CNPJ válido
        int[] n = new int[14];
        for (int i = 0; i < 12; i++) {
            n[i] = (int) (Math.random() * 10);
        }

        // Digito 1
        int sum = 0;
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 12; i++) sum += n[i] * weights1[i];
        int r = sum % 11;
        n[12] = (r < 2) ? 0 : 11 - r;

        // Digito 2
        sum = 0;
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 13; i++) sum += n[i] * weights2[i];
        r = sum % 11;
        n[13] = (r < 2) ? 0 : 11 - r;

        StringBuilder sb = new StringBuilder();
        for (int d : n) sb.append(d);
        return sb.toString();
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

        savedFunc.setFiliais(new java.util.HashSet<>(filiais));
        return funcionarioRepository.save(savedFunc);
    }
}
