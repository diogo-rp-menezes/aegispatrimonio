package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    
    Optional<Pessoa> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // ✅ APENAS métodos paginados
    Page<Pessoa> findAll(Pageable pageable);
    
    @Query("SELECT p FROM Pessoa p WHERE p.departamento.id = :departamentoId")
    Page<Pessoa> findByDepartamentoId(@Param("departamentoId") Long departamentoId, Pageable pageable);
    
    @Query("SELECT p FROM Pessoa p WHERE p.nome LIKE %:nome%")
    Page<Pessoa> findByNomeContaining(@Param("nome") String nome, Pageable pageable);
    
    @Query("SELECT p FROM Pessoa p ORDER BY p.nome")
    Page<Pessoa> findAllOrderByNome(Pageable pageable);
}