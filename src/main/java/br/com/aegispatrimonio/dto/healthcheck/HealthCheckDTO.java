package br.com.aegispatrimonio.dto.healthcheck;

import java.util.List;

public record HealthCheckDTO(
    // Workstation Info
    String computerName,
    String domain,
    String osName,
    String osVersion,
    String osArchitecture,

    // Motherboard Info
    String motherboardManufacturer,
    String motherboardModel,
    String motherboardSerialNumber,

    // CPU Info
    String cpuModel,
    Integer cpuCores,
    Integer cpuThreads,

    // Component Lists
    List<DiscoDTO> discos,
    List<MemoriaDTO> memorias,
    List<AdaptadorRedeDTO> adaptadoresRede
) {
}
