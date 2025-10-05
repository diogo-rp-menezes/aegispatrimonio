package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
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
class DepartamentoServiceTest {

    @InjectMocks
    private DepartamentoService departamentoService;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private FilialRepository filialRepository;

    private Departamento departamento;
    private Filial filial;

    @BeforeEach
    void setUp() {
        filial = new Filial();
        filial.setId(1L);
        filial.setNome("Filial Teste");

        departamento = new Departamento();
        departamento.setId(1L);
        departamento.setNome("TI");
        departamento.setFilial(filial);
    }

    @Test
    @DisplayName("Deve criar um novo departamento com sucesso quando os dados são válidos")
    void criar_quandoDadosValidos_deveRetornarDepartamentoResponseDTO() {
        DepartamentoRequestDTO request = new DepartamentoRequestDTO();
        request.setNome("Recursos Humanos");
        request.setCentroCusto("RH-123");
        request.setFilialId(1L);

        when(filialRepository.existsById(1L)).thenReturn(true);
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));

        when(departamentoRepository.save(any(Departamento.class))).thenAnswer(invocation -> {
            Departamento deptoSalvo = invocation.getArgument(0);
            deptoSalvo.setId(2L);
            return deptoSalvo;
        });

        DepartamentoResponseDTO response = departamentoService.criar(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Recursos Humanos", response.getNome());
        verify(filialRepository, times(1)).existsById(1L);
        verify(departamentoRepository, times(1)).save(any(Departamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar departamento com Filial inexistente")
    void criar_quandoFilialNaoExiste_deveLancarExcecao() {
        DepartamentoRequestDTO request = new DepartamentoRequestDTO();
        request.setFilialId(99L);

        when(filialRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departamentoService.criar(request);
        });

        assertTrue(exception.getMessage().contains("Filial não encontrada"));
        verify(departamentoRepository, never()).save(any(Departamento.class));
    }

    @Test
    @DisplayName("Deve atualizar um departamento com sucesso quando os dados são válidos")
    void atualizar_quandoDadosValidos_deveRetornarDepartamentoResponseDTO() {
        // --- Arrange (Organizar) ---
        Long departamentoId = 1L;
        DepartamentoRequestDTO request = new DepartamentoRequestDTO();
        request.setNome("TI Atualizado");
        request.setCentroCusto("TI-456");
        request.setFilialId(1L);

        when(departamentoRepository.findById(departamentoId)).thenReturn(Optional.of(departamento));
        when(filialRepository.findById(1L)).thenReturn(Optional.of(filial));
        when(departamentoRepository.save(any(Departamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act (Agir) ---
        DepartamentoResponseDTO response = departamentoService.atualizar(departamentoId, request);

        // --- Assert (Verificar) ---
        assertNotNull(response);
        assertEquals(departamentoId, response.getId());
        assertEquals("TI Atualizado", response.getNome());
        assertEquals("TI-456", response.getCentroCusto());

        verify(departamentoRepository, times(1)).findById(departamentoId);
        verify(departamentoRepository, times(1)).save(any(Departamento.class));
    }

    @Test
    @DisplayName("Deve buscar um departamento por ID e retornar DTO quando encontrado")
    void buscarPorId_quandoDepartamentoExiste_deveRetornarOptionalDeDepartamentoResponseDTO() {
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));

        Optional<DepartamentoResponseDTO> resultado = departamentoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        DepartamentoResponseDTO dto = resultado.get();
        assertEquals(departamento.getId(), dto.getId());
        assertEquals(departamento.getNome(), dto.getNome());

        verify(departamentoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar um departamento por ID e retornar vazio quando não encontrado")
    void buscarPorId_quandoDepartamentoNaoExiste_deveRetornarOptionalVazio() {
        when(departamentoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DepartamentoResponseDTO> resultado = departamentoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
        verify(departamentoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve 'deletar' um departamento (soft delete) com sucesso quando o departamento existe")
    void deletar_quandoDepartamentoExiste_deveInvocarSoftDeleteDoRepositorio() {
        Long departamentoId = 1L;
        when(departamentoRepository.findById(departamentoId)).thenReturn(Optional.of(departamento));

        assertDoesNotThrow(() -> departamentoService.deletar(departamentoId));

        verify(departamentoRepository, times(1)).delete(departamento);
    }
}
