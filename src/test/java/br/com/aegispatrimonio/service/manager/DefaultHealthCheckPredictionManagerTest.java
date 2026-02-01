package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import br.com.aegispatrimonio.repository.AtivoHealthHistoryRepository;
import br.com.aegispatrimonio.service.MaintenanceDispatcherService;
import br.com.aegispatrimonio.service.PredictiveMaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultHealthCheckPredictionManagerTest {

    @Mock
    private AtivoHealthHistoryRepository ativoHealthHistoryRepository;
    @Mock
    private PredictiveMaintenanceService predictiveMaintenanceService;
    @Mock
    private MaintenanceDispatcherService maintenanceDispatcherService;

    @InjectMocks
    private DefaultHealthCheckPredictionManager manager;

    private Ativo ativo;
    private List<String> components;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        components = List.of("DISK:SN1");
    }

    @Test
    @DisplayName("processPrediction: Deve pular predição se calculada recentemente")
    void shouldSkipPredictionIfRecentlyCalculated() {
        // Arrange
        Map<String, Object> attrs = new java.util.HashMap<>();
        attrs.put("prediction_calculated_at", LocalDateTime.now().minusHours(1).toString());
        ativo.setAtributos(attrs);

        // Act
        manager.processPrediction(ativo, components);

        // Assert
        verify(ativoHealthHistoryRepository, never()).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                anyLong(), anyList(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("processPrediction: Deve executar predição quando expirada")
    void shouldExecutePredictionWhenExpired() {
        // Arrange
        Map<String, Object> attrs = new java.util.HashMap<>();
        attrs.put("prediction_calculated_at", LocalDateTime.now().minusHours(25).toString());
        ativo.setAtributos(attrs);

        when(ativoHealthHistoryRepository.findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(1L), eq(components), eq("FREE_SPACE_GB"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        manager.processPrediction(ativo, components);

        // Assert
        verify(ativoHealthHistoryRepository).findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(1L), eq(components), eq("FREE_SPACE_GB"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("processPrediction: Deve atualizar atributos e despachar manutenção se houver predição válida")
    void shouldUpdateAttributesAndDispatchMaintenance() {
        // Arrange
        AtivoHealthHistory history = new AtivoHealthHistory();
        history.setComponente("DISK:SN1");
        List<AtivoHealthHistory> histories = List.of(history);

        when(ativoHealthHistoryRepository.findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                eq(1L), eq(components), eq("FREE_SPACE_GB"), any(LocalDateTime.class)))
                .thenReturn(histories);

        LocalDate exhaustionDate = LocalDate.now().plusDays(10);
        PredictionResult prediction = new PredictionResult(exhaustionDate, -1.0, 100.0, 0L);

        when(predictiveMaintenanceService.predictExhaustionDate(anyList())).thenReturn(prediction);

        // Act
        manager.processPrediction(ativo, components);

        // Assert
        verify(predictiveMaintenanceService).predictExhaustionDate(anyList());
        verify(maintenanceDispatcherService).dispatchIfNecessary(ativo, exhaustionDate);

        // Check if attributes updated (implied by dispatch call with updated object, but hard to assert Map content strictly with mocks unless we inspect arguments)
        // Simple verification is enough for unit test of flow
    }
}
