package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.model.Filial;
import org.springframework.stereotype.Component;

@Component
public class FilialMapper {

    public FilialDTO toDTO(Filial filial) {
        if (filial == null) {
            return null;
        }
        return new FilialDTO(
                filial.getId(),
                filial.getNome(),
                filial.getCodigo(),
                filial.getTipo(),
                filial.getCnpj(),
                filial.getEndereco(),
                filial.getStatus()
        );
    }

    public Filial toEntity(FilialCreateDTO filialCreateDTO) {
        if (filialCreateDTO == null) {
            return null;
        }
        Filial filial = new Filial();
        filial.setNome(filialCreateDTO.nome());
        filial.setCodigo(filialCreateDTO.codigo());
        filial.setTipo(filialCreateDTO.tipo());
        filial.setCnpj(filialCreateDTO.cnpj());
        filial.setEndereco(filialCreateDTO.endereco());
        return filial;
    }
}
