package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "logging.level.org.hibernate.stat=DEBUG"
})
public class HealthCheckCollectionsManagerPerformanceIT extends BaseIT {

    @Autowired
    private HealthCheckCollectionsManager collectionsManager;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private AtivoDetalheHardwareRepository detalheHardwareRepository;

    @Autowired
    private DiscoRepository discoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    public void testReplaceCollections_Performance() {
        // Setup Data
        Filial filial = new Filial();
        filial.setNome("Test Filial");
        filial.setCodigo("TEST001");
        filial.setCnpj("00.000.000/0001-00");
        filial.setTipo(TipoFilial.MATRIZ);
        filial = filialRepository.save(filial);

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Test Tipo");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipoAtivo = tipoAtivoRepository.save(tipoAtivo);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Test Fornecedor");
        fornecedor.setCnpj("00.000.000/0001-01");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedor = fornecedorRepository.save(fornecedor);

        Ativo ativo = new Ativo();
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setFornecedor(fornecedor);
        ativo.setNome("Test PC");
        ativo.setNumeroPatrimonio("PAT001");
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(BigDecimal.valueOf(1000));
        ativo.setDataRegistro(LocalDate.now());
        ativo = ativoRepository.save(ativo);

        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        detalhes.setAtivo(ativo);
        detalhes.setComputerName("COMP001");
        detalhes = detalheHardwareRepository.save(detalhes);

        int diskCount = 10;
        List<Disco> discos = new ArrayList<>();
        for (int i = 0; i < diskCount; i++) {
            Disco disco = new Disco();
            disco.setAtivoDetalheHardware(detalhes);
            disco.setModel("Model " + i);
            disco.setType("SSD");
            disco.setSerial("SN" + i);
            discos.add(disco);
        }
        discoRepository.saveAll(discos);

        entityManager.flush();
        entityManager.clear(); // Clear context to force DB hit

        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.clear();

        // Act
        HealthCheckDTO dto = new HealthCheckDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                Collections.emptyList(), // Empty list triggers delete
                null, null
        );
        collectionsManager.replaceCollections(detalhes, dto);

        entityManager.flush();

        // Assert
        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("Queries executed: " + queryCount);

        // After optimization:
        // 1 delete (disco)
        // 1 delete (memoria)
        // 1 delete (adaptador)
        // Total expected: 3

        assertThat(queryCount).as("Should have few queries after optimization").isLessThanOrEqualTo(3);
    }
}
