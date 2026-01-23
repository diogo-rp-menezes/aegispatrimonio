package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;
import java.util.Map;

/**
 * DTO para representar os dados de um Ativo na resposta de uma API.
 */
public record AtivoDTO(
    Long id,
    String nome,
    String numeroPatrimonio,
    Long tipoAtivoId,
    String tipoAtivo,
    Long localizacaoId,
    String localizacao,
    Long filialId,
    String filial,
    Long fornecedorId,
    String fornecedorNome,
    StatusAtivo status,
    // CORREÇÃO: Adicionados campos para o responsável
    Long funcionarioResponsavelId,
    String funcionarioResponsavelNome,
    AtivoDetalheHardwareDTO detalheHardware,
    Map<String, Object> atributos
) {
}
