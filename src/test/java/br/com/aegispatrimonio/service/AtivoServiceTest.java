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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
// CORREÇÃO: Importar todos os métodos estáticos do Mockito para resolver os erros de compilação.
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
    @Mock
    private SearchOptimizationService searchOptimizationService;
    @Mock
    private CurrentUserProvider currentUserProvider; // Adicionado mock para CurrentUserProvider
    @Mock
    private AtivoHealthHistoryRepository healthHistoryRepository;

    @InjectMocks
    private AtivoService ativoService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    // Entidades de Teste
    private Usuario adminUser, regularUser;
    private Filial filialA, filialB;
    private Ativo ativo;
    private Localizacao localizacao;

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
        adminUser.setEmail("admin@aegis.com"); // Adicionado email
        adminUser.setFuncionario(adminFunc);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setFiliais(Set.of(filialA));
        regularUser = new Usuario();
        regularUser.setId(2L);
        regularUser.setRole("ROLE_USER");
        regularUser.setEmail("user@aegis.com"); // Adicionado email
        regularUser.setFuncionario(funcionario);

        ativo = new Ativo();
        ativo.setId(10L);
        ativo.setFilial(filialA);

        // Mock do currentUserProvider no setUp para garantir que sempre haja um usuário
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser); 
    }

    @AfterEach
    void tearDown() {
        // Se mockedSecurityContextHolder foi inicializado, feche-o.
        // if (mockedSecurityContextHolder != null) {
        //     mockedSecurityContextHolder.close();
        // }
    }

    private void mockUser(Usuario usuario) {
        // Agora mockamos o currentUserProvider diretamente
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(usuario);
    }

    @Test
    @DisplayName("BuscarPorId: Usuário comum não pode acessar ativo de outra filial")
    void buscarPorId_quandoNaoAdminEOutraFilial_deveLancarExcecao() {
        mockUser(regularUser);
        Ativo ativoOutraFilial = new Ativo();
        ativoOutraFilial.setId(11L);
        ativoOutraFilial.setFilial(filialB);
        when(ativoRepository.findByIdWithDetails(11L)).thenReturn(Optional.of(ativoOutraFilial));
        // CORREÇÃO: Mockar o repositório de funcionários para garantir que a verificação de segurança ocorra
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(regularUser.getFuncionario()));

        assertThrows(AccessDeniedException.class, () -> ativoService.buscarPorId(11L));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se localização não pertence à filial do ativo")
    void criar_quandoLocalizacaoInconsistente_deveLancarExcecao() {
        Localizacao localizacaoOutraFilial = new Localizacao();
        localizacaoOutraFilial.setId(2L);
        localizacaoOutraFilial.setFilial(filialB);

        AtivoCreateDTO createDTO = new AtivoCreateDTO(1L, "Notebook", 1L, "PAT-01", localizacaoOutraFilial.getId(), LocalDate.now(), 1L, BigDecimal.TEN, 1L, "Obs", null, null);

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

        AtivoUpdateDTO updateDTO = new AtivoUpdateDTO(1L, "Nome", "PAT-10", 1L, 1L, StatusAtivo.ATIVO, LocalDate.now(), 1L, BigDecimal.TEN, responsavelOutraFilial.getId(), "Obs", null, null);

        when(ativoRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(ativo));
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

    @Test
    @DisplayName("Criar: Deve salvar detalhes de hardware quando informados")
    void criar_comDetalhesHardware_deveSalvarCorretamente() {
        // Arrange
        br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO hardwareDTO = new br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO(
            "PC-01", "DOMAIN", "Windows 11", "22H2", "x64",
            "Dell", "Model X", "SN123", "i7", 8, 16
        );

        AtivoCreateDTO createDTO = new AtivoCreateDTO(
            1L, "PC Gamer", 1L, "PAT-99", 1L, LocalDate.now(), 1L,
            BigDecimal.valueOf(5000), 1L, "Obs", null, hardwareDTO
        );

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(new TipoAtivo()));
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(new Fornecedor()));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(adminUser.getFuncionario()));

        when(ativoRepository.save(any(Ativo.class))).thenAnswer(invocation -> {
            Ativo a = invocation.getArgument(0);
            a.setId(99L);
            return a;
        });

        // Mock para o retorno final (não precisa ter os detalhes para este teste passar, pois verificamos o capture)
        when(ativoRepository.findByIdWithDetails(99L)).thenReturn(Optional.of(ativo));

        // Act
        ativoService.criar(createDTO);

        // Assert
        org.mockito.ArgumentCaptor<Ativo> ativoCaptor = org.mockito.ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());

        Ativo ativoSalvo = ativoCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertNotNull(ativoSalvo.getDetalheHardware());
        org.junit.jupiter.api.Assertions.assertEquals("PC-01", ativoSalvo.getDetalheHardware().getComputerName());
        org.junit.jupiter.api.Assertions.assertEquals("Windows 11", ativoSalvo.getDetalheHardware().getOsName());
        org.junit.jupiter.api.Assertions.assertEquals(ativoSalvo, ativoSalvo.getDetalheHardware().getAtivo());
    }

    @Test
    @DisplayName("ListarTodos: Deve usar busca fuzzy quando nome é fornecido")
    void listarTodos_comBuscaFuzzy_deveRankearResultados() {
        // Arrange
        String query = "laptp";

        br.com.aegispatrimonio.dto.AtivoNameDTO n1 = new br.com.aegispatrimonio.dto.AtivoNameDTO(1L, "Desktop");
        br.com.aegispatrimonio.dto.AtivoNameDTO n2 = new br.com.aegispatrimonio.dto.AtivoNameDTO(2L, "Laptop");
        br.com.aegispatrimonio.dto.AtivoNameDTO n3 = new br.com.aegispatrimonio.dto.AtivoNameDTO(3L, "Lap Top");

        // Mock candidates returned by Repository
        java.util.List<br.com.aegispatrimonio.dto.AtivoNameDTO> candidates = java.util.List.of(n1, n2, n3);
        // Mock ranked result returned by Service
        java.util.List<br.com.aegispatrimonio.dto.AtivoNameDTO> ranked = java.util.List.of(n2, n3);

        mockUser(adminUser);

        // When finding candidates (name is null)
        when(ativoRepository.findSimpleByFilters(any(), any(), any(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(candidates);

        // When ranking
        when(searchOptimizationService.rankResults(eq(query), anyList(), any())).thenReturn((java.util.List) ranked);

        // When fetching details for the page content
        Ativo a2 = new Ativo(); a2.setId(2L); a2.setNome("Laptop");
        Ativo a3 = new Ativo(); a3.setId(3L); a3.setNome("Lap Top");
        when(ativoRepository.findAllByIdInWithDetails(anyList())).thenReturn(java.util.List.of(a2, a3));

        br.com.aegispatrimonio.dto.AtivoDTO dto2 = new br.com.aegispatrimonio.dto.AtivoDTO(2L, "Laptop", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        br.com.aegispatrimonio.dto.AtivoDTO dto3 = new br.com.aegispatrimonio.dto.AtivoDTO(3L, "Lap Top", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(ativoMapper.toDTO(a2)).thenReturn(dto2);
        when(ativoMapper.toDTO(a3)).thenReturn(dto3);

        // Act
        org.springframework.data.domain.Page<br.com.aegispatrimonio.dto.AtivoDTO> result = ativoService.listarTodos(
                org.springframework.data.domain.Pageable.ofSize(10),
                null, null, null, query, null);

        // Assert
        verify(searchOptimizationService).rankResults(eq(query), anyList(), any());
        verify(ativoRepository).findAllByIdInWithDetails(anyList());
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getContent().size());
        org.junit.jupiter.api.Assertions.assertEquals("Laptop", result.getContent().get(0).nome());
    }

    @Test
    @DisplayName("getHealthHistory: Deve retornar lista quando usuário tem permissão")
    void getHealthHistory_ShouldReturnList_WhenUserHasPermission() {
        mockUser(adminUser);
        AtivoHealthHistory history = new AtivoHealthHistory();
        history.setDataRegistro(java.time.LocalDateTime.now());
        history.setComponente("DISK:0");
        history.setValor(100.0);
        history.setMetrica("FREE_SPACE_GB");

        when(ativoRepository.findById(10L)).thenReturn(Optional.of(ativo));
        when(healthHistoryRepository.findByAtivoIdAndMetricaOrderByDataRegistroAsc(10L, "FREE_SPACE_GB"))
                .thenReturn(java.util.List.of(history));

        java.util.List<br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO> result = ativoService.getHealthHistory(10L);

        org.junit.jupiter.api.Assertions.assertFalse(result.isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals("DISK:0", result.get(0).componente());
    }
}
