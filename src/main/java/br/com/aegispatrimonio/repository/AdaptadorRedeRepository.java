package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.AdaptadorRede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdaptadorRedeRepository extends JpaRepository<AdaptadorRede, Long> {
    List<AdaptadorRede> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    @Modifying
    @Query("DELETE FROM AdaptadorRede a WHERE a.ativoDetalheHardware.id = :id")
    void deleteByAtivoDetalheHardwareId(@Param("id") Long ativoDetalheHardwareId);
}
