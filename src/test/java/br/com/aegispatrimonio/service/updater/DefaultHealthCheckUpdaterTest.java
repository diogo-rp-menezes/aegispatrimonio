package br.com.aegispatrimonio.service.updater;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.repository.AtivoDetalheHardwareRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultHealthCheckUpdaterTest {

    @Mock
    private HealthCheckMapper mapper;

    @Mock
    private AtivoDetalheHardwareRepository repository;

    @InjectMocks
    private DefaultHealthCheckUpdater updater;

    @Test
    @DisplayName("Deve chamar o mapper quando a entidade é nova (createdNow = true)")
    void updateScalars_whenEntityIsNew_shouldCallMapper() {
        // Given
        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        HealthCheckDTO dto = new HealthCheckDTO("PC-NEW", null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        boolean createdNow = true;
        Long ativoId = 1L;

        // When
        updater.updateScalars(ativoId, detalhes, dto, createdNow);

        // Then
        verify(mapper, times(1)).updateEntityFromDto(detalhes, dto);
        verify(repository, never()).updateScalars(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve chamar o repositório quando a entidade já existe (createdNow = false)")
    void updateScalars_whenEntityExists_shouldCallRepository() {
        // Given
        AtivoDetalheHardware detalhes = new AtivoDetalheHardware();
        HealthCheckDTO dto = new HealthCheckDTO(
                "PC-UPDATED", "domain.local", "Windows 11", "22H2", "x64",
                "ASUS", "ROG STRIX", "SN12345", "Intel i9", 16, 32,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );
        boolean createdNow = false;
        Long ativoId = 1L;

        // When
        updater.updateScalars(ativoId, detalhes, dto, createdNow);

        // Then
        verify(repository, times(1)).updateScalars(
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
        verify(mapper, never()).updateEntityFromDto(any(), any());
    }
}
