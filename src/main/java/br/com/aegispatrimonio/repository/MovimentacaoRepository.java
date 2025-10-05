package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    // --- Otimização N+1 ---
    // A anotação @EntityGraph instrui o Spring Data JPA a carregar todas as entidades
    // relacionadas na mesma query inicial, usando JOINs, resolvendo o problema N+1.
    String EAGER_GRAPH = "movimentacao-entity-graph";

    @Override
    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Optional<Movimentacao> findById(Long id);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByAtivoId(Long ativoId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByStatus(StatusMovimentacao status, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByPessoaDestinoId(Long pessoaDestinoId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByDataMovimentacaoBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "localizacaoOrigem", "localizacaoDestino", "pessoaOrigem", "pessoaDestino"})
    Page<Movimentacao> findByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status, Pageable pageable);

    boolean existsByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status);

}