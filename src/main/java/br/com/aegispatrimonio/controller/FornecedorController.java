package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Fornecedores.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar fornecedores.
 */
@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    /**
     * Lista todos os fornecedores cadastrados no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de FornecedorDTO representando todos os fornecedores ativos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<FornecedorDTO> listarTodos() {
        return fornecedorService.listarTodos();
    }

    /**
     * Busca um fornecedor específico pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do fornecedor a ser buscado.
     * @return O FornecedorDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se o fornecedor não for encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public FornecedorDTO buscarPorId(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id);
    }

    /**
     * Cria um novo fornecedor no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param fornecedorCreateDTO O DTO contendo os dados para a criação do novo fornecedor.
     * @return O FornecedorDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FornecedorDTO criar(@RequestBody @Valid FornecedorCreateDTO fornecedorCreateDTO) {
        return fornecedorService.criar(fornecedorCreateDTO);
    }

    /**
     * Atualiza um fornecedor existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do fornecedor a ser atualizado.
     * @param fornecedorUpdateDTO O DTO contendo os dados para a atualização.
     * @return O FornecedorDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se o fornecedor não for encontrado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FornecedorDTO atualizar(@PathVariable Long id, @RequestBody @Valid FornecedorUpdateDTO fornecedorUpdateDTO) {
        return fornecedorService.atualizar(id, fornecedorUpdateDTO);
    }

    /**
     * Deleta um fornecedor (realiza uma exclusão lógica, marcando-o como INATIVO).
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do fornecedor a ser deletado.
     * @throws jakarta.persistence.EntityNotFoundException se o fornecedor não for encontrado.
     * @throws IllegalStateException se existirem ativos associados a este fornecedor.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
    }
}
