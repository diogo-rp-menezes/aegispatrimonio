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
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class MovimentacaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Ativo ativo;
    private Funcionario userOrigem;
    private Funcionario userDestino;
    private Localizacao localOrigem;
    private Localizacao localDestino;

    @BeforeEach
    void setUp() {
        // Limpeza
        movimentacaoRepository.deleteAll();
        ativoRepository.deleteAll();
        usuarioRepository.deleteAll();
        funcionarioRepository.deleteAll();
        departamentoRepository.deleteAll();
        localizacaoRepository.deleteAll();
        filialRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        fornecedorRepository.deleteAll();

        // Setup de Dados
        Filial filial = createFilial("Matriz", "MTRZ", "00.000.000/0001-00");
        Departamento depto = createDepartamento("TI", filial);
        this.userOrigem = createFuncionarioAndUsuario("User Origem", "origem@aegis.com", "ROLE_USER", depto, Set.of(filial));
        this.userDestino = createFuncionarioAndUsuario("User Destino", "destino@aegis.com", "ROLE_USER", depto, Set.of(filial));
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin", "admin@aegis.com", "ROLE_ADMIN", depto, Set.of(filial));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        this.localOrigem = createLocalizacao("Sala 101", filial);
        this.localDestino = createLocalizacao("Sala 102", filial);

        TipoAtivo tipoAtivo = createTipoAtivo("Notebook");
        Fornecedor fornecedor = createFornecedor("Dell", "11.111.111/0001-11");
        this.ativo = createAtivo("Notebook-01", "PAT-001", filial, tipoAtivo, fornecedor, userOrigem, localOrigem);
    }

    @Test
    @DisplayName("Deve criar e efetivar uma movimentação com sucesso")
    void cicloDeVidaMovimentacao_deveFuncionarCorretamente() throws Exception {
        // 1. Criar Movimentação
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(ativo.getId(), localOrigem.getId(), localDestino.getId(),
                userOrigem.getId(), userDestino.getId(), LocalDate.now(), "Movimentação de teste", "Obs");

        String responseString = mockMvc.perform(post("/movimentacoes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andReturn().getResponse().getContentAsString();

        Long movimentacaoId = objectMapper.readTree(responseString).get("id").asLong();

        // 2. Efetivar Movimentação
        mockMvc.perform(post("/movimentacoes/efetivar/{id}", movimentacaoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EFETIVADA")));

        // 3. Verificar se o Ativo foi atualizado
        Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow();
        assertEquals(localDestino.getId(), ativoAtualizado.getLocalizacao().getId());
        assertEquals(userDestino.getId(), ativoAtualizado.getFuncionarioResponsavel().getId());
    }

    // --- Helper Methods ---

    private Filial createFilial(String nome, String codigo, String cnpj) {
        Filial f = new Filial();
        f.setNome(nome); f.setCodigo(codigo); f.setCnpj(cnpj); f.setTipo(TipoFilial.MATRIZ); f.setStatus(Status.ATIVO);
        return filialRepository.save(f);
    }

    private Departamento createDepartamento(String nome, Filial filial) {
        Departamento d = new Departamento();
        d.setNome(nome); d.setFilial(filial);
        return departamentoRepository.save(d);
    }

    private Funcionario createFuncionarioAndUsuario(String nome, String email, String role, Departamento depto, Set<Filial> filiais) {
        Funcionario func = new Funcionario();
        func.setNome(nome); func.setMatricula(nome.replaceAll("\\s+", "") + "-001"); func.setCargo("Analista");
        func.setDepartamento(depto); func.setFiliais(filiais); func.setStatus(Status.ATIVO);
        Usuario user = new Usuario();
        user.setEmail(email); user.setPassword(passwordEncoder.encode("password")); user.setRole(role);
        user.setStatus(Status.ATIVO); user.setFuncionario(func); func.setUsuario(user);
        return funcionarioRepository.save(func);
    }

    private Localizacao createLocalizacao(String nome, Filial filial) {
        Localizacao loc = new Localizacao();
        loc.setNome(nome); loc.setFilial(filial); loc.setStatus(Status.ATIVO);
        return localizacaoRepository.save(loc);
    }

    private TipoAtivo createTipoAtivo(String nome) {
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome(nome); tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO); tipo.setStatus(Status.ATIVO);
        return tipoAtivoRepository.save(tipo);
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome); f.setCnpj(cnpj); f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel, Localizacao local) {
        Ativo a = new Ativo();
        a.setNome(nome); a.setNumeroPatrimonio(patrimonio); a.setFilial(filial); a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor); a.setFuncionarioResponsavel(responsavel); a.setLocalizacao(local);
        a.setDataAquisicao(LocalDate.now()); a.setValorAquisicao(new BigDecimal("3000.00"));
        a.setStatus(StatusAtivo.ATIVO); a.setDataRegistro(LocalDate.now());
        return ativoRepository.save(a);
    }
}
