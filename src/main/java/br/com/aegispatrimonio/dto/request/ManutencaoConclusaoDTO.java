package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// CORREÇÃO: Transformado em record para um construtor claro e imutabilidade.
public record ManutencaoConclusaoDTO(
    @NotBlank(message = "A descrição do serviço é obrigatória.")
    @Size(max = 2000, message = "Descrição do serviço deve ter no máximo 2000 caracteres")
    String descricaoServico,

    @NotNull(message = "O custo real é obrigatório.")
    @PositiveOrZero(message = "Custo real não pode ser negativo.")
    BigDecimal custoReal,

    @NotNull(message = "O tempo de execução é obrigatório.")
    @PositiveOrZero(message = "Tempo de execução não pode ser negativo.")
    Integer tempoExecucao
) {}
