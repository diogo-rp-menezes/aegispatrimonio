package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "discos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Disco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_detalhe_hardware_id", nullable = false)
    private AtivoDetalheHardware ativoDetalheHardware;

    @Column(name = "model")
    private String model;

    @Column(name = "serial")
    private String serial;

    @Column(name = "type")
    private String type; // SSD ou HDD

    @Column(name = "total_gb", precision = 10, scale = 2)
    private BigDecimal totalGb;

    @Column(name = "free_gb", precision = 10, scale = 2)
    private BigDecimal freeGb;

    @Column(name = "free_percent")
    private Integer freePercent;
}
