package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoAtivoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotBlank(message = "Categoria contábil é obrigatória")
    @Size(max = 50, message = "Categoria contábil deve ter no máximo 50 caracteres")
    private String categoriaContabil;

    @Size(max = 50, message = "Ícone deve ter no máximo 50 caracteres")
    private String icone;
}