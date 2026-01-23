package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.service.DepartamentoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Departamentos.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar departamentos.
 */
@RestController
@RequestMapping("/api/v1/departamentos")
@Tag(name = "Departamentos", description = "Gerenciamento de departamentos")
@SecurityRequirement(name = "bearerAuth") // Indica que todos os endpoints deste controller requerem JWT
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @Operation(summary = "Lista todos os departamentos",
               description = "Retorna uma lista de todos os departamentos. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de departamentos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = DepartamentoDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<DepartamentoDTO> listarTodos() {
        return departamentoService.listarTodos();
    }

    @Operation(summary = "Busca um departamento por ID",
               description = "Retorna os detalhes de um departamento específico. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Departamento encontrado",
                    content = @Content(schema = @Schema(implementation = DepartamentoDTO.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public DepartamentoDTO buscarPorId(@Parameter(description = "ID do departamento", example = "1") @PathVariable Long id) {
        return departamentoService.buscarPorId(id);
    }

    @Operation(summary = "Cria um novo departamento",
               description = "Cria um novo departamento no sistema. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Departamento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DepartamentoDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "409", description = "Conflito (ex: departamento já existente)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DepartamentoDTO criar(@RequestBody @Valid DepartamentoCreateDTO departamentoCreateDTO) {
        return departamentoService.criar(departamentoCreateDTO);
    }

    @Operation(summary = "Atualiza um departamento existente",
               description = "Atualiza os dados de um departamento específico. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Departamento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DepartamentoDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflito (ex: departamento já existente)")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartamentoDTO atualizar(@Parameter(description = "ID do departamento", example = "1") @PathVariable Long id, @RequestBody @Valid DepartamentoUpdateDTO departamentoUpdateDTO) {
        return departamentoService.atualizar(id, departamentoUpdateDTO);
    }

    @Operation(summary = "Deleta um departamento",
               description = "Deleta um departamento do sistema. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Departamento deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflito (ex: departamentos associados)")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@Parameter(description = "ID do departamento", example = "1") @PathVariable Long id) {
        departamentoService.deletar(id);
    }
}
