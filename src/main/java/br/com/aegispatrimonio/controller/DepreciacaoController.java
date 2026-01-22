package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.service.DepreciacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller para gerenciar operações relacionadas à depreciação de ativos.
 * Fornece endpoints para calcular e recalcular valores de depreciação.
 * Acesso restrito a usuários com a role 'ADMIN'.
 */
@RestController
@RequestMapping("/depreciacao")
@RequiredArgsConstructor
@Tag(name = "Depreciação", description = "Operações relacionadas ao cálculo e gestão de depreciação de ativos")
@SecurityRequirement(name = "bearerAuth") // Indica que todos os endpoints deste controller requerem JWT
public class DepreciacaoController {

    private final DepreciacaoService depreciacaoService;

    /**
     * Inicia um processo de recálculo da depreciação para todos os ativos elegíveis no sistema.
     * Esta é uma operação que pode ser de longa duração, dependendo do volume de dados.
     *
     * @return ResponseEntity com status 200 OK e uma mensagem de confirmação.
     */
    @PostMapping("/recalcular-todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recalcular depreciação de todos os ativos",
               description = "Inicia o recálculo completo da depreciação para todos os ativos do sistema. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recálculo iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "500", description = "Erro interno no processamento")
    })
    public ResponseEntity<String> recalcularDepreciacaoTodos() {
        depreciacaoService.recalcularDepreciacaoTodosAtivos();
        return ResponseEntity.ok("Recálculo de depreciação iniciado");
    }

    /**
     * Inicia um processo de recálculo da depreciação para um ativo específico.
     *
     * @param ativoId O ID do ativo para o qual a depreciação será recalculada.
     * @return ResponseEntity com status 200 OK e uma mensagem de confirmação.
     * @throws br.com.aegispatrimonio.exception.ResourceNotFoundException se o ativo não for encontrado.
     */
    @PostMapping("/recalcular/{ativoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recalcular depreciação de um ativo específico",
               description = "Inicia o recálculo completo da depreciação para um ativo específico. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recálculo do ativo iniciado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno no processamento")
    })
    public ResponseEntity<String> recalcularDepreciacaoAtivo(
            @Parameter(description = "ID do ativo para recálculo", example = "1", required = true)
            @PathVariable Long ativoId) {
        depreciacaoService.recalcularDepreciacaoCompleta(ativoId);
        return ResponseEntity.ok("Recálculo de depreciação do ativo iniciado");
    }

    /**
     * Calcula e retorna o valor da depreciação mensal para um ativo específico.
     *
     * @param ativoId O ID do ativo para o qual a depreciação mensal será calculada.
     * @return ResponseEntity com status 200 OK e o valor da depreciação mensal no corpo da resposta.
     * @throws br.com.aegispatrimonio.exception.ResourceNotFoundException se o ativo não for encontrado.
     */
    @GetMapping("/calcular-mensal/{ativoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Calcular depreciação mensal de um ativo",
               description = "Calcula o valor mensal de depreciação para um ativo específico. Acesso restrito a ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de acesso (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado"),
        @ApiResponse(responseCode = "400", description = "Ativo não possui dados para cálculo de depreciação")
    })
    public ResponseEntity<BigDecimal> calcularDepreciacaoMensal(
            @Parameter(description = "ID do ativo para cálculo", example = "1", required = true)
            @PathVariable Long ativoId) {
        BigDecimal depreciacao = depreciacaoService.calcularDepreciacaoMensal(ativoId);
        return ResponseEntity.ok(depreciacao);
    }
}
