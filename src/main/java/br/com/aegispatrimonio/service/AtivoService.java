package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.healthcheck.HealthCheckPayloadDTO;
import br.com.aegispatrimonio.dto.healthcheck.DiskInfoDTO;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper, TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository, FornecedorRepository fornecedorRepository, FuncionarioRepository funcionarioRepository, FilialRepository filialRepository, ManutencaoRepository manutencaoRepository, MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService, CurrentUserProvider currentUserProvider, AtivoHealthHistoryRepository healthHistoryRepository, PredictiveMaintenanceService predictiveMaintenanceService, SearchOptimizationService searchOptimizationService) {
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
                                      String nome) {
        Usuario usuarioLogado = getUsuarioLogado();
        org.springframework.data.domain.Pageable effectivePageable =
                (pageable == null) ? org.springframework.data.domain.Pageable.unpaged() : pageable;
        boolean isFuzzySearch = (nome != null && !nome.isBlank());

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
            List<Ativo> candidates;
            // Fetch candidates matching other filters, ignoring name (limited to 1000 for safety)
            org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 1000);

            if (isAdmin) {
                candidates = ativoRepository.findByFilters(filialId, tipoAtivoId, status, null, limit).getContent();
            } else {
                candidates = ativoRepository.findByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, null, limit).getContent();
            }

            // Rank in memory using Levenshtein distance
            List<Ativo> ranked = searchOptimizationService.rankResults(nome, candidates, Ativo::getNome);

            // Manual Pagination
            int start = (int) effectivePageable.getOffset();
            int end = Math.min((start + effectivePageable.getPageSize()), ranked.size());
            List<AtivoDTO> pageContent = (start > ranked.size()) ? List.of() :
                    ranked.subList(start, end).stream().map(ativoMapper::toDTO).collect(Collectors.toList());

            return new PageImpl<>(pageContent, effectivePageable, ranked.size());
        }

        // 3. Original Path (Strict / DB Paged)
        boolean unpaged = effectivePageable.isUnpaged();
        boolean hasFilters = (filialId != null) || (tipoAtivoId != null) || (status != null);

        if (isAdmin) {
            if (unpaged && !hasFilters) {
                return new PageImpl<>(ativoRepository.findAllWithDetails().stream().map(ativoMapper::toDTO).collect(Collectors.toList()));
            }
            return ativoRepository.findByFilters(filialId, tipoAtivoId, status, null, effectivePageable).map(ativoMapper::toDTO);
        } else {
            if (unpaged && !hasFilters) {
                return new PageImpl<>(ativoRepository.findByFilialIdInWithDetails(userFiliais).stream().map(ativoMapper::toDTO).collect(Collectors.toList()));
            }
            return ativoRepository.findByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, null, effectivePageable).map(ativoMapper::toDTO);
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

        AtivoDTO dto = ativoMapper.toDTO(ativo);

        // Calculate Predictive Maintenance
        LocalDate worstPrediction = null;
        // In a real scenario, we would iterate known disks. Here we fetch all history for the asset.
        // For optimization, we should probably add a method to repo to get distinct components.
        // For MVP, we assume "DISK:0" or similar. But since we don't know the serials without querying history...
        // Let's rely on the assumption that if there is data, it is recent.
        // Or simply query specifically for "FREE_SPACE_GB" metric.
        // Since we don't want N+1 queries for components, let's just pick the last few records if needed.
        // But predictExhaustionDate needs history.
        // Let's skip complex component discovery for now and just try to find prediction if we have history.
        // Warning: This can be expensive if history is huge.
        // A better approach for production: Store the predicted date in the Ativo entity during the updateHealthCheck process.
        // However, the prompt asked to follow "Shift Left" and show value.
        // Calculating on read ensures it's always up to date with the latest algorithm.
        // Let's leave it as null by default unless we implement a dedicated endpoint for insights.
        // Actually, let's look at the plan. "Update AtivoDTO to include previsaoEsgotamentoDisco".
        // It's better to calculate this during ingestion (updateHealthCheck) and store it in Ativo attributes or a new column.
        // Since I can't easily add a column to Ativo without migration (I did create V7 for history though),
        // I'll calculate it on read but restrict it to a specific well-known component if possible, or iterate.
        // Since I created AtivoHealthHistoryRepository, let's use it.
        // But without knowing the component name (Serial), it's hard.
        // I'll modify the loop below to check distinct components if I can.

        // Alternative: Calculate on write (updateHealthCheck) and store in Ativo.atributos['previsaoEsgotamento'].
        // This is much more efficient.
        if (ativo.getAtributos() != null && ativo.getAtributos().containsKey("previsaoEsgotamentoDisco")) {
             try {
                 String dateStr = (String) ativo.getAtributos().get("previsaoEsgotamentoDisco");
                 worstPrediction = LocalDate.parse(dateStr);
             } catch (Exception e) {
                 // Ignore parsing error
             }
        }

        // Create a new DTO with the prediction
        return new AtivoDTO(
            dto.id(), dto.nome(), dto.numeroPatrimonio(), dto.tipoAtivoId(), dto.tipoAtivo(),
            dto.localizacaoId(), dto.localizacao(), dto.filialId(), dto.filial(),
            dto.fornecedorId(), dto.fornecedorNome(), dto.status(), dto.valorAquisicao(),
            dto.funcionarioResponsavelId(), dto.funcionarioResponsavelNome(),
            dto.detalheHardware(), dto.atributos(), worstPrediction
        );
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
            LocalDate worstPrediction = null;

            for (DiskInfoDTO disk : payload.discos()) {
                if (disk.freeGb() != null) {
                    AtivoHealthHistory history = new AtivoHealthHistory();
                    history.setAtivo(ativo);
                    history.setComponente("DISK:" + disk.serial());
                    history.setMetrica("FREE_SPACE_GB");
                    history.setValor(disk.freeGb());
                    healthHistoryRepository.save(history);

                    // Calculate Prediction for this disk
                    List<AtivoHealthHistory> diskHistory = healthHistoryRepository
                            .findByAtivoIdAndMetricaOrderByDataRegistroAsc(id, "DISK:" + disk.serial());

                    LocalDate prediction = predictiveMaintenanceService.predictExhaustionDate(diskHistory);
                    if (prediction != null) {
                        if (worstPrediction == null || prediction.isBefore(worstPrediction)) {
                            worstPrediction = prediction;
                        }
                    }
                }
            }

            // Store worst prediction in attributes
            if (worstPrediction != null) {
                if (ativo.getAtributos() == null) {
                    ativo.setAtributos(new java.util.HashMap<>());
                }
                ativo.getAtributos().put("previsaoEsgotamentoDisco", worstPrediction.toString());
            }
        }

        ativoRepository.save(ativo);
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
