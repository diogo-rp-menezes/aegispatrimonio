package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.request.PessoaRequestDTO;
import br.com.aegispatrimonio.dto.response.PessoaResponseDTO;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.repository.PessoaRepository;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final DepartamentoRepository departamentoRepository;

    @Transactional
    public PessoaResponseDTO criar(PessoaRequestDTO request) {
        validarEmailUnico(request.getEmail());

        Pessoa pessoa = convertToEntity(request);

        Departamento departamento = departamentoRepository.findById(request.getDepartamentoId())
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado: " + request.getDepartamentoId()));
        pessoa.setDepartamento(departamento);

        return convertToResponseDTO(pessoaRepository.save(pessoa));
    }

    @Transactional(readOnly = true)
    public Optional<PessoaResponseDTO> buscarPorId(Long id) {
        return pessoaRepository.findById(id).map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Optional<PessoaResponseDTO> buscarPorEmail(String email) {
        return pessoaRepository.findByEmail(email).map(this::convertToResponseDTO);
    }

    @Transactional
    public PessoaResponseDTO atualizar(Long id, PessoaRequestDTO request) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada: " + id));

        if (request.getEmail() != null && !pessoa.getEmail().equals(request.getEmail())) {
            validarEmailUnico(request.getEmail());
        }

        updateEntityFromRequest(pessoa, request);

        Departamento departamento = departamentoRepository.findById(request.getDepartamentoId())
                .orElseThrow(() -> new RuntimeException("Departamento não encontrado: " + request.getDepartamentoId()));
        pessoa.setDepartamento(departamento);

        return convertToResponseDTO(pessoaRepository.save(pessoa));
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada: " + id));
        pessoaRepository.delete(pessoa);
    }

    @Transactional(readOnly = true)
    public Page<PessoaResponseDTO> listar(Pageable pageable) {
        // Alterado: Usa o método padrão findAll. A ordenação é definida no Controller.
        return pessoaRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PessoaResponseDTO> listarPorDepartamento(Long departamentoId, Pageable pageable) {
        return pessoaRepository.findByDepartamentoId(departamentoId, pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PessoaResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        // Alterado: Usa o método de busca case-insensitive do repositório.
        return pessoaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToResponseDTO);
    }

    // Métodos auxiliares
    private Pessoa convertToEntity(PessoaRequestDTO request) {
        Pessoa pessoa = new Pessoa();
        updateEntityFromRequest(pessoa, request);
        return pessoa;
    }

    private void updateEntityFromRequest(Pessoa pessoa, PessoaRequestDTO request) {
        pessoa.setNome(request.getNome());
        pessoa.setEmail(request.getEmail());
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
        // Alterado: Usa o método existsByEmail para maior eficiência.
        if (email != null && pessoaRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já cadastrado: " + email);
        }
    }
}