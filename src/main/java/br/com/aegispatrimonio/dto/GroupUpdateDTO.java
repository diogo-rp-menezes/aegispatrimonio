package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record GroupUpdateDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    String description,
    Set<Long> permissionIds
) {}
