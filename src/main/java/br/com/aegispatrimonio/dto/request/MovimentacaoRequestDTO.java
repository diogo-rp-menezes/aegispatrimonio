package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// CORREÇÃO: Transformado em record para padronização e imutabilidade.
public record MovimentacaoRequestDTO(
    @NotNull(message = "Ativo é obrigatório")
    Long ativoId,

    @NotNull(message = "Localização de origem é obrigatória")
    Long localizacaoOrigemId,

    @NotNull(message = "Localização de destino é obrigatória")
    Long localizacaoDestinoId,

    @NotNull(message = "Funcionário de origem é obrigatório")
    Long funcionarioOrigemId,

    @NotNull(message = "Funcionário de destino é obrigatório")
    Long funcionarioDestinoId,

    LocalDate dataMovimentacao,

    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    String motivo,

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    String observacoes
) {}
