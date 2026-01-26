package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.dto.healthcheck.SystemHealthDTO;
import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoDetalheHardwareRepository;
import br.com.aegispatrimonio.repository.AtivoHealthHistoryRepository;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.HealthCheckHistoryRepository;
import br.com.aegispatrimonio.service.collector.OSHIHealthCheckCollector;
import br.com.aegispatrimonio.service.manager.HealthCheckCollectionsManager;
import br.com.aegispatrimonio.service.policy.HealthCheckAuthorizationPolicy;
import br.com.aegispatrimonio.service.updater.HealthCheckUpdater;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthCheckService implements IHealthCheckService {

    private final AtivoRepository ativoRepository;
    private final AtivoDetalheHardwareRepository detalheHardwareRepository;
    private final CurrentUserProvider currentUserProvider;
    private final HealthCheckAuthorizationPolicy authorizationPolicy;
    private final HealthCheckUpdater healthCheckUpdater;
    private final HealthCheckCollectionsManager collectionsManager;
    private final OSHIHealthCheckCollector oshiCollector;
    private final HealthCheckHistoryRepository healthCheckHistoryRepository;
    private final PredictiveMaintenanceService predictiveMaintenanceService;
    private final AlertNotificationService alertNotificationService;
    private final AtivoHealthHistoryRepository ativoHealthHistoryRepository;

    public HealthCheckService(AtivoRepository ativoRepository,
                              AtivoDetalheHardwareRepository detalheHardwareRepository,
                              CurrentUserProvider currentUserProvider,
                              HealthCheckAuthorizationPolicy authorizationPolicy,
                              HealthCheckUpdater healthCheckUpdater,
                              HealthCheckCollectionsManager collectionsManager,
                              OSHIHealthCheckCollector oshiCollector,
                              HealthCheckHistoryRepository healthCheckHistoryRepository,
                              PredictiveMaintenanceService predictiveMaintenanceService,
                              AlertNotificationService alertNotificationService,
                              AtivoHealthHistoryRepository ativoHealthHistoryRepository) {
        this.ativoRepository = ativoRepository;
        this.detalheHardwareRepository = detalheHardwareRepository;
        this.currentUserProvider = currentUserProvider;
        this.authorizationPolicy = authorizationPolicy;
        this.healthCheckUpdater = healthCheckUpdater;
        this.collectionsManager = collectionsManager;
        this.oshiCollector = oshiCollector;
        this.healthCheckHistoryRepository = healthCheckHistoryRepository;
        this.predictiveMaintenanceService = predictiveMaintenanceService;
        this.alertNotificationService = alertNotificationService;
        this.ativoHealthHistoryRepository = ativoHealthHistoryRepository;
    }

    @Override
    public void performSystemHealthCheck() {
        var history = oshiCollector.collect();
        healthCheckHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void updateHealthCheck(Long ativoId, HealthCheckDTO healthCheckDTO) {
        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        Ativo ativo = ativoRepository.findByIdWithDetails(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        authorizationPolicy.assertCanUpdate(usuarioLogado, ativo);

        DetalhesResult detalhesResult = findOrCreateDetalhes(ativo);
        AtivoDetalheHardware detalhes = detalhesResult.detalhes();
        boolean createdNow = detalhesResult.createdNow();

        healthCheckUpdater.updateScalars(ativo.getId(), detalhes, healthCheckDTO, createdNow);

        collectionsManager.replaceCollections(detalhes, healthCheckDTO);
    }

    @Override
    public SystemHealthDTO getLatestSystemHealth() {
        return healthCheckHistoryRepository.findTopByOrderByCreatedAtDesc()
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    public Page<SystemHealthDTO> getSystemHealthHistory(Pageable pageable) {
        return healthCheckHistoryRepository.findByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public List<SystemHealthDTO> getRecentSystemAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        BigDecimal cpuLimit = new BigDecimal("0.90");
        BigDecimal memLimit = new BigDecimal("0.10");
        return healthCheckHistoryRepository.findByCreatedAtAfterAndCpuUsageGreaterThanOrCreatedAtAfterAndMemFreePercentLessThanOrderByCreatedAtDesc(
                cutoff, cpuLimit, cutoff, memLimit
        ).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private SystemHealthDTO mapToDTO(br.com.aegispatrimonio.model.HealthCheckHistory h) {
        return new SystemHealthDTO(
                h.getId(),
                h.getCreatedAt(),
                h.getHost(),
                h.getCpuUsage(),
                h.getMemFreePercent(),
                h.getDisks(),
                h.getNets()
        );
    }

    @Override
    @Transactional
    public void processHealthCheckPayload(Long id, HealthCheckPayloadDTO payload) {
        Ativo ativo = ativoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        Usuario usuarioLogado = currentUserProvider.getCurrentUsuario();
        authorizationPolicy.assertCanUpdate(usuarioLogado, ativo);

        // Update basic hardware details directly from payload
        updateHardwareDetailsFromPayload(ativo, payload);

        // Process Disks and History
        List<AtivoHealthHistory> historyToSave = new java.util.ArrayList<>();
        List<String> componentsToFetch = new java.util.ArrayList<>();

        // 1. CPU Load
        if (payload.cpuLoad() != null) {
            AtivoHealthHistory history = new AtivoHealthHistory();
            history.setAtivo(ativo);
            history.setComponente("CPU");
            history.setMetrica("CPU_LOAD_PERCENT");
            history.setValor(payload.cpuLoad());
            historyToSave.add(history);
        }

        // 2. Memory
        if (payload.memoryTotal() != null && payload.memoryAvailable() != null && payload.memoryTotal() > 0) {
            double freePercent = (double) payload.memoryAvailable() / payload.memoryTotal();
            AtivoHealthHistory history = new AtivoHealthHistory();
            history.setAtivo(ativo);
            history.setComponente("RAM");
            history.setMetrica("MEM_FREE_PERCENT");
            history.setValor(freePercent);
            historyToSave.add(history);
        }

        // 3. Disks
        if (payload.discos() != null) {
            for (DiskInfoDTO disk : payload.discos()) {
                if (disk.freeGb() != null) {
                    AtivoHealthHistory history = new AtivoHealthHistory();
                    history.setAtivo(ativo);
                    history.setComponente("DISK:" + disk.serial());
                    history.setMetrica("FREE_SPACE_GB");
                    history.setValor(disk.freeGb());
                    historyToSave.add(history);
                    componentsToFetch.add("DISK:" + disk.serial());
                }
            }
        }

        if (!historyToSave.isEmpty()) {
            ativoHealthHistoryRepository.saveAll(historyToSave);

            // Prediction logic only runs if there are disks involved (for now)
            if (!componentsToFetch.isEmpty()) {
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
                            .findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(id, componentsToFetch, "FREE_SPACE_GB", cutoffDate);

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
                    }
                }
            }
        }

        ativoRepository.save(ativo);

        // Trigger alert check
        alertNotificationService.checkAndCreateAlerts(ativo.getId(), ativo.getPrevisaoEsgotamentoDisco(), payload);
    }

    private void updateHardwareDetailsFromPayload(Ativo ativo, HealthCheckPayloadDTO dto) {
        if (dto == null) {
            return;
        }

        AtivoDetalheHardware hardware = ativo.getDetalheHardware();
        if (hardware == null) {
            hardware = new AtivoDetalheHardware();
            hardware.setAtivo(ativo);
            ativo.setDetalheHardware(hardware);
        }

        hardware.setComputerName(dto.computerName());
        hardware.setDomain(dto.domain());
        hardware.setOsName(dto.osName());
        hardware.setOsVersion(dto.osVersion());
        hardware.setOsArchitecture(dto.osArchitecture());
        hardware.setMotherboardManufacturer(dto.motherboardManufacturer());
        hardware.setMotherboardModel(dto.motherboardModel());
        hardware.setMotherboardSerialNumber(dto.motherboardSerialNumber());
        hardware.setCpuModel(dto.cpuModel());
        hardware.setCpuCores(dto.cpuCores());
        hardware.setCpuThreads(dto.cpuThreads());
    }

    private DetalhesResult findOrCreateDetalhes(Ativo ativo) {
        AtivoDetalheHardware detalhes = detalheHardwareRepository.findById(ativo.getId()).orElse(null);
        boolean createdNow = (detalhes == null);

        if (createdNow) {
            detalhes = new AtivoDetalheHardware();
            detalhes.setAtivo(ativo);
            detalhes.setId(ativo.getId());
            detalhes = detalheHardwareRepository.saveAndFlush(detalhes);
        }
        return new DetalhesResult(detalhes, createdNow);
    }

    private record DetalhesResult(AtivoDetalheHardware detalhes, boolean createdNow) {}
}
