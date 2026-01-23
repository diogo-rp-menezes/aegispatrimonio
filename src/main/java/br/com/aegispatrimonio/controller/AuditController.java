package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.EntityRevisionDTO;
import br.com.aegispatrimonio.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/ativos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<EntityRevisionDTO<AtivoDTO>>> getAtivoHistory(@PathVariable Long id) {
        List<EntityRevisionDTO<AtivoDTO>> history = auditService.getAtivoHistory(id);
        return ResponseEntity.ok(history);
    }
}
