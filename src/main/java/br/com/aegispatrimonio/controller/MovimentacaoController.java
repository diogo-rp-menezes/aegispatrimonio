package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import br.com.aegispatrimonio.service.MovimentacaoService;
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
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping
    public ResponseEntity<MovimentacaoResponseDTO> criar(@Valid @RequestBody MovimentacaoRequestDTO request) {
        MovimentacaoResponseDTO response = movimentacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarTodos() {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarTodos();
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<MovimentacaoResponseDTO> movimentacao = movimentacaoService.buscarPorId(id);
        return movimentacao.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorAtivo(@PathVariable Long ativoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorAtivo(ativoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorStatus(@PathVariable StatusMovimentacao status) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorStatus(status);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/pessoa-destino/{pessoaDestinoId}")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorPessoaDestino(@PathVariable Long pessoaDestinoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorPessoaDestino(pessoaDestinoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/localizacao-destino/{localizacaoDestinoId}")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarPorLocalizacaoDestino(@PathVariable Long localizacaoDestinoId) {
        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarPorLocalizacaoDestino(localizacaoDestinoId);
        return ResponseEntity.ok(movimentacoes);
    }

    @PostMapping("/{id}/efetivar")
    public ResponseEntity<MovimentacaoResponseDTO> efetivarMovimentacao(@PathVariable Long id) {
        MovimentacaoResponseDTO response = movimentacaoService.efetivarMovimentacao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<MovimentacaoResponseDTO> cancelarMovimentacao(
            @PathVariable Long id,
            @RequestParam String motivo) {
        MovimentacaoResponseDTO response = movimentacaoService.cancelarMovimentacao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        movimentacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}