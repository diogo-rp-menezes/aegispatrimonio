package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoRequestDTO {

    @NotNull(message = "Ativo é obrigatório")
    private Long ativoId;

    @NotNull(message = "Localização de origem é obrigatória")
    private Long localizacaoOrigemId;

    @NotNull(message = "Localização de destino é obrigatória")
    private Long localizacaoDestinoId;

    @NotNull(message = "Pessoa de origem é obrigatória")
    private Long pessoaOrigemId;

    @NotNull(message = "Pessoa de destino é obrigatória")
    private Long pessoaDestinoId;

    private LocalDate dataMovimentacao;

    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    private String motivo;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}