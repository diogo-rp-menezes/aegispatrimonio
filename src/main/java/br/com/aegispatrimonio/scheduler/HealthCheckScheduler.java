package br.com.aegispatrimonio.scheduler;

import br.com.aegispatrimonio.service.IHealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);
    private final IHealthCheckService healthCheckService;

    public HealthCheckScheduler(IHealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Scheduled(cron = "${aegis.healthcheck.collect.cron:0 0 0/12 * * *}")
    public void collectSystemMetrics() {
        log.info("Starting scheduled system health check collection...");
        try {
            healthCheckService.performSystemHealthCheck();
            log.info("System health check collected successfully.");
        } catch (Exception e) {
            log.error("Failed to collect system health check metrics", e);
        }
    }
}
