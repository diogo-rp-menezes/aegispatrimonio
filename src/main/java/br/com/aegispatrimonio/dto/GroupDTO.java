package br.com.aegispatrimonio.dto;

import java.util.Set;

public record GroupDTO(
    Long id,
    String name,
    String description,
    Set<PermissionDTO> permissions
) {}
