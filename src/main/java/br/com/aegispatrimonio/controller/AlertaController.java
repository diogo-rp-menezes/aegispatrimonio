package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AlertaDTO;
import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.service.AlertNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
public class AlertaController {

    private final AlertNotificationService alertService;

    public AlertaController(AlertNotificationService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<AlertaDTO> listarAlertas(
            @PageableDefault(sort = "dataCriacao", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Boolean lido) {

        return alertService.listarAlertas(pageable, lido).map(this::toDTO);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<AlertaDTO> getRecentAlerts() {
        return alertService.getRecentAlerts().stream().map(this::toDTO).toList();
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
    }

    private AlertaDTO toDTO(Alerta alerta) {
        return new AlertaDTO(
                alerta.getId(),
                alerta.getAtivo().getId(),
                alerta.getAtivo().getNome(),
                alerta.getTipo(),
                alerta.getTitulo(),
                alerta.getMensagem(),
                alerta.getDataCriacao(),
                alerta.isLido(),
                alerta.getDataLeitura()
        );
    }
}
