package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Pessoa pessoa;

    public CustomUserDetails(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(pessoa.getRole()));
    }

    @Override
    public String getPassword() {
        return pessoa.getPassword();
    }

    @Override
    public String getUsername() {
        return pessoa.getEmail();
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
        return pessoa.getStatus() == br.com.aegispatrimonio.model.Status.ATIVO;
    }
}
