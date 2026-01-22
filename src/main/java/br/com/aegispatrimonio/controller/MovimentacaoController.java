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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ProblemDetail; // Importar ProblemDetail para erros

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller para gerenciar o ciclo de vida e as operações relacionadas a Movimentações de Ativos.
 * Fornece endpoints para criar, listar, buscar, efetivar, cancelar e deletar movimentações.
 */
@RestController
@RequestMapping("/movimentacoes")
@RequiredArgsConstructor
@Tag(name = "Movimentações", description = "Operações relacionadas à gestão de movimentações de ativos")
@SecurityRequirement(name = "bearerAuth")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Cria uma nova movimentação", description = "Cria uma nova solicitação de movimentação de ativo. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou conflito de regras de negócio", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (ex: movimentação pendente para o ativo)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MovimentacaoResponseDTO> criar(@Valid @RequestBody MovimentacaoRequestDTO request) {
        MovimentacaoResponseDTO response = movimentacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista todas as movimentações", description = "Retorna uma lista paginada de todas as movimentações de ativos. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarTodos(
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findAll(pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Busca uma movimentação por ID", description = "Retorna os detalhes de uma movimentação específica. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentação encontrada", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MovimentacaoResponseDTO> buscarPorId(@Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        Optional<MovimentacaoResponseDTO> movimentacao = movimentacaoService.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações por ID do ativo", description = "Retorna uma lista paginada de movimentações para um ativo específico. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ativo não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") @PathVariable Long ativoId,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByAtivoId(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações por status", description = "Retorna uma lista paginada de movimentações com um status específico. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorStatus(
            @Parameter(description = "Status da movimentação", example = "PENDENTE") @PathVariable StatusMovimentacao status,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByStatus(status, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/funcionario-destino/{funcionarioDestinoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações por funcionário de destino", description = "Retorna uma lista paginada de movimentações com um funcionário de destino específico. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorFuncionarioDestino(
            @Parameter(description = "ID do funcionário de destino", example = "1") @PathVariable Long funcionarioDestinoId,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByFuncionarioDestinoId(funcionarioDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/localizacao-destino/{localizacaoDestinoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações por localização de destino", description = "Retorna uma lista paginada de movimentações com uma localização de destino específica. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorLocalizacaoDestino(
            @Parameter(description = "ID da localização de destino", example = "1") @PathVariable Long localizacaoDestinoId,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações por período", description = "Retorna uma lista paginada de movimentações dentro de um período de datas. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datas inválidas", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorPeriodo(
            @Parameter(description = "Data de início do período (YYYY-MM-DD)", example = "2023-01-01") @RequestParam LocalDate startDate,
            @Parameter(description = "Data de fim do período (YYYY-MM-DD)", example = "2023-12-31") @RequestParam LocalDate endDate,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByPeriodo(startDate, endDate, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/ativo/pendentes/{ativoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista movimentações pendentes por ativo", description = "Retorna uma lista paginada de movimentações pendentes para um ativo específico. Acesso permitido para ADMIN e USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ativo não encontrado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPendentesPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") @PathVariable Long ativoId,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findMovimentacoesPendentesPorAtivo(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @PostMapping("/efetivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Efetiva uma movimentação", description = "Efetiva uma movimentação pendente, atualizando a localização e o responsável do ativo. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentação efetivada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (movimentação não está pendente)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MovimentacaoResponseDTO> efetivarMovimentacao(@Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancela uma movimentação", description = "Cancela uma movimentação pendente. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentação cancelada com sucesso", content = @Content(schema = @Schema(implementation = MovimentacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (movimentação não está pendente)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MovimentacaoResponseDTO> cancelarMovimentacao(
            @Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id,
            @Parameter(description = "Motivo do cancelamento") @RequestBody String motivo) {
        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma movimentação", description = "Deleta uma movimentação pendente do sistema. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movimentação deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito (movimentação não está pendente)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deletar(@Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        movimentacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
