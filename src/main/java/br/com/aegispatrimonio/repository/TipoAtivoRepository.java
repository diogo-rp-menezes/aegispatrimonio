package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAtivoRepository extends JpaRepository<TipoAtivo, Long> {
    
    Optional<TipoAtivo> findByNome(String nome);
    
    boolean existsByNome(String nome);
    
    // NOVOS MÉTODOS PAGINADOS
    Page<TipoAtivo> findAll(Pageable pageable);
    
    Page<TipoAtivo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    @Query("SELECT t FROM TipoAtivo t ORDER BY t.nome")
    Page<TipoAtivo> findAllOrderByNome(Pageable pageable);
}