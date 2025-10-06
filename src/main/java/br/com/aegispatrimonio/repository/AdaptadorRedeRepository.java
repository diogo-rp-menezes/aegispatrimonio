package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.AdaptadorRede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdaptadorRedeRepository extends JpaRepository<AdaptadorRede, Long> {
    List<AdaptadorRede> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    void deleteByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);
}
