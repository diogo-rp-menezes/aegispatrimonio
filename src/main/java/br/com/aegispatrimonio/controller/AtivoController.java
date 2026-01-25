package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar as operações CRUD de Ativos.
 * Fornece endpoints para listar, buscar, criar, atualizar e deletar ativos.
 * Acesso protegido por RBAC Granular (Aegis Shield).
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
     * Se filialId for informado, verifica permissão de leitura nesse contexto.
     * Caso contrário, o serviço filtra os ativos permitidos ao usuário.
     *
     * @return Uma lista de AtivoDTO representando todos os ativos.
     */
    @GetMapping
    @PreAuthorize("#p1 == null or @permissionService.hasPermission(authentication, null, 'ATIVO', 'READ', #p1)")
    public Page<AtivoDTO> listarTodos(
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
     * Requer permissão READ no contexto da filial do ativo.
     *
     * @param id O ID do ativo a ser buscado.
     * @return O AtivoDTO correspondente ao ID fornecido.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'READ')")
    public AtivoDTO buscarPorId(@PathVariable Long id) {
        return ativoService.buscarPorId(id);
    }

    /**
     * Cria um novo ativo no sistema.
     * Requer permissão CREATE no contexto da filial informada.
     *
     * @param ativoCreateDTO O DTO contendo os dados para a criação do novo ativo.
     * @return O AtivoDTO recém-criado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.hasPermission(authentication, null, 'ATIVO', 'CREATE', #p0.filialId())")
    public AtivoDTO criar(@RequestBody @Valid AtivoCreateDTO ativoCreateDTO) {
        return ativoService.criar(ativoCreateDTO);
    }

    /**
     * Atualiza um ativo existente.
     * Requer permissão UPDATE no contexto atual do ativo E no contexto da filial alvo.
     *
     * @param id O ID do ativo a ser atualizado.
     * @param ativoUpdateDTO O DTO contendo os dados para a atualização.
     * @return O AtivoDTO com os dados atualizados.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'UPDATE') and @permissionService.hasPermission(authentication, null, 'ATIVO', 'UPDATE', #p1.filialId())")
    public AtivoDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtivoUpdateDTO ativoUpdateDTO) {
        return ativoService.atualizar(id, ativoUpdateDTO);
    }

    /**
     * Deleta um ativo (realiza uma exclusão lógica, marcando-o como BAIXADO).
     * Requer permissão DELETE no contexto da filial do ativo.
     *
     * @param id O ID do ativo a ser deletado.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'DELETE')")
    public void deletar(@PathVariable Long id) {
        ativoService.deletar(id);
    }

    /**
     * Atualiza o status de saúde (health check) de um ativo.
     * Recebe dados completos de hardware e disco para análise preditiva.
     * Requer permissão UPDATE no contexto da filial do ativo.
     *
     * @param id O ID do ativo a ter seu health check atualizado.
     */
    @PatchMapping("/{id}/health-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'UPDATE')")
    public void updateHealthCheck(@PathVariable Long id, @RequestBody HealthCheckPayloadDTO payload) {
        ativoService.processarHealthCheck(id, payload);
    }

    /**
     * Recupera o histórico de saúde (ex: espaço em disco) do ativo.
     * Requer permissão READ no contexto da filial do ativo.
     *
     * @param id O ID do ativo.
     * @return Lista de AtivoHealthHistoryDTO.
     */
    @GetMapping("/{id}/health-history")
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'READ')")
    public List<AtivoHealthHistoryDTO> getHealthHistory(@PathVariable Long id) {
        return ativoService.getHealthHistory(id);
    }
}
