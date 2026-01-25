package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Disco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoRepository extends JpaRepository<Disco, Long> {
    List<Disco> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    @Modifying
    @Query("DELETE FROM Disco d WHERE d.ativoDetalheHardware.id = :id")
    void deleteByAtivoDetalheHardwareId(@Param("id") Long ativoDetalheHardwareId);
}
