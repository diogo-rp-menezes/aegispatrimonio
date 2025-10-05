package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @InjectMocks
    private PessoaService pessoaService;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    private Pessoa pessoa;
    private Departamento departamento;

    @BeforeEach
    void setUp() {
        departamento = new Departamento();
        departamento.setId(1L);
        departamento.setNome("TI");

        pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("João da Silva");
        pessoa.setEmail("joao.silva@empresa.com");
        pessoa.setDepartamento(departamento);
    }

    @Test
    @DisplayName("Deve criar uma nova pessoa com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarPessoaResponseDTO() {
        PessoaRequestDTO request = new PessoaRequestDTO();
        request.setNome("Maria Oliveira");
        request.setEmail("maria.oliveira@empresa.com");
        request.setDepartamentoId(1L);

        when(pessoaRepository.existsByEmail("maria.oliveira@empresa.com")).thenReturn(false);
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));

        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> {
            Pessoa pessoaSalva = invocation.getArgument(0);
            pessoaSalva.setId(2L);
            return pessoaSalva;
        });

        PessoaResponseDTO response = pessoaService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Maria Oliveira", response.getNome());
        verify(pessoaRepository, times(1)).existsByEmail("maria.oliveira@empresa.com");
        verify(departamentoRepository, times(1)).findById(1L);
        verify(pessoaRepository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar pessoa com email duplicado")
    void criar_quandoEmailJaExiste_deveLancarExcecao() {
        PessoaRequestDTO request = new PessoaRequestDTO();
        request.setEmail("joao.silva@empresa.com");

        when(pessoaRepository.existsByEmail("joao.silva@empresa.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pessoaService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Email já cadastrado"));
        verify(departamentoRepository, never()).findById(anyLong());
        verify(pessoaRepository, never()).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar pessoa com Departamento inexistente")
    void criar_quandoDepartamentoNaoExiste_deveLancarExcecao() {
        PessoaRequestDTO request = new PessoaRequestDTO();
        request.setEmail("novo.email@empresa.com");
        request.setDepartamentoId(99L);

        when(pessoaRepository.existsByEmail("novo.email@empresa.com")).thenReturn(false);
        when(departamentoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pessoaService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Departamento não encontrado"));
        verify(pessoaRepository, never()).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve atualizar uma pessoa com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarPessoaResponseDTO() {
        // --- Arrange (Organizar) ---
        Long pessoaId = 1L;
        PessoaRequestDTO request = new PessoaRequestDTO();
        request.setNome("João da Silva Santos");
        request.setEmail("joao.santos@empresa.com");
        request.setDepartamentoId(1L);

        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existsByEmail("joao.santos@empresa.com")).thenReturn(false);
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        PessoaResponseDTO response = pessoaService.atualizar(pessoaId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(pessoaId, response.getId());
        assertEquals("João da Silva Santos", response.getNome());
        assertEquals("joao.santos@empresa.com", response.getEmail());

        verify(pessoaRepository, times(1)).findById(pessoaId);
        verify(pessoaRepository, times(1)).existsByEmail("joao.santos@empresa.com");
        verify(pessoaRepository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve buscar uma pessoa por ID e retornar DTO quando encontrada")
    void buscarPorId_quandoPessoaExiste_deveRetornarOptionalDePessoaResponseDTO() {
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));

        Optional<PessoaResponseDTO> resultado = pessoaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        PessoaResponseDTO dto = resultado.get();
        assertEquals(pessoa.getId(), dto.getId());
        assertEquals(pessoa.getNome(), dto.getNome());

        verify(pessoaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar uma pessoa por ID e retornar vazio quando não encontrada")
    void buscarPorId_quandoPessoaNaoExiste_deveRetornarOptionalVazio() {
        when(pessoaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PessoaResponseDTO> resultado = pessoaService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(pessoaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' uma pessoa (soft delete) com sucesso quando a pessoa existe")
    void deletar_quandoPessoaExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long pessoaId = 1L;
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));

        assertDoesNotThrow(() -> pessoaService.deletar(pessoaId));

        verify(pessoaRepository, times(1)).delete(pessoa);
    }
}
