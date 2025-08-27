package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    
    Optional<Pessoa> findByEmail(String email);
    
    List<Pessoa> findByDepartamentoId(Long departamentoId);
    
    @Query("SELECT p FROM Pessoa p WHERE p.nome LIKE %:nome%")
    List<Pessoa> findByNomeContaining(@Param("nome") String nome);
    
    boolean existsByEmail(String email);
}