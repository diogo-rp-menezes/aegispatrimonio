package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.mapper.FornecedorMapper;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
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
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private FornecedorMapper fornecedorMapper;

    @InjectMocks
    private FornecedorService fornecedorService;

    private Fornecedor fornecedor;
    private FornecedorCreateDTO createDTO;
    private FornecedorUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Dell");
        fornecedor.setCnpj("11.111.111/0001-11");

        createDTO = new FornecedorCreateDTO("HP", "22.222.222/0001-22", null, null, null, null, null);
        updateDTO = new FornecedorUpdateDTO("HP Inc.", "22.222.222/0001-22", null, null, null, null, null, Status.INATIVO);
    }

    @Test
    @DisplayName("Deve criar um fornecedor com sucesso")
    void criar_quandoCnpjNaoExiste_deveSalvarFornecedor() {
        when(fornecedorRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(fornecedorMapper.toEntity(any(FornecedorCreateDTO.class))).thenReturn(fornecedor);
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        fornecedorService.criar(createDTO);

        verify(fornecedorRepository).save(fornecedor);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar fornecedor com CNPJ duplicado")
    void criar_quandoCnpjJaExiste_deveLancarExcecao() {
        // CORREÇÃO: Retornar o fornecedor com ID para evitar NullPointerException na validação.
        when(fornecedorRepository.findByCnpj(createDTO.cnpj())).thenReturn(Optional.of(fornecedor));

        assertThrows(IllegalArgumentException.class, () -> fornecedorService.criar(createDTO));
        verify(fornecedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar um fornecedor com sucesso")
    void atualizar_quandoIdExiste_deveAtualizarFornecedor() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        fornecedorService.atualizar(1L, updateDTO);

        verify(fornecedorRepository).save(fornecedor);
        assertEquals("HP Inc.", fornecedor.getNome());
        assertEquals(Status.INATIVO, fornecedor.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar para um CNPJ que já existe em outro fornecedor")
    void atualizar_quandoNovoCnpjJaExiste_deveLancarExcecao() {
        Fornecedor outroFornecedor = new Fornecedor();
        outroFornecedor.setId(2L);
        outroFornecedor.setCnpj(updateDTO.cnpj());

        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.findByCnpj(updateDTO.cnpj())).thenReturn(Optional.of(outroFornecedor));

        assertThrows(IllegalArgumentException.class, () -> fornecedorService.atualizar(1L, updateDTO));
        verify(fornecedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar um fornecedor que não está em uso")
    void deletar_quandoNaoAssociadoAAtivos_deveDeletar() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(ativoRepository.existsByFornecedorId(1L)).thenReturn(false);

        fornecedorService.deletar(1L);

        verify(fornecedorRepository).delete(fornecedor);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar fornecedor associado a ativos")
    void deletar_quandoAssociadoAAtivos_deveLancarExcecao() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(ativoRepository.existsByFornecedorId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fornecedorService.deletar(1L));
        verify(fornecedorRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar fornecedor com ID inexistente")
    void buscarPorId_quandoNaoEncontrado_deveLancarExcecao() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fornecedorService.buscarPorId(99L));
    }
}
