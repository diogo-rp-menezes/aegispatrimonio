package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.healthcheck.AdaptadorRedeDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiscoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.MemoriaDTO;
import br.com.aegispatrimonio.model.AdaptadorRede;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Disco;
import br.com.aegispatrimonio.model.Memoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckMapperTest {

    private HealthCheckMapper healthCheckMapper;

    @BeforeEach
    void setUp() {
        healthCheckMapper = new HealthCheckMapper();
    }

    @Test
    @DisplayName("Deve atualizar AtivoDetalheHardware a partir de HealthCheckDTO")
    void updateEntityFromDto_deveAtualizarEntidade() {
        // Arrange
        HealthCheckDTO dto = new HealthCheckDTO(
                "TEST-PC", "workgroup", "Windows 11", "10.0.22621", "x64",
                "ASUS", "TUF GAMING B550M-PLUS", "SN12345",
                "AMD Ryzen 5 5600X", 6, 12, null, null, null
        );
        AtivoDetalheHardware entity = new AtivoDetalheHardware();

        // Act
        healthCheckMapper.updateEntityFromDto(entity, dto);

        // Assert
        assertEquals(dto.computerName(), entity.getComputerName());
        assertEquals(dto.domain(), entity.getDomain());
        assertEquals(dto.osName(), entity.getOsName());
        assertEquals(dto.osVersion(), entity.getOsVersion());
        assertEquals(dto.osArchitecture(), entity.getOsArchitecture());
        assertEquals(dto.motherboardManufacturer(), entity.getMotherboardManufacturer());
        assertEquals(dto.motherboardModel(), entity.getMotherboardModel());
        assertEquals(dto.motherboardSerialNumber(), entity.getMotherboardSerialNumber());
        assertEquals(dto.cpuModel(), entity.getCpuModel());
        assertEquals(dto.cpuCores(), entity.getCpuCores());
        assertEquals(dto.cpuThreads(), entity.getCpuThreads());
    }

    @Test
    @DisplayName("Deve mapear DiscoDTO para entidade Disco")
    void toEntity_deveMapearDiscoDTO() {
        // Arrange
        DiscoDTO dto = new DiscoDTO("Samsung SSD 970 EVO", "S123", "SSD", 
                                  new BigDecimal("500.0"), new BigDecimal("250.0"), 50);

        // Act
        Disco entity = healthCheckMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals(dto.model(), entity.getModel());
        assertEquals(dto.serial(), entity.getSerial());
        assertEquals(dto.type(), entity.getType());
        assertEquals(dto.totalGb(), entity.getTotalGb());
        assertEquals(dto.freeGb(), entity.getFreeGb());
        assertEquals(dto.freePercent(), entity.getFreePercent());
    }

    @Test
    @DisplayName("toEntity(DiscoDTO) deve retornar null para DTO nulo")
    void toEntity_disco_deveRetornarNullParaDTONulo() {
        assertNull(healthCheckMapper.toEntity((DiscoDTO) null));
    }

    @Test
    @DisplayName("Deve mapear MemoriaDTO para entidade Memoria")
    void toEntity_deveMapearMemoriaDTO() {
        // Arrange
        MemoriaDTO dto = new MemoriaDTO("Corsair", "SN-MEM-456", "CMK16GX4M2B3200C16", 16);

        // Act
        Memoria entity = healthCheckMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals(dto.manufacturer(), entity.getManufacturer());
        assertEquals(dto.serialNumber(), entity.getSerialNumber());
        assertEquals(dto.partNumber(), entity.getPartNumber());
        assertEquals(dto.sizeGb(), entity.getSizeGb());
    }

    @Test
    @DisplayName("toEntity(MemoriaDTO) deve retornar null para DTO nulo")
    void toEntity_memoria_deveRetornarNullParaDTONulo() {
        assertNull(healthCheckMapper.toEntity((MemoriaDTO) null));
    }

    @Test
    @DisplayName("Deve mapear AdaptadorRedeDTO para entidade AdaptadorRede")
    void toEntity_deveMapearAdaptadorRedeDTO() {
        // Arrange
        AdaptadorRedeDTO dto = new AdaptadorRedeDTO("Intel(R) Ethernet Connection", "AA:BB:CC:DD:EE:FF", "192.168.1.10");

        // Act
        AdaptadorRede entity = healthCheckMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals(dto.description(), entity.getDescription());
        assertEquals(dto.macAddress(), entity.getMacAddress());
        assertEquals(dto.ipAddresses(), entity.getIpAddresses());
    }

    @Test
    @DisplayName("toEntity(AdaptadorRedeDTO) deve retornar null para DTO nulo")
    void toEntity_adaptadorRede_deveRetornarNullParaDTONulo() {
        assertNull(healthCheckMapper.toEntity((AdaptadorRedeDTO) null));
    }
}
