package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    Page<Manutencao> findByAtivoId(Long ativoId, Pageable pageable);
    
    Page<Manutencao> findByStatus(StatusManutencao status, Pageable pageable);
    
    Page<Manutencao> findByTipo(TipoManutencao tipo, Pageable pageable);
    
    Page<Manutencao> findBySolicitanteId(Long solicitanteId, Pageable pageable);
    
    Page<Manutencao> findByFornecedorId(Long fornecedorId, Pageable pageable);
    
    @Query("SELECT m FROM Manutencao m WHERE m.dataSolicitacao BETWEEN :startDate AND :endDate")
    Page<Manutencao> findByPeriodoSolicitacao(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate, 
                                             Pageable pageable);
    
    @Query("SELECT m FROM Manutencao m WHERE m.dataConclusao BETWEEN :startDate AND :endDate")
    Page<Manutencao> findByPeriodoConclusao(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate, 
                                           Pageable pageable);
    
    @Query("SELECT m FROM Manutencao m WHERE m.status IN ('SOLICITADA', 'APROVADA', 'EM_ANDAMENTO')")
    Page<Manutencao> findManutencoesPendentes(Pageable pageable);
    
    @Query("SELECT m FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status IN ('SOLICITADA', 'APROVADA', 'EM_ANDAMENTO')")
    Page<Manutencao> findManutencoesPendentesPorAtivo(@Param("ativoId") Long ativoId, Pageable pageable);
    
    @Query("SELECT m FROM Manutencao m ORDER BY m.dataSolicitacao DESC")
    Page<Manutencao> findAllOrderByDataSolicitacaoDesc(Pageable pageable);
    
    @Query("SELECT SUM(m.custoReal) FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status = 'CONCLUIDA'")
    BigDecimal findCustoTotalManutencaoPorAtivo(@Param("ativoId") Long ativoId);
}