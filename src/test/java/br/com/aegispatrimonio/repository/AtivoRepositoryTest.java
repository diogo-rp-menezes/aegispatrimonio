package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AtivoRepositoryTest extends BaseIT {

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private TipoAtivoRepository tipoAtivoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    private Filial filial;
    private TipoAtivo tipoAtivo;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        filialRepository.deleteAll();
        tipoAtivoRepository.deleteAll();
        fornecedorRepository.deleteAll();

        // Setup Filial
        filial = new Filial();
        filial.setNome("Filial Teste");
        filial.setCodigo("FIL01");
        filial.setCnpj("12.345.678/0001-99");
        filial.setTipo(TipoFilial.FILIAL);
        filial.setStatus(Status.ATIVO);
        filial = filialRepository.save(filial);

        // Setup TipoAtivo
        tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome("Eletrônicos");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);
        tipoAtivo = tipoAtivoRepository.save(tipoAtivo);

        // Setup Fornecedor
        fornecedor = new Fornecedor();
        fornecedor.setNome("Fornecedor Teste");
        fornecedor.setCnpj("98.765.432/0001-88");
        fornecedor.setStatus(StatusFornecedor.ATIVO);
        fornecedor = fornecedorRepository.save(fornecedor);
    }

    @Test
    void shouldFindAtivosByNome() {
        // Arrange
        Ativo notebook = new Ativo();
        notebook.setNome("Notebook Dell");
        notebook.setNumeroPatrimonio("PAT001");
        notebook.setFilial(filial);
        notebook.setTipoAtivo(tipoAtivo);
        notebook.setFornecedor(fornecedor);
        notebook.setValorAquisicao(new BigDecimal("5000.00"));
        notebook.setDataAquisicao(LocalDate.now());
        notebook.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(notebook);

        Ativo monitor = new Ativo();
        monitor.setNome("Monitor LG");
        monitor.setNumeroPatrimonio("PAT002");
        monitor.setFilial(filial);
        monitor.setTipoAtivo(tipoAtivo);
        monitor.setFornecedor(fornecedor);
        monitor.setValorAquisicao(new BigDecimal("1000.00"));
        monitor.setDataAquisicao(LocalDate.now());
        monitor.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(monitor);

        // Act & Assert - Search for "Note" (should find Notebook)
        Page<Ativo> result = ativoRepository.findByFilters(
                filial.getId(), null, null, "Note", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Notebook Dell");

        // Act & Assert - Search for "lg" (should find Monitor - case insensitive)
        result = ativoRepository.findByFilters(
                filial.getId(), null, null, "lg", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Monitor LG");

        // Act & Assert - Search with multiple filiais method
        result = ativoRepository.findByFilialIdsAndFilters(
                Collections.singleton(filial.getId()), filial.getId(), null, null, "Note", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Notebook Dell");
    }
}
