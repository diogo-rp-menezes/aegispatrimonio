package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.model.Departamento;
import org.springframework.stereotype.Component;

@Component
public class DepartamentoMapper {

    public DepartamentoDTO toDTO(Departamento departamento) {
        if (departamento == null) {
            return null;
        }
        return new DepartamentoDTO(
                departamento.getId(),
                departamento.getNome(),
                departamento.getFilial().getNome()
        );
    }

    public Departamento toEntity(DepartamentoCreateDTO departamentoCreateDTO) {
        if (departamentoCreateDTO == null) {
            return null;
        }
        Departamento departamento = new Departamento();
        departamento.setNome(departamentoCreateDTO.nome());
        // A Filial será buscada e atribuída na camada de Serviço.
        return departamento;
    }
}
