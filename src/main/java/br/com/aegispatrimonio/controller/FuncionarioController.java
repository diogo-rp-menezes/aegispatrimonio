package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Funcionários e seus respectivos Usuários.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar funcionários.
 */
@RestController
@RequestMapping("/funcionarios") // Endpoint alterado para /funcionarios
@Validated
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    /**
     * Lista todos os funcionários cadastrados no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de FuncionarioDTO representando todos os funcionários ativos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<FuncionarioDTO> listarTodos() {
        return funcionarioService.listarTodos();
    }

    /**
     * Busca um funcionário específico pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do funcionário a ser buscado.
     * @return O FuncionarioDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se o funcionário não for encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public FuncionarioDTO buscarPorId(@PathVariable Long id) {
        return funcionarioService.buscarPorId(id);
    }

    /**
     * Cria um novo funcionário e seu usuário de sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param createDTO O DTO contendo os dados para a criação.
     * @return O FuncionarioDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FuncionarioDTO criar(@RequestBody @Valid FuncionarioCreateDTO createDTO) {
        return funcionarioService.criar(createDTO);
    }

    /**
     * Atualiza um funcionário existente e seu usuário de sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do funcionário a ser atualizado.
     * @param updateDTO O DTO contendo os dados para a atualização.
     * @return O FuncionarioDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se o funcionário não for encontrado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FuncionarioDTO atualizar(@PathVariable Long id, @RequestBody @Valid FuncionarioUpdateDTO updateDTO) {
        return funcionarioService.atualizar(id, updateDTO);
    }

    /**
     * Deleta um funcionário (realiza uma exclusão lógica, marcando-o como INATIVO).
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do funcionário a ser deletado.
     * @throws jakarta.persistence.EntityNotFoundException se o funcionário não for encontrado.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        funcionarioService.deletar(id);
    }
}
