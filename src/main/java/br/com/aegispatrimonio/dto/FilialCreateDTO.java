package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.TipoFilial;

public record FilialCreateDTO(
    String nome,
    String codigo,
    TipoFilial tipo,
    String cnpj,
    String endereco
) {
}
