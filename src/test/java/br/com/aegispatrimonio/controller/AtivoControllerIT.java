package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
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
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
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

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Filial filialA;
    private Departamento deptoA;
    private Pessoa userA;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        this.filialA = createFilial("Filial A", "FLA", "00.000.000/0001-00");
        this.deptoA = createDepartamento("TI A", this.filialA);
        this.userA = createUser("userA", "userA@aegis.com", "ROLE_USER", this.filialA, this.deptoA);
        this.tipoAtivo = createTipoAtivo("Desktop");
        this.fornecedor = createFornecedor("Dell", "11.111.111/0001-11");

        userToken = jwtService.generateToken(new CustomUserDetails(this.userA));
    }

    @Test
    @DisplayName("Listar Ativos: Deve retornar 401 Unauthorized sem autenticação")
    void listarAtivos_semAutenticacao_deveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/ativos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listar Ativos: Deve retornar 200 OK com autenticação")
    void listarAtivos_comAutenticacao_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/ativos").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Listar Ativos: Usuário de filial deve ver apenas ativos da sua filial")
    void listarAtivos_quandoUsuarioDeFilial_deveRetornarApenasAtivosDaSuaFilial() throws Exception {
        Filial filialB = createFilial("Filial B", "FLB", "00.000.000/0002-00");
        Ativo ativoFilialA = createAtivo("PC-01", "PAT-01", this.filialA, this.tipoAtivo, this.fornecedor, this.userA);
        createAtivo("PC-02", "PAT-02", filialB, this.tipoAtivo, this.fornecedor, this.userA);

        mockMvc.perform(get("/ativos").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(ativoFilialA.getId().intValue())));
    }

    @Test
    @DisplayName("Buscar Ativo por ID: Deve retornar 200 OK e o ativo correto")
    void buscarPorId_comIdExistente_deveRetornarAtivo() throws Exception {
        Ativo ativo = createAtivo("PC-03", "PAT-03", this.filialA, this.tipoAtivo, this.fornecedor, this.userA);

        mockMvc.perform(get("/ativos/{id}", ativo.getId()).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ativo.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("PC-03")));
    }

    @Test
    @DisplayName("Buscar Ativo por ID: Deve retornar 404 Not Found para ID inexistente")
    void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
        mockMvc.perform(get("/ativos/{id}", 999L).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Criar Ativo: Deve criar um novo ativo e retornar 201 Created")
    void criarAtivo_comDadosValidos_deveRetornarCreated() throws Exception {
        AtivoCreateDTO createDTO = new AtivoCreateDTO(this.filialA.getId(), "Notebook-01", tipoAtivo.getId(), "PAT-NOTE-01", null, LocalDate.now(), fornecedor.getId(), BigDecimal.valueOf(3500.50), userA.getId(), "Em uso", null);

        mockMvc.perform(post("/ativos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Notebook-01")));
    }

    @Test
    @DisplayName("Atualizar Ativo: Deve atualizar um ativo existente e retornar 200 OK")
    void atualizarAtivo_comDadosValidos_deveRetornarOk() throws Exception {
        Ativo ativo = createAtivo("PC-04", "PAT-04", this.filialA, this.tipoAtivo, this.fornecedor, this.userA);
        AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(ativo.getFilial().getId(), "PC-04-Atualizado", "PAT-04", tipoAtivo.getId(), null, StatusAtivo.ATIVO, LocalDate.now(), fornecedor.getId(), BigDecimal.valueOf(4000.00), userA.getId(), "Em uso", null);

        mockMvc.perform(put("/ativos/{id}", ativo.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("PC-04-Atualizado")));
    }

    @Test
    @DisplayName("Deletar Ativo: Deve deletar um ativo e retornar 204 No Content")
    void deletarAtivo_comIdExistente_deveRetornarNoContent() throws Exception {
        Ativo ativo = createAtivo("PC-05-DELETE", "PAT-05", this.filialA, this.tipoAtivo, this.fornecedor, this.userA);

        mockMvc.perform(delete("/ativos/{id}", ativo.getId()).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Verifica se o ativo foi realmente deletado
        mockMvc.perform(get("/ativos/{id}", ativo.getId()).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Health Check: Deve atualizar o health check do ativo e retornar 204 No Content")
    void updateHealthCheck_comDadosValidos_deveRetornarNoContent() throws Exception {
        Ativo ativo = createAtivo("PC-06-HEALTH", "PAT-06", this.filialA, this.tipoAtivo, this.fornecedor, this.userA);
        HealthCheckDTO healthCheckDTO = new HealthCheckDTO("Test-PC", "WORKGROUP", "Test OS", "1.0", "x64", "Test-Manu", "Test-Model", "Test-Serial", "Test-CPU", 2, 4, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        mockMvc.perform(patch("/ativos/{id}/health-check", ativo.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(healthCheckDTO)))
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

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome);
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Pessoa responsavel) {
        Ativo ativo = new Ativo();
        ativo.setNome(nome);
        ativo.setNumeroPatrimonio(patrimonio);
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipo);
        ativo.setFornecedor(fornecedor);
        ativo.setPessoaResponsavel(responsavel);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(new BigDecimal("1000.00"));
        ativo.setStatus(StatusAtivo.ATIVO);
        return ativoRepository.save(ativo);
    }
}
