package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.TipoAtivoCreateDTO;
import br.com.aegispatrimonio.dto.TipoAtivoDTO;
import br.com.aegispatrimonio.service.TipoAtivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Tipos de Ativo (ex: Notebook, Cadeira, Monitor).
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar tipos de ativo.
 */
@RestController
@RequestMapping("/api/v1/tipos-ativos")
public class TipoAtivoController {

    private final TipoAtivoService tipoAtivoService;

    public TipoAtivoController(TipoAtivoService tipoAtivoService) {
        this.tipoAtivoService = tipoAtivoService;
    }

    /**
     * Lista todos os tipos de ativo cadastrados no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de TipoAtivoDTO representando todos os tipos de ativo.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<TipoAtivoDTO> listarTodos() {
        return tipoAtivoService.listarTodos();
    }

    /**
     * Busca um tipo de ativo específico pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do tipo de ativo a ser buscado.
     * @return O TipoAtivoDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se o tipo de ativo não for encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public TipoAtivoDTO buscarPorId(@PathVariable Long id) {
        return tipoAtivoService.buscarPorId(id);
    }

    /**
     * Cria um novo tipo de ativo no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param tipoAtivoCreateDTO O DTO contendo os dados para a criação do novo tipo de ativo.
     * @return O TipoAtivoDTO recém-criado.
     * @throws IllegalArgumentException se já existir um tipo de ativo com o mesmo nome.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TipoAtivoDTO criar(@RequestBody @Valid TipoAtivoCreateDTO tipoAtivoCreateDTO) {
        return tipoAtivoService.criar(tipoAtivoCreateDTO);
    }

    /**
     * Atualiza um tipo de ativo existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do tipo de ativo a ser atualizado.
     * @param tipoAtivoUpdateDTO O DTO contendo os dados para a atualização.
     * @return O TipoAtivoDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se o tipo de ativo não for encontrado.
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outro tipo de ativo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TipoAtivoDTO atualizar(@PathVariable Long id, @RequestBody @Valid TipoAtivoCreateDTO tipoAtivoUpdateDTO) {
        return tipoAtivoService.atualizar(id, tipoAtivoUpdateDTO);
    }

    /**
     * Deleta um tipo de ativo.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do tipo de ativo a ser deletado.
     * @throws jakarta.persistence.EntityNotFoundException se o tipo de ativo não for encontrado.
     * @throws IllegalStateException se existirem ativos associados a este tipo.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        tipoAtivoService.deletar(id);
    }
}
