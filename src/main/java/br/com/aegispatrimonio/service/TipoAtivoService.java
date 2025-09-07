package br.com.aegispatrimonio.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.TipoAtivoRequestDTO;
import br.com.aegispatrimonio.dto.response.TipoAtivoResponseDTO;
import br.com.aegispatrimonio.model.TipoAtivo;
import br.com.aegispatrimonio.repository.TipoAtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TipoAtivoService {

    private final TipoAtivoRepository tipoAtivoRepository;

    @Transactional
    public TipoAtivoResponseDTO criar(TipoAtivoRequestDTO request) {
        log.info("Criando novo tipo de ativo: {}", request.getNome());
        validarNomeUnico(request.getNome());
        
        TipoAtivo tipoAtivo = convertToEntity(request);
        TipoAtivo savedTipoAtivo = tipoAtivoRepository.save(tipoAtivo);
        
        return convertToResponseDTO(savedTipoAtivo);
    }

    @Transactional(readOnly = true)
    public Page<TipoAtivoResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos os tipos de ativo com paginação");
        return tipoAtivoRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<TipoAtivoResponseDTO> buscarPorId(Long id) {
        log.debug("Buscando tipo de ativo por ID: {}", id);
        return tipoAtivoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<TipoAtivoResponseDTO> buscarPorNome(String nome) {
        log.debug("Buscando tipo de ativo por nome: {}", nome);
        return tipoAtivoRepository.findByNome(nome)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<TipoAtivoResponseDTO> buscarPorNomePaginado(String nome, Pageable pageable) {
        log.debug("Buscando tipos de ativo por nome: {} com paginação", nome);
        return tipoAtivoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public TipoAtivoResponseDTO atualizar(Long id, TipoAtivoRequestDTO request) {
        log.info("Atualizando tipo de ativo ID: {}", id);
        TipoAtivo tipoExistente = tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de ativo não encontrado com ID: " + id));
        
        if (!tipoExistente.getNome().equals(request.getNome())) {
            validarNomeUnico(request.getNome());
        }
        
        updateEntityFromRequest(tipoExistente, request);
        TipoAtivo updatedTipoAtivo = tipoAtivoRepository.save(tipoExistente);
        
        return convertToResponseDTO(updatedTipoAtivo);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando tipo de ativo ID: {}", id);
        TipoAtivo tipoAtivo = tipoAtivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de ativo não encontrado com ID: " + id));
        tipoAtivoRepository.delete(tipoAtivo);
    }

    @Transactional(readOnly = true)
    public boolean existePorNome(String nome) {
        return tipoAtivoRepository.existsByNome(nome);
    }

    // Métodos de conversão (MANTIDOS)
    private TipoAtivo convertToEntity(TipoAtivoRequestDTO request) {
        TipoAtivo tipoAtivo = new TipoAtivo();
        updateEntityFromRequest(tipoAtivo, request);
        return tipoAtivo;
    }

    private void updateEntityFromRequest(TipoAtivo tipoAtivo, TipoAtivoRequestDTO request) {
        tipoAtivo.setNome(request.getNome());
        tipoAtivo.setDescricao(request.getDescricao());
        tipoAtivo.setCategoriaContabil(request.getCategoriaContabil());
        tipoAtivo.setIcone(request.getIcone());
    }

    private TipoAtivoResponseDTO convertToResponseDTO(TipoAtivo tipoAtivo) {
        TipoAtivoResponseDTO dto = new TipoAtivoResponseDTO();
        dto.setId(tipoAtivo.getId());
        dto.setNome(tipoAtivo.getNome());
        dto.setDescricao(tipoAtivo.getDescricao());
        dto.setCategoriaContabil(tipoAtivo.getCategoriaContabil());
        dto.setIcone(tipoAtivo.getIcone());
        dto.setCriadoEm(tipoAtivo.getCriadoEm());
        dto.setAtualizadoEm(tipoAtivo.getAtualizadoEm());
        return dto;
    }

    private void validarNomeUnico(String nome) {
        if (tipoAtivoRepository.existsByNome(nome)) {
            throw new RuntimeException("Já existe um tipo de ativo com o nome: " + nome);
        }
    }
}