package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public FornecedorDTO toDTO(Fornecedor fornecedor) {
        if (fornecedor == null) {
            return null;
        }
        return new FornecedorDTO(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getCnpj(),
                fornecedor.getEndereco(),
                fornecedor.getNomeContatoPrincipal(),
                fornecedor.getEmailPrincipal(),
                fornecedor.getTelefonePrincipal(),
                fornecedor.getObservacoes(),
                fornecedor.getStatus()
        );
    }

    public Fornecedor toEntity(FornecedorCreateDTO fornecedorCreateDTO) {
        if (fornecedorCreateDTO == null) {
            return null;
        }
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(fornecedorCreateDTO.nome());
        fornecedor.setCnpj(fornecedorCreateDTO.cnpj());
        fornecedor.setEndereco(fornecedorCreateDTO.endereco());
        fornecedor.setNomeContatoPrincipal(fornecedorCreateDTO.nomeContatoPrincipal());
        fornecedor.setEmailPrincipal(fornecedorCreateDTO.emailPrincipal());
        fornecedor.setTelefonePrincipal(fornecedorCreateDTO.telefonePrincipal());
        fornecedor.setObservacoes(fornecedorCreateDTO.observacoes());
        return fornecedor;
    }
}
