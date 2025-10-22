package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.mapper.FornecedorMapper;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.StatusFornecedor;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private FornecedorMapper fornecedorMapper;

    @Mock
    private AtivoRepository ativoRepository;

    @InjectMocks
    private FornecedorService fornecedorService;

    private Fornecedor fornecedor;
    private FornecedorDTO fornecedorDTO;
    private FornecedorCreateDTO fornecedorCreateDTO;
    private FornecedorUpdateDTO fornecedorUpdateDTO;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Fornecedor Teste");
        fornecedor.setCnpj("11.111.111/0001-11");
        fornecedor.setStatus(StatusFornecedor.ATIVO);

        fornecedorDTO = new FornecedorDTO(1L, "Fornecedor Teste", "11.111.111/0001-11", "Endereco", "Contato", "contato@teste.com", "123456789", "Obs", StatusFornecedor.ATIVO);
        fornecedorCreateDTO = new FornecedorCreateDTO("Fornecedor Novo", "22.222.222/0001-22", "Endereco Novo", "Contato Novo", "contato@novo.com", "987654321", "Obs Nova");
        fornecedorUpdateDTO = new FornecedorUpdateDTO("Fornecedor Update", "33.333.333/0001-33", "Endereco Update", "Contato Update", "contato@update.com", "112233445", "Obs Update", StatusFornecedor.INATIVO);
    }

    @Test
    @DisplayName("ListarTodos: Deve retornar uma lista de todos os fornecedores")
    void listarTodos_deveRetornarListaDeFornecedoresDTO() {
        when(fornecedorRepository.findAll()).thenReturn(Collections.singletonList(fornecedor));
        when(fornecedorMapper.toDTO(any(Fornecedor.class))).thenReturn(fornecedorDTO);

        List<FornecedorDTO> result = fornecedorService.listarTodos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(fornecedorDTO, result.get(0));
        verify(fornecedorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("BuscarPorId: Deve retornar o fornecedor quando o ID existir")
    void buscarPorId_quandoEncontrado_deveRetornarFornecedorDTO() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorMapper.toDTO(fornecedor)).thenReturn(fornecedorDTO);

        FornecedorDTO result = fornecedorService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(fornecedorDTO, result);
        verify(fornecedorRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("BuscarPorId: Deve lançar EntityNotFoundException quando o ID não existir")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> fornecedorService.buscarPorId(99L));
        assertEquals("Fornecedor não encontrado com ID: 99", exception.getMessage());
        verify(fornecedorRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Criar: Deve criar um novo fornecedor com sucesso")
    void criar_comDadosValidos_deveRetornarFornecedorDTO() {
        when(fornecedorRepository.findByCnpj(fornecedorCreateDTO.cnpj())).thenReturn(Optional.empty());
        when(fornecedorMapper.toEntity(fornecedorCreateDTO)).thenReturn(fornecedor);
        when(fornecedorRepository.save(fornecedor)).thenReturn(fornecedor);
        when(fornecedorMapper.toDTO(fornecedor)).thenReturn(fornecedorDTO);

        FornecedorDTO result = fornecedorService.criar(fornecedorCreateDTO);

        assertNotNull(result);
        assertEquals(fornecedorDTO, result);
        verify(fornecedorRepository, times(1)).findByCnpj(fornecedorCreateDTO.cnpj());
        verify(fornecedorRepository, times(1)).save(fornecedor);
    }

    @Test
    @DisplayName("Criar: Deve lançar IllegalArgumentException ao tentar criar com CNPJ duplicado")
    void criar_comCnpjDuplicado_deveLancarExcecao() {
        // CORREÇÃO: O fornecedor existente mockado DEVE ter um ID para evitar NullPointerException.
        Fornecedor fornecedorExistente = new Fornecedor();
        fornecedorExistente.setId(2L);
        when(fornecedorRepository.findByCnpj(fornecedorCreateDTO.cnpj())).thenReturn(Optional.of(fornecedorExistente));

        var exception = assertThrows(IllegalArgumentException.class, () -> fornecedorService.criar(fornecedorCreateDTO));
        assertEquals("Já existe um fornecedor cadastrado com o CNPJ informado.", exception.getMessage());
        verify(fornecedorRepository, never()).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Atualizar: Deve atualizar um fornecedor existente com sucesso")
    void atualizar_quandoEncontrado_deveRetornarFornecedorDTO() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.findByCnpj(fornecedorUpdateDTO.cnpj())).thenReturn(Optional.empty());
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);
        when(fornecedorMapper.toDTO(any(Fornecedor.class))).thenReturn(fornecedorDTO);

        FornecedorDTO result = fornecedorService.atualizar(1L, fornecedorUpdateDTO);

        assertNotNull(result);
        assertEquals(fornecedorDTO, result);
        verify(fornecedorRepository, times(1)).findById(1L);
        verify(fornecedorRepository, times(1)).save(fornecedor);
        assertEquals(fornecedorUpdateDTO.nome(), fornecedor.getNome());
        assertEquals(fornecedorUpdateDTO.cnpj(), fornecedor.getCnpj());
        assertEquals(fornecedorUpdateDTO.status(), fornecedor.getStatus());
    }

    @Test
    @DisplayName("Atualizar: Deve lançar EntityNotFoundException quando o ID não existir")
    void atualizar_quandoNaoEncontrado_deveLancarExcecao() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fornecedorService.atualizar(99L, fornecedorUpdateDTO));
        verify(fornecedorRepository, never()).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Atualizar: Deve lançar IllegalArgumentException ao tentar atualizar com CNPJ duplicado")
    void atualizar_comCnpjDuplicado_deveLancarExcecao() {
        Fornecedor outroFornecedor = new Fornecedor();
        outroFornecedor.setId(2L);
        outroFornecedor.setCnpj(fornecedorUpdateDTO.cnpj());

        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.findByCnpj(fornecedorUpdateDTO.cnpj())).thenReturn(Optional.of(outroFornecedor));

        var exception = assertThrows(IllegalArgumentException.class, () -> fornecedorService.atualizar(1L, fornecedorUpdateDTO));
        assertEquals("Já existe um fornecedor cadastrado com o CNPJ informado.", exception.getMessage());
        verify(fornecedorRepository, never()).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deletar: Deve realizar soft delete do fornecedor se não houver ativos associados")
    void deletar_quandoSemAtivosAssociados_deveRealizarSoftDeleteComSucesso() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(ativoRepository.existsByFornecedorId(1L)).thenReturn(false);
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor); // Mock save para soft delete

        assertDoesNotThrow(() -> fornecedorService.deletar(1L));

        verify(fornecedorRepository, times(1)).findById(1L);
        verify(ativoRepository, times(1)).existsByFornecedorId(1L);

        ArgumentCaptor<Fornecedor> fornecedorCaptor = ArgumentCaptor.forClass(Fornecedor.class);
        verify(fornecedorRepository, times(1)).save(fornecedorCaptor.capture());
        assertEquals(StatusFornecedor.INATIVO, fornecedorCaptor.getValue().getStatus());

        verify(fornecedorRepository, never()).delete(any(Fornecedor.class)); // Verifica que delete NÃO foi chamado
    }

    @Test
    @DisplayName("Deletar: Deve lançar IllegalStateException se houver ativos associados")
    void deletar_quandoComAtivosAssociados_deveLancarExcecao() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(ativoRepository.existsByFornecedorId(1L)).thenReturn(true);

        var exception = assertThrows(IllegalStateException.class, () -> fornecedorService.deletar(1L));
        assertEquals("Não é possível deletar o fornecedor, pois existem ativos associados a ele.", exception.getMessage());
        verify(fornecedorRepository, never()).save(any(Fornecedor.class)); // Verifica que save NÃO foi chamado
    }

    @Test
    @DisplayName("Deletar: Deve lançar EntityNotFoundException quando o ID não existir")
    void deletar_quandoNaoEncontrado_deveLancarExcecao() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fornecedorService.deletar(99L));
        verify(ativoRepository, never()).existsByFornecedorId(anyLong());
        verify(fornecedorRepository, never()).save(any(Fornecedor.class)); // Verifica que save NÃO foi chamado
    }
}