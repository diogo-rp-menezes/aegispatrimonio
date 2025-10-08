package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class DepreciacaoControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AtivoRepository ativoRepository;

    @Test
    void recalcularDepreciacaoTodos_deveRecalcularDepreciacaoParaTodosAtivos() {
        // Ação
        ResponseEntity<String> response = restTemplate.postForEntity("/depreciacao/recalcular-todos", null, String.class);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recálculo de depreciação iniciado", response.getBody());

        // Espera assíncrona até que a depreciação do primeiro ativo seja recalculada
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Ativo ativo1 = ativoRepository.findById(1L).orElseThrow();
            // O cálculo exato depende da data atual, mas deve ser maior que zero
            assertEquals(1, ativo1.getDepreciacaoAcumulada().compareTo(BigDecimal.ZERO), "A depreciação acumulada do ativo 1 deve ser maior que zero.");
        });

        // Espera assíncrona para o segundo ativo
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Ativo ativo2 = ativoRepository.findById(2L).orElseThrow();
            assertEquals(1, ativo2.getDepreciacaoAcumulada().compareTo(BigDecimal.ZERO), "A depreciação acumulada do ativo 2 deve ser maior que zero.");
        });
    }

    @Test
    void recalcularDepreciacaoAtivo_deveRecalcularDepreciacaoParaAtivoEspecifico() {
        // Ação
        ResponseEntity<String> response = restTemplate.postForEntity("/depreciacao/recalcular/1", null, String.class);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recálculo de depreciação do ativo iniciado", response.getBody());

        // Espera assíncrona
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Ativo ativo = ativoRepository.findById(1L).orElseThrow();
            assertEquals(1, ativo.getDepreciacaoAcumulada().compareTo(BigDecimal.ZERO), "A depreciação acumulada deve ser maior que zero.");
        });
    }

    @Test
    void calcularDepreciacaoMensal_deveRetornarValorCalculado() {
        // Ação
        ResponseEntity<BigDecimal> response = restTemplate.getForEntity("/depreciacao/calcular-mensal/1", BigDecimal.class);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BigDecimal depreciacaoMensalEsperada = new BigDecimal("180.00"); // (12000 - 1200) / 60
        assertEquals(0, depreciacaoMensalEsperada.compareTo(response.getBody().setScale(2, RoundingMode.HALF_UP)));
    }
}
