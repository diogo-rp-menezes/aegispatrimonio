package br.com.aegispatrimonio.controller;

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

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pessoas")
@RequiredArgsConstructor
@Tag(name = "Pessoas", description = "Operações relacionadas à gestão de pessoas (colaboradores)")
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    @Operation(summary = "Criar pessoa", description = "Cadastra uma nova pessoa no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Departamento não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<PessoaResponseDTO> criar(@Valid @RequestBody PessoaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pessoaService.criar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pessoa por ID", description = "Recupera uma pessoa específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<PessoaResponseDTO> buscarPorId(
            @Parameter(description = "ID da pessoa", example = "1") @PathVariable Long id) {
        return pessoaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar pessoa por email", description = "Recupera uma pessoa pelo email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<PessoaResponseDTO> buscarPorEmail(
            @Parameter(description = "Email da pessoa", example = "joao.silva@empresa.com") @PathVariable String email) {
        return pessoaService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa", description = "Atualiza os dados de uma pessoa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa atualizada"),
        @ApiResponse(responseCode = "404", description = "Pessoa ou departamento não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<PessoaResponseDTO> atualizar(
            @Parameter(description = "ID da pessoa", example = "1") @PathVariable Long id,
            @Valid @RequestBody PessoaRequestDTO request) {
        return ResponseEntity.ok(pessoaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pessoa", description = "Remove uma pessoa do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pessoa deletada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da pessoa", example = "1") @PathVariable Long id) {
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar pessoas com filtros", description = "Retorna pessoas paginadas com filtros")
    @ApiResponse(responseCode = "200", description = "Pessoas listadas com sucesso")
    public ResponseEntity<Page<PessoaResponseDTO>> listar(
            @Parameter(description = "ID do departamento", example = "1") @RequestParam(required = false) Long departamentoId,
            @Parameter(description = "Nome para busca", example = "joão") @RequestParam(required = false) String nome,
            @Parameter(description = "Parâmetros de paginação") @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        
        if (departamentoId != null) {
            return ResponseEntity.ok(pessoaService.listarPorDepartamento(departamentoId, pageable));
        }
        if (nome != null && !nome.trim().isEmpty()) {
            return ResponseEntity.ok(pessoaService.buscarPorNome(nome, pageable));
        }
        
        return ResponseEntity.ok(pessoaService.listar(pageable));
    }
}