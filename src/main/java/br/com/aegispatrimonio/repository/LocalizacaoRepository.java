package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    List<Localizacao> findByFilialId(Long filialId);

    boolean existsByLocalizacaoPaiId(Long localizacaoPaiId);

    Optional<Localizacao> findByNomeAndFilialAndLocalizacaoPai(String nome, Filial filial, Localizacao localizacaoPai);

}
