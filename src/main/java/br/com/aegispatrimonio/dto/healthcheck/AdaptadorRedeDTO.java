package br.com.aegispatrimonio.dto.healthcheck;

public record AdaptadorRedeDTO(
    String description,
    String macAddress,
    String ipAddresses
) {
}
