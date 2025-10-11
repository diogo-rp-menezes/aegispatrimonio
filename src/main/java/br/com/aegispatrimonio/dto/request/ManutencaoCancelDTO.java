package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// CORREÇÃO: Transformado em record para um construtor claro e imutabilidade.
public record ManutencaoCancelDTO(
    @NotBlank(message = "O motivo do cancelamento é obrigatório.")
    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    String motivo
) {}
