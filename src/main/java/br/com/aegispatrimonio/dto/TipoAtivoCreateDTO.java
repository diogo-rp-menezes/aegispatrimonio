package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoAtivoCreateDTO(
    @NotBlank @Size(max = 100) String nome
) {
}
