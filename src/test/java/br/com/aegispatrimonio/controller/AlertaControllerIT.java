package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static br.com.aegispatrimonio.model.StatusFornecedor.ATIVO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc
@Transactional
class AlertaControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AlertaRepository alertaRepository;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private LocalizacaoRepository localizacaoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;

    private Ativo ativoExistente;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        Filial filial = createFilial("Filial Alert", "FL-AL", "03000000000103");
        Departamento depto = createDepartamento("TI", filial);
        Localizacao loc = createLocalizacao("Lab", filial);
        TipoAtivo tipo = createTipoAtivo("Server");
        Fornecedor forn = createFornecedor("IBM", "22222222000122");
        Funcionario func = createFuncionario("Admin Alert", depto, Set.of(filial));

        this.ativoExistente = createAtivo("Server-01", "PAT-SRV-01", filial, tipo, forn, func, loc);
        this.usuarioAdmin = createUsuario(null, "admin-alert@example.com", "ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListAlerts() throws Exception {
        mockLogin(usuarioAdmin);

        Alerta alerta = new Alerta();
        alerta.setAtivo(ativoExistente);
        alerta.setTipo(TipoAlerta.INFO);
        alerta.setTitulo("Test Alert");
        alerta.setMensagem("Msg");
        alertaRepository.save(alerta);

        mockMvc.perform(get("/api/v1/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Test Alert"));
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        mockLogin(usuarioAdmin);

        Alerta alerta = new Alerta();
        alerta.setAtivo(ativoExistente);
        alerta.setTipo(TipoAlerta.INFO);
        alerta.setTitulo("Test Alert Read");
        alerta.setMensagem("Msg");
        alerta.setLido(false);
        alerta = alertaRepository.save(alerta);

        mockMvc.perform(put("/api/v1/alertas/" + alerta.getId() + "/ler"))
                .andExpect(status().isNoContent());

        Alerta updated = alertaRepository.findById(alerta.getId()).orElseThrow();
        assert updated.isLido();
    }

    // --- Helpers (Simplified) ---
    private Usuario createUsuario(Funcionario funcionario, String email, String roleName) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword("password");
        u.setRole(roleName);
        u.setFuncionario(funcionario);
        u.setStatus(Status.ATIVO);

        Role rbacRole = roleRepository.findByName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setName(roleName);
            return roleRepository.save(r);
        });
        u.setRoles(Set.of(rbacRole));

        return usuarioRepository.save(u);
    }

    private void mockLogin(Usuario usuario) {
        CustomUserDetails principal = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
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
