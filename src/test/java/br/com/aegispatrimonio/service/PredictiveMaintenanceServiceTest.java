package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.AtivoHealthHistory;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PredictiveMaintenanceServiceTest {

    private final PredictiveMaintenanceService service = new PredictiveMaintenanceService();

    @Test
    void predictExhaustionDate_ShouldReturnDate_WhenTrendIsDecreasing() {
        List<AtivoHealthHistory> history = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now().minusDays(10);

        // Linear decrease: 100, 90, 80...
        // y = 100 - 10x
        for (int i = 0; i < 5; i++) {
            AtivoHealthHistory h = new AtivoHealthHistory();
            h.setDataRegistro(start.plusDays(i));
            h.setValor(100.0 - (10.0 * i));
            history.add(h);
        }

        LocalDate predicted = service.predictExhaustionDate(history);

        assertNotNull(predicted);
        // Should reach 0 at day 10 (start + 10 days)
        assertEquals(start.toLocalDate().plusDays(10), predicted);
    }

    @Test
    void predictExhaustionDate_ShouldReturnNull_WhenTrendIsIncreasing() {
        List<AtivoHealthHistory> history = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            AtivoHealthHistory h = new AtivoHealthHistory();
            h.setDataRegistro(start.plusDays(i));
            h.setValor(50.0 + (10.0 * i));
            history.add(h);
        }

        LocalDate predicted = service.predictExhaustionDate(history);
        assertNull(predicted, "Should return null for increasing trend (disk clearing up)");
    }

    @Test
    void predictExhaustionDate_ShouldReturnNull_WhenInsufficientData() {
        List<AtivoHealthHistory> history = new ArrayList<>();
        history.add(new AtivoHealthHistory());

        LocalDate predicted = service.predictExhaustionDate(history);
        assertNull(predicted);
    }
}
