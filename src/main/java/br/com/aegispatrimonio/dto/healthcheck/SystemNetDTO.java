package br.com.aegispatrimonio.dto.healthcheck;

public record SystemNetDTO(
    String interfaceName,
    Long bytesTx,
    Long bytesRx
) {}
