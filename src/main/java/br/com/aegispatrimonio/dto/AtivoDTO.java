package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    BigDecimal valorAquisicao,
    Long funcionarioResponsavelId,
    String funcionarioResponsavelNome,
    AtivoDetalheHardwareDTO detalheHardware,
    Map<String, Object> atributos,
    LocalDate previsaoEsgotamentoDisco
) {
    // Secondary constructor to maintain backward compatibility
    public AtivoDTO(Long id, String nome, String numeroPatrimonio, Long tipoAtivoId, String tipoAtivo, Long localizacaoId, String localizacao, Long filialId, String filial, Long fornecedorId, String fornecedorNome, StatusAtivo status, BigDecimal valorAquisicao, Long funcionarioResponsavelId, String funcionarioResponsavelNome, AtivoDetalheHardwareDTO detalheHardware, Map<String, Object> atributos) {
        this(id, nome, numeroPatrimonio, tipoAtivoId, tipoAtivo, localizacaoId, localizacao, filialId, filial, fornecedorId, fornecedorNome, status, valorAquisicao, funcionarioResponsavelId, funcionarioResponsavelNome, detalheHardware, atributos, null);
    }
}
