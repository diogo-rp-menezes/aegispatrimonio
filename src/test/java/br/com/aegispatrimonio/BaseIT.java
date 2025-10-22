package br.com.aegispatrimonio;

// import org.springframework.beans.factory.annotation.Autowired; // Removido
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Bean; // Removido
// import org.springframework.context.annotation.Import; // Removido
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
// import org.springframework.transaction.PlatformTransactionManager; // Removido
// import org.springframework.transaction.support.TransactionTemplate; // Removido
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.DockerClientFactory;

// import br.com.aegispatrimonio.config.TestTransactionConfig; // Removido

// CORREÇÃO: Especifica a classe de aplicação principal para garantir que todo o contexto seja carregado.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = AegispatrimonioApplication.class
)
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
// @Import(TestTransactionConfig.class) // Removido: Importar TransactionTemplate apenas onde for necessário
public abstract class BaseIT {

    // Tenta usar Testcontainers/MySQL quando o Docker estiver disponível.
    // Caso contrário, fazemos fallback para H2 in-memory para permitir execução local sem Docker.

    public static MySQLContainer<?> mysqlContainer;

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            mysqlContainer = new MySQLContainer<>("mysql:8.0.26")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
            // Start container programmatically so lifecycle is explicit
            mysqlContainer.start();
        } else {
            mysqlContainer = null; // Indica fallback para H2
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (mysqlContainer != null) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
            registry.add("spring.datasource.username", mysqlContainer::getUsername);
            registry.add("spring.datasource.password", mysqlContainer::getPassword);
            registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
            // Use update to allow Hibernate to add missing columns when migrations are out-of-sync
            registry.add("spring.flyway.enabled", () -> "true");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        } else {
            // Fallback: H2 in-memory - bom para testes unitários/locais sem Docker.
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
            registry.add("spring.flyway.enabled", () -> "false");
        }
    }

    // @Autowired // Removido
    // protected PlatformTransactionManager transactionManager; // Removido

    // @Bean // Removido
    // public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) { // Removido
    //     return new TransactionTemplate(transactionManager); // Removido
    // }
}
