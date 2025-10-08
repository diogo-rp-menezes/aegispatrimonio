package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.PessoaCreateDTO;
import br.com.aegispatrimonio.dto.PessoaDTO;
import br.com.aegispatrimonio.dto.PessoaUpdateDTO;
import br.com.aegispatrimonio.mapper.PessoaMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.Status;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;
    @Mock
    private PessoaMapper pessoaMapper;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PessoaService pessoaService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private Pessoa admin, userFilialA;
    private Filial filialA, filialB;
    private Departamento deptoFilialA;

    @BeforeEach
    void setUp() {
        filialA = new Filial();
        filialA.setId(1L);
        filialA.setNome("Filial A");

        filialB = new Filial();
        filialB.setId(2L);
        filialB.setNome("Filial B");

        deptoFilialA = new Departamento();
        deptoFilialA.setId(1L);
        deptoFilialA.setFilial(filialA);

        admin = new Pessoa();
        admin.setId(1L);
        admin.setRole("ROLE_ADMIN");
        admin.setFilial(filialA);
        admin.setDepartamento(deptoFilialA);

        userFilialA = new Pessoa();
        userFilialA.setId(2L);
        userFilialA.setRole("ROLE_USER");
        userFilialA.setFilial(filialA);
        userFilialA.setDepartamento(deptoFilialA);

        // Mock do SecurityContextHolder
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
    @DisplayName("Criar: Usuário não-admin não pode criar usuário em outra filial")
    void criar_quandoNaoAdminTentaCriarEmOutraFilial_deveLancarExcecao() {
        mockUser(userFilialA);
        PessoaCreateDTO createDTO = new PessoaCreateDTO(2L, "Nome", "Matr", "Cargo", "email@email.com", "senha", "ROLE_USER", 2L);

        assertThrows(AccessDeniedException.class, () -> pessoaService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se departamento não pertence à filial")
    void criar_quandoDepartamentoNaoPertenceAFilial_deveLancarExcecao() {
        mockUser(admin);
        PessoaCreateDTO createDTO = new PessoaCreateDTO(2L, "Nome", "Matr", "Cargo", "email@email.com", "senha", "ROLE_USER", 1L);

        when(filialRepository.findById(2L)).thenReturn(Optional.of(filialB));
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(deptoFilialA)); // Depto da Filial A

        assertThrows(IllegalArgumentException.class, () -> pessoaService.criar(createDTO));
    }

    @Test
    @DisplayName("Criar: Deve encodar a senha ao criar usuário")
    void criar_deveEncodarSenha() {
        mockUser(admin);
        PessoaCreateDTO createDTO = new PessoaCreateDTO(1L, "Nome", "Matr", "Cargo", "email@email.com", "senha123", "ROLE_USER", 1L);
        Pessoa pessoa = new Pessoa();

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filialA));
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(deptoFilialA));
        when(pessoaMapper.toEntity(createDTO)).thenReturn(pessoa);
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        pessoaService.criar(createDTO);

        verify(passwordEncoder).encode("senha123");
        verify(pessoaRepository).save(pessoa);
    }

    @Test
    @DisplayName("Atualizar: Deve encodar a senha se uma nova for fornecida")
    void atualizar_quandoNovaSenhaFornecida_deveEncodar() {
        mockUser(admin);
        PessoaUpdateDTO updateDTO = new PessoaUpdateDTO(1L, "Nome", "Matr", "Cargo", "email@email.com", "novaSenha456", "ROLE_USER", 1L, Status.ATIVO);

        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(userFilialA));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(userFilialA);

        pessoaService.atualizar(2L, updateDTO);

        verify(passwordEncoder).encode("novaSenha456");
        verify(pessoaRepository).save(userFilialA);
    }

    @Test
    @DisplayName("Atualizar: Não deve encodar a senha se for nula ou vazia")
    void atualizar_quandoSenhaNulaOuVazia_naoDeveEncodar() {
        mockUser(admin);
        PessoaUpdateDTO updateDTO = new PessoaUpdateDTO(1L, "Nome", "Matr", "Cargo", "email@email.com", " ", "ROLE_USER", 1L, Status.ATIVO);

        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(userFilialA));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(userFilialA);

        pessoaService.atualizar(2L, updateDTO);

        verify(passwordEncoder, never()).encode(anyString());
        verify(pessoaRepository).save(userFilialA);
    }

    @Test
    @DisplayName("Deletar: Deve deletar usuário com sucesso")
    void deletar_deveDeletarUsuario() {
        mockUser(admin);
        when(pessoaRepository.findById(2L)).thenReturn(Optional.of(userFilialA));

        pessoaService.deletar(2L);

        verify(pessoaRepository).deleteById(2L);
    }

    @Test
    @DisplayName("Deletar: Não-admin não pode deletar usuário de outra filial")
    void deletar_quandoNaoAdminTentaDeletarEmOutraFilial_deveLancarExcecao() {
        mockUser(userFilialA);
        Pessoa userFilialB = new Pessoa();
        userFilialB.setId(3L);
        userFilialB.setFilial(filialB);

        when(pessoaRepository.findById(3L)).thenReturn(Optional.of(userFilialB));

        assertThrows(AccessDeniedException.class, () -> pessoaService.deletar(3L));
        verify(pessoaRepository, never()).deleteById(any());
    }
}
