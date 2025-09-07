package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.FornecedorRequestDTO;
import br.com.aegispatrimonio.dto.response.FornecedorResponseDTO;
import br.com.aegispatrimonio.service.FornecedorService;
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
import java.util.Optional;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores", description = "Operações relacionadas à gestão de fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping
    @Operation(summary = "Criar novo fornecedor", description = "Cadastra um novo fornecedor no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso", 
                    content = @Content(schema = @Schema(implementation = FornecedorResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "CNPJ ou email já cadastrado")
    })
    public ResponseEntity<FornecedorResponseDTO> criar(@Valid @RequestBody FornecedorRequestDTO request) {
        FornecedorResponseDTO response = fornecedorService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os fornecedores", description = "Retorna uma lista de todos os fornecedores cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de fornecedores recuperada com sucesso")
    public ResponseEntity<List<FornecedorResponseDTO>> listarTodos() {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.listarTodos();
        return ResponseEntity.ok(fornecedores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID", description = "Recupera um fornecedor pelo seu identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<FornecedorResponseDTO> buscarPorId(
            @Parameter(description = "ID do fornecedor", example = "1") 
            @PathVariable Long id) {
        Optional<FornecedorResponseDTO> fornecedor = fornecedorService.buscarPorId(id);
        return fornecedor.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Buscar fornecedor por nome exato", description = "Recupera um fornecedor pelo nome exato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<FornecedorResponseDTO> buscarPorNome(
            @Parameter(description = "Nome exato do fornecedor", example = "Microsoft Brasil") 
            @PathVariable String nome) {
        Optional<FornecedorResponseDTO> fornecedor = fornecedorService.buscarPorNome(nome);
        return fornecedor.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar fornecedores por email", description = "Retorna fornecedores que possuem o email informado")
    @ApiResponse(responseCode = "200", description = "Fornecedores encontrados com sucesso")
    public ResponseEntity<List<FornecedorResponseDTO>> buscarPorEmail(
            @Parameter(description = "Email para busca", example = "contato@empresa.com") 
            @PathVariable String email) {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.buscarPorEmail(email);
        return ResponseEntity.ok(fornecedores);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar fornecedores por nome contendo", description = "Retorna fornecedores cujo nome contenha o texto informado")
    @ApiResponse(responseCode = "200", description = "Fornecedores encontrados com sucesso")
    public ResponseEntity<List<FornecedorResponseDTO>> buscarPorNomeContendo(
            @Parameter(description = "Texto para busca no nome", example = "tech") 
            @RequestParam String nome) {
        List<FornecedorResponseDTO> fornecedores = fornecedorService.buscarPorNomeContendo(nome);
        return ResponseEntity.ok(fornecedores);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor", description = "Atualiza os dados de um fornecedor existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Novo CNPJ ou email já existe em outro fornecedor")
    })
    public ResponseEntity<FornecedorResponseDTO> atualizar(
            @Parameter(description = "ID do fornecedor", example = "1") 
            @PathVariable Long id,
            @Valid @RequestBody FornecedorRequestDTO request) {
        FornecedorResponseDTO response = fornecedorService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar fornecedor", description = "Remove um fornecedor do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Fornecedor deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado"),
        @ApiResponse(responseCode = "409", description = "Fornecedor possui ativos vinculados")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do fornecedor", example = "1") 
            @PathVariable Long id) {
        fornecedorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}