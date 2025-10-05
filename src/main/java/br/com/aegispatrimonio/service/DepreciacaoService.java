package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DepreciacaoService {

    private final AtivoRepository ativoRepository;

    /**
     * Otimizado: Calcula a depreciação mensal de todos os ativos.
     * Usa streaming para baixo consumo de memória e batch update para performance.
     * Executa automaticamente no primeiro dia de cada mês.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void calcularDepreciacaoMensal() {
        log.info("Iniciando cálculo de depreciação mensal...");
        List<Ativo> ativosParaSalvar = new ArrayList<>();

        // Otimização 1: Usar Stream para não carregar todos os ativos na memória.
        try (Stream<Ativo> ativosStream = ativoRepository.streamByStatus(StatusAtivo.ATIVO)) {
            ativosStream.forEach(ativo -> {
                if (deveCalcularDepreciacao(ativo)) {
                    try {
                        prepararDepreciacaoParaAtivo(ativo);
                        ativosParaSalvar.add(ativo);
                        log.debug("Depreciação preparada para ativo {}.", ativo.getNumeroPatrimonio());
                    } catch (Exception e) {
                        log.error("Erro ao preparar depreciação para ativo {}: {}",
                                ativo.getNumeroPatrimonio(), e.getMessage());
                    }
                }
            });
        }

        // Otimização 2: Salvar todos os ativos modificados em uma única operação (batch update).
        if (!ativosParaSalvar.isEmpty()) {
            ativoRepository.saveAll(ativosParaSalvar);
            log.info("Cálculo de depreciação concluído. {} ativos foram depreciados.", ativosParaSalvar.size());
        } else {
            log.info("Cálculo de depreciação concluído. Nenhum ativo precisou ser depreciado.");
        }
    }

    /**
     * Otimizado: Recalcula a depreciação para todos os ativos.
     * Usa streaming para baixo consumo de memória e batch update para performance.
     */
    @Transactional
    public void recalcularDepreciacaoTodosAtivos() {
        log.info("Iniciando recálculo completo de depreciação...");
        List<Ativo> ativosParaSalvar = new ArrayList<>();

        // Otimização 1: Usar Stream para não carregar todos os ativos na memória.
        try (Stream<Ativo> ativosStream = ativoRepository.streamAll()) {
            ativosStream.forEach(ativo -> {
                try {
                    prepararRecalculoCompleto(ativo);
                    ativosParaSalvar.add(ativo);
                } catch (Exception e) {
                    log.error("Erro ao recalcular depreciação para ativo {}: {}",
                            ativo.getNumeroPatrimonio(), e.getMessage());
                }
            });
        }

        // Otimização 2: Salvar todos os ativos modificados em uma única operação (batch update).
        if (!ativosParaSalvar.isEmpty()) {
            ativoRepository.saveAll(ativosParaSalvar);
            log.info("Recálculo de depreciação concluído. {} ativos foram processados.", ativosParaSalvar.size());
        } else {
            log.info("Recálculo de depreciação concluído. Nenhum ativo foi processado.");
        }
    }

    /**
     * Prepara a depreciação para um ativo específico, mas não o salva.
     */
    private void prepararDepreciacaoParaAtivo(Ativo ativo) {
        BigDecimal depreciacaoMensal = calcularValorDepreciacaoMensal(ativo);
        BigDecimal depreciacaoAcumulada = ativo.getDepreciacaoAcumulada() != null ?
                ativo.getDepreciacaoAcumulada() : BigDecimal.ZERO;

        BigDecimal novaDepreciacaoAcumulada = depreciacaoAcumulada.add(depreciacaoMensal);

        BigDecimal valorDepreciavelMaximo = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (novaDepreciacaoAcumulada.compareTo(valorDepreciavelMaximo) > 0) {
            novaDepreciacaoAcumulada = valorDepreciavelMaximo;
        }

        ativo.setDepreciacaoAcumulada(novaDepreciacaoAcumulada);
        ativo.setValorContabilAtual(ativo.getValorAquisicao().subtract(novaDepreciacaoAcumulada));
        ativo.setDataUltimaDepreciacao(LocalDate.now());
    }

    /**
     * Prepara o recálculo da depreciação acumulada para um ativo, mas não o salva.
     */
    private void prepararRecalculoCompleto(Ativo ativo) {
        if (ativo.getDataInicioDepreciacao() == null) {
            return; 
        }

        LocalDate dataInicio = ativo.getDataInicioDepreciacao();
        LocalDate dataAtual = LocalDate.now();

        long mesesDecorridos = ChronoUnit.MONTHS.between(
                dataInicio.withDayOfMonth(1),
                dataAtual.withDayOfMonth(1)
        );

        if (mesesDecorridos < 0) {
             mesesDecorridos = 0;
        }

        BigDecimal depreciacaoMensal = calcularValorDepreciacaoMensal(ativo);
        BigDecimal depreciacaoTotal = depreciacaoMensal.multiply(BigDecimal.valueOf(mesesDecorridos));

        BigDecimal valorDepreciavelMaximo = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (depreciacaoTotal.compareTo(valorDepreciavelMaximo) > 0) {
            depreciacaoTotal = valorDepreciavelMaximo;
        }

        ativo.setDepreciacaoAcumulada(depreciacaoTotal);
        ativo.setValorContabilAtual(ativo.getValorAquisicao().subtract(depreciacaoTotal));
        ativo.setDataUltimaDepreciacao(dataAtual);
    }

    // MÉTODOS DE CÁLCULO E VALIDAÇÃO (LÓGICA PURA)

    public BigDecimal calcularValorDepreciacaoMensal(Ativo ativo) {
        if (!deveCalcularDepreciacao(ativo)) {
            return BigDecimal.ZERO;
        }

        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());

        if (ativo.getMetodoDepreciacao() == null) {
            return BigDecimal.ZERO;
        }

        switch (ativo.getMetodoDepreciacao()) {
            case LINEAR:
                return calcularDepreciacaoLinear(valorDepreciavel, ativo.getVidaUtilMeses());
            case ACELERADA:
                return calcularDepreciacaoAcelerada(ativo, valorDepreciavel);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calcularDepreciacaoLinear(BigDecimal valorDepreciavel, Integer vidaUtilMeses) {
        if (vidaUtilMeses == null || vidaUtilMeses <= 0) {
            return BigDecimal.ZERO;
        }
        return valorDepreciavel.divide(BigDecimal.valueOf(vidaUtilMeses), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDepreciacaoAcelerada(Ativo ativo, BigDecimal valorDepreciavel) {
        if (ativo.getVidaUtilMeses() == null || ativo.getVidaUtilMeses() <= 0 || ativo.getDataInicioDepreciacao() == null) {
            return BigDecimal.ZERO;
        }
        long mesesDecorridos = ChronoUnit.MONTHS.between(ativo.getDataInicioDepreciacao().withDayOfMonth(1), LocalDate.now().withDayOfMonth(1));
        if (mesesDecorridos < 0) {
            return BigDecimal.ZERO;
        }
        int vidaUtil = ativo.getVidaUtilMeses();
        int somaDigitos = vidaUtil * (vidaUtil + 1) / 2;
        if (somaDigitos == 0) return BigDecimal.ZERO;
        int digitoAtual = (int) (vidaUtil - mesesDecorridos);
        if (digitoAtual <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal taxa = BigDecimal.valueOf(digitoAtual).divide(BigDecimal.valueOf(somaDigitos), 10, RoundingMode.HALF_UP);
        return valorDepreciavel.multiply(taxa);
    }

    public boolean deveCalcularDepreciacao(Ativo ativo) {
        if (ativo.getStatus() != StatusAtivo.ATIVO || ativo.getDataInicioDepreciacao() == null ||
            ativo.getDataInicioDepreciacao().isAfter(LocalDate.now()) ||
            ativo.getVidaUtilMeses() == null || ativo.getVidaUtilMeses() <= 0) {
            return false;
        }
        BigDecimal depreciacaoAcumulada = ativo.getDepreciacaoAcumulada() != null ? ativo.getDepreciacaoAcumulada() : BigDecimal.ZERO;
        BigDecimal valorDepreciavelMaximo = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        return depreciacaoAcumulada.compareTo(valorDepreciavelMaximo) < 0;
    }

    // MÉTODOS DE PONTO DE ENTRADA (SOBRECARGAS PARA O CONTROLLER)

    @Transactional
    public void recalcularDepreciacaoCompleta(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + ativoId));
        prepararRecalculoCompleto(ativo);
        ativoRepository.save(ativo);
    }

    public BigDecimal calcularDepreciacaoMensal(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + ativoId));
        return calcularValorDepreciacaoMensal(ativo);
    }
}