package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.mapper.FuncionarioMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private FuncionarioMapper funcionarioMapper;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private FilialRepository filialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FuncionarioService funcionarioService;

    private FuncionarioCreateDTO createDTO;
    private FuncionarioUpdateDTO updateDTO;
    private Departamento departamento;
    private Filial filial;
    private Funcionario funcionario;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);

        departamento = new Departamento();
        departamento.setId(1L);

        createDTO = new FuncionarioCreateDTO("Novo Funcionario", "M-001", "Tester", 1L, Set.of(1L), "teste@aegis.com", "password123", "ROLE_USER");
        updateDTO = new FuncionarioUpdateDTO("Funcionario Atualizado", "M-002", "Senior Tester", 1L, Status.ATIVO, Set.of(1L), "update@aegis.com", "newPassword", "ROLE_ADMIN");

        funcionario = new Funcionario();
        funcionario.setId(1L);
        
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("original@aegis.com");
        usuario.setFuncionario(funcionario);
        funcionario.setUsuario(usuario);
    }

    @Test
    @DisplayName("Criar: Deve criar Funcionario e Usuario com sucesso")
    void criar_quandoValido_deveSalvarFuncionarioEUsuario() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departamentoRepository.findById(anyLong())).thenReturn(Optional.of(departamento));
        when(filialRepository.findAllById(any())).thenReturn(List.of(filial));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(funcionario);

        funcionarioService.criar(createDTO);

        verify(funcionarioRepository).save(any(Funcionario.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção se email já existir")
    void criar_quandoEmailJaExiste_deveLancarExcecao() {
        when(usuarioRepository.findByEmail(createDTO.email())).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> funcionarioService.criar(createDTO));
        verify(funcionarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Atualizar: Deve atualizar Funcionario e Usuario com sucesso")
    void atualizar_quandoValido_deveSalvarFuncionarioEUsuario() {
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departamentoRepository.findById(anyLong())).thenReturn(Optional.of(departamento));
        when(filialRepository.findAllById(any())).thenReturn(List.of(filial));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(funcionario);

        funcionarioService.atualizar(1L, updateDTO);

        verify(funcionarioRepository).save(funcionario);
        verify(passwordEncoder).encode("newPassword");
        assertEquals("Funcionario Atualizado", funcionario.getNome());
        assertEquals("update@aegis.com", funcionario.getUsuario().getEmail());
    }

    @Test
    @DisplayName("Atualizar: Não deve alterar a senha se for nula ou vazia")
    void atualizar_quandoSenhaNula_naoDeveAlterarSenha() {
        FuncionarioUpdateDTO updateSemSenha = new FuncionarioUpdateDTO("Nome", "M-003", "Cargo", 1L, Status.ATIVO, Set.of(1L), "email@email.com", " ", "ROLE_USER");

        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(departamentoRepository.findById(anyLong())).thenReturn(Optional.of(departamento));
        when(filialRepository.findAllById(any())).thenReturn(List.of(filial));
        when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(funcionario);

        funcionarioService.atualizar(1L, updateSemSenha);

        verify(passwordEncoder, never()).encode(anyString());
        verify(funcionarioRepository).save(funcionario);
    }

    @Test
    @DisplayName("Deletar: Deve chamar deleteById do repositório")
    void deletar_quandoValido_deveChamarDelete() {
        when(funcionarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(funcionarioRepository).deleteById(1L);

        funcionarioService.deletar(1L);

        verify(funcionarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se funcionário não existe")
    void deletar_quandoNaoExiste_deveLancarExcecao() {
        when(funcionarioRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> funcionarioService.deletar(99L));
    }
}
