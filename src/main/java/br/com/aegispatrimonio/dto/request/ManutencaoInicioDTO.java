package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManutencaoInicioDTO {

    @NotNull(message = "O ID do técnico é obrigatório.")
    private Long tecnicoId;
    
}
