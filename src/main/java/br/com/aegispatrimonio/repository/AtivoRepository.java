package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo; // ✅ Import do enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    
    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);
    
    List<Ativo> findByTipoAtivoId(Long tipoAtivoId);
    
    List<Ativo> findByLocalizacaoId(Long localizacaoId);
    
    List<Ativo> findByStatus(StatusAtivo status); // ✅ Agora funciona!
    
    @Query("SELECT a FROM Ativo a WHERE a.valorAquisicao BETWEEN :valorMin AND :valorMax")
    List<Ativo> findByValorAquisicaoBetween(@Param("valorMin") BigDecimal valorMin, 
                                          @Param("valorMax") BigDecimal valorMax);
    
    boolean existsByNumeroPatrimonio(String numeroPatrimonio);
}