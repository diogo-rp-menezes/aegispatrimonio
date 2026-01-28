package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ativo_detalhe_hardware")
@Getter
@Setter
@NoArgsConstructor
public class AtivoDetalheHardware {

    @Id
    private Long id; // Mesma chave primária do Ativo

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Mapeia a chave primária para a entidade Ativo
    @JoinColumn(name = "id")
    private Ativo ativo;

    // --- Workstation Info ---
    @Column(name = "nome_maquina")
    private String computerName;

    @Column(name = "dominio")
    private String domain;

    @Column(name = "sistema_operacional")
    private String osName;

    @Column(name = "versao_so")
    private String osVersion;

    @Column(name = "arquitetura_so")
    private String osArchitecture;

    // --- Motherboard Info ---
    @Column(name = "fabricante")
    private String motherboardManufacturer;

    @Column(name = "modelo")
    private String motherboardModel;

    @Column(name = "numero_serie")
    private String motherboardSerialNumber;

    // --- CPU Info ---
    @Column(name = "processador")
    private String cpuModel;

    @Column(name = "processadores_fisicos")
    private Integer cpuCores;

    @Column(name = "processadores_logicos")
    private Integer cpuThreads;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // Data da última atualização do Health Check

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
