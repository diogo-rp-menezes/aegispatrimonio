package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoNameDTO;
import br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.dto.query.AtivoQueryParams;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AtivoService implements IAtivoService {

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
    private final AtivoHealthHistoryRepository healthHistoryRepository;
    private final SearchOptimizationService searchOptimizationService;
    private final UserContextService userContextService;

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper,
            TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository,
            FornecedorRepository fornecedorRepository, FuncionarioRepository funcionarioRepository,
            FilialRepository filialRepository, ManutencaoRepository manutencaoRepository,
            MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService,
            AtivoHealthHistoryRepository healthHistoryRepository, SearchOptimizationService searchOptimizationService,
            UserContextService userContextService) {
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
        this.healthHistoryRepository = healthHistoryRepository;
        this.searchOptimizationService = searchOptimizationService;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AtivoDTO> listarTodos(org.springframework.data.domain.Pageable pageable,
            AtivoQueryParams queryParams) {
        org.springframework.data.domain.Pageable effectivePageable = (pageable == null)
                ? org.springframework.data.domain.Pageable.unpaged()
                : pageable;

        Long filialId = queryParams.filialId();
        Long tipoAtivoId = queryParams.tipoAtivoId();
        StatusAtivo status = queryParams.status();
        String nome = queryParams.nome();
        String health = queryParams.health();

        boolean isFuzzySearch = (nome != null && !nome.isBlank());

        // Calculate Date Range for Predictive Health
        LocalDate minDate = null;
        LocalDate maxDate = null;
        Boolean hasPrediction = null;

        if (health != null) {
            LocalDate now = LocalDate.now();
            switch (health) {
                case "CRITICO" -> maxDate = now.plusDays(7);
                case "ALERTA" -> {
                    minDate = now.plusDays(7);
                    maxDate = now.plusDays(30);
                }
                case "SAUDAVEL" -> minDate = now.plusDays(30);
                case "INDETERMINADO" -> hasPrediction = false;
            }
        }

        // 1. Resolve Scope (Admin vs User Filiais)
        Set<Long> userFiliais = null;
        boolean isAdmin = userContextService.isAdmin();

        if (!isAdmin) {
            userFiliais = userContextService.getUserFiliais();
        }

        // 2. Fuzzy Search Path (Shift Left Optimization)
        if (isFuzzySearch) {
            List<AtivoNameDTO> candidates;
            // Fetch candidates matching other filters, ignoring name (limited to 1000 for
            // safety)
            org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 1000);

            if (isAdmin) {
                candidates = ativoRepository.findSimpleByFilters(filialId, tipoAtivoId, status, minDate, maxDate,
                        hasPrediction, limit);
            } else {
                candidates = ativoRepository.findSimpleByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status,
                        minDate, maxDate, hasPrediction, limit);
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
            Map<Long, Ativo> entityMap = fullEntities.stream()
                    .collect(Collectors.toMap(Ativo::getId, Function.identity()));
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
                return new PageImpl<>(ativoRepository.findAllWithDetails().stream().map(ativoMapper::toDTO)
                        .collect(Collectors.toList()));
            }
            return ativoRepository.findByFilters(filialId, tipoAtivoId, status, null, minDate, maxDate, hasPrediction,
                    effectivePageable).map(ativoMapper::toDTO);
        } else {
            if (unpaged && !hasFilters) {
                return new PageImpl<>(ativoRepository.findByFilialIdInWithDetails(userFiliais).stream()
                        .map(ativoMapper::toDTO).collect(Collectors.toList()));
            }
            return ativoRepository.findByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, null, minDate,
                    maxDate, hasPrediction, effectivePageable).map(ativoMapper::toDTO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AtivoDTO buscarPorId(Long id) {
        Ativo ativo = ativoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (!userContextService.isAdmin()) {
            Set<Long> userFiliais = userContextService.getUserFiliais();
            if (!userFiliais.contains(ativo.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar ativos desta filial.");
            }
        }

        return ativoMapper.toDTO(ativo);
    }

    @Override
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Filial não encontrada com ID: " + ativoCreateDTO.filialId()));
        ativo.setFilial(filial);

        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(ativoCreateDTO.tipoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tipo de Ativo não encontrado com ID: " + ativoCreateDTO.tipoAtivoId()));
        ativo.setTipoAtivo(tipoAtivo);

        Fornecedor fornecedor = fornecedorRepository.findById(ativoCreateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fornecedor não encontrado com ID: " + ativoCreateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoCreateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoCreateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Localização não encontrada com ID: " + ativoCreateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        }

        if (ativoCreateDTO.funcionarioResponsavelId() != null) {
            Funcionario responsavel = funcionarioRepository
                    .findByIdWithFiliais(ativoCreateDTO.funcionarioResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário responsável não encontrado com ID: "
                            + ativoCreateDTO.funcionarioResponsavelId()));
            validarConsistenciaResponsavel(responsavel, filial);
            ativo.setFuncionarioResponsavel(responsavel);
        }

        gerenciarDetalheHardware(ativo, ativoCreateDTO.detalheHardware());

        Ativo ativoSalvo = ativoRepository.save(ativo);

        Ativo ativoCompleto = ativoRepository.findByIdWithDetails(ativoSalvo.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Ativo recém-criado não encontrado. ID: " + ativoSalvo.getId()));

        return ativoMapper.toDTO(ativoCompleto);
    }

    @Override
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Filial não encontrada com ID: " + ativoUpdateDTO.filialId()));
        ativo.setFilial(filial);

        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(ativoUpdateDTO.tipoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tipo de Ativo não encontrado com ID: " + ativoUpdateDTO.tipoAtivoId()));
        ativo.setTipoAtivo(tipoAtivo);

        Fornecedor fornecedor = fornecedorRepository.findById(ativoUpdateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fornecedor não encontrado com ID: " + ativoUpdateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoUpdateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoUpdateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Localização não encontrada com ID: " + ativoUpdateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        } else {
            ativo.setLocalizacao(null);
        }

        if (ativoUpdateDTO.funcionarioResponsavelId() != null) {
            Funcionario responsavel = funcionarioRepository
                    .findByIdWithFiliais(ativoUpdateDTO.funcionarioResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário responsável não encontrado com ID: "
                            + ativoUpdateDTO.funcionarioResponsavelId()));
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

    @Override
    @Transactional
    public void deletar(Long id) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (manutencaoRepository.existsByAtivoId(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar o ativo, pois existem manutenções associadas a ele.");
        }

        if (movimentacaoRepository.existsByAtivoId(id)) {
            throw new IllegalStateException(
                    "Não é possível deletar o ativo, pois existem movimentações associadas a ele.");
        }

        ativoRepository.delete(ativo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtivoHealthHistoryDTO> getHealthHistory(Long ativoId) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + ativoId));

        if (!userContextService.isAdmin()) {
            Set<Long> userFiliais = userContextService.getUserFiliais();
            if (!userFiliais.contains(ativo.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar o histórico deste ativo.");
            }
        }

        return healthHistoryRepository.findByAtivoIdAndMetricaOrderByDataRegistroAsc(ativoId, "FREE_SPACE_GB")
                .stream()
                .map(h -> new AtivoHealthHistoryDTO(h.getDataRegistro(), h.getComponente(), h.getValor(),
                        h.getMetrica()))
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
