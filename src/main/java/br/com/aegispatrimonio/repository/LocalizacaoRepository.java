package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {
    
    List<Localizacao> findByFilialId(Long filialId);
    
    List<Localizacao> findByLocalizacaoPaiId(Long localizacaoPaiId);
    
    @Query("SELECT l FROM Localizacao l WHERE l.nome LIKE %:nome%")
    List<Localizacao> findByNomeContaining(@Param("nome") String nome);
}