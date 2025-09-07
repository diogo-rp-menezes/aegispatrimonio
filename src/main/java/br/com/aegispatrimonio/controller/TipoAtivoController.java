package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.TipoAtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.TipoAtivoResponseDTO;
import br.com.aegispatrimonio.service.TipoAtivoService;
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
@RequestMapping("/tipos-ativo")
@RequiredArgsConstructor
@Tag(name = "Tipos de Ativo", description = "Operações relacionadas à gestão de tipos de ativos patrimoniais")
public class TipoAtivoController {

    private final TipoAtivoService tipoAtivoService;

    @PostMapping
    @Operation(summary = "Criar novo tipo de ativo", description = "Cadastra um novo tipo de ativo no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tipo de ativo criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = TipoAtivoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Tipo de ativo com mesmo nome já existe")
    })
    public ResponseEntity<TipoAtivoResponseDTO> criar(@Valid @RequestBody TipoAtivoRequestDTO request) {
        TipoAtivoResponseDTO response = tipoAtivoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tipos de ativo", description = "Retorna uma lista paginada de todos os tipos de ativo cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de ativo recuperada com sucesso")
    public ResponseEntity<Page<TipoAtivoResponseDTO>> listarTodos(
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<TipoAtivoResponseDTO> tiposAtivo = tipoAtivoService.listarTodos(pageable);
        return ResponseEntity.ok(tiposAtivo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de ativo por ID", description = "Recupera um tipo de ativo pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Tipo de ativo não encontrado")
    })
    public ResponseEntity<TipoAtivoResponseDTO> buscarPorId(
            @Parameter(description = "ID do tipo de ativo", example = "1") 
            @PathVariable Long id) {
        Optional<TipoAtivoResponseDTO> tipoAtivo = tipoAtivoService.buscarPorId(id);
        return tipoAtivo.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Buscar tipo de ativo por nome", description = "Recupera um tipo de ativo pelo nome exato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Tipo de ativo não encontrado")
    })
    public ResponseEntity<TipoAtivoResponseDTO> buscarPorNome(
            @Parameter(description = "Nome exato do tipo de ativo", example = "Computador") 
            @PathVariable String nome) {
        Optional<TipoAtivoResponseDTO> tipoAtivo = tipoAtivoService.buscarPorNome(nome);
        return tipoAtivo.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar tipos de ativo por nome", description = "Busca tipos de ativo por nome com paginação")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    public ResponseEntity<Page<TipoAtivoResponseDTO>> buscarPorNome(
            @Parameter(description = "Nome para busca", example = "comp") 
            @RequestParam String nome,
            @Parameter(description = "Parâmetros de paginação") 
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<TipoAtivoResponseDTO> tiposAtivo = tipoAtivoService.buscarPorNomePaginado(nome, pageable);
        return ResponseEntity.ok(tiposAtivo);
    }

    @GetMapping("/verificar-nome/{nome}")
    @Operation(summary = "Verificar nome do tipo de ativo", description = "Verifica se um nome de tipo de ativo já existe no sistema")
    @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    public ResponseEntity<Boolean> verificarNome(
            @Parameter(description = "Nome a ser verificado", example = "Computador") 
            @PathVariable String nome) {
        boolean existe = tipoAtivoService.existePorNome(nome);
        return ResponseEntity.ok(existe);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de ativo", description = "Atualiza os dados de um tipo de ativo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de ativo atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tipo de ativo não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Novo nome já existe em outro tipo de ativo")
    })
    public ResponseEntity<TipoAtivoResponseDTO> atualizar(
            @Parameter(description = "ID do tipo de ativo", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody TipoAtivoRequestDTO request) {
        TipoAtivoResponseDTO response = tipoAtivoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tipo de ativo", description = "Remove um tipo de ativo do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tipo de ativo deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tipo de ativo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Tipo de ativo possui ativos vinculados")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do tipo de ativo", example = "1") 
            @PathVariable Long id) {
        tipoAtivoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}