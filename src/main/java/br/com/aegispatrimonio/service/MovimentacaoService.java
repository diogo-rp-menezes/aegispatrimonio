package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.MovimentacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.MovimentacaoResponseDTO;
import br.com.aegispatrimonio.exception.ResourceConflictException;
import br.com.aegispatrimonio.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import br.com.aegispatrimonio.model.*;
import br.com.aegispatrimonio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final AtivoRepository ativoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UserContextService userContextService;

    @Transactional
    public MovimentacaoResponseDTO criar(MovimentacaoRequestDTO request) {
        log.info("Criando nova movimentação para o ativo ID: {}", request.ativoId());

        if (movimentacaoRepository.existsByAtivoIdAndStatus(request.ativoId(), StatusMovimentacao.PENDENTE)) {
            throw new ResourceConflictException("Já existe uma movimentação pendente para este ativo.");
        }

        Movimentacao movimentacao = convertToEntity(request);
        requireAcessoFilialAtivo(movimentacao.getAtivo());
        Movimentacao savedMovimentacao = movimentacaoRepository.save(movimentacao);
        return convertToResponseDTO(savedMovimentacao);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findAll(Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findAll(pageable).map(this::convertToResponseDTO);
        }
        return movimentacaoRepository.findByAtivoFilialIdIn(getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MovimentacaoResponseDTO> buscarPorId(Long id) {
        return movimentacaoRepository.findById(id)
                .map(this::requireAcessoFilial)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByAtivoId(Long ativoId, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByAtivoId(ativoId, pageable).map(this::convertToResponseDTO);
        }
        return movimentacaoRepository.findByAtivoIdAndAtivoFilialIdIn(ativoId, getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByStatus(StatusMovimentacao status, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByStatus(status, pageable).map(this::convertToResponseDTO);
        }
        return movimentacaoRepository.findByStatusAndAtivoFilialIdIn(status, getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByFuncionarioDestinoId(Long funcionarioDestinoId, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByFuncionarioDestinoId(funcionarioDestinoId, pageable)
                    .map(this::convertToResponseDTO);
        }
        return movimentacaoRepository
                .findByFuncionarioDestinoIdAndAtivoFilialIdIn(funcionarioDestinoId, getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByLocalizacaoDestinoId(Long localizacaoDestinoId, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByLocalizacaoDestinoId(localizacaoDestinoId, pageable)
                    .map(this::convertToResponseDTO);
        }
        return movimentacaoRepository
                .findByLocalizacaoDestinoIdAndAtivoFilialIdIn(localizacaoDestinoId, getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findByPeriodo(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByDataMovimentacaoBetween(startDate, endDate, pageable)
                    .map(this::convertToResponseDTO);
        }
        return movimentacaoRepository
                .findByDataMovimentacaoBetweenAndAtivoFilialIdIn(startDate, endDate, getUserFiliais(), pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponseDTO> findMovimentacoesPendentesPorAtivo(Long ativoId, Pageable pageable) {
        if (isAdmin()) {
            return movimentacaoRepository.findByAtivoIdAndStatus(ativoId, StatusMovimentacao.PENDENTE, pageable)
                    .map(this::convertToResponseDTO);
        }
        return movimentacaoRepository
                .findByAtivoIdAndStatusAndAtivoFilialIdIn(ativoId, StatusMovimentacao.PENDENTE, getUserFiliais(),
                        pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public MovimentacaoResponseDTO efetivarMovimentacao(Long id) {
        log.info("Efetivando movimentação ID: {}", id);
        Movimentacao movimentacao = buscarEntidadeComAcesso(id);

        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new ResourceConflictException("Somente movimentações pendentes podem ser efetivadas.");
        }

        Ativo ativo = movimentacao.getAtivo();
        ativo.setLocalizacao(movimentacao.getLocalizacaoDestino());
        ativo.setFuncionarioResponsavel(movimentacao.getFuncionarioDestino());
        ativoRepository.save(ativo);

        movimentacao.setStatus(StatusMovimentacao.EFETIVADA);
        movimentacao.setDataEfetivacao(LocalDate.now());
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);

        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public MovimentacaoResponseDTO cancelarMovimentacao(Long id, String motivoCancelamento) {
        log.info("Cancelando movimentação ID: {}", id);
        Movimentacao movimentacao = buscarEntidadeComAcesso(id);

        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new ResourceConflictException("Somente movimentações pendentes podem ser canceladas.");
        }

        movimentacao.setStatus(StatusMovimentacao.CANCELADA);
        movimentacao.setObservacoes(motivoCancelamento);
        Movimentacao updatedMovimentacao = movimentacaoRepository.save(movimentacao);

        return convertToResponseDTO(updatedMovimentacao);
    }

    @Transactional
    public void deletar(Long id) {
        log.warn("Tentativa de deletar movimentação ID: {}", id);
        Movimentacao movimentacao = buscarEntidadeComAcesso(id);

        if (movimentacao.getStatus() != StatusMovimentacao.PENDENTE) {
            throw new ResourceConflictException("Apenas movimentações com status 'PENDENTE' podem ser deletadas.");
        }

        movimentacaoRepository.delete(movimentacao);
        log.info("Movimentação ID: {} deletada com sucesso.", id);
    }

    private boolean isAdmin() {
        return userContextService.isAdmin();
    }

    private Set<Long> getUserFiliais() {
        return userContextService.getUserFiliais();
    }

    private Movimentacao buscarEntidadeComAcesso(Long id) {
        return movimentacaoRepository.findById(id)
                .map(this::requireAcessoFilial)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada com ID: " + id));
    }

    /**
     * Garante que não-ADMIN só crie/acesse registros de ativos pertencentes às suas filiais.
     */
    private void requireAcessoFilialAtivo(Ativo ativo) {
        if (!isAdmin()) {
            Set<Long> userFiliais = getUserFiliais();
            if (!userFiliais.contains(ativo.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar ativos desta filial.");
            }
        }
    }

    /**
     * Garante que não-ADMIN só acesse movimentações cujo ativo pertence às suas filiais.
     */
    private Movimentacao requireAcessoFilial(Movimentacao movimentacao) {
        if (!isAdmin()) {
            Set<Long> userFiliais = getUserFiliais();
            if (!userFiliais.contains(movimentacao.getAtivo().getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar movimentações desta filial.");
            }
        }
        return movimentacao;
    }

    private Movimentacao convertToEntity(MovimentacaoRequestDTO request) {
        Ativo ativo = ativoRepository.findById(request.ativoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo não encontrado com ID: " + request.ativoId()));

        if (ativo.getStatus() != StatusAtivo.ATIVO) {
            throw new ResourceConflictException(
                    "Não é possível movimentar um ativo que não está com status 'ATIVO'. Status atual: "
                            + ativo.getStatus());
        }

        Localizacao localizacaoOrigem = ativo.getLocalizacao();
        Funcionario funcionarioOrigem = ativo.getFuncionarioResponsavel();

        if (!Objects.equals(localizacaoOrigem.getId(), request.localizacaoOrigemId())) {
            throw new ResourceConflictException(
                    "A localização de origem informada não corresponde à localização atual do ativo.");
        }
        if (!Objects.equals(funcionarioOrigem.getId(), request.funcionarioOrigemId())) {
            throw new ResourceConflictException(
                    "O funcionário de origem informado não corresponde ao responsável atual do ativo.");
        }

        Localizacao localizacaoDestino = localizacaoRepository.findById(request.localizacaoDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Localização de destino não encontrada com ID: " + request.localizacaoDestinoId()));
        Funcionario funcionarioDestino = funcionarioRepository.findById(request.funcionarioDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Funcionário de destino não encontrado com ID: " + request.funcionarioDestinoId()));

        Long filialIdDoAtivo = ativo.getFilial().getId();
        if (!Objects.equals(localizacaoDestino.getFilial().getId(), filialIdDoAtivo)) {
            throw new IllegalArgumentException("A localização de destino deve pertencer à mesma filial do ativo.");
        }
        if (funcionarioDestino.getFiliais().stream().noneMatch(f -> f.getId().equals(filialIdDoAtivo))) {
            throw new IllegalArgumentException("O funcionário de destino deve pertencer à mesma filial do ativo.");
        }

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setAtivo(ativo);
        movimentacao.setLocalizacaoOrigem(localizacaoOrigem);
        movimentacao.setLocalizacaoDestino(localizacaoDestino);
        movimentacao.setFuncionarioOrigem(funcionarioOrigem);
        movimentacao.setFuncionarioDestino(funcionarioDestino);
        movimentacao.setDataMovimentacao(request.dataMovimentacao());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setObservacoes(request.observacoes());
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
        dto.setFuncionarioOrigemId(movimentacao.getFuncionarioOrigem().getId());
        dto.setFuncionarioOrigemNome(movimentacao.getFuncionarioOrigem().getNome());
        dto.setFuncionarioDestinoId(movimentacao.getFuncionarioDestino().getId());
        dto.setFuncionarioDestinoNome(movimentacao.getFuncionarioDestino().getNome());
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
