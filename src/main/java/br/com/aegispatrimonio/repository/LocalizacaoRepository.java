package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    // --- Otimização N+1 ---
    // A anotação @EntityGraph instrui o Spring Data JPA a carregar as entidades
    // relacionadas 'filial' e 'localizacaoPai' na mesma query inicial, usando JOINs.

    @Override
    @EntityGraph(attributePaths = {"filial", "localizacaoPai"})
    Page<Localizacao> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"filial", "localizacaoPai"})
    Optional<Localizacao> findById(Long id);

    @EntityGraph(attributePaths = {"filial", "localizacaoPai"})
    Page<Localizacao> findByFilialId(Long filialId, Pageable pageable);

    @EntityGraph(attributePaths = {"filial", "localizacaoPai"})
    Page<Localizacao> findByLocalizacaoPaiId(Long localizacaoPaiId, Pageable pageable);

    @EntityGraph(attributePaths = {"filial", "localizacaoPai"})
    Page<Localizacao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

}