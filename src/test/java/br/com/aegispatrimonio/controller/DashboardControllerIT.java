package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class DashboardControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    private Usuario admin;
    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setNome("Filial Dashboard");
        filial.setCodigo("DASH01");
        filial.setTipo(TipoFilial.FILIAL);
        filial.setCnpj("00.000.000/0002-00");
        filial.setStatus(Status.ATIVO);
        filial = filialRepository.save(filial);

        // TenantContext must be set because the Service uses it
        TenantContext.setFilialId(filial.getId());

        Departamento depto = new Departamento();
        depto.setNome("TI");
        depto.setFilial(filial);
        depto = departamentoRepository.save(depto);

        Funcionario func = new Funcionario();
        func.setNome("Admin Dashboard");
        func.setMatricula("ADM002");
        func.setCargo("Admin");
        func.setDepartamento(depto);
        func.setFiliais(Set.of(filial));
        func.setStatus(Status.ATIVO);
        func = funcionarioRepository.save(func);

        admin = new Usuario();
        admin.setEmail("dashboard@admin.com");
        admin.setPassword("password");
        admin.setRole("ROLE_ADMIN");
        admin.setFuncionario(func);
        admin.setStatus(Status.ATIVO);

        Role r = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_ADMIN");
            return roleRepository.save(role);
        });
        admin.setRoles(Set.of(r));

        admin = usuarioRepository.save(admin);
    }

    @Test
    void shouldReturnStats() throws Exception {
        mockLogin(admin);

        // Create some assets
        TipoAtivo tipo = new TipoAtivo();
        tipo.setNome("Tipo");
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo = tipoAtivoRepository.save(tipo);

        Fornecedor forn = new Fornecedor();
        forn.setNome("Forn");
        forn.setCnpj("11.111.111/0001-11");
        forn.setStatus(StatusFornecedor.ATIVO);
        forn = fornecedorRepository.save(forn);

        Ativo a1 = new Ativo();
        a1.setNome("Critical Asset");
        a1.setNumeroPatrimonio("P1");
        a1.setFilial(filial);
        a1.setTipoAtivo(tipo);
        a1.setFornecedor(forn);
        a1.setValorAquisicao(java.math.BigDecimal.TEN);
        a1.setDataAquisicao(LocalDate.now());
        a1.setStatus(StatusAtivo.ATIVO);
        a1.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));
        ativoRepository.save(a1);

        Ativo a2 = new Ativo();
        a2.setNome("Safe Asset");
        a2.setNumeroPatrimonio("P2");
        a2.setFilial(filial);
        a2.setTipoAtivo(tipo);
        a2.setFornecedor(forn);
        a2.setValorAquisicao(java.math.BigDecimal.TEN);
        a2.setDataAquisicao(LocalDate.now());
        a2.setStatus(StatusAtivo.ATIVO);
        a2.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(40));
        ativoRepository.save(a2);

        mockMvc.perform(get("/api/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtivos", is(2)))
                .andExpect(jsonPath("$.predicaoCritica", is(1)))
                .andExpect(jsonPath("$.predicaoAlerta", is(0)))
                .andExpect(jsonPath("$.predicaoSegura", is(1)));
    }

    private void mockLogin(Usuario usuario) {
        CustomUserDetails principal = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
