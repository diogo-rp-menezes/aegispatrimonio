package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.HealthCheckHistory;
import br.com.aegispatrimonio.repository.HealthCheckHistoryRepository;
import br.com.aegispatrimonio.service.IHealthCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HealthCheckIntegrationTest extends BaseIT {

    @Autowired
    private IHealthCheckService healthCheckService;

    @Autowired
    private HealthCheckHistoryRepository healthCheckHistoryRepository;

    @Test
    void shouldPerformSystemHealthCheckAndSaveHistory() {
        // Act
        assertDoesNotThrow(() -> healthCheckService.performSystemHealthCheck());

        // Assert
        List<HealthCheckHistory> history = healthCheckHistoryRepository.findAll();
        assertThat(history).hasSize(1);

        HealthCheckHistory record = history.get(0);
        assertThat(record.getHost()).isNotNull();
        assertThat(record.getCpuUsage()).isNotNull();
        assertThat(record.getMemFreePercent()).isNotNull();
        assertThat(record.getDisks()).isNotNull().startsWith("[");
        assertThat(record.getNets()).isNotNull().startsWith("[");

        System.out.println("Collected Host: " + record.getHost());
        System.out.println("CPU Usage: " + record.getCpuUsage());
        System.out.println("Disks: " + record.getDisks());
    }
}
