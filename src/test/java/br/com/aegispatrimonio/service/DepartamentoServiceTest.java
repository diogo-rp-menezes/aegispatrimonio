package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.mapper.DepartamentoMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
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
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private DepartamentoMapper departamentoMapper;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private DepartamentoService departamentoService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private Pessoa admin, userFilialA;
    private Filial filialA, filialB;
    private Departamento departamento;

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

        departamento = new Departamento();
        departamento.setId(10L);
        departamento.setNome("TI");
        departamento.setFilial(filialA);

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
    @DisplayName("Criar: Deve lançar exceção se nome do departamento já existir na mesma filial")
    void criar_quandoNomeDuplicadoNaFilial_deveLancarExcecao() {
        mockUser(admin);
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("TI", 1L);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        // CORREÇÃO: Retornar o departamento com ID para evitar NullPointerException na validação.
        when(departamentoRepository.findByNomeAndFilialId("TI", 1L)).thenReturn(Optional.of(departamento));

        assertThrows(IllegalArgumentException.class, () -> departamentoService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve permitir criar departamento com nome duplicado em filial diferente")
    void criar_quandoNomeDuplicadoEmOutraFilial_deveCriar() {
        mockUser(admin);
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("Financeiro", 2L); // Criando na Filial B
        when(filialRepository.findById(2L)).thenReturn(Optional.of(filialB));
        // Simula que "Financeiro" já existe, mas em outra filial (não será encontrado por findByNomeAndFilialId)
        when(departamentoRepository.findByNomeAndFilialId("Financeiro", 2L)).thenReturn(Optional.empty());
        when(departamentoMapper.toEntity(createDTO)).thenReturn(new Departamento());
        when(departamentoRepository.save(any(Departamento.class))).thenReturn(new Departamento());

        assertDoesNotThrow(() -> departamentoService.criar(createDTO));
        verify(departamentoRepository).save(any(Departamento.class));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se houver pessoas associadas")
    void deletar_quandoPessoasAssociadas_deveLancarExcecao() {
        mockUser(admin);
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));
        when(pessoaRepository.existsByDepartamentoId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> departamentoService.deletar(10L));
        verify(departamentoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deletar: Deve deletar com sucesso quando não há dependências")
    void deletar_quandoSemPessoasAssociadas_deveDeletar() {
        mockUser(admin);
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));
        when(pessoaRepository.existsByDepartamentoId(10L)).thenReturn(false);

        departamentoService.deletar(10L);

        verify(departamentoRepository).delete(departamento);
    }

    @Test
    @DisplayName("BuscarPorId: Não-admin não pode ver departamento de outra filial")
    void buscarPorId_quandoNaoAdminEOutraFilial_deveLancarExcecao() {
        mockUser(userFilialA);
        Departamento deptoFilialB = new Departamento();
        deptoFilialB.setId(20L);
        deptoFilialB.setFilial(filialB);
        when(departamentoRepository.findById(20L)).thenReturn(Optional.of(deptoFilialB));

        assertThrows(AccessDeniedException.class, () -> departamentoService.buscarPorId(20L));
    }
}
