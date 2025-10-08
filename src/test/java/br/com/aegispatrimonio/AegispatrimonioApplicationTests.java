package br.com.aegispatrimonio;

import org.junit.jupiter.api.Test;

// Ao herdar de BaseIT, este teste agora usará o Testcontainers e o perfil "test",
// garantindo um ambiente de banco de dados limpo e consistente.
class AegispatrimonioApplicationTests extends BaseIT {

	@Test
	void contextLoads() {
		// Este teste agora não apenas carrega o contexto, mas o faz contra
		// um banco de dados MySQL real, iniciado em um container, com um schema
		// criado pelo Hibernate. Se este teste passar, significa que a aplicação
		// inteira está saudável e configurada corretamente para testes.
	}

}
