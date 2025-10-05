package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.FilialRequestDTO;
import br.com.aegispatrimonio.dto.response.FilialResponseDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.FilialRepository;
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
class FilialServiceTest {

    @InjectMocks
    private FilialService filialService;

    @Mock
    private FilialRepository filialRepository;

    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);
        filial.setNome("Filial Matriz");
        filial.setCodigo("MAT-01");
    }

    @Test
    @DisplayName("Deve criar uma nova filial com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarFilialResponseDTO() {
        FilialRequestDTO request = new FilialRequestDTO();
        request.setNome("Filial Nova");
        request.setCodigo("FIL-02");

        when(filialRepository.existsByCodigo("FIL-02")).thenReturn(false);
        when(filialRepository.existsByNome("Filial Nova")).thenReturn(false);

        when(filialRepository.save(any(Filial.class))).thenAnswer(invocation -> {
            Filial filialSalva = invocation.getArgument(0);
            filialSalva.setId(2L);
            return filialSalva;
        });

        FilialResponseDTO response = filialService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Filial Nova", response.getNome());
        assertEquals("FIL-02", response.getCodigo());

        verify(filialRepository, times(1)).existsByCodigo("FIL-02");
        verify(filialRepository, times(1)).existsByNome("Filial Nova");
        verify(filialRepository, times(1)).save(any(Filial.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar filial com código duplicado")
    void criar_quandoCodigoJaExiste_deveLancarExcecao() {
        FilialRequestDTO request = new FilialRequestDTO();
        request.setCodigo("MAT-01");
        request.setNome("Outro Nome");

        when(filialRepository.existsByCodigo("MAT-01")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            filialService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Já existe uma filial com o código"));
        verify(filialRepository, never()).existsByNome(anyString());
        verify(filialRepository, never()).save(any(Filial.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar filial com nome duplicado")
    void criar_quandoNomeJaExiste_deveLancarExcecao() {
        FilialRequestDTO request = new FilialRequestDTO();
        request.setCodigo("FIL-03");
        request.setNome("Filial Matriz");

        when(filialRepository.existsByCodigo("FIL-03")).thenReturn(false);
        when(filialRepository.existsByNome("Filial Matriz")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            filialService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Já existe uma filial com o nome"));
        verify(filialRepository, times(1)).existsByCodigo("FIL-03");
        verify(filialRepository, never()).save(any(Filial.class));
    }

    @Test
    @DisplayName("Deve atualizar uma filial com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarFilialResponseDTO() {
        // --- Arrange (Organizar) ---
        Long filialId = 1L;
        String novoNome = "Filial Matriz Atualizada";
        FilialRequestDTO request = new FilialRequestDTO();
        request.setNome(novoNome);
        request.setCodigo(filial.getCodigo()); // Mesmo código

        when(filialRepository.findById(filialId)).thenReturn(Optional.of(filial));
        // Como o nome mudou, a validação será chamada. Simulamos que o novo nome é válido.
        when(filialRepository.existsByNome(novoNome)).thenReturn(false);
        when(filialRepository.save(any(Filial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        FilialResponseDTO response = filialService.atualizar(filialId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(filialId, response.getId());
        assertEquals(novoNome, response.getNome());

        verify(filialRepository, times(1)).findById(filialId);
        verify(filialRepository, times(1)).existsByNome(novoNome); // Verificamos que a validação de nome FOI chamada
        verify(filialRepository, never()).existsByCodigo(anyString()); // Verificamos que a validação de código NÃO foi chamada
        verify(filialRepository, times(1)).save(any(Filial.class));
    }

    @Test
    @DisplayName("Deve buscar uma filial por ID e retornar DTO quando encontrada")
    void buscarPorId_quandoFilialExiste_deveRetornarOptionalDeFilialResponseDTO() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));

        Optional<FilialResponseDTO> resultado = filialService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        FilialResponseDTO dto = resultado.get();
        assertEquals(filial.getId(), dto.getId());
        assertEquals(filial.getNome(), dto.getNome());

        verify(filialRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar uma filial por ID e retornar vazio quando não encontrada")
    void buscarPorId_quandoFilialNaoExiste_deveRetornarOptionalVazio() {
        when(filialRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<FilialResponseDTO> resultado = filialService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(filialRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' uma filial (soft delete) com sucesso quando a filial existe")
    void deletar_quandoFilialExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long filialId = 1L;
        when(filialRepository.findById(filialId)).thenReturn(Optional.of(filial));

        assertDoesNotThrow(() -> filialService.deletar(filialId));

        verify(filialRepository, times(1)).delete(filial);
    }
}
