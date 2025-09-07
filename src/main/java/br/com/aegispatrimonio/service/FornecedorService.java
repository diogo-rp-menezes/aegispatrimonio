package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.FornecedorRequestDTO;
import br.com.aegispatrimonio.dto.response.FornecedorResponseDTO;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Transactional
    public FornecedorResponseDTO criar(FornecedorRequestDTO request) {
        validarNomeUnico(request.getNome());
        
        Fornecedor fornecedor = convertToEntity(request);
        Fornecedor savedFornecedor = fornecedorRepository.save(fornecedor);
        
        return convertToResponseDTO(savedFornecedor);
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponseDTO> listarTodos(Pageable pageable) {
        return fornecedorRepository.findAllOrderByNome(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<FornecedorResponseDTO> buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<FornecedorResponseDTO> buscarPorNome(String nome) {
        return fornecedorRepository.findByNome(nome)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponseDTO> buscarPorEmail(String email, Pageable pageable) {
        return fornecedorRepository.findByEmailContato(email, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponseDTO> buscarPorNomeContendo(String nome, Pageable pageable) {
        return fornecedorRepository.findByNomeContaining(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public FornecedorResponseDTO atualizar(Long id, FornecedorRequestDTO request) {
        Fornecedor fornecedorExistente = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com ID: " + id));
        
        if (!fornecedorExistente.getNome().equals(request.getNome())) {
            validarNomeUnico(request.getNome());
        }
        
        updateEntityFromRequest(fornecedorExistente, request);
        Fornecedor updatedFornecedor = fornecedorRepository.save(fornecedorExistente);
        
        return convertToResponseDTO(updatedFornecedor);
    }

    @Transactional
    public void deletar(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com ID: " + id));
        fornecedorRepository.delete(fornecedor);
    }

    // Métodos de conversão
    private Fornecedor convertToEntity(FornecedorRequestDTO request) {
        Fornecedor fornecedor = new Fornecedor();
        updateEntityFromRequest(fornecedor, request);
        return fornecedor;
    }

    private void updateEntityFromRequest(Fornecedor fornecedor, FornecedorRequestDTO request) {
        fornecedor.setNome(request.getNome());
        fornecedor.setEmailContato(request.getEmailContato());
        fornecedor.setTelefoneContato(request.getTelefoneContato());
    }

    private FornecedorResponseDTO convertToResponseDTO(Fornecedor fornecedor) {
        FornecedorResponseDTO dto = new FornecedorResponseDTO();
        dto.setId(fornecedor.getId());
        dto.setNome(fornecedor.getNome());
        dto.setEmailContato(fornecedor.getEmailContato());
        dto.setTelefoneContato(fornecedor.getTelefoneContato());
        dto.setCriadoEm(fornecedor.getCriadoEm());
        dto.setAtualizadoEm(fornecedor.getAtualizadoEm());
        return dto;
    }

    private void validarNomeUnico(String nome) {
        if (fornecedorRepository.existsByNome(nome)) {
            throw new RuntimeException("Já existe um fornecedor com o nome: " + nome);
        }
    }
}