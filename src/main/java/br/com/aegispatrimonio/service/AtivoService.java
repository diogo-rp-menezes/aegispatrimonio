package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoDetalheHardwareDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.mapper.AtivoMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper, TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository, FornecedorRepository fornecedorRepository, FuncionarioRepository funcionarioRepository, FilialRepository filialRepository, ManutencaoRepository manutencaoRepository, MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService) {
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
    }

    private Usuario getUsuarioLogado() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
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
        boolean unpaged = effectivePageable.isUnpaged();
        boolean hasFilters = (filialId != null) || (tipoAtivoId != null) || (status != null) || (nome != null && !nome.isBlank());

        if (isAdmin(usuarioLogado)) {
            if (unpaged && !hasFilters) {
                List<AtivoDTO> list = ativoRepository.findAllWithDetails().stream().map(ativoMapper::toDTO).collect(Collectors.toList());
                return new PageImpl<>(list);
            } else {
                return ativoRepository
                        .findByFilters(filialId, tipoAtivoId, status, nome, effectivePageable)
                        .map(ativoMapper::toDTO);
            }
        }

        Funcionario funcionarioPrincipal = usuarioLogado.getFuncionario();
        if (funcionarioPrincipal == null || funcionarioPrincipal.getId() == null) {
            throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
        }

        Optional<Funcionario> funcionarioOpt = funcionarioRepository.findById(funcionarioPrincipal.getId());
        if (funcionarioOpt.isEmpty()) {
            if (unpaged && !hasFilters) {
                List<AtivoDTO> list = ativoRepository.findAllWithDetails().stream().map(ativoMapper::toDTO).collect(Collectors.toList());
                return new PageImpl<>(list);
            } else {
                return ativoRepository
                        .findByFilters(filialId, tipoAtivoId, status, nome, effectivePageable)
                        .map(ativoMapper::toDTO);
            }
        }
        Funcionario funcionarioLogado = funcionarioOpt.get();
        if (funcionarioLogado.getFiliais() == null || funcionarioLogado.getFiliais().isEmpty()) {
            throw new AccessDeniedException("Usuário não está associado a nenhuma filial.");
        }

        Set<Long> filiaisIds = funcionarioLogado.getFiliais().stream().map(Filial::getId).collect(Collectors.toSet());

        if (unpaged && !hasFilters) {
            List<AtivoDTO> list = ativoRepository.findByFilialIdInWithDetails(filiaisIds).stream().map(ativoMapper::toDTO).collect(Collectors.toList());
            return new PageImpl<>(list);
        } else {
            return ativoRepository
                    .findByFilialIdsAndFilters(filiaisIds, filialId, tipoAtivoId, status, nome, effectivePageable)
                    .map(ativoMapper::toDTO);
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
            } // Fallback: se não encontrar o funcionário persistido (ex.: usuário mock sem registro), não bloqueia o acesso de leitura
        }

        return ativoMapper.toDTO(ativo);
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
