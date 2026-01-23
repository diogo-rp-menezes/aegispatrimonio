package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.EntityRevisionDTO;
import br.com.aegispatrimonio.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/ativos/{id}")
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'READ')")
    public ResponseEntity<List<EntityRevisionDTO<AtivoDTO>>> getAtivoHistory(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getAtivoHistory(id));
    }
}
