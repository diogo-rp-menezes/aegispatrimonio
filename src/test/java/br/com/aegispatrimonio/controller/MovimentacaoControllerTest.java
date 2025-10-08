package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import br.com.aegispatrimonio.service.MovimentacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoControllerTest {

    @Mock
    private MovimentacaoService movimentacaoService;

    @InjectMocks
    private MovimentacaoController movimentacaoController;

    private final Pageable pageable = Pageable.unpaged();

    @Test
    void criar_deveRetornarCreatedComMovimentacao() {
        MovimentacaoRequestDTO request = new MovimentacaoRequestDTO(1L, 1L, 2L, 1L, 2L, LocalDate.now(), "Motivo", "Obs");
        MovimentacaoResponseDTO responseDTO = new MovimentacaoResponseDTO();
        responseDTO.setId(1L);
        when(movimentacaoService.criar(any(MovimentacaoRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<MovimentacaoResponseDTO> response = movimentacaoController.criar(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void listarTodos_deveRetornarPaginaDeMovimentacoes() {
        Page<MovimentacaoResponseDTO> pagina = new PageImpl<>(Collections.singletonList(new MovimentacaoResponseDTO()));
        when(movimentacaoService.findAll(any(Pageable.class))).thenReturn(pagina);

        ResponseEntity<Page<MovimentacaoResponseDTO>> response = movimentacaoController.listarTodos(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pagina, response.getBody());
    }

    @Test
    void buscarPorId_quandoEncontrado_deveRetornarOkComMovimentacao() {
        Long id = 1L;
        MovimentacaoResponseDTO responseDTO = new MovimentacaoResponseDTO();
        when(movimentacaoService.buscarPorId(id)).thenReturn(Optional.of(responseDTO));

        ResponseEntity<MovimentacaoResponseDTO> response = movimentacaoController.buscarPorId(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void buscarPorId_quandoNaoEncontrado_deveRetornarNotFound() {
        Long id = 1L;
        when(movimentacaoService.buscarPorId(id)).thenReturn(Optional.empty());

        ResponseEntity<MovimentacaoResponseDTO> response = movimentacaoController.buscarPorId(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarPorAtivo_deveRetornarPaginaDeMovimentacoes() {
        Long ativoId = 1L;
        Page<MovimentacaoResponseDTO> pagina = new PageImpl<>(Collections.singletonList(new MovimentacaoResponseDTO()));
        when(movimentacaoService.findByAtivoId(ativoId, pageable)).thenReturn(pagina);

        ResponseEntity<Page<MovimentacaoResponseDTO>> response = movimentacaoController.listarPorAtivo(ativoId, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pagina, response.getBody());
    }

    @Test
    void listarPorStatus_deveRetornarPaginaDeMovimentacoes() {
        StatusMovimentacao status = StatusMovimentacao.PENDENTE;
        Page<MovimentacaoResponseDTO> pagina = new PageImpl<>(Collections.singletonList(new MovimentacaoResponseDTO()));
        when(movimentacaoService.findByStatus(status, pageable)).thenReturn(pagina);

        ResponseEntity<Page<MovimentacaoResponseDTO>> response = movimentacaoController.listarPorStatus(status, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pagina, response.getBody());
    }

    @Test
    void efetivarMovimentacao_deveRetornarOkComMovimentacao() {
        Long id = 1L;
        MovimentacaoResponseDTO responseDTO = new MovimentacaoResponseDTO();
        responseDTO.setStatus(StatusMovimentacao.EFETIVADA);
        when(movimentacaoService.efetivarMovimentacao(id)).thenReturn(responseDTO);

        ResponseEntity<MovimentacaoResponseDTO> response = movimentacaoController.efetivarMovimentacao(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusMovimentacao.EFETIVADA, response.getBody().getStatus());
    }

    @Test
    void cancelarMovimentacao_deveRetornarOkComMovimentacao() {
        Long id = 1L;
        String motivo = "Cancelado";
        MovimentacaoResponseDTO responseDTO = new MovimentacaoResponseDTO();
        responseDTO.setStatus(StatusMovimentacao.CANCELADA);
        when(movimentacaoService.cancelarMovimentacao(id, motivo)).thenReturn(responseDTO);

        ResponseEntity<MovimentacaoResponseDTO> response = movimentacaoController.cancelarMovimentacao(id, motivo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusMovimentacao.CANCELADA, response.getBody().getStatus());
    }

    @Test
    void deletar_deveRetornarNoContent() {
        Long id = 1L;
        doNothing().when(movimentacaoService).deletar(id);

        ResponseEntity<Void> response = movimentacaoController.deletar(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(movimentacaoService, times(1)).deletar(id);
    }
}
