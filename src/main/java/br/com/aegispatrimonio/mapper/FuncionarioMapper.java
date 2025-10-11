package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FuncionarioDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper para converter a entidade Funcionario em seu DTO de resposta.
 */
@Component
public class FuncionarioMapper {

    public FuncionarioDTO toDTO(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }

        Usuario usuario = funcionario.getUsuario();

        return new FuncionarioDTO(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getMatricula(),
                funcionario.getCargo(),
                (usuario != null) ? usuario.getEmail() : null,
                (usuario != null) ? usuario.getRole() : null,
                (funcionario.getDepartamento() != null) ? funcionario.getDepartamento().getNome() : null,
                (funcionario.getFiliais() != null) ? funcionario.getFiliais().stream().map(Filial::getNome).collect(Collectors.toSet()) : Collections.emptySet(),
                funcionario.getStatus()
        );
    }
}
