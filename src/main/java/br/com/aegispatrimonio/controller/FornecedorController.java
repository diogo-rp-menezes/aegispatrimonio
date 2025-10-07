package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fornecedores") // REMOVIDO o /api
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public List<FornecedorDTO> listarTodos() {
        return fornecedorService.listarTodos();
    }

    @GetMapping("/{id}")
    public FornecedorDTO buscarPorId(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorDTO criar(@RequestBody @Valid FornecedorCreateDTO fornecedorCreateDTO) {
        return fornecedorService.criar(fornecedorCreateDTO);
    }

    @PutMapping("/{id}")
    public FornecedorDTO atualizar(@PathVariable Long id, @RequestBody @Valid FornecedorUpdateDTO fornecedorUpdateDTO) {
        return fornecedorService.atualizar(id, fornecedorUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
    }
}
