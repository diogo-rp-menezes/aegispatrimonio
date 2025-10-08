package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.mapper.LocalizacaoMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.Status;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private Pessoa admin, userFilialA;
    private Filial filialA, filialB;
    private Localizacao localizacao;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);

        filialB = new Filial();
        filialB.setId(2L);

        admin = new Pessoa();
        admin.setId(1L);
        admin.setRole("ROLE_ADMIN");
        admin.setFilial(filialA);

        userFilialA = new Pessoa();
        userFilialA.setId(2L);
        userFilialA.setRole("ROLE_USER");
        userFilialA.setFilial(filialA);

        localizacao = new Localizacao();
        localizacao.setId(10L);
        localizacao.setFilial(filialA);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    private void mockUser(Pessoa pessoa) {
        CustomUserDetails userDetails = new CustomUserDetails(pessoa);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se usuário não-admin tentar criar localização em outra filial")
    void criar_quandoNaoAdminEmOutraFilial_deveLancarExcecao() {
        mockUser(userFilialA);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sala 1", null, 2L, null);

        assertThrows(AccessDeniedException.class, () -> localizacaoService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se localização pai pertencer a outra filial")
    void criar_quandoPaiDeOutraFilial_deveLancarExcecao() {
        mockUser(admin);
        Localizacao pai = new Localizacao();
        pai.setId(20L);
        pai.setFilial(filialB);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sala 1", null, 1L, 20L);

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(localizacaoRepository.findById(20L)).thenReturn(Optional.of(pai));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.criar(createDTO));
    }

    @Test
    @DisplayName("Atualizar: Deve lançar exceção ao tentar se tornar pai de si mesmo")
    void atualizar_quandoPaiIgualId_deveLancarExcecao() {
        mockUser(admin);
        LocalizacaoUpdateDTO updateDTO = new LocalizacaoUpdateDTO("Nome", null, 1L, 10L, Status.ATIVO);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacao));
        // CORREÇÃO: Adicionar mock para a busca da filial para evitar EntityNotFoundException.
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.atualizar(10L, updateDTO));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se for pai de outras localizações")
    void deletar_quandoPaiDeOutros_deveLancarExcecao() {
        mockUser(admin);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacao));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(false);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se tiver ativos associados")
    void deletar_quandoTemAtivos_deveLancarExcecao() {
        mockUser(admin);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacao));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve deletar com sucesso quando não há dependências")
    void deletar_quandoValido_deveDeletar() {
        mockUser(admin);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacao));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(false);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(false);

        localizacaoService.deletar(10L);

        verify(localizacaoRepository).delete(localizacao);
    }
}
