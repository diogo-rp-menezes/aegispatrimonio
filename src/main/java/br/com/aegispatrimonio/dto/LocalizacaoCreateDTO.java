package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LocalizacaoCreateDTO(
    @NotBlank @Size(max = 100) String nome,

    @Size(max = 255) String descricao,

    @NotNull @Positive Long filialId,

    @Positive Long localizacaoPaiId // Opcional, mas se enviado, deve ser positivo
) {
}
