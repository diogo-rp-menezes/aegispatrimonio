package br.com.aegispatrimonio.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * DTO para criar um novo Funcionário e seu respectivo Usuário de sistema.
 */
public record FuncionarioCreateDTO(
    // Dados do Funcionário
    @NotBlank @Size(max = 255) String nome,
    @Size(max = 50) String matricula,
    @NotBlank @Size(max = 100) String cargo,
    @NotNull @Positive Long departamentoId,

    // CORREÇÃO: Alterado para um Set para suportar múltiplas filiais
    @NotEmpty Set<@NotNull @Positive Long> filiaisIds,

    // Dados do Usuário associado
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotBlank String role
) {
}
