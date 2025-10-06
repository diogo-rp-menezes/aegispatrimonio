package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;

public record LocalizacaoDTO(
    Long id,
    String nome,
    String descricao,
    String filial,
    String localizacaoPai,
    Status status
) {
}
