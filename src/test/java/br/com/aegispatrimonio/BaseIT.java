package br.com.aegispatrimonio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AegispatrimonioApplication.class)
@ActiveProfiles("test")
@Sql(scripts = { "/cleanup.sql", "/data.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIT {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // H2 Configuration
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");

        // Disable Flyway and use Hibernate for Schema
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.hbm2ddl.auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        // Security
        registry.add("jwt.secret", () -> "c2VjdXJlLXNlY3JldC1leGFtcGxlLXNlZWt0ZXN0LXNlY3JldA==");
        registry.add("jwt.expiration", () -> "28800000");

        // Application
        registry.add("app.frontend-url", () -> "http://localhost:5173");
    }
}
