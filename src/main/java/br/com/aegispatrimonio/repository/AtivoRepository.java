package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.dto.AtivoNameDTO;
import br.com.aegispatrimonio.dto.ChartDataDTO;
import br.com.aegispatrimonio.dto.RiskyAssetDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query(value = "SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.fornecedor " +
           "LEFT JOIN FETCH a.funcionarioResponsavel " +
           "LEFT JOIN FETCH a.detalheHardware " +
           "WHERE (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))",
           countQuery = "SELECT COUNT(a) FROM Ativo a WHERE (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))")
    Page<Ativo> findByFilters(@Param("filialId") Long filialId,
                              @Param("tipoAtivoId") Long tipoAtivoId,
                              @Param("status") StatusAtivo status,
                              @Param("nome") String nome,
                              @Param("minDate") java.time.LocalDate minDate,
                              @Param("maxDate") java.time.LocalDate maxDate,
                              @Param("hasPrediction") Boolean hasPrediction,
                              Pageable pageable);

    @Query(value = "SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.fornecedor " +
           "LEFT JOIN FETCH a.funcionarioResponsavel " +
           "LEFT JOIN FETCH a.detalheHardware " +
           "WHERE a.filial.id IN :filialIds " +
           "AND (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))",
           countQuery = "SELECT COUNT(a) FROM Ativo a WHERE a.filial.id IN :filialIds " +
           "AND (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))")
    Page<Ativo> findByFilialIdsAndFilters(@Param("filialIds") Set<Long> filialIds,
                                          @Param("filialId") Long filialId,
                                          @Param("tipoAtivoId") Long tipoAtivoId,
                                          @Param("status") StatusAtivo status,
                                          @Param("nome") String nome,
                                          @Param("minDate") java.time.LocalDate minDate,
                                          @Param("maxDate") java.time.LocalDate maxDate,
                                          @Param("hasPrediction") Boolean hasPrediction,
                                          Pageable pageable);

    @Query("SELECT new br.com.aegispatrimonio.dto.AtivoNameDTO(a.id, a.nome) FROM Ativo a WHERE (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))")
    List<AtivoNameDTO> findSimpleByFilters(@Param("filialId") Long filialId,
                                           @Param("tipoAtivoId") Long tipoAtivoId,
                                           @Param("status") StatusAtivo status,
                                           @Param("minDate") java.time.LocalDate minDate,
                                           @Param("maxDate") java.time.LocalDate maxDate,
                                           @Param("hasPrediction") Boolean hasPrediction,
                                           Pageable pageable);

    @Query("SELECT new br.com.aegispatrimonio.dto.AtivoNameDTO(a.id, a.nome) FROM Ativo a WHERE a.filial.id IN :filialIds " +
           "AND (:filialId IS NULL OR a.filial.id = :filialId) " +
           "AND (:tipoAtivoId IS NULL OR a.tipoAtivo.id = :tipoAtivoId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:minDate IS NULL OR a.previsaoEsgotamentoDisco >= :minDate) " +
           "AND (:maxDate IS NULL OR a.previsaoEsgotamentoDisco < :maxDate) " +
           "AND (:hasPrediction IS NULL OR (:hasPrediction = true AND a.previsaoEsgotamentoDisco IS NOT NULL) OR (:hasPrediction = false AND a.previsaoEsgotamentoDisco IS NULL))")
    List<AtivoNameDTO> findSimpleByFilialIdsAndFilters(@Param("filialIds") Set<Long> filialIds,
                                                      @Param("filialId") Long filialId,
                                                      @Param("tipoAtivoId") Long tipoAtivoId,
                                                      @Param("status") StatusAtivo status,
                                                      @Param("minDate") java.time.LocalDate minDate,
                                                      @Param("maxDate") java.time.LocalDate maxDate,
                                                      @Param("hasPrediction") Boolean hasPrediction,
                                                      Pageable pageable);

    @Query("SELECT a FROM Ativo a " +
           "LEFT JOIN FETCH a.filial " +
           "LEFT JOIN FETCH a.localizacao " +
           "LEFT JOIN FETCH a.tipoAtivo " +
           "LEFT JOIN FETCH a.funcionarioResponsavel " +
           "WHERE a.id IN :ids")
    List<Ativo> findAllByIdInWithDetails(@Param("ids") List<Long> ids);

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

    @Query("SELECT COUNT(a) FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    long countByCurrentTenant();

    @Query("SELECT COUNT(a) FROM Ativo a WHERE a.status = :status AND a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    long countByStatusAndCurrentTenant(@Param("status") StatusAtivo status);

    @Query("SELECT SUM(a.valorAquisicao) FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    BigDecimal getValorTotalByCurrentTenant();

    @Query("SELECT COUNT(a) FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} AND a.previsaoEsgotamentoDisco < :criticalDate")
    long countCriticalPredictionsByCurrentTenant(@Param("criticalDate") java.time.LocalDate criticalDate);

    @Query("SELECT COUNT(a) FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} AND a.previsaoEsgotamentoDisco >= :startDate AND a.previsaoEsgotamentoDisco < :endDate")
    long countWarningPredictionsByCurrentTenant(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} AND a.previsaoEsgotamentoDisco >= :safeDate")
    long countSafePredictionsByCurrentTenant(@Param("safeDate") java.time.LocalDate safeDate);

    @Query("SELECT new br.com.aegispatrimonio.dto.RiskyAssetDTO(a.id, a.nome, a.tipoAtivo.nome, a.previsaoEsgotamentoDisco) " +
           "FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} " +
           "AND a.previsaoEsgotamentoDisco IS NOT NULL " +
           "ORDER BY a.previsaoEsgotamentoDisco ASC")
    List<RiskyAssetDTO> findTopRiskyAssetsByCurrentTenant(Pageable pageable);

    @Query("SELECT new br.com.aegispatrimonio.dto.ChartDataDTO(a.status, COUNT(a)) " +
           "FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} " +
           "GROUP BY a.status")
    List<ChartDataDTO> countByStatusGrouped();

    @Query("SELECT new br.com.aegispatrimonio.dto.ChartDataDTO(a.tipoAtivo.nome, COUNT(a)) " +
           "FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} " +
           "GROUP BY a.tipoAtivo.nome")
    List<ChartDataDTO> countByTipoAtivoGrouped();

    @Query("SELECT a.previsaoEsgotamentoDisco FROM Ativo a WHERE a.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()} " +
           "AND a.previsaoEsgotamentoDisco >= :startDate AND a.previsaoEsgotamentoDisco < :endDate")
    List<java.time.LocalDate> findPredictionsBetween(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
}
