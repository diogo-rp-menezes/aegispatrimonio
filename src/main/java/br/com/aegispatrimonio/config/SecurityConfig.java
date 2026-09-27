package br.com.aegispatrimonio.config;

import br.com.aegispatrimonio.security.CustomMethodSecurityExpressionHandler;
import br.com.aegispatrimonio.security.CustomUserDetailsService;
import br.com.aegispatrimonio.security.DelegatedAccessDeniedHandler;
import br.com.aegispatrimonio.security.DelegatedAuthenticationEntryPoint;
import br.com.aegispatrimonio.security.JwtAuthFilter;
import br.com.aegispatrimonio.security.TenantFilter;
import br.com.aegispatrimonio.security.oauth2.CustomOAuth2UserService;
import br.com.aegispatrimonio.security.oauth2.OAuth2AuthenticationSuccessHandler;
import br.com.aegispatrimonio.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Nota L6 (audit): AntPathRequestMatcher foi removido em favor de patterns String,
// que em Spring Security 6.4 + Spring MVC resolvem para MvcRequestMatcher (PathPattern).
// PathPatternRequestMatcher só existe a partir do Spring Security 6.5+ (Boot 3.5+).
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final DelegatedAuthenticationEntryPoint authEntryPoint;
    private final DelegatedAccessDeniedHandler accessDeniedHandler;
    private final IPermissionService permissionService;
    private final PermissionEvaluator permissionEvaluator;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Value("${app.cors.allowed-origins:http://localhost:8080,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // CORREÇÃO: Configuração explícita para tratar exceções de segurança
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint) // Para falhas de autenticação (401)
                        .accessDeniedHandler(accessDeniedHandler) // Para falhas de autorização (403)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Protected API
                        .requestMatchers("/api/**").authenticated()
                        // Actuator: only health probes are public (k8s/docker liveness/readiness);
                        // everything else (prometheus, metrics, env, ...) requires ROLE_ADMIN
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Public/System
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        // Embedded SPA (served from classpath:/static): shell, assets and PWA files.
                        // SPA routes (e.g. /dashboard) are forwarded to /index.html by SpaWebFilter.
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/manifest.webmanifest").permitAll()
                        .requestMatchers("/pwa-*.png").permitAll()
                        // Default deny: anything not explicitly allowed above requires authentication
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new TenantFilter(), JwtAuthFilter.class)
                .addFilterAfter(new br.com.aegispatrimonio.security.TenantAccessFilter(), JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Filial-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        CustomMethodSecurityExpressionHandler handler = new CustomMethodSecurityExpressionHandler(permissionService);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
