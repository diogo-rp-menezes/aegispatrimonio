package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.mapper.AtivoMapper;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import br.com.aegispatrimonio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtivoServiceBenchmarkTest {

    @Mock private AtivoRepository ativoRepository;
    @Mock private AtivoMapper ativoMapper;
    @Mock private TipoAtivoRepository tipoAtivoRepository;
    @Mock private LocalizacaoRepository localizacaoRepository;
    @Mock private FornecedorRepository fornecedorRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private FilialRepository filialRepository;
    @Mock private ManutencaoRepository manutencaoRepository;
    @Mock private MovimentacaoRepository movimentacaoRepository;
    @Mock private DepreciacaoService depreciacaoService;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private AtivoHealthHistoryRepository healthHistoryRepository;
    @Mock private PredictiveMaintenanceService predictiveMaintenanceService;
    @Mock private SearchOptimizationService searchOptimizationService;
    @Mock private AlertNotificationService alertNotificationService;

    @InjectMocks
    private AtivoService ativoService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testProcessarHealthCheck_NPlusOneQuery() {
        // Arrange
        Long ativoId = 1L;
        int diskCount = 50;
        List<DiskInfoDTO> disks = new ArrayList<>();
        for (int i = 0; i < diskCount; i++) {
            // DiskInfoDTO(String model, String serial, String type, Double totalGb, Double freeGb, Double freePercent)
            disks.add(new DiskInfoDTO("Model" + i, "SN" + i, "Type" + i, 1000.0, 500.0, 50.0));
        }

        // HealthCheckPayloadDTO(computerName, domain, osName, osVersion, osArchitecture, motherboardManufacturer, motherboardModel, motherboardSerialNumber, cpuModel, cpuCores, cpuThreads, discos)
        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                "PC-TEST", "DOMAIN", "OS", "1.0", "x64",
                "MoboMaker", "MoboModel", "MoboSN",
                "CPU", 4, 8,
                0.5, 16000L, 8000L, // Added CPU/Memory params
                disks
        );

        Ativo ativo = new Ativo();
        ativo.setId(ativoId);
        ativo.setAtributos(new java.util.HashMap<>());

        when(ativoRepository.findByIdWithDetails(ativoId)).thenReturn(Optional.of(ativo));
        when(healthHistoryRepository.findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(eq(ativoId), anyList(), eq("FREE_SPACE_GB"), any(java.time.LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        ativoService.processarHealthCheck(ativoId, payload);

        // Assert
        // Verify old methods are NOT called
        verify(healthHistoryRepository, never()).findByAtivoIdAndMetricaOrderByDataRegistroAsc(any(), any());
        verify(healthHistoryRepository, never()).save(any(AtivoHealthHistory.class));

        // Verify new optimized methods are called exactly once
        verify(healthHistoryRepository, times(1)).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(eq(ativoId), anyList(), eq("FREE_SPACE_GB"), any(java.time.LocalDateTime.class));
        verify(healthHistoryRepository, times(1)).saveAll(anyList());
    }
}
