package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.mapper.FornecedorMapper;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;

    public FornecedorService(FornecedorRepository fornecedorRepository, FornecedorMapper fornecedorMapper) {
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorMapper = fornecedorMapper;
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
        Fornecedor fornecedor = fornecedorMapper.toEntity(fornecedorCreateDTO);
        Fornecedor fornecedorSalvo = fornecedorRepository.save(fornecedor);
        return fornecedorMapper.toDTO(fornecedorSalvo);
    }

    @Transactional
    public FornecedorDTO atualizar(Long id, FornecedorUpdateDTO fornecedorUpdateDTO) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));

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
        fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));
        fornecedorRepository.deleteById(id);
    }
}
