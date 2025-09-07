package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Departamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    
    // Método para paginação
    Page<Departamento> findAll(Pageable pageable);
    
    // Métodos adicionais para busca paginada
    Page<Departamento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Departamento> findByFilialId(Long filialId, Pageable pageable);
}