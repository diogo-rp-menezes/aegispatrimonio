package br.com.aegispatrimonio.dto.response;

import br.com.aegispatrimonio.model.StatusMovimentacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoResponseDTO {

    private Long id;
    private Long ativoId;
    private String ativoNome;
    private String ativoNumeroPatrimonio;
    
    private Long localizacaoOrigemId;
    private String localizacaoOrigemNome;
    
    private Long localizacaoDestinoId;
    private String localizacaoDestinoNome;
    
    // CORREÇÃO: Renomeado de pessoa para funcionario
    private Long funcionarioOrigemId;
    private String funcionarioOrigemNome;
    
    // CORREÇÃO: Renomeado de pessoa para funcionario
    private Long funcionarioDestinoId;
    private String funcionarioDestinoNome;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataMovimentacao;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataEfetivacao;
    
    private StatusMovimentacao status;
    private String motivo;
    private String observacoes;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadoEm;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime atualizadoEm;
}
