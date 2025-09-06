package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByAtivoId(Long ativoId);

    List<Movimentacao> findByStatus(StatusMovimentacao status);

    List<Movimentacao> findByPessoaDestinoId(Long pessoaDestinoId);

    List<Movimentacao> findByLocalizacaoDestinoId(Long localizacaoDestinoId);

    @Query("SELECT m FROM Movimentacao m WHERE m.dataMovimentacao BETWEEN :startDate AND :endDate")
    List<Movimentacao> findByPeriodo(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Movimentacao m WHERE m.ativo.id = :ativoId AND m.status = 'PENDENTE'")
    List<Movimentacao> findMovimentacoesPendentesPorAtivo(@Param("ativoId") Long ativoId);
}