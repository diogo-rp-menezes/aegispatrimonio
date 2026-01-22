package br.com.aegispatrimonio.service.provider;

import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.security.CustomUserDetails;
import br.com.aegispatrimonio.service.CurrentUserProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Usuario getCurrentUsuario() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
    }
}
