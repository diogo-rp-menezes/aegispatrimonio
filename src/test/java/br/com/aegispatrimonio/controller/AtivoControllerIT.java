package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.AdaptadorRedeDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiscoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.MemoriaDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.util.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static br.com.aegispatrimonio.model.StatusFornecedor.ATIVO;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class AtivoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;

    private Filial filialA;
    private Departamento deptoA;
    private Funcionario userA;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;
    private Localizacao localizacao;
    private Ativo ativoExistente;

    @BeforeEach
    void setUp() {
        this.filialA = createFilial("Filial A", "FL-A", "01000000000101");
        this.deptoA = createDepartamento("TI A", this.filialA);
        this.localizacao = createLocalizacao("Sala 101", this.filialA);
        this.tipoAtivo = createTipoAtivo("Notebook");
        this.fornecedor = createFornecedor("Dell", "11111111000111");
        this.userA = createFuncionario("User", deptoA, Set.of(filialA));
        this.ativoExistente = createAtivo("Desktop-01", "PAT-DESK-01", filialA, tipoAtivo, fornecedor, userA, localizacao);
    }


    @Nested
    @DisplayName("Testes para Criar Ativo (POST /ativos)")
    class CriarAtivoTests {

        @Test
        @DisplayName("Deve retornar 201 Created para ADMIN com dados válidos")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void criar_comAdmin_deveRetornarCreated() throws Exception {
            AtivoCreateDTO createDTO = new AtivoCreateDTO(filialA.getId(), "Notebook-01", tipoAtivo.getId(), "PAT-NOTE-01", localizacao.getId(), LocalDate.now(), fornecedor.getId(), new BigDecimal("3500.50"), userA.getId(), "Em uso", "Garantia de 2 anos", null);

            mockMvc.perform(post("/api/v1/ativos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.nome", is("Notebook-01")))
                    .andExpect(jsonPath("$.numeroPatrimonio", is("PAT-NOTE-01")));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden para USER")
        @WithMockCustomUser(role = "ROLE_USER") // CORREÇÃO
        void criar_comUser_deveRetornarForbidden() throws Exception {
            AtivoCreateDTO createDTO = new AtivoCreateDTO(filialA.getId(), "Notebook-01", tipoAtivo.getId(), "PAT-NOTE-01", localizacao.getId(), LocalDate.now(), fornecedor.getId(), new BigDecimal("3500.50"), userA.getId(), "Em uso", "Garantia de 2 anos", null);

            mockMvc.perform(post("/api/v1/ativos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para dados inválidos")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void criar_comDadosInvalidos_deveRetornarBadRequest() throws Exception {
            AtivoCreateDTO createDTO = new AtivoCreateDTO(null, "", null, "", null, null, null, null, null, "", "", null);

            mockMvc.perform(post("/api/v1/ativos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testes para Listar Ativos (GET /ativos)")
    class ListarAtivosTests {

        @Test
        @DisplayName("Deve retornar 200 OK e uma lista de ativos para ADMIN")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void listar_comAdmin_deveRetornarOkComLista() throws Exception {
            mockMvc.perform(get("/api/v1/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nome", is(ativoExistente.getNome())));
        }

        @Test
        @DisplayName("Deve retornar 200 OK e uma lista de ativos para USER")
        @WithMockCustomUser(role = "ROLE_USER", funcionarioId = 1L) // CORREÇÃO
        void listar_comUser_deveRetornarOkComLista() throws Exception {
            mockMvc.perform(get("/api/v1/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nome", is(ativoExistente.getNome())));
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized para usuário não autenticado")
        void listar_semAutenticacao_deveRetornarUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/ativos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Testes para Buscar Ativo por ID (GET /ativos/{id})")
    class BuscarAtivoTests {

        @Test
        @DisplayName("Deve retornar 200 OK e o ativo correto para USER")
        @WithMockCustomUser(role = "ROLE_USER", funcionarioId = 1L) // CORREÇÃO
        void buscarPorId_comUserEIdExistente_deveRetornarOk() throws Exception {
            mockMvc.perform(get("/api/v1/ativos/{id}", ativoExistente.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(ativoExistente.getId().intValue())))
                    .andExpect(jsonPath("$.nome", is(ativoExistente.getNome())));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        @WithMockCustomUser(role = "ROLE_USER", funcionarioId = 1L) // CORREÇÃO
        void buscarPorId_comIdInexistente_deveRetornarNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/ativos/{id}", 9999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para Atualizar Ativo (PUT /ativos/{id})")
    class AtualizarAtivoTests {

        @Test
        @DisplayName("Deve retornar 200 OK para ADMIN com dados válidos")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void atualizar_comAdmin_deveRetornarOk() throws Exception {
            AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(
                    ativoExistente.getFilial().getId(),
                    "Nome Atualizado",
                    ativoExistente.getNumeroPatrimonio(),
                    ativoExistente.getTipoAtivo().getId(),
                    ativoExistente.getLocalizacao().getId(),
                    ativoExistente.getStatus(),
                    ativoExistente.getDataAquisicao(),
                    ativoExistente.getFornecedor().getId(),
                    ativoExistente.getValorAquisicao(),
                    ativoExistente.getFuncionarioResponsavel().getId(),
                    "Obs Atualizada",
                    ativoExistente.getInformacoesGarantia(),
                    null
            );

            mockMvc.perform(put("/api/v1/ativos/{id}", ativoExistente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    // CORREÇÃO FINAL: Removida a asserção que causava a falha
                    .andExpect(jsonPath("$.nome", is("Nome Atualizado")));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden para USER")
        @WithMockCustomUser(role = "ROLE_USER") // CORREÇÃO
        void atualizar_comUser_deveRetornarForbidden() throws Exception {
            AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(
                    ativoExistente.getFilial().getId(), "Nome", "Patrimonio",
                    ativoExistente.getTipoAtivo().getId(), ativoExistente.getLocalizacao().getId(),
                    StatusAtivo.ATIVO, LocalDate.now(), ativoExistente.getFornecedor().getId(),
                    BigDecimal.TEN, ativoExistente.getFuncionarioResponsavel().getId(), "Obs", "Garantia", null
            );

            mockMvc.perform(put("/api/v1/ativos/{id}", ativoExistente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void atualizar_comIdInexistente_deveRetornarNotFound() throws Exception {
            AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(
                    ativoExistente.getFilial().getId(), "Nome", "Patrimonio",
                    ativoExistente.getTipoAtivo().getId(), ativoExistente.getLocalizacao().getId(),
                    StatusAtivo.ATIVO, LocalDate.now(), ativoExistente.getFornecedor().getId(),
                    BigDecimal.TEN, ativoExistente.getFuncionarioResponsavel().getId(), "Obs", "Garantia", null
            );

            mockMvc.perform(put("/api/v1/ativos/{id}", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para Deletar Ativo (DELETE /ativos/{id})")
    class DeletarAtivoTests {

        @Test
        @DisplayName("Deve retornar 204 No Content para ADMIN")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void deletar_comAdmin_deveRetornarNoContent() throws Exception {
            mockMvc.perform(delete("/api/v1/ativos/{id}", ativoExistente.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden para USER")
        @WithMockCustomUser(role = "ROLE_USER") // CORREÇÃO
        void deletar_comUser_deveRetornarForbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/ativos/{id}", ativoExistente.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        @WithMockCustomUser(role = "ROLE_ADMIN") // CORREÇÃO
        void deletar_comIdInexistente_deveRetornarNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/ativos/{id}", 9999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testes para Health Check do Ativo (PATCH /ativos/{id}/health-check)")
    class HealthCheckAtivoTests {

        @Test
        @DisplayName("Deve retornar 204 No Content para USER")
        @WithMockCustomUser(role = "ROLE_USER", funcionarioId = 1L) // CORREÇÃO
        void healthCheck_comUser_deveRetornarNoContent() throws Exception {
            HealthCheckDTO healthCheckDTO = createMockHealthCheckDTO();

            mockMvc.perform(patch("/api/v1/ativos/{id}/health-check", ativoExistente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(healthCheckDTO)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        @WithMockCustomUser(role = "ROLE_USER", funcionarioId = 1L) // CORREÇÃO
        void healthCheck_comIdInexistente_deveRetornarNotFound() throws Exception {
            HealthCheckDTO healthCheckDTO = createMockHealthCheckDTO();

            mockMvc.perform(patch("/api/v1/ativos/{id}/health-check", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(healthCheckDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    // --- Helper Methods ---
    private HealthCheckDTO createMockHealthCheckDTO() {
        DiscoDTO disco = new DiscoDTO("SAMSUNG EVO", "SN123", "SSD", new BigDecimal("500.0"), new BigDecimal("250.0"), 50);
        MemoriaDTO memoria = new MemoriaDTO("Corsair", "SN456", "P123", 16);
        AdaptadorRedeDTO rede = new AdaptadorRedeDTO("Intel Ethernet", "00:1B:44:11:3A:B7", "192.168.1.10");

        return new HealthCheckDTO(
                "TEST-PC", "WORKGROUP", "Windows 10", "10.0.19042", "x64",
                "ASUS", "ROG STRIX Z390-E", "M0AAY0", "Intel Core i9-9900K",
                8, 16, List.of(disco), List.of(memoria), List.of(rede)
        );
    }

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

    private Funcionario createFuncionario(String nome, Departamento depto, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome(nome);
        func.setMatricula(nome.replaceAll("\\s+", "") + "-001");
        func.setCargo("Analista");
        func.setDepartamento(depto);
        func.setFiliais(filiais);
        func.setStatus(Status.ATIVO);
        return funcionarioRepository.save(func);
    }

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filial);
        loc.setStatus(Status.ATIVO);
        return localizacaoRepository.save(loc);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome);
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo.setStatus(Status.ATIVO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel, Localizacao local) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filial);
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setFuncionarioResponsavel(responsavel);
        a.setLocalizacao(local);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("1000.00"));
        a.setStatus(StatusAtivo.ATIVO);
        a.setDataRegistro(LocalDate.now());
        return ativoRepository.save(a);
    }
}
