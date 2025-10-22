package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByCnpj(String cnpj);

    @Query("SELECT f FROM Fornecedor f WHERE f.id = :id")
    Optional<Fornecedor> findByIdEvenInactive(@Param("id") Long id);

}
