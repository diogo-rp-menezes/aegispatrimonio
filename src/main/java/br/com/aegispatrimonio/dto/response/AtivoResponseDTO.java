package br.com.aegispatrimonio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtivoResponseDTO {

    private Long id;
    private String nome;
    private Long tipoAtivoId;
    private String tipoAtivoNome;
    private String numeroPatrimonio;
    private Long localizacaoId;
    private String localizacaoNome;
    private StatusAtivo status;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataAquisicao;
    
    private Long fornecedorId;
    private String fornecedorNome;
    private BigDecimal valorAquisicao;
    private BigDecimal valorResidual;
    private Integer vidaUtilMeses;
    private MetodoDepreciacao metodoDepreciacao;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInicioDepreciacao;
    
    private BigDecimal taxaDepreciacaoMensal;
    private BigDecimal depreciacaoAcumulada;
    private BigDecimal valorContabilAtual;
    private String informacoesGarantia;
    private Long pessoaResponsavelId;
    private String pessoaResponsavelNome;
    private String observacoes;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataRegistro;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadoEm;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime atualizadoEm;

    public static AtivoResponseDTO fromEntity(Ativo ativo) {
        AtivoResponseDTO dto = new AtivoResponseDTO();
        dto.setId(ativo.getId());
        dto.setNome(ativo.getNome());
        
        if (ativo.getTipoAtivo() != null) {
            dto.setTipoAtivoId(ativo.getTipoAtivo().getId());
            dto.setTipoAtivoNome(ativo.getTipoAtivo().getNome());
        }
        
        dto.setNumeroPatrimonio(ativo.getNumeroPatrimonio());
        
        if (ativo.getLocalizacao() != null) {
            dto.setLocalizacaoId(ativo.getLocalizacao().getId());
            dto.setLocalizacaoNome(ativo.getLocalizacao().getNome());
        }
        
        dto.setStatus(ativo.getStatus());
        dto.setDataAquisicao(ativo.getDataAquisicao());
        
        if (ativo.getFornecedor() != null) {
            dto.setFornecedorId(ativo.getFornecedor().getId());
            dto.setFornecedorNome(ativo.getFornecedor().getNome());
        }
        
        dto.setValorAquisicao(ativo.getValorAquisicao());
        dto.setValorResidual(ativo.getValorResidual());
        dto.setVidaUtilMeses(ativo.getVidaUtilMeses());
        dto.setMetodoDepreciacao(ativo.getMetodoDepreciacao());
        dto.setDataInicioDepreciacao(ativo.getDataInicioDepreciacao());
        dto.setTaxaDepreciacaoMensal(ativo.getTaxaDepreciacaoMensal());
        
        // Calcular depreciação acumulada e valor contábil
        if (ativo.getDataInicioDepreciacao() != null && ativo.getTaxaDepreciacaoMensal() != null) {
            dto.setDepreciacaoAcumulada(ativo.calcularDepreciacaoAtual());
            dto.setValorContabilAtual(ativo.getValorAquisicao().subtract(dto.getDepreciacaoAcumulada()));
        }
        
        dto.setInformacoesGarantia(ativo.getInformacoesGarantia());
        
        if (ativo.getPessoaResponsavel() != null) {
            dto.setPessoaResponsavelId(ativo.getPessoaResponsavel().getId());
            dto.setPessoaResponsavelNome(ativo.getPessoaResponsavel().getNome());
        }
        
        dto.setObservacoes(ativo.getObservacoes());
        dto.setDataRegistro(ativo.getDataRegistro());
        dto.setCriadoEm(ativo.getCriadoEm());
        dto.setAtualizadoEm(ativo.getAtualizadoEm());
        
        return dto;
    }
}