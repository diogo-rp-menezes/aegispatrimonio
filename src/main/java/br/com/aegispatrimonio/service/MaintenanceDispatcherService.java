package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import br.com.aegispatrimonio.repository.ManutencaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Autonomous agent responsible for dispatching maintenance requests based on predictive analysis.
 * Acts as the bridge between "Health Monitoring" and "Service Management".
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceDispatcherService {

    private final ManutencaoRepository manutencaoRepository;
    private final ManutencaoService manutencaoService;

    @Transactional
    public void dispatchIfNecessary(Ativo ativo, LocalDate predictionDate) {
        if (predictionDate == null) {
            return;
        }

        long daysUntilFailure = ChronoUnit.DAYS.between(LocalDate.now(), predictionDate);

        // Threshold: 7 days or less
        if (daysUntilFailure <= 7) {
            // Check for existing open tickets to prevent spam
            boolean hasOpenTicket = manutencaoRepository.existsByAtivoIdAndStatusIn(
                    ativo.getId(),
                    Set.of(StatusManutencao.SOLICITADA, StatusManutencao.APROVADA, StatusManutencao.EM_ANDAMENTO)
            );

            if (!hasOpenTicket) {
                String description = String.format("Manutenção Preditiva Automática: Previsão de esgotamento de disco em %d dias (%s). Ação imediata recomendada.",
                        daysUntilFailure, predictionDate);

                log.info("SYSTEM: Dispatching autonomous maintenance for Ativo {} due to critical prediction.", ativo.getId());

                manutencaoService.criarManutencaoSistemica(ativo, description, TipoManutencao.PREDITIVA);
            } else {
                log.debug("SYSTEM: Critical prediction for Ativo {}, but open maintenance ticket already exists.", ativo.getId());
            }
        }
    }
}
