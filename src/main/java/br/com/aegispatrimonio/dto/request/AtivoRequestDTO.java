package br.com.aegispatrimonio.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtivoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "Tipo do ativo é obrigatório")
    private Long tipoAtivoId;

    @NotBlank(message = "Número de patrimônio é obrigatório")
    @Size(max = 50, message = "Número de patrimônio deve ter no máximo 50 caracteres")
    private String numeroPatrimonio;

    @NotNull(message = "Localização é obrigatória")
    private Long localizacaoId;

    @NotNull(message = "Status é obrigatório")
    private StatusAtivo status;

    @NotNull(message = "Data de aquisição é obrigatória")
    @PastOrPresent(message = "Data de aquisição deve ser hoje ou no passado")
    private LocalDate dataAquisicao;

    @NotNull(message = "Fornecedor é obrigatório")
    private Long fornecedorId;

    @NotNull(message = "Valor de aquisição é obrigatório")
    @Positive(message = "Valor de aquisição deve ser positivo")
    @Digits(integer = 15, fraction = 2, message = "Valor de aquisição deve ter no máximo 2 casas decimais")
    private BigDecimal valorAquisicao;

    @NotNull(message = "Valor residual é obrigatório")
    @PositiveOrZero(message = "Valor residual não pode ser negativo")
    @Digits(integer = 15, fraction = 2, message = "Valor residual deve ter no máximo 2 casas decimais")
    private BigDecimal valorResidual;

    @Positive(message = "Vida útil deve ser positiva")
    private Integer vidaUtilMeses;

    private MetodoDepreciacao metodoDepreciacao;

    private LocalDate dataInicioDepreciacao;

    @Size(max = 255, message = "Informações de garantia devem ter no máximo 255 caracteres")
    private String informacoesGarantia;

    @NotNull(message = "Pessoa responsável é obrigatória")
    private Long pessoaResponsavelId;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}