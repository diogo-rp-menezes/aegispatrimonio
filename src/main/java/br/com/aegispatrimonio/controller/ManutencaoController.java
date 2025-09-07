package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import br.com.aegispatrimonio.service.ManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/manutencoes")
@RequiredArgsConstructor
@Tag(name = "Manutenções", description = "Operações relacionadas à gestão de manutenções de ativos")
public class ManutencaoController {

    private final ManutencaoService manutencaoService;

    @PostMapping
    @Operation(summary = "Criar nova manutenção", description = "Solicita uma nova manutenção para um ativo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Manutenção criada com sucesso", 
                    content = @Content(schema = @Schema(implementation = ManutencaoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<ManutencaoResponseDTO> criar(@Valid @RequestBody ManutencaoRequestDTO request) {
        ManutencaoResponseDTO response = manutencaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as manutenções", description = "Retorna uma lista de todas as manutenções cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de manutenções recuperada com sucesso")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarTodos() {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarTodos();
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar manutenção por ID", description = "Recupera uma manutenção pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção encontrada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada")
    })
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id) {
        Optional<ManutencaoResponseDTO> manutencao = manutencaoService.buscarPorId(id);
        return manutencao.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ativo/{ativoId}")
    @Operation(summary = "Listar manutenções por ativo", description = "Retorna todas as manutenções de um ativo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenções encontradas"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long ativoId) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorAtivo(ativoId);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar manutenções por status", description = "Retorna manutenções filtradas por status")
    @ApiResponse(responseCode = "200", description = "Manutenções encontradas com sucesso")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorStatus(
            @Parameter(description = "Status da manutenção", example = "PENDENTE") 
            @PathVariable StatusManutencao status) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorStatus(status);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar manutenções por tipo", description = "Retorna manutenções filtradas por tipo")
    @ApiResponse(responseCode = "200", description = "Manutenções encontradas com sucesso")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPorTipo(
            @Parameter(description = "Tipo de manutenção", example = "CORRETIVA") 
            @PathVariable TipoManutencao tipo) {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPorTipo(tipo);
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Listar manutenções pendentes", description = "Retorna todas as manutenções com status pendente")
    @ApiResponse(responseCode = "200", description = "Manutenções pendentes encontradas")
    public ResponseEntity<List<ManutencaoResponseDTO>> listarPendentes() {
        List<ManutencaoResponseDTO> manutencoes = manutencaoService.listarPendentes();
        return ResponseEntity.ok(manutencoes);
    }

    @GetMapping("/ativo/{ativoId}/custo-total")
    @Operation(summary = "Obter custo total de manutenções por ativo", description = "Calcula o custo total de todas as manutenções de um ativo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Custo total calculado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<BigDecimal> obterCustoTotalPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long ativoId) {
        BigDecimal custoTotal = manutencaoService.obterCustoTotalManutencaoPorAtivo(ativoId);
        return ResponseEntity.ok(custoTotal);
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar manutenção", description = "Aprova uma manutenção pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção aprovada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Manutenção não está pendente")
    })
    public ResponseEntity<ManutencaoResponseDTO> aprovarManutencao(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id) {
        ManutencaoResponseDTO response = manutencaoService.aprovarManutencao(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar manutenção", description = "Inicia a execução de uma manutenção aprovada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção iniciada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Manutenção ou técnico não encontrado"),
        @ApiResponse(responseCode = "400", description = "Manutenção não está aprovada")
    })
    public ResponseEntity<ManutencaoResponseDTO> iniciarManutencao(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "ID do técnico responsável", example = "1") 
            @RequestParam Long tecnicoResponsavelId) {
        ManutencaoResponseDTO response = manutencaoService.iniciarManutencao(id, tecnicoResponsavelId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Concluir manutenção", description = "Conclui uma manutenção em andamento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção concluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Manutenção não está em andamento")
    })
    public ResponseEntity<ManutencaoResponseDTO> concluirManutencao(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Descrição do serviço realizado", example = "Troca de peças e ajustes") 
            @RequestParam String descricaoServico,
            @Parameter(description = "Custo real da manutenção", example = "350.75") 
            @RequestParam BigDecimal custoReal,
            @Parameter(description = "Tempo de execução em minutos", example = "120") 
            @RequestParam Integer tempoExecucaoMinutos) {
        ManutencaoResponseDTO response = manutencaoService.concluirManutencao(id, descricaoServico, custoReal, tempoExecucaoMinutos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar manutenção", description = "Cancela uma manutenção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção cancelada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada")
    })
    public ResponseEntity<ManutencaoResponseDTO> cancelarManutencao(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Motivo do cancelamento", example = "Peças indisponíveis") 
            @RequestParam String motivo) {
        ManutencaoResponseDTO response = manutencaoService.cancelarManutencao(id, motivo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar manutenção", description = "Remove uma manutenção do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Manutenção deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Manutenção não pode ser deletada devido ao status")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da manutenção", example = "1") 
            @PathVariable Long id) {
        manutencaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}