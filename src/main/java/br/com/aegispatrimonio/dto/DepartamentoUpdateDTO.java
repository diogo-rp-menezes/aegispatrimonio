package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DepartamentoUpdateDTO(
    @NotBlank @Size(max = 100) String nome,
    @NotNull @Positive Long filialId
) {
}
