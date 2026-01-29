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
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final AtivoRepository ativoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider

    @Transactional
    public ManutencaoResponseDTO criar(ManutencaoRequestDTO request) {
        Manutencao manutencao = convertToEntity(request);
        Manutencao savedManutencao = manutencaoRepository.save(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} criou a manutenção com ID {} para o ativo {}.", auditor.getEmail(), savedManutencao.getId(), savedManutencao.getAtivo().getId());

        return convertToResponseDTO(savedManutencao);
    }

    /**
     * Creates a maintenance ticket systematically (autonomous agent), bypassing the logged-in user check.
     * The requester is set to the asset's responsible employee.
     */
    @Transactional
    public Optional<ManutencaoResponseDTO> criarManutencaoSistemica(Ativo ativo, String descricao, TipoManutencao tipo) {
        if (ativo.getFuncionarioResponsavel() == null) {
            log.warn("SYSTEM: Cannot create autonomous maintenance for asset {} ({}) because it has no responsible employee.", ativo.getNome(), ativo.getId());
            return Optional.empty();
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(ativo.getFuncionarioResponsavel());
        manutencao.setTipo(tipo);
        manutencao.setDescricaoProblema(descricao);
        // Defaults
        manutencao.setStatus(StatusManutencao.SOLICITADA);
        manutencao.setDataSolicitacao(LocalDate.now());
        manutencao.setObservacoes("Criado automaticamente pelo Agente de Manutenção Preditiva.");

        Manutencao savedManutencao = manutencaoRepository.save(manutencao);

        log.info("AUDIT: SYSTEM_AUTO criou a manutenção com ID {} para o ativo {}.", savedManutencao.getId(), savedManutencao.getAtivo().getId());

        return Optional.of(convertToResponseDTO(savedManutencao));
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
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.SOLICITADA, "aprovada");
        manutencao.setStatus(StatusManutencao.APROVADA);
        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} aprovou a manutenção com ID {} para o ativo {}.", auditor.getEmail(), updatedManutencao.getId(), updatedManutencao.getAtivo().getId());

        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO iniciar(Long id, ManutencaoInicioDTO inicioDTO) {
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.APROVADA, "iniciada");

        Funcionario tecnico = funcionarioRepository.findById(inicioDTO.tecnicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado com ID: " + inicioDTO.tecnicoId()));

        Ativo ativo = manutencao.getAtivo();
        validarConsistenciaFilial(tecnico, ativo, "Técnico responsável");

        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ativoRepository.save(ativo);

        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setTecnicoResponsavel(tecnico);
        manutencao.setDataInicio(LocalDate.now());

        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} iniciou a manutenção com ID {} para o ativo {} com técnico {}.", auditor.getEmail(), updatedManutencao.getId(), updatedManutencao.getAtivo().getId(), tecnico.getNome());

        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO concluir(Long id, ManutencaoConclusaoDTO conclusaoDTO) {
        Manutencao manutencao = buscarEntidadePorId(id);
        validarStatus(manutencao, StatusManutencao.EM_ANDAMENTO, "concluída");

        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(ativo);

        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDescricaoServico(conclusaoDTO.descricaoServico());
        manutencao.setCustoReal(conclusaoDTO.custoReal());
        manutencao.setTempoExecucaoMinutos(conclusaoDTO.tempoExecucao());
        manutencao.setDataConclusao(LocalDate.now());

        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} concluiu a manutenção com ID {} para o ativo {}.", auditor.getEmail(), updatedManutencao.getId(), updatedManutencao.getAtivo().getId());

        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO cancelar(Long id, ManutencaoCancelDTO cancelDTO) {
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
        manutencao.setObservacoes(cancelDTO.motivo());

        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} cancelou a manutenção com ID {} para o ativo {}. Motivo: {}.", auditor.getEmail(), updatedManutencao.getId(), updatedManutencao.getAtivo().getId(), cancelDTO.motivo());

        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public void deletar(Long id) {
        Manutencao manutencao = buscarEntidadePorId(id);

        if (manutencao.getStatus() != StatusManutencao.SOLICITADA) {
            throw new ResourceConflictException("Apenas manutenções com status 'SOLICITADA' podem ser deletadas. Considere cancelar a manutenção para preservar o histórico.");
        }

        manutencaoRepository.delete(manutencao);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        log.info("AUDIT: Usuário {} deletou a manutenção com ID {} para o ativo {}.", auditor.getEmail(), id, manutencao.getAtivo().getId());
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
        Ativo ativo = ativoRepository.findById(request.ativoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado com ID: " + request.ativoId()));

        if (ativo.getStatus() != StatusAtivo.ATIVO) {
            throw new ResourceConflictException("Não é possível criar manutenção para um ativo que não está com status 'ATIVO'. Status atual: " + ativo.getStatus());
        }

        Funcionario solicitante = funcionarioRepository.findById(request.solicitanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante não encontrado com ID: " + request.solicitanteId()));

        validarConsistenciaFilial(solicitante, ativo, "Solicitante");

        Fornecedor fornecedor = null;
        if (request.fornecedorId() != null) {
            fornecedor = fornecedorRepository.findById(request.fornecedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + request.fornecedorId()));
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setAtivo(ativo);
        manutencao.setSolicitante(solicitante);
        manutencao.setFornecedor(fornecedor);
        manutencao.setTipo(request.tipo());
        manutencao.setDescricaoProblema(request.descricaoProblema());
        manutencao.setCustoEstimado(request.custoEstimado());
        manutencao.setDataPrevistaConclusao(request.dataPrevistaConclusao());
        manutencao.setObservacoes(request.observacoes());

        return manutencao;
    }

    private void validarConsistenciaFilial(Funcionario funcionario, Ativo ativo, String papel) {
        boolean pertenceAFilial = funcionario.getFiliais().stream()
                .anyMatch(f -> f.getId().equals(ativo.getFilial().getId()));
        if (!pertenceAFilial) {
            throw new IllegalArgumentException(papel + " deve pertencer à mesma filial do ativo.");
        }
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
