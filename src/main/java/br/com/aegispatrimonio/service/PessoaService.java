package br.com.aegispatrimonio.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    @Transactional
    public PessoaResponseDTO criar(PessoaRequestDTO request) {
        validarEmailUnico(request.getEmail());
        
        Pessoa pessoa = convertToEntity(request);
        Pessoa savedPessoa = pessoaRepository.save(pessoa);
        
        return convertToResponseDTO(savedPessoa);
    }

    @Transactional(readOnly = true)
    public List<PessoaResponseDTO> listarTodos() {
        return pessoaRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PessoaResponseDTO buscarPorId(Long id) {
        return pessoaRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public PessoaResponseDTO buscarPorEmail(String email) {
        return pessoaRepository.findByEmail(email)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com email: " + email));
    }

    @Transactional(readOnly = true)
    public List<PessoaResponseDTO> listarPorDepartamento(Long departamentoId) {
        return pessoaRepository.findByDepartamentoId(departamentoId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PessoaResponseDTO> buscarPorNome(String nome) {
        return pessoaRepository.findByNomeContaining(nome).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional
    public PessoaResponseDTO atualizar(Long id, PessoaRequestDTO request) {
        Pessoa pessoaExistente = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));
        
        if (request.getEmail() != null && !pessoaExistente.getEmail().equals(request.getEmail())) {
            validarEmailUnico(request.getEmail());
        }
        
        updateEntityFromRequest(pessoaExistente, request);
        Pessoa updatedPessoa = pessoaRepository.save(pessoaExistente);
        
        return convertToResponseDTO(updatedPessoa);
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));
        pessoaRepository.delete(pessoa);
    }

    // Métodos de conversão
    private Pessoa convertToEntity(PessoaRequestDTO request) {
        Pessoa pessoa = new Pessoa();
        updateEntityFromRequest(pessoa, request);
        return pessoa;
    }

    private void updateEntityFromRequest(Pessoa pessoa, PessoaRequestDTO request) {
        pessoa.setNome(request.getNome());
        pessoa.setEmail(request.getEmail());
        // Departamento será setado via ID posteriormente
    }

    private PessoaResponseDTO convertToResponseDTO(Pessoa pessoa) {
        PessoaResponseDTO dto = new PessoaResponseDTO();
        dto.setId(pessoa.getId());
        dto.setNome(pessoa.getNome());
        dto.setEmail(pessoa.getEmail());
        dto.setCriadoEm(pessoa.getCriadoEm());
        dto.setAtualizadoEm(pessoa.getAtualizadoEm());
        
        if (pessoa.getDepartamento() != null) {
            dto.setDepartamentoId(pessoa.getDepartamento().getId());
            dto.setDepartamentoNome(pessoa.getDepartamento().getNome());
        }
        
        return dto;
    }

    private void validarEmailUnico(String email) {
        if (email != null && pessoaRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Já existe uma pessoa com o email: " + email);
        }
    }
}