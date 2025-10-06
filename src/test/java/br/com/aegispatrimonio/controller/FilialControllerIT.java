package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FilialRequestDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
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
class FilialControllerIT {

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

    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    private Filial filialSP, filialRJ, filialBA;

    @BeforeEach
    void setUp() {
        departamentoRepository.deleteAll();
        filialRepository.deleteAll();

        filialSP = new Filial();
        filialSP.setNome("Matriz São Paulo");
        filialSP.setCodigo("SP-01");

        filialRJ = new Filial();
        filialRJ.setNome("Filial Rio de Janeiro");
        filialRJ.setCodigo("RJ-01");

        filialBA = new Filial();
        filialBA.setNome("Filial Salvador");
        filialBA.setCodigo("BA-01");

        filialRepository.saveAll(List.of(filialSP, filialRJ, filialBA));
    }

    @Test
    @DisplayName("Deve criar uma nova filial quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201EFilialCriada() throws Exception {
        FilialRequestDTO request = new FilialRequestDTO();
        request.setNome("Filial Curitiba");
        request.setCodigo("PR-01");

        mockMvc.perform(post("/filiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Filial Curitiba")))
                .andExpect(jsonPath("$.codigo", is("PR-01")));

        assertEquals(4, filialRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar criar filial com código duplicado")
    void criar_quandoCodigoDuplicado_deveRetornar409() throws Exception {
        FilialRequestDTO requestDuplicada = new FilialRequestDTO();
        requestDuplicada.setNome("Outra Filial SP");
        requestDuplicada.setCodigo(filialSP.getCodigo());

        mockMvc.perform(post("/filiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(3, filialRepository.count());
    }

    @Test
    @DisplayName("Deve buscar uma filial por ID e retornar 200 com os dados corretos")
    void buscarPorId_quandoFilialExiste_deveRetornar200EFilial() throws Exception {
        mockMvc.perform(get("/filiais/{id}", filialRJ.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(filialRJ.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Filial Rio de Janeiro")));
    }

    @Test
    @DisplayName("Deve listar todas as 3 filiais quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodasAsFiliais() throws Exception {
        mockMvc.perform(get("/filiais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar 2 filiais ao filtrar por nome 'Filial'")
    void listar_filtrandoPorNome_deveRetornarFiliaisCorrespondentes() throws Exception {
        mockMvc.perform(get("/filiais").param("nome", "Filial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve listar 1 filial ao filtrar por código 'BA-01'")
    void listar_filtrandoPorCodigo_deveRetornarFilialCorrespondente() throws Exception {
        mockMvc.perform(get("/filiais").param("codigo", "BA-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Filial Salvador")));
    }

    @Test
    @DisplayName("Deve listar 1 filial ao filtrar por nome 'Matriz' e código 'SP-01'")
    void listar_filtrandoPorNomeECodigo_deveRetornarFilialCorrespondente() throws Exception {
        mockMvc.perform(get("/filiais")
                        .param("nome", "Matriz")
                        .param("codigo", "SP-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o filtro não corresponde a nenhuma filial")
    void listar_quandoFiltroNaoCorresponde_deveRetornarListaVazia() throws Exception {
        mockMvc.perform(get("/filiais").param("nome", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @DisplayName("Deve atualizar uma filial e retornar 200 com os dados atualizados")
    void atualizar_quandoDadosValidos_deveRetornar200EDadosAtualizados() throws Exception {
        FilialRequestDTO requestAtualizacao = new FilialRequestDTO();
        requestAtualizacao.setNome("Filial Bahia");
        requestAtualizacao.setCodigo(filialBA.getCodigo());

        mockMvc.perform(put("/filiais/{id}", filialBA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestAtualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Filial Bahia")));
    }

    @Test
    @DisplayName("Deve excluir uma filial e retornar 204 No Content")
    void deletar_quandoFilialExiste_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/filiais/{id}", filialBA.getId()))
                .andExpect(status().isNoContent());

        assertEquals(2, filialRepository.count());
    }
}
