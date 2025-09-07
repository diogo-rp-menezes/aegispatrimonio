package br.com.aegispatrimonio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo; // IMPORT ADICIONADO
import br.com.aegispatrimonio.repository.AtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DepreciacaoService {

    private final AtivoRepository ativoRepository;

    /**
     * Calcula a depreciação mensal de todos os ativos ativos
     * Executa automaticamente no primeiro dia de cada mês
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void calcularDepreciacaoMensal() {
        log.info("Iniciando cálculo de depreciação mensal...");
        
        List<Ativo> ativosAtivos = ativoRepository.findByStatus(StatusAtivo.ATIVO);
        
        int ativosProcessados = 0;
        int ativosDepreciados = 0;
        
        for (Ativo ativo : ativosAtivos) {
            if (deveCalcularDepreciacao(ativo)) {
                calcularDepreciacaoParaAtivo(ativo);
                ativosDepreciados++;
            }
            ativosProcessados++;
        }
        
        log.info("Cálculo de depreciação concluído. Processados: {}, Depreciados: {}", 
                ativosProcessados, ativosDepreciados);
    }

    /**
     * Calcula a depreciação para um ativo específico
     */
    @Transactional
    public void calcularDepreciacaoParaAtivo(Ativo ativo) {
        try {
            BigDecimal depreciacaoMensal = calcularDepreciacaoMensal(ativo);
            BigDecimal depreciacaoAcumulada = ativo.getDepreciacaoAcumulada() != null ? 
                    ativo.getDepreciacaoAcumulada() : BigDecimal.ZERO;
            
            BigDecimal novaDepreciacaoAcumulada = depreciacaoAcumulada.add(depreciacaoMensal);
            ativo.setDepreciacaoAcumulada(novaDepreciacaoAcumulada);
            
            BigDecimal valorContabilAtual = ativo.getValorAquisicao().subtract(novaDepreciacaoAcumulada);
            ativo.setValorContabilAtual(valorContabilAtual);
            
            ativo.setDataUltimaDepreciacao(LocalDate.now());
            ativoRepository.save(ativo);
            
            log.debug("Depreciação calculada para ativo {}: R$ {}", 
                    ativo.getNumeroPatrimonio(), depreciacaoMensal);
            
        } catch (Exception e) {
            log.error("Erro ao calcular depreciação para ativo {}: {}", 
                    ativo.getNumeroPatrimonio(), e.getMessage());
        }
    }

    /**
     * Calcula o valor da depreciação mensal para um ativo
     */
    public BigDecimal calcularDepreciacaoMensal(Ativo ativo) {
        if (!deveCalcularDepreciacao(ativo)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        
        switch (ativo.getMetodoDepreciacao()) {
            case LINEAR:
                return calcularDepreciacaoLinear(valorDepreciavel, ativo.getVidaUtilMeses());
            
            case ACELERADA:
                return calcularDepreciacaoAcelerada(ativo, valorDepreciavel);
            
            default:
                return calcularDepreciacaoLinear(valorDepreciavel, ativo.getVidaUtilMeses());
        }
    }

    /**
     * Calcula depreciação pelo método linear
     */
    private BigDecimal calcularDepreciacaoLinear(BigDecimal valorDepreciavel, Integer vidaUtilMeses) {
        if (vidaUtilMeses == null || vidaUtilMeses <= 0) {
            return BigDecimal.ZERO;
        }
        
        return valorDepreciavel.divide(
            BigDecimal.valueOf(vidaUtilMeses), 
            10, 
            RoundingMode.HALF_UP
        );
    }

    /**
     * Calcula depreciação pelo método acelerado (exemplo: soma dos dígitos)
     */
    private BigDecimal calcularDepreciacaoAcelerada(Ativo ativo, BigDecimal valorDepreciavel) {
        // Implementação simplificada do método acelerado
        // Usando método da soma dos dígitos dos anos
        
        if (ativo.getVidaUtilMeses() == null || ativo.getVidaUtilMeses() <= 0) {
            return BigDecimal.ZERO;
        }
        
        long mesesDecorridos = ChronoUnit.MONTHS.between(
            ativo.getDataInicioDepreciacao().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(1)
        );
        
        if (mesesDecorridos <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Método da soma dos dígitos (exemplo)
        int vidaUtil = ativo.getVidaUtilMeses();
        int somaDigitos = vidaUtil * (vidaUtil + 1) / 2;
        int digitoAtual = (int) (vidaUtil - mesesDecorridos + 1);
        
        if (digitoAtual <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal taxa = BigDecimal.valueOf(digitoAtual)
                .divide(BigDecimal.valueOf(somaDigitos), 10, RoundingMode.HALF_UP);
        
        return valorDepreciavel.multiply(taxa);
    }

    /**
     * Verifica se o ativo deve ter depreciação calculada
     */
    public boolean deveCalcularDepreciacao(Ativo ativo) {
        // Verificar se o ativo está ativo
        if (ativo.getStatus() != StatusAtivo.ATIVO) {
            return false;
        }
        
        // Verificar se tem data de início de depreciação
        if (ativo.getDataInicioDepreciacao() == null) {
            return false;
        }
        
        // Verificar se a data de início é anterior ou igual à data atual
        if (ativo.getDataInicioDepreciacao().isAfter(LocalDate.now())) {
            return false;
        }
        
        // Verificar se ainda há vida útil restante
        if (ativo.getVidaUtilMeses() == null || ativo.getVidaUtilMeses() <= 0) {
            return false;
        }
        
        // Verificar se já atingiu o valor residual
        BigDecimal depreciacaoAcumulada = ativo.getDepreciacaoAcumulada() != null ? 
                ativo.getDepreciacaoAcumulada() : BigDecimal.ZERO;
        
        BigDecimal valorDepreciavelMaximo = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        
        return depreciacaoAcumulada.compareTo(valorDepreciavelMaximo) < 0;
    }

    /**
     * Recalcula a depreciação acumulada para um ativo desde a data de início
     */
    @Transactional
    public void recalcularDepreciacaoCompleta(Ativo ativo) {
        if (!deveCalcularDepreciacao(ativo)) {
            return;
        }
        
        LocalDate dataInicio = ativo.getDataInicioDepreciacao();
        LocalDate dataAtual = LocalDate.now();
        
        long mesesDecorridos = ChronoUnit.MONTHS.between(
            dataInicio.withDayOfMonth(1),
            dataAtual.withDayOfMonth(1)
        );
        
        if (mesesDecorridos <= 0) {
            ativo.setDepreciacaoAcumulada(BigDecimal.ZERO);
            ativo.setValorContabilAtual(ativo.getValorAquisicao());
            return;
        }
        
        BigDecimal depreciacaoMensal = calcularDepreciacaoMensal(ativo);
        BigDecimal depreciacaoTotal = depreciacaoMensal.multiply(BigDecimal.valueOf(mesesDecorridos));
        
        // Não permite depreciação além do valor depreciável
        BigDecimal valorDepreciavelMaximo = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (depreciacaoTotal.compareTo(valorDepreciavelMaximo) > 0) {
            depreciacaoTotal = valorDepreciavelMaximo;
        }
        
        ativo.setDepreciacaoAcumulada(depreciacaoTotal);
        ativo.setValorContabilAtual(ativo.getValorAquisicao().subtract(depreciacaoTotal));
        ativo.setDataUltimaDepreciacao(dataAtual);
        
        ativoRepository.save(ativo);
    }

    /**
     * Recalcula a depreciação para todos os ativos
     */
    @Transactional
    public void recalcularDepreciacaoTodosAtivos() {
        log.info("Iniciando recálculo completo de depreciação...");
        
        List<Ativo> ativos = ativoRepository.findAll();
        int ativosProcessados = 0;
        
        for (Ativo ativo : ativos) {
            try {
                recalcularDepreciacaoCompleta(ativo);
                ativosProcessados++;
            } catch (Exception e) {
                log.error("Erro ao recalculcar depreciação para ativo {}: {}", 
                        ativo.getNumeroPatrimonio(), e.getMessage());
            }
        }
        
        log.info("Recálculo de depreciação concluído. Ativos processados: {}", ativosProcessados);
    }

    /**
     * Recalcula depreciação para um ativo específico (sobrecarga para o controller)
     */
    @Transactional
    public void recalcularDepreciacaoCompleta(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + ativoId));
        recalcularDepreciacaoCompleta(ativo);
    }

    /**
     * Calcula depreciação mensal para um ativo específico (sobrecarga para o controller)
     */
    public BigDecimal calcularDepreciacaoMensal(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + ativoId));
        return calcularDepreciacaoMensal(ativo);
    }
}