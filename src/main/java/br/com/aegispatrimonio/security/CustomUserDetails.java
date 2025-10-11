package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementação de UserDetails que encapsula um objeto Usuario.
 * É usado pelo Spring Security para realizar a autenticação e autorização.
 */
public class CustomUserDetails implements UserDetails {

    // CORREÇÃO: Alterado de Pessoa para Usuario
    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // CORREÇÃO: Busca a role do objeto Usuario
        return Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole()));
    }

    @Override
    public String getPassword() {
        // CORREÇÃO: Busca a senha do objeto Usuario
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        // CORREÇÃO: Busca o email do objeto Usuario
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
        // CORREÇÃO: Verifica o status do objeto Usuario
        return usuario.getStatus() == Status.ATIVO;
    }
}
