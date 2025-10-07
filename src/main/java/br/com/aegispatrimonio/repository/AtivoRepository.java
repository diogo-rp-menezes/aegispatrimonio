package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    List<Ativo> findByFilialId(Long filialId);

    // --- Métodos para Streaming ---

    /**
     * Retorna um Stream de todos os ativos com um status específico.
     * Ideal para processamento em lote com baixo consumo de memória.
     */
    Stream<Ativo> findAllByStatus(StatusAtivo status);

    /**
     * Retorna um Stream de todos os ativos da tabela.
     * Ideal para processamento em lote com baixo consumo de memória.
     */
    @Query("select a from Ativo a")
    Stream<Ativo> streamAll();
}
