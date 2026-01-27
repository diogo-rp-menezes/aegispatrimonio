package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.TipoAlerta;
import br.com.aegispatrimonio.repository.AlertaRepository;
import br.com.aegispatrimonio.repository.AtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private AlertNotificationService service;

    private Ativo ativo;
    private HealthCheckPayloadDTO emptyPayload;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Test Asset");
        emptyPayload = new HealthCheckPayloadDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void shouldCreateCriticalAlertWhenPredictionIsBelow7Days() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        // Changed to findByAtivoIdAndLidoFalse
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, ativo.getPrevisaoEsgotamentoDisco(), emptyPayload);

        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void shouldCreateWarningAlertWhenPredictionIsBetween7And30Days() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(20));
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, ativo.getPrevisaoEsgotamentoDisco(), emptyPayload);

        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void shouldNotCreateAlertIfAlreadyExists() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);

        Alerta existingAlert = new Alerta();
        existingAlert.setTipo(TipoAlerta.CRITICO);
        List<Alerta> existingList = new ArrayList<>();
        existingList.add(existingAlert);

        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(existingList);

        service.checkAndCreateAlerts(1L, ativo.getPrevisaoEsgotamentoDisco(), emptyPayload);

        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void shouldNotCreateAlertIfSafe() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(40));

        // Note: findByAtivoIdAndLidoFalse is called even if safe?
        // checkAndCreateAlerts calls findBy... first.
        // So we might need to mock it if we don't want NPE, or it might be called.
        // But since checkDiskPredictiveAlerts returns early if > 30 days, maybe save is not called.
        // But checkResourceUsageAlerts is also called.
        // Let's check logic:
        // checkAndCreateAlerts calls:
        //   getReferenceById
        //   findByAtivoIdAndLidoFalse
        //   checkDiskPredictiveAlerts
        //   checkResourceUsageAlerts
        // So we definitely need to mock findByAtivoIdAndLidoFalse.

        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo); // Need this for getReferenceById
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, ativo.getPrevisaoEsgotamentoDisco(), emptyPayload);

        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void shouldCreateCriticalAlertWhenCpuIsHigh() {
        HealthCheckPayloadDTO highCpuPayload = new HealthCheckPayloadDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                0.95, // cpuLoad
                16000L, 8000L, // memory safe
                null
        );
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, null, highCpuPayload);

        verify(alertaRepository).save(argThat(a ->
            a.getTitulo().contains("Sobrecarga de CPU") && a.getTipo() == TipoAlerta.CRITICO
        ));
    }

    @Test
    void shouldCreateCriticalAlertWhenMemoryIsLow() {
        HealthCheckPayloadDTO lowMemPayload = new HealthCheckPayloadDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                0.10, // cpu safe
                1000L, 50L, // memory: 50/1000 = 0.05 (5%)
                null
        );
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, null, lowMemPayload);

        verify(alertaRepository).save(argThat(a ->
            a.getTitulo().contains("Memória RAM") && a.getTipo() == TipoAlerta.CRITICO
        ));
    }

    @Test
    void shouldCreateCriticalAlertWhenDiskSpaceIsLow() {
        DiskInfoDTO lowSpaceDisk = new DiskInfoDTO("SSD", "SN123", "NVMe", 500.0, 40.0, 0.08); // 8% free
        HealthCheckPayloadDTO lowDiskPayload = new HealthCheckPayloadDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                0.10, // cpu safe
                16000L, 8000L, // memory safe
                Collections.singletonList(lowSpaceDisk)
        );
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, null, lowDiskPayload);

        verify(alertaRepository).save(argThat(a ->
            a.getTitulo().contains("Espaço em Disco Crítico") &&
            a.getMensagem().contains("SSD") &&
            a.getTipo() == TipoAlerta.CRITICO
        ));
    }

    @Test
    void shouldCreateCriticalAlertWhenDiskSpaceIsLowCalculated() {
        // 500GB total, 40GB free = 8% free (Critical < 10%)
        // freePercent is NULL to simulate "dumb" agent
        DiskInfoDTO lowSpaceDisk = new DiskInfoDTO("SSD", "SN123", "NVMe", 500.0, 40.0, null);
        HealthCheckPayloadDTO lowDiskPayload = new HealthCheckPayloadDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                0.10, // cpu safe
                16000L, 8000L, // memory safe
                Collections.singletonList(lowSpaceDisk)
        );
        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, null, lowDiskPayload);

        verify(alertaRepository).save(argThat(a ->
            a.getTitulo().contains("Espaço em Disco Crítico") &&
            a.getMensagem().contains("SSD") &&
            a.getTipo() == TipoAlerta.CRITICO
        ));
    }
}
