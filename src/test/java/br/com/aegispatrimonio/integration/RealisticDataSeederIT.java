package br.com.aegispatrimonio.integration;

import br.com.aegispatrimonio.AegispatrimonioApplication;
import br.com.aegispatrimonio.repository.AtivoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest(classes = AegispatrimonioApplication.class)
@ActiveProfiles("dev")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RealisticDataSeederIT {

    @Autowired
    private AtivoRepository ativoRepository;

    @Test
    void testRealisticDataSeeded() {
        // Set Tenant Context (simulating logged in user)
        br.com.aegispatrimonio.context.TenantContext.setFilialId(1L);

        // 1. Verify total assets seeded
        long totalAtivos = ativoRepository.count();
        Assertions.assertTrue(totalAtivos >= 20, "Should have seeded at least 20 assets, found: " + totalAtivos);

        // 2. Verify Critical Prediction Seeding (i <= 2)
        // These assets are designed to fail in < 7 days from now.
        // Seeder uses: startValue=50, decrease=2.0, daysToZero=25.
        // History generated for last 30 days.
        // So they hit zero 5 days BEFORE today? No.
        // Wait, let's re-read the logic:
        // history value = startValue - (dailyDecrease * (30 - d))
        // d=30 (30 days ago): value = 50 - (2 * 0) = 50
        // d=0 (today): value = 50 - (2 * 30) = -10 (clamped to 0.1)
        // Linear Regression on this will show slope ~ -2.
        // Intercept (at d=30 which is index 0): 50.
        // Equation: y = 50 - 2x (where x is days since 30 days ago).
        // 0 = 50 - 2x => x = 25.
        // So it hits zero 25 days AFTER the first record (which was 30 days ago).
        // So it hit zero 5 days AGO.

        // Wait, if it hit zero 5 days ago, the prediction date is in the PAST.
        // `countCriticalPredictionsByCurrentTenant(criticalThreshold)` counts where `previsao < criticalThreshold`.
        // criticalThreshold is `now.plusDays(7)`.
        // So a past date is definitely < criticalThreshold.

        // But the seeder condition `if (prediction.exhaustionDate() != null)`...
        // `PredictiveMaintenanceService` says:
        // `if (daysToZero <= 0) return null;`
        // daysToZero is relative to `firstDay` (30 days ago).
        // If x=25, and current time is x=30.
        // daysToZero = 25.
        // 25 > 0. So it returns `firstDay + 25 days`.
        // Which is `now - 5 days`.

        // So the date IS in the past.
        // Is that considered "Critical"?
        // `DashboardService`: `previsaoEsgotamentoDisco < criticalThreshold` (now + 7).
        // Yes, (now - 5) < (now + 7). So it is Critical.

        // So we expect countCriticalPredictions > 0.

        LocalDate criticalThreshold = LocalDate.now().plusDays(7);
        long criticalCount = ativoRepository.countCriticalPredictionsByCurrentTenant(criticalThreshold);
        Assertions.assertTrue(criticalCount > 0, "Should have seeded critical predictions, found: " + criticalCount);

        // 3. Verify Warning Prediction Seeding (i <= 5)
        // ALERTA: start=100, dec=2.
        // y = 100 - 2x. 0 = 100 - 2x => x = 50.
        // 50 days from start (30 days ago) => 20 days from NOW.
        // Warning Threshold is `now + 30`.
        // 20 days is < 30. So it is Warning.
        // It is also > 7? Yes.
        // DashboardService uses `countWarningPredictionsByCurrentTenant(criticalThreshold, warningThreshold)`
        // `previsao >= criticalThreshold AND previsao < warningThreshold`.
        // (now + 20) >= (now + 7) AND (now + 20) < (now + 30).
        // Yes.

        LocalDate warningThreshold = LocalDate.now().plusDays(30);
        long warningCount = ativoRepository.countWarningPredictionsByCurrentTenant(criticalThreshold, warningThreshold);
        Assertions.assertTrue(warningCount > 0, "Should have seeded warning predictions, found: " + warningCount);

        br.com.aegispatrimonio.context.TenantContext.clear();
    }
}
