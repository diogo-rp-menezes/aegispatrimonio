package br.com.aegispatrimonio.dto.response;

import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoResponseDTO {

    private Long id;
    private Long ativoId;
    private String ativoNome;
    private String ativoNumeroPatrimonio;
    
    private TipoManutencao tipo;
    private StatusManutencao status;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataSolicitacao;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicio;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataConclusao;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataPrevistaConclusao;
    
    private String descricaoProblema;
    private String descricaoServico;
    private BigDecimal custoEstimado;
    private BigDecimal custoReal;
    
    private Long fornecedorId;
    private String fornecedorNome;
    
    private Long solicitanteId;
    private String solicitanteNome;
    
    private Long tecnicoResponsavelId;
    private String tecnicoResponsavelNome;
    
    private Integer tempoExecucaoMinutos;
    private String observacoes;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadoEm;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime atualizadoEm;
}