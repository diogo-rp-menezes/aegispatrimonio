package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    List<Manutencao> findByAtivoId(Long ativoId);

    List<Manutencao> findByStatus(StatusManutencao status);

    List<Manutencao> findByTipo(TipoManutencao tipo);

    List<Manutencao> findBySolicitanteId(Long solicitanteId);

    List<Manutencao> findByFornecedorId(Long fornecedorId);

    @Query("SELECT m FROM Manutencao m WHERE m.dataSolicitacao BETWEEN :startDate AND :endDate")
    List<Manutencao> findByPeriodoSolicitacao(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Manutencao m WHERE m.dataConclusao BETWEEN :startDate AND :endDate")
    List<Manutencao> findByPeriodoConclusao(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Manutencao m WHERE m.status IN ('SOLICITADA', 'APROVADA', 'EM_ANDAMENTO')")
    List<Manutencao> findManutencoesPendentes();

    @Query("SELECT m FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status IN ('SOLICITADA', 'APROVADA', 'EM_ANDAMENTO')")
    List<Manutencao> findManutencoesPendentesPorAtivo(@Param("ativoId") Long ativoId);

    @Query("SELECT SUM(m.custoReal) FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status = 'CONCLUIDA'")
    BigDecimal findCustoTotalManutencaoPorAtivo(@Param("ativoId") Long ativoId);
}