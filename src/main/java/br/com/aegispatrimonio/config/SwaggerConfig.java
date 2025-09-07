package br.com.aegispatrimonio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aegis Patrimônio - API Documentation")
                        .description("Sistema de gestão patrimonial - Documentação completa da API REST")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Diogo Menezes")
                                .email("diogorpm@hotmail.com")
                                .url("https://github.com/diogorpm"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Servidor de Desenvolvimento Local"),
                        new Server()
                                .url("https://api.aegispatrimonio.com.br/api")
                                .description("Servidor de Produção")
                ));
    }
}