package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.TipoFilial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilialRepository extends JpaRepository<Filial, Long> {

    Optional<Filial> findByCnpj(String cnpj);

    Optional<Filial> findByCodigo(String codigo);

    Optional<Filial> findByTipo(TipoFilial tipo);
}
