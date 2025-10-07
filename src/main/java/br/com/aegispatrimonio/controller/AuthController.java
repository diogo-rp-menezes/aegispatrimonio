package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.LoginRequestDTO;
import br.com.aegispatrimonio.dto.LoginResponseDTO;
import br.com.aegispatrimonio.security.CustomUserDetailsService;
import br.com.aegispatrimonio.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // REMOVIDO o /api
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        // Autentica o usuário com o Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        // Se a autenticação for bem-sucedida, gera o token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
        final String token = jwtService.generateToken(userDetails);

        // Retorna o token na resposta
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
