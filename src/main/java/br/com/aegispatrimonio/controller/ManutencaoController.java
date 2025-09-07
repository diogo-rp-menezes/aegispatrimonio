package br.com.aegispatrimonio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.StatusManutencao;
import br.com.aegispatrimonio.model.TipoManutencao;
import br.com.aegispatrimonio.service.ManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/manutencoes")
@RequiredArgsConstructor
@Tag(name = "Manutenções", description = "Operações relacionadas à gestão de manutenções de ativos")
public class ManutencaoController {

    private final ManutencaoService manutencaoService;

    @PostMapping
    @Operation(summary = "Criar manutenção", description = "Cria uma nova solicitação de manutenção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Manutenção criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<ManutencaoResponseDTO> criar(@Valid @RequestBody ManutencaoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manutencaoService.criar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar manutenção por ID", description = "Recupera uma manutenção específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção encontrada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada")
    })
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id) {
        return manutencaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/aprovar/{id}")
    @Operation(summary = "Aprovar manutenção", description = "Aprova uma manutenção solicitada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção aprovada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Status inválido para aprovação")
    })
    public ResponseEntity<ManutencaoResponseDTO> aprovar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(manutencaoService.aprovar(id));
    }

    @PostMapping("/iniciar/{id}")
    @Operation(summary = "Iniciar manutenção", description = "Inicia a execução de uma manutenção aprovada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção iniciada"),
        @ApiResponse(responseCode = "404", description = "Manutenção ou técnico não encontrado"),
        @ApiResponse(responseCode = "400", description = "Status inválido para início")
    })
    public ResponseEntity<ManutencaoResponseDTO> iniciar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Parameter(description = "ID do técnico responsável", example = "1") @RequestParam Long tecnicoId) {
        return ResponseEntity.ok(manutencaoService.iniciar(id, tecnicoId));
    }

    @PostMapping("/concluir/{id}")
    @Operation(summary = "Concluir manutenção", description = "Conclui uma manutenção em andamento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção concluída"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Status inválido para conclusão")
    })
    public ResponseEntity<ManutencaoResponseDTO> concluir(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Parameter(description = "Descrição do serviço", example = "Troca de peças") @RequestParam String descricaoServico,
            @Parameter(description = "Custo real", example = "350.75") @RequestParam BigDecimal custoReal,
            @Parameter(description = "Tempo de execução (minutos)", example = "120") @RequestParam Integer tempoExecucao) {
        return ResponseEntity.ok(manutencaoService.concluir(id, descricaoServico, custoReal, tempoExecucao));
    }

    @PostMapping("/cancelar/{id}")
    @Operation(summary = "Cancelar manutenção", description = "Cancela uma manutenção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção cancelada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Não é possível cancelar")
    })
    public ResponseEntity<ManutencaoResponseDTO> cancelar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Parameter(description = "Motivo do cancelamento", example = "Peças indisponíveis") @RequestParam String motivo) {
        return ResponseEntity.ok(manutencaoService.cancelar(id, motivo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar manutenção", description = "Remove uma manutenção do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Manutenção deletada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id) {
        manutencaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar manutenções com filtros", description = "Retorna manutenções paginadas com filtros")
    @ApiResponse(responseCode = "200", description = "Manutenções listadas com sucesso")
    public ResponseEntity<Page<ManutencaoResponseDTO>> listar(
            @Parameter(description = "ID do ativo", example = "1") @RequestParam(required = false) Long ativoId,
            @Parameter(description = "Status da manutenção", example = "SOLICITADA") @RequestParam(required = false) StatusManutencao status,
            @Parameter(description = "Tipo de manutenção", example = "CORRETIVA") @RequestParam(required = false) TipoManutencao tipo,
            @Parameter(description = "ID do solicitante", example = "1") @RequestParam(required = false) Long solicitanteId,
            @Parameter(description = "ID do fornecedor", example = "1") @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "Data inicial solicitação", example = "2024-01-01") @RequestParam(required = false) LocalDate dataSolicitacaoInicio,
            @Parameter(description = "Data final solicitação", example = "2024-12-31") @RequestParam(required = false) LocalDate dataSolicitacaoFim,
            @Parameter(description = "Data inicial conclusão", example = "2024-01-01") @RequestParam(required = false) LocalDate dataConclusaoInicio,
            @Parameter(description = "Data final conclusão", example = "2024-12-31") @RequestParam(required = false) LocalDate dataConclusaoFim,
            @Parameter(description = "Apenas pendentes", example = "true") @RequestParam(required = false) Boolean pendentes,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataSolicitacao,desc") Pageable pageable) {
        
        if (ativoId != null && Boolean.TRUE.equals(pendentes)) {
            return ResponseEntity.ok(manutencaoService.listarPendentesPorAtivo(ativoId, pageable));
        }
        if (Boolean.TRUE.equals(pendentes)) {
            return ResponseEntity.ok(manutencaoService.listarPendentes(pageable));
        }
        if (ativoId != null) {
            return ResponseEntity.ok(manutencaoService.listarPorAtivo(ativoId, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(manutencaoService.listarPorStatus(status, pageable));
        }
        if (tipo != null) {
            return ResponseEntity.ok(manutencaoService.listarPorTipo(tipo, pageable));
        }
        if (solicitanteId != null) {
            return ResponseEntity.ok(manutencaoService.listarPorSolicitante(solicitanteId, pageable));
        }
        if (fornecedorId != null) {
            return ResponseEntity.ok(manutencaoService.listarPorFornecedor(fornecedorId, pageable));
        }
        if (dataSolicitacaoInicio != null && dataSolicitacaoFim != null) {
            return ResponseEntity.ok(manutencaoService.listarPorPeriodoSolicitacao(dataSolicitacaoInicio, dataSolicitacaoFim, pageable));
        }
        if (dataConclusaoInicio != null && dataConclusaoFim != null) {
            return ResponseEntity.ok(manutencaoService.listarPorPeriodoConclusao(dataConclusaoInicio, dataConclusaoFim, pageable));
        }
        
        return ResponseEntity.ok(manutencaoService.listar(pageable));
    }

    @GetMapping("/custo-total")
    @Operation(summary = "Obter custo total por ativo", description = "Calcula custo total de manutenções concluídas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Custo calculado"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<BigDecimal> custoTotalPorAtivo(
            @Parameter(description = "ID do ativo", example = "1") @RequestParam Long ativoId) {
        return ResponseEntity.ok(manutencaoService.custoTotalPorAtivo(ativoId));
    }
}