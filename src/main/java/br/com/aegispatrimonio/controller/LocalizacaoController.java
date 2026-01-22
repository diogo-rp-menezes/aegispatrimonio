package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.service.LocalizacaoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Localizações (ex: Sala de Reunião, Almoxarifado).
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar localizações.
 */
@RestController
@RequestMapping("/localizacoes")
@Tag(name = "Localizações", description = "Gerencia o cadastro de localizações")
@SecurityRequirement(name = "bearerAuth")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    public LocalizacaoController(LocalizacaoService localizacaoService) {
        this.localizacaoService = localizacaoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista todas as localizações", description = "Retorna a lista de todas as localizações cadastradas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(schema = @Schema(implementation = LocalizacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    public List<LocalizacaoDTO> listarTodos() {
        return localizacaoService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Busca uma localização por ID", description = "Retorna uma localização específica com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Localização encontrada", content = @Content(schema = @Schema(implementation = LocalizacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public LocalizacaoDTO buscarPorId(@Parameter(description = "ID da localização a ser buscada", example = "1") @PathVariable Long id) {
        return localizacaoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma nova localização", description = "Cria uma nova localização no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Localização criada com sucesso", content = @Content(schema = @Schema(implementation = LocalizacaoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    public LocalizacaoDTO criar(@Valid @RequestBody LocalizacaoCreateDTO localizacaoCreateDTO) {
        return localizacaoService.criar(localizacaoCreateDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma localização existente", description = "Atualiza os dados de uma localização existente no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Localização atualizada com sucesso", content = @Content(schema = @Schema(implementation = LocalizacaoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public LocalizacaoDTO atualizar(@Parameter(description = "ID da localização a ser atualizada", example = "1") @PathVariable Long id, @Valid @RequestBody LocalizacaoUpdateDTO localizacaoUpdateDTO) {
        return localizacaoService.atualizar(id, localizacaoUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deleta uma localização", description = "Deleta uma localização do sistema (exclusão lógica).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Localização deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public void deletar(@Parameter(description = "ID da localização a ser deletada", example = "1") @PathVariable Long id) {
        localizacaoService.deletar(id);
    }
}
