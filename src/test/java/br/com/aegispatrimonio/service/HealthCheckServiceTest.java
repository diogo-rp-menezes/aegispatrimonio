package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.policy.HealthCheckAuthorizationPolicy;
import br.com.aegispatrimonio.service.manager.HealthCheckCollectionsManager;
import br.com.aegispatrimonio.service.updater.HealthCheckUpdater;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private AtivoDetalheHardwareRepository detalheHardwareRepository;
    @Mock
    private HealthCheckMapper healthCheckMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private HealthCheckAuthorizationPolicy healthCheckAuthorizationPolicy;
    @Mock
    private HealthCheckUpdater healthCheckUpdater;
    @Mock
    private HealthCheckCollectionsManager collectionsManager;
    @Mock
    private PredictiveMaintenanceService predictiveMaintenanceService;
    @Mock
    private AlertNotificationService alertNotificationService;
    @Mock
    private AtivoHealthHistoryRepository ativoHealthHistoryRepository;

    @InjectMocks
    private HealthCheckService healthCheckService;

    private Ativo ativo;
    private Usuario adminUser, regularUser, unauthorizedUser;
    private HealthCheckDTO healthCheckDTO;
    private AtivoDetalheHardware detalhesHardware;

    @BeforeEach
    void setUp() {
        Filial filialA = new Filial();
        filialA.setId(1L);

        Filial filialB = new Filial();
        filialB.setId(2L);

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setFilial(filialA);

        Funcionario adminFunc = new Funcionario();
        adminFunc.setFiliais(Set.of(filialA, filialB));
        adminUser = new Usuario();
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setFuncionario(adminFunc);

        Funcionario regularFunc = new Funcionario();
        regularFunc.setId(2L);
        regularFunc.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setRole("ROLE_USER");
        regularUser.setFuncionario(regularFunc);

        Funcionario unauthorizedFunc = new Funcionario();
        unauthorizedFunc.setId(3L);
        unauthorizedFunc.setFiliais(Set.of(filialB));
        unauthorizedUser = new Usuario();
        unauthorizedUser.setRole("ROLE_USER");
        unauthorizedUser.setFuncionario(unauthorizedFunc);

        healthCheckDTO = new HealthCheckDTO("PC-01", null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        detalhesHardware = new AtivoDetalheHardware();
        detalhesHardware.setId(1L);
        detalhesHardware.setAtivo(ativo);

        // Relaxed mocking for updateHealthCheck tests that might call currentUserProvider
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);
    }

    @Test
    @DisplayName("updateHealthCheck: Deve atualizar com sucesso para usuário ADMIN")
    void updateHealthCheck_comAdmin_deveAtualizarComSucesso() {
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.of(detalhesHardware));
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(healthCheckUpdater, times(1)).updateScalars(eq(1L), eq(detalhesHardware), eq(healthCheckDTO), eq(false));
        verify(collectionsManager, times(1)).replaceCollections(eq(detalhesHardware), eq(healthCheckDTO));
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
    }

    @Test
    @DisplayName("processHealthCheckPayload: Deve salvar histórico e atualizar hardware")
    void processHealthCheckPayload_shouldSaveHistory() {
        // Arrange
        DiskInfoDTO disk = new DiskInfoDTO("Model", "SN1", "SSD", 500.0, 200.0, 40.0);
        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                "PC-01", "DOM", "Win11", "22H2", "x64", "Dell", "X", "SN", "i7", 8, 16,
                50.0, 16000L, 8000L, List.of(disk)
        );

        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(ativoHealthHistoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        // Act
        healthCheckService.processHealthCheckPayload(1L, payload);

        // Assert
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
        verify(ativoRepository).save(ativo);
        verify(ativoHealthHistoryRepository).saveAll(anyList());
        // Verify alert notification
        verify(alertNotificationService).checkAndCreateAlerts(eq(1L), any(), eq(payload));

        // Verify hardware details update (indirectly via ativo state)
        // Since we didn't mock AtivoDetalheHardwareRepository save (it's cascading or handled inside logic),
        // we check if the ativo object was modified.
        // The implementation calls updateHardwareDetailsFromPayload which modifies the Ativo object.
    }

    @Test
    @DisplayName("processHealthCheckPayload: Deve pular predição se calculada recentemente")
    void processHealthCheckPayload_shouldSkipPredictionIfRecentlyCalculated() {
        // Arrange
        Map<String, Object> attrs = new java.util.HashMap<>();
        attrs.put("prediction_calculated_at", LocalDateTime.now().minusHours(1).toString());
        ativo.setAtributos(attrs);

        DiskInfoDTO disk = new DiskInfoDTO("Model", "SN1", "SSD", 500.0, 200.0, 40.0);
        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                "PC-01", null, null, null, null, null, null, null, null, null, null,
                null, null, null, List.of(disk)
        );

        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(ativoHealthHistoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        // Act
        healthCheckService.processHealthCheckPayload(1L, payload);

        // Assert
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
        // Should NOT fetch history for prediction
        verify(ativoHealthHistoryRepository, never()).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                anyLong(), anyList(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("processHealthCheckPayload: Deve executar predição quando expirada")
    void processHealthCheckPayload_shouldExecutePredictionWhenExpired() {
        // Arrange
        Map<String, Object> attrs = new java.util.HashMap<>();
        attrs.put("prediction_calculated_at", LocalDateTime.now().minusHours(25).toString());
        ativo.setAtributos(attrs);

        DiskInfoDTO disk = new DiskInfoDTO("Model", "SN1", "SSD", 500.0, 200.0, 40.0);
        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                "PC-01", null, null, null, null, null, null, null, null, null, null,
                null, null, null, List.of(disk)
        );

        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(ativoHealthHistoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(ativoHealthHistoryRepository.findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(1L), anyList(), eq("FREE_SPACE_GB"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        // Act
        healthCheckService.processHealthCheckPayload(1L, payload);

        // Assert
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
        verify(ativoHealthHistoryRepository).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(1L), anyList(), eq("FREE_SPACE_GB"), any(LocalDateTime.class));
    }
}
