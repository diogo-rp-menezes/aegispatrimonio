package br.com.aegispatrimonio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.aegispatrimonio.model.Localizacao;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {
    
    // Métodos existentes (mantidos para compatibilidade)
    List<Localizacao> findByFilialId(Long filialId);
    List<Localizacao> findByLocalizacaoPaiId(Long localizacaoPaiId);
    
    @Query("SELECT l FROM Localizacao l WHERE l.nome LIKE %:nome%")
    List<Localizacao> findByNomeContaining(@Param("nome") String nome);
    
    // NOVOS métodos para paginação (seguindo padrão Ativo - MESMA ASSINATURA)
    Page<Localizacao> findAll(Pageable pageable);
    
    Page<Localizacao> findByFilialId(Long filialId, Pageable pageable);
    
    Page<Localizacao> findByLocalizacaoPaiId(Long localizacaoPaiId, Pageable pageable);
    
    @Query("SELECT l FROM Localizacao l WHERE l.nome LIKE %:nome%")
    Page<Localizacao> findByNomeContaining(@Param("nome") String nome, Pageable pageable);
    
    // Método ordenado padrão (como no Ativo não tem, mas mantendo por consistência)
    @Query("SELECT l FROM Localizacao l ORDER BY l.nome")
    Page<Localizacao> findAllOrderByNome(Pageable pageable);
}