package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.service.TipoAtivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-ativo") // REMOVIDO o /api
public class TipoAtivoController {

    private final TipoAtivoService tipoAtivoService;

    public TipoAtivoController(TipoAtivoService tipoAtivoService) {
        this.tipoAtivoService = tipoAtivoService;
    }

    @GetMapping
    public List<TipoAtivoDTO> listarTodos() {
        return tipoAtivoService.listarTodos();
    }

    @GetMapping("/{id}")
    public TipoAtivoDTO buscarPorId(@PathVariable Long id) {
        return tipoAtivoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoAtivoDTO criar(@RequestBody @Valid TipoAtivoCreateDTO tipoAtivoCreateDTO) {
        return tipoAtivoService.criar(tipoAtivoCreateDTO);
    }

    @PutMapping("/{id}")
    public TipoAtivoDTO atualizar(@PathVariable Long id, @RequestBody @Valid TipoAtivoCreateDTO tipoAtivoUpdateDTO) {
        return tipoAtivoService.atualizar(id, tipoAtivoUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        tipoAtivoService.deletar(id);
    }
}
