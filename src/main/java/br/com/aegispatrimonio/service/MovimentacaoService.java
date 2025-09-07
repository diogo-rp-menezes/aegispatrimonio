package br.com.aegispatrimonio.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.model.Movimentacao;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.StatusMovimentacao;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.MovimentacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final AtivoRepository ativoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public MovimentacaoResponseDTO criar(MovimentacaoRequestDTO request) {
        log.info("Criando nova movimentação para o ativo ID: {}", request.getAtivoId());
        
        validarMovimentacao(request);
        Movimentacao movimentacao = convertToEntity(request);
        Movimentacao savedMovimentacao = movimentacaoRepository.save(movimentacao);
        
        return convertToResponseDTO(savedMovimentacao);
    }

    // TODOS OS MÉTODOS DE CONSULTA AGORA SÃO PAGINADOS
    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findAll(Pageable pageable) {
        log.debug("Listando todas as movimentações paginadas");
        return movimentacaoRepository.findAllOrderByDataMovimentacaoDesc(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando movimentação por ID: {}", id);
        return movimentacaoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByAtivoId(Long ativoId, Pageable pageable) {
        log.debug("Listando movimentações por ativo ID: {}", ativoId);
        return movimentacaoRepository.findByAtivoId(ativoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByStatus(StatusMovimentacao status, Pageable pageable) {
        log.debug("Listando movimentações por status: {}", status);
        return movimentacaoRepository.findByStatus(status, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByPessoaDestinoId(Long pessoaDestinoId, Pageable pageable) {
        log.debug("Listando movimentações por pessoa destino ID: {}", pessoaDestinoId);
        return movimentacaoRepository.findByPessoaDestinoId(pessoaDestinoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable) {
        log.debug("Listando movimentações por localização destino ID: {}", localizacaoDestinoId);
        return movimentacaoRepository.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByPeriodo(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Listando movimentações por período: {} até {}", startDate, endDate);
        return movimentacaoRepository.findByPeriodo(startDate, endDate, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findMovimentacoesPendentesPorAtivo(Long ativoId, Pageable pageable) {
        log.debug("Listando movimentações pendentes por ativo ID: {}", ativoId);
        return movimentacaoRepository.findMovimentacoesPendentesPorAtivo(ativoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public MovimentacaoResponseDTO efetivarMovimentacao(Long id) {
        log.info("Efetivando movimentação ID: {}", id);
        
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
        
        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new RuntimeException("Somente movimentações pendentes podem be efetivadas");
        }
        
        // Atualizar o ativo com nova localização e responsável
        Ativo ativo = movimentacao.getAtivo();
        ativo.setLocalizacao(movimentacao.getLocalizacaoDestino());
        ativo.setPessoaResponsavel(movimentacao.getPessoaDestino());
        ativoRepository.save(ativo);
        
        // Atualizar status da movimentação
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        movimentacao.setDataEfetivacao(LocalDate.now());
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);
        
        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public MovimentacaoResponseDTO cancelarMovimentacao(Long id, String motivoCancelamento) {
        log.info("Cancelando movimentação ID: {}", id);
        
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
        
        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new RuntimeException("Somente movimentações pendentes podem ser canceladas");
        }
        
        movimentacao.setStatus(StatusMovimentacao.CANCELADA);
        movimentacao.setObservacoes(motivoCancelamento);
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);
        
        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando movimentação ID: {}", id);
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
        movimentacaoRepository.delete(movimentacao);
    }

    // Métodos auxiliares
    private void validarMovimentacao(MovimentacaoRequestDTO request) {
        // Verificar se o ativo existe
        if (!ativoRepository.existsById(request.getAtivoId())) {
            throw new RuntimeException("Ativo não encontrado com ID: " + request.getAtivoId());
        }
        
        // ✅ CORRIGIDO - Uso de método mais eficiente para validação
        if (movimentacaoRepository.existsByAtivoIdAndStatus(request.getAtivoId(), StatusMovimentacao.PENDENTE)) {
            throw new RuntimeException("Já existe uma movimentação pendente para este ativo");
        }
        
        // Verificar se localizações existem
        if (!localizacaoRepository.existsById(request.getLocalizacaoOrigemId()) ||
            !localizacaoRepository.existsById(request.getLocalizacaoDestinoId())) {
            throw new RuntimeException("Localização de origem ou destino não encontrada");
        }
        
        // Verificar se pessoas existem
        if (!pessoaRepository.existsById(request.getPessoaOrigemId()) ||
            !pessoaRepository.existsById(request.getPessoaDestinoId())) {
            throw new RuntimeException("Pessoa de origem ou destino não encontrada");
        }
    }

    private Movimentacao convertToEntity(MovimentacaoRequestDTO request) {
        Movimentacao movimentacao = new Movimentacao();
        updateEntityFromRequest(movimentacao, request);
        return movimentacao;
    }

    private void updateEntityFromRequest(Movimentacao movimentacao, MovimentacaoRequestDTO request) {
        // Buscar e configurar entidades relacionadas
        Ativo ativo = ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new RuntimeException("Ativo não encontrado"));
        movimentacao.setAtivo(ativo);
        
        Localizacao localizacaoOrigem = localizacaoRepository.findById(request.getLocalizacaoOrigemId())
                .orElseThrow(() -> new RuntimeException("Localização de origem não encontrada"));
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        
        Localizacao localizacaoDestino = localizacaoRepository.findById(request.getLocalizacaoDestinoId())
                .orElseThrow(() -> new RuntimeException("Localização de destino não encontrada"));
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        
        Pessoa pessoaOrigem = pessoaRepository.findById(request.getPessoaOrigemId())
                .orElseThrow(() -> new RuntimeException("Pessoa de origem não encontrada"));
        movimentacao.setPessoaOrigem(pessoaOrigem);
        
        Pessoa pessoaDestino = pessoaRepository.findById(request.getPessoaDestinoId())
                .orElseThrow(() -> new RuntimeException("Pessoa de destino não encontrada"));
        movimentacao.setPessoaDestino(pessoaDestino);
        
        movimentacao.setDataMovimentacao(request.getDataMovimentacao());
        movimentacao.setMotivo(request.getMotivo());
        movimentacao.setObservacoes(request.getObservacoes());
    }

    private MovimentacaoResponseDTO convertToResponseDTO(Movimentacao movimentacao) {
        MovimentacaoResponseDTO dto = new MovimentacaoResponseDTO();
        dto.setId(movimentacao.getId());
        
        // Ativo
        dto.setAtivoId(movimentacao.getAtivo().getId());
        dto.setAtivoNome(movimentacao.getAtivo().getNome());
        dto.setAtivoNumeroPatrimonio(movimentacao.getAtivo().getNumeroPatrimonio());
        
        // Localizações
        dto.setLocalizacaoOrigemId(movimentacao.getLocalizacaoOrigem().getId());
        dto.setLocalizacaoOrigemNome(movimentacao.getLocalizacaoOrigem().getNome());
        dto.setLocalizacaoDestinoId(movimentacao.getLocalizacaoDestino().getId());
        dto.setLocalizacaoDestinoNome(movimentacao.getLocalizacaoDestino().getNome());
        
        // Pessoas
        dto.setPessoaOrigemId(movimentacao.getPessoaOrigem().getId());
        dto.setPessoaOrigemNome(movimentacao.getPessoaOrigem().getNome());
        dto.setPessoaDestinoId(movimentacao.getPessoaDestino().getId());
        dto.setPessoaDestinoNome(movimentacao.getPessoaDestino().getNome());
        
        // Datas e status
        dto.setDataMovimentacao(movimentacao.getDataMovimentacao());
        dto.setDataEfetivacao(movimentacao.getDataEfetivacao());
        dto.setStatus(movimentacao.getStatus());
        dto.setMotivo(movimentacao.getMotivo());
        dto.setObservacoes(movimentacao.getObservacoes());
        dto.setCriadoEm(movimentacao.getCriadoEm());
        dto.setAtualizadoEm(movimentacao.getAtualizadoEm());
        
        return dto;
    }
}