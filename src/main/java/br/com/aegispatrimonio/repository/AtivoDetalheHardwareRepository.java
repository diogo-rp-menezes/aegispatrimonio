package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtivoDetalheHardwareRepository extends JpaRepository<AtivoDetalheHardware, Long> {
}
