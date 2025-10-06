package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    List<Ativo> findByFilialId(Long filialId);
}
