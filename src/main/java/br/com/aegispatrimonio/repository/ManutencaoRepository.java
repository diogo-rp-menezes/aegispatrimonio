package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Manutencao;
import br.com.aegispatrimonio.model.StatusManutencao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long>, JpaSpecificationExecutor<Manutencao> {

    boolean existsByAtivoId(Long ativoId);

    boolean existsByAtivoIdAndStatusIn(Long ativoId, Collection<StatusManutencao> statuses);

    @Override
    @EntityGraph(attributePaths = {"ativo", "solicitante", "fornecedor", "tecnicoResponsavel"})
    Optional<Manutencao> findById(Long id);

    @Query("SELECT SUM(m.custoReal) FROM Manutencao m WHERE m.ativo.id = :ativoId AND m.status = br.com.aegispatrimonio.model.StatusManutencao.CONCLUIDA")
    BigDecimal findCustoTotalManutencaoPorAtivo(@Param("ativoId") Long ativoId);

}
