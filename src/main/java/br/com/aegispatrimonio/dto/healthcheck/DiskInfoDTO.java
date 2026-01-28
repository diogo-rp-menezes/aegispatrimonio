package br.com.aegispatrimonio.dto.healthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiskInfoDTO(
    String model,
    String serial,
    String type,
    Double totalGb,
    Double freeGb,
    Double freePercent
) {}
