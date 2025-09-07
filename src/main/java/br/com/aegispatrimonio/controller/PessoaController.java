package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.service.PessoaService;
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

import java.util.List;

@RestController
@RequestMapping("/pessoas")
@RequiredArgsConstructor
@Tag(name = "Pessoas", description = "Operações relacionadas à gestão de pessoas (colaboradores)")
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    @Operation(summary = "Criar nova pessoa", description = "Cadastra uma nova pessoa (colaborador) no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso", 
                    content = @Content(schema = @Schema(implementation = PessoaResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<PessoaResponseDTO> criar(@Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as pessoas", description = "Retorna uma lista de todas as pessoas cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de pessoas recuperada com sucesso")
    public ResponseEntity<List<PessoaResponseDTO>> listarTodos() {
        List<PessoaResponseDTO> pessoas = pessoaService.listarTodos();
        return ResponseEntity.ok(pessoas);
    }

    @GetMapping("/{id}") // CORREÇÃO AQUI - Fechamento das aspas
    @Operation(summary = "Buscar pessoa por ID", description = "Recupera uma pessoa pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<PessoaResponseDTO> buscarPorId(
            @Parameter(description = "ID da pessoa", example = "1") 
            @PathVariable Long id) {
        PessoaResponseDTO pessoa = pessoaService.buscarPorId(id);
        return ResponseEntity.ok(pessoa);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar pessoa por email", description = "Recupera uma pessoa pelo email exato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<PessoaResponseDTO> buscarPorEmail(
            @Parameter(description = "Email da pessoa", example = "joao.silva@empresa.com") 
            @PathVariable String email) {
        PessoaResponseDTO pessoa = pessoaService.buscarPorEmail(email);
        return ResponseEntity.ok(pessoa);
    }

    @GetMapping("/departamento/{departamentoId}")
    @Operation(summary = "Listar pessoas por departamento", description = "Retorna pessoas pertencentes a um departamento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoas encontradas"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<List<PessoaResponseDTO>> listarPorDepartamento(
            @Parameter(description = "ID do departamento", example = "1") 
            @PathVariable Long departamentoId) {
        List<PessoaResponseDTO> pessoas = pessoaService.listarPorDepartamento(departamentoId);
        return ResponseEntity.ok(pessoas);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar pessoas por nome", description = "Retorna pessoas cujo nome contenha o texto informado")
    @ApiResponse(responseCode = "200", description = "Pessoas encontradas com sucesso")
    public ResponseEntity<List<PessoaResponseDTO>> buscarPorNome(
            @Parameter(description = "Texto para busca no nome", example = "joão") 
            @RequestParam String nome) {
        List<PessoaResponseDTO> pessoas = pessoaService.buscarPorNome(nome);
        return ResponseEntity.ok(pessoas);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa", description = "Atualiza os dados de uma pessoa existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pessoa ou departamento não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Novo email já existe em outra pessoa")
    })
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @Parameter(description = "ID da pessoa", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pessoa", description = "Remove uma pessoa do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pessoa deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada"),
        @ApiResponse(responseCode = "409", description = "Pessoa possui ativos ou movimentações vinculadas")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da pessoa", example = "1") 
            @PathVariable Long id) {
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}