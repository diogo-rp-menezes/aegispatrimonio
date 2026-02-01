package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "manutencoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"ativo", "fornecedor", "solicitante", "tecnicoResponsavel"})
@SQLDelete(sql = "UPDATE manutencoes SET status = 'CANCELADA' WHERE id = ?")
@Where(clause = "status <> 'CANCELADA'")
@Audited
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
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

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    // CORREÇÃO: Alterado de Pessoa para Funcionario
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Funcionario solicitante;

    // CORREÇÃO: Alterado de Pessoa para Funcionario
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "tecnico_responsavel_id")
    private Funcionario tecnicoResponsavel;

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
