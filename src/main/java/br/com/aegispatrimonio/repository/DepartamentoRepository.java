package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Departamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    // --- Otimização N+1 ---
    // A anotação @EntityGraph instrui o Spring Data JPA a carregar a entidade
    // relacionada 'filial' na mesma query inicial, usando um JOIN.

    @Override
    @EntityGraph(attributePaths = {"filial"})
    Page<Departamento> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"filial"})
    Optional<Departamento> findById(Long id);

    @EntityGraph(attributePaths = {"filial"})
    Page<Departamento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"filial"})
    Page<Departamento> findByFilialId(Long filialId, Pageable pageable);
}