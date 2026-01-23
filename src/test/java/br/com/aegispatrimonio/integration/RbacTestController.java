package br.com.aegispatrimonio.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/rbac")
public class RbacTestController {

    @GetMapping("/read")
    @PreAuthorize("hasPermission(#id, 'ATIVO', {'READ', #filialId})")
    public ResponseEntity<String> read(@P("id") @RequestParam Long id, @P("filialId") @RequestParam Long filialId) {
        return ResponseEntity.ok("Allowed");
    }
}
