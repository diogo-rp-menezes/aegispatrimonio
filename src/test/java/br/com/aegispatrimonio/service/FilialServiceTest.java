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
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private FilialMapper filialMapper;

    @InjectMocks
    private FilialService filialService;

    private Filial matriz;
    private Filial filial;

    @BeforeEach
    void setUp() {
        matriz = new Filial();
        matriz.setId(1L);
        matriz.setNome("Matriz");
        matriz.setCnpj("00.000.000/0001-00");
        matriz.setCodigo("MTRZ");
        matriz.setTipo(TipoFilial.MATRIZ);

        filial = new Filial();
        filial.setId(2L);
        filial.setNome("Filial");
        filial.setCnpj("11.111.111/0001-11");
        filial.setCodigo("FL01");
        filial.setTipo(TipoFilial.FILIAL);
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar lista de DTOs")
    void listarTodos_deveRetornarListaDeDTOs() {
        when(filialRepository.findAll()).thenReturn(List.of(matriz, filial));

        filialService.listarTodos();

        verify(filialRepository).findAll();
        verify(filialMapper, times(2)).toDTO(any(Filial.class));
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar DTO quando ID existe")
    void buscarPorId_quandoIdExiste_deveRetornarDTO() {
        when(filialRepository.findById(1L)).thenReturn(Optional.of(matriz));

        filialService.buscarPorId(1L);

        verify(filialRepository).findById(1L);
        verify(filialMapper).toDTO(matriz);
    }

    @Test
    @DisplayName("BuscarPorId: Deve lançar EntityNotFoundException quando ID não existe")
    void buscarPorId_quandoIdNaoExiste_deveLancarExcecao() {
        when(filialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> filialService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Criar: Deve salvar e retornar DTO quando dados são válidos")
    void criar_quandoValido_deveSalvarEretornarDTO() {
        FilialCreateDTO createDTO = new FilialCreateDTO("Nova Filial", "FL02", TipoFilial.FILIAL, "22.222.222/0001-22", "Endereço");
        when(filialRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(filialRepository.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(filialMapper.toEntity(createDTO)).thenReturn(filial);
        when(filialRepository.save(filial)).thenReturn(filial);

        filialService.criar(createDTO);

        verify(filialRepository).save(filial);
        verify(filialMapper).toDTO(filial);
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção para CNPJ duplicado")
    void criar_quandoCnpjDuplicado_deveLancarExcecao() {
        FilialCreateDTO createDTO = new FilialCreateDTO("Nova Filial", "FL02", TipoFilial.FILIAL, "00.000.000/0001-00", "Endereço");
        when(filialRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.of(matriz));

        assertThrows(IllegalArgumentException.class, () -> filialService.criar(createDTO));
        verify(filialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção para Código duplicado")
    void criar_quandoCodigoDuplicado_deveLancarExcecao() {
        FilialCreateDTO createDTO = new FilialCreateDTO("Nova Filial", "MTRZ", TipoFilial.FILIAL, "22.222.222/0001-22", "Endereço");
        when(filialRepository.findByCodigo(createDTO.codigo())).thenReturn(Optional.of(matriz));

        assertThrows(IllegalArgumentException.class, () -> filialService.criar(createDTO));
        verify(filialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar: Deve lançar exceção ao tentar criar segunda Matriz")
    void criar_quandoTentaCriarSegundaMatriz_deveLancarExcecao() {
        FilialCreateDTO createDTO = new FilialCreateDTO("Nova Matriz", "MTRZ2", TipoFilial.MATRIZ, "22.222.222/0001-22", "Endereço");
        when(filialRepository.findByTipo(TipoFilial.MATRIZ)).thenReturn(Optional.of(matriz));

        assertThrows(IllegalArgumentException.class, () -> filialService.criar(createDTO));
    }

    @Test
    @DisplayName("Atualizar: Deve atualizar com sucesso")
    void atualizar_quandoValido_deveAtualizar() {
        FilialUpdateDTO updateDTO = new FilialUpdateDTO("Filial Atualizada", "FL01-A", TipoFilial.FILIAL, "11.111.111/0001-11", "Novo Endereço", Status.ATIVO);
        when(filialRepository.findById(2L)).thenReturn(Optional.of(filial));
        when(filialRepository.save(any(Filial.class))).thenReturn(filial);

        filialService.atualizar(2L, updateDTO);

        verify(filialRepository).save(filial);
        assertEquals("Filial Atualizada", filial.getNome());
    }

    @Test
    @DisplayName("Deletar: Deve deletar filial sem dependências")
    void deletar_quandoSemDependencias_deveDeletar() {
        when(filialRepository.findById(2L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(2L)).thenReturn(false);
        when(funcionarioRepository.existsByFiliais_Id(2L)).thenReturn(false);

        filialService.deletar(2L);

        verify(filialRepository).delete(filial);
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se houver departamentos")
    void deletar_quandoDepartamentosExistem_deveLancarExcecao() {
        when(filialRepository.findById(2L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> filialService.deletar(2L));
    }

    @Test
    @DisplayName("Deletar: Deve lançar exceção se houver funcionários")
    void deletar_quandoFuncionariosExistem_deveLancarExcecao() {
        when(filialRepository.findById(2L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.existsByFilialId(2L)).thenReturn(false);
        when(funcionarioRepository.existsByFiliais_Id(2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> filialService.deletar(2L));
    }
}
