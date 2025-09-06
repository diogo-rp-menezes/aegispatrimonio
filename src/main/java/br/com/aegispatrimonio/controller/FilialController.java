package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FilialRequestDTO;
import br.com.aegispatrimonio.dto.response.FilialResponseDTO;
import br.com.aegispatrimonio.service.FilialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/filiais")
@RequiredArgsConstructor
public class FilialController {

    private final FilialService filialService;

    @PostMapping
    public ResponseEntity<FilialResponseDTO> criar(@Valid @RequestBody FilialRequestDTO request) {
        FilialResponseDTO response = filialService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FilialResponseDTO>> listarTodos() {
        List<FilialResponseDTO> filiais = filialService.listarTodos();
        return ResponseEntity.ok(filiais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilialResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorId(id);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<FilialResponseDTO> buscarPorCodigo(@PathVariable String codigo) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorCodigo(codigo);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<FilialResponseDTO> buscarPorNome(@PathVariable String nome) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorNome(nome);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<FilialResponseDTO>> buscarPorNomeContendo(@RequestParam String nome) {
        List<FilialResponseDTO> filiais = filialService.buscarPorNomeContendo(nome);
        return ResponseEntity.ok(filiais);
    }

    @GetMapping("/verificar-codigo/{codigo}")
    public ResponseEntity<Boolean> verificarCodigo(@PathVariable String codigo) {
        boolean existe = filialService.existePorCodigo(codigo);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilialResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FilialRequestDTO request) {
        FilialResponseDTO response = filialService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        filialService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}