// language: java
// File: src/test/java/br/com/aegispatrimonio/controller/DepreciacaoControllerIT.java
package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class DepreciacaoControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    // Repositories
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Ativo ativo;

    @BeforeEach
    void setUp() {
        Filial filial = createFilial("Sede Teste", "SEDE-TESTE", "99.999.999/0001-99");
        Departamento depto = createDepartamento("TI Depreciação", filial);
        Funcionario adminFunc = createFuncionarioAndUsuario("Admin Depreciação", "admin.dep@aegis.com", "ROLE_ADMIN", depto, Set.of(filial));
        this.adminToken = jwtService.generateToken(new CustomUserDetails(adminFunc.getUsuario()));

        TipoAtivo tipoAtivo = createTipoAtivo("Servidor");
        Fornecedor fornecedor = createFornecedor("HP", "88.888.888/0001-88");
        Localizacao local = createLocalizacao("Datacenter", filial);
        this.ativo = createAtivo("Servidor-01", "SRV-001", filial, tipoAtivo, fornecedor, adminFunc, local, new BigDecimal("12000.00"), new BigDecimal("2000.00"), 100);
    }

    @Test
    @DisplayName("Recalcular Ativo: Deve recalcular depreciação e retornar 200 OK")
    void recalcularDepreciacaoAtivo_deveRecalcularEretornarOk() throws Exception {
        mockMvc.perform(post("/depreciacao/recalcular/{ativoId}", ativo.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Recálculo de depreciação do ativo iniciado"));

        // A operação é síncrona, então a verificação pode ser feita imediatamente.
        Ativo ativoAtualizado = ativoRepository.findById(ativo.getId()).orElseThrow();
        // O ativo foi adquirido há 1 ano (12 meses). Depreciação mensal é de 100.
        // A depreciação total recalculada deve ser 12 * 100 = 1200.
        BigDecimal depreciacaoEsperada = new BigDecimal("1200.00");
        assertEquals(0, depreciacaoEsperada.compareTo(ativoAtualizado.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Calcular Mensal: Deve calcular e retornar o valor correto com status 200 OK")
    void calcularDepreciacaoMensal_deveRetornarValorCorreto() throws Exception {
        BigDecimal depreciacaoMensalEsperada = new BigDecimal("100.00"); // (12000 - 2000) / 100

        mockMvc.perform(get("/depreciacao/calcular-mensal/{ativoId}", ativo.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(depreciacaoMensalEsperada.setScale(10, RoundingMode.HALF_UP).toString()));
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
        return tipoAtivoRepository.findByNome(nome).orElseGet(() -> {
            TipoAtivo tipo = new TipoAtivo();
            tipo.setNome(nome); tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO); tipo.setStatus(Status.ATIVO);
            return tipoAtivoRepository.save(tipo);
        });
    }

    private Fornecedor createFornecedor(String nome, String cnpj) {
        Fornecedor f = new Fornecedor();
        f.setNome(nome); f.setCnpj(cnpj); f.setStatus(Status.ATIVO);
        return fornecedorRepository.save(f);
    }

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel, Localizacao local, BigDecimal valorAquisicao, BigDecimal valorResidual, int vidaUtil) {
        Ativo a = new Ativo();
        a.setNome(nome); a.setNumeroPatrimonio(patrimonio); a.setFilial(filial); a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor); a.setFuncionarioResponsavel(responsavel); a.setLocalizacao(local);
        a.setDataAquisicao(LocalDate.now().minusYears(1)); a.setDataInicioDepreciacao(LocalDate.now().minusYears(1));
        a.setValorAquisicao(valorAquisicao); a.setValorResidual(valorResidual); a.setVidaUtilMeses(vidaUtil);
        a.setStatus(StatusAtivo.ATIVO); a.setDataRegistro(LocalDate.now());
        a.setDepreciacaoAcumulada(BigDecimal.ZERO);
        return ativoRepository.save(a);
    }
}
