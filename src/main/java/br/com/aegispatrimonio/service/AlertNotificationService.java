package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AlertaRepository;
import br.com.aegispatrimonio.repository.AtivoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class AlertNotificationService {

    private final AlertaRepository alertaRepository;
    private final AtivoRepository ativoRepository;
    private final UserContextService userContextService;

    public AlertNotificationService(AlertaRepository alertaRepository,
            AtivoRepository ativoRepository,
            UserContextService userContextService) {
        this.alertaRepository = alertaRepository;
        this.ativoRepository = ativoRepository;
        this.userContextService = userContextService;
    }

    @Transactional(readOnly = true)
    public List<Alerta> getRecentAlerts() {
        if (userContextService.isAdmin()) {
            return alertaRepository.findTop5ByLidoFalseOrderByDataCriacaoDesc();
        }
        Set<Long> filialIds = userContextService.getUserFiliais();
        return alertaRepository.findTop5ByAtivo_Filial_IdInAndLidoFalseOrderByDataCriacaoDesc(filialIds);
    }

    @Transactional(readOnly = true)
    public Page<Alerta> listarAlertas(Pageable pageable, Boolean lido) {
        if (userContextService.isAdmin()) {
            if (lido == null) {
                return alertaRepository.findAll(pageable);
            } else {
                return alertaRepository.findByLido(lido, pageable);
            }
        } else {
            Set<Long> filialIds = userContextService.getUserFiliais();
            if (lido == null) {
                return alertaRepository.findByAtivo_Filial_IdIn(filialIds, pageable);
            } else {
                return alertaRepository.findByAtivo_Filial_IdInAndLido(filialIds, lido, pageable);
            }
        }
    }

    @Transactional
    public void markAsRead(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta não encontrado: " + id));

        if (!userContextService.isAdmin()) {
            Set<Long> filialIds = userContextService.getUserFiliais();
            if (!filialIds.contains(alerta.getAtivo().getFilial().getId())) {
                throw new AccessDeniedException("Acesso negado ao alerta desta filial.");
            }
        }

        alerta.setLido(true);
        alerta.setDataLeitura(LocalDateTime.now());
        alertaRepository.save(alerta);
    }

    @Async("taskExecutor")
    @Transactional
    public void checkAndCreateAlerts(Long ativoId, LocalDate previsaoEsgotamentoDisco, HealthCheckPayloadDTO payload) {
        Ativo ativoRef = ativoRepository.getReferenceById(ativoId);
        List<Alerta> existingAlerts = alertaRepository.findByAtivoIdAndLidoFalse(ativoId);
        checkDiskPredictiveAlerts(ativoRef, previsaoEsgotamentoDisco, existingAlerts);
        checkResourceUsageAlerts(ativoRef, payload, existingAlerts);
    }

    private void checkResourceUsageAlerts(Ativo ativo, HealthCheckPayloadDTO payload, List<Alerta> existingAlerts) {
        // CPU Check (> 90%)
        if (payload.cpuLoad() != null && payload.cpuLoad() > 0.90) {
            createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Sobrecarga de CPU Detectada",
                    "O uso de CPU está em " + String.format("%.1f", payload.cpuLoad() * 100)
                            + "%. Verifique processos travados.",
                    existingAlerts);
        }

        // Memory Check (< 10% free)
        if (payload.memoryTotal() != null && payload.memoryAvailable() != null && payload.memoryTotal() > 0) {
            double freePercent = (double) payload.memoryAvailable() / payload.memoryTotal();
            if (freePercent < 0.10) {
                createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Memória RAM Crítica",
                        "Memória livre está abaixo de 10% (" + String.format("%.1f", freePercent * 100)
                                + "%). Risco de travamento.",
                        existingAlerts);
            }
        }

        // Disk Check (< 10% free)
        if (payload.discos() != null) {
            for (DiskInfoDTO disk : payload.discos()) {
                Double freePercent = disk.freePercent();
                if (freePercent == null && disk.totalGb() != null && disk.freeGb() != null && disk.totalGb() > 0) {
                    freePercent = disk.freeGb() / disk.totalGb();
                }

                if (freePercent != null && freePercent < 0.10) {
                    String diskName = disk.model() != null ? disk.model()
                            : (disk.serial() != null ? disk.serial() : "Desconhecido");
                    createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Espaço em Disco Crítico",
                            "O disco " + diskName + " está com menos de 10% de espaço livre (" +
                                    String.format("%.1f", freePercent * 100) + "%). Risco de parada.",
                            existingAlerts);
                }
            }
        }
    }

    private void checkDiskPredictiveAlerts(Ativo ativo, LocalDate previsaoEsgotamentoDisco,
            List<Alerta> existingAlerts) {
        if (previsaoEsgotamentoDisco == null) {
            return;
        }

        LocalDate now = LocalDate.now();
        long daysUntilExhaustion = ChronoUnit.DAYS.between(now, previsaoEsgotamentoDisco);

        // Se a previsão já passou (negativo) ou é hoje/amanhã, também é crítico
        if (daysUntilExhaustion < 7) {
            createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Risco Crítico de Falha de Disco",
                    "A previsão de esgotamento do disco é para " + daysUntilExhaustion + " dias ("
                            + previsaoEsgotamentoDisco + "). Ação imediata necessária.",
                    existingAlerts);
        } else if (daysUntilExhaustion < 30) {
            createAlertIfNotExists(ativo, TipoAlerta.WARNING, "Alerta de Capacidade de Disco",
                    "A previsão de esgotamento do disco é para " + daysUntilExhaustion + " dias ("
                            + previsaoEsgotamentoDisco + "). Planeje a manutenção.",
                    existingAlerts);
        }
    }

    private void createAlertIfNotExists(Ativo ativo, TipoAlerta tipo, String titulo, String mensagem,
            List<Alerta> existingAlerts) {
        // Evita criar múltiplos alertas não lidos do mesmo tipo para o mesmo ativo
        boolean exists = existingAlerts.stream().anyMatch(a -> a.getTipo() == tipo);
        if (!exists) {
            Alerta alerta = new Alerta();
            alerta.setAtivo(ativo);
            alerta.setTipo(tipo);
            alerta.setTitulo(titulo);
            alerta.setMensagem(mensagem);
            alertaRepository.save(alerta);
            existingAlerts.add(alerta);
        }
    }
}
