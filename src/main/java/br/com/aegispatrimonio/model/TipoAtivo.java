package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "tipos_ativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SQLDelete(sql = "UPDATE tipos_ativo SET status = 'INATIVO' WHERE id = ?")
@Where(clause = "status = 'ATIVO'")
public class TipoAtivo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nome;
    
    private String descricao;
    
    @Column(name = "categoria_contabil", nullable = false)
    private String categoriaContabil;
    
    private String icone;

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