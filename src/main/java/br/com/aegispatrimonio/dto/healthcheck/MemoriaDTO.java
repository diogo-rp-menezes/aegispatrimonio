package br.com.aegispatrimonio.dto.healthcheck;

public record MemoriaDTO(
    String manufacturer,
    String serialNumber,
    String partNumber,
    Integer sizeGb
) {
}
