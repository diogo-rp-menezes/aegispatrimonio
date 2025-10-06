package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;

public record FornecedorDTO(
    Long id,
    String nome,
    String cnpj,
    String endereco,
    String nomeContatoPrincipal,
    String emailPrincipal,
    String telefonePrincipal,
    String observacoes,
    Status status
) {
}
