package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AtivoMapperTest {

    private AtivoMapper ativoMapper;

    @BeforeEach
    void setUp() {
        ativoMapper = new AtivoMapper();
    }

    @Test
    @DisplayName("Deve mapear Ativo para AtivoDTO corretamente")
    void toDTO_deveMapearAtivoParaDTO() {
        // Arrange
        Filial filial = new Filial();
        filial.setId(1L);
        filial.setNome("Sede");

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Notebook");

        Localizacao localizacao = new Localizacao();
        localizacao.setId(1L);
        localizacao.setNome("Sala TI");

        Funcionario responsavel = new Funcionario();
        responsavel.setId(1L);
        responsavel.setNome("João da Silva");

        Ativo ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Notebook Dell G15");
        ativo.setNumeroPatrimonio("PAT-001");
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setLocalizacao(localizacao);
        ativo.setFuncionarioResponsavel(responsavel);

        // Act
        AtivoDTO dto = ativoMapper.toDTO(ativo);

        // Assert
        assertNotNull(dto);
        assertEquals(ativo.getId(), dto.id());
        assertEquals(ativo.getNome(), dto.nome());
        assertEquals(ativo.getNumeroPatrimonio(), dto.numeroPatrimonio());
        assertEquals(ativo.getStatus(), dto.status());
        assertEquals(filial.getNome(), dto.filial());
        assertEquals(tipoAtivo.getNome(), dto.tipoAtivo());
        assertEquals(localizacao.getNome(), dto.localizacao());
        assertEquals(responsavel.getId(), dto.funcionarioResponsavelId());
        assertEquals(responsavel.getNome(), dto.funcionarioResponsavelNome());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Ativo for nulo")
    void toDTO_deveRetornarNullParaAtivoNulo() {
        // Act
        AtivoDTO dto = ativoMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO deve lidar com entidades relacionadas nulas")
    void toDTO_deveLidarComRelacionadosNulos() {
        // Arrange
        Filial filial = new Filial();
        filial.setId(1L);
        filial.setNome("Sede");

        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setId(1L);
        tipoAtivo.setNome("Notebook");

        Ativo ativo = new Ativo();
        ativo.setId(1L);
        ativo.setNome("Notebook Dell G15");
        ativo.setNumeroPatrimonio("PAT-001");
        ativo.setStatus(StatusAtivo.ATIVO);
        ativo.setFilial(filial);
        ativo.setTipoAtivo(tipoAtivo);
        ativo.setLocalizacao(null); // Localização nula
        ativo.setFuncionarioResponsavel(null); // Responsável nulo

        // Act
        AtivoDTO dto = ativoMapper.toDTO(ativo);

        // Assert
        assertNotNull(dto);
        assertNull(dto.localizacao());
        assertNull(dto.funcionarioResponsavelId());
        assertNull(dto.funcionarioResponsavelNome());
    }

    @Test
    @DisplayName("Deve mapear AtivoCreateDTO para Ativo corretamente")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        AtivoCreateDTO createDTO = new AtivoCreateDTO(
                1L, "Novo Ativo", 1L, "PAT-NEW", 1L, 
                LocalDate.now(), 1L, BigDecimal.valueOf(1500.00), 1L, 
                "Observações", "Garantia 1 ano", null
        );

        // Act
        Ativo ativo = ativoMapper.toEntity(createDTO);

        // Assert
        assertNotNull(ativo);
        assertEquals(createDTO.nome(), ativo.getNome());
        assertEquals(createDTO.numeroPatrimonio(), ativo.getNumeroPatrimonio());
        assertEquals(createDTO.dataAquisicao(), ativo.getDataAquisicao());
        assertEquals(createDTO.valorAquisicao(), ativo.getValorAquisicao());
        assertEquals(createDTO.observacoes(), ativo.getObservacoes());
        assertEquals(createDTO.informacoesGarantia(), ativo.getInformacoesGarantia());
        
        // Verifica que campos de entidade não são mapeados
        assertNull(ativo.getId());
        assertNull(ativo.getFilial());
        assertNull(ativo.getTipoAtivo());
        assertNull(ativo.getLocalizacao());
    }

    @Test
    @DisplayName("toEntity deve retornar null quando AtivoCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        Ativo ativo = ativoMapper.toEntity(null);

        // Assert
        assertNull(ativo);
    }
}
