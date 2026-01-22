package br.com.aegispatrimonio.service.policy;

import br.com.aegispatrimonio.model.Ativo;
import br.com.aegispatrimonio.model.Funcionario;
import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.FuncionarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultHealthCheckAuthorizationPolicy implements HealthCheckAuthorizationPolicy {

    private final FuncionarioRepository funcionarioRepository;

    public DefaultHealthCheckAuthorizationPolicy(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public void assertCanUpdate(Usuario usuario, Ativo ativo) {
        if (isAdmin(usuario)) {
            return;
        }

        Funcionario funcionarioLogado = usuario.getFuncionario();
        if (funcionarioLogado == null) {
            throw new AccessDeniedException("Usuário não está associado a um funcionário.");
        }

        // Recarregar o funcionário para garantir que as filiais estão atualizadas (evitar LazyInitializationException)
        Optional<Funcionario> optionalFuncionario = funcionarioRepository.findById(funcionarioLogado.getId());
        if (optionalFuncionario.isEmpty()) {
            throw new AccessDeniedException("Funcionário associado ao usuário não foi encontrado no sistema.");
        }
        funcionarioLogado = optionalFuncionario.get();

        if (funcionarioLogado.getFiliais() == null || funcionarioLogado.getFiliais().isEmpty()) {
            throw new AccessDeniedException("Usuário não está associado a nenhuma filial.");
        }

        boolean temAcesso = funcionarioLogado.getFiliais().stream()
                .anyMatch(f -> f.getId().equals(ativo.getFilial().getId()));

        if (!temAcesso) {
            throw new AccessDeniedException("Você não tem permissão para acessar/modificar este ativo.");
        }
    }

    private boolean isAdmin(Usuario usuario) {
        return "ROLE_ADMIN".equals(usuario.getRole());
    }
}
