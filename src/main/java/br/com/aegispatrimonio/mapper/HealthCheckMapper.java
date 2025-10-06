package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.healthcheck.AdaptadorRedeDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiscoDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.dto.healthcheck.MemoriaDTO;
import br.com.aegispatrimonio.model.AdaptadorRede;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Disco;
import br.com.aegispatrimonio.model.Memoria;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckMapper {

    public void updateEntityFromDto(AtivoDetalheHardware entity, HealthCheckDTO dto) {
        entity.setComputerName(dto.computerName());
        entity.setDomain(dto.domain());
        entity.setOsName(dto.osName());
        entity.setOsVersion(dto.osVersion());
        entity.setOsArchitecture(dto.osArchitecture());
        entity.setMotherboardManufacturer(dto.motherboardManufacturer());
        entity.setMotherboardModel(dto.motherboardModel());
        entity.setMotherboardSerialNumber(dto.motherboardSerialNumber());
        entity.setCpuModel(dto.cpuModel());
        entity.setCpuCores(dto.cpuCores());
        entity.setCpuThreads(dto.cpuThreads());
    }

    public Disco toEntity(DiscoDTO dto) {
        if (dto == null) return null;
        Disco entity = new Disco();
        entity.setModel(dto.model());
        entity.setSerial(dto.serial());
        entity.setType(dto.type());
        entity.setTotalGb(dto.totalGb());
        entity.setFreeGb(dto.freeGb());
        entity.setFreePercent(dto.freePercent());
        return entity;
    }

    public Memoria toEntity(MemoriaDTO dto) {
        if (dto == null) return null;
        Memoria entity = new Memoria();
        entity.setManufacturer(dto.manufacturer());
        entity.setSerialNumber(dto.serialNumber());
        entity.setPartNumber(dto.partNumber());
        entity.setSizeGb(dto.sizeGb());
        return entity;
    }

    public AdaptadorRede toEntity(AdaptadorRedeDTO dto) {
        if (dto == null) return null;
        AdaptadorRede entity = new AdaptadorRede();
        entity.setDescription(dto.description());
        entity.setMacAddress(dto.macAddress());
        entity.setIpAddresses(dto.ipAddresses());
        return entity;
    }
}
