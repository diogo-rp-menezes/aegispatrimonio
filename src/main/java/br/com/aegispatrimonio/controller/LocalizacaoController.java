package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.LocalizacaoCreateDTO;
import br.com.aegispatrimonio.dto.LocalizacaoDTO;
import br.com.aegispatrimonio.dto.LocalizacaoUpdateDTO;
import br.com.aegispatrimonio.service.LocalizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Localizações (ex: Sala de Reunião, Almoxarifado).
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar localizações.
 */
@RestController
@RequestMapping("/localizacoes")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    public LocalizacaoController(LocalizacaoService localizacaoService) {
        this.localizacaoService = localizacaoService;
    }

    /**
     * Lista todas as localizações cadastradas no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de LocalizacaoDTO representando todas as localizações ativas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<LocalizacaoDTO> listarTodos() {
        return localizacaoService.listarTodos();
    }

    /**
     * Busca uma localização específica pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID da localização a ser buscada.
     * @return O LocalizacaoDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se a localização não for encontrada.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public LocalizacaoDTO buscarPorId(@PathVariable Long id) {
        return localizacaoService.buscarPorId(id);
    }

    /**
     * Cria uma nova localização no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param localizacaoCreateDTO O DTO contendo os dados para a criação da nova localização.
     * @return O LocalizacaoDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LocalizacaoDTO criar(@RequestBody @Valid LocalizacaoCreateDTO localizacaoCreateDTO) {
        return localizacaoService.criar(localizacaoCreateDTO);
    }

    /**
     * Atualiza uma localização existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID da localização a ser atualizada.
     * @param localizacaoUpdateDTO O DTO contendo os dados para a atualização.
     * @return O LocalizacaoDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se a localização não for encontrada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LocalizacaoDTO atualizar(@PathVariable Long id, @RequestBody @Valid LocalizacaoUpdateDTO localizacaoUpdateDTO) {
        return localizacaoService.atualizar(id, localizacaoUpdateDTO);
    }

    /**
     * Deleta uma localização (realiza uma exclusão lógica, marcando-a como INATIVO).
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID da localização a ser deletada.
     * @throws jakarta.persistence.EntityNotFoundException se a localização não for encontrada.
     * @throws IllegalStateException se existirem ativos associados a esta localização.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        localizacaoService.deletar(id);
    }
}
