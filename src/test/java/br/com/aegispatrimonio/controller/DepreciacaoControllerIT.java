package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

// CORREÇÃO: Define explicitamente a execução dos dois scripts, na ordem correta, sobrescrevendo o @Sql da BaseIT.
@Sql(scripts = {"/cleanup.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class DepreciacaoControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // O script data.sql já cria o usuário admin com ID 1.
        // Apenas precisamos buscar esse usuário para gerar o token.
        Pessoa admin = pessoaRepository.findById(1L).orElseThrow(() -> new RuntimeException("Usuário admin não encontrado pelo data.sql"));
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
    }

    private HttpEntity<String> createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        return new HttpEntity<>(headers);
    }

    @Test
    void recalcularDepreciacaoTodos_deveRecalcularDepreciacaoParaTodosAtivos() {
        // Ação
        ResponseEntity<String> response = restTemplate.exchange("/depreciacao/recalcular-todos", HttpMethod.POST, createAuthHeaders(), String.class);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recálculo de depreciação iniciado", response.getBody());

        // Espera assíncrona até que a depreciação do primeiro ativo seja recalculada
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Ativo ativo1 = ativoRepository.findById(1L).orElseThrow();
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
        ResponseEntity<String> response = restTemplate.exchange("/depreciacao/recalcular/1", HttpMethod.POST, createAuthHeaders(), String.class);

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
        ResponseEntity<BigDecimal> response = restTemplate.exchange("/depreciacao/calcular-mensal/1", HttpMethod.GET, createAuthHeaders(), BigDecimal.class);

        // Verificação
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BigDecimal depreciacaoMensalEsperada = new BigDecimal("180.00"); // (12000 - 1200) / 60
        assertEquals(0, depreciacaoMensalEsperada.compareTo(response.getBody().setScale(2, RoundingMode.HALF_UP)));
    }
}
