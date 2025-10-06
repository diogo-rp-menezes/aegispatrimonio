package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Disco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoRepository extends JpaRepository<Disco, Long> {
    List<Disco> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    void deleteByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);
}
