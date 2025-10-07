package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Pessoa;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public static CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    public static boolean isCurrentUserAdmin() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return false;
        }
        // Checa a autoridade de forma robusta, em vez de comparar strings.
        return userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    public static Long getCurrentUserFilialId() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails != null) {
            Pessoa pessoa = userDetails.getPessoa();
            if (pessoa != null && pessoa.getFilial() != null) {
                return pessoa.getFilial().getId();
            }
        }
        return null; // Ou lançar uma exceção se um usuário sem filial for um estado inválido
    }
}
