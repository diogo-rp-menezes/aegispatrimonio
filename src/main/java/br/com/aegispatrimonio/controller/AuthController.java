package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FilialSimpleDTO;
import br.com.aegispatrimonio.dto.LoginRequestDTO;
import br.com.aegispatrimonio.dto.LoginResponseDTO;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.CustomUserDetailsService;
import br.com.aegispatrimonio.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável pelo processo de autenticação.
 * Fornece um endpoint público para que os usuários possam obter um token JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Autentica um usuário com base em seu email e senha e retorna um token JWT se as credenciais forem válidas.
     * Este é um endpoint público e não requer autenticação prévia.
     *
     * @param loginRequest DTO contendo o email e a senha do usuário.
     * @return ResponseEntity com status 200 OK e um LoginResponseDTO contendo o token JWT em caso de sucesso.
     * @throws org.springframework.security.core.AuthenticationException se as credenciais forem inválidas, resultando em uma resposta 401 Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        // Autentica o usuário com o Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        // Se a autenticação for bem-sucedida, gera o token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
        final String token = jwtService.generateToken(userDetails);

        List<FilialSimpleDTO> filiais = Collections.emptyList();
        if (userDetails instanceof CustomUserDetails) {
            Usuario usuario = ((CustomUserDetails) userDetails).getUsuario();
            if (usuario.getFuncionario() != null && usuario.getFuncionario().getFiliais() != null) {
                filiais = usuario.getFuncionario().getFiliais().stream()
                        .map(f -> new FilialSimpleDTO(f.getId(), f.getNome()))
                        .collect(Collectors.toList());
            }
        }

        // Retorna o token na resposta
        return ResponseEntity.ok(new LoginResponseDTO(token, filiais));
    }
}
