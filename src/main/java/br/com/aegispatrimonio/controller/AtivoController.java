package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.AtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.AtivoResponseDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.service.AtivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ativos")
@RequiredArgsConstructor
public class AtivoController {

    private final AtivoService ativoService;

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> criar(@Valid @RequestBody AtivoRequestDTO request) {
        AtivoResponseDTO response = ativoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ativoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patrimonio/{numeroPatrimonio}")
    public ResponseEntity<AtivoResponseDTO> buscarPorNumeroPatrimonio(@PathVariable String numeroPatrimonio) {
        return ativoService.buscarPorNumeroPatrimonio(numeroPatrimonio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<AtivoResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<AtivoResponseDTO> ativos = ativoService.listarTodos(pageable);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/tipo/{tipoAtivoId}")
    public ResponseEntity<List<AtivoResponseDTO>> listarPorTipo(@PathVariable Long tipoAtivoId) {
        List<AtivoResponseDTO> ativos = ativoService.listarPorTipo(tipoAtivoId);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/localizacao/{localizacaoId}")
    public ResponseEntity<List<AtivoResponseDTO>> listarPorLocalizacao(@PathVariable Long localizacaoId) {
        List<AtivoResponseDTO> ativos = ativoService.listarPorLocalizacao(localizacaoId);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AtivoResponseDTO>> listarPorStatus(@PathVariable StatusAtivo status) {
        List<AtivoResponseDTO> ativos = ativoService.listarPorStatus(status);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/valor")
    public ResponseEntity<List<AtivoResponseDTO>> buscarPorFaixaDeValor(
            @RequestParam BigDecimal valorMin,
            @RequestParam BigDecimal valorMax) {
        List<AtivoResponseDTO> ativos = ativoService.buscarPorFaixaDeValor(valorMin, valorMax);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/verificar-patrimonio/{numeroPatrimonio}")
    public ResponseEntity<Boolean> verificarNumeroPatrimonio(@PathVariable String numeroPatrimonio) {
        boolean existe = ativoService.existePorNumeroPatrimonio(numeroPatrimonio);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtivoRequestDTO request) {
        AtivoResponseDTO response = ativoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        ativoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}