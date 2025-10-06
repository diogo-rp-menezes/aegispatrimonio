package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoCancelDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoConclusaoDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoInicioDTO;
import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final AtivoRepository ativoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public ManutencaoResponseDTO criar(ManutencaoRequestDTO request) {
        log.info("Criando nova manutenção para ativo ID: {}", request.getAtivoId());
        Manutencao manutencao = convertToEntity(request);
        Manutencao savedManutencao = manutencaoRepository.save(manutencao);
        return convertToResponseDTO(savedManutencao);
    }

    @Transactional(readOnly = true)
    public Optional<ManutencaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando manutenção ID: {}", id);
        return manutencaoRepository.findById(id).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listar(Long ativoId, StatusManutencao status, TipoManutencao tipo, Long solicitanteId, Long fornecedorId,
                                              LocalDate dataSolicitacaoInicio, LocalDate dataSolicitacaoFim, LocalDate dataConclusaoInicio,
                                              LocalDate dataConclusaoFim, Pageable pageable) {
        log.debug("Listando manutenções com filtros");
        Specification<Manutencao> spec = ManutencaoSpecification.build(ativoId, status, tipo, solicitanteId, fornecedorId,
                dataSolicitacaoInicio, dataSolicitacaoFim, dataConclusaoInicio, dataConclusaoFim);
        return manutencaoRepository.findAll(spec, pageable).map(this::convertToResponseDTO);
    }

    @Transactional
    public ManutencaoResponseDTO aprovar(Long id) {
        log.info("Aprovando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.SOLICITADA, "aprovada");
        manutencao.setStatus(StatusManutencao.APROVADA);
        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO iniciar(Long id, ManutencaoInicioDTO inicioDTO) {
        log.info("Iniciando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.APROVADA, "iniciada");

        Pessoa tecnico = pessoaRepository.findById(inicioDTO.getTecnicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado com ID: " + inicioDTO.getTecnicoId()));

        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ativoRepository.save(ativo);

        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setTecnicoResponsavel(tecnico);
        manutencao.setDataInicio(LocalDate.now());

        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO concluir(Long id, ManutencaoConclusaoDTO conclusaoDTO) {
        log.info("Concluindo manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.EM_ANDAMENTO, "concluída");

        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(ativo);

        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDescricaoServico(conclusaoDTO.getDescricaoServico());
        manutencao.setCustoReal(conclusaoDTO.getCustoReal());
        manutencao.setTempoExecucaoMinutos(conclusaoDTO.getTempoExecucao());
        manutencao.setDataConclusao(LocalDate.now());

        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO cancelar(Long id, ManutencaoCancelDTO cancelDTO) {
        log.info("Cancelando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);

        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA || manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new ResourceConflictException("Manutenção já foi concluída ou cancelada e não pode ser alterada.");
        }

        if (manutencao.getStatus() == StatusManutencao.EM_ANDAMENTO) {
            Ativo ativo = manutencao.getAtivo();
            ativo.setStatus(StatusAtivo.ATIVO);
            ativoRepository.save(ativo);
        }

        manutencao.setStatus(StatusManutencao.CANCELADA);
        manutencao.setObservacoes(cancelDTO.getMotivo());

        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando manutenção ID: {}", id);
        if (!manutencaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Manutenção não encontrada com ID: " + id);
        }
        manutencaoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal custoTotalPorAtivo(Long ativoId) {
        log.debug("Calculando custo total para ativo ID: {}", ativoId);
        if (!ativoRepository.existsById(ativoId)) {
            throw new ResourceNotFoundException("Ativo não encontrado com ID: " + ativoId);
        }
        BigDecimal custoTotal = manutencaoRepository.findCustoTotalManutencaoPorAtivo(ativoId);
        return custoTotal != null ? custoTotal : BigDecimal.ZERO;
    }

    private Manutencao buscarEntidadePorId(Long id) {
        return manutencaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manutenção não encontrada com ID: " + id));
    }

    private void validarStatus(Manutencao manutencao, StatusManutencao statusEsperado, String acao) {
        if (manutencao.getStatus() != statusEsperado) {
            throw new ResourceConflictException("A manutenção não pode ser " + acao + " pois seu status atual é '" + manutencao.getStatus() + "' e o esperado era '" + statusEsperado + "'.");
        }
    }

    private Manutencao convertToEntity(ManutencaoRequestDTO request) {
        Ativo ativo = ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado com ID: " + request.getAtivoId()));

        Pessoa solicitante = pessoaRepository.findById(request.getSolicitanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante não encontrado com ID: " + request.getSolicitanteId()));

        Fornecedor fornecedor = null;
        if (request.getFornecedorId() != null) {
            fornecedor = fornecedorRepository.findById(request.getFornecedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + request.getFornecedorId()));
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
        manutencao.setFornecedor(fornecedor);
        manutencao.setTipo(request.getTipo());
        manutencao.setDescricaoProblema(request.getDescricaoProblema());
        manutencao.setCustoEstimado(request.getCustoEstimado());
        manutencao.setDataPrevistaConclusao(request.getDataPrevistaConclusao());
        manutencao.setObservacoes(request.getObservacoes());

        return manutencao;
    }

    private ManutencaoResponseDTO convertToResponseDTO(Manutencao manutencao) {
        ManutencaoResponseDTO dto = new ManutencaoResponseDTO();
        dto.setId(manutencao.getId());
        dto.setAtivoId(manutencao.getAtivo().getId());
        dto.setAtivoNome(manutencao.getAtivo().getNome());
        dto.setAtivoNumeroPatrimonio(manutencao.getAtivo().getNumeroPatrimonio());
        dto.setTipo(manutencao.getTipo());
        dto.setStatus(manutencao.getStatus());
        dto.setDataSolicitacao(manutencao.getDataSolicitacao());
        dto.setDataInicio(manutencao.getDataInicio());
        dto.setDataConclusao(manutencao.getDataConclusao());
        dto.setDataPrevistaConclusao(manutencao.getDataPrevistaConclusao());
        dto.setDescricaoProblema(manutencao.getDescricaoProblema());
        dto.setDescricaoServico(manutencao.getDescricaoServico());
        dto.setCustoEstimado(manutencao.getCustoEstimado());
        dto.setCustoReal(manutencao.getCustoReal());
        dto.setSolicitanteId(manutencao.getSolicitante().getId());
        dto.setSolicitanteNome(manutencao.getSolicitante().getNome());
        dto.setTempoExecucaoMinutos(manutencao.getTempoExecucaoMinutos());
        dto.setObservacoes(manutencao.getObservacoes());
        dto.setCriadoEm(manutencao.getCriadoEm());
        dto.setAtualizadoEm(manutencao.getAtualizadoEm());

        if (manutencao.getFornecedor() != null) {
            dto.setFornecedorId(manutencao.getFornecedor().getId());
            dto.setFornecedorNome(manutencao.getFornecedor().getNome());
        }

        if (manutencao.getTecnicoResponsavel() != null) {
            dto.setTecnicoResponsavelId(manutencao.getTecnicoResponsavel().getId());
            dto.setTecnicoResponsavelNome(manutencao.getTecnicoResponsavel().getNome());
        }

        return dto;
    }
}