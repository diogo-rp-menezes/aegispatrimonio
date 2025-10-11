package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;
import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * DTO para atualizar um Funcionário existente e seu respectivo Usuário de sistema.
 */
public record FuncionarioUpdateDTO(
    // Dados do Funcionário
    @NotBlank @Size(max = 255) String nome,
    @Size(max = 50) String matricula,
    @NotBlank @Size(max = 100) String cargo,
    @NotNull @Positive Long departamentoId,
    @NotNull Status status, // Status do funcionário (ATIVO/INATIVO)

    // CORREÇÃO: Alterado para um Set para suportar múltiplas filiais
    @NotEmpty Set<@NotNull @Positive Long> filiaisIds,

    // Dados do Usuário associado
    @NotBlank @Email @Size(max = 255) String email,

    // Senha é opcional na atualização. Se for nula ou vazia, não é alterada.
    @Size(min = 8, max = 100) String password,

    @NotBlank String role
) {
}
