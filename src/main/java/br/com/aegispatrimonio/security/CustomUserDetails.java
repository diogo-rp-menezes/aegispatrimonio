package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementação de UserDetails que encapsula um objeto Usuario.
 * É usado pelo Spring Security para realizar a autenticação e autorização.
 */
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;
    private final Collection<? extends GrantedAuthority> authorities;

    // Construtor original (para uso da aplicação real)
    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
        // Garante que a role tenha exatamente um prefixo ROLE_
        if (usuario != null && usuario.getRole() != null && !usuario.getRole().trim().isEmpty()) {
            String role = usuario.getRole().trim();
            String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority(normalized));
        } else {
            this.authorities = Collections.emptyList(); // Ou lançar exceção, dependendo da regra de negócio
        }
    }

    // NOVO Construtor (para uso em testes e para flexibilidade futura)
    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        this.usuario = usuario;
        this.authorities = authorities;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getStatus() == Status.ATIVO;
    }
}
