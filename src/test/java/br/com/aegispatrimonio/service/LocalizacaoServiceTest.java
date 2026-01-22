package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.mapper.LocalizacaoMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @Mock
    private CurrentUserProvider currentUserProvider; // Adicionado mock para CurrentUserProvider

    @InjectMocks
    private LocalizacaoService localizacaoService;

    // Removido MockedStatic para SecurityContextHolder, pois não é mais necessário
    // private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private Usuario adminUser, userFilialA, userFilialB;
    private Filial filialA, filialB;
    private Localizacao localizacaoA, localizacaoB;
    private LocalizacaoDTO localizacaoDTO;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);
        filialA.setNome("Filial A");

        filialB = new Filial();
        filialB.setId(2L);
        filialB.setNome("Filial B");

        localizacaoA = new Localizacao();
        localizacaoA.setId(10L);
        localizacaoA.setNome("Sala 101");
        localizacaoA.setFilial(filialA);

        localizacaoB = new Localizacao();
        localizacaoB.setId(20L);
        localizacaoB.setNome("Auditório B");
        localizacaoB.setFilial(filialB);

        localizacaoDTO = new LocalizacaoDTO(10L, "Sala 101", "Descrição", "Filial A", null, Status.ATIVO);

        // --- Usuários ---
        adminUser = new Usuario();
        adminUser.setRole("ROLE_ADMIN");

        Funcionario funcA = new Funcionario();
        funcA.setFiliais(Set.of(filialA));
        userFilialA = new Usuario();
        userFilialA.setRole("ROLE_USER");
        userFilialA.setFuncionario(funcA);

        Funcionario funcB = new Funcionario();
        funcB.setFiliais(Set.of(filialB));
        userFilialB = new Usuario();
        userFilialB.setRole("ROLE_USER");
        userFilialB.setFuncionario(funcB);

        // Removido o mock do SecurityContextHolder
        // Authentication authentication = Mockito.mock(Authentication.class);
        // SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        // lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        // mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        // mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        // Removido o fechamento do MockedStatic
        // if (mockedSecurityContextHolder != null) {
        //     mockedSecurityContextHolder.close();
        // }
    }

    private void mockUser(Usuario usuario) {
        // Agora mockamos o currentUserProvider diretamente
        when(currentUserProvider.getCurrentUsuario()).thenReturn(usuario);
    }

    // --- Testes de Listagem ---
    @Test
    @DisplayName("ListarTodos: ADMIN deve ver todas as localizações")
    void listarTodos_comAdmin_deveRetornarTodas() {
        mockUser(adminUser);
        when(localizacaoRepository.findAll()).thenReturn(List.of(localizacaoA, localizacaoB));

        List<LocalizacaoDTO> result = localizacaoService.listarTodos();

        assertEquals(2, result.size());
        verify(localizacaoRepository).findAll();
    }

    @Test
    @DisplayName("ListarTodos: USER deve ver apenas localizações de sua filial")
    void listarTodos_comUser_deveRetornarLocalizacoesDaFilial() {
        mockUser(userFilialA);
        when(localizacaoRepository.findByFilialIdIn(Set.of(1L))).thenReturn(List.of(localizacaoA));

        List<LocalizacaoDTO> result = localizacaoService.listarTodos();

        assertEquals(1, result.size());
        verify(localizacaoRepository).findByFilialIdIn(Set.of(1L));
    }

    // --- Testes de Busca por ID ---
    @Test
    @DisplayName("BuscarPorId: USER pode ver localização de sua filial")
    void buscarPorId_comUserAutorizado_deveRetornarDTO() {
        mockUser(userFilialA);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(localizacaoMapper.toDTO(localizacaoA)).thenReturn(localizacaoDTO);

        LocalizacaoDTO result = localizacaoService.buscarPorId(10L);

        assertNotNull(result);
        assertEquals(localizacaoDTO.id(), result.id());
    }

    @Test
    @DisplayName("BuscarPorId: USER não pode ver localização de outra filial")
    void buscarPorId_comUserNaoAutorizado_deveLancarExcecao() {
        mockUser(userFilialB);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));

        assertThrows(AccessDeniedException.class, () -> localizacaoService.buscarPorId(10L));
    }

    // --- Testes de Criação ---
    @Test
    @DisplayName("Criar: Deve criar localização com sucesso")
    void criar_comDadosValidos_deveRetornarDTO() {
        mockUser(adminUser);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Almoxarifado", "", 1L, null);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(localizacaoRepository.findByNomeAndFilialAndLocalizacaoPai(any(), any(), any())).thenReturn(Optional.empty());
        when(localizacaoMapper.toEntity(createDTO)).thenReturn(new Localizacao());
        when(localizacaoRepository.save(any(Localizacao.class))).thenReturn(localizacaoA);
        when(localizacaoMapper.toDTO(any(Localizacao.class))).thenReturn(localizacaoDTO);

        LocalizacaoDTO result = localizacaoService.criar(createDTO);

        assertNotNull(result);
        verify(localizacaoRepository).save(any(Localizacao.class));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção para nome duplicado na mesma hierarquia")
    void criar_comNomeDuplicado_deveLancarExcecao() {
        mockUser(adminUser);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sala 101", "", 1L, null);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        // Simula que já existe uma localização com esse nome
        when(localizacaoRepository.findByNomeAndFilialAndLocalizacaoPai("Sala 101", filialA, null)).thenReturn(Optional.of(localizacaoA));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se localização pai não pertence à mesma filial")
    void criar_comPaiEmFilialDiferente_deveLancarExcecao() {
        mockUser(adminUser);
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Sub-sala", "", 1L, 20L); // Localização na Filial A, Pai na Filial B
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(localizacaoRepository.findById(20L)).thenReturn(Optional.of(localizacaoB));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.criar(createDTO));
    }

    // --- Testes de Atualização ---
    @Test
    @DisplayName("Atualizar: Não deve permitir que uma localização seja seu próprio pai")
    void atualizar_comPaiSendoPropriaLocalizacao_deveLancarExcecao() {
        mockUser(adminUser);
        LocalizacaoUpdateDTO updateDTO = new LocalizacaoUpdateDTO("Novo Nome", "", 1L, 10L, Status.ATIVO); // ID da localização e do pai são os mesmos (10L)
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));

        assertThrows(IllegalArgumentException.class, () -> localizacaoService.atualizar(10L, updateDTO));
    }

    // --- Testes de Exclusão ---
    @Test
    @DisplayName("Deletar: Deve lançar exceção se ativos estiverem associados")
    void deletar_comAtivosAssociados_deveLancarExcecao() {
        mockUser(adminUser);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se for pai de outras localizações")
    void deletar_quandoForPai_deveLancarExcecao() {
        mockUser(adminUser);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(false);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> localizacaoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve deletar com sucesso se todas as condições forem atendidas")
    void deletar_comCondicoesValidas_deveChamarDelete() {
        mockUser(adminUser);
        when(localizacaoRepository.findById(10L)).thenReturn(Optional.of(localizacaoA));
        when(ativoRepository.existsByLocalizacaoId(10L)).thenReturn(false);
        when(localizacaoRepository.existsByLocalizacaoPaiId(10L)).thenReturn(false);

        localizacaoService.deletar(10L);

        verify(localizacaoRepository).deleteById(10L);
    }
}
