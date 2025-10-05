package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"departamento"})
@Entity
@Table(name = "pessoas")
@SQLDelete(sql = "UPDATE pessoas SET status = 'INATIVO' WHERE id = ?")
@Where(clause = "status = 'ATIVO'")
public class Pessoa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(unique = true)
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

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
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}