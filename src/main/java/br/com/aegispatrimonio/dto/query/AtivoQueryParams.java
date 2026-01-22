package br.com.aegispatrimonio.dto.query;

import br.com.aegispatrimonio.model.StatusAtivo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtivoQueryParams(
        Long filialId,
        Long tipoAtivoId,
        StatusAtivo status
) {}
