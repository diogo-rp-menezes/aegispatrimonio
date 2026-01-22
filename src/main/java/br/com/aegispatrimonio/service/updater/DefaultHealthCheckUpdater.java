package br.com.aegispatrimonio.service.updater;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.repository.AtivoDetalheHardwareRepository;
import org.springframework.stereotype.Component;

@Component
public class DefaultHealthCheckUpdater implements HealthCheckUpdater {

    private final HealthCheckMapper mapper;
    private final AtivoDetalheHardwareRepository repository;

    public DefaultHealthCheckUpdater(HealthCheckMapper mapper, AtivoDetalheHardwareRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public void updateScalars(Long ativoId, AtivoDetalheHardware detalhes, HealthCheckDTO dto, boolean createdNow) {
        if (createdNow) {
            // Se a entidade foi acabada de criar, o mapper atualiza os campos e o dirty checking do JPA fará o update.
            mapper.updateEntityFromDto(detalhes, dto);
        } else {
            // Se a entidade já existe, usamos a query JPQL otimizada para fazer um update direto no banco.
            repository.updateScalars(
                    ativoId,
                    dto.computerName(),
                    dto.domain(),
                    dto.osName(),
                    dto.osVersion(),
                    dto.osArchitecture(),
                    dto.motherboardManufacturer(),
                    dto.motherboardModel(),
                    dto.motherboardSerialNumber(),
                    dto.cpuModel(),
                    dto.cpuCores(),
                    dto.cpuThreads()
            );
        }
    }
}
