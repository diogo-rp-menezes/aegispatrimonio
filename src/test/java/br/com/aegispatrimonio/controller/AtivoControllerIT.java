package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.AtivoRequestDTO;
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
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AtivoControllerIT {

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

    @Autowired private AtivoRepository ativoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;

    private TipoAtivo tipoTI, tipoMobiliario;
    private Localizacao locSala1, locSala2;
    private Pessoa pessoa1;
    private Fornecedor fornecedor1;
    private Ativo ativo1;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        pessoaRepository.deleteAll();
        departamentoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        fornecedorRepository.deleteAll();
        filialRepository.deleteAll();

        Filial filial1 = new Filial();
        filial1.setNome("Matriz SP");
        filial1.setCodigo("SP-01");
        filialRepository.save(filial1);

        Departamento depto1 = new Departamento();
        depto1.setNome("TI");
        depto1.setFilial(filial1);
        departamentoRepository.save(depto1);

        pessoa1 = new Pessoa();
        pessoa1.setNome("Diogo");
        pessoa1.setEmail("diogo@test.com");
        pessoa1.setDepartamento(depto1);
        pessoaRepository.save(pessoa1);

        fornecedor1 = new Fornecedor();
        fornecedor1.setNome("Dell");
        fornecedorRepository.save(fornecedor1);

        tipoTI = new TipoAtivo();
        tipoTI.setNome("Notebook");
        tipoTI.setCategoriaContabil("TI-NB");
        tipoAtivoRepository.save(tipoTI);

        tipoMobiliario = new TipoAtivo();
        tipoMobiliario.setNome("Cadeira");
        tipoMobiliario.setCategoriaContabil("MOB-CAD");
        tipoAtivoRepository.save(tipoMobiliario);

        locSala1 = new Localizacao();
        locSala1.setNome("Sala 101");
        locSala1.setFilial(filial1);
        localizacaoRepository.save(locSala1);

        locSala2 = new Localizacao();
        locSala2.setNome("Sala 102");
        locSala2.setFilial(filial1);
        localizacaoRepository.save(locSala2);

        ativo1 = new Ativo();
        ativo1.setNumeroPatrimonio("NTK-001");
        ativo1.setNome("Notebook Dell");
        ativo1.setTipoAtivo(tipoTI);
        ativo1.setLocalizacao(locSala1);
        ativo1.setFornecedor(fornecedor1);
        ativo1.setPessoaResponsavel(pessoa1);
        ativo1.setValorAquisicao(new BigDecimal("5000"));
        ativo1.setStatus(StatusAtivo.ATIVO);

        Ativo ativo2 = new Ativo();
        ativo2.setNumeroPatrimonio("CAD-001");
        ativo2.setNome("Cadeira Presidente");
        ativo2.setTipoAtivo(tipoMobiliario);
        ativo2.setLocalizacao(locSala2);
        ativo2.setFornecedor(fornecedor1);
        ativo2.setPessoaResponsavel(pessoa1);
        ativo2.setValorAquisicao(new BigDecimal("1200"));
        ativo2.setStatus(StatusAtivo.ATIVO);

        Ativo ativo3 = new Ativo();
        ativo3.setNumeroPatrimonio("MON-001");
        ativo3.setNome("Monitor Dell");
        ativo3.setTipoAtivo(tipoTI);
        ativo3.setLocalizacao(locSala1);
        ativo3.setFornecedor(fornecedor1);
        ativo3.setPessoaResponsavel(pessoa1);
        ativo3.setValorAquisicao(new BigDecimal("1500"));
        ativo3.setStatus(StatusAtivo.EM_MANUTENCAO);

        Ativo ativo4 = new Ativo();
        ativo4.setNumeroPatrimonio("NTK-002");
        ativo4.setNome("Notebook Lenovo");
        ativo4.setTipoAtivo(tipoTI);
        ativo4.setLocalizacao(locSala2);
        ativo4.setFornecedor(fornecedor1);
        ativo4.setPessoaResponsavel(pessoa1);
        ativo4.setValorAquisicao(new BigDecimal("4500"));
        ativo4.setStatus(StatusAtivo.ATIVO);

        ativoRepository.saveAll(List.of(ativo1, ativo2, ativo3, ativo4));
    }

    @Test
    @DisplayName("Deve criar um novo ativo quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201EAtivoCriado() throws Exception {
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setNome("Cadeira Gamer");
        request.setNumeroPatrimonio("CAD-002");
        request.setTipoAtivoId(tipoMobiliario.getId());
        request.setLocalizacaoId(locSala1.getId());
        request.setFornecedorId(fornecedor1.getId());
        request.setPessoaResponsavelId(pessoa1.getId());
        request.setDataAquisicao(LocalDate.of(2024, 1, 1));
        request.setValorAquisicao(new BigDecimal("1800.00"));
        request.setStatus(StatusAtivo.ATIVO);

        mockMvc.perform(post("/ativos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Cadeira Gamer")));

        assertEquals(5, ativoRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar criar ativo com número de patrimônio duplicado")
    void criar_quandoNumeroPatrimonioDuplicado_deveRetornar409() throws Exception {
        AtivoRequestDTO requestDuplicada = new AtivoRequestDTO();
        requestDuplicada.setNome("Notebook Novo");
        requestDuplicada.setNumeroPatrimonio(ativo1.getNumeroPatrimonio());
        requestDuplicada.setTipoAtivoId(tipoTI.getId());
        requestDuplicada.setLocalizacaoId(locSala1.getId());
        requestDuplicada.setFornecedorId(fornecedor1.getId());
        requestDuplicada.setPessoaResponsavelId(pessoa1.getId());
        requestDuplicada.setDataAquisicao(LocalDate.now());
        requestDuplicada.setValorAquisicao(BigDecimal.TEN);
        requestDuplicada.setStatus(StatusAtivo.ATIVO);

        mockMvc.perform(post("/ativos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(4, ativoRepository.count());
    }

    @Test
    @DisplayName("Deve buscar um ativo por ID e retornar 200 com os dados corretos")
    void buscarPorId_quandoAtivoExiste_deveRetornar200EAtivo() throws Exception {
        mockMvc.perform(get("/ativos/{id}", ativo1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ativo1.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(ativo1.getNome())))
                .andExpect(jsonPath("$.numeroPatrimonio", is(ativo1.getNumeroPatrimonio())));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar um ativo com ID inexistente")
    void buscarPorId_quandoAtivoNaoExiste_deveRetornar404() throws Exception {
        mockMvc.perform(get("/ativos/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todos os 4 ativos quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodosOsAtivos() throws Exception {
        mockMvc.perform(get("/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.content", hasSize(4)));
    }

    @Test
    @DisplayName("Deve listar 2 ativos ao filtrar por parte do nome 'Notebook'")
    void listar_filtrandoPorNome_deveRetornarAtivosCorrespondentes() throws Exception {
        mockMvc.perform(get("/ativos").param("nome", "Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Deve listar 3 ativos ao filtrar por tipo 'TI'")
    void listar_filtrandoPorTipo_deveRetornarAtivosCorrespondentes() throws Exception {
        mockMvc.perform(get("/ativos").param("tipoAtivoId", tipoTI.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    @DisplayName("Deve listar 2 ativos ao filtrar pela localização 'Sala 102'")
    void listar_filtrandoPorLocalizacao_deveRetornarAtivosCorrespondentes() throws Exception {
        mockMvc.perform(get("/ativos").param("localizacaoId", locSala2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve listar 1 ativo ao filtrar por status 'EM_MANUTENCAO'")
    void listar_filtrandoPorStatus_deveRetornarAtivosCorrespondentes() throws Exception {
        mockMvc.perform(get("/ativos").param("status", "EM_MANUTENCAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Monitor Dell")));
    }

    @Test
    @DisplayName("Deve listar 2 ativos ao filtrar por nome 'Notebook' e status 'ATIVO'")
    void listar_filtrandoPorNomeEStatus_deveRetornarAtivosCorrespondentes() throws Exception {
        mockMvc.perform(get("/ativos")
                        .param("nome", "Notebook")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o filtro não corresponde a nenhum ativo")
    void listar_quandoFiltroNaoCorresponde_deveRetornarListaVazia() throws Exception {
        mockMvc.perform(get("/ativos").param("nome", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }
}
