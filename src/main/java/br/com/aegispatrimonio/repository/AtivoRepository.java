package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
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

    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);

    boolean existsByFornecedorId(Long fornecedorId);

    boolean existsByLocalizacaoId(Long localizacaoId);

    boolean existsByTipoAtivoId(Long tipoAtivoId);

    Stream<Ativo> findAllByStatus(StatusAtivo status);

    @Query("select a from Ativo a")
    Stream<Ativo> streamAll();
}
