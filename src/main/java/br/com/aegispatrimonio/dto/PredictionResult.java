package br.com.aegispatrimonio.dto;

import java.time.LocalDate;

public record PredictionResult(
    LocalDate exhaustionDate,
    double slope,
    double intercept,
    long baseEpochDay
) {}
