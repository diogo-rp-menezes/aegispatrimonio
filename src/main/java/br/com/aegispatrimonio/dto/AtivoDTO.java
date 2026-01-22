package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;

/**
 * DTO para representar os dados de um Ativo na resposta de uma API.
 */
public record AtivoDTO(
    Long id,
    String nome,
    String numeroPatrimonio,
    String tipoAtivo,
    String localizacao,
    String filial,
    StatusAtivo status,
    // CORREÇÃO: Adicionados campos para o responsável
    Long funcionarioResponsavelId,
    String funcionarioResponsavelNome,
    AtivoDetalheHardwareDTO detalheHardware
) {
}
