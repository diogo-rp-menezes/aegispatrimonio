package br.com.aegispatrimonio.dto;

public record AtivoDetalheHardwareDTO(
    String computerName,
    String domain,
    String osName,
    String osVersion,
    String osArchitecture,
    String motherboardManufacturer,
    String motherboardModel,
    String motherboardSerialNumber,
    String cpuModel,
    Integer cpuCores,
    Integer cpuThreads
) {}
