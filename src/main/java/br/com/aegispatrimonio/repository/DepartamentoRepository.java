package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    
    List<Departamento> findByFilialId(Long filialId);
    
    List<Departamento> findByNomeContaining(String nome); // ✅ Derived query
    
    Optional<Departamento> findByCentroCusto(String centroCusto);
    
    // ✅ Consultas adicionais úteis com derived queries:
    boolean existsByCentroCusto(String centroCusto);
    
    List<Departamento> findByFilialIdAndNomeContaining(Long filialId, String nome);
    
    Optional<Departamento> findFirstByCentroCusto(String centroCusto);
}