package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
        
        validarMovimentacao(request);
        Movimentacao movimentacao = convertToEntity(request);
        Movimentacao savedMovimentacao = movimentacaoRepository.save(movimentacao);
        
        return convertToResponseDTO(savedMovimentacao);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarTodos() {
        log.debug("Listando todas as movimentações");
        return movimentacaoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando movimentação por ID: {}", id);
        return movimentacaoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorAtivo(Long ativoId) {
        log.debug("Listando movimentações por ativo ID: {}", ativoId);
        return movimentacaoRepository.findByAtivoId(ativoId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorStatus(StatusMovimentacao status) {
        log.debug("Listando movimentações por status: {}", status);
        return movimentacaoRepository.findByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorPessoaDestino(Long pessoaDestinoId) {
        log.debug("Listando movimentações por pessoa destino ID: {}", pessoaDestinoId);
        return movimentacaoRepository.findByPessoaDestinoId(pessoaDestinoId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorLocalizacaoDestino(Long localizacaoDestinoId) {
        log.debug("Listando movimentações por localização destino ID: {}", localizacaoDestinoId);
        return movimentacaoRepository.findByLocalizacaoDestinoId(localizacaoDestinoId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public MovimentacaoResponseDTO efetivarMovimentacao(Long id) {
        log.info("Efetivando movimentação ID: {}", id);
        
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimentação não encontrada com ID: " + id));
        
        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new RuntimeException("Somente movimentações pendentes podem ser efetivadas");
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
        
        // Verificar se há movimentações pendentes para o mesmo ativo
        List<Movimentacao> movimentacoesPendentes = movimentacaoRepository
                .findMovimentacoesPendentesPorAtivo(request.getAtivoId());
        
        if (!movimentacoesPendentes.isEmpty()) {
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