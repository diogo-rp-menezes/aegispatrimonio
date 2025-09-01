package br.com.aegispatrimonio.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.DepartamentoRequestDTO;
import br.com.aegispatrimonio.dto.response.DepartamentoResponseDTO;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    @Transactional
    public DepartamentoResponseDTO criar(DepartamentoRequestDTO request) {
        validarRequest(request);
        validarCentroCustoUnico(request.getCentroCusto(), null);
        
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
    public DepartamentoResponseDTO buscarPorId(Long id) {
        return convertToResponseDTO(
            departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado com ID: " + id))
        );
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
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Nome não pode ser vazio para busca");
        }
        
        return departamentoRepository.findByNomeContaining(nome.trim()).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public DepartamentoResponseDTO atualizar(Long id, DepartamentoRequestDTO request) {
        validarRequest(request);
        
        Departamento departamentoExistente = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado com ID: " + id));
        
        validarCentroCustoUnico(request.getCentroCusto(), id);
        
        updateEntityFromRequest(departamentoExistente, request);
        Departamento updatedDepartamento = departamentoRepository.save(departamentoExistente);
        
        return convertToResponseDTO(updatedDepartamento);
    }

    @Transactional
    public void deletar(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new RuntimeException("Departamento não encontrado com ID: " + id);
        }
        departamentoRepository.deleteById(id);
    }

    // Métodos auxiliares
    private void validarRequest(DepartamentoRequestDTO request) {
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome do departamento é obrigatório");
        }
        
        if (request.getCentroCusto() == null || request.getCentroCusto().trim().isEmpty()) {
            throw new RuntimeException("Centro de custo é obrigatório");
        }
        
        if (request.getFilialId() == null) {
            throw new RuntimeException("ID da filial é obrigatório");
        }
    }

    private void validarCentroCustoUnico(String centroCusto, Long idIgnorado) {
        if (centroCusto != null) {
            Optional<Departamento> departamentoExistente = departamentoRepository.findByCentroCusto(centroCusto);
            
            if (departamentoExistente.isPresent() && 
                (idIgnorado == null || !departamentoExistente.get().getId().equals(idIgnorado))) {
                throw new RuntimeException("Já existe um departamento com o centro de custo: " + centroCusto);
            }
        }
    }

    private Departamento convertToEntity(DepartamentoRequestDTO request) {
        Departamento departamento = new Departamento();
        updateEntityFromRequest(departamento, request);
        return departamento;
    }

    private void updateEntityFromRequest(Departamento departamento, DepartamentoRequestDTO request) {
        departamento.setNome(request.getNome().trim());
        departamento.setCentroCusto(request.getCentroCusto().trim());
        // Filial será setada via ID posteriormente no controller ou via mapper
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
}