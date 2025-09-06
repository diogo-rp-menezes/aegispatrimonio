package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.ManutencaoRequestDTO;
import br.com.aegispatrimonio.dto.response.ManutencaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        log.info("Criando nova solicitação de manutenção para o ativo ID: {}", request.getAtivoId());
        
        validarManutencao(request);
        Manutencao manutencao = convertToEntity(request);
        Manutencao savedManutencao = manutencaoRepository.save(manutencao);
        
        return convertToResponseDTO(savedManutencao);
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarTodos() {
        log.debug("Listando todas as manutenções");
        return manutencaoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ManutencaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando manutenção por ID: {}", id);
        return manutencaoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarPorAtivo(Long ativoId) {
        log.debug("Listando manutenções por ativo ID: {}", ativoId);
        return manutencaoRepository.findByAtivoId(ativoId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarPorStatus(StatusManutencao status) {
        log.debug("Listando manutenções por status: {}", status);
        return manutencaoRepository.findByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarPorTipo(TipoManutencao tipo) {
        log.debug("Listando manutenções por tipo: {}", tipo);
        return manutencaoRepository.findByTipo(tipo).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarPendentes() {
        log.debug("Listando manutenções pendentes");
        return manutencaoRepository.findManutencoesPendentes().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public ManutencaoResponseDTO aprovarManutencao(Long id) {
        log.info("Aprovando manutenção ID: {}", id);
        
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada com ID: " + id));
        
        if (manutencao.getStatus() != StatusManutencao.SOLICITADA) {
            throw new RuntimeException("Somente manutenções solicitadas podem ser aprovadas");
        }
        
        manutencao.setStatus(StatusManutencao.APROVADA);
        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);
        
        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO iniciarManutencao(Long id, Long tecnicoResponsavelId) {
        log.info("Iniciando manutenção ID: {}", id);
        
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada com ID: " + id));
        
        if (manutencao.getStatus() != StatusManutencao.APROVADA) {
            throw new RuntimeException("Somente manutenções aprovadas podem ser iniciadas");
        }
        
        Pessoa tecnico = pessoaRepository.findById(tecnicoResponsavelId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado com ID: " + tecnicoResponsavelId));
        
        // Atualizar status do ativo para em manutenção
        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.EM_MANUTENCAO);
        ativoRepository.save(ativo);
        
        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setTecnicoResponsavel(tecnico);
        manutencao.setDataInicio(LocalDate.now());
        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);
        
        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO concluirManutencao(Long id, String descricaoServico, BigDecimal custoReal, 
                                                   Integer tempoExecucaoMinutos) {
        log.info("Concluindo manutenção ID: {}", id);
        
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada com ID: " + id));
        
        if (manutencao.getStatus() != StatusManutencao.EM_ANDAMENTO) {
            throw new RuntimeException("Somente manutenções em andamento podem ser concluídas");
        }
        
        // Atualizar status do ativo para ativo
        Ativo ativo = manutencao.getAtivo();
        ativo.setStatus(StatusAtivo.ATIVO);
        ativoRepository.save(ativo);
        
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDescricaoServico(descricaoServico);
        manutencao.setCustoReal(custoReal);
        manutencao.setTempoExecucaoMinutos(tempoExecucaoMinutos);
        manutencao.setDataConclusao(LocalDate.now());
        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);
        
        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional
    public ManutencaoResponseDTO cancelarManutencao(Long id, String motivo) {
        log.info("Cancelando manutenção ID: {}", id);
        
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada com ID: " + id));
        
        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA || 
            manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new RuntimeException("Manutenção já concluída ou cancelada não pode ser cancelada");
        }
        
        // Se estava em andamento, reativar o ativo
        if (manutencao.getStatus() == StatusManutencao.EM_ANDAMENTO) {
            Ativo ativo = manutencao.getAtivo();
            ativo.setStatus(StatusAtivo.ATIVO);
            ativoRepository.save(ativo);
        }
        
        manutencao.setStatus(StatusManutencao.CANCELADA);
        manutencao.setObservacoes(motivo);
        Manutencao updatedManutencao = manutencaoRepository.save(manutencao);
        
        return convertToResponseDTO(updatedManutencao);
    }

    @Transactional(readOnly = true)
    public BigDecimal obterCustoTotalManutencaoPorAtivo(Long ativoId) {
        log.debug("Obtendo custo total de manutenção para ativo ID: {}", ativoId);
        BigDecimal custoTotal = manutencaoRepository.findCustoTotalManutencaoPorAtivo(ativoId);
        return custoTotal != null ? custoTotal : BigDecimal.ZERO;
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando manutenção ID: {}", id);
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada com ID: " + id));
        manutencaoRepository.delete(manutencao);
    }

    // Métodos auxiliares
    private void validarManutencao(ManutencaoRequestDTO request) {
        // Verificar se o ativo existe
        if (!ativoRepository.existsById(request.getAtivoId())) {
            throw new RuntimeException("Ativo não encontrado com ID: " + request.getAtivoId());
        }
        
        // Verificar se solicitante existe
        if (!pessoaRepository.existsById(request.getSolicitanteId())) {
            throw new RuntimeException("Solicitante não encontrado com ID: " + request.getSolicitanteId());
        }
        
        // Verificar se fornecedor existe (se informado)
        if (request.getFornecedorId() != null && !fornecedorRepository.existsById(request.getFornecedorId())) {
            throw new RuntimeException("Fornecedor não encontrado com ID: " + request.getFornecedorId());
        }
        
        // Verificar se técnico existe (se informado)
        if (request.getTecnicoResponsavelId() != null && !pessoaRepository.existsById(request.getTecnicoResponsavelId())) {
            throw new RuntimeException("Técnico não encontrado com ID: " + request.getTecnicoResponsavelId());
        }
    }

    private Manutencao convertToEntity(ManutencaoRequestDTO request) {
        Manutencao manutencao = new Manutencao();
        updateEntityFromRequest(manutencao, request);
        return manutencao;
    }

    private void updateEntityFromRequest(Manutencao manutencao, ManutencaoRequestDTO request) {
        // Buscar e configurar entidades relacionadas
        Ativo ativo = ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado"));
        manutencao.setAtivo(ativo);
        
        Pessoa solicitante = pessoaRepository.findById(request.getSolicitanteId())
                .orElseThrow(() -> new RuntimeException("Solicitante não encontrado"));
        manutencao.setSolicitante(solicitante);
        
        if (request.getFornecedorId() != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(request.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            manutencao.setFornecedor(fornecedor);
        }
        
        if (request.getTecnicoResponsavelId() != null) {
            Pessoa tecnico = pessoaRepository.findById(request.getTecnicoResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));
            manutencao.setTecnicoResponsavel(tecnico);
        }
        
        manutencao.setTipo(request.getTipo());
        manutencao.setDescricaoProblema(request.getDescricaoProblema());
        manutencao.setDescricaoServico(request.getDescricaoServico());
        manutencao.setCustoEstimado(request.getCustoEstimado());
        manutencao.setDataPrevistaConclusao(request.getDataPrevistaConclusao());
        manutencao.setObservacoes(request.getObservacoes());
    }

    private ManutencaoResponseDTO convertToResponseDTO(Manutencao manutencao) {
        ManutencaoResponseDTO dto = new ManutencaoResponseDTO();
        dto.setId(manutencao.getId());
        
        // Ativo
        dto.setAtivoId(manutencao.getAtivo().getId());
        dto.setAtivoNome(manutencao.getAtivo().getNome());
        dto.setAtivoNumeroPatrimonio(manutencao.getAtivo().getNumeroPatrimonio());
        
        // Status e tipo
        dto.setTipo(manutencao.getTipo());
        dto.setStatus(manutencao.getStatus());
        
        // Datas
        dto.setDataSolicitacao(manutencao.getDataSolicitacao());
        dto.setDataInicio(manutencao.getDataInicio());
        dto.setDataConclusao(manutencao.getDataConclusao());
        dto.setDataPrevistaConclusao(manutencao.getDataPrevistaConclusao());
        
        // Descrições e custos
        dto.setDescricaoProblema(manutencao.getDescricaoProblema());
        dto.setDescricaoServico(manutencao.getDescricaoServico());
        dto.setCustoEstimado(manutencao.getCustoEstimado());
        dto.setCustoReal(manutencao.getCustoReal());
        
        // Fornecedor
        if (manutencao.getFornecedor() != null) {
            dto.setFornecedorId(manutencao.getFornecedor().getId());
            dto.setFornecedorNome(manutencao.getFornecedor().getNome());
        }
        
        // Pessoas
        dto.setSolicitanteId(manutencao.getSolicitante().getId());
        dto.setSolicitanteNome(manutencao.getSolicitante().getNome());
        
        if (manutencao.getTecnicoResponsavel() != null) {
            dto.setTecnicoResponsavelId(manutencao.getTecnicoResponsavel().getId());
            dto.setTecnicoResponsavelNome(manutencao.getTecnicoResponsavel().getNome());
        }
        
        // Outros campos
        dto.setTempoExecucaoMinutos(manutencao.getTempoExecucaoMinutos());
        dto.setObservacoes(manutencao.getObservacoes());
        dto.setCriadoEm(manutencao.getCriadoEm());
        dto.setAtualizadoEm(manutencao.getAtualizadoEm());
        
        return dto;
    }
}