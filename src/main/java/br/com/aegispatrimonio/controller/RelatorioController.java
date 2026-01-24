package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ativos")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios e documentos")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @Operation(summary = "Gerar Termo de Responsabilidade", description = "Gera um arquivo PDF com o termo de responsabilidade para o ativo e funcionário responsável.")
    @GetMapping("/{id}/termo")
    @PreAuthorize("@permissionService.hasAtivoPermission(authentication, #id, 'READ')")
    public ResponseEntity<byte[]> gerarTermoResponsabilidade(@PathVariable Long id) {
        byte[] pdfBytes = relatorioService.gerarTermoResponsabilidade(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "termo_responsabilidade_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
