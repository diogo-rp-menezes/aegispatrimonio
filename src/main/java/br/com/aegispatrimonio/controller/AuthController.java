package br.com.aegispatrimonio.controller;

import br.com.aegispatrimonio.dto.FilialSimpleDTO;
import br.com.aegispatrimonio.dto.LoginRequestDTO;
import br.com.aegispatrimonio.dto.LoginResponseDTO;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping({ "/api/v1/auth", "/api/auth" })
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Autentica um usuário com base em seu email e senha e retorna um token JWT se
     * as credenciais forem válidas.
     * Este é um endpoint público e não requer autenticação prévia.
     *
     * @param loginRequest DTO contendo o email e a senha do usuário.
     * @return ResponseEntity com status 200 OK e um LoginResponseDTO contendo o
     *         token JWT em caso de sucesso.
     * @throws org.springframework.security.core.AuthenticationException se as
     *                                                                   credenciais
     *                                                                   forem
     *                                                                   inválidas,
     *                                                                   resultando
     *                                                                   em uma
     *                                                                   resposta
     *                                                                   401
     *                                                                   Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        Authentication authentication;
        try {
            // Autentica o usuário com o Spring Security
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            log.error("Erro na autenticação: ", e);
            throw e;
        }

        // Se a autenticação for bem-sucedida, gera o token
        // Use o principal da autenticação para evitar recarregar do banco e possíveis
        // erros de estado
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(buildResponse(userDetails, token));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(buildResponse(userDetails, ""));
    }

    private LoginResponseDTO buildResponse(UserDetails userDetails, String token) {
        List<FilialSimpleDTO> filiais = Collections.emptyList();
        if (userDetails instanceof CustomUserDetails) {
            Usuario usuario = ((CustomUserDetails) userDetails).getUsuario();
            try {
                if (usuario.getFuncionario() != null && usuario.getFuncionario().getFiliais() != null) {
                    filiais = usuario.getFuncionario().getFiliais().stream()
                            .map(f -> new FilialSimpleDTO(f.getId(), f.getNome()))
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Captura exceções como LazyInitializationException para evitar que o login
                // falhe completamente
                // se houver problemas ao carregar as filiais (que são info secundária no login)
                log.warn("Erro ao carregar filiais para o usuário {}: {}", userDetails.getUsername(), e.getMessage());
                // filiais permanece vazia
            }
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponseDTO(token, filiais, roles);
    }
}
