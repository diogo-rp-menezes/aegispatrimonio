package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.mapper.AtivoMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtivoServiceTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private AtivoMapper ativoMapper;
    @Mock
    private TipoAtivoRepository tipoAtivoRepository;
    @Mock
    private LocalizacaoRepository localizacaoRepository;
    @Mock
    private FornecedorRepository fornecedorRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private ManutencaoRepository manutencaoRepository;
    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private DepreciacaoService depreciacaoService;

    @InjectMocks
    private AtivoService ativoService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    // Entidades de Teste
    private Usuario adminUser, regularUser;
    private Filial filialA, filialB;
    private Ativo ativo;
    private Localizacao localizacao;
    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);

        filialB = new Filial();
        filialB.setId(2L);

        localizacao = new Localizacao();
        localizacao.setId(1L);
        localizacao.setFilial(filialA);

        Funcionario adminFunc = new Funcionario();
        adminFunc.setId(1L);
        adminFunc.setFiliais(Set.of(filialA, filialB));
        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setFuncionario(adminFunc);

        funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setId(2L);
        regularUser.setRole("ROLE_USER");
        regularUser.setFuncionario(funcionario);

        ativo = new Ativo();
        ativo.setId(10L);
        ativo.setFilial(filialA);

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
    @DisplayName("BuscarPorId: Usuário comum não pode acessar ativo de outra filial")
    void buscarPorId_quandoNaoAdminEOutraFilial_deveLancarExcecao() {
        mockUser(regularUser);
        Ativo ativoOutraFilial = new Ativo();
        ativoOutraFilial.setId(11L);
        ativoOutraFilial.setFilial(filialB);
        when(ativoRepository.findById(11L)).thenReturn(Optional.of(ativoOutraFilial));

        assertThrows(AccessDeniedException.class, () -> ativoService.buscarPorId(11L));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se localização não pertence à filial do ativo")
    void criar_quandoLocalizacaoInconsistente_deveLancarExcecao() {
        Localizacao localizacaoOutraFilial = new Localizacao();
        localizacaoOutraFilial.setId(2L);
        localizacaoOutraFilial.setFilial(filialB);

        AtivoCreateDTO createDTO = new AtivoCreateDTO(1L, "Notebook", 1L, "PAT-01", localizacaoOutraFilial.getId(), LocalDate.now(), 1L, BigDecimal.TEN, 1L, "Obs", null);

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(new TipoAtivo()));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(new Fornecedor()));
        when(localizacaoRepository.findById(2L)).thenReturn(Optional.of(localizacaoOutraFilial));

        assertThrows(IllegalArgumentException.class, () -> ativoService.criar(createDTO));
    }

    @Test
    @DisplayName("Atualizar: Deve lançar exceção se responsável não pertence à filial do ativo")
    void atualizar_quandoResponsavelInconsistente_deveLancarExcecao() {
        Funcionario responsavelOutraFilial = new Funcionario();
        responsavelOutraFilial.setId(3L);
        responsavelOutraFilial.setFiliais(Set.of(filialB));

        AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(1L, "Nome", "PAT-10", 1L, 1L, StatusAtivo.ATIVO, LocalDate.now(), 1L, BigDecimal.TEN, responsavelOutraFilial.getId(), "Obs", null);

        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativo));
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(new TipoAtivo()));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(new Fornecedor()));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(funcionarioRepository.findById(3L)).thenReturn(Optional.of(responsavelOutraFilial));

        assertThrows(IllegalArgumentException.class, () -> ativoService.atualizar(10L, updateDTO));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se existem manutenções associadas")
    void deletar_quandoExistemManutencoes_deveLancarExcecao() {
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativo));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ativoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se existem movimentações associadas")
    void deletar_quandoExistemMovimentacoes_deveLancarExcecao() {
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativo));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(false);
        when(movimentacaoRepository.existsByAtivoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ativoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve deletar ativo com sucesso")
    void deletar_quandoValido_deveChamarDelete() {
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativo));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(false);
        when(movimentacaoRepository.existsByAtivoId(10L)).thenReturn(false);

        ativoService.deletar(10L);

        verify(ativoRepository).delete(ativo);
    }
}
