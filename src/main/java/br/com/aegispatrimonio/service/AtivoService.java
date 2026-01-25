package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoNameDTO;
import br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
import br.com.aegispatrimonio.dto.PredictionResult;
import br.com.aegispatrimonio.mapper.AtivoMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final AtivoMapper ativoMapper;
    private final TipoAtivoRepository tipoAtivoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final FilialRepository filialRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final DepreciacaoService depreciacaoService;
    private final CurrentUserProvider currentUserProvider;
    private final AtivoHealthHistoryRepository healthHistoryRepository;
    private final PredictiveMaintenanceService predictiveMaintenanceService;
    private final SearchOptimizationService searchOptimizationService;
    private final AlertNotificationService alertNotificationService;

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper, TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository, FornecedorRepository fornecedorRepository, FuncionarioRepository funcionarioRepository, FilialRepository filialRepository, ManutencaoRepository manutencaoRepository, MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService, CurrentUserProvider currentUserProvider, AtivoHealthHistoryRepository healthHistoryRepository, PredictiveMaintenanceService predictiveMaintenanceService, SearchOptimizationService searchOptimizationService, AlertNotificationService alertNotificationService) {
        this.ativoRepository = ativoRepository;
        this.ativoMapper = ativoMapper;
        this.tipoAtivoRepository = tipoAtivoRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.filialRepository = filialRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.depreciacaoService = depreciacaoService;
        this.currentUserProvider = currentUserProvider;
        this.healthHistoryRepository = healthHistoryRepository;
        this.predictiveMaintenanceService = predictiveMaintenanceService;
        this.searchOptimizationService = searchOptimizationService;
        this.alertNotificationService = alertNotificationService;
    }

    private Usuario getUsuarioLogado() {
        return currentUserProvider.getCurrentUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }

    @Transactional(readOnly = true)
    public Page<AtivoDTO> listarTodos(org.springframework.data.domain.Pageable pageable,
                                      Long filialId,
                                      Long tipoAtivoId,
                                      StatusAtivo status,
                                      String nome,
                                      String health) {
        Usuario usuarioLogado = getUsuarioLogado();
        org.springframework.data.domain.Pageable effectivePageable =
                (pageable == null) ? org.springframework.data.domain.Pageable.unpaged() : pageable;
        boolean isFuzzySearch = (nome != null && !nome.isBlank());

        // Calculate Date Range for Predictive Health
        LocalDate minDate = null;
        LocalDate maxDate = null;
        if (health != null) {
            LocalDate now = LocalDate.now();
            switch (health) {
                case "CRITICO" -> maxDate = now.plusDays(7);
                case "ALERTA" -> {
                    minDate = now.plusDays(7);
                    maxDate = now.plusDays(30);
                }
                case "SAUDAVEL" -> minDate = now.plusDays(30);
            }
        }

        // 1. Resolve Scope (Admin vs User Filiais)
        Set<Long> userFiliais = null;
        boolean isAdmin = isAdmin(usuarioLogado);

        if (!isAdmin) {
            Funcionario funcionarioPrincipal = usuarioLogado.getFuncionario();
            if (funcionarioPrincipal == null || funcionarioPrincipal.getId() == null) {
                throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
            }
            // Fetch fresh from DB to get Lazy collections
            Optional<Funcionario> fOpt = funcionarioRepository.findById(funcionarioPrincipal.getId());
            if (fOpt.isPresent()) {
                userFiliais = fOpt.get().getFiliais().stream().map(Filial::getId).collect(Collectors.toSet());
                if (userFiliais.isEmpty()) throw new AccessDeniedException("Usuário não está associado a nenhuma filial.");
            } else {
                throw new AccessDeniedException("Funcionário não encontrado.");
            }
        }

        // 2. Fuzzy Search Path (Shift Left Optimization)
        if (isFuzzySearch) {
            List<AtivoNameDTO> candidates;
            // Fetch candidates matching other filters, ignoring name (limited to 1000 for safety)
            org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 1000);

            if (isAdmin) {
                candidates = ativoRepository.findSimpleByFilters(filialId, tipoAtivoId, status, minDate, maxDate, limit);
            } else {
                candidates = ativoRepository.findSimpleByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, minDate, maxDate, limit);
            }

            // Rank in memory using Levenshtein distance
            List<AtivoNameDTO> ranked = searchOptimizationService.rankResults(nome, candidates, AtivoNameDTO::nome);

            // Manual Pagination
            int start = (int) effectivePageable.getOffset();
            int end = Math.min((start + effectivePageable.getPageSize()), ranked.size());

            if (start > ranked.size()) {
                 return new PageImpl<>(List.of(), effectivePageable, ranked.size());
            }

            List<AtivoNameDTO> pageContentDTOs = ranked.subList(start, end);
            List<Long> ids = pageContentDTOs.stream().map(AtivoNameDTO::id).collect(Collectors.toList());

            List<Ativo> fullEntities = ativoRepository.findAllByIdInWithDetails(ids);

            // Sort entities to match ranking order
            Map<Long, Ativo> entityMap = fullEntities.stream().collect(Collectors.toMap(Ativo::getId, Function.identity()));
            List<AtivoDTO> pageContent = ids.stream()
                .map(entityMap::get)
                .filter(Objects::nonNull)
                .map(ativoMapper::toDTO)
                .collect(Collectors.toList());

            return new PageImpl<>(pageContent, effectivePageable, ranked.size());
        }

        // 3. Original Path (Strict / DB Paged)
        boolean unpaged = effectivePageable.isUnpaged();
        boolean hasFilters = (filialId != null) || (tipoAtivoId != null) || (status != null) || (health != null);

        if (isAdmin) {
            if (unpaged && !hasFilters) {
                return new PageImpl<>(ativoRepository.findAllWithDetails().stream().map(ativoMapper::toDTO).collect(Collectors.toList()));
            }
            return ativoRepository.findByFilters(filialId, tipoAtivoId, status, null, minDate, maxDate, effectivePageable).map(ativoMapper::toDTO);
        } else {
            if (unpaged && !hasFilters) {
                return new PageImpl<>(ativoRepository.findByFilialIdInWithDetails(userFiliais).stream().map(ativoMapper::toDTO).collect(Collectors.toList()));
            }
            return ativoRepository.findByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, null, minDate, maxDate, effectivePageable).map(ativoMapper::toDTO);
        }
    }

    @Transactional(readOnly = true)
    public AtivoDTO buscarPorId(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Ativo ativo = ativoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioPrincipal = usuarioLogado.getFuncionario();
            if (funcionarioPrincipal == null || funcionarioPrincipal.getId() == null) {
                throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
            }
            Optional<Funcionario> funcionarioOpt = funcionarioRepository.findById(funcionarioPrincipal.getId());
            if (funcionarioOpt.isPresent()) {
                Funcionario funcionarioLogado = funcionarioOpt.get();
                if (funcionarioLogado.getFiliais().stream().noneMatch(f -> f.getId().equals(ativo.getFilial().getId()))) {
                    throw new AccessDeniedException("Você não tem permissão para acessar ativos desta filial.");
                }
            }
        }

        return ativoMapper.toDTO(ativo);
    }

    @Transactional
    public void processarHealthCheck(Long id, HealthCheckPayloadDTO payload) {
        Ativo ativo = ativoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        // Update basic hardware details
        AtivoDetalheHardwareDTO hardwareDTO = new AtivoDetalheHardwareDTO(
                payload.computerName(), payload.domain(), payload.osName(), payload.osVersion(),
                payload.osArchitecture(), payload.motherboardManufacturer(), payload.motherboardModel(),
                payload.motherboardSerialNumber(), payload.cpuModel(), payload.cpuCores(), payload.cpuThreads()
        );
        gerenciarDetalheHardware(ativo, hardwareDTO);

        // Process Disks and History
        if (payload.discos() != null) {
            List<AtivoHealthHistory> historyToSave = new java.util.ArrayList<>();
            List<String> componentsToFetch = new java.util.ArrayList<>();

            for (DiskInfoDTO disk : payload.discos()) {
                if (disk.freeGb() != null) {
                    AtivoHealthHistory history = new AtivoHealthHistory();
                    history.setAtivo(ativo);
                    history.setComponente("DISK:" + disk.serial());
                    history.setMetrica("FREE_SPACE_GB");
                    history.setValor(disk.freeGb());
                    historyToSave.add(history);
                    componentsToFetch.add("DISK:" + disk.serial());
                }
            }

            if (!historyToSave.isEmpty()) {
                healthHistoryRepository.saveAll(historyToSave);

                // Throttling: Check if prediction was calculated recently (< 24h)
                boolean shouldRecalculate = true;
                if (ativo.getAtributos() != null && ativo.getAtributos().containsKey("prediction_calculated_at")) {
                    try {
                        java.time.LocalDateTime lastCalc = java.time.LocalDateTime.parse(ativo.getAtributos().get("prediction_calculated_at").toString());
                        if (lastCalc.plusHours(24).isAfter(java.time.LocalDateTime.now())) {
                            shouldRecalculate = false;
                        }
                    } catch (Exception e) {
                        // Ignore parse errors and recalculate
                    }
                }

                if (shouldRecalculate) {
                    // Fetch history for all components at once (Sliding Window: 90 days)
                    java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(90);
                    List<AtivoHealthHistory> allHistory = healthHistoryRepository
                            .findByAtivoIdAndComponenteInAndMetricaAndDataRegistroAfterOrderByDataRegistroAsc(id, componentsToFetch, "FREE_SPACE_GB", cutoffDate);

                    // Group by component
                    java.util.Map<String, List<AtivoHealthHistory>> historyByComponent = allHistory.stream()
                            .collect(Collectors.groupingBy(AtivoHealthHistory::getComponente));

                    PredictionResult worstPredictionResult = null;

                    for (List<AtivoHealthHistory> componentHistory : historyByComponent.values()) {
                        PredictionResult prediction = predictiveMaintenanceService.predictExhaustionDate(componentHistory);
                        if (prediction != null && prediction.exhaustionDate() != null) {
                            if (worstPredictionResult == null ||
                                    prediction.exhaustionDate().isBefore(worstPredictionResult.exhaustionDate())) {
                                worstPredictionResult = prediction;
                            }
                        }
                    }

                    // Store worst prediction in attributes
                    if (worstPredictionResult != null) {
                        if (ativo.getAtributos() == null) {
                            ativo.setAtributos(new java.util.HashMap<>());
                        }
                        ativo.getAtributos().put("previsaoEsgotamentoDisco", worstPredictionResult.exhaustionDate().toString());
                        ativo.getAtributos().put("prediction_slope", worstPredictionResult.slope());
                        ativo.getAtributos().put("prediction_intercept", worstPredictionResult.intercept());
                        ativo.getAtributos().put("prediction_base_epoch_day", worstPredictionResult.baseEpochDay());
                        ativo.getAtributos().put("prediction_calculated_at", java.time.LocalDateTime.now().toString());

                        ativo.setPrevisaoEsgotamentoDisco(worstPredictionResult.exhaustionDate());
                    }
                }
            }
        }

        ativoRepository.save(ativo);

        // Trigger alert check
        alertNotificationService.checkAndCreateAlerts(ativo);
    }

    @Transactional
    public AtivoDTO criar(AtivoCreateDTO ativoCreateDTO) {
        validarNumeroPatrimonio(ativoCreateDTO.numeroPatrimonio(), null);

        Ativo ativo = new Ativo();
        ativo.setNome(ativoCreateDTO.nome());
        ativo.setNumeroPatrimonio(ativoCreateDTO.numeroPatrimonio());
        ativo.setDataAquisicao(ativoCreateDTO.dataAquisicao());
        ativo.setValorAquisicao(ativoCreateDTO.valorAquisicao());
        ativo.setObservacoes(ativoCreateDTO.observacoes());
        ativo.setInformacoesGarantia(ativoCreateDTO.informacoesGarantia());
        ativo.setAtributos(ativoCreateDTO.atributos());

        Filial filial = filialRepository.findById(ativoCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + ativoCreateDTO.filialId()));
        ativo.setFilial(filial);

        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(ativoCreateDTO.tipoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + ativoCreateDTO.tipoAtivoId()));
        ativo.setTipoAtivo(tipoAtivo);

        Fornecedor fornecedor = fornecedorRepository.findById(ativoCreateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + ativoCreateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoCreateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoCreateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + ativoCreateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        }

        if (ativoCreateDTO.funcionarioResponsavelId() != null) {
            Funcionario responsavel = funcionarioRepository.findById(ativoCreateDTO.funcionarioResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário responsável não encontrado com ID: " + ativoCreateDTO.funcionarioResponsavelId()));
            validarConsistenciaResponsavel(responsavel, filial);
            ativo.setFuncionarioResponsavel(responsavel);
        }

        gerenciarDetalheHardware(ativo, ativoCreateDTO.detalheHardware());

        Ativo ativoSalvo = ativoRepository.save(ativo);
        
        Ativo ativoCompleto = ativoRepository.findByIdWithDetails(ativoSalvo.getId())
                .orElseThrow(() -> new IllegalStateException("Ativo recém-criado não encontrado. ID: " + ativoSalvo.getId()));

        return ativoMapper.toDTO(ativoCompleto);
    }

    @Transactional
    public AtivoDTO atualizar(Long id, AtivoUpdateDTO ativoUpdateDTO) {
        Ativo ativo = ativoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        validarNumeroPatrimonio(ativoUpdateDTO.numeroPatrimonio(), id);

        BigDecimal valorAquisicaoOriginal = ativo.getValorAquisicao();
        LocalDate dataAquisicaoOriginal = ativo.getDataAquisicao();

        ativo.setNome(ativoUpdateDTO.nome());
        ativo.setNumeroPatrimonio(ativoUpdateDTO.numeroPatrimonio());
        ativo.setStatus(ativoUpdateDTO.status());
        ativo.setDataAquisicao(ativoUpdateDTO.dataAquisicao());
        ativo.setValorAquisicao(ativoUpdateDTO.valorAquisicao());
        ativo.setObservacoes(ativoUpdateDTO.observacoes());
        ativo.setInformacoesGarantia(ativoUpdateDTO.informacoesGarantia());
        ativo.setAtributos(ativoUpdateDTO.atributos());

        Filial filial = filialRepository.findById(ativoUpdateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + ativoUpdateDTO.filialId()));
        ativo.setFilial(filial);

        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(ativoUpdateDTO.tipoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + ativoUpdateDTO.tipoAtivoId()));
        ativo.setTipoAtivo(tipoAtivo);

        Fornecedor fornecedor = fornecedorRepository.findById(ativoUpdateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + ativoUpdateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoUpdateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoUpdateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + ativoUpdateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        } else {
            ativo.setLocalizacao(null);
        }

        if (ativoUpdateDTO.funcionarioResponsavelId() != null) {
            Funcionario responsavel = funcionarioRepository.findById(ativoUpdateDTO.funcionarioResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário responsável não encontrado com ID: " + ativoUpdateDTO.funcionarioResponsavelId()));
            validarConsistenciaResponsavel(responsavel, filial);
            ativo.setFuncionarioResponsavel(responsavel);
        } else {
            ativo.setFuncionarioResponsavel(null);
        }

        gerenciarDetalheHardware(ativo, ativoUpdateDTO.detalheHardware());

        Ativo ativoAtualizado = ativoRepository.save(ativo);

        boolean precisaRecalcular = !Objects.equals(valorAquisicaoOriginal, ativoUpdateDTO.valorAquisicao()) ||
                                  !Objects.equals(dataAquisicaoOriginal, ativoUpdateDTO.dataAquisicao());

        if (precisaRecalcular) {
            depreciacaoService.recalcularDepreciacaoCompleta(ativoAtualizado.getId());
        }
        
        return ativoMapper.toDTO(ativoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (manutencaoRepository.existsByAtivoId(id)) {
            throw new IllegalStateException("Não é possível deletar o ativo, pois existem manutenções associadas a ele.");
        }

        if (movimentacaoRepository.existsByAtivoId(id)) {
            throw new IllegalStateException("Não é possível deletar o ativo, pois existem movimentações associadas a ele.");
        }

        ativoRepository.delete(ativo);
    }

    @Transactional(readOnly = true)
    public List<AtivoHealthHistoryDTO> getHealthHistory(Long ativoId) {
        Usuario usuarioLogado = getUsuarioLogado();
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioPrincipal = usuarioLogado.getFuncionario();
            if (funcionarioPrincipal == null || funcionarioPrincipal.getId() == null) {
                throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
            }
            Optional<Funcionario> funcionarioOpt = funcionarioRepository.findById(funcionarioPrincipal.getId());
            if (funcionarioOpt.isPresent()) {
                Funcionario funcionarioLogado = funcionarioOpt.get();
                if (funcionarioLogado.getFiliais().stream().noneMatch(f -> f.getId().equals(ativo.getFilial().getId()))) {
                    throw new AccessDeniedException("Você não tem permissão para acessar o histórico deste ativo.");
                }
            }
        }

        return healthHistoryRepository.findByAtivoIdAndMetricaOrderByDataRegistroAsc(ativoId, "FREE_SPACE_GB")
                .stream()
                .map(h -> new AtivoHealthHistoryDTO(h.getDataRegistro(), h.getComponente(), h.getValor(), h.getMetrica()))
                .collect(Collectors.toList());
    }

    private void validarNumeroPatrimonio(String numeroPatrimonio, Long ativoId) {
        Optional<Ativo> ativoExistente = ativoRepository.findByNumeroPatrimonio(numeroPatrimonio);
        if (ativoExistente.isPresent() && !ativoExistente.get().getId().equals(ativoId)) {
            throw new IllegalArgumentException("Já existe um ativo cadastrado com o número de patrimônio informado.");
        }
    }

    private void validarConsistenciaLocalizacao(Localizacao localizacao, Filial filial) {
        if (!localizacao.getFilial().getId().equals(filial.getId())) {
            throw new IllegalArgumentException("A localização selecionada não pertence à filial do ativo.");
        }
    }

    private void validarConsistenciaResponsavel(Funcionario responsavel, Filial filial) {
        boolean responsavelPertenceAFilial = responsavel.getFiliais().stream()
                .anyMatch(f -> f.getId().equals(filial.getId()));
        if (!responsavelPertenceAFilial) {
            throw new IllegalArgumentException("O responsável selecionado não pertence à filial do ativo.");
        }
    }

    private void gerenciarDetalheHardware(Ativo ativo, AtivoDetalheHardwareDTO dto) {
        if (dto == null) {
            return;
        }

        AtivoDetalheHardware hardware = ativo.getDetalheHardware();
        if (hardware == null) {
            hardware = new AtivoDetalheHardware();
            hardware.setAtivo(ativo);
            ativo.setDetalheHardware(hardware);
        }

        hardware.setComputerName(dto.computerName());
        hardware.setDomain(dto.domain());
        hardware.setOsName(dto.osName());
        hardware.setOsVersion(dto.osVersion());
        hardware.setOsArchitecture(dto.osArchitecture());
        hardware.setMotherboardManufacturer(dto.motherboardManufacturer());
        hardware.setMotherboardModel(dto.motherboardModel());
        hardware.setMotherboardSerialNumber(dto.motherboardSerialNumber());
        hardware.setCpuModel(dto.cpuModel());
        hardware.setCpuCores(dto.cpuCores());
        hardware.setCpuThreads(dto.cpuThreads());
    }
}
