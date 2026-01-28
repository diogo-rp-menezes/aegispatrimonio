package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepreciacaoServiceBenchmarkTest {

    @Mock
    private AtivoRepository ativoRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DepreciacaoService depreciacaoService;

    @BeforeEach
    void setUp() {
        Usuario adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setEmail("admin@test.com");
        lenient().when(currentUserProvider.getCurrentUsuario()).thenReturn(adminUser);
    }

    @Test
    @DisplayName("Performance: Deve processar em lotes para evitar OutOfMemory")
    void recalcularDepreciacaoTodosAtivos_deveProcessarEmLotes() {
        // Criar 250 ativos (Batch size esperado: 100)
        List<Ativo> ativos = new ArrayList<>();
        IntStream.range(0, 250).forEach(i -> {
            Ativo a = new Ativo();
            a.setId((long) i);
            a.setStatus(StatusAtivo.ATIVO);
            a.setValorAquisicao(new BigDecimal("1000"));
            a.setValorResidual(BigDecimal.ZERO);
            a.setVidaUtilMeses(100);
            a.setDataInicioDepreciacao(LocalDate.now().minusMonths(10));
            a.setMetodoDepreciacao(MetodoDepreciacao.LINEAR);
            a.setDepreciacaoAcumulada(BigDecimal.ZERO);
            ativos.add(a);
        });

        when(ativoRepository.streamAll()).thenReturn(ativos.stream());

        depreciacaoService.recalcularDepreciacaoTodosAtivos();

        // Espera-se 3 chamadas para 250 itens com batch de 100:
        // 1. 100 itens
        // 2. 100 itens
        // 3. 50 itens
        verify(ativoRepository, times(3)).saveAll(any());
        verify(entityManager, times(3)).flush();
        verify(entityManager, times(3)).clear();
    }
}
