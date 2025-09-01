package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.service.DepartamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @PostMapping
    public ResponseEntity<DepartamentoResponseDTO> criar(@Valid @RequestBody DepartamentoRequestDTO request) {
        DepartamentoResponseDTO response = departamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DepartamentoResponseDTO>> listarTodos() {
        List<DepartamentoResponseDTO> departamentos = departamentoService.listarTodos();
        return ResponseEntity.ok(departamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        DepartamentoResponseDTO departamento = departamentoService.buscarPorId(id);
        return ResponseEntity.ok(departamento);
    }

    @GetMapping("/filial/{filialId}")
    public ResponseEntity<List<DepartamentoResponseDTO>> listarPorFilial(@PathVariable Long filialId) {
        List<DepartamentoResponseDTO> departamentos = departamentoService.listarPorFilial(filialId);
        return ResponseEntity.ok(departamentos);
    }

    @GetMapping("/centro-custo/{centroCusto}")
    public ResponseEntity<DepartamentoResponseDTO> buscarPorCentroCusto(@PathVariable String centroCusto) {
        DepartamentoResponseDTO departamento = departamentoService.buscarPorCentroCusto(centroCusto);
        return ResponseEntity.ok(departamento);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<DepartamentoResponseDTO>> buscarPorNome(@RequestParam String nome) {
        List<DepartamentoResponseDTO> departamentos = departamentoService.buscarPorNome(nome);
        return ResponseEntity.ok(departamentos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody DepartamentoRequestDTO request) {
        DepartamentoResponseDTO response = departamentoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        departamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}