package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FilialRequestDTO;
import br.com.aegispatrimonio.dto.response.FilialResponseDTO;
import br.com.aegispatrimonio.service.FilialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/filiais")
@RequiredArgsConstructor
@Tag(name = "Filiais", description = "Operações relacionadas à gestão de filiais da empresa")
public class FilialController {

    private final FilialService filialService;

    @PostMapping
    @Operation(summary = "Criar nova filial", description = "Cria uma nova filial no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Filial criada com sucesso", 
                    content = @Content(schema = @Schema(implementation = FilialResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Código ou nome da filial já existe")
    })
    public ResponseEntity<FilialResponseDTO> criar(@Valid @RequestBody FilialRequestDTO request) {
        FilialResponseDTO response = filialService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as filiais", description = "Retorna uma lista paginada de todas as filiais, com filtro opcional por nome")
    public ResponseEntity<Page<FilialResponseDTO>> listarTodos(
            @Parameter(description = "Nome para filtro (opcional)", example = "são") 
            @RequestParam(required = false) String nome,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        
        if (nome != null && !nome.trim().isEmpty()) {
            return ResponseEntity.ok(filialService.buscarPorNomeContendo(nome, pageable));
        }
        
        return ResponseEntity.ok(filialService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar filial por ID", description = "Recupera uma filial pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filial encontrada"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada")
    })
    public ResponseEntity<FilialResponseDTO> buscarPorId(
            @Parameter(description = "ID da filial", example = "1") 
            @PathVariable Long id) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorId(id);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar filial por código", description = "Recupera uma filial pelo seu código único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filial encontrada"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada")
    })
    public ResponseEntity<FilialResponseDTO> buscarPorCodigo(
            @Parameter(description = "Código da filial", example = "FIL-001") 
            @PathVariable String codigo) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorCodigo(codigo);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Buscar filial por nome exato", description = "Recupera uma filial pelo nome exato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filial encontrada"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada")
    })
    public ResponseEntity<FilialResponseDTO> buscarPorNome(
            @Parameter(description = "Nome exato da filial", example = "Matriz São Paulo") 
            @PathVariable String nome) {
        Optional<FilialResponseDTO> filial = filialService.buscarPorNome(nome);
        return filial.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/verificar-codigo/{codigo}")
    @Operation(summary = "Verificar código da filial", description = "Verifica se um código de filial já existe no sistema")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    public ResponseEntity<Boolean> verificarCodigo(
            @Parameter(description = "Código a ser verificado", example = "FIL-001") 
            @PathVariable String codigo) {
        boolean existe = filialService.existePorCodigo(codigo);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar filial", description = "Atualiza os dados de uma filial existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filial atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Novo código ou nome já existe em outra filial")
    })
    public ResponseEntity<FilialResponseDTO> atualizar(
            @Parameter(description = "ID da filial", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody FilialRequestDTO request) {
        FilialResponseDTO response = filialService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar filial", description = "Remove uma filial do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Filial deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Filial não encontrada"),
        @ApiResponse(responseCode = "409", description = "Filial possui departamentos ou ativos vinculados")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da filial", example = "1") 
            @PathVariable Long id) {
        filialService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}