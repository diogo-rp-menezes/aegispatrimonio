package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalizacaoMapperTest {

    private LocalizacaoMapper localizacaoMapper;

    @BeforeEach
    void setUp() {
        localizacaoMapper = new LocalizacaoMapper();
    }

    @Test
    @DisplayName("Deve mapear Localizacao completa para LocalizacaoDTO")
    void toDTO_deveMapearLocalizacaoCompletaParaDTO() {
        // Arrange
        Filial filial = new Filial();
        filial.setNome("Edifício Central");

        Localizacao pai = new Localizacao();
        pai.setNome("10º Andar");

        Localizacao localizacao = new Localizacao();
        localizacao.setId(1L);
        localizacao.setNome("Sala de Reuniões 101");
        localizacao.setDescricao("Sala de reuniões principal do 10º andar");
        localizacao.setStatus(Status.ATIVO);
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(pai);

        // Act
        LocalizacaoDTO dto = localizacaoMapper.toDTO(localizacao);

        // Assert
        assertNotNull(dto);
        assertEquals(localizacao.getId(), dto.id());
        assertEquals(localizacao.getNome(), dto.nome());
        assertEquals(localizacao.getDescricao(), dto.descricao());
        assertEquals(filial.getNome(), dto.filial());
        assertEquals(pai.getNome(), dto.localizacaoPai());
        assertEquals(localizacao.getStatus(), dto.status());
    }

    @Test
    @DisplayName("toDTO deve lidar com localizacaoPai nula")
    void toDTO_deveLidarComLocalizacaoPaiNula() {
        // Arrange
        Filial filial = new Filial();
        filial.setNome("Edifício Anexo");

        Localizacao localizacao = new Localizacao();
        localizacao.setId(2L);
        localizacao.setNome("Térreo");
        localizacao.setFilial(filial);
        localizacao.setLocalizacaoPai(null);
        localizacao.setStatus(Status.ATIVO);

        // Act
        LocalizacaoDTO dto = localizacaoMapper.toDTO(localizacao);

        // Assert
        assertNotNull(dto);
        assertEquals("Térreo", dto.nome());
        assertNull(dto.localizacaoPai());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Localizacao for nula")
    void toDTO_deveRetornarNullParaLocalizacaoNula() {
        // Act
        LocalizacaoDTO dto = localizacaoMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear LocalizacaoCreateDTO para Localizacao")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        LocalizacaoCreateDTO createDTO = new LocalizacaoCreateDTO("Almoxarifado", "Estoque de materiais", 1L, null);

        // Act
        Localizacao localizacao = localizacaoMapper.toEntity(createDTO);

        // Assert
        assertNotNull(localizacao);
        assertEquals(createDTO.nome(), localizacao.getNome());
        assertEquals(createDTO.descricao(), localizacao.getDescricao());
        assertNull(localizacao.getId());
        assertNull(localizacao.getFilial());
        assertNull(localizacao.getLocalizacaoPai());
    }

    @Test
    @DisplayName("toEntity deve retornar null quando LocalizacaoCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        Localizacao localizacao = localizacaoMapper.toEntity(null);

        // Assert
        assertNull(localizacao);
    }
}
