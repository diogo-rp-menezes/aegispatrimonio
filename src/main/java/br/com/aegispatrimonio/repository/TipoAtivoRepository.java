package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAtivoRepository extends JpaRepository<TipoAtivo, Long> {

    /**
     * Busca um tipo de ativo pelo seu nome exato (case-sensitive).
     * @param nome O nome exato do tipo de ativo.
     * @return Um Optional contendo o tipo de ativo, se encontrado.
     */
    Optional<TipoAtivo> findByNome(String nome);

    /**
     * Verifica de forma eficiente se um tipo de ativo com o nome especificado já existe.
     * @param nome O nome a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsByNome(String nome);

    /**
     * Busca tipos de ativo cujo nome contém a string fornecida, ignorando maiúsculas/minúsculas.
     * @param nome A string a ser buscada no nome do tipo de ativo.
     * @param pageable Informações de paginação.
     * @return Uma página de tipos de ativo que correspondem ao critério.
     */
    Page<TipoAtivo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}