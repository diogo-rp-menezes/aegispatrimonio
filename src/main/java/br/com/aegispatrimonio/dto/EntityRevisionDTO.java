package br.com.aegispatrimonio.dto;

public record EntityRevisionDTO<T>(
    T entity,
    RevisionDTO revision
) {
}
