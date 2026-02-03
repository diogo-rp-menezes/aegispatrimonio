package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.service.FilialService;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar as operações CRUD de Filiais.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar filiais.
 */
@RestController
@RequestMapping("/api/v1/filiais")
@Validated
@Tag(name = "Filiais", description = "Gerenciamento de filiais da empresa")
@SecurityRequirement(name = "bearerAuth") // Indica que todos os endpoints deste controller requerem JWT
public class FilialController {

    private final FilialService filialService;

    public FilialController(FilialService filialService) {
        this.filialService = filialService;
    }

    @Operation(summary = "Lista todas as filiais", description = "Retorna uma lista de todas as filiais. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filiais retornada com sucesso", content = @Content(schema = @Schema(implementation = FilialDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<FilialDTO> listarTodos() {
        return filialService.listarTodos();
    }

    @Operation(summary = "Busca uma filial por ID", description = "Retorna os detalhes de uma filial específica. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filial encontrada", content = @Content(schema = @Schema(implementation = FilialDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Filial não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<FilialDTO> buscarPorId(
            @Parameter(description = "ID da filial", example = "1") @PathVariable Long id) {
        Optional<FilialDTO> filial = filialService.buscarPorId(id);
        return filial.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria uma nova filial", description = "Cria uma nova filial no sistema. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filial criada com sucesso", content = @Content(schema = @Schema(implementation = FilialDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (ex: CNPJ ou código duplicado)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FilialDTO criar(@RequestBody @Valid FilialCreateDTO filialCreateDTO) {
        return filialService.criar(filialCreateDTO);
    }

    @Operation(summary = "Atualiza uma filial existente", description = "Atualiza os dados de uma filial específica. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filial atualizada com sucesso", content = @Content(schema = @Schema(implementation = FilialDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Filial não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (ex: CNPJ ou código duplicado)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilialDTO> atualizar(
            @Parameter(description = "ID da filial", example = "1") @PathVariable Long id,
            @RequestBody @Valid FilialUpdateDTO filialUpdateDTO) {
        Optional<FilialDTO> filialAtualizada = filialService.atualizar(id, filialUpdateDTO);
        return filialAtualizada.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deleta uma filial (exclusão lógica)", description = "Marca uma filial como INATIVA. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filial deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Filial não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (ex: departamentos/funcionários associados)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@Parameter(description = "ID da filial", example = "1") @PathVariable Long id) {
        filialService.deletar(id);
    }
}
