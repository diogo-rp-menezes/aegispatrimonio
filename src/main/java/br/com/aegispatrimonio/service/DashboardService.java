package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.ChartDataDTO;
import br.com.aegispatrimonio.dto.DashboardStatsDTO;
import br.com.aegispatrimonio.dto.RiskyAssetDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AtivoRepository ativoRepository;
    private final LocalizacaoRepository localizacaoRepository;

    public DashboardService(AtivoRepository ativoRepository, LocalizacaoRepository localizacaoRepository) {
        this.ativoRepository = ativoRepository;
        this.localizacaoRepository = localizacaoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        long totalAtivos = ativoRepository.countByCurrentTenant();
        long ativosEmManutencao = ativoRepository.countByStatusAndCurrentTenant(StatusAtivo.EM_MANUTENCAO);
        BigDecimal valorTotal = ativoRepository.getValorTotalByCurrentTenant();
        long totalLocalizacoes = localizacaoRepository.countByCurrentTenant();

        if (valorTotal == null) {
            valorTotal = BigDecimal.ZERO;
        }

        // Predictive Maintenance Stats
        LocalDate now = LocalDate.now();
        LocalDate criticalThreshold = now.plusDays(7);
        LocalDate warningThreshold = now.plusDays(30);

        long predicaoCritica = ativoRepository.countCriticalPredictionsByCurrentTenant(criticalThreshold);
        long predicaoAlerta = ativoRepository.countWarningPredictionsByCurrentTenant(criticalThreshold, warningThreshold);
        long predicaoSegura = ativoRepository.countSafePredictionsByCurrentTenant(warningThreshold);
        long predicaoIndeterminada = Math.max(0, totalAtivos - (predicaoCritica + predicaoAlerta + predicaoSegura));

        // Trend Analysis (Next 8 Weeks)
        LocalDate endOfTrend = now.plusWeeks(8);
        List<LocalDate> failureDates = ativoRepository.findPredictionsBetween(now, endOfTrend);

        Map<Integer, Long> weeksMap = failureDates.stream()
            .collect(Collectors.groupingBy(
                date -> (int) ChronoUnit.WEEKS.between(now, date),
                Collectors.counting()
            ));

        List<ChartDataDTO> failureTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 0; i < 8; i++) {
             long count = weeksMap.getOrDefault(i, 0L);
             LocalDate weekStart = now.plusWeeks(i);
             String label = weekStart.format(formatter);
             failureTrend.add(new ChartDataDTO(label, count));
        }

        List<ChartDataDTO> ativosPorStatus = ativoRepository.countByStatusGrouped();
        List<ChartDataDTO> ativosPorTipo = ativoRepository.countByTipoAtivoGrouped();
        List<RiskyAssetDTO> riskyAssets = ativoRepository.findTopRiskyAssetsByCurrentTenant(PageRequest.of(0, 5));

        return new DashboardStatsDTO(
            totalAtivos,
            ativosEmManutencao,
            valorTotal,
            totalLocalizacoes,
            predicaoCritica,
            predicaoAlerta,
            predicaoSegura,
            predicaoIndeterminada,
            ativosPorStatus,
            ativosPorTipo,
            riskyAssets,
            failureTrend
        );
    }
}
