package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.mapper.FilialMapper;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.TipoFilial;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
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

    @Mock
    private FilialRepository filialRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private PessoaRepository pessoaRepository;
    @Mock
    private FilialMapper filialMapper;

    @InjectMocks
    private FilialService filialService;

    private Filial filial;
    private FilialCreateDTO createDTO;
    private FilialUpdateDTO updateDTO;
    private FilialDTO filialDTO;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);
        filial.setNome("Matriz");
        filial.setCnpj("00.000.000/0001-00");
        filial.setCodigo("MTRZ");
        filial.setTipo(TipoFilial.MATRIZ);

        createDTO = new FilialCreateDTO("Filial Nova", "FLNV", TipoFilial.FILIAL, "11.111.111/0001-11", "Endereço Nova");
        updateDTO = new FilialUpdateDTO("Filial Editada", "FLED", TipoFilial.FILIAL, "22.222.222/0001-22", "Endereço Editada", Status.INATIVO);
        filialDTO = new FilialDTO(1L, "Filial DTO", "FLDTO", TipoFilial.FILIAL, "33.333.333/0001-33", "Endereço DTO", Status.ATIVO);
    }

    @Test
    @DisplayName("Deve criar uma filial com sucesso")
    void criar_quandoValido_deveRetornarDTO() {
        when(filialRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(filialRepository.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(filialMapper.toEntity(any(FilialCreateDTO.class))).thenReturn(filial);
        when(filialRepository.save(any(Filial.class))).thenReturn(filial);
        when(filialMapper.toDTO(any(Filial.class))).thenReturn(filialDTO);

        FilialDTO result = filialService.criar(createDTO);

        assertNotNull(result);
        verify(filialRepository).save(filial);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar filial com CNPJ duplicado")
    void criar_quandoCnpjDuplicado_deveLancarExcecao() {
        // CORREÇÃO: Retornar a filial com ID para evitar NullPointerException na validação.
        when(filialRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.of(filial));

        assertThrows(IllegalArgumentException.class, () -> filialService.criar(createDTO));
        verify(filialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar uma segunda Matriz")
    void criar_quandoTentaCriarSegundaMatriz_deveLancarExcecao() {
        FilialCreateDTO segundaMatrizDTO = new FilialCreateDTO("Outra Matriz", "OMTZ", TipoFilial.MATRIZ, "33.333.333/0001-33", "Endereço Matriz");
        // CORREÇÃO: Retornar a filial com ID para evitar NullPointerException na validação.
        when(filialRepository.findByTipo(TipoFilial.MATRIZ)).thenReturn(Optional.of(filial));

        assertThrows(IllegalArgumentException.class, () -> filialService.criar(segundaMatrizDTO));
    }

    @Test
    @DisplayName("Deve atualizar uma filial com sucesso")
    void atualizar_quandoValido_deveRetornarDTO() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(filialRepository.save(any(Filial.class))).thenReturn(filial);
        when(filialMapper.toDTO(any(Filial.class))).thenReturn(filialDTO);

        FilialDTO result = filialService.atualizar(1L, updateDTO);

        assertNotNull(result);
        verify(filialRepository).save(filial);
        assertEquals("Filial Editada", filial.getNome());
        assertEquals(Status.INATIVO, filial.getStatus());
    }

    @Test
    @DisplayName("Deve deletar filial que não possui dependências")
    void deletar_quandoSemDependencias_deveExecutarDelete() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(1L)).thenReturn(false);
        when(pessoaRepository.existsByFilialId(1L)).thenReturn(false);

        filialService.deletar(1L);

        verify(filialRepository).delete(filial);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar filial com departamentos associados")
    void deletar_quandoExistemDepartamentos_deveLancarExcecao() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> filialService.deletar(1L));
        verify(filialRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar filial com pessoas associadas")
    void deletar_quandoExistemPessoas_deveLancarExcecao() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(1L)).thenReturn(false);
        when(pessoaRepository.existsByFilialId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> filialService.deletar(1L));
        verify(filialRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar filial com ID inexistente")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(filialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> filialService.buscarPorId(99L));
    }
}
