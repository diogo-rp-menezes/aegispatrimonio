package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro responsável por interceptar o header X-Filial-ID e configurar o contexto de isolamento.
 * Implementa a camada de "Orquestração" do Synaptic Switching.
 */
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    private static final String TENANT_HEADER = "X-Filial-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String filialIdStr = request.getHeader(TENANT_HEADER);

        if (filialIdStr != null && !filialIdStr.isBlank()) {
            try {
                Long filialId = Long.parseLong(filialIdStr);
                TenantContext.setFilialId(filialId);
                log.debug("Context switch: Filial ID set to {}", filialId);
            } catch (NumberFormatException e) {
                log.warn("Invalid Tenant Header format: {}", filialIdStr);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
