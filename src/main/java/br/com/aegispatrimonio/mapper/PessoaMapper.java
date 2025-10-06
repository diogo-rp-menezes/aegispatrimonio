package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.PessoaCreateDTO;
import br.com.aegispatrimonio.dto.PessoaDTO;
import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {

    public PessoaDTO toDTO(Pessoa pessoa) {
        if (pessoa == null) {
            return null;
        }
        return new PessoaDTO(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getMatricula(),
                pessoa.getCargo(),
                pessoa.getEmail(),
                pessoa.getDepartamento().getNome(),
                pessoa.getFilial().getNome(), // Mapeia o nome da Filial
                pessoa.getStatus()
        );
    }

    public Pessoa toEntity(PessoaCreateDTO pessoaCreateDTO) {
        if (pessoaCreateDTO == null) {
            return null;
        }
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(pessoaCreateDTO.nome());
        pessoa.setMatricula(pessoaCreateDTO.matricula());
        pessoa.setCargo(pessoaCreateDTO.cargo());
        pessoa.setEmail(pessoaCreateDTO.email());
        // O Departamento e a Filial serão buscados e atribuídos na camada de Serviço.
        return pessoa;
    }
}
