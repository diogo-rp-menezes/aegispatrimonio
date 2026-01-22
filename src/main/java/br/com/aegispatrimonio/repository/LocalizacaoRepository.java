package br.com.aegispatrimonio.repository;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, Long> {

    // CORREÇÃO: Adicionado método para buscar localizações em múltiplas filiais.
    List<Localizacao> findByFilialIdIn(Set<Long> filialIds);

    boolean existsByLocalizacaoPaiId(Long localizacaoPaiId);

    Optional<Localizacao> findByNomeAndFilialAndLocalizacaoPai(String nome, Filial filial, Localizacao localizacaoPai);

    @org.springframework.data.jpa.repository.Query("SELECT l FROM Localizacao l WHERE l.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    List<Localizacao> findAllByCurrentTenant();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(l) FROM Localizacao l WHERE l.filial.id = :#{T(br.com.aegispatrimonio.context.TenantContext).getFilialId()}")
    long countByCurrentTenant();
}
