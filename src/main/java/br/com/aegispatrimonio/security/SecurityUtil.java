package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
        return userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Retorna os IDs de todas as filiais às quais o usuário logado está associado.
     * @return Um Set de Long contendo os IDs das filiais. Retorna um conjunto vazio se o usuário não for um funcionário ou não tiver filiais.
     */
    public static Set<Long> getCurrentUserFilialIds() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails != null) {
            Usuario usuario = userDetails.getUsuario();
            if (usuario != null) {
                Funcionario funcionario = usuario.getFuncionario();
                if (funcionario != null && funcionario.getFiliais() != null) {
                    return funcionario.getFiliais().stream()
                            .map(Filial::getId)
                            .collect(Collectors.toSet());
                }
            }
        }
        return Collections.emptySet();
    }
}
