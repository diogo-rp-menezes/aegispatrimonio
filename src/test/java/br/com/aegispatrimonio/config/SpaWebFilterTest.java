package br.com.aegispatrimonio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaWebFilterTest {

    @InjectMocks
    private SpaWebFilter spaWebFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Test
    void doFilterInternal_shouldForwardToIndex_whenPathIsSpaRoute() throws Exception {
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        spaWebFilter.doFilterInternal(request, response, chain);

        verify(requestDispatcher).forward(request, response);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldChain_whenPathIsApi() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/ativos");
        when(request.getMethod()).thenReturn("GET"); // Ensure path check causes the skip

        spaWebFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(any());
    }

    @Test
    void doFilterInternal_shouldChain_whenPathIsStaticFile() throws Exception {
        when(request.getRequestURI()).thenReturn("/assets/style.css");
        when(request.getMethod()).thenReturn("GET");

        spaWebFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldChain_whenPathIsRoot() throws Exception {
        when(request.getRequestURI()).thenReturn("/");
        when(request.getMethod()).thenReturn("GET");

        spaWebFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
