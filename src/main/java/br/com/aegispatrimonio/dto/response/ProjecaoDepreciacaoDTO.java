package br.com.aegispatrimonio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjecaoDepreciacaoDTO {
    private LocalDate mesReferencia;
    private BigDecimal depreciacaoMensal;
    private BigDecimal depreciacaoAcumulada;
    private BigDecimal valorContabil;
    private Double percentualDepreciado;
}