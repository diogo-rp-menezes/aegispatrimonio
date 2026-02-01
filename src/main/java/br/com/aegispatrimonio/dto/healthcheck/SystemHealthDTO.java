package br.com.aegispatrimonio.dto.healthcheck;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SystemHealthDTO(
    Long id,
    LocalDateTime createdAt,
    String host,
    BigDecimal cpuUsage,
    BigDecimal memFreePercent,
    List<SystemDiskDTO> disks,
    List<SystemNetDTO> nets
) {
}
