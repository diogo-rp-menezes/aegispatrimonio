package br.com.aegispatrimonio.dto;

public record PermissionDTO(
    Long id,
    String resource,
    String action,
    String description,
    String contextKey
) {}
