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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/movimentacoes")
@RequiredArgsConstructor
@Tag(name = "Movimentações", description = "Operações relacionadas à gestão de movimentações de ativos")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    // ✅ CORRETO - Verbo primeiro: criar
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

    // ✅ CORRETO - Verbo primeiro: listar todos
    @GetMapping
    @Operation(summary = "Listar todas as movimentações", description = "Retorna uma lista paginada de todas as movimentações")
    @ApiResponse(responseCode = "200", description = "Lista de movimentações recuperada com sucesso")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarTodos(
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findAll(pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: buscar
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

    // ✅ CORRETO - Verbo primeiro: listar por ativo
    @GetMapping("/ativo/{ativoId}")
    @Operation(summary = "Listar movimentações por ativo", description = "Retorna todas as movimentações de um ativo específico com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long ativoId,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByAtivoId(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: listar por status
    @GetMapping("/status/{status}")
    @Operation(summary = "Listar movimentações por status", description = "Retorna movimentações filtradas por status com paginação")
    @ApiResponse(responseCode = "200", description = "Movimentações encontradas com sucesso")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorStatus(
            @Parameter(description = "Status da movimentação", example = "PENDENTE") 
            @PathVariable StatusMovimentacao status,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByStatus(status, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: listar por pessoa destino
    @GetMapping("/pessoa-destino/{pessoaDestinoId}")
    @Operation(summary = "Listar movimentações por pessoa destino", description = "Retorna movimentações onde a pessoa é o destino com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorPessoaDestino(
            @Parameter(description = "ID da pessoa destino", example = "1") 
            @PathVariable Long pessoaDestinoId,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByPessoaDestinoId(pessoaDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: listar por localização destino
    @GetMapping("/localizacao-destino/{localizacaoDestinoId}")
    @Operation(summary = "Listar movimentações por localização destino", description = "Retorna movimentações onde a localização é o destino com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimentações encontradas"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorLocalizacaoDestino(
            @Parameter(description = "ID da localização destino", example = "1") 
            @PathVariable Long localizacaoDestinoId,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: listar por período
    @GetMapping("/periodo")
    @Operation(summary = "Listar movimentações por período", description = "Retorna movimentações em um período específico com paginação")
    @ApiResponse(responseCode = "200", description = "Movimentações encontradas con sucesso")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorPeriodo(
            @Parameter(description = "Data inicial do período", example = "2024-01-01") 
            @RequestParam LocalDate startDate,
            @Parameter(description = "Data final do período", example = "2024-12-31") 
            @RequestParam LocalDate endDate,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByPeriodo(startDate, endDate, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: listar pendentes por ativo
    @GetMapping("/ativo/pendentes/{ativoId}")
    @Operation(summary = "Listar movimentações pendentes por ativo", description = "Retorna movimentações pendentes de um ativo específico com paginação")
    @ApiResponse(responseCode = "200", description = "Movimentações pendentes encontradas")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPendentesPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long ativoId,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataMovimentacao,desc") Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findMovimentacoesPendentesPorAtivo(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // ✅ CORRETO - Verbo primeiro: efetivar
    @PostMapping("/efetivar/{id}")
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

    // ✅ CORRETO - Verbo primeiro: cancelar
    @PostMapping("/cancelar/{id}")
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

    // ✅ CORRETO - Verbo primeiro: deletar
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