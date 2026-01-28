package br.com.aegispatrimonio.dto.healthcheck;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SystemHealthDTO(
    Long id,
    LocalDateTime createdAt,
    String host,
    BigDecimal cpuUsage,
    BigDecimal memFreePercent,
    String disks,
    String nets
) {
}
