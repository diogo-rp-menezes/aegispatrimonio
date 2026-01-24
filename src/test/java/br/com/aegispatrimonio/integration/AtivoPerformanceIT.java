package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.AtivoService;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
public class AtivoPerformanceIT extends BaseIT {

    @Autowired private AtivoService ativoService;
    @Autowired private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private DepartamentoRepository departamentoRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;

    private Long filialId;
    private Long tipoAtivoId;
    private Usuario adminUser;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).execute(status -> {
            ativoRepository.deleteAll();
            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            tipoAtivoRepository.deleteAll();
            fornecedorRepository.deleteAll();
            roleRepository.deleteAll();
            permissionRepository.deleteAll();
            entityManager.flush();
            entityManager.clear();

            // Setup Data
            Filial filial = new Filial();
            filial.setNome("Filial Teste");
            filial.setCodigo("F001");
            filial.setCnpj("00.000.000/0001-00");
            filial.setStatus(Status.ATIVO);
            filial = filialRepository.save(filial);
            filialId = filial.getId();

            TipoAtivo tipoAtivo = new TipoAtivo();
            tipoAtivo.setNome("Notebook");
            tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
            tipoAtivo = tipoAtivoRepository.save(tipoAtivo);
            tipoAtivoId = tipoAtivo.getId();

            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome("Fornecedor Teste");
            fornecedor.setCnpj("11.111.111/0001-11");
            fornecedor = fornecedorRepository.save(fornecedor);

            Departamento dept = new Departamento();
            dept.setNome("TI");
            dept.setFilial(filial);
            dept.setStatus(Status.ATIVO);
            dept = departamentoRepository.save(dept);

            // Create Admin User
            Role roleAdmin = new Role();
            roleAdmin.setName("ROLE_ADMIN");
            roleAdmin.setPermissions(Collections.emptySet());
            roleRepository.save(roleAdmin);

            Funcionario adminFunc = new Funcionario();
            adminFunc.setNome("Admin User");
            adminFunc.setDepartamento(dept);
            adminFunc.setFiliais(Set.of(filial));
            adminFunc.setMatricula("ADMIN001");
            adminFunc.setCargo("Administrator");
            adminFunc.setStatus(Status.ATIVO);
            funcionarioRepository.save(adminFunc);

            adminUser = new Usuario();
            adminUser.setEmail("admin@test.com");
            adminUser.setPassword("pass");
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setRoles(Set.of(roleAdmin));
            adminUser.setFuncionario(adminFunc);
            usuarioRepository.save(adminUser);

            // Create 5 Ativos
            for (int i = 0; i < 5; i++) {
                Ativo ativo = new Ativo();
                ativo.setNome("Ativo " + i);
                ativo.setNumeroPatrimonio("PAT-" + i);
                ativo.setFilial(filial);
                ativo.setTipoAtivo(tipoAtivo);
                ativo.setFornecedor(fornecedor);
                ativo.setValorAquisicao(BigDecimal.valueOf(1000 + i));
                ativo.setDataAquisicao(LocalDate.now());
                ativo.setStatus(StatusAtivo.ATIVO);
                ativo.setFuncionarioResponsavel(adminFunc); // Add relationship to verify join
                ativoRepository.save(ativo);
            }

            entityManager.flush();
            entityManager.clear();
            return null;
        });
    }

    @Test
    void verifyNPlusOneProblem() {
        // Authenticate as Admin
        CustomUserDetails principal = new CustomUserDetails(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Run the method under test
        // Passing a filter (filialId) to force the usage of 'findByFilters' instead of 'findAllWithDetails'
        // OR simply rely on the fact that we have 5 items and check pagination.
        // The service uses findByFilters if isFuzzySearch is false.

        Pageable pageable = PageRequest.of(0, 10);
        ativoService.listarTodos(pageable, filialId, null, null, null);

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("Query Count: " + queryCount);

        // With Optimization, we expect:
        // 1 count query (since it returns Page)
        // 1 select query (with all joins)
        // Total = 2 queries.

        assertThat(queryCount).as("Should have efficient queries (Count + Select)").isLessThanOrEqualTo(2);
    }
}
