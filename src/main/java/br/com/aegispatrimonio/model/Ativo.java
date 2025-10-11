package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"tipoAtivo", "localizacao", "fornecedor", "funcionarioResponsavel", "filial"}) // CORREÇÃO
@Entity
@Table(name = "ativos")
@SQLDelete(sql = "UPDATE ativos SET status = 'BAIXADO' WHERE id = ?")
@Where(clause = "status <> 'BAIXADO'")
public class Ativo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id", nullable = false)
    private Filial filial;
    
    @Column(nullable = false)
    private String nome;
    
    @ManyToOne
    @JoinColumn(name = "tipo_ativo_id", nullable = false)
    private TipoAtivo tipoAtivo;
    
    @Column(name = "numero_patrimonio", nullable = false, unique = true)
    private String numeroPatrimonio;
    
    @ManyToOne
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacao;
    
    @Enumerated(EnumType.STRING)
    private StatusAtivo status;
    
    @Column(name = "data_aquisicao", nullable = false)
    private LocalDate dataAquisicao;
    
    @ManyToOne
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;
    
    @Column(name = "valor_aquisicao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAquisicao;
    
    @Column(name = "valor_residual", precision = 15, scale = 2)
    private BigDecimal valorResidual;
    
    @Column(name = "vida_util_meses")
    private Integer vidaUtilMeses;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_depreciacao")
    private MetodoDepreciacao metodoDepreciacao;
    
    @Column(name = "data_inicio_depreciacao")
    private LocalDate dataInicioDepreciacao;
    
    @Column(name = "taxa_depreciacao_mensal", precision = 15, scale = 6)
    private BigDecimal taxaDepreciacaoMensal;
    
    @Column(name = "informacoes_garantia")
    private String informacoesGarantia;
    
    // CORREÇÃO: Renomeado de pessoaResponsavel para funcionarioResponsavel
    @ManyToOne
    @JoinColumn(name = "funcionario_responsavel_id")
    private Funcionario funcionarioResponsavel;
    
    private String observacoes;
    
    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
    
    @Column(name = "depreciacao_acumulada", precision = 15, scale = 2)
    private BigDecimal depreciacaoAcumulada;

    @Column(name = "valor_contabil_atual", precision = 15, scale = 2)
    private BigDecimal valorContabilAtual;

    @Column(name = "data_ultima_depreciacao")
    private LocalDate dataUltimaDepreciacao;
    
    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        dataRegistro = LocalDate.now();
        
        if (status == null) {
            status = StatusAtivo.ATIVO;
        }
        if (valorResidual == null) {
            valorResidual = BigDecimal.ZERO;
        }
        if (metodoDepreciacao == null) {
            metodoDepreciacao = MetodoDepreciacao.LINEAR;
        }
    }
    
    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
    
    public BigDecimal calcularDepreciacaoAtual() {
        return BigDecimal.ZERO;
    }
}
