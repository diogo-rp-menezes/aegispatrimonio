package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.DepartamentoCreateDTO;
import br.com.aegispatrimonio.dto.DepartamentoDTO;
import br.com.aegispatrimonio.dto.DepartamentoUpdateDTO;
import br.com.aegispatrimonio.mapper.DepartamentoMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final DepartamentoMapper departamentoMapper;
    private final FilialRepository filialRepository;
    private final FuncionarioRepository funcionarioRepository; // CORREÇÃO

    public DepartamentoService(DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper, FilialRepository filialRepository, FuncionarioRepository funcionarioRepository) {
        this.departamentoRepository = departamentoRepository;
        this.departamentoMapper = departamentoMapper;
        this.filialRepository = filialRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    private Usuario getUsuarioLogado() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsuario();
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarTodos() {
        Usuario usuarioLogado = getUsuarioLogado();
        if (isAdmin(usuarioLogado)) {
            return departamentoRepository.findAll().stream().map(departamentoMapper::toDTO).collect(Collectors.toList());
        }

        Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
        if (funcionarioLogado == null || funcionarioLogado.getFiliais().isEmpty()) {
            throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
        }

        Set<Long> filiaisIds = funcionarioLogado.getFiliais().stream().map(Filial::getId).collect(Collectors.toSet());
        return departamentoRepository.findByFilialIdIn(filiaisIds).stream().map(departamentoMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartamentoDTO buscarPorId(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));

        if (!isAdmin(usuarioLogado)) {
            Funcionario funcionarioLogado = usuarioLogado.getFuncionario();
            if (funcionarioLogado == null || funcionarioLogado.getFiliais().stream().noneMatch(f -> f.getId().equals(departamento.getFilial().getId()))) {
                throw new AccessDeniedException("Você não tem permissão para acessar departamentos de outra filial.");
            }
        }

        return departamentoMapper.toDTO(departamento);
    }

    @Transactional
    public DepartamentoDTO criar(DepartamentoCreateDTO departamentoCreateDTO) {
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
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));

        Filial filial = filialRepository.findById(departamentoUpdateDTO.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada com ID: " + departamentoUpdateDTO.filialId()));

        validarNomeUnicoPorFilial(departamentoUpdateDTO.nome(), filial.getId(), id);

        departamento.setNome(departamentoUpdateDTO.nome());
        departamento.setFilial(filial);

        Departamento departamentoAtualizado = departamentoRepository.save(departamento);
        return departamentoMapper.toDTO(departamentoAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Departamento não encontrado com ID: " + id);
        }

        if (funcionarioRepository.existsByDepartamentoId(id)) { // CORREÇÃO
            throw new IllegalStateException("Não é possível deletar o departamento, pois existem funcionários associados a ele.");
        }

        departamentoRepository.deleteById(id);
    }

    private void validarNomeUnicoPorFilial(String nome, Long filialId, Long departamentoId) {
        Optional<Departamento> deptoExistente = departamentoRepository.findByNomeAndFilialId(nome, filialId);

        if (deptoExistente.isPresent() && !deptoExistente.get().getId().equals(departamentoId)) {
            throw new IllegalArgumentException("Já existe um departamento com este nome nesta filial.");
        }
    }
}
