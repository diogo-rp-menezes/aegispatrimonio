package br.com.aegispatrimonio.dto;

import br.com.aegispatrimonio.model.Status;

import java.util.Set;

/**
 * DTO para representar os dados de um funcionário na resposta de uma API.
 * Combina informações do funcionário, seu usuário de sistema e seus relacionamentos.
 */
public record FuncionarioDTO(
    Long id,
    String nome,
    String matricula,
    String cargo,
    String email,      // Proveniente da entidade Usuario
    String role,       // Proveniente da entidade Usuario
    String departamento, // Nome do departamento
    Long departamentoId, // ID do departamento
    Set<String> filiais, // Nomes das filiais associadas
    Set<Long> filiaisIds, // IDs das filiais associadas
    Status status
) {
}
