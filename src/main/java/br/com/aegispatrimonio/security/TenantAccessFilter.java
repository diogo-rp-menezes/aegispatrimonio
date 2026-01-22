package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.context.TenantContext;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de validação de acesso ao Tenant.
 * Garante que o usuário autenticado tem permissão para acessar a Filial solicitada.
 */
public class TenantAccessFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantAccessFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Long requestedFilialId = TenantContext.getFilialId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (requestedFilialId != null && authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Usuario usuario = userDetails.getUsuario();

                if (usuario.getFuncionario() != null) {
                    Set<Filial> allowedFiliais = usuario.getFuncionario().getFiliais();

                    boolean isAllowed = allowedFiliais.stream()
                            .anyMatch(f -> f.getId().equals(requestedFilialId));

                    if (!isAllowed) {
                        log.warn("Access Denied: User {} tried to access unauthorized Filial ID {}",
                                usuario.getEmail(), requestedFilialId);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Access Denied: You do not have permission to access this Filial.");
                        return;
                    } else {
                        log.debug("Access Granted: User {} accessing Filial ID {}",
                                usuario.getEmail(), requestedFilialId);
                    }
                } else {
                    // Se usuário não tem funcionário associado, assume que não tem acesso a filiais ou é admin global?
                    // Por segurança, se pediu filial e não tem vinculo, nega.
                    // A menos que seja ROLE_ADMIN e implementemos bypass.
                    // Para MVP, assumimos bloqueio.
                    log.warn("Access Denied: User {} has no linked Funcionario to check Filial access", usuario.getEmail());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
