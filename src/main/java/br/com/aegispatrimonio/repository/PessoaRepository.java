package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    // --- Otimização N+1 ---
    // A anotação @EntityGraph instrui o Spring Data JPA a carregar a entidade
    // relacionada 'departamento' na mesma query inicial, usando um JOIN.

    @Override
    @EntityGraph(attributePaths = {"departamento"})
    Page<Pessoa> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"departamento"})
    Optional<Pessoa> findById(Long id);

    @EntityGraph(attributePaths = {"departamento"})
    Optional<Pessoa> findByEmail(String email);

    @EntityGraph(attributePaths = {"departamento"})
    Page<Pessoa> findByDepartamentoId(Long departamentoId, Pageable pageable);

    @EntityGraph(attributePaths = {"departamento"})
    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Verifica de forma eficiente se uma pessoa com o email especificado já existe.
     * @param email O email a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsByEmail(String email);
}