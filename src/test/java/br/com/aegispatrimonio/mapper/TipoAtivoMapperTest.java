package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.model.CategoriaContabil;
import br.com.aegispatrimonio.model.TipoAtivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TipoAtivoMapperTest {

    private TipoAtivoMapper tipoAtivoMapper;

    @BeforeEach
    void setUp() {
        tipoAtivoMapper = new TipoAtivoMapper();
    }

    @Test
    @DisplayName("Deve mapear TipoAtivo para TipoAtivoDTO corretamente")
    void toDTO_deveMapearTipoAtivoParaDTO() {
        // Arrange
        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Veículo");
        tipoAtivo.setCategoriaContabil(CategoriaContabil.IMOBILIZADO);

        // Act
        TipoAtivoDTO dto = tipoAtivoMapper.toDTO(tipoAtivo);

        // Assert
        assertNotNull(dto);
        assertEquals(tipoAtivo.getId(), dto.id());
        assertEquals(tipoAtivo.getNome(), dto.nome());
        assertEquals(tipoAtivo.getCategoriaContabil(), dto.categoriaContabil());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando TipoAtivo for nulo")
    void toDTO_deveRetornarNullParaTipoAtivoNulo() {
        // Act
        TipoAtivoDTO dto = tipoAtivoMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear TipoAtivoCreateDTO para TipoAtivo corretamente")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        TipoAtivoCreateDTO createDTO = new TipoAtivoCreateDTO("Móvel", CategoriaContabil.IMOBILIZADO);

        // Act
        TipoAtivo tipoAtivo = tipoAtivoMapper.toEntity(createDTO);

        // Assert
        assertNotNull(tipoAtivo);
        assertEquals(createDTO.nome(), tipoAtivo.getNome());
        assertEquals(createDTO.categoriaContabil(), tipoAtivo.getCategoriaContabil());
        assertNull(tipoAtivo.getId()); // ID não deve ser mapeado
    }

    @Test
    @DisplayName("toEntity deve retornar null quando TipoAtivoCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        TipoAtivo tipoAtivo = tipoAtivoMapper.toEntity(null);

        // Assert
        assertNull(tipoAtivo);
    }
}
