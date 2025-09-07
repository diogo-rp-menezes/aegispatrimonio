package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import br.com.aegispatrimonio.service.MovimentacaoService;
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
@RequestMapping("/movimentacoes")
@RequiredArgsConstructor
@Tag(name = "Movimentações", description = "Operações relacionadas à gestão de movimentações de ativos")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping
    @Operation(summary = "Criar nova movimentação", description = "Solicita uma nova movimentação de ativo entre localizações ou pessoas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso", 
                    content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Ativo, localização ou pessoa não encontrada"),
        @ApiResponse(responseCode = "409", description = "Ativo já possui movimentação pendente")
    })
    public ResponseEntity<MovimentacaoResponseDTO> criar(@Valid @RequestBody MovimentacaoRequestDTO request) {
        MovimentacaoResponseDTO response = movimentacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as movimentações", description = "Retorna uma lista de todas as movimentações cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de movimentações recuperada com sucesso")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarTodos() {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarTodos();
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar movimentação por ID", description = "Recupera uma movimentação pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentação encontrada"),
        @ApiResponse(responseCode = "404", description = "Movimentação não encontrada")
    })
    public ResponseEntity<MovimentacaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da movimentação", example = "1") 
            @PathVariable Long id) {
        Optional<MovimentacaoResponseDTO> movimentacao = movimentacaoService.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    @Operation(summary = "Listar movimentações por ativo", description = "Retorna todas as movimentações de um ativo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long ativoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorAtivo(ativoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar movimentações por status", description = "Retorna movimentações filtradas por status")
    @ApiResponse(responseCode = "200", description = "Movimentações encontradas com sucesso")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorStatus(
            @Parameter(description = "Status da movimentação", example = "PENDENTE") 
            @PathVariable StatusMovimentacao status) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorStatus(status);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/pessoa-destino/{pessoaDestinoId}")
    @Operation(summary = "Listar movimentações por pessoa destino", description = "Retorna movimentações onde a pessoa é o destino")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorPessoaDestino(
            @Parameter(description = "ID da pessoa destino", example = "1") 
            @PathVariable Long pessoaDestinoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorPessoaDestino(pessoaDestinoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/localizacao-destino/{localizacaoDestinoId}")
    @Operation(summary = "Listar movimentações por localização destino", description = "Retorna movimentações onde a localização é o destino")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorLocalizacaoDestino(
            @Parameter(description = "ID da localização destino", example = "1") 
            @PathVariable Long localizacaoDestinoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorLocalizacaoDestino(localizacaoDestinoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @PostMapping("/{id}/efetivar")
    @Operation(summary = "Efetivar movimentação", description = "Efetiva uma movimentação pendente, realizando a transferência do ativo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentação efetivada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Movimentação não encontrada"),
        @ApiResponse(responseCode = "400", description = "Movimentação não está pendente")
    })
    public ResponseEntity<MovimentacaoResponseDTO> efetivarMovimentacao(
            @Parameter(description = "ID da movimentação", example = "1") 
            @PathVariable Long id) {
        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar movimentação", description = "Cancela uma movimentação pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentação cancelada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Movimentação não encontrada"),
        @ApiResponse(responseCode = "400", description = "Movimentação não está pendente")
    })
    public ResponseEntity<MovimentacaoResponseDTO> cancelarMovimentacao(
            @Parameter(description = "ID da movimentação", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Motivo do cancelamento", example = "Mudança de planejamento") 
            @RequestParam String motivo) {
        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar movimentação", description = "Remove uma movimentação do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movimentação deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Movimentação não encontrada"),
        @ApiResponse(responseCode = "400", description = "Movimentação não pode ser deletada devido ao status")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da movimentação", example = "1") 
            @PathVariable Long id) {
        movimentacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}