package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.LocalizacaoResponseDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
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
class LocalizacaoServiceTest {

    @InjectMocks
    private LocalizacaoService localizacaoService;

    @Mock
    private LocalizacaoRepository localizacaoRepository;

    @Mock
    private FilialRepository filialRepository;

    private Localizacao localizacao;
    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);
        filial.setNome("Filial Teste");

        localizacao = new Localizacao();
        localizacao.setId(1L);
        localizacao.setNome("Andar 1");
        localizacao.setFilial(filial);
    }

    @Test
    @DisplayName("Deve criar uma nova localização com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarLocalizacaoResponseDTO() {
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setNome("Sala de Reuniões");
        request.setFilialId(1L);
        request.setLocalizacaoPaiId(1L);

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));

        when(localizacaoRepository.save(any(Localizacao.class))).thenAnswer(invocation -> {
            Localizacao locSalva = invocation.getArgument(0);
            locSalva.setId(2L);
            return locSalva;
        });

        LocalizacaoResponseDTO response = localizacaoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Sala de Reuniões", response.getNome());

        verify(filialRepository, times(1)).findById(1L);
        verify(localizacaoRepository, times(1)).findById(1L);
        verify(localizacaoRepository, times(1)).save(any(Localizacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar localização com Filial inexistente")
    void criar_quandoFilialNaoExiste_deveLancarExcecao() {
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setFilialId(99L);

        when(filialRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localizacaoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Filial não encontrada"));
        verify(localizacaoRepository, never()).save(any(Localizacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar localização com Localização Pai inexistente")
    void criar_quandoLocalizacaoPaiNaoExiste_deveLancarExcecao() {
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setFilialId(1L);
        request.setLocalizacaoPaiId(99L);

        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(localizacaoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localizacaoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Localização pai não encontrada"));
        verify(localizacaoRepository, never()).save(any(Localizacao.class));
    }

    @Test
    @DisplayName("Deve atualizar uma localização com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarLocalizacaoResponseDTO() {
        // --- Arrange (Organizar) ---
        Long localizacaoId = 1L;
        LocalizacaoRequestDTO request = new LocalizacaoRequestDTO();
        request.setNome("Andar 1 - Atualizado");
        request.setFilialId(1L);

        when(localizacaoRepository.findById(localizacaoId)).thenReturn(Optional.of(localizacao));
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(localizacaoRepository.save(any(Localizacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        LocalizacaoResponseDTO response = localizacaoService.atualizar(localizacaoId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(localizacaoId, response.getId());
        assertEquals("Andar 1 - Atualizado", response.getNome());

        verify(localizacaoRepository, times(1)).findById(localizacaoId);
        verify(localizacaoRepository, times(1)).save(any(Localizacao.class));
    }

    @Test
    @DisplayName("Deve buscar uma localização por ID e retornar DTO quando encontrada")
    void buscarPorId_quandoLocalizacaoExiste_deveRetornarOptionalDeLocalizacaoResponseDTO() {
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));

        Optional<LocalizacaoResponseDTO> resultado = localizacaoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        LocalizacaoResponseDTO dto = resultado.get();
        assertEquals(localizacao.getId(), dto.getId());
        assertEquals(localizacao.getNome(), dto.getNome());

        verify(localizacaoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar uma localização por ID e retornar vazio quando não encontrada")
    void buscarPorId_quandoLocalizacaoNaoExiste_deveRetornarOptionalVazio() {
        when(localizacaoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<LocalizacaoResponseDTO> resultado = localizacaoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(localizacaoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' uma localização (soft delete) com sucesso quando ela existe")
    void deletar_quandoLocalizacaoExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long localizacaoId = 1L;
        when(localizacaoRepository.findById(localizacaoId)).thenReturn(Optional.of(localizacao));

        assertDoesNotThrow(() -> localizacaoService.deletar(localizacaoId));

        verify(localizacaoRepository, times(1)).delete(localizacao);
    }
}
