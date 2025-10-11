package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    // CORREÇÃO: Adicionado método para buscar departamentos em múltiplas filiais.
    List<Departamento> findByFilialIdIn(Set<Long> filialIds);

    boolean existsByFilialId(Long filialId);

    Optional<Departamento> findByNomeAndFilialId(String nome, Long filialId);
}
