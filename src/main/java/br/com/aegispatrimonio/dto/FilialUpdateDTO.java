package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.TipoFilial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;

public record FilialUpdateDTO(
    @NotBlank(message = "O nome da filial não pode ser vazio.")
    String nome,

    @NotBlank(message = "O código da filial não pode ser vazio.")
    String codigo,

    @NotNull(message = "O tipo da filial não pode ser nulo.")
    TipoFilial tipo,

    @NotBlank(message = "O CNPJ não pode ser vazio.")
    @CNPJ(message = "CNPJ inválido.")
    String cnpj,

    String endereco,

    @NotNull(message = "O status não pode ser nulo.")
    Status status
) {
}
