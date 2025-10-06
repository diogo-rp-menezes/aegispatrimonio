package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {
    List<Localizacao> findByFilialId(Long filialId);
}
