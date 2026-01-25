package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.TipoAlerta;
import br.com.aegispatrimonio.repository.AlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

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
    void shouldCreateCriticalAlertWhenPredictionIsBelow7Days() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));

        when(alertaRepository.findByAtivoIdAndLidoFalseAndTipo(1L, TipoAlerta.CRITICO))
                .thenReturn(Collections.emptyList());

        service.checkAndCreateAlerts(ativo);

        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void shouldCreateWarningAlertWhenPredictionIsBetween7And30Days() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(20));

        when(alertaRepository.findByAtivoIdAndLidoFalseAndTipo(1L, TipoAlerta.WARNING))
                .thenReturn(Collections.emptyList());

        service.checkAndCreateAlerts(ativo);

        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void shouldNotCreateAlertIfAlreadyExists() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(5));

        when(alertaRepository.findByAtivoIdAndLidoFalseAndTipo(1L, TipoAlerta.CRITICO))
                .thenReturn(Collections.singletonList(new Alerta()));

        service.checkAndCreateAlerts(ativo);

        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void shouldNotCreateAlertIfSafe() {
        ativo.setPrevisaoEsgotamentoDisco(LocalDate.now().plusDays(40));

        service.checkAndCreateAlerts(ativo);

        verify(alertaRepository, never()).save(any(Alerta.class));
    }
}
