package br.com.aegispatrimonio.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.aegispatrimonio.dto.request.AtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.AtivoResponseDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.service.AtivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ativos")
@RequiredArgsConstructor
@Tag(name = "Ativos", description = "Operações relacionadas à gestão de ativos patrimoniais")
public class AtivoController {

    private final AtivoService ativoService;

    @PostMapping
    @Operation(summary = "Criar novo ativo", description = "Cria um novo ativo patrimonial no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ativo criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = AtivoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Número de patrimônio já existe")
    })
    public ResponseEntity<AtivoResponseDTO> criar(@Valid @RequestBody AtivoRequestDTO request) {
        AtivoResponseDTO response = ativoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ativo por ID", description = "Recupera um ativo pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado")
    })
    public ResponseEntity<AtivoResponseDTO> buscarPorId(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long id) {
        return ativoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patrimonio/{numeroPatrimonio}")
    @Operation(summary = "Buscar ativo por número de patrimônio", description = "Recupera um ativo pelo seu número de patrimônio")
    public ResponseEntity<AtivoResponseDTO> buscarPorNumeroPatrimonio(
            @Parameter(description = "Número do patrimônio", example = "PAT-2024-001") 
            @PathVariable String numeroPatrimonio) {
        return ativoService.buscarPorNumeroPatrimonio(numeroPatrimonio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos os ativos", description = "Retorna uma lista paginada de todos os ativos, com filtro opcional por nome")
    public ResponseEntity<Page<AtivoResponseDTO>> listarTodos(
            @Parameter(description = "Nome para filtro (opcional)", example = "computador") 
            @RequestParam(required = false) String nome,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        
        if (nome != null && !nome.trim().isEmpty()) {
            return ResponseEntity.ok(ativoService.buscarPorNome(nome, pageable));
        }
        
        return ResponseEntity.ok(ativoService.listarTodos(pageable));
    }

    @GetMapping("/tipo/{tipoAtivoId}")
    @Operation(summary = "Listar ativos por tipo", description = "Retorna ativos filtrados por tipo com paginação")
    public ResponseEntity<Page<AtivoResponseDTO>> listarPorTipo(
            @Parameter(description = "ID do tipo de ativo", example = "1") 
            @PathVariable Long tipoAtivoId,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<AtivoResponseDTO> ativos = ativoService.listarPorTipo(tipoAtivoId, pageable);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/localizacao/{localizacaoId}")
    @Operation(summary = "Listar ativos por localização", description = "Retorna ativos filtrados por localização com paginação")
    public ResponseEntity<Page<AtivoResponseDTO>> listarPorLocalizacao(
            @Parameter(description = "ID da localização", example = "1") 
            @PathVariable Long localizacaoId,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<AtivoResponseDTO> ativos = ativoService.listarPorLocalizacao(localizacaoId, pageable);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar ativos por status", description = "Retorna ativos filtrados por status com paginação")
    public ResponseEntity<Page<AtivoResponseDTO>> listarPorStatus(
            @Parameter(description = "Status do ativo", example = "ATIVO") 
            @PathVariable StatusAtivo status,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<AtivoResponseDTO> ativos = ativoService.listarPorStatus(status, pageable);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/valor")
    @Operation(summary = "Buscar por faixa de valor", description = "Retorna ativos dentro de uma faixa de valor com paginação")
    public ResponseEntity<Page<AtivoResponseDTO>> buscarPorFaixaDeValor(
            @Parameter(description = "Valor mínimo", example = "1000.00") 
            @RequestParam BigDecimal valorMin,
            @Parameter(description = "Valor máximo", example = "5000.00") 
            @RequestParam BigDecimal valorMax,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<AtivoResponseDTO> ativos = ativoService.buscarPorFaixaDeValor(valorMin, valorMax, pageable);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/verificar-patrimonio/{numeroPatrimonio}")
    @Operation(summary = "Verificar número de patrimônio", description = "Verifica se um número de patrimônio já existe")
    public ResponseEntity<Boolean> verificarNumeroPatrimonio(
            @Parameter(description = "Número do patrimônio a verificar", example = "PAT-2024-001") 
            @PathVariable String numeroPatrimonio) {
        boolean existe = ativoService.existePorNumeroPatrimonio(numeroPatrimonio);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ativo", description = "Atualiza os dados de um ativo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ativo atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Ativo não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<AtivoResponseDTO> atualizar(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody AtivoRequestDTO request) {
        AtivoResponseDTO response = ativoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ativo", description = "Remove um ativo do sistema")
    @ApiResponse(responseCode = "204", description = "Ativo deletado com sucesso")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do ativo", example = "1") 
            @PathVariable Long id) {
        ativoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}