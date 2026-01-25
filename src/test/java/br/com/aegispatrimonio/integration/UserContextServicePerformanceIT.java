package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.CurrentUserProvider;
import br.com.aegispatrimonio.service.UserContextService;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Transactional
public class UserContextServicePerformanceIT extends BaseIT {

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    private Long userId;

    @BeforeEach
    void setUp() {
        // Create Filial
        Filial filial = new Filial();
        filial.setNome("Matriz Teste");
        filial.setCodigo("MTZ001");
        filial.setCnpj("12.345.678/0001-90");
        filial.setTipo(TipoFilial.MATRIZ);
        filial.setStatus(Status.ATIVO);
        filial = filialRepository.save(filial);

        // Create Departamento
        Departamento departamento = new Departamento();
        departamento.setNome("TI");
        departamento.setFilial(filial);
        departamento.setStatus(Status.ATIVO);
        departamento = departamentoRepository.save(departamento);

        // Create Funcionario
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setMatricula("F001");
        funcionario.setCargo("Desenvolvedor");
        funcionario.setDepartamento(departamento);
        funcionario.setStatus(Status.ATIVO);
        funcionario.setFiliais(Set.of(filial));
        funcionario = funcionarioRepository.save(funcionario);

        // Create Usuario
        Usuario usuario = new Usuario();
        usuario.setEmail("joao.silva@aegis.com.br");
        usuario.setPassword("password");
        usuario.setRole("ROLE_USER");
        usuario.setStatus(Status.ATIVO);
        usuario.setFuncionario(funcionario);
        usuario = usuarioRepository.save(usuario);

        this.userId = usuario.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testGetUserFiliaisPerformance() {
        // Setup User in Context
        Usuario usuario = usuarioRepository.findById(userId).orElseThrow();
        when(currentUserProvider.getCurrentUsuario()).thenReturn(usuario);

        // Prepare Statistics
        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Clear persistence context to force DB hit
        entityManager.clear();

        // Execute Method
        Set<Long> filiais = userContextService.getUserFiliais();

        // Verify result
        assertThat(filiais).isNotEmpty();

        // Measure
        long queryCount = stats.getPrepareStatementCount();
        System.out.println("Query Count: " + queryCount);

        // Optimized expectation: 2 queries (1 for Funcionario with JOIN Filiais, 1 for Usuario inverse relation check)
        assertThat(queryCount).isEqualTo(2);
    }
}
