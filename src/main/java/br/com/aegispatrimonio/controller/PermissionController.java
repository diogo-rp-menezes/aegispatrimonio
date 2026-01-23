package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.PermissionCreateDTO;
import br.com.aegispatrimonio.dto.PermissionDTO;
import br.com.aegispatrimonio.service.RbacManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Gerenciamento de Permissões Granulares")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final RbacManagementService rbacService;

    public PermissionController(RbacManagementService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as permissões")
    public List<PermissionDTO> listarTodos() {
        return rbacService.listarPermissoes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova permissão")
    public PermissionDTO criar(@Valid @RequestBody PermissionCreateDTO dto) {
        return rbacService.criarPermissao(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma permissão")
    public void deletar(@PathVariable Long id) {
        rbacService.deletarPermissao(id);
    }
}
