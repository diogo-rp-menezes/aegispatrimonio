package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.dto.FuncionarioCreateDTO;
import br.com.aegispatrimonio.dto.FuncionarioDTO;
import br.com.aegispatrimonio.dto.FuncionarioUpdateDTO;
import br.com.aegispatrimonio.mapper.FuncionarioMapper;
import br.com.aegispatrimonio.model.Departamento;
import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.DepartamentoRepository;
import br.com.aegispatrimonio.repository.FilialRepository;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FuncionarioService {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioService.class);

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final DepartamentoRepository departamentoRepository;
    private final FilialRepository filialRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider; // Injetando CurrentUserProvider

    public FuncionarioService(FuncionarioRepository funcionarioRepository, UsuarioRepository usuarioRepository, FuncionarioMapper funcionarioMapper, DepartamentoRepository departamentoRepository, FilialRepository filialRepository, PasswordEncoder passwordEncoder, CurrentUserProvider currentUserProvider) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.departamentoRepository = departamentoRepository;
        this.filialRepository = filialRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<FuncionarioDTO> listarTodos() {
        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        return funcionarios.stream()
                .map(funcionarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FuncionarioDTO buscarPorId(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + id));
        return funcionarioMapper.toDTO(funcionario);
    }

    @Transactional
    public FuncionarioDTO criar(FuncionarioCreateDTO createDTO) {
        // Valida se o email já está em uso
        usuarioRepository.findByEmail(createDTO.email()).ifPresent(u -> {
            throw new IllegalArgumentException("O email fornecido já está em uso.");
        });

        // Busca as entidades relacionadas
        Departamento departamento = departamentoRepository.findById(createDTO.departamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + createDTO.departamentoId()));

        Set<Filial> filiais = new HashSet<>(filialRepository.findAllById(createDTO.filiaisIds()));
        if (filiais.size() != createDTO.filiaisIds().size()) {
            throw new EntityNotFoundException("Uma ou mais filiais não foram encontradas.");
        }

        // Cria e salva o Funcionário
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(createDTO.nome());
        funcionario.setMatricula(createDTO.matricula());
        funcionario.setCargo(createDTO.cargo());
        funcionario.setDepartamento(departamento);
        funcionario.setFiliais(filiais);

        // Cria e associa o Usuário
        Usuario usuario = new Usuario();
        usuario.setEmail(createDTO.email());
        usuario.setPassword(passwordEncoder.encode(createDTO.password()));
        usuario.setRole(createDTO.role());
        usuario.setFuncionario(funcionario);
        funcionario.setUsuario(usuario);

        Funcionario funcionarioSalvo = funcionarioRepository.save(funcionario);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} criou o funcionário com ID {} (email: {}).", auditor.getEmail(), funcionarioSalvo.getId(), funcionarioSalvo.getUsuario().getEmail());

        return funcionarioMapper.toDTO(funcionarioSalvo);
    }

    @Transactional
    public FuncionarioDTO atualizar(Long id, FuncionarioUpdateDTO updateDTO) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + id));

        Usuario usuario = funcionario.getUsuario();

        // Valida se o novo email já está em uso por outro usuário
        usuarioRepository.findByEmail(updateDTO.email()).ifPresent(u -> {
            if (!u.getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("O email fornecido já está em uso por outro usuário.");
            }
        });

        // Busca entidades relacionadas
        Departamento departamento = departamentoRepository.findById(updateDTO.departamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + updateDTO.departamentoId()));

        Set<Filial> filiais = new HashSet<>(filialRepository.findAllById(updateDTO.filiaisIds()));
        if (filiais.size() != updateDTO.filiaisIds().size()) {
            throw new EntityNotFoundException("Uma ou mais filiais não foram encontradas.");
        }

        // Atualiza dados do Funcionário
        funcionario.setNome(updateDTO.nome());
        funcionario.setMatricula(updateDTO.matricula());
        funcionario.setCargo(updateDTO.cargo());
        funcionario.setDepartamento(departamento);
        funcionario.setStatus(updateDTO.status());
        funcionario.setFiliais(filiais);

        // Atualiza dados do Usuário
        usuario.setEmail(updateDTO.email());
        usuario.setRole(updateDTO.role());
        if (updateDTO.password() != null && !updateDTO.password().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(updateDTO.password()));
        }

        Funcionario funcionarioAtualizado = funcionarioRepository.save(funcionario);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} atualizou o funcionário com ID {} (email: {}).", auditor.getEmail(), funcionarioAtualizado.getId(), funcionarioAtualizado.getUsuario().getEmail());

        return funcionarioMapper.toDTO(funcionarioAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + id));

        // A exclusão lógica é feita pela anotação @SQLDelete na entidade
        funcionarioRepository.deleteById(id);

        Usuario auditor = currentUserProvider.getCurrentUsuario();
        logger.info("AUDIT: Usuário {} deletou o funcionário com ID {} (email: {}).", auditor.getEmail(), id, funcionario.getUsuario().getEmail());
    }
}
