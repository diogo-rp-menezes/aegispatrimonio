package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.TipoAlerta;
import br.com.aegispatrimonio.repository.AlertaRepository;
import br.com.aegispatrimonio.repository.AtivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServicePerformanceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private AlertNotificationService service;

    private Ativo ativo;

    @BeforeEach
    void setUp() {
        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Test Asset");
    }

    @Test
    void shouldCallRepositoryOnceForMultipleFailingDisks() {
        // Create 10 failing disks
        List<DiskInfoDTO> disks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            disks.add(new DiskInfoDTO("Disk " + i, "SN" + i, "Type", 1000.0, 50.0, 0.05)); // 5% free (critical)
        }

        HealthCheckPayloadDTO payload = new HealthCheckPayloadDTO(
                null, null, null, null, null, null, null, null, null, null, null,
                0.10, // cpu safe
                16000L, 8000L, // memory safe
                disks
        );

        when(ativoRepository.getReferenceById(1L)).thenReturn(ativo);
        // Important: Return a mutable list
        when(alertaRepository.findByAtivoIdAndLidoFalse(1L)).thenReturn(new ArrayList<>());

        service.checkAndCreateAlerts(1L, null, payload);

        // Verify the new method is called once
        verify(alertaRepository, times(1)).findByAtivoIdAndLidoFalse(1L);

        // Verify the old method is NEVER called
        verify(alertaRepository, never()).findByAtivoIdAndLidoFalseAndTipo(any(), any());

        // Verify we saved 1 alert (because the first one is added to the list, subsequent checks see it)
        // Wait, logic is: "If NOT exists in list, create and add to list".
        // 1st disk: list empty -> create alert, save, add to list.
        // 2nd disk: list has CRITICO alert -> exists match -> skip.
        // So we should save only 1 alert!
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }
}
