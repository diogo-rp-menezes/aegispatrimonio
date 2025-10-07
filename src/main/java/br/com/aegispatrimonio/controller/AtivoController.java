package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckDTO;
import br.com.aegispatrimonio.service.AtivoService;
import br.com.aegispatrimonio.service.HealthCheckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ativos") // REMOVIDO o /api
public class AtivoController {

    private final AtivoService ativoService;
    private final HealthCheckService healthCheckService;

    public AtivoController(AtivoService ativoService, HealthCheckService healthCheckService) {
        this.ativoService = ativoService;
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public List<AtivoDTO> listarTodos() {
        return ativoService.listarTodos();
    }

    @GetMapping("/{id}")
    public AtivoDTO buscarPorId(@PathVariable Long id) {
        return ativoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtivoDTO criar(@RequestBody @Valid AtivoCreateDTO ativoCreateDTO) {
        return ativoService.criar(ativoCreateDTO);
    }

    @PutMapping("/{id}")
    public AtivoDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtivoUpdateDTO ativoUpdateDTO) {
        return ativoService.atualizar(id, ativoUpdateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        ativoService.deletar(id);
    }

    @PatchMapping("/{id}/health-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateHealthCheck(@PathVariable Long id, @RequestBody @Valid HealthCheckDTO healthCheckDTO) {
        healthCheckService.updateHealthCheck(id, healthCheckDTO);
    }
}
