package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ativo_health_history")
@Getter
@Setter
@NoArgsConstructor
public class AtivoHealthHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(nullable = false)
    private String componente; // Ex: "DISK-0"

    @Column(nullable = false)
    private Double valor; // Ex: 100.0 (Free GB)

    @Column(nullable = false)
    private String metrica; // Ex: "FREE_SPACE_GB"

    @PrePersist
    protected void onCreate() {
        if (dataRegistro == null) {
            dataRegistro = LocalDateTime.now();
        }
    }
}
