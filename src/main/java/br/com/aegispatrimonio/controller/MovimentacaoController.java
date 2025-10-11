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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<MovimentacaoResponseDTO> criar(@Valid @RequestBody MovimentacaoRequestDTO request) {
        MovimentacaoResponseDTO response = movimentacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findAll(pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<MovimentacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<MovimentacaoResponseDTO> movimentacao = movimentacaoService.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorAtivo(
            @PathVariable Long ativoId,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByAtivoId(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorStatus(
            @PathVariable StatusMovimentacao status,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByStatus(status, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    // CORREÇÃO: Endpoint e método atualizados para Funcionario
    @GetMapping("/funcionario-destino/{funcionarioDestinoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorFuncionarioDestino(
            @PathVariable Long funcionarioDestinoId,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByFuncionarioDestinoId(funcionarioDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/localizacao-destino/{localizacaoDestinoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorLocalizacaoDestino(
            @PathVariable Long localizacaoDestinoId,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPorPeriodo(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findByPeriodo(startDate, endDate, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/ativo/pendentes/{ativoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> listarPendentesPorAtivo(
            @PathVariable Long ativoId,
            @PageableDefault(size = 10, sort = "dataMovimentacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.findMovimentacoesPendentesPorAtivo(ativoId, pageable);
        return ResponseEntity.ok(movimentacoes);
    }

    @PostMapping("/efetivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimentacaoResponseDTO> efetivarMovimentacao(@PathVariable Long id) {
        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimentacaoResponseDTO> cancelarMovimentacao(
            @PathVariable Long id,
            @RequestBody String motivo) {
        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        movimentacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
