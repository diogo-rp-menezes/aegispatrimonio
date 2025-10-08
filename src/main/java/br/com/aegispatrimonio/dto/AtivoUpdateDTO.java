package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtivoUpdateDTO(
    @NotNull @Positive Long filialId,

    @NotBlank @Size(max = 255) String nome,

    @NotBlank @Size(max = 50) String numeroPatrimonio, // Adicionado

    @NotNull @Positive Long tipoAtivoId,

    @Positive Long localizacaoId,

    @NotNull StatusAtivo status,

    @NotNull @PastOrPresent LocalDate dataAquisicao,

    @NotNull @Positive Long fornecedorId,

    @NotNull @Positive BigDecimal valorAquisicao,

    @Positive Long pessoaResponsavelId,

    String observacoes,

    String informacoesGarantia
) {
}
