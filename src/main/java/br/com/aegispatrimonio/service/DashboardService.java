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
import java.util.List;

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
            ativosPorStatus,
            ativosPorTipo,
            riskyAssets
        );
    }
}
