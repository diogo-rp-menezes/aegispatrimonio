package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;

public interface IHealthCheckService {
    void updateHealthCheck(Long ativoId, HealthCheckDTO dto);
    void performSystemHealthCheck();
}
