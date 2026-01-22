package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AtivoDetalheHardwareRepository extends JpaRepository<AtivoDetalheHardware, Long> {

    @Modifying
    @Query("update AtivoDetalheHardware d set " +
            "d.computerName = :computerName, " +
            "d.domain = :domain, " +
            "d.osName = :osName, " +
            "d.osVersion = :osVersion, " +
            "d.osArchitecture = :osArchitecture, " +
            "d.motherboardManufacturer = :motherboardManufacturer, " +
            "d.motherboardModel = :motherboardModel, " +
            "d.motherboardSerialNumber = :motherboardSerialNumber, " +
            "d.cpuModel = :cpuModel, " +
            "d.cpuCores = :cpuCores, " +
            "d.cpuThreads = :cpuThreads, " +
            "d.lastUpdated = CURRENT_TIMESTAMP " +
            "where d.id = :id")
    int updateScalars(
            @Param("id") Long id,
            @Param("computerName") String computerName,
            @Param("domain") String domain,
            @Param("osName") String osName,
            @Param("osVersion") String osVersion,
            @Param("osArchitecture") String osArchitecture,
            @Param("motherboardManufacturer") String motherboardManufacturer,
            @Param("motherboardModel") String motherboardModel,
            @Param("motherboardSerialNumber") String motherboardSerialNumber,
            @Param("cpuModel") String cpuModel,
            @Param("cpuCores") Integer cpuCores,
            @Param("cpuThreads") Integer cpuThreads
    );
}
