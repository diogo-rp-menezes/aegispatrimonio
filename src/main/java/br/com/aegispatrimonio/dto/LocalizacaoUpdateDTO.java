package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LocalizacaoUpdateDTO(
    @NotBlank @Size(max = 100) String nome,

    @Size(max = 255) String descricao,

    @NotNull @Positive Long filialId,

    @Positive Long localizacaoPaiId,

    @NotNull Status status
) {
}
