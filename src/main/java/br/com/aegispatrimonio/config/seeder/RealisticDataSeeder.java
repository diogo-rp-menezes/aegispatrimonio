package br.com.aegispatrimonio.config.seeder;

import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.service.PredictiveMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class RealisticDataSeeder implements CommandLineRunner {

    private final AtivoRepository ativoRepository;
    private final FilialRepository filialRepository;
    private final TipoAtivoRepository tipoAtivoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AtivoHealthHistoryRepository ativoHealthHistoryRepository;
    private final PredictiveMaintenanceService predictiveMaintenanceService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (ativoRepository.count() > 0) {
            return; // Já existem dados, pular seed
        }

        System.out.println("🌱 Semeando dados realistas para MVP (Shift Left)...");

        // 1. Carregar dependências (Assumindo que V4_Insert_Seed_Data.sql já rodou)
        Filial filial = filialRepository.findById(1L).orElseThrow();
        TipoAtivo tipoNotebook = tipoAtivoRepository.findById(1L).orElseThrow();
        TipoAtivo tipoMonitor = tipoAtivoRepository.findById(2L).orElseThrow();
        Fornecedor fornecedor = fornecedorRepository.findById(1L).orElseThrow();
        Localizacao localizacao = localizacaoRepository.findById(1L).orElseThrow();
        Funcionario admin = funcionarioRepository.findById(1L).orElseThrow();

        Random random = new Random();

        // 2. Criar 20 Ativos
        for (int i = 1; i <= 20; i++) {
            Ativo ativo = new Ativo();
            boolean isNotebook = i <= 15;
            ativo.setTipoAtivo(isNotebook ? tipoNotebook : tipoMonitor);
            ativo.setNome((isNotebook ? "Notebook Dell Latitude " : "Monitor Dell P2419H ") + i);
            ativo.setNumeroPatrimonio("PAT-" + (1000 + i));
            ativo.setFilial(filial);
            ativo.setLocalizacao(localizacao);
            ativo.setFornecedor(fornecedor);
            ativo.setFuncionarioResponsavel(admin);
            ativo.setStatus(StatusAtivo.ATIVO);
            ativo.setDataAquisicao(LocalDate.now().minusMonths(random.nextInt(24) + 1));
            ativo.setValorAquisicao(new BigDecimal(isNotebook ? 5000 : 1200));
            ativo.setVidaUtilMeses(60);
            ativo.setMetodoDepreciacao(MetodoDepreciacao.LINEAR);
            ativo.setDataRegistro(LocalDate.now());

            // Detalhe Hardware (só para notebooks)
            if (isNotebook) {
                AtivoDetalheHardware hardware = new AtivoDetalheHardware();
                hardware.setAtivo(ativo);
                hardware.setComputerName("DESKTOP-" + (1000 + i));
                hardware.setOsName("Windows 11 Pro");
                hardware.setCpuModel("Intel Core i7-1255U");
                hardware.setCpuCores(10);
                ativo.setDetalheHardware(hardware);
            }

            ativo = ativoRepository.save(ativo);

            // 3. Simular Health History para Notebooks (Predictive Maintenance)
            if (isNotebook) {
                // Cenários:
                // i=1,2: CRITICO (Falha em < 7 dias)
                // i=3,4,5: ALERTA (Falha em < 30 dias)
                // i=6..15: SAUDAVEL ou INDETERMINADO

                List<AtivoHealthHistory> history = new ArrayList<>();
                double startValue;
                double dailyDecrease;

                if (i <= 2) {
                    // CRITICO: 50GB start, -2GB/day -> 25 days to zero.
                    // Se passarmos 20 dias de histórico, sobram 5 dias -> Critico (<7)
                    startValue = 50.0;
                    dailyDecrease = 2.0;
                } else if (i <= 5) {
                    // ALERTA: 100GB start, -2GB/day -> 50 days to zero.
                    // Se passarmos 30 dias de historico, sobram 20 dias -> Alerta (<30)
                    startValue = 100.0;
                    dailyDecrease = 2.0;
                } else {
                    // SAUDAVEL: Stable or slight decrease
                    startValue = 200.0;
                    dailyDecrease = 0.1;
                }

                // Gerar 30 dias de histórico
                for (int d = 30; d >= 0; d--) {
                    AtivoHealthHistory point = new AtivoHealthHistory();
                    point.setAtivo(ativo);
                    point.setComponente("DISK:0");
                    point.setMetrica("FREE_SPACE_GB");
                    point.setDataRegistro(LocalDateTime.now().minusDays(d));

                    double noise = (random.nextDouble() - 0.5) * 0.5; // Ruído +/- 0.25 GB
                    double value = startValue - (dailyDecrease * (30 - d)) + noise;
                    if (value < 0) value = 0.1;

                    point.setValor(value);
                    history.add(point);
                }
                ativoHealthHistoryRepository.saveAll(history);

                // Calcular e Atualizar Predição
                PredictionResult prediction = predictiveMaintenanceService.predictExhaustionDate(history);
                if (prediction != null && prediction.exhaustionDate() != null) {
                    ativo.setPrevisaoEsgotamentoDisco(prediction.exhaustionDate());

                    if (ativo.getAtributos() == null) {
                        ativo.setAtributos(new java.util.HashMap<>());
                    }
                    ativo.getAtributos().put("previsaoEsgotamentoDisco", prediction.exhaustionDate().toString());
                    ativo.getAtributos().put("prediction_slope", prediction.slope());
                    ativo.getAtributos().put("prediction_intercept", prediction.intercept());

                    ativoRepository.save(ativo);
                }
            }
        }
        System.out.println("✅ Dados semeados com sucesso! (20 Ativos)");
    }
}
