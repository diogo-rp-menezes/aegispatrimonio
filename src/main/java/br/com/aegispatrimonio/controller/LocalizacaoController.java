package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.LocalizacaoResponseDTO;
import br.com.aegispatrimonio.service.LocalizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/localizacoes")
@RequiredArgsConstructor
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @PostMapping
    public ResponseEntity<LocalizacaoResponseDTO> criar(@Valid @RequestBody LocalizacaoRequestDTO request) {
        LocalizacaoResponseDTO response = localizacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarTodos() {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarTodos();
        return ResponseEntity.ok(localizacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalizacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<LocalizacaoResponseDTO> localizacao = localizacaoService.buscarPorId(id);
        return localizacao.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filial/{filialId}")
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarPorFilial(@PathVariable Long filialId) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarPorFilial(filialId);
        return ResponseEntity.ok(localizacoes);
    }

    @GetMapping("/pai/{localizacaoPaiId}")
    public ResponseEntity<List<LocalizacaoResponseDTO>> listarPorLocalizacaoPai(@PathVariable Long localizacaoPaiId) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.listarPorLocalizacaoPai(localizacaoPaiId);
        return ResponseEntity.ok(localizacoes);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LocalizacaoResponseDTO>> buscarPorNome(@RequestParam String nome) {
        List<LocalizacaoResponseDTO> localizacoes = localizacaoService.buscarPorNome(nome);
        return ResponseEntity.ok(localizacoes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalizacaoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LocalizacaoRequestDTO request) {
        LocalizacaoResponseDTO response = localizacaoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        localizacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}