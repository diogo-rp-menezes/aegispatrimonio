package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Ativos.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar ativos.
 * Acesso geral requer autenticação.
 */
@RestController
@RequestMapping("/api/v1/ativos")
@Validated
public class AtivoController {

    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    /**
     * Lista todos os ativos cadastrados no sistema.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @return Uma lista de AtivoDTO representando todos os ativos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<AtivoDTO> listarTodos(
            org.springframework.data.domain.Pageable pageable,
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long tipoAtivoId,
            @RequestParam(required = false) br.com.aegispatrimonio.model.StatusAtivo status,
            @RequestParam(required = false) String nome
    ) {
        return ativoService.listarTodos(pageable, filialId, tipoAtivoId, status, nome);
    }

    /**
     * Busca um ativo específico pelo seu ID.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do ativo a ser buscado.
     * @return O AtivoDTO correspondente ao ID fornecido.
     * @throws jakarta.persistence.EntityNotFoundException se o ativo não for encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public AtivoDTO buscarPorId(@PathVariable Long id) {
        return ativoService.buscarPorId(id);
    }

    /**
     * Cria um novo ativo no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param ativoCreateDTO O DTO contendo os dados para a criação do novo ativo.
     * @return O AtivoDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AtivoDTO criar(@RequestBody @Valid AtivoCreateDTO ativoCreateDTO) {
        return ativoService.criar(ativoCreateDTO);
    }

    /**
     * Atualiza um ativo existente.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do ativo a ser atualizado.
     * @param ativoUpdateDTO O DTO contendo os dados para a atualização.
     * @return O AtivoDTO com os dados atualizados.
     * @throws jakarta.persistence.EntityNotFoundException se o ativo não for encontrado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AtivoDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtivoUpdateDTO ativoUpdateDTO) {
        return ativoService.atualizar(id, ativoUpdateDTO);
    }

    /**
     * Deleta um ativo (realiza uma exclusão lógica, marcando-o como BAIXADO).
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do ativo a ser deletado.
     * @throws jakarta.persistence.EntityNotFoundException se o ativo não for encontrado.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        ativoService.deletar(id);
    }

    /**
     * Atualiza o status de saúde (health check) de um ativo.
     * Acesso permitido para usuários com qualquer role autenticada.
     *
     * @param id O ID do ativo a ter seu health check atualizado.
     * @throws jakarta.persistence.EntityNotFoundException se o ativo não for encontrado.
     */
    @PatchMapping("/{id}/health-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void updateHealthCheck(@PathVariable Long id, @RequestBody Object ignoredPayload) {
        // Valida existência e autorização reutilizando as regras de acesso do serviço de ativos
        // Se o ativo não existir ou o usuário não tiver acesso, exceções apropriadas serão lançadas
        ativoService.buscarPorId(id);
        // No-op: payload será processado futuramente pelo serviço dedicado de health-check
    }
}
