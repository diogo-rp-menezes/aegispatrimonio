package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DashboardStatsDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private LocalizacaoRepository localizacaoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldReturnCorrectStats() {
        when(ativoRepository.countByCurrentTenant()).thenReturn(10L);
        when(ativoRepository.countByStatusAndCurrentTenant(StatusAtivo.EM_MANUTENCAO)).thenReturn(2L);
        when(ativoRepository.getValorTotalByCurrentTenant()).thenReturn(new BigDecimal("1000.00"));
        when(localizacaoRepository.countByCurrentTenant()).thenReturn(5L);

        when(ativoRepository.countCriticalPredictionsByCurrentTenant(any(LocalDate.class))).thenReturn(1L);
        when(ativoRepository.countWarningPredictionsByCurrentTenant(any(LocalDate.class), any(LocalDate.class))).thenReturn(3L);
        when(ativoRepository.countSafePredictionsByCurrentTenant(any(LocalDate.class))).thenReturn(6L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertThat(stats.totalAtivos()).isEqualTo(10L);
        assertThat(stats.ativosEmManutencao()).isEqualTo(2L);
        assertThat(stats.valorTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(stats.totalLocalizacoes()).isEqualTo(5L);
        assertThat(stats.predicaoCritica()).isEqualTo(1L);
        assertThat(stats.predicaoAlerta()).isEqualTo(3L);
        assertThat(stats.predicaoSegura()).isEqualTo(6L);
    }
}
