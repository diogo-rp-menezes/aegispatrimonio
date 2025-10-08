package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.CategoriaContabil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoAtivoCreateDTO(
        @NotBlank @Size(max = 100) String nome,
        @NotNull CategoriaContabil categoriaContabil
) {
}
