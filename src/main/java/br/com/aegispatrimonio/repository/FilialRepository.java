package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Filial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilialRepository extends JpaRepository<Filial, Long> {

    /**
     * Busca uma filial pelo seu código único.
     * @param codigo O código da filial.
     * @return Um Optional contendo a filial, se encontrada.
     */
    Optional<Filial> findByCodigo(String codigo);

    /**
     * Busca uma filial pelo seu nome exato (case-sensitive).
     * Usado para validações de unicidade onde a correspondência exata é necessária.
     * @param nome O nome exato da filial.
     * @return Um Optional contendo a filial, se encontrada.
     */
    Optional<Filial> findByNome(String nome);

    /**
     * Busca filiais cujo nome contém a string fornecida, ignorando maiúsculas/minúsculas.
     * @param nome A string a ser buscada no nome da filial.
     * @param pageable Informações de paginação.
     * @return Uma página de filiais que correspondem ao critério.
     */
    Page<Filial> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Verifica de forma eficiente se uma filial com o código especificado já existe.
     * @param codigo O código a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica de forma eficiente se uma filial com o nome especificado já existe.
     * @param nome O nome a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsByNome(String nome);
}