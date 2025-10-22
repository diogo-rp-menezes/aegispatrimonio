package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.StatusFornecedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FornecedorMapperTest {

    private FornecedorMapper fornecedorMapper;

    @BeforeEach
    void setUp() {
        fornecedorMapper = new FornecedorMapper();
    }

    @Test
    @DisplayName("Deve mapear Fornecedor para FornecedorDTO corretamente")
    void toDTO_deveMapearFornecedorParaDTO() {
        // Arrange
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNome("Dell Computadores");
        fornecedor.setCnpj("12.345.678/0001-00");
        fornecedor.setEndereco("Av. Industrial, 100");
        fornecedor.setNomeContatoPrincipal("Carlos Andrade");
        fornecedor.setEmailPrincipal("carlos.andrade@dell.com");
        fornecedor.setTelefonePrincipal("11987654321");
        fornecedor.setObservacoes("Contato para compras de hardware");
        fornecedor.setStatus(StatusFornecedor.ATIVO);

        // Act
        FornecedorDTO dto = fornecedorMapper.toDTO(fornecedor);

        // Assert
        assertNotNull(dto);
        assertEquals(fornecedor.getId(), dto.id());
        assertEquals(fornecedor.getNome(), dto.nome());
        assertEquals(fornecedor.getCnpj(), dto.cnpj());
        assertEquals(fornecedor.getEndereco(), dto.endereco());
        assertEquals(fornecedor.getNomeContatoPrincipal(), dto.nomeContatoPrincipal());
        assertEquals(fornecedor.getEmailPrincipal(), dto.emailPrincipal());
        assertEquals(fornecedor.getTelefonePrincipal(), dto.telefonePrincipal());
        assertEquals(fornecedor.getObservacoes(), dto.observacoes());
        assertEquals(fornecedor.getStatus(), dto.status());
    }

    @Test
    @DisplayName("toDTO deve retornar null quando Fornecedor for nulo")
    void toDTO_deveRetornarNullParaFornecedorNulo() {
        // Act
        FornecedorDTO dto = fornecedorMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear FornecedorCreateDTO para Fornecedor corretamente")
    void toEntity_deveMapearDTOparaEntidade() {
        // Arrange
        FornecedorCreateDTO createDTO = new FornecedorCreateDTO(
                "HP Inc.",
                "98.765.432/0001-11",
                "Rua da Tecnologia, 200",
                "Ana Pereira",
                "ana.pereira@hp.com",
                "21912345678",
                "Contato para impressoras"
        );

        // Act
        Fornecedor fornecedor = fornecedorMapper.toEntity(createDTO);

        // Assert
        assertNotNull(fornecedor);
        assertEquals(createDTO.nome(), fornecedor.getNome());
        assertEquals(createDTO.cnpj(), fornecedor.getCnpj());
        assertEquals(createDTO.endereco(), fornecedor.getEndereco());
        assertEquals(createDTO.nomeContatoPrincipal(), fornecedor.getNomeContatoPrincipal());
        assertEquals(createDTO.emailPrincipal(), fornecedor.getEmailPrincipal());
        assertEquals(createDTO.telefonePrincipal(), fornecedor.getTelefonePrincipal());
        assertEquals(createDTO.observacoes(), fornecedor.getObservacoes());
        assertNull(fornecedor.getId()); // ID não deve ser mapeado
        assertNull(fornecedor.getStatus()); // Status é gerenciado pelo serviço
    }

    @Test
    @DisplayName("toEntity deve retornar null quando FornecedorCreateDTO for nulo")
    void toEntity_deveRetornarNullParaDTONulo() {
        // Act
        Fornecedor fornecedor = fornecedorMapper.toEntity(null);

        // Assert
        assertNull(fornecedor);
    }
}
