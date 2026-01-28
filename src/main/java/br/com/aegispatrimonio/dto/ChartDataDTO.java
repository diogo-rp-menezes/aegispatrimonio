package br.com.aegispatrimonio.dto;

public record ChartDataDTO(String label, long value) {
    public ChartDataDTO(Object label, long value) {
        this(label != null ? label.toString() : "N/A", value);
    }
}
