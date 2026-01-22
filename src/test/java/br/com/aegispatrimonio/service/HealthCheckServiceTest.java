package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.policy.HealthCheckAuthorizationPolicy;
import br.com.aegispatrimonio.service.manager.HealthCheckCollectionsManager;
import br.com.aegispatrimonio.service.updater.HealthCheckUpdater;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private AtivoDetalheHardwareRepository detalheHardwareRepository;
    @Mock
    private DiscoRepository discoRepository;
    @Mock
    private MemoriaRepository memoriaRepository;
    @Mock
    private AdaptadorRedeRepository adaptadorRedeRepository;
    @Mock
    private HealthCheckMapper healthCheckMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private HealthCheckAuthorizationPolicy healthCheckAuthorizationPolicy; // Adicionado mock para HealthCheckAuthorizationPolicy
    @Mock
    private HealthCheckUpdater healthCheckUpdater;
    @Mock
    private HealthCheckCollectionsManager collectionsManager;

    @InjectMocks
    private HealthCheckService healthCheckService;

    private Ativo ativo;
    private Usuario adminUser, regularUser, unauthorizedUser;
    private HealthCheckDTO healthCheckDTO;
    private AtivoDetalheHardware detalhesHardware;

    @BeforeEach
    void setUp() {
        Filial filialA = new Filial();
        filialA.setId(1L);

        Filial filialB = new Filial();
        filialB.setId(2L);

        ativo = new Ativo();
        ativo.setId(1L);
        ativo.setFilial(filialA);

        Funcionario adminFunc = new Funcionario();
        adminFunc.setFiliais(Set.of(filialA, filialB));
        adminUser = new Usuario();
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setFuncionario(adminFunc);

        Funcionario regularFunc = new Funcionario();
        regularFunc.setId(2L);
        regularFunc.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setRole("ROLE_USER");
        regularUser.setFuncionario(regularFunc);

        Funcionario unauthorizedFunc = new Funcionario();
        unauthorizedFunc.setId(3L);
        unauthorizedFunc.setFiliais(Set.of(filialB));
        unauthorizedUser = new Usuario();
        unauthorizedUser.setRole("ROLE_USER");
        unauthorizedUser.setFuncionario(unauthorizedFunc);

        healthCheckDTO = new HealthCheckDTO("PC-01", null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        detalhesHardware = new AtivoDetalheHardware();
        detalhesHardware.setId(1L);
        detalhesHardware.setAtivo(ativo);

        // Mock do currentUserProvider para todos os testes
        when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser); // Default para admin, pode ser sobrescrito nos testes específicos
    }

    @Test
    @DisplayName("Deve atualizar o HealthCheck com sucesso para usuário ADMIN")
    void updateHealthCheck_comAdmin_deveAtualizarComSucesso() {
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.of(detalhesHardware));
        // Mock para a política de autorização
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(healthCheckUpdater, times(1)).updateScalars(eq(1L), eq(detalhesHardware), eq(healthCheckDTO), eq(false));
        verify(collectionsManager, times(1)).replaceCollections(eq(detalhesHardware), eq(healthCheckDTO));
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
    }

    @Test
    @DisplayName("Deve atualizar o HealthCheck com sucesso para usuário autorizado da mesma filial")
    void updateHealthCheck_comUsuarioAutorizado_deveAtualizarComSucesso() {
        when(currentUserProvider.getCurrentUsuario()).thenReturn(regularUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.of(detalhesHardware));
        // Mock para a política de autorização
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(healthCheckUpdater, times(1)).updateScalars(eq(1L), eq(detalhesHardware), eq(healthCheckDTO), eq(false));
        verify(collectionsManager, times(1)).replaceCollections(eq(detalhesHardware), eq(healthCheckDTO));
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(regularUser, ativo);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException para usuário de outra filial")
    void updateHealthCheck_comUsuarioNaoAutorizado_deveLancarExcecao() {
        when(currentUserProvider.getCurrentUsuario()).thenReturn(unauthorizedUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        // Mock para a política de autorização lançar AccessDeniedException
        doThrow(AccessDeniedException.class).when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        assertThrows(AccessDeniedException.class, () -> healthCheckService.updateHealthCheck(1L, healthCheckDTO));
        verify(healthCheckUpdater, never()).updateScalars(anyLong(), any(AtivoDetalheHardware.class), any(HealthCheckDTO.class), anyBoolean());
        verify(collectionsManager, never()).replaceCollections(any(AtivoDetalheHardware.class), any(HealthCheckDTO.class));
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(unauthorizedUser, ativo);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ativo não existir")
    void updateHealthCheck_quandoAtivoNaoEncontrado_deveLancarExcecao() {
        // O currentUserProvider já está mockado no BeforeEach para adminUser
        when(ativoRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> healthCheckService.updateHealthCheck(99L, healthCheckDTO));
        verify(healthCheckAuthorizationPolicy, never()).assertCanUpdate(any(Usuario.class), any(Ativo.class)); // Não deve chamar a política se o ativo não for encontrado
        verify(healthCheckUpdater, never()).updateScalars(anyLong(), any(AtivoDetalheHardware.class), any(HealthCheckDTO.class), anyBoolean());
        verify(collectionsManager, never()).replaceCollections(any(AtivoDetalheHardware.class), any(HealthCheckDTO.class));
    }

    @Test
    @DisplayName("Deve criar novos detalhes de hardware se não existirem")
    void updateHealthCheck_quandoDetalhesNaoExistem_deveCriarNovo() {
        // O currentUserProvider já está mockado no BeforeEach para adminUser
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.empty());
        when(detalheHardwareRepository.saveAndFlush(any(AtivoDetalheHardware.class))).thenReturn(detalhesHardware);
        // Mock para a política de autorização
        doNothing().when(healthCheckAuthorizationPolicy).assertCanUpdate(any(Usuario.class), any(Ativo.class));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(healthCheckUpdater, times(1)).updateScalars(eq(1L), any(AtivoDetalheHardware.class), eq(healthCheckDTO), eq(true));
        verify(collectionsManager, times(1)).replaceCollections(any(AtivoDetalheHardware.class), eq(healthCheckDTO));
        verify(healthCheckAuthorizationPolicy).assertCanUpdate(adminUser, ativo);
    }
}
