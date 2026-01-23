package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.StatusAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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

    // CORREÇÃO: Renomeado de pessoaResponsavelId para funcionarioResponsavelId
    @Positive Long funcionarioResponsavelId,

    String observacoes,

    String informacoesGarantia,

    AtivoDetalheHardwareDTO detalheHardware,

    Map<String, Object> atributos
) {
    public AtivoUpdateDTO(Long filialId, String nome, String numeroPatrimonio, Long tipoAtivoId, Long localizacaoId, StatusAtivo status, LocalDate dataAquisicao, Long fornecedorId, BigDecimal valorAquisicao, Long funcionarioResponsavelId, String observacoes, String informacoesGarantia, AtivoDetalheHardwareDTO detalheHardware) {
        this(filialId, nome, numeroPatrimonio, tipoAtivoId, localizacaoId, status, dataAquisicao, fornecedorId, valorAquisicao, funcionarioResponsavelId, observacoes, informacoesGarantia, detalheHardware, null);
    }
}
