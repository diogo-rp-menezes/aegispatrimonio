package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Memoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoriaRepository extends JpaRepository<Memoria, Long> {
    List<Memoria> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    @Modifying
    @Query("DELETE FROM Memoria m WHERE m.ativoDetalheHardware.id = :id")
    void deleteByAtivoDetalheHardwareId(@Param("id") Long ativoDetalheHardwareId);
}
