package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FilialCreateDTO;
import br.com.aegispatrimonio.dto.FilialDTO;
import br.com.aegispatrimonio.dto.FilialUpdateDTO;
import br.com.aegispatrimonio.service.FilialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.web.server.ResponseStatusException; // Importar ResponseStatusException

import java.util.List;
import java.util.Optional; // Importar Optional

/**
 * Controller para gerenciar as operações CRUD de Filiais.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar filiais.
 */
@RestController
@RequestMapping("/filiais")
@Validated // Ativa a validação para os parâmetros de método nesta classe
public class FilialController {

    private final FilialService filialService;

    public FilialController(FilialService filialService) {
        this.filialService = filialService;
    }

    /**
     * Lista todas as filiais cadastradas no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de FilialDTO representando todas as filiais ativas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<FilialDTO> listarTodos() {
        return filialService.listarTodos();
    }

    /**
     * Busca uma filial específica pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID da filial a ser buscada.
     * @return O FilialDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se a filial não for encontrada.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<FilialDTO> buscarPorId(@PathVariable Long id) { // Alterado o tipo de retorno
        Optional<FilialDTO> filial = filialService.buscarPorId(id); // O serviço retorna Optional
        return filial.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Cria uma nova filial no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param filialCreateDTO O DTO contendo os dados para a criação da nova filial.
     * @return O FilialDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FilialDTO criar(@RequestBody @Valid FilialCreateDTO filialCreateDTO) {
        return filialService.criar(filialCreateDTO);
    }

    /**
     * Atualiza uma filial existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID da filial a ser atualizada.
     * @param filialUpdateDTO O DTO contendo os dados para a atualização.
     * @return O FilialDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se a filial não for encontrada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilialDTO> atualizar(@PathVariable Long id, @RequestBody @Valid FilialUpdateDTO filialUpdateDTO) {
        Optional<FilialDTO> filialAtualizada = filialService.atualizar(id, filialUpdateDTO);
        return filialAtualizada.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deleta uma filial (realiza uma exclusão lógica, marcando-a como INATIVO).
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID da filial a ser deletada.
     * @throws jakarta.persistence.EntityNotFoundException se a filial não for encontrada.
     * @throws IllegalStateException se existirem ativos ou pessoas associadas a esta filial.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        filialService.deletar(id);
    }
}
