package br.com.aegispatrimonio.dto;

import java.math.BigDecimal;

public record DashboardStatsDTO(
    long totalAtivos,
    long ativosEmManutencao,
    BigDecimal valorTotal,
    long totalLocalizacoes,
    long predicaoCritica,
    long predicaoAlerta,
    long predicaoSegura
) {}
