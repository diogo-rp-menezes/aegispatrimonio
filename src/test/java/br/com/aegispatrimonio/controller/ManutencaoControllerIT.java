package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class ManutencaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ManutencaoRepository manutencaoRepository;
    @Autowired
    private AtivoRepository ativoRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private FilialRepository filialRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Ativo ativo;
    private Pessoa solicitante;
    private Pessoa tecnico;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        Filial filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00");
        Departamento depto = createDepartamento("TI", filial);
        this.solicitante = createUser("solicitante", "solicitante@aegis.com", "ROLE_USER", filial, depto);
        this.tecnico = createUser("tecnico", "tecnico@aegis.com", "ROLE_USER", filial, depto);
        Pessoa admin = createUser("admin", "admin@aegis.com", "ROLE_ADMIN", filial, depto);
        this.adminToken = jwtService.generateToken(new CustomUserDetails(admin));

        TipoAtivo tipoAtivo = createTipoAtivo("Notebook");
        this.fornecedor = createFornecedor("Dell", "11.111.111/0001-11");
        this.ativo = createAtivo("Notebook-01", "PAT-001", filial, tipoAtivo, this.fornecedor, this.solicitante);
    }

    @Test
    @DisplayName("Deve criar uma manutenção e retornar 201 Created")
    void criarManutencao_comDadosValidos_deveRetornarCreated() throws Exception {
        ManutencaoRequestDTO requestDTO = new ManutencaoRequestDTO(
                ativo.getId(), TipoManutencao.CORRETIVA, solicitante.getId(), null, null, "Tela quebrada", null, null, null, null);

        mockMvc.perform(post("/manutencoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricaoProblema", is("Tela quebrada")))
                .andExpect(jsonPath("$.status", is(StatusManutencao.SOLICITADA.toString())));
    }

    @Test
    @DisplayName("Deve seguir o fluxo completo de uma manutenção (Criar, Aprovar, Iniciar, Concluir)")
    void fluxoCompletoManutencao_deveFuncionarCorretamente() throws Exception {
        // 1. Criar
        ManutencaoRequestDTO requestDTO = new ManutencaoRequestDTO(
                ativo.getId(), TipoManutencao.CORRETIVA, solicitante.getId(), null, null, "Não liga", null, null, null, null);
        MvcResult result = mockMvc.perform(post("/manutencoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()).andReturn();
        ManutencaoResponseDTO manutencao = objectMapper.readValue(result.getResponse().getContentAsString(), ManutencaoResponseDTO.class);

        // 2. Aprovar
        mockMvc.perform(post("/manutencoes/aprovar/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusManutencao.APROVADA.toString())));

        // 3. Iniciar
        ManutencaoInicioDTO inicioDTO = new ManutencaoInicioDTO();
        inicioDTO.setTecnicoId(tecnico.getId());
        mockMvc.perform(post("/manutencoes/iniciar/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusManutencao.EM_ANDAMENTO.toString())))
                .andExpect(jsonPath("$.tecnicoResponsavelId", is(tecnico.getId().intValue())));

        // 4. Concluir
        ManutencaoConclusaoDTO conclusaoDTO = new ManutencaoConclusaoDTO();
        conclusaoDTO.setDescricaoServico("Placa-mãe substituída.");
        conclusaoDTO.setCustoReal(BigDecimal.valueOf(800.50));
        conclusaoDTO.setTempoExecucao(120);
        mockMvc.perform(post("/manutencoes/concluir/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conclusaoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusManutencao.CONCLUIDA.toString())))
                .andExpect(jsonPath("$.custoReal", is(800.50)));
    }

    @Test
    @DisplayName("Deve cancelar uma manutenção e retornar 200 OK")
    void cancelarManutencao_comJustificativa_deveRetornarOk() throws Exception {
        Manutencao manutencao = createManutencao(ativo, solicitante, StatusManutencao.SOLICITADA);
        ManutencaoCancelDTO cancelDTO = new ManutencaoCancelDTO();
        cancelDTO.setMotivo("Problema resolvido pelo usuário.");

        mockMvc.perform(post("/manutencoes/cancelar/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusManutencao.CANCELADA.toString())));
    }

    @Test
    @DisplayName("Deve deletar uma manutenção e retornar 204 No Content")
    void deletarManutencao_comIdExistente_deveRetornarNoContent() throws Exception {
        Manutencao manutencao = createManutencao(ativo, solicitante, StatusManutencao.SOLICITADA);

        mockMvc.perform(delete("/manutencoes/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/manutencoes/{id}", manutencao.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---
    private Manutencao createManutencao(Ativo ativo, Pessoa solicitante, StatusManutencao status) {
        Manutencao manutencao = new Manutencao();
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
        manutencao.setDataSolicitacao(LocalDate.now());
        manutencao.setDescricaoProblema("Teste de problema");
        manutencao.setTipo(TipoManutencao.CORRETIVA);
        manutencao.setStatus(status);
        return manutencaoRepository.save(manutencao);
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setCodigo(codigo);
        filial.setTipo(TipoFilial.MATRIZ);
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

    private Pessoa createUser(String nome, String email, String role, Filial filial, Departamento depto) {
        Pessoa user = new Pessoa();
        user.setNome(nome);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setFilial(filial);
        user.setDepartamento(depto);
        user.setStatus(Status.ATIVO);
        user.setCargo("Analista");
        return pessoaRepository.save(user);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome);
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO); // Adiciona um valor padrão
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Pessoa responsavel) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filial);
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setPessoaResponsavel(responsavel);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("2500.00"));
        a.setStatus(StatusAtivo.ATIVO);
        return ativoRepository.save(a);
    }
}
