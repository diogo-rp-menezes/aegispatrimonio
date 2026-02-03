package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "ativo", "localizacaoOrigem", "localizacaoDestino", "funcionarioOrigem", "funcionarioDestino" })
@SQLDelete(sql = "UPDATE movimentacoes SET status = 'CANCELADA' WHERE id = ?")
@SQLRestriction("status <> 'CANCELADA'")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @ManyToOne
    @JoinColumn(name = "localizacao_origem_id", nullable = false)
    private Localizacao localizacaoOrigem;

    @ManyToOne
    @JoinColumn(name = "localizacao_destino_id", nullable = false)
    private Localizacao localizacaoDestino;

    // CORREÇÃO: Alterado de Pessoa para Funcionario
    @ManyToOne
    @JoinColumn(name = "funcionario_origem_id", nullable = false)
    private Funcionario funcionarioOrigem;

    // CORREÇÃO: Alterado de Pessoa para Funcionario
    @ManyToOne
    @JoinColumn(name = "funcionario_destino_id", nullable = false)
    private Funcionario funcionarioDestino;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "data_efetivacao")
    private LocalDate dataEfetivacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMovimentacao status;

    private String motivo;

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
            status = StatusMovimentacao.PENDENTE;
        }
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
