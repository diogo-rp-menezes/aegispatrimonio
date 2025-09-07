package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        validarManutencao(request);
        Manutencao manutencao = convertToEntity(request);
        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional(readOnly = true)
    public Optional<ManutencaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando manutenção ID: {}", id);
        return manutencaoRepository.findById(id).map(this::convertToResponseDTO);
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
    public ManutencaoResponseDTO iniciar(Long id, Long tecnicoId) {
        log.info("Iniciando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.APROVADA, "iniciada");
        
        Pessoa tecnico = pessoaRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado: " + tecnicoId));
        
        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ativoRepository.save(ativo);
        
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setTecnicoResponsavel(tecnico);
        manutencao.setDataInicio(LocalDate.now());
        
        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO concluir(Long id, String descricaoServico, BigDecimal custoReal, Integer tempoExecucao) {
        log.info("Concluindo manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.EM_ANDAMENTO, "concluída");
        
        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(ativo);
        
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDescricaoServico(descricaoServico);
        manutencao.setCustoReal(custoReal);
        manutencao.setTempoExecucaoMinutos(tempoExecucao);
        manutencao.setDataConclusao(LocalDate.now());
        
        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO cancelar(Long id, String motivo) {
        log.info("Cancelando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        
        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA || 
            manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new RuntimeException("Manutenção já concluída ou cancelada");
        }
        
        if (manutencao.getStatus() == StatusManutencao.EM_ANDAMENTO) {
            Ativo ativo = manutencao.getAtivo();
            ativo.setStatus(StatusAtivo.ATIVO);
            ativoRepository.save(ativo);
        }
        
        manutencao.setStatus(StatusManutencao.CANCELADA);
        manutencao.setObservacoes(motivo);
        
        return convertToResponseDTO(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando manutenção ID: {}", id);
        Manutencao manutencao = buscarEntidadePorId(id);
        manutencaoRepository.delete(manutencao);
    }

    @Transactional(readOnly = true)
    public BigDecimal custoTotalPorAtivo(Long ativoId) {
        log.debug("Calculando custo total para ativo ID: {}", ativoId);
        BigDecimal custoTotal = manutencaoRepository.findCustoTotalManutencaoPorAtivo(ativoId);
        return custoTotal != null ? custoTotal : BigDecimal.ZERO;
    }

    // Métodos paginados
    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listar(Pageable pageable) {
        log.debug("Listando manutenções paginadas");
        return manutencaoRepository.findAllOrderByDataSolicitacaoDesc(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorAtivo(Long ativoId, Pageable pageable) {
        log.debug("Listando manutenções por ativo ID: {}", ativoId);
        return manutencaoRepository.findByAtivoId(ativoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorStatus(StatusManutencao status, Pageable pageable) {
        log.debug("Listando manutenções por status: {}", status);
        return manutencaoRepository.findByStatus(status, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorTipo(TipoManutencao tipo, Pageable pageable) {
        log.debug("Listando manutenções por tipo: {}", tipo);
        return manutencaoRepository.findByTipo(tipo, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorSolicitante(Long solicitanteId, Pageable pageable) {
        log.debug("Listando manutenções por solicitante ID: {}", solicitanteId);
        return manutencaoRepository.findBySolicitanteId(solicitanteId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorFornecedor(Long fornecedorId, Pageable pageable) {
        log.debug("Listando manutenções por fornecedor ID: {}", fornecedorId);
        return manutencaoRepository.findByFornecedorId(fornecedorId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorPeriodoSolicitacao(LocalDate inicio, LocalDate fim, Pageable pageable) {
        log.debug("Listando manutenções por período de solicitação: {} a {}", inicio, fim);
        return manutencaoRepository.findByPeriodoSolicitacao(inicio, fim, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPorPeriodoConclusao(LocalDate inicio, LocalDate fim, Pageable pageable) {
        log.debug("Listando manutenções por período de conclusão: {} a {}", inicio, fim);
        return manutencaoRepository.findByPeriodoConclusao(inicio, fim, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPendentes(Pageable pageable) {
        log.debug("Listando manutenções pendentes");
        return manutencaoRepository.findManutencoesPendentes(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponseDTO> listarPendentesPorAtivo(Long ativoId, Pageable pageable) {
        log.debug("Listando manutenções pendentes por ativo ID: {}", ativoId);
        return manutencaoRepository.findManutencoesPendentesPorAtivo(ativoId, pageable)
                .map(this::convertToResponseDTO);
    }

    // Métodos auxiliares
    private Manutencao buscarEntidadePorId(Long id) {
        return manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada: " + id));
    }

    private void validarStatus(Manutencao manutencao, StatusManutencao statusEsperado, String acao) {
        if (manutencao.getStatus() != statusEsperado) {
            throw new RuntimeException("Manutenção não pode ser " + acao + ". Status atual: " + manutencao.getStatus());
        }
    }

    private void validarManutencao(ManutencaoRequestDTO request) {
        if (!ativoRepository.existsById(request.getAtivoId())) {
            throw new RuntimeException("Ativo não encontrado: " + request.getAtivoId());
        }
        if (!pessoaRepository.existsById(request.getSolicitanteId())) {
            throw new RuntimeException("Solicitante não encontrado: " + request.getSolicitanteId());
        }
        if (request.getFornecedorId() != null && !fornecedorRepository.existsById(request.getFornecedorId())) {
            throw new RuntimeException("Fornecedor não encontrado: " + request.getFornecedorId());
        }
        if (request.getTecnicoResponsavelId() != null && !pessoaRepository.existsById(request.getTecnicoResponsavelId())) {
            throw new RuntimeException("Técnico não encontrado: " + request.getTecnicoResponsavelId());
        }
    }

    private Manutencao convertToEntity(ManutencaoRequestDTO request) {
        Manutencao manutencao = new Manutencao();
        
        manutencao.setAtivo(ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado")));
        
        manutencao.setSolicitante(pessoaRepository.findById(request.getSolicitanteId())
                .orElseThrow(() -> new RuntimeException("Solicitante não encontrado")));
        
        if (request.getFornecedorId() != null) {
            manutencao.setFornecedor(fornecedorRepository.findById(request.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado")));
        }
        
        if (request.getTecnicoResponsavelId() != null) {
            manutencao.setTecnicoResponsavel(pessoaRepository.findById(request.getTecnicoResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Técnico não encontrado")));
        }
        
        manutencao.setTipo(request.getTipo());
        manutencao.setDescricaoProblema(request.getDescricaoProblema());
        manutencao.setDescricaoServico(request.getDescricaoServico());
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