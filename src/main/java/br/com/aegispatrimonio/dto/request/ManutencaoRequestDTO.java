package br.com.aegispatrimonio.dto.request;

import br.com.aegispatrimonio.model.TipoManutencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// CORREÇÃO: Substituído record por POJO compatível com testes (no-arg + construtor com 10 parâmetros)
public class ManutencaoRequestDTO {

    @NotNull(message = "Ativo é obrigatório")
    private Long ativoId;

    @NotNull(message = "Tipo de manutenção é obrigatório")
    private TipoManutencao tipo;

    @NotNull(message = "Solicitante é obrigatório")
    private Long solicitanteId;

    private Long fornecedorId;

    // Campo adicional usado em alguns testes/versões
    private Long tecnicoId;

    @NotBlank(message = "Descrição do problema é obrigatória")
    @Size(max = 1000, message = "Descrição do problema deve ter no máximo 1000 caracteres")
    private String descricaoProblema;

    @PositiveOrZero(message = "Custo estimado não pode ser negativo")
    private BigDecimal custoEstimado;

    private LocalDate dataPrevistaConclusao;

    // Campo adicional para compatibilidade com versões anteriores
    private LocalDate dataRealizada;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    public ManutencaoRequestDTO() {
    }

    // Construtor com 10 parâmetros (compatível com chamadas existentes nos testes)
    public ManutencaoRequestDTO(Long ativoId,
                                TipoManutencao tipo,
                                Long solicitanteId,
                                Long fornecedorId,
                                Long tecnicoId,
                                String descricaoProblema,
                                BigDecimal custoEstimado,
                                LocalDate dataPrevistaConclusao,
                                LocalDate dataRealizada,
                                String observacoes) {
        this.ativoId = ativoId;
        this.tipo = tipo;
        this.solicitanteId = solicitanteId;
        this.fornecedorId = fornecedorId;
        this.tecnicoId = tecnicoId;
        this.descricaoProblema = descricaoProblema;
        this.custoEstimado = custoEstimado;
        this.dataPrevistaConclusao = dataPrevistaConclusao;
        this.dataRealizada = dataRealizada;
        this.observacoes = observacoes;
    }

    // Construtor alternativo com 8 parâmetros (similar ao record anterior)
    public ManutencaoRequestDTO(Long ativoId,
                                TipoManutencao tipo,
                                Long solicitanteId,
                                Long tecnicoId,
                                String descricaoProblema,
                                BigDecimal custoEstimado,
                                LocalDate dataPrevistaConclusao,
                                String observacoes) {
        this(ativoId, tipo, solicitanteId, null, tecnicoId, descricaoProblema, custoEstimado, dataPrevistaConclusao, null, observacoes);
    }

    // Getters e Setters
    public Long getAtivoId() { return ativoId; }
    public void setAtivoId(Long ativoId) { this.ativoId = ativoId; }

    public TipoManutencao getTipo() { return tipo; }
    public void setTipo(TipoManutencao tipo) { this.tipo = tipo; }

    public Long getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(Long solicitanteId) { this.solicitanteId = solicitanteId; }

    public Long getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(Long fornecedorId) { this.fornecedorId = fornecedorId; }

    public Long getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(Long tecnicoId) { this.tecnicoId = tecnicoId; }

    public String getDescricaoProblema() { return descricaoProblema; }
    public void setDescricaoProblema(String descricaoProblema) { this.descricaoProblema = descricaoProblema; }

    public BigDecimal getCustoEstimado() { return custoEstimado; }
    public void setCustoEstimado(BigDecimal custoEstimado) { this.custoEstimado = custoEstimado; }

    public LocalDate getDataPrevistaConclusao() { return dataPrevistaConclusao; }
    public void setDataPrevistaConclusao(LocalDate dataPrevistaConclusao) { this.dataPrevistaConclusao = dataPrevistaConclusao; }

    public LocalDate getDataRealizada() { return dataRealizada; }
    public void setDataRealizada(LocalDate dataRealizada) { this.dataRealizada = dataRealizada; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    // Métodos de conveniência compatíveis com record accessors
    public Long ativoId() { return getAtivoId(); }
    public TipoManutencao tipo() { return getTipo(); }
    public Long solicitanteId() { return getSolicitanteId(); }
    public Long fornecedorId() { return getFornecedorId(); }
    public Long tecnicoId() { return getTecnicoId(); }
    public String descricaoProblema() { return getDescricaoProblema(); }
    public BigDecimal custoEstimado() { return getCustoEstimado(); }
    public LocalDate dataPrevistaConclusao() { return getDataPrevistaConclusao(); }
    public LocalDate dataRealizada() { return getDataRealizada(); }
    public String observacoes() { return getObservacoes(); }
}
