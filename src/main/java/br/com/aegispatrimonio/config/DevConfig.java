package br.com.aegispatrimonio.config;

import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev") // Este bean só será ativado quando o perfil "dev" estiver ativo
@RequiredArgsConstructor
public class DevConfig {

    // CORREÇÃO: Injetado UsuarioRepository em vez de PessoaRepository
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @Bean
    public CommandLineRunner printDevToken() {
        return args -> {
            // CORREÇÃO: Busca o usuário na nova tabela de usuarios
            usuarioRepository.findByEmail("admin@aegis.com").ifPresent(adminUser -> {
                // Gera um token para ele
                String token = jwtService.generateToken(new CustomUserDetails(adminUser));

                // Imprime o token no console de forma bem visível
                System.out.println("\n\n--- TOKEN DE DESENVOLVIMENTO (admin@aegis.com) ---");
                System.out.println("Authorization: Bearer " + token);
                System.out.println("--- COPIE O TOKEN ACIMA PARA USAR NO POSTMAN ---\n\n");
            });
        };
    }
}
