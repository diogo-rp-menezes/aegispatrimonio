package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.PessoaCreateDTO;
import br.com.aegispatrimonio.dto.PessoaDTO;
import br.com.aegispatrimonio.dto.PessoaUpdateDTO;
import br.com.aegispatrimonio.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pessoas") // REMOVIDO o /api
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @GetMapping
    public List<PessoaDTO> listarTodos() {
        return pessoaService.listarTodos();
    }

    @GetMapping("/{id}")
    public PessoaDTO buscarPorId(@PathVariable Long id) {
        return pessoaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaDTO criar(@RequestBody @Valid PessoaCreateDTO pessoaCreateDTO) {
        return pessoaService.criar(pessoaCreateDTO);
    }

    @PutMapping("/{id}")
    public PessoaDTO atualizar(@PathVariable Long id, @RequestBody @Valid PessoaUpdateDTO pessoaUpdateDTO) {
        return pessoaService.atualizar(id, pessoaUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        pessoaService.deletar(id);
    }
}
