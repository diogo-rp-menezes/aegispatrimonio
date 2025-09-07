package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.LocalizacaoResponseDTO;
import br.com.aegispatrimonio.service.LocalizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/localizacoes")
@RequiredArgsConstructor
@Tag(name = "Localizações", description = "Operações relacionadas à gestão de localizações físicas dos ativos")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @PostMapping
    @Operation(summary = "Criar nova localização", description = "Cadastra uma nueva localização física no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Localização criada com sucesso", 
                    content = @Content(schema = @Schema(implementation = LocalizacaoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Filial ou localização pai não encontrada"),
        @ApiResponse(responseCode = "409", description = "Localização com mesmo nome já existe na mesma hierarquia")
    })
    public ResponseEntity<LocalizacaoResponseDTO> criar(@Valid @RequestBody LocalizacaoRequestDTO request) {
        LocalizacaoResponseDTO response = localizacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as localizações", description = "Retorna uma lista paginada de todas as localizações, com filtros opcionais")
    public ResponseEntity<Page<LocalizacaoResponseDTO>> listarTodos(
            @Parameter(description = "Nome para filtro (opcional)", example = "sala") 
            @RequestParam(required = false) String nome,
            @Parameter(description = "ID da filial para filtro (opcional)", example = "1") 
            @RequestParam(required = false) Long filialId,
            @Parameter(description = "ID da localização pai para filtro (opcional)", example = "1") 
            @RequestParam(required = false) Long localizacaoPaiId,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        
        if (nome != null && !nome.trim().isEmpty()) {
            return ResponseEntity.ok(localizacaoService.buscarPorNome(nome, pageable));
        }
        
        if (filialId != null) {
            return ResponseEntity.ok(localizacaoService.listarPorFilial(filialId, pageable));
        }
        
        if (localizacaoPaiId != null) {
            return ResponseEntity.ok(localizacaoService.listarPorLocalizacaoPai(localizacaoPaiId, pageable));
        }
        
        return ResponseEntity.ok(localizacaoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar localização por ID", description = "Recupera uma localização pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Localização encontrada"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<LocalizacaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da localização", example = "1") 
            @PathVariable Long id) {
        Optional<LocalizacaoResponseDTO> localizacao = localizacaoService.buscarPorId(id);
        return localizacao.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar localização", description = "Atualiza os dados de uma localização existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Localização atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Novo nome já existe na mesma hierarquia")
    })
    public ResponseEntity<LocalizacaoResponseDTO> atualizar(
            @Parameter(description = "ID da localização", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody LocalizacaoRequestDTO request) {
        LocalizacaoResponseDTO response = localizacaoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar localização", description = "Remove uma localização do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Localização deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada"),
        @ApiResponse(responseCode = "409", description = "Localização possui sublocalizações ou ativos vinculados")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da localização", example = "1") 
            @PathVariable Long id) {
        localizacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}