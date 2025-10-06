package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ativo_detalhes_hardware")
@Getter
@Setter
@NoArgsConstructor
public class AtivoDetalheHardware {

    @Id
    private Long id; // Mesma chave primária do Ativo

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Mapeia a chave primária para a entidade Ativo
    @JoinColumn(name = "id")
    private Ativo ativo;

    // --- Workstation Info ---
    @Column(name = "computer_name")
    private String computerName;

    @Column(name = "domain")
    private String domain;

    @Column(name = "os_name")
    private String osName;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "os_architecture")
    private String osArchitecture;

    // --- Motherboard Info ---
    @Column(name = "motherboard_manufacturer")
    private String motherboardManufacturer;

    @Column(name = "motherboard_model")
    private String motherboardModel;

    @Column(name = "motherboard_serial_number")
    private String motherboardSerialNumber;

    // --- CPU Info ---
    @Column(name = "cpu_model")
    private String cpuModel;

    @Column(name = "cpu_cores")
    private Integer cpuCores;

    @Column(name = "cpu_threads")
    private Integer cpuThreads;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // Data da última atualização do Health Check

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
