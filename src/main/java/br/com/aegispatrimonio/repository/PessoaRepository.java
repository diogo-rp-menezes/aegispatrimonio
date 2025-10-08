package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    Optional<Pessoa> findByEmail(String email);

    List<Pessoa> findByFilialId(Long filialId);

    boolean existsByDepartamentoId(Long departamentoId);

    boolean existsByFilialId(Long filialId);
}
