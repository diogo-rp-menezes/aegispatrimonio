package br.com.aegispatrimonio.dto;

public record HealthCheckDiscoDTO(
    String model,
    String serialNumber,
    String type,
    Double totalGb,
    Double freeGb,
    Double freePercent
) {
}
