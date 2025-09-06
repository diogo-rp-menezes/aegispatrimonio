package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.TipoAtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.TipoAtivoResponseDTO;
import br.com.aegispatrimonio.service.TipoAtivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tipos-ativo")
@RequiredArgsConstructor
public class TipoAtivoController {

    private final TipoAtivoService tipoAtivoService;

    @PostMapping
    public ResponseEntity<TipoAtivoResponseDTO> criar(@Valid @RequestBody TipoAtivoRequestDTO request) {
        TipoAtivoResponseDTO response = tipoAtivoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TipoAtivoResponseDTO>> listarTodos() {
        List<TipoAtivoResponseDTO> tiposAtivo = tipoAtivoService.listarTodos();
        return ResponseEntity.ok(tiposAtivo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoAtivoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<TipoAtivoResponseDTO> tipoAtivo = tipoAtivoService.buscarPorId(id);
        return tipoAtivo.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<TipoAtivoResponseDTO> buscarPorNome(@PathVariable String nome) {
        Optional<TipoAtivoResponseDTO> tipoAtivo = tipoAtivoService.buscarPorNome(nome);
        return tipoAtivo.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/verificar-nome/{nome}")
    public ResponseEntity<Boolean> verificarNome(@PathVariable String nome) {
        boolean existe = tipoAtivoService.existePorNome(nome);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoAtivoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoAtivoRequestDTO request) {
        TipoAtivoResponseDTO response = tipoAtivoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tipoAtivoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}