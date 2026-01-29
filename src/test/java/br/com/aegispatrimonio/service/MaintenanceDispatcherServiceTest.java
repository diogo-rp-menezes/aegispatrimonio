package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import br.com.aegispatrimonio.repository.ManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceDispatcherServiceTest {

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @Mock
    private ManutencaoService manutencaoService;

    private MaintenanceDispatcherService maintenanceDispatcherService;

    @BeforeEach
    void setUp() {
        maintenanceDispatcherService = new MaintenanceDispatcherService(manutencaoRepository, manutencaoService);
    }

    @Test
    void shouldDispatchWhenCriticalAndNoTicket() {
        Ativo ativo = new Ativo();
        ativo.setId(1L);
        LocalDate criticalDate = LocalDate.now().plusDays(5);

        when(manutencaoRepository.existsByAtivoIdAndStatusIn(eq(1L), anySet())).thenReturn(false);

        maintenanceDispatcherService.dispatchIfNecessary(ativo, criticalDate);

        verify(manutencaoService).criarManutencaoSistemica(eq(ativo), contains("Manutenção Preditiva Automática"), eq(TipoManutencao.PREDITIVA));
    }

    @Test
    void shouldNotDispatchWhenNotCritical() {
        Ativo ativo = new Ativo();
        ativo.setId(1L);
        LocalDate safeDate = LocalDate.now().plusDays(10);

        maintenanceDispatcherService.dispatchIfNecessary(ativo, safeDate);

        verify(manutencaoRepository, never()).existsByAtivoIdAndStatusIn(anyLong(), anySet());
        verify(manutencaoService, never()).criarManutencaoSistemica(any(), anyString(), any());
    }

    @Test
    void shouldNotDispatchWhenTicketExists() {
        Ativo ativo = new Ativo();
        ativo.setId(1L);
        LocalDate criticalDate = LocalDate.now().plusDays(3);

        when(manutencaoRepository.existsByAtivoIdAndStatusIn(eq(1L), anySet())).thenReturn(true);

        maintenanceDispatcherService.dispatchIfNecessary(ativo, criticalDate);

        verify(manutencaoService, never()).criarManutencaoSistemica(any(), anyString(), any());
    }
}
