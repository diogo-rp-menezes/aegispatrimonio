package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ManutencaoConclusaoDTO {

    @NotBlank(message = "A descrição do serviço é obrigatória.")
    private String descricaoServico;

    @NotNull(message = "O custo real é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O custo real deve ser maior que zero.")
    private BigDecimal custoReal;

    @NotNull(message = "O tempo de execução é obrigatório.")
    @Min(value = 1, message = "O tempo de execução deve ser de no mínimo 1 minuto.")
    private Integer tempoExecucao;
    
}
