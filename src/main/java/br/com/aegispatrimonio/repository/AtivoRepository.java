package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long>, JpaSpecificationExecutor<Ativo> {

    // --- Otimização N+1 ---
    @Override
    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Optional<Ativo> findById(Long id);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    List<Ativo> findByStatus(StatusAtivo status);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findByTipoAtivoId(Long tipoAtivoId, Pageable pageable);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findByLocalizacaoId(Long localizacaoId, Pageable pageable);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findByStatus(StatusAtivo status, Pageable pageable);

    @Query("SELECT a FROM Ativo a WHERE a.valorAquisicao BETWEEN :valorMin AND :valorMax")
    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findByValorAquisicaoBetween(@Param("valorMin") BigDecimal valorMin,
                                          @Param("valorMax") BigDecimal valorMax,
                                          Pageable pageable);

    @EntityGraph(attributePaths = {"tipoAtivo", "localizacao", "fornecedor", "pessoaResponsavel"})
    Page<Ativo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    boolean existsByNumeroPatrimonio(String numeroPatrimonio);

    // --- Métodos de Streaming para Processamento em Lote ---

    /**
     * Retorna um Stream de todos os ativos. Ideal para processamento em lote
     * com baixo consumo de memória, pois não carrega todos os ativos de uma vez.
     * Requer uma transação ativa (@Transactional).
     */
    @Query("SELECT a FROM Ativo a")
    Stream<Ativo> streamAll();

    /**
     * Retorna um Stream de todos os ativos com um status específico.
     * Ideal para o job agendado de depreciação.
     */
    @Query("SELECT a FROM Ativo a WHERE a.status = :status")
    Stream<Ativo> streamByStatus(@Param("status") StatusAtivo status);

}