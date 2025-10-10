package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.BaseIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de integração para verificar a conectividade com o Docker via Testcontainers.
 */
public class DockerConnectionIT extends BaseIT {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação com Testcontainers")
    void contextLoads() {
        // Este teste é bem-sucedido se o contexto da aplicação Spring for carregado
        // corretamente. Como a classe agora herda de BaseIT, o contexto é
        // automaticamente configurado com o contêiner do banco de dados.
        // Uma falha aqui indicaria um problema na inicialização do contêiner,
        // geralmente relacionado à conectividade com o Docker.
    }
}
