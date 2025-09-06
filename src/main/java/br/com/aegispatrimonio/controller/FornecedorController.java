package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FornecedorRequestDTO;
import br.com.aegispatrimonio.dto.response.FornecedorResponseDTO;
import br.com.aegispatrimonio.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping
    public ResponseEntity<FornecedorResponseDTO> criar(@Valid @RequestBody FornecedorRequestDTO request) {
        FornecedorResponseDTO response = fornecedorService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FornecedorResponseDTO>> listarTodos() {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.listarTodos();
        return ResponseEntity.ok(fornecedores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<FornecedorResponseDTO> fornecedor = fornecedorService.buscarPorId(id);
        return fornecedor.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<FornecedorResponseDTO> buscarPorNome(@PathVariable String nome) {
        Optional<FornecedorResponseDTO> fornecedor = fornecedorService.buscarPorNome(nome);
        return fornecedor.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<FornecedorResponseDTO>> buscarPorEmail(@PathVariable String email) {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.buscarPorEmail(email);
        return ResponseEntity.ok(fornecedores);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<FornecedorResponseDTO>> buscarPorNomeContendo(@RequestParam String nome) {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.buscarPorNomeContendo(nome);
        return ResponseEntity.ok(fornecedores);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FornecedorRequestDTO request) {
        FornecedorResponseDTO response = fornecedorService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}