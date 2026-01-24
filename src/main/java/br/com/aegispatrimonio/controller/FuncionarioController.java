package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Funcionários e seus respectivos Usuários.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar funcionários.
 * Protegido por RBAC Granular.
 */
@RestController
@RequestMapping("/api/v1/funcionarios")
@Tag(name = "Funcionários", description = "Gerencia o cadastro de funcionários e seus respectivos usuários")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista todos os funcionários", description = "Retorna a lista de todos os funcionários cadastrados no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content(schema = @Schema(hidden = true)))
    })
    public List<FuncionarioDTO> listarTodos() {
        return funcionarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasFuncionarioPermission(authentication, #id, 'READ')")
    @Operation(summary = "Busca um funcionário por ID", description = "Retorna um funcionário específico com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public FuncionarioDTO buscarPorId(@Parameter(description = "ID do funcionário a ser buscado", example = "1") @PathVariable Long id) {
        return funcionarioService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, null, 'FUNCIONARIO', 'CREATE', #createDTO.filiaisIds)")
    @Operation(summary = "Cria um novo funcionário", description = "Cria um novo funcionário e seu respectivo usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content(schema = @Schema(hidden = true)))
    })
    public FuncionarioDTO criar(@Valid @RequestBody FuncionarioCreateDTO createDTO) {
        return funcionarioService.criar(createDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasFuncionarioPermission(authentication, #id, 'UPDATE') and @permissionService.hasPermission(authentication, null, 'FUNCIONARIO', 'UPDATE', #updateDTO.filiaisIds)")
    @Operation(summary = "Atualiza um funcionário existente", description = "Atualiza os dados de um funcionário existente e seu respectivo usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public FuncionarioDTO atualizar(@Parameter(description = "ID do funcionário a ser atualizado", example = "1") @PathVariable Long id, @Valid @RequestBody FuncionarioUpdateDTO updateDTO) {
        return funcionarioService.atualizar(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasFuncionarioPermission(authentication, #id, 'DELETE')")
    @Operation(summary = "Deleta um funcionário", description = "Deleta um funcionário do sistema (exclusão lógica).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public void deletar(@Parameter(description = "ID do funcionário a ser deletado", example = "1") @PathVariable Long id) {
        funcionarioService.deletar(id);
    }
}
