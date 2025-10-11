package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.mapper.LocalizacaoMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
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
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalizacaoServiceTest {

    @Mock
    private LocalizacaoRepository localizacaoRepository;
    @Mock
    private LocalizacaoMapper localizacaoMapper;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private AtivoRepository ativoRepository;

    @InjectMocks
    private LocalizacaoService localizacaoService;

    private Usuario adminUser, regularUser;
    private Filial filialA, filialB;
    private Localizacao localizacaoA, localizacaoB;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);

        filialB = new Filial();
        filialB.setId(2L);

        Funcionario adminFunc = new Funcionario();
        adminFunc.setId(1L);
        adminFunc.setFiliais(Set.of(filialA, filialB));
        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setFuncionario(adminFunc);

        Funcionario regularFunc = new Funcionario();
        regularFunc.setId(2L);
        regularFunc.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setId(2L);
        regularUser.setRole("ROLE_USER");
        regularUser.setFuncionario(regularFunc);

        localizacaoA = new Localizacao();
        localizacaoA.setId(10L);
        localizacaoA.setFilial(filialA);

        localizacaoB = new Localizacao();
        localizacaoB.setId(20L);
        localizacaoB.setFilial(filialB);

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextImpl securityContextImpl = new SecurityContextImpl();
        securityContextImpl.setAuthentication(authentication);
        // Use real SecurityContextHolder so mocks behave predictably
        SecurityContextHolder.setContext(securityContextImpl);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockUser(Usuario usuario) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        lenient().when(auth.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se usuário não-admin tentar criar localização em outra filial")
    void criar_quandoNaoAdminEmOutraFilial_deveLancarExcecao() {
        mockUser(regularUser);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sala 1", null, 2L, null);
        // stub pode ser desnecessário pois a checagem de permissão ocorre antes da consulta;
        // marca como lenient para evitar UnnecessaryStubbingException
        lenient().when(filialRepository.findById(2L)).thenReturn(Optional.of(filialB));

        assertThrows(AccessDeniedException.class, () -> localizacaoService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se localização pai pertencer a outra filial")
    void criar_quandoPaiDeOutraFilial_deveLancarExcecao() {
        mockUser(adminUser);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sala 1", null, 1L, 20L);

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(localizacaoRepository.findById(20L)).thenReturn(Optional.of(localizacaoB));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.criar(createDTO));
    }

    @Test
    @DisplayName("Atualizar: Deve lançar exceção ao tentar se tornar pai de si mesmo")
    void atualizar_quandoPaiIgualId_deveLancarExcecao() {
        mockUser(adminUser);
        LocalizacaoUpdateDTO updateDTO = new LocalizacaoUpdateDTO("Nome", null, 1L, 10L, Status.ATIVO);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.atualizar(10L, updateDTO));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se for pai de outras localizações")
    void deletar_quandoPaiDeOutros_deveLancarExcecao() {
        mockUser(adminUser);
        when(localizacaoRepository.existsById(10L)).thenReturn(true);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se tiver ativos associados")
    void deletar_quandoTemAtivos_deveLancarExcecao() {
        mockUser(adminUser);
        when(localizacaoRepository.existsById(10L)).thenReturn(true);
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve deletar com sucesso quando não há dependências")
    void deletar_quandoValido_deveDeletar() {
        mockUser(adminUser);
        when(localizacaoRepository.existsById(10L)).thenReturn(true);
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(false);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(false);

        localizacaoService.deletar(10L);

        verify(localizacaoRepository).deleteById(10L);
    }
}
