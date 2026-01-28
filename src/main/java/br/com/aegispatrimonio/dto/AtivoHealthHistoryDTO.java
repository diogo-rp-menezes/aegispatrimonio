package br.com.aegispatrimonio.dto;

import java.time.LocalDateTime;

public record AtivoHealthHistoryDTO(
    LocalDateTime dataRegistro,
    String componente,
    Double valor,
    String metrica
) {}
