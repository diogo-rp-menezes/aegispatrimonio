package br.com.aegispatrimonio.mapper;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.AtivoDetalheHardware;
import br.com.aegispatrimonio.model.Funcionario;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

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

        AtivoDetalheHardware hw = ativo.getDetalheHardware();
        AtivoDetalheHardwareDTO hwDTO = null;
        if (hw != null) {
            hwDTO = new AtivoDetalheHardwareDTO(
                hw.getComputerName(),
                hw.getDomain(),
                hw.getOsName(),
                hw.getOsVersion(),
                hw.getOsArchitecture(),
                hw.getMotherboardManufacturer(),
                hw.getMotherboardModel(),
                hw.getMotherboardSerialNumber(),
                hw.getCpuModel(),
                hw.getCpuCores(),
                hw.getCpuThreads()
            );
        }

        LocalDate previsaoEsgotamento = ativo.getPrevisaoEsgotamentoDisco();

        // Fallback for compatibility during migration/mixed state
        if (previsaoEsgotamento == null && ativo.getAtributos() != null && ativo.getAtributos().containsKey("previsaoEsgotamentoDisco")) {
             try {
                 Object val = ativo.getAtributos().get("previsaoEsgotamentoDisco");
                 if (val != null) {
                    String dateStr = val.toString();
                    previsaoEsgotamento = LocalDate.parse(dateStr);
                 }
             } catch (Exception e) {
                 // ignore parsing error
             }
        }

        return new AtivoDTO(
                ativo.getId(),
                ativo.getNome(),
                ativo.getNumeroPatrimonio(),
                ativo.getTipoAtivo().getId(),
                ativo.getTipoAtivo().getNome(),
                ativo.getLocalizacao() != null ? ativo.getLocalizacao().getId() : null,
                ativo.getLocalizacao() != null ? ativo.getLocalizacao().getNome() : null,
                ativo.getFilial().getId(),
                ativo.getFilial().getNome(),
                ativo.getFornecedor() != null ? ativo.getFornecedor().getId() : null,
                ativo.getFornecedor() != null ? ativo.getFornecedor().getNome() : null,
                ativo.getStatus(),
                ativo.getValorAquisicao(),
                responsavelId,
                responsavelNome,
                hwDTO,
                ativo.getAtributos(),
                previsaoEsgotamento
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
        ativo.setAtributos(ativoCreateDTO.atributos());

        return ativo;
    }
}
