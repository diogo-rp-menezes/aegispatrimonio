package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionCreateDTO(
    @NotBlank(message = "O recurso é obrigatório")
    @Size(max = 64)
    String resource,

    @NotBlank(message = "A ação é obrigatória")
    @Size(max = 32)
    String action,

    @Size(max = 255)
    String description,

    @Size(max = 32)
    String contextKey
) {}
