package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    String EAGER_GRAPH = "manutencao-entity-graph";

    // --- Otimização N+1 ---
    // A anotação @EntityGraph instrui o Spring Data JPA a carregar todas as entidades
    // relacionadas na mesma query inicial, usando JOINs, resolvendo o problema N+1.

    @Override
    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Optional<Manutencao> findById(Long id);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByAtivoId(Long ativoId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByStatus(StatusManutencao status, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByTipo(TipoManutencao tipo, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findBySolicitanteId(Long solicitanteId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByFornecedorId(Long fornecedorId, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByDataSolicitacaoBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByDataConclusaoBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByStatusIn(List<StatusManutencao> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Page<Manutencao> findByAtivoIdAndStatusIn(Long ativoId, List<StatusManutencao> statuses, Pageable pageable);

    @Query("SELECT SUM(m.custoReal) FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status = br.com.aegispatrimonio.model.StatusManutencao.CONCLUIDA")
    BigDecimal findCustoTotalManutencaoPorAtivo(@Param("ativoId") Long ativoId);
}