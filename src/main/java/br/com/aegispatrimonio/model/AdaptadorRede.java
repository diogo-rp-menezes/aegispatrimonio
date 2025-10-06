package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "adaptadores_rede")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AdaptadorRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_detalhe_hardware_id", nullable = false)
    private AtivoDetalheHardware ativoDetalheHardware;

    @Column(name = "description")
    private String description;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "ip_addresses", columnDefinition = "TEXT")
    private String ipAddresses; // Armazenado como string JSON ou delimitado por vírgula
}
