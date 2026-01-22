package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DashboardStatsDTO;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final AtivoRepository ativoRepository;
    private final LocalizacaoRepository localizacaoRepository;

    public DashboardService(AtivoRepository ativoRepository, LocalizacaoRepository localizacaoRepository) {
        this.ativoRepository = ativoRepository;
        this.localizacaoRepository = localizacaoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        long totalAtivos = ativoRepository.countByCurrentTenant();
        long ativosEmManutencao = ativoRepository.countByStatusAndCurrentTenant(StatusAtivo.EM_MANUTENCAO);
        BigDecimal valorTotal = ativoRepository.getValorTotalByCurrentTenant();
        long totalLocalizacoes = localizacaoRepository.countByCurrentTenant();

        if (valorTotal == null) {
            valorTotal = BigDecimal.ZERO;
        }

        return new DashboardStatsDTO(
            totalAtivos,
            ativosEmManutencao,
            valorTotal,
            totalLocalizacoes
        );
    }
}
