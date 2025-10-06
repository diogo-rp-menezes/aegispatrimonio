package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;

public record PessoaDTO(
    Long id,
    String nome,
    String matricula,
    String cargo,
    String email,
    String departamento,
    String filial, // Campo adicionado para o nome da Filial
    Status status
) {
}
