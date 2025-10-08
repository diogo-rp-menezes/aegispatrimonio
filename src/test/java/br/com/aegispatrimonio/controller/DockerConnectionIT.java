package br.com.aegispatrimonio.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de integração para verificar a conectividade com o Docker via Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
public class DockerConnectionIT {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação com Testcontainers")
    void contextLoads() {
        // Este teste é bem-sucedido se o contexto da aplicação Spring for carregado
        // corretamente. Como o perfil "test" ativa o Testcontainers, uma falha aqui
        // indicaria um problema na inicialização do contêiner do banco de dados,
        // geralmente relacionado à conectividade com o Docker.
    }
}
