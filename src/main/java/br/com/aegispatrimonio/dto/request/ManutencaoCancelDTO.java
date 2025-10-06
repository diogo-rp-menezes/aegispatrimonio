package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManutencaoCancelDTO {

    @NotBlank(message = "O motivo do cancelamento é obrigatório.")
    private String motivo;
    
}
