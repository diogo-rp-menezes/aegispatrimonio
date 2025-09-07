package br.com.aegispatrimonio.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.FilialRequestDTO;
import br.com.aegispatrimonio.dto.response.FilialResponseDTO;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.FilialRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FilialService {

    private final FilialRepository filialRepository;

    @Transactional
    public FilialResponseDTO criar(FilialRequestDTO request) {
        validarCodigoUnico(request.getCodigo());
        validarNomeUnico(request.getNome());
        
        Filial filial = convertToEntity(request);
        Filial savedFilial = filialRepository.save(filial);
        
        return convertToResponseDTO(savedFilial);
    }

    @Transactional(readOnly = true)
    public Page<FilialResponseDTO> listarTodos(Pageable pageable) {
        return filialRepository.findAllOrderByNome(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<FilialResponseDTO> buscarPorId(Long id) {
        return filialRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<FilialResponseDTO> buscarPorCodigo(String codigo) {
        return filialRepository.findByCodigo(codigo)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<FilialResponseDTO> buscarPorNome(String nome) {
        return filialRepository.findByNome(nome)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<FilialResponseDTO> buscarPorNomeContendo(String nome, Pageable pageable) {
        return filialRepository.findByNomeContaining(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public FilialResponseDTO atualizar(Long id, FilialRequestDTO request) {
        Filial filialExistente = filialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filial não encontrada com ID: " + id));
        
        if (!filialExistente.getCodigo().equals(request.getCodigo())) {
            validarCodigoUnico(request.getCodigo());
        }
        
        if (!filialExistente.getNome().equals(request.getNome())) {
            validarNomeUnico(request.getNome());
        }
        
        updateEntityFromRequest(filialExistente, request);
        Filial updatedFilial = filialRepository.save(filialExistente);
        
        return convertToResponseDTO(updatedFilial);
    }

    @Transactional
    public void deletar(Long id) {
        Filial filial = filialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filial não encontrada com ID: " + id));
        filialRepository.delete(filial);
    }

    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return filialRepository.existsByCodigo(codigo);
    }

    // Métodos de conversão
    private Filial convertToEntity(FilialRequestDTO request) {
        Filial filial = new Filial();
        updateEntityFromRequest(filial, request);
        return filial;
    }

    private void updateEntityFromRequest(Filial filial, FilialRequestDTO request) {
        filial.setNome(request.getNome());
        filial.setCodigo(request.getCodigo());
    }

    private FilialResponseDTO convertToResponseDTO(Filial filial) {
        FilialResponseDTO dto = new FilialResponseDTO();
        dto.setId(filial.getId());
        dto.setNome(filial.getNome());
        dto.setCodigo(filial.getCodigo());
        dto.setCriadoEm(filial.getCriadoEm());
        dto.setAtualizadoEm(filial.getAtualizadoEm());
        return dto;
    }

    private void validarCodigoUnico(String codigo) {
        if (filialRepository.existsByCodigo(codigo)) {
            throw new RuntimeException("Já existe uma filial com o código: " + codigo);
        }
    }

    private void validarNomeUnico(String nome) {
        if (filialRepository.findByNome(nome).isPresent()) {
            throw new RuntimeException("Já existe uma filial com o nome: " + nome);
        }
    }
}