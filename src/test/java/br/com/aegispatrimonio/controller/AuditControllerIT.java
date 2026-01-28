package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static br.com.aegispatrimonio.model.StatusFornecedor.ATIVO;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuditControllerIT extends BaseIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private DepartamentoRepository departamentoRepository;

    private Usuario adminUser;
    private Long ativoId;
    private Filial filial;

    @BeforeEach
    void setUp() {
        // Setup Admin User and Filial in a transaction
        transactionTemplate.execute(status -> {
            filial = new Filial();
            filial.setNome("Filial Audit");
            filial.setCodigo("FL-AUD");
            filial.setCnpj("88888888000188");
            filial.setTipo(TipoFilial.FILIAL);
            filial.setStatus(Status.ATIVO);
            filialRepository.save(filial);

            Departamento depto = new Departamento();
            depto.setNome("Audit Dept");
            depto.setFilial(filial);
            departamentoRepository.save(depto);

            TipoAtivo tipo = new TipoAtivo();
            tipo.setNome("Laptop");
            tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
            tipoAtivoRepository.save(tipo);

            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome("Audit Corp");
            fornecedor.setCnpj("77777777000177");
            fornecedor.setStatus(ATIVO);
            fornecedorRepository.save(fornecedor);

            // Create Funcionario for Admin (required by TenantAccessFilter)
            Funcionario func = new Funcionario();
            func.setNome("Admin Auditor");
            func.setMatricula("AUD-001");
            func.setCargo("Auditor");
            func.setDepartamento(depto);
            func.setFiliais(Set.of(filial));
            func.setStatus(Status.ATIVO);
            funcionarioRepository.save(func);

            // Create Admin User
            adminUser = new Usuario();
            adminUser.setEmail("audit_admin@aegis.com");
            adminUser.setPassword("123456");
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setStatus(Status.ATIVO);
            adminUser.setFuncionario(func);

            // Ensure Role exists
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_ADMIN");
                return roleRepository.save(r);
            });
            adminUser.setRoles(Set.of(adminRole));

            usuarioRepository.save(adminUser);

            return null;
        });

        // Mock Login
        mockLogin(adminUser);
        TenantContext.setFilialId(filial.getId());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void testGetAtivoHistory() throws Exception {
        // 1. Create Ativo (Revision 1)
        transactionTemplate.execute(status -> {
            Ativo ativo = new Ativo();
            ativo.setNome("Ativo Original");
            ativo.setNumeroPatrimonio("AUD-001");
            ativo.setFilial(filial);
            ativo.setTipoAtivo(tipoAtivoRepository.findAll().get(0));
            ativo.setFornecedor(fornecedorRepository.findAll().get(0));
            ativo.setDataAquisicao(LocalDate.now());
            ativo.setValorAquisicao(new BigDecimal("2000.00"));
            ativo.setDataRegistro(LocalDate.now());
            ativo.setStatus(StatusAtivo.ATIVO);

            ativo = ativoRepository.save(ativo);
            ativoId = ativo.getId();
            return null;
        });

        // 2. Update Ativo (Revision 2)
        transactionTemplate.execute(status -> {
            Ativo ativo = ativoRepository.findById(ativoId).orElseThrow();
            ativo.setNome("Ativo Modificado");
            ativoRepository.save(ativo);
            return null;
        });

        // 3. Call API
        mockMvc.perform(get("/api/v1/audit/ativos/{id}", ativoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].entity.nome", is("Ativo Original")))
                .andExpect(jsonPath("$[0].revision.revisionType", is("ADD")))
                .andExpect(jsonPath("$[1].entity.nome", is("Ativo Modificado")))
                .andExpect(jsonPath("$[1].revision.revisionType", is("MOD")));
    }

    private void mockLogin(Usuario usuario) {
        CustomUserDetails principal = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
