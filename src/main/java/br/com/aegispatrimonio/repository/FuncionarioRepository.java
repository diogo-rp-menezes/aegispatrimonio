package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    boolean existsByDepartamentoId(Long departamentoId);
    boolean existsByFiliais_Id(Long filialId);
}
