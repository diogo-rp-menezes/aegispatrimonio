package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.TipoFilial;

public record FilialDTO(
    Long id,
    String nome,
    String codigo,
    TipoFilial tipo,
    String cnpj,
    String endereco,
    Status status
) {
}
