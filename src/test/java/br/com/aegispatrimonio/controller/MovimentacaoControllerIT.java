package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class MovimentacaoControllerIT {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    // Repositórios para setup
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Ativo ativo1, ativo2, ativo3, ativo4;
    private Localizacao loc1, loc2, loc3, loc4;
    private Pessoa p1, p2, p3;

    @BeforeEach
    void setUp() {
        // Limpeza em ordem reversa de dependência para evitar erros de constraint
        movimentacaoRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        pessoaRepository.deleteAll();
        departamentoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        fornecedorRepository.deleteAll();
        filialRepository.deleteAll();

        // Setup de dados frescos para cada teste
        Filial f1 = new Filial();
        f1.setNome("Matriz Teste");
        f1.setCodigo("MATRIZ-TESTE");
        filialRepository.save(f1);

        Departamento d1 = new Departamento();
        d1.setNome("TI Teste");
        d1.setFilial(f1);
        departamentoRepository.save(d1);

        p1 = new Pessoa();
        p1.setNome("Pessoa 1");
        p1.setEmail("p1@teste.com");
        p1.setDepartamento(d1);
        pessoaRepository.save(p1);

        p2 = new Pessoa();
        p2.setNome("Pessoa 2");
        p2.setEmail("p2@teste.com");
        p2.setDepartamento(d1);
        pessoaRepository.save(p2);

        p3 = new Pessoa();
        p3.setNome("Pessoa 3");
        p3.setEmail("p3@teste.com");
        p3.setDepartamento(d1);
        pessoaRepository.save(p3);

        loc1 = new Localizacao();
        loc1.setNome("Loc 1");
        loc1.setFilial(f1);
        localizacaoRepository.save(loc1);

        loc2 = new Localizacao();
        loc2.setNome("Loc 2");
        loc2.setFilial(f1);
        localizacaoRepository.save(loc2);

        loc3 = new Localizacao();
        loc3.setNome("Loc 3");
        loc3.setFilial(f1);
        localizacaoRepository.save(loc3);

        loc4 = new Localizacao();
        loc4.setNome("Loc 4");
        loc4.setFilial(f1);
        localizacaoRepository.save(loc4);

        TipoAtivo ta1 = new TipoAtivo();
        ta1.setNome("Notebook Teste");
        ta1.setCategoriaContabil("TI-NB");
        tipoAtivoRepository.save(ta1);

        Fornecedor forn1 = new Fornecedor();
        forn1.setNome("Fornecedor Teste");
        fornecedorRepository.save(forn1);

        ativo1 = new Ativo();
        ativo1.setNumeroPatrimonio("ATIVO-01");
        ativo1.setNome("Notebook 1");
        ativo1.setTipoAtivo(ta1);
        ativo1.setLocalizacao(loc1);
        ativo1.setFornecedor(forn1);
        ativo1.setPessoaResponsavel(p1);
        ativo1.setValorAquisicao(BigDecimal.valueOf(1000));
        ativo1.setValorResidual(BigDecimal.valueOf(100));
        ativo1.setStatus(StatusAtivo.ATIVO);
        ativo1.setDataAquisicao(LocalDate.now());
        ativoRepository.save(ativo1);

        ativo2 = new Ativo();
        ativo2.setNumeroPatrimonio("ATIVO-02");
        ativo2.setNome("Notebook 2");
        ativo2.setTipoAtivo(ta1);
        ativo2.setLocalizacao(loc2);
        ativo2.setFornecedor(forn1);
        ativo2.setPessoaResponsavel(p1);
        ativo2.setValorAquisicao(BigDecimal.valueOf(2000));
        ativo2.setValorResidual(BigDecimal.valueOf(200));
        ativo2.setStatus(StatusAtivo.ATIVO);
        ativo2.setDataAquisicao(LocalDate.now());
        ativoRepository.save(ativo2);

        ativo3 = new Ativo();
        ativo3.setNumeroPatrimonio("ATIVO-03");
        ativo3.setNome("Notebook 3");
        ativo3.setTipoAtivo(ta1);
        ativo3.setLocalizacao(loc1);
        ativo3.setFornecedor(forn1);
        ativo3.setPessoaResponsavel(p1);
        ativo3.setValorAquisicao(BigDecimal.valueOf(3000));
        ativo3.setValorResidual(BigDecimal.valueOf(300));
        ativo3.setStatus(StatusAtivo.ATIVO);
        ativo3.setDataAquisicao(LocalDate.now());
        ativoRepository.save(ativo3);

        ativo4 = new Ativo();
        ativo4.setNumeroPatrimonio("ATIVO-04");
        ativo4.setNome("Notebook 4");
        ativo4.setTipoAtivo(ta1);
        ativo4.setLocalizacao(loc1);
        ativo4.setFornecedor(forn1);
        ativo4.setPessoaResponsavel(p1);
        ativo4.setValorAquisicao(BigDecimal.valueOf(4000));
        ativo4.setValorResidual(BigDecimal.valueOf(400));
        ativo4.setStatus(StatusAtivo.ATIVO);
        ativo4.setDataAquisicao(LocalDate.now());
        ativoRepository.save(ativo4);
    }

    private MovimentacaoRequestDTO criarRequestValido(Long ativoId, Long locOrigemId, Long locDestinoId, Long pOrigemId, Long pDestinoId, String motivo) {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO();
        request.setAtivoId(ativoId);
        request.setLocalizacaoOrigemId(locOrigemId);
        request.setLocalizacaoDestinoId(locDestinoId);
        request.setPessoaOrigemId(pOrigemId);
        request.setPessoaDestinoId(pDestinoId);
        request.setDataMovimentacao(LocalDate.now());
        request.setMotivo(motivo);
        return request;
    }

    @Test
    @DisplayName("Deve criar uma nova movimentação quando dados válidos são enviados via POST")
    void criar_quandoDadosValidos_deveRetornar201EMovimentacaoCriada() throws Exception {
        MovimentacaoRequestDTO request = criarRequestValido(ativo1.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Motivo Teste");

        mockMvc.perform(post("/movimentacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativoNome", is(ativo1.getNome())));

        assertEquals(1, movimentacaoRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar criar movimentação para ativo com movimentação pendente")
    void criar_quandoAtivoJaTemMovimentacaoPendente_deveRetornar409Conflict() throws Exception {
        MovimentacaoRequestDTO primeiraRequest = criarRequestValido(ativo1.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Primeira");
        mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(primeiraRequest))).andExpect(status().isCreated());

        MovimentacaoRequestDTO requestDuplicada = criarRequestValido(ativo1.getId(), loc2.getId(), loc3.getId(), p2.getId(), p3.getId(), "Segunda");

        mockMvc.perform(post("/movimentacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(1, movimentacaoRepository.count());
    }

    @Test
    @DisplayName("Deve efetivar uma movimentação PENDENTE e atualizar o ativo")
    void efetivar_quandoMovimentacaoPendente_deveRetornar200EAtivoAtualizado() throws Exception {
        MovimentacaoRequestDTO request = criarRequestValido(ativo1.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Para efetivar");

        MvcResult result = mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();
        Long movimentacaoId = objectMapper.readValue(result.getResponse().getContentAsString(), MovimentacaoResponseDTO.class).getId();

        mockMvc.perform(post("/movimentacoes/efetivar/{id}", movimentacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EFETIVADA")));

        Ativo ativoAtualizado = ativoRepository.findById(ativo1.getId()).orElseThrow();
        assertEquals(loc2.getId(), ativoAtualizado.getLocalizacao().getId());
        assertEquals(p2.getId(), ativoAtualizado.getPessoaResponsavel().getId());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar efetivar uma movimentação que não está PENDENTE")
    void efetivar_quandoStatusNaoPendente_deveRetornar409Conflict() throws Exception {
        MovimentacaoRequestDTO request = criarRequestValido(ativo2.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Para cancelar");

        MvcResult result = mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();
        Long movimentacaoId = objectMapper.readValue(result.getResponse().getContentAsString(), MovimentacaoResponseDTO.class).getId();

        mockMvc.perform(post("/movimentacoes/cancelar/{id}", movimentacaoId).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString("Cancelado"))).andExpect(status().isOk());

        mockMvc.perform(post("/movimentacoes/efetivar/{id}", movimentacaoId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve cancelar uma movimentação PENDENTE e retornar 200 OK")
    void cancelar_quandoMovimentacaoPendente_deveRetornar200EStatusCancelada() throws Exception {
        MovimentacaoRequestDTO request = criarRequestValido(ativo3.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Para cancelar");

        MvcResult result = mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();
        Long movimentacaoId = objectMapper.readValue(result.getResponse().getContentAsString(), MovimentacaoResponseDTO.class).getId();

        mockMvc.perform(post("/movimentacoes/cancelar/{id}", movimentacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("Cancelamento solicitado pelo teste.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar cancelar uma movimentação que não está PENDENTE")
    void cancelar_quandoStatusNaoPendente_deveRetornar409Conflict() throws Exception {
        MovimentacaoRequestDTO request = criarRequestValido(ativo4.getId(), loc1.getId(), loc2.getId(), p1.getId(), p2.getId(), "Para efetivar");

        MvcResult result = mockMvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();
        Long movimentacaoId = objectMapper.readValue(result.getResponse().getContentAsString(), MovimentacaoResponseDTO.class).getId();

        mockMvc.perform(post("/movimentacoes/efetivar/{id}", movimentacaoId)).andExpect(status().isOk());

        mockMvc.perform(post("/movimentacoes/cancelar/{id}", movimentacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("Tentativa inválida")))
                .andExpect(status().isConflict());
    }
}
