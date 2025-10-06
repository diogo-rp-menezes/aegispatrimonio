package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class ManutencaoControllerIT {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ManutencaoRepository manutencaoRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private FornecedorRepository fornecedorRepository;

    private Ativo ativo1;
    private Pessoa solicitante, tecnico;
    private Manutencao manutencaoSolicitada, manutencaoAprovada;

    @BeforeEach
    void setUp() {
        manutencaoRepository.deleteAll();
        ativoRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        pessoaRepository.deleteAll();
        departamentoRepository.deleteAll();
        fornecedorRepository.deleteAll();
        filialRepository.deleteAll();

        Filial filial = new Filial();
        filial.setNome("Matriz SP");
        filial.setCodigo("SP-01");
        filialRepository.save(filial);

        Departamento depto = new Departamento();
        depto.setNome("TI");
        depto.setFilial(filial);
        departamentoRepository.save(depto);

        solicitante = new Pessoa();
        solicitante.setNome("Ana");
        solicitante.setEmail("ana@test.com");
        solicitante.setDepartamento(depto);
        pessoaRepository.save(solicitante);

        tecnico = new Pessoa();
        tecnico.setNome("Pedro");
        tecnico.setEmail("pedro@test.com");
        tecnico.setDepartamento(depto);
        pessoaRepository.save(tecnico);

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Notebook");
        tipoAtivo.setCategoriaContabil("TI-NB");
        tipoAtivoRepository.save(tipoAtivo);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Dell");
        fornecedorRepository.save(fornecedor);

        ativo1 = new Ativo();
        ativo1.setNumeroPatrimonio("NTK-001");
        ativo1.setNome("Notebook Dell");
        ativo1.setTipoAtivo(tipoAtivo);
        ativo1.setFornecedor(fornecedor);
        ativo1.setPessoaResponsavel(solicitante);
        ativo1.setValorAquisicao(new BigDecimal("5000"));
        ativo1.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(ativo1);

        manutencaoSolicitada = new Manutencao();
        manutencaoSolicitada.setAtivo(ativo1);
        manutencaoSolicitada.setSolicitante(solicitante);
        manutencaoSolicitada.setTipo(TipoManutencao.CORRETIVA);
        manutencaoSolicitada.setDescricaoProblema("Tela quebrada");
        manutencaoRepository.save(manutencaoSolicitada);

        manutencaoAprovada = new Manutencao();
        manutencaoAprovada.setAtivo(ativo1);
        manutencaoAprovada.setSolicitante(solicitante);
        manutencaoAprovada.setTipo(TipoManutencao.PREVENTIVA);
        manutencaoAprovada.setDescricaoProblema("Limpeza interna");
        manutencaoAprovada.setStatus(StatusManutencao.APROVADA);
        manutencaoRepository.save(manutencaoAprovada);
    }

    @Test
    @DisplayName("Deve criar uma nova manutenção quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201() throws Exception {
        ManutencaoRequestDTO request = new ManutencaoRequestDTO();
        request.setAtivoId(ativo1.getId());
        request.setSolicitanteId(solicitante.getId());
        request.setTipo(TipoManutencao.CORRETIVA);
        request.setDescricaoProblema("Não liga");

        mockMvc.perform(post("/manutencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricaoProblema", is("Não liga")));

        assertEquals(3, manutencaoRepository.count());
    }

    @Test
    @DisplayName("Deve aprovar uma manutenção SOLICITADA")
    void aprovar_quandoManutencaoSolicitada_deveRetornar200() throws Exception {
        mockMvc.perform(post("/manutencoes/aprovar/{id}", manutencaoSolicitada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APROVADA")));
    }

    @Test
    @DisplayName("Deve iniciar uma manutenção APROVADA")
    void iniciar_quandoManutencaoAprovada_deveRetornar200() throws Exception {
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(tecnico.getId());

        mockMvc.perform(post("/manutencoes/iniciar/{id}", manutencaoAprovada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EM_ANDAMENTO")))
                .andExpect(jsonPath("$.tecnicoResponsavelId", is(tecnico.getId().intValue())));
    }

    @Test
    @DisplayName("Deve concluir uma manutenção EM_ANDAMENTO")
    void concluir_quandoManutencaoEmAndamento_deveRetornar200() throws Exception {
        manutencaoAprovada.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencaoRepository.save(manutencaoAprovada);

        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO();
        conclusaoDTO.setDescricaoServico("Limpeza concluída");
        conclusaoDTO.setCustoReal(new BigDecimal("150.00"));
        conclusaoDTO.setTempoExecucao(60);

        mockMvc.perform(post("/manutencoes/concluir/{id}", manutencaoAprovada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conclusaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDA")));
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção SOLICITADA")
    void cancelar_quandoManutencaoSolicitada_deveRetornar200() throws Exception {
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO();
        cancelDTO.setMotivo("Não será mais necessário.");

        mockMvc.perform(post("/manutencoes/cancelar/{id}", manutencaoSolicitada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    @DisplayName("Deve listar 2 manutenções quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodasAsManutencoes() throws Exception {
        mockMvc.perform(get("/manutencoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Deve listar 1 manutenção ao filtrar por status SOLICITADA")
    void listar_filtrandoPorStatus_deveRetornarManutencaoCorrespondente() throws Exception {
        mockMvc.perform(get("/manutencoes").param("status", "SOLICITADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].descricaoProblema", is("Tela quebrada")));
    }
}
