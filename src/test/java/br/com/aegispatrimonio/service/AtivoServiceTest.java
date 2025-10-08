package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private PessoaRepository pessoaRepository;
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
    private Pessoa admin, user;
    private Filial filialA, filialB;
    private Ativo ativoFilialA;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);
        filialA.setNome("Filial A");

        filialB = new Filial();
        filialB.setId(2L);
        filialB.setNome("Filial B");

        admin = new Pessoa();
        admin.setId(1L);
        admin.setRole("ROLE_ADMIN");
        admin.setFilial(filialA);

        user = new Pessoa();
        user.setId(2L);
        user.setRole("ROLE_USER");
        user.setFilial(filialA);

        ativoFilialA = new Ativo();
        ativoFilialA.setId(10L);
        ativoFilialA.setFilial(filialA);

        // Mock do SecurityContextHolder para simular usuário logado
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
    @DisplayName("ListarTodos: Admin deve ver todos os ativos")
    void listarTodos_quandoAdmin_deveRetornarTodosAtivos() {
        mockUser(admin);
        when(ativoRepository.findAll()).thenReturn(Collections.singletonList(new Ativo()));

        List<AtivoDTO> result = ativoService.listarTodos();

        assertFalse(result.isEmpty());
        verify(ativoRepository).findAll();
        verify(ativoRepository, never()).findByFilialId(any());
    }

    @Test
    @DisplayName("ListarTodos: Usuário comum deve ver apenas ativos de sua filial")
    void listarTodos_quandoNaoAdmin_deveRetornarAtivosDaFilial() {
        mockUser(user);
        when(ativoRepository.findByFilialId(filialA.getId())).thenReturn(Collections.singletonList(new Ativo()));

        List<AtivoDTO> result = ativoService.listarTodos();

        assertFalse(result.isEmpty());
        verify(ativoRepository, never()).findAll();
        verify(ativoRepository).findByFilialId(filialA.getId());
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar ativo se for da mesma filial")
    void buscarPorId_quandoMesmaFilial_deveRetornarAtivo() {
        mockUser(user);
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoFilialA));

        ativoService.buscarPorId(10L);

        verify(ativoRepository).findById(10L);
    }

    @Test
    @DisplayName("BuscarPorId: Deve lançar exceção se for de outra filial e não for admin")
    void buscarPorId_quandoOutraFilial_deveLancarExcecao() {
        mockUser(user); // User da Filial A
        Ativo ativoFilialB = new Ativo();
        ativoFilialB.setId(11L);
        ativoFilialB.setFilial(filialB); // Ativo da Filial B
        when(ativoRepository.findById(11L)).thenReturn(Optional.of(ativoFilialB));

        assertThrows(AccessDeniedException.class, () -> ativoService.buscarPorId(11L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se existirem manutenções associadas")
    void deletar_quandoExistemManutencoes_deveLancarExcecao() {
        mockUser(admin);
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoFilialA));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ativoService.deletar(10L));
        verify(ativoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se existirem movimentações associadas")
    void deletar_quandoExistemMovimentacoes_deveLancarExcecao() {
        mockUser(admin);
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoFilialA));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(false);
        when(movimentacaoRepository.existsByAtivoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ativoService.deletar(10L));
        verify(ativoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deletar: Deve deletar o ativo com sucesso")
    void deletar_quandoValido_deveDeletarAtivo() {
        mockUser(admin);
        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoFilialA));
        when(manutencaoRepository.existsByAtivoId(10L)).thenReturn(false);
        when(movimentacaoRepository.existsByAtivoId(10L)).thenReturn(false);

        ativoService.deletar(10L);

        verify(ativoRepository).delete(ativoFilialA);
    }

    @Test
    @DisplayName("Atualizar: Deve chamar recálculo de depreciação ao mudar valor de aquisição")
    void atualizar_quandoMudaValorAquisicao_deveChamarRecalculo() {
        mockUser(admin);
        Ativo ativoOriginal = new Ativo();
        ativoOriginal.setId(10L);
        ativoOriginal.setFilial(filialA);
        ativoOriginal.setValorAquisicao(new BigDecimal("1000"));
        ativoOriginal.setDataAquisicao(LocalDate.now());

        AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(filialA.getId(), "Nome", "PAT-123", 1L, 1L, StatusAtivo.ATIVO, LocalDate.now(), 1L, new BigDecimal("1500"), 1L, null, null);

        Localizacao localizacao = new Localizacao();
        localizacao.setFilial(filialA);
        Pessoa pessoa = new Pessoa();
        pessoa.setFilial(filialA);

        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoOriginal));
        when(filialRepository.findById(any())).thenReturn(Optional.of(filialA));
        when(tipoAtivoRepository.findById(any())).thenReturn(Optional.of(new TipoAtivo()));
        when(fornecedorRepository.findById(any())).thenReturn(Optional.of(new Fornecedor()));
        when(localizacaoRepository.findById(any())).thenReturn(Optional.of(localizacao));
        when(pessoaRepository.findById(any())).thenReturn(Optional.of(pessoa));
        when(ativoRepository.save(any())).thenReturn(ativoOriginal);

        ativoService.atualizar(10L, updateDTO);

        verify(depreciacaoService).recalcularDepreciacaoCompleta(10L);
    }

    @Test
    @DisplayName("Atualizar: Não deve chamar recálculo de depreciação se valores financeiros não mudam")
    void atualizar_quandoNaoMudaValorAquisicao_naoDeveChamarRecalculo() {
        mockUser(admin);
        Ativo ativoOriginal = new Ativo();
        ativoOriginal.setId(10L);
        ativoOriginal.setFilial(filialA);
        BigDecimal valorOriginal = new BigDecimal("1000");
        LocalDate dataOriginal = LocalDate.now();
        ativoOriginal.setValorAquisicao(valorOriginal);
        ativoOriginal.setDataAquisicao(dataOriginal);

        AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(filialA.getId(), "Nome Novo", "PAT-123", 1L, 1L, StatusAtivo.ATIVO, dataOriginal, 1L, valorOriginal, 1L, null, null);

        Localizacao localizacao = new Localizacao();
        localizacao.setFilial(filialA);
        Pessoa pessoa = new Pessoa();
        pessoa.setFilial(filialA);

        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativoOriginal));
        when(filialRepository.findById(any())).thenReturn(Optional.of(filialA));
        when(tipoAtivoRepository.findById(any())).thenReturn(Optional.of(new TipoAtivo()));
        when(fornecedorRepository.findById(any())).thenReturn(Optional.of(new Fornecedor()));
        when(localizacaoRepository.findById(any())).thenReturn(Optional.of(localizacao));
        when(pessoaRepository.findById(any())).thenReturn(Optional.of(pessoa));
        when(ativoRepository.save(any())).thenReturn(ativoOriginal);

        ativoService.atualizar(10L, updateDTO);

        verify(depreciacaoService, never()).recalcularDepreciacaoCompleta(anyLong());
    }
}
