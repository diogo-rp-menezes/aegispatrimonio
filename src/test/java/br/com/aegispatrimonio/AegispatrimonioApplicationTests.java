package br.com.aegispatrimonio;

import org.junit.jupiter.api.Test;

/**
 * Teste de smoke para garantir que o contexto da aplicação Spring carrega corretamente
 * em um ambiente de integração completo com Testcontainers.
 */
class AegispatrimonioApplicationTests extends BaseIT {

	@Test
	void contextLoads() {
		// Se este teste passar, significa que a aplicação inteira (incluindo a conexão
		// com o banco de dados, Flyway e JPA) está inicializando corretamente para os testes.
	}

}
