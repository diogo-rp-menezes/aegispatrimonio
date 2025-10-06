package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FornecedorRequestDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
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
class FornecedorControllerIT {

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

    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private AtivoRepository ativoRepository;

    private Fornecedor fornecedorDell;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        fornecedorRepository.deleteAll();

        fornecedorDell = new Fornecedor();
        fornecedorDell.setNome("Dell Computadores");

        Fornecedor fornecedorHP = new Fornecedor();
        fornecedorHP.setNome("HP Brasil");

        fornecedorRepository.saveAll(List.of(fornecedorDell, fornecedorHP));
    }

    @Test
    @DisplayName("Deve criar um novo fornecedor quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201() throws Exception {
        FornecedorRequestDTO request = new FornecedorRequestDTO();
        request.setNome("Soluções em TI");
        request.setEmailContato("contato@solucoesti.com");

        mockMvc.perform(post("/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Soluções em TI")));

        assertEquals(3, fornecedorRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar criar fornecedor com nome duplicado")
    void criar_quandoNomeDuplicado_deveRetornar409() throws Exception {
        FornecedorRequestDTO requestDuplicada = new FornecedorRequestDTO();
        requestDuplicada.setNome(fornecedorDell.getNome());

        mockMvc.perform(post("/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(2, fornecedorRepository.count());
    }

    @Test
    @DisplayName("Deve listar todos os 2 fornecedores quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodosOsFornecedores() throws Exception {
        mockMvc.perform(get("/fornecedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Deve listar 1 fornecedor ao filtrar por nome 'Dell'")
    void listar_filtrandoPorNome_deveRetornarFornecedorCorrespondente() throws Exception {
        mockMvc.perform(get("/fornecedores").param("nome", "Dell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Deve excluir um fornecedor e retornar 204 No Content")
    void deletar_quandoFornecedorExiste_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/fornecedores/{id}", fornecedorDell.getId()))
                .andExpect(status().isNoContent());

        assertEquals(1, fornecedorRepository.count());
    }
}
