package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.collector.OSHIHealthCheckCollector;
import br.com.aegispatrimonio.service.manager.HealthCheckCollectionsManager;
import br.com.aegispatrimonio.service.policy.HealthCheckAuthorizationPolicy;
import br.com.aegispatrimonio.service.updater.HealthCheckUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceBenchmarkTest {

    @Mock private AtivoRepository ativoRepository;
    @Mock private AtivoDetalheHardwareRepository detalheHardwareRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private HealthCheckAuthorizationPolicy authorizationPolicy;
    @Mock private HealthCheckUpdater healthCheckUpdater;
    @Mock private HealthCheckCollectionsManager collectionsManager;
    @Mock private OSHIHealthCheckCollector oshiCollector;
    @Mock private HealthCheckHistoryRepository healthCheckHistoryRepository;
    @Mock private PredictiveMaintenanceService predictiveMaintenanceService;
    @Mock private AlertNotificationService alertNotificationService;
    @Mock private AtivoHealthHistoryRepository ativoHealthHistoryRepository;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    void testProcessHealthCheckPayload_NPlusOneQuery() {
        // Arrange
        Long ativoId = 1L;
        int diskCount = 50;
        List<DiskInfoDTO> disks = new ArrayList<>();
        for (int i = 0; i < diskCount; i++) {
            disks.add(new DiskInfoDTO("Model" + i, "SN" + i, "Type" + i, 1000.0, 500.0, 50.0));
        }

        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                "PC-TEST", "DOMAIN", "OS", "1.0", "x64",
                "MoboMaker", "MoboModel", "MoboSN",
                "CPU", 4, 8,
                0.5, 16000L, 8000L,
                disks
        );

        Ativo ativo = new Ativo();
        ativo.setId(ativoId);
        ativo.setAtributos(new java.util.HashMap<>());

        when(ativoRepository.findByIdWithDetails(ativoId)).thenReturn(Optional.of(ativo));

        // Mock optimization: findBy...ComponenteIn... must be called, not individual saves or finds
        when(ativoHealthHistoryRepository.findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(ativoId), anyList(), eq("FREE_SPACE_GB"), any(java.time.LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        healthCheckService.processHealthCheckPayload(ativoId, payload);

        // Assert
        // Verify old methods are NOT called (N+1 check)
        verify(ativoHealthHistoryRepository, never()).findByAtivoIdAndMetricaOrderByDataRegistroAsc(any(), any());
        verify(ativoHealthHistoryRepository, never()).save(any(AtivoHealthHistory.class)); // saveAll should be used

        // Verify new optimized methods are called exactly once
        verify(ativoHealthHistoryRepository, times(1)).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(ativoId), anyList(), eq("FREE_SPACE_GB"), any(java.time.LocalDateTime.class));
        verify(ativoHealthHistoryRepository, times(1)).saveAll(anyList());
    }
}
