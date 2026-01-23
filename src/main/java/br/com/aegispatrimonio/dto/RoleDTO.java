package br.com.aegispatrimonio.dto;

import java.util.Set;

public record RoleDTO(
    Long id,
    String name,
    String description,
    Set<PermissionDTO> permissions
) {}
