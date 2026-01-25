package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.TipoAlerta;
import br.com.aegispatrimonio.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertNotificationService {

    private final AlertaRepository alertaRepository;

    public AlertNotificationService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public void checkAndCreateAlerts(Ativo ativo) {
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
