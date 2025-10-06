package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import br.com.aegispatrimonio.repository.MovimentacaoRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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

        // 1. Validação de regra de negócio
        if (movimentacaoRepository.existsByAtivoIdAndStatus(request.getAtivoId(), StatusMovimentacao.PENDENTE)) {
            throw new ResourceConflictException("Já existe uma movimentação pendente para este ativo");
        }

        // 2. Conversão e validação de existência de entidades
        Movimentacao movimentacao = convertToEntity(request);

        // 3. Persistência
        Movimentacao savedMovimentacao = movimentacaoRepository.save(movimentacao);
        return convertToResponseDTO(savedMovimentacao);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findAll(Pageable pageable) {
        return movimentacaoRepository.findAll(pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoResponseDTO> buscarPorId(Long id) {
        return movimentacaoRepository.findById(id).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByAtivoId(Long ativoId, Pageable pageable) {
        return movimentacaoRepository.findByAtivoId(ativoId, pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByStatus(StatusMovimentacao status, Pageable pageable) {
        return movimentacaoRepository.findByStatus(status, pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByPessoaDestinoId(Long pessoaDestinoId, Pageable pageable) {
        return movimentacaoRepository.findByPessoaDestinoId(pessoaDestinoId, pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable) {
        return movimentacaoRepository.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByPeriodo(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return movimentacaoRepository.findByDataMovimentacaoBetween(startDate, endDate, pageable).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findMovimentacoesPendentesPorAtivo(Long ativoId, Pageable pageable) {
        return movimentacaoRepository.findByAtivoIdAndStatus(ativoId, StatusMovimentacao.PENDENTE, pageable).map(this::convertToResponseDTO);
    }

    @Transactional
    public MovimentacaoResponseDTO efetivarMovimentacao(Long id) {
        log.info("Efetivando movimentação ID: {}", id);
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada com ID: " + id));
        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new ResourceConflictException("Somente movimentações pendentes podem ser efetivadas");
        }
        Ativo ativo = movimentacao.getAtivo();
        ativo.setLocalizacao(movimentacao.getLocalizacaoDestino());
        ativo.setPessoaResponsavel(movimentacao.getPessoaDestino());
        ativoRepository.save(ativo);
        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        movimentacao.setDataEfetivacao(LocalDate.now());
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);
        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public MovimentacaoResponseDTO cancelarMovimentacao(Long id, String motivoCancelamento) {
        log.info("Cancelando movimentação ID: {}", id);
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada com ID: " + id));
        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new ResourceConflictException("Somente movimentações pendentes podem ser canceladas");
        }
        movimentacao.setStatus(StatusMovimentacao.CANCELADA);
        movimentacao.setObservacoes(motivoCancelamento);
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);
        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando movimentação ID: {}", id);
        if (!movimentacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movimentação não encontrada com ID: " + id);
        }
        movimentacaoRepository.deleteById(id);
    }

    private Movimentacao convertToEntity(MovimentacaoRequestDTO request) {
        Ativo ativo = ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado com ID: " + request.getAtivoId()));
        Localizacao localizacaoOrigem = localizacaoRepository.findById(request.getLocalizacaoOrigemId())
                .orElseThrow(() -> new ResourceNotFoundException("Localização de origem não encontrada com ID: " + request.getLocalizacaoOrigemId()));
        Localizacao localizacaoDestino = localizacaoRepository.findById(request.getLocalizacaoDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Localização de destino não encontrada com ID: " + request.getLocalizacaoDestinoId()));
        Pessoa pessoaOrigem = pessoaRepository.findById(request.getPessoaOrigemId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa de origem não encontrada com ID: " + request.getPessoaOrigemId()));
        Pessoa pessoaDestino = pessoaRepository.findById(request.getPessoaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa de destino não encontrada com ID: " + request.getPessoaDestinoId()));

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setAtivo(ativo);
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        movimentacao.setPessoaOrigem(pessoaOrigem);
        movimentacao.setPessoaDestino(pessoaDestino);
        movimentacao.setDataMovimentacao(request.getDataMovimentacao());
        movimentacao.setMotivo(request.getMotivo());
        movimentacao.setObservacoes(request.getObservacoes());
        return movimentacao;
    }

    private MovimentacaoResponseDTO convertToResponseDTO(Movimentacao movimentacao) {
        MovimentacaoResponseDTO dto = new MovimentacaoResponseDTO();
        dto.setId(movimentacao.getId());
        dto.setAtivoId(movimentacao.getAtivo().getId());
        dto.setAtivoNome(movimentacao.getAtivo().getNome());
        dto.setAtivoNumeroPatrimonio(movimentacao.getAtivo().getNumeroPatrimonio());
        dto.setLocalizacaoOrigemId(movimentacao.getLocalizacaoOrigem().getId());
        dto.setLocalizacaoOrigemNome(movimentacao.getLocalizacaoOrigem().getNome());
        dto.setLocalizacaoDestinoId(movimentacao.getLocalizacaoDestino().getId());
        dto.setLocalizacaoDestinoNome(movimentacao.getLocalizacaoDestino().getNome());
        dto.setPessoaOrigemId(movimentacao.getPessoaOrigem().getId());
        dto.setPessoaOrigemNome(movimentacao.getPessoaOrigem().getNome());
        dto.setPessoaDestinoId(movimentacao.getPessoaDestino().getId());
        dto.setPessoaDestinoNome(movimentacao.getPessoaDestino().getNome());
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