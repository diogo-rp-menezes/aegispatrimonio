package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.TipoFilial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilialMapperTest {

    private FilialMapper filialMapper;

    @BeforeEach
    void setUp() {
        filialMapper = new FilialMapper();
    }

    @Test
    @DisplayName("Deve mapear Filial para FilialDTO corretamente")
    void toDTO_deveMapearFilialParaDTO() {
        // Arrange
        Filial filial = new Filial();
        filial.setId(1L);
        filial.setNome("Matriz RJ");
        filial.setCodigo("MATRIZ-RJ");
        filial.setTipo(TipoFilial.MATRIZ);
        filial.setCnpj("12.345.678/0001-99");
        filial.setEndereco("Rua Principal, 123");
        filial.setStatus(Status.ATIVO);

        // Act
        FilialDTO dto = filialMapper.toDTO(filial);

        // Assert
        assertNotNull(dto);
        assertEquals(filial.getId(), dto.id());
        assertEquals(filial.getNome(), dto.nome());
        assertEquals(filial.getCodigo(), dto.codigo());
        assertEquals(filial.getTipo(), dto.tipo());
        assertEquals(filial.getCnpj(), dto.cnpj());
        assertEquals(filial.getEndereco(), dto.endereco());
        assertEquals(filial.getStatus(), dto.status());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Filial for nula")
    void toDTO_deveRetornarNullParaFilialNula() {
        // Act
        FilialDTO dto = filialMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear FilialCreateDTO para Filial corretamente")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        FilialCreateDTO createDTO = new FilialCreateDTO(
                "Filial SP",
                "FILIAL-SP",
                TipoFilial.FILIAL,
                "98.765.432/0001-11",
                "Avenida Paulista, 1000"
        );

        // Act
        Filial filial = filialMapper.toEntity(createDTO);

        // Assert
        assertNotNull(filial);
        assertEquals(createDTO.nome(), filial.getNome());
        assertEquals(createDTO.codigo(), filial.getCodigo());
        assertEquals(createDTO.tipo(), filial.getTipo());
        assertEquals(createDTO.cnpj(), filial.getCnpj());
        assertEquals(createDTO.endereco(), filial.getEndereco());
        assertNull(filial.getId()); // ID não deve ser mapeado do DTO de criação
        assertNull(filial.getStatus()); // Status é gerenciado pelo serviço, não pelo mapper
    }

    @Test
    @DisplayName("toEntity deve retornar null quando FilialCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        Filial filial = filialMapper.toEntity(null);

        // Assert
        assertNull(filial);
    }
}
