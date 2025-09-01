package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
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

    @Transactional
    public DepartamentoResponseDTO criar(DepartamentoRequestDTO request) {
        validarCentroCustoUnico(request.getCentroCusto());
        
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

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> listarPorFilial(Long filialId) {
        return departamentoRepository.findByFilialId(filialId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DepartamentoResponseDTO> buscarPorCentroCusto(String centroCusto) {
        return departamentoRepository.findByCentroCusto(centroCusto)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> buscarPorNome(String nome) {
        return departamentoRepository.findByNomeContaining(nome).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public DepartamentoResponseDTO atualizar(Long id, DepartamentoRequestDTO request) {
        Departamento departamentoExistente = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado com ID: " + id));
        
        if (request.getCentroCusto() != null && !departamentoExistente.getCentroCusto().equals(request.getCentroCusto())) {
            validarCentroCustoUnico(request.getCentroCusto());
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

    // Métodos de conversão
    private Departamento convertToEntity(DepartamentoRequestDTO request) {
        Departamento departamento = new Departamento();
        updateEntityFromRequest(departamento, request);
        return departamento;
    }

    private void updateEntityFromRequest(Departamento departamento, DepartamentoRequestDTO request) {
        departamento.setNome(request.getNome());
        departamento.setCentroCusto(request.getCentroCusto());
        // Filial será setada via ID posteriormente
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

    private void validarCentroCustoUnico(String centroCusto) {
        if (centroCusto != null && departamentoRepository.findByCentroCusto(centroCusto).isPresent()) {
            throw new RuntimeException("Já existe um departamento com o centro de custo: " + centroCusto);
        }
    }
}