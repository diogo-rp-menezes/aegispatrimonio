package br.com.aegispatrimonio.dto.healthcheck;

import java.math.BigDecimal;

public record DiscoDTO(
    String model,
    String serial,
    String type,
    BigDecimal totalGb,
    BigDecimal freeGb,
    Integer freePercent
) {
}
