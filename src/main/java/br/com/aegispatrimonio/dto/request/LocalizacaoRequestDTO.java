package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalizacaoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    private Long localizacaoPaiId;

    @NotNull(message = "Filial é obrigatória")
    private Long filialId;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;
}