package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.mapper.FornecedorMapper;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;
    private final AtivoRepository ativoRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository, FornecedorMapper fornecedorMapper, AtivoRepository ativoRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorMapper = fornecedorMapper;
        this.ativoRepository = ativoRepository;
    }

    @Transactional(readOnly = true)
    public List<FornecedorDTO> listarTodos() {
        return fornecedorRepository.findAll()
                .stream()
                .map(fornecedorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FornecedorDTO buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .map(fornecedorMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));
    }

    @Transactional
    public FornecedorDTO criar(FornecedorCreateDTO fornecedorCreateDTO) {
        validarCnpjUnico(fornecedorCreateDTO.cnpj(), null);

        Fornecedor fornecedor = fornecedorMapper.toEntity(fornecedorCreateDTO);
        Fornecedor fornecedorSalvo = fornecedorRepository.save(fornecedor);
        return fornecedorMapper.toDTO(fornecedorSalvo);
    }

    @Transactional
    public FornecedorDTO atualizar(Long id, FornecedorUpdateDTO fornecedorUpdateDTO) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));

        validarCnpjUnico(fornecedorUpdateDTO.cnpj(), id);

        fornecedor.setNome(fornecedorUpdateDTO.nome());
        fornecedor.setCnpj(fornecedorUpdateDTO.cnpj());
        fornecedor.setEndereco(fornecedorUpdateDTO.endereco());
        fornecedor.setNomeContatoPrincipal(fornecedorUpdateDTO.nomeContatoPrincipal());
        fornecedor.setEmailPrincipal(fornecedorUpdateDTO.emailPrincipal());
        fornecedor.setTelefonePrincipal(fornecedorUpdateDTO.telefonePrincipal());
        fornecedor.setObservacoes(fornecedorUpdateDTO.observacoes());
        fornecedor.setStatus(fornecedorUpdateDTO.status());

        Fornecedor fornecedorAtualizado = fornecedorRepository.save(fornecedor);
        return fornecedorMapper.toDTO(fornecedorAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));

        if (ativoRepository.existsByFornecedorId(id)) {
            throw new IllegalStateException("Não é possível deletar o fornecedor, pois existem ativos associados a ele.");
        }

        fornecedorRepository.delete(fornecedor);
    }

    private void validarCnpjUnico(String cnpj, Long fornecedorId) {
        Optional<Fornecedor> fornecedorExistente = fornecedorRepository.findByCnpj(cnpj);
        if (fornecedorExistente.isPresent() && !fornecedorExistente.get().getId().equals(fornecedorId)) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com o CNPJ informado.");
        }
    }
}
