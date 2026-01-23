package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.RoleCreateDTO;
import br.com.aegispatrimonio.dto.RoleDTO;
import br.com.aegispatrimonio.dto.RoleUpdateDTO;
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
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Gerenciamento de Roles (Perfis de Acesso)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RbacManagementService rbacService;

    public RoleController(RbacManagementService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as roles")
    public List<RoleDTO> listarTodos() {
        return rbacService.listarRoles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca role por ID")
    public RoleDTO buscarPorId(@PathVariable Long id) {
        return rbacService.buscarRolePorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova role")
    public RoleDTO criar(@Valid @RequestBody RoleCreateDTO dto) {
        return rbacService.criarRole(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma role existente")
    public RoleDTO atualizar(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return rbacService.atualizarRole(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma role")
    public void deletar(@PathVariable Long id) {
        rbacService.deletarRole(id);
    }
}
