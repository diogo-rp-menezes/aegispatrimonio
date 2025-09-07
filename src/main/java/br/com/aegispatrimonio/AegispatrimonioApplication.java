package br.com.aegispatrimonio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("br.com.aegispatrimonio.repository")
@EntityScan("br.com.aegispatrimonio.model")  
@ComponentScan("br.com.aegispatrimonio")
public class AegispatrimonioApplication {

    public static void main(String[] args) {
        SpringApplication.run(AegispatrimonioApplication.class, args);
    }
}