package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.mapper.TipoAtivoMapper;
import br.com.aegispatrimonio.model.CategoriaContabil;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAtivoServiceTest {

    @Mock
    private TipoAtivoRepository tipoAtivoRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private TipoAtivoMapper tipoAtivoMapper;

    @InjectMocks
    private TipoAtivoService tipoAtivoService;

    private TipoAtivo tipoAtivo;
    private TipoAtivoCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Notebook");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);

        createDTO = new TipoAtivoCreateDTO("Desktop", CategoriaContabil.IMOBILIZADO);
    }

    @Test
    @DisplayName("Deve criar um tipo de ativo com sucesso")
    void criar_quandoNomeNaoExiste_deveSalvarTipoAtivo() {
        when(tipoAtivoRepository.findByNome(anyString())).thenReturn(Optional.empty());
        when(tipoAtivoMapper.toEntity(any(TipoAtivoCreateDTO.class))).thenReturn(tipoAtivo);
        when(tipoAtivoRepository.save(any(TipoAtivo.class))).thenReturn(tipoAtivo);

        tipoAtivoService.criar(createDTO);

        verify(tipoAtivoRepository).save(tipoAtivo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tipo de ativo com nome duplicado")
    void criar_quandoNomeJaExiste_deveLancarExcecao() {
        when(tipoAtivoRepository.findByNome("Desktop")).thenReturn(Optional.of(tipoAtivo));

        assertThrows(IllegalArgumentException.class, () -> tipoAtivoService.criar(createDTO));
        verify(tipoAtivoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar um tipo de ativo com sucesso")
    void atualizar_quandoIdExiste_deveAtualizarTipoAtivo() {
        TipoAtivoCreateDTO updateDTO = new TipoAtivoCreateDTO("Notebook Gamer", CategoriaContabil.IMOBILIZADO);
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(tipoAtivoRepository.save(any(TipoAtivo.class))).thenReturn(tipoAtivo);

        tipoAtivoService.atualizar(1L, updateDTO);

        verify(tipoAtivoRepository).save(tipoAtivo);
        assertEquals("Notebook Gamer", tipoAtivo.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar para um nome que já existe em outro tipo")
    void atualizar_quandoNovoNomeJaExiste_deveLancarExcecao() {
        TipoAtivoCreateDTO updateDTO = new TipoAtivoCreateDTO("Desktop", CategoriaContabil.IMOBILIZADO);
        TipoAtivo outroTipo = new TipoAtivo();
        outroTipo.setId(2L);
        outroTipo.setNome("Desktop");

        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(tipoAtivoRepository.findByNome("Desktop")).thenReturn(Optional.of(outroTipo));

        assertThrows(IllegalArgumentException.class, () -> tipoAtivoService.atualizar(1L, updateDTO));
        verify(tipoAtivoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar um tipo de ativo que não está em uso")
    void deletar_quandoNaoAssociadoAAtivo_deveDeletar() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(ativoRepository.existsByTipoAtivoId(1L)).thenReturn(false);

        tipoAtivoService.deletar(1L);

        verify(tipoAtivoRepository).delete(tipoAtivo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar tipo de ativo associado a ativos")
    void deletar_quandoAssociadoAAtivo_deveLancarExcecao() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(ativoRepository.existsByTipoAtivoId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> tipoAtivoService.deletar(1L));
        verify(tipoAtivoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar tipo de ativo com ID inexistente")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(tipoAtivoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tipoAtivoService.buscarPorId(99L));
    }
}
