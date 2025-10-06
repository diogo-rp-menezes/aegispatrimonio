package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.model.Localizacao;
import org.springframework.stereotype.Component;

@Component
public class LocalizacaoMapper {

    public LocalizacaoDTO toDTO(Localizacao localizacao) {
        if (localizacao == null) {
            return null;
        }
        return new LocalizacaoDTO(
                localizacao.getId(),
                localizacao.getNome(),
                localizacao.getDescricao(),
                localizacao.getFilial().getNome(),
                localizacao.getLocalizacaoPai() != null ? localizacao.getLocalizacaoPai().getNome() : null,
                localizacao.getStatus()
        );
    }

    public Localizacao toEntity(LocalizacaoCreateDTO localizacaoCreateDTO) {
        if (localizacaoCreateDTO == null) {
            return null;
        }
        Localizacao localizacao = new Localizacao();
        localizacao.setNome(localizacaoCreateDTO.nome());
        localizacao.setDescricao(localizacaoCreateDTO.descricao());
        // A Filial e a LocalizacaoPai serão buscadas e atribuídas na camada de Serviço.
        return localizacao;
    }
}
