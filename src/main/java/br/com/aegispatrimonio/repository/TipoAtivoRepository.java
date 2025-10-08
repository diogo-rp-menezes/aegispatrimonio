package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.TipoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAtivoRepository extends JpaRepository<TipoAtivo, Long> {

    Optional<TipoAtivo> findByNome(String nome);

}
