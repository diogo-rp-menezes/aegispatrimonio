package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoHealthHistoryDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AtivoService implements IAtivoService {

    /**
     * Tamanho de página usado quando a listagem é "unpaged" (export/relatório):
     * itera o resultado em chunks para evitar carregar todo o parque de ativos
     * (com 4-6 JOINs FETCH) em uma única query.
     */
    private static final int UNPAGED_FETCH_PAGE_SIZE = 500;

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
    private final UserContextService userContextService;

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper,
            TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository,
            FornecedorRepository fornecedorRepository, FuncionarioRepository funcionarioRepository,
            FilialRepository filialRepository, ManutencaoRepository manutencaoRepository,
            MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService,
            AtivoHealthHistoryRepository healthHistoryRepository,
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

        // 2. Busca por nome (M4): usa índice FULLTEXT do MySQL (ranking por relevância no
        // banco). Fallback para LIKE quando o termo tem menos de 3 caracteres ou quando
        // o banco não suporta MATCH...AGAINST (ex.: H2 em dev/e2e/testes).
        if (isFuzzySearch && nome.trim().length() >= 3) {
            try {
                String statusName = (status != null) ? status.name() : null;
                Page<Ativo> page = isAdmin
                        ? ativoRepository.searchByNomeFullText(nome, filialId, tipoAtivoId, statusName, minDate,
                                maxDate, hasPrediction, effectivePageable)
                        : ativoRepository.searchByNomeFullTextByFilialIds(nome, userFiliais, filialId, tipoAtivoId,
                                statusName, minDate, maxDate, hasPrediction, effectivePageable);
                return page.map(ativoMapper::toDTO);
            } catch (org.springframework.dao.InvalidDataAccessResourceUsageException
                    | org.springframework.orm.jpa.JpaSystemException e) {
                // Banco sem suporte a FULLTEXT (ex.: H2) — cai para o caminho LIKE abaixo.
            }
        }

        // 3. Original Path (Strict / DB Paged)
        boolean unpaged = effectivePageable.isUnpaged();
        boolean hasFilters = (filialId != null) || (tipoAtivoId != null) || (status != null) || (health != null);

        // M2 (audit): o caminho unpaged carregava TODOS os ativos com 4-6 JOINs FETCH em
        // uma única query. Substituído por iteração de páginas de 500 registros, limitando
        // o pico de memória por query. O contrato Page<AtivoDTO> com total correto é
        // preservado (PageImpl com Pageable.unpaged() e total acumulado).
        if (unpaged && !hasFilters) {
            List<AtivoDTO> allContent = new java.util.ArrayList<>();
            long total = 0;
            int pageNumber = 0;
            org.springframework.data.domain.Page<Ativo> page;
            do {
                org.springframework.data.domain.Pageable chunk = org.springframework.data.domain.PageRequest
                        .of(pageNumber, UNPAGED_FETCH_PAGE_SIZE);
                page = isAdmin
                        ? ativoRepository.findByFilters(null, null, null, null, null, null, null, chunk)
                        : ativoRepository.findByFilialIdsAndFilters(userFiliais, null, null, null, null, null, null,
                                null, chunk);
                page.getContent().forEach(a -> allContent.add(ativoMapper.toDTO(a)));
                total = page.getTotalElements();
                pageNumber++;
            } while (pageNumber < page.getTotalPages());

            return new PageImpl<>(allContent, effectivePageable, total);
        }

        // Fallback LIKE (termo curto ou banco sem FULLTEXT): passa o nome como filtro.
        String nomeLike = isFuzzySearch ? nome : null;
        if (isAdmin) {
            return ativoRepository.findByFilters(filialId, tipoAtivoId, status, nomeLike, minDate, maxDate,
                    hasPrediction, effectivePageable).map(ativoMapper::toDTO);
        }
        return ativoRepository.findByFilialIdsAndFilters(userFiliais, filialId, tipoAtivoId, status, nomeLike, minDate,
                maxDate, hasPrediction, effectivePageable).map(ativoMapper::toDTO);
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
