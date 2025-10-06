package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Memoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoriaRepository extends JpaRepository<Memoria, Long> {
    List<Memoria> findByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);

    void deleteByAtivoDetalheHardwareId(Long ativoDetalheHardwareId);
}
