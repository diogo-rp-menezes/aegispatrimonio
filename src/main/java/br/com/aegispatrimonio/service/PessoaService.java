package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.PessoaCreateDTO;
import br.com.aegispatrimonio.dto.PessoaDTO;
import br.com.aegispatrimonio.dto.PessoaUpdateDTO;
import br.com.aegispatrimonio.mapper.PessoaMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Pessoa;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.PessoaRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final PessoaMapper pessoaMapper;
    private final DepartamentoRepository departamentoRepository;
    private final FilialRepository filialRepository;
    private final PasswordEncoder passwordEncoder;

    public PessoaService(PessoaRepository pessoaRepository, PessoaMapper pessoaMapper, DepartamentoRepository departamentoRepository, FilialRepository filialRepository, PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.pessoaMapper = pessoaMapper;
        this.departamentoRepository = departamentoRepository;
        this.filialRepository = filialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Pessoa getPessoaLogada() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getPessoa();
    }

    private boolean isAdmin(Pessoa pessoa) {
        return "ROLE_ADMIN".equals(pessoa.getRole());
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO> listarTodos() {
        Pessoa pessoaLogada = getPessoaLogada();
        if (isAdmin(pessoaLogada)) {
            return pessoaRepository.findAll().stream().map(pessoaMapper::toDTO).collect(Collectors.toList());
        }
        Long filialId = pessoaLogada.getFilial().getId();
        return pessoaRepository.findByFilialId(filialId).stream().map(pessoaMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PessoaDTO buscarPorId(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada) && !pessoa.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar usuários de outra filial.");
        }

        return pessoaMapper.toDTO(pessoa);
    }

    @Transactional
    public PessoaDTO criar(PessoaCreateDTO pessoaCreateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();

        if (!isAdmin(pessoaLogada) && !pessoaCreateDTO.filialId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você só pode criar usuários para a sua própria filial.");
        }

        Pessoa pessoa = pessoaMapper.toEntity(pessoaCreateDTO);

        Filial filial = filialRepository.findById(pessoaCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + pessoaCreateDTO.filialId()));
        pessoa.setFilial(filial);

        Departamento departamento = departamentoRepository.findById(pessoaCreateDTO.departamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + pessoaCreateDTO.departamentoId()));
        pessoa.setDepartamento(departamento);

        pessoa.setPassword(passwordEncoder.encode(pessoaCreateDTO.password()));
        pessoa.setRole(pessoaCreateDTO.role());

        Pessoa pessoaSalva = pessoaRepository.save(pessoa);
        return pessoaMapper.toDTO(pessoaSalva);
    }

    @Transactional
    public PessoaDTO atualizar(Long id, PessoaUpdateDTO pessoaUpdateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada)) {
            if (!pessoa.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para editar usuários de outra filial.");
            }
            if (!pessoa.getFilial().getId().equals(pessoaUpdateDTO.filialId())) {
                throw new AccessDeniedException("Você não tem permissão para transferir usuários entre filiais.");
            }
        }

        pessoa.setNome(pessoaUpdateDTO.nome());
        pessoa.setMatricula(pessoaUpdateDTO.matricula());
        pessoa.setCargo(pessoaUpdateDTO.cargo());
        pessoa.setEmail(pessoaUpdateDTO.email());
        pessoa.setStatus(pessoaUpdateDTO.status());
        pessoa.setRole(pessoaUpdateDTO.role());

        if (pessoaUpdateDTO.password() != null && !pessoaUpdateDTO.password().isEmpty()) {
            pessoa.setPassword(passwordEncoder.encode(pessoaUpdateDTO.password()));
        }

        if (pessoaUpdateDTO.filialId() != null) {
            Filial filial = filialRepository.findById(pessoaUpdateDTO.filialId())
                    .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + pessoaUpdateDTO.filialId()));
            pessoa.setFilial(filial);
        }

        if (pessoaUpdateDTO.departamentoId() != null) {
            Departamento departamento = departamentoRepository.findById(pessoaUpdateDTO.departamentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + pessoaUpdateDTO.departamentoId()));
            pessoa.setDepartamento(departamento);
        }

        Pessoa pessoaAtualizada = pessoaRepository.save(pessoa);
        return pessoaMapper.toDTO(pessoaAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada com ID: " + id));

        if (!isAdmin(pessoaLogada) && !pessoa.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar usuários de outra filial.");
        }

        pessoaRepository.deleteById(id);
    }
}
