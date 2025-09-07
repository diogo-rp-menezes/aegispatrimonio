package br.com.aegispatrimonio.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.AtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.AtivoResponseDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final TipoAtivoRepository tipoAtivoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public AtivoResponseDTO criar(AtivoRequestDTO request) {
        validarNumeroPatrimonioUnico(request.getNumeroPatrimonio());
        
        Ativo ativo = convertToEntity(request);
        Ativo savedAtivo = ativoRepository.save(ativo);
        
        return convertToResponseDTO(savedAtivo);
    }

    @Transactional(readOnly = true)
    public Optional<AtivoResponseDTO> buscarPorId(Long id) {
        return ativoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AtivoResponseDTO> buscarPorNumeroPatrimonio(String numeroPatrimonio) {
        return ativoRepository.findByNumeroPatrimonio(numeroPatrimonio)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> listarTodos(Pageable pageable) {
        return ativoRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> listarPorTipo(Long tipoAtivoId, Pageable pageable) {
        return ativoRepository.findByTipoAtivoId(tipoAtivoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> listarPorLocalizacao(Long localizacaoId, Pageable pageable) {
        return ativoRepository.findByLocalizacaoId(localizacaoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> listarPorStatus(StatusAtivo status, Pageable pageable) {
        return ativoRepository.findByStatus(status, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return ativoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AtivoResponseDTO> buscarPorFaixaDeValor(BigDecimal valorMin, BigDecimal valorMax, Pageable pageable) {
        return ativoRepository.findByValorAquisicaoBetween(valorMin, valorMax, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public AtivoResponseDTO atualizar(Long id, AtivoRequestDTO request) {
        Ativo ativoExistente = ativoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + id));
        
        if (!ativoExistente.getNumeroPatrimonio().equals(request.getNumeroPatrimonio())) {
            validarNumeroPatrimonioUnico(request.getNumeroPatrimonio());
        }
        
        updateEntityFromRequest(ativoExistente, request);
        Ativo updatedAtivo = ativoRepository.save(ativoExistente);
        
        return convertToResponseDTO(updatedAtivo);
    }

    @Transactional
    public void deletar(Long id) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado com ID: " + id));
        ativoRepository.delete(ativo);
    }

    @Transactional(readOnly = true)
    public boolean existePorNumeroPatrimonio(String numeroPatrimonio) {
        return ativoRepository.existsByNumeroPatrimonio(numeroPatrimonio);
    }

    // Métodos de conversão (MANTIDOS)
    private Ativo convertToEntity(AtivoRequestDTO request) {
        Ativo ativo = new Ativo();
        updateEntityFromRequest(ativo, request);
        return ativo;
    }

    private void updateEntityFromRequest(Ativo ativo, AtivoRequestDTO request) {
        ativo.setNome(request.getNome());
        ativo.setNumeroPatrimonio(request.getNumeroPatrimonio());
        ativo.setStatus(request.getStatus());
        ativo.setDataAquisicao(request.getDataAquisicao());
        ativo.setValorAquisicao(request.getValorAquisicao());
        ativo.setValorResidual(request.getValorResidual());
        ativo.setVidaUtilMeses(request.getVidaUtilMeses());
        ativo.setMetodoDepreciacao(request.getMetodoDepreciacao());
        ativo.setDataInicioDepreciacao(request.getDataInicioDepreciacao());
        ativo.setInformacoesGarantia(request.getInformacoesGarantia());
        ativo.setObservacoes(request.getObservacoes());
        
        // Configurar relações
        if (request.getTipoAtivoId() != null) {
            TipoAtivo tipoAtivo = tipoAtivoRepository.findById(request.getTipoAtivoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de ativo não encontrado"));
            ativo.setTipoAtivo(tipoAtivo);
        }
        
        if (request.getLocalizacaoId() != null) {
            Localizacao localizacao = localizacaoRepository.findById(request.getLocalizacaoId())
                    .orElseThrow(() -> new RuntimeException("Localização não encontrada"));
            ativo.setLocalizacao(localizacao);
        }
        
        if (request.getFornecedorId() != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(request.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            ativo.setFornecedor(fornecedor);
        }
        
        if (request.getPessoaResponsavelId() != null) {
            Pessoa pessoa = pessoaRepository.findById(request.getPessoaResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Pessoa responsável não encontrada"));
            ativo.setPessoaResponsavel(pessoa);
        }
        
        // Configurar campos calculados
        if (ativo.getVidaUtilMeses() != null && ativo.getVidaUtilMeses() > 0) {
            BigDecimal depreciableAmount = ativo.getValorAquisicao().subtract(ativo.getValorResidual());
            BigDecimal monthlyRate = depreciableAmount.divide(
                BigDecimal.valueOf(ativo.getVidaUtilMeses()), 
                10, 
                java.math.RoundingMode.HALF_UP
            );
            ativo.setTaxaDepreciacaoMensal(monthlyRate);
        }
    }

    private AtivoResponseDTO convertToResponseDTO(Ativo ativo) {
        return AtivoResponseDTO.fromEntity(ativo);
    }

    private void validarNumeroPatrimonioUnico(String numeroPatrimonio) {
        if (ativoRepository.existsByNumeroPatrimonio(numeroPatrimonio)) {
            throw new RuntimeException("Número de patrimônio já existe: " + numeroPatrimonio);
        }
    }
}