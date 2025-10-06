package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class PessoaControllerIT {

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

    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;

    private Departamento deptoTI, deptoRH;
    private Pessoa pessoa1;

    @BeforeEach
    void setUp() {
        pessoaRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        Filial filial1 = new Filial();
        filial1.setNome("Matriz SP");
        filial1.setCodigo("SP-01");
        filialRepository.save(filial1);

        deptoTI = new Departamento();
        deptoTI.setNome("TI");
        deptoTI.setFilial(filial1);
        departamentoRepository.save(deptoTI);

        deptoRH = new Departamento();
        deptoRH.setNome("Recursos Humanos");
        deptoRH.setFilial(filial1);
        departamentoRepository.save(deptoRH);

        pessoa1 = new Pessoa();
        pessoa1.setNome("Ana Silva");
        pessoa1.setEmail("ana.silva@test.com");
        pessoa1.setDepartamento(deptoTI);

        Pessoa pessoa2 = new Pessoa();
        pessoa2.setNome("Bruno Costa");
        pessoa2.setEmail("bruno.costa@test.com");
        pessoa2.setDepartamento(deptoTI);

        Pessoa pessoa3 = new Pessoa();
        pessoa3.setNome("Carla Dias");
        pessoa3.setEmail("carla.dias@test.com");
        pessoa3.setDepartamento(deptoRH);

        pessoaRepository.saveAll(List.of(pessoa1, pessoa2, pessoa3));
    }

    @Test
    @DisplayName("Deve criar uma nova pessoa quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201EPessoaCriada() throws Exception {
        PessoaRequestDTO request = new PessoaRequestDTO();
        request.setNome("José da Silva");
        request.setEmail("jose.silva@empresa.com");
        request.setDepartamentoId(deptoTI.getId());

        mockMvc.perform(post("/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("José da Silva")));

        assertEquals(4, pessoaRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar criar pessoa com email duplicado")
    void criar_quandoEmailDuplicado_deveRetornar409() throws Exception {
        PessoaRequestDTO requestDuplicada = new PessoaRequestDTO();
        requestDuplicada.setNome("Ana Nova");
        requestDuplicada.setEmail(pessoa1.getEmail());
        requestDuplicada.setDepartamentoId(deptoRH.getId());

        mockMvc.perform(post("/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(3, pessoaRepository.count());
    }

    @Test
    @DisplayName("Deve listar todas as 3 pessoas quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodasAsPessoas() throws Exception {
        mockMvc.perform(get("/pessoas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar 1 pessoa ao filtrar por nome 'Ana'")
    void listar_filtrandoPorNome_deveRetornarPessoasCorrespondentes() throws Exception {
        mockMvc.perform(get("/pessoas").param("nome", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Ana Silva")));
    }

    @Test
    @DisplayName("Deve listar 2 pessoas ao filtrar pelo departamento de TI")
    void listar_filtrandoPorDepartamento_deveRetornarPessoasCorrespondentes() throws Exception {
        mockMvc.perform(get("/pessoas").param("departamentoId", deptoTI.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve excluir uma pessoa e retornar 204 No Content")
    void deletar_quandoPessoaExiste_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/pessoas/{id}", pessoa1.getId()))
                .andExpect(status().isNoContent());

        assertEquals(2, pessoaRepository.count());
    }
}
