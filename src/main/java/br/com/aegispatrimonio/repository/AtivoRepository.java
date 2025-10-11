package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    // CORREÇÃO: Adicionado método para buscar ativos em múltiplas filiais.
    List<Ativo> findByFilialIdIn(Set<Long> filialIds);

    // O método findByFilialId(Long filialId) foi removido por ser obsoleto.

    Optional<Ativo> findByNumeroPatrimonio(String numeroPatrimonio);

    boolean existsByFornecedorId(Long fornecedorId);

    boolean existsByLocalizacaoId(Long localizacaoId);

    boolean existsByTipoAtivoId(Long tipoAtivoId);

    // --- Métodos para Streaming ---

    Stream<Ativo> findAllByStatus(StatusAtivo status);

    @Query("select a from Ativo a")
    Stream<Ativo> streamAll();
}
