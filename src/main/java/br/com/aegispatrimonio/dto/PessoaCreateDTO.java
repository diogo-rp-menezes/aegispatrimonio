package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PessoaCreateDTO(
    @NotNull @Positive Long filialId,

    @NotBlank @Size(max = 255) String nome,

    @Size(max = 50) String matricula,

    @NotBlank @Size(max = 100) String cargo,

    @NotBlank @Email @Size(max = 255) String email,

    @NotBlank @Size(min = 8, max = 100) String password,

    @NotBlank String role,

    @NotNull @Positive Long departamentoId
) {
}
