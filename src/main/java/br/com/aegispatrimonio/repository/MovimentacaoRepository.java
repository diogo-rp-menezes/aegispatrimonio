package br.com.aegispatrimonio.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.StatusMovimentacao;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    // TODOS OS MÉTODOS AGORA SÃO PAGINADOS
    Page<Movimentacao> findAll(Pageable pageable);
    
    Page<Movimentacao> findByAtivoId(Long ativoId, Pageable pageable);
    
    Page<Movimentacao> findByStatus(StatusMovimentacao status, Pageable pageable);
    
    Page<Movimentacao> findByPessoaDestinoId(Long pessoaDestinoId, Pageable pageable);
    
    Page<Movimentacao> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable);
    
    @Query("SELECT m FROM Movimentacao m WHERE m.dataMovimentacao BETWEEN :startDate AND :endDate")
    Page<Movimentacao> findByPeriodo(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate, 
                                    Pageable pageable);
    
    @Query("SELECT m FROM Movimentacao m WHERE m.ativo.id = :ativoId AND m.status = 'PENDENTE'")
    Page<Movimentacao> findMovimentacoesPendentesPorAtivo(@Param("ativoId") Long ativoId, Pageable pageable);
    
    // ✅ NOVO MÉTODO - Para validação (mais eficiente)
    @Query("SELECT COUNT(m) > 0 FROM Movimentacao m WHERE m.ativo.id = :ativoId AND m.status = :status")
    boolean existsByAtivoIdAndStatus(@Param("ativoId") Long ativoId, @Param("status") StatusMovimentacao status);
    
    @Query("SELECT m FROM Movimentacao m ORDER BY m.dataMovimentacao DESC")
    Page<Movimentacao> findAllOrderByDataMovimentacaoDesc(Pageable pageable);
}