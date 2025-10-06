package br.com.aegispatrimonio.dto;

public record HealthCheckMemoriaDTO(
    Integer sizeGb,
    String manufacturer,
    String partNumber,
    String serialNumber
) {
}
