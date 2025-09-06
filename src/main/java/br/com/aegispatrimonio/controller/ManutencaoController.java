package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import br.com.aegispatrimonio.service.ManutencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/manutencoes")
@RequiredArgsConstructor
public class ManutencaoController {

    private final ManutencaoService manutencaoService;

    @PostMapping
    public ResponseEntity<ManutencaoResponseDTO> criar(@Valid @RequestBody ManutencaoRequestDTO request) {
        ManutencaoResponseDTO response = manutencaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ManutencaoResponseDTO>> listarTodos() {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarTodos();
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<ManutencaoResponseDTO> manutencao = manutencaoService.buscarPorId(id);
        return manutencao.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorAtivo(@PathVariable Long ativoId) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorAtivo(ativoId);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorStatus(@PathVariable StatusManutencao status) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorStatus(status);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorTipo(@PathVariable TipoManutencao tipo) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorTipo(tipo);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPendentes() {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPendentes();
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/ativo/{ativoId}/custo-total")
    public ResponseEntity<BigDecimal> obterCustoTotalPorAtivo(@PathVariable Long ativoId) {
        BigDecimal custoTotal = manutencaoService.obterCustoTotalManutencaoPorAtivo(ativoId);
        return ResponseEntity.ok(custoTotal);
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<ManutencaoResponseDTO> aprovarManutencao(@PathVariable Long id) {
        ManutencaoResponseDTO response = manutencaoService.aprovarManutencao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<ManutencaoResponseDTO> iniciarManutencao(
            @PathVariable Long id,
            @RequestParam Long tecnicoResponsavelId) {
        ManutencaoResponseDTO response = manutencaoService.iniciarManutencao(id, tecnicoResponsavelId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/concluir")
    public ResponseEntity<ManutencaoResponseDTO> concluirManutencao(
            @PathVariable Long id,
            @RequestParam String descricaoServico,
            @RequestParam BigDecimal custoReal,
            @RequestParam Integer tempoExecucaoMinutos) {
        ManutencaoResponseDTO response = manutencaoService.concluirManutencao(id, descricaoServico, custoReal, tempoExecucaoMinutos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ManutencaoResponseDTO> cancelarManutencao(
            @PathVariable Long id,
            @RequestParam String motivo) {
        ManutencaoResponseDTO response = manutencaoService.cancelarManutencao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        manutencaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}