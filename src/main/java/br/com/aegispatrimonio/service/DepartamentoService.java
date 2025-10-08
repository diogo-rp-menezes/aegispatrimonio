package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.mapper.DepartamentoMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final DepartamentoMapper departamentoMapper;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper, FilialRepository filialRepository, PessoaRepository pessoaRepository) {
        this.departamentoRepository = departamentoRepository;
        this.departamentoMapper = departamentoMapper;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
    }

    private Pessoa getPessoaLogada() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getPessoa();
    }

    private boolean isAdmin(Pessoa pessoa) {
        return "ROLE_ADMIN".equals(pessoa.getRole());
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarTodos() {
        Pessoa pessoaLogada = getPessoaLogada();
        if (isAdmin(pessoaLogada)) {
            return departamentoRepository.findAll().stream().map(departamentoMapper::toDTO).collect(Collectors.toList());
        }
        Long filialId = pessoaLogada.getFilial().getId();
        return departamentoRepository.findByFilialId(filialId).stream().map(departamentoMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartamentoDTO buscarPorId(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada) && !departamento.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar departamentos de outra filial.");
        }

        return departamentoMapper.toDTO(departamento);
    }

    @Transactional
    public DepartamentoDTO criar(DepartamentoCreateDTO departamentoCreateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();

        if (!isAdmin(pessoaLogada) && !departamentoCreateDTO.filialId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você só pode criar departamentos para a sua própria filial.");
        }

        Filial filial = filialRepository.findById(departamentoCreateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + departamentoCreateDTO.filialId()));

        validarNomeUnicoPorFilial(departamentoCreateDTO.nome(), filial.getId(), null);

        Departamento departamento = departamentoMapper.toEntity(departamentoCreateDTO);
        departamento.setFilial(filial);

        Departamento departamentoSalvo = departamentoRepository.save(departamento);
        return departamentoMapper.toDTO(departamentoSalvo);
    }

    @Transactional
    public DepartamentoDTO atualizar(Long id, DepartamentoUpdateDTO departamentoUpdateDTO) {
        Pessoa pessoaLogada = getPessoaLogada();
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada)) {
            if (!departamento.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
                throw new AccessDeniedException("Você não tem permissão para editar departamentos de outra filial.");
            }
            if (departamentoUpdateDTO.filialId() != null && !departamento.getFilial().getId().equals(departamentoUpdateDTO.filialId())) {
                throw new AccessDeniedException("Você não tem permissão para transferir departamentos entre filiais.");
            }
        }

        Filial filial = departamento.getFilial();
        if (departamentoUpdateDTO.filialId() != null) {
            filial = filialRepository.findById(departamentoUpdateDTO.filialId())
                    .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + departamentoUpdateDTO.filialId()));
        }

        validarNomeUnicoPorFilial(departamentoUpdateDTO.nome(), filial.getId(), id);

        departamento.setNome(departamentoUpdateDTO.nome());
        departamento.setFilial(filial);

        Departamento departamentoAtualizado = departamentoRepository.save(departamento);
        return departamentoMapper.toDTO(departamentoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Pessoa pessoaLogada = getPessoaLogada();
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));

        if (!isAdmin(pessoaLogada) && !departamento.getFilial().getId().equals(pessoaLogada.getFilial().getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar departamentos de outra filial.");
        }

        if (pessoaRepository.existsByDepartamentoId(id)) {
            throw new IllegalStateException("Não é possível deletar o departamento, pois existem pessoas associadas a ele.");
        }

        departamentoRepository.delete(departamento);
    }

    private void validarNomeUnicoPorFilial(String nome, Long filialId, Long departamentoId) {
        Optional<Departamento> deptoExistente = departamentoRepository.findByNomeAndFilialId(nome, filialId);

        if (deptoExistente.isPresent() && !deptoExistente.get().getId().equals(departamentoId)) {
            throw new IllegalArgumentException("Já existe um departamento com este nome nesta filial.");
        }
    }
}
