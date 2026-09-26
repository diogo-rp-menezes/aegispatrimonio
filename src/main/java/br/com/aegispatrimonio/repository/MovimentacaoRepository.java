package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    boolean existsByAtivoId(Long ativoId);

    boolean existsByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status);

    Page<Movimentacao> findByAtivoId(Long ativoId, Pageable pageable);

    Page<Movimentacao> findByStatus(StatusMovimentacao status, Pageable pageable);

    // CORREÇÃO: Método renomeado para corresponder à entidade Movimentacao refatorada
    Page<Movimentacao> findByFuncionarioDestinoId(Long funcionarioDestinoId, Pageable pageable);

    Page<Movimentacao> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable);

    Page<Movimentacao> findByDataMovimentacaoBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Movimentacao> findByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status, Pageable pageable);

    // Isolamento de tenant: variantes filtradas pelas filiais do ativo
    Page<Movimentacao> findByAtivoFilialIdIn(Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByAtivoIdAndAtivoFilialIdIn(Long ativoId, Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByStatusAndAtivoFilialIdIn(StatusMovimentacao status, Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByFuncionarioDestinoIdAndAtivoFilialIdIn(Long funcionarioDestinoId, Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByLocalizacaoDestinoIdAndAtivoFilialIdIn(Long localizacaoDestinoId, Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByDataMovimentacaoBetweenAndAtivoFilialIdIn(LocalDate startDate, LocalDate endDate, Collection<Long> filiaisIds, Pageable pageable);

    Page<Movimentacao> findByAtivoIdAndStatusAndAtivoFilialIdIn(Long ativoId, StatusMovimentacao status, Collection<Long> filiaisIds, Pageable pageable);

}
