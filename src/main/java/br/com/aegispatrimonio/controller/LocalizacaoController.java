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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/localizacoes")
@RequiredArgsConstructor
@Tag(name = "Localizações", description = "Operações relacionadas à gestão de localizações físicas dos ativos")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @PostMapping
    @Operation(summary = "Criar nova localização", description = "Cadastra uma nova localização física no sistema")
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
    @Operation(summary = "Listar todas as localizações", description = "Retorna uma lista de todas as localizações cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de localizações recuperada com sucesso")
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarTodos() {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarTodos();
        return ResponseEntity.ok(localizacoes);
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

    @GetMapping("/filial/{filialId}")
    @Operation(summary = "Listar localizações por filial", description = "Retorna localizações pertencentes a uma filial específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Localizações encontradas"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada")
    })
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarPorFilial(
            @Parameter(description = "ID da filial", example = "1") 
            @PathVariable Long filialId) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarPorFilial(filialId);
        return ResponseEntity.ok(localizacoes);
    }

    @GetMapping("/pai/{localizacaoPaiId}")
    @Operation(summary = "Listar localizações filhas", description = "Retorna localizações que são subordinadas a uma localização pai")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Localizações filhas encontradas"),
        @ApiResponse(responseCode = "404", description = "Localização pai não encontrada")
    })
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarPorLocalizacaoPai(
            @Parameter(description = "ID da localização pai", example = "1") 
            @PathVariable Long localizacaoPaiId) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarPorLocalizacaoPai(localizacaoPaiId);
        return ResponseEntity.ok(localizacoes);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar localizações por nome", description = "Retorna localizações cujo nome contenha o texto informado")
    @ApiResponse(responseCode = "200", description = "Localizações encontradas com sucesso")
    public ResponseEntity<List<LocalizacaoResponseDTO>> buscarPorNome(
            @Parameter(description = "Texto para busca no nome", example = "sala") 
            @RequestParam String nome) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.buscarPorNome(nome);
        return ResponseEntity.ok(localizacoes);
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