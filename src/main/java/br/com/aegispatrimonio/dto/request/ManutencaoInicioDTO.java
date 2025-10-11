package br.com.aegispatrimonio.dto.request;

import jakarta.validation.constraints.NotNull;

// CORREÇÃO: Substituído record por POJO para compatibilidade com testes (no-arg + setter/getter)
public class ManutencaoInicioDTO {

    @NotNull(message = "O ID do técnico é obrigatório.")
    private Long tecnicoId;

    public ManutencaoInicioDTO() {
    }

    public ManutencaoInicioDTO(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    // Accessor compatível com record
    public Long tecnicoId() { return getTecnicoId(); }
}
