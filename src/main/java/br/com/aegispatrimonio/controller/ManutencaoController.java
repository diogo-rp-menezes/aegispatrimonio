package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importar PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/manutencoes")
@RequiredArgsConstructor
@Tag(name = "Manutenções", description = "Operações relacionadas à gestão de manutenções de ativos")
public class ManutencaoController {

    private final ManutencaoService manutencaoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
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
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Adicionado PreAuthorize
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Adicionado PreAuthorize
    @Operation(summary = "Listar e filtrar manutenções", description = "Retorna manutenções paginadas com filtros dinâmicos")
    @ApiResponse(responseCode = "200", description = "Manutenções listadas com sucesso")
    public ResponseEntity<Page<ManutencaoResponseDTO>> listar(
            @Parameter(description = "ID do ativo") @RequestParam(required = false) Long ativoId,
            @Parameter(description = "Status da manutenção") @RequestParam(required = false) StatusManutencao status,
            @Parameter(description = "Tipo de manutenção") @RequestParam(required = false) TipoManutencao tipo,
            @Parameter(description = "ID do solicitante") @RequestParam(required = false) Long solicitanteId,
            @Parameter(description = "ID do fornecedor") @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "Data inicial da solicitação") @RequestParam(required = false) LocalDate dataSolicitacaoInicio,
            @Parameter(description = "Data final da solicitação") @RequestParam(required = false) LocalDate dataSolicitacaoFim,
            @Parameter(description = "Data inicial da conclusão") @RequestParam(required = false) LocalDate dataConclusaoInicio,
            @Parameter(description = "Data final da conclusão") @RequestParam(required = false) LocalDate dataConclusaoFim,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "dataSolicitacao,desc") Pageable pageable) {

        Page<ManutencaoResponseDTO> manutenções = manutencaoService.listar(ativoId, status, tipo, solicitanteId, fornecedorId,
                dataSolicitacaoInicio, dataSolicitacaoFim, dataConclusaoInicio, dataConclusaoFim, pageable);
        return ResponseEntity.ok(manutenções);
    }

    @PostMapping("/aprovar/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
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
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
    @Operation(summary = "Iniciar manutenção", description = "Inicia a execução de uma manutenção aprovada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção iniciada"),
        @ApiResponse(responseCode = "404", description = "Manutenção ou técnico não encontrado"),
        @ApiResponse(responseCode = "400", description = "Status inválido para início")
    })
    public ResponseEntity<ManutencaoResponseDTO> iniciar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Valid @RequestBody ManutencaoInicioDTO inicioDTO) {
        return ResponseEntity.ok(manutencaoService.iniciar(id, inicioDTO));
    }

    @PostMapping("/concluir/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
    @Operation(summary = "Concluir manutenção", description = "Conclui uma manutenção em andamento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção concluída"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Status inválido para conclusão")
    })
    public ResponseEntity<ManutencaoResponseDTO> concluir(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Valid @RequestBody ManutencaoConclusaoDTO conclusaoDTO) {
        return ResponseEntity.ok(manutencaoService.concluir(id, conclusaoDTO));
    }

    @PostMapping("/cancelar/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
    @Operation(summary = "Cancelar manutenção", description = "Cancela uma manutenção")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manutenção cancelada"),
        @ApiResponse(responseCode = "404", description = "Manutenção não encontrada"),
        @ApiResponse(responseCode = "400", description = "Não é possível cancelar")
    })
    public ResponseEntity<ManutencaoResponseDTO> cancelar(
            @Parameter(description = "ID da manutenção", example = "1") @PathVariable Long id,
            @Valid @RequestBody ManutencaoCancelDTO cancelDTO) {
        return ResponseEntity.ok(manutencaoService.cancelar(id, cancelDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Adicionado PreAuthorize
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

    @GetMapping("/custo-total")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Adicionado PreAuthorize
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
