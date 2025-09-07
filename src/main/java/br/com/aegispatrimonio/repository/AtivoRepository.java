package br.com.aegispatrimonio.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long>, JpaSpecificationExecutor<Ativo> {
    
    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);
    
    // MÉTODO ADICIONADO: Buscar por status (lista)
    List<Ativo> findByStatus(StatusAtivo status);
    
    // MÉTODOS PAGINADOS (substituem as versões List)
    Page<Ativo> findByTipoAtivoId(Long tipoAtivoId, Pageable pageable);
    
    Page<Ativo> findByLocalizacaoId(Long localizacaoId, Pageable pageable);
    
    Page<Ativo> findByStatus(StatusAtivo status, Pageable pageable);
    
    @Query("SELECT a FROM Ativo a WHERE a.valorAquisicao BETWEEN :valorMin AND :valorMax")
    Page<Ativo> findByValorAquisicaoBetween(@Param("valorMin") BigDecimal valorMin, 
                                          @Param("valorMax") BigDecimal valorMax,
                                          Pageable pageable);
    
    Page<Ativo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    boolean existsByNumeroPatrimonio(String numeroPatrimonio);
}