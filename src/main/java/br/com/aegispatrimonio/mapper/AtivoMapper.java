package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.model.Ativo;
import org.springframework.stereotype.Component;

@Component
public class AtivoMapper {

    public AtivoDTO toDTO(Ativo ativo) {
        if (ativo == null) {
            return null;
        }
        return new AtivoDTO(
                ativo.getId(),
                ativo.getNome(),
                ativo.getNumeroPatrimonio(),
                ativo.getTipoAtivo().getNome(),
                ativo.getLocalizacao() != null ? ativo.getLocalizacao().getNome() : null, // Trata campo opcional
                ativo.getFilial().getNome(), // Mapeia o nome da Filial
                ativo.getStatus()
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

        // As entidades relacionadas (Filial, TipoAtivo, Localizacao, etc.) serão
        // buscadas e atribuídas na camada de Serviço (Service).

        return ativo;
    }
}
