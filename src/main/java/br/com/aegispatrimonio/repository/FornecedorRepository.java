package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    /**
     * Busca um fornecedor pelo seu nome exato (case-sensitive).
     * @param nome O nome exato do fornecedor.
     * @return Um Optional contendo o fornecedor, se encontrado.
     */
    Optional<Fornecedor> findByNome(String nome);

    /**
     * Busca fornecedores cujo nome contém a string fornecida, ignorando maiúsculas/minúsculas.
     * @param nome A string a ser buscada no nome do fornecedor.
     * @param pageable Informações de paginação.
     * @return Uma página de fornecedores que correspondem ao critério.
     */
    Page<Fornecedor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Busca fornecedores pelo email de contato.
     * @param emailContato O email de contato a ser buscado.
     * @param pageable Informações de paginação.
     * @return Uma página de fornecedores que correspondem ao critério.
     */
    Page<Fornecedor> findByEmailContato(String emailContato, Pageable pageable);

    /**
     * Verifica de forma eficiente se um fornecedor com o nome especificado já existe.
     * @param nome O nome a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsByNome(String nome);

}