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
                    // M2 (audit): nunca logar o token completo em INFO. Em INFO fica
                    // apenas a instrução de como obtê-lo; o token em si só em DEBUG.
                    logger.info("\n\n--- DEV: token de admin@aegis.com disponível ---");
                    logger.info("Para obter o token, faça POST /api/v1/auth/login com as "
                            + "credenciais de desenvolvimento, ou habilite o nível DEBUG "
                            + "para br.com.aegispatrimonio.config.DevConfig.");
                    logger.info("--------------------------------------------------\n\n");

                    if (logger.isDebugEnabled()) {
                        String token = jwtService.generateToken(new CustomUserDetails(adminUser));
                        logger.debug("Authorization: Bearer {}", token);
                    }
                });
                return null;
            });
        };
    }
}
