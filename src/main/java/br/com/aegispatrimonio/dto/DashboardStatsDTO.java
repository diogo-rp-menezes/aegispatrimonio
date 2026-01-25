package br.com.aegispatrimonio.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsDTO(
    long totalAtivos,
    long ativosEmManutencao,
    BigDecimal valorTotal,
    long totalLocalizacoes,
    long predicaoCritica,
    long predicaoAlerta,
    long predicaoSegura,
    List<ChartDataDTO> ativosPorStatus,
    List<ChartDataDTO> ativosPorTipo,
    List<RiskyAssetDTO> riskyAssets,
    List<ChartDataDTO> failureTrend
) {}
