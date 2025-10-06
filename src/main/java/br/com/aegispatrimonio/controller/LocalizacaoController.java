package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.service.LocalizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localizacoes")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    public LocalizacaoController(LocalizacaoService localizacaoService) {
        this.localizacaoService = localizacaoService;
    }

    @GetMapping
    public List<LocalizacaoDTO> listarTodos() {
        return localizacaoService.listarTodos();
    }

    @GetMapping("/{id}")
    public LocalizacaoDTO buscarPorId(@PathVariable Long id) {
        return localizacaoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalizacaoDTO criar(@RequestBody @Valid LocalizacaoCreateDTO localizacaoCreateDTO) { // Anotação @Valid adicionada
        return localizacaoService.criar(localizacaoCreateDTO);
    }

    @PutMapping("/{id}")
    public LocalizacaoDTO atualizar(@PathVariable Long id, @RequestBody @Valid LocalizacaoUpdateDTO localizacaoUpdateDTO) { // Anotação @Valid adicionada
        return localizacaoService.atualizar(id, localizacaoUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        localizacaoService.deletar(id);
    }
}
