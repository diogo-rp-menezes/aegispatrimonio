package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.model.AtivoHealthHistory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service responsible for predictive maintenance analysis using simple regression models.
 * Implements "Shift Left" by using deterministic math instead of LLMs.
 */
@Service
public class PredictiveMaintenanceService {

    /**
     * Predicts the date when the metric value will reach zero using Simple Linear Regression (Least Squares).
     * Useful for disk usage forecasting.
     *
     * @param history List of historical data points, ordered by date.
     * @return PredictionResult containing exhaustion date and model parameters, or null if slope is positive/flat or insufficient data.
     */
    public PredictionResult predictExhaustionDate(List<AtivoHealthHistory> history) {
        if (history == null || history.size() < 2) {
            return null; // Not enough data points
        }

        // Simple Linear Regression: y = alpha + beta * x
        // x = time (days from first record), y = value

        long firstDay = history.get(0).getDataRegistro().toLocalDate().toEpochDay();
        int n = history.size();

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;

        for (AtivoHealthHistory point : history) {
            double x = point.getDataRegistro().toLocalDate().toEpochDay() - firstDay;
            double y = point.getValor();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denominator = n * sumXX - sumX * sumX;
        if (Math.abs(denominator) < 1e-10) {
            return null; // Vertical line or single X point
        }

        double beta = (n * sumXY - sumX * sumY) / denominator; // Slope
        double alpha = (sumY - beta * sumX) / n; // Intercept

        // If slope is >= 0, it's not decreasing (not emptying)
        if (beta >= -1e-10) {
            return null;
        }

        // Solve for y = 0: 0 = alpha + beta * x  =>  x = -alpha / beta
        double daysToZero = -alpha / beta;

        if (daysToZero <= 0) {
             return null; // Already exhausted in the past (before first record)
        }

        LocalDate exhaustionDate = LocalDate.ofEpochDay(firstDay + (long) daysToZero);
        return new PredictionResult(exhaustionDate, beta, alpha, firstDay);
    }
}
