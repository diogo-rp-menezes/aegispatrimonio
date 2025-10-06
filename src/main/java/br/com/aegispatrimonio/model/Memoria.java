package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Memoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_detalhe_hardware_id", nullable = false)
    private AtivoDetalheHardware ativoDetalheHardware;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "size_gb")
    private Integer sizeGb;
}
