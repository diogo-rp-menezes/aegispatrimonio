package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AlertaRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlertNotificationService {

    private final AlertaRepository alertaRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FuncionarioRepository funcionarioRepository;

    public AlertNotificationService(AlertaRepository alertaRepository,
                                    CurrentUserProvider currentUserProvider,
                                    FuncionarioRepository funcionarioRepository) {
        this.alertaRepository = alertaRepository;
        this.currentUserProvider = currentUserProvider;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Alerta> getRecentAlerts() {
        Usuario user = getUsuarioLogado();
        if (isAdmin(user)) {
            return alertaRepository.findTop5ByLidoFalseOrderByDataCriacaoDesc();
        }
        Set<Long> filialIds = getUserFiliais(user);
        return alertaRepository.findTop5ByAtivo_Filial_IdInAndLidoFalseOrderByDataCriacaoDesc(filialIds);
    }

    @Transactional(readOnly = true)
    public Page<Alerta> listarAlertas(Pageable pageable, Boolean lido) {
        Usuario user = getUsuarioLogado();
        if (isAdmin(user)) {
            if (lido == null) {
                return alertaRepository.findAll(pageable);
            } else {
                return alertaRepository.findByLido(lido, pageable);
            }
        } else {
            Set<Long> filialIds = getUserFiliais(user);
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

        Usuario user = getUsuarioLogado();
        if (!isAdmin(user)) {
            Set<Long> filialIds = getUserFiliais(user);
            if (!filialIds.contains(alerta.getAtivo().getFilial().getId())) {
                throw new AccessDeniedException("Acesso negado ao alerta desta filial.");
            }
        }

        alerta.setLido(true);
        alerta.setDataLeitura(LocalDateTime.now());
        alertaRepository.save(alerta);
    }

    private Usuario getUsuarioLogado() {
        return currentUserProvider.getCurrentUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }

    private Set<Long> getUserFiliais(Usuario usuario) {
        Funcionario funcionario = usuario.getFuncionario();
        if (funcionario == null) {
            throw new AccessDeniedException("Usuário não vinculado a um funcionário.");
        }
        // Fetch to ensure loaded
        Optional<Funcionario> fOpt = funcionarioRepository.findById(funcionario.getId());
        if (fOpt.isEmpty()) {
            throw new AccessDeniedException("Funcionário não encontrado.");
        }
        Set<Long> ids = fOpt.get().getFiliais().stream().map(Filial::getId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            throw new AccessDeniedException("Funcionário sem filiais vinculadas.");
        }
        return ids;
    }

    @Transactional
    public void checkAndCreateAlerts(Ativo ativo, HealthCheckPayloadDTO payload) {
        checkDiskPredictiveAlerts(ativo);
        checkResourceUsageAlerts(ativo, payload);
    }

    private void checkResourceUsageAlerts(Ativo ativo, HealthCheckPayloadDTO payload) {
        // CPU Check (> 90%)
        if (payload.cpuLoad() != null && payload.cpuLoad() > 0.90) {
            createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Sobrecarga de CPU Detectada",
                    "O uso de CPU está em " + String.format("%.1f", payload.cpuLoad() * 100) + "%. Verifique processos travados.");
        }

        // Memory Check (< 10% free)
        if (payload.memoryTotal() != null && payload.memoryAvailable() != null && payload.memoryTotal() > 0) {
            double freePercent = (double) payload.memoryAvailable() / payload.memoryTotal();
            if (freePercent < 0.10) {
                createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Memória RAM Crítica",
                        "Memória livre está abaixo de 10% (" + String.format("%.1f", freePercent * 100) + "%). Risco de travamento.");
            }
        }
    }

    private void checkDiskPredictiveAlerts(Ativo ativo) {
        if (ativo.getPrevisaoEsgotamentoDisco() == null) {
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate previsao = ativo.getPrevisaoEsgotamentoDisco();
        long daysUntilExhaustion = ChronoUnit.DAYS.between(now, previsao);

        // Se a previsão já passou (negativo) ou é hoje/amanhã, também é crítico
        if (daysUntilExhaustion < 7) {
            createAlertIfNotExists(ativo, TipoAlerta.CRITICO, "Risco Crítico de Falha de Disco",
                    "A previsão de esgotamento do disco é para " + daysUntilExhaustion + " dias (" + previsao + "). Ação imediata necessária.");
        } else if (daysUntilExhaustion < 30) {
            createAlertIfNotExists(ativo, TipoAlerta.WARNING, "Alerta de Capacidade de Disco",
                    "A previsão de esgotamento do disco é para " + daysUntilExhaustion + " dias (" + previsao + "). Planeje a manutenção.");
        }
    }

    private void createAlertIfNotExists(Ativo ativo, TipoAlerta tipo, String titulo, String mensagem) {
        // Evita criar múltiplos alertas não lidos do mesmo tipo para o mesmo ativo
        List<Alerta> existing = alertaRepository.findByAtivoIdAndLidoFalseAndTipo(ativo.getId(), tipo);
        if (existing.isEmpty()) {
            Alerta alerta = new Alerta();
            alerta.setAtivo(ativo);
            alerta.setTipo(tipo);
            alerta.setTitulo(titulo);
            alerta.setMensagem(mensagem);
            alertaRepository.save(alerta);
        }
    }
}
