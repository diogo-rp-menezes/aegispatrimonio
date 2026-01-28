package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.dto.DashboardStatsDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.service.DashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DashboardIntegrationTest extends BaseIT {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void shouldReturnStatsForCurrentTenantOnly() {
        // Setup Shared Entities
        TipoAtivo tipoNotebook = new TipoAtivo();
        tipoNotebook.setNome("Notebook");
        tipoNotebook.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        entityManager.persist(tipoNotebook);

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome("Dell");
        fornecedor.setCnpj("00.000.000/0001-00");
        entityManager.persist(fornecedor);

        // Setup Filial A
        Filial filialA = new Filial();
        filialA.setNome("Filial A");
        filialA.setCodigo("FIL-A");
        filialA.setCnpj("11.111.111/0001-11");
        filialA.setTipo(TipoFilial.FILIAL);
        filialA.setStatus(Status.ATIVO);
        entityManager.persist(filialA);

        // Setup Filial B
        Filial filialB = new Filial();
        filialB.setNome("Filial B");
        filialB.setCodigo("FIL-B");
        filialB.setCnpj("22.222.222/0001-22");
        filialB.setTipo(TipoFilial.FILIAL);
        filialB.setStatus(Status.ATIVO);
        entityManager.persist(filialB);

        // Setup Ativos for Filial A
        // Ativo 1: Critical (Predict < 7 days)
        Ativo ativo1 = createAtivo(filialA, tipoNotebook, fornecedor, "Ativo 1", "PAT-001");
        ativo1.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));
        entityManager.persist(ativo1);

        // Ativo 2: Safe (Predict > 30 days)
        Ativo ativo2 = createAtivo(filialA, tipoNotebook, fornecedor, "Ativo 2", "PAT-002");
        ativo2.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(60));
        entityManager.persist(ativo2);

        // Ativo 4: Indeterminate (No Prediction)
        Ativo ativo4 = createAtivo(filialA, tipoNotebook, fornecedor, "Ativo 4", "PAT-004");
        entityManager.persist(ativo4);

        // Setup Ativos for Filial B
        // Ativo 3: Critical (Predict < 7 days) - Should NOT count for Filial A
        Ativo ativo3 = createAtivo(filialB, tipoNotebook, fornecedor, "Ativo 3", "PAT-003");
        ativo3.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));
        entityManager.persist(ativo3);

        entityManager.flush();
        entityManager.clear();

        // Execution: Set Context to Filial A
        TenantContext.setFilialId(filialA.getId());

        DashboardStatsDTO stats = dashboardService.getStats();

        // Assertions
        assertThat(stats.totalAtivos()).as("Total Ativos for Filial A").isEqualTo(3L);
        assertThat(stats.predicaoCritica()).as("Critical Predictions for Filial A").isEqualTo(1L);
        assertThat(stats.predicaoSegura()).as("Safe Predictions for Filial A").isEqualTo(1L);
        assertThat(stats.predicaoIndeterminada()).as("Indeterminate Predictions for Filial A").isEqualTo(1L);

        // Risky Assets Check
        assertThat(stats.riskyAssets())
            .hasSize(1)
            .extracting("nome")
            .containsOnly("Ativo 1");

        // Status Check
        assertThat(stats.ativosPorStatus())
            .isNotEmpty()
            .extracting("value")
            .contains(3L); // All 3 are ATIVO
    }

    private Ativo createAtivo(Filial filial, TipoAtivo tipo, Fornecedor fornecedor, String nome, String patrimonio) {
        Ativo ativo = new Ativo();
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipo);
        ativo.setFornecedor(fornecedor);
        ativo.setNome(nome);
        ativo.setNumeroPatrimonio(patrimonio);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setValorAquisicao(BigDecimal.valueOf(1000));
        ativo.setDataRegistro(LocalDate.now());
        ativo.setStatus(StatusAtivo.ATIVO);
        return ativo;
    }
}
