package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAtivoServiceTest {

    @Mock
    private TipoAtivoRepository tipoAtivoRepository;

    @Mock
    private TipoAtivoMapper tipoAtivoMapper;

    @Mock
    private AtivoRepository ativoRepository;

    @InjectMocks
    private TipoAtivoService tipoAtivoService;

    private TipoAtivo tipoAtivo;
    private TipoAtivoDTO tipoAtivoDTO;
    private TipoAtivoCreateDTO tipoAtivoCreateDTO;

    @BeforeEach
    void setUp() {
        tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Hardware");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);

        tipoAtivoDTO = new TipoAtivoDTO(1L, "Hardware", CategoriaContabil.IMOBILIZADO);
        tipoAtivoCreateDTO = new TipoAtivoCreateDTO("Software", CategoriaContabil.INTANGIVEL);
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar uma lista de todos os tipos de ativo")
    void listarTodos_deveRetornarListaDeTiposAtivoDTO() {
        when(tipoAtivoRepository.findAll()).thenReturn(Collections.singletonList(tipoAtivo));
        when(tipoAtivoMapper.toDTO(any(TipoAtivo.class))).thenReturn(tipoAtivoDTO);

        List<TipoAtivoDTO> result = tipoAtivoService.listarTodos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(tipoAtivoDTO, result.get(0));
        verify(tipoAtivoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar o tipo de ativo quando o ID existir")
    void buscarPorId_quandoEncontrado_deveRetornarTipoAtivoDTO() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(tipoAtivoMapper.toDTO(tipoAtivo)).thenReturn(tipoAtivoDTO);

        TipoAtivoDTO result = tipoAtivoService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(tipoAtivoDTO, result);
        verify(tipoAtivoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("BuscarPorId: Deve lançar EntityNotFoundException quando o ID não existir")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(tipoAtivoRepository.findById(99L)).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> tipoAtivoService.buscarPorId(99L));
        assertEquals("Tipo de Ativo não encontrado com ID: 99", exception.getMessage());
        verify(tipoAtivoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Criar: Deve criar um novo tipo de ativo com sucesso")
    void criar_comDadosValidos_deveRetornarTipoAtivoDTO() {
        when(tipoAtivoRepository.findByNome(tipoAtivoCreateDTO.nome())).thenReturn(Optional.empty());
        when(tipoAtivoMapper.toEntity(tipoAtivoCreateDTO)).thenReturn(tipoAtivo);
        when(tipoAtivoRepository.save(tipoAtivo)).thenReturn(tipoAtivo);
        when(tipoAtivoMapper.toDTO(tipoAtivo)).thenReturn(tipoAtivoDTO);

        TipoAtivoDTO result = tipoAtivoService.criar(tipoAtivoCreateDTO);

        assertNotNull(result);
        assertEquals(tipoAtivoDTO, result);
        verify(tipoAtivoRepository, times(1)).findByNome(tipoAtivoCreateDTO.nome());
        verify(tipoAtivoRepository, times(1)).save(tipoAtivo);
    }

    @Test
    @DisplayName("Criar: Deve lançar IllegalArgumentException ao tentar criar com nome duplicado")
    void criar_comNomeDuplicado_deveLancarExcecao() {
        // CORREÇÃO: O tipo de ativo existente mockado DEVE ter um ID para evitar NullPointerException.
        TipoAtivo tipoAtivoExistente = new TipoAtivo();
        tipoAtivoExistente.setId(2L);
        when(tipoAtivoRepository.findByNome(tipoAtivoCreateDTO.nome())).thenReturn(Optional.of(tipoAtivoExistente));

        var exception = assertThrows(IllegalArgumentException.class, () -> tipoAtivoService.criar(tipoAtivoCreateDTO));
        assertEquals("Já existe um tipo de ativo com este nome.", exception.getMessage());
        verify(tipoAtivoRepository, never()).save(any(TipoAtivo.class));
    }

    @Test
    @DisplayName("Atualizar: Deve atualizar um tipo de ativo existente com sucesso")
    void atualizar_quandoEncontrado_deveRetornarTipoAtivoDTO() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(tipoAtivoRepository.findByNome(tipoAtivoCreateDTO.nome())).thenReturn(Optional.empty());
        when(tipoAtivoRepository.save(any(TipoAtivo.class))).thenReturn(tipoAtivo);
        when(tipoAtivoMapper.toDTO(any(TipoAtivo.class))).thenReturn(tipoAtivoDTO);

        TipoAtivoDTO result = tipoAtivoService.atualizar(1L, tipoAtivoCreateDTO);

        assertNotNull(result);
        assertEquals(tipoAtivoDTO, result);
        verify(tipoAtivoRepository, times(1)).findById(1L);
        verify(tipoAtivoRepository, times(1)).save(tipoAtivo);
        assertEquals(tipoAtivoCreateDTO.nome(), tipoAtivo.getNome());
        assertEquals(tipoAtivoCreateDTO.categoriaContabil(), tipoAtivo.getCategoriaContabil());
    }

    @Test
    @DisplayName("Deletar: Deve deletar o tipo de ativo se não houver ativos associados")
    void deletar_quandoSemAtivosAssociados_deveDeletarComSucesso() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(ativoRepository.existsByTipoAtivoId(1L)).thenReturn(false);
        doNothing().when(tipoAtivoRepository).delete(tipoAtivo);

        assertDoesNotThrow(() -> tipoAtivoService.deletar(1L));

        verify(tipoAtivoRepository, times(1)).findById(1L);
        verify(ativoRepository, times(1)).existsByTipoAtivoId(1L);
        verify(tipoAtivoRepository, times(1)).delete(tipoAtivo);
    }

    @Test
    @DisplayName("Deletar: Deve lançar IllegalStateException se houver ativos associados")
    void deletar_quandoComAtivosAssociados_deveLancarExcecao() {
        when(tipoAtivoRepository.findById(1L)).thenReturn(Optional.of(tipoAtivo));
        when(ativoRepository.existsByTipoAtivoId(1L)).thenReturn(true);

        var exception = assertThrows(IllegalStateException.class, () -> tipoAtivoService.deletar(1L));
        assertEquals("Não é possível deletar o tipo de ativo, pois existem ativos associados a ele.", exception.getMessage());
        verify(tipoAtivoRepository, never()).delete(any(TipoAtivo.class));
    }
}
