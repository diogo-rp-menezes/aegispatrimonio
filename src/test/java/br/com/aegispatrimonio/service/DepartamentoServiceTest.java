package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.mapper.DepartamentoMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private DepartamentoMapper departamentoMapper;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private DepartamentoService departamentoService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    // Entidades de Teste
    private Usuario adminUser, regularUser;
    private Filial filialA, filialB;
    private Departamento deptoFilialA, deptoFilialB;

    @BeforeEach
    void setUp() {
        // Configuração de Filiais
        filialA = new Filial();
        filialA.setId(1L);
        filialB = new Filial();
        filialB.setId(2L);

        // Configuração de Usuários e Funcionários
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

        // Configuração de Departamentos
        deptoFilialA = new Departamento();
        deptoFilialA.setId(10L);
        deptoFilialA.setNome("TI");
        deptoFilialA.setFilial(filialA);

        deptoFilialB = new Departamento();
        deptoFilialB.setId(20L);
        deptoFilialB.setNome("RH");
        deptoFilialB.setFilial(filialB);

        // Mock do SecurityContext
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
    @DisplayName("ListarTodos: Admin deve retornar todos os departamentos")
    void listarTodos_quandoAdmin_deveRetornarTodos() {
        mockUser(adminUser);
        when(departamentoRepository.findAll()).thenReturn(List.of(deptoFilialA, deptoFilialB));

        departamentoService.listarTodos();

        verify(departamentoRepository).findAll();
        verify(departamentoRepository, never()).findByFilialIdIn(any());
    }

    @Test
    @DisplayName("ListarTodos: Usuário comum deve retornar apenas departamentos de suas filiais")
    void listarTodos_quandoNaoAdmin_deveRetornarApenasDaSuaFilial() {
        mockUser(regularUser);
        Set<Long> expectedFilialIds = Set.of(1L);
        when(departamentoRepository.findByFilialIdIn(expectedFilialIds)).thenReturn(List.of(deptoFilialA));

        departamentoService.listarTodos();

        verify(departamentoRepository, never()).findAll();
        verify(departamentoRepository).findByFilialIdIn(expectedFilialIds);
    }

    @Test
    @DisplayName("BuscarPorId: Admin pode acessar departamento de qualquer filial")
    void buscarPorId_quandoAdmin_deveRetornarDepartamento() {
        mockUser(adminUser);
        when(departamentoRepository.findById(20L)).thenReturn(Optional.of(deptoFilialB));

        assertDoesNotThrow(() -> departamentoService.buscarPorId(20L));
        verify(departamentoRepository).findById(20L);
    }

    @Test
    @DisplayName("BuscarPorId: Usuário comum não pode acessar departamento de outra filial")
    void buscarPorId_quandoNaoAdminEOutraFilial_deveLancarExcecao() {
        mockUser(regularUser);
        when(departamentoRepository.findById(20L)).thenReturn(Optional.of(deptoFilialB));

        assertThrows(AccessDeniedException.class, () -> departamentoService.buscarPorId(20L));
    }

    @Test
    @DisplayName("Criar: Deve criar departamento com sucesso")
    void criar_quandoValido_deveSalvar() {
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("Financeiro", 1L);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(departamentoRepository.findByNomeAndFilialId("Financeiro", 1L)).thenReturn(Optional.empty());
        when(departamentoMapper.toEntity(createDTO)).thenReturn(new Departamento());

        departamentoService.criar(createDTO);

        verify(departamentoRepository).save(any(Departamento.class));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção para nome duplicado na mesma filial")
    void criar_quandoNomeDuplicado_deveLancarExcecao() {
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("TI", 1L);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(departamentoRepository.findByNomeAndFilialId("TI", 1L)).thenReturn(Optional.of(deptoFilialA));

        assertThrows(IllegalArgumentException.class, () -> departamentoService.criar(createDTO));
    }

    @Test
    @DisplayName("Deletar: Deve deletar com sucesso quando não há dependências")
    void deletar_quandoSemDependencias_deveChamarDelete() {
        when(departamentoRepository.existsById(10L)).thenReturn(true);
        when(funcionarioRepository.existsByDepartamentoId(10L)).thenReturn(false);

        departamentoService.deletar(10L);

        verify(departamentoRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se houver funcionários associados")
    void deletar_quandoFuncionarioAssociado_deveLancarExcecao() {
        when(departamentoRepository.existsById(10L)).thenReturn(true);
        when(funcionarioRepository.existsByDepartamentoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> departamentoService.deletar(10L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se departamento não existe")
    void deletar_quandoNaoExiste_deveLancarExcecao() {
        when(departamentoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> departamentoService.deletar(99L));
    }
}
