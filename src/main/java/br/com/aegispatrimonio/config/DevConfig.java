package br.com.aegispatrimonio.config;

import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("dev") // Este bean só será ativado quando o perfil "dev" estiver ativo
@RequiredArgsConstructor
public class DevConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevConfig.class);

    // CORREÇÃO: Injetado UsuarioRepository em vez de PessoaRepository
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final TransactionTemplate transactionTemplate;

    @Bean
    public CommandLineRunner printDevToken() {
        return args -> {
            transactionTemplate.execute(status -> {
                // CORREÇÃO: Busca o usuário na nova tabela de usuarios
                usuarioRepository.findByEmail("admin@aegis.com").ifPresent(adminUser -> {
                    // Gera um token para ele
                    String token = jwtService.generateToken(new CustomUserDetails(adminUser));

                    // Imprime o token no console de forma bem visível
                    logger.info("\n\n--- TOKEN DE DESENVOLVIMENTO (admin@aegis.com) ---");
                    logger.info("Authorization: Bearer {}", token);
                    logger.info("--- COPIE O TOKEN ACIMA PARA USAR NO POSTMAN ---\n\n");
                });
                return null;
            });
        };
    }
}
