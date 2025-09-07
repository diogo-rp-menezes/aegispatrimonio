package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.service.DepreciacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/depreciacao")
@RequiredArgsConstructor
@Tag(name = "Depreciação", description = "Operações relacionadas ao cálculo e gestão de depreciação de ativos")
public class DepreciacaoController {

    private final DepreciacaoService depreciacaoService;

    @PostMapping("/recalcular-todos")
    @Operation(summary = "Recalcular depreciação de todos os ativos", 
               description = "Inicia o recálculo completo da depreciação para todos os ativos do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recálculo iniciado com sucesso", 
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no processamento")
    })
    public ResponseEntity<String> recalcularDepreciacaoTodos() {
        depreciacaoService.recalcularDepreciacaoTodosAtivos();
        return ResponseEntity.ok("Recálculo de depreciação iniciado");
    }

    @PostMapping("/recalcular/{ativoId}")
    @Operation(summary = "Recalcular depreciação de um ativo específico", 
               description = "Inicia o recálculo completo da depreciação para um ativo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recálculo do ativo iniciado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno no processamento")
    })
    public ResponseEntity<String> recalcularDepreciacaoAtivo(
            @Parameter(description = "ID do ativo para recálculo", example = "1", required = true)
            @PathVariable Long ativoId) {
        depreciacaoService.recalcularDepreciacaoCompleta(ativoId);
        return ResponseEntity.ok("Recálculo de depreciação do ativo iniciado");
    }

    @GetMapping("/calcular-mensal/{ativoId}")
    @Operation(summary = "Calcular depreciação mensal de um ativo", 
               description = "Calcula o valor mensal de depreciação para um ativo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo realizado com sucesso", 
                    content = @Content(schema = @Schema(implementation = BigDecimal.class))),
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