package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long>, JpaSpecificationExecutor<Funcionario> {
    boolean existsByDepartamentoId(Long departamentoId);
    boolean existsByFiliais_Id(Long filialId);
    List<Funcionario> findDistinctByFiliais_IdIn(Set<Long> filialIds);
}
