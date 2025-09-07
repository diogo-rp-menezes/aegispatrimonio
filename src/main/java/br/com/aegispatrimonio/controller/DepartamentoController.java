package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.service.DepartamentoService;
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
@RequestMapping("/departamentos")
@RequiredArgsConstructor
@Tag(name = "Departamentos", description = "Operações relacionadas à gestão de departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @PostMapping
    @Operation(summary = "Criar novo departamento", description = "Cria um novo departamento no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Departamento criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = DepartamentoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Departamento já existe")
    })
    public ResponseEntity<DepartamentoResponseDTO> criar(@Valid @RequestBody DepartamentoRequestDTO request) {
        DepartamentoResponseDTO response = departamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os departamentos", description = "Retorna uma lista paginada de todos os departamentos, com filtros opcionais")
    public ResponseEntity<Page<DepartamentoResponseDTO>> listarTodos(
            @Parameter(description = "Nome para filtro (opcional)", example = "ti") 
            @RequestParam(required = false) String nome,
            @Parameter(description = "ID da filial para filtro (opcional)", example = "1") 
            @RequestParam(required = false) Long filialId,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        
        if (nome != null && !nome.trim().isEmpty()) {
            return ResponseEntity.ok(departamentoService.buscarPorNome(nome, pageable));
        }
        
        if (filialId != null) {
            return ResponseEntity.ok(departamentoService.listarPorFilial(filialId, pageable));
        }
        
        return ResponseEntity.ok(departamentoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar departamento por ID", description = "Recupera um departamento pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<DepartamentoResponseDTO> buscarPorId(
            @Parameter(description = "ID do departamento", example = "1") 
            @PathVariable Long id) {
        Optional<DepartamentoResponseDTO> departamento = departamentoService.buscarPorId(id);
        return departamento.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar departamento", description = "Atualiza os dados de um departamento existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Departamento atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<DepartamentoResponseDTO> atualizar(
            @Parameter(description = "ID do departamento", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody DepartamentoRequestDTO request) {
        DepartamentoResponseDTO response = departamentoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar departamento", description = "Remove um departamento do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Departamento deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Departamento possui ativos vinculados")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do departamento", example = "1") 
            @PathVariable Long id) {
        departamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}