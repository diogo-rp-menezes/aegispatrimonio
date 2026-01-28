package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AtivoPredictiveRepositoryTest extends BaseIT {

    @Autowired
    private AtivoRepository ativoRepository;
    @Autowired
    private FilialRepository filialRepository;
    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;

    private Filial filial;
    private TipoAtivo tipo;
    private Fornecedor forn;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        filialRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        fornecedorRepository.deleteAll();

        filial = new Filial();
        filial.setNome("Filial Teste");
        filial.setCodigo("FIL01");
        filial.setCnpj("12.345.678/0001-99");
        filial.setTipo(TipoFilial.FILIAL);
        filial.setStatus(Status.ATIVO);
        filial = filialRepository.save(filial);

        tipo = new TipoAtivo();
        tipo.setNome("Tipo");
        tipo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo = tipoAtivoRepository.save(tipo);

        forn = new Fornecedor();
        forn.setNome("Forn");
        forn.setCnpj("00.000.000/0001-00");
        forn.setStatus(StatusFornecedor.ATIVO);
        forn = fornecedorRepository.save(forn);

        TenantContext.setFilialId(filial.getId());
    }

    @Test
    void shouldCountPredictionsCorrectly() {
        createAtivoWithPrediction(LocalDate.now().plusDays(5));  // Critical
        createAtivoWithPrediction(LocalDate.now().plusDays(20)); // Warning
        createAtivoWithPrediction(LocalDate.now().plusDays(40)); // Safe
        createAtivoWithPrediction(null); // No prediction

        LocalDate now = LocalDate.now();
        LocalDate criticalThreshold = now.plusDays(7);
        LocalDate warningThreshold = now.plusDays(30);

        long critical = ativoRepository.countCriticalPredictionsByCurrentTenant(criticalThreshold);
        long warning = ativoRepository.countWarningPredictionsByCurrentTenant(criticalThreshold, warningThreshold);
        long safe = ativoRepository.countSafePredictionsByCurrentTenant(warningThreshold);

        assertThat(critical).isEqualTo(1);
        assertThat(warning).isEqualTo(1);
        assertThat(safe).isEqualTo(1);
    }

    private void createAtivoWithPrediction(LocalDate prediction) {
        Ativo ativo = new Ativo();
        ativo.setNome("Ativo " + (prediction != null ? prediction.toString() : "Null"));
        ativo.setNumeroPatrimonio("PAT-" + System.nanoTime());
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipo);
        ativo.setFornecedor(forn);
        ativo.setValorAquisicao(BigDecimal.TEN);
        ativo.setDataAquisicao(LocalDate.now());
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setPrevisaoEsgotamentoDisco(prediction);
        ativoRepository.save(ativo);
    }
}
