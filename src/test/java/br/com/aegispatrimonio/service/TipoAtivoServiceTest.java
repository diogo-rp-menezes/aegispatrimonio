package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.TipoAtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.TipoAtivoResponseDTO;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
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
class TipoAtivoServiceTest {

    @InjectMocks
    private TipoAtivoService tipoAtivoService;

    @Mock
    private TipoAtivoRepository tipoAtivoRepository;

    private TipoAtivo tipoAtivo;

    @BeforeEach
    void setUp() {
        tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Móveis");
        tipoAtivo.setCategoriaContabil("IMOBILIZADO");
    }

    @Test
    @DisplayName("Deve criar um novo tipo de ativo com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarTipoAtivoResponseDTO() {
        TipoAtivoRequestDTO request = new TipoAtivoRequestDTO();
        request.setNome("Veículos");
        request.setCategoriaContabil("IMOBILIZADO");

        when(tipoAtivoRepository.existsByNome("Veículos")).thenReturn(false);

        when(tipoAtivoRepository.save(any(TipoAtivo.class))).thenAnswer(invocation -> {
            TipoAtivo tipoAtivoSalvo = invocation.getArgument(0);
            tipoAtivoSalvo.setId(2L);
            return tipoAtivoSalvo;
        });

        TipoAtivoResponseDTO response = tipoAtivoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Veículos", response.getNome());

        verify(tipoAtivoRepository, times(1)).existsByNome("Veículos");
        verify(tipoAtivoRepository, times(1)).save(any(TipoAtivo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar tipo de ativo com nome duplicado")
    void criar_quandoNomeJaExiste_deveLancarExcecao() {
        TipoAtivoRequestDTO request = new TipoAtivoRequestDTO();
        request.setNome("Móveis");

        when(tipoAtivoRepository.existsByNome("Móveis")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tipoAtivoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Já existe um tipo de ativo com o nome"));
        verify(tipoAtivoRepository, never()).save(any(TipoAtivo.class));
    }

    @Test
    @DisplayName("Deve atualizar um tipo de ativo com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarTipoAtivoResponseDTO() {
        // --- Arrange (Organizar) ---
        Long tipoAtivoId = 1L;
        String novoNome = "Móveis de Escritório";
        TipoAtivoRequestDTO request = new TipoAtivoRequestDTO();
        request.setNome(novoNome);

        when(tipoAtivoRepository.findById(tipoAtivoId)).thenReturn(Optional.of(tipoAtivo));
        when(tipoAtivoRepository.existsByNome(novoNome)).thenReturn(false);
        when(tipoAtivoRepository.save(any(TipoAtivo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        TipoAtivoResponseDTO response = tipoAtivoService.atualizar(tipoAtivoId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(tipoAtivoId, response.getId());
        assertEquals(novoNome, response.getNome());

        verify(tipoAtivoRepository, times(1)).findById(tipoAtivoId);
        verify(tipoAtivoRepository, times(1)).existsByNome(novoNome);
        verify(tipoAtivoRepository, times(1)).save(any(TipoAtivo.class));
    }

    @Test
    @DisplayName("Deve buscar um tipo de ativo por ID e retornar DTO quando encontrado")
    void buscarPorId_quandoTipoAtivoExiste_deveRetornarOptionalDeTipoAtivoResponseDTO() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));

        Optional<TipoAtivoResponseDTO> resultado = tipoAtivoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        TipoAtivoResponseDTO dto = resultado.get();
        assertEquals(tipoAtivo.getId(), dto.getId());
        assertEquals(tipoAtivo.getNome(), dto.getNome());

        verify(tipoAtivoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar um tipo de ativo por ID e retornar vazio quando não encontrado")
    void buscarPorId_quandoTipoAtivoNaoExiste_deveRetornarOptionalVazio() {
        when(tipoAtivoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TipoAtivoResponseDTO> resultado = tipoAtivoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(tipoAtivoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' um tipo de ativo (soft delete) com sucesso quando ele existe")
    void deletar_quandoTipoAtivoExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long tipoAtivoId = 1L;
        when(tipoAtivoRepository.findById(tipoAtivoId)).thenReturn(Optional.of(tipoAtivo));

        assertDoesNotThrow(() -> tipoAtivoService.deletar(tipoAtivoId));

        verify(tipoAtivoRepository, times(1)).delete(tipoAtivo);
    }
}
