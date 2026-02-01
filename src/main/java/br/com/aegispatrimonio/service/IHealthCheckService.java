package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.dto.healthcheck.SystemHealthDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IHealthCheckService {
    void updateHealthCheck(Long ativoId, HealthCheckDTO dto);
    void performSystemHealthCheck();
    void processHealthCheckPayload(Long id, HealthCheckPayloadDTO payload);

    SystemHealthDTO getLatestSystemHealth();
    Page<SystemHealthDTO> getSystemHealthHistory(Pageable pageable);
    List<SystemHealthDTO> getRecentSystemAlerts();
}
