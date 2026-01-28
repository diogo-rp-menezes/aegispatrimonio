package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.dto.ChartDataDTO;
import br.com.aegispatrimonio.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AtivoRepositoryAggregationTest extends BaseIT {

    @Autowired
    private AtivoRepository ativoRepository;
    @Autowired private FilialRepository filialRepository;
    @Autowired private TipoAtivoRepository tipoAtivoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;

    private Filial filial;
    private TipoAtivo tipo1;
    private TipoAtivo tipo2;
    private Fornecedor fornecedor;

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

        tipo1 = new TipoAtivo();
        tipo1.setNome("Notebook");
        tipo1.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo1.setStatus(Status.ATIVO);
        tipo1 = tipoAtivoRepository.save(tipo1);

        tipo2 = new TipoAtivo();
        tipo2.setNome("Monitor");
        tipo2.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipo2.setStatus(Status.ATIVO);
        tipo2 = tipoAtivoRepository.save(tipo2);

        fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor Teste");
        fornecedor.setCnpj("12.345.678/0001-00");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedor = fornecedorRepository.save(fornecedor);

        createAtivo("Note 1", tipo1, StatusAtivo.ATIVO);
        createAtivo("Note 2", tipo1, StatusAtivo.EM_MANUTENCAO);
        createAtivo("Monitor 1", tipo2, StatusAtivo.ATIVO);

        // Another branch (should be ignored)
        Filial otherFilial = new Filial();
        otherFilial.setNome("Filial B");
        otherFilial.setCodigo("FIL02");
        otherFilial.setCnpj("99.999.999/0001-99");
        otherFilial.setTipo(TipoFilial.FILIAL);
        otherFilial.setStatus(Status.ATIVO);
        otherFilial = filialRepository.save(otherFilial);
        createAtivo("Other", tipo1, StatusAtivo.ATIVO, otherFilial);

        TenantContext.setFilialId(filial.getId());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCountByStatusGrouped() {
        List<ChartDataDTO> results = ativoRepository.countByStatusGrouped();

        assertThat(results).hasSize(2);
        // Expect: ATIVO -> 2 (Note 1, Monitor 1), EM_MANUTENCAO -> 1 (Note 2)
        assertThat(results).extracting(ChartDataDTO::label)
                .containsExactlyInAnyOrder("ATIVO", "EM_MANUTENCAO");

        assertThat(results).filteredOn(r -> r.label().equals("ATIVO"))
                .extracting(ChartDataDTO::value).containsOnly(2L);
    }

    @Test
    void shouldCountByTipoAtivoGrouped() {
        List<ChartDataDTO> results = ativoRepository.countByTipoAtivoGrouped();

        assertThat(results).hasSize(2);
        // Expect: Notebook -> 2, Monitor -> 1
        assertThat(results).extracting(ChartDataDTO::label)
                .containsExactlyInAnyOrder("Notebook", "Monitor");

        assertThat(results).filteredOn(r -> r.label().equals("Notebook"))
                .extracting(ChartDataDTO::value).containsOnly(2L);
    }

    private void createAtivo(String nome, TipoAtivo tipo, StatusAtivo status) {
        createAtivo(nome, tipo, status, this.filial);
    }

    private void createAtivo(String nome, TipoAtivo tipo, StatusAtivo status, Filial f) {
        Ativo a = new Ativo();
        a.setNome(nome);
        a.setNumeroPatrimonio("PAT-" + nome);
        a.setFilial(f);
        a.setTipoAtivo(tipo);
        a.setStatus(status);
        a.setValorAquisicao(BigDecimal.TEN);
        a.setDataAquisicao(LocalDate.now());
        a.setFornecedor(this.fornecedor);
        ativoRepository.save(a);
    }
}
