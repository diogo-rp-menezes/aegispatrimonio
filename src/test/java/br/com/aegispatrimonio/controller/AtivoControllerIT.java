package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class AtivoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositórios para setup de dados
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Filial filialA;
    private Departamento deptoA;
    private Funcionario userA;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;
    private Localizacao localizacao;
    private Ativo ativoExistente;

    @BeforeEach
    void setUp() {
        // Limpeza é feita pelo @Transactional, mas podemos deletar para garantir IDs previsíveis se necessário

        // Criação de dados base para os testes
        this.filialA = createFilial("Filial A", "FL-A", "01.000.000/0001-01");
        this.deptoA = createDepartamento("TI A", this.filialA);
        this.localizacao = createLocalizacao("Sala 101", this.filialA);
        this.tipoAtivo = createTipoAtivo("Notebook");
        this.fornecedor = createFornecedor("Dell", "11.111.111/0001-11");

        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", "admin@aegis.com", "ROLE_ADMIN", deptoA, Set.of(filialA));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        this.userA = createFuncionarioAndUsuario("User", "user@aegis.com", "ROLE_USER", deptoA, Set.of(filialA));
        this.userToken = jwtService.generateToken(new CustomUserDetails(this.userA.getUsuario()));

        this.ativoExistente = createAtivo("Desktop-01", "PAT-DESK-01", filialA, tipoAtivo, fornecedor, userA, localizacao);
    }

    @Test
    @DisplayName("Criar Ativo: Deve retornar 201 Created para ADMIN com dados válidos")
    void criar_comAdmin_deveRetornarCreated() throws Exception {
        AtivoCreateDTO createDTO = new AtivoCreateDTO(filialA.getId(), "Notebook-01", tipoAtivo.getId(), "PAT-NOTE-01", localizacao.getId(), LocalDate.now(), fornecedor.getId(), BigDecimal.valueOf(3500.50), userA.getId(), "Em uso", "Garantia de 2 anos");

        mockMvc.perform(post("/ativos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Notebook-01")));
    }

    @Test
    @DisplayName("Criar Ativo: Deve retornar 403 Forbidden para USER")
    void criar_comUser_deveRetornarForbidden() throws Exception {
        AtivoCreateDTO createDTO = new AtivoCreateDTO(filialA.getId(), "Notebook-01", tipoAtivo.getId(), "PAT-NOTE-01", localizacao.getId(), LocalDate.now(), fornecedor.getId(), BigDecimal.valueOf(3500.50), userA.getId(), "Em uso", "Garantia de 2 anos");

        mockMvc.perform(post("/ativos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criar Ativo: Deve retornar 400 Bad Request para dados inválidos")
    void criar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
        // Nome em branco, que é obrigatório
        AtivoCreateDTO createDTO = new AtivoCreateDTO(filialA.getId(), "", tipoAtivo.getId(), "PAT-NOTE-02", localizacao.getId(), LocalDate.now(), fornecedor.getId(), BigDecimal.valueOf(3500.50), userA.getId(), "Em uso", "Garantia de 2 anos");

        mockMvc.perform(post("/ativos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de ativos para ADMIN")
    void listarTodos_comAdmin_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/ativos").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Desktop-01")));
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar 200 e a lista de ativos para USER")
    void listarTodos_comUser_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/ativos").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Desktop-01")));
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar 404 Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        long idInexistente = 999L;
        mockMvc.perform(get("/ativos/{id}", idInexistente)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deletar: Deve retornar 403 Forbidden para USER")
    void deletar_comUser_deveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/ativos/{id}", ativoExistente.getId())
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

        return funcionarioRepository.save(func);
    }

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filial);
        loc.setStatus(Status.ATIVO);
        return localizacaoRepository.save(loc);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome);
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo.setStatus(Status.ATIVO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel, Localizacao local) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filial);
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setFuncionarioResponsavel(responsavel);
        a.setLocalizacao(local);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("1000.00"));
        a.setStatus(StatusAtivo.ATIVO);
        a.setDataRegistro(LocalDate.now());
        return ativoRepository.save(a);
    }
}
