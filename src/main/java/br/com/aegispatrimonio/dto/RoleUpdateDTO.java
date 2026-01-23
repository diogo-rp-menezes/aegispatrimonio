package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RoleUpdateDTO(
    @NotBlank(message = "O nome da role é obrigatório")
    @Size(max = 64)
    String name,

    @Size(max = 255)
    String description,

    Set<Long> permissionIds
) {}
