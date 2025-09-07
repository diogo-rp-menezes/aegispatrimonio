package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long>, JpaSpecificationExecutor<Ativo> {
    
    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);
    
    List<Ativo> findByTipoAtivoId(Long tipoAtivoId);
    
    List<Ativo> findByLocalizacaoId(Long localizacaoId);
    
    List<Ativo> findByStatus(StatusAtivo status);
    
    @Query("SELECT a FROM Ativo a WHERE a.valorAquisicao BETWEEN :valorMin AND :valorMax")
    List<Ativo> findByValorAquisicaoBetween(@Param("valorMin") BigDecimal valorMin, 
                                          @Param("valorMax") BigDecimal valorMax);
    
    boolean existsByNumeroPatrimonio(String numeroPatrimonio);
    
    // NOVO: Método para paginação
    Page<Ativo> findAll(Pageable pageable);
    
    // NOVO: Métodos paginados para as consultas existentes
    Page<Ativo> findByTipoAtivoId(Long tipoAtivoId, Pageable pageable);
    Page<Ativo> findByLocalizacaoId(Long localizacaoId, Pageable pageable);
    Page<Ativo> findByStatus(StatusAtivo status, Pageable pageable);
    Page<Ativo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}