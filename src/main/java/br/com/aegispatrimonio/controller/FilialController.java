package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.service.FilialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/filiais") // REMOVIDO o /api
public class FilialController {

    private final FilialService filialService;

    public FilialController(FilialService filialService) {
        this.filialService = filialService;
    }

    @GetMapping
    public List<FilialDTO> listarTodos() {
        return filialService.listarTodos();
    }

    @GetMapping("/{id}")
    public FilialDTO buscarPorId(@PathVariable Long id) {
        return filialService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilialDTO criar(@RequestBody @Valid FilialCreateDTO filialCreateDTO) {
        return filialService.criar(filialCreateDTO);
    }

    @PutMapping("/{id}")
    public FilialDTO atualizar(@PathVariable Long id, @RequestBody @Valid FilialUpdateDTO filialUpdateDTO) {
        return filialService.atualizar(id, filialUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        filialService.deletar(id);
    }
}
