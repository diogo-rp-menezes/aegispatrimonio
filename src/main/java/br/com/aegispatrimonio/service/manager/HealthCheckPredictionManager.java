package br.com.aegispatrimonio.service.manager;

import br.com.aegispatrimonio.model.Ativo;

import java.util.List;

/**
 * Interface for managing predictive maintenance logic during Health Check processing.
 */
public interface HealthCheckPredictionManager {

    /**
     * Processes predictive analysis for the given asset based on historical data of the specified components.
     * Checks if prediction is needed (throttling), fetches history, calculates prediction,
     * updates asset attributes, and dispatches maintenance if necessary.
     *
     * @param ativo The asset to analyze and update.
     * @param componentsToFetch The list of component identifiers (e.g. "DISK:SN123") to fetch history for.
     */
    void processPrediction(Ativo ativo, List<String> componentsToFetch);
}
