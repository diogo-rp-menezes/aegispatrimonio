package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "movimentacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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

    @ManyToOne
    @JoinColumn(name = "pessoa_origem_id", nullable = false)
    private Pessoa pessoaOrigem;

    @ManyToOne
    @JoinColumn(name = "pessoa_destino_id", nullable = false)
    private Pessoa pessoaDestino;

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
        atualizadoEm = LocalDateTime.now();
    }
}