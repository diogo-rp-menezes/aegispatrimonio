package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    List<Departamento> findByFilialId(Long filialId);

    boolean existsByFilialId(Long filialId);

    Optional<Departamento> findByNomeAndFilialId(String nome, Long filialId);
}
