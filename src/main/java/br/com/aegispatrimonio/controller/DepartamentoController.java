package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.service.DepartamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Departamentos.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar departamentos.
 */
@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    /**
     * Lista todos os departamentos cadastrados.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de DepartamentoDTO.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<DepartamentoDTO> listarTodos() {
        return departamentoService.listarTodos();
    }

    /**
     * Busca um departamento específico pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do departamento a ser buscado.
     * @return O DepartamentoDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se o departamento não for encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public DepartamentoDTO buscarPorId(@PathVariable Long id) {
        return departamentoService.buscarPorId(id);
    }

    /**
     * Cria um novo departamento.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param departamentoCreateDTO O DTO contendo os dados para a criação do novo departamento.
     * @return O DepartamentoDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DepartamentoDTO criar(@RequestBody @Valid DepartamentoCreateDTO departamentoCreateDTO) {
        return departamentoService.criar(departamentoCreateDTO);
    }

    /**
     * Atualiza um departamento existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do departamento a ser atualizado.
     * @param departamentoUpdateDTO O DTO contendo os dados para a atualização.
     * @return O DepartamentoDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se o departamento não for encontrado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartamentoDTO atualizar(@PathVariable Long id, @RequestBody @Valid DepartamentoUpdateDTO departamentoUpdateDTO) {
        return departamentoService.atualizar(id, departamentoUpdateDTO);
    }

    /**
     * Deleta um departamento.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do departamento a ser deletado.
     * @throws jakarta.persistence.EntityNotFoundException se o departamento não for encontrado.
     * @throws IllegalStateException se existirem pessoas associadas a este departamento.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        departamentoService.deletar(id);
    }
}
