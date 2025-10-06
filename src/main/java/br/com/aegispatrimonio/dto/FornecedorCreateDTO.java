package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record FornecedorCreateDTO(
    @NotBlank @Size(max = 255) String nome,

    @NotBlank @CNPJ String cnpj,

    @Size(max = 255) String endereco,

    @Size(max = 255) String nomeContatoPrincipal,

    @Email @Size(max = 255) String emailPrincipal,

    @Size(max = 50) String telefonePrincipal,

    String observacoes
) {
}
