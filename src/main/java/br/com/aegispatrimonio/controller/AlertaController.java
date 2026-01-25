package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.AlertaDTO;
import br.com.aegispatrimonio.model.Alerta;
import br.com.aegispatrimonio.repository.AlertaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/alertas")
public class AlertaController {

    private final AlertaRepository alertaRepository;

    public AlertaController(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<AlertaDTO> listarAlertas(
            @PageableDefault(sort = "dataCriacao", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Boolean lido) {

        Page<Alerta> page;
        if (lido == null) {
            page = alertaRepository.findAll(pageable);
        } else {
            page = alertaRepository.findByLido(lido, pageable);
        }

        return page.map(this::toDTO);
    }

    @PutMapping("/{id}/ler")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Transactional
    public ResponseEntity<Void> marcarComoLido(@PathVariable Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta não encontrado: " + id));

        if (!alerta.isLido()) {
            alerta.setLido(true);
            alerta.setDataLeitura(LocalDateTime.now());
            alertaRepository.save(alerta);
        }
        return ResponseEntity.noContent().build();
    }

    private AlertaDTO toDTO(Alerta alerta) {
        return new AlertaDTO(
                alerta.getId(),
                alerta.getAtivo().getId(),
                alerta.getAtivo().getNome(),
                alerta.getTipo(),
                alerta.getTitulo(),
                alerta.getMensagem(),
                alerta.getDataCriacao(),
                alerta.isLido(),
                alerta.getDataLeitura()
        );
    }
}
