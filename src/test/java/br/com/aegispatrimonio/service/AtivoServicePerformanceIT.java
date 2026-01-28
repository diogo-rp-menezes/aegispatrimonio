package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "logging.level.org.hibernate.stat=DEBUG"
})
public class AtivoServicePerformanceIT extends BaseIT {

    @Autowired
    private AtivoService ativoService;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    public void testCriarAtivoPerformance() {
        // Setup Data
        Filial filial = new Filial();
        filial.setNome("Test Filial Perf");
        filial.setCodigo("PERF001");
        filial.setCnpj("00.000.000/0002-00");
        filial.setTipo(TipoFilial.MATRIZ);
        filial = filialRepository.save(filial);

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Test Tipo Perf");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipoAtivo = tipoAtivoRepository.save(tipoAtivo);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Test Fornecedor Perf");
        fornecedor.setCnpj("00.000.000/0002-01");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedor = fornecedorRepository.save(fornecedor);

        Departamento departamento = new Departamento();
        departamento.setNome("IT");
        departamento.setFilial(filial);
        departamento = departamentoRepository.save(departamento);

        Funcionario funcionario = new Funcionario();
        funcionario.setNome("John Doe");
        funcionario.setMatricula("M001");
        funcionario.setCargo("Analyst");
        funcionario.setDepartamento(departamento);
        funcionario.setFiliais(Collections.singleton(filial));
        funcionario = funcionarioRepository.save(funcionario);

        entityManager.flush();
        entityManager.clear(); // Clear context to force DB hit

        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.clear();

        // Act
        AtivoCreateDTO dto = new AtivoCreateDTO(
            filial.getId(),
            "Laptop Perf",
            tipoAtivo.getId(),
            "PAT-PERF-001",
            null,
            LocalDate.now(),
            fornecedor.getId(),
            BigDecimal.valueOf(2500),
            funcionario.getId(),
            "Obs",
            "Warranty",
            null,
            null
        );

        ativoService.criar(dto);

        entityManager.flush();

        // Assert
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("Queries executed: " + queryCount);

        // Baseline was 9. Optimized should be 8.
        assertThat(queryCount).as("Should have eliminated N+1 query for filiais").isLessThanOrEqualTo(8);
    }
}
