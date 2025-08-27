package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.exception.NotFoundException; // ✅ Seu próprio NotFoundException
import br.com.aegispatrimonio.exception.DuplicateException;
import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.StatusAtivo;
import br.com.aegispatrimonio.repository.AtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtivoService {

    private final AtivoRepository ativoRepository;

    @Transactional
    public Ativo criar(Ativo ativo) {
        log.info("Criando novo ativo: {}", ativo.getNome());
        validarNumeroPatrimonioUnico(ativo.getNumeroPatrimonio());
        return ativoRepository.save(ativo);
    }

    @Transactional(readOnly = true)
    public Optional<Ativo> buscarPorId(Long id) {
        log.debug("Buscando ativo por ID: {}", id);
        return ativoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Ativo> buscarPorNumeroPatrimonio(String numeroPatrimonio) {
        log.debug("Buscando ativo por número de patrimônio: {}", numeroPatrimonio);
        return ativoRepository.findByNumeroPatrimonio(numeroPatrimonio);
    }

    @Transactional(readOnly = true)
    public List<Ativo> listarPorTipo(Long tipoAtivoId) {
        log.debug("Listando ativos por tipo: {}", tipoAtivoId);
        return ativoRepository.findByTipoAtivoId(tipoAtivoId);
    }

    @Transactional(readOnly = true)
    public List<Ativo> listarPorLocalizacao(Long localizacaoId) {
        log.debug("Listando ativos por localização: {}", localizacaoId);
        return ativoRepository.findByLocalizacaoId(localizacaoId);
    }

    @Transactional(readOnly = true)
    public List<Ativo> listarPorStatus(StatusAtivo status) {
        log.debug("Listando ativos por status: {}", status);
        return ativoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<Ativo> listarTodos(Pageable pageable) {
        log.debug("Listando todos ativos paginados");
        return ativoRepository.findAll(pageable);
    }

    @Transactional
    public Ativo atualizar(Long id, Ativo ativoAtualizado) {
        log.info("Atualizando ativo ID: {}", id);
        Ativo ativoExistente = ativoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ativo não encontrado"));
        
        // Atualiza apenas campos permitidos
        ativoExistente.setNome(ativoAtualizado.getNome());
        ativoExistente.setValorAquisicao(ativoAtualizado.getValorAquisicao());
        ativoExistente.setStatus(ativoAtualizado.getStatus());
        // ... outros campos
        
        return ativoRepository.save(ativoExistente);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando ativo ID: {}", id);
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ativo não encontrado"));
        ativoRepository.delete(ativo);
    }

    @Transactional(readOnly = true)
    public List<Ativo> buscarPorFaixaDeValor(BigDecimal valorMin, BigDecimal valorMax) {
        log.debug("Buscando ativos por faixa de valor: {} - {}", valorMin, valorMax);
        return ativoRepository.findByValorAquisicaoBetween(valorMin, valorMax);
    }

    @Transactional(readOnly = true)
    public boolean existePorNumeroPatrimonio(String numeroPatrimonio) {
        return ativoRepository.existsByNumeroPatrimonio(numeroPatrimonio);
    }

    private void validarNumeroPatrimonioUnico(String numeroPatrimonio) {
        if (ativoRepository.existsByNumeroPatrimonio(numeroPatrimonio)) {
            throw new DuplicateException("Número de patrimônio já existe: " + numeroPatrimonio);
        }
    }
}