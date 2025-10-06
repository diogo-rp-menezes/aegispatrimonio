package br.com.aegispatrimonio.dto;

public record HealthCheckAdaptadorRedeDTO(
    String description,
    String macAddress,
    String ipAddress
) {
}
