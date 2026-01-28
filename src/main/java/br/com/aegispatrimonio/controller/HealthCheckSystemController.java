package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.healthcheck.SystemHealthDTO;
import br.com.aegispatrimonio.service.IHealthCheckService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-check/system")
@PreAuthorize("hasRole('ADMIN')")
public class HealthCheckSystemController {

    private final IHealthCheckService healthCheckService;

    public HealthCheckSystemController(IHealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/last")
    public SystemHealthDTO getLast() {
        return healthCheckService.getLatestSystemHealth();
    }

    @GetMapping("/history")
    public Page<SystemHealthDTO> getHistory(Pageable pageable) {
        return healthCheckService.getSystemHealthHistory(pageable);
    }

    @GetMapping("/alerts")
    public List<SystemHealthDTO> getAlerts() {
        return healthCheckService.getRecentSystemAlerts();
    }
}
