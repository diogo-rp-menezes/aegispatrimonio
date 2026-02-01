package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowAprovacaoServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private WorkflowAprovacaoService service;

    private Manutencao manutencao;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setEmail("admin@aegis.com");

        Ativo ativo = new Ativo();
        ativo.setId(1L);

        manutencao = new Manutencao();
        manutencao.setId(10L);
        manutencao.setAtivo(ativo);

        // Use lenient() because some tests (like exception cases) might not reach the line that calls currentUserProvider
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(usuario);
    }

    @Test
    void aprovar_ShouldSucceed_WhenStatusIsSolicitada() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);

        service.aprovar(manutencao);

        assertEquals(StatusManutencao.APROVADA, manutencao.getStatus());
        verify(currentUserProvider).getCurrentUsuario();
    }

    @Test
    void aprovar_ShouldThrowException_WhenStatusIsNotSolicitada() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);

        assertThrows(ResourceConflictException.class, () -> service.aprovar(manutencao));
    }

    @Test
    void iniciar_ShouldSucceed_WhenStatusIsAprovada() {
        manutencao.setStatus(StatusManutencao.APROVADA);

        service.iniciar(manutencao);

        assertEquals(StatusManutencao.EM_ANDAMENTO, manutencao.getStatus());
        assertNotNull(manutencao.getDataInicio());
        verify(currentUserProvider).getCurrentUsuario();
    }

    @Test
    void concluir_ShouldSucceed_WhenStatusIsEmAndamento() {
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);

        service.concluir(manutencao);

        assertEquals(StatusManutencao.CONCLUIDA, manutencao.getStatus());
        assertNotNull(manutencao.getDataConclusao());
        verify(currentUserProvider).getCurrentUsuario();
    }

    @Test
    void cancelar_ShouldSucceed_WhenStatusIsSolicitada() {
        manutencao.setStatus(StatusManutencao.SOLICITADA);

        service.cancelar(manutencao);

        assertEquals(StatusManutencao.CANCELADA, manutencao.getStatus());
        verify(currentUserProvider).getCurrentUsuario();
    }

    @Test
    void cancelar_ShouldThrowException_WhenAlreadyConcluida() {
        manutencao.setStatus(StatusManutencao.CONCLUIDA);

        assertThrows(ResourceConflictException.class, () -> service.cancelar(manutencao));
    }
}
