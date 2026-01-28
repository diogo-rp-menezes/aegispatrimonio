package br.com.aegispatrimonio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SpaWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Forward non-API, non-static GET requests to index.html for SPA routing
        if ("GET".equals(request.getMethod()) &&
            !path.startsWith("/api") &&
            !path.startsWith("/actuator") &&
            !path.startsWith("/swagger-ui") &&
            !path.startsWith("/v3/api-docs") &&
            !path.contains(".") &&
            !path.equals("/")) {

            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}
