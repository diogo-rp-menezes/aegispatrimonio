package br.com.aegispatrimonio.service.updater;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;

public interface HealthCheckUpdater {
    void updateScalars(Long ativoId, AtivoDetalheHardware detalhes, HealthCheckDTO dto, boolean createdNow);
}
