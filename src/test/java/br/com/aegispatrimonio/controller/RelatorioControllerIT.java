package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class RelatorioControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    private Ativo ativo;
    private Usuario usuarioUser;

    @BeforeEach
    void setUp() {
        Filial filial = createFilial("Filial Relatorio", "FL-REL", "99000000000199");
        Departamento depto = createDepartamento("TI Relatorio", filial);
        TipoAtivo tipo = createTipoAtivo("Notebook Relatorio");
        Fornecedor fornecedor = createFornecedor("Fornecedor Relatorio", "99999999000199");
        Funcionario userA = createFuncionario("User Relatorio", depto, Set.of(filial));

        this.ativo = createAtivo("Laptop Relatorio", "PAT-REL-01", filial, tipo, fornecedor, userA);
        this.usuarioUser = createUsuario(userA, "relatorio@example.com", "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve gerar QR Code para ativo existente e usuário autorizado")
    void gerarQrCode_deveRetornarImagem() throws Exception {
        mockLogin(usuarioUser);

        mockMvc.perform(get("/api/v1/ativos/{id}/qrcode", ativo.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden para ID inexistente (Security First)")
    void gerarQrCode_comIdInexistente_deveRetornarForbidden() throws Exception {
        mockLogin(usuarioUser);

        mockMvc.perform(get("/api/v1/ativos/{id}/qrcode", 9999L))
                .andExpect(status().isForbidden());
    }

    // --- Helper Methods ---

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

        // Grant READ permission
        if ("ROLE_USER".equals(roleName)) {
             Permission pRead = permissionRepository.findByResourceAndAction("ATIVO", "READ")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "ATIVO", "READ", "Read Ativo", "filialId")));

             if (rbacRole.getPermissions() == null) {
                 rbacRole.setPermissions(new java.util.HashSet<>());
             }
             rbacRole.getPermissions().add(pRead);
             roleRepository.save(rbacRole);
        }

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

    private Ativo createAtivo(String nome, String patrimonio, Filial filial, TipoAtivo tipo, Fornecedor fornecedor, Funcionario responsavel) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio(patrimonio);
        a.setFilial(filial);
        a.setTipoAtivo(tipo);
        a.setFornecedor(fornecedor);
        a.setFuncionarioResponsavel(responsavel);
        a.setDataAquisicao(LocalDate.now());
        a.setValorAquisicao(new BigDecimal("1000.00"));
        a.setStatus(StatusAtivo.ATIVO);
        a.setDataRegistro(LocalDate.now());
        return ativoRepository.save(a);
    }
}
