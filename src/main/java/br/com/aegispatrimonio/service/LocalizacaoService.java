package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.LocalizacaoResponseDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LocalizacaoService {

    private final LocalizacaoRepository localizacaoRepository;
    private final FilialRepository filialRepository;

    @Transactional
    public LocalizacaoResponseDTO criar(LocalizacaoRequestDTO request) {
        log.info("Criando nova localização: {}", request.getNome());

        Localizacao localizacao = convertToEntity(request);
        Localizacao savedLocalizacao = localizacaoRepository.save(localizacao);

        return convertToResponseDTO(savedLocalizacao);
    }

    @Transactional(readOnly = true)
    public Page<LocalizacaoResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando localizações paginadas");
        return localizacaoRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LocalizacaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando localização por ID: {}", id);
        return localizacaoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<LocalizacaoResponseDTO> listarPorFilial(Long filialId, Pageable pageable) {
        log.debug("Listando localizações por filial paginadas");
        return localizacaoRepository.findByFilialId(filialId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<LocalizacaoResponseDTO> listarPorLocalizacaoPai(Long localizacaoPaiId, Pageable pageable) {
        log.debug("Listando localizações por localização pai paginadas");
        return localizacaoRepository.findByLocalizacaoPaiId(localizacaoPaiId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<LocalizacaoResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        log.debug("Buscando localizações por nome paginadas");
        // Alterado: Usa o método de busca case-insensitive do repositório.
        return localizacaoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public LocalizacaoResponseDTO atualizar(Long id, LocalizacaoRequestDTO request) {
        log.info("Atualizando localização ID: {}", id);

        Localizacao localizacaoExistente = localizacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Localização não encontrada com ID: " + id));

        updateEntityFromRequest(localizacaoExistente, request);
        Localizacao updatedLocalizacao = localizacaoRepository.save(localizacaoExistente);

        return convertToResponseDTO(updatedLocalizacao);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando localização ID: {}", id);
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Localização não encontrada com ID: " + id));
        localizacaoRepository.delete(localizacao);
    }

    // Métodos de conversão (mantidos exatamente como estavam)
    private Localizacao convertToEntity(LocalizacaoRequestDTO request) {
        Localizacao localizacao = new Localizacao();
        updateEntityFromRequest(localizacao, request);
        return localizacao;
    }

    private void updateEntityFromRequest(Localizacao localizacao, LocalizacaoRequestDTO request) {
        localizacao.setNome(request.getNome());
        localizacao.setDescricao(request.getDescricao());

        // Configurar filial
        Filial filial = filialRepository.findById(request.getFilialId())
                .orElseThrow(() -> new RuntimeException("Filial não encontrada com ID: " + request.getFilialId()));
        localizacao.setFilial(filial);

        // Configurar localização pai (se existir)
        if (request.getLocalizacaoPaiId() != null) {
            Localizacao localizacaoPai = localizacaoRepository.findById(request.getLocalizacaoPaiId())
                    .orElseThrow(() -> new RuntimeException("Localização pai não encontrada com ID: " + request.getLocalizacaoPaiId()));
            localizacao.setLocalizacaoPai(localizacaoPai);
        } else {
            localizacao.setLocalizacaoPai(null);
        }
    }

    private LocalizacaoResponseDTO convertToResponseDTO(Localizacao localizacao) {
        LocalizacaoResponseDTO dto = new LocalizacaoResponseDTO();
        dto.setId(localizacao.getId());
        dto.setNome(localizacao.getNome());
        dto.setDescricao(localizacao.getDescricao());
        dto.setCriadoEm(localizacao.getCriadoEm());
        dto.setAtualizadoEm(localizacao.getAtualizadoEm());

        // Configurar filial
        if (localizacao.getFilial() != null) {
            dto.setFilialId(localizacao.getFilial().getId());
            dto.setFilialNome(localizacao.getFilial().getNome());
        }

        // Configurar localização pai
        if (localizacao.getLocalizacaoPai() != null) {
            dto.setLocalizacaoPaiId(localizacao.getLocalizacaoPai().getId());
            dto.setLocalizacaoPaiNome(localizacao.getLocalizacaoPai().getNome());
        }

        return dto;
    }
}