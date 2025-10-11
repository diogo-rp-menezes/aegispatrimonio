package br.com.aegispatrimonio.security;

import br.com.aegispatrimonio.model.Usuario;
import br.com.aegispatrimonio.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Serviço para carregar os detalhes de um usuário para o Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Localiza um usuário pelo seu email (que é usado como username no sistema).
     * @param email O email do usuário a ser carregado.
     * @return um objeto UserDetails que o Spring Security pode usar para autenticação e autorização.
     * @throws UsernameNotFoundException se o usuário não for encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // CORREÇÃO: Busca na entidade Usuario em vez de Pessoa
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));

        // O CustomUserDetails precisará ser ajustado para receber um objeto Usuario
        return new CustomUserDetails(usuario);
    }
}
