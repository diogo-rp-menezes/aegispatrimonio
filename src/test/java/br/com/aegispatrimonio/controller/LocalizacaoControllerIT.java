package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class LocalizacaoControllerIT {

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

    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private AtivoRepository ativoRepository;

    private Filial filialSP;
    private Localizacao andar1SP;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        filialRepository.deleteAll();

        filialSP = new Filial();
        filialSP.setNome("Matriz São Paulo");
        filialSP.setCodigo("SP-01");
        filialRepository.save(filialSP);

        Localizacao predioSP = new Localizacao();
        predioSP.setNome("Prédio Principal");
        predioSP.setFilial(filialSP);
        localizacaoRepository.save(predioSP);

        andar1SP = new Localizacao();
        andar1SP.setNome("1º Andar");
        andar1SP.setFilial(filialSP);
        andar1SP.setLocalizacaoPai(predioSP);
        localizacaoRepository.save(andar1SP);

        Localizacao sala101 = new Localizacao();
        sala101.setNome("Sala 101");
        sala101.setFilial(filialSP);
        sala101.setLocalizacaoPai(andar1SP);
        localizacaoRepository.save(sala101);
    }

    @Test
    @DisplayName("Deve criar uma nova localização quando dados válidos são enviados")
    void criar_quandoDadosValidos_deveRetornar201() throws Exception {
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setNome("Sala de Reuniões");
        request.setFilialId(filialSP.getId());
        request.setLocalizacaoPaiId(andar1SP.getId());

        mockMvc.perform(post("/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Sala de Reuniões")));

        assertEquals(4, localizacaoRepository.count());
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar criar localização com filialId inexistente")
    void criar_quandoFilialNaoExiste_deveRetornar404() throws Exception {
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setNome("Localização Fantasma");
        request.setFilialId(9999L);

        mockMvc.perform(post("/localizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todas as 3 localizações quando nenhum filtro é aplicado")
    void listar_semFiltros_deveRetornarTodasAsLocalizacoes() throws Exception {
        mockMvc.perform(get("/localizacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Deve listar 1 localização ao filtrar por nome 'Sala 101'")
    void listar_filtrandoPorNome_deveRetornarLocalizacaoCorrespondente() throws Exception {
        mockMvc.perform(get("/localizacoes").param("nome", "Sala 101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Deve excluir uma localização e retornar 204 No Content")
    void deletar_quandoLocalizacaoExiste_deveRetornar204() throws Exception {
        Localizacao paraDeletar = new Localizacao();
        paraDeletar.setNome("Para Deletar");
        paraDeletar.setFilial(filialSP);
        localizacaoRepository.save(paraDeletar);

        long countAntes = localizacaoRepository.count();

        mockMvc.perform(delete("/localizacoes/{id}", paraDeletar.getId()))
                .andExpect(status().isNoContent());

        assertEquals(countAntes - 1, localizacaoRepository.count());
    }
}
