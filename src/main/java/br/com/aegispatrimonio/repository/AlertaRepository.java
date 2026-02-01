package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.model.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long>, JpaSpecificationExecutor<Alerta> {

    @EntityGraph(attributePaths = "ativo")
    List<Alerta> findTop5ByLidoFalseOrderByDataCriacaoDesc();

    @EntityGraph(attributePaths = "ativo")
    List<Alerta> findTop5ByAtivo_Filial_IdInAndLidoFalseOrderByDataCriacaoDesc(Collection<Long> filialIds);

    List<Alerta> findByAtivoIdAndLidoFalseAndTipo(Long ativoId, TipoAlerta tipo);

    List<Alerta> findByAtivoIdAndLidoFalse(Long ativoId);

    @EntityGraph(attributePaths = "ativo")
    Page<Alerta> findByAtivo_Filial_IdIn(Collection<Long> filialIds, Pageable pageable);

    @EntityGraph(attributePaths = "ativo")
    Page<Alerta> findByAtivo_Filial_IdInAndLido(Collection<Long> filialIds, boolean lido, Pageable pageable);

    @EntityGraph(attributePaths = "ativo")
    Page<Alerta> findByLidoFalse(Pageable pageable);

    @EntityGraph(attributePaths = "ativo")
    Page<Alerta> findByLido(boolean lido, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "ativo")
    Page<Alerta> findAll(Pageable pageable);
}
