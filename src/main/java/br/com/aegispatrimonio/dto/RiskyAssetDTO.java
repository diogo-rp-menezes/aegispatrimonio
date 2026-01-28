package br.com.aegispatrimonio.dto;

import java.time.LocalDate;

public record RiskyAssetDTO(
    Long id,
    String nome,
    String tipoAtivo,
    LocalDate previsaoEsgotamento
) {}
