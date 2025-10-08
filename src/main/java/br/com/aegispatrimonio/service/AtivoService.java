package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.AtivoCreateDTO;
import br.com.aegispatrimonio.dto.AtivoDTO;
import br.com.aegispatrimonio.dto.AtivoUpdateDTO;
import br.com.aegispatrimonio.mapper.AtivoMapper;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final AtivoMapper ativoMapper;
    private final TipoAtivoRepository tipoAtivoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PessoaRepository pessoaRepository;
    private final FilialRepository filialRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final DepreciacaoService depreciacaoService; // Injetado para recálculo automático

    public AtivoService(AtivoRepository ativoRepository, AtivoMapper ativoMapper, TipoAtivoRepository tipoAtivoRepository, LocalizacaoRepository localizacaoRepository, FornecedorRepository fornecedorRepository, PessoaRepository pessoaRepository, FilialRepository filialRepository, ManutencaoRepository manutencaoRepository, MovimentacaoRepository movimentacaoRepository, DepreciacaoService depreciacaoService) {
        this.ativoRepository = ativoRepository;
        this.ativoMapper = ativoMapper;
        this.tipoAtivoRepository = tipoAtivoRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.pessoaRepository = pessoaRepository;
        this.filialRepository = filialRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.depreciacaoService = depreciacaoService;
    }

    private Pessoa getPessoaLogada() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getPessoa();
    }

    private boolean isAdmin(Pessoa pessoa) {
        return "ROLE_ADMIN".equals(pessoa.getRole());
    }

    @Transactional(readOnly = true)
    public List<AtivoDTO> listarTodos() {
        Pessoa pessoaLogada = getPessoaLogada();

        if (isAdmin(pessoaLogada)) {
            return ativoRepository.findAll().stream().map(ativoMapper::toDTO).collect(Collectors.toList());
        }

        Long filialId = pessoaLogada.getFilial().getId();
        return ativoRepository.findByFilialId(filialId).stream().map(ativoMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AtivoDTO buscarPorId(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada) && !ativo.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar ativos de outra filial.");
        }

        return ativoMapper.toDTO(ativo);
    }

    @Transactional
    public AtivoDTO criar(AtivoCreateDTO ativoCreateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();

        if (!isAdmin(pessoaLogada) && !ativoCreateDTO.filialId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você só pode criar ativos para a sua própria filial.");
        }

        validarNumeroPatrimonio(ativoCreateDTO.numeroPatrimonio(), null);

        Ativo ativo = ativoMapper.toEntity(ativoCreateDTO);

        Filial filial = filialRepository.findById(ativoCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + ativoCreateDTO.filialId()));
        ativo.setFilial(filial);

        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(ativoCreateDTO.tipoAtivoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Ativo não encontrado com ID: " + ativoCreateDTO.tipoAtivoId()));
        ativo.setTipoAtivo(tipoAtivo);

        if (ativoCreateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoCreateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + ativoCreateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        }

        Fornecedor fornecedor = fornecedorRepository.findById(ativoCreateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + ativoCreateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoCreateDTO.pessoaResponsavelId() != null) {
            Pessoa pessoa = pessoaRepository.findById(ativoCreateDTO.pessoaResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Pessoa responsável não encontrada com ID: " + ativoCreateDTO.pessoaResponsavelId()));
            validarConsistenciaResponsavel(pessoa, filial);
            ativo.setPessoaResponsavel(pessoa);
        }

        Ativo ativoSalvo = ativoRepository.save(ativo);
        return ativoMapper.toDTO(ativoSalvo);
    }

    @Transactional
    public AtivoDTO atualizar(Long id, AtivoUpdateDTO ativoUpdateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada)) {
            if (!ativo.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para editar ativos de outra filial.");
            }
            if (ativoUpdateDTO.filialId() != null && !ativo.getFilial().getId().equals(ativoUpdateDTO.filialId())) {
                throw new AccessDeniedException("Você não tem permissão para transferir ativos entre filiais.");
            }
        }

        validarNumeroPatrimonio(ativoUpdateDTO.numeroPatrimonio(), id);

        // Armazena valores financeiros originais para verificar se precisam de recálculo
        BigDecimal valorAquisicaoOriginal = ativo.getValorAquisicao();
        LocalDate dataAquisicaoOriginal = ativo.getDataAquisicao();
        // NOTA: Outros campos como valorResidual, vidaUtilMeses, metodoDepreciacao e dataInicioDepreciacao
        // também deveriam estar no DTO e acionar o recálculo.

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

        if (ativoUpdateDTO.localizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(ativoUpdateDTO.localizacaoId())
                    .orElseThrow(() -> new EntityNotFoundException("Localização não encontrada com ID: " + ativoUpdateDTO.localizacaoId()));
            validarConsistenciaLocalizacao(localizacao, filial);
            ativo.setLocalizacao(localizacao);
        } else {
            ativo.setLocalizacao(null);
        }

        Fornecedor fornecedor = fornecedorRepository.findById(ativoUpdateDTO.fornecedorId())
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + ativoUpdateDTO.fornecedorId()));
        ativo.setFornecedor(fornecedor);

        if (ativoUpdateDTO.pessoaResponsavelId() != null) {
            Pessoa pessoa = pessoaRepository.findById(ativoUpdateDTO.pessoaResponsavelId())
                    .orElseThrow(() -> new EntityNotFoundException("Pessoa responsável não encontrada com ID: " + ativoUpdateDTO.pessoaResponsavelId()));
            validarConsistenciaResponsavel(pessoa, filial);
            ativo.setPessoaResponsavel(pessoa);
        } else {
            ativo.setPessoaResponsavel(null);
        }

        Ativo ativoAtualizado = ativoRepository.save(ativo);

        // Verifica se uma alteração financeira requer o recálculo da depreciação
        boolean precisaRecalcular = !Objects.equals(valorAquisicaoOriginal, ativoUpdateDTO.valorAquisicao()) ||
                                  !Objects.equals(dataAquisicaoOriginal, ativoUpdateDTO.dataAquisicao());

        if (precisaRecalcular) {
            depreciacaoService.recalcularDepreciacaoCompleta(ativoAtualizado.getId());
        }

        return ativoMapper.toDTO(ativoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ativo não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada) && !ativo.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar ativos de outra filial.");
        }

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

    private void validarConsistenciaResponsavel(Pessoa responsavel, Filial filial) {
        if (!responsavel.getFilial().getId().equals(filial.getId())) {
            throw new IllegalArgumentException("O responsável selecionado não pertence à filial do ativo.");
        }
    }
}
