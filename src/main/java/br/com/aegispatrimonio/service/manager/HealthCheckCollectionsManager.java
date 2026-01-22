package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;

public interface HealthCheckCollectionsManager {
    void replaceCollections(AtivoDetalheHardware detalhes, HealthCheckDTO dto);
}
