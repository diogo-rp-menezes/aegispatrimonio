package br.com.aegispatrimonio.dto;

import java.util.List;

public record HealthCheckDTO(
    // Workstation Info
    String computerName,
    String domain,
    String osName,
    String osVersion,

    // Motherboard Info
    String motherboardManufacturer,
    String motherboardModel,
    String motherboardSerialNumber,

    // CPU Info
    String cpuModel,
    String cpuArchitecture,
    Integer cpuCores,
    Integer cpuThreads,

    // Component Lists
    List<HealthCheckDiscoDTO> discos,
    List<HealthCheckMemoriaDTO> memorias,
    List<HealthCheckAdaptadorRedeDTO> adaptadoresRede
) {
}
