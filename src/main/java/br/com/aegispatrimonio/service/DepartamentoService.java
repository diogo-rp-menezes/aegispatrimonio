package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final FilialRepository filialRepository;

    @Transactional
    public DepartamentoResponseDTO criar(DepartamentoRequestDTO request) {
        validarFilial(request.getFilialId());
        
        Departamento departamento = convertToEntity(request);
        Departamento savedDepartamento = departamentoRepository.save(departamento);
        
        return convertToResponseDTO(savedDepartamento);
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> listarTodos() {
        return departamentoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DepartamentoResponseDTO> buscarPorId(Long id) {
        return departamentoRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public DepartamentoResponseDTO atualizar(Long id, DepartamentoRequestDTO request) {
        Departamento departamentoExistente = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado com ID: " + id));
        
        if (!departamentoExistente.getFilial().getId().equals(request.getFilialId())) {
            validarFilial(request.getFilialId());
        }
        
        updateEntityFromRequest(departamentoExistente, request);
        Departamento updatedDepartamento = departamentoRepository.save(departamentoExistente);
        
        return convertToResponseDTO(updatedDepartamento);
    }

    @Transactional
    public void deletar(Long id) {
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado com ID: " + id));
        departamentoRepository.delete(departamento);
    }

    @Transactional(readOnly = true)
    public boolean existePorId(Long id) {
        return departamentoRepository.existsById(id);
    }

    // Métodos de conversão
    private Departamento convertToEntity(DepartamentoRequestDTO request) {
        Departamento departamento = new Departamento();
        updateEntityFromRequest(departamento, request);
        return departamento;
    }

    private void updateEntityFromRequest(Departamento departamento, DepartamentoRequestDTO request) {
        departamento.setNome(request.getNome());
        departamento.setCentroCusto(request.getCentroCusto());
        
        // Configurar filial
        Filial filial = filialRepository.findById(request.getFilialId())
                .orElseThrow(() -> new RuntimeException("Filial não encontrada com ID: " + request.getFilialId()));
        departamento.setFilial(filial);
    }

    private DepartamentoResponseDTO convertToResponseDTO(Departamento departamento) {
        DepartamentoResponseDTO dto = new DepartamentoResponseDTO();
        dto.setId(departamento.getId());
        dto.setNome(departamento.getNome());
        dto.setCentroCusto(departamento.getCentroCusto());
        dto.setCriadoEm(departamento.getCriadoEm());
        dto.setAtualizadoEm(departamento.getAtualizadoEm());
        
        if (departamento.getFilial() != null) {
            dto.setFilialId(departamento.getFilial().getId());
            dto.setFilialNome(departamento.getFilial().getNome());
        }
        
        return dto;
    }

    private void validarFilial(Long filialId) {
        if (!filialRepository.existsById(filialId)) {
            throw new RuntimeException("Filial não encontrada com ID: " + filialId);
        }
    }
}