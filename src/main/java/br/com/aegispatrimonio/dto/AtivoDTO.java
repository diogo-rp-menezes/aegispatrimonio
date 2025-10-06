package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;

public record AtivoDTO(
    Long id,
    String nome,
    String numeroPatrimonio,
    String tipoAtivo,
    String localizacao,
    String filial, // Campo adicionado para o nome da Filial
    StatusAtivo status
) {
}
