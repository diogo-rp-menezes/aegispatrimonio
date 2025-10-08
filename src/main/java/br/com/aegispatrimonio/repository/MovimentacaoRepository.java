package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    boolean existsByAtivoId(Long ativoId);

    boolean existsByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status);

    Page<Movimentacao> findByAtivoId(Long ativoId, Pageable pageable);

    Page<Movimentacao> findByStatus(StatusMovimentacao status, Pageable pageable);

    Page<Movimentacao> findByPessoaDestinoId(Long pessoaDestinoId, Pageable pageable);

    Page<Movimentacao> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable);

    Page<Movimentacao> findByDataMovimentacaoBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Movimentacao> findByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status, Pageable pageable);

}
