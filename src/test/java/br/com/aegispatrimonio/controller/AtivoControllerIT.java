package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class AtivoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Filial filialA;
    private Pessoa userA;

    @BeforeEach
    void setUp() {
        // Cria um ambiente de teste limpo para cada método de teste
        this.filialA = createFilial("Filial A", "FLA", "00.000.000/0001-00");
        Departamento deptoA = createDepartamento("TI A", this.filialA);
        this.userA = createUser("userA", "userA@aegis.com", "ROLE_USER", this.filialA, deptoA);

        // Gera o token para o usuário de teste
        userToken = jwtService.generateToken(new CustomUserDetails(this.userA));
    }

    @Test
    void listarAtivos_semAutenticacao_deveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/ativos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarAtivos_comAutenticacao_deveRetornarOk() throws Exception {
        mockMvc.perform(get("/api/ativos")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void listarAtivos_quandoUsuarioDeFilial_deveRetornarApenasAtivosDaSuaFilial() throws Exception {
        // Arrange: Cria um cenário com múltiplas filiais e ativos
        Filial filialB = createFilial("Filial B", "FLB", "00.000.000/0002-00");
        TipoAtivo tipo = createTipoAtivo("Desktop");
        Fornecedor fornecedor = createFornecedor("Dell", "11.111.111/0001-11");

        Ativo ativoFilialA = createAtivo("PC-01", "PAT-01", this.filialA, tipo, fornecedor, this.userA);
        createAtivo("PC-02", "PAT-02", filialB, tipo, fornecedor, this.userA); // Ativo em outra filial

        // Act & Assert
        mockMvc.perform(get("/api/ativos")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Espera que a lista tenha exatamente 1 ativo
                .andExpect(jsonPath("$[0].id", is(ativoFilialA.getId().intValue()))); // Espera que seja o ativo da Filial A
    }

    // --- Helper Methods para criação de entidades de teste ---

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
        Ativo ativo = new Ativo();
        ativo.setNome(nome);
        ativo.setNumeroPatrimonio(patrimonio);
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipo);
        ativo.setFornecedor(fornecedor);
        ativo.setPessoaResponsavel(responsavel);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(new BigDecimal("1000.00"));
        ativo.setStatus(StatusAtivo.ATIVO);
        return ativoRepository.save(ativo);
    }
}
