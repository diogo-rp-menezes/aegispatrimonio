package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.GroupCreateDTO;
import br.com.aegispatrimonio.dto.GroupDTO;
import br.com.aegispatrimonio.dto.GroupUpdateDTO;
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
@RequestMapping("/api/v1/groups")
@Tag(name = "Groups", description = "Gerenciamento de Grupos de Acesso")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class GroupController {

    private final RbacManagementService rbacService;

    public GroupController(RbacManagementService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os grupos")
    public List<GroupDTO> listarTodos() {
        return rbacService.listarGrupos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca grupo por ID")
    public GroupDTO buscarPorId(@PathVariable Long id) {
        return rbacService.buscarGrupoPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo grupo")
    public GroupDTO criar(@Valid @RequestBody GroupCreateDTO dto) {
        return rbacService.criarGrupo(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um grupo existente")
    public GroupDTO atualizar(@PathVariable Long id, @Valid @RequestBody GroupUpdateDTO dto) {
        return rbacService.atualizarGrupo(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um grupo")
    public void deletar(@PathVariable Long id) {
        rbacService.deletarGrupo(id);
    }
}
