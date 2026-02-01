package br.com.aegispatrimonio.dto.healthcheck;

import java.math.BigDecimal;

public record SystemDiskDTO(
    String mount,
    Long total,
    Long free,
    BigDecimal freePercent
) {}
