package br.com.aegispatrimonio.util;

import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Crie o mock do Usuario
        Usuario usuario = new Usuario();
        usuario.setId(customUser.id());
        usuario.setEmail(customUser.email());
        usuario.setRole(customUser.role());

        // Associe um funcionário se necessário (opcional, mas bom para testes de serviço)
        if (customUser.funcionarioId() > 0) {
            Funcionario funcionario = new Funcionario();
            funcionario.setId(customUser.funcionarioId());
            usuario.setFuncionario(funcionario);
        }

        // Crie o CustomUserDetails
        CustomUserDetails principal = new CustomUserDetails(usuario);

        // Crie a autenticação
        Set<SimpleGrantedAuthority> authorities = Stream.of(customUser.role())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", authorities);
        context.setAuthentication(auth);
        return context;
    }
}
