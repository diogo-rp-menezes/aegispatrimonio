package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import br.com.aegispatrimonio.repository.AtivoHealthHistoryRepository;
import br.com.aegispatrimonio.service.MaintenanceDispatcherService;
import br.com.aegispatrimonio.service.PredictiveMaintenanceService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of HealthCheckPredictionManager.
 * Orchestrates the predictive maintenance workflow: throttling -> fetching -> prediction -> update -> dispatch.
 */
@Component
public class DefaultHealthCheckPredictionManager implements HealthCheckPredictionManager {

    private final AtivoHealthHistoryRepository ativoHealthHistoryRepository;
    private final PredictiveMaintenanceService predictiveMaintenanceService;
    private final MaintenanceDispatcherService maintenanceDispatcherService;

    public DefaultHealthCheckPredictionManager(AtivoHealthHistoryRepository ativoHealthHistoryRepository,
                                               PredictiveMaintenanceService predictiveMaintenanceService,
                                               MaintenanceDispatcherService maintenanceDispatcherService) {
        this.ativoHealthHistoryRepository = ativoHealthHistoryRepository;
        this.predictiveMaintenanceService = predictiveMaintenanceService;
        this.maintenanceDispatcherService = maintenanceDispatcherService;
    }

    @Override
    public void processPrediction(Ativo ativo, List<String> componentsToFetch) {
        if (ativo == null || componentsToFetch == null || componentsToFetch.isEmpty()) {
            return;
        }

        // Throttling: Check if prediction was calculated recently (< 24h)
        boolean shouldRecalculate = true;
        if (ativo.getAtributos() != null && ativo.getAtributos().containsKey("prediction_calculated_at")) {
            try {
                java.time.LocalDateTime lastCalc = java.time.LocalDateTime.parse(ativo.getAtributos().get("prediction_calculated_at").toString());
                if (lastCalc.plusHours(24).isAfter(java.time.LocalDateTime.now())) {
                    shouldRecalculate = false;
                }
            } catch (Exception e) {
                // Ignore parse errors and recalculate
            }
        }

        if (shouldRecalculate) {
            // Fetch history for all components at once (Sliding Window: 90 days)
            java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(90);
            List<AtivoHealthHistory> allHistory = ativoHealthHistoryRepository
                    .findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(
                            ativo.getId(), componentsToFetch, "FREE_SPACE_GB", cutoffDate);

            // Group by component
            java.util.Map<String, List<AtivoHealthHistory>> historyByComponent = allHistory.stream()
                    .collect(Collectors.groupingBy(AtivoHealthHistory::getComponente));

            PredictionResult worstPredictionResult = null;

            for (List<AtivoHealthHistory> componentHistory : historyByComponent.values()) {
                PredictionResult prediction = predictiveMaintenanceService.predictExhaustionDate(componentHistory);
                if (prediction != null && prediction.exhaustionDate() != null) {
                    if (worstPredictionResult == null ||
                            prediction.exhaustionDate().isBefore(worstPredictionResult.exhaustionDate())) {
                        worstPredictionResult = prediction;
                    }
                }
            }

            // Store worst prediction in attributes
            if (worstPredictionResult != null) {
                if (ativo.getAtributos() == null) {
                    ativo.setAtributos(new java.util.HashMap<>());
                }
                ativo.getAtributos().put("previsaoEsgotamentoDisco", worstPredictionResult.exhaustionDate().toString());
                ativo.getAtributos().put("prediction_slope", worstPredictionResult.slope());
                ativo.getAtributos().put("prediction_intercept", worstPredictionResult.intercept());
                ativo.getAtributos().put("prediction_base_epoch_day", worstPredictionResult.baseEpochDay());
                ativo.getAtributos().put("prediction_calculated_at", java.time.LocalDateTime.now().toString());

                ativo.setPrevisaoEsgotamentoDisco(worstPredictionResult.exhaustionDate());

                // Autonomous dispatch
                maintenanceDispatcherService.dispatchIfNecessary(ativo, worstPredictionResult.exhaustionDate());
            }
        }
    }
}
