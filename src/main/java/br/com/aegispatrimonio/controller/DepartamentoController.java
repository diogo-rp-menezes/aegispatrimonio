package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.service.DepartamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public List<DepartamentoDTO> listarTodos() {
        return departamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public DepartamentoDTO buscarPorId(@PathVariable Long id) {
        return departamentoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartamentoDTO criar(@RequestBody @Valid DepartamentoCreateDTO departamentoCreateDTO) { // Anotação @Valid adicionada
        return departamentoService.criar(departamentoCreateDTO);
    }

    @PutMapping("/{id}")
    public DepartamentoDTO atualizar(@PathVariable Long id, @RequestBody @Valid DepartamentoUpdateDTO departamentoUpdateDTO) { // Anotação @Valid adicionada
        return departamentoService.atualizar(id, departamentoUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        departamentoService.deletar(id);
    }
}
