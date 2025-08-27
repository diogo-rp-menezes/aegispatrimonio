package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    
    Optional<Fornecedor> findByNome(String nome);
    
    List<Fornecedor> findByEmailContato(String emailContato);
    
    @Query("SELECT f FROM Fornecedor f WHERE f.nome LIKE %:nome%")
    List<Fornecedor> findByNomeContaining(@Param("nome") String nome);
    
    boolean existsByNome(String nome);
}