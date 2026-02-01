package br.com.aegispatrimonio.dto.query;

import br.com.aegispatrimonio.model.StatusAtivo;

public record AtivoQueryParams(
        Long filialId,
        Long tipoAtivoId,
        StatusAtivo status,
        String nome,
        String health
) {}
