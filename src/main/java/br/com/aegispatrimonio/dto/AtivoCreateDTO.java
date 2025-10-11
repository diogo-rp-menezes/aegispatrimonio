package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtivoCreateDTO(
    @NotNull @Positive Long filialId,

    @NotBlank @Size(max = 255) String nome,

    @NotNull @Positive Long tipoAtivoId,

    @NotBlank @Size(max = 100) String numeroPatrimonio,

    @Positive Long localizacaoId, // Opcional, mas se enviado, deve ser positivo

    @NotNull @PastOrPresent LocalDate dataAquisicao,

    @NotNull @Positive Long fornecedorId,

    @NotNull @Positive BigDecimal valorAquisicao,

    // CORREÇÃO: Renomeado de pessoaResponsavelId para funcionarioResponsavelId
    @Positive Long funcionarioResponsavelId, // Opcional, mas se enviado, deve ser positivo

    String observacoes,

    String informacoesGarantia
) {
}
