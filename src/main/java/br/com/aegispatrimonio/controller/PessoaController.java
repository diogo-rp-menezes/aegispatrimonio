package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<PessoaResponseDTO> criar(@Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PessoaResponseDTO>> listarTodos() {
        List<PessoaResponseDTO> pessoas = pessoaService.listarTodos();
        return ResponseEntity.ok(pessoas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PessoaResponseDTO> buscarPorId(@PathVariable Long id) {
        PessoaResponseDTO pessoa = pessoaService.buscarPorId(id);
        return ResponseEntity.ok(pessoa);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<PessoaResponseDTO> buscarPorEmail(@PathVariable String email) {
        PessoaResponseDTO pessoa = pessoaService.buscarPorEmail(email);
        return ResponseEntity.ok(pessoa);
    }

    @GetMapping("/departamento/{departamentoId}")
    public ResponseEntity<List<PessoaResponseDTO>> listarPorDepartamento(@PathVariable Long departamentoId) {
        List<PessoaResponseDTO> pessoas = pessoaService.listarPorDepartamento(departamentoId);
        return ResponseEntity.ok(pessoas);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PessoaResponseDTO>> buscarPorNome(@RequestParam String nome) {
        List<PessoaResponseDTO> pessoas = pessoaService.buscarPorNome(nome);
        return ResponseEntity.ok(pessoas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}