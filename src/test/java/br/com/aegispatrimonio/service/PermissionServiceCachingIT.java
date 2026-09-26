package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionServiceCachingIT extends BaseIT {

    @Autowired
    private IPermissionService permissionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Authentication auth;
    private Long filialId;

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).execute(status -> {
            usuarioRepository.deleteAll();
            funcionarioRepository.deleteAll();
            departamentoRepository.deleteAll();
            filialRepository.deleteAll();
            roleRepository.deleteAll();
            permissionRepository.deleteAll();

            Filial filial = new Filial();
            filial.setNome("Test Filial");
            filial.setCodigo("TF001");
            filial.setCnpj("00.000.000/0000-00");
            filial.setStatus(Status.ATIVO);
            filial = filialRepository.save(filial);
            this.filialId = filial.getId();

            Departamento dept = new Departamento();
            dept.setNome("Test Dept");
            dept.setFilial(filial);
            dept.setStatus(Status.ATIVO);
            dept = departamentoRepository.save(dept);

            Funcionario func = new Funcionario();
            func.setNome("Test Func");
            func.setMatricula("M001");
            func.setCargo("Tester");
            func.setStatus(Status.ATIVO);
            func.setDepartamento(dept);
            func.setFiliais(Set.of(filial));
            func = funcionarioRepository.save(func);

            Permission p = new Permission();
            p.setResource("ATIVO");
            p.setAction("READ");
            p.setContextKey("filialId");
            p = permissionRepository.save(p);

            Role role = new Role();
            role.setName("ROLE_USER");
            role.setPermissions(Set.of(p));
            role = roleRepository.save(role);

            Usuario user = new Usuario();
            user.setEmail("test@example.com");
            user.setPassword("password");
            user.setRole("ROLE_USER");
            user.setRoles(Set.of(role));
            user.setStatus(Status.ATIVO);
            user.setFuncionario(func);
            user = usuarioRepository.save(user);

            CustomUserDetails userDetails = new CustomUserDetails(user);
            auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return null;
        });
    }

    @Test
    void testPermissionCachingImpact() {
        // Warm up cache
        permissionService.hasPermission(auth, null, "ATIVO", "READ", filialId);

        // Prepare Statistics
        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Second call should be cached
        System.out.println("Starting second call (should be cached)...");
        boolean allowed = permissionService.hasPermission(auth, null, "ATIVO", "READ", filialId);
        assertThat(allowed).isTrue();

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("Query Count for second call: " + queryCount);

        // If caching is working, queryCount should be 0 (or very low if some other things are happening)
        // Since all DB hits are inside @Cacheable methods called via effectiveSelf
        assertThat(queryCount).as("Second call should not hit the database if caching works").isEqualTo(0);
    }
}
