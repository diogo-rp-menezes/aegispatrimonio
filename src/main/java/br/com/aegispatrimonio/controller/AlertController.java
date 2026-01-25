package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.service.AlertNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertNotificationService alertService;

    public AlertController(AlertNotificationService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<Alerta> getRecentAlerts() {
        return alertService.getRecentAlerts();
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
    }
}
