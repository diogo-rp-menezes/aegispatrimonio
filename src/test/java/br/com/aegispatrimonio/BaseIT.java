package br.com.aegispatrimonio;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AegispatrimonioApplication.class
)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIT {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    static {
        mysqlContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // SOBRESCREVER COMPLETAMENTE as configurações de datasource
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Configurações específicas para testes
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.hbm2ddl.auto", () -> "none");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "America/Sao_Paulo");

        // Flyway
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");

        // Security
        registry.add("jwt.secret", () -> "c2VjdXJlLXNlY3JldC1leGFtcGxlLXNlZWt0ZXN0LXNlY3JldA==");
        registry.add("jwt.expiration", () -> "28800000");

        // Desabilitar recursos desnecessários
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("management.endpoints.web.exposure.include", () -> "");
        registry.add("springdoc.api-docs.enabled", () -> "false");
        registry.add("springdoc.swagger-ui.enabled", () -> "false");
    }

    @BeforeAll
    static void debugTestContainers() {
        System.out.println("=== TESTCONTAINERS CONFIGURATION ===");
        System.out.println("JDBC URL: " + mysqlContainer.getJdbcUrl());
        System.out.println("Username: " + mysqlContainer.getUsername());
        System.out.println("Password: " + mysqlContainer.getPassword());
        System.out.println("Container ID: " + mysqlContainer.getContainerId());
        System.out.println("Is Running: " + mysqlContainer.isRunning());
        System.out.println("================================");
    }
}