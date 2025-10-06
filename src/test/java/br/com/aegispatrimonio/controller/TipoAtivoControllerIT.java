package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.TipoAtivoRequestDTO;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
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
class TipoAtivoControllerIT {

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

    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private AtivoRepository ativoRepository;

    private TipoAtivo tipoHardware;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        tipoAtivoRepository.deleteAll();

        tipoHardware = new TipoAtivo();
        tipoHardware.setNome("Hardware");
        tipoHardware.setCategoriaContabil("TI-HW");

        TipoAtivo tipoSoftware = new TipoAtivo();
        tipoSoftware.setNome("Software");
        tipoSoftware.setCategoriaContabil("TI-SW");

        TipoAtivo tipoMobiliario = new TipoAtivo();
        tipoMobiliario.setNome("Mobiliário");
        tipoMobiliario.setCategoriaContabil("MOB-GEN");

        tipoAtivoRepository.saveAll(List.of(tipoHardware, tipoSoftware, tipoMobiliario));
    }

    @Test
    @DisplayName("Deve criar um novo tipo de ativo quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201ETipoAtivoCriado() throws Exception {
        TipoAtivoRequestDTO request = new TipoAtivoRequestDTO();
        request.setNome("Equipamentos de Rede");
        request.setDescricao("Roteadores, switches, etc.");
        request.setCategoriaContabil("TI-HW-REDE");
        request.setIcone("router");

        mockMvc.perform(post("/tipos-ativo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Equipamentos de Rede")));

        assertEquals(4, tipoAtivoRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar criar tipo de ativo com nome duplicado")
    void criar_quandoNomeDuplicado_deveRetornar409() throws Exception {
        TipoAtivoRequestDTO requestDuplicada = new TipoAtivoRequestDTO();
        requestDuplicada.setNome(tipoHardware.getNome());
        requestDuplicada.setDescricao("...");
        requestDuplicada.setCategoriaContabil("...");
        requestDuplicada.setIcone("...");

        mockMvc.perform(post("/tipos-ativo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDuplicada)))
                .andExpect(status().isConflict());

        assertEquals(3, tipoAtivoRepository.count());
    }

    @Test
    @DisplayName("Deve listar todos os 3 tipos de ativo quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodosOsTipos() throws Exception {
        mockMvc.perform(get("/tipos-ativo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar 2 tipos de ativo ao filtrar pela categoria contábil 'TI'")
    void listar_filtrandoPorCategoriaContabil_deveRetornarTiposCorrespondentes() throws Exception {
        mockMvc.perform(get("/tipos-ativo").param("categoriaContabil", "TI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Deve listar 1 tipo de ativo ao filtrar por nome 'Mobiliário'")
    void listar_filtrandoPorNome_deveRetornarTipoCorrespondente() throws Exception {
        mockMvc.perform(get("/tipos-ativo").param("nome", "Mobiliário"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Mobiliário")));
    }

    @Test
    @DisplayName("Deve excluir um tipo de ativo e retornar 204 No Content")
    void deletar_quandoTipoAtivoExiste_deveRetornar204() throws Exception {
        // Criar um tipo de ativo sem dependências para poder ser excluído
        TipoAtivo tipoParaDeletar = new TipoAtivo();
        tipoParaDeletar.setNome("Tipo a ser Excluído");
        tipoParaDeletar.setCategoriaContabil("DEL-01");
        tipoAtivoRepository.save(tipoParaDeletar);

        mockMvc.perform(delete("/tipos-ativo/{id}", tipoParaDeletar.getId()))
                .andExpect(status().isNoContent());

        assertEquals(3, tipoAtivoRepository.count());
    }
}
