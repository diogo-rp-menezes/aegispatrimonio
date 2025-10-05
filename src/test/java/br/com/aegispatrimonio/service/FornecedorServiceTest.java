package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.FornecedorRequestDTO;
import br.com.aegispatrimonio.dto.response.FornecedorResponseDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.repository.FornecedorRepository;
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
class FornecedorServiceTest {

    @InjectMocks
    private FornecedorService fornecedorService;

    @Mock
    private FornecedorRepository fornecedorRepository;

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Fornecedor Principal");
        fornecedor.setEmailContato("contato@principal.com");
    }

    @Test
    @DisplayName("Deve criar um novo fornecedor com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarFornecedorResponseDTO() {
        FornecedorRequestDTO request = new FornecedorRequestDTO();
        request.setNome("Novo Fornecedor");
        request.setEmailContato("contato@novo.com");

        when(fornecedorRepository.existsByNome("Novo Fornecedor")).thenReturn(false);

        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(invocation -> {
            Fornecedor fornecedorSalvo = invocation.getArgument(0);
            fornecedorSalvo.setId(2L);
            return fornecedorSalvo;
        });

        FornecedorResponseDTO response = fornecedorService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Novo Fornecedor", response.getNome());

        verify(fornecedorRepository, times(1)).existsByNome("Novo Fornecedor");
        verify(fornecedorRepository, times(1)).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar fornecedor com nome duplicado")
    void criar_quandoNomeJaExiste_deveLancarExcecao() {
        FornecedorRequestDTO request = new FornecedorRequestDTO();
        request.setNome("Fornecedor Principal");

        when(fornecedorRepository.existsByNome("Fornecedor Principal")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fornecedorService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Já existe um fornecedor com o nome"));
        verify(fornecedorRepository, never()).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve atualizar um fornecedor com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarFornecedorResponseDTO() {
        // --- Arrange (Organizar) ---
        Long fornecedorId = 1L;
        String novoNome = "Fornecedor Principal Ltda.";
        FornecedorRequestDTO request = new FornecedorRequestDTO();
        request.setNome(novoNome);

        when(fornecedorRepository.findById(fornecedorId)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.existsByNome(novoNome)).thenReturn(false);
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        FornecedorResponseDTO response = fornecedorService.atualizar(fornecedorId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(fornecedorId, response.getId());
        assertEquals(novoNome, response.getNome());

        verify(fornecedorRepository, times(1)).findById(fornecedorId);
        verify(fornecedorRepository, times(1)).existsByNome(novoNome);
        verify(fornecedorRepository, times(1)).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve buscar um fornecedor por ID e retornar DTO quando encontrado")
    void buscarPorId_quandoFornecedorExiste_deveRetornarOptionalDeFornecedorResponseDTO() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));

        Optional<FornecedorResponseDTO> resultado = fornecedorService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        FornecedorResponseDTO dto = resultado.get();
        assertEquals(fornecedor.getId(), dto.getId());
        assertEquals(fornecedor.getNome(), dto.getNome());

        verify(fornecedorRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar um fornecedor por ID e retornar vazio quando não encontrado")
    void buscarPorId_quandoFornecedorNaoExiste_deveRetornarOptionalVazio() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<FornecedorResponseDTO> resultado = fornecedorService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(fornecedorRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' um fornecedor (soft delete) com sucesso quando ele existe")
    void deletar_quandoFornecedorExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long fornecedorId = 1L;
        when(fornecedorRepository.findById(fornecedorId)).thenReturn(Optional.of(fornecedor));

        assertDoesNotThrow(() -> fornecedorService.deletar(fornecedorId));

        verify(fornecedorRepository, times(1)).delete(fornecedor);
    }
}
