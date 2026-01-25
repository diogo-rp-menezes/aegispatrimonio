package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;

public interface IHealthCheckService {
    void updateHealthCheck(Long ativoId, HealthCheckDTO dto);
    void performSystemHealthCheck();
    void processHealthCheckPayload(Long id, HealthCheckPayloadDTO payload);
}
