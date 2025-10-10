package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.model.TipoAtivo;
import org.springframework.stereotype.Component;

@Component
public class TipoAtivoMapper {

    public TipoAtivoDTO toDTO(TipoAtivo tipoAtivo) {
        if (tipoAtivo == null) {
            return null;
        }
        return new TipoAtivoDTO(tipoAtivo.getId(), tipoAtivo.getNome(), tipoAtivo.getCategoriaContabil());
    }

    public TipoAtivo toEntity(TipoAtivoCreateDTO tipoAtivoCreateDTO) {
        if (tipoAtivoCreateDTO == null) {
            return null;
        }
        TipoAtivo tipoAtivo = new TipoAtivo();
        tipoAtivo.setNome(tipoAtivoCreateDTO.nome());
        tipoAtivo.setCategoriaContabil(tipoAtivoCreateDTO.categoriaContabil());
        return tipoAtivo;
    }
}
