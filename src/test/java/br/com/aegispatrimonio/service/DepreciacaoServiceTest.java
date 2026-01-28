package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.model.Usuario;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepreciacaoServiceTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private CurrentUserProvider currentUserProvider; // Adicionado mock para CurrentUserProvider

    @InjectMocks
    private DepreciacaoService depreciacaoService;

    private Ativo ativo;
    private Usuario adminUser; // Adicionado para mockar o usuário

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setValorAquisicao(new BigDecimal("12000.00"));
        ativo.setValorResidual(new BigDecimal("2000.00"));
        ativo.setVidaUtilMeses(100); // 10.000 / 100 = 100 por mês
        ativo.setDataInicioDepreciacao(LocalDate.now().minusMonths(12)); // 1 ano atrás
        ativo.setMetodoDepreciacao(MetodoDepreciacao.LINEAR);
        ativo.setDepreciacaoAcumulada(BigDecimal.ZERO);

        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRole("ROLE_ADMIN");
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser); // Mockando o usuário logado (lenient para evitar UnnecessaryStubbing em testes que não usam)
    }

    @Test
    @DisplayName("Deve depreciar ativos elegíveis no agendamento mensal")
    void calcularDepreciacaoMensalAgendada_deveDepreciarAtivosElegiveis() {
        when(ativoRepository.findAllByStatus(StatusAtivo.ATIVO)).thenReturn(Stream.of(ativo));

        depreciacaoService.calcularDepreciacaoMensalAgendada();

        verify(ativoRepository).saveAll(anyList());
        BigDecimal depreciacaoMensalEsperada = new BigDecimal("100.00");
        assertEquals(0, depreciacaoMensalEsperada.compareTo(ativo.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
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
    @DisplayName("Deve recalcular a depreciação completa para um ativo existente")
    void recalcularDepreciacaoCompleta_quandoAtivoExiste_deveRecalcular() {
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        depreciacaoService.recalcularDepreciacaoCompleta(1L);

        verify(ativoRepository).save(ativo);
        BigDecimal depreciacaoTotalEsperada = new BigDecimal("1200.00"); // 100 * 12 meses
        assertEquals(0, depreciacaoTotalEsperada.compareTo(ativo.getDepreciacaoAcumulada().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve lançar exceção ao recalcular depreciação de ativo inexistente")
    void recalcularDepreciacaoCompleta_quandoAtivoNaoExiste_deveLancarExcecao() {
        when(ativoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> depreciacaoService.recalcularDepreciacaoCompleta(99L));
    }

    @Test
    @DisplayName("Deve calcular depreciação acelerada corretamente")
    void calcularDepreciacaoMensal_comMetodoAcelerada_deveCalcularCorretamente() {
        ativo.setMetodoDepreciacao(MetodoDepreciacao.ACELERADA);
        ativo.setVidaUtilMeses(10); // Vida útil curta para facilitar o cálculo
        ativo.setDataInicioDepreciacao(LocalDate.now().minusMonths(2)); // Começou há 2 meses
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        // --- Cálculo da depreciação esperada para validação ---
        // Soma dos dígitos para 10 meses: 10+9+8+7+6+5+4+3+2+1 = 55
        // Meses decorridos = 2. O período atual é o 3º mês.
        // Dígito reverso para o 3º mês (índice 2) = 10 - 2 = 8
        // Taxa = 8 / 55
        // Valor depreciável = 12000 - 2000 = 10000
        // Depreciação = 10000 * (8 / 55) = 1454.5454...
        BigDecimal depreciacaoEsperada = new BigDecimal("1454.55");

        // --- Execução e Validação ---
        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        assertEquals(0, depreciacaoEsperada.compareTo(valorCalculado.setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve calcular depreciação saldo decrescente corretamente")
    void calcularDepreciacaoMensal_comMetodoSaldoDecrescente_deveCalcularCorretamente() {
        ativo.setMetodoDepreciacao(MetodoDepreciacao.SALDO_DECRESCENTE);
        ativo.setVidaUtilMeses(10);
        ativo.setDataInicioDepreciacao(LocalDate.now().minusMonths(2));
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        // --- Cálculo da depreciação esperada para validação ---
        // Valor depreciável = 12000 - 2000 = 10000
        // Vida útil = 10 meses. Taxa = 2 / 10 = 0.2
        // Meses decorridos = 2
        // Fator decaimento = (1 - 0.2)^2 = 0.8^2 = 0.64
        // Depreciação = 10000 * 0.64 * 0.2 = 1280.00
        BigDecimal depreciacaoEsperada = new BigDecimal("1280.00");

        // --- Execução e Validação ---
        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        assertEquals(0, depreciacaoEsperada.compareTo(valorCalculado.setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    @DisplayName("Deve calcular depreciação saldo decrescente 150% corretamente")
    void calcularDepreciacaoMensal_comMetodoSaldoDecrescente150_deveCalcularCorretamente() {
        ativo.setMetodoDepreciacao(MetodoDepreciacao.SALDO_DECRESCENTE_150);
        ativo.setVidaUtilMeses(10);
        ativo.setDataInicioDepreciacao(LocalDate.now().minusMonths(2));
        when(ativoRepository.findById(1L)).thenReturn(Optional.of(ativo));

        // --- Cálculo da depreciação esperada para validação ---
        // Valor depreciável = 12000 - 2000 = 10000
        // Vida útil = 10 meses. Taxa = 1.5 / 10 = 0.15
        // Meses decorridos = 2
        // Fator decaimento = (1 - 0.15)^2 = 0.85^2 = 0.7225
        // Depreciação = 10000 * 0.7225 * 0.15 = 1083.75
        BigDecimal depreciacaoEsperada = new BigDecimal("1083.75");

        // --- Execução e Validação ---
        BigDecimal valorCalculado = depreciacaoService.calcularDepreciacaoMensal(1L);

        assertEquals(0, depreciacaoEsperada.compareTo(valorCalculado.setScale(2, RoundingMode.HALF_UP)));
    }
}
