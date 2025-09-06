package br.com.aegispatrimonio.dto.request;

import br.com.aegispatrimonio.model.TipoManutencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoRequestDTO {

    @NotNull(message = "Ativo é obrigatório")
    private Long ativoId;

    @NotNull(message = "Tipo de manutenção é obrigatório")
    private TipoManutencao tipo;

    @NotNull(message = "Solicitante é obrigatório")
    private Long solicitanteId;

    private Long fornecedorId;
    private Long tecnicoResponsavelId;

    @NotBlank(message = "Descrição do problema é obrigatória")
    @Size(max = 1000, message = "Descrição do problema deve ter no máximo 1000 caracteres")
    private String descricaoProblema;

    @Size(max = 1000, message = "Descrição do serviço deve ter no máximo 1000 caracteres")
    private String descricaoServico;

    @PositiveOrZero(message = "Custo estimado não pode ser negativo")
    private BigDecimal custoEstimado;

    private LocalDate dataPrevistaConclusao;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}