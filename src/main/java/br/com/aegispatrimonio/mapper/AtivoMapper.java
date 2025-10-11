package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Funcionario;
import org.springframework.stereotype.Component;

@Component
public class AtivoMapper {

    public AtivoDTO toDTO(Ativo ativo) {
        if (ativo == null) {
            return null;
        }

        // CORREÇÃO: Lógica para extrair dados do funcionário responsável
        Funcionario responsavel = ativo.getFuncionarioResponsavel();
        Long responsavelId = (responsavel != null) ? responsavel.getId() : null;
        String responsavelNome = (responsavel != null) ? responsavel.getNome() : null;

        return new AtivoDTO(
                ativo.getId(),
                ativo.getNome(),
                ativo.getNumeroPatrimonio(),
                ativo.getTipoAtivo().getNome(),
                ativo.getLocalizacao() != null ? ativo.getLocalizacao().getNome() : null,
                ativo.getFilial().getNome(),
                ativo.getStatus(),
                responsavelId,
                responsavelNome
        );
    }

    public Ativo toEntity(AtivoCreateDTO ativoCreateDTO) {
        if (ativoCreateDTO == null) {
            return null;
        }

        Ativo ativo = new Ativo();
        ativo.setNome(ativoCreateDTO.nome());
        ativo.setNumeroPatrimonio(ativoCreateDTO.numeroPatrimonio());
        ativo.setDataAquisicao(ativoCreateDTO.dataAquisicao());
        ativo.setValorAquisicao(ativoCreateDTO.valorAquisicao());
        ativo.setObservacoes(ativoCreateDTO.observacoes());
        ativo.setInformacoesGarantia(ativoCreateDTO.informacoesGarantia());

        // As entidades relacionadas (Filial, TipoAtivo, Funcionario, etc.) 
        // são buscadas e atribuídas na camada de Serviço (Service).

        return ativo;
    }
}
