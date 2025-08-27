package br.com.aegispatrimonio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.aegispatrimonio.model.Filial;

@Repository
public interface FilialRepository extends JpaRepository<Filial, Long> {
    
    Optional<Filial> findByCodigo(String codigo);
    
    Optional<Filial> findByNome(String nome);
    
    @Query("SELECT f FROM Filial f WHERE f.nome LIKE %:nome%")
    List<Filial> findByNomeContaining(@Param("nome") String nome);
    
    boolean existsByCodigo(String codigo);
}