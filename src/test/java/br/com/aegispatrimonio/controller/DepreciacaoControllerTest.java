package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.service.DepreciacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepreciacaoControllerTest {

    @Mock
    private DepreciacaoService depreciacaoService;

    @InjectMocks
    private DepreciacaoController depreciacaoController;

    @Test
    void recalcularDepreciacaoTodos_deveChamarServicoERetornarOk() {
        // Ação
        ResponseEntity<String> response = depreciacaoController.recalcularDepreciacaoTodos();

        // Verificação
        verify(depreciacaoService, times(1)).recalcularDepreciacaoTodosAtivos();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recálculo de depreciação iniciado", response.getBody());
    }

    @Test
    void recalcularDepreciacaoAtivo_deveChamarServicoERetornarOk() {
        // Dados
        Long ativoId = 1L;

        // Ação
        ResponseEntity<String> response = depreciacaoController.recalcularDepreciacaoAtivo(ativoId);

        // Verificação
        verify(depreciacaoService, times(1)).recalcularDepreciacaoCompleta(ativoId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recálculo de depreciação do ativo iniciado", response.getBody());
    }

    @Test
    void calcularDepreciacaoMensal_deveRetornarValorCalculado() {
        // Dados
        Long ativoId = 1L;
        BigDecimal depreciacaoEsperada = new BigDecimal("150.75");
        when(depreciacaoService.calcularDepreciacaoMensal(ativoId)).thenReturn(depreciacaoEsperada);

        // Ação
        ResponseEntity<BigDecimal> response = depreciacaoController.calcularDepreciacaoMensal(ativoId);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(depreciacaoEsperada, response.getBody());
    }
}