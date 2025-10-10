package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class MovimentacaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MovimentacaoRepository movimentacaoRepository;
    @Autowired
    private AtivoRepository ativoRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private LocalizacaoRepository localizacaoRepository;
    @Autowired
    private FilialRepository filialRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Ativo ativo;
    private Pessoa userOrigem;
    private Pessoa userDestino;
    private Localizacao localOrigem;
    private Localizacao localDestino;

    @BeforeEach
    void setUp() {
        Filial filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00");
        Departamento depto = createDepartamento("TI", filial);
        Pessoa admin = createUser("admin", "admin@aegis.com", "ROLE_ADMIN", filial, depto);
        this.adminToken = jwtService.generateToken(new CustomUserDetails(admin));

        this.userOrigem = createUser("User Origem", "origem@aegis.com", "ROLE_USER", filial, depto);
        this.userDestino = createUser("User Destino", "destino@aegis.com", "ROLE_USER", filial, depto);

        this.localOrigem = createLocalizacao("Sala 101", filial);
        this.localDestino = createLocalizacao("Sala 102", filial);

        TipoAtivo tipoAtivo = createTipoAtivo("Notebook");
        Fornecedor fornecedor = createFornecedor("Dell", "11.111.111/0001-11");
        this.ativo = createAtivo("Notebook-01", "PAT-001", filial, tipoAtivo, fornecedor, userOrigem, localOrigem);
    }

    @Test
    @DisplayName("Deve criar uma movimentação e retornar 201 Created")
    void criar_deveCriarMovimentacao() throws Exception {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(ativo.getId(), localOrigem.getId(), localDestino.getId(),
                userOrigem.getId(), userDestino.getId(), LocalDate.now(), "Movimentação de teste", "Obs");

        mockMvc.perform(post("/movimentacoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativoId", is(ativo.getId().intValue())));
    }

    @Test
    @DisplayName("Deve efetivar uma movimentação e retornar 200 OK")
    void efetivarMovimentacao_deveEfetivar() throws Exception {
        Movimentacao movimentacao = createMovimentacao(StatusMovimentacao.PENDENTE);

        mockMvc.perform(post("/movimentacoes/efetivar/{id}", movimentacao.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusMovimentacao.EFETIVADA.toString())));

        Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow();
        assertEquals(localDestino.getId(), ativoAtualizado.getLocalizacao().getId());
        assertEquals(userDestino.getId(), ativoAtualizado.getPessoaResponsavel().getId());
    }

    @Test
    @DisplayName("Deve cancelar uma movimentação e retornar 200 OK")
    void cancelarMovimentacao_deveCancelar() throws Exception {
        Movimentacao movimentacao = createMovimentacao(StatusMovimentacao.PENDENTE);
        String motivo = "Cancelado via teste";

        mockMvc.perform(post("/movimentacoes/cancelar/{id}", movimentacao.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(motivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(StatusMovimentacao.CANCELADA.toString())));
    }

    @Test
    @DisplayName("Deve listar movimentações por status e retornar 200 OK")
    void listarPorStatus_deveRetornarMovimentacoes() throws Exception {
        createMovimentacao(StatusMovimentacao.PENDENTE);
        createMovimentacao(StatusMovimentacao.EFETIVADA);

        mockMvc.perform(get("/movimentacoes/status/{status}", StatusMovimentacao.PENDENTE)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("PENDENTE")));
    }

    @Test
    @DisplayName("Deve listar movimentações por período e retornar 200 OK")
    void listarPorPeriodo_deveRetornarMovimentacoes() throws Exception {
        createMovimentacao(StatusMovimentacao.PENDENTE);
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/movimentacoes/periodo")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // --- Helper Methods ---

    private Movimentacao createMovimentacao(StatusMovimentacao status) {
        Movimentacao mov = new Movimentacao();
        mov.setAtivo(this.ativo);
        mov.setPessoaOrigem(this.userOrigem);
        mov.setPessoaDestino(this.userDestino);
        mov.setLocalizacaoOrigem(this.localOrigem);
        mov.setLocalizacaoDestino(this.localDestino);
        mov.setDataMovimentacao(LocalDate.now());
        mov.setStatus(status);
        return movimentacaoRepository.save(mov);
    }

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial f = new Filial();
        f.setNome(nome);
        f.setCodigo(codigo);
        f.setTipo(TipoFilial.MATRIZ);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return filialRepository.save(f);
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

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome);
        loc.setFilial(filial);
        return localizacaoRepository.save(loc);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome);
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome);
        f.setCnpj(cnpj);
        f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Pessoa responsavel, Localizacao localizacao) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filial);
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setPessoaResponsavel(responsavel);
        a.setLocalizacao(localizacao);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("3000.00"));
        a.setStatus(StatusAtivo.ATIVO);
        return ativoRepository.save(a);
    }
}
