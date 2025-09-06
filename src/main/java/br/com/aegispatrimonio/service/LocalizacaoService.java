package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.LocalizacaoRequestDTO;
import br.com.aegispatrimonio.dto.response.LocalizacaoResponseDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Localizacao;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.LocalizacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<LocalizacaoResponseDTO> listarTodos() {
        log.debug("Listando todas as localizações");
        return localizacaoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LocalizacaoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando localização por ID: {}", id);
        return localizacaoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<LocalizacaoResponseDTO> listarPorFilial(Long filialId) {
        log.debug("Listando localizações por filial: {}", filialId);
        return localizacaoRepository.findByFilialId(filialId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocalizacaoResponseDTO> listarPorLocalizacaoPai(Long localizacaoPaiId) {
        log.debug("Listando localizações por localização pai: {}", localizacaoPaiId);
        return localizacaoRepository.findByLocalizacaoPaiId(localizacaoPaiId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocalizacaoResponseDTO> buscarPorNome(String nome) {
        log.debug("Buscando localizações por nome: {}", nome);
        return localizacaoRepository.findByNomeContaining(nome).stream()
                .map(this::convertToResponseDTO)
                .toList();
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

    // Métodos de conversão
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