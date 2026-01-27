package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Status;
import br.com.aegispatrimonio.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementação de UserDetails que encapsula um objeto Usuario.
 * É usado pelo Spring Security para realizar a autenticação e autorização.
 * Também implementa OAuth2User para suportar autenticação social.
 */
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Usuario usuario;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public static CustomUserDetails create(Usuario usuario, Map<String, Object> attributes) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        userDetails.setAttributes(attributes);
        return userDetails;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // Construtor original (para uso da aplicação real)
    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
        java.util.Set<GrantedAuthority> auths = new java.util.HashSet<>();

        if (usuario != null) {
            // New RBAC Roles
            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                for (br.com.aegispatrimonio.model.Role r : usuario.getRoles()) {
                    auths.add(new SimpleGrantedAuthority(r.getName()));
                }
            }
            // Legacy Fallback
            else if (usuario.getRole() != null && !usuario.getRole().trim().isEmpty()) {
                String role = usuario.getRole().trim();
                String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                auths.add(new SimpleGrantedAuthority(normalized));
            }
        }

        this.authorities = auths;
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

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(usuario.getId());
    }
}
