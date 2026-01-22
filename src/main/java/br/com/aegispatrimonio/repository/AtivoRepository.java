package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    @Query("SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.funcionarioResponsavel")
    List<Ativo> findAllWithDetails();

    @Query("SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.funcionarioResponsavel " +
           "WHERE a.id = :id")
    Optional<Ativo> findByIdWithDetails(@Param("id") Long id);

    // CORREÇÃO: Adicionando o método que faltava para corrigir o erro de compilação.
    @Query("SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.funcionarioResponsavel " +
           "WHERE a.filial.id IN :filialIds")
    List<Ativo> findByFilialIdInWithDetails(@Param("filialIds") Set<Long> filialIds);

    List<Ativo> findByFilialIdIn(Set<Long> filialIds);

    // Suporte à paginação mantendo compatibilidade no controller/serviço
    Page<Ativo> findByFilialIdIn(Set<Long> filialIds, Pageable pageable);

    @Query("SELECT a FROM Ativo a WHERE (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Ativo> findByFilters(@Param("filialId") Long filialId,
                              @Param("tipoAtivoId") Long tipoAtivoId,
                              @Param("status") StatusAtivo status,
                              Pageable pageable);

    @Query("SELECT a FROM Ativo a WHERE a.filial.id IN :filialIds " +
           "AND (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Ativo> findByFilialIdsAndFilters(@Param("filialIds") Set<Long> filialIds,
                                          @Param("filialId") Long filialId,
                                          @Param("tipoAtivoId") Long tipoAtivoId,
                                          @Param("status") StatusAtivo status,
                                          Pageable pageable);

    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);

    boolean existsByFornecedorId(Long fornecedorId);

    boolean existsByLocalizacaoId(Long localizacaoId);

    boolean existsByTipoAtivoId(Long tipoAtivoId);

    Stream<Ativo> findAllByStatus(StatusAtivo status);

    @Query("select a from Ativo a")
    Stream<Ativo> streamAll();

    /**
     * Retorna todos os ativos do contexto atual (Synaptic Switching / Tenant Isolation).
     */
    @Query("SELECT a FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    List<Ativo> findAllByCurrentTenant();
}
