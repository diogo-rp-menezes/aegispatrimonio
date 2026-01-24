package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.MetodoDepreciacao;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import jakarta.persistence.EntityManager;
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepreciacaoService {

    private final AtivoRepository ativoRepository;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider
    private final EntityManager entityManager;

    // O construtor manual foi removido, pois @RequiredArgsConstructor já o gera.
    // public DepreciacaoService(AtivoRepository ativoRepository, CurrentUserProvider currentUserProvider) {
    //     this.ativoRepository = ativoRepository;
    //     this.currentUserProvider = currentUserProvider;
    // }

    @Transactional
    @Scheduled(cron = "0 0 2 1 * ?") // Executa no primeiro dia de cada mês às 02:00
    public void calcularDepreciacaoMensalAgendada() {
        log.info("Iniciando job de cálculo de depreciação mensal...");
        try (Stream<Ativo> ativos = ativoRepository.findAllByStatus(StatusAtivo.ATIVO)) {
            List<Ativo> ativosParaAtualizar = ativos
                    .filter(this::isAtivoElegivelParaDepreciacao)
                    .peek(this::aplicarDepreciacaoMensal)
                    .collect(Collectors.toList());

            if (!ativosParaAtualizar.isEmpty()) {
                ativoRepository.saveAll(ativosParaAtualizar);
                log.info("{} ativos foram depreciados com sucesso.", ativosParaAtualizar.size());
            } else {
                log.info("Nenhum ativo elegível para depreciação encontrado.");
            }
        }
    }

    @Transactional
    public void recalcularDepreciacaoTodosAtivos() {
        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} iniciou o recálculo completo da depreciação para todos os ativos.", auditor.getEmail());

        int batchSize = 100;
        int count = 0;
        int totalProcessed = 0;
        List<Ativo> buffer = new ArrayList<>();

        try (Stream<Ativo> ativos = ativoRepository.streamAll()) {
            Iterator<Ativo> iterator = ativos.iterator();
            while (iterator.hasNext()) {
                Ativo ativo = iterator.next();
                recalcularDepreciacaoTotal(ativo);
                buffer.add(ativo);
                count++;

                if (count % batchSize == 0) {
                    ativoRepository.saveAll(buffer);
                    entityManager.flush();
                    entityManager.clear();
                    totalProcessed += buffer.size();
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                ativoRepository.saveAll(buffer);
                entityManager.flush();
                entityManager.clear();
                totalProcessed += buffer.size();
                buffer.clear();
            }
            log.info("Recálculo concluído para {} ativos.", totalProcessed);
        }
    }

    @Transactional
    public void recalcularDepreciacaoCompleta(Long ativoId) {
        Ativo ativo = findAtivoById(ativoId);
        recalcularDepreciacaoTotal(ativo);
        ativoRepository.save(ativo);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} recalculou a depreciação completa para o ativo ID: {}.", auditor.getEmail(), ativoId);
    }

    public BigDecimal calcularDepreciacaoMensal(Long ativoId) {
        // CORREÇÃO: Utiliza o método auxiliar que já trata o caso de 'não encontrado'.
        Ativo ativo = findAtivoById(ativoId);
        if (isAtivoElegivelParaDepreciacao(ativo)) {
            return calcularValorDepreciacaoMensal(ativo);
        }
        return BigDecimal.ZERO;
    }

    private void recalcularDepreciacaoTotal(Ativo ativo) {
        if (ativo.getDataInicioDepreciacao() == null || ativo.getVidaUtilMeses() == null || ativo.getVidaUtilMeses() <= 0) {
            ativo.setDepreciacaoAcumulada(BigDecimal.ZERO);
            ativo.setValorContabilAtual(ativo.getValorAquisicao());
            return;
        }

        long mesesParaDepreciar = ChronoUnit.MONTHS.between(ativo.getDataInicioDepreciacao(), LocalDate.now());
        if (mesesParaDepreciar < 0) mesesParaDepreciar = 0;

        BigDecimal depreciacaoTotal = BigDecimal.ZERO;
        for (int i = 0; i < mesesParaDepreciar; i++) {
            LocalDate dataCalculo = ativo.getDataInicioDepreciacao().plusMonths(i);
            depreciacaoTotal = depreciacaoTotal.add(calcularValorDepreciacaoMensal(ativo, dataCalculo));
        }

        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (depreciacaoTotal.compareTo(valorDepreciavel) > 0) {
            depreciacaoTotal = valorDepreciavel;
        }

        ativo.setDepreciacaoAcumulada(depreciacaoTotal);
        ativo.setValorContabilAtual(ativo.getValorAquisicao().subtract(depreciacaoTotal));
        ativo.setDataUltimaDepreciacao(ativo.getDataInicioDepreciacao().plusMonths(mesesParaDepreciar - 1));
    }

    private void aplicarDepreciacaoMensal(Ativo ativo) {
        BigDecimal depreciacaoEsteMes = calcularValorDepreciacaoMensal(ativo);
        BigDecimal novaDepreciacaoAcumulada = ativo.getDepreciacaoAcumulada().add(depreciacaoEsteMes);

        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (novaDepreciacaoAcumulada.compareTo(valorDepreciavel) > 0) {
            novaDepreciacaoAcumulada = valorDepreciavel;
        }

        ativo.setDepreciacaoAcumulada(novaDepreciacaoAcumulada);
        ativo.setValorContabilAtual(ativo.getValorAquisicao().subtract(novaDepreciacaoAcumulada));
        ativo.setDataUltimaDepreciacao(LocalDate.now());
    }

    private BigDecimal calcularValorDepreciacaoMensal(Ativo ativo) {
        return calcularValorDepreciacaoMensal(ativo, LocalDate.now());
    }

    private BigDecimal calcularValorDepreciacaoMensal(Ativo ativo, LocalDate dataCalculo) {
        BigDecimal valorDepreciavel = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
        if (valorDepreciavel.compareTo(BigDecimal.ZERO) <= 0 || ativo.getVidaUtilMeses() <= 0) {
            return BigDecimal.ZERO;
        }

        if (ativo.getMetodoDepreciacao() == MetodoDepreciacao.ACELERADA) {
            long vidaUtil = ativo.getVidaUtilMeses();
            long somaDigitos = vidaUtil * (vidaUtil + 1) / 2;
            if (somaDigitos == 0) return BigDecimal.ZERO;

            long mesesDecorridos = ChronoUnit.MONTHS.between(ativo.getDataInicioDepreciacao(), dataCalculo);
            if (mesesDecorridos >= vidaUtil) return BigDecimal.ZERO;

            long digitoAtual = vidaUtil - mesesDecorridos;
            BigDecimal taxa = BigDecimal.valueOf(digitoAtual).divide(BigDecimal.valueOf(somaDigitos), 10, RoundingMode.HALF_UP);
            return valorDepreciavel.multiply(taxa);
        }

        // Padrão: MetodoDepreciacao.LINEAR
        return valorDepreciavel.divide(BigDecimal.valueOf(ativo.getVidaUtilMeses()), 10, RoundingMode.HALF_UP);
    }

    private boolean isAtivoElegivelParaDepreciacao(Ativo ativo) {
        LocalDate hoje = LocalDate.now();
        return ativo.getStatus() == StatusAtivo.ATIVO &&
                ativo.getDepreciacaoAcumulada().compareTo(ativo.getValorAquisicao().subtract(ativo.getValorResidual())) < 0 &&
                (ativo.getDataUltimaDepreciacao() == null || !ativo.getDataUltimaDepreciacao().withDayOfMonth(1).isEqual(hoje.withDayOfMonth(1)));
    }

    private Ativo findAtivoById(Long id) {
        return ativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado com o ID: " + id));
    }
}
