package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
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
class DepartamentoControllerIT {

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

    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private PessoaRepository pessoaRepository;

    private Filial filialSP, filialRJ;
    private Departamento deptoTI_SP;

    @BeforeEach
    void setUp() {
        pessoaRepository.deleteAll();
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        filialSP = new Filial();
        filialSP.setNome("Matriz São Paulo");
        filialSP.setCodigo("SP-01");
        filialRepository.save(filialSP);

        filialRJ = new Filial();
        filialRJ.setNome("Filial Rio de Janeiro");
        filialRJ.setCodigo("RJ-01");
        filialRepository.save(filialRJ);

        deptoTI_SP = new Departamento();
        deptoTI_SP.setNome("Tecnologia da Informação");
        deptoTI_SP.setFilial(filialSP);

        Departamento deptoRH_SP = new Departamento();
        deptoRH_SP.setNome("Recursos Humanos");
        deptoRH_SP.setFilial(filialSP);

        Departamento deptoVendas_RJ = new Departamento();
        deptoVendas_RJ.setNome("Vendas");
        deptoVendas_RJ.setFilial(filialRJ);

        departamentoRepository.saveAll(List.of(deptoTI_SP, deptoRH_SP, deptoVendas_RJ));
    }

    @Test
    @DisplayName("Deve criar um novo departamento quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201() throws Exception {
        DepartamentoRequestDTO request = new DepartamentoRequestDTO();
        request.setNome("Financeiro");
        request.setFilialId(filialSP.getId());

        mockMvc.perform(post("/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Financeiro")));

        assertEquals(4, departamentoRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar criar departamento com filialId inexistente")
    void criar_quandoFilialNaoExiste_deveRetornar404() throws Exception {
        DepartamentoRequestDTO request = new DepartamentoRequestDTO();
        request.setNome("Departamento Fantasma");
        request.setFilialId(9999L);

        mockMvc.perform(post("/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todos os 3 departamentos quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodosOsDepartamentos() throws Exception {
        mockMvc.perform(get("/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar 2 departamentos ao filtrar pela filial de SP")
    void listar_filtrandoPorFilial_deveRetornarDepartamentosCorrespondentes() throws Exception {
        mockMvc.perform(get("/departamentos").param("filialId", filialSP.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve listar 1 departamento ao filtrar por nome 'Tecnologia'")
    void listar_filtrandoPorNome_deveRetornarDepartamentoCorrespondente() throws Exception {
        mockMvc.perform(get("/departamentos").param("nome", "Tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Deve excluir um departamento e retornar 204 No Content")
    void deletar_quandoDepartamentoExiste_deveRetornar204() throws Exception {
        // Criar um departamento novo para garantir que não tem pessoas vinculadas
        Departamento deptoParaDeletar = new Departamento();
        deptoParaDeletar.setNome("Para Deletar");
        deptoParaDeletar.setFilial(filialRJ);
        departamentoRepository.save(deptoParaDeletar);

        mockMvc.perform(delete("/departamentos/{id}", deptoParaDeletar.getId()))
                .andExpect(status().isNoContent());

        assertEquals(3, departamentoRepository.count());
    }
}
