package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "manutencoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoManutencao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusManutencao status;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "data_prevista_conclusao")
    private LocalDate dataPrevistaConclusao;

    private String descricaoProblema;

    private String descricaoServico;

    @Column(precision = 15, scale = 2)
    private BigDecimal custoEstimado;

    @Column(precision = 15, scale = 2)
    private BigDecimal custoReal;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Pessoa solicitante;

    @ManyToOne
    @JoinColumn(name = "tecnico_responsavel_id")
    private Pessoa tecnicoResponsavel;

    private Integer tempoExecucaoMinutos;

    private String observacoes;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        if (status == null) {
            status = StatusManutencao.SOLICITADA;
        }
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}