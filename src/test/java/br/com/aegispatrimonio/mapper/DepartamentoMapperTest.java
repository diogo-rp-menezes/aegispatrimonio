package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartamentoMapperTest {

    private DepartamentoMapper departamentoMapper;

    @BeforeEach
    void setUp() {
        departamentoMapper = new DepartamentoMapper();
    }

    @Test
    @DisplayName("Deve mapear Departamento para DepartamentoDTO corretamente")
    void toDTO_deveMapearDepartamentoParaDTO() {
        // Arrange
        Filial filial = new Filial();
        filial.setId(1L);
        filial.setNome("Sede SP");

        Departamento departamento = new Departamento();
        departamento.setId(1L);
        departamento.setNome("Recursos Humanos");
        departamento.setFilial(filial);

        // Act
        DepartamentoDTO dto = departamentoMapper.toDTO(departamento);

        // Assert
        assertNotNull(dto);
        assertEquals(departamento.getId(), dto.id());
        assertEquals(departamento.getNome(), dto.nome());
        assertEquals(filial.getNome(), dto.filial());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Departamento for nulo")
    void toDTO_deveRetornarNullParaDepartamentoNulo() {
        // Act
        DepartamentoDTO dto = departamentoMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear DepartamentoCreateDTO para Departamento corretamente")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        DepartamentoCreateDTO createDTO = new DepartamentoCreateDTO("Financeiro", 1L);

        // Act
        Departamento departamento = departamentoMapper.toEntity(createDTO);

        // Assert
        assertNotNull(departamento);
        assertEquals(createDTO.nome(), departamento.getNome());
        assertNull(departamento.getId());
        assertNull(departamento.getFilial()); // Confirma que a entidade relacionada não é mapeada aqui
    }

    @Test
    @DisplayName("toEntity deve retornar null quando DepartamentoCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        Departamento departamento = departamentoMapper.toEntity(null);

        // Assert
        assertNull(departamento);
    }
}
