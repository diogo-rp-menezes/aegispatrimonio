package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.model.Filial;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequestScope
public class UserContextService {

    private final CurrentUserProvider currentUserProvider;
    private final FuncionarioRepository funcionarioRepository;
    private final IPermissionService permissionService;

    private Funcionario cachedFuncionario;

    public UserContextService(CurrentUserProvider currentUserProvider, FuncionarioRepository funcionarioRepository,
            IPermissionService permissionService) {
        this.currentUserProvider = currentUserProvider;
        this.funcionarioRepository = funcionarioRepository;
        this.permissionService = permissionService;
    }

    public Usuario getCurrentUser() {
        return currentUserProvider.getCurrentUsuario();
    }

    public boolean isAdmin() {
        Usuario usuario = getCurrentUser();
        return permissionService.hasRole(usuario.getEmail(), "ROLE_ADMIN");
    }

    @Transactional(readOnly = true)
    public Funcionario getCurrentFuncionario() {
        if (cachedFuncionario != null) {
            return cachedFuncionario;
        }

        Usuario usuario = getCurrentUser();
        Funcionario funcionarioPrincipal = usuario.getFuncionario();

        if (funcionarioPrincipal == null || funcionarioPrincipal.getId() == null) {
            throw new AccessDeniedException("Usuário não é um funcionário ou não está associado a nenhuma filial.");
        }

        // Fetch fresh from DB to get Lazy collections
        cachedFuncionario = funcionarioRepository.findByIdWithFiliais(funcionarioPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("Funcionário não encontrado."));

        return cachedFuncionario;
    }

    @Transactional(readOnly = true)
    public Set<Long> getUserFiliais() {
        Funcionario funcionario = getCurrentFuncionario();
        Set<Long> filiais = funcionario.getFiliais().stream()
                .map(Filial::getId)
                .collect(Collectors.toSet());

        if (filiais.isEmpty()) {
            throw new AccessDeniedException("Usuário não está associado a nenhuma filial.");
        }
        return filiais;
    }
}
