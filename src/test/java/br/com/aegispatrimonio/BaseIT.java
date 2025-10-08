package br.com.aegispatrimonio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
public abstract class BaseIT {

    @Container
    @ServiceConnection
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.3.0");

    /*
     * A anotação @ServiceConnection substitui a necessidade do @DynamicPropertySource
     * para configurar a URL, usuário e senha do banco de dados.
     * As propriedades de Flyway e DDL devem ser movidas para o arquivo
     * `src/test/resources/application-test.properties`.
     */
}
