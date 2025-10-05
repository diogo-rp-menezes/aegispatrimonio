package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "localizacoes")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"localizacaoPai", "filial"})
@SQLDelete(sql = "UPDATE localizacoes SET status = 'INATIVO' WHERE id = ?")
@Where(clause = "status = 'ATIVO'")
public class Localizacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @ManyToOne
    @JoinColumn(name = "localizacao_pai_id")
    private Localizacao localizacaoPai;
    
    @ManyToOne
    @JoinColumn(name = "filial_id", nullable = false)
    private Filial filial;
    
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
    
    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        if (status == null) {
            status = Status.ATIVO;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}