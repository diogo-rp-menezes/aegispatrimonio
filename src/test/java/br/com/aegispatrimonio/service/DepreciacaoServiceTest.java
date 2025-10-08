package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepreciacaoServiceTest {

    @Mock
    private AtivoRepository ativoRepository;

    @InjectMocks
    private DepreciacaoService depreciacaoService;

    private Ativo ativo;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNumeroPatrimonio("12345");
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setValorAquisicao(new BigDecimal("12000.00"));
        ativo.setValorResidual(new BigDecimal("1200.00"));
        ativo.setVidaUtilMeses(120); // 10 anos
        ativo.setDataInicioDepreciacao(LocalDate.now().minusYears(1));
        ativo.setMetodoDepreciacao(MetodoDepreciacao.LINEAR);
        ativo.setDepreciacaoAcumulada(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve depreciar ativos elegíveis no agendamento mensal")
    void calcularDepreciacaoMensalAgendada_deveDepreciarAtivosElegiveis() {
        when(ativoRepository.findAllByStatus(StatusAtivo.ATIVO)).thenReturn(Stream.of(ativo));

        depreciacaoService.calcularDepreciacaoMensalAgendada();

        verify(ativoRepository, times(1)).saveAll(anyList());
        BigDecimal depreciacaoMensalEsperada = new BigDecimal("90.00"); // (12000 - 1200) / 120
        assertEquals(0, depreciacaoMensalEsperada.compareTo(ativo.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve recalcular a depreciação para todos os ativos")
    void recalcularDepreciacaoTodosAtivos_deveRecalcularDepreciacao() {
        when(ativoRepository.streamAll()).thenReturn(Stream.of(ativo));

        depreciacaoService.recalcularDepreciacaoTodosAtivos();

        verify(ativoRepository, times(1)).saveAll(anyList());
        // 12 meses se passaram
        BigDecimal depreciacaoTotalEsperada = new BigDecimal("1080.00"); // 90 * 12
        assertEquals(0, depreciacaoTotalEsperada.compareTo(ativo.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve recalcular a depreciação completa para um ativo existente")
    void recalcularDepreciacaoCompleta_quandoAtivoExiste_deveRecalcular() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        depreciacaoService.recalcularDepreciacaoCompleta(1L);

        verify(ativoRepository, times(1)).save(ativo);
        BigDecimal depreciacaoTotalEsperada = new BigDecimal("1080.00"); // 90 * 12
        assertEquals(0, depreciacaoTotalEsperada.compareTo(ativo.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve lançar exceção ao recalcular depreciação de ativo inexistente")
    void recalcularDepreciacaoCompleta_quandoAtivoNaoExiste_deveLancarExcecao() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> depreciacaoService.recalcularDepreciacaoCompleta(1L));
    }

    @Test
    @DisplayName("Deve calcular o valor da depreciação mensal para um ativo")
    void calcularDepreciacaoMensal_quandoAtivoExiste_deveRetornarValor() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        BigDecimal depreciacaoMensalEsperada = new BigDecimal("90.00"); // (12000 - 1200) / 120
        assertEquals(0, depreciacaoMensalEsperada.compareTo(valorCalculado.setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve retornar zero para depreciação mensal se o ativo não for ATIVO")
    void calcularDepreciacaoMensal_quandoAtivoNaoAtivo_retornaZero() {
        ativo.setStatus(StatusAtivo.BAIXADO);
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        assertEquals(BigDecimal.ZERO, valorCalculado);
    }

    @Test
    @DisplayName("Não deve depreciar se o ativo já foi depreciado no mês corrente")
    void calcularDepreciacaoMensalAgendada_quandoJaDepreciadoNoMes_naoDeveDepreciar() {
        ativo.setDataUltimaDepreciacao(LocalDate.now());
        when(ativoRepository.findAllByStatus(StatusAtivo.ATIVO)).thenReturn(Stream.of(ativo));

        depreciacaoService.calcularDepreciacaoMensalAgendada();

        verify(ativoRepository, never()).saveAll(anyList());
        assertEquals(BigDecimal.ZERO, ativo.getDepreciacaoAcumulada());
    }

    @Test
    @DisplayName("Não deve depreciar se o ativo já está totalmente depreciado")
    void calcularDepreciacaoMensalAgendada_quandoTotalmenteDepreciado_naoDeveDepreciar() {
        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        ativo.setDepreciacaoAcumulada(valorDepreciavel);
        when(ativoRepository.findAllByStatus(StatusAtivo.ATIVO)).thenReturn(Stream.of(ativo));

        depreciacaoService.calcularDepreciacaoMensalAgendada();

        verify(ativoRepository, never()).saveAll(anyList());
        assertEquals(valorDepreciavel, ativo.getDepreciacaoAcumulada());
    }

    @Test
    @DisplayName("Deve calcular depreciação acelerada corretamente")
    void calcularDepreciacaoMensal_comMetodoAcelerada_deveCalcularCorretamente() {
        ativo.setMetodoDepreciacao(MetodoDepreciacao.ACELERADA);
        ativo.setVidaUtilMeses(120); // 10 anos
        ativo.setDataInicioDepreciacao(LocalDate.now().minusMonths(5)); // Começou há 5 meses

        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        // --- Cálculo da depreciação esperada para validação ---
        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        long vidaUtil = ativo.getVidaUtilMeses();
        long somaDigitos = vidaUtil * (vidaUtil + 1) / 2;
        long mesesDecorridos = ChronoUnit.MONTHS.between(ativo.getDataInicioDepreciacao(), LocalDate.now());
        long digitoAtual = vidaUtil - mesesDecorridos;
        BigDecimal taxa = BigDecimal.valueOf(digitoAtual).divide(BigDecimal.valueOf(somaDigitos), 10, RoundingMode.HALF_UP);
        BigDecimal depreciacaoEsperada = valorDepreciavel.multiply(taxa);

        // --- Execução e Validação ---
        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        // Compara o valor calculado com o esperado para o método acelerado
        assertEquals(0, depreciacaoEsperada.setScale(2, RoundingMode.HALF_UP).compareTo(valorCalculado.setScale(2, RoundingMode.HALF_UP)));

        // Garante que é diferente do cálculo linear
        BigDecimal depreciacaoLinear = valorDepreciavel.divide(BigDecimal.valueOf(vidaUtil), 2, RoundingMode.HALF_UP);
        assertNotEquals(0, depreciacaoLinear.compareTo(valorCalculado));
    }
}
