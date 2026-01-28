package br.com.aegispatrimonio.dto.healthcheck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HealthCheckPayloadDTO(
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
    Integer cpuThreads,
    Double cpuLoad,
    Long memoryTotal,
    Long memoryAvailable,
    List<DiskInfoDTO> discos
) {}
