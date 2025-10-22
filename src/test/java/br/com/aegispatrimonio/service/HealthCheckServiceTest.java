package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.mapper.HealthCheckMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    
    // CORREÇÃO: Adicionar o Mock que faltava para a nova dependência.
    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private HealthCheckService healthCheckService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

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
        regularFunc.setId(2L); // Adicionado ID para o mock funcionar
        regularFunc.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setRole("ROLE_USER");
        regularUser.setFuncionario(regularFunc);

        Funcionario unauthorizedFunc = new Funcionario();
        unauthorizedFunc.setId(3L); // Adicionado ID para o mock funcionar
        unauthorizedFunc.setFiliais(Set.of(filialB));
        unauthorizedUser = new Usuario();
        unauthorizedUser.setRole("ROLE_USER");
        unauthorizedUser.setFuncionario(unauthorizedFunc);

        healthCheckDTO = new HealthCheckDTO("PC-01", null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        detalhesHardware = new AtivoDetalheHardware();
        detalhesHardware.setId(1L);
        detalhesHardware.setAtivo(ativo);

        // Mock do SecurityContextHolder
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    private void mockUser(Usuario usuario) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        lenient().when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    @Test
    @DisplayName("Deve atualizar o HealthCheck com sucesso para usuário ADMIN")
    void updateHealthCheck_comAdmin_deveAtualizarComSucesso() {
        mockUser(adminUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.of(detalhesHardware));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(detalheHardwareRepository, times(1)).save(any(AtivoDetalheHardware.class));
        verify(discoRepository, times(1)).deleteByAtivoDetalheHardwareId(anyLong());
        verify(memoriaRepository, times(1)).deleteByAtivoDetalheHardwareId(anyLong());
        verify(adaptadorRedeRepository, times(1)).deleteByAtivoDetalheHardwareId(anyLong());
    }

    @Test
    @DisplayName("Deve atualizar o HealthCheck com sucesso para usuário autorizado da mesma filial")
    void updateHealthCheck_comUsuarioAutorizado_deveAtualizarComSucesso() {
        mockUser(regularUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(funcionarioRepository.findById(regularUser.getFuncionario().getId())).thenReturn(Optional.of(regularUser.getFuncionario()));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.of(detalhesHardware));

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(detalheHardwareRepository, times(1)).save(any(AtivoDetalheHardware.class));
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException para usuário de outra filial")
    void updateHealthCheck_comUsuarioNaoAutorizado_deveLancarExcecao() {
        mockUser(unauthorizedUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(funcionarioRepository.findById(unauthorizedUser.getFuncionario().getId())).thenReturn(Optional.of(unauthorizedUser.getFuncionario()));

        assertThrows(AccessDeniedException.class, () -> healthCheckService.updateHealthCheck(1L, healthCheckDTO));
        verify(detalheHardwareRepository, never()).save(any(AtivoDetalheHardware.class));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ativo não existir")
    void updateHealthCheck_quandoAtivoNaoEncontrado_deveLancarExcecao() {
        mockUser(adminUser);
        when(ativoRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> healthCheckService.updateHealthCheck(99L, healthCheckDTO));
    }

    @Test
    @DisplayName("Deve criar novos detalhes de hardware se não existirem")
    void updateHealthCheck_quandoDetalhesNaoExistem_deveCriarNovo() {
        mockUser(adminUser);
        when(ativoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ativo));
        when(detalheHardwareRepository.findById(1L)).thenReturn(Optional.empty());

        healthCheckService.updateHealthCheck(1L, healthCheckDTO);

        verify(detalheHardwareRepository, times(1)).save(any(AtivoDetalheHardware.class));
    }
}
