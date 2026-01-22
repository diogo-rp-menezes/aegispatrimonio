package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FornecedorCreateDTO;
import br.com.aegispatrimonio.dto.FornecedorDTO;
import br.com.aegispatrimonio.dto.FornecedorUpdateDTO;
import br.com.aegispatrimonio.mapper.FornecedorMapper;
import br.com.aegispatrimonio.model.Fornecedor;
import br.com.aegispatrimonio.model.StatusFornecedor;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.AtivoRepository;
import br.com.aegispatrimonio.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FornecedorService {

    private static final Logger logger = LoggerFactory.getLogger(FornecedorService.class);

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;
    private final AtivoRepository ativoRepository;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider

    public FornecedorService(FornecedorRepository fornecedorRepository, FornecedorMapper fornecedorMapper, AtivoRepository ativoRepository, CurrentUserProvider currentUserProvider) {
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorMapper = fornecedorMapper;
        this.ativoRepository = ativoRepository;
        this.currentUserProvider = currentUserProvider;
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

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        String cnpjMasked = maskCnpj(fornecedorSalvo.getCnpj());
        logger.info("AUDIT: Usuário {} criou o fornecedor com ID {} e nome {}. CNPJ: {}.", auditor.getEmail(), fornecedorSalvo.getId(), fornecedorSalvo.getNome(), cnpjMasked);

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

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        String cnpjMasked = maskCnpj(fornecedorAtualizado.getCnpj());
        logger.info("AUDIT: Usuário {} atualizou o fornecedor com ID {} e nome {}. CNPJ: {}.", auditor.getEmail(), fornecedorAtualizado.getId(), fornecedorAtualizado.getNome(), cnpjMasked);

        return fornecedorMapper.toDTO(fornecedorAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com ID: " + id));

        if (ativoRepository.existsByFornecedorId(id)) {
            throw new IllegalStateException("Não é possível deletar o fornecedor, pois existem ativos associados a ele.");
        }

        fornecedor.setStatus(StatusFornecedor.INATIVO); // Realiza o soft delete
        fornecedorRepository.save(fornecedor); // Salva a alteração de status

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        String cnpjMasked = maskCnpj(fornecedor.getCnpj());
        logger.info("AUDIT: Usuário {} deletou (soft delete) o fornecedor com ID {} e nome {}. CNPJ: {}.", auditor.getEmail(), id, fornecedor.getNome(), cnpjMasked);
    }

    private void validarCnpjUnico(String cnpj, Long fornecedorId) {
        Optional<Fornecedor> fornecedorExistente = fornecedorRepository.findByCnpj(cnpj);
        if (fornecedorExistente.isPresent() && !fornecedorExistente.get().getId().equals(fornecedorId)) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com o CNPJ informado.");
        }
    }

    private String maskCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() < 4) {
            return "****";
        }
        return "****" + cnpj.substring(cnpj.length() - 4);
    }
}
